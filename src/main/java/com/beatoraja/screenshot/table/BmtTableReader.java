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
        return readAllWithDiagnostics(tablePath).tables();
    }

    /**
     * Like {@link #readAll}, but also reports which .bmt files failed to parse
     * (corrupt, unexpected structure, etc.) instead of silently dropping them -
     * so a table that's missing from the app can actually be tracked down.
     */
    public static ReadResult readAllWithDiagnostics(Path tablePath) throws IOException {
        List<BmtTableData> tables = new ArrayList<>();
        List<String> failedFiles = new ArrayList<>();
        if (tablePath == null || !Files.isDirectory(tablePath)) {
            return new ReadResult(tables, failedFiles);
        }

        try (var stream = Files.list(tablePath)) {
            stream.filter(path -> path.getFileName().toString().toLowerCase().endsWith(".bmt"))
                    .forEach(path -> {
                        try {
                            BmtTableData table = read(path);
                            if (table != null) {
                                tables.add(table);
                            } else {
                                failedFiles.add(path.getFileName().toString());
                            }
                        } catch (Exception e) {
                            failedFiles.add(path.getFileName().toString());
                        }
                    });
        }
        return new ReadResult(tables, failedFiles);
    }

    public record ReadResult(List<BmtTableData> tables, List<String> failedFiles) {
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
