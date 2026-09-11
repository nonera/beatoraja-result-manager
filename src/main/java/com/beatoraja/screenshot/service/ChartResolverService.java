package com.beatoraja.screenshot.service;

import com.beatoraja.screenshot.config.AppConfig;
import com.beatoraja.screenshot.model.ScreenshotEntry;
import com.beatoraja.screenshot.player.BeatorajaPaths;
import com.beatoraja.screenshot.player.ClearTypeMapper;
import com.beatoraja.screenshot.player.PlayerPlayLogService;
import com.beatoraja.screenshot.player.SongDatabaseService;
import com.beatoraja.screenshot.table.DifficultyTableRegistry;
import com.beatoraja.screenshot.table.TableLevelParser;
import com.beatoraja.screenshot.table.TableLookupService;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ChartResolverService implements AutoCloseable {

    private final DifficultyTableRegistry registry = new DifficultyTableRegistry();
    private PlayerPlayLogService playLogService;
    private SongDatabaseService songDatabaseService;
    private Path beatorajaDirectory;

    public void reload(AppConfig config) throws IOException {
        closeConnections();

        beatorajaDirectory = config.resolveBeatorajaDirectory();
        if (beatorajaDirectory == null || !beatorajaDirectory.toFile().isDirectory()) {
            registry.clear();
            return;
        }

        BeatorajaPaths paths = BeatorajaPaths.resolve(beatorajaDirectory, config.getPlayerName());
        registry.loadFromTableDirectory(paths.tableDirectory());
        registry.setTablePriority(config.getTablePriorityOrder());
        registry.setNotationRules(toRegistryRules(config.getTableNotationRules()));
        try {
            playLogService = new PlayerPlayLogService(paths.scoreDataLogDb());
            songDatabaseService = new SongDatabaseService(paths.songDatabase());
        } catch (java.sql.SQLException e) {
            throw new IOException("プレイヤーデータベースを開けません: " + e.getMessage(), e);
        }
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

    public TableLookupService.EnrichedScreenshot enrich(ScreenshotEntry entry) {
        ResolvedChart resolved = resolveFromPlayerData(entry);
        if (resolved != null) {
            return buildEnriched(entry, resolved);
        }
        return enrichFromFilename(entry);
    }

    private ResolvedChart resolveFromPlayerData(ScreenshotEntry entry) {
        if (playLogService == null || !playLogService.isAvailable()) {
            return null;
        }
        if (entry.getCapturedAt() == null) {
            return null;
        }
        if ("Play".equals(entry.getStateLabel()) || "Music Select".equals(entry.getStateLabel())
                || "Decide".equals(entry.getStateLabel()) || "Config".equals(entry.getStateLabel())) {
            return null;
        }

        try {
            Integer clearId = ClearTypeMapper.toClearId(entry.getClearType());
            List<PlayerPlayLogService.PlayLogEntry> candidates = playLogService.findNear(entry.getCapturedAt(), clearId);
            if (candidates.isEmpty()) {
                return null;
            }

            PlayerPlayLogService.PlayLogEntry best = candidates.get(0);
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
                    resolution.candidates(), song != null ? String.valueOf(song.level()) : "");
        } catch (Exception ignored) {
            return null;
        }
    }

    private TableLookupService.EnrichedScreenshot enrichFromFilename(ScreenshotEntry entry) {
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
                entry.isBmsLevelOnly() ? entry.getLevel() : ""
        ));
    }

    private TableLookupService.EnrichedScreenshot buildEnriched(ScreenshotEntry entry, ResolvedChart resolved) {
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

        return new TableLookupService.EnrichedScreenshot(
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
                !resolved.sha256().isBlank()
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
        } catch (Exception ignored) {
        }
        try {
            if (songDatabaseService != null) {
                songDatabaseService.close();
            }
        } catch (Exception ignored) {
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
            String bmsLevel
    ) {
    }
}
