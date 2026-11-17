package com.beatoraja.screenshot.service;

import com.beatoraja.screenshot.config.AppConfig;
import com.beatoraja.screenshot.parser.FilenameParser;
import com.beatoraja.screenshot.model.ScreenshotEntry;
import com.beatoraja.screenshot.player.PlayerKind;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class ScreenshotScanner {

    public List<ScreenshotEntry> scan(Path directory) throws IOException {
        return scan(directory, PlayerKind.BEATORAJA);
    }

    /** The PNGs in {@code directory}, newest first, with names parsed as {@code player} writes them. */
    public List<ScreenshotEntry> scan(Path directory, PlayerKind player) throws IOException {
        if (directory == null || !Files.isDirectory(directory)) {
            return List.of();
        }

        List<ScreenshotEntry> entries = new ArrayList<>();
        try (Stream<Path> stream = Files.list(directory)) {
            stream.filter(path -> Files.isRegularFile(path)
                            && path.getFileName().toString().toLowerCase().endsWith(".png"))
                    .map(path -> FilenameParser.parse(path, player))
                    .sorted(Comparator.comparing(
                            ScreenshotEntry::getCapturedAt,
                            Comparator.nullsLast(Comparator.reverseOrder())
                    ).thenComparing(ScreenshotEntry::getFileName, Comparator.reverseOrder()))
                    .forEach(entries::add);
        }
        return entries;
    }
}
