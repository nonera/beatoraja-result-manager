package com.beatoraja.screenshot.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.beatoraja.screenshot.util.AppPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class PostedStateStore {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.INDENT_OUTPUT);

    private final Map<String, PostedRecord> records = new HashMap<>();

    public static PostedStateStore load() {
        PostedStateStore store = new PostedStateStore();
        Path file = AppPaths.postedStateFile();
        if (!Files.exists(file)) {
            return store;
        }
        try {
            PostedStateFile data = MAPPER.readValue(file.toFile(), PostedStateFile.class);
            if (data.entries != null) {
                store.records.putAll(data.entries);
            }
        } catch (IOException ignored) {
        }
        return store;
    }

    public void save() throws IOException {
        Files.createDirectories(AppPaths.appDataDir());
        PostedStateFile data = new PostedStateFile();
        data.entries = records;
        MAPPER.writeValue(AppPaths.postedStateFile().toFile(), data);
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
