package com.beatoraja.screenshot.ui.render;

/** Capture state plus the per-service posted flags, rendered as a row of chips. */
public record StateCell(String stateLabel, boolean twitterPosted, boolean discordPosted)
        implements Comparable<StateCell> {

    public String state() {
        return stateLabel == null || stateLabel.isBlank() ? "" : stateLabel.trim();
    }

    public String displayText() {
        StringBuilder builder = new StringBuilder(state());
        if (twitterPosted) {
            append(builder, "X");
        }
        if (discordPosted) {
            append(builder, "Discord");
        }
        return builder.isEmpty() ? "-" : builder.toString();
    }

    @Override
    public int compareTo(StateCell other) {
        return displayText().compareToIgnoreCase(other.displayText());
    }

    private static void append(StringBuilder builder, String label) {
        if (!builder.isEmpty()) {
            builder.append(' ');
        }
        builder.append('[').append(label).append(']');
    }
}
