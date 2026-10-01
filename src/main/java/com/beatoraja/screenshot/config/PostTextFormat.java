package com.beatoraja.screenshot.config;

import com.beatoraja.screenshot.player.PlayScore;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Which pieces make up an auto-generated post text, in what order, with what
 * surrounding text, plus display names for DJ RANKs. Saved inside {@link AppConfig}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PostTextFormat {

    public static final String NOTATION = "notation";
    public static final String TITLE = "title";
    public static final String CLEAR = "clear";
    public static final String RANK = "rank";
    public static final String BP = "bp";
    public static final String RATE = "rate";
    public static final String EX_SCORE = "exscore";
    public static final String RANK_DIFF = "rankdiff";

    /** Every item key in default order, with the name shown in the settings dialog. */
    public static final Map<String, String> ITEM_DISPLAY_NAMES;
    /** Item keys in default order. */
    public static final List<String> ITEM_KEYS;

    static {
        Map<String, String> names = new LinkedHashMap<>();
        names.put(NOTATION, "難易度表記");
        names.put(TITLE, "タイトル");
        names.put(CLEAR, "クリアランプ");
        names.put(RANK, "DJ RANK");
        names.put(BP, "BP (BAD+POOR)");
        names.put(RATE, "スコアレート");
        names.put(EX_SCORE, "EXスコア");
        names.put(RANK_DIFF, "区切りからの差分");
        ITEM_DISPLAY_NAMES = Map.copyOf(names);
        ITEM_KEYS = List.copyOf(names.keySet());
    }

    private List<Item> items = new ArrayList<>();
    private Map<String, String> rankLabels = new LinkedHashMap<>();

    public PostTextFormat() {
    }

    /** The format that reproduces the original fixed post text: notation, title, clear lamp, rank. */
    public static PostTextFormat defaults() {
        PostTextFormat format = new PostTextFormat();
        format.items.add(new Item(NOTATION, true, "", ""));
        format.items.add(new Item(TITLE, true, "", ""));
        format.items.add(new Item(CLEAR, true, "", ""));
        format.items.add(new Item(RANK, true, "", ""));
        format.items.add(new Item(BP, false, "BP", ""));
        format.items.add(new Item(RATE, false, "", "%"));
        format.items.add(new Item(EX_SCORE, false, "EX", ""));
        format.items.add(new Item(RANK_DIFF, false, "", ""));
        return format;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items == null ? new ArrayList<>() : new ArrayList<>(items);
    }

    public Map<String, String> getRankLabels() {
        return rankLabels;
    }

    public void setRankLabels(Map<String, String> rankLabels) {
        this.rankLabels = rankLabels == null ? new LinkedHashMap<>() : new LinkedHashMap<>(rankLabels);
    }

    /** The display name for a DJ RANK key (MAX, AAA, ... F), or the key itself when unset. */
    public String rankLabel(String rankKey) {
        if (rankKey == null || rankKey.isBlank()) {
            return "";
        }
        String label = rankLabels.get(rankKey.trim());
        return label == null || label.isBlank() ? rankKey.trim() : label;
    }

    /**
     * Drops unknown/duplicate item keys and appends any missing ones (disabled, with their
     * default affixes), so configs saved by older versions still list every item.
     */
    @JsonIgnore
    public PostTextFormat normalized() {
        PostTextFormat result = new PostTextFormat();
        Set<String> seen = new LinkedHashSet<>();
        for (Item item : items) {
            if (item != null && ITEM_KEYS.contains(item.getKey()) && seen.add(item.getKey())) {
                result.items.add(item.copy());
            }
        }
        for (Item item : defaults().items) {
            if (seen.add(item.getKey())) {
                item.setEnabled(false);
                result.items.add(item);
            }
        }
        for (String key : PlayScore.RANK_KEYS) {
            String label = rankLabels.get(key);
            if (label != null && !label.isBlank()) {
                result.rankLabels.put(key, label);
            }
        }
        return result;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        private String key = "";
        private boolean enabled;
        private String prefix = "";
        private String suffix = "";

        public Item() {
        }

        public Item(String key, boolean enabled, String prefix, String suffix) {
            setKey(key);
            setEnabled(enabled);
            setPrefix(prefix);
            setSuffix(suffix);
        }

        public Item copy() {
            return new Item(key, enabled, prefix, suffix);
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key == null ? "" : key;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix == null ? "" : prefix;
        }

        public String getSuffix() {
            return suffix;
        }

        public void setSuffix(String suffix) {
            this.suffix = suffix == null ? "" : suffix;
        }
    }
}
