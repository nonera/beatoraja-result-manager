package com.beatoraja.screenshot.table;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolves the shared abbreviation prefix among notations in the same table/symbol
 * group, so symbol overrides replace only the stable part and keep level suffixes
 * (including decimals) intact.
 */
public final class NotationPrefixResolver {

    private NotationPrefixResolver() {
    }

    public static String resolveReplaceablePrefix(List<String> notations, List<String> knownTags, String fallbackSymbol) {
        if (notations == null || notations.isEmpty()) {
            return safe(fallbackSymbol);
        }

        List<String> symbolParts = new ArrayList<>();
        for (String notation : notations) {
            if (notation == null || notation.isBlank()) {
                continue;
            }
            String symbol = TableLevelParser.parse(notation, knownTags).symbol();
            if (!symbol.isBlank()) {
                symbolParts.add(symbol);
            }
        }

        if (!symbolParts.isEmpty()) {
            String symbolPrefix = longestCommonPrefix(symbolParts);
            if (!symbolPrefix.isBlank()) {
                return symbolPrefix;
            }
            if (!safe(fallbackSymbol).isBlank()) {
                return safe(fallbackSymbol);
            }
        }

        if (notations.size() == 1) {
            return safe(fallbackSymbol);
        }

        String rawPrefix = longestCommonPrefix(notations);
        return trimPrefixWithoutSuffix(rawPrefix, notations);
    }

    public static String applySymbolOverride(String notation, String prefix, String override) {
        if (notation == null || notation.isBlank() || override == null || override.isBlank()) {
            return notation == null ? "" : notation;
        }
        if (prefix == null || prefix.isBlank() || !notation.startsWith(prefix)) {
            return notation;
        }
        return override + notation.substring(prefix.length());
    }

    private static String trimPrefixWithoutSuffix(String prefix, List<String> notations) {
        String trimmed = prefix;
        while (!trimmed.isBlank()) {
            boolean allHaveSuffix = true;
            for (String notation : notations) {
                if (notation == null || notation.length() <= trimmed.length()) {
                    allHaveSuffix = false;
                    break;
                }
            }
            if (allHaveSuffix) {
                return trimmed;
            }
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return "";
    }

    static String longestCommonPrefix(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        String prefix = values.get(0);
        for (int i = 1; i < values.size(); i++) {
            String value = values.get(i);
            if (value == null) {
                return "";
            }
            while (!value.startsWith(prefix)) {
                if (prefix.isEmpty()) {
                    return "";
                }
                prefix = prefix.substring(0, prefix.length() - 1);
            }
        }
        return prefix;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
