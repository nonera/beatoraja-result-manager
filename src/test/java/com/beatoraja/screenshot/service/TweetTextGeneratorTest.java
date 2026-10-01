package com.beatoraja.screenshot.service;

import com.beatoraja.screenshot.config.PostTextFormat;
import com.beatoraja.screenshot.model.ScreenshotEntry;
import com.beatoraja.screenshot.player.PlayScore;
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

    /** 1500 notes, EX 2620 (87.33%, AA, AAA-47), BP 45. */
    private static final PlayScore SCORE = new PlayScore(900, 300, 120, 100, 30, 20, 10, 5, 15, 10, 3, 2, 1500);

    /** EX 1790 / 2000: AAA+12. */
    private static final PlayScore SCORE_FOR_AAA = new PlayScore(790, 0, 210, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1000);

    private static PostTextFormat allEnabled() {
        PostTextFormat format = PostTextFormat.defaults();
        format.getItems().forEach(item -> item.setEnabled(true));
        return format;
    }

    @Test
    void defaultFormatMatchesOriginalLayout() {
        assertEquals("★12 Song FULL COMBO CLEAR AAA",
                TweetTextGenerator.generate(entry("Song"), "★12", SCORE_FOR_AAA, PostTextFormat.defaults()));
        assertEquals("★12 Song FULL COMBO CLEAR AAA", TweetTextGenerator.generate(entry("Song"), "★12"));
    }

    @Test
    void allItemsWithAffixes() {
        assertEquals("★12 Song FULL COMBO CLEAR AA BP45 87.33% EX2620 AAA-47",
                TweetTextGenerator.generate(entry("Song"), "★12", SCORE, allEnabled()));
    }

    @Test
    void scoreRankOverridesFileNameRank() {
        // file name says AAA, the matched play is AA
        String text = TweetTextGenerator.generate(entry("Song"), "", SCORE, PostTextFormat.defaults());
        assertEquals("Song FULL COMBO CLEAR AA", text);
    }

    @Test
    void itemsFollowConfiguredOrderAndEnabledState() {
        PostTextFormat format = allEnabled();
        PostTextFormat.Item bp = format.getItems().remove(4);
        format.getItems().add(0, bp);
        format.getItems().get(1).setEnabled(false); // notation
        bp.setPrefix("ミス");
        bp.setSuffix("回");
        assertEquals("ミス45回 Song FULL COMBO CLEAR AA 87.33% EX2620 AAA-47",
                TweetTextGenerator.generate(entry("Song"), "★12", SCORE, format));
    }

    @Test
    void rankLabelsApplyToRankAndDiff() {
        PostTextFormat format = allEnabled();
        format.setRankLabels(java.util.Map.of("AA", "ダブルA", "AAA", "トリプルA"));
        assertEquals("Song FULL COMBO CLEAR ダブルA BP45 87.33% EX2620 トリプルA-47",
                TweetTextGenerator.generate(entry("Song"), "", SCORE, format));
    }

    @Test
    void scoreItemsOmittedWithoutPlayData() {
        PostTextFormat format = allEnabled();
        format.setRankLabels(java.util.Map.of("AAA", "3A"));
        assertEquals("★12 Song FULL COMBO CLEAR 3A",
                TweetTextGenerator.generate(entry("Song"), "★12", null, format));
    }

    @Test
    void titleTruncationWorksWithCustomFormat() {
        String longTitle = "あ".repeat(50);
        String text = TweetTextGenerator.generate(entry(longTitle), "", SCORE, allEnabled(), 20);
        assertTrue(text.contains("…"));
        assertTrue(text.endsWith("BP45 87.33% EX2620 AAA-47"));
    }

    @Test
    void normalizedAppendsMissingItemsDisabled() {
        PostTextFormat format = new PostTextFormat();
        format.setItems(java.util.List.of(new PostTextFormat.Item(PostTextFormat.BP, true, "BP", ""),
                new PostTextFormat.Item("unknown", true, "", "")));
        PostTextFormat normalized = format.normalized();
        assertEquals(PostTextFormat.ITEM_KEYS.size(), normalized.getItems().size());
        assertEquals(PostTextFormat.BP, normalized.getItems().get(0).getKey());
        assertTrue(normalized.getItems().get(0).isEnabled());
        assertFalse(normalized.getItems().get(1).isEnabled());
    }
}
