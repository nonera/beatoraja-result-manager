package com.beatoraja.screenshot.util;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ProcessUtils {

    private static final Logger LOG = AppLogging.get(ProcessUtils.class);

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
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to destroy process tree for pid " + process.pid(), e);
            process.destroyForcibly();
        }
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }
}
