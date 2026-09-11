package com.beatoraja.screenshot.table;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TableLevelParser {

    private static final Pattern LEVEL_ONLY = Pattern.compile("^LEVEL(\\d+|\\?\\?\\?)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern TRAILING_LEVEL = Pattern.compile("^(\\d+(?:\\.\\d+)?|\\?\\?\\?)$");

    private static final List<String> DEFAULT_TAGS = List.of(
            "DPBMS★", "DPBMS☆", "SNJ★", "SNJ☆", "sl", "st", "δ", "▼", "縦", "☆", "★"
    );

    private TableLevelParser() {
    }

    public static ParsedTableLevel parse(String folderName, List<String> knownTags) {
        if (folderName == null || folderName.isBlank()) {
            return ParsedTableLevel.empty();
        }

        String value = folderName.trim();
        Matcher levelOnly = LEVEL_ONLY.matcher(value);
        if (levelOnly.matches()) {
            return new ParsedTableLevel("", normalizeLevel(levelOnly.group(1)), true);
        }

        List<String> tags = mergeTags(knownTags);
        for (String tag : tags) {
            if (!value.startsWith(tag)) {
                continue;
            }
            String remainder = value.substring(tag.length()).trim();
            if (remainder.isEmpty()) {
                return new ParsedTableLevel(tag, "", false);
            }
            Matcher trailing = TRAILING_LEVEL.matcher(remainder);
            if (trailing.matches()) {
                return new ParsedTableLevel(tag, normalizeLevel(trailing.group(1)), false);
            }
        }

        int splitIndex = findLevelStartIndex(value);
        if (splitIndex <= 0 || splitIndex >= value.length()) {
            return new ParsedTableLevel(value, "", false);
        }

        String symbol = value.substring(0, splitIndex);
        String level = normalizeLevel(value.substring(splitIndex));
        return new ParsedTableLevel(symbol, level, false);
    }

    private static List<String> mergeTags(List<String> knownTags) {
        List<String> tags = new ArrayList<>(DEFAULT_TAGS);
        if (knownTags != null) {
            for (String tag : knownTags) {
                if (tag != null && !tag.isBlank() && !tags.contains(tag)) {
                    tags.add(tag);
                }
            }
        }
        tags.sort(Comparator.comparingInt(String::length).reversed());
        return tags;
    }

    private static int findLevelStartIndex(String value) {
        for (int i = value.length() - 1; i >= 0; i--) {
            char ch = value.charAt(i);
            if (Character.isDigit(ch) || ch == '?' || ch == '.') {
                continue;
            }
            return i + 1;
        }
        return -1;
    }

    private static String normalizeLevel(String level) {
        if (level == null) {
            return "";
        }
        return level.toUpperCase(Locale.ROOT).equals("???") ? "???" : level;
    }

    public record ParsedTableLevel(String symbol, String level, boolean bmsLevelOnly) {
        public static ParsedTableLevel empty() {
            return new ParsedTableLevel("", "", false);
        }

        public String notation() {
            if (symbol.isBlank() && level.isBlank()) {
                return "";
            }
            if (symbol.isBlank()) {
                return "LEVEL" + level;
            }
            if (level.isBlank()) {
                return symbol;
            }
            return symbol + level;
        }

        public boolean hasTableSymbol() {
            return !symbol.isBlank() && !bmsLevelOnly;
        }
    }
}
