package com.beatoraja.screenshot.player;

import java.time.LocalDateTime;
import java.util.List;

/** A player's play history, searched for the play a screenshot was taken of. */
public interface PlayLogSource extends AutoCloseable {

    boolean isAvailable();

    /**
     * Plays that may be the one captured at {@code capturedAt}, best match first.
     * {@code expectedClearId} is the lamp read from the screenshot's file name, or null.
     */
    List<PlayerPlayLogService.PlayLogEntry> findNear(LocalDateTime capturedAt, Integer expectedClearId);

    @Override
    void close();
}
