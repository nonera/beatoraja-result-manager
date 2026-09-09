package com.beatoraja.screenshot.ui;

import com.beatoraja.screenshot.model.ScreenshotEntry;

import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import javax.swing.JSplitPane;

public class PreviewPanel extends JPanel {

    private final JLabel imageLabel = new JLabel("画像を選択してください", JLabel.CENTER);
    private final JPanel multiPreviewPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
    private final JTextArea messageArea = new JTextArea(3, 40);
    private final JScrollPane imageScrollPane = new JScrollPane(imageLabel);

    public PreviewPanel() {
        setLayout(new BorderLayout());

        imageScrollPane.setPreferredSize(new Dimension(480, 400));
        multiPreviewPanel.setVisible(false);

        JPanel imageContainer = new JPanel(new BorderLayout());
        imageContainer.add(imageScrollPane, BorderLayout.CENTER);
        imageContainer.add(multiPreviewPanel, BorderLayout.SOUTH);

        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setRows(3);
        JScrollPane messageScroll = new JScrollPane(messageArea);
        messageScroll.setPreferredSize(new Dimension(480, 72));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, imageContainer, messageScroll);
        splitPane.setResizeWeight(0.82);
        splitPane.setContinuousLayout(true);
        add(splitPane, BorderLayout.CENTER);
    }

    public void showEntries(List<ScreenshotEntry> entries, String message) {
        messageArea.setText(message == null ? "" : message);
        multiPreviewPanel.removeAll();

        if (entries == null || entries.isEmpty()) {
            imageLabel.setIcon(null);
            imageLabel.setText("画像を選択してください");
            imageScrollPane.setVisible(true);
            multiPreviewPanel.setVisible(false);
            revalidate();
            repaint();
            return;
        }

        if (entries.size() == 1) {
            imageScrollPane.setVisible(true);
            multiPreviewPanel.setVisible(false);
            imageLabel.setText("");
            imageLabel.setIcon(loadScaledImage(entries.get(0), 900, 500));
        } else {
            imageScrollPane.setVisible(false);
            multiPreviewPanel.setVisible(true);
            for (ScreenshotEntry entry : entries) {
                multiPreviewPanel.add(new JLabel(loadScaledImage(entry, 240, 135)));
            }
        }

        revalidate();
        repaint();
    }

    public String getMessage() {
        return messageArea.getText();
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
