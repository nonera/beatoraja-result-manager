package com.beatoraja.screenshot.util;

import java.nio.file.Path;

/**
 * Where the app keeps its data (config, posted state, logs, browser profile). A class of its
 * own with no state, so the logger can find its folder without initializing AppPaths.
 */
public final class AppDataLocation {

    private static final String FOLDER = "beatoraja-screenshot-manager";

    private AppDataLocation() {
    }

    /** %APPDATA%\beatoraja-screenshot-manager, or ~/.beatoraja-screenshot-manager without APPDATA. Creates nothing. */
    public static Path resolve() {
        String appData = System.getenv("APPDATA");
        if (appData != null && !appData.isBlank()) {
            return Path.of(appData, FOLDER);
        }
        return Path.of(System.getProperty("user.home"), "." + FOLDER);
    }
}
