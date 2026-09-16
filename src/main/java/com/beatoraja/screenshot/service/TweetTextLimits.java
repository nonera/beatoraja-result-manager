package com.beatoraja.screenshot.service;

/** Twitter's post length rules: 280 units, where CJK code points count as two. */
public final class TweetTextLimits {

    public static final int WEIGHTED_LIMIT = 280;

    private TweetTextLimits() {
    }

    public static int weightedLength(String text) {
        if (text == null) {
            return 0;
        }
        int total = 0;
        for (int i = 0; i < text.length(); ) {
            int codePoint = text.codePointAt(i);
            total += isWide(codePoint) ? 2 : 1;
            i += Character.charCount(codePoint);
        }
        return total;
    }

    public static boolean exceedsLimit(String text) {
        return weightedLength(text) > WEIGHTED_LIMIT;
    }

    private static boolean isWide(int codePoint) {
        return (codePoint >= 0x1100 && codePoint <= 0x11FF)
                || (codePoint >= 0x2E80 && codePoint <= 0xA4CF)
                || (codePoint >= 0xAC00 && codePoint <= 0xD7A3)
                || (codePoint >= 0xF900 && codePoint <= 0xFAFF)
                || (codePoint >= 0xFE30 && codePoint <= 0xFE4F)
                || (codePoint >= 0xFF00 && codePoint <= 0xFF60)
                || (codePoint >= 0xFFE0 && codePoint <= 0xFFE6)
                || (codePoint >= 0x20000 && codePoint <= 0x3FFFD);
    }
}
