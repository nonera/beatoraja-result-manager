package com.beatoraja.screenshot.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.beatoraja.screenshot.player.PlayerKind;
import com.beatoraja.screenshot.util.AppLogging;
import com.beatoraja.screenshot.util.AppPaths;
import com.beatoraja.screenshot.util.AtomicFiles;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AppConfig {

    public static final int MIN_UI_FONT_SIZE = 10;
    public static final int MAX_UI_FONT_SIZE = 24;
    public static final int DEFAULT_UI_FONT_SIZE = 13;

    private static final Logger LOG = AppLogging.get(AppConfig.class);
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.INDENT_OUTPUT);

    private boolean firstRunCompleted;
    private String screenshotDirectory = "";
    private String beatorajaDirectory = "";
    private String playerName = "";
    /** {@link PlayerKind#configKey()}; blank in configs saved before the setting existed. */
    private String player = "";
    private List<DiscordWebhookEntry> discordWebhooks = new ArrayList<>();
    private boolean discordAutoPostEnabled;
    private int discordAutoPostBatchSize = 4;
    private String discordAutoPostWebhookName = "";
    private Boolean autoUpdateEnabled;
    private boolean deleteScreenshotsOnExit;
    private String uiTheme = "";
    private String uiFontFamily = "";
    private int uiFontSize;
    private List<String> tablePriorityOrder = new ArrayList<>();
    private List<TableNotationRule> tableNotationRules = new ArrayList<>();
    private PostTextFormat postTextFormat = PostTextFormat.defaults();
    private String twitterBrowser = "";
    private String twitterChromeProfile = "";
    /**
     * X tokens as configs before the cookie map stored them. Only ever read from such a file:
     * load moves them into {@link #twitterCookies} and they are never written back.
     */
    @JsonProperty(value = "twitterAuthToken", access = JsonProperty.Access.WRITE_ONLY)
    private String legacyTwitterAuthToken = "";
    @JsonProperty(value = "twitterCt0", access = JsonProperty.Access.WRITE_ONLY)
    private String legacyTwitterCt0 = "";
    private Map<String, String> twitterCookies = new LinkedHashMap<>();
    private Map<String, String> clearLampLabels = new LinkedHashMap<>();
    /** Overrides of {@link ClearLampLabels#DEFAULT_SHORT_LABELS}, keyed like {@link #clearLampLabels}. */
    private Map<String, String> clearLampShortLabels = new LinkedHashMap<>();
    /** Overrides of {@link ClearLampLabels#DEFAULT_GROUPED_HIDDEN}: lamp → listed in the grouped post text. */
    private Map<String, Boolean> clearLampGroupedVisibility = new LinkedHashMap<>();

    public static AppConfig load() {
        Path configFile = AppPaths.configFile();
        if (!Files.exists(configFile)) {
            return new AppConfig();
        }
        try {
            return parse(Files.readAllBytes(configFile));
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Failed to load config from " + configFile.toAbsolutePath(), e);
            return new AppConfig();
        }
    }

    /** Writes the config through a temporary file, so a crash mid-save cannot corrupt it. */
    public void save() throws IOException {
        AtomicFiles.write(AppPaths.configFile(), toJson());
    }

    public boolean isFirstRunCompleted() {
        return firstRunCompleted;
    }

    public void setFirstRunCompleted(boolean firstRunCompleted) {
        this.firstRunCompleted = firstRunCompleted;
    }

    public String getScreenshotDirectory() {
        return screenshotDirectory;
    }

    public void setScreenshotDirectory(String screenshotDirectory) {
        this.screenshotDirectory = screenshotDirectory == null ? "" : screenshotDirectory;
    }

    public String getBeatorajaDirectory() {
        return beatorajaDirectory;
    }

    public void setBeatorajaDirectory(String beatorajaDirectory) {
        this.beatorajaDirectory = beatorajaDirectory == null ? "" : beatorajaDirectory;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName == null ? "" : playerName;
    }

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player == null ? "" : player;
    }

    /**
     * The player chosen in the settings. Configs saved before that choice existed fall back to
     * what the player-data folder looks like, and to beatoraja when that says nothing.
     */
    public PlayerKind playerKind() {
        return PlayerKind.fromConfigKey(player)
                .or(() -> PlayerKind.detect(resolveBeatorajaDirectory()))
                .orElse(PlayerKind.BEATORAJA);
    }

    public void setPlayerKind(PlayerKind kind) {
        setPlayer(kind == null ? "" : kind.configKey());
    }

    public java.nio.file.Path resolveBeatorajaDirectory() {
        if (beatorajaDirectory != null && !beatorajaDirectory.isBlank()) {
            return java.nio.file.Path.of(beatorajaDirectory);
        }
        if (screenshotDirectory != null && !screenshotDirectory.isBlank()) {
            return com.beatoraja.screenshot.player.PlayerKind.playerFolderOf(
                    java.nio.file.Path.of(screenshotDirectory)
            );
        }
        return null;
    }

    public List<DiscordWebhookEntry> getDiscordWebhooks() {
        return discordWebhooks;
    }

    public void setDiscordWebhooks(List<DiscordWebhookEntry> discordWebhooks) {
        this.discordWebhooks = discordWebhooks == null ? new ArrayList<>() : discordWebhooks;
    }

    public boolean isDiscordAutoPostEnabled() {
        return discordAutoPostEnabled;
    }

    public void setDiscordAutoPostEnabled(boolean discordAutoPostEnabled) {
        this.discordAutoPostEnabled = discordAutoPostEnabled;
    }

    public int getDiscordAutoPostBatchSize() {
        return discordAutoPostBatchSize;
    }

    public void setDiscordAutoPostBatchSize(int discordAutoPostBatchSize) {
        if (discordAutoPostBatchSize < 1) {
            this.discordAutoPostBatchSize = 1;
        } else if (discordAutoPostBatchSize > 10) {
            this.discordAutoPostBatchSize = 10;
        } else {
            this.discordAutoPostBatchSize = discordAutoPostBatchSize;
        }
    }

    public String getDiscordAutoPostWebhookName() {
        return discordAutoPostWebhookName;
    }

    public void setDiscordAutoPostWebhookName(String discordAutoPostWebhookName) {
        this.discordAutoPostWebhookName = discordAutoPostWebhookName == null ? "" : discordAutoPostWebhookName;
    }

    public DiscordWebhookEntry resolveDiscordAutoPostWebhook() {
        if (discordAutoPostWebhookName != null && !discordAutoPostWebhookName.isBlank()) {
            for (DiscordWebhookEntry entry : discordWebhooks) {
                if (discordAutoPostWebhookName.equals(entry.getName())
                        && entry.getUrl() != null && !entry.getUrl().isBlank()) {
                    return entry;
                }
            }
        }
        for (DiscordWebhookEntry entry : discordWebhooks) {
            if (entry.getUrl() != null && !entry.getUrl().isBlank()) {
                return entry;
            }
        }
        return null;
    }

    public boolean isAutoUpdateEnabled() {
        return autoUpdateEnabled == null || autoUpdateEnabled;
    }

    public void setAutoUpdateEnabled(boolean autoUpdateEnabled) {
        this.autoUpdateEnabled = autoUpdateEnabled;
    }

    public boolean isDeleteScreenshotsOnExit() {
        return deleteScreenshotsOnExit;
    }

    public void setDeleteScreenshotsOnExit(boolean deleteScreenshotsOnExit) {
        this.deleteScreenshotsOnExit = deleteScreenshotsOnExit;
    }

    /** Returns {@code "dark"} or {@code "light"}; dark is the default. */
    public String getUiTheme() {
        return "light".equalsIgnoreCase(uiTheme) ? "light" : "dark";
    }

    public void setUiTheme(String uiTheme) {
        this.uiTheme = "light".equalsIgnoreCase(uiTheme) ? "light" : "dark";
    }

    /** Empty means "pick the first available Japanese-capable UI font". */
    public String getUiFontFamily() {
        return uiFontFamily == null ? "" : uiFontFamily;
    }

    public void setUiFontFamily(String uiFontFamily) {
        this.uiFontFamily = uiFontFamily == null ? "" : uiFontFamily.trim();
    }

    public int getUiFontSize() {
        return uiFontSize < MIN_UI_FONT_SIZE || uiFontSize > MAX_UI_FONT_SIZE
                ? DEFAULT_UI_FONT_SIZE
                : uiFontSize;
    }

    public void setUiFontSize(int uiFontSize) {
        this.uiFontSize = Math.min(MAX_UI_FONT_SIZE, Math.max(MIN_UI_FONT_SIZE, uiFontSize));
    }

    public List<String> getTablePriorityOrder() {
        return tablePriorityOrder;
    }

    public void setTablePriorityOrder(List<String> tablePriorityOrder) {
        this.tablePriorityOrder = tablePriorityOrder == null ? new ArrayList<>() : tablePriorityOrder;
    }

    public PostTextFormat getPostTextFormat() {
        return postTextFormat;
    }

    public void setPostTextFormat(PostTextFormat postTextFormat) {
        this.postTextFormat = postTextFormat == null ? PostTextFormat.defaults() : postTextFormat.normalized();
    }

    public List<TableNotationRule> getTableNotationRules() {
        return tableNotationRules;
    }

    public void setTableNotationRules(List<TableNotationRule> tableNotationRules) {
        this.tableNotationRules = tableNotationRules == null ? new ArrayList<>() : tableNotationRules;
    }

    public String getTwitterBrowser() {
        return twitterBrowser;
    }

    public void setTwitterBrowser(String twitterBrowser) {
        this.twitterBrowser = twitterBrowser == null ? "" : twitterBrowser;
    }

    public String getTwitterChromeProfile() {
        return twitterChromeProfile;
    }

    public void setTwitterChromeProfile(String twitterChromeProfile) {
        this.twitterChromeProfile = twitterChromeProfile == null ? "" : twitterChromeProfile;
    }

    public Map<String, String> getTwitterCookies() {
        return twitterCookies;
    }

    public void setTwitterCookies(Map<String, String> twitterCookies) {
        this.twitterCookies = twitterCookies == null ? new LinkedHashMap<>() : new LinkedHashMap<>(twitterCookies);
    }

    /** The saved X {@code auth_token} cookie, or "" when not logged in. */
    public String twitterAuthToken() {
        return twitterCookies.getOrDefault("auth_token", "");
    }

    /** The saved X {@code ct0} cookie, or "" when not logged in. */
    public String twitterCt0() {
        return twitterCookies.getOrDefault("ct0", "");
    }

    /** Custom clear lamp display names ("俗称"), keyed by the canonical, upper-cased clear type text. */
    public Map<String, String> getClearLampLabels() {
        return clearLampLabels;
    }

    public void setClearLampLabels(Map<String, String> clearLampLabels) {
        this.clearLampLabels = clearLampLabels == null ? new LinkedHashMap<>() : new LinkedHashMap<>(clearLampLabels);
    }

    /** Short names for the lamp-grouped post text that differ from the defaults, keyed like {@link #getClearLampLabels()}. */
    public Map<String, String> getClearLampShortLabels() {
        return clearLampShortLabels;
    }

    public void setClearLampShortLabels(Map<String, String> clearLampShortLabels) {
        this.clearLampShortLabels = clearLampShortLabels == null
                ? new LinkedHashMap<>()
                : new LinkedHashMap<>(clearLampShortLabels);
    }

    public Map<String, Boolean> getClearLampGroupedVisibility() {
        return clearLampGroupedVisibility;
    }

    public void setClearLampGroupedVisibility(Map<String, Boolean> clearLampGroupedVisibility) {
        this.clearLampGroupedVisibility = clearLampGroupedVisibility == null
                ? new LinkedHashMap<>()
                : new LinkedHashMap<>(clearLampGroupedVisibility);
    }

    public String resolveClearLampLabel(String clearType) {
        return ClearLampLabels.resolve(clearType, clearLampLabels);
    }

    public boolean hasManualTwitterAuth() {
        return !twitterAuthToken().isBlank() && !twitterCt0().isBlank();
    }

    public void clearTwitterAuth() {
        twitterCookies.clear();
    }

    /** Reads a config file's contents, moving old-format X tokens into the cookie map. */
    static AppConfig parse(byte[] json) throws IOException {
        AppConfig config = MAPPER.readValue(json, AppConfig.class);
        config.migrateLegacyTwitterTokens();
        return config;
    }

    /** Bytes as {@link #save()} writes them. */
    byte[] toJson() throws IOException {
        return MAPPER.writeValueAsBytes(this);
    }

    private void migrateLegacyTwitterTokens() {
        if (twitterCookies == null) {
            twitterCookies = new LinkedHashMap<>();
        }
        if (twitterCookies.isEmpty()) {
            if (legacyTwitterAuthToken != null && !legacyTwitterAuthToken.isBlank()) {
                twitterCookies.put("auth_token", legacyTwitterAuthToken);
            }
            if (legacyTwitterCt0 != null && !legacyTwitterCt0.isBlank()) {
                twitterCookies.put("ct0", legacyTwitterCt0);
            }
        }
        legacyTwitterAuthToken = "";
        legacyTwitterCt0 = "";
    }

    public static class DiscordWebhookEntry {
        private String name = "";
        private String url = "";

        public DiscordWebhookEntry() {
        }

        public DiscordWebhookEntry(String name, String url) {
            this.name = name;
            this.url = url;
        }

        /** An independent copy, so edits to it do not reach the original. */
        public DiscordWebhookEntry copy() {
            return new DiscordWebhookEntry(name, url);
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        @Override
        public String toString() {
            return name == null || name.isBlank() ? url : name;
        }
    }

    /**
     * Per-difficulty-table notation customization. {@link #symbolOverrides} normally
     * holds one table-wide entry under the empty string key; legacy per-symbol keys
     * are still read for backward compatibility. Also controls whether this table's
     * notation should always be included in the default post notation even when a
     * higher-priority table already provides one for the same song.
     */
    public static class TableNotationRule {
        /** Map key for a table-wide symbol override (replaces the .bmt tag prefix). */
        public static final String TABLE_WIDE_OVERRIDE_KEY =
                com.beatoraja.screenshot.table.DifficultyTableRegistry.TableNotationRule.TABLE_WIDE_OVERRIDE_KEY;
        private String tableTag = "";
        private boolean alwaysInclude;
        private Map<String, String> symbolOverrides = new LinkedHashMap<>();

        public TableNotationRule() {
        }

        public String getTableTag() {
            return tableTag;
        }

        public void setTableTag(String tableTag) {
            this.tableTag = tableTag == null ? "" : tableTag;
        }

        public boolean isAlwaysInclude() {
            return alwaysInclude;
        }

        public void setAlwaysInclude(boolean alwaysInclude) {
            this.alwaysInclude = alwaysInclude;
        }

        public Map<String, String> getSymbolOverrides() {
            return symbolOverrides;
        }

        public void setSymbolOverrides(Map<String, String> symbolOverrides) {
            this.symbolOverrides = symbolOverrides == null ? new LinkedHashMap<>() : symbolOverrides;
        }
    }
}
