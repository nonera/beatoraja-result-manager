package com.beatoraja.screenshot.service;

import com.beatoraja.screenshot.config.ClearLampLabels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The post text used when one post carries too many images for a line each: one line per
 * clear lamp, best lamp first, listing the charts' post notations in post order.
 * <pre>
 * HC : st3/集3 st2/★22
 * NC : st6/dy1 st4
 * </pre>
 * Titles are left out since they can be read off the images.
 */
public final class LampGroupedTweetText {

    /** Below this many weighted units per image, a line per image no longer fits a post. */
    public static final int MIN_WEIGHTED_PER_IMAGE = 40;

    /** Charts without a notation are listed by title, cut to this many weighted units. */
    private static final int FALLBACK_TITLE_WEIGHTED_LENGTH = 20;

    private static final String UNKNOWN_LAMP_LABEL = "?";

    private LampGroupedTweetText() {
    }

    /** One image of the post: its beatoraja clear type text, post notation and title. */
    public record Item(String clearType, String notation, String title) {
    }

    /** Whether a post of {@code imageCount} images should use this format. */
    public static boolean shouldUse(int imageCount) {
        return imageCount > 0 && TweetTextLimits.WEIGHTED_LIMIT / imageCount < MIN_WEIGHTED_PER_IMAGE;
    }

    public static String generate(List<Item> items, Map<String, String> shortLabelOverrides) {
        return generate(items, shortLabelOverrides, Map.of());
    }

    /**
     * {@code visibilityOverrides} turns lamps on or off over
     * {@link ClearLampLabels#DEFAULT_GROUPED_HIDDEN}; charts of a hidden lamp are left out.
     */
    public static String generate(List<Item> items, Map<String, String> shortLabelOverrides,
            Map<String, Boolean> visibilityOverrides) {
        // Group by the displayed short label, so lamps sharing one (e.g. both assists as "AS")
        // end up on one line; lamps are visited best first so lines come out in that order.
        Map<String, List<String>> notationsByLabel = new LinkedHashMap<>();
        List<String> lampOrder = new ArrayList<>(ClearLampLabels.CANONICAL_ORDER);
        Collections.reverse(lampOrder);
        for (String lamp : lampOrder) {
            if (!ClearLampLabels.isShownInGroupedPost(lamp, visibilityOverrides)) {
                continue;
            }
            for (Item item : items) {
                if (lamp.equals(canonical(item.clearType()))) {
                    notationsByLabel.computeIfAbsent(ClearLampLabels.resolveShort(lamp, shortLabelOverrides),
                            k -> new ArrayList<>()).add(chartLabel(item));
                }
            }
        }
        for (Item item : items) {
            if (!lampOrder.contains(canonical(item.clearType()))) {
                String label = item.clearType() == null || item.clearType().isBlank()
                        ? UNKNOWN_LAMP_LABEL
                        : item.clearType().trim();
                notationsByLabel.computeIfAbsent(label, k -> new ArrayList<>()).add(chartLabel(item));
            }
        }

        List<String> lines = new ArrayList<>();
        notationsByLabel.forEach((label, notations) -> {
            String joined = String.join(" ", notations);
            // A blank short name (FAILED by default) lists the charts without a label.
            lines.add(label.isBlank() ? joined : label + " : " + joined);
        });
        return String.join("\n", lines);
    }

    private static String canonical(String clearType) {
        return clearType == null ? "" : clearType.trim().toUpperCase(Locale.ROOT);
    }

    private static String chartLabel(Item item) {
        if (item.notation() != null && !item.notation().isBlank()) {
            return item.notation().trim();
        }
        String title = item.title() == null ? "" : item.title().trim();
        return title.isEmpty() ? UNKNOWN_LAMP_LABEL : TweetTextGenerator.truncate(title, FALLBACK_TITLE_WEIGHTED_LENGTH);
    }
}
