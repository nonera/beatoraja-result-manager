package com.beatoraja.screenshot.service.twitter;

import com.beatoraja.screenshot.util.AppLogging;
import com.beatoraja.screenshot.util.AppPaths;
import com.beatoraja.screenshot.util.ProcessUtils;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;

public class TwitterChromeLoginService {

    private static final Logger LOG = AppLogging.get(TwitterChromeLoginService.class);
    private static final Duration STARTUP_TIMEOUT = Duration.ofSeconds(20);
    private static final Duration EXTRACTION_TIMEOUT = Duration.ofSeconds(35);
    private static final Duration COOKIE_RETRY_INTERVAL = Duration.ofMillis(1000);
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
        ChromiumProfileLock.clearDebugArtifacts(profileDir);

        browserProcess = ChromiumLauncher.launch(browser, profileDir, true, List.of("https://x.com/i/flow/login"));
    }

    /**
     * ログイン後、ユーザーがブラウザを閉じるまで待つ。
     * 強制終了すると Cookie がディスクに保存されないことがある。
     */
    public void waitForLoginBrowserClosed(BooleanSupplier cancelled) throws IOException, InterruptedException {
        if (browserProcess == null || !browserProcess.isAlive()) {
            browserProcess = null;
            Thread.sleep(COOKIE_FLUSH_DELAY.toMillis());
            ChromiumProfileLock.waitForProfileUnlock(profileDirectory());
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
        ChromiumProfileLock.waitForProfileUnlock(profileDirectory());
    }

    public TwitterCookies extractCookiesFromProfile(BooleanSupplier cancelled)
            throws IOException, InterruptedException {
        if (browserProcess != null && browserProcess.isAlive()) {
            throw new IOException("Cookie 取得前に、ログイン用ブラウザを閉じてください。");
        }

        ChromiumProfileLock.waitForProfileUnlock(profileDirectory());
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
                ChromiumProfileLock.clearDebugArtifacts(profileDirectory());
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
        } catch (Exception e) {
            LOG.log(Level.FINE, "Failed to refresh Twitter cookies silently", e);
        }
        return Optional.empty();
    }

    private TwitterCookies runCdpExtraction(BooleanSupplier cancelled, int attempt) throws IOException, InterruptedException {
        Path browser = browserExecutable()
                .orElseThrow(() -> new IOException("Chrome または Edge が見つかりません。"));

        Path profileDir = profileDirectory();
        Files.createDirectories(profileDir);
        ChromiumProfileLock.clearDebugArtifacts(profileDir);

        // Off-screen: the user never works in this window, it only lets CDP read the cookies.
        browserProcess = ChromiumLauncher.launch(browser, profileDir, false, List.of(
                "--remote-debugging-port=0",
                "--window-position=-32000,-32000",
                "--window-size=960,720",
                "about:blank"));

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
                try {
                    TwitterCookies cookies = new ChromiumDevToolsClient(debugPort).readTwitterCookies();
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
            ChromiumProfileLock.waitForProfileUnlock(profileDirectory());
        }
    }

    public void stopBrowser() {
        ProcessUtils.destroyProcessTree(browserProcess);
        browserProcess = null;
    }
}
