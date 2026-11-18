package com.beatoraja.screenshot.util;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** File writes that never leave a half-written file behind, and setting aside files that cannot be read. */
public final class AtomicFiles {

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private AtomicFiles() {
    }

    /**
     * Writes {@code content} to a temporary file next to {@code target}, then moves it over
     * {@code target}. A crash mid-write leaves the old file intact.
     */
    public static void write(Path target, byte[] content) throws IOException {
        Path directory = target.toAbsolutePath().getParent();
        Files.createDirectories(directory);
        Path temp = Files.createTempFile(directory, target.getFileName().toString(), ".tmp");
        try {
            Files.write(temp, content);
            try {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    /**
     * Renames an unreadable {@code file} to {@code <name>.broken-<yyyyMMdd-HHmmss>} so it is kept
     * for inspection and not overwritten by the next save. Returns the new path.
     */
    public static Path setAside(Path file) throws IOException {
        Path aside = file.resolveSibling(file.getFileName() + ".broken-" + LocalDateTime.now().format(STAMP));
        return Files.move(file, aside, StandardCopyOption.REPLACE_EXISTING);
    }
}
