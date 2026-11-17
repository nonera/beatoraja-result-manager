package com.beatoraja.screenshot.service;

import com.beatoraja.screenshot.util.AppLogging;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

public class DiscordWebhookService {

    private static final Logger LOG = AppLogging.get(DiscordWebhookService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String BOUNDARY_PREFIX = "----BeatorajaScreenshot";
    /** Discord's per-message attachment limit. */
    public static final int MAX_IMAGES_PER_POST = 10;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    public PostResult post(String webhookUrl, String text, List<Path> imagePaths) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            return PostResult.failed("Discord Webhook URL が設定されていません。");
        }
        if (imagePaths == null || imagePaths.isEmpty()) {
            return PostResult.failed("画像が選択されていません。");
        }
        if (imagePaths.size() > MAX_IMAGES_PER_POST) {
            return PostResult.failed("Discord は最大" + MAX_IMAGES_PER_POST + "枚まで送信できます。");
        }

        try {
            String boundary = BOUNDARY_PREFIX + UUID.randomUUID();
            byte[] body = buildMultipartBody(boundary, text, imagePaths);
            String url = webhookUrl.contains("?")
                    ? webhookUrl + "&wait=true"
                    : webhookUrl + "?wait=true";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMinutes(2))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return PostResult.ok(extractMessageId(response.body()), response.body());
            }
            return PostResult.failed("HTTP " + response.statusCode() + ": " + response.body());
        } catch (Exception e) {
            return PostResult.failed(e.getMessage());
        }
    }

    private byte[] buildMultipartBody(String boundary, String text, List<Path> imagePaths) throws IOException {
        String payloadJson = MAPPER.createObjectNode()
                .put("content", text == null ? "" : text)
                .toString();

        MultipartBuilder builder = new MultipartBuilder(boundary);
        builder.addJsonPart("payload_json", payloadJson);

        for (int i = 0; i < imagePaths.size(); i++) {
            Path imagePath = imagePaths.get(i);
            String fileName = imagePath.getFileName().toString();
            byte[] bytes = Files.readAllBytes(imagePath);
            builder.addFilePart("files[" + i + "]", headerSafeFileName(fileName), contentType(fileName), bytes);
        }

        return builder.build();
    }

    /** The image media type for {@code fileName}'s extension; PNG when unknown, as screenshots are. */
    static String contentType(String fileName) {
        String lower = fileName.toLowerCase(java.util.Locale.ROOT);
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (lower.endsWith(".gif")) {
            return "image/gif";
        }
        if (lower.endsWith(".webp")) {
            return "image/webp";
        }
        return "image/png";
    }

    /** {@code fileName} with what would break the quoted filename="…" header (quotes, line breaks) replaced. */
    static String headerSafeFileName(String fileName) {
        return fileName.replace("\"", "'").replace("\r", " ").replace("\n", " ");
    }

    private String extractMessageId(String body) {
        try {
            JsonNode root = MAPPER.readTree(body);
            if (root.has("id")) {
                return root.get("id").asText();
            }
        } catch (Exception e) {
            LOG.log(Level.FINE, "Failed to parse Discord webhook response JSON", e);
        }
        return "";
    }

    public record PostResult(boolean success, String messageId, String message) {
        public static PostResult ok(String messageId, String message) {
            return new PostResult(true, messageId, message);
        }

        public static PostResult failed(String message) {
            return new PostResult(false, "", message);
        }
    }

    private static final class MultipartBuilder {
        private final String boundary;
        private final StringBuilder textPart = new StringBuilder();
        private final java.io.ByteArrayOutputStream binaryPart = new java.io.ByteArrayOutputStream();

        private MultipartBuilder(String boundary) {
            this.boundary = boundary;
        }

        private void addJsonPart(String name, String json) {
            textPart.append("--").append(boundary).append("\r\n");
            textPart.append("Content-Disposition: form-data; name=\"").append(name).append("\"\r\n");
            textPart.append("Content-Type: application/json; charset=UTF-8\r\n\r\n");
            textPart.append(json).append("\r\n");
        }

        private void addFilePart(String name, String fileName, String contentType, byte[] bytes) throws IOException {
            textPart.append("--").append(boundary).append("\r\n");
            textPart.append("Content-Disposition: form-data; name=\"").append(name)
                    .append("\"; filename=\"").append(fileName).append("\"\r\n");
            textPart.append("Content-Type: ").append(contentType).append("\r\n\r\n");
            binaryPart.write(textPart.toString().getBytes(StandardCharsets.UTF_8));
            textPart.setLength(0);
            binaryPart.write(bytes);
            binaryPart.write("\r\n".getBytes(StandardCharsets.UTF_8));
        }

        private byte[] build() throws IOException {
            binaryPart.write(textPart.toString().getBytes(StandardCharsets.UTF_8));
            binaryPart.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
            return binaryPart.toByteArray();
        }
    }
}
