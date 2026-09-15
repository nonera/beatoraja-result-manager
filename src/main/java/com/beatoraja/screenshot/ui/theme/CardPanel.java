package com.beatoraja.screenshot.ui.theme;

import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;

/** Panel with a rounded, slightly raised surface used to group related content. */
public class CardPanel extends JPanel {

    private static final int ARC = 10;

    public CardPanel(LayoutManager layout) {
        super(layout);
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(UiTheme.surface());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARC, ARC);
            g2.setColor(UiTheme.subtle());
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);
        } finally {
            g2.dispose();
        }
        super.paintComponent(g);
    }

    @Override
    public java.awt.Color getBackground() {
        return UiTheme.surface();
    }
}
