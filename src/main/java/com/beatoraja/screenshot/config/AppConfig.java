package com.beatoraja.screenshot.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.beatoraja.screenshot.util.AppPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AppConfig {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.INDENT_OUTPUT);

    private boolean firstRunCompleted;
    private String screenshotDirectory = "";
    private String beatorajaDirectory = "";
    private String playerName = "";
    private List<DiscordWebhookEntry> discordWebhooks = new ArrayList<>();
    private List<String> tablePriorityOrder = new ArrayList<>();
    private List<TableNotationRule> tableNotationRules = new ArrayList<>();
    private String twitterBrowser = "";
    private String twitterChromeProfile = "";
    private String twitterAuthToken = "";
    private String twitterCt0 = "";

    public static AppConfig load() {
        Path configFile = AppPaths.configFile();
        if (!Files.exists(configFile)) {
            return new AppConfig();
        }
        try {
            return MAPPER.readValue(configFile.toFile(), AppConfig.class);
        } catch (IOException e) {
            return new AppConfig();
        }
    }

    public void save() throws IOException {
        Files.createDirectories(AppPaths.appDataDir());
        MAPPER.writeValue(AppPaths.configFile().toFile(), this);
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

    public java.nio.file.Path resolveBeatorajaDirectory() {
        if (beatorajaDirectory != null && !beatorajaDirectory.isBlank()) {
            return java.nio.file.Path.of(beatorajaDirectory);
        }
        if (screenshotDirectory != null && !screenshotDirectory.isBlank()) {
            return com.beatoraja.screenshot.table.TableLookupService.deriveBeatorajaDirectory(
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

    public List<String> getTablePriorityOrder() {
        return tablePriorityOrder;
    }

    public void setTablePriorityOrder(List<String> tablePriorityOrder) {
        this.tablePriorityOrder = tablePriorityOrder == null ? new ArrayList<>() : tablePriorityOrder;
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

    public String getTwitterAuthToken() {
        return twitterAuthToken;
    }

    public void setTwitterAuthToken(String twitterAuthToken) {
        this.twitterAuthToken = twitterAuthToken == null ? "" : twitterAuthToken;
    }

    public String getTwitterCt0() {
        return twitterCt0;
    }

    public void setTwitterCt0(String twitterCt0) {
        this.twitterCt0 = twitterCt0 == null ? "" : twitterCt0;
    }

    public boolean hasManualTwitterAuth() {
        return !twitterAuthToken.isBlank() && !twitterCt0.isBlank();
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
     * Per-difficulty-table notation customization: symbol replacements keyed by
     * the raw symbol they replace ("sl を STELLA に変える" — most tables only need
     * one entry, but some switch symbol partway through their levels, e.g. "A1"
     * .. "A9" then "AA1" ..) plus whether this table's notation should always be
     * included in the default post notation even when a higher-priority table
     * already provides one for the same song.
     */
    public static class TableNotationRule {
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
