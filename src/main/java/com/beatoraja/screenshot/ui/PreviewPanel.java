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
import javax.swing.JSplitPane;

public class PreviewPanel extends JPanel {

    public interface PostOrderChangeListener {
        void onMoveUp(int index);

        void onMoveDown(int index);
    }

    private static final String CARD_SINGLE = "single";
    private static final String CARD_MULTI = "multi";

    private PostOrderChangeListener postOrderChangeListener;

    private final JLabel imageLabel = new JLabel("画像を選択してください", JLabel.CENTER);
    private static final int MULTI_PREVIEW_WIDTH = 440;

    private final JPanel multiPreviewPanel = new JPanel();
    private final JScrollPane multiScrollPane = new JScrollPane(multiPreviewPanel);
    private final JPanel imageCards = new JPanel(new CardLayout());
    private final JTextArea messageArea = new JTextArea(3, 40);
    private final JScrollPane imageScrollPane = new JScrollPane(imageLabel);

    public PreviewPanel() {
        setLayout(new BorderLayout());

        imageScrollPane.setPreferredSize(new Dimension(480, 400));
        multiPreviewPanel.setLayout(new BoxLayout(multiPreviewPanel, BoxLayout.Y_AXIS));
        multiScrollPane.setPreferredSize(new Dimension(480, 400));
        multiScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        multiScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        imageCards.add(imageScrollPane, CARD_SINGLE);
        imageCards.add(multiScrollPane, CARD_MULTI);

        messageArea.setLineWrap(false);
        messageArea.setRows(3);
        JScrollPane messageScroll = new JScrollPane(messageArea);
        messageScroll.setPreferredSize(new Dimension(480, 96));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, imageCards, messageScroll);
        splitPane.setResizeWeight(0.75);
        splitPane.setContinuousLayout(true);
        add(splitPane, BorderLayout.CENTER);
    }

    public void setPostOrderChangeListener(PostOrderChangeListener listener) {
        this.postOrderChangeListener = listener;
    }

    public void showEntries(List<ScreenshotEntry> entries, String message) {
        messageArea.setText(message == null ? "" : message);
        multiPreviewPanel.removeAll();

        if (entries == null || entries.isEmpty()) {
            imageLabel.setIcon(null);
            imageLabel.setText("画像を選択してください");
            showImageCard(CARD_SINGLE);
            messageArea.setRows(3);
            revalidate();
            repaint();
            return;
        }

        int lineCount = Math.max(1, message == null ? 0 : message.split("\n", -1).length);
        messageArea.setRows(Math.min(Math.max(3, lineCount), 8));

        if (entries.size() == 1) {
            showImageCard(CARD_SINGLE);
            imageLabel.setText("");
            imageLabel.setIcon(loadScaledImage(entries.get(0), 900, 500));
        } else {
            showImageCard(CARD_MULTI);
            JLabel orderHint = new JLabel("投稿順 (↑↓で変更)");
            orderHint.setAlignmentX(Component.LEFT_ALIGNMENT);
            multiPreviewPanel.add(orderHint);
            multiPreviewPanel.add(Box.createVerticalStrut(4));

            for (int i = 0; i < entries.size(); i++) {
                ScreenshotEntry entry = entries.get(i);
                multiPreviewPanel.add(buildMultiPreviewRow(entries, entry, i));
                if (i < entries.size() - 1) {
                    multiPreviewPanel.add(Box.createVerticalStrut(8));
                }
            }
            multiPreviewPanel.add(Box.createVerticalGlue());
        }

        revalidate();
        repaint();
    }

    public String getMessage() {
        return messageArea.getText();
    }

    private JPanel buildMultiPreviewRow(List<ScreenshotEntry> entries, ScreenshotEntry entry, int index) {
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

        row.add(controls, BorderLayout.WEST);
        row.add(thumb, BorderLayout.CENTER);
        return row;
    }

    private void showImageCard(String card) {
        CardLayout layout = (CardLayout) imageCards.getLayout();
        layout.show(imageCards, card);
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
