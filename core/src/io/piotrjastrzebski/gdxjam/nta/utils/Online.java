package io.piotrjastrzebski.gdxjam.nta.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.utils.*;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okio.BufferedSource;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Simple wrapper for firebase functions
 */
@Slf4j
public class Online {
    protected final String basePath;
    protected final Json json;
    protected final OkHttpClient client;
    protected final OkHttpClient streamClient;
    protected final String playerId;
    protected boolean isHosting;
    protected ScheduledExecutorService ses;

    public Online (String basePath) {
        this.basePath = basePath;
        json = new Json();

        // GDX net client doesnt like PATCH and we kinda want it, so we use OkHttp
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        // any config we need?
        client = builder.build();

        // we dont want stream to timeout, setting it in call doesnt seem to quite work :/
        builder.readTimeout(0, TimeUnit.SECONDS);
        // any config we need?
        streamClient = builder.build();

        // probably no need for this to change?
        playerId = Long.toHexString(new Random().nextLong()).substring(0, 5);

        ses = Executors.newSingleThreadScheduledExecutor();

        if (true) return;
        String content = "{\"" + playerId + "\": " + System.currentTimeMillis() + "}";
        log.warn("content = {}", content);

        update("hosts.json", content, new OnResponse() {
            @Override
            public void success (String data) {
                log.debug("Got a response! {}", data);
            }

            @Override
            public void failed () {
                log.debug("Got a failure!");
            }
        });

        get("hosts.json", new OnResponse() {
            @Override
            public void success (String data) {
                log.debug("Got a response! {}", data);
            }

            @Override
            public void failed () {
                log.debug("Got a failure!");
            }
        });
    }

    ScheduledFuture<?> hostFuture;
    public void host (GameListener gameListener) {
        // in case we cancel before a reply
        isHosting = true;
        String content = "{\"" + playerId + "\": " + System.currentTimeMillis() + "}";
        update("hosts.json", content, new OnResponse() {
            @Override
            public void success (String data) {
                log.debug("Hosting! {}", data);
                join(playerId, gameListener);
                // TODO stream our game location
            }

            @Override
            public void failed () {
                isHosting = false;
                log.warn("Failed to host!");
                gameListener.fail();
            }
        });

        // keep updating so out time is fresh
        hostFuture = ses.scheduleAtFixedRate(() -> {
            String update = "{\"" + playerId + "\": " + System.currentTimeMillis() + "}";
            update("hosts.json", update, null);
        }, 30, 30, TimeUnit.SECONDS);
    }

    public String playerId (){
        return playerId;
    }

    public void cancelHost () {
        if (!isHosting) return;
        isHosting = false;
        hostFuture.cancel(false);
        delete("hosts/" + playerId + ".json", null);
        delete("games/" + playerId + ".json", null);
    }

    Call hostsCall;
    public void hosts (HostsListener listener) {
//        put -> {"path":"/","data":{"9fc4c":1585487219173,"ebd59":1585488180508}}
//        patch -> {"path":"/","data":{"d1bd5":1585488312798}}
//        put -> {"path":"/d1bd5","data":null}

        hostsCall = stream("hosts.json", new OnStream() {
            Array<Player> hosts = new Array<>();
            ObjectMap<String, Player> hostCache = new ObjectMap<>();
            @Override
            public void event (String type, String path, JsonValue data) {
                log.debug("Hosts event {} -> {} \n{}", type, path, data);
                switch (type) {
                case "put": {
                    if (path.equals("/")) {
                        // initial data
                        for (JsonValue hv : data) {
                            Player host = new Player(hv.name, hv.asLong());
                            // skip if old (ie we didnt remove it for some reason)
                            if (TimeUtils.timeSinceMillis(host.timestamp) > TimeUnit.SECONDS.toMillis(45)) {
                                continue;
                            }
                            hostCache.put(host.id, host);
                        }
                    } else {
                        // something was removed
                        String id = path.substring(1);
                        if (hostCache.remove(id) != null) {
                            log.debug("Removed host {}", id);
                        }
                    }
                } break;
                case "patch": {
                    if (path.equals("/")) {
                        // something was added
                        for (JsonValue hv : data) {
                            Player host = new Player(hv.name, hv.asLong());
                            hostCache.put(host.id, host);
                        }
                    }
                } break;
                }
                hosts.clear();
                for (Player host : hostCache.values()) {
                    hosts.add(host);
                }
                //noinspection ComparatorCombinators
                hosts.sort((o1, o2) -> Long.compare(o1.timestamp, o2.timestamp));

                listener.onResult(hosts);
            }

            @Override
            public void failed () {
                log.info("Hosts failed");
                listener.onResult(new Array<>());
            }
        });


        if (true) return;
        get("hosts.json", new OnResponse() {
            @Override
            public void success (String data) {
                Array<Player> hosts = new Array<>();
                if (data != null) {
                    JsonReader reader = new JsonReader();
                    for (JsonValue value : reader.parse(data)) {
                        Player host = new Player(value.name, value.asLong());
                        log.warn("{}", host);
                        hosts.add(host);
                    }

                    //noinspection ComparatorCombinators
                    hosts.sort((o1, o2) -> Long.compare(o1.timestamp, o2.timestamp));
                }
                listener.onResult(hosts);
            }

            @Override
            public void failed () {
                listener.onResult(new Array<>());
            }
        });
    }

    public void cancelHosts () {
        if (hostsCall != null) {
            hostsCall.cancel();
            hostsCall = null;
        }
    }

    public void dispose () {
        cancelHost();
        ses.shutdown();
        // other stuff
    }

    protected void get (String path, OnResponse onResponse) {
        request(Net.HttpMethods.GET, path, null, false, onResponse);
    }

    protected void replace (String path, String content, OnResponse onResponse) {
        request(Net.HttpMethods.PUT, path, content, true, onResponse);
    }

    protected void update (String path, String content, OnResponse onResponse) {
        request(Net.HttpMethods.PATCH, path, content, true, onResponse);
    }

    protected void add (String path, String content, OnResponse onResponse) {
        request(Net.HttpMethods.POST, path, content, false, onResponse);
    }

    protected void delete (String path, OnResponse onResponse) {
        request(Net.HttpMethods.DELETE, path, null, true, onResponse);
    }

    private void request (String method, String path, String content, boolean silent, OnResponse onResponse) {
        RequestBody body = null;
        if (content != null) {
            body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), content);
        }
        // probably can add params in easier way...
        String url = basePath + path;
        if (silent) {
            // we use less bandwidth if we use this param
            url += "?print=silent";
        }
        Request.Builder rb = new Request.Builder().url(url);
        rb.method(method, body);

        Request request = rb.build();

        log.debug("{} -> {}", request.method(), request.url());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse (Call call, Response response) throws IOException {
                int status = response.code();
                if (status >= 200 && status < 300) {
                    // lets assume its okish?
                    ResponseBody body = response.body();
                    final String result = body != null? body.string() : null;
                    log.debug("result {}", result);
                    if (onResponse != null) Gdx.app.postRunnable(() -> onResponse.success(result));
                } else {
                    log.warn("Call to '{}' failed with status {}", request.url(), status);
                    if (onResponse != null) Gdx.app.postRunnable(onResponse::failed);
                }
            }

            @Override
            public void onFailure (Call call, IOException e) {
                log.error("Call to '{}' failed with exception", request.url(), e);
                if (onResponse != null) Gdx.app.postRunnable(onResponse::failed);
            }
        });
    }

    private Call stream(String path, OnStream listener) {
        String url = basePath + path;
        Request.Builder rb = new Request.Builder().url(url);
        // no method needed i guess
        rb.header("Accept", "text/event-stream");
        rb.header("Cache-Control", "no-cache");

        Request request = rb.build();

        log.debug("{} -> {}", request.method(), request.url());
        Call call = streamClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onResponse (Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    log.warn("call failed");
                    Gdx.app.postRunnable(listener::failed);
                    return;
                }
                log.debug("Streaming {}", path);
                ResponseBody body = response.body();
                if (body == null) return;

                BufferedSource source = body.source();

                JsonReader reader = new JsonReader();
                while (!call.isCanceled()) {
                    // we get data in format, lets assume for now that it is always 3 lines
                    // event: method
                    // data: json
                    // <empty line>
                    //
                    // patch when stuff is updated
                    //      event: patch
                    //      data: {"path":"/","data":{"7466b":1585487372104}}
                    //
                    // put when its removed, data=null
                    //      event: put
                    //      data: {"path":"/7466b","data":null}

                    try {
                        String el = source.readUtf8LineStrict();
                        if (el.isEmpty() || !el.startsWith("event: ")) {
                            log.warn("Unexpected event line '{}'", el);
                            continue;
                        }
                        String dl = source.readUtf8LineStrict();
                        if (dl.isEmpty() || !dl.startsWith("data: ")) {
                            log.warn("Unexpected data line '{}'", el);
                            continue;
                        }
                        String empty = source.readUtf8LineStrict();
                        if (!empty.isEmpty()) {
                            log.warn("Unexpected empty line {}", empty);
                            continue;
                        }
                        String event = el.substring("event: ".length());
                        String data = dl.substring("data: ".length());
                        log.debug("{} -> {}", event, data);
                        // keep alive probably
                        if ("null".equals(data)) {
                            continue;
                        }
                        JsonValue parse = reader.parse(data);
                        String path = parse.get("path").asString();
                        JsonValue value = parse.get("data");
                        Gdx.app.postRunnable(() -> listener.event(event, path, value));
                    } catch (IOException ex) {
                        log.error("welp", ex);
                        break;
                    }
                }
                log.info("done!");
            }

            @Override
            public void onFailure (Call call, IOException e) {
                log.error("Call to '{}' failed with exception", request.url(), e);
                Gdx.app.postRunnable(listener::failed);
            }
        });
        return call;
    }

    Call gameCall;
    String gameHostId;
    GameState state;
    public void join (String hostId, GameListener listener) {
        // could use ETag stuff to verify we dont join same thing twice or whatever
        if (gameCall != null) {
            gameCall.cancel();
        }

        state = new GameState(this, listener);
        String path = "games/" + hostId + ".json";
        String joinContent = "{" +
            "\"player\":" + "\"" + playerId + "\"," +
            "\"action\":" + "\"join\"," +
            "\"ts\":" + System.currentTimeMillis() +
            "}";
        add(path, joinContent, new OnResponse() {
            @Override
            public void success (String data) {
                gameHostId = hostId;
                log.debug("Joined! {}", data);
                gameCall = stream(path, state);
            }

            @Override
            public void failed () {
                log.warn("join failed");
            }
        });
    }

    static class GameState implements OnStream {
        private Online online;
        private GameListener listener;
        private long startTime;
        String p1;
        long p1join;
        boolean p1Start;
        String p2;
        long p2join;
        boolean p2Start;
        boolean started;

        public GameState (Online online, GameListener listener) {
            this.online = online;
            this.listener = listener;
            startTime = System.currentTimeMillis();
        }

        @Override
        public void event (String type, String path, JsonValue data) {
            // ignore events before start time just in case
            // when we host we get
            // game event put -> '/'
//            data: {
//                -M3b-PdJg-17b0l3BTit: {
//                    action: join
//                    player: c1d31
//                    ts: 1585497351875
//                }
//            }

//            game event put -> '/-M3b-QZjYt_qLNpUfC2d',
//                data: {
//                action: join
//                player: 5240d
//                ts: 1585497355234
//            }

            // when we join we get
//            put -> '/',
//                data: {
//                -M3b-PdJg-17b0l3BTit: {
//                    action: join
//                    player: c1d31
//                    ts: 1585497351875
//                }
//                -M3b-QZjYt_qLNpUfC2d: {
//                    action: join
//                    player: 5240d
//                    ts: 1585497355234
//                }
//            }

            log.debug("game event {} -> '{}', \n{}", type, path, data);
            switch (type) {
            case "put": {
                if (path.equals("/")) {
                    if (data.isNull()) {
                        // game removed
                        listener.end();
                    } else {
                        // new game
                        for (JsonValue value : data) {
                            process(value);
                        }
                    }
                } else {
                    // new event /id
                    process(data);
                }
            } break;
            }
        }

        ScheduledFuture<?> cancelGame;
        private void process (JsonValue value) {
            String player = value.getString("player");
            String action = value.getString("action");
            // could use ts to correct for lag a bit
            long ts = value.getLong("ts");

            switch (action) {
            case "join": {
                if (p1 == null) {
                    p1 = player;
                    p1join = ts;
                    log.info("{} joined as p1", player);
                } else if (p2 == null) {
                    p2 = player;
                    p2join = ts;
                    log.info("{} joined as p2", player);
                } else {
                    log.warn("{} joined but slots are filed", player);
                }
                if (p1 != null && p2 != null) {
                    // when we have two players try to start
                    online.start();
                    // if we dont start just end
                    cancelGame = online.ses.schedule(() -> {
                        listener.end();
                    }, 5, TimeUnit.SECONDS);
                }
            } break;
            case "start": {
                // start game when both players want to start
                if (p1 != null && p1.equals(player) && !p1Start) {
                    // we dont want some old action to start
                    p1Start = ts > startTime;
                    // if we didnt start, send event
                    if (p1Start && !p1.equals(online.playerId) && !p2Start) {
                        online.start();
                    }
                } else if (p2 != null && p2.equals(player) && !p2Start) {
                    // we dont want some old action to start
                    p2Start = ts > startTime;
                    // if we didnt start, send event
                    if (p2Start && !p2.equals(online.playerId) && !p1Start) {
                        online.start();
                    }
                }
                if (p1Start && p2Start && !started) {
                    if (cancelGame != null) {
                        cancelGame.cancel(false);
                        cancelGame = null;
                    }
                    started = true;
                    listener.start(p1, p2, p1join);
                }
            } break;
            case "nuke": {
                int siloId = value.getInt("silo");
                float tx = value.getFloat("x");
                float ty = value.getFloat("y");
                log.info("{}s silo#{} nukes {}, {}", player, siloId, tx, ty);
                listener.nuke(player, siloId, tx, ty);
            } break;
            case "leave": {
                log.info("{} left ", player);
                listener.end();
            } break;
            }
        }

        @Override
        public void failed () {

        }
    }

    void start () {
        if (gameHostId == null) {
            return;
        }
        String path = "games/" + gameHostId + ".json";
        String content = "{" +
            "\"player\":" + "\"" + playerId + "\"," +
            "\"action\":" + "\"start\"," +
            "\"ts\":" + System.currentTimeMillis() +
            "}";
        add(path, content, null);
    }

    public void launchNuke (int siloId, float x, float y) {
        if (gameHostId == null) return;
        String path = "games/" + gameHostId + ".json";
        String launchContent = "{" +
            "\"player\":" + "\"" + playerId + "\"," +
            "\"action\":" + "\"nuke\"," +
            "\"ts\":" + System.currentTimeMillis() + "," +
            "\"silo\":" + siloId + "," +
            "\"x\":" + x + "," +
            "\"y\":" + y +
            "}";
        add(path, launchContent, null);
    }

    public void leave () {
        if (gameHostId == null) {
            return;
        }
        if (gameCall != null) {
            gameCall.cancel();
        }
        String path = "games/" + gameHostId + ".json";
        String leaveContent = "{" +
            "\"player\":" + "\"" + playerId + "\"," +
            "\"action\":" + "\"leave\"," +
            "\"ts\":" + System.currentTimeMillis() +
            "}";
        add(path, leaveContent, new OnResponse() {
            @Override
            public void success (String data) {
                log.debug("Left! {}", data);
            }

            @Override
            public void failed () {
                isHosting = false;
                log.warn("Failed to leave!");
            }
        });
        gameHostId = null;
    }

    public interface HostsListener {
        void onResult (Array<Player> hosts);
    }

    private interface OnResponse {
        void success (String data);
        void failed ();
    }

    private interface OnStream {
        void event (String type, String path, JsonValue data);
        void failed ();
    }

    public interface GameListener {
        void start (String host, String other, long seed);

        void nuke (String player, int silo, float x, float y);

        void end ();

        void fail ();
    }

    @AllArgsConstructor
    @ToString
    public static class Player {
        public String id;
        public long timestamp;
    }
}
