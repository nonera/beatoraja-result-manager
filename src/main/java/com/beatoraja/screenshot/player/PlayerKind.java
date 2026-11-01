package com.beatoraja.screenshot.player;

import java.nio.file.Files;
import java.nio.file.Path;

/** Which BMS player a configured player-data folder belongs to, detected from its contents. */
public enum PlayerKind {
    BEATORAJA("beatoraja"),
    BMZ("BMZ Player"),
    UNKNOWN("不明");

    private final String displayName;

    PlayerKind(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    /**
     * beatoraja folders carry {@code config_sys.json}; BMZ Player data folders carry
     * {@code config.toml} (also accepted one level down in {@code data/}, i.e. when the
     * portable install's exe folder is chosen instead of its data folder).
     */
    public static PlayerKind detect(Path directory) {
        if (directory == null || !Files.isDirectory(directory)) {
            return UNKNOWN;
        }
        if (Files.isRegularFile(directory.resolve("config_sys.json"))) {
            return BEATORAJA;
        }
        if (BmzPaths.findDataDirectory(directory) != null) {
            return BMZ;
        }
        return UNKNOWN;
    }
}
