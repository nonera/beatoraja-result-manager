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
    private Map<String, String> twitterCookies = new LinkedHashMap<>();

    public static AppConfig load() {
        Path configFile = AppPaths.configFile();
        if (!Files.exists(configFile)) {
            return new AppConfig();
        }
        try {
            AppConfig config = MAPPER.readValue(configFile.toFile(), AppConfig.class);
            config.normalizeTwitterCookies();
            return config;
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

    public Map<String, String> getTwitterCookies() {
        return twitterCookies;
    }

    public void setTwitterCookies(Map<String, String> twitterCookies) {
        this.twitterCookies = twitterCookies == null ? new LinkedHashMap<>() : new LinkedHashMap<>(twitterCookies);
        this.twitterAuthToken = "";
        this.twitterCt0 = "";
    }

    public String getTwitterAuthToken() {
        String fromMap = twitterCookies.get("auth_token");
        if (fromMap != null && !fromMap.isBlank()) {
            return fromMap;
        }
        return twitterAuthToken;
    }

    public void setTwitterAuthToken(String twitterAuthToken) {
        String value = twitterAuthToken == null ? "" : twitterAuthToken;
        if (value.isBlank()) {
            twitterCookies.remove("auth_token");
        } else {
            twitterCookies.put("auth_token", value);
        }
        this.twitterAuthToken = "";
    }

    public String getTwitterCt0() {
        String fromMap = twitterCookies.get("ct0");
        if (fromMap != null && !fromMap.isBlank()) {
            return fromMap;
        }
        return twitterCt0;
    }

    public void setTwitterCt0(String twitterCt0) {
        String value = twitterCt0 == null ? "" : twitterCt0;
        if (value.isBlank()) {
            twitterCookies.remove("ct0");
        } else {
            twitterCookies.put("ct0", value);
        }
        this.twitterCt0 = "";
    }

    public boolean hasManualTwitterAuth() {
        return !getTwitterAuthToken().isBlank() && !getTwitterCt0().isBlank();
    }

    public void clearTwitterAuth() {
        twitterCookies.clear();
        twitterAuthToken = "";
        twitterCt0 = "";
    }

    private void normalizeTwitterCookies() {
        if (twitterCookies == null) {
            twitterCookies = new LinkedHashMap<>();
        }
        if (twitterCookies.isEmpty()) {
            if (twitterAuthToken != null && !twitterAuthToken.isBlank()) {
                twitterCookies.put("auth_token", twitterAuthToken);
            }
            if (twitterCt0 != null && !twitterCt0.isBlank()) {
                twitterCookies.put("ct0", twitterCt0);
            }
        }
        twitterAuthToken = "";
        twitterCt0 = "";
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
     * Per-difficulty-table notation customization. {@link #symbolOverrides} normally
     * holds one table-wide entry under the empty string key; legacy per-symbol keys
     * are still read for backward compatibility. Also controls whether this table's
     * notation should always be included in the default post notation even when a
     * higher-priority table already provides one for the same song.
     */
    public static class TableNotationRule {
        /** Map key for a table-wide symbol override (replaces the .bmt tag prefix). */
        public static final String TABLE_WIDE_OVERRIDE_KEY = "";
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
