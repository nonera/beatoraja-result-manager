package com.beatoraja.screenshot.table;

import com.beatoraja.screenshot.config.AppConfig;
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

public class DifficultyTableRegistry {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final List<String> knownTags = new ArrayList<>();
    private final List<String> tableKeys = new ArrayList<>();
    private final Map<String, String> tagNames = new LinkedHashMap<>();
    /** Folder-name prefix from each table's .bmt {@code tag} field (e.g. "14key闇★"). */
    private final Map<String, String> tablePrefixByKey = new LinkedHashMap<>();
    private final Map<String, Map<String, Set<String>>> notationsByTagAndSymbol = new LinkedHashMap<>();
    private final Map<String, Set<TableMatch>> matchesByTitle = new LinkedHashMap<>();
    private final Map<String, Set<TableMatch>> matchesBySha256 = new LinkedHashMap<>();
    private final Map<String, Set<TableMatch>> matchesByMd5 = new LinkedHashMap<>();
    private List<String> tablePriority = List.of();
    private List<String> failedTableFiles = List.of();
    private int loadedFileCount;
    private Map<String, TableNotationRule> notationRulesByTag = Map.of();

    /**
     * Order in which table tags should be preferred when a song's symbol/notation is
     * ambiguous across multiple loaded difficulty tables. Tags not listed sort last,
     * in their existing (alphabetical) order.
     */
    public void setTablePriority(List<String> tagsInPriorityOrder) {
        this.tablePriority = tagsInPriorityOrder == null ? List.of() : List.copyOf(tagsInPriorityOrder);
    }

    /**
     * Per-table exact-match notation rewrites, and which tables should always
     * contribute to the default notation even when outranked by another table.
     */
    public void setNotationRules(List<TableNotationRule> rules) {
        Map<String, TableNotationRule> map = new LinkedHashMap<>();
        if (rules != null) {
            for (TableNotationRule rule : rules) {
                if (rule.tableTag() != null && !rule.tableTag().isBlank()) {
                    map.put(rule.tableTag(), rule);
                }
            }
        }
        this.notationRulesByTag = map;
    }

    public void loadFromBeatoraja(Path beatorajaDirectory) throws IOException {
        clear();
        if (beatorajaDirectory == null || !Files.isDirectory(beatorajaDirectory)) {
            return;
        }

        Path tablePath = resolveTablePath(beatorajaDirectory);
        loadFromResolvedTablePath(tablePath);
    }

    public void loadFromTableDirectory(Path tablePath) throws IOException {
        clear();
        if (tablePath == null || !Files.isDirectory(tablePath)) {
            return;
        }
        loadFromResolvedTablePath(tablePath);
    }

    private void loadFromResolvedTablePath(Path tablePath) throws IOException {
        BmtTableReader.ReadResult result = BmtTableReader.readAllWithDiagnostics(tablePath);
        for (BmtTableReader.BmtTableData table : result.tables()) {
            registerTable(table);
        }
        failedTableFiles = result.failedFiles();
        loadedFileCount = result.tables().size();
    }

    /** .bmt files that failed to parse during the last load, by file name. */
    public List<String> getFailedTableFiles() {
        return failedTableFiles;
    }

    /**
     * Number of .bmt files successfully parsed on the last load, before collapsing
     * same-tag/same-name entries into one table. If this is much higher than
     * {@link #getKnownTables()}'s size, the table directory likely has stale
     * duplicate .bmt files left behind by whatever manages it (e.g. beatoraja
     * itself or an external tool like BeMusicSeeker re-exporting without cleanup).
     */
    public int getLoadedFileCount() {
        return loadedFileCount;
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
        String tableName = table.name == null ? "" : table.name;
        // Symbols (e.g. "sl") are only reliable for TableLevelParser's folder-name
        // parsing; they're not a safe identity key (some sources reuse the same
        // symbol across distinct tables, or vary it across re-exports of the same
        // table). Identity/dedup instead uses an exact match on the official name.
        if (table.tag != null && !table.tag.isBlank()) {
            knownTags.add(table.tag);
        }
        String tableKey = tableName;
        if (!tableKey.isBlank()) {
            tableKeys.add(tableKey);
            tagNames.putIfAbsent(tableKey, tableName);
            if (table.tag != null && !table.tag.isBlank()) {
                tablePrefixByKey.put(tableKey, table.tag);
            }
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

            String folderTag = !tableKey.isBlank() ? tableKey : parsed.symbol();
            if (!parsed.symbol().isBlank()) {
                notationsByTagAndSymbol.computeIfAbsent(folderTag, ignored -> new LinkedHashMap<>())
                        .computeIfAbsent(parsed.symbol(), ignored -> new LinkedHashSet<>())
                        .add(notation);
            }

            for (BmtTableReader.BmtSong song : folder.songs) {
                if (song.title == null || song.title.isBlank()) {
                    continue;
                }
                TableMatch match = new TableMatch(
                        tableName,
                        folderTag,
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
        return resolve(collectByTitle(title)).candidates();
    }

    public List<String> findNotationsByHash(String sha256, String md5) {
        return resolve(collectByHash(sha256, md5)).candidates();
    }

    public NotationResolution resolveForTitle(String title) {
        if (title == null || title.isBlank()) {
            return new NotationResolution("", "", List.of());
        }
        return resolve(collectByTitle(title));
    }

    public NotationResolution resolveByHash(String sha256, String md5) {
        return resolve(collectByHash(sha256, md5));
    }

    private Set<TableMatch> collectByHash(String sha256, String md5) {
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
        return matches;
    }

    private Set<TableMatch> collectByTitle(String title) {
        String key = normalizeTitle(title);
        Set<TableMatch> exact = matchesByTitle.get(key);
        return exact != null ? exact : Set.of();
    }

    /**
     * Sorts matches by table priority, applies each table's rename rules, and
     * combines the top-priority notation with any "always include" tables'
     * notations into a single default while keeping every distinct notation
     * available as a candidate (e.g. for manual selection).
     */
    private NotationResolution resolve(Set<TableMatch> matches) {
        List<TableMatch> ordered = new ArrayList<>(matches);
        ordered.sort(Comparator
                .comparingInt((TableMatch match) -> priorityIndex(match.tableTag()))
                .thenComparing(TableMatch::notation));

        Set<String> candidates = new LinkedHashSet<>();
        String preferred = null;
        List<String> alwaysInclude = new ArrayList<>();
        for (TableMatch match : ordered) {
            String effective = applyRename(match);
            candidates.add(effective);
            if (preferred == null) {
                preferred = effective;
            }
            TableNotationRule rule = notationRulesByTag.get(match.tableTag());
            if (rule != null && rule.alwaysInclude() && !effective.equals(preferred) && !alwaysInclude.contains(effective)) {
                alwaysInclude.add(effective);
            }
        }

        if (preferred == null) {
            return new NotationResolution("", "", List.of());
        }
        List<String> defaultParts = new ArrayList<>();
        defaultParts.add(preferred);
        defaultParts.addAll(alwaysInclude);
        return new NotationResolution(preferred, String.join("/", defaultParts), new ArrayList<>(candidates));
    }

    /**
     * Replaces the shared table prefix (.bmt tag when available, otherwise the common
     * symbol prefix) while keeping level suffixes intact, including decimals and
     * embedded stars such as "★1" in "★★1".
     */
    private String applyRename(TableMatch match) {
        TableNotationRule rule = notationRulesByTag.get(match.tableTag());
        if (rule == null) {
            return match.notation();
        }
        String override = resolveSymbolOverride(rule, match.symbol());
        if (override == null || override.isBlank()) {
            return match.notation();
        }
        String prefix = resolveRewritePrefix(match);
        String rewritten = NotationPrefixResolver.applySymbolOverride(match.notation(), prefix, override);
        if (!rewritten.equals(match.notation())) {
            return rewritten;
        }
        if (!match.level().isBlank()) {
            return override + match.level();
        }
        return match.notation();
    }

    private static String resolveSymbolOverride(TableNotationRule rule, String symbol) {
        Map<String, String> overrides = rule.symbolOverrides();
        String tableWide = overrides.get(AppConfig.TableNotationRule.TABLE_WIDE_OVERRIDE_KEY);
        if (tableWide != null && !tableWide.isBlank()) {
            return tableWide;
        }
        if (symbol != null && !symbol.isBlank()) {
            return overrides.get(symbol);
        }
        return null;
    }

    String resolveRewritePrefix(TableMatch match) {
        String tablePrefix = tablePrefixByKey.get(match.tableTag());
        if (tablePrefix != null && !tablePrefix.isBlank() && match.notation().startsWith(tablePrefix)) {
            return tablePrefix;
        }
        List<String> groupNotations = getNotationsForTagAndSymbol(match.tableTag(), match.symbol());
        return NotationPrefixResolver.resolveReplaceablePrefix(groupNotations, knownTags, match.symbol());
    }

    public String getTablePrefix(String tableKey) {
        return tablePrefixByKey.getOrDefault(tableKey, "");
    }

    private int priorityIndex(String tableTag) {
        int index = tablePriority.indexOf(tableTag);
        return index < 0 ? Integer.MAX_VALUE : index;
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

    public List<TableInfo> getKnownTables() {
        Set<String> seen = new LinkedHashSet<>(tableKeys);
        List<TableInfo> result = new ArrayList<>();
        for (String key : seen) {
            result.add(new TableInfo(key, tagNames.getOrDefault(key, key)));
        }
        return result;
    }

    /**
     * All distinct raw symbols (e.g. "A", "AA") registered under the given table,
     * in the order their folders appear. Usually just one; tables that switch
     * symbol partway through their level progression have more than one.
     */
    public List<String> getSymbolsForTag(String tag) {
        Map<String, Set<String>> bySymbol = notationsByTagAndSymbol.get(tag);
        return bySymbol == null ? List.of() : new ArrayList<>(bySymbol.keySet());
    }

    /**
     * All distinct raw symbols across loaded difficulty tables, ordered by configured
     * table priority then folder appearance order within each table.
     */
    public List<String> getSymbolsInPriorityOrder() {
        List<String> tagsInOrder = new ArrayList<>();
        for (String tag : tablePriority) {
            if (notationsByTagAndSymbol.containsKey(tag) && !tagsInOrder.contains(tag)) {
                tagsInOrder.add(tag);
            }
        }
        for (String tag : notationsByTagAndSymbol.keySet()) {
            if (!tagsInOrder.contains(tag)) {
                tagsInOrder.add(tag);
            }
        }

        List<String> result = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (String tag : tagsInOrder) {
            for (String symbol : getSymbolsForTag(tag)) {
                if (seen.add(symbol)) {
                    result.add(symbol);
                }
            }
        }
        return result;
    }

    /**
     * All distinct notations (e.g. "A1".."A9") registered under the given table
     * for one specific raw symbol, in the order their folders appear. Used to let
     * the user see what a symbol rename will actually affect.
     */
    public List<String> getNotationsForTagAndSymbol(String tag, String symbol) {
        Map<String, Set<String>> bySymbol = notationsByTagAndSymbol.get(tag);
        if (bySymbol == null) {
            return List.of();
        }
        Set<String> found = bySymbol.get(symbol);
        return found == null ? List.of() : new ArrayList<>(found);
    }

    public void clear() {
        knownTags.clear();
        tableKeys.clear();
        tagNames.clear();
        tablePrefixByKey.clear();
        notationsByTagAndSymbol.clear();
        failedTableFiles = List.of();
        loadedFileCount = 0;
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

    public record TableInfo(String tag, String name) {
    }

    /**
     * {@code primary} is the single top-priority notation (used for symbol/level
     * display); {@code defaultPostNotation} is {@code primary} plus any "always
     * include" tables' notations joined with "/".
     */
    public record NotationResolution(String primary, String defaultPostNotation, List<String> candidates) {
    }

    /**
     * {@code symbolOverrides} normally holds one table-wide entry under the empty
     * string key; legacy per-symbol keys are still honored as a fallback.
     */
    public record TableNotationRule(String tableTag, boolean alwaysInclude, Map<String, String> symbolOverrides) {
        public TableNotationRule {
            symbolOverrides = symbolOverrides == null ? Map.of() : symbolOverrides;
        }
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
