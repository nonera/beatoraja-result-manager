package com.beatoraja.screenshot.player;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayScoreTest {

    /** A score with {@code pgreat} PGREATs, {@code great} GREATs, and the rest BAD on a {@code notes}-note chart. */
    private static PlayScore score(int notes, int pgreat, int great) {
        int bad = notes - pgreat - great;
        return new PlayScore(pgreat, 0, great, 0, 0, 0, bad, 0, 0, 0, 0, 0, notes);
    }

    @Test
    void exScoreBpAndRate() {
        PlayScore score = new PlayScore(900, 300, 120, 100, 30, 20, 10, 5, 15, 10, 3, 2, 1500);
        assertEquals(2620, score.exScore());
        assertEquals(3000, score.maxExScore());
        assertEquals(45, score.bp());
        assertEquals(87.333, score.ratePercent(), 0.001);
        assertEquals("AA", score.rank());
    }

    @Test
    void rankBoundariesUseCeilingOfNinths() {
        // 1000 notes -> max 2000; AAA needs ceil(2000*8/9) = 1778
        assertEquals("AAA", score(1000, 778, 222).rank()); // 1556 + 222 = 1778
        assertEquals("AA", score(1000, 777, 222).rank());  // 1554 + 222 = 1776
        assertEquals("F", score(1000, 0, 0).rank());
        assertEquals("AAA", score(1000, 1000, 0).rank()); // MAX is a boundary, not a DJ RANK
    }

    @Test
    void nearestBoundaryPicksCloserSide() {
        // 1500 notes: AA = 2334, AAA = 2667. EX 2620 -> AAA-47 (vs AA+286)
        PlayScore score = new PlayScore(900, 300, 120, 100, 30, 20, 10, 5, 15, 10, 3, 2, 1500);
        assertEquals(new PlayScore.BoundaryDiff("AAA", -47), score.nearestBoundaryDiff());
    }

    @Test
    void nearestBoundaryAheadOfCurrentRank() {
        // 1000 notes: AAA = 1778, EX 1790 -> AAA+12 (vs MAX-210)
        assertEquals(new PlayScore.BoundaryDiff("AAA", 12), score(1000, 790, 210).nearestBoundaryDiff());
    }

    @Test
    void nearestBoundaryNearMax() {
        // 1000 notes, EX 1990 -> MAX-10
        assertEquals(new PlayScore.BoundaryDiff("MAX", -10), score(1000, 990, 10).nearestBoundaryDiff());
        assertEquals(new PlayScore.BoundaryDiff("MAX", 0), score(1000, 1000, 0).nearestBoundaryDiff());
    }

    @Test
    void nearestBoundaryTieGoesToPlusSide() {
        // 9 notes -> max 18, ninths are exact: AA = 14, AAA = 16. EX 15 -> AA+1 (tie with AAA-1)
        assertEquals(new PlayScore.BoundaryDiff("AA", 1), score(9, 7, 1).nearestBoundaryDiff());
    }

    @Test
    void nearestBoundaryFromF() {
        // 9 notes -> E = 4. EX 1 -> F+1 (vs E-3); EX 3 -> E-1
        assertEquals(new PlayScore.BoundaryDiff("F", 1), score(9, 0, 1).nearestBoundaryDiff());
        assertEquals(new PlayScore.BoundaryDiff("E", -1), score(9, 1, 1).nearestBoundaryDiff());
    }
}
