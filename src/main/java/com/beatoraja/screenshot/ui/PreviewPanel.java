package com.beatoraja.screenshot.ui;

import com.beatoraja.screenshot.model.ScreenshotEntry;
import com.beatoraja.screenshot.ui.theme.Badge;
import com.beatoraja.screenshot.ui.theme.CardPanel;
import com.beatoraja.screenshot.ui.theme.ResultPalette;
import com.beatoraja.screenshot.ui.theme.UiTheme;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Shows the currently selected screenshot(s): a scrollable list of full-size thumbnail
 * previews (read-only - no reordering there, since tall rows make the overall order hard
 * to read at a glance), and below it a compact, single-line-per-row order list that is
 * what you actually drag to reorder. Final caption wording is reviewed and fixed in one
 * place, the merged-text confirmation dialog shown right before posting.
 */
public class PreviewPanel extends JPanel {

    public interface PostOrderChangeListener {
        void onMoveUp(int index);

        void onMoveDown(int index);

        /** Fired after a row is dropped following a drag; {@code fromIndex} may equal {@code toIndex}. */
        void onReorder(int fromIndex, int toIndex);
    }

    private static final int PREVIEW_WIDTH = 420;
    private static final DateTimeFormatter CAPTURED_AT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
    private static final int AUTO_SCROLL_EDGE = 30;
    private static final int AUTO_SCROLL_STEP = 16;
    private static final int ORDER_ROW_HEIGHT = 34;

    private PostOrderChangeListener postOrderChangeListener;

    private final JLabel headerTitleLabel = new JLabel();
    private final JLabel headerDateLabel = new JLabel();
    private final JPanel headerBadgeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
    private final JPanel previewListPanel = new JPanel();
    private final JScrollPane previewScrollPane = new JScrollPane(previewListPanel);
    private final JPanel orderListPanel = new JPanel();
    private final JScrollPane orderScrollPane = new JScrollPane(orderListPanel);
    private final List<JPanel> orderRows = new ArrayList<>();
    private int dragSourceIndex = -1;
    private int autoScrollDirection;
    private final Timer autoScrollTimer = new Timer(30, e -> performAutoScroll());

    public PreviewPanel() {
        setLayout(new BorderLayout(0, 8));
        setBorder(new EmptyBorder(8, 8, 8, 8));

        add(buildHeaderCard(), BorderLayout.NORTH);

        previewListPanel.setLayout(new BoxLayout(previewListPanel, BoxLayout.Y_AXIS));
        previewListPanel.setOpaque(false);
        previewScrollPane.setPreferredSize(new Dimension(480, 340));
        // Preferred size doubles as the layout's minimum unless overridden, which would
        // otherwise stop the split pane from shrinking this below its initial size.
        previewScrollPane.setMinimumSize(new Dimension(200, 120));
        previewScrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        previewScrollPane.setOpaque(false);
        previewScrollPane.getViewport().setOpaque(false);
        previewScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        previewScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        orderListPanel.setLayout(new BoxLayout(orderListPanel, BoxLayout.Y_AXIS));
        orderListPanel.setOpaque(false);
        orderScrollPane.setPreferredSize(new Dimension(480, 160));
        orderScrollPane.setMinimumSize(new Dimension(200, 90));
        orderScrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        orderScrollPane.setOpaque(false);
        orderScrollPane.getViewport().setOpaque(false);
        orderScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        orderScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JLabel orderCaption = new JLabel("投稿順（ドラッグで並び替え）");
        orderCaption.setForeground(UiTheme.mutedText());

        CardPanel orderCard = new CardPanel(new BorderLayout(0, 4));
        orderCard.setBorder(new EmptyBorder(6, 10, 8, 10));
        orderCard.add(orderCaption, BorderLayout.NORTH);
        orderCard.add(orderScrollPane, BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(previewScrollPane, BorderLayout.CENTER);
        center.add(orderCard, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);
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

    public void setPostOrderChangeListener(PostOrderChangeListener listener) {
        this.postOrderChangeListener = listener;
    }

    /**
     * @param messagesByFile current post text for each entry, keyed by {@link ScreenshotEntry#getFileName()}
     */
    public void showEntries(List<ScreenshotEntry> entries, Map<String, String> messagesByFile) {
        previewListPanel.removeAll();
        orderListPanel.removeAll();
        updateHeader(entries);
        orderRows.clear();
        dragSourceIndex = -1;
        autoScrollTimer.stop();

        if (entries == null || entries.isEmpty()) {
            revalidate();
            repaint();
            return;
        }

        for (int i = 0; i < entries.size(); i++) {
            ScreenshotEntry entry = entries.get(i);
            String text = messagesByFile == null ? "" : messagesByFile.get(entry.getFileName());
            previewListPanel.add(buildPreviewRow(entry, i, text));
            if (i < entries.size() - 1) {
                previewListPanel.add(Box.createVerticalStrut(8));
            }

            JPanel orderRow = buildOrderRow(entries, entry, i);
            orderRows.add(orderRow);
            orderListPanel.add(orderRow);
        }
        previewListPanel.add(Box.createVerticalGlue());

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
            headerDateLabel.setText("");
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

    /** A read-only thumbnail + caption row; reordering happens in the compact order list below. */
    private JPanel buildPreviewRow(ScreenshotEntry entry, int index, String text) {
        CardPanel row = new CardPanel(new BorderLayout(8, 0));
        row.setBorder(new EmptyBorder(6, 8, 6, 8));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JLabel orderLabel = Badge.label(String.valueOf(index + 1), UiTheme.accent());
        JPanel controls = new JPanel();
        controls.setOpaque(false);
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        controls.add(orderLabel);

        JLabel thumb = new JLabel(loadScaledImage(entry, PREVIEW_WIDTH, 260));
        thumb.setToolTipText(entry.getFileName());

        JLabel captionPreview = new JLabel(previewText(text));
        captionPreview.setForeground(UiTheme.mutedText());

        JPanel content = new JPanel(new BorderLayout(0, 4));
        content.setOpaque(false);
        content.add(thumb, BorderLayout.NORTH);
        content.add(captionPreview, BorderLayout.CENTER);

        row.add(controls, BorderLayout.WEST);
        row.add(content, BorderLayout.CENTER);
        return row;
    }

    /**
     * A read-only, single-line snippet of the caption - the final wording is fixed in the
     * confirmation dialog shown right before posting, not here.
     */
    private static String previewText(String text) {
        if (text == null || text.isBlank()) {
            return "(投稿文なし)";
        }
        String preview = text.replace('\n', ' ');
        return preview.length() > 40 ? preview.substring(0, 40) + "…" : preview;
    }

    /** One compact, single-line row in the order list: number, drag handle, title, ↑↓. */
    private JPanel buildOrderRow(List<ScreenshotEntry> entries, ScreenshotEntry entry, int index) {
        CardPanel row = new CardPanel(new BorderLayout(8, 0));
        row.setBorder(new EmptyBorder(2, 8, 2, 8));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, ORDER_ROW_HEIGHT));
        row.setPreferredSize(new Dimension(row.getPreferredSize().width, ORDER_ROW_HEIGHT));

        JPanel leading = new JPanel();
        leading.setOpaque(false);
        leading.setLayout(new BoxLayout(leading, BoxLayout.X_AXIS));
        leading.add(Badge.label(String.valueOf(index + 1), UiTheme.accent()));
        leading.add(Box.createHorizontalStrut(8));
        leading.add(buildDragHandle(index));

        JLabel titleLabel = new JLabel(entry.getTitle().isBlank() ? entry.getFileName() : entry.getTitle());

        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        buttons.add(orderButton("↑", index > 0, () -> {
            if (postOrderChangeListener != null) {
                postOrderChangeListener.onMoveUp(index);
            }
        }));
        buttons.add(Box.createHorizontalStrut(2));
        buttons.add(orderButton("↓", index < entries.size() - 1, () -> {
            if (postOrderChangeListener != null) {
                postOrderChangeListener.onMoveDown(index);
            }
        }));

        row.add(leading, BorderLayout.WEST);
        row.add(titleLabel, BorderLayout.CENTER);
        row.add(buttons, BorderLayout.EAST);
        return row;
    }

    /** A drag-to-reorder handle; the ↑↓ buttons remain for keyboard/accessibility use. */
    private JLabel buildDragHandle(int index) {
        JLabel handle = new JLabel("≡");
        handle.setAlignmentX(Component.CENTER_ALIGNMENT);
        handle.setForeground(UiTheme.mutedText());
        handle.setToolTipText("ドラッグして並び替え");
        handle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        handle.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragSourceIndex = index;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                int from = dragSourceIndex;
                dragSourceIndex = -1;
                autoScrollTimer.stop();
                clearDragHighlight();
                if (from < 0) {
                    return;
                }
                int to = rowIndexAt(SwingUtilities.convertPoint(handle, e.getPoint(), orderListPanel));
                if (to >= 0 && to != from && postOrderChangeListener != null) {
                    postOrderChangeListener.onReorder(from, to);
                }
            }
        });
        handle.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragSourceIndex < 0) {
                    return;
                }
                Point pointInList = SwingUtilities.convertPoint(handle, e.getPoint(), orderListPanel);
                highlightRowAt(rowIndexAt(pointInList));
                updateAutoScroll(SwingUtilities.convertPoint(handle, e.getPoint(), orderScrollPane.getViewport()));
            }
        });
        return handle;
    }

    /** Starts/stops auto-scrolling the order list when a drag is held near the viewport's top/bottom edge. */
    private void updateAutoScroll(Point pointInViewport) {
        int viewportHeight = orderScrollPane.getViewport().getHeight();
        if (pointInViewport.y < AUTO_SCROLL_EDGE) {
            autoScrollDirection = -1;
        } else if (pointInViewport.y > viewportHeight - AUTO_SCROLL_EDGE) {
            autoScrollDirection = 1;
        } else {
            autoScrollTimer.stop();
            return;
        }
        if (!autoScrollTimer.isRunning()) {
            autoScrollTimer.start();
        }
    }

    private void performAutoScroll() {
        if (dragSourceIndex < 0) {
            autoScrollTimer.stop();
            return;
        }
        JScrollBar bar = orderScrollPane.getVerticalScrollBar();
        bar.setValue(bar.getValue() + autoScrollDirection * AUTO_SCROLL_STEP);
        Point mousePosition = orderScrollPane.getMousePosition(true);
        if (mousePosition != null) {
            Point pointerInList = SwingUtilities.convertPoint(orderScrollPane, mousePosition, orderListPanel);
            highlightRowAt(rowIndexAt(pointerInList));
        }
    }

    /** Which row (by original index) the given point, in {@link #orderListPanel} coordinates, is over. */
    private int rowIndexAt(Point pointInOrderList) {
        for (int i = 0; i < orderRows.size(); i++) {
            Rectangle bounds = orderRows.get(i).getBounds();
            if (pointInOrderList.y < bounds.y + bounds.height / 2) {
                return i;
            }
        }
        return Math.max(0, orderRows.size() - 1);
    }

    private void highlightRowAt(int targetIndex) {
        for (int i = 0; i < orderRows.size(); i++) {
            orderRows.get(i).setBorder(i == targetIndex
                    ? BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(2, 0, 0, 0, UiTheme.accent()),
                            new EmptyBorder(0, 8, 2, 8))
                    : new EmptyBorder(2, 8, 2, 8));
        }
    }

    private void clearDragHighlight() {
        for (JPanel row : orderRows) {
            row.setBorder(new EmptyBorder(2, 8, 2, 8));
        }
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
}
