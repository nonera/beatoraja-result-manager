package com.beatoraja.screenshot.service;

import com.beatoraja.screenshot.model.ScreenshotEntry;
import com.beatoraja.screenshot.util.AppLogging;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ScreenshotFolderCleanupService {

    private static final Logger LOG = AppLogging.get(ScreenshotFolderCleanupService.class);
    private final ScreenshotScanner scanner = new ScreenshotScanner();

    public int deleteAllPng(Path directory) throws IOException {
        List<ScreenshotEntry> entries = scanner.scan(directory);
        int deleted = 0;
        for (ScreenshotEntry entry : entries) {
            try {
                if (Files.deleteIfExists(entry.getFilePath())) {
                    deleted++;
                }
            } catch (IOException e) {
                LOG.log(Level.WARNING, "Failed to delete screenshot: " + entry.getFilePath(), e);
            }
        }
        return deleted;
    }
}
