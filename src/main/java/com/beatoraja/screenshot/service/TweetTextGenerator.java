package com.beatoraja.screenshot.service;

import com.beatoraja.screenshot.db.ScreenshotRecord;
import com.beatoraja.screenshot.model.ScreenshotEntry;

import java.util.List;

public final class TweetTextGenerator {

    private TweetTextGenerator() {
    }

    public static String generate(ScreenshotEntry entry) {
        return generate(entry, "");
    }

    public static String generate(ScreenshotEntry entry, String postNotation) {
        return generate(List.of(entry), postNotation);
    }

    public static String generate(ScreenshotRecord record) {
        if (record == null) {
            return "";
        }
        return buildResult(record.postNotation(), record.title(), record.clearType(), record.rank(), record.fileName());
    }

    public static String generate(List<ScreenshotEntry> entries, String postNotation) {
        if (entries == null || entries.isEmpty()) {
            return "";
        }
        ScreenshotEntry entry = entries.get(0);
        return buildResult(
                postNotation,
                entry.getTitle(),
                entry.getClearType(),
                entry.getRank(),
                entry.getFileName()
        );
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
