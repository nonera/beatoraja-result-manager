package com.beatoraja.screenshot.ui;

import com.beatoraja.screenshot.config.AppConfig;
import com.beatoraja.screenshot.db.ScreenshotDatabase;
import com.beatoraja.screenshot.db.ScreenshotRecord;
import com.beatoraja.screenshot.model.ScreenshotEntry;
import com.beatoraja.screenshot.service.DiscordAutoPostQueue;
import com.beatoraja.screenshot.service.DiscordWebhookService;
import com.beatoraja.screenshot.service.PostBatchSplitter;
import com.beatoraja.screenshot.service.PostedStateStore;
import com.beatoraja.screenshot.service.ScreenshotFolderCleanupService;
import com.beatoraja.screenshot.service.ScreenshotScanner;
import com.beatoraja.screenshot.service.ScreenshotWatcher;
import com.beatoraja.screenshot.service.TweetTextGenerator;
import com.beatoraja.screenshot.service.TweetTextLimits;
import com.beatoraja.screenshot.service.ClixService;
import com.beatoraja.screenshot.service.twitter.TwitterAuthService;
import com.beatoraja.screenshot.service.ChartResolverService;
import com.beatoraja.screenshot.table.TableLookupService;
import com.beatoraja.screenshot.ui.theme.AccentButton;
import com.beatoraja.screenshot.ui.theme.UiTheme;
import com.beatoraja.screenshot.util.AppIcons;
import com.beatoraja.screenshot.util.AppLogging;
import com.beatoraja.screenshot.util.AppVersion;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
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
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Color;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class MainFrame extends JFrame {

    private static final Logger LOG = AppLogging.get(MainFrame.class);

    private final AppConfig config;
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
        previewPanel.showEntries(postOrderEntries, buildMessagesByFile(postOrderEntries));
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

    /** The auto-generated post text for one entry; final wording is fixed in the pre-post confirmation dialog. */
    private String resolveEntryMessage(ScreenshotEntry entry) {
        return TweetTextGenerator.generate(entry, resolvePostNotation(entry));
    }

    private Map<String, String> buildMessagesByFile(List<ScreenshotEntry> entries) {
        Map<String, String> messages = new LinkedHashMap<>();
        for (ScreenshotEntry entry : entries) {
            messages.put(entry.getFileName(), resolveEntryMessage(entry));
        }
        return messages;
    }

    /** Joins each entry's own post text, in order, into the caption sent for one post/batch. */
    private String mergeMessages(List<ScreenshotEntry> entries) {
        List<String> lines = new ArrayList<>();
        for (ScreenshotEntry entry : entries) {
            lines.add(resolveEntryMessage(entry));
        }
        return String.join("\n", lines);
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
        selectionCountLabel.setText(selectedCount == 0 ? "" : selectedCount + " 枚選択中");
        twitterButton.setEnabled(selectedCount >= 1);
        twitterButton.setToolTipText(selectedCount > 4
                ? "5枚以上は4枚ずつ別ツイートとして順番に投稿します"
                : null);
        discordButton.setEnabled(selectedCount >= 1 && discordWebhookCombo.getItemCount() > 0);
        discordButton.setToolTipText(selectedCount > 10
                ? "11枚以上は10枚ずつ分割送信します"
                : null);
    }

    private void refreshScreenshots() {
        Path screenshotDir = Path.of(config.getScreenshotDirectory());
        try {
            reloadTableRegistry();
            List<ScreenshotEntry> entries = new ScreenshotScanner().scan(screenshotDir);
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
        // The new screenshot's play result may have just been written to
        // scoredatalog.db, after chartResolverService last loaded it, so
        // reload the player databases before resolving the sha256/md5.
        reloadTableRegistry();
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
            screenshotWatcher.start(screenshotDir, entry -> SwingUtilities.invokeLater(() -> onScreenshotDetected(entry)));
        } catch (IOException e) {
            statusLabel.setText("フォルダ監視を開始できませんでした");
        }
    }

    private void onScreenshotDetected(ScreenshotEntry entry) {
        LOG.info("Screenshot detected: " + entry.getFileName() + " (" + entry.getStateLabel() + ")");
        syncDatabaseEntry(entry);
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
        List<String> notations = resolvePostNotations(entries);
        String message = TweetTextGenerator.generate(entries, notations);
        List<Path> imagePaths = entries.stream().map(ScreenshotEntry::getFilePath).collect(Collectors.toList());

        DiscordWebhookService discordWebhookService = new DiscordWebhookService();
        DiscordWebhookService.PostResult result = discordWebhookService.post(webhook.getUrl(), message, imagePaths);
        if (result.success()) {
            for (ScreenshotEntry entry : entries) {
                postedStateStore.markDiscordPosted(entry.getFileName(), result.messageId());
            }
            return new AutoDiscordPostResult(entries.size(), List.of(), null);
        }
        return new AutoDiscordPostResult(0, entries, result.message());
    }

    private record AutoDiscordPostResult(int postedCount, List<ScreenshotEntry> failedEntries, String errorMessage) {
    }

    private static String joinBatchText(List<ScreenshotEntry> batch, Map<String, String> messagesByFile) {
        return batch.stream().map(e -> messagesByFile.get(e.getFileName())).collect(Collectors.joining("\n"));
    }

    /**
     * Titles are never auto-shortened past this many weighted units. Kept generous
     * (~15 Japanese characters) since the notation/comment portion of a caption is
     * typically at most ~100 characters, not the reason a post goes over the limit, and
     * because the confirmation dialog shown before posting is a manual-edit fallback for
     * whatever this can't fix automatically - there's no need to crush titles further.
     */
    private static final int TITLE_MIN_WEIGHTED_LENGTH = 30;

    /**
     * If a batch's joined caption is over the limit, shortens entries' titles - longest
     * title first - until it fits or no more can be shortened. Returns the file names it
     * abbreviated.
     */
    private List<String> abbreviateBatchIfOverLimit(List<ScreenshotEntry> batch, Map<String, String> messagesByFile) {
        List<String> abbreviated = new ArrayList<>();
        int over = TweetTextLimits.weightedLength(joinBatchText(batch, messagesByFile)) - TweetTextLimits.WEIGHTED_LIMIT;
        if (over <= 0) {
            return abbreviated;
        }

        List<ScreenshotEntry> candidates = new ArrayList<>(batch);
        candidates.sort((a, b) -> Integer.compare(
                TweetTextLimits.weightedLength(b.getTitle()), TweetTextLimits.weightedLength(a.getTitle())));

        for (ScreenshotEntry entry : candidates) {
            if (over <= 0) {
                break;
            }
            int titleWeighted = TweetTextLimits.weightedLength(entry.getTitle());
            int targetTitleWeighted = Math.max(TITLE_MIN_WEIGHTED_LENGTH, titleWeighted - over - 1);
            if (targetTitleWeighted >= titleWeighted) {
                continue;
            }
            messagesByFile.put(entry.getFileName(),
                    TweetTextGenerator.generate(entry, resolvePostNotation(entry), targetTitleWeighted));
            abbreviated.add(entry.getFileName());
            over = TweetTextLimits.weightedLength(joinBatchText(batch, messagesByFile)) - TweetTextLimits.WEIGHTED_LIMIT;
        }
        return abbreviated;
    }

    /**
     * Shows the exact text that will be tweeted - merged per batch, editable right there -
     * and asks the user to confirm before a browser is opened to actually post it. Since
     * this is the last stop before posting, it also re-validates the 280-weighted-unit limit
     * on every confirm attempt and won't let the user through until each batch fits or they
     * cancel, looping with whatever they last typed preserved.
     *
     * @return the (possibly user-edited) text to post for each batch, or {@code null} if
     *         the user canceled
     */
    private List<String> promptFinalPostTexts(List<List<ScreenshotEntry>> batches, Map<String, String> messagesByFile,
            int abbreviatedCount) {
        List<String> texts = new ArrayList<>();
        for (List<ScreenshotEntry> batch : batches) {
            texts.add(joinBatchText(batch, messagesByFile));
        }

        while (true) {
            JPanel content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            if (abbreviatedCount > 0) {
                content.add(new JLabel("※文字数制限のため、" + abbreviatedCount + " 件の曲名を自動的に省略しています。"));
                content.add(Box.createVerticalStrut(8));
            }

            List<JTextArea> areas = new ArrayList<>();
            for (int i = 0; i < batches.size(); i++) {
                if (batches.size() > 1) {
                    content.add(new JLabel((i + 1) + " 件目のツイート"));
                }
                JTextArea area = new JTextArea(texts.get(i), 6, 40);
                area.setLineWrap(true);
                area.setWrapStyleWord(true);
                areas.add(area);

                JLabel counter = new JLabel();
                updateWeightedCountLabel(counter, area.getText());
                area.getDocument().addDocumentListener((SimpleDocumentListener) () -> updateWeightedCountLabel(counter, area.getText()));

                content.add(new JScrollPane(area));
                content.add(counter);
                content.add(Box.createVerticalStrut(8));
            }

            JScrollPane outer = new JScrollPane(content);
            outer.setPreferredSize(new Dimension(460, 360));

            int result = JOptionPane.showConfirmDialog(this, outer, "この内容で投稿しますか？",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result != JOptionPane.OK_OPTION) {
                return null;
            }

            for (int i = 0; i < areas.size(); i++) {
                texts.set(i, areas.get(i).getText());
            }

            List<String> overLimitBatches = new ArrayList<>();
            for (int i = 0; i < texts.size(); i++) {
                int weighted = TweetTextLimits.weightedLength(texts.get(i));
                if (weighted > TweetTextLimits.WEIGHTED_LIMIT) {
                    overLimitBatches.add((i + 1) + "件目: " + weighted + " / " + TweetTextLimits.WEIGHTED_LIMIT + " 文字");
                }
            }
            if (overLimitBatches.isEmpty()) {
                return texts;
            }
            abbreviatedCount = 0; // already shown once; don't repeat the note on every retry
            JOptionPane.showMessageDialog(this,
                    "文字数制限を超えています。修正してください。\n\n" + String.join("\n", overLimitBatches),
                    "文字数オーバー", JOptionPane.WARNING_MESSAGE);
        }
    }

    private static void updateWeightedCountLabel(JLabel label, String text) {
        int weighted = TweetTextLimits.weightedLength(text);
        label.setText(weighted + " / " + TweetTextLimits.WEIGHTED_LIMIT);
        label.setForeground(weighted > TweetTextLimits.WEIGHTED_LIMIT ? new Color(0xE8484F) : UiTheme.mutedText());
    }

    @FunctionalInterface
    private interface SimpleDocumentListener extends DocumentListener {
        void onChange();

        @Override
        default void insertUpdate(DocumentEvent e) {
            onChange();
        }

        @Override
        default void removeUpdate(DocumentEvent e) {
            onChange();
        }

        @Override
        default void changedUpdate(DocumentEvent e) {
            onChange();
        }
    }

    private void postToTwitter() {
        List<ScreenshotEntry> selected = List.copyOf(postOrderEntries);
        if (selected.isEmpty()) {
            return;
        }
        Map<String, String> messageSnapshot = buildMessagesByFile(selected);

        List<List<ScreenshotEntry>> batches = PostBatchSplitter.partition(selected, 4);
        List<String> abbreviatedFileNames = new ArrayList<>();
        for (List<ScreenshotEntry> batch : batches) {
            abbreviatedFileNames.addAll(abbreviateBatchIfOverLimit(batch, messageSnapshot));
        }

        TwitterAuthService authService = new TwitterAuthService(config);
        if (!authService.hasStoredSession()) {
            TwitterLoginDialog loginDialog = new TwitterLoginDialog(this, config);
            ClixService.AuthResult loginResult = loginDialog.showAndLogin();
            if (!loginResult.success()) {
                JOptionPane.showMessageDialog(this, loginResult.message(), "Twitter ログイン", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        List<String> finalBatchTexts = promptFinalPostTexts(batches, messageSnapshot, abbreviatedFileNames.size());
        if (finalBatchTexts == null) {
            return;
        }

        setPostingEnabled(false);
        statusLabel.setText("Twitter 投稿 (0/" + batches.size() + ")");
        LOG.info("Twitter manual post started: " + selected.size() + " image(s), "
                + batches.size() + " batch(es)");

        new Thread(() -> {
            ClixService clixService = new ClixService(config);
            List<ScreenshotEntry> postedEntries = new ArrayList<>();
            String lastError = null;
            for (int i = 0; i < batches.size(); i++) {
                List<ScreenshotEntry> batch = batches.get(i);
                int batchNumber = i + 1;
                SwingUtilities.invokeLater(() ->
                        statusLabel.setText("Twitter 投稿 (" + batchNumber + "/" + batches.size()
                                + ") — ブラウザで投稿してください..."));
                String text = finalBatchTexts.get(i);
                List<Path> imagePaths = batch.stream().map(ScreenshotEntry::getFilePath).collect(Collectors.toList());
                ClixService.PostResult result = clixService.post(text, imagePaths);
                if (result.success()) {
                    postedEntries.addAll(batch);
                } else {
                    lastError = result.message();
                    break;
                }
            }

            List<ScreenshotEntry> postedSnapshot = List.copyOf(postedEntries);
            String errorMessage = lastError;
            int batchCount = batches.size();
            SwingUtilities.invokeLater(() -> {
                setPostingEnabled(true);
                if (errorMessage == null) {
                    LOG.info("Twitter manual post succeeded: " + postedSnapshot.size() + " image(s)");
                    for (ScreenshotEntry entry : postedSnapshot) {
                        postedStateStore.markTwitterPosted(entry.getFileName(), "");
                    }
                    savePostedStateQuietly();
                    databasePanel.setPostedStateStore(postedStateStore);
                    statusLabel.setText("Twitter 投稿完了");
                    String completionMessage = batchCount > 1
                            ? batchCount + " 件のツイート投稿が完了しました。"
                            : "Twitter に投稿しました。";
                    JOptionPane.showMessageDialog(this, completionMessage, "完了", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    statusLabel.setText("Twitter 投稿失敗");
                    LOG.warning("Twitter manual post failed: " + AppLogging.sanitize(errorMessage));
                    if (!postedSnapshot.isEmpty()) {
                        for (ScreenshotEntry entry : postedSnapshot) {
                            postedStateStore.markTwitterPosted(entry.getFileName(), "");
                        }
                        savePostedStateQuietly();
                        databasePanel.setPostedStateStore(postedStateStore);
                    }
                    String message = postedSnapshot.isEmpty()
                            ? errorMessage
                            : postedSnapshot.size() + " 枚までは投稿済みです。\n" + errorMessage;
                    if (errorMessage.contains("ログイン") || errorMessage.contains("認証")) {
                        int answer = JOptionPane.showConfirmDialog(
                                this,
                                message + "\n\nTwitter に再ログインしますか？",
                                "Twitter 投稿失敗",
                                JOptionPane.YES_NO_OPTION
                        );
                        if (answer == JOptionPane.YES_OPTION) {
                            TwitterLoginDialog loginDialog = new TwitterLoginDialog(this, config);
                            loginDialog.showAndLogin();
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, message, "Twitter 投稿失敗", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
        }, "twitter-post").start();
    }

    private void postToDiscord() {
        List<ScreenshotEntry> selected = List.copyOf(postOrderEntries);
        if (selected.isEmpty()) {
            return;
        }
        Map<String, String> messageSnapshot = buildMessagesByFile(selected);

        AppConfig.DiscordWebhookEntry webhook = (AppConfig.DiscordWebhookEntry) discordWebhookCombo.getSelectedItem();
        if (webhook == null || webhook.getUrl() == null || webhook.getUrl().isBlank()) {
            JOptionPane.showMessageDialog(this, "Discord Webhook URL を設定してください。", "設定不足", JOptionPane.WARNING_MESSAGE);
            return;
        }

        setPostingEnabled(false);
        List<List<ScreenshotEntry>> batches = PostBatchSplitter.partition(selected, 10);
        statusLabel.setText("Discord に送信中... (0/" + batches.size() + ")");
        LOG.info("Discord manual post started: " + selected.size() + " image(s), "
                + batches.size() + " batch(es) via "
                + AppLogging.maskWebhookUrl(webhook.getUrl()));

        new Thread(() -> {
            DiscordWebhookService discordWebhookService = new DiscordWebhookService();
            List<ScreenshotEntry> postedEntries = new ArrayList<>();
            String lastError = null;
            for (int i = 0; i < batches.size(); i++) {
                List<ScreenshotEntry> batch = batches.get(i);
                int batchNumber = i + 1;
                SwingUtilities.invokeLater(() ->
                        statusLabel.setText("Discord に送信中... (" + batchNumber + "/" + batches.size() + ")"));
                String message = batch.stream().map(e -> messageSnapshot.get(e.getFileName()))
                        .collect(Collectors.joining("\n"));
                List<Path> imagePaths = batch.stream().map(ScreenshotEntry::getFilePath).collect(Collectors.toList());
                DiscordWebhookService.PostResult result =
                        discordWebhookService.post(webhook.getUrl(), message, imagePaths);
                if (result.success()) {
                    postedEntries.addAll(batch);
                } else {
                    lastError = result.message();
                    break;
                }
            }

            List<ScreenshotEntry> postedSnapshot = List.copyOf(postedEntries);
            String errorMessage = lastError;
            int batchCount = batches.size();
            SwingUtilities.invokeLater(() -> {
                setPostingEnabled(true);
                if (errorMessage == null) {
                    LOG.info("Discord manual post succeeded: " + postedSnapshot.size() + " image(s)");
                    for (ScreenshotEntry entry : postedSnapshot) {
                        postedStateStore.markDiscordPosted(entry.getFileName(), "");
                    }
                    savePostedStateQuietly();
                    databasePanel.setPostedStateStore(postedStateStore);
                    statusLabel.setText("Discord 送信完了");
                    String completionMessage = batchCount > 1
                            ? "Discord に " + batchCount + " 回に分けて送信しました。"
                            : "Discord に送信しました。";
                    JOptionPane.showMessageDialog(this, completionMessage, "完了", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    statusLabel.setText("Discord 送信失敗");
                    LOG.warning("Discord manual post failed: " + AppLogging.sanitize(errorMessage));
                    if (!postedSnapshot.isEmpty()) {
                        for (ScreenshotEntry entry : postedSnapshot) {
                            postedStateStore.markDiscordPosted(entry.getFileName(), "");
                        }
                        savePostedStateQuietly();
                        databasePanel.setPostedStateStore(postedStateStore);
                    }
                    String message = postedSnapshot.isEmpty()
                            ? errorMessage
                            : postedSnapshot.size() + " 枚までは送信済みです。\n" + errorMessage;
                    JOptionPane.showMessageDialog(this, message, "Discord 送信失敗", JOptionPane.ERROR_MESSAGE);
                }
            });
        }, "discord-post").start();
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

        java.util.Map<String, String> tablePrefixByTag = new java.util.LinkedHashMap<>();
        for (com.beatoraja.screenshot.table.DifficultyTableRegistry.TableInfo info : knownTables) {
            String prefix = chartResolverService.getTablePrefix(info.tag());
            if (!prefix.isBlank()) {
                tablePrefixByTag.put(info.tag(), prefix);
            }
        }

        TableNotationRulesDialog dialog = new TableNotationRulesDialog(this, config.getTableNotationRules(),
                knownTables, notationsByTagAndSymbol, chartResolverService.getLoadedFileCount(),
                chartResolverService.getKnownTags(), tablePrefixByTag);
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
