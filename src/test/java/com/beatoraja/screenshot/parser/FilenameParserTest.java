package com.beatoraja.screenshot.parser;

import com.beatoraja.screenshot.model.ScreenshotEntry;
import com.beatoraja.screenshot.player.PlayerKind;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilenameParserTest {

    @TempDir
    Path tempDir;

    @Test
    void parseResultScreenshot() {
        Path file = tempDir.resolve("20250908_141530_LEVEL12 Song CLEAR AAA.png");
        ScreenshotEntry entry = FilenameParser.parse(file);

        assertEquals("Result", entry.getStateLabel());
        assertEquals("Song", entry.getTitle());
        assertTrue(entry.isResultScreenshot());
    }

    @Test
    void parsePlayScreenshot() {
        Path file = tempDir.resolve("20250908_141530_Play_LEVEL12 Song.png");
        ScreenshotEntry entry = FilenameParser.parse(file);

        assertEquals("Play", entry.getStateLabel());
        assertFalse(entry.isResultScreenshot());
    }

    @Test
    void parseMusicSelectScreenshot() {
        Path file = tempDir.resolve("20250908_141530_Music_Select.png");
        ScreenshotEntry entry = FilenameParser.parse(file);

        assertEquals("Music Select", entry.getStateLabel());
        assertFalse(entry.isResultScreenshot());
    }

    @Test
    void parseTableResultScreenshot() {
        Path file = tempDir.resolve("20250908_141530_sl12 Song CLEAR AAA.png");
        ScreenshotEntry entry = FilenameParser.parse(file);

        assertEquals("Result", entry.getStateLabel());
        assertEquals("sl12", entry.getRawTableFolder());
        assertTrue(entry.isResultScreenshot());
    }

    @Test
    void parseBmzResultScreenshot() {
        ScreenshotEntry entry = FilenameParser.parse(tempDir.resolve("20261105_203045_123_Song_[ANOTHER]_Hard.png"), PlayerKind.BMZ);

        assertEquals(LocalDateTime.of(2026, 11, 5, 20, 30, 45, 123_000_000), entry.getCapturedAt());
        assertTrue(entry.isResultScreenshot());
        assertEquals("Song_[ANOTHER]", entry.getTitle());
        assertEquals("HARD CLEAR", entry.getClearType());
        assertEquals("", entry.getRank());
        assertEquals("", entry.getRawTableFolder());
    }

    @Test
    void parseBmzResultScreenshotWithDuplicateCounterAndLabelInTitle() {
        ScreenshotEntry numbered = FilenameParser.parse(tempDir.resolve("20261105_203045_123_曲名_LightAssistEasy_1.png"), PlayerKind.BMZ);
        assertEquals("曲名", numbered.getTitle());
        assertEquals("LIGHT ASSIST EASY CLEAR", numbered.getClearType());

        ScreenshotEntry labelInTitle = FilenameParser.parse(tempDir.resolve("20261105_203045_123_Song_Hard_Failed.png"), PlayerKind.BMZ);
        assertEquals("Song_Hard", labelInTitle.getTitle());
        assertEquals("FAILED", labelInTitle.getClearType());
    }

    @Test
    void parseBmzNonResultScreenshots() {
        ScreenshotEntry select = FilenameParser.parse(tempDir.resolve("20261105_203045_123_Music_Select.png"), PlayerKind.BMZ);
        ScreenshotEntry folder = FilenameParser.parse(tempDir.resolve("20261105_203045_123_Music_Select_Stella ＞ st0.png"), PlayerKind.BMZ);
        ScreenshotEntry decide = FilenameParser.parse(tempDir.resolve("20261105_203045_123_Decide_曲名_[ANOTHER].png"), PlayerKind.BMZ);
        ScreenshotEntry play = FilenameParser.parse(tempDir.resolve("20261105_203045_123_Play_曲名_[ANOTHER].png"), PlayerKind.BMZ);

        assertEquals("Music Select", select.getStateLabel());
        assertEquals("Music Select", folder.getStateLabel());
        assertEquals("Decide", decide.getStateLabel());
        assertEquals("Play", play.getStateLabel());
        assertEquals("曲名_[ANOTHER]", play.getTitle());
        assertNotNull(play.getCapturedAt());
    }

    @Test
    void bmzResultWithUnknownLampKeepsWholeBodyAsTitle() {
        ScreenshotEntry entry = FilenameParser.parse(tempDir.resolve("20261105_203045_123_Song_Hazard.png"), PlayerKind.BMZ);

        assertEquals(LocalDateTime.of(2026, 11, 5, 20, 30, 45, 123_000_000), entry.getCapturedAt());
        assertTrue(entry.isResultScreenshot());
        assertEquals("Song_Hazard", entry.getTitle());
        assertEquals("", entry.getClearType());
    }

    @Test
    void beatorajaPlayerParsesMillisecondLikeNamesAsBeatoraja() {
        ScreenshotEntry entry = FilenameParser.parse(tempDir.resolve("20250908_141530_123_456 CLEAR AAA.png"));

        assertEquals(LocalDateTime.of(2025, 9, 8, 14, 15, 30), entry.getCapturedAt());
        assertEquals("CLEAR", entry.getClearType());
        assertEquals("AAA", entry.getRank());
    }

    @Test
    void bmzPlayerIgnoresOtherNames() {
        ScreenshotEntry entry = FilenameParser.parse(tempDir.resolve("bmz-screenshot-1761555600123.png"), PlayerKind.BMZ);

        assertEquals("", entry.getStateLabel());
        assertFalse(entry.isResultScreenshot());
    }
}
