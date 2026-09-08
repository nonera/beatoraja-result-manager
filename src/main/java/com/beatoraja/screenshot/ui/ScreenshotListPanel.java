package com.beatoraja.screenshot.ui;

import com.beatoraja.screenshot.config.AppConfig;
import com.beatoraja.screenshot.model.ScreenshotEntry;
import com.beatoraja.screenshot.service.PostedStateStore;

import javax.imageio.ImageIO;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScreenshotListPanel extends JPanel {

    public interface SelectionListener {
        void onSelectionChanged(List<ScreenshotEntry> selectedEntries);
    }

    private final JList<ScreenshotEntry> list = new JList<>();
    private final ScreenshotListModel listModel = new ScreenshotListModel();
    private final Map<Path, ImageIcon> thumbnailCache = new HashMap<>();
    private SelectionListener selectionListener;
    private PostedStateStore postedStateStore;

    public ScreenshotListPanel() {
        setLayout(new BorderLayout());
        list.setModel(listModel);
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.setCellRenderer(new ScreenshotCellRenderer());
        list.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && selectionListener != null) {
                selectionListener.onSelectionChanged(getSelectedEntries());
            }
        });
        add(new JScrollPane(list), BorderLayout.CENTER);
    }

    public void setSelectionListener(SelectionListener listener) {
        this.selectionListener = listener;
    }

    public void setPostedStateStore(PostedStateStore postedStateStore) {
        this.postedStateStore = postedStateStore;
        list.repaint();
    }

    public void setEntries(List<ScreenshotEntry> entries) {
        listModel.setEntries(entries);
        list.repaint();
    }

    public void addEntry(ScreenshotEntry entry) {
        listModel.addEntry(entry);
        list.repaint();
    }

    public List<ScreenshotEntry> getSelectedEntries() {
        return list.getSelectedValuesList();
    }

    public int getSelectedCount() {
        return list.getSelectedIndices().length;
    }

    private class ScreenshotListModel extends javax.swing.AbstractListModel<ScreenshotEntry> {
        private final List<ScreenshotEntry> entries = new ArrayList<>();

        public void setEntries(List<ScreenshotEntry> newEntries) {
            entries.clear();
            entries.addAll(newEntries);
            fireContentsChanged(this, 0, Math.max(entries.size() - 1, 0));
        }

        public void addEntry(ScreenshotEntry entry) {
            if (entries.contains(entry)) {
                return;
            }
            entries.add(0, entry);
            fireIntervalAdded(this, 0, 0);
        }

        @Override
        public int getSize() {
            return entries.size();
        }

        @Override
        public ScreenshotEntry getElementAt(int index) {
            return entries.get(index);
        }
    }

    private class ScreenshotCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus
        ) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (!(value instanceof ScreenshotEntry entry)) {
                return label;
            }

            label.setIcon(loadThumbnail(entry.getFilePath()));
            label.setText(buildLabelText(entry));
            label.setPreferredSize(new Dimension(280, 72));
            label.setIconTextGap(8);
            return label;
        }

        private String buildLabelText(ScreenshotEntry entry) {
            StringBuilder builder = new StringBuilder("<html>");
            builder.append(escapeHtml(entry.getFileName()));
            if (postedStateStore != null) {
                PostedStateStore.PostedRecord record = postedStateStore.get(entry.getFileName());
                builder.append("<br><small>");
                if (record.isTwitterPosted()) {
                    builder.append("[Twitter]");
                }
                if (record.isDiscordPosted()) {
                    builder.append("[Discord]");
                }
                builder.append("</small>");
            }
            builder.append("</html>");
            return builder.toString();
        }

        private String escapeHtml(String text) {
            return text.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;");
        }

        private ImageIcon loadThumbnail(Path path) {
            if (thumbnailCache.containsKey(path)) {
                return thumbnailCache.get(path);
            }
            try {
                BufferedImage original = ImageIO.read(path.toFile());
                if (original == null) {
                    return null;
                }
                Image scaled = original.getScaledInstance(96, 54, Image.SCALE_SMOOTH);
                BufferedImage thumbnail = new BufferedImage(96, 54, BufferedImage.TYPE_INT_RGB);
                Graphics2D graphics = thumbnail.createGraphics();
                graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                graphics.drawImage(scaled, 0, 0, null);
                graphics.dispose();
                ImageIcon icon = new ImageIcon(thumbnail);
                thumbnailCache.put(path, icon);
                return icon;
            } catch (IOException e) {
                return null;
            }
        }
    }
}
