package com.beatoraja.screenshot.service.update;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class ZipExtractUtils {

    public static final String APP_FOLDER_NAME = "beatoraja-screenshot-manager";

    private ZipExtractUtils() {
    }

    public static Path extractRootDirectory(Path zipFile, Path destinationDir) throws IOException {
        Files.createDirectories(destinationDir);

        try (InputStream input = Files.newInputStream(zipFile);
                ZipInputStream zipInputStream = new ZipInputStream(input)) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                Path resolved = destinationDir.resolve(entry.getName()).normalize();
                if (!resolved.startsWith(destinationDir.normalize())) {
                    throw new IOException("Unsafe zip entry: " + entry.getName());
                }
                Files.createDirectories(resolved.getParent());
                Files.copy(zipInputStream, resolved);
            }
        }

        Path appDir = destinationDir.resolve(APP_FOLDER_NAME);
        if (!Files.isDirectory(appDir)) {
            throw new IOException("Failed to locate extracted app directory: " + appDir);
        }
        return appDir;
    }
}
