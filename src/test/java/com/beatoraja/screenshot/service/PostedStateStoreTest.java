package com.beatoraja.screenshot.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PostedStateStoreTest {

    @TempDir
    Path tempDir;

    @Test
    void savesAndLoadsBackWithoutLeavingTemporaryFiles() throws Exception {
        Path file = tempDir.resolve("posted.json");
        PostedStateStore store = PostedStateStore.load(file);
        store.markDiscordPosted("a.png", "123");
        store.save();

        PostedStateStore loaded = PostedStateStore.load(file);
        assertNull(loaded.loadProblem());
        assertTrue(loaded.get("a.png").isDiscordPosted());
        assertFalse(loaded.get("a.png").isTwitterPosted());
        try (Stream<Path> files = Files.list(tempDir)) {
            assertEquals(List.of("posted.json"), files.map(p -> p.getFileName().toString()).toList());
        }
    }

    @Test
    void setsAsideAnUnreadableFileAndSaysSo() throws Exception {
        Path file = tempDir.resolve("posted.json");
        Files.writeString(file, "{ broken");

        PostedStateStore store = PostedStateStore.load(file);

        assertNotNull(store.loadProblem());
        assertFalse(Files.exists(file));
        try (Stream<Path> files = Files.list(tempDir)) {
            assertTrue(files.anyMatch(p -> p.getFileName().toString().startsWith("posted.json.broken-")));
        }
    }
}
