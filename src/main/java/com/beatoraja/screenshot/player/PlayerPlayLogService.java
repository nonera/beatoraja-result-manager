package com.beatoraja.screenshot.player;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PlayerPlayLogService implements AutoCloseable {

    private static final long BEFORE_WINDOW_SECONDS = 180;
    private static final long AFTER_WINDOW_SECONDS = 30;

    private final Connection connection;
    private final boolean available;

    public PlayerPlayLogService(Path scoreDataLogDb) throws SQLException {
        if (scoreDataLogDb == null || !Files.exists(scoreDataLogDb)) {
            connection = null;
            available = false;
            return;
        }
        connection = DriverManager.getConnection("jdbc:sqlite:" + scoreDataLogDb.toAbsolutePath());
        available = true;
    }

    public boolean isAvailable() {
        return available;
    }

    public List<PlayLogEntry> findNear(LocalDateTime capturedAt, Integer expectedClearId) throws SQLException {
        if (!available || capturedAt == null) {
            return List.of();
        }

        long center = capturedAt.atZone(ZoneId.systemDefault()).toEpochSecond();
        long from = center - BEFORE_WINDOW_SECONDS;
        long to = center + AFTER_WINDOW_SECONDS;

        List<PlayLogEntry> entries = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT sha256, clear, date
                FROM scoredatalog
                WHERE date BETWEEN ? AND ?
                ORDER BY date DESC
                """)) {
            statement.setLong(1, from);
            statement.setLong(2, to);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    entries.add(new PlayLogEntry(
                            rs.getString("sha256"),
                            rs.getInt("clear"),
                            rs.getLong("date")
                    ));
                }
            }
        }

        if (expectedClearId != null) {
            List<PlayLogEntry> filtered = entries.stream()
                    .filter(entry -> entry.clearId() == expectedClearId)
                    .toList();
            if (!filtered.isEmpty()) {
                entries = new ArrayList<>(filtered);
            }
        }

        entries.sort(Comparator.comparingLong(entry -> Math.abs(entry.dateEpoch() - center)));
        return entries;
    }

    @Override
    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    public record PlayLogEntry(String sha256, int clearId, long dateEpoch) {
    }
}
