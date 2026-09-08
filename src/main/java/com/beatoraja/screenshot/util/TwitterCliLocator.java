package com.beatoraja.screenshot.util;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public final class TwitterCliLocator {

    private TwitterCliLocator() {
    }

    public static Optional<Path> locate() {
        for (Path candidate : candidates()) {
            if (Files.isRegularFile(candidate)) {
                return Optional.of(candidate.toAbsolutePath().normalize());
            }
        }
        return findOnSystemPath("twitter.exe").or(() -> findOnSystemPath("twitter"));
    }

    public static Path expectedBundledPath() {
        return AppPaths.bundledTwitterExe().toAbsolutePath().normalize();
    }

    private static List<Path> candidates() {
        List<Path> paths = new ArrayList<>();
        paths.add(AppPaths.bundledTwitterExe());
        paths.add(Paths.get(System.getProperty("user.dir")).resolve("tools").resolve("twitter.exe"));

        Path projectRoot = findProjectRoot();
        if (projectRoot != null) {
            paths.add(projectRoot.resolve("tools").resolve("twitter.exe"));
        }

        String localAppData = System.getenv("LOCALAPPDATA");
        if (localAppData != null && !localAppData.isBlank()) {
            Path pythonRoot = Path.of(localAppData, "Programs", "Python");
            if (Files.isDirectory(pythonRoot)) {
                try (Stream<Path> versions = Files.list(pythonRoot)) {
                    versions.filter(Files::isDirectory)
                            .map(dir -> dir.resolve("Scripts").resolve("twitter.exe"))
                            .forEach(paths::add);
                } catch (Exception ignored) {
                }
            }
            Path localPython = Path.of(localAppData, "Python");
            if (Files.isDirectory(localPython)) {
                try (Stream<Path> versions = Files.list(localPython)) {
                    versions.filter(Files::isDirectory)
                            .map(dir -> dir.resolve("Scripts").resolve("twitter.exe"))
                            .forEach(paths::add);
                } catch (Exception ignored) {
                }
            }
        }

        String appData = System.getenv("APPDATA");
        if (appData != null && !appData.isBlank()) {
            paths.add(Path.of(appData, "Python", "Scripts", "twitter.exe"));
        }

        return paths;
    }

    private static Path findProjectRoot() {
        Path dir = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        for (int i = 0; i < 6 && dir != null; i++) {
            if (Files.exists(dir.resolve("settings.gradle.kts")) || Files.exists(dir.resolve("build.gradle.kts"))) {
                return dir;
            }
            dir = dir.getParent();
        }
        return null;
    }

    private static Optional<Path> findOnSystemPath(String command) {
        try {
            Process process = new ProcessBuilder("where", command)
                    .redirectErrorStream(true)
                    .start();
            if (!process.waitFor(8, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return Optional.empty();
            }
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            for (String line : output.split("\\R")) {
                String trimmed = line.trim();
                if (trimmed.isBlank()) {
                    continue;
                }
                Path path = Path.of(trimmed);
                if (Files.isRegularFile(path)) {
                    return Optional.of(path.toAbsolutePath().normalize());
                }
            }
        } catch (Exception ignored) {
        }
        return Optional.empty();
    }
}
