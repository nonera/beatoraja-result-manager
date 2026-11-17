package com.beatoraja.screenshot.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.beatoraja.screenshot.util.AppLogging;
import com.beatoraja.screenshot.util.AppPaths;
import com.beatoraja.screenshot.util.AtomicFiles;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class PostedStateStore {

    private static final Logger LOG = AppLogging.get(PostedStateStore.class);
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.INDENT_OUTPUT);

    private final Map<String, PostedRecord> records = new HashMap<>();
    private final Path file;
    /** Why the saved state could not be read, for telling the user; null when it loaded fine. */
    private String loadProblem;

    /** An empty store that saves to the app's posted.json. */
    public PostedStateStore() {
        this(AppPaths.postedStateFile());
    }

    private PostedStateStore(Path file) {
        this.file = file;
    }

    public static PostedStateStore load() {
        return load(AppPaths.postedStateFile());
    }

    /**
     * Reads the posted state from {@code file}. An unreadable file is renamed aside rather than
     * silently treated as empty (which would make every screenshot look unposted and invite
     * re-sending), and {@link #loadProblem()} explains what happened.
     */
    static PostedStateStore load(Path file) {
        PostedStateStore store = new PostedStateStore(file);
        if (!Files.exists(file)) {
            return store;
        }
        try {
            PostedStateFile data = MAPPER.readValue(file.toFile(), PostedStateFile.class);
            if (data.entries != null) {
                store.records.putAll(data.entries);
            }
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Failed to load posted state from " + file.toAbsolutePath(), e);
            String kept;
            try {
                kept = AtomicFiles.setAside(file).getFileName().toString();
            } catch (IOException moveError) {
                LOG.log(Level.WARNING, "Failed to set aside unreadable posted state", moveError);
                kept = file.getFileName().toString();
            }
            store.loadProblem = "投稿済みの記録(" + file.getFileName() + ")を読めなかったため、" + kept
                    + " として残し、空の記録で始めました。投稿済みの表示と Discord の自動投稿に注意してください。";
        }
        return store;
    }

    public String loadProblem() {
        return loadProblem;
    }

    /** Writes the state through a temporary file, so a crash mid-save cannot corrupt it. */
    public void save() throws IOException {
        PostedStateFile data = new PostedStateFile();
        data.entries = records;
        AtomicFiles.write(file, MAPPER.writeValueAsBytes(data));
    }

    public PostedRecord get(String fileName) {
        return records.getOrDefault(fileName, new PostedRecord());
    }

    public void markTwitterPosted(String fileName, String tweetId) {
        PostedRecord record = records.computeIfAbsent(fileName, key -> new PostedRecord());
        record.twitter = new ChannelPosted(Instant.now().toString(), tweetId);
    }

    public void markDiscordPosted(String fileName, String messageId) {
        PostedRecord record = records.computeIfAbsent(fileName, key -> new PostedRecord());
        record.discord = new ChannelPosted(Instant.now().toString(), messageId);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PostedStateFile {
        public Map<String, PostedRecord> entries = new HashMap<>();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PostedRecord {
        public ChannelPosted twitter;
        public ChannelPosted discord;

        public boolean isTwitterPosted() {
            return twitter != null;
        }

        public boolean isDiscordPosted() {
            return discord != null;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ChannelPosted {
        public String postedAt;
        public String id;

        public ChannelPosted() {
        }

        public ChannelPosted(String postedAt, String id) {
            this.postedAt = postedAt;
            this.id = id;
        }
    }
}
