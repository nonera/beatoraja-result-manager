package com.beatoraja.screenshot.ui;

import com.beatoraja.screenshot.config.AppConfig;
import com.beatoraja.screenshot.config.PostTextFormat;
import com.beatoraja.screenshot.db.ScreenshotDatabase;
import com.beatoraja.screenshot.db.ScreenshotRecord;
import com.beatoraja.screenshot.model.ScreenshotEntry;
import com.beatoraja.screenshot.player.PlayerKind;
import com.beatoraja.screenshot.service.DiscordAutoPostQueue;
import com.beatoraja.screenshot.service.DiscordWebhookService;
import com.beatoraja.screenshot.service.LampGroupedTweetText;
import com.beatoraja.screenshot.service.PostBatchSplitter;
import com.beatoraja.screenshot.service.BatchPoster;
import com.beatoraja.screenshot.service.PostedStateStore;
import com.beatoraja.screenshot.service.PostTextComposer;
import com.beatoraja.screenshot.service.PostTextComposer.PostContext;
import com.beatoraja.screenshot.service.ScreenshotFolderCleanupService;
import com.beatoraja.screenshot.service.ScreenshotScanner;
import com.beatoraja.screenshot.service.ScreenshotWatcher;
import com.beatoraja.screenshot.service.ClixService;
import com.beatoraja.screenshot.service.twitter.TwitterAuthService;
import com.beatoraja.screenshot.service.ChartResolverService;
import com.beatoraja.screenshot.table.DifficultyTableRegistry;
import com.beatoraja.screenshot.table.TableLookupService;
import com.beatoraja.screenshot.ui.theme.AccentButton;
import com.beatoraja.screenshot.ui.theme.UiTheme;
import com.beatoraja.screenshot.util.AppIcons;
import com.beatoraja.screenshot.util.AppLogging;
import com.beatoraja.screenshot.util.AppVersion;

import javax.swing.BorderFactory;
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
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class MainFrame extends JFrame {

    private static final Logger LOG = AppLogging.get(MainFrame.class);
    private static final int BMZ_RESOLVE_RETRY_MILLIS = 2000;

    private final AppConfig config;
    private final PostTextComposer postComposer;
    private PostedStateStore postedStateStore;
    private ScreenshotWatcher screenshotWatcher;
    private ScreenshotDatabase screenshotDatabase;
    private final ChartResolverService chartResolverService = new ChartResolverService();
    private final DiscordAutoPostQueue discordAutoPostQueue = new DiscordAutoPostQueue();
    private volatile boolean discordAutoPostInProgress;

    private final PreviewPanel previewPanel = new PreviewPanel();
    private final DatabasePanel databasePanel = new DatabasePanel();
    private final List<ScreenshotEntry> postOrderEntries = new ArrayList<>();
    private final JLabel statusLabel = new JLabel("準備中...");
    private final JLabel selectionCountLabel = new JLabel();
    private final JProgressBar indexProgressBar = new JProgressBar();
    private final JComboBox<AppConfig.DiscordWebhookEntry> discordWebhookCombo = new JComboBox<>();
    private final JButton twitterButton = new AccentButton("Twitter に投稿", UiTheme.twitterAccent());
    private final JButton discordButton = new AccentButton("Discord に送信", UiTheme.discordAccent());
    private JMenuItem refreshMenuItem;
    private SwingWorker<List<TableLookupService.EnrichedScreenshot>, Integer> indexingWorker;

    public MainFrame(AppConfig config) {
        super("beatoraja Screenshot Manager v" + AppVersion.get());
        AppIcons.applyTo(this);
        this.config = config;
        this.postComposer = new PostTextComposer(config, this::resolvePostContext);
        this.postedStateStore = PostedStateStore.load();
        initDatabase();
        reloadTableRegistry();
        buildUi();
        previewPanel.setPostOrderChangeListener(new PreviewPanel.PostOrderChangeListener() {
            @Override
            public void onMoveUp(int index) {
                movePostOrderEntry(index, index - 1);
            }

            @Override
            public void onMoveDown(int index) {
                movePostOrderEntry(index, index + 1);
            }

            @Override
            public void onReorder(int fromIndex, int toIndex) {
                reorderPostOrderEntry(fromIndex, toIndex);
            }
        });
        databasePanel.setNotationChangeListener(this::refreshSelectedTweetText);
        databasePanel.setSelectionListener(this::onSelectionChanged);
        databasePanel.setPostedStateStore(postedStateStore);
        applyClearLampLabels();
        syncSymbolPriorityOrder();
        reloadDiscordWebhooks();
        reloadDatabaseQuietly();
        refreshScreenshots();
        startWatcher();
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitApplication();
            }
        });
        setSize(1200, 760);
        setMinimumSize(new Dimension(900, 600));
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

        JMenu selectionMenu = new JMenu("選択");
        JMenuItem selectFlaggedItem = new JMenuItem("フラグ付きをすべて選択");
        selectFlaggedItem.addActionListener(e -> databasePanel.selectAllFlagged());
        selectionMenu.add(selectFlaggedItem);
        menuBar.add(selectionMenu);

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
        JMenuItem postTextFormatItem = new JMenuItem("投稿文の書式...");
        postTextFormatItem.addActionListener(e -> openPostTextFormatDialog());
        settingsMenu.add(postTextFormatItem);
        menuBar.add(settingsMenu);

        JMenu helpMenu = new JMenu("ヘルプ");
        JMenuItem usageItem = new JMenuItem("使い方...");
        usageItem.addActionListener(e -> HelpDialog.showUsage(this));
        helpMenu.add(usageItem);
        JMenuItem openLogsItem = new JMenuItem("ログフォルダを開く");
        openLogsItem.addActionListener(e -> HelpDialog.openLogFolder(this));
        helpMenu.add(openLogsItem);
        JMenuItem repositoryItem = new JMenuItem("GitHub リポジトリ");
        repositoryItem.addActionListener(e -> HelpDialog.openRepository(this));
        helpMenu.add(repositoryItem);
        helpMenu.addSeparator();
        JMenuItem aboutItem = new JMenuItem("バージョン情報...");
        aboutItem.addActionListener(e -> HelpDialog.showAbout(this));
        helpMenu.add(aboutItem);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(buildToolBar(), BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, databasePanel, previewPanel);
        splitPane.setResizeWeight(0.55);
        splitPane.setBorder(null);
        mainPanel.add(splitPane, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        updateActionButtons(0);
    }

    private JPanel buildToolBar() {
        selectionCountLabel.setForeground(UiTheme.mutedText());

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        left.add(new JLabel("Discord 送信先"));
        left.add(discordWebhookCombo);
        left.add(selectionCountLabel);

        twitterButton.addActionListener(e -> postToTwitter());
        discordButton.addActionListener(e -> postToDiscord());
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(twitterButton);
        right.add(discordButton);

        JPanel toolBar = new JPanel(new BorderLayout(8, 0));
        toolBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UiTheme.subtle()),
                new EmptyBorder(8, 10, 8, 10)));
        toolBar.add(left, BorderLayout.WEST);
        toolBar.add(right, BorderLayout.EAST);
        return toolBar;
    }

    private JPanel buildStatusBar() {
        statusLabel.setForeground(UiTheme.mutedText());
        indexProgressBar.setStringPainted(true);
        indexProgressBar.setPreferredSize(new Dimension(220, indexProgressBar.getPreferredSize().height));
        indexProgressBar.setVisible(false);

        JLabel versionLabel = new JLabel("v" + AppVersion.get());
        versionLabel.setForeground(UiTheme.mutedText());
        versionLabel.setFont(versionLabel.getFont().deriveFont(Font.PLAIN, versionLabel.getFont().getSize2D() - 1f));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.add(indexProgressBar);
        right.add(versionLabel);

        JPanel statusPanel = new JPanel(new BorderLayout(8, 0));
        statusPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UiTheme.subtle()),
                new EmptyBorder(4, 10, 4, 10)));
        statusPanel.add(statusLabel, BorderLayout.CENTER);
        statusPanel.add(right, BorderLayout.EAST);
        return statusPanel;
    }

    private void reloadDatabaseQuietly() {
        if (screenshotDatabase == null) {
            return;
        }
        try {
            databasePanel.reload(screenshotDatabase);
        } catch (SQLException e) {
            statusLabel.setText("DB 読み込み失敗: " + e.getMessage());
        }
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
            syncSymbolPriorityOrder();
        } catch (IOException e) {
            statusLabel.setText("難易度表・プレイログの読み込みに失敗: " + e.getMessage());
        }
    }

    private void syncSymbolPriorityOrder() {
        databasePanel.setSymbolPriorityOrder(chartResolverService.getSymbolsInPriorityOrder());
    }

    private void onSelectionChanged(List<ScreenshotEntry> selectedEntries) {
        syncPostOrder(selectedEntries);
        refreshPostPreview();
        updateActionButtons(postOrderEntries.size());
        statusLabel.setText(postOrderEntries.isEmpty()
                ? "画像を選択してください"
                : postOrderEntries.size() + "枚選択中");
    }

    private void refreshSelectedTweetText() {
        syncPostOrder(databasePanel.getSelectedEntries());
        refreshPostPreview();
    }

    private void syncPostOrder(List<ScreenshotEntry> newlySelected) {
        if (newlySelected == null || newlySelected.isEmpty()) {
            postOrderEntries.clear();
            return;
        }

        Set<String> selectedNames = new HashSet<>();
        for (ScreenshotEntry entry : newlySelected) {
            selectedNames.add(entry.getFileName());
        }

        List<ScreenshotEntry> kept = new ArrayList<>();
        for (ScreenshotEntry entry : postOrderEntries) {
            if (selectedNames.contains(entry.getFileName())) {
                kept.add(entry);
            }
        }

        Set<String> keptNames = new HashSet<>();
        for (ScreenshotEntry entry : kept) {
            keptNames.add(entry.getFileName());
        }
        for (ScreenshotEntry entry : newlySelected) {
            if (!keptNames.contains(entry.getFileName())) {
                kept.add(entry);
            }
        }

        postOrderEntries.clear();
        postOrderEntries.addAll(kept);
    }

    private void refreshPostPreview() {
        previewPanel.showEntries(postOrderEntries, postComposer.messagesByFile(postOrderEntries));
    }

    private void movePostOrderEntry(int fromIndex, int toIndex) {
        if (fromIndex < 0 || toIndex < 0 || fromIndex >= postOrderEntries.size() || toIndex >= postOrderEntries.size()) {
            return;
        }
        Collections.swap(postOrderEntries, fromIndex, toIndex);
        refreshPostPreview();
    }

    /** Drag-and-drop reorder: unlike {@link #movePostOrderEntry}, this moves rather than swaps. */
    private void reorderPostOrderEntry(int fromIndex, int toIndex) {
        if (fromIndex < 0 || toIndex < 0 || fromIndex >= postOrderEntries.size()
                || toIndex >= postOrderEntries.size() || fromIndex == toIndex) {
            return;
        }
        ScreenshotEntry moved = postOrderEntries.remove(fromIndex);
        postOrderEntries.add(toIndex, moved);
        refreshPostPreview();
    }

    /** Pushes the current custom clear lamp labels ("俗称") to the panels that display them. */
    private void applyClearLampLabels() {
        databasePanel.setClearLampLabels(config.getClearLampLabels());
        previewPanel.setClearLampLabels(config.getClearLampLabels());
    }

    /** The post notation (a user-chosen one saved in the DB wins) and the matched play's score, if any. */
    private PostContext resolvePostContext(ScreenshotEntry entry) {
        if (entry == null) {
            return PostContext.NONE;
        }
        String savedNotation = "";
        try {
            if (screenshotDatabase != null) {
                ScreenshotRecord record = screenshotDatabase.findByFilePath(entry.getFilePath());
                if (record != null) {
                    savedNotation = record.postNotation();
                }
            }
        } catch (Exception e) {
            // fall through to the resolved default
        }
        try {
            TableLookupService.EnrichedScreenshot enriched = chartResolverService.enrich(entry);
            String notation = savedNotation.isBlank() ? enriched.defaultPostNotation() : savedNotation;
            return new PostContext(notation, enriched.score());
        } catch (Exception e) {
            return new PostContext(savedNotation, null);
        }
    }

    private void updateActionButtons(int selectedCount) {
        selectionCountLabel.setText(selectedCount == 0 ? "" : selectedCount + " 枚選択中");
        twitterButton.setEnabled(selectedCount >= 1);
        int maxImages = ClixService.MAX_IMAGES_PER_POST;
        String tooltip = null;
        if (selectedCount > maxImages) {
            tooltip = (maxImages + 1) + "枚以上は" + maxImages + "枚ずつ別ツイートとして順番に投稿します";
        } else if (LampGroupedTweetText.shouldUse(selectedCount)) {
            tooltip = "枚数が多いため、投稿文はランプ別のまとめ形式になります";
        }
        twitterButton.setToolTipText(tooltip);
        discordButton.setEnabled(selectedCount >= 1 && discordWebhookCombo.getItemCount() > 0);
        int maxDiscordImages = DiscordWebhookService.MAX_IMAGES_PER_POST;
        discordButton.setToolTipText(selectedCount > maxDiscordImages
                ? (maxDiscordImages + 1) + "枚以上は" + maxDiscordImages + "枚ずつ分割送信します"
                : null);
    }

    private void refreshScreenshots() {
        Path screenshotDir = Path.of(config.getScreenshotDirectory());
        try {
            reloadTableRegistry();
            List<ScreenshotEntry> entries = new ScreenshotScanner().scan(screenshotDir, config.playerKind());
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

    /**
     * Resolves and stores a newly detected screenshot, returning it as resolved. For BMZ
     * Player screenshots that is where the song, lamp and rank come from, since their file
     * names do not identify the chart.
     */
    private TableLookupService.EnrichedScreenshot syncDatabaseEntry(ScreenshotEntry entry) {
        // The new screenshot's play result may have just been written to
        // scoredatalog.db, after chartResolverService last loaded it, so
        // reload the player databases before resolving the sha256/md5.
        reloadTableRegistry();
        TableLookupService.EnrichedScreenshot enriched = chartResolverService.enrich(entry);
        if (screenshotDatabase != null) {
            try {
                screenshotDatabase.upsert(enriched);
                databasePanel.reload(screenshotDatabase);
            } catch (SQLException e) {
                statusLabel.setText("DB 更新失敗: " + e.getMessage());
            }
        }
        return enriched;
    }

    private void startWatcher() {
        if (screenshotWatcher != null) {
            screenshotWatcher.close();
        }
        Path screenshotDir = Path.of(config.getScreenshotDirectory());
        screenshotWatcher = new ScreenshotWatcher();
        try {
            screenshotWatcher.start(screenshotDir, config.playerKind(), entry -> SwingUtilities.invokeLater(() -> onScreenshotDetected(entry)));
        } catch (IOException e) {
            statusLabel.setText("フォルダ監視を開始できませんでした");
        }
    }

    private void onScreenshotDetected(ScreenshotEntry entry) {
        LOG.info("Screenshot detected: " + entry.getFileName() + " (" + entry.getStateLabel() + ")");
        TableLookupService.EnrichedScreenshot resolved = syncDatabaseEntry(entry);
        if (chartResolverService.getPlayerKind() == PlayerKind.BMZ && entry.isResultScreenshot()
                && !resolved.resolvedFromPlayerData()) {
            // BMZ Player may still be saving the play to score.db; try once more shortly.
            Timer retry = new Timer(BMZ_RESOLVE_RETRY_MILLIS, e -> offerToAutoPost(syncDatabaseEntry(entry).entry()));
            retry.setRepeats(false);
            retry.start();
            return;
        }
        offerToAutoPost(resolved.entry());
    }

    private void offerToAutoPost(ScreenshotEntry entry) {
        int queueSizeBefore = discordAutoPostQueue.size();
        discordAutoPostQueue.offer(entry, postedStateStore);
        if (discordAutoPostQueue.size() > queueSizeBefore) {
            LOG.info("Discord auto-post queue: " + discordAutoPostQueue.size() + " item(s)");
        }
        tryAutoPostDiscord();
        statusLabel.setText("新しいスクショを検出しました");
    }

    private void tryAutoPostDiscord() {
        if (discordAutoPostInProgress || !config.isDiscordAutoPostEnabled()) {
            return;
        }
        AppConfig.DiscordWebhookEntry webhook = config.resolveDiscordAutoPostWebhook();
        if (webhook == null || webhook.getUrl() == null || webhook.getUrl().isBlank()) {
            return;
        }
        int batchSize = config.getDiscordAutoPostBatchSize();
        if (discordAutoPostQueue.size() < batchSize) {
            return;
        }

        List<ScreenshotEntry> batch = discordAutoPostQueue.pollBatch(batchSize);
        if (batch.isEmpty()) {
            return;
        }

        LOG.info("Discord auto-post starting: " + batch.size() + " image(s) via "
                + AppLogging.maskWebhookUrl(webhook.getUrl()));
        discordAutoPostInProgress = true;
        new Thread(() -> {
            AutoDiscordPostResult result = postDiscordBatch(batch, webhook);
            SwingUtilities.invokeLater(() -> {
                discordAutoPostInProgress = false;
                if (!result.failedEntries().isEmpty()) {
                    discordAutoPostQueue.requeueFront(result.failedEntries());
                    LOG.warning("Discord auto-post failed, requeued "
                            + result.failedEntries().size() + " image(s): "
                            + AppLogging.sanitize(result.errorMessage()));
                }
                if (result.postedCount() > 0) {
                    savePostedStateQuietly();
                    databasePanel.setPostedStateStore(postedStateStore);
                    statusLabel.setText("Discord 自動投稿: " + result.postedCount() + " 枚送信");
                    LOG.info("Discord auto-post succeeded: " + result.postedCount() + " image(s)");
                } else if (result.errorMessage() != null) {
                    statusLabel.setText("Discord 自動投稿失敗: " + result.errorMessage());
                }
                tryAutoPostDiscord();
            });
        }, "discord-auto-post").start();
    }

    private AutoDiscordPostResult postDiscordBatch(List<ScreenshotEntry> entries,
            AppConfig.DiscordWebhookEntry webhook) {
        // A screenshot may have been sent manually (and marked posted) after it was already
        // queued for auto-post, so re-check here to avoid posting it twice.
        List<ScreenshotEntry> pending = entries.stream()
                .filter(entry -> !postedStateStore.get(entry.getFileName()).isDiscordPosted())
                .collect(Collectors.toList());

        // A queued screenshot's file may since have been deleted (e.g. from disk directly,
        // bypassing the database panel's own queue cleanup). That file is gone for good, so
        // requeuing it would just fail the same way forever and block every other queued
        // image behind it - drop it instead of retrying. The rest of the batch wasn't actually
        // ready yet (it's short of the configured batch size), so put it back in the queue to
        // wait for new screenshots instead of posting an undersized batch now.
        List<ScreenshotEntry> missing = pending.stream()
                .filter(entry -> !Files.exists(entry.getFilePath()))
                .collect(Collectors.toList());
        if (!missing.isEmpty()) {
            for (ScreenshotEntry entry : missing) {
                LOG.warning("Discord auto-post: dropping queued image no longer on disk: " + entry.getFileName());
            }
            pending.removeAll(missing);
            if (!pending.isEmpty()) {
                discordAutoPostQueue.requeueFront(pending);
            }
            return new AutoDiscordPostResult(0, List.of(), null);
        }

        if (pending.isEmpty()) {
            return new AutoDiscordPostResult(0, List.of(), null);
        }

        String message = postComposer.merged(pending);
        List<Path> imagePaths = pending.stream().map(ScreenshotEntry::getFilePath).collect(Collectors.toList());

        DiscordWebhookService discordWebhookService = new DiscordWebhookService();
        DiscordWebhookService.PostResult result = discordWebhookService.post(webhook.getUrl(), message, imagePaths);
        if (result.success()) {
            for (ScreenshotEntry entry : pending) {
                postedStateStore.markDiscordPosted(entry.getFileName(), result.messageId());
            }
            return new AutoDiscordPostResult(pending.size(), List.of(), null);
        }
        return new AutoDiscordPostResult(0, pending, result.message());
    }

    private record AutoDiscordPostResult(int postedCount, List<ScreenshotEntry> failedEntries, String errorMessage) {
    }

    private void postToTwitter() {
        List<ScreenshotEntry> selected = List.copyOf(postOrderEntries);
        if (selected.isEmpty()) {
            return;
        }
        List<List<ScreenshotEntry>> batches = PostBatchSplitter.partition(selected, ClixService.MAX_IMAGES_PER_POST);
        PostTextComposer.TweetTexts tweetTexts = postComposer.tweetTexts(batches);

        TwitterAuthService authService = new TwitterAuthService(config);
        if (!authService.hasStoredSession()) {
            TwitterLoginDialog loginDialog = new TwitterLoginDialog(this, config);
            ClixService.AuthResult loginResult = loginDialog.showAndLogin();
            if (!loginResult.success()) {
                JOptionPane.showMessageDialog(this, loginResult.message(), "Twitter ログイン", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        List<String> texts = PostConfirmDialog.confirmTweets(this, tweetTexts.texts(), tweetTexts.abbreviatedCount());
        if (texts == null) {
            return;
        }
        ClixService clixService = new ClixService(config);
        runManualPost(new ManualPost(
                "Twitter",
                batch -> "Twitter 投稿 (" + batch + "/" + batches.size() + ") — ブラウザで投稿してください...",
                "Twitter 投稿 (0/" + batches.size() + ")",
                "Twitter 投稿完了",
                batches.size() > 1 ? batches.size() + " 件のツイート投稿が完了しました。" : "Twitter に投稿しました。",
                "Twitter 投稿失敗",
                "投稿済み",
                (text, images) -> {
                    ClixService.PostResult result = clixService.post(text, images);
                    return result.success() ? null : failureMessage(result.message());
                },
                posted -> posted.forEach(entry -> postedStateStore.markTwitterPosted(entry.getFileName(), "")),
                this::showTwitterPostFailure
        ), selected, batches, texts);
    }

    /** X failures caused by the login offer to log in again. */
    private void showTwitterPostFailure(String message, String error) {
        if (error.contains("ログイン") || error.contains("認証")) {
            int answer = JOptionPane.showConfirmDialog(this, message + "\n\nTwitter に再ログインしますか？",
                    "Twitter 投稿失敗", JOptionPane.YES_NO_OPTION);
            if (answer == JOptionPane.YES_OPTION) {
                new TwitterLoginDialog(this, config).showAndLogin();
            }
        } else {
            JOptionPane.showMessageDialog(this, message, "Twitter 投稿失敗", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void postToDiscord() {
        List<ScreenshotEntry> selected = List.copyOf(postOrderEntries);
        if (selected.isEmpty()) {
            return;
        }
        AppConfig.DiscordWebhookEntry webhook = (AppConfig.DiscordWebhookEntry) discordWebhookCombo.getSelectedItem();
        if (webhook == null || webhook.getUrl() == null || webhook.getUrl().isBlank()) {
            JOptionPane.showMessageDialog(this, "Discord Webhook URL を設定してください。", "設定不足", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<List<ScreenshotEntry>> batches = PostBatchSplitter.partition(selected, DiscordWebhookService.MAX_IMAGES_PER_POST);
        Map<String, String> messages = postComposer.messagesByFile(selected);
        List<String> texts = PostConfirmDialog.confirmDiscord(this,
                batches.stream().map(batch -> PostTextComposer.join(batch, messages)).toList());
        if (texts == null) {
            return;
        }
        LOG.info("Discord manual post via " + AppLogging.maskWebhookUrl(webhook.getUrl()));
        DiscordWebhookService discordWebhookService = new DiscordWebhookService();
        runManualPost(new ManualPost(
                "Discord",
                batch -> "Discord に送信中... (" + batch + "/" + batches.size() + ")",
                "Discord に送信中... (0/" + batches.size() + ")",
                "Discord 送信完了",
                batches.size() > 1 ? "Discord に " + batches.size() + " 回に分けて送信しました。" : "Discord に送信しました。",
                "Discord 送信失敗",
                "送信済み",
                (text, images) -> {
                    DiscordWebhookService.PostResult result = discordWebhookService.post(webhook.getUrl(), text, images);
                    return result.success() ? null : failureMessage(result.message());
                },
                posted -> {
                    posted.forEach(entry -> postedStateStore.markDiscordPosted(entry.getFileName(), ""));
                    discordAutoPostQueue.removePosted(posted);
                },
                (message, error) -> JOptionPane.showMessageDialog(this, message, "Discord 送信失敗", JOptionPane.ERROR_MESSAGE)
        ), selected, batches, texts);
    }

    private static String failureMessage(String message) {
        return message == null || message.isBlank() ? "投稿に失敗しました" : message;
    }

    /**
     * How one service's manual post reads and records: the status texts, the sender, how sent
     * screenshots are marked posted, and how a failure is shown ({@code (message, error)}).
     */
    private record ManualPost(
            String service,
            IntFunction<String> progressStatus,
            String startStatus,
            String doneStatus,
            String completionMessage,
            String failedStatus,
            String sentWord,
            BatchPoster.Sender sender,
            Consumer<List<ScreenshotEntry>> markPosted,
            BiConsumer<String, String> showFailure) {
    }

    /**
     * Sends the batches on a worker thread, then - on the EDT - records whatever went out as
     * posted (also when a later batch failed) and tells the user how it ended.
     */
    private void runManualPost(ManualPost post, List<ScreenshotEntry> selected, List<List<ScreenshotEntry>> batches,
            List<String> texts) {
        setPostingEnabled(false);
        statusLabel.setText(post.startStatus());
        LOG.info(post.service() + " manual post started: " + selected.size() + " image(s), "
                + batches.size() + " batch(es)");

        new Thread(() -> {
            BatchPoster.Result result = BatchPoster.postInOrder(batches, texts, post.sender(),
                    batch -> SwingUtilities.invokeLater(() -> statusLabel.setText(post.progressStatus().apply(batch))));
            SwingUtilities.invokeLater(() -> {
                setPostingEnabled(true);
                if (!result.posted().isEmpty()) {
                    post.markPosted().accept(result.posted());
                    savePostedStateQuietly();
                    databasePanel.setPostedStateStore(postedStateStore);
                }
                if (result.succeeded()) {
                    LOG.info(post.service() + " manual post succeeded: " + result.posted().size() + " image(s)");
                    statusLabel.setText(post.doneStatus());
                    JOptionPane.showMessageDialog(this, post.completionMessage(), "完了", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                statusLabel.setText(post.failedStatus());
                LOG.warning(post.service() + " manual post failed: " + AppLogging.sanitize(result.error()));
                String message = result.posted().isEmpty()
                        ? result.error()
                        : result.posted().size() + " 枚までは" + post.sentWord() + "です。\n" + result.error();
                post.showFailure().accept(message, result.error());
            });
        }, post.service().toLowerCase(Locale.ROOT) + "-post").start();
    }

    private void savePostedStateQuietly() {
        try {
            postedStateStore.save();
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Failed to save posted state", e);
        }
    }

    private void setPostingEnabled(boolean enabled) {
        int selectedCount = databasePanel.getSelectedCount();
        twitterButton.setEnabled(enabled && selectedCount >= 1);
        discordButton.setEnabled(enabled && selectedCount >= 1 && discordWebhookCombo.getItemCount() > 0);
    }

    private void openSettings() {
        String previousAppearance = appearanceKey(config);
        SettingsDialog dialog = new SettingsDialog(this, config, updatedConfig -> {
            if (!previousAppearance.equals(appearanceKey(updatedConfig))) {
                reopenWithAppearance(updatedConfig);
                return;
            }
            reloadDiscordWebhooks();
            reloadTableRegistry();
            applyClearLampLabels();
            refreshScreenshots();
            startWatcher();
        });
        dialog.setVisible(true);
    }

    private static String appearanceKey(AppConfig config) {
        return config.getUiTheme() + "\u0000" + config.getUiFontFamily() + "\u0000" + config.getUiFontSize();
    }

    /**
     * Rebuilds the window so every cached theme color and font-derived size is re-read.
     * Repainting in place would leave the custom renderers, borders and row heights stale.
     */
    private void reopenWithAppearance(AppConfig updatedConfig) {
        UiTheme.apply(updatedConfig.getUiTheme(), updatedConfig.getUiFontFamily(), updatedConfig.getUiFontSize());
        dispose();
        SwingUtilities.invokeLater(() -> new MainFrame(updatedConfig).setVisible(true));
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

    /** The loaded difficulty tables, or null - after telling the user - when none are loaded. */
    private List<DifficultyTableRegistry.TableInfo> requireLoadedTables(String dialogTitle) {
        List<DifficultyTableRegistry.TableInfo> knownTables = chartResolverService.getKnownTables();
        if (knownTables.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "難易度表が読み込まれていません。設定でbeatorajaフォルダを指定してください。",
                    dialogTitle, JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        warnAboutFailedTableFiles();
        return knownTables;
    }

    /** Saves the config; on failure tells the user and returns false. */
    private boolean saveConfig() {
        try {
            config.save();
            return true;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "設定の保存に失敗しました: " + e.getMessage(),
                    "エラー", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void openTablePriorityDialog() {
        List<DifficultyTableRegistry.TableInfo> knownTables = requireLoadedTables("難易度表の優先順位");
        if (knownTables == null) {
            return;
        }
        Map<String, String> symbolOverrideByTag = new LinkedHashMap<>();
        for (AppConfig.TableNotationRule rule : config.getTableNotationRules()) {
            if (!rule.getSymbolOverrides().isEmpty()) {
                symbolOverrideByTag.put(rule.getTableTag(),
                        String.join("/", new LinkedHashSet<>(rule.getSymbolOverrides().values())));
            }
        }

        List<String> newOrder = new TablePriorityDialog(this, config.getTablePriorityOrder(), knownTables,
                symbolOverrideByTag, chartResolverService.getLoadedFileCount()).showDialog();
        if (newOrder == null) {
            return;
        }
        config.setTablePriorityOrder(newOrder);
        if (saveConfig()) {
            refreshScreenshots();
        }
    }

    private void openPostTextFormatDialog() {
        PostTextFormat newFormat = new PostTextFormatDialog(this, config.getPostTextFormat()).showDialog();
        if (newFormat == null) {
            return;
        }
        config.setPostTextFormat(newFormat);
        if (saveConfig()) {
            refreshPostPreview();
        }
    }

    private void openTableNotationRulesDialog() {
        List<DifficultyTableRegistry.TableInfo> knownTables = requireLoadedTables("難易度表ごとの投稿表記ルール");
        if (knownTables == null) {
            return;
        }
        Map<String, Map<String, List<String>>> notationsByTagAndSymbol = new LinkedHashMap<>();
        Map<String, String> tablePrefixByTag = new LinkedHashMap<>();
        for (DifficultyTableRegistry.TableInfo info : knownTables) {
            Map<String, List<String>> bySymbol = new LinkedHashMap<>();
            for (String symbol : chartResolverService.getSymbolsForTag(info.tag())) {
                bySymbol.put(symbol, chartResolverService.getNotationsForTagAndSymbol(info.tag(), symbol));
            }
            notationsByTagAndSymbol.put(info.tag(), bySymbol);
            String prefix = chartResolverService.getTablePrefix(info.tag());
            if (!prefix.isBlank()) {
                tablePrefixByTag.put(info.tag(), prefix);
            }
        }

        List<AppConfig.TableNotationRule> newRules = new TableNotationRulesDialog(this, config.getTableNotationRules(),
                knownTables, notationsByTagAndSymbol, chartResolverService.getLoadedFileCount(),
                chartResolverService.getKnownTags(), tablePrefixByTag).showDialog();
        if (newRules == null) {
            return;
        }
        config.setTableNotationRules(newRules);
        if (saveConfig()) {
            refreshScreenshots();
        }
    }

    private void exitApplication() {
        maybeDeleteScreenshotsOnExit();
        dispose();
        System.exit(0);
    }

    private void maybeDeleteScreenshotsOnExit() {
        if (!config.isDeleteScreenshotsOnExit()) {
            return;
        }
        String screenshotDir = config.getScreenshotDirectory();
        if (screenshotDir == null || screenshotDir.isBlank()) {
            return;
        }
        Path directory = Path.of(screenshotDir);
        if (!Files.isDirectory(directory)) {
            return;
        }
        try {
            int deleted = new ScreenshotFolderCleanupService().deleteAllPng(directory);
            LOG.info("Deleted " + deleted + " screenshot(s) on exit from " + directory);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Failed to delete screenshots on exit", e);
        }
    }

    @Override
    public void dispose() {
        if (screenshotWatcher != null) {
            screenshotWatcher.close();
        }
        if (screenshotDatabase != null) {
            try {
                screenshotDatabase.close();
            } catch (SQLException e) {
                LOG.log(Level.WARNING, "Failed to close screenshot database", e);
            }
        }
        databasePanel.shutdown();
        chartResolverService.close();
        LOG.info("Main window closed");
        super.dispose();
    }
}
