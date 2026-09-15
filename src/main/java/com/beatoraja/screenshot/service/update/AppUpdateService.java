package com.beatoraja.screenshot.service.update;

import com.beatoraja.screenshot.util.AppLogging;
import com.beatoraja.screenshot.util.AppPaths;
import com.beatoraja.screenshot.util.AppVersion;
import com.beatoraja.screenshot.util.VersionComparator;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AppUpdateService {

    private static final Logger LOG = AppLogging.get(AppUpdateService.class);
    private static final String EXECUTABLE_NAME = "beatoraja-screenshot-manager.exe";

    public interface ProgressListener {
        void onStatus(String message);

        void onProgress(long downloadedBytes, long totalBytes);
    }

    public enum UpdateOutcome {
        NOT_APPLICABLE,
        UP_TO_DATE,
        FAILED,
        RESTARTING
    }

    private final GitHubReleaseClient releaseClient = new GitHubReleaseClient();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public static boolean shouldCheckForUpdates(boolean autoUpdateEnabled, String... args) {
        if (!autoUpdateEnabled) {
            return false;
        }
        if (!isWindows()) {
            return false;
        }
        if ("dev".equalsIgnoreCase(AppVersion.get())) {
            return false;
        }
        for (String arg : args) {
            if ("--skip-update".equals(arg)) {
                return false;
            }
        }
        Path executable = mainExecutable();
        return executable != null && Files.isRegularFile(executable);
    }

    public UpdateOutcome tryAutoUpdate(ProgressListener listener) {
        try {
            String currentVersion = VersionComparator.normalize(AppVersion.get());
            if (currentVersion.isBlank()) {
                return UpdateOutcome.NOT_APPLICABLE;
            }

            listener.onStatus("更新を確認しています...");
            GitHubReleaseClient.ReleaseInfo release = releaseClient.fetchLatestRelease();
            if (!VersionComparator.isNewer(release.version(), currentVersion)) {
                LOG.info("Application is up to date: " + currentVersion);
                return UpdateOutcome.UP_TO_DATE;
            }

            Path executable = mainExecutable();
            Path targetDir = AppPaths.exeDir();
            if (executable == null || !Files.isRegularFile(executable)) {
                throw new IOException("Application executable was not found.");
            }
            if (!Files.isWritable(targetDir)) {
                throw new IOException("Application directory is not writable: " + targetDir);
            }

            Path workDir = Files.createTempDirectory("beatoraja-screenshot-manager-update-");
            Path zipPath = workDir.resolve("release.zip");
            Path extractDir = workDir.resolve("extracted");

            listener.onStatus("v" + release.version() + " をダウンロードしています...");
            downloadRelease(release.downloadUrl(), zipPath, listener);

            listener.onStatus("更新ファイルを展開しています...");
            Path sourceDir = ZipExtractUtils.extractRootDirectory(zipPath, extractDir);
            Path sourceExecutable = sourceDir.resolve(EXECUTABLE_NAME);
            if (!Files.isRegularFile(sourceExecutable)) {
                throw new IOException("Downloaded release did not contain " + EXECUTABLE_NAME);
            }

            listener.onStatus("更新を適用しています...");
            long parentPid = ProcessHandle.current().pid();
            WindowsUpdateLauncher.launch(sourceDir, targetDir, executable, parentPid);
            LOG.info("Update launcher started. Current version " + currentVersion + " -> " + release.version());
            return UpdateOutcome.RESTARTING;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Automatic update failed", e);
            listener.onStatus("更新に失敗しました: " + e.getMessage());
            return UpdateOutcome.FAILED;
        }
    }

    private void downloadRelease(URI downloadUrl, Path destination, ProgressListener listener)
            throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(downloadUrl)
                .timeout(Duration.ofMinutes(10))
                .header("User-Agent", "beatoraja-screenshot-manager")
                .GET()
                .build();

        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() != 200) {
            throw new IOException("Release download failed with HTTP " + response.statusCode());
        }

        long totalBytes = response.headers().firstValueAsLong("Content-Length").orElse(-1L);
        try (InputStream input = response.body()) {
            Files.createDirectories(destination.getParent());
            try (var output = Files.newOutputStream(destination)) {
                byte[] buffer = new byte[8192];
                long downloaded = 0;
                int read;
                while ((read = input.read(buffer)) >= 0) {
                    output.write(buffer, 0, read);
                    downloaded += read;
                    listener.onProgress(downloaded, totalBytes);
                }
            }
        }
    }

    public static Path mainExecutable() {
        Path candidate = AppPaths.exeDir().resolve(EXECUTABLE_NAME);
        return Files.isRegularFile(candidate) ? candidate : null;
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }
}
