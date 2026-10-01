package com.beatoraja.screenshot.player;

import java.util.List;

/**
 * Judge counts of one play, as recorded in beatoraja's scoredatalog table, plus the
 * values derived from them (EX score, BP, score rate, DJ RANK and rank-boundary diff).
 */
public record PlayScore(
        int epg, int lpg,
        int egr, int lgr,
        int egd, int lgd,
        int ebd, int lbd,
        int epr, int lpr,
        int ems, int lms,
        int notes
) {

    /** DJ RANK boundary names, highest first; AAA..E need a rate of 8/9..2/9 (see {@link #rank()}). */
    public static final List<String> RANK_KEYS = List.of("MAX", "AAA", "AA", "A", "B", "C", "D", "E", "F");

    public int exScore() {
        return (epg + lpg) * 2 + egr + lgr;
    }

    public int maxExScore() {
        return notes * 2;
    }

    /** Miss count as shown on beatoraja's result screen: BAD + POOR, including empty POOR. */
    public int bp() {
        return ebd + lbd + epr + lpr + ems + lms;
    }

    /** Score rate in percent (0-100), or 0 when the chart has no notes. */
    public double ratePercent() {
        int max = maxExScore();
        return max <= 0 ? 0 : exScore() * 100.0 / max;
    }

    /** DJ RANK by beatoraja's thresholds (AAA &gt;= 8/9, AA &gt;= 7/9, ... E &gt;= 2/9, otherwise F). */
    public String rank() {
        int ex = exScore();
        for (int k = 8; k >= 2; k--) {
            if (ex >= boundary(k)) {
                return RANK_KEYS.get(9 - k);
            }
        }
        return "F";
    }

    /**
     * The closer of "ahead of the current rank's boundary" (+) and "short of the next
     * boundary" (-), e.g. AAA+12 or MAX-30. Ties go to the + side. Above AAA the next
     * boundary is MAX; a MAX score reports MAX+0.
     */
    public BoundaryDiff nearestBoundaryDiff() {
        int ex = exScore();
        int max = maxExScore();
        if (ex >= max) {
            return new BoundaryDiff("MAX", 0);
        }
        String current = rank();
        int currentIndex = RANK_KEYS.indexOf(current);
        int currentBoundary = boundaryOf(current);
        String next = RANK_KEYS.get(currentIndex - 1);
        int nextBoundary = boundaryOf(next);

        int ahead = ex - currentBoundary;
        int behind = nextBoundary - ex;
        return ahead <= behind
                ? new BoundaryDiff(current, ahead)
                : new BoundaryDiff(next, -behind);
    }

    private int boundaryOf(String rankKey) {
        return switch (rankKey) {
            case "MAX" -> maxExScore();
            case "F" -> 0;
            default -> boundary(9 - RANK_KEYS.indexOf(rankKey));
        };
    }

    /** Minimum EX score for a rate of k/9, i.e. ceil(maxEx * k / 9). */
    private int boundary(int k) {
        return (maxExScore() * k + 8) / 9;
    }

    /** A rank boundary key (from {@link #RANK_KEYS}) and the signed EX-score difference from it. */
    public record BoundaryDiff(String rankKey, int diff) {
    }
}
