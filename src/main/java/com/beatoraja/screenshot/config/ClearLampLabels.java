package com.beatoraja.screenshot.config;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * User-configurable display names ("俗称") for beatoraja's clear lamp types. Overrides are
 * keyed by the canonical, trimmed/upper-cased clear type text and only ever change what is
 * shown to the user - callers that need the canonical value (color lookups, the numeric
 * clear ID) must keep using the original, unresolved clear type text.
 */
public final class ClearLampLabels {

    /** Canonical clear types in beatoraja's natural clear-difficulty order, for editing UI. */
    public static final List<String> CANONICAL_ORDER = List.of(
            "NO PLAY",
            "FAILED",
            "ASSIST EASY CLEAR",
            "LIGHT ASSIST EASY CLEAR",
            "EASY CLEAR",
            "CLEAR",
            "HARD CLEAR",
            "EXHARD CLEAR",
            "FULL COMBO",
            "PERFECT",
            "MAX"
    );

    /**
     * Default short names used in the lamp-grouped post text (many images in one post).
     * Light assist and assist share "AS"; FAILED has none, so its charts (when shown at all,
     * see {@link #DEFAULT_GROUPED_HIDDEN}) are listed without a label. All are editable.
     */
    public static final Map<String, String> DEFAULT_SHORT_LABELS = Map.ofEntries(
            Map.entry("MAX", "MAX"),
            Map.entry("PERFECT", "PF"),
            Map.entry("FULL COMBO", "FC"),
            Map.entry("EXHARD CLEAR", "EXH"),
            Map.entry("HARD CLEAR", "HC"),
            Map.entry("CLEAR", "NC"),
            Map.entry("EASY CLEAR", "EC"),
            Map.entry("LIGHT ASSIST EASY CLEAR", "AS"),
            Map.entry("ASSIST EASY CLEAR", "AS"),
            Map.entry("FAILED", ""),
            Map.entry("NO PLAY", "無し")
    );

    /** Lamps left out of the lamp-grouped post text unless the user turns them on. */
    public static final java.util.Set<String> DEFAULT_GROUPED_HIDDEN = java.util.Set.of(
            "LIGHT ASSIST EASY CLEAR", "ASSIST EASY CLEAR", "FAILED", "NO PLAY");

    private ClearLampLabels() {
    }

    /** Whether {@code canonicalClearType} is listed in the lamp-grouped post text. */
    public static boolean isShownInGroupedPost(String canonicalClearType, Map<String, Boolean> overrides) {
        Boolean override = overrides == null ? null : overrides.get(canonicalClearType);
        return override != null ? override : !DEFAULT_GROUPED_HIDDEN.contains(canonicalClearType);
    }

    /** The short name for {@code clearType}: the user's override, else the default, else the text itself. */
    public static String resolveShort(String clearType, Map<String, String> overrides) {
        if (clearType == null || clearType.isBlank()) {
            return "";
        }
        String key = clearType.trim().toUpperCase(Locale.ROOT);
        String override = overrides == null ? null : overrides.get(key);
        if (override != null && !override.isBlank()) {
            return override;
        }
        return DEFAULT_SHORT_LABELS.getOrDefault(key, clearType.trim());
    }

    /** Returns the user's custom label for {@code clearType} if one is set, else {@code clearType} unchanged. */
    public static String resolve(String clearType, Map<String, String> overrides) {
        if (clearType == null || clearType.isBlank() || overrides == null || overrides.isEmpty()) {
            return clearType == null ? "" : clearType;
        }
        String override = overrides.get(clearType.trim().toUpperCase(Locale.ROOT));
        return override != null && !override.isBlank() ? override : clearType;
    }
}
