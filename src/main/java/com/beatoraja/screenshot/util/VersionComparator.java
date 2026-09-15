package com.beatoraja.screenshot.util;

import java.util.ArrayList;
import java.util.List;

public final class VersionComparator {

    private VersionComparator() {
    }

    public static String normalize(String version) {
        if (version == null) {
            return "";
        }
        String trimmed = version.trim();
        if (trimmed.startsWith("v") || trimmed.startsWith("V")) {
            return trimmed.substring(1);
        }
        return trimmed;
    }

    public static int compare(String left, String right) {
        List<Integer> leftParts = parseParts(normalize(left));
        List<Integer> rightParts = parseParts(normalize(right));
        int max = Math.max(leftParts.size(), rightParts.size());
        for (int i = 0; i < max; i++) {
            int l = i < leftParts.size() ? leftParts.get(i) : 0;
            int r = i < rightParts.size() ? rightParts.get(i) : 0;
            if (l != r) {
                return Integer.compare(l, r);
            }
        }
        return 0;
    }

    public static boolean isNewer(String candidate, String current) {
        return compare(candidate, current) > 0;
    }

    private static List<Integer> parseParts(String version) {
        List<Integer> parts = new ArrayList<>();
        if (version.isBlank()) {
            return parts;
        }
        String numeric = version.split("-", 2)[0];
        for (String segment : numeric.split("\\.")) {
            if (segment.isBlank()) {
                continue;
            }
            StringBuilder digits = new StringBuilder();
            for (int i = 0; i < segment.length(); i++) {
                char ch = segment.charAt(i);
                if (Character.isDigit(ch)) {
                    digits.append(ch);
                } else {
                    break;
                }
            }
            if (!digits.isEmpty()) {
                parts.add(Integer.parseInt(digits.toString()));
            }
        }
        return parts;
    }
}
