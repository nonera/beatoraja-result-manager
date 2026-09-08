package com.beatoraja.screenshot.db;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

public record ScreenshotRecord(
        long id,
        Path filePath,
        String fileName,
        LocalDateTime capturedAt,
        String title,
        String sha256,
        String md5,
        String tableSymbol,
        String tableLevelNum,
        String level,
        String rank,
        String clearType,
        String stateLabel,
        String postNotation,
        List<String> availableNotations,
        boolean notationEdited,
        boolean resolvedFromPlayer
) {
    public String displayLevel() {
        if (!tableLevelNum.isBlank()) {
            return tableLevelNum;
        }
        if (!level.isBlank()) {
            return level;
        }
        return "";
    }
}
