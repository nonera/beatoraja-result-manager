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

    private ClearLampLabels() {
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
