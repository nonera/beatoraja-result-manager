package com.beatoraja.screenshot.parser;

import com.beatoraja.screenshot.model.ScreenshotEntry;
import com.beatoraja.screenshot.player.ClearTypeMapper;
import com.beatoraja.screenshot.player.PlayerKind;

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
    /**
     * BMZ Player (v0.5.2+, {@code crates/bmz-player/src/app/screenshot.rs}):
     * {@code <yyyyMMdd_HHmmss_SSS>_<body>.png}, where body is {@code Music_Select[_<folder>]},
     * {@code Decide_<title>[_<subtitle>]}, {@code Play_<title>[_<subtitle>]} or, on the result
     * screen, {@code <title>[_<subtitle>]_<clear label>[_<n>]}.
     */
    private static final Pattern BMZ_FILE_PATTERN =
            Pattern.compile("^(\\d{8}_\\d{6})_(\\d{3})_(.+)\\.png$", Pattern.CASE_INSENSITIVE);
    /** Result body: title, clear label ({@code Hard}, {@code FullCombo}, ...) and a duplicate-name counter. */
    private static final Pattern BMZ_RESULT_BODY = Pattern.compile("^(.+)_([A-Za-z]+)(?:_\\d+)?$");

    private FilenameParser() {
    }

    /** Parses a beatoraja screenshot name. */
    public static ScreenshotEntry parse(Path filePath) {
        return parse(filePath, PlayerKind.BEATORAJA);
    }

    /** Parses a screenshot name written by {@code player}. */
    public static ScreenshotEntry parse(Path filePath, PlayerKind player) {
        return player == PlayerKind.BMZ ? parseBmz(filePath) : parseBeatoraja(filePath);
    }

    private static ScreenshotEntry parseBmz(Path filePath) {
        String fileName = filePath.getFileName().toString();
        Matcher matcher = BMZ_FILE_PATTERN.matcher(fileName);
        if (!matcher.matches()) {
            return emptyEntry(filePath, fileName, null);
        }
        LocalDateTime capturedAt = parseCapturedAt(matcher.group(1));
        if (capturedAt != null) {
            capturedAt = capturedAt.plusNanos(Long.parseLong(matcher.group(2)) * 1_000_000L);
        }
        ParsedMetadata metadata = parseBmzBody(matcher.group(3));
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

    private static ScreenshotEntry parseBeatoraja(Path filePath) {
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

    /**
     * Metadata for a BMZ Player name body. Anything not Music Select / Decide / Play is a result
     * screen; its lamp is returned under beatoraja's name for it, or blank when the label is
     * unknown (the title then keeps the whole body). The rank is not in the file name.
     */
    private static ParsedMetadata parseBmzBody(String body) {
        if (body.equals("Music_Select") || body.startsWith("Music_Select_")) {
            return new ParsedMetadata("Music Select", "", "", "", "", "");
        }
        if (body.startsWith("Decide_")) {
            return new ParsedMetadata("Decide", body.substring("Decide_".length()), "", "", "", "");
        }
        if (body.startsWith("Play_")) {
            return new ParsedMetadata("Play", body.substring("Play_".length()), "", "", "", "");
        }
        Matcher resultMatcher = BMZ_RESULT_BODY.matcher(body);
        Integer clearId = resultMatcher.matches() ? ClearTypeMapper.fromBmzLabel(resultMatcher.group(2)) : null;
        if (clearId == null) {
            return new ParsedMetadata("Result", body, "", "", "", "");
        }
        return new ParsedMetadata("Result", resultMatcher.group(1), "", "", ClearTypeMapper.toClearName(clearId), "");
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
