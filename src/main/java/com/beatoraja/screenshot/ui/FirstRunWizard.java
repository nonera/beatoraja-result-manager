package com.beatoraja.screenshot.ui;

import com.beatoraja.screenshot.config.AppConfig;
import com.beatoraja.screenshot.service.ClixService;
import com.beatoraja.screenshot.service.twitter.TwitterAuthService;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FirstRunWizard extends JPanel {

    public interface CompletionListener {
        void onCompleted(AppConfig config);
    }

    private final JTextField beatorajaDirField = new JTextField(32);
    private final JTextField webhookNameField = new JTextField("default", 20);
    private final JTextField webhookUrlField = new JTextField(32);
    private final JLabel twitterStatusLabel = new JLabel("未ログイン");
    private final AppConfig config;
    private final CompletionListener listener;

    public FirstRunWizard(AppConfig config, CompletionListener listener) {
        this.config = config;
        this.listener = listener;
        buildUi();
    }

    private void buildUi() {
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        form.add(new JLabel("beatoraja スクショ管理の初期設定"), gbc);

        gbc.gridy++;
        gbc.gridwidth = 1;
        form.add(new JLabel("beatoraja フォルダ"), gbc);

        gbc.gridx = 1;
        JPanel beatorajaPanel = new JPanel(new BorderLayout(6, 0));
        beatorajaPanel.add(beatorajaDirField, BorderLayout.CENTER);
        JButton browseBeatoraja = new JButton("参照...");
        browseBeatoraja.addActionListener(e -> chooseBeatorajaDir());
        beatorajaPanel.add(browseBeatoraja, BorderLayout.EAST);
        form.add(beatorajaPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        form.add(new JLabel("screenshot/ フォルダを自動検出します"), gbc);

        gbc.gridy++;
        form.add(new JLabel("Discord Webhook（任意）"), gbc);

        gbc.gridy++;
        gbc.gridwidth = 1;
        form.add(new JLabel("名前"), gbc);
        gbc.gridx = 1;
        form.add(webhookNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        form.add(new JLabel("URL"), gbc);
        gbc.gridx = 1;
        form.add(webhookUrlField, gbc);

        gbc.gridy++;
        gbc.gridwidth = 2;
        form.add(new JLabel("Twitter 認証（任意）"), gbc);

        gbc.gridy++;
        gbc.gridwidth = 1;
        form.add(new JLabel("状態"), gbc);
        gbc.gridx = 1;
        JPanel twitterPanel = new JPanel(new BorderLayout(6, 0));
        twitterPanel.add(twitterStatusLabel, BorderLayout.CENTER);
        JButton loginTwitter = new JButton("Twitter にログイン");
        loginTwitter.addActionListener(e -> loginTwitter());
        twitterPanel.add(loginTwitter, BorderLayout.EAST);
        form.add(twitterPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        form.add(new JLabel("Chrome / Edge が起動します。X へログイン後「ログイン完了」を押してください"), gbc);

        add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new BorderLayout());
        actions.add(new JLabel("Twitter は後から設定画面でもログインできます。"), BorderLayout.CENTER);

        JButton complete = new JButton("設定を保存して開始");
        complete.addActionListener(e -> completeSetup());
        actions.add(complete, BorderLayout.EAST);
        add(actions, BorderLayout.SOUTH);
    }

    private void chooseBeatorajaDir() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            beatorajaDirField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void loginTwitter() {
        java.awt.Window owner = javax.swing.SwingUtilities.getWindowAncestor(this);
        TwitterLoginDialog dialog = new TwitterLoginDialog(owner, config);
        ClixService.AuthResult result = dialog.showAndLogin();
        twitterStatusLabel.setText(result.success() ? "ログイン済み" : result.message());
    }

    private void completeSetup() {
        File beatorajaDir = new File(beatorajaDirField.getText().trim());
        if (!beatorajaDir.isDirectory()) {
            JOptionPane.showMessageDialog(this, "beatoraja フォルダを選択してください。", "入力エラー", JOptionPane.WARNING_MESSAGE);
            return;
        }

        File screenshotDir = new File(beatorajaDir, "screenshot");
        if (!screenshotDir.isDirectory()) {
            int answer = JOptionPane.showConfirmDialog(
                    this,
                    "screenshot フォルダが見つかりません。作成しますか？",
                    "確認",
                    JOptionPane.YES_NO_OPTION
            );
            if (answer != JOptionPane.YES_OPTION || !screenshotDir.mkdirs()) {
                JOptionPane.showMessageDialog(this, "screenshot フォルダを用意してください。", "エラー", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        config.setScreenshotDirectory(screenshotDir.getAbsolutePath());
        config.setBeatorajaDirectory(beatorajaDir.getAbsolutePath());

        List<AppConfig.DiscordWebhookEntry> webhooks = new ArrayList<>();
        String webhookUrl = webhookUrlField.getText().trim();
        if (!webhookUrl.isBlank()) {
            webhooks.add(new AppConfig.DiscordWebhookEntry(
                    webhookNameField.getText().trim(),
                    webhookUrl
            ));
        }
        config.setDiscordWebhooks(webhooks);
        config.setFirstRunCompleted(true);

        try {
            config.save();
            listener.onCompleted(config);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "設定の保存に失敗しました: " + e.getMessage(), "エラー", JOptionPane.ERROR_MESSAGE);
        }
    }
}
