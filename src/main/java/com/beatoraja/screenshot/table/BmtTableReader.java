package com.beatoraja.screenshot.table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

public final class BmtTableReader {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private BmtTableReader() {
    }

    public static List<BmtTableData> readAll(Path tablePath) throws IOException {
        List<BmtTableData> tables = new ArrayList<>();
        if (tablePath == null || !Files.isDirectory(tablePath)) {
            return tables;
        }

        try (var stream = Files.list(tablePath)) {
            stream.filter(path -> path.getFileName().toString().toLowerCase().endsWith(".bmt"))
                    .forEach(path -> {
                        try {
                            BmtTableData table = read(path);
                            if (table != null && table.folder != null) {
                                tables.add(table);
                            }
                        } catch (IOException ignored) {
                        }
                    });
        }
        return tables;
    }

    public static BmtTableData read(Path path) throws IOException {
        try (var input = new BufferedInputStream(
                path.toString().toLowerCase().endsWith(".bmt")
                        ? new GZIPInputStream(Files.newInputStream(path))
                        : Files.newInputStream(path)
        )) {
            return MAPPER.readValue(input, BmtTableData.class);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BmtTableData {
        public String name;
        public String tag;
        public String url;
        public List<BmtFolder> folder = List.of();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BmtFolder {
        public String name;
        public List<BmtSong> songs = List.of();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BmtSong {
        public String title;
        public String md5;
        public String sha256;
    }
}
