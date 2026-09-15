package com.beatoraja.screenshot.service;

import java.util.ArrayList;
import java.util.List;

public final class PostBatchSplitter {

    private PostBatchSplitter() {
    }

    public static <T> List<List<T>> partition(List<T> items, int maxBatchSize) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        if (maxBatchSize < 1) {
            throw new IllegalArgumentException("maxBatchSize must be >= 1");
        }
        List<List<T>> batches = new ArrayList<>();
        for (int i = 0; i < items.size(); i += maxBatchSize) {
            batches.add(List.copyOf(items.subList(i, Math.min(i + maxBatchSize, items.size()))));
        }
        return batches;
    }
}
