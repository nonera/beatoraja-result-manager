package com.beatoraja.screenshot.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class AppPaths {

    private static final Path APP_DATA = resolveAppData();
    private static final Path EXE_DIR = resolveExeDir();

    private AppPaths() {
    }

    public static Path appDataDir() {
        return APP_DATA;
    }

    public static Path configFile() {
        return APP_DATA.resolve("config.json");
    }

    public static Path postedStateFile() {
        return APP_DATA.resolve("posted.json");
    }

    public static Path exeDir() {
        return EXE_DIR;
    }

    public static Path bundledTwitterExe() {
        return EXE_DIR.resolve("tools").resolve("twitter.exe");
    }

    public static Path twitterChromeProfileDir() {
        return APP_DATA.resolve("twitter-chrome-profile");
    }

    private static Path resolveAppData() {
        String appData = System.getenv("APPDATA");
        if (appData != null && !appData.isBlank()) {
            Path dir = Paths.get(appData, "beatoraja-screenshot-manager");
            try {
                Files.createDirectories(dir);
            } catch (IOException ignored) {
            }
            return dir;
        }
        return Paths.get(System.getProperty("user.home"), ".beatoraja-screenshot-manager");
    }

    private static Path resolveExeDir() {
        String appDir = System.getProperty("app.dir");
        if (appDir != null && !appDir.isBlank()) {
            return Paths.get(appDir);
        }

        try {
            Path codeSource = Path.of(AppPaths.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI());
            if (Files.isRegularFile(codeSource)) {
                return codeSource.getParent();
            }
            if (Files.isDirectory(codeSource)) {
                return codeSource;
            }
        } catch (Exception ignored) {
        }

        return Paths.get(System.getProperty("user.dir"));
    }
}
