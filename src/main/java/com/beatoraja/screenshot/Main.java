package com.beatoraja.screenshot;

import com.beatoraja.screenshot.config.AppConfig;
import com.beatoraja.screenshot.service.update.AppUpdateService;
import com.beatoraja.screenshot.ui.FirstRunWizard;
import com.beatoraja.screenshot.ui.MainFrame;
import com.beatoraja.screenshot.ui.UpdateProgressDialog;
import com.beatoraja.screenshot.ui.theme.UiTheme;
import com.beatoraja.screenshot.util.AppIcons;
import com.beatoraja.screenshot.util.AppLogging;
import com.beatoraja.screenshot.util.AppPaths;
import com.beatoraja.screenshot.util.AppVersion;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

public class Main {

    private static final Logger LOG = AppLogging.get(Main.class);

    public static void main(String[] args) {
        AppLogging.initialize();
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) ->
                LOG.log(java.util.logging.Level.SEVERE,
                        "Uncaught exception in thread " + thread.getName(), throwable));

        LOG.info("app.dir system property: " + System.getProperty("app.dir")
                + " -> resolved exe directory: " + AppPaths.exeDir());

        AppConfig config = AppConfig.load();
        UiTheme.install(config.getUiTheme(), config.getUiFontFamily(), config.getUiFontSize());
        if (maybeApplyAutomaticUpdate(config, args)) {
            return;
        }

        launchApplication(config);
    }

    private static boolean maybeApplyAutomaticUpdate(AppConfig config, String[] args) {
        if (!AppUpdateService.shouldCheckForUpdates(config.isAutoUpdateEnabled(), args)) {
            return false;
        }

        CountDownLatch finished = new CountDownLatch(1);
        AtomicReference<AppUpdateService.UpdateOutcome> outcome =
                new AtomicReference<>(AppUpdateService.UpdateOutcome.NOT_APPLICABLE);
        AtomicReference<UpdateProgressDialog> dialogRef = new AtomicReference<>();

        SwingUtilities.invokeLater(() -> {
            UpdateProgressDialog dialog = new UpdateProgressDialog();
            dialogRef.set(dialog);

            Thread worker = new Thread(() -> {
                try {
                    AppUpdateService service = new AppUpdateService();
                    AppUpdateService.UpdateOutcome result = service.tryAutoUpdate(new AppUpdateService.ProgressListener() {
                        @Override
                        public void onStatus(String message) {
                            dialog.updateStatus(message);
                        }

                        @Override
                        public void onProgress(long downloadedBytes, long totalBytes) {
                            dialog.updateProgress(downloadedBytes, totalBytes);
                        }
                    });
                    outcome.set(result);
                    if (result == AppUpdateService.UpdateOutcome.FAILED) {
                        Thread.sleep(2500);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    SwingUtilities.invokeLater(() -> {
                        dialog.dispose();
                        finished.countDown();
                    });
                }
            }, "app-update");
            worker.setDaemon(false);
            worker.start();

            // Modal, so this blocks the EDT until the worker above disposes it - it must
            // start the worker first, or the dialog can never be dismissed to unblock this.
            dialog.setVisible(true);
        });

        try {
            finished.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            UpdateProgressDialog dialog = dialogRef.get();
            if (dialog != null) {
                dialog.dispose();
            }
            return false;
        }

        if (outcome.get() == AppUpdateService.UpdateOutcome.RESTARTING) {
            LOG.info("Exiting current process so the updater can replace files and restart.");
            System.exit(0);
            return true;
        }
        return false;
    }

    private static void launchApplication(AppConfig config) {
        SwingUtilities.invokeLater(() -> {
            LOG.info("Starting beatoraja Screenshot Manager v" + AppVersion.get());
            if (!config.isFirstRunCompleted() || config.getScreenshotDirectory().isBlank()) {
                showFirstRunWizard(config);
            } else {
                new MainFrame(config).setVisible(true);
            }
        });
    }

    private static void showFirstRunWizard(AppConfig config) {
        JFrame frame = new JFrame("beatoraja Screenshot Manager v" + AppVersion.get() + " - 初期設定");
        AppIcons.applyTo(frame);
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
