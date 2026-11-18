package com.beatoraja.screenshot.service;

import com.beatoraja.screenshot.db.ScreenshotRecord;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/** Deletes screenshot image files; safe to run off the EDT as it touches no database or UI. */
public final class ScreenshotFileRemover {

    /** The records whose files are gone now (deleted, or already missing), and why the others failed. */
    public record Result(List<ScreenshotRecord> removed, List<String> failures) {
    }

    private ScreenshotFileRemover() {
    }

    public static Result deleteFiles(List<ScreenshotRecord> records) {
        List<ScreenshotRecord> removed = new ArrayList<>();
        List<String> failures = new ArrayList<>();
        for (ScreenshotRecord record : records) {
            try {
                Files.deleteIfExists(record.filePath());
                removed.add(record);
            } catch (IOException e) {
                failures.add(record.fileName() + ": " + e.getMessage());
            }
        }
        return new Result(List.copyOf(removed), List.copyOf(failures));
    }
}
