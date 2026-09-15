package com.beatoraja.screenshot.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PostBatchSplitterTest {

    @Test
    void partitionSplitsIntoFixedSizeBatches() {
        List<List<Integer>> batches = PostBatchSplitter.partition(List.of(1, 2, 3, 4, 5, 11), 4);
        assertEquals(2, batches.size());
        assertEquals(List.of(1, 2, 3, 4), batches.get(0));
        assertEquals(List.of(5, 11), batches.get(1));
    }

    @Test
    void partitionReturnsEmptyForEmptyInput() {
        assertTrue(PostBatchSplitter.partition(List.of(), 4).isEmpty());
    }
}
