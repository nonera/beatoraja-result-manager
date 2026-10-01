package com.beatoraja.screenshot.ui;

import com.beatoraja.screenshot.config.PostTextFormat;
import com.beatoraja.screenshot.model.ScreenshotEntry;
import com.beatoraja.screenshot.player.PlayScore;
import com.beatoraja.screenshot.service.TweetTextGenerator;
import com.beatoraja.screenshot.util.AppIcons;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lets the user choose which items make up the auto-generated post text, in what order,
 * with what text before/after each, and rename DJ RANKs. Shows a live sample preview.
 */
public class PostTextFormatDialog extends JDialog {

    private static final ScreenshotEntry SAMPLE_ENTRY = new ScreenshotEntry(
            Path.of("sample.png"), "sample.png", null, "Result",
            "サンプル曲 [ANOTHER]", "", "12", "HARD CLEAR", "AA");
    /** 1500 notes, EX 2620 / 3000 (87.33%, AA), BP 45. */
    private static final PlayScore SAMPLE_SCORE = new PlayScore(
            900, 300, 120, 100, 30, 20, 10, 5, 15, 10, 3, 2, 1500);
    private static final String SAMPLE_NOTATION = "★12";

    private final ItemTableModel tableModel = new ItemTableModel();
    private final JTable table = new JTable(tableModel);
    private final Map<String, JTextField> rankFields = new LinkedHashMap<>();
    private final JLabel previewLabel = new JLabel();
    private PostTextFormat result;

    public PostTextFormatDialog(Frame owner, PostTextFormat current) {
        super(owner, "投稿文の書式", true);
        AppIcons.applyTo(this);
        buildUi();
        load((current == null ? PostTextFormat.defaults() : current).normalized());
        setSize(560, 560);
        setLocationRelativeTo(owner);
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        root.add(new JLabel("<html>チェックした項目を上から順にスペース区切りで並べます。"
                + "BP・スコアレート・EXスコア・区切りからの差分はプレーデータ (scoredatalog.db) と"
                + "照合できたスクショにだけ付きます。</html>"), BorderLayout.NORTH);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setMaxWidth(48);
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        JPanel itemsPanel = new JPanel(new BorderLayout(4, 4));
        itemsPanel.setBorder(BorderFactory.createTitledBorder("項目"));
        itemsPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel moveButtons = new JPanel();
        moveButtons.setLayout(new BoxLayout(moveButtons, BoxLayout.Y_AXIS));
        moveButtons.add(button("上へ", () -> moveBy(-1)));
        moveButtons.add(button("下へ", () -> moveBy(1)));
        itemsPanel.add(moveButtons, BorderLayout.EAST);

        JPanel rankPanel = new JPanel(new GridLayout(0, 6, 4, 4));
        rankPanel.setBorder(BorderFactory.createTitledBorder("ランク表示名 (空欄はそのまま。差分表記の区切り名にも使います)"));
        for (String key : PlayScore.RANK_KEYS) {
            JTextField field = new JTextField();
            field.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    updatePreview();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    updatePreview();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    updatePreview();
                }
            });
            rankFields.put(key, field);
            rankPanel.add(new JLabel(key, JLabel.RIGHT));
            rankPanel.add(field);
        }

        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.setBorder(BorderFactory.createTitledBorder("プレビュー (サンプル)"));
        previewPanel.add(previewLabel, BorderLayout.CENTER);

        JPanel south = new JPanel();
        south.setLayout(new BoxLayout(south, BoxLayout.Y_AXIS));
        south.add(rankPanel);
        south.add(previewPanel);

        JPanel center = new JPanel(new BorderLayout(4, 4));
        center.add(itemsPanel, BorderLayout.CENTER);
        center.add(south, BorderLayout.SOUTH);
        root.add(center, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton reset = new JButton("初期値に戻す");
        reset.addActionListener(e -> load(PostTextFormat.defaults()));
        JButton ok = new JButton("OK");
        ok.addActionListener(e -> {
            stopEditing();
            result = buildFormat();
            dispose();
        });
        JButton cancel = new JButton("キャンセル");
        cancel.addActionListener(e -> dispose());
        actions.add(reset);
        actions.add(ok);
        actions.add(cancel);
        root.add(actions, BorderLayout.SOUTH);

        tableModel.addTableModelListener(e -> updatePreview());
        setContentPane(root);
    }

    private void load(PostTextFormat format) {
        stopEditing();
        tableModel.setItems(format.getItems());
        for (Map.Entry<String, JTextField> entry : rankFields.entrySet()) {
            entry.getValue().setText(format.getRankLabels().getOrDefault(entry.getKey(), ""));
        }
        updatePreview();
    }

    private PostTextFormat buildFormat() {
        PostTextFormat format = new PostTextFormat();
        format.setItems(tableModel.items);
        Map<String, String> labels = new LinkedHashMap<>();
        for (Map.Entry<String, JTextField> entry : rankFields.entrySet()) {
            String text = entry.getValue().getText();
            if (text != null && !text.isBlank()) {
                labels.put(entry.getKey(), text.trim());
            }
        }
        format.setRankLabels(labels);
        return format.normalized();
    }

    private void updatePreview() {
        String text = TweetTextGenerator.generate(SAMPLE_ENTRY, SAMPLE_NOTATION, SAMPLE_SCORE, buildFormat());
        previewLabel.setText(text);
    }

    private void stopEditing() {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
    }

    private JButton button(String label, Runnable action) {
        JButton button = new JButton(label);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.addActionListener(e -> action.run());
        return button;
    }

    private void moveBy(int direction) {
        stopEditing();
        int index = table.getSelectedRow();
        int target = index + direction;
        if (index < 0 || target < 0 || target >= tableModel.getRowCount()) {
            return;
        }
        tableModel.move(index, target);
        table.setRowSelectionInterval(target, target);
    }

    public PostTextFormat showDialog() {
        setVisible(true);
        return result;
    }

    private static final class ItemTableModel extends AbstractTableModel {
        private static final String[] COLUMNS = {"表示", "項目", "前に付ける文字", "後に付ける文字"};
        private final List<PostTextFormat.Item> items = new ArrayList<>();

        void setItems(List<PostTextFormat.Item> newItems) {
            items.clear();
            for (PostTextFormat.Item item : newItems) {
                items.add(item.copy());
            }
            fireTableDataChanged();
        }

        void move(int from, int to) {
            items.add(to, items.remove(from));
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return items.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMNS.length;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMNS[column];
        }

        @Override
        public Class<?> getColumnClass(int column) {
            return column == 0 ? Boolean.class : String.class;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return column != 1;
        }

        @Override
        public Object getValueAt(int row, int column) {
            PostTextFormat.Item item = items.get(row);
            return switch (column) {
                case 0 -> item.isEnabled();
                case 1 -> PostTextFormat.ITEM_DISPLAY_NAMES.getOrDefault(item.getKey(), item.getKey());
                case 2 -> item.getPrefix();
                default -> item.getSuffix();
            };
        }

        @Override
        public void setValueAt(Object value, int row, int column) {
            PostTextFormat.Item item = items.get(row);
            switch (column) {
                case 0 -> item.setEnabled(Boolean.TRUE.equals(value));
                case 2 -> item.setPrefix(value == null ? "" : value.toString());
                case 3 -> item.setSuffix(value == null ? "" : value.toString());
                default -> {
                    return;
                }
            }
            fireTableRowsUpdated(row, row);
        }
    }
}
