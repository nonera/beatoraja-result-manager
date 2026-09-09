package com.beatoraja.screenshot.ui;

import com.beatoraja.screenshot.config.AppConfig;
import com.beatoraja.screenshot.service.ClixService;
import com.beatoraja.screenshot.service.twitter.TwitterAuthService;

import com.beatoraja.screenshot.player.BeatorajaPaths;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SettingsDialog extends JDialog {

    public interface SaveListener {
        void onSaved(AppConfig config);
    }

    private final AppConfig config;
    private final SaveListener listener;
    private final JTextField screenshotDirField = new JTextField(32);
    private final JTextField beatorajaDirField = new JTextField(32);
    private final JComboBox<String> playerNameCombo = new JComboBox<>();
    private final JLabel twitterStatusLabel = new JLabel("未確認");
    private final WebhookTableModel webhookTableModel = new WebhookTableModel();

    public SettingsDialog(Frame owner, AppConfig config, SaveListener listener) {
        super(owner, "設定", true);
        this.config = config;
        this.listener = listener;
        buildUi();
        loadValues();
        refreshTwitterStatusQuietly();
        setSize(760, 620);
        setLocationRelativeTo(owner);
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(new JLabel("スクショフォルダ"), gbc);
        gbc.gridx = 1;
        JPanel screenshotPanel = new JPanel(new BorderLayout(6, 0));
        screenshotPanel.add(screenshotDirField, BorderLayout.CENTER);
        JButton browseScreenshot = new JButton("参照...");
        browseScreenshot.addActionListener(e -> chooseScreenshotDir());
        screenshotPanel.add(browseScreenshot, BorderLayout.EAST);
        form.add(screenshotPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
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
        form.add(new JLabel("プレイヤー名"), gbc);
        gbc.gridx = 1;
        JPanel playerPanel = new JPanel(new BorderLayout(6, 0));
        playerNameCombo.setEditable(true);
        playerPanel.add(playerNameCombo, BorderLayout.CENTER);
        JButton refreshPlayers = new JButton("更新");
        refreshPlayers.addActionListener(e -> refreshPlayerNames());
        playerPanel.add(refreshPlayers, BorderLayout.EAST);
        form.add(playerPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        form.add(new JLabel("空欄の場合は config_sys.json の playername を使用（scoredatalog.db 照合用）"), gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        form.add(new JLabel("Twitter 認証"), gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;
        form.add(new JLabel("状態"), gbc);
        gbc.gridx = 1;
        JPanel twitterPanel = new JPanel(new BorderLayout(6, 0));
        twitterPanel.add(twitterStatusLabel, BorderLayout.CENTER);
        JButton checkTwitter = new JButton("再確認");
        checkTwitter.addActionListener(e -> checkTwitterAuth());
        twitterPanel.add(checkTwitter, BorderLayout.EAST);
        form.add(twitterPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        JPanel twitterActions = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        JButton loginTwitter = new JButton("Twitter にログイン");
        loginTwitter.addActionListener(e -> loginTwitter());
        twitterActions.add(loginTwitter);
        JButton logoutTwitter = new JButton("ログアウト");
        logoutTwitter.addActionListener(e -> logoutTwitter());
        twitterActions.add(logoutTwitter);
        form.add(twitterActions, gbc);

        gbc.gridy++;
        form.add(new JLabel("Chrome / Edge が起動します。X へログイン後「ログイン完了」を押してください"), gbc);

        root.add(form, BorderLayout.NORTH);

        JTable webhookTable = new JTable(webhookTableModel);
        webhookTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        webhookTable.getColumnModel().getColumn(1).setPreferredWidth(480);
        JScrollPane webhookScroll = new JScrollPane(webhookTable);
        webhookScroll.setBorder(javax.swing.BorderFactory.createTitledBorder("Discord Webhook"));
        root.add(webhookScroll, BorderLayout.CENTER);

        JPanel webhookActions = new JPanel();
        JButton addWebhook = new JButton("追加");
        addWebhook.addActionListener(e -> webhookTableModel.addRow());
        JButton removeWebhook = new JButton("削除");
        removeWebhook.addActionListener(e -> {
            int row = webhookTable.getSelectedRow();
            if (row >= 0) {
                webhookTableModel.removeRow(row);
            }
        });
        webhookActions.add(addWebhook);
        webhookActions.add(removeWebhook);
        root.add(webhookActions, BorderLayout.EAST);

        JPanel bottom = new JPanel(new BorderLayout());
        JButton save = new JButton("保存");
        save.addActionListener(e -> saveSettings());
        bottom.add(save, BorderLayout.EAST);
        root.add(bottom, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void loadValues() {
        screenshotDirField.setText(config.getScreenshotDirectory());
        beatorajaDirField.setText(config.getBeatorajaDirectory());
        webhookTableModel.setRows(new ArrayList<>(config.getDiscordWebhooks()));
        twitterStatusLabel.setText(config.hasManualTwitterAuth() ? "ログイン情報あり（未確認）" : "未ログイン");
        refreshPlayerNames();
        playerNameCombo.getEditor().setItem(config.getPlayerName());
    }

    private void refreshPlayerNames() {
        String current = playerNameCombo.getEditor() != null
                ? String.valueOf(playerNameCombo.getEditor().getItem())
                : config.getPlayerName();

        playerNameCombo.removeAllItems();
        playerNameCombo.addItem("");

        String beatorajaDirText = beatorajaDirField.getText().trim();
        if (!beatorajaDirText.isBlank()) {
            Path beatorajaDir = Path.of(beatorajaDirText);
            for (String name : BeatorajaPaths.listPlayerNames(beatorajaDir)) {
                playerNameCombo.addItem(name);
            }
        }

        playerNameCombo.getEditor().setItem(current == null ? "" : current);
    }

    private void refreshTwitterStatusQuietly() {
        if (!config.hasManualTwitterAuth()) {
            return;
        }
        new Thread(() -> {
            ClixService.AuthResult result = new TwitterAuthService(config).verifyStoredSession();
            javax.swing.SwingUtilities.invokeLater(() -> twitterStatusLabel.setText(result.message()));
        }, "twitter-auth-status").start();
    }

    private void chooseBeatorajaDir() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            beatorajaDirField.setText(chooser.getSelectedFile().getAbsolutePath());
            refreshPlayerNames();
        }
    }

    private void chooseScreenshotDir() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            screenshotDirField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void loginTwitter() {
        TwitterLoginDialog dialog = new TwitterLoginDialog(this, config);
        ClixService.AuthResult result = dialog.showAndLogin();
        twitterStatusLabel.setText(result.message());
    }

    private void logoutTwitter() {
        new TwitterAuthService(config).logout();
        twitterStatusLabel.setText("未ログイン");
    }

    private void checkTwitterAuth() {
        twitterStatusLabel.setText("確認中...");
        new Thread(() -> {
            TwitterAuthService authService = new TwitterAuthService(config);
            ClixService.AuthResult result = authService.verifyStoredSession();
            if (!result.success() && authService.refreshSilently()) {
                result = authService.verifyStoredSession();
            }
            ClixService.AuthResult finalResult = result;
            javax.swing.SwingUtilities.invokeLater(() ->
                    twitterStatusLabel.setText(finalResult.message()));
        }, "twitter-auth-check-settings").start();
    }

    private void saveSettings() {
        File screenshotDir = new File(screenshotDirField.getText().trim());
        if (!screenshotDir.isDirectory()) {
            JOptionPane.showMessageDialog(this, "スクショフォルダを選択してください。", "入力エラー", JOptionPane.WARNING_MESSAGE);
            return;
        }

        config.setScreenshotDirectory(screenshotDir.getAbsolutePath());
        config.setBeatorajaDirectory(beatorajaDirField.getText().trim());
        String playerName = String.valueOf(playerNameCombo.getEditor().getItem()).trim();
        config.setPlayerName(playerName);
        config.setDiscordWebhooks(webhookTableModel.getRows());
        config.setFirstRunCompleted(true);

        try {
            config.save();
            listener.onSaved(config);
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "保存に失敗しました: " + e.getMessage(), "エラー", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static class WebhookTableModel extends AbstractTableModel {
        private final String[] columns = {"名前", "Webhook URL"};
        private List<AppConfig.DiscordWebhookEntry> rows = new ArrayList<>();

        public void setRows(List<AppConfig.DiscordWebhookEntry> rows) {
            this.rows = new ArrayList<>(rows);
            fireTableDataChanged();
        }

        public List<AppConfig.DiscordWebhookEntry> getRows() {
            return new ArrayList<>(rows);
        }

        public void addRow() {
            rows.add(new AppConfig.DiscordWebhookEntry("", ""));
            fireTableRowsInserted(rows.size() - 1, rows.size() - 1);
        }

        public void removeRow(int index) {
            rows.remove(index);
            fireTableRowsDeleted(index, index);
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            AppConfig.DiscordWebhookEntry row = rows.get(rowIndex);
            return columnIndex == 0 ? row.getName() : row.getUrl();
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            AppConfig.DiscordWebhookEntry row = rows.get(rowIndex);
            if (columnIndex == 0) {
                row.setName(String.valueOf(aValue));
            } else {
                row.setUrl(String.valueOf(aValue));
            }
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }
}
