package com.beatoraja.screenshot.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TweetTextLimitsTest {

    @Test
    void asciiCountsAsOneUnitPerCharacter() {
        assertEquals(5, TweetTextLimits.weightedLength("Hello"));
    }

    @Test
    void japaneseCountsAsTwoUnitsPerCharacter() {
        assertEquals(10, TweetTextLimits.weightedLength("あアー漢字"));
    }

    @Test
    void nullTextHasZeroLength() {
        assertEquals(0, TweetTextLimits.weightedLength(null));
    }

    @Test
    void exceedsLimitAtTheBoundary() {
        String exactly280 = "a".repeat(280);
        String over280 = "a".repeat(281);
        assertFalse(TweetTextLimits.exceedsLimit(exactly280));
        assertTrue(TweetTextLimits.exceedsLimit(over280));
    }
}
