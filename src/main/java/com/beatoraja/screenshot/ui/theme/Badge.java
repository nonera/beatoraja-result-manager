package com.beatoraja.screenshot.ui.theme;

import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/** Rounded "pill" rendering shared by the table renderers and the preview card. */
public final class Badge {

    public static final int HORIZONTAL_PADDING = 8;
    private static final int VERTICAL_PADDING = 3;

    private static final float FILL_ALPHA = 0.22f;
    private static final float BORDER_ALPHA = 0.45f;

    private Badge() {
    }

    public static int width(FontMetrics metrics, String text) {
        return metrics.stringWidth(text) + HORIZONTAL_PADDING * 2;
    }

    /** Pill height for the given font, so badges follow the configured UI font size. */
    public static int height(FontMetrics metrics) {
        return metrics.getHeight() + VERTICAL_PADDING * 2;
    }

    /**
     * Draws a pill of {@code text} tinted with {@code color} on top of {@code background}
     * and returns the width it occupied.
     */
    public static int paint(Graphics2D g2, int x, int y, int height, String text, Color color, Color background) {
        FontMetrics metrics = g2.getFontMetrics();
        int width = width(metrics, text);
        int pillHeight = Math.min(height(metrics), height);
        int pillY = y + (height - pillHeight) / 2;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(ResultPalette.tint(color, background, FILL_ALPHA));
        g2.fillRoundRect(x, pillY, width, pillHeight, pillHeight, pillHeight);
        g2.setColor(ResultPalette.tint(color, background, BORDER_ALPHA));
        g2.drawRoundRect(x, pillY, width - 1, pillHeight - 1, pillHeight, pillHeight);

        g2.setColor(readableText(color, background));
        int baseline = pillY + (pillHeight - metrics.getHeight()) / 2 + metrics.getAscent();
        g2.drawString(text, x + HORIZONTAL_PADDING, baseline);
        return width;
    }

    /** A standalone pill component for use inside ordinary Swing layouts. */
    public static JLabel label(String text, Color color) {
        return new BadgeLabel(text, color);
    }

    private static Color readableText(Color color, Color background) {
        float backgroundLuminance = luminance(background);
        Color candidate = color;
        // Pale lamp colors (PERFECT, MAX) wash out on light backgrounds; darken them instead.
        if (backgroundLuminance > 0.5f && luminance(candidate) > 0.62f) {
            candidate = candidate.darker().darker();
        } else if (backgroundLuminance <= 0.5f && luminance(candidate) < 0.35f) {
            candidate = candidate.brighter().brighter();
        }
        return candidate;
    }

    private static float luminance(Color color) {
        return (0.2126f * color.getRed() + 0.7152f * color.getGreen() + 0.0722f * color.getBlue()) / 255f;
    }

    private static final class BadgeLabel extends JLabel {
        private final Color badgeColor;

        private BadgeLabel(String text, Color badgeColor) {
            super(text);
            this.badgeColor = badgeColor;
            setOpaque(false);
            setFont(getFont().deriveFont(Font.BOLD, getFont().getSize2D() - 1f));
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics metrics = getFontMetrics(getFont());
            return new Dimension(width(metrics, getText()), height(metrics));
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setFont(getFont());
                Color background = getParent() == null ? UiTheme.surface() : getParent().getBackground();
                Badge.paint(g2, 0, 0, getHeight(), getText(), badgeColor, background);
            } finally {
                g2.dispose();
            }
        }
    }
}
