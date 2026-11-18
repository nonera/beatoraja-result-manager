package com.beatoraja.screenshot.service;

import com.beatoraja.screenshot.config.AppConfig;
import com.beatoraja.screenshot.model.ScreenshotEntry;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PostTextComposerTest {

    private static ScreenshotEntry entry(String name, String title, String clear) {
        return new ScreenshotEntry(Path.of(name), name, null, "Result", title, "", "", clear, "AA");
    }

    private final PostTextComposer composer = new PostTextComposer(new AppConfig(),
            entry -> new PostTextComposer.PostContext("st" + entry.getFileName(), null));

    @Test
    void joinsOneLinePerScreenshotInOrder() {
        String text = composer.merged(List.of(entry("1", "Song A", "HARD CLEAR"), entry("2", "Song B", "CLEAR")));

        assertEquals("st1 Song A HARD CLEAR AA\nst2 Song B CLEAR AA", text);
    }

    @Test
    void shortensLongTitlesUntilTheTweetFits() {
        List<ScreenshotEntry> batch = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            batch.add(entry(String.valueOf(i), "長い曲名".repeat(10), "HARD CLEAR"));
        }

        PostTextComposer.TweetTexts texts = composer.tweetTexts(List.of(batch));

        assertTrue(texts.abbreviatedCount() > 0);
        assertTrue(TweetTextLimits.weightedLength(texts.texts().get(0)) <= TweetTextLimits.WEIGHTED_LIMIT);
    }

    @Test
    void usesTheLampGroupedFormForManyImages() {
        List<ScreenshotEntry> batch = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            batch.add(entry(String.valueOf(i), "Song", i % 2 == 0 ? "HARD CLEAR" : "CLEAR"));
        }

        PostTextComposer.TweetTexts texts = composer.tweetTexts(List.of(batch));

        assertEquals("HC : st0 st2 st4 st6\nNC : st1 st3 st5 st7", texts.texts().get(0));
        assertEquals(0, texts.abbreviatedCount());
    }
}
