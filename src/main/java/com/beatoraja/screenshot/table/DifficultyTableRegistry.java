package com.beatoraja.screenshot.table;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class DifficultyTableRegistry {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final List<String> knownTags = new ArrayList<>();
    private final Map<String, Set<TableMatch>> matchesByTitle = new LinkedHashMap<>();
    private final Map<String, Set<TableMatch>> matchesBySha256 = new LinkedHashMap<>();
    private final Map<String, Set<TableMatch>> matchesByMd5 = new LinkedHashMap<>();

    public void loadFromBeatoraja(Path beatorajaDirectory) throws IOException {
        clear();
        if (beatorajaDirectory == null || !Files.isDirectory(beatorajaDirectory)) {
            return;
        }

        Path tablePath = resolveTablePath(beatorajaDirectory);
        for (BmtTableReader.BmtTableData table : BmtTableReader.readAll(tablePath)) {
            registerTable(table);
        }
    }

    public void loadFromTableDirectory(Path tablePath) throws IOException {
        clear();
        if (tablePath == null || !Files.isDirectory(tablePath)) {
            return;
        }
        for (BmtTableReader.BmtTableData table : BmtTableReader.readAll(tablePath)) {
            registerTable(table);
        }
    }

    private Path resolveTablePath(Path beatorajaDirectory) throws IOException {
        Path configFile = beatorajaDirectory.resolve("config_sys.json");
        if (!Files.exists(configFile)) {
            return beatorajaDirectory.resolve("table");
        }

        JsonNode root = MAPPER.readTree(configFile.toFile());
        String tablePathValue = root.path("tablepath").asText("table");
        Path tablePath = Path.of(tablePathValue);
        if (tablePath.isAbsolute()) {
            return tablePath;
        }
        return beatorajaDirectory.resolve(tablePath);
    }

    private void registerTable(BmtTableReader.BmtTableData table) {
        if (table.tag != null && !table.tag.isBlank()) {
            knownTags.add(table.tag);
        }

        if (table.folder == null) {
            return;
        }

        for (BmtTableReader.BmtFolder folder : table.folder) {
            if (folder.name == null || folder.songs == null) {
                continue;
            }

            TableLevelParser.ParsedTableLevel parsed = TableLevelParser.parse(folder.name, knownTags);
            String notation = parsed.notation();
            if (notation.isBlank()) {
                continue;
            }

            for (BmtTableReader.BmtSong song : folder.songs) {
                if (song.title == null || song.title.isBlank()) {
                    continue;
                }
                TableMatch match = new TableMatch(
                        table.name == null ? "" : table.name,
                        table.tag == null ? parsed.symbol() : table.tag,
                        parsed.symbol(),
                        parsed.level(),
                        notation,
                        folder.name,
                        nullToEmpty(song.md5),
                        nullToEmpty(song.sha256)
                );

                String titleKey = normalizeTitle(song.title);
                matchesByTitle.computeIfAbsent(titleKey, ignored -> new LinkedHashSet<>()).add(match);
                if (!match.sha256().isBlank()) {
                    matchesBySha256.computeIfAbsent(match.sha256(), ignored -> new LinkedHashSet<>()).add(match);
                }
                if (!match.md5().isBlank()) {
                    matchesByMd5.computeIfAbsent(match.md5(), ignored -> new LinkedHashSet<>()).add(match);
                }
            }
        }
    }

    public List<String> findNotationsForTitle(String title) {
        if (title == null || title.isBlank()) {
            return List.of();
        }
        return toNotationList(collectByTitle(title));
    }

    public List<String> findNotationsByHash(String sha256, String md5) {
        Set<TableMatch> matches = new LinkedHashSet<>();
        if (sha256 != null && !sha256.isBlank()) {
            Set<TableMatch> found = matchesBySha256.get(sha256.toLowerCase(Locale.ROOT));
            if (found != null) {
                matches.addAll(found);
            }
        }
        if (md5 != null && !md5.isBlank()) {
            Set<TableMatch> found = matchesByMd5.get(md5.toLowerCase(Locale.ROOT));
            if (found != null) {
                matches.addAll(found);
            }
        }
        return toNotationList(matches);
    }

    private Set<TableMatch> collectByTitle(String title) {
        Set<TableMatch> matches = new LinkedHashSet<>();
        String key = normalizeTitle(title);

        Set<TableMatch> exact = matchesByTitle.get(key);
        if (exact != null) {
            matches.addAll(exact);
        }

        for (Map.Entry<String, Set<TableMatch>> entry : matchesByTitle.entrySet()) {
            if (entry.getKey().contains(key) || key.contains(entry.getKey())) {
                matches.addAll(entry.getValue());
            }
        }
        return matches;
    }

    private List<String> toNotationList(Set<TableMatch> matches) {
        Set<String> notations = new TreeSet<>(Comparator.naturalOrder());
        for (TableMatch match : matches) {
            notations.add(match.notation());
        }
        return new ArrayList<>(notations);
    }

    public TableLevelParser.ParsedTableLevel parseFolderName(String folderName) {
        return TableLevelParser.parse(folderName, knownTags);
    }

    public TableLevelParser.ParsedTableLevel parseNotation(String notation) {
        return TableLevelParser.parse(notation, knownTags);
    }

    public List<String> getKnownTags() {
        return List.copyOf(knownTags);
    }

    public void clear() {
        knownTags.clear();
        matchesByTitle.clear();
        matchesBySha256.clear();
        matchesByMd5.clear();
    }

    public static String normalizeTitle(String title) {
        return title.trim()
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    public record TableMatch(
            String tableName,
            String tableTag,
            String symbol,
            String level,
            String notation,
            String folderName,
            String md5,
            String sha256
    ) {
    }
}
