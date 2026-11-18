package com.beatoraja.screenshot.service;

import com.beatoraja.screenshot.config.ClearLampLabels;
import com.beatoraja.screenshot.config.PostTextFormat;
import com.beatoraja.screenshot.model.ScreenshotEntry;
import com.beatoraja.screenshot.player.PlayScore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class TweetTextGenerator {

    private static final int NO_TITLE_LIMIT = Integer.MAX_VALUE;

    private TweetTextGenerator() {
    }

    /**
     * Builds the post text from {@code format}'s enabled items, in order, with the title in
     * full and no lamp-name overrides (used for the format dialog's preview). {@code score} is
     * the matched play's judge counts; when null, score-derived items (BP, rate, ...) are
     * omitted and the rank falls back to the one in the file name.
     */
    public static String generate(ScreenshotEntry entry, String postNotation, PlayScore score, PostTextFormat format) {
        return generate(entry, postNotation, score, format, NO_TITLE_LIMIT, Map.of());
    }

    /**
     * Same as above, but truncates the title to at most {@code maxTitleWeightedLength}
     * Twitter-weighted units (CJK counts as two, an ellipsis marks the cut) and shows the clear
     * lamp under the user's custom label when one is configured for it.
     */
    public static String generate(ScreenshotEntry entry, String postNotation, PlayScore score, PostTextFormat format,
            int maxTitleWeightedLength, Map<String, String> clearLampLabels) {
        return buildResult(format, new Values(
                postNotation,
                truncateToWeightedLength(entry.getTitle(), maxTitleWeightedLength),
                ClearLampLabels.resolve(entry.getClearType(), clearLampLabels),
                entry.getRank(),
                score
        ), entry.getFileName());
    }

    /** {@code text} cut to {@code maxWeighted} Twitter-weighted units, ending in an ellipsis when cut. */
    static String truncate(String text, int maxWeighted) {
        return truncateToWeightedLength(text, maxWeighted);
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

    private static String buildResult(PostTextFormat format, Values values, String fallback) {
        PostTextFormat effective = format == null ? PostTextFormat.defaults() : format;
        List<String> parts = new ArrayList<>();
        for (PostTextFormat.Item item : effective.getItems()) {
            if (!item.isEnabled()) {
                continue;
            }
            String value = valueOf(item.getKey(), values, effective);
            if (value != null && !value.isBlank()) {
                parts.add(item.getPrefix() + value.trim() + item.getSuffix());
            }
        }
        String text = String.join(" ", parts).trim();
        return text.isBlank() ? fallback : text;
    }

    private static String valueOf(String key, Values values, PostTextFormat format) {
        PlayScore score = values.score();
        return switch (key) {
            case PostTextFormat.NOTATION -> values.postNotation();
            case PostTextFormat.TITLE -> values.title();
            case PostTextFormat.CLEAR -> values.clearType();
            case PostTextFormat.RANK -> format.rankLabel(hasNotes(score) ? score.rank() : values.fileRank());
            case PostTextFormat.BP -> score == null ? "" : String.valueOf(score.bp());
            case PostTextFormat.RATE -> hasNotes(score) ? String.format(Locale.ROOT, "%.2f", score.ratePercent()) : "";
            case PostTextFormat.EX_SCORE -> score == null ? "" : String.valueOf(score.exScore());
            case PostTextFormat.RANK_DIFF -> hasNotes(score) ? formatDiff(score.nearestBoundaryDiff(), format) : "";
            default -> "";
        };
    }

    private static boolean hasNotes(PlayScore score) {
        return score != null && score.notes() > 0;
    }

    private static String formatDiff(PlayScore.BoundaryDiff diff, PostTextFormat format) {
        String sign = diff.diff() < 0 ? "-" : "+";
        return format.rankLabel(diff.rankKey()) + sign + Math.abs(diff.diff());
    }

    private record Values(String postNotation, String title, String clearType, String fileRank, PlayScore score) {
    }
}
