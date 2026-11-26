package com.beatoraja.screenshot.service;

import com.beatoraja.screenshot.config.AppConfig;
import com.beatoraja.screenshot.model.EnrichedScreenshot;
import com.beatoraja.screenshot.model.ScreenshotEntry;
import com.beatoraja.screenshot.player.BeatorajaPaths;
import com.beatoraja.screenshot.player.BmzPaths;
import com.beatoraja.screenshot.player.BmzPlayLogService;
import com.beatoraja.screenshot.player.ClearTypeMapper;
import com.beatoraja.screenshot.player.PlayLogEntry;
import com.beatoraja.screenshot.player.PlayLogSource;
import com.beatoraja.screenshot.player.PlayScore;
import com.beatoraja.screenshot.player.PlayerKind;
import com.beatoraja.screenshot.player.PlayerPlayLogService;
import com.beatoraja.screenshot.player.SongDatabaseService;
import com.beatoraja.screenshot.table.BmzTableReader;
import com.beatoraja.screenshot.table.DifficultyTableRegistry;
import com.beatoraja.screenshot.table.TableLevelParser;
import com.beatoraja.screenshot.util.AppLogging;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ChartResolverService implements AutoCloseable {

    private static final Logger LOG = AppLogging.get(ChartResolverService.class);

    private final DifficultyTableRegistry registry = new DifficultyTableRegistry();
    private PlayLogSource playLogService;
    private SongDatabaseService songDatabaseService;
    private Path beatorajaDirectory;
    private PlayerKind playerKind = PlayerKind.BEATORAJA;
    /** The play-history and song databases opened by the last reload, for {@link #reloadPlayData()}. */
    private Path playLogDb;
    private Path songDb;

    public void reload(AppConfig config) throws IOException {
        closeConnections();
        playLogDb = null;
        songDb = null;

        beatorajaDirectory = config.resolveBeatorajaDirectory();
        playerKind = config.playerKind();
        if (beatorajaDirectory == null || !beatorajaDirectory.toFile().isDirectory()) {
            registry.clear();
            return;
        }
        if (playerKind == PlayerKind.BMZ) {
            reloadBmz(config);
            return;
        }

        BeatorajaPaths paths = BeatorajaPaths.resolve(beatorajaDirectory, config.getPlayerName());
        registry.loadFromTableDirectory(paths.tableDirectory());
        registry.setTablePriority(config.getTablePriorityOrder());
        registry.setNotationRules(toRegistryRules(config.getTableNotationRules()));
        try {
            playLogDb = paths.scoreDataLogDb();
            songDb = paths.songDatabase();
            openPlayData();
        } catch (java.sql.SQLException e) {
            throw new IOException("プレイヤーデータベースを開けません: " + e.getMessage(), e);
        }
    }

    private void reloadBmz(AppConfig config) throws IOException {
        BmzPaths paths = BmzPaths.resolve(beatorajaDirectory, config.getPlayerName());
        try {
            registry.loadFromTables(BmzTableReader.readAll(paths.libraryDb()));
            registry.setTablePriority(config.getTablePriorityOrder());
            registry.setNotationRules(toRegistryRules(config.getTableNotationRules()));
            playLogDb = paths.scoreDb();
            songDb = paths.libraryDb();
            openPlayData();
        } catch (java.sql.SQLException e) {
            throw new IOException("BMZ Player のデータベースを開けません: " + e.getMessage(), e);
        }
    }

    private void openPlayData() throws java.sql.SQLException {
        if (playerKind == PlayerKind.BMZ) {
            playLogService = new BmzPlayLogService(playLogDb);
            songDatabaseService = SongDatabaseService.forBmzLibrary(songDb);
        } else {
            playLogService = new PlayerPlayLogService(playLogDb);
            songDatabaseService = new SongDatabaseService(songDb);
        }
    }

    /**
     * Re-reads the play history and the song database - enough to see a play saved, or a song
     * added, after the last reload - leaving the difficulty tables (slow to parse) as they are.
     */
    public void reloadPlayData() throws IOException {
        if (playLogDb == null) {
            return;
        }
        closeConnections();
        try {
            openPlayData();
        } catch (java.sql.SQLException e) {
            closeConnections();
            throw new IOException("プレイ履歴・曲データベースを開けません: " + e.getMessage(), e);
        }
    }

    public PlayerKind getPlayerKind() {
        return playerKind;
    }

    public List<DifficultyTableRegistry.TableInfo> getKnownTables() {
        return registry.getKnownTables();
    }

    public List<String> getSymbolsForTag(String tag) {
        return registry.getSymbolsForTag(tag);
    }

    public List<String> getSymbolsInPriorityOrder() {
        return registry.getSymbolsInPriorityOrder();
    }

    public List<String> getNotationsForTagAndSymbol(String tag, String symbol) {
        return registry.getNotationsForTagAndSymbol(tag, symbol);
    }

    public List<String> getKnownTags() {
        return registry.getKnownTags();
    }

    public String getTablePrefix(String tableKey) {
        return registry.getTablePrefix(tableKey);
    }

    public List<String> getFailedTableFiles() {
        return registry.getFailedTableFiles();
    }

    public int getLoadedFileCount() {
        return registry.getLoadedFileCount();
    }

    private List<DifficultyTableRegistry.TableNotationRule> toRegistryRules(List<AppConfig.TableNotationRule> configRules) {
        List<DifficultyTableRegistry.TableNotationRule> result = new ArrayList<>();
        if (configRules == null) {
            return result;
        }
        for (AppConfig.TableNotationRule rule : configRules) {
            result.add(new DifficultyTableRegistry.TableNotationRule(
                    rule.getTableTag(), rule.isAlwaysInclude(), rule.getSymbolOverrides()));
        }
        return result;
    }

    public EnrichedScreenshot enrich(ScreenshotEntry entry) {
        ScreenshotEntry named = withBmzTableLabel(entry);
        ResolvedChart resolved = resolveFromPlayerData(named);
        if (resolved != null) {
            return buildEnriched(withPlayResult(named, resolved), resolved);
        }
        return enrichFromFilename(named);
    }

    /**
     * BMZ Player v0.5.3+ puts the chart's difficulty-table label before the title
     * ({@code ★12_Song_[ANOTHER]}). The file name parser cannot tell that label from a title that
     * itself holds an underscore, so it is split off here, and only when it names a level folder
     * of a loaded table; it then serves as the table folder, as beatoraja's folder name does.
     */
    private ScreenshotEntry withBmzTableLabel(ScreenshotEntry entry) {
        if (playerKind != PlayerKind.BMZ || !entry.getRawTableFolder().isBlank()) {
            return entry;
        }
        String title = entry.getTitle();
        int separator = title.indexOf('_');
        if (separator <= 0 || separator == title.length() - 1) {
            return entry;
        }
        String label = title.substring(0, separator);
        if (!registry.isKnownFolderName(label)) {
            return entry;
        }
        return new ScreenshotEntry(
                entry.getFilePath(),
                entry.getFileName(),
                entry.getCapturedAt(),
                entry.getStateLabel(),
                title.substring(separator + 1),
                label,
                entry.getLevel(),
                entry.getClearType(),
                entry.getRank()
        );
    }

    private ResolvedChart resolveFromPlayerData(ScreenshotEntry entry) {
        if (playLogService == null || !playLogService.isAvailable()) {
            return null;
        }
        if (entry.getCapturedAt() == null) {
            return null;
        }
        if (!entry.mayShowPlayResult()) {
            return null;
        }

        try {
            Integer clearId = ClearTypeMapper.toClearId(entry.getClearType());
            List<PlayLogEntry> candidates = playLogService.findNear(entry.getCapturedAt(), clearId);
            if (candidates.isEmpty()) {
                return null;
            }

            PlayLogEntry best = candidates.get(0);
            SongDatabaseService.SongRecord song = songDatabaseService != null
                    ? songDatabaseService.findBySha256(best.sha256())
                    : null;

            String md5 = song == null ? "" : song.md5();
            String sha256 = best.sha256();
            String fullTitle = song != null && !song.fullTitle().isBlank() ? song.fullTitle() : entry.getTitle();
            String bareTitle = song != null ? song.title() : "";

            DifficultyTableRegistry.NotationResolution resolution = registry.resolveByHash(sha256, md5);
            // Difficulty tables register songs under either the bare TITLE or the
            // TITLE+SUBTITLE display form depending on the table generator, so try both.
            if (resolution.candidates().isEmpty() && !fullTitle.isBlank()) {
                resolution = registry.resolveForTitle(fullTitle);
            }
            if (resolution.candidates().isEmpty() && !bareTitle.isBlank() && !bareTitle.equals(fullTitle)) {
                resolution = registry.resolveForTitle(bareTitle);
            }

            String primaryNotation = resolution.primary();
            if (primaryNotation.isBlank() && entry.getRawTableFolder() != null && !entry.getRawTableFolder().isBlank()) {
                primaryNotation = registry.parseFolderName(entry.getRawTableFolder()).notation();
            }
            String defaultPostNotation = !resolution.defaultPostNotation().isBlank()
                    ? resolution.defaultPostNotation()
                    : primaryNotation;

            return new ResolvedChart(sha256, md5, fullTitle, primaryNotation, defaultPostNotation,
                    resolution.candidates(), song != null ? String.valueOf(song.level()) : "", best.score(),
                    best.clearId());
        } catch (Exception e) {
            LOG.log(Level.FINE, "Failed to resolve chart from player data for " + entry.getFileName(), e);
            return null;
        }
    }

    private EnrichedScreenshot enrichFromFilename(ScreenshotEntry entry) {
        TableLevelParser.ParsedTableLevel parsed;
        if (entry.isBmsLevelOnly()) {
            parsed = new TableLevelParser.ParsedTableLevel("", entry.getLevel(), true);
        } else if (!entry.getRawTableFolder().isBlank()) {
            parsed = registry.parseFolderName(entry.getRawTableFolder());
        } else {
            parsed = TableLevelParser.ParsedTableLevel.empty();
        }

        DifficultyTableRegistry.NotationResolution resolution = registry.resolveForTitle(entry.getTitle());
        List<String> available = new ArrayList<>(resolution.candidates());
        String folderNotation = parsed.notation();
        String primaryNotation = !folderNotation.isBlank() ? folderNotation : resolution.primary();
        if (!primaryNotation.isBlank() && !available.contains(primaryNotation)) {
            available.add(0, primaryNotation);
        }
        String defaultPostNotation = !folderNotation.isBlank() ? folderNotation : resolution.defaultPostNotation();
        if (defaultPostNotation.isBlank() && !available.isEmpty()) {
            defaultPostNotation = available.get(0);
        }

        return buildEnriched(entry, new ResolvedChart(
                "",
                "",
                entry.getTitle(),
                primaryNotation,
                defaultPostNotation,
                available,
                entry.isBmsLevelOnly() ? entry.getLevel() : "",
                null,
                null
        ));
    }

    /**
     * BMZ Player screenshot names carry only a sanitized title, the lamp and (v0.5.3+) the
     * table label, so once the play is
     * found its song, lamp and rank stand in for what beatoraja would have written into the file
     * name. beatoraja entries are returned unchanged.
     */
    private ScreenshotEntry withPlayResult(ScreenshotEntry entry, ResolvedChart resolved) {
        if (playerKind != PlayerKind.BMZ) {
            return entry;
        }
        return new ScreenshotEntry(
                entry.getFilePath(),
                entry.getFileName(),
                entry.getCapturedAt(),
                "Result",
                resolved.title(),
                "",
                resolved.bmsLevel(),
                resolved.clearId() == null ? "" : ClearTypeMapper.toClearName(resolved.clearId()),
                resolved.score() == null ? "" : resolved.score().rank()
        );
    }

    private EnrichedScreenshot buildEnriched(ScreenshotEntry entry, ResolvedChart resolved) {
        TableLevelParser.ParsedTableLevel parsed = resolved.primaryNotation().isBlank()
                ? TableLevelParser.ParsedTableLevel.empty()
                : registry.parseNotation(resolved.primaryNotation());

        String tableSymbol = parsed.hasTableSymbol() ? parsed.symbol() : "";
        String tableLevelNum = parsed.hasTableSymbol() ? parsed.level() : "";
        String bmsLevel = entry.isBmsLevelOnly() ? entry.getLevel() : resolved.bmsLevel();

        if (tableSymbol.isBlank() && tableLevelNum.isBlank() && !bmsLevel.isBlank()) {
            tableLevelNum = "";
        }

        List<String> available = mergeNotations(resolved.availableNotations(), resolved.primaryNotation(), entry.getRawTableFolder());

        return new EnrichedScreenshot(
                entry,
                resolved.sha256(),
                resolved.md5(),
                resolved.title(),
                tableSymbol,
                tableLevelNum,
                bmsLevel,
                tableSymbol,
                parsed.hasTableSymbol() ? parsed.level() : bmsLevel,
                resolved.defaultPostNotation(),
                available,
                !resolved.sha256().isBlank(),
                resolved.score()
        );
    }

    private List<String> mergeNotations(List<String> fromHash, String preferred, String rawTableFolder) {
        Set<String> merged = new LinkedHashSet<>();
        if (fromHash != null) {
            merged.addAll(fromHash);
        }
        if (preferred != null && !preferred.isBlank()) {
            merged.add(preferred);
        }
        if (rawTableFolder != null && !rawTableFolder.isBlank()) {
            String folderNotation = registry.parseFolderName(rawTableFolder).notation();
            if (!folderNotation.isBlank()) {
                merged.add(folderNotation);
            }
        }
        return new ArrayList<>(merged);
    }

    private void closeConnections() {
        try {
            if (playLogService != null) {
                playLogService.close();
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to close play log service", e);
        }
        try {
            if (songDatabaseService != null) {
                songDatabaseService.close();
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to close song database service", e);
        }
        playLogService = null;
        songDatabaseService = null;
    }

    @Override
    public void close() {
        closeConnections();
        registry.clear();
    }

    private record ResolvedChart(
            String sha256,
            String md5,
            String title,
            String primaryNotation,
            String defaultPostNotation,
            List<String> availableNotations,
            String bmsLevel,
            PlayScore score,
            Integer clearId
    ) {
    }
}
