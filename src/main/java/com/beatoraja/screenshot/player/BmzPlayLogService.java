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

/**
 * Play history from BMZ Player's {@code profiles/<id>/score.db} ({@code score_history} table).
 * BMZ screenshots carry no song or lamp in their file name, so a screenshot is matched to the
 * latest play recorded shortly before it: {@code played_at} is stamped when the play finishes,
 * just before the result screen appears.
 */
public class BmzPlayLogService implements PlayLogSource {

    /** How long after a play's finish a screenshot is still taken to be of its result screen. */
    private static final long AFTER_PLAY_WINDOW_SECONDS = 600;
    /** Slack for played_at landing a moment after the screenshot's millisecond stamp. */
    private static final long CLOCK_SLACK_SECONDS = 5;

    private static final List<String> JUDGE_COLUMNS = List.of(
            "fast_pgreat", "slow_pgreat", "fast_great", "slow_great", "fast_good", "slow_good",
            "fast_bad", "slow_bad", "fast_poor", "slow_poor", "fast_empty_poor", "slow_empty_poor",
            "total_notes");

    private final boolean available;
    private final List<PlayerPlayLogService.PlayLogEntry> entriesByDate = new ArrayList<>();

    public BmzPlayLogService(Path scoreDb) throws SQLException {
        if (scoreDb == null || !Files.exists(scoreDb)) {
            available = false;
            return;
        }

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + scoreDb.toAbsolutePath());
             Statement statement = connection.createStatement()) {
            Set<String> tableColumns = readColumns(statement);
            boolean hasScoreColumns = tableColumns.containsAll(JUDGE_COLUMNS);
            String columns = hasScoreColumns
                    ? "chart_sha256, clear_type, played_at, " + String.join(", ", JUDGE_COLUMNS)
                    : "chart_sha256, clear_type, played_at";
            String where = tableColumns.contains("autoplay") ? " WHERE autoplay = 0" : "";
            try (ResultSet rs = statement.executeQuery("SELECT " + columns + " FROM score_history" + where)) {
                while (rs.next()) {
                    Integer clearId = ClearTypeMapper.fromBmzLabel(rs.getString("clear_type"));
                    entriesByDate.add(new PlayerPlayLogService.PlayLogEntry(
                            rs.getString("chart_sha256"),
                            clearId == null ? 0 : clearId,
                            rs.getLong("played_at"),
                            hasScoreColumns ? readScore(rs) : null
                    ));
                }
            }
        }
        entriesByDate.sort(Comparator.comparingLong(PlayerPlayLogService.PlayLogEntry::dateEpoch));
        available = true;
    }

    private static Set<String> readColumns(Statement statement) throws SQLException {
        Set<String> columns = new HashSet<>();
        try (ResultSet rs = statement.executeQuery("PRAGMA table_info(score_history)")) {
            while (rs.next()) {
                columns.add(rs.getString("name").toLowerCase(Locale.ROOT));
            }
        }
        return columns;
    }

    private static PlayScore readScore(ResultSet rs) throws SQLException {
        return new PlayScore(
                rs.getInt("fast_pgreat"), rs.getInt("slow_pgreat"),
                rs.getInt("fast_great"), rs.getInt("slow_great"),
                rs.getInt("fast_good"), rs.getInt("slow_good"),
                rs.getInt("fast_bad"), rs.getInt("slow_bad"),
                rs.getInt("fast_poor"), rs.getInt("slow_poor"),
                rs.getInt("fast_empty_poor"), rs.getInt("slow_empty_poor"),
                rs.getInt("total_notes")
        );
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    /** At most one entry: the latest play finished within the window before {@code capturedAt}. */
    @Override
    public List<PlayerPlayLogService.PlayLogEntry> findNear(LocalDateTime capturedAt, Integer expectedClearId) {
        if (!available || capturedAt == null) {
            return List.of();
        }
        long shotEpoch = capturedAt.atZone(ZoneId.systemDefault()).toEpochSecond();
        long from = shotEpoch - AFTER_PLAY_WINDOW_SECONDS;
        long to = shotEpoch + CLOCK_SLACK_SECONDS;
        int last = upperBound(to) - 1;
        if (last < 0) {
            return List.of();
        }
        PlayerPlayLogService.PlayLogEntry entry = entriesByDate.get(last);
        return entry.dateEpoch() >= from ? List.of(entry) : List.of();
    }

    /** Index of the first entry played after {@code toInclusive}. */
    private int upperBound(long toInclusive) {
        int lo = 0;
        int hi = entriesByDate.size();
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (entriesByDate.get(mid).dateEpoch() <= toInclusive) {
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
}
