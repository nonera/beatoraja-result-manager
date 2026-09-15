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
    private String badgeText = "";
    private Color badgeColor = Color.GRAY;

    public BadgeCellRenderer(Function<String, Color> colorResolver, boolean centered) {
        this.colorResolver = colorResolver;
        this.centered = centered;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
        String text = value == null ? "" : String.valueOf(value).trim();
        badgeText = "-".equals(text) ? "" : text;
        badgeColor = colorResolver.apply(badgeText);
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
