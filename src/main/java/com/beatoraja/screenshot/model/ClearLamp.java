package com.beatoraja.screenshot.model;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * The clear lamps, worst first. Every per-lamp fact the app needs lives here: beatoraja's clear
 * id (= ordinal) and the name it writes into screenshot file names, BMZ Player's label, the
 * default short name and visibility for the lamp-grouped post text, and the badge color.
 */
public enum ClearLamp {
    NO_PLAY("NO PLAY", "NoPlay", "無し", false, 0x6B7280),
    FAILED("FAILED", "Failed", "", false, 0xA8323C),
    ASSIST_EASY("ASSIST EASY CLEAR", "AssistEasy", "AS", false, 0xB06CE0),
    LIGHT_ASSIST_EASY("LIGHT ASSIST EASY CLEAR", "LightAssistEasy", "AS", false, 0xB06CE0),
    EASY("EASY CLEAR", "Easy", "EC", true, 0x43C46A),
    NORMAL("CLEAR", "Normal", "NC", true, 0x4F8CFF),
    HARD("HARD CLEAR", "Hard", "HC", true, 0xE8484F),
    EXHARD("EXHARD CLEAR", "ExHard", "EXH", true, 0xF2B035),
    FULL_COMBO("FULL COMBO", "FullCombo", "FC", true, 0x34D6E8),
    PERFECT("PERFECT", "Perfect", "PF", true, 0xFFD24A),
    MAX("MAX", "Max", "MAX", true, 0xFFE9A8);

    /** Lamp names longest first, so a name contained in another ("HARD CLEAR" in "EXHARD CLEAR") is tried after it. */
    private static final List<ClearLamp> BY_NAME_LENGTH = Arrays.stream(values())
            .sorted(Comparator.comparingInt((ClearLamp lamp) -> lamp.displayName.length()).reversed())
            .toList();

    private final String displayName;
    private final String bmzLabel;
    private final String defaultShortName;
    private final boolean shownInGroupedPostByDefault;
    private final int colorRgb;

    ClearLamp(String displayName, String bmzLabel, String defaultShortName, boolean shownInGroupedPostByDefault,
            int colorRgb) {
        this.displayName = displayName;
        this.bmzLabel = bmzLabel;
        this.defaultShortName = defaultShortName;
        this.shownInGroupedPostByDefault = shownInGroupedPostByDefault;
        this.colorRgb = colorRgb;
    }

    /** beatoraja's clear id: 0 = NO PLAY … 10 = MAX. */
    public int id() {
        return ordinal();
    }

    /** The name beatoraja writes into result-screenshot file names, e.g. "HARD CLEAR". */
    public String displayName() {
        return displayName;
    }

    /** BMZ Player's {@code clear_type} label (bmz-core {@code ClearType::as_str}), e.g. "Hard". */
    public String bmzLabel() {
        return bmzLabel;
    }

    public String defaultShortName() {
        return defaultShortName;
    }

    public boolean shownInGroupedPostByDefault() {
        return shownInGroupedPostByDefault;
    }

    public int colorRgb() {
        return colorRgb;
    }

    public static Optional<ClearLamp> fromId(int id) {
        return id >= 0 && id < values().length ? Optional.of(values()[id]) : Optional.empty();
    }

    /** The lamp named {@code name} (case and surrounding spaces ignored). */
    public static Optional<ClearLamp> fromDisplayName(String name) {
        if (name == null) {
            return Optional.empty();
        }
        String key = name.trim().toUpperCase(Locale.ROOT);
        return Arrays.stream(values()).filter(lamp -> lamp.displayName.equals(key)).findFirst();
    }

    public static Optional<ClearLamp> fromBmzLabel(String label) {
        if (label == null) {
            return Optional.empty();
        }
        String key = label.trim();
        return Arrays.stream(values()).filter(lamp -> lamp.bmzLabel.equals(key)).findFirst();
    }

    /** The first lamp whose name appears in {@code text}, longer names tried first. */
    public static Optional<ClearLamp> findIn(String text) {
        if (text == null) {
            return Optional.empty();
        }
        return BY_NAME_LENGTH.stream().filter(lamp -> text.contains(lamp.displayName)).findFirst();
    }
}
