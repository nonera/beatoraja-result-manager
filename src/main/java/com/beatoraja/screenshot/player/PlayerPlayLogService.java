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

public class PlayerPlayLogService implements PlayLogSource {

    private static final long BEFORE_WINDOW_SECONDS = 180;
    private static final long AFTER_WINDOW_SECONDS = 30;

    private static final List<String> SCORE_COLUMNS = List.of(
            "epg", "lpg", "egr", "lgr", "egd", "lgd", "ebd", "lbd", "epr", "lpr", "ems", "lms", "notes");

    private final boolean available;
    private final PlayHistory history;

    public PlayerPlayLogService(Path scoreDataLogDb) throws SQLException {
        if (scoreDataLogDb == null || !Files.exists(scoreDataLogDb)) {
            available = false;
            history = new PlayHistory(List.of());
            return;
        }
        List<PlayLogEntry> entries = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + scoreDataLogDb.toAbsolutePath());
             Statement statement = connection.createStatement()) {
            boolean hasScoreColumns = PlayHistory.columns(statement, "scoredatalog").containsAll(SCORE_COLUMNS);
            String columns = hasScoreColumns
                    ? "sha256, clear, date, " + String.join(", ", SCORE_COLUMNS)
                    : "sha256, clear, date";
            try (ResultSet rs = statement.executeQuery("SELECT " + columns + " FROM scoredatalog")) {
                while (rs.next()) {
                    entries.add(new PlayLogEntry(
                            rs.getString("sha256"),
                            rs.getInt("clear"),
                            rs.getLong("date"),
                            hasScoreColumns ? readScore(rs) : null
                    ));
                }
            }
        }
        history = new PlayHistory(entries);
        available = true;
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

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public List<PlayLogEntry> findNear(LocalDateTime capturedAt, Integer expectedClearId) {
        if (!available || capturedAt == null) {
            return List.of();
        }

        long center = capturedAt.atZone(ZoneId.systemDefault()).toEpochSecond();
        long from = center - BEFORE_WINDOW_SECONDS;
        long to = center + AFTER_WINDOW_SECONDS;

        List<PlayLogEntry> entries = history.between(from, to);

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
    public void close() {
        history.clear();
    }
}
