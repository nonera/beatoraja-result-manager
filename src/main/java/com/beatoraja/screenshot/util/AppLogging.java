package com.beatoraja.screenshot.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public final class AppLogging {

    private static final String ROOT_LOGGER_NAME = "com.beatoraja.screenshot";
    private static volatile boolean initialized;

    private AppLogging() {
    }

    public static void initialize() {
        if (initialized) {
            return;
        }
        synchronized (AppLogging.class) {
            if (initialized) {
                return;
            }
            try {
                Path logDir = resolveLogDir();
                Files.createDirectories(logDir);

                Logger root = Logger.getLogger(ROOT_LOGGER_NAME);
                root.setUseParentHandlers(false);
                root.setLevel(Level.ALL);

                FileHandler fileHandler = new FileHandler(
                        logDir.resolve("app.%g.log").toString(),
                        1024 * 1024,
                        5,
                        true
                );
                fileHandler.setFormatter(new SimpleFormatter());
                fileHandler.setLevel(Level.ALL);
                root.addHandler(fileHandler);

                ConsoleHandler consoleHandler = new ConsoleHandler();
                consoleHandler.setLevel(Level.INFO);
                root.addHandler(consoleHandler);

                initialized = true;
                root.info("Logging initialized: " + logDir.toAbsolutePath());
            } catch (IOException e) {
                System.err.println("Failed to initialize logging: " + e.getMessage());
            }
        }
    }

    public static Logger get() {
        initialize();
        return Logger.getLogger(ROOT_LOGGER_NAME);
    }

    public static Logger get(Class<?> type) {
        initialize();
        return Logger.getLogger(type.getName());
    }

    public static String maskWebhookUrl(String url) {
        if (url == null || url.isBlank()) {
            return url;
        }
        return url.replaceAll("(webhooks/\\d+/)[^/?\\s]+", "$1****");
    }

    static Path resolveLogDir() {
        String appData = System.getenv("APPDATA");
        if (appData != null && !appData.isBlank()) {
            return Path.of(appData, "beatoraja-screenshot-manager", "logs");
        }
        return Path.of(System.getProperty("user.home"), ".beatoraja-screenshot-manager", "logs");
    }

    public static String sanitize(String message) {
        if (message == null || message.isBlank()) {
            return message;
        }
        String sanitized = message;
        sanitized = sanitized.replaceAll("(webhooks/\\d+/)[^/?\\s\"']+", "$1****");
        sanitized = sanitized.replaceAll("(auth_token=)[^;&\\s\"']+", "$1****");
        sanitized = sanitized.replaceAll("(ct0=)[^;&\\s\"']+", "$1****");
        return sanitized;
    }
}
