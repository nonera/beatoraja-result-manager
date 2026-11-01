package com.beatoraja.screenshot.player;

import com.beatoraja.screenshot.table.BmtTableReader;
import com.beatoraja.screenshot.table.BmzTableReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BmzPlayerDataTest {

    private static final String SHA = "a".repeat(64);

    @TempDir
    Path tempDir;

    @Test
    void detectsPlayerKindFromFolderContents() throws Exception {
        Path beatoraja = Files.createDirectories(tempDir.resolve("beatoraja"));
        Files.writeString(beatoraja.resolve("config_sys.json"), "{}");
        Path bmzExe = Files.createDirectories(tempDir.resolve("bmz/data"));
        Files.writeString(bmzExe.resolve("config.toml"), "version = 1\n");

        assertEquals(PlayerKind.BEATORAJA, PlayerKind.detect(beatoraja));
        assertEquals(PlayerKind.BMZ, PlayerKind.detect(bmzExe));
        assertEquals(PlayerKind.BMZ, PlayerKind.detect(tempDir.resolve("bmz")));
        assertEquals(PlayerKind.UNKNOWN, PlayerKind.detect(tempDir));
    }

    @Test
    void resolvesPathsFromConfigToml() throws Exception {
        Files.writeString(tempDir.resolve("config.toml"), """
                version = 1
                active_profile = "main"

                [screenshot]
                dir = "data/shots"
                """);
        Files.createDirectories(tempDir.resolve("profiles/main"));
        Files.createDirectories(tempDir.resolve("profiles/sub"));

        BmzPaths paths = BmzPaths.resolve(tempDir, "");
        assertEquals("main", paths.profileId());
        assertEquals(tempDir.resolve("profiles/main/score.db"), paths.scoreDb());
        assertEquals(tempDir.resolve("library.db"), paths.libraryDb());
        assertEquals(tempDir.resolve("shots"), paths.screenshotDirectory());

        assertEquals("sub", BmzPaths.resolve(tempDir, "sub").profileId());
        assertEquals(List.of("main", "sub"), BmzPaths.listProfileIds(tempDir));
    }

    @Test
    void defaultsWhenConfigTomlIsMinimal() throws Exception {
        Files.writeString(tempDir.resolve("config.toml"), "version = 1\n");

        BmzPaths paths = BmzPaths.resolve(tempDir, null);
        assertEquals("default", paths.profileId());
        assertEquals(tempDir.resolve("screenshots"), paths.screenshotDirectory());
    }

    @Test
    void mapsBmzClearLabels() {
        assertEquals(5, ClearTypeMapper.fromBmzLabel("Normal"));
        assertEquals(7, ClearTypeMapper.fromBmzLabel("ExHard"));
        assertNull(ClearTypeMapper.fromBmzLabel("Hazard"));
        assertEquals("HARD CLEAR", ClearTypeMapper.toClearName(6));
        assertEquals("CLEAR", ClearTypeMapper.toClearName(ClearTypeMapper.fromBmzLabel("Normal")));
    }

    @Test
    void findsLatestPlayBeforeScreenshot() throws Exception {
        Path scoreDb = tempDir.resolve("score.db");
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + scoreDb);
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE score_history (
                        id INTEGER PRIMARY KEY, chart_sha256 TEXT, played_at INTEGER, clear_type TEXT,
                        total_notes INTEGER, autoplay INTEGER NOT NULL DEFAULT 0,
                        fast_pgreat INTEGER, slow_pgreat INTEGER, fast_great INTEGER, slow_great INTEGER,
                        fast_good INTEGER, slow_good INTEGER, fast_bad INTEGER, slow_bad INTEGER,
                        fast_poor INTEGER, slow_poor INTEGER, fast_empty_poor INTEGER, slow_empty_poor INTEGER)
                    """);
            statement.execute("INSERT INTO score_history VALUES (1, 'old', 1000, 'Failed', 100, 0, 0,0,0,0,0,0,0,0,0,0,0,0)");
            statement.execute("INSERT INTO score_history VALUES (2, '" + SHA + "', 1300, 'Hard', 100, 0, 40,40,10,5,1,1,1,0,1,0,1,0)");
            statement.execute("INSERT INTO score_history VALUES (3, 'auto', 1310, 'Max', 100, 1, 0,0,0,0,0,0,0,0,0,0,0,0)");
        }

        BmzPlayLogService service = new BmzPlayLogService(scoreDb);
        List<PlayerPlayLogService.PlayLogEntry> found = service.findNear(at(1320), null);
        assertEquals(1, found.size());
        PlayerPlayLogService.PlayLogEntry play = found.get(0);
        assertEquals(SHA, play.sha256());
        assertEquals(6, play.clearId());
        assertNotNull(play.score());
        assertEquals(175, play.score().exScore());
        assertEquals(3, play.score().bp());

        assertTrue(service.findNear(at(1300 + 601), null).isEmpty());
        assertTrue(service.findNear(at(900), null).isEmpty());
    }

    @Test
    void readsLibraryChartsAndDifficultyTables() throws Exception {
        Path libraryDb = tempDir.resolve("library.db");
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + libraryDb);
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE charts (id INTEGER PRIMARY KEY, sha256 TEXT, md5 TEXT, title TEXT, subtitle TEXT, play_level TEXT)");
            statement.execute("INSERT INTO charts VALUES (1, '" + SHA + "', 'm', 'Song', '[ANOTHER]', '12')");
            statement.execute("INSERT INTO charts VALUES (2, '" + SHA + "', 'm', 'Dup', '', '1')");
            statement.execute("CREATE TABLE difficulty_tables (id INTEGER PRIMARY KEY, source_url TEXT, head_url TEXT, name TEXT, symbol TEXT, level_order TEXT, fetched_at INTEGER)");
            statement.execute("INSERT INTO difficulty_tables VALUES (1, 'u', 'h', 'Satellite', 'sl', '[\"0\",\"12\"]', 0)");
            statement.execute("CREATE TABLE difficulty_table_entries (id INTEGER PRIMARY KEY, table_id INTEGER, level TEXT, md5 TEXT, sha256 TEXT, title TEXT, artist TEXT, comment TEXT)");
            statement.execute("INSERT INTO difficulty_table_entries VALUES (1, 1, '12', 'm', '" + SHA + "', 'Song', '', '')");
        }

        SongDatabaseService songs = SongDatabaseService.forBmzLibrary(libraryDb);
        SongDatabaseService.SongRecord song = songs.findBySha256(SHA);
        assertEquals("Song [ANOTHER]", song.fullTitle());
        assertEquals(12, song.level());

        List<BmtTableReader.BmtTableData> tables = BmzTableReader.readAll(libraryDb);
        assertEquals(1, tables.size());
        assertEquals("sl", tables.get(0).tag);
        assertEquals(1, tables.get(0).folder.size());
        assertEquals("sl12", tables.get(0).folder.get(0).name);
        assertEquals(SHA, tables.get(0).folder.get(0).songs.get(0).sha256);
    }

    private static LocalDateTime at(long epochSeconds) {
        return LocalDateTime.ofEpochSecond(epochSeconds, 0,
                ZoneId.systemDefault().getRules().getOffset(java.time.Instant.ofEpochSecond(epochSeconds)));
    }
}
