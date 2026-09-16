package com.beatoraja.screenshot.service;

import com.beatoraja.screenshot.db.ScreenshotRecord;
import com.beatoraja.screenshot.model.ScreenshotEntry;

import java.util.ArrayList;
import java.util.List;

public final class TweetTextGenerator {

    private TweetTextGenerator() {
    }

    public static String generate(ScreenshotEntry entry) {
        return generate(entry, "");
    }

    public static String generate(ScreenshotEntry entry, String postNotation) {
        return buildResult(
                postNotation,
                entry.getTitle(),
                entry.getClearType(),
                entry.getRank(),
                entry.getFileName()
        );
    }

    /**
     * Same as {@link #generate(ScreenshotEntry, String)}, but truncates the title to at most
     * {@code maxTitleWeightedLength} Twitter-weighted units (CJK counts as two), appending an
     * ellipsis when the title had to be shortened. Used to fit a caption under the tweet
     * length limit without touching the notation/clear type/rank.
     */
    public static String generate(ScreenshotEntry entry, String postNotation, int maxTitleWeightedLength) {
        return buildResult(
                postNotation,
                truncateToWeightedLength(entry.getTitle(), maxTitleWeightedLength),
                entry.getClearType(),
                entry.getRank(),
                entry.getFileName()
        );
    }

    private static String truncateToWeightedLength(String text, int maxWeighted) {
        if (text == null || TweetTextLimits.weightedLength(text) <= maxWeighted) {
            return text;
        }
        int budget = Math.max(0, maxWeighted - 1); // reserve 1 unit for the ellipsis
        StringBuilder builder = new StringBuilder();
        int used = 0;
        for (int i = 0; i < text.length(); ) {
            int codePoint = text.codePointAt(i);
            int width = TweetTextLimits.weightedLength(new String(Character.toChars(codePoint)));
            if (used + width > budget) {
                break;
            }
            builder.appendCodePoint(codePoint);
            used += width;
            i += Character.charCount(codePoint);
        }
        return builder.append('…').toString();
    }

    public static String generate(ScreenshotRecord record) {
        if (record == null) {
            return "";
        }
        return buildResult(record.postNotation(), record.title(), record.clearType(), record.rank(), record.fileName());
    }

    /**
     * Builds one line per entry (in order), joined with newlines, so every selected
     * screenshot's info is included instead of only the first one.
     */
    public static String generate(List<ScreenshotEntry> entries, List<String> postNotations) {
        if (entries == null || entries.isEmpty()) {
            return "";
        }
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            String notation = postNotations != null && i < postNotations.size() ? postNotations.get(i) : "";
            lines.add(generate(entries.get(i), notation));
        }
        return String.join("\n", lines);
    }

    private static String buildResult(String postNotation, String title, String clearType, String rank, String fallback) {
        StringBuilder builder = new StringBuilder();
        if (postNotation != null && !postNotation.isBlank()) {
            builder.append(postNotation.trim());
        }
        if (title != null && !title.isBlank()) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(title.trim());
        }
        if (clearType != null && !clearType.isBlank()) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(clearType.trim());
        }
        if (rank != null && !rank.isBlank()) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(rank.trim());
        }
        String text = builder.toString().trim();
        return text.isBlank() ? fallback : text;
    }
}
