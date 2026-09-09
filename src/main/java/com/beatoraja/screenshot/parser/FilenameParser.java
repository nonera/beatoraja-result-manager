package com.beatoraja.screenshot.parser;

import com.beatoraja.screenshot.model.ScreenshotEntry;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FilenameParser {

    private static final DateTimeFormatter CAPTURED_AT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final Pattern FILE_PATTERN = Pattern.compile("^(\\d{8}_\\d{6})(.*)\\.png$", Pattern.CASE_INSENSITIVE);
    private static final Pattern LEVEL_PATTERN = Pattern.compile("^LEVEL(\\d+)(?:\\s+(.*))?$", Pattern.CASE_INSENSITIVE);
    private static final String[] CLEAR_TYPES = {
            "LIGHT ASSIST EASY CLEAR", "ASSIST EASY CLEAR", "EASY CLEAR",
            "EXHARD CLEAR", "HARD CLEAR", "FULL COMBO", "NO PLAY", "FAILED", "CLEAR", "PERFECT", "MAX"
    };
    private static final String[] RANKS = {"AAA", "AA", "A", "B", "C", "D", "E", "F"};

    private FilenameParser() {
    }

    public static ScreenshotEntry parse(Path filePath) {
        String fileName = filePath.getFileName().toString();
        Matcher matcher = FILE_PATTERN.matcher(fileName);
        if (!matcher.matches()) {
            return emptyEntry(filePath, fileName, null);
        }

        LocalDateTime capturedAt = parseCapturedAt(matcher.group(1));
        String suffix = matcher.group(2).trim();
        ParsedMetadata metadata = parseSuffix(suffix);
        return new ScreenshotEntry(
                filePath,
                fileName,
                capturedAt,
                metadata.stateLabel(),
                metadata.title(),
                metadata.rawTableFolder(),
                metadata.level(),
                metadata.clearType(),
                metadata.rank()
        );
    }

    private static ScreenshotEntry emptyEntry(Path filePath, String fileName, LocalDateTime capturedAt) {
        return new ScreenshotEntry(filePath, fileName, capturedAt, "", fileName, "", "", "", "");
    }

    private static LocalDateTime parseCapturedAt(String value) {
        try {
            return LocalDateTime.parse(value, CAPTURED_AT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static final Pattern LR2ORAJA_PREFIX = Pattern.compile("^_LR2oraja", Pattern.CASE_INSENSITIVE);

    private static ParsedMetadata parseSuffix(String suffix) {
        if (suffix.isBlank()) {
            return ParsedMetadata.empty();
        }
        suffix = LR2ORAJA_PREFIX.matcher(suffix).replaceFirst("");
        if (suffix.startsWith("_Music_Select")) {
            return new ParsedMetadata("Music Select", "", "", "", "", "");
        }
        if (suffix.startsWith("_Decide")) {
            return new ParsedMetadata("Decide", "", "", "", "", "");
        }
        if (suffix.startsWith("_Config")) {
            return new ParsedMetadata("Config", "", "", "", "", "");
        }
        if (suffix.startsWith("_Play_")) {
            return parsePlayBody(suffix.substring("_Play_".length()));
        }
        return parseResultBody(suffix.startsWith("_") ? suffix.substring(1) : suffix);
    }

    private static ParsedMetadata parsePlayBody(String body) {
        Matcher levelMatcher = LEVEL_PATTERN.matcher(body);
        if (levelMatcher.matches()) {
            String title = levelMatcher.group(2) == null ? "" : levelMatcher.group(2).trim();
            return new ParsedMetadata("Play", title, "", levelMatcher.group(1), "", "");
        }

        int spaceIndex = body.indexOf(' ');
        if (spaceIndex < 0) {
            return new ParsedMetadata("Play", "", body, "", "", "");
        }
        return new ParsedMetadata(
                "Play",
                body.substring(spaceIndex + 1).trim(),
                body.substring(0, spaceIndex).trim(),
                "",
                "",
                ""
        );
    }

    private static ParsedMetadata parseResultBody(String body) {
        String clearType = extractToken(body, CLEAR_TYPES);
        String rank = extractToken(body, RANKS);

        String remaining = body;
        if (!clearType.isEmpty()) {
            remaining = removeLastOccurrence(remaining, clearType).trim();
        }
        if (!rank.isEmpty()) {
            remaining = removeLastOccurrence(remaining, rank).trim();
        }

        Matcher levelMatcher = LEVEL_PATTERN.matcher(remaining);
        if (levelMatcher.matches()) {
            String title = levelMatcher.group(2) == null ? "" : levelMatcher.group(2).trim();
            return new ParsedMetadata("Result", title, "", levelMatcher.group(1), clearType, rank);
        }

        int firstSpace = remaining.indexOf(' ');
        if (firstSpace < 0) {
            // Course result etc.: title only, no level/table
            return new ParsedMetadata("Course Result", remaining, "", "", clearType, rank);
        }

        String tableLevel = remaining.substring(0, firstSpace).trim();
        String title = remaining.substring(firstSpace + 1).trim();
        return new ParsedMetadata("Result", title, tableLevel, "", clearType, rank);
    }

    private static String extractToken(String text, String[] tokens) {
        for (String token : tokens) {
            if (text.contains(token)) {
                return token;
            }
        }
        return "";
    }

    /**
     * Removes only the right-most occurrence of {@code token} from {@code text}.
     * Clear type / rank tags are appended after the title in the filename, so a
     * global replace (as String.replace would do) can also strip matching
     * characters out of the title itself -- most visibly for single-letter ranks
     * like "A"/"B"/"C" that commonly appear inside song titles.
     */
    private static String removeLastOccurrence(String text, String token) {
        int index = text.lastIndexOf(token);
        if (index < 0) {
            return text;
        }
        return text.substring(0, index) + text.substring(index + token.length());
    }

    private record ParsedMetadata(
            String stateLabel,
            String title,
            String rawTableFolder,
            String level,
            String clearType,
            String rank
    ) {
        static ParsedMetadata empty() {
            return new ParsedMetadata("", "", "", "", "", "");
        }
    }
}
