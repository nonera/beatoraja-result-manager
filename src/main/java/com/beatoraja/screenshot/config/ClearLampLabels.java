package com.beatoraja.screenshot.config;

import com.beatoraja.screenshot.model.ClearLamp;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * User-configurable display names ("俗称") for beatoraja's clear lamp types. Overrides are
 * keyed by the canonical, trimmed/upper-cased clear type text and only ever change what is
 * shown to the user - callers that need the canonical value (color lookups, the numeric
 * clear ID) must keep using the original, unresolved clear type text. The lamps themselves
 * and their defaults come from {@link ClearLamp}.
 */
public final class ClearLampLabels {

    /** Canonical clear types in beatoraja's natural clear-difficulty order, for editing UI. */
    public static final List<String> CANONICAL_ORDER =
            Arrays.stream(ClearLamp.values()).map(ClearLamp::displayName).toList();

    private ClearLampLabels() {
    }

    /** The built-in short name for {@code canonicalClearType} in the lamp-grouped post text ("" for none). */
    public static String defaultShortName(String canonicalClearType) {
        return ClearLamp.fromDisplayName(canonicalClearType).map(ClearLamp::defaultShortName).orElse("");
    }

    /** Whether {@code canonicalClearType} is listed in the lamp-grouped post text when the user has not said. */
    public static boolean isShownInGroupedPostByDefault(String canonicalClearType) {
        return ClearLamp.fromDisplayName(canonicalClearType).map(ClearLamp::shownInGroupedPostByDefault).orElse(true);
    }

    /** Whether {@code canonicalClearType} is listed in the lamp-grouped post text. */
    public static boolean isShownInGroupedPost(String canonicalClearType, Map<String, Boolean> overrides) {
        Boolean override = overrides == null ? null : overrides.get(canonicalClearType);
        return override != null ? override : isShownInGroupedPostByDefault(canonicalClearType);
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
        return ClearLamp.fromDisplayName(key).map(ClearLamp::defaultShortName).orElse(clearType.trim());
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
