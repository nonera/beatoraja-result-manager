package com.beatoraja.screenshot.ui;

import com.beatoraja.screenshot.table.DifficultyTableRegistry;

import javax.swing.JButton;
import java.awt.Component;

/** Small pieces several settings dialogs share, so their wording and behavior stay the same. */
final class DialogParts {

    private DialogParts() {
    }

    /** A button for a vertical button column (centered in it) that runs {@code action}. */
    static JButton button(String label, Runnable action) {
        JButton button = new JButton(label);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.addActionListener(e -> action.run());
        return button;
    }

    /**
     * "難易度表: N 件", plus how many .bmt files were merged into them when that differs, which
     * hints at stale duplicate files in the table folder.
     */
    static String tableCountText(int knownTableCount, int loadedFileCount) {
        return "難易度表: " + knownTableCount + " 件"
                + (loadedFileCount != knownTableCount
                        ? "（読み込んだ.bmtファイル " + loadedFileCount + " 件を同名/同タグでまとめた結果）"
                        : "");
    }

    /** The table's name, or its key when it has none. */
    static String tableLabel(DifficultyTableRegistry.TableInfo info) {
        return info.name().isBlank() ? info.tag() : info.name();
    }
}
