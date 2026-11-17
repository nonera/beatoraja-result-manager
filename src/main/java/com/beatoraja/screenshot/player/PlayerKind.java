package com.beatoraja.screenshot.player;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Which BMS player the app works with. Chosen in the settings (people do not run beatoraja and
 * BMZ Player side by side); {@link #detect} only seeds that choice for configs saved before it.
 */
public enum PlayerKind {
    BEATORAJA("beatoraja", "beatoraja"),
    BMZ("bmz", "BMZ Player"),
    UNKNOWN("", "不明");

    private final String configKey;
    private final String displayName;

    PlayerKind(String configKey, String displayName) {
        this.configKey = configKey;
        this.displayName = displayName;
    }

    /** The value stored in the app's config file. */
    public String configKey() {
        return configKey;
    }

    public String displayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    /** The kind stored as {@code key}, or {@link #UNKNOWN} for a blank or unrecognized value. */
    public static PlayerKind fromConfigKey(String key) {
        if (key != null) {
            for (PlayerKind kind : values()) {
                if (kind != UNKNOWN && kind.configKey.equalsIgnoreCase(key.trim())) {
                    return kind;
                }
            }
        }
        return UNKNOWN;
    }

    /**
     * The player folder a screenshot folder belongs to, for when only the screenshot folder is
     * configured: the parent of beatoraja's {@code screenshot} folder or of BMZ Player's default
     * {@code screenshots} folder inside its data folder; otherwise the folder itself.
     */
    public static Path playerFolderOf(Path screenshotDirectory) {
        if (screenshotDirectory == null) {
            return null;
        }
        Path dir = screenshotDirectory.toAbsolutePath().normalize();
        String name = dir.getFileName() == null ? "" : dir.getFileName().toString();
        if ("screenshot".equalsIgnoreCase(name)) {
            return dir.getParent();
        }
        if ("screenshots".equalsIgnoreCase(name) && dir.getParent() != null
                && BmzPaths.findDataDirectory(dir.getParent()) != null) {
            return dir.getParent();
        }
        return dir;
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
