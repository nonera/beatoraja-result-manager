package com.beatoraja.screenshot.table;

import com.beatoraja.screenshot.model.ScreenshotEntry;

import java.util.ArrayList;
import java.util.List;

import java.nio.file.Path;

/**
 * @deprecated Use {@link com.beatoraja.screenshot.service.ChartResolverService} instead.
 */
@Deprecated
public class TableLookupService {

    public record EnrichedScreenshot(
            ScreenshotEntry entry,
            String sha256,
            String md5,
            String resolvedTitle,
            String tableSymbol,
            String tableLevelNum,
            String level,
            String displaySymbol,
            String displayLevel,
            String defaultPostNotation,
            List<String> availableNotations,
            boolean resolvedFromPlayerData
    ) {
        public EnrichedScreenshot(
                ScreenshotEntry entry,
                String tableSymbol,
                String tableLevelNum,
                String displaySymbol,
                String displayLevel,
                String defaultPostNotation,
                List<String> availableNotations
        ) {
            this(entry, "", "", entry.getTitle(), tableSymbol, tableLevelNum, "",
                    displaySymbol, displayLevel, defaultPostNotation, availableNotations, false);
        }

        public List<String> mergedAvailableNotations() {
            List<String> merged = new ArrayList<>();
            if (availableNotations != null) {
                merged.addAll(availableNotations);
            }
            if (defaultPostNotation != null && !defaultPostNotation.isBlank() && !merged.contains(defaultPostNotation)) {
                merged.add(0, defaultPostNotation);
            }
            return merged;
        }
    }

    public static Path deriveBeatorajaDirectory(java.nio.file.Path screenshotDirectory) {
        if (screenshotDirectory == null) {
            return null;
        }
        java.nio.file.Path dir = screenshotDirectory.toAbsolutePath().normalize();
        if (dir.getFileName() != null && "screenshot".equalsIgnoreCase(dir.getFileName().toString())) {
            return dir.getParent();
        }
        return dir;
    }
}
