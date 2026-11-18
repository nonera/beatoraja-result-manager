package com.beatoraja.screenshot.model;

import com.beatoraja.screenshot.player.PlayScore;

import java.util.ArrayList;
import java.util.List;

/**
 * A screenshot with what was worked out about it: the chart's hashes and title, its difficulty
 * table symbol and level, the notation to post with and the alternatives, whether it came from
 * the player's own play data, and that play's judge counts ({@code score} is null when none).
 */
public record EnrichedScreenshot(
        ScreenshotEntry entry,
        String sha256,
        String md5,
        String resolvedTitle,
        String tableSymbol,
        String tableLevelNum,
        String level,
        String displaySymbol,
        String displayLevel,
        String defaultPostNotation,
        List<String> availableNotations,
        boolean resolvedFromPlayerData,
        PlayScore score
) {

    /** The candidate notations with the default one first (added if it was not among them). */
    public List<String> mergedAvailableNotations() {
        List<String> merged = new ArrayList<>();
        if (availableNotations != null) {
            merged.addAll(availableNotations);
        }
        if (defaultPostNotation != null && !defaultPostNotation.isBlank() && !merged.contains(defaultPostNotation)) {
            merged.add(0, defaultPostNotation);
        }
        return merged;
    }
}
