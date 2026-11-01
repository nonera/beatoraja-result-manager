package com.beatoraja.screenshot.table;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads the difficulty tables BMZ Player keeps in library.db ({@code difficulty_tables} /
 * {@code difficulty_table_entries}) into the same shape as beatoraja's .bmt files, so
 * {@link DifficultyTableRegistry} handles both alike. bmz stores the header's {@code tag}
 * (falling back to {@code symbol}) in the {@code symbol} column, which is what beatoraja
 * uses as the .bmt tag and folder-name prefix ("sl" + "12" → "sl12").
 */
public final class BmzTableReader {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private BmzTableReader() {
    }

    public static List<BmtTableReader.BmtTableData> readAll(Path libraryDb) throws SQLException {
        List<BmtTableReader.BmtTableData> tables = new ArrayList<>();
        if (libraryDb == null || !Files.exists(libraryDb)) {
            return tables;
        }

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + libraryDb.toAbsolutePath());
             Statement statement = connection.createStatement()) {
            if (!hasTable(statement, "difficulty_tables") || !hasTable(statement, "difficulty_table_entries")) {
                return tables;
            }

            Map<Long, BmtTableReader.BmtTableData> byId = new LinkedHashMap<>();
            Map<Long, Map<String, BmtTableReader.BmtFolder>> foldersById = new LinkedHashMap<>();
            try (ResultSet rs = statement.executeQuery(
                    "SELECT id, name, symbol, source_url, level_order FROM difficulty_tables ORDER BY id")) {
                while (rs.next()) {
                    long id = rs.getLong("id");
                    BmtTableReader.BmtTableData table = new BmtTableReader.BmtTableData();
                    table.name = nullToEmpty(rs.getString("name"));
                    table.tag = nullToEmpty(rs.getString("symbol"));
                    table.url = nullToEmpty(rs.getString("source_url"));
                    table.folder = new ArrayList<>();
                    byId.put(id, table);

                    // Pre-create folders in the table's own level order so they list the way bmz shows them.
                    Map<String, BmtTableReader.BmtFolder> folders = new LinkedHashMap<>();
                    for (String level : parseLevelOrder(rs.getString("level_order"))) {
                        folders.computeIfAbsent(level, l -> newFolder(table.tag, l));
                    }
                    foldersById.put(id, folders);
                }
            }

            try (ResultSet rs = statement.executeQuery(
                    "SELECT table_id, level, md5, sha256, title FROM difficulty_table_entries ORDER BY id")) {
                while (rs.next()) {
                    long tableId = rs.getLong("table_id");
                    BmtTableReader.BmtTableData table = byId.get(tableId);
                    if (table == null) {
                        continue;
                    }
                    String level = nullToEmpty(rs.getString("level"));
                    BmtTableReader.BmtFolder folder = foldersById.get(tableId)
                            .computeIfAbsent(level, l -> newFolder(table.tag, l));
                    BmtTableReader.BmtSong song = new BmtTableReader.BmtSong();
                    song.title = nullToEmpty(rs.getString("title"));
                    song.md5 = nullToEmpty(rs.getString("md5"));
                    song.sha256 = nullToEmpty(rs.getString("sha256"));
                    folder.songs.add(song);
                }
            }

            for (Map.Entry<Long, BmtTableReader.BmtTableData> entry : byId.entrySet()) {
                for (BmtTableReader.BmtFolder folder : foldersById.get(entry.getKey()).values()) {
                    if (!folder.songs.isEmpty()) {
                        entry.getValue().folder.add(folder);
                    }
                }
                tables.add(entry.getValue());
            }
        }
        return tables;
    }

    private static BmtTableReader.BmtFolder newFolder(String tag, String level) {
        BmtTableReader.BmtFolder folder = new BmtTableReader.BmtFolder();
        folder.name = tag + level;
        folder.songs = new ArrayList<>();
        return folder;
    }

    /** level_order is a JSON array of strings (serde_json of {@code Vec<String>}). */
    private static List<String> parseLevelOrder(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            JsonNode node = MAPPER.readTree(json);
            if (!node.isArray()) {
                return List.of();
            }
            return MAPPER.convertValue(node, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            return List.of();
        }
    }

    private static boolean hasTable(Statement statement, String name) throws SQLException {
        try (ResultSet rs = statement.executeQuery(
                "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = '" + name + "'")) {
            return rs.next();
        }
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
