package com.beatoraja.screenshot;

import com.beatoraja.screenshot.config.AppConfig;
import com.beatoraja.screenshot.ui.FirstRunWizard;
import com.beatoraja.screenshot.ui.MainFrame;
import com.beatoraja.screenshot.util.AppVersion;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }

            AppConfig config = AppConfig.load();
            if (!config.isFirstRunCompleted() || config.getScreenshotDirectory().isBlank()) {
                showFirstRunWizard(config);
            } else {
                new MainFrame(config).setVisible(true);
            }
        });
    }

    private static void showFirstRunWizard(AppConfig config) {
        JFrame frame = new JFrame("beatoraja Screenshot Manager v" + AppVersion.get() + " - 初期設定");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(new FirstRunWizard(config, updatedConfig -> {
            frame.dispose();
            new MainFrame(updatedConfig).setVisible(true);
        }));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
