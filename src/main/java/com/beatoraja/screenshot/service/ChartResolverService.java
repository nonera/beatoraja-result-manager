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
        try {
            playLogService = new PlayerPlayLogService(paths.scoreDataLogDb());
            songDatabaseService = new SongDatabaseService(paths.songDatabase());
        } catch (java.sql.SQLException e) {
            throw new IOException("プレイヤーデータベースを開けません: " + e.getMessage(), e);
        }
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
            String title = song != null && !song.fullTitle().isBlank() ? song.fullTitle() : entry.getTitle();
            List<String> notations = registry.findNotationsByHash(sha256, md5);
            if (notations.isEmpty() && !title.isBlank()) {
                notations = registry.findNotationsForTitle(title);
            }

            String preferred = choosePreferredNotation(entry.getRawTableFolder(), notations);
            return new ResolvedChart(sha256, md5, title, preferred, notations, song != null ? String.valueOf(song.level()) : "");
        } catch (Exception ignored) {
            return null;
        }
    }

    private String choosePreferredNotation(String rawTableFolder, List<String> notations) {
        if (!notations.isEmpty()) {
            return notations.get(0);
        }
        if (rawTableFolder != null && !rawTableFolder.isBlank()) {
            return registry.parseFolderName(rawTableFolder).notation();
        }
        return "";
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

        List<String> available = new ArrayList<>(registry.findNotationsForTitle(entry.getTitle()));
        String defaultNotation = parsed.notation();
        if (!defaultNotation.isBlank() && !available.contains(defaultNotation)) {
            available.add(0, defaultNotation);
        }
        if (defaultNotation.isBlank() && !available.isEmpty()) {
            defaultNotation = available.get(0);
        }

        return buildEnriched(entry, new ResolvedChart(
                "",
                "",
                entry.getTitle(),
                defaultNotation,
                available,
                entry.isBmsLevelOnly() ? entry.getLevel() : ""
        ));
    }

    private TableLookupService.EnrichedScreenshot buildEnriched(ScreenshotEntry entry, ResolvedChart resolved) {
        TableLevelParser.ParsedTableLevel parsed = resolved.preferredNotation().isBlank()
                ? TableLevelParser.ParsedTableLevel.empty()
                : registry.parseNotation(resolved.preferredNotation());

        String tableSymbol = parsed.hasTableSymbol() ? parsed.symbol() : "";
        String tableLevelNum = parsed.hasTableSymbol() ? parsed.level() : "";
        String bmsLevel = entry.isBmsLevelOnly() ? entry.getLevel() : resolved.bmsLevel();

        if (tableSymbol.isBlank() && tableLevelNum.isBlank() && !bmsLevel.isBlank()) {
            tableLevelNum = "";
        }

        List<String> available = mergeNotations(resolved.availableNotations(), resolved.preferredNotation(), entry.getRawTableFolder());

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
                resolved.preferredNotation(),
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
            String preferredNotation,
            List<String> availableNotations,
            String bmsLevel
    ) {
    }
}
