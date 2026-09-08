package com.beatoraja.screenshot.player;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class SongDatabaseService implements AutoCloseable {

    private final boolean available;
    private final Map<String, SongRecord> bySha256 = new HashMap<>();

    public SongDatabaseService(Path songDatabase) throws SQLException {
        if (songDatabase == null || !Files.exists(songDatabase)) {
            available = false;
            return;
        }

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + songDatabase.toAbsolutePath());
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("""
                     SELECT md5, sha256, title, subtitle, level
                     FROM song
                     """)) {
            while (rs.next()) {
                String sha256 = rs.getString("sha256");
                if (sha256 == null || sha256.isBlank() || bySha256.containsKey(sha256)) {
                    continue;
                }
                String title = rs.getString("title");
                String subtitle = rs.getString("subtitle");
                String fullTitle = subtitle == null || subtitle.isBlank()
                        ? nullToEmpty(title)
                        : nullToEmpty(title) + " " + subtitle.trim();
                bySha256.put(sha256, new SongRecord(
                        nullToEmpty(rs.getString("md5")),
                        sha256,
                        fullTitle,
                        rs.getInt("level")
                ));
            }
        }
        available = true;
    }

    public boolean isAvailable() {
        return available;
    }

    public SongRecord findBySha256(String sha256) {
        if (!available || sha256 == null || sha256.isBlank()) {
            return null;
        }
        return bySha256.get(sha256);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    @Override
    public void close() {
        bySha256.clear();
    }

    public record SongRecord(String md5, String sha256, String fullTitle, int level) {
    }
}
