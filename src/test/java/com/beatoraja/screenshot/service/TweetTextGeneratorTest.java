package com.beatoraja.screenshot.service;

import com.beatoraja.screenshot.model.ScreenshotEntry;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TweetTextGeneratorTest {

    private static ScreenshotEntry entry(String title) {
        return new ScreenshotEntry(
                Path.of("shot.png"), "shot.png", null, "state",
                title, "folder", "10", "FULL COMBO CLEAR", "AAA");
    }

    private static ScreenshotEntry titleOnlyEntry(String title) {
        return new ScreenshotEntry(
                Path.of("shot.png"), "shot.png", null, "state",
                title, "folder", "10", "", "");
    }

    @Test
    void generateWithoutLimitKeepsFullTitle() {
        String text = TweetTextGenerator.generate(entry("Short Title"), "");
        assertTrue(text.contains("Short Title"));
    }

    @Test
    void generateWithTitleLimitTruncatesLongTitle() {
        String longTitle = "あ".repeat(50);
        String text = TweetTextGenerator.generate(entry(longTitle), "", 20);
        assertTrue(text.contains("…"));
        assertFalse(text.contains(longTitle));
    }

    @Test
    void generateWithTitleLimitLeavesShortTitleUntouched() {
        String text = TweetTextGenerator.generate(entry("短いタイトル"), "", 40);
        assertTrue(text.contains("短いタイトル"));
        assertFalse(text.contains("…"));
    }

    @Test
    void truncatedTitleFitsWithinBudget() {
        String longTitle = "漢".repeat(50);
        String text = TweetTextGenerator.generate(titleOnlyEntry(longTitle), "", 20);
        assertTrue(TweetTextLimits.weightedLength(text) <= 20);
        assertEquals(text, TweetTextGenerator.generate(titleOnlyEntry(longTitle), "", 20));
    }
}
