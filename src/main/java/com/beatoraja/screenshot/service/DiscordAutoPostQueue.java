package com.beatoraja.screenshot.service;

import com.beatoraja.screenshot.model.ScreenshotEntry;
import com.beatoraja.screenshot.util.AppLogging;

import java.util.ArrayDeque;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Holds Discord-unposted screenshots detected by the folder watcher until
 * the configured batch size is reached.
 */
public class DiscordAutoPostQueue {

    private static final Logger LOG = AppLogging.get(DiscordAutoPostQueue.class);

    private final Deque<ScreenshotEntry> queue = new ArrayDeque<>();
    private final Set<String> queuedFileNames = new HashSet<>();

    public synchronized void offer(ScreenshotEntry entry, PostedStateStore postedStateStore) {
        if (entry == null || postedStateStore == null) {
            return;
        }
        if (!entry.isResultScreenshot()) {
            LOG.fine("Skipped Discord auto-post queue (not a result screenshot): " + entry.getFileName());
            return;
        }
        String fileName = entry.getFileName();
        if (postedStateStore.get(fileName).isDiscordPosted()) {
            LOG.fine("Skipped Discord auto-post queue (already posted): " + fileName);
            return;
        }
        if (queuedFileNames.contains(fileName)) {
            return;
        }
        queue.addLast(entry);
        queuedFileNames.add(fileName);
    }

    public synchronized int size() {
        return queue.size();
    }

    public synchronized List<ScreenshotEntry> pollBatch(int batchSize) {
        List<ScreenshotEntry> batch = new ArrayList<>();
        int count = Math.max(1, batchSize);
        while (batch.size() < count && !queue.isEmpty()) {
            ScreenshotEntry entry = queue.pollFirst();
            if (entry != null) {
                queuedFileNames.remove(entry.getFileName());
                batch.add(entry);
            }
        }
        return batch;
    }

    public synchronized void requeueFront(List<ScreenshotEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return;
        }
        for (int i = entries.size() - 1; i >= 0; i--) {
            ScreenshotEntry entry = entries.get(i);
            queue.addFirst(entry);
            queuedFileNames.add(entry.getFileName());
        }
    }

    /**
     * Drops entries that were just posted through another channel (e.g. a manual send) so
     * they aren't also picked up by a later auto-post batch.
     */
    public synchronized void removePosted(List<ScreenshotEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return;
        }
        for (ScreenshotEntry entry : entries) {
            String fileName = entry.getFileName();
            if (queuedFileNames.remove(fileName)) {
                queue.removeIf(e -> fileName.equals(e.getFileName()));
            }
        }
    }
}
