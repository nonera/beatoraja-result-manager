package com.beatoraja.screenshot.service;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LampGroupedTweetTextTest {

    @Test
    void usedOnceAnImageGetsUnderFortyUnits() {
        assertFalse(LampGroupedTweetText.shouldUse(7));
        assertTrue(LampGroupedTweetText.shouldUse(8));
        assertTrue(LampGroupedTweetText.shouldUse(20));
    }

    @Test
    void groupsNotationsByLampBestFirstInPostOrder() {
        String text = LampGroupedTweetText.generate(List.of(
                new LampGroupedTweetText.Item("CLEAR", "st6/dy1", "A"),
                new LampGroupedTweetText.Item("HARD CLEAR", "st3/集3", "B"),
                new LampGroupedTweetText.Item("CLEAR", "st4", "C"),
                new LampGroupedTweetText.Item("HARD CLEAR", "st2/★22", "D")
        ), Map.of());

        assertEquals("HC : st3/集3 st2/★22\nNC : st6/dy1 st4", text);
    }

    private static final List<LampGroupedTweetText.Item> LOW_LAMPS = List.of(
            new LampGroupedTweetText.Item("EASY CLEAR", "st0", ""),
            new LampGroupedTweetText.Item("ASSIST EASY CLEAR", "st1", ""),
            new LampGroupedTweetText.Item("LIGHT ASSIST EASY CLEAR", "st2", ""),
            new LampGroupedTweetText.Item("FAILED", "st4", ""),
            new LampGroupedTweetText.Item("NO PLAY", "st3", "")
    );

    @Test
    void leavesOutAssistFailedAndNoPlayByDefault() {
        assertEquals("EC : st0", LampGroupedTweetText.generate(LOW_LAMPS, Map.of()));
    }

    @Test
    void shownLampsSharingAShortNameShareALine() {
        String text = LampGroupedTweetText.generate(LOW_LAMPS, Map.of(), Map.of(
                "ASSIST EASY CLEAR", true,
                "LIGHT ASSIST EASY CLEAR", true,
                "FAILED", true,
                "NO PLAY", true,
                "EASY CLEAR", false));

        assertEquals("AS : st2 st1\nst4\n無し : st3", text);
    }

    @Test
    void usesCustomShortNamesAndFallsBackToTitle() {
        String text = LampGroupedTweetText.generate(List.of(
                new LampGroupedTweetText.Item("HARD CLEAR", "", "Song"),
                new LampGroupedTweetText.Item("", "st1", "")
        ), Map.of("HARD CLEAR", "難"));

        assertEquals("難 : Song\n? : st1", text);
    }
}
