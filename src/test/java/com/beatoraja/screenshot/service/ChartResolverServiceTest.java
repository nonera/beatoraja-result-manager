package com.beatoraja.screenshot.service;

import com.beatoraja.screenshot.config.AppConfig;
import com.beatoraja.screenshot.model.EnrichedScreenshot;
import com.beatoraja.screenshot.parser.FilenameParser;
import com.beatoraja.screenshot.player.PlayerKind;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChartResolverServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void splitsBmzTableLabelOffTheTitle() throws Exception {
        try (ChartResolverService service = bmzServiceWithSatelliteTable()) {
            EnrichedScreenshot result = enrich(service, "20261121_203045_123_sl12_Song_[ANOTHER]_Hard.png");
            assertEquals("Song_[ANOTHER]", result.entry().getTitle());
            assertEquals("sl12", result.entry().getRawTableFolder());
            assertEquals("sl12", result.defaultPostNotation());

            EnrichedScreenshot decide = enrich(service, "20261121_203045_123_Decide_sl12_Song.png");
            assertEquals("Song", decide.entry().getTitle());
            assertEquals("sl12", decide.entry().getRawTableFolder());
        }
    }

    @Test
    void keepsTitleWhoseFirstPartIsNotATableLevel() throws Exception {
        try (ChartResolverService service = bmzServiceWithSatelliteTable()) {
            EnrichedScreenshot unknownLevel = enrich(service, "20261121_203045_123_sl99_Song_Hard.png");
            assertEquals("sl99_Song", unknownLevel.entry().getTitle());
            assertEquals("", unknownLevel.entry().getRawTableFolder());

            EnrichedScreenshot plain = enrich(service, "20261121_203045_123_Foo_Bar_Hard.png");
            assertEquals("Foo_Bar", plain.entry().getTitle());

            EnrichedScreenshot older = enrich(service, "20261105_203045_123_Song_Hard.png");
            assertEquals("Song", older.entry().getTitle());
        }
    }

    private ChartResolverService bmzServiceWithSatelliteTable() throws Exception {
        Files.writeString(tempDir.resolve("config.toml"), "version = 1\n");
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + tempDir.resolve("library.db"));
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE charts (id INTEGER PRIMARY KEY, sha256 TEXT, md5 TEXT, title TEXT, subtitle TEXT, play_level TEXT)");
            statement.execute("CREATE TABLE difficulty_tables (id INTEGER PRIMARY KEY, source_url TEXT, head_url TEXT, name TEXT, symbol TEXT, level_order TEXT, fetched_at INTEGER)");
            statement.execute("INSERT INTO difficulty_tables VALUES (1, 'u', 'h', 'Satellite', 'sl', '[\"12\"]', 0)");
            statement.execute("CREATE TABLE difficulty_table_entries (id INTEGER PRIMARY KEY, table_id INTEGER, level TEXT, md5 TEXT, sha256 TEXT, title TEXT, artist TEXT, comment TEXT)");
            statement.execute("INSERT INTO difficulty_table_entries VALUES (1, 1, '12', 'm', 's', 'Other', '', '')");
        }
        AppConfig config = new AppConfig();
        config.setBeatorajaDirectory(tempDir.toString());
        config.setPlayerKind(PlayerKind.BMZ);
        ChartResolverService service = new ChartResolverService();
        service.reload(config);
        return service;
    }

    private EnrichedScreenshot enrich(ChartResolverService service, String fileName) {
        return service.enrich(FilenameParser.parse(tempDir.resolve(fileName), PlayerKind.BMZ));
    }
}
