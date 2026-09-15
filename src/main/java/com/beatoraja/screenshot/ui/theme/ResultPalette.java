package com.beatoraja.screenshot.ui.theme;

import java.awt.Color;
import java.util.Locale;
import java.util.Map;

/** Colors for clear lamps, ranks and capture states, following beatoraja's conventions. */
public final class ResultPalette {

    private static final Color NEUTRAL = new Color(0x7A828C);

    private static final Map<String, Color> LAMP_COLORS = Map.ofEntries(
            Map.entry("NO PLAY", new Color(0x6B7280)),
            Map.entry("FAILED", new Color(0xA8323C)),
            Map.entry("LIGHT ASSIST EASY CLEAR", new Color(0xB06CE0)),
            Map.entry("ASSIST EASY CLEAR", new Color(0xB06CE0)),
            Map.entry("EASY CLEAR", new Color(0x43C46A)),
            Map.entry("CLEAR", new Color(0x4F8CFF)),
            Map.entry("HARD CLEAR", new Color(0xE8484F)),
            Map.entry("EXHARD CLEAR", new Color(0xF2B035)),
            Map.entry("FULL COMBO", new Color(0x34D6E8)),
            Map.entry("PERFECT", new Color(0xFFD24A)),
            Map.entry("MAX", new Color(0xFFE9A8))
    );

    private static final Map<String, Color> RANK_COLORS = Map.ofEntries(
            Map.entry("MAX", new Color(0xFFE9A8)),
            Map.entry("AAA", new Color(0xFFC93C)),
            Map.entry("AA", new Color(0xC4CBD6)),
            Map.entry("A", new Color(0xE08B45)),
            Map.entry("B", new Color(0x5AA9F0)),
            Map.entry("C", new Color(0x4CC79A)),
            Map.entry("D", new Color(0x9C86E0)),
            Map.entry("E", new Color(0xE07AA8)),
            Map.entry("F", new Color(0x7A828C))
    );

    private static final Map<String, Color> STATE_COLORS = Map.of(
            "Result", new Color(0x43C46A),
            "Course Result", new Color(0xF2B035),
            "Play", new Color(0x4F8CFF),
            "Music Select", new Color(0x8B93A1),
            "Decide", new Color(0xB06CE0),
            "Config", new Color(0x7A828C)
    );

    private ResultPalette() {
    }

    public static Color lampColor(String lamp) {
        return lookup(LAMP_COLORS, lamp);
    }

    public static Color rankColor(String rank) {
        return lookup(RANK_COLORS, rank);
    }

    public static Color stateColor(String state) {
        if (state == null) {
            return NEUTRAL;
        }
        return adaptForTheme(STATE_COLORS.getOrDefault(state.trim(), NEUTRAL));
    }

    /** Difficulty table symbols cycle through a fixed hue set so each table stays visually distinct. */
    public static Color symbolColor(String symbol) {
        if (symbol == null || symbol.isBlank() || "-".equals(symbol)) {
            return NEUTRAL;
        }
        float hue = Math.abs(symbol.hashCode() % 360) / 360f;
        return Color.getHSBColor(hue, 0.55f, UiTheme.isDark() ? 0.85f : 0.70f);
    }

    /** Blends {@code color} into {@code background} so a badge stays legible on any row. */
    public static Color tint(Color color, Color background, float alpha) {
        float inverse = 1f - alpha;
        return new Color(
                Math.round(color.getRed() * alpha + background.getRed() * inverse),
                Math.round(color.getGreen() * alpha + background.getGreen() * inverse),
                Math.round(color.getBlue() * alpha + background.getBlue() * inverse)
        );
    }

    private static Color lookup(Map<String, Color> colors, String value) {
        if (value == null) {
            return NEUTRAL;
        }
        String key = value.trim().toUpperCase(Locale.ROOT);
        return adaptForTheme(colors.getOrDefault(key, NEUTRAL));
    }

    /**
     * The palette is tuned for the dark theme; pale entries such as the AA silver or the
     * PERFECT gold lose all contrast on a light surface, so darken them there.
     */
    private static Color adaptForTheme(Color color) {
        if (UiTheme.isDark()) {
            return color;
        }
        Color adapted = color;
        while (relativeLuminance(adapted) > 0.55f) {
            adapted = new Color(
                    Math.round(adapted.getRed() * 0.78f),
                    Math.round(adapted.getGreen() * 0.78f),
                    Math.round(adapted.getBlue() * 0.78f)
            );
        }
        return adapted;
    }

    private static float relativeLuminance(Color color) {
        return (0.2126f * color.getRed() + 0.7152f * color.getGreen() + 0.0722f * color.getBlue()) / 255f;
    }
}
