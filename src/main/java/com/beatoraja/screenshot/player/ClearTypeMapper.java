package com.beatoraja.screenshot.player;

import com.beatoraja.screenshot.model.ClearLamp;

/** Converts between clear ids, beatoraja lamp names and BMZ Player labels, all defined in {@link ClearLamp}. */
public final class ClearTypeMapper {

    private ClearTypeMapper() {
    }

    /** Clear id (beatoraja numbering) for a BMZ Player clear_type label, or null if unknown. */
    public static Integer fromBmzLabel(String label) {
        return ClearLamp.fromBmzLabel(label).map(ClearLamp::id).orElse(null);
    }

    /** The lamp name beatoraja writes into result-screenshot file names, e.g. 6 → "HARD CLEAR". */
    public static String toClearName(int clearId) {
        return ClearLamp.fromId(clearId).map(ClearLamp::displayName).orElse("");
    }

    public static Integer toClearId(String clearTypeName) {
        return ClearLamp.fromDisplayName(clearTypeName).map(ClearLamp::id).orElse(null);
    }
}
