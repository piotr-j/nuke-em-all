package io.piotrjastrzebski.gdxjam.nta.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.Random;

/**
 * Simple wrapper for firebase functions
 */
@Slf4j
public class Online {
    protected final String basePath;
    protected final Json json;
    protected final OkHttpClient client;
    protected final String hostId;
    protected boolean isHosting;

    public Online (String basePath) {
        this.basePath = basePath;
        json = new Json();

        // GDX net client doesnt like PATCH and we kinda want it, so we use OkHttp
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        // any config we need?
        client = builder.build();

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
                log.warn("Hosting! {}", data);
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
                log.warn("Stopped hosting! {}", data);
            }

            @Override
            public void failed () {
                log.warn("Failed to stop hosting!");
            }
        });
    }

    public void hosts (HostsListener listener) {
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

    public void dispose () {
        cancelHost();
        // other stuff
    }

    protected void get (String path, OnResponse onResponse) {
        request(Net.HttpMethods.GET, path, null, onResponse);
    }

    protected void replace (String path, String content, OnResponse onResponse) {
        request(Net.HttpMethods.PUT, path, content, onResponse);
    }

    protected void update (String path, String content, OnResponse onResponse) {
        request(Net.HttpMethods.PATCH, path, content, onResponse);
    }

    protected void add (String path, String content, OnResponse onResponse) {
        request(Net.HttpMethods.POST, path, content, onResponse);
    }

    protected void delete (String path, OnResponse onResponse) {
        request(Net.HttpMethods.DELETE, path, null, onResponse);
    }

    private void request (String method, String path, String content, OnResponse onResponse) {
        RequestBody body = null;
        if (content != null) {
            body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), content);
        }
        Request.Builder rb = new Request.Builder().url(basePath + path);
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

    public interface HostsListener {
        void onResult (Array<Host> hosts);
    }

    private interface OnResponse {
        void success (String data);
        void failed ();
    }

    @AllArgsConstructor
    @ToString
    public static class Host {
        public String id;
        public long timestamp;
    }
}
