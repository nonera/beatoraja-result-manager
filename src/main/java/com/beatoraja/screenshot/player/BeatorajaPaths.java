package com.beatoraja.screenshot.player;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public final class BeatorajaPaths {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Path beatorajaDirectory;
    private final String playerName;
    private final Path playerDirectory;
    private final Path scoreDataLogDb;
    private final Path scoreLogDb;
    private final Path songDatabase;
    private final Path tableDirectory;

    private BeatorajaPaths(
            Path beatorajaDirectory,
            String playerName,
            Path playerDirectory,
            Path scoreDataLogDb,
            Path scoreLogDb,
            Path songDatabase,
            Path tableDirectory
    ) {
        this.beatorajaDirectory = beatorajaDirectory;
        this.playerName = playerName;
        this.playerDirectory = playerDirectory;
        this.scoreDataLogDb = scoreDataLogDb;
        this.scoreLogDb = scoreLogDb;
        this.songDatabase = songDatabase;
        this.tableDirectory = tableDirectory;
    }

    public static BeatorajaPaths resolve(Path beatorajaDirectory, String playerNameOverride) throws IOException {
        Path configFile = beatorajaDirectory.resolve("config_sys.json");
        if (!Files.exists(configFile)) {
            throw new IOException("config_sys.json が見つかりません: " + configFile);
        }

        JsonNode root = MAPPER.readTree(configFile.toFile());
        String playerName = playerNameOverride == null || playerNameOverride.isBlank()
                ? root.path("playername").asText("player1")
                : playerNameOverride;
        String playerPathValue = root.path("playerpath").asText("player");
        String songPathValue = root.path("songpath").asText("songdata.db");
        String tablePathValue = root.path("tablepath").asText("table");

        Path playerPath = Path.of(playerPathValue);
        if (!playerPath.isAbsolute()) {
            playerPath = beatorajaDirectory.resolve(playerPath);
        }

        Path songPath = Path.of(songPathValue);
        if (!songPath.isAbsolute()) {
            songPath = beatorajaDirectory.resolve(songPath);
        }

        Path tablePath = Path.of(tablePathValue);
        if (!tablePath.isAbsolute()) {
            tablePath = beatorajaDirectory.resolve(tablePath);
        }

        Path playerDir = playerPath.resolve(playerName);
        return new BeatorajaPaths(
                beatorajaDirectory,
                playerName,
                playerDir,
                playerDir.resolve("scoredatalog.db"),
                playerDir.resolve("scorelog.db"),
                songPath,
                tablePath
        );
    }

    public static List<String> listPlayerNames(Path beatorajaDirectory) {
        List<String> names = new ArrayList<>();
        if (beatorajaDirectory == null) {
            return names;
        }

        Path configFile = beatorajaDirectory.resolve("config_sys.json");
        String playerPathValue = "player";
        if (Files.exists(configFile)) {
            try {
                JsonNode root = MAPPER.readTree(configFile.toFile());
                playerPathValue = root.path("playerpath").asText("player");
            } catch (IOException ignored) {
            }
        }

        Path playerPath = Path.of(playerPathValue);
        if (!playerPath.isAbsolute()) {
            playerPath = beatorajaDirectory.resolve(playerPath);
        }
        if (!Files.isDirectory(playerPath)) {
            return names;
        }

        try (Stream<Path> stream = Files.list(playerPath)) {
            stream.filter(Files::isDirectory)
                    .map(path -> path.getFileName().toString())
                    .sorted(Comparator.naturalOrder())
                    .forEach(names::add);
        } catch (IOException ignored) {
        }
        return names;
    }

    public Path beatorajaDirectory() {
        return beatorajaDirectory;
    }

    public String playerName() {
        return playerName;
    }

    public Path playerDirectory() {
        return playerDirectory;
    }

    public Path scoreDataLogDb() {
        return scoreDataLogDb;
    }

    public Path scoreLogDb() {
        return scoreLogDb;
    }

    public Path songDatabase() {
        return songDatabase;
    }

    public Path tableDirectory() {
        return tableDirectory;
    }
}
