package com.beatoraja.screenshot.service;

import com.beatoraja.screenshot.model.ScreenshotEntry;
import com.beatoraja.screenshot.parser.FilenameParser;
import com.beatoraja.screenshot.util.AppLogging;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ScreenshotWatcher implements AutoCloseable {

    private static final Logger LOG = AppLogging.get(ScreenshotWatcher.class);

    public interface Listener {
        void onScreenshotAdded(ScreenshotEntry entry);
    }

    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "screenshot-watcher");
        thread.setDaemon(true);
        return thread;
    });

    private WatchService watchService;
    private Path watchedDirectory;
    private volatile boolean running;

    public void start(Path directory, Listener listener) throws IOException {
        stop();
        watchedDirectory = directory;
        watchService = FileSystems.getDefault().newWatchService();
        directory.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
        running = true;

        executor.submit(() -> watchLoop(listener));
    }

    private void watchLoop(Listener listener) {
        while (running) {
            WatchKey key;
            try {
                key = watchService.poll(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            if (key == null) {
                continue;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                if (event.kind() != StandardWatchEventKinds.ENTRY_CREATE) {
                    continue;
                }
                Path created = watchedDirectory.resolve((Path) event.context());
                if (!created.getFileName().toString().toLowerCase().endsWith(".png")) {
                    continue;
                }
                waitForFile(created);
                listener.onScreenshotAdded(FilenameParser.parse(created));
            }
            key.reset();
        }
    }

    private void waitForFile(Path path) {
        for (int i = 0; i < 20; i++) {
            try {
                if (Files.exists(path) && Files.size(path) > 0) {
                    return;
                }
                Thread.sleep(100);
            } catch (IOException | InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    public void stop() {
        running = false;
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException e) {
                LOG.log(Level.WARNING, "Failed to close screenshot watch service", e);
            }
            watchService = null;
        }
    }

    @Override
    public void close() {
        stop();
        executor.shutdownNow();
    }
}
