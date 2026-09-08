package com.beatoraja.screenshot.player;

import java.util.Locale;
import java.util.Map;

public final class ClearTypeMapper {

    private static final Map<String, Integer> CLEAR_NAME_TO_ID = Map.ofEntries(
            Map.entry("NO PLAY", 0),
            Map.entry("FAILED", 1),
            Map.entry("ASSIST EASY CLEAR", 2),
            Map.entry("LIGHT ASSIST EASY CLEAR", 3),
            Map.entry("EASY CLEAR", 4),
            Map.entry("CLEAR", 5),
            Map.entry("HARD CLEAR", 6),
            Map.entry("EXHARD CLEAR", 7),
            Map.entry("FULL COMBO", 8),
            Map.entry("PERFECT", 9),
            Map.entry("MAX", 10)
    );

    private ClearTypeMapper() {
    }

    public static Integer toClearId(String clearTypeName) {
        if (clearTypeName == null || clearTypeName.isBlank()) {
            return null;
        }
        return CLEAR_NAME_TO_ID.get(clearTypeName.trim().toUpperCase(Locale.ROOT));
    }
}
