package com.beatoraja.screenshot.service;

import com.beatoraja.screenshot.model.ScreenshotEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiscordAutoPostQueueTest {

    @TempDir
    Path tempDir;

    private DiscordAutoPostQueue queue;
    private PostedStateStore postedStateStore;

    @BeforeEach
    void setUp() {
        queue = new DiscordAutoPostQueue();
        postedStateStore = new PostedStateStore();
    }

    @Test
    void offerAcceptsOnlyResultScreenshots() {
        queue.offer(resultEntry("result.png"), postedStateStore);
        queue.offer(playEntry("play.png"), postedStateStore);

        assertEquals(1, queue.size());
    }

    @Test
    void offerSkipsAlreadyPosted() {
        postedStateStore.markDiscordPosted("result.png", "message-id");

        queue.offer(resultEntry("result.png"), postedStateStore);

        assertEquals(0, queue.size());
    }

    @Test
    void pollBatchAndRequeueFront() {
        queue.offer(resultEntry("a.png"), postedStateStore);
        queue.offer(resultEntry("b.png"), postedStateStore);
        queue.offer(resultEntry("c.png"), postedStateStore);

        List<ScreenshotEntry> batch = queue.pollBatch(2);
        assertEquals(2, batch.size());
        assertEquals(1, queue.size());

        queue.requeueFront(batch);
        assertEquals(3, queue.size());
        assertEquals("a.png", queue.pollBatch(1).get(0).getFileName());
    }

    @Test
    void pollBatchNeverReturnsMoreThanAvailable() {
        queue.offer(resultEntry("only.png"), postedStateStore);

        List<ScreenshotEntry> batch = queue.pollBatch(4);

        assertEquals(1, batch.size());
        assertTrue(queue.size() >= 0);
    }

    @Test
    void removePostedDropsMatchingEntry() {
        ScreenshotEntry a = resultEntry("a.png");
        ScreenshotEntry b = resultEntry("b.png");
        queue.offer(a, postedStateStore);
        queue.offer(b, postedStateStore);

        queue.removePosted(List.of(a));

        assertEquals(1, queue.size());
        assertEquals("b.png", queue.pollBatch(1).get(0).getFileName());
    }

    private ScreenshotEntry resultEntry(String fileName) {
        return new ScreenshotEntry(
                tempDir.resolve(fileName),
                fileName,
                LocalDateTime.of(2025, 9, 8, 14, 15, 30),
                "Result",
                "Song",
                "sl12",
                "",
                "CLEAR",
                "AAA"
        );
    }

    private ScreenshotEntry playEntry(String fileName) {
        return new ScreenshotEntry(
                tempDir.resolve(fileName),
                fileName,
                LocalDateTime.of(2025, 9, 8, 14, 15, 30),
                "Play",
                "Song",
                "",
                "12",
                "",
                ""
        );
    }
}
