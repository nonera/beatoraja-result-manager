package com.beatoraja.screenshot.ui;

import com.beatoraja.screenshot.config.AppConfig;
import com.beatoraja.screenshot.config.ClearLampLabels;
import com.beatoraja.screenshot.service.ClixService;
import com.beatoraja.screenshot.service.twitter.TwitterAuthService;
import com.beatoraja.screenshot.player.BeatorajaPaths;
import com.beatoraja.screenshot.util.AppIcons;
import com.beatoraja.screenshot.util.AppLogging;
import com.beatoraja.screenshot.util.AppPaths;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Desktop;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SettingsDialog extends JDialog {

    private static final Logger LOG = AppLogging.get(SettingsDialog.class);

    private static final String THEME_DARK = "ダーク";
    private static final String THEME_LIGHT = "ライト";
    private static final String FONT_AUTO = "自動";

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
    private final ClearLampLabelTableModel clearLampLabelTableModel = new ClearLampLabelTableModel();
    private final JCheckBox discordAutoPostEnabledBox = new JCheckBox("有効");
    private final JSpinner discordAutoPostBatchSpinner = new JSpinner(
            new SpinnerNumberModel(4, 1, 10, 1));
    private final JComboBox<String> discordAutoPostWebhookCombo = new JComboBox<>();
    private final JCheckBox autoUpdateEnabledBox = new JCheckBox("起動時に GitHub Releases から自動更新する");
    private final JCheckBox deleteScreenshotsOnExitBox =
            new JCheckBox("終了時にスクショフォルダ内の画像を削除する");
    private final JComboBox<String> themeCombo = new JComboBox<>(new String[]{THEME_DARK, THEME_LIGHT});
    private final JComboBox<String> fontFamilyCombo = new JComboBox<>();
    private final JSpinner fontSizeSpinner = new JSpinner(new SpinnerNumberModel(
            AppConfig.DEFAULT_UI_FONT_SIZE, AppConfig.MIN_UI_FONT_SIZE, AppConfig.MAX_UI_FONT_SIZE, 1));

    public SettingsDialog(Frame owner, AppConfig config, SaveListener listener) {
        super(owner, "設定", true);
        AppIcons.applyTo(this);
        this.config = config;
        this.listener = listener;
        buildUi();
        loadValues();
        refreshTwitterStatusQuietly();
        setSize(780, 840);
        setMinimumSize(new java.awt.Dimension(640, 480));
        setResizable(true);
        setLocationRelativeTo(owner);
    }

    /** Makes a BoxLayout child grow to the scroll pane's full width. */
    private static JPanel stretch(JPanel panel) {
        panel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
        return panel;
    }

    private void buildUi() {
        JPanel stack = new JPanel();
        stack.setLayout(new javax.swing.BoxLayout(stack, javax.swing.BoxLayout.Y_AXIS));
        stack.setBorder(new EmptyBorder(12, 12, 12, 12));
        stack.add(fullWidth(buildFolderSection()));
        stack.add(javax.swing.Box.createVerticalStrut(8));
        stack.add(fullWidth(buildTwitterSection()));
        stack.add(javax.swing.Box.createVerticalStrut(8));
        stack.add(fullWidth(buildWebhookSection()));
        stack.add(javax.swing.Box.createVerticalStrut(8));
        stack.add(fullWidth(buildClearLampLabelSection()));
        stack.add(javax.swing.Box.createVerticalStrut(8));
        stack.add(fullWidth(buildDiscordAutoSection()));
        stack.add(javax.swing.Box.createVerticalStrut(8));
        stack.add(fullWidth(buildAppUpdateSection()));
        stack.add(javax.swing.Box.createVerticalStrut(8));
        stack.add(fullWidth(buildAppearanceSection()));

        // NORTH keeps the preferred height while stretching children to the viewport width.
        JPanel content = new JPanel(new BorderLayout());
        content.add(stack, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBorder(new EmptyBorder(8, 12, 12, 12));
        JPanel bottomActions = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));
        JButton openLogs = new JButton("ログフォルダを開く");
        openLogs.addActionListener(e -> openLogFolder());
        bottomActions.add(openLogs);
        JButton save = new JButton("保存");
        save.addActionListener(e -> saveSettings());
        bottomActions.add(save);
        bottom.add(bottomActions, BorderLayout.EAST);

        JPanel root = new JPanel(new BorderLayout());
        root.add(scrollPane, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private static JPanel fullWidth(JPanel panel) {
        panel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        return panel;
    }

    private JPanel buildFolderSection() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
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
        gbc.gridwidth = 2;
        form.add(deleteScreenshotsOnExitBox, gbc);
        gbc.gridy++;
        form.add(new JLabel("アプリ終了時に beatoraja の screenshot フォルダ内 PNG を削除します"), gbc);

        gbc.gridwidth = 1;
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
        return form;
    }

    private JPanel buildTwitterSection() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        gbc.gridx = 0;
        gbc.gridy = 0;
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
        return form;
    }

    private JPanel buildWebhookSection() {
        JTable webhookTable = new JTable(webhookTableModel);
        webhookTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        webhookTable.getColumnModel().getColumn(1).setPreferredWidth(480);
        JScrollPane webhookScroll = new JScrollPane(webhookTable);
        webhookScroll.setBorder(javax.swing.BorderFactory.createTitledBorder("Discord Webhook"));
        webhookScroll.setPreferredSize(new java.awt.Dimension(520, 140));
        webhookScroll.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);

        JPanel webhookActions = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        webhookActions.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
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

        JPanel section = new JPanel(new BorderLayout(0, 4));
        section.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        section.add(webhookScroll, BorderLayout.CENTER);
        section.add(webhookActions, BorderLayout.SOUTH);
        return section;
    }

    private JPanel buildClearLampLabelSection() {
        JTable table = new JTable(clearLampLabelTableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(200);
        table.getColumnModel().getColumn(1).setPreferredWidth(280);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(javax.swing.BorderFactory.createTitledBorder("クリアランプ表示名（俗称）"));
        scroll.setPreferredSize(new java.awt.Dimension(520, 260));
        scroll.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);

        JLabel hint = new JLabel("投稿文や一覧に表示するクリア名を俗称などに変更できます（空欄でデフォルト表示に戻ります）");
        hint.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);

        JButton resetAll = new JButton("すべてデフォルトに戻す");
        resetAll.addActionListener(e -> clearLampLabelTableModel.setOverrides(Map.of()));
        JPanel actions = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        actions.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        actions.add(resetAll);

        JPanel section = new JPanel(new BorderLayout(0, 4));
        section.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        section.add(hint, BorderLayout.NORTH);
        section.add(scroll, BorderLayout.CENTER);
        section.add(actions, BorderLayout.SOUTH);
        return section;
    }

    private JPanel buildDiscordAutoSection() {
        JPanel discordAutoPanel = new JPanel(new GridBagLayout());
        discordAutoPanel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        discordAutoPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Discord 自動投稿"));
        GridBagConstraints autoGbc = new GridBagConstraints();
        autoGbc.insets = new Insets(6, 6, 6, 6);
        autoGbc.fill = GridBagConstraints.HORIZONTAL;
        autoGbc.weightx = 1.0;

        autoGbc.gridx = 0;
        autoGbc.gridy = 0;
        autoGbc.weightx = 0;
        discordAutoPanel.add(new JLabel("自動投稿"), autoGbc);
        autoGbc.gridx = 1;
        autoGbc.weightx = 1.0;
        discordAutoPanel.add(discordAutoPostEnabledBox, autoGbc);

        autoGbc.gridx = 0;
        autoGbc.gridy++;
        autoGbc.weightx = 0;
        discordAutoPanel.add(new JLabel("投稿枚数"), autoGbc);
        autoGbc.gridx = 1;
        autoGbc.weightx = 1.0;
        discordAutoPanel.add(discordAutoPostBatchSpinner, autoGbc);

        autoGbc.gridx = 0;
        autoGbc.gridy++;
        autoGbc.weightx = 0;
        discordAutoPanel.add(new JLabel("送信先 Webhook"), autoGbc);
        autoGbc.gridx = 1;
        autoGbc.weightx = 1.0;
        discordAutoPanel.add(discordAutoPostWebhookCombo, autoGbc);

        autoGbc.gridx = 0;
        autoGbc.gridy++;
        autoGbc.gridwidth = 2;
        discordAutoPanel.add(new JLabel("リザルト（Result）の未投稿スクショが設定枚数に達したら自動送信します（1〜10枚）"), autoGbc);
        return discordAutoPanel;
    }

    private JPanel buildAppUpdateSection() {
        JPanel appUpdatePanel = new JPanel(new GridBagLayout());
        appUpdatePanel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        appUpdatePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("アプリ更新"));
        GridBagConstraints updateGbc = new GridBagConstraints();
        updateGbc.insets = new Insets(6, 6, 6, 6);
        updateGbc.gridx = 0;
        updateGbc.gridy = 0;
        updateGbc.gridwidth = 2;
        updateGbc.anchor = GridBagConstraints.WEST;
        appUpdatePanel.add(autoUpdateEnabledBox, updateGbc);
        updateGbc.gridy++;
        appUpdatePanel.add(new JLabel("新しい zip をダウンロードして上書き後、自動的に再起動します"), updateGbc);
        return appUpdatePanel;
    }

    private JPanel buildAppearanceSection() {
        fontFamilyCombo.removeAllItems();
        fontFamilyCombo.addItem(FONT_AUTO);
        for (String family : com.beatoraja.screenshot.ui.theme.UiTheme.japaneseCapableFontFamilies()) {
            fontFamilyCombo.addItem(family);
        }
        fontFamilyCombo.setPrototypeDisplayValue("Yu Gothic UI");

        JPanel appearancePanel = new JPanel(new GridBagLayout());
        appearancePanel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        appearancePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("外観"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        appearancePanel.add(new JLabel("テーマ"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        appearancePanel.add(themeCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0;
        appearancePanel.add(new JLabel("フォント"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        appearancePanel.add(fontFamilyCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0;
        appearancePanel.add(new JLabel("サイズ"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        appearancePanel.add(fontSizeSpinner, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        appearancePanel.add(new JLabel("保存すると画面を開き直して反映します"), gbc);
        return appearancePanel;
    }

    private void loadValues() {
        screenshotDirField.setText(config.getScreenshotDirectory());
        beatorajaDirField.setText(config.getBeatorajaDirectory());
        webhookTableModel.setRows(new ArrayList<>(config.getDiscordWebhooks()));
        clearLampLabelTableModel.setOverrides(config.getClearLampLabels());
        discordAutoPostEnabledBox.setSelected(config.isDiscordAutoPostEnabled());
        discordAutoPostBatchSpinner.setValue(config.getDiscordAutoPostBatchSize());
        reloadDiscordAutoPostWebhookChoices(config.getDiscordAutoPostWebhookName());
        autoUpdateEnabledBox.setSelected(config.isAutoUpdateEnabled());
        deleteScreenshotsOnExitBox.setSelected(config.isDeleteScreenshotsOnExit());
        themeCombo.setSelectedItem("light".equals(config.getUiTheme()) ? THEME_LIGHT : THEME_DARK);
        String fontFamily = config.getUiFontFamily();
        fontFamilyCombo.setSelectedItem(fontFamily.isBlank() ? FONT_AUTO : fontFamily);
        if (fontFamilyCombo.getSelectedIndex() < 0) {
            fontFamilyCombo.setSelectedItem(FONT_AUTO);
        }
        fontSizeSpinner.setValue(config.getUiFontSize());
        twitterStatusLabel.setText(config.hasManualTwitterAuth() ? "ログイン情報あり（未確認）" : "未ログイン");
        refreshPlayerNames();
        playerNameCombo.getEditor().setItem(config.getPlayerName());
    }

    private void reloadDiscordAutoPostWebhookChoices(String selectedName) {
        discordAutoPostWebhookCombo.removeAllItems();
        discordAutoPostWebhookCombo.addItem("");
        for (AppConfig.DiscordWebhookEntry entry : webhookTableModel.getRows()) {
            if (entry.getUrl() != null && !entry.getUrl().isBlank()) {
                discordAutoPostWebhookCombo.addItem(entry.getName());
            }
        }
        if (selectedName != null && !selectedName.isBlank()) {
            discordAutoPostWebhookCombo.setSelectedItem(selectedName);
        }
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

    private void openLogFolder() {
        try {
            Path logDir = AppPaths.logsDir();
            Files.createDirectories(logDir);
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(logDir.toFile());
            } else {
                JOptionPane.showMessageDialog(this, logDir.toAbsolutePath(), "ログフォルダ",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to open log folder", e);
            JOptionPane.showMessageDialog(this, "ログフォルダを開けませんでした: " + e.getMessage(),
                    "エラー", JOptionPane.ERROR_MESSAGE);
        }
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
        config.setClearLampLabels(clearLampLabelTableModel.getOverrides());
        String currentWebhookName = discordAutoPostWebhookCombo.getSelectedItem() == null
                ? ""
                : String.valueOf(discordAutoPostWebhookCombo.getSelectedItem()).trim();
        reloadDiscordAutoPostWebhookChoices(currentWebhookName);
        config.setDiscordAutoPostEnabled(discordAutoPostEnabledBox.isSelected());
        config.setAutoUpdateEnabled(autoUpdateEnabledBox.isSelected());
        config.setDeleteScreenshotsOnExit(deleteScreenshotsOnExitBox.isSelected());
        config.setUiTheme(THEME_LIGHT.equals(themeCombo.getSelectedItem()) ? "light" : "dark");
        Object selectedFont = fontFamilyCombo.getSelectedItem();
        config.setUiFontFamily(selectedFont == null || FONT_AUTO.equals(selectedFont) ? "" : String.valueOf(selectedFont));
        config.setUiFontSize((Integer) fontSizeSpinner.getValue());
        config.setDiscordAutoPostBatchSize((Integer) discordAutoPostBatchSpinner.getValue());
        Object selectedWebhook = discordAutoPostWebhookCombo.getSelectedItem();
        config.setDiscordAutoPostWebhookName(selectedWebhook == null ? "" : String.valueOf(selectedWebhook).trim());
        config.setFirstRunCompleted(true);

        try {
            config.save();
            listener.onSaved(config);
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "保存に失敗しました: " + e.getMessage(), "エラー", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Fixed rows, one per canonical clear lamp type - unlike {@link WebhookTableModel} there is
     * nothing to add or remove, only each row's custom display name to edit. A blank value means
     * "use the default beatoraja name" and is dropped rather than stored.
     */
    private static class ClearLampLabelTableModel extends AbstractTableModel {
        private final String[] columns = {"クリアランプ", "表示名（空欄でデフォルト）"};
        private final List<String> canonicalTypes = ClearLampLabels.CANONICAL_ORDER;
        private Map<String, String> overrides = new LinkedHashMap<>();

        public void setOverrides(Map<String, String> overrides) {
            this.overrides = new LinkedHashMap<>(overrides == null ? Map.of() : overrides);
            fireTableDataChanged();
        }

        public Map<String, String> getOverrides() {
            Map<String, String> result = new LinkedHashMap<>();
            for (String type : canonicalTypes) {
                String value = overrides.get(type);
                if (value != null && !value.isBlank()) {
                    result.put(type, value.trim());
                }
            }
            return result;
        }

        @Override
        public int getRowCount() {
            return canonicalTypes.size();
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
            return columnIndex == 1;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            String type = canonicalTypes.get(rowIndex);
            if (columnIndex == 0) {
                return type;
            }
            String value = overrides.get(type);
            return value == null ? "" : value;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex != 1) {
                return;
            }
            String type = canonicalTypes.get(rowIndex);
            String text = aValue == null ? "" : String.valueOf(aValue).trim();
            if (text.isBlank()) {
                overrides.remove(type);
            } else {
                overrides.put(type, text);
            }
            fireTableCellUpdated(rowIndex, columnIndex);
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
