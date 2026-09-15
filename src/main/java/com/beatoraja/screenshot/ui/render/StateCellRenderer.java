package com.beatoraja.screenshot.ui.render;

import com.beatoraja.screenshot.ui.theme.Badge;
import com.beatoraja.screenshot.ui.theme.ResultPalette;
import com.beatoraja.screenshot.ui.theme.UiTheme;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

/** Renders the capture state and the Twitter / Discord posted flags as adjacent chips. */
public class StateCellRenderer extends DefaultTableCellRenderer {

    private static final int GAP = 4;

    private StateCell cell = new StateCell("", false, false);

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
        cell = value instanceof StateCell stateCell ? stateCell : new StateCell("", false, false);
        setFont(table.getFont().deriveFont(Font.BOLD, table.getFont().getSize2D() - 1f));
        setToolTipText(cell.displayText());
        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setFont(getFont());
            int x = 6;
            String state = cell.state();
            if (state.isEmpty() && !cell.twitterPosted() && !cell.discordPosted()) {
                FontMetrics metrics = g2.getFontMetrics();
                g2.setColor(UiTheme.mutedAgainst(getForeground(), getBackground()));
                g2.drawString("-", x, (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent());
                return;
            }
            if (!state.isEmpty()) {
                x += Badge.paint(g2, x, 0, getHeight(), state, ResultPalette.stateColor(state), getBackground()) + GAP;
            }
            if (cell.twitterPosted()) {
                x += Badge.paint(g2, x, 0, getHeight(), "X", UiTheme.twitterAccent(), getBackground()) + GAP;
            }
            if (cell.discordPosted()) {
                Badge.paint(g2, x, 0, getHeight(), "Discord", UiTheme.discordAccent(), getBackground());
            }
        } finally {
            g2.dispose();
        }
    }
}
