package com.beatoraja.screenshot.ui.render;

import com.beatoraja.screenshot.ui.theme.UiTheme;

import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.nio.file.Path;

/** Paints the cached screenshot thumbnail, or a placeholder while it is still loading. */
public class ThumbnailCellRenderer extends DefaultTableCellRenderer {

    private final ThumbnailCache cache;
    private Icon icon;

    public ThumbnailCellRenderer(ThumbnailCache cache) {
        this.cache = cache;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
        icon = value instanceof Path path ? cache.get(path) : null;
        setToolTipText(value instanceof Path path ? path.getFileName().toString() : null);
        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int x = Math.max(0, (getWidth() - cache.width()) / 2);
            int y = Math.max(0, (getHeight() - cache.height()) / 2);
            if (icon == null) {
                g2.setColor(UiTheme.subtle());
                g2.fillRoundRect(x, y, cache.width(), cache.height(), 6, 6);
            } else {
                icon.paintIcon(this, g2, x, y);
            }
        } finally {
            g2.dispose();
        }
    }
}
