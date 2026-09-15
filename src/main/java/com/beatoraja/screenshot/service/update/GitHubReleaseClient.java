package com.beatoraja.screenshot.service.update;

import com.beatoraja.screenshot.util.AppLogging;
import com.beatoraja.screenshot.util.VersionComparator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.logging.Logger;

public class GitHubReleaseClient {

    private static final Logger LOG = AppLogging.get(GitHubReleaseClient.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String REPO = "nonera/beatoraja-result-manager";
    private static final String ASSET_NAME = "beatoraja-screenshot-manager.zip";
    private static final String USER_AGENT = "beatoraja-screenshot-manager";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public record ReleaseInfo(String tagName, String version, URI downloadUrl) {
    }

    public ReleaseInfo fetchLatestRelease() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.github.com/repos/" + REPO + "/releases/latest"))
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", USER_AGENT)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("GitHub Releases API returned HTTP " + response.statusCode());
        }

        JsonNode root = MAPPER.readTree(response.body());
        String tagName = root.path("tag_name").asText("");
        if (tagName.isBlank()) {
            throw new IOException("Latest release tag was missing.");
        }

        URI downloadUrl = null;
        for (JsonNode asset : root.path("assets")) {
            if (ASSET_NAME.equals(asset.path("name").asText())) {
                downloadUrl = URI.create(asset.path("browser_download_url").asText());
                break;
            }
        }
        if (downloadUrl == null) {
            throw new IOException("Release asset not found: " + ASSET_NAME);
        }

        String version = VersionComparator.normalize(tagName);
        LOG.info("Latest release detected: " + tagName);
        return new ReleaseInfo(tagName, version, downloadUrl);
    }
}
