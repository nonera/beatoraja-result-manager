package com.beatoraja.screenshot.parser;

import com.beatoraja.screenshot.model.ScreenshotEntry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

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
    void parseBmzScreenshot() {
        Path file = tempDir.resolve("bmz-screenshot-1761555600123.png");
        ScreenshotEntry entry = FilenameParser.parse(file);

        assertEquals(LocalDateTime.ofInstant(Instant.ofEpochMilli(1761555600123L), ZoneId.systemDefault()),
                entry.getCapturedAt());
        assertEquals("", entry.getStateLabel());
        assertFalse(entry.isResultScreenshot());
    }

    @Test
    void parseBmzScreenshotWithCollisionSuffix() {
        assertNotNull(FilenameParser.parse(tempDir.resolve("bmz-screenshot-1761555600123-2.png")).getCapturedAt());
        assertNotNull(FilenameParser.parse(tempDir.resolve("bmz-screenshot-1761555600123-overflow.png")).getCapturedAt());
    }
}
