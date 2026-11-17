package com.beatoraja.screenshot.service;

import com.beatoraja.screenshot.model.ScreenshotEntry;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BatchPosterTest {

    private static ScreenshotEntry entry(String name) {
        return new ScreenshotEntry(Path.of(name), name, null, "Result", name, "", "", "", "");
    }

    @Test
    void sendsEveryBatchInOrder() {
        List<String> sent = new ArrayList<>();
        List<Integer> started = new ArrayList<>();
        BatchPoster.Result result = BatchPoster.postInOrder(
                List.of(List.of(entry("a"), entry("b")), List.of(entry("c"))),
                List.of("first", "second"),
                (text, images) -> {
                    sent.add(text + ":" + images.size());
                    return null;
                },
                started::add);

        assertTrue(result.succeeded());
        assertEquals(List.of("first:2", "second:1"), sent);
        assertEquals(List.of(1, 2), started);
        assertEquals(3, result.posted().size());
    }

    @Test
    void stopsAtTheFirstFailureKeepingWhatWentOut() {
        List<String> sent = new ArrayList<>();
        BatchPoster.Result result = BatchPoster.postInOrder(
                List.of(List.of(entry("a")), List.of(entry("b")), List.of(entry("c"))),
                List.of("1", "2", "3"),
                (text, images) -> {
                    sent.add(text);
                    return "2".equals(text) ? "boom" : null;
                },
                batch -> { });

        assertFalse(result.succeeded());
        assertEquals("boom", result.error());
        assertEquals(List.of("1", "2"), sent);
        assertEquals(List.of("a"), result.posted().stream().map(ScreenshotEntry::getFileName).toList());
    }
}
