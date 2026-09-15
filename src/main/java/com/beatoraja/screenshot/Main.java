package com.beatoraja.screenshot;

import com.beatoraja.screenshot.config.AppConfig;
import com.beatoraja.screenshot.ui.FirstRunWizard;
import com.beatoraja.screenshot.ui.MainFrame;
import com.beatoraja.screenshot.util.AppLogging;
import com.beatoraja.screenshot.util.AppVersion;

import javax.swing.JFrame;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

    private static final Logger LOG = AppLogging.get(Main.class);

    public static void main(String[] args) {
        AppLogging.initialize();
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) ->
                LOG.log(java.util.logging.Level.SEVERE,
                        "Uncaught exception in thread " + thread.getName(), throwable));

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                LOG.log(java.util.logging.Level.FINE, "Failed to set system look and feel", e);
            }

            LOG.info("Starting beatoraja Screenshot Manager v" + AppVersion.get());
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
