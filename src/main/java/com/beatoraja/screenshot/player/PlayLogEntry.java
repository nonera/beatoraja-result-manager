package com.beatoraja.screenshot.player;

/**
 * One recorded play: the chart, the lamp as a beatoraja clear id, when it was played (epoch
 * seconds) and its judge counts ({@code score} is null when the player's history has none).
 */
public record PlayLogEntry(String sha256, int clearId, long dateEpoch, PlayScore score) {
}
