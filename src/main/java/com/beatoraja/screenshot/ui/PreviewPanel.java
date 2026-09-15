package com.beatoraja.screenshot.ui;

import com.beatoraja.screenshot.model.ScreenshotEntry;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Shows the currently selected screenshot(s) and lets the user edit the post text.
 * With multiple images, each image owns its own text field (keyed by file name in
 * {@link MessageChangeListener} callbacks) instead of one shared block of text, so
 * reordering or deleting a line can never desynchronize a caption from its image.
 * With a single image, that same per-image field sits directly under it - there is
 * no separate shared text box.
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
    private static final int MULTI_PREVIEW_WIDTH = 440;

    private PostOrderChangeListener postOrderChangeListener;
    private MessageChangeListener messageChangeListener;
    private boolean programmaticTextUpdate;
    private String singleEntryFileName;

    private final JLabel imageLabel = new JLabel("画像を選択してください", JLabel.CENTER);
    private final JTextArea singleMessageArea = new JTextArea(3, 40);
    private final JPanel multiPreviewPanel = new JPanel();
    private final JScrollPane multiScrollPane = new JScrollPane(multiPreviewPanel);
    private final JPanel cards = new JPanel(new CardLayout());

    public PreviewPanel() {
        setLayout(new BorderLayout());

        JScrollPane imageScrollPane = new JScrollPane(imageLabel);
        imageScrollPane.setPreferredSize(new Dimension(480, 380));

        singleMessageArea.setLineWrap(false);
        singleMessageArea.setRows(3);
        singleMessageArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                notifySingleMessageChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                notifySingleMessageChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                notifySingleMessageChanged();
            }
        });
        JScrollPane singleMessageScroll = new JScrollPane(singleMessageArea);
        singleMessageScroll.setPreferredSize(new Dimension(480, 96));

        JPanel singleCard = new JPanel(new BorderLayout(0, 8));
        singleCard.add(imageScrollPane, BorderLayout.CENTER);
        singleCard.add(singleMessageScroll, BorderLayout.SOUTH);

        multiPreviewPanel.setLayout(new BoxLayout(multiPreviewPanel, BoxLayout.Y_AXIS));
        multiScrollPane.setPreferredSize(new Dimension(480, 480));
        multiScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        multiScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        cards.add(singleCard, CARD_SINGLE);
        cards.add(multiScrollPane, CARD_MULTI);
        add(cards, BorderLayout.CENTER);
    }

    public void setPostOrderChangeListener(PostOrderChangeListener listener) {
        this.postOrderChangeListener = listener;
    }

    public void setMessageChangeListener(MessageChangeListener listener) {
        this.messageChangeListener = listener;
    }

    private void notifySingleMessageChanged() {
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

        if (entries == null || entries.isEmpty()) {
            imageLabel.setIcon(null);
            imageLabel.setText("画像を選択してください");
            showCard(CARD_SINGLE);
            singleEntryFileName = null;
            setSingleMessageText("");
            revalidate();
            repaint();
            return;
        }

        if (entries.size() == 1) {
            ScreenshotEntry entry = entries.get(0);
            showCard(CARD_SINGLE);
            imageLabel.setText("");
            imageLabel.setIcon(loadScaledImage(entry, 900, 500));
            singleEntryFileName = entry.getFileName();
            setSingleMessageText(messagesByFile == null ? "" : messagesByFile.get(entry.getFileName()));
        } else {
            showCard(CARD_MULTI);
            singleEntryFileName = null;
            JLabel orderHint = new JLabel("投稿順 (↑↓で変更)。各画像の投稿文はその画像専用で、投稿時に順番どおり結合されます。");
            orderHint.setAlignmentX(Component.LEFT_ALIGNMENT);
            multiPreviewPanel.add(orderHint);
            multiPreviewPanel.add(Box.createVerticalStrut(4));

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

    private JPanel buildMultiPreviewRow(List<ScreenshotEntry> entries, ScreenshotEntry entry, int index, String text) {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        JLabel orderLabel = new JLabel((index + 1) + ".");
        orderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        controls.add(orderLabel);

        JButton upButton = new JButton("↑");
        upButton.setMargin(new java.awt.Insets(0, 4, 0, 4));
        upButton.setEnabled(index > 0);
        upButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        upButton.addActionListener(e -> {
            if (postOrderChangeListener != null) {
                postOrderChangeListener.onMoveUp(index);
            }
        });
        controls.add(upButton);

        JButton downButton = new JButton("↓");
        downButton.setMargin(new java.awt.Insets(0, 4, 0, 4));
        downButton.setEnabled(index < entries.size() - 1);
        downButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        downButton.addActionListener(e -> {
            if (postOrderChangeListener != null) {
                postOrderChangeListener.onMoveDown(index);
            }
        });
        controls.add(downButton);

        JLabel thumb = new JLabel(loadScaledImage(entry, MULTI_PREVIEW_WIDTH, 900));
        thumb.setToolTipText(entry.getFileName());

        // Text is set via the constructor, before the listener below is attached,
        // so seeding it here never reports back as a user edit.
        JTextArea entryMessageArea = new JTextArea(text == null ? "" : text, 2, 30);
        entryMessageArea.setLineWrap(true);
        entryMessageArea.setWrapStyleWord(true);
        String fileName = entry.getFileName();
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
                if (messageChangeListener != null) {
                    messageChangeListener.onMessageChanged(fileName, entryMessageArea.getText());
                }
            }
        });
        JScrollPane entryMessageScroll = new JScrollPane(entryMessageArea);
        entryMessageScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        entryMessageScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel content = new JPanel(new BorderLayout(0, 4));
        content.add(thumb, BorderLayout.NORTH);
        content.add(entryMessageScroll, BorderLayout.CENTER);

        row.add(controls, BorderLayout.WEST);
        row.add(content, BorderLayout.CENTER);
        return row;
    }

    private void showCard(String card) {
        CardLayout layout = (CardLayout) cards.getLayout();
        layout.show(cards, card);
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
            BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = scaled.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
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
}
