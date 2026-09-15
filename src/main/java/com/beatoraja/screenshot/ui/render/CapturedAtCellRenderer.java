package com.beatoraja.screenshot.ui.render;

import com.beatoraja.screenshot.ui.theme.UiTheme;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Shows the capture date with the clock time de-emphasized so the date column scans quickly. */
public class CapturedAtCellRenderer extends DefaultTableCellRenderer {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    private String datePart = "";
    private String timePart = "";

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
        if (value instanceof LocalDateTime dateTime) {
            datePart = dateTime.format(DATE);
            timePart = dateTime.format(TIME);
        } else {
            datePart = "";
            timePart = "";
        }
        setToolTipText(datePart.isEmpty() ? null : datePart + " " + timePart);
        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (datePart.isEmpty()) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setFont(getFont());
            FontMetrics metrics = g2.getFontMetrics();
            int baseline = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
            g2.setColor(getForeground());
            g2.drawString(datePart, 6, baseline);
            g2.setColor(UiTheme.mutedAgainst(getForeground(), getBackground()));
            g2.drawString(timePart, 6 + metrics.stringWidth(datePart + " "), baseline);
        } finally {
            g2.dispose();
        }
    }
}
