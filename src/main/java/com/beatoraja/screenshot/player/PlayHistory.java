package com.beatoraja.screenshot.player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Plays held in time order, with the time-window lookups both players' play logs need, and the
 * column check they use to see which optional columns a history table has.
 */
final class PlayHistory {

    private final List<PlayLogEntry> byDate;

    PlayHistory(List<PlayLogEntry> entries) {
        this.byDate = new ArrayList<>(entries);
        this.byDate.sort(Comparator.comparingLong(PlayLogEntry::dateEpoch));
    }

    /** Plays from {@code fromInclusive} to {@code toInclusive} (epoch seconds), oldest first. */
    List<PlayLogEntry> between(long fromInclusive, long toInclusive) {
        List<PlayLogEntry> found = new ArrayList<>();
        for (int i = firstAtOrAfter(fromInclusive); i < byDate.size(); i++) {
            PlayLogEntry entry = byDate.get(i);
            if (entry.dateEpoch() > toInclusive) {
                break;
            }
            found.add(entry);
        }
        return found;
    }

    /** The latest play at or before {@code toInclusive} (epoch seconds), if any. */
    Optional<PlayLogEntry> latestAtOrBefore(long toInclusive) {
        int index = firstAtOrAfter(toInclusive + 1) - 1;
        return index < 0 ? Optional.empty() : Optional.of(byDate.get(index));
    }

    void clear() {
        byDate.clear();
    }

    /** Index of the first play at or after {@code epoch}. */
    private int firstAtOrAfter(long epoch) {
        int lo = 0;
        int hi = byDate.size();
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (byDate.get(mid).dateEpoch() < epoch) {
                lo = mid + 1;
            } else {
                hi = mid;
            }
        }
        return lo;
    }

    /** The lower-cased column names of {@code table}. */
    static Set<String> columns(Statement statement, String table) throws SQLException {
        Set<String> columns = new HashSet<>();
        try (ResultSet rs = statement.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (rs.next()) {
                columns.add(rs.getString("name").toLowerCase(Locale.ROOT));
            }
        }
        return columns;
    }
}
