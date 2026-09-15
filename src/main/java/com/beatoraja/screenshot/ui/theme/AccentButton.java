package com.beatoraja.screenshot.ui.theme;

import javax.swing.JButton;
import java.awt.Color;

/** Filled button in a brand color that falls back to the default styling when disabled. */
public class AccentButton extends JButton {

    private final Color accent;

    public AccentButton(String text, Color accent) {
        super(text);
        this.accent = accent;
        putClientProperty("JButton.buttonType", "roundRect");
        setFocusPainted(false);
        applyColors();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        applyColors();
    }

    private void applyColors() {
        if (accent == null) {
            return;
        }
        setBackground(isEnabled() ? accent : null);
        setForeground(isEnabled() ? Color.WHITE : null);
    }
}
