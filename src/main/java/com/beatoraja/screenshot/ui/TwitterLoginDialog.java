package com.beatoraja.screenshot.ui;

import com.beatoraja.screenshot.config.AppConfig;
import com.beatoraja.screenshot.service.ClixService;
import com.beatoraja.screenshot.util.AppIcons;
import com.beatoraja.screenshot.service.twitter.TwitterAuthService;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class TwitterLoginDialog extends JDialog {

    private final AppConfig config;
    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private final AtomicBoolean loginConfirmed = new AtomicBoolean(false);
    private final JLabel statusLabel = new JLabel("準備中...");
    private final JButton completeButton = new JButton("ログイン完了");
    private final JButton cancelButton = new JButton("キャンセル");
    private final CountDownLatch loginLatch = new CountDownLatch(1);
    private ClixService.AuthResult result = ClixService.AuthResult.failed("未実行");

    public TwitterLoginDialog(Window owner, AppConfig config) {
        super(owner, "Twitter ログイン", ModalityType.APPLICATION_MODAL);
        AppIcons.applyTo(this);
        this.config = config;
        buildUi();
        setSize(620, 220);
        setLocationRelativeTo(owner);
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(16, 16, 16, 16));

        statusLabel.setText("<html>Chrome / Edge を起動します...</html>");
        root.add(statusLabel, BorderLayout.CENTER);

        completeButton.setEnabled(false);
        completeButton.addActionListener(e -> confirmLogin());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.add(completeButton);
        cancelButton.addActionListener(e -> cancelLogin());
        actions.add(cancelButton);
        root.add(actions, BorderLayout.SOUTH);

        setContentPane(root);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void confirmLogin() {
        loginConfirmed.set(true);
        completeButton.setEnabled(false);
        statusLabel.setText("<html>開いた Chrome / Edge を<strong>閉じて</strong>ください。<br>閉じると Cookie 取得を開始します。</html>");
        loginLatch.countDown();
    }

    private void cancelLogin() {
        cancelled.set(true);
        loginLatch.countDown();
        dispose();
    }

    public ClixService.AuthResult showAndLogin() {
        new Thread(this::runLogin, "twitter-login").start();
        setVisible(true);
        return result;
    }

    private void runLogin() {
        TwitterAuthService authService = new TwitterAuthService(config);
        result = authService.loginInteractive(new TwitterAuthService.LoginProgressListener() {
            @Override
            public void onStatus(String status) {
                SwingUtilities.invokeLater(() -> statusLabel.setText("<html>" + escapeHtml(status) + "</html>"));
            }

            @Override
            public void onBrowserOpened() {
                SwingUtilities.invokeLater(() -> {
                    completeButton.setEnabled(true);
                    statusLabel.setText(
                            "<html>表示されたブラウザで X にログインし、完了したら<strong>「ログイン完了」</strong>を押してください。<br>"
                                    + "その後、開いたブラウザを閉じます。</html>");
                });
            }

            @Override
            public boolean awaitLoginComplete() throws InterruptedException {
                loginLatch.await();
                return loginConfirmed.get() && !cancelled.get();
            }

            @Override
            public boolean isCancelled() {
                return cancelled.get();
            }
        });

        SwingUtilities.invokeLater(() -> {
            if (result.success()) {
                dispose();
            } else if (!cancelled.get()) {
                statusLabel.setText("<html>" + escapeHtml(result.message()) + "</html>");
                completeButton.setEnabled(false);
            }
        });
    }

    private static String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
