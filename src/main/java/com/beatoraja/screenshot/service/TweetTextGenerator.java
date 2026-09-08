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
