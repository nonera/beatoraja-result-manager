package com.beatoraja.screenshot.service.twitter;

import com.beatoraja.screenshot.util.AppLogging;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

/**
 * Shared helpers for launching short-lived Chromium instances against the
 * persisted Twitter/X login profile without leaving stale locks behind.
 */
final class ChromiumProfileLock {

    private static final Logger LOG = AppLogging.get(ChromiumProfileLock.class);
    private static final Duration UNLOCK_TIMEOUT = Duration.ofSeconds(25);

    private ChromiumProfileLock() {
    }

    static void clearDebugArtifacts(Path profileDir) {
        try {
            Files.deleteIfExists(profileDir.resolve("DevToolsActivePort"));
            Files.deleteIfExists(profileDir.resolve("DevToolsActivePort.lock"));
        } catch (IOException e) {
            LOG.log(Level.FINE, "Failed to clear Chromium debug artifacts in " + profileDir, e);
        }
    }

    static void waitForProfileUnlock(Path profileDir) throws InterruptedException, IOException {
        Path singletonLock = profileDir.resolve("SingletonLock");
        Path lockFile = profileDir.resolve("lockfile");
        long deadline = System.currentTimeMillis() + UNLOCK_TIMEOUT.toMillis();

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
}
