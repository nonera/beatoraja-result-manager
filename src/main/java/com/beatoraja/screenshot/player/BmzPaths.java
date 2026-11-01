package com.beatoraja.screenshot.player;

import com.beatoraja.screenshot.util.AppLogging;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * File locations inside a BMZ Player data folder (the folder holding {@code config.toml}).
 * Layout per bmz-player {@code crates/bmz-player/src/paths.rs}:
 * <pre>
 * config.toml, library.db, screenshots/, profiles/&lt;id&gt;/score.db
 * </pre>
 */
public final class BmzPaths {

    private static final Logger LOG = AppLogging.get(BmzPaths.class);
    private static final TomlMapper MAPPER = new TomlMapper();
    private static final String CONFIG_FILE = "config.toml";

    private final Path dataDirectory;
    private final String profileId;
    private final Path profileDirectory;
    private final Path scoreDb;
    private final Path libraryDb;
    private final Path screenshotDirectory;

    private BmzPaths(Path dataDirectory, String profileId, Path screenshotDirectory) {
        this.dataDirectory = dataDirectory;
        this.profileId = profileId;
        this.profileDirectory = dataDirectory.resolve("profiles").resolve(profileId);
        this.scoreDb = profileDirectory.resolve("score.db");
        this.libraryDb = dataDirectory.resolve("library.db");
        this.screenshotDirectory = screenshotDirectory;
    }

    /** The data folder for {@code directory}: itself or its {@code data/} child, whichever holds config.toml. */
    public static Path findDataDirectory(Path directory) {
        if (directory == null) {
            return null;
        }
        if (Files.isRegularFile(directory.resolve(CONFIG_FILE))) {
            return directory;
        }
        Path nested = directory.resolve("data");
        if (Files.isRegularFile(nested.resolve(CONFIG_FILE))) {
            return nested;
        }
        return null;
    }

    public static BmzPaths resolve(Path directory, String profileOverride) throws IOException {
        Path dataDirectory = findDataDirectory(directory);
        if (dataDirectory == null) {
            throw new IOException("config.toml が見つかりません: " + directory);
        }

        JsonNode root = MAPPER.readTree(dataDirectory.resolve(CONFIG_FILE).toFile());
        String profileId = profileOverride == null || profileOverride.isBlank()
                ? root.path("active_profile").asText("default")
                : profileOverride;
        if (profileId.isBlank()) {
            profileId = "default";
        }
        String screenshotDir = root.path("screenshot").path("dir").asText("screenshots");
        return new BmzPaths(dataDirectory, profileId, resolveScreenshotDirectory(dataDirectory, screenshotDir));
    }

    /**
     * Mirrors bmz-player's {@code screenshot_dir}: blank means "screenshots", relative paths
     * resolve against the data folder, and a legacy leading {@code data/} is dropped.
     */
    static Path resolveScreenshotDirectory(Path dataDirectory, String configured) {
        String trimmed = configured == null ? "" : configured.trim();
        Path path = Path.of(trimmed.isEmpty() ? "screenshots" : trimmed);
        if (path.isAbsolute()) {
            return path;
        }
        if ("data".equals(path.getName(0).toString())) {
            return path.getNameCount() == 1
                    ? dataDirectory
                    : dataDirectory.resolve(path.subpath(1, path.getNameCount()));
        }
        return dataDirectory.resolve(path);
    }

    public static List<String> listProfileIds(Path directory) {
        List<String> ids = new ArrayList<>();
        Path dataDirectory = findDataDirectory(directory);
        if (dataDirectory == null) {
            return ids;
        }
        Path profiles = dataDirectory.resolve("profiles");
        if (!Files.isDirectory(profiles)) {
            return ids;
        }
        try (Stream<Path> stream = Files.list(profiles)) {
            stream.filter(Files::isDirectory)
                    .map(path -> path.getFileName().toString())
                    .sorted(Comparator.naturalOrder())
                    .forEach(ids::add);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Failed to list BMZ profiles in " + profiles.toAbsolutePath(), e);
        }
        return ids;
    }

    public Path dataDirectory() {
        return dataDirectory;
    }

    public String profileId() {
        return profileId;
    }

    public Path profileDirectory() {
        return profileDirectory;
    }

    public Path scoreDb() {
        return scoreDb;
    }

    public Path libraryDb() {
        return libraryDb;
    }

    public Path screenshotDirectory() {
        return screenshotDirectory;
    }
}
