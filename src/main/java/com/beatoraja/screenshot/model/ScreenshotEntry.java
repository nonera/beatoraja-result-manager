package com.beatoraja.screenshot.model;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Objects;

public class ScreenshotEntry {

    private final Path filePath;
    private final String fileName;
    private final LocalDateTime capturedAt;
    private final String stateLabel;
    private final String title;
    private final String rawTableFolder;
    private final String level;
    private final String clearType;
    private final String rank;

    public ScreenshotEntry(
            Path filePath,
            String fileName,
            LocalDateTime capturedAt,
            String stateLabel,
            String title,
            String rawTableFolder,
            String level,
            String clearType,
            String rank
    ) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.capturedAt = capturedAt;
        this.stateLabel = stateLabel;
        this.title = title == null ? "" : title;
        this.rawTableFolder = rawTableFolder == null ? "" : rawTableFolder;
        this.level = level == null ? "" : level;
        this.clearType = clearType == null ? "" : clearType;
        this.rank = rank == null ? "" : rank;
    }

    public Path getFilePath() {
        return filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public LocalDateTime getCapturedAt() {
        return capturedAt;
    }

    public String getStateLabel() {
        return stateLabel;
    }

    public String getTitle() {
        return title;
    }

    public String getRawTableFolder() {
        return rawTableFolder;
    }

    /** @deprecated use {@link #getRawTableFolder()} */
    @Deprecated
    public String getTableLevel() {
        return rawTableFolder;
    }

    public String getLevel() {
        return level;
    }

    public String getClearType() {
        return clearType;
    }

    public String getRank() {
        return rank;
    }

    public boolean isBmsLevelOnly() {
        return rawTableFolder.isBlank() && !level.isBlank();
    }

    public boolean isResultScreenshot() {
        return "Result".equals(stateLabel);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ScreenshotEntry that)) {
            return false;
        }
        return Objects.equals(filePath, that.filePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filePath);
    }

    @Override
    public String toString() {
        return fileName;
    }
}
