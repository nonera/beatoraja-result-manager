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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class PlayerPlayLogService implements AutoCloseable {

    private static final long BEFORE_WINDOW_SECONDS = 180;
    private static final long AFTER_WINDOW_SECONDS = 30;

    private static final List<String> SCORE_COLUMNS = List.of(
            "epg", "lpg", "egr", "lgr", "egd", "lgd", "ebd", "lbd", "epr", "lpr", "ems", "lms", "notes");

    private final boolean available;
    private final List<PlayLogEntry> entriesByDate = new ArrayList<>();

    public PlayerPlayLogService(Path scoreDataLogDb) throws SQLException {
        if (scoreDataLogDb == null || !Files.exists(scoreDataLogDb)) {
            available = false;
            return;
        }

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + scoreDataLogDb.toAbsolutePath());
             Statement statement = connection.createStatement()) {
            boolean hasScoreColumns = readColumns(statement).containsAll(SCORE_COLUMNS);
            String columns = hasScoreColumns
                    ? "sha256, clear, date, " + String.join(", ", SCORE_COLUMNS)
                    : "sha256, clear, date";
            try (ResultSet rs = statement.executeQuery("SELECT " + columns + " FROM scoredatalog")) {
                while (rs.next()) {
                    entriesByDate.add(new PlayLogEntry(
                            rs.getString("sha256"),
                            rs.getInt("clear"),
                            rs.getLong("date"),
                            hasScoreColumns ? readScore(rs) : null
                    ));
                }
            }
        }
        entriesByDate.sort(Comparator.comparingLong(PlayLogEntry::dateEpoch));
        available = true;
    }

    private static Set<String> readColumns(Statement statement) throws SQLException {
        Set<String> columns = new HashSet<>();
        try (ResultSet rs = statement.executeQuery("PRAGMA table_info(scoredatalog)")) {
            while (rs.next()) {
                columns.add(rs.getString("name").toLowerCase(Locale.ROOT));
            }
        }
        return columns;
    }

    private static PlayScore readScore(ResultSet rs) throws SQLException {
        return new PlayScore(
                rs.getInt("epg"), rs.getInt("lpg"),
                rs.getInt("egr"), rs.getInt("lgr"),
                rs.getInt("egd"), rs.getInt("lgd"),
                rs.getInt("ebd"), rs.getInt("lbd"),
                rs.getInt("epr"), rs.getInt("lpr"),
                rs.getInt("ems"), rs.getInt("lms"),
                rs.getInt("notes")
        );
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

    /** {@code score} is null when the scoredatalog table has no judge-count columns. */
    public record PlayLogEntry(String sha256, int clearId, long dateEpoch, PlayScore score) {
    }
}
