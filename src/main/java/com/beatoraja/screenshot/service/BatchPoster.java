package com.beatoraja.screenshot.service;

import com.beatoraja.screenshot.model.ScreenshotEntry;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

/** Sends batches of screenshots one after another, stopping at the first failure. */
public final class BatchPoster {

    /** Sends one post; returns null on success, else the error message to show. */
    @FunctionalInterface
    public interface Sender {
        String send(String text, List<Path> imagePaths);
    }

    /** The screenshots that went out before any failure, and that failure's message (null if none). */
    public record Result(List<ScreenshotEntry> posted, String error) {
        public boolean succeeded() {
            return error == null;
        }
    }

    private BatchPoster() {
    }

    /**
     * Sends {@code batches.get(i)} with {@code texts.get(i)} in order. {@code onBatchStart}
     * receives the 1-based number of each batch before it is sent.
     */
    public static Result postInOrder(List<List<ScreenshotEntry>> batches, List<String> texts, Sender sender,
            IntConsumer onBatchStart) {
        List<ScreenshotEntry> posted = new ArrayList<>();
        for (int i = 0; i < batches.size(); i++) {
            List<ScreenshotEntry> batch = batches.get(i);
            onBatchStart.accept(i + 1);
            String error = sender.send(texts.get(i), batch.stream().map(ScreenshotEntry::getFilePath).toList());
            if (error != null) {
                return new Result(List.copyOf(posted), error);
            }
            posted.addAll(batch);
        }
        return new Result(List.copyOf(posted), null);
    }
}
