package com.beatoraja.screenshot.player;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PlayerPlayLogService implements AutoCloseable {

    private static final long BEFORE_WINDOW_SECONDS = 180;
    private static final long AFTER_WINDOW_SECONDS = 30;

    private final boolean available;
    private final List<PlayLogEntry> entriesByDate = new ArrayList<>();

    public PlayerPlayLogService(Path scoreDataLogDb) throws SQLException {
        if (scoreDataLogDb == null || !Files.exists(scoreDataLogDb)) {
            available = false;
            return;
        }

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + scoreDataLogDb.toAbsolutePath());
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("""
                     SELECT sha256, clear, date
                     FROM scoredatalog
                     """)) {
            while (rs.next()) {
                entriesByDate.add(new PlayLogEntry(
                        rs.getString("sha256"),
                        rs.getInt("clear"),
                        rs.getLong("date")
                ));
            }
        }
        entriesByDate.sort(Comparator.comparingLong(PlayLogEntry::dateEpoch));
        available = true;
    }

    public boolean isAvailable() {
        return available;
    }

    public List<PlayLogEntry> findNear(LocalDateTime capturedAt, Integer expectedClearId) {
        if (!available || capturedAt == null) {
            return List.of();
        }

        long center = capturedAt.atZone(ZoneId.systemDefault()).toEpochSecond();
        long from = center - BEFORE_WINDOW_SECONDS;
        long to = center + AFTER_WINDOW_SECONDS;

        int fromIndex = lowerBound(from);
        List<PlayLogEntry> entries = new ArrayList<>();
        for (int i = fromIndex; i < entriesByDate.size(); i++) {
            PlayLogEntry entry = entriesByDate.get(i);
            if (entry.dateEpoch() > to) {
                break;
            }
            entries.add(entry);
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

    private int lowerBound(long fromInclusive) {
        int lo = 0;
        int hi = entriesByDate.size();
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (entriesByDate.get(mid).dateEpoch() < fromInclusive) {
                lo = mid + 1;
            } else {
                hi = mid;
            }
        }
        return lo;
    }

    @Override
    public void close() {
        entriesByDate.clear();
    }

    public record PlayLogEntry(String sha256, int clearId, long dateEpoch) {
    }
}
