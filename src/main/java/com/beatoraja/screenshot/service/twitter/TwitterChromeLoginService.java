package com.beatoraja.screenshot.service.twitter;

import com.beatoraja.screenshot.util.AppPaths;
import com.beatoraja.screenshot.util.ProcessUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;

public class TwitterChromeLoginService {

    private static final Duration STARTUP_TIMEOUT = Duration.ofSeconds(20);
    private static final Duration EXTRACTION_TIMEOUT = Duration.ofSeconds(35);
    private static final Duration COOKIE_RETRY_INTERVAL = Duration.ofMillis(1000);
    private static final Duration PROFILE_UNLOCK_TIMEOUT = Duration.ofSeconds(25);
    private static final Duration BROWSER_CLOSE_TIMEOUT = Duration.ofMinutes(2);
    private static final Duration COOKIE_FLUSH_DELAY = Duration.ofSeconds(2);
    private static final int EXTRACTION_ATTEMPTS = 2;

    private Process browserProcess;

    public Optional<Path> browserExecutable() {
        return ChromiumBrowserLocator.locate();
    }

    public Path profileDirectory() {
        return AppPaths.twitterChromeProfileDir();
    }

    public void openLoginBrowser() throws IOException {
        Path browser = browserExecutable()
                .orElseThrow(() -> new IOException("Chrome または Edge が見つかりません。いずれかをインストールしてください。"));

        Path profileDir = profileDirectory();
        Files.createDirectories(profileDir);
        stopBrowser();
        clearDebugArtifacts(profileDir);

        List<String> command = baseLaunchArgs(browser, profileDir, false);
        command.add("https://x.com/i/flow/login");

        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);
        browserProcess = builder.start();
    }

    /**
     * ログイン後、ユーザーがブラウザを閉じるまで待つ。
     * 強制終了すると Cookie がディスクに保存されないことがある。
     */
    public void waitForLoginBrowserClosed(BooleanSupplier cancelled) throws IOException, InterruptedException {
        if (browserProcess == null || !browserProcess.isAlive()) {
            browserProcess = null;
            Thread.sleep(COOKIE_FLUSH_DELAY.toMillis());
            waitForProfileUnlock();
            return;
        }

        long deadline = System.currentTimeMillis() + BROWSER_CLOSE_TIMEOUT.toMillis();
        while (browserProcess.isAlive() && System.currentTimeMillis() < deadline) {
            if (cancelled.getAsBoolean()) {
                throw new IOException("ログインをキャンセルしました");
            }
            Thread.sleep(300);
        }

        if (browserProcess.isAlive()) {
            throw new IOException(
                    "ブラウザが閉じられませんでした。"
                            + " 「ログイン完了」を押したあと、開いた Chrome / Edge のウィンドウを閉じてください。");
        }

        browserProcess = null;
        Thread.sleep(COOKIE_FLUSH_DELAY.toMillis());
        waitForProfileUnlock();
    }

    public TwitterCookies extractCookiesFromProfile(BooleanSupplier cancelled)
            throws IOException, InterruptedException {
        if (browserProcess != null && browserProcess.isAlive()) {
            throw new IOException("Cookie 取得前に、ログイン用ブラウザを閉じてください。");
        }

        waitForProfileUnlock();
        if (cancelled.getAsBoolean()) {
            throw new IOException("ログインをキャンセルしました");
        }

        IOException lastError = null;
        for (int attempt = 1; attempt <= EXTRACTION_ATTEMPTS; attempt++) {
            if (cancelled.getAsBoolean()) {
                throw new IOException("ログインをキャンセルしました");
            }
            try {
                return runCdpExtraction(cancelled, attempt);
            } catch (IOException e) {
                lastError = e;
                clearDebugArtifacts(profileDirectory());
                Thread.sleep(COOKIE_RETRY_INTERVAL.toMillis());
            }
        }
        throw lastError == null
                ? new IOException("Cookie の取得に失敗しました")
                : lastError;
    }

    public Optional<TwitterCookies> silentRefresh() {
        try {
            if (browserProcess != null && browserProcess.isAlive()) {
                return Optional.empty();
            }
            TwitterCookies cookies = extractCookiesFromProfile(() -> false);
            if (cookies.isComplete()) {
                return Optional.of(cookies);
            }
        } catch (Exception ignored) {
        }
        return Optional.empty();
    }

    private TwitterCookies runCdpExtraction(BooleanSupplier cancelled, int attempt) throws IOException, InterruptedException {
        Path browser = browserExecutable()
                .orElseThrow(() -> new IOException("Chrome または Edge が見つかりません。"));

        Path profileDir = profileDirectory();
        Files.createDirectories(profileDir);
        clearDebugArtifacts(profileDir);

        List<String> command = baseLaunchArgs(browser, profileDir, true);
        command.add("--remote-debugging-port=0");
        command.add("--window-position=-32000,-32000");
        command.add("--window-size=960,720");
        command.add("about:blank");

        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);
        browserProcess = builder.start();

        try {
            int debugPort;
            try {
                debugPort = ChromiumDevToolsClient.waitForDebugPort(profileDir, STARTUP_TIMEOUT);
            } catch (IOException e) {
                throw new IOException("Cookie 取得用ブラウザの起動に失敗しました（試行 " + attempt + "）: " + e.getMessage(), e);
            }

            long deadline = System.currentTimeMillis() + EXTRACTION_TIMEOUT.toMillis();
            IOException lastReadError = null;

            while (System.currentTimeMillis() < deadline) {
                if (cancelled.getAsBoolean()) {
                    throw new IOException("ログインをキャンセルしました");
                }
                if (!browserProcess.isAlive()) {
                    throw new IOException("Cookie 取得用ブラウザが予期せず終了しました");
                }
                try (ChromiumDevToolsClient client = new ChromiumDevToolsClient(debugPort)) {
                    TwitterCookies cookies = client.readTwitterCookies();
                    if (cookies.isComplete()) {
                        return cookies;
                    }
                } catch (IOException e) {
                    lastReadError = e;
                } catch (Exception e) {
                    lastReadError = new IOException("Cookie 読み取りエラー: " + e.getMessage(), e);
                }
                Thread.sleep(COOKIE_RETRY_INTERVAL.toMillis());
            }

            if (lastReadError != null) {
                throw new IOException("Cookie 取得がタイムアウトしました（試行 " + attempt + "）: " + lastReadError.getMessage(), lastReadError);
            }
            throw new IOException("Cookie 取得がタイムアウトしました（試行 " + attempt + "）");
        } finally {
            stopBrowser();
            waitForProfileUnlock();
        }
    }

    private List<String> baseLaunchArgs(Path browser, Path profileDir, boolean automation) {
        List<String> command = new ArrayList<>();
        command.add(browser.toAbsolutePath().toString());
        command.add("--user-data-dir=" + profileDir.toAbsolutePath());
        command.add("--no-first-run");
        command.add("--no-default-browser-check");
        command.add("--disable-sync");
        command.add("--disable-features=ChromeWhatsNewUI,TranslateUI");
        if (!automation) {
            command.add("--disable-blink-features=AutomationControlled");
        }
        return command;
    }

    private void clearDebugArtifacts(Path profileDir) {
        try {
            Files.deleteIfExists(profileDir.resolve("DevToolsActivePort"));
            Files.deleteIfExists(profileDir.resolve("DevToolsActivePort.lock"));
        } catch (IOException ignored) {
        }
    }

    private void waitForProfileUnlock() throws InterruptedException, IOException {
        Path profileDir = profileDirectory();
        Path singletonLock = profileDir.resolve("SingletonLock");
        Path lockFile = profileDir.resolve("lockfile");
        long deadline = System.currentTimeMillis() + PROFILE_UNLOCK_TIMEOUT.toMillis();

        while (System.currentTimeMillis() < deadline) {
            if (!Files.exists(singletonLock) && !Files.exists(lockFile)) {
                Thread.sleep(400);
                if (!Files.exists(singletonLock) && !Files.exists(lockFile)) {
                    return;
                }
            }
            Thread.sleep(200);
        }
        throw new IOException(
                "ブラウザプロファイルのロック解除待ちがタイムアウトしました。"
                        + " Chrome / Edge のウィンドウが残っていないか確認してください。");
    }

    public void stopBrowser() {
        ProcessUtils.destroyProcessTree(browserProcess);
        browserProcess = null;
    }
}
