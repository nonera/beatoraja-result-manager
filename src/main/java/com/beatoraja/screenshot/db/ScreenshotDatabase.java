package com.beatoraja.screenshot.db;

import com.beatoraja.screenshot.table.TableLookupService;
import com.beatoraja.screenshot.util.AppPaths;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ScreenshotDatabase implements AutoCloseable {

    private static final DateTimeFormatter DB_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Connection connection;

    public ScreenshotDatabase() throws SQLException {
        Path dbPath = AppPaths.appDataDir().resolve("screenshots.db");
        connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath.toAbsolutePath());
        initSchema();
        migrateSchema();
    }

    private void initSchema() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS screenshots (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        file_path TEXT NOT NULL UNIQUE,
                        file_name TEXT NOT NULL,
                        captured_at TEXT,
                        title TEXT,
                        sha256 TEXT,
                        md5 TEXT,
                        table_symbol TEXT,
                        table_level_num TEXT,
                        level TEXT,
                        rank TEXT,
                        clear_type TEXT,
                        state_label TEXT,
                        post_notation TEXT,
                        available_notations TEXT,
                        notation_edited INTEGER NOT NULL DEFAULT 0,
                        resolved_from_player INTEGER NOT NULL DEFAULT 0,
                        created_at TEXT NOT NULL,
                        updated_at TEXT NOT NULL
                    )
                    """);
            statement.execute("""
                    CREATE INDEX IF NOT EXISTS idx_screenshots_captured_at
                    ON screenshots(captured_at DESC)
                    """);
        }
    }

    private void migrateSchema() throws SQLException {
        addColumnIfMissing("table_symbol", "TEXT");
        addColumnIfMissing("table_level_num", "TEXT");
        addColumnIfMissing("post_notation", "TEXT");
        addColumnIfMissing("available_notations", "TEXT");
        addColumnIfMissing("notation_edited", "INTEGER NOT NULL DEFAULT 0");
        addColumnIfMissing("sha256", "TEXT");
        addColumnIfMissing("md5", "TEXT");
        addColumnIfMissing("resolved_from_player", "INTEGER NOT NULL DEFAULT 0");
        addColumnIfMissing("flagged", "INTEGER NOT NULL DEFAULT 0");
    }

    private void addColumnIfMissing(String column, String definition) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE screenshots ADD COLUMN " + column + " " + definition);
        } catch (SQLException e) {
            if (e.getMessage() == null || !e.getMessage().contains("duplicate column")) {
                throw e;
            }
        }
    }

    public void upsert(TableLookupService.EnrichedScreenshot enriched) throws SQLException {
        String now = LocalDateTime.now().format(DB_TIME);
        var entry = enriched.entry();
        String capturedAt = entry.getCapturedAt() == null ? null : entry.getCapturedAt().format(DB_TIME);
        String availableJson = writeJson(enriched.mergedAvailableNotations());
        String title = enriched.resolvedTitle() == null || enriched.resolvedTitle().isBlank()
                ? entry.getTitle()
                : enriched.resolvedTitle();

        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO screenshots (
                    file_path, file_name, captured_at, title, sha256, md5, table_symbol, table_level_num, level,
                    rank, clear_type, state_label, post_notation, available_notations,
                    notation_edited, resolved_from_player, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, ?, ?, ?)
                ON CONFLICT(file_path) DO UPDATE SET
                    file_name = excluded.file_name,
                    captured_at = excluded.captured_at,
                    title = excluded.title,
                    sha256 = excluded.sha256,
                    md5 = excluded.md5,
                    table_symbol = CASE WHEN screenshots.notation_edited = 1 THEN screenshots.table_symbol ELSE excluded.table_symbol END,
                    table_level_num = excluded.table_level_num,
                    level = excluded.level,
                    rank = excluded.rank,
                    clear_type = excluded.clear_type,
                    state_label = excluded.state_label,
                    post_notation = CASE WHEN screenshots.notation_edited = 1 THEN screenshots.post_notation ELSE excluded.post_notation END,
                    available_notations = excluded.available_notations,
                    resolved_from_player = excluded.resolved_from_player,
                    updated_at = excluded.updated_at
                """)) {
            statement.setString(1, entry.getFilePath().toAbsolutePath().toString());
            statement.setString(2, entry.getFileName());
            statement.setString(3, capturedAt);
            statement.setString(4, title);
            statement.setString(5, enriched.sha256());
            statement.setString(6, enriched.md5());
            statement.setString(7, enriched.tableSymbol());
            statement.setString(8, enriched.tableLevelNum());
            statement.setString(9, enriched.level());
            statement.setString(10, entry.getRank());
            statement.setString(11, entry.getClearType());
            statement.setString(12, entry.getStateLabel());
            statement.setString(13, enriched.defaultPostNotation());
            statement.setString(14, availableJson);
            statement.setInt(15, enriched.resolvedFromPlayerData() ? 1 : 0);
            statement.setString(16, now);
            statement.setString(17, now);
            statement.executeUpdate();
        }
    }

    public void syncAll(List<TableLookupService.EnrichedScreenshot> entries) throws SQLException {
        connection.setAutoCommit(false);
        try {
            for (TableLookupService.EnrichedScreenshot entry : entries) {
                upsert(entry);
            }
            removeMissingFiles();
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public void updateFlagged(long id, boolean flagged) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                UPDATE screenshots
                SET flagged = ?, updated_at = ?
                WHERE id = ?
                """)) {
            statement.setInt(1, flagged ? 1 : 0);
            statement.setString(2, LocalDateTime.now().format(DB_TIME));
            statement.setLong(3, id);
            statement.executeUpdate();
        }
    }

    public void updateNotation(long id, String tableSymbol, String postNotation) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                UPDATE screenshots
                SET table_symbol = ?, post_notation = ?, notation_edited = 1, updated_at = ?
                WHERE id = ?
                """)) {
            statement.setString(1, tableSymbol == null ? "" : tableSymbol);
            statement.setString(2, postNotation == null ? "" : postNotation);
            statement.setString(3, LocalDateTime.now().format(DB_TIME));
            statement.setLong(4, id);
            statement.executeUpdate();
        }
    }

    public ScreenshotRecord findByFilePath(Path filePath) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT id, file_path, file_name, captured_at, title, sha256, md5, table_symbol, table_level_num, level,
                       rank, clear_type, state_label, post_notation, available_notations, notation_edited,
                       resolved_from_player, flagged
                FROM screenshots
                WHERE file_path = ?
                """)) {
            statement.setString(1, filePath.toAbsolutePath().toString());
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapRecord(rs);
                }
            }
        }
        return null;
    }

    private void removeMissingFiles() throws SQLException {
        List<Long> removeIds = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT id, file_path FROM screenshots")) {
            while (rs.next()) {
                if (!Files.exists(Path.of(rs.getString("file_path")))) {
                    removeIds.add(rs.getLong("id"));
                }
            }
        }

        if (removeIds.isEmpty()) {
            return;
        }

        try (PreparedStatement delete = connection.prepareStatement("DELETE FROM screenshots WHERE id = ?")) {
            for (Long id : removeIds) {
                delete.setLong(1, id);
                delete.addBatch();
            }
            delete.executeBatch();
        }
    }

    public List<ScreenshotRecord> findAll() throws SQLException {
        List<ScreenshotRecord> records = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("""
                     SELECT id, file_path, file_name, captured_at, title, sha256, md5, table_symbol, table_level_num, level,
                            rank, clear_type, state_label, post_notation, available_notations, notation_edited,
                            resolved_from_player, flagged
                     FROM screenshots
                     ORDER BY captured_at DESC, id DESC
                     """)) {
            while (rs.next()) {
                records.add(mapRecord(rs));
            }
        }
        return records;
    }

    private ScreenshotRecord mapRecord(ResultSet rs) throws SQLException {
        String capturedAtRaw = rs.getString("captured_at");
        LocalDateTime capturedAt = capturedAtRaw == null ? null : LocalDateTime.parse(capturedAtRaw, DB_TIME);
        return new ScreenshotRecord(
                rs.getLong("id"),
                Path.of(rs.getString("file_path")),
                rs.getString("file_name"),
                capturedAt,
                nullToEmpty(rs.getString("title")),
                nullToEmpty(rs.getString("sha256")),
                nullToEmpty(rs.getString("md5")),
                nullToEmpty(rs.getString("table_symbol")),
                nullToEmpty(rs.getString("table_level_num")),
                nullToEmpty(rs.getString("level")),
                nullToEmpty(rs.getString("rank")),
                nullToEmpty(rs.getString("clear_type")),
                nullToEmpty(rs.getString("state_label")),
                nullToEmpty(rs.getString("post_notation")),
                readJsonList(rs.getString("available_notations")),
                rs.getInt("notation_edited") == 1,
                rs.getInt("resolved_from_player") == 1,
                rs.getInt("flagged") == 1
        );
    }

    private String writeJson(List<String> values) {
        try {
            return MAPPER.writeValueAsString(values == null ? List.of() : values);
        } catch (Exception e) {
            return "[]";
        }
    }

    private List<String> readJsonList(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return MAPPER.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            return List.of();
        }
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    public static String formatDate(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DISPLAY_TIME);
    }

    @Override
    public void close() throws SQLException {
        connection.close();
    }
}
