package com.beatoraja.screenshot.service.twitter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChromiumDevToolsClient implements AutoCloseable {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final List<String> COOKIE_URLS = List.of("https://x.com", "https://twitter.com");
    private static final Duration HTTP_TIMEOUT = Duration.ofSeconds(8);
    private static final Duration COMMAND_TIMEOUT = Duration.ofSeconds(15);

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final int debugPort;

    public ChromiumDevToolsClient(int debugPort) {
        this.debugPort = debugPort;
    }

    public static int waitForDebugPort(Path userDataDir, Duration timeout) throws IOException, InterruptedException {
        Path portFile = userDataDir.resolve("DevToolsActivePort");
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        IOException lastError = null;

        while (System.currentTimeMillis() < deadline) {
            if (Files.exists(portFile)) {
                try {
                    List<String> lines = Files.readAllLines(portFile, StandardCharsets.UTF_8);
                    if (!lines.isEmpty() && !lines.get(0).isBlank()) {
                        int port = Integer.parseInt(lines.get(0).trim());
                        if (isDebugPortReady(port)) {
                            return port;
                        }
                        lastError = new IOException("デバッグポート " + port + " に接続できません");
                    }
                } catch (NumberFormatException e) {
                    lastError = new IOException("DevToolsActivePort の内容が不正です");
                }
            }
            Thread.sleep(150);
        }
        if (lastError != null) {
            throw new IOException("ブラウザのデバッグポート取得がタイムアウトしました: " + lastError.getMessage());
        }
        throw new IOException("ブラウザのデバッグポート取得がタイムアウトしました（DevToolsActivePort が作成されません）");
    }

    private static boolean isDebugPortReady(int port) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://127.0.0.1:" + port + "/json/version"))
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() < 400 && response.body().contains("webSocketDebuggerUrl");
        } catch (Exception e) {
            return false;
        }
    }

    public TwitterCookies readTwitterCookies() throws Exception {
        String webSocketUrl = findOrCreatePage("https://x.com/home");
        try (ChromiumCdpSession session = ChromiumCdpSession.connect(httpClient, webSocketUrl, HTTP_TIMEOUT)) {
            session.send("Network.enable", Map.of(), COMMAND_TIMEOUT);
            session.send("Page.navigate", Map.of("url", "https://x.com/home"), COMMAND_TIMEOUT);
            Thread.sleep(2500);
            JsonNode result = session.send("Network.getCookies", Map.of("urls", COOKIE_URLS), COMMAND_TIMEOUT);
            return parseCookies(result);
        }
    }

    /**
     * Opens (or reuses) a tab in this debugged browser and returns a connected CDP session,
     * for callers that need to drive the page directly (e.g. UI automation).
     */
    public ChromiumCdpSession openSession(String initialUrl) throws Exception {
        String webSocketUrl = findOrCreatePage(initialUrl);
        return ChromiumCdpSession.connect(httpClient, webSocketUrl, HTTP_TIMEOUT);
    }

    private TwitterCookies parseCookies(JsonNode result) throws IOException {
        Map<String, String> values = new HashMap<>();
        for (JsonNode cookie : result.path("cookies")) {
            if (!isTwitterDomain(cookie.path("domain").asText(""))) {
                continue;
            }
            values.putIfAbsent(cookie.path("name").asText(), cookie.path("value").asText());
        }
        String authToken = values.get("auth_token");
        String ct0 = values.get("ct0");
        if ((authToken == null || authToken.isBlank()) && (ct0 == null || ct0.isBlank())) {
            throw new IOException("auth_token / ct0 が見つかりませんでした（ログイン状態が保存されていない可能性があります）");
        }
        return new TwitterCookies(values);
    }

    private static boolean isTwitterDomain(String domain) {
        if (domain == null || domain.isBlank()) {
            return false;
        }
        String normalized = domain.startsWith(".") ? domain.substring(1) : domain;
        return normalized.equals("x.com")
                || normalized.equals("twitter.com")
                || normalized.endsWith(".x.com")
                || normalized.endsWith(".twitter.com");
    }

    private String findOrCreatePage(String url) throws IOException, InterruptedException {
        HttpRequest listRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + debugPort + "/json/list"))
                .timeout(HTTP_TIMEOUT)
                .GET()
                .build();
        HttpResponse<String> listResponse = httpClient.send(listRequest, HttpResponse.BodyHandlers.ofString());
        if (listResponse.statusCode() < 400) {
            for (JsonNode target : MAPPER.readTree(listResponse.body())) {
                if (!"page".equals(target.path("type").asText(""))) {
                    continue;
                }
                String webSocketUrl = target.path("webSocketDebuggerUrl").asText(null);
                if (webSocketUrl != null && !webSocketUrl.isBlank()) {
                    return webSocketUrl;
                }
            }
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + debugPort + "/json/new?" + url))
                .timeout(HTTP_TIMEOUT)
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new IOException("CDP タブ作成に失敗しました (HTTP " + response.statusCode() + ")");
        }
        JsonNode node = MAPPER.readTree(response.body());
        String webSocketUrl = node.path("webSocketDebuggerUrl").asText(null);
        if (webSocketUrl == null || webSocketUrl.isBlank()) {
            throw new IOException("CDP WebSocket URL を取得できませんでした");
        }
        return webSocketUrl;
    }

    @Override
    public void close() {
    }
}
