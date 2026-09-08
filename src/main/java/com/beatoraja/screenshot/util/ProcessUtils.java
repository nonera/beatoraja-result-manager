package com.beatoraja.screenshot.util;

import java.util.concurrent.TimeUnit;

public final class ProcessUtils {

    private ProcessUtils() {
    }

    public static void destroyProcessTree(Process process) {
        if (process == null) {
            return;
        }
        try {
            if (!process.isAlive()) {
                return;
            }
            if (isWindows()) {
                Process kill = new ProcessBuilder("taskkill", "/F", "/T", "/PID", String.valueOf(process.pid()))
                        .redirectErrorStream(true)
                        .start();
                kill.waitFor(15, TimeUnit.SECONDS);
            } else {
                process.destroyForcibly();
            }
            process.waitFor(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            process.destroyForcibly();
        } catch (Exception ignored) {
            process.destroyForcibly();
        }
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }
}
