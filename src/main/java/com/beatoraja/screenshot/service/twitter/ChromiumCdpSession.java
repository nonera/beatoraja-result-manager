package com.beatoraja.screenshot.service.twitter;

import com.beatoraja.screenshot.util.AppLogging;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

final class ChromiumCdpSession implements AutoCloseable {

    private static final Logger LOG = AppLogging.get(ChromiumCdpSession.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final AtomicInteger nextId = new AtomicInteger(1);
    private final ConcurrentHashMap<Integer, CompletableFuture<JsonNode>> pending = new ConcurrentHashMap<>();
    private WebSocket webSocket;

    static ChromiumCdpSession connect(HttpClient httpClient, String webSocketUrl, Duration timeout) throws Exception {
        ChromiumCdpSession session = new ChromiumCdpSession();
        session.open(httpClient, webSocketUrl, timeout);
        return session;
    }

    private void open(HttpClient httpClient, String webSocketUrl, Duration timeout) throws Exception {
        CompletableFuture<Void> openFuture = new CompletableFuture<>();
        AtomicReference<WebSocket> ref = new AtomicReference<>();

        httpClient.newWebSocketBuilder()
                .connectTimeout(timeout)
                .buildAsync(URI.create(webSocketUrl), new WebSocket.Listener() {
                    private final StringBuilder buffer = new StringBuilder();

                    @Override
                    public void onOpen(WebSocket webSocket) {
                        ref.set(webSocket);
                        ChromiumCdpSession.this.webSocket = webSocket;
                        openFuture.complete(null);
                        webSocket.request(1);
                    }

                    @Override
                    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                        buffer.append(data);
                        if (!last) {
                            webSocket.request(1);
                            return null;
                        }
                        try {
                            JsonNode node = MAPPER.readTree(buffer.toString());
                            buffer.setLength(0);
                            if (node.has("id")) {
                                CompletableFuture<JsonNode> future = pending.remove(node.path("id").asInt());
                                if (future != null) {
                                    if (node.has("error")) {
                                        future.completeExceptionally(new IOException(node.path("error").toString()));
                                    } else {
                                        future.complete(node.path("result"));
                                    }
                                }
                            }
                        } catch (Exception e) {
                            LOG.log(Level.FINE, "Failed to parse CDP WebSocket message", e);
                        }
                        webSocket.request(1);
                        return null;
                    }

                    @Override
                    public void onError(WebSocket webSocket, Throwable error) {
                        openFuture.completeExceptionally(error);
                        pending.values().forEach(f -> f.completeExceptionally(error));
                        pending.clear();
                    }
                })
                .join();

        openFuture.get(timeout.toSeconds(), TimeUnit.SECONDS);
        if (webSocket == null) {
            throw new IOException("CDP WebSocket に接続できませんでした");
        }
    }

    JsonNode send(String method, Map<String, Object> params, Duration timeout) throws Exception {
        int id = nextId.getAndIncrement();
        CompletableFuture<JsonNode> future = new CompletableFuture<>();
        pending.put(id, future);
        String body = MAPPER.writeValueAsString(Map.of("id", id, "method", method, "params", params));
        webSocket.sendText(body, true);
        try {
            return future.get(timeout.toSeconds(), TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            pending.remove(id);
            throw new IOException(method + " の CDP 応答がタイムアウトしました", e);
        }
    }

    @Override
    public void close() {
        pending.values().forEach(f -> f.completeExceptionally(new IOException("CDP セッションを終了しました")));
        pending.clear();
        if (webSocket != null && !webSocket.isOutputClosed()) {
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "done");
        }
    }
}
