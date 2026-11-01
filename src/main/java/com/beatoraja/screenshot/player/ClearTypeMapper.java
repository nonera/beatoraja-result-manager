package com.beatoraja.screenshot.player;

import java.util.List;
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

    /** BMZ Player's {@code clear_type} labels (bmz-core {@code ClearType::as_str}), in beatoraja clear-id order. */
    private static final List<String> BMZ_LABELS = List.of(
            "NoPlay", "Failed", "AssistEasy", "LightAssistEasy", "Easy",
            "Normal", "Hard", "ExHard", "FullCombo", "Perfect", "Max");

    /** beatoraja's result-screenshot lamp names, indexed by clear id. */
    private static final List<String> CLEAR_NAMES = List.of(
            "NO PLAY", "FAILED", "ASSIST EASY CLEAR", "LIGHT ASSIST EASY CLEAR", "EASY CLEAR",
            "CLEAR", "HARD CLEAR", "EXHARD CLEAR", "FULL COMBO", "PERFECT", "MAX");

    private ClearTypeMapper() {
    }

    /** Clear id (beatoraja numbering) for a BMZ Player clear_type label, or null if unknown. */
    public static Integer fromBmzLabel(String label) {
        if (label == null) {
            return null;
        }
        int index = BMZ_LABELS.indexOf(label.trim());
        return index < 0 ? null : index;
    }

    /** The lamp name beatoraja writes into result-screenshot file names, e.g. 6 → "HARD CLEAR". */
    public static String toClearName(int clearId) {
        return clearId >= 0 && clearId < CLEAR_NAMES.size() ? CLEAR_NAMES.get(clearId) : "";
    }

    public static Integer toClearId(String clearTypeName) {
        if (clearTypeName == null || clearTypeName.isBlank()) {
            return null;
        }
        return CLEAR_NAME_TO_ID.get(clearTypeName.trim().toUpperCase(Locale.ROOT));
    }
}
