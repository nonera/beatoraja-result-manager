package com.beatoraja.screenshot.ui;

import com.beatoraja.screenshot.model.ScreenshotEntry;
import com.beatoraja.screenshot.ui.theme.Badge;
import com.beatoraja.screenshot.ui.theme.CardPanel;
import com.beatoraja.screenshot.ui.theme.ResultPalette;
import com.beatoraja.screenshot.ui.theme.UiTheme;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Shows the currently selected screenshot(s) and lets the user edit the post text.
 * With multiple images, each image owns its own text field (keyed by file name in
 * {@link MessageChangeListener} callbacks) instead of one shared block of text, so
 * reordering or deleting a line can never desynchronize a caption from its image.
 * With a single image, that same per-image field sits in the message card below it -
 * there is no separate shared text box.
 */
public class PreviewPanel extends JPanel {

    public interface PostOrderChangeListener {
        void onMoveUp(int index);

        void onMoveDown(int index);
    }

    public interface MessageChangeListener {
        void onMessageChanged(String fileName, String text);
    }

    private static final String CARD_SINGLE = "single";
    private static final String CARD_MULTI = "multi";
    private static final int MULTI_PREVIEW_WIDTH = 420;
    private static final int TWEET_WEIGHTED_LIMIT = 280;
    private static final DateTimeFormatter CAPTURED_AT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

    private PostOrderChangeListener postOrderChangeListener;
    private MessageChangeListener messageChangeListener;
    private boolean programmaticTextUpdate;
    private String singleEntryFileName;

    private final JLabel headerTitleLabel = new JLabel();
    private final JLabel headerDateLabel = new JLabel();
    private final JPanel headerBadgeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
    private final ImageCanvas imageCanvas = new ImageCanvas();
    private final JPanel multiPreviewPanel = new JPanel();
    private final JScrollPane multiScrollPane = new JScrollPane(multiPreviewPanel);
    private final JPanel imageCards = new JPanel(new CardLayout());
    private final JTextArea singleMessageArea = new JTextArea(3, 40);
    private final JLabel singleMessageCountLabel = new JLabel();

    public PreviewPanel() {
        setLayout(new BorderLayout(0, 8));
        setBorder(new EmptyBorder(8, 8, 8, 8));

        add(buildHeaderCard(), BorderLayout.NORTH);

        imageCanvas.setPreferredSize(new Dimension(480, 400));
        multiPreviewPanel.setLayout(new BoxLayout(multiPreviewPanel, BoxLayout.Y_AXIS));
        multiPreviewPanel.setOpaque(false);
        multiScrollPane.setPreferredSize(new Dimension(480, 400));
        multiScrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        multiScrollPane.setOpaque(false);
        multiScrollPane.getViewport().setOpaque(false);
        multiScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        multiScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        imageCards.setOpaque(false);
        imageCards.add(imageCanvas, CARD_SINGLE);
        imageCards.add(multiScrollPane, CARD_MULTI);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, imageCards, buildMessageCard());
        splitPane.setResizeWeight(1.0);
        splitPane.setContinuousLayout(true);
        splitPane.setBorder(null);
        add(splitPane, BorderLayout.CENTER);

        updateMessageCount();
    }

    private JPanel buildHeaderCard() {
        headerTitleLabel.setFont(headerTitleLabel.getFont()
                .deriveFont(Font.BOLD, UiTheme.fontSize() + 2f));
        headerDateLabel.setForeground(UiTheme.mutedText());
        headerBadgeRow.setOpaque(false);
        // Pin the height so the card keeps one size whether or not the selection has badges;
        // otherwise the surrounding BoxLayout squeezes the row down to a few pixels.
        int badgeHeight = Badge.height(headerBadgeRow.getFontMetrics(headerBadgeRow.getFont()));
        Dimension badgeRowSize = new Dimension(10, badgeHeight + 2);
        headerBadgeRow.setPreferredSize(badgeRowSize);
        headerBadgeRow.setMinimumSize(badgeRowSize);
        headerBadgeRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, badgeRowSize.height));

        JPanel textColumn = new JPanel();
        textColumn.setOpaque(false);
        textColumn.setLayout(new BoxLayout(textColumn, BoxLayout.Y_AXIS));
        headerTitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerBadgeRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        textColumn.add(headerTitleLabel);
        textColumn.add(Box.createVerticalStrut(6));
        textColumn.add(headerBadgeRow);

        CardPanel card = new CardPanel(new BorderLayout(8, 0));
        card.setBorder(new EmptyBorder(10, 12, 10, 12));
        card.add(textColumn, BorderLayout.CENTER);
        card.add(headerDateLabel, BorderLayout.EAST);
        return card;
    }

    /** The single-image case's message card. With multiple images, each row has its own instead. */
    private JPanel buildMessageCard() {
        singleMessageArea.setLineWrap(false);
        singleMessageArea.setRows(3);
        singleMessageArea.setOpaque(false);
        singleMessageArea.setBorder(new EmptyBorder(4, 4, 4, 4));
        singleMessageArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                onSingleMessageTextChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                onSingleMessageTextChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                onSingleMessageTextChanged();
            }
        });

        JLabel caption = new JLabel("投稿文");
        caption.setForeground(UiTheme.mutedText());
        singleMessageCountLabel.setForeground(UiTheme.mutedText());

        JPanel captionRow = new JPanel(new BorderLayout());
        captionRow.setOpaque(false);
        captionRow.add(caption, BorderLayout.WEST);
        captionRow.add(singleMessageCountLabel, BorderLayout.EAST);

        JScrollPane messageScroll = new JScrollPane(singleMessageArea);
        messageScroll.setBorder(new EmptyBorder(0, 0, 0, 0));
        messageScroll.setOpaque(false);
        messageScroll.getViewport().setOpaque(false);

        CardPanel card = new CardPanel(new BorderLayout(0, 6));
        card.setBorder(new EmptyBorder(8, 12, 10, 12));
        card.setPreferredSize(new Dimension(480, 120));
        card.add(captionRow, BorderLayout.NORTH);
        card.add(messageScroll, BorderLayout.CENTER);
        return card;
    }

    public void setPostOrderChangeListener(PostOrderChangeListener listener) {
        this.postOrderChangeListener = listener;
    }

    public void setMessageChangeListener(MessageChangeListener listener) {
        this.messageChangeListener = listener;
    }

    private void onSingleMessageTextChanged() {
        updateMessageCount();
        if (!programmaticTextUpdate && messageChangeListener != null && singleEntryFileName != null) {
            messageChangeListener.onMessageChanged(singleEntryFileName, singleMessageArea.getText());
        }
    }

    private void setSingleMessageText(String text) {
        programmaticTextUpdate = true;
        try {
            singleMessageArea.setText(text == null ? "" : text);
        } finally {
            programmaticTextUpdate = false;
        }
    }

    /**
     * @param messagesByFile current post text for each entry, keyed by {@link ScreenshotEntry#getFileName()}
     */
    public void showEntries(List<ScreenshotEntry> entries, Map<String, String> messagesByFile) {
        multiPreviewPanel.removeAll();
        updateHeader(entries);

        if (entries == null || entries.isEmpty()) {
            imageCanvas.setImage(null);
            showImageCard(CARD_SINGLE);
            singleEntryFileName = null;
            setSingleMessageText("");
            revalidate();
            repaint();
            return;
        }

        if (entries.size() == 1) {
            ScreenshotEntry entry = entries.get(0);
            showImageCard(CARD_SINGLE);
            imageCanvas.setImage(readImage(entry));
            singleEntryFileName = entry.getFileName();
            setSingleMessageText(messagesByFile == null ? "" : messagesByFile.get(entry.getFileName()));
        } else {
            showImageCard(CARD_MULTI);
            singleEntryFileName = null;
            for (int i = 0; i < entries.size(); i++) {
                ScreenshotEntry entry = entries.get(i);
                String text = messagesByFile == null ? "" : messagesByFile.get(entry.getFileName());
                multiPreviewPanel.add(buildMultiPreviewRow(entries, entry, i, text));
                if (i < entries.size() - 1) {
                    multiPreviewPanel.add(Box.createVerticalStrut(8));
                }
            }
            multiPreviewPanel.add(Box.createVerticalGlue());
        }

        revalidate();
        repaint();
    }

    private void updateHeader(List<ScreenshotEntry> entries) {
        headerBadgeRow.removeAll();

        if (entries == null || entries.isEmpty()) {
            headerTitleLabel.setText("未選択");
            headerTitleLabel.setForeground(UiTheme.mutedText());
            headerDateLabel.setText("");
            headerBadgeRow.revalidate();
            headerBadgeRow.repaint();
            return;
        }

        headerTitleLabel.setForeground(javax.swing.UIManager.getColor("Label.foreground"));
        if (entries.size() > 1) {
            headerTitleLabel.setText(entries.size() + " 枚を選択中");
            headerDateLabel.setText("投稿順は ↑ ↓ で変更");
            headerBadgeRow.add(mutedLabel(entries.get(0).getTitle() + " ほか"));
        } else {
            ScreenshotEntry entry = entries.get(0);
            headerTitleLabel.setText(entry.getTitle().isBlank() ? entry.getFileName() : entry.getTitle());
            headerDateLabel.setText(entry.getCapturedAt() == null ? "" : entry.getCapturedAt().format(CAPTURED_AT));
            addBadge(entry.getRawTableFolder(), ResultPalette.symbolColor(entry.getRawTableFolder()));
            addBadge(entry.getLevel().isBlank() ? "" : "Lv " + entry.getLevel(), UiTheme.accent());
            addBadge(entry.getRank(), ResultPalette.rankColor(entry.getRank()));
            addBadge(entry.getClearType(), ResultPalette.lampColor(entry.getClearType()));
            if (headerBadgeRow.getComponentCount() == 0) {
                headerBadgeRow.add(mutedLabel(entry.getStateLabel()));
            }
        }

        headerBadgeRow.revalidate();
        headerBadgeRow.repaint();
    }

    private void addBadge(String text, java.awt.Color color) {
        if (text == null || text.isBlank()) {
            return;
        }
        headerBadgeRow.add(Badge.label(text, color));
    }

    private JLabel mutedLabel(String text) {
        JLabel label = new JLabel(text == null ? "" : text);
        label.setForeground(UiTheme.mutedText());
        return label;
    }

    private void updateMessageCount() {
        updateWeightedCountLabel(singleMessageCountLabel, singleMessageArea.getText());
    }

    private static void updateWeightedCountLabel(JLabel label, String text) {
        int weighted = weightedLength(text);
        label.setText(weighted + " / " + TWEET_WEIGHTED_LIMIT);
        label.setForeground(weighted > TWEET_WEIGHTED_LIMIT
                ? new java.awt.Color(0xE8484F)
                : UiTheme.mutedText());
    }

    /** Twitter counts CJK code points as two units, so mirror that here. */
    private static int weightedLength(String text) {
        if (text == null) {
            return 0;
        }
        int total = 0;
        for (int i = 0; i < text.length(); ) {
            int codePoint = text.codePointAt(i);
            total += isWide(codePoint) ? 2 : 1;
            i += Character.charCount(codePoint);
        }
        return total;
    }

    private static boolean isWide(int codePoint) {
        return (codePoint >= 0x1100 && codePoint <= 0x11FF)
                || (codePoint >= 0x2E80 && codePoint <= 0xA4CF)
                || (codePoint >= 0xAC00 && codePoint <= 0xD7A3)
                || (codePoint >= 0xF900 && codePoint <= 0xFAFF)
                || (codePoint >= 0xFE30 && codePoint <= 0xFE4F)
                || (codePoint >= 0xFF00 && codePoint <= 0xFF60)
                || (codePoint >= 0xFFE0 && codePoint <= 0xFFE6)
                || (codePoint >= 0x20000 && codePoint <= 0x3FFFD);
    }

    private JPanel buildMultiPreviewRow(List<ScreenshotEntry> entries, ScreenshotEntry entry, int index, String text) {
        CardPanel row = new CardPanel(new BorderLayout(8, 0));
        row.setBorder(new EmptyBorder(6, 8, 6, 8));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JPanel controls = new JPanel();
        controls.setOpaque(false);
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        JLabel orderLabel = Badge.label(String.valueOf(index + 1), UiTheme.accent());
        orderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        controls.add(orderLabel);
        controls.add(Box.createVerticalStrut(6));

        controls.add(orderButton("↑", index > 0, () -> {
            if (postOrderChangeListener != null) {
                postOrderChangeListener.onMoveUp(index);
            }
        }));
        controls.add(Box.createVerticalStrut(2));
        controls.add(orderButton("↓", index < entries.size() - 1, () -> {
            if (postOrderChangeListener != null) {
                postOrderChangeListener.onMoveDown(index);
            }
        }));

        JLabel thumb = new JLabel(loadScaledImage(entry, MULTI_PREVIEW_WIDTH, 900));
        thumb.setToolTipText(entry.getFileName());

        JPanel content = new JPanel(new BorderLayout(0, 4));
        content.setOpaque(false);
        content.add(thumb, BorderLayout.NORTH);
        content.add(buildRowMessageArea(entry.getFileName(), text), BorderLayout.CENTER);

        row.add(controls, BorderLayout.WEST);
        row.add(content, BorderLayout.CENTER);
        return row;
    }

    /**
     * Text is set via the constructor, before the listener below is attached, so seeding
     * it here never reports back as a user edit.
     */
    private JPanel buildRowMessageArea(String fileName, String text) {
        JTextArea entryMessageArea = new JTextArea(text == null ? "" : text, 2, 30);
        entryMessageArea.setLineWrap(true);
        entryMessageArea.setWrapStyleWord(true);
        entryMessageArea.setOpaque(false);
        entryMessageArea.setBorder(new EmptyBorder(4, 4, 4, 4));

        JLabel countLabel = new JLabel();
        countLabel.setForeground(UiTheme.mutedText());
        updateWeightedCountLabel(countLabel, entryMessageArea.getText());

        entryMessageArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                fire();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                fire();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                fire();
            }

            private void fire() {
                updateWeightedCountLabel(countLabel, entryMessageArea.getText());
                if (messageChangeListener != null) {
                    messageChangeListener.onMessageChanged(fileName, entryMessageArea.getText());
                }
            }
        });

        JScrollPane messageScroll = new JScrollPane(entryMessageArea);
        messageScroll.setBorder(new EmptyBorder(0, 0, 0, 0));
        messageScroll.setOpaque(false);
        messageScroll.getViewport().setOpaque(false);
        messageScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        messageScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel countRow = new JPanel(new BorderLayout());
        countRow.setOpaque(false);
        countRow.add(countLabel, BorderLayout.EAST);

        JPanel panel = new JPanel(new BorderLayout(0, 2));
        panel.setOpaque(false);
        panel.add(messageScroll, BorderLayout.CENTER);
        panel.add(countRow, BorderLayout.SOUTH);
        return panel;
    }

    private JButton orderButton(String text, boolean enabled, Runnable action) {
        JButton button = new JButton(text);
        button.setMargin(new java.awt.Insets(0, 4, 0, 4));
        button.setEnabled(enabled);
        button.setFocusable(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.putClientProperty("JButton.buttonType", "roundRect");
        button.addActionListener(e -> action.run());
        return button;
    }

    private void showImageCard(String card) {
        CardLayout layout = (CardLayout) imageCards.getLayout();
        layout.show(imageCards, card);
    }

    private static BufferedImage readImage(ScreenshotEntry entry) {
        try {
            return ImageIO.read(entry.getFilePath().toFile());
        } catch (IOException e) {
            return null;
        }
    }

    private ImageIconWrapper loadScaledImage(ScreenshotEntry entry, int maxWidth, int maxHeight) {
        try {
            BufferedImage original = ImageIO.read(entry.getFilePath().toFile());
            if (original == null) {
                return null;
            }
            double scale = Math.min(
                    (double) maxWidth / original.getWidth(),
                    (double) maxHeight / original.getHeight()
            );
            int width = Math.max(1, (int) (original.getWidth() * scale));
            int height = Math.max(1, (int) (original.getHeight() * scale));
            BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = scaled.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setClip(new RoundRectangle2D.Float(0, 0, width, height, 10, 10));
            graphics.drawImage(original, 0, 0, width, height, null);
            graphics.dispose();
            return new ImageIconWrapper(scaled);
        } catch (IOException e) {
            return null;
        }
    }

    private static final class ImageIconWrapper extends javax.swing.ImageIcon {
        ImageIconWrapper(Image image) {
            super(image);
        }
    }

    /** Paints one screenshot scaled to fit the available space, never enlarged beyond 1:1. */
    private static final class ImageCanvas extends javax.swing.JComponent {
        private BufferedImage image;

        private void setImage(BufferedImage image) {
            this.image = image;
            repaint();
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (image == null) {
                    paintPlaceholder(g2);
                    return;
                }
                double scale = Math.min(1.0, Math.min(
                        (double) getWidth() / image.getWidth(),
                        (double) getHeight() / image.getHeight()));
                int width = Math.max(1, (int) (image.getWidth() * scale));
                int height = Math.max(1, (int) (image.getHeight() * scale));
                int x = (getWidth() - width) / 2;
                int y = (getHeight() - height) / 2;
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.setClip(new RoundRectangle2D.Float(x, y, width, height, 10, 10));
                g2.drawImage(image, x, y, width, height, null);
            } finally {
                g2.dispose();
            }
        }

        private void paintPlaceholder(Graphics2D g2) {
            String text = "画像を選択してください";
            g2.setColor(UiTheme.mutedText());
            g2.setFont(getFont());
            java.awt.FontMetrics metrics = g2.getFontMetrics();
            g2.drawString(text, (getWidth() - metrics.stringWidth(text)) / 2, getHeight() / 2);
        }
    }
}
