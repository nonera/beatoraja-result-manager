package com.beatoraja.screenshot.service.twitter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ChromiumBrowserLocator {

    private ChromiumBrowserLocator() {
    }

    public static Optional<Path> locate() {
        for (Path candidate : candidates()) {
            if (Files.isRegularFile(candidate)) {
                return Optional.of(candidate);
            }
        }
        return Optional.empty();
    }

    private static List<Path> candidates() {
        List<Path> paths = new ArrayList<>();
        addIfPresent(paths, envPath("ProgramFiles", "Google", "Chrome", "Application", "chrome.exe"));
        addIfPresent(paths, envPath("ProgramFiles(x86)", "Google", "Chrome", "Application", "chrome.exe"));
        addIfPresent(paths, envPath("LOCALAPPDATA", "Google", "Chrome", "Application", "chrome.exe"));
        addIfPresent(paths, envPath("ProgramFiles(x86)", "Microsoft", "Edge", "Application", "msedge.exe"));
        addIfPresent(paths, envPath("ProgramFiles", "Microsoft", "Edge", "Application", "msedge.exe"));
        addIfPresent(paths, envPath("LOCALAPPDATA", "Microsoft", "Edge", "Application", "msedge.exe"));
        return paths;
    }

    private static void addIfPresent(List<Path> paths, Path path) {
        if (path != null) {
            paths.add(path);
        }
    }

    private static Path envPath(String envName, String... parts) {
        String base = System.getenv(envName);
        if (base == null || base.isBlank()) {
            return null;
        }
        Path path = Path.of(base);
        for (String part : parts) {
            path = path.resolve(part);
        }
        return path;
    }
}
