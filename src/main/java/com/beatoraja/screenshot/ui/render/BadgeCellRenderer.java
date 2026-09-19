package com.beatoraja.screenshot.ui.render;

import com.beatoraja.screenshot.ui.theme.Badge;
import com.beatoraja.screenshot.ui.theme.UiTheme;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.function.Function;

/** Draws a single tinted pill for values such as clear lamps, ranks and table symbols. */
public class BadgeCellRenderer extends DefaultTableCellRenderer {

    private final Function<String, Color> colorResolver;
    private final boolean centered;
    private final Function<String, String> labelResolver;
    private String badgeText = "";
    private Color badgeColor = Color.GRAY;

    public BadgeCellRenderer(Function<String, Color> colorResolver, boolean centered) {
        this(colorResolver, centered, Function.identity());
    }

    /**
     * @param labelResolver maps the cell's raw value to the text actually drawn in the badge
     *         (e.g. a user-configured display name), while {@code colorResolver} still sees
     *         the raw value so custom labels don't break color lookups keyed by the original text
     */
    public BadgeCellRenderer(Function<String, Color> colorResolver, boolean centered,
            Function<String, String> labelResolver) {
        this.colorResolver = colorResolver;
        this.centered = centered;
        this.labelResolver = labelResolver;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
        String text = value == null ? "" : String.valueOf(value).trim();
        String rawText = "-".equals(text) ? "" : text;
        badgeColor = colorResolver.apply(rawText);
        badgeText = rawText.isEmpty() ? "" : labelResolver.apply(rawText);
        setFont(table.getFont().deriveFont(Font.BOLD, table.getFont().getSize2D() - 1f));
        setToolTipText(badgeText.isEmpty() ? null : badgeText);
        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setFont(getFont());
            if (badgeText.isEmpty()) {
                paintPlaceholder(g2);
                return;
            }
            FontMetrics metrics = g2.getFontMetrics();
            int width = Badge.width(metrics, badgeText);
            int x = centered ? Math.max(4, (getWidth() - width) / 2) : 6;
            Badge.paint(g2, x, 0, getHeight(), badgeText, badgeColor, getBackground());
        } finally {
            g2.dispose();
        }
    }

    private void paintPlaceholder(Graphics2D g2) {
        FontMetrics metrics = g2.getFontMetrics();
        g2.setColor(UiTheme.mutedAgainst(getForeground(), getBackground()));
        int x = centered ? (getWidth() - metrics.stringWidth("-")) / 2 : 6;
        g2.drawString("-", x, (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent());
    }
}
