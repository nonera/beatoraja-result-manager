package com.beatoraja.screenshot.player;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayHistoryTest {

    private static PlayLogEntry play(String sha, long epoch) {
        return new PlayLogEntry(sha, 5, epoch, null);
    }

    private final PlayHistory history = new PlayHistory(List.of(play("c", 300), play("a", 100), play("b", 200)));

    @Test
    void returnsPlaysInsideTheWindowOldestFirst() {
        assertEquals(List.of("a", "b"), history.between(100, 250).stream().map(PlayLogEntry::sha256).toList());
        assertEquals(List.of(), history.between(301, 400));
    }

    @Test
    void findsTheLatestPlayAtOrBeforeATime() {
        assertEquals(Optional.of("b"), history.latestAtOrBefore(299).map(PlayLogEntry::sha256));
        assertEquals(Optional.of("c"), history.latestAtOrBefore(300).map(PlayLogEntry::sha256));
        assertEquals(Optional.empty(), history.latestAtOrBefore(99));
    }
}
