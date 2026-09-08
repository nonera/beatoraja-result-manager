package com.beatoraja.screenshot.ui;

import com.beatoraja.screenshot.config.AppConfig;
import com.beatoraja.screenshot.db.ScreenshotDatabase;
import com.beatoraja.screenshot.db.ScreenshotRecord;
import com.beatoraja.screenshot.model.ScreenshotEntry;
import com.beatoraja.screenshot.service.DiscordWebhookService;
import com.beatoraja.screenshot.service.PostedStateStore;
import com.beatoraja.screenshot.service.ScreenshotScanner;
import com.beatoraja.screenshot.service.ScreenshotWatcher;
import com.beatoraja.screenshot.service.TweetTextGenerator;
import com.beatoraja.screenshot.service.TwitterCliService;
import com.beatoraja.screenshot.service.twitter.TwitterAuthService;
import com.beatoraja.screenshot.service.ChartResolverService;
import com.beatoraja.screenshot.table.TableLookupService;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MainFrame extends JFrame {

    private final AppConfig config;
    private PostedStateStore postedStateStore;
    private ScreenshotWatcher screenshotWatcher;
    private ScreenshotDatabase screenshotDatabase;
    private final ChartResolverService chartResolverService = new ChartResolverService();

    private final ScreenshotListPanel listPanel = new ScreenshotListPanel();
    private final PreviewPanel previewPanel = new PreviewPanel();
    private final DatabasePanel databasePanel = new DatabasePanel();
    private final JLabel statusLabel = new JLabel("準備中...");
    private final JProgressBar indexProgressBar = new JProgressBar();
    private final JComboBox<AppConfig.DiscordWebhookEntry> discordWebhookCombo = new JComboBox<>();
    private final JButton twitterButton = new JButton("Twitter に投稿");
    private final JButton discordButton = new JButton("Discord に送信");
    private JMenuItem refreshMenuItem;
    private SwingWorker<List<TableLookupService.EnrichedScreenshot>, Integer> indexingWorker;

    public MainFrame(AppConfig config) {
        super("beatoraja Screenshot Manager");
        this.config = config;
        this.postedStateStore = PostedStateStore.load();
        initDatabase();
        reloadTableRegistry();
        buildUi();
        databasePanel.setNotationChangeListener(this::refreshSelectedTweetText);
        reloadDiscordWebhooks();
        listPanel.setPostedStateStore(postedStateStore);
        listPanel.setSelectionListener(this::onSelectionChanged);
        refreshScreenshots();
        startWatcher();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 760);
        setLocationRelativeTo(null);
    }

    private void initDatabase() {
        try {
            screenshotDatabase = new ScreenshotDatabase();
        } catch (SQLException e) {
            screenshotDatabase = null;
            JOptionPane.showMessageDialog(this,
                    "データベースの初期化に失敗しました: " + e.getMessage(),
                    "警告", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void buildUi() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("ファイル");
        refreshMenuItem = new JMenuItem("再読み込み");
        refreshMenuItem.addActionListener(e -> refreshScreenshots());
        fileMenu.add(refreshMenuItem);
        menuBar.add(fileMenu);

        JMenu settingsMenu = new JMenu("設定");
        JMenuItem settingsItem = new JMenuItem("設定...");
        settingsItem.addActionListener(e -> openSettings());
        settingsMenu.add(settingsItem);
        JMenuItem tablePriorityItem = new JMenuItem("難易度表の優先順位...");
        tablePriorityItem.addActionListener(e -> openTablePriorityDialog());
        settingsMenu.add(tablePriorityItem);
        JMenuItem tableNotationRulesItem = new JMenuItem("難易度表ごとの投稿表記ルール...");
        tableNotationRulesItem.addActionListener(e -> openTableNotationRulesDialog());
        settingsMenu.add(tableNotationRulesItem);
        menuBar.add(settingsMenu);
        setJMenuBar(menuBar);

        JPanel galleryPanel = new JPanel(new BorderLayout());
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listPanel, previewPanel);
        splitPane.setResizeWeight(0.35);
        galleryPanel.add(splitPane, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new BorderLayout(8, 8));
        actionPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel discordPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        discordPanel.add(new JLabel("Discord 送信先:"));
        discordPanel.add(discordWebhookCombo);
        actionPanel.add(discordPanel, BorderLayout.WEST);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        twitterButton.addActionListener(e -> postToTwitter());
        discordButton.addActionListener(e -> postToDiscord());
        buttons.add(twitterButton);
        buttons.add(discordButton);
        actionPanel.add(buttons, BorderLayout.EAST);
        galleryPanel.add(actionPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("ギャラリー", galleryPanel);
        tabbedPane.addTab("データベース", databasePanel);
        add(tabbedPane, BorderLayout.CENTER);

        indexProgressBar.setStringPainted(true);
        indexProgressBar.setPreferredSize(new Dimension(220, indexProgressBar.getPreferredSize().height));
        indexProgressBar.setVisible(false);

        JPanel statusPanel = new JPanel(new BorderLayout(8, 0));
        statusPanel.setBorder(new EmptyBorder(2, 8, 2, 8));
        statusPanel.add(statusLabel, BorderLayout.CENTER);
        statusPanel.add(indexProgressBar, BorderLayout.EAST);
        add(statusPanel, BorderLayout.SOUTH);

        updateActionButtons(0);
    }

    private void reloadDiscordWebhooks() {
        discordWebhookCombo.removeAllItems();
        for (AppConfig.DiscordWebhookEntry webhook : config.getDiscordWebhooks()) {
            if (webhook.getUrl() != null && !webhook.getUrl().isBlank()) {
                discordWebhookCombo.addItem(webhook);
            }
        }
        discordButton.setEnabled(discordWebhookCombo.getItemCount() > 0);
    }

    private void reloadTableRegistry() {
        try {
            chartResolverService.reload(config);
        } catch (IOException e) {
            statusLabel.setText("難易度表・プレイログの読み込みに失敗: " + e.getMessage());
        }
    }

    private void onSelectionChanged(List<ScreenshotEntry> selectedEntries) {
        List<String> postNotations = resolvePostNotations(selectedEntries);
        String message = TweetTextGenerator.generate(selectedEntries, postNotations);
        previewPanel.showEntries(selectedEntries, message);
        updateActionButtons(selectedEntries.size());
        statusLabel.setText(selectedEntries.isEmpty()
                ? "画像を選択してください"
                : selectedEntries.size() + "枚選択中");
    }

    private void refreshSelectedTweetText() {
        onSelectionChanged(listPanel.getSelectedEntries());
    }

    private List<String> resolvePostNotations(List<ScreenshotEntry> selectedEntries) {
        List<String> notations = new ArrayList<>();
        if (selectedEntries == null) {
            return notations;
        }
        for (ScreenshotEntry entry : selectedEntries) {
            notations.add(resolvePostNotation(entry));
        }
        return notations;
    }

    private String resolvePostNotation(ScreenshotEntry entry) {
        if (entry == null) {
            return "";
        }
        try {
            if (screenshotDatabase != null) {
                ScreenshotRecord record = screenshotDatabase.findByFilePath(entry.getFilePath());
                if (record != null && !record.postNotation().isBlank()) {
                    return record.postNotation();
                }
            }
            TableLookupService.EnrichedScreenshot enriched = chartResolverService.enrich(entry);
            return enriched.defaultPostNotation();
        } catch (Exception e) {
            return "";
        }
    }

    private void updateActionButtons(int selectedCount) {
        twitterButton.setEnabled(selectedCount >= 1 && selectedCount <= 4);
        discordButton.setEnabled(selectedCount >= 1 && selectedCount <= 10 && discordWebhookCombo.getItemCount() > 0);

        twitterButton.setToolTipText(selectedCount > 4 ? "Twitter は最大4枚まで" : null);
        discordButton.setToolTipText(selectedCount > 10 ? "Discord は最大10枚まで" : null);
    }

    private void refreshScreenshots() {
        Path screenshotDir = Path.of(config.getScreenshotDirectory());
        try {
            reloadTableRegistry();
            List<ScreenshotEntry> entries = new ScreenshotScanner().scan(screenshotDir);
            listPanel.setEntries(entries);
            statusLabel.setText(entries.size() + " 件のスクショを読み込みました");
            startIndexing(entries);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "スクショフォルダの読み込みに失敗しました: " + e.getMessage(),
                    "エラー", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void startIndexing(List<ScreenshotEntry> entries) {
        if (screenshotDatabase == null) {
            return;
        }
        if (indexingWorker != null && !indexingWorker.isDone()) {
            indexingWorker.cancel(true);
        }

        int total = entries.size();
        indexProgressBar.setMinimum(0);
        indexProgressBar.setMaximum(total);
        indexProgressBar.setValue(0);
        indexProgressBar.setString("インデックス作成中... 0 / " + total);
        indexProgressBar.setVisible(true);
        refreshMenuItem.setEnabled(false);

        indexingWorker = new SwingWorker<>() {
            @Override
            protected List<TableLookupService.EnrichedScreenshot> doInBackground() {
                List<TableLookupService.EnrichedScreenshot> enriched = new ArrayList<>(total);
                int done = 0;
                for (ScreenshotEntry entry : entries) {
                    if (isCancelled()) {
                        break;
                    }
                    enriched.add(chartResolverService.enrich(entry));
                    done++;
                    publish(done);
                }
                return enriched;
            }

            @Override
            protected void process(List<Integer> chunks) {
                int latest = chunks.get(chunks.size() - 1);
                indexProgressBar.setValue(latest);
                indexProgressBar.setString("インデックス作成中... " + latest + " / " + total);
            }

            @Override
            protected void done() {
                refreshMenuItem.setEnabled(true);
                indexProgressBar.setVisible(false);
                if (isCancelled()) {
                    return;
                }
                try {
                    screenshotDatabase.syncAll(get());
                    databasePanel.reload(screenshotDatabase);
                    statusLabel.setText(total + " 件のスクショ");
                } catch (Exception e) {
                    statusLabel.setText("DB 同期失敗: " + e.getMessage());
                }
            }
        };
        indexingWorker.execute();
    }

    private void syncDatabaseEntry(ScreenshotEntry entry) {
        if (screenshotDatabase == null) {
            return;
        }
        try {
            screenshotDatabase.upsert(chartResolverService.enrich(entry));
            databasePanel.reload(screenshotDatabase);
        } catch (SQLException e) {
            statusLabel.setText("DB 更新失敗: " + e.getMessage());
        }
    }

    private void startWatcher() {
        if (screenshotWatcher != null) {
            screenshotWatcher.close();
        }
        Path screenshotDir = Path.of(config.getScreenshotDirectory());
        screenshotWatcher = new ScreenshotWatcher();
        try {
            screenshotWatcher.start(screenshotDir, entry -> SwingUtilities.invokeLater(() -> {
                listPanel.addEntry(entry);
                syncDatabaseEntry(entry);
                statusLabel.setText("新しいスクショを検出: " + entry.getFileName());
            }));
        } catch (IOException e) {
            statusLabel.setText("フォルダ監視を開始できませんでした");
        }
    }

    private void postToTwitter() {
        List<ScreenshotEntry> selected = listPanel.getSelectedEntries();
        if (selected.isEmpty()) {
            return;
        }
        if (selected.size() > 4) {
            JOptionPane.showMessageDialog(this, "Twitter は最大4枚まで投稿できます。", "制限", JOptionPane.WARNING_MESSAGE);
            return;
        }

        TwitterAuthService authService = new TwitterAuthService(config);
        if (!authService.hasStoredSession()) {
            TwitterLoginDialog loginDialog = new TwitterLoginDialog(this, config);
            TwitterCliService.AuthResult loginResult = loginDialog.showAndLogin();
            if (!loginResult.success()) {
                JOptionPane.showMessageDialog(this, loginResult.message(), "Twitter ログイン", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        setPostingEnabled(false);
        statusLabel.setText("Twitter に投稿中...");

        String message = previewPanel.getMessage();
        List<Path> imagePaths = selected.stream().map(ScreenshotEntry::getFilePath).collect(Collectors.toList());

        new Thread(() -> {
            TwitterAuthService threadAuthService = new TwitterAuthService(config);
            TwitterCliService.AuthResult verified = threadAuthService.verifyStoredSession();
            if (!verified.success()) {
                threadAuthService.refreshSilently();
            }

            TwitterCliService twitterCliService = new TwitterCliService(config);
            TwitterCliService.PostResult result = twitterCliService.post(message, imagePaths);
            SwingUtilities.invokeLater(() -> {
                setPostingEnabled(true);
                if (result.success()) {
                    for (ScreenshotEntry entry : selected) {
                        postedStateStore.markTwitterPosted(entry.getFileName(), result.tweetId());
                    }
                    savePostedStateQuietly();
                    listPanel.setPostedStateStore(postedStateStore);
                    statusLabel.setText("Twitter 投稿完了");
                    JOptionPane.showMessageDialog(this, "Twitter に投稿しました。", "完了", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    statusLabel.setText("Twitter 投稿失敗");
                    if (result.message().contains("認証")) {
                        int answer = JOptionPane.showConfirmDialog(
                                this,
                                result.message() + "\n\nTwitter に再ログインしますか？",
                                "Twitter 投稿失敗",
                                JOptionPane.YES_NO_OPTION
                        );
                        if (answer == JOptionPane.YES_OPTION) {
                            TwitterLoginDialog loginDialog = new TwitterLoginDialog(this, config);
                            loginDialog.showAndLogin();
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, result.message(), "Twitter 投稿失敗", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
        }, "twitter-post").start();
    }

    private void postToDiscord() {
        List<ScreenshotEntry> selected = listPanel.getSelectedEntries();
        if (selected.isEmpty()) {
            return;
        }
        if (selected.size() > 10) {
            JOptionPane.showMessageDialog(this, "Discord は最大10枚まで送信できます。", "制限", JOptionPane.WARNING_MESSAGE);
            return;
        }

        AppConfig.DiscordWebhookEntry webhook = (AppConfig.DiscordWebhookEntry) discordWebhookCombo.getSelectedItem();
        if (webhook == null || webhook.getUrl() == null || webhook.getUrl().isBlank()) {
            JOptionPane.showMessageDialog(this, "Discord Webhook URL を設定してください。", "設定不足", JOptionPane.WARNING_MESSAGE);
            return;
        }

        setPostingEnabled(false);
        statusLabel.setText("Discord に送信中...");

        String message = previewPanel.getMessage();
        List<Path> imagePaths = selected.stream().map(ScreenshotEntry::getFilePath).collect(Collectors.toList());

        new Thread(() -> {
            DiscordWebhookService discordWebhookService = new DiscordWebhookService();
            DiscordWebhookService.PostResult result = discordWebhookService.post(webhook.getUrl(), message, imagePaths);
            SwingUtilities.invokeLater(() -> {
                setPostingEnabled(true);
                if (result.success()) {
                    for (ScreenshotEntry entry : selected) {
                        postedStateStore.markDiscordPosted(entry.getFileName(), result.messageId());
                    }
                    savePostedStateQuietly();
                    listPanel.setPostedStateStore(postedStateStore);
                    statusLabel.setText("Discord 送信完了");
                    JOptionPane.showMessageDialog(this, "Discord に送信しました。", "完了", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    statusLabel.setText("Discord 送信失敗");
                    JOptionPane.showMessageDialog(this, result.message(), "Discord 送信失敗", JOptionPane.ERROR_MESSAGE);
                }
            });
        }, "discord-post").start();
    }

    private void savePostedStateQuietly() {
        try {
            postedStateStore.save();
        } catch (IOException ignored) {
        }
    }

    private void setPostingEnabled(boolean enabled) {
        twitterButton.setEnabled(enabled && listPanel.getSelectedCount() >= 1 && listPanel.getSelectedCount() <= 4);
        discordButton.setEnabled(enabled && listPanel.getSelectedCount() >= 1 && listPanel.getSelectedCount() <= 10
                && discordWebhookCombo.getItemCount() > 0);
    }

    private void openSettings() {
        SettingsDialog dialog = new SettingsDialog(this, config, updatedConfig -> {
            reloadDiscordWebhooks();
            reloadTableRegistry();
            refreshScreenshots();
            startWatcher();
        });
        dialog.setVisible(true);
    }

    private void warnAboutFailedTableFiles() {
        List<String> failed = chartResolverService.getFailedTableFiles();
        if (failed.isEmpty()) {
            return;
        }
        JOptionPane.showMessageDialog(this,
                "以下の難易度表ファイルの読み込みに失敗し、一覧に反映されていません:\n"
                        + String.join("\n", failed)
                        + "\n\nbeatoraja側でその表を再取得すると直る場合があります。",
                "難易度表の読み込み失敗", JOptionPane.WARNING_MESSAGE);
    }

    private void openTablePriorityDialog() {
        List<com.beatoraja.screenshot.table.DifficultyTableRegistry.TableInfo> knownTables =
                chartResolverService.getKnownTables();
        if (knownTables.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "難易度表が読み込まれていません。設定でbeatorajaフォルダを指定してください。",
                    "難易度表の優先順位", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        warnAboutFailedTableFiles();

        java.util.Map<String, String> symbolOverrideByTag = new java.util.LinkedHashMap<>();
        for (AppConfig.TableNotationRule rule : config.getTableNotationRules()) {
            if (!rule.getSymbolOverrides().isEmpty()) {
                String combined = String.join("/", new java.util.LinkedHashSet<>(rule.getSymbolOverrides().values()));
                symbolOverrideByTag.put(rule.getTableTag(), combined);
            }
        }

        TablePriorityDialog dialog = new TablePriorityDialog(this, config.getTablePriorityOrder(), knownTables,
                symbolOverrideByTag, chartResolverService.getLoadedFileCount());
        List<String> newOrder = dialog.showDialog();
        if (newOrder == null) {
            return;
        }

        config.setTablePriorityOrder(newOrder);
        try {
            config.save();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "設定の保存に失敗しました: " + e.getMessage(),
                    "エラー", JOptionPane.ERROR_MESSAGE);
            return;
        }
        refreshScreenshots();
    }

    private void openTableNotationRulesDialog() {
        List<com.beatoraja.screenshot.table.DifficultyTableRegistry.TableInfo> knownTables =
                chartResolverService.getKnownTables();
        if (knownTables.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "難易度表が読み込まれていません。設定でbeatorajaフォルダを指定してください。",
                    "難易度表ごとの投稿表記ルール", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        warnAboutFailedTableFiles();

        java.util.Map<String, java.util.Map<String, List<String>>> notationsByTagAndSymbol = new java.util.LinkedHashMap<>();
        for (com.beatoraja.screenshot.table.DifficultyTableRegistry.TableInfo info : knownTables) {
            java.util.Map<String, List<String>> bySymbol = new java.util.LinkedHashMap<>();
            for (String symbol : chartResolverService.getSymbolsForTag(info.tag())) {
                bySymbol.put(symbol, chartResolverService.getNotationsForTagAndSymbol(info.tag(), symbol));
            }
            notationsByTagAndSymbol.put(info.tag(), bySymbol);
        }

        TableNotationRulesDialog dialog = new TableNotationRulesDialog(this, config.getTableNotationRules(),
                knownTables, notationsByTagAndSymbol, chartResolverService.getLoadedFileCount());
        List<AppConfig.TableNotationRule> newRules = dialog.showDialog();
        if (newRules == null) {
            return;
        }

        config.setTableNotationRules(newRules);
        try {
            config.save();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "設定の保存に失敗しました: " + e.getMessage(),
                    "エラー", JOptionPane.ERROR_MESSAGE);
            return;
        }
        refreshScreenshots();
    }

    @Override
    public void dispose() {
        if (screenshotWatcher != null) {
            screenshotWatcher.close();
        }
        if (screenshotDatabase != null) {
            try {
                screenshotDatabase.close();
            } catch (SQLException ignored) {
            }
        }
        chartResolverService.close();
        super.dispose();
    }
}
