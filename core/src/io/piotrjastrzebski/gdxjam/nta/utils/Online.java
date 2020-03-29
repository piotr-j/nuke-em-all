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
    protected final String hostId;
    protected boolean isHosting;

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
        hostId = Long.toHexString(new Random().nextLong()).substring(0, 5);
        if (true) return;
        String content = "{\"" + hostId + "\": " + System.currentTimeMillis() + "}";
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

    public String host () {
        // in case we cancel before a reply
        isHosting = true;
        String content = "{\"" + hostId + "\": " + System.currentTimeMillis() + "}";
        update("hosts.json", content, new OnResponse() {
            @Override
            public void success (String data) {
                log.debug("Hosting! {}", data);
                // TODO stream our game location
            }

            @Override
            public void failed () {
                isHosting = false;
                log.warn("Failed to host!");
            }
        });
        return hostId;
    }

    public void cancelHost () {
        if (!isHosting) return;
        isHosting = false;
        delete("hosts/" + hostId + ".json", new OnResponse() {
            @Override
            public void success (String data) {
                log.debug("Stopped hosting! {}", data);
            }

            @Override
            public void failed () {
                log.warn("Failed to stop hosting!");
            }
        });
    }

    public void hosts (HostsListener listener) {
//        put -> {"path":"/","data":{"9fc4c":1585487219173,"ebd59":1585488180508}}
//        patch -> {"path":"/","data":{"d1bd5":1585488312798}}
//        put -> {"path":"/d1bd5","data":null}

        stream("hosts.json", new OnStream() {
            Array<Host> hosts = new Array<>();
            ObjectMap<String, Host> hostCache = new ObjectMap<>();
            JsonReader reader = new JsonReader();
            @Override
            public void event (String type, String rawData) {
                log.info("Hosts event {} -> {}", type, rawData);
                JsonValue jsonData = reader.parse(rawData);
                String path = jsonData.get("path").asString();
                JsonValue value = jsonData.get("data");

                switch (type) {
                case "put": {
                    if (path.equals("/")) {
                        // initial data
                        for (JsonValue hv : value) {
                            Host host = new Host(hv.name, hv.asLong());
                            // skip if old (ie we didnt remove it for some reason)
                            if (TimeUtils.timeSinceMillis(host.timestamp) > TimeUnit.MINUTES.toMillis(2)) {
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
                        for (JsonValue hv : value) {
                            Host host = new Host(hv.name, hv.asLong());
                            hostCache.put(host.id, host);
                        }
                    }
                } break;
                }
                hosts.clear();
                for (Host host : hostCache.values()) {
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
                Array<Host> hosts = new Array<>();
                if (data != null) {
                    JsonReader reader = new JsonReader();
                    for (JsonValue value : reader.parse(data)) {
                        Host host = new Host(value.name, value.asLong());
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
        cancelStream();
    }

    public void dispose () {
        cancelHost();
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
                    Gdx.app.postRunnable(() -> onResponse.success(result));
                } else {
                    log.warn("Call to '{}' failed with status {}", request.url(), status);
                    Gdx.app.postRunnable(onResponse::failed);
                }
            }

            @Override
            public void onFailure (Call call, IOException e) {
                log.error("Call to '{}' failed with exception", request.url(), e);
                Gdx.app.postRunnable(onResponse::failed);
            }
        });
    }

    Call streamCall = null;
    private void stream(String path, OnStream listener) {
        String url = basePath + path;
        Request.Builder rb = new Request.Builder().url(url);
        // no method needed i guess
        rb.header("Accept", "text/event-stream");
        rb.header("Cache-Control", "no-cache");

        Request request = rb.build();

        log.debug("{} -> {}", request.method(), request.url());
        if (streamCall != null) {
            streamCall.cancel();
        }
        streamCall = streamClient.newCall(request);
        streamCall.enqueue(new Callback() {
            @Override
            public void onResponse (Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    log.info("call failed");
                    Gdx.app.postRunnable(listener::failed);
                    return;
                }
                log.info("Streaming {}", path);
                ResponseBody body = response.body();
                if (body == null) return;

                BufferedSource source = body.source();

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
                        Gdx.app.postRunnable(() -> listener.event(event, data));
                    } catch (IOException ex) {
                        log.info("timeout!");
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
    }

    private void cancelStream () {
        if (streamCall != null) {
            streamCall.cancel();
        }
    }

    public void join (Host host, JoinListener listener) {
        // could use ETag stuff to verify we dont join same thing twice or whatever
    }

    public interface HostsListener {
        void onResult (Array<Host> hosts);
    }

    public interface JoinListener {
        void joined ();
        void failed ();
    }

    private interface OnResponse {
        void success (String data);
        void failed ();
    }

    private interface OnStream {
        void event (String type, String data);
        void failed ();
    }

    @AllArgsConstructor
    @ToString
    public static class Host {
        public String id;
        public long timestamp;
    }
}
