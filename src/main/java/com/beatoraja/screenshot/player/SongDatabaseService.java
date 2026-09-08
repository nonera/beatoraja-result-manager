package com.beatoraja.screenshot.player;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SongDatabaseService implements AutoCloseable {

    private final Connection connection;
    private final boolean available;

    public SongDatabaseService(Path songDatabase) throws SQLException {
        if (songDatabase == null || !Files.exists(songDatabase)) {
            connection = null;
            available = false;
            return;
        }
        connection = DriverManager.getConnection("jdbc:sqlite:" + songDatabase.toAbsolutePath());
        available = true;
    }

    public boolean isAvailable() {
        return available;
    }

    public SongRecord findBySha256(String sha256) throws SQLException {
        if (!available || sha256 == null || sha256.isBlank()) {
            return null;
        }

        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT md5, sha256, title, subtitle, level
                FROM song
                WHERE sha256 = ?
                LIMIT 1
                """)) {
            statement.setString(1, sha256);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                String title = rs.getString("title");
                String subtitle = rs.getString("subtitle");
                String fullTitle = subtitle == null || subtitle.isBlank()
                        ? nullToEmpty(title)
                        : nullToEmpty(title) + " " + subtitle.trim();
                return new SongRecord(
                        nullToEmpty(rs.getString("md5")),
                        nullToEmpty(rs.getString("sha256")),
                        fullTitle,
                        rs.getInt("level")
                );
            }
        }
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    @Override
    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    public record SongRecord(String md5, String sha256, String fullTitle, int level) {
    }
}
