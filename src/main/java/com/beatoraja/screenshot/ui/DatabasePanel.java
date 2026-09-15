package com.beatoraja.screenshot.ui;

import com.beatoraja.screenshot.config.AppConfig;
import com.beatoraja.screenshot.db.ScreenshotDatabase;
import com.beatoraja.screenshot.db.ScreenshotRecord;
import com.beatoraja.screenshot.model.ScreenshotEntry;
import com.beatoraja.screenshot.service.PostedStateStore;
import com.beatoraja.screenshot.ui.render.BadgeCellRenderer;
import com.beatoraja.screenshot.ui.render.CapturedAtCellRenderer;
import com.beatoraja.screenshot.ui.render.StateCell;
import com.beatoraja.screenshot.ui.render.StateCellRenderer;
import com.beatoraja.screenshot.ui.render.ThumbnailCache;
import com.beatoraja.screenshot.ui.render.ThumbnailCellRenderer;
import com.beatoraja.screenshot.ui.theme.Badge;
import com.beatoraja.screenshot.ui.theme.ResultPalette;
import com.beatoraja.screenshot.ui.theme.UiTheme;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.FlowLayout;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class DatabasePanel extends JPanel {

    private static final String FILTER_ALL = "すべて";

    private static final int COL_THUMBNAIL = 0;
    private static final int COL_CAPTURED_AT = 1;
    private static final int COL_FLAG = 2;
    private static final int COL_TITLE = 3;
    private static final int COL_SYMBOL = 4;
    private static final int COL_LEVEL = 5;
    private static final int COL_RANK = 6;
    private static final int COL_LAMP = 7;
    private static final int COL_NOTATION = 8;
    private static final int COL_STATE = 9;

    private static final int THUMBNAIL_WIDTH = 72;
    private static final int THUMBNAIL_HEIGHT = 40;

    public interface NotationChangeListener {
        void onNotationChanged();
    }

    public interface SelectionListener {
        void onSelectionChanged(List<ScreenshotEntry> selectedEntries);
    }

    private final DatabaseTableModel tableModel = new DatabaseTableModel();
    private final JTable table = new StripedTable(tableModel);
    private final TableRowSorter<DatabaseTableModel> sorter = new TableRowSorter<>(tableModel);
    private final MultiColumnSortSupport sortSupport =
            new MultiColumnSortSupport(table, sorter, COL_CAPTURED_AT);
    private final ThumbnailCache thumbnailCache =
            new ThumbnailCache(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, this::repaintTable);
    private final JComboBox<String> symbolFilterCombo = new JComboBox<>();
    private final JTextField titleSearchField = new JTextField(20);
    private final JCheckBox flaggedOnlyFilterBox = new JCheckBox("フラグのみ");
    private final JCheckBox thumbnailVisibleBox = new JCheckBox("サムネイル", true);
    private final JLabel rowCountLabel = new JLabel();
    private List<String> symbolPriorityOrder = List.of();
    private ScreenshotDatabase database;
    private NotationChangeListener notationChangeListener;
    private SelectionListener selectionListener;
    private PostedStateStore postedStateStore;

    public DatabasePanel() {
        setLayout(new BorderLayout(0, 0));

        sorter.setComparator(COL_CAPTURED_AT, Comparator.comparing(
                (LocalDateTime dateTime) -> dateTime,
                Comparator.nullsLast(Comparator.naturalOrder())
        ));
        sorter.setComparator(COL_SYMBOL, this::compareSymbols);
        sorter.setSortable(COL_THUMBNAIL, false);
        table.setRowSorter(sorter);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setFillsViewportHeight(true);
        table.setShowGrid(false);
        table.setRowHeight(rowHeight(true));
        table.getTableHeader().setDefaultRenderer(sortSupport.createHeaderRenderer());

        configureColumns();

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && selectionListener != null) {
                selectionListener.onSelectionChanged(getSelectedEntries());
            }
        });

        symbolFilterCombo.addActionListener(e -> applyFilters());
        flaggedOnlyFilterBox.addActionListener(e -> applyFilters());
        thumbnailVisibleBox.addActionListener(e -> applyThumbnailVisibility());
        titleSearchField.putClientProperty("JTextField.placeholderText", "曲名で絞り込み");
        titleSearchField.putClientProperty("JTextField.showClearButton", true);
        titleSearchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFilters();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFilters();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFilters();
            }
        });

        add(buildFilterBar(), BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        scrollPane.getViewport().setBackground(table.getBackground());
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel buildFilterBar() {
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        filters.setOpaque(false);
        filters.add(new JLabel("記号"));
        filters.add(symbolFilterCombo);
        filters.add(titleSearchField);
        filters.add(flaggedOnlyFilterBox);
        filters.add(thumbnailVisibleBox);

        rowCountLabel.setForeground(UiTheme.mutedText());
        JPanel count = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 6));
        count.setOpaque(false);
        count.add(rowCountLabel);

        JPanel bar = new JPanel(new BorderLayout());
        bar.setBorder(new EmptyBorder(2, 6, 2, 6));
        bar.add(filters, BorderLayout.CENTER);
        bar.add(count, BorderLayout.EAST);
        return bar;
    }

    /**
     * Widths are measured from the current font rather than hard-coded, so the columns keep
     * fitting their content when the UI font size is changed in the settings.
     */
    private void configureColumns() {
        Font base = table.getFont();
        FontMetrics plain = table.getFontMetrics(base);
        FontMetrics badge = table.getFontMetrics(base.deriveFont(Font.BOLD, base.getSize2D() - 1f));

        int thumbnail = THUMBNAIL_WIDTH + 12;
        setColumnWidth(COL_THUMBNAIL, thumbnail, thumbnail, thumbnail);

        int date = plain.stringWidth("2025/09/15 00:00") + 20;
        setColumnWidth(COL_CAPTURED_AT, date, date, date + 60);

        int flag = plain.getHeight() + 14;
        setColumnWidth(COL_FLAG, flag, flag, flag);

        setColumnWidth(COL_TITLE, scaled(240), scaled(110), Integer.MAX_VALUE);

        int symbol = Badge.width(badge, "★★★") + 12;
        setColumnWidth(COL_SYMBOL, symbol, Badge.width(badge, "★") + 12, symbol * 2);

        int level = plain.stringWidth("999") + 28;
        setColumnWidth(COL_LEVEL, level, level, level * 2);

        int rank = Badge.width(badge, "AAA") + 12;
        setColumnWidth(COL_RANK, rank, rank, rank * 2);

        setColumnWidth(COL_LAMP,
                Badge.width(badge, "EXHARD CLEAR") + 16,
                Badge.width(badge, "CLEAR") + 16,
                Badge.width(badge, "LIGHT ASSIST EASY CLEAR") + 16);

        setColumnWidth(COL_NOTATION, scaled(120), scaled(70), scaled(240));

        int stateGaps = 26;
        setColumnWidth(COL_STATE,
                Badge.width(badge, "Result") + Badge.width(badge, "X")
                        + Badge.width(badge, "Discord") + stateGaps,
                Badge.width(badge, "Result") + stateGaps,
                Badge.width(badge, "Course Result") + Badge.width(badge, "X")
                        + Badge.width(badge, "Discord") + stateGaps);

        column(COL_THUMBNAIL).setCellRenderer(new ThumbnailCellRenderer(thumbnailCache));
        column(COL_CAPTURED_AT).setCellRenderer(new CapturedAtCellRenderer());
        column(COL_TITLE).setCellRenderer(new TitleCellRenderer());
        column(COL_SYMBOL).setCellRenderer(new BadgeCellRenderer(ResultPalette::symbolColor, true));
        column(COL_LEVEL).setCellRenderer(new LevelCellRenderer());
        column(COL_RANK).setCellRenderer(new BadgeCellRenderer(ResultPalette::rankColor, true));
        column(COL_LAMP).setCellRenderer(new BadgeCellRenderer(ResultPalette::lampColor, false));
        column(COL_NOTATION).setCellRenderer(new TitleCellRenderer());
        column(COL_STATE).setCellRenderer(new StateCellRenderer());
    }

    private TableColumn column(int modelIndex) {
        return table.getColumnModel().getColumn(modelIndex);
    }

    private void setColumnWidth(int modelIndex, int preferred, int min, int max) {
        TableColumn tableColumn = column(modelIndex);
        // TableColumn clamps min against the current max (and vice versa), so drop the
        // previous bounds before applying the new ones.
        tableColumn.setMinWidth(0);
        tableColumn.setMaxWidth(Integer.MAX_VALUE);
        tableColumn.setMinWidth(min);
        tableColumn.setMaxWidth(max);
        tableColumn.setPreferredWidth(preferred);
    }

    /** Scales a width that was tuned for the default font size. */
    private static int scaled(int base) {
        return Math.round(base * UiTheme.fontSize() / (float) AppConfig.DEFAULT_UI_FONT_SIZE);
    }

    private void applyThumbnailVisibility() {
        boolean visible = thumbnailVisibleBox.isSelected();
        int width = visible ? THUMBNAIL_WIDTH + 12 : 0;
        setColumnWidth(COL_THUMBNAIL, width, width, width);
        table.setRowHeight(rowHeight(visible));
    }

    /** Rows must fit the configured UI font, and the thumbnail when that column is shown. */
    private int rowHeight(boolean withThumbnail) {
        int textHeight = table.getFontMetrics(table.getFont()).getHeight() + 10;
        return withThumbnail ? Math.max(THUMBNAIL_HEIGHT + 8, textHeight) : textHeight;
    }

    private void repaintTable() {
        table.repaint();
    }

    public void setNotationChangeListener(NotationChangeListener listener) {
        this.notationChangeListener = listener;
    }

    public void setSelectionListener(SelectionListener listener) {
        this.selectionListener = listener;
    }

    public void setPostedStateStore(PostedStateStore postedStateStore) {
        this.postedStateStore = postedStateStore;
        table.repaint();
    }

    public void setSymbolPriorityOrder(List<String> symbolPriorityOrder) {
        this.symbolPriorityOrder = symbolPriorityOrder == null ? List.of() : List.copyOf(symbolPriorityOrder);
        sorter.sort();
        updateSymbolFilterChoices();
    }

    public void reload(ScreenshotDatabase database) throws SQLException {
        this.database = database;
        tableModel.setRecords(database.findAll());
        updateSymbolFilterChoices();
    }

    public List<ScreenshotEntry> getSelectedEntries() {
        int[] viewRows = table.getSelectedRows();
        List<ScreenshotEntry> entries = new ArrayList<>(viewRows.length);
        for (int viewRow : viewRows) {
            int modelRow = table.convertRowIndexToModel(viewRow);
            entries.add(toEntry(tableModel.getRecordAt(modelRow)));
        }
        return entries;
    }

    public int getSelectedCount() {
        return table.getSelectedRowCount();
    }

    public void selectAllFlagged() {
        table.clearSelection();
        ListSelectionModel selectionModel = table.getSelectionModel();
        int selectedCount = 0;
        for (int viewRow = 0; viewRow < table.getRowCount(); viewRow++) {
            int modelRow = table.convertRowIndexToModel(viewRow);
            if (tableModel.getRecordAt(modelRow).flagged()) {
                selectionModel.addSelectionInterval(viewRow, viewRow);
                selectedCount++;
            }
        }
        if (selectionListener != null) {
            selectionListener.onSelectionChanged(getSelectedEntries());
        }
        if (selectedCount == 0) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "フラグ付きのスクショがありません。",
                    "選択",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public ScreenshotRecord getSelectedRecord() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        return tableModel.getRecordAt(modelRow);
    }

    public void shutdown() {
        thumbnailCache.shutdown();
    }

    static ScreenshotEntry toEntry(ScreenshotRecord record) {
        if (record == null) {
            return null;
        }
        return new ScreenshotEntry(
                record.filePath(),
                record.fileName(),
                record.capturedAt(),
                record.stateLabel(),
                record.title(),
                record.tableSymbol(),
                record.displayLevel(),
                record.clearType(),
                record.rank()
        );
    }

    private int compareSymbols(Object left, Object right) {
        String leftSymbol = normalizeSymbol(left);
        String rightSymbol = normalizeSymbol(right);
        int leftIndex = symbolPriorityIndex(leftSymbol);
        int rightIndex = symbolPriorityIndex(rightSymbol);
        if (leftIndex != rightIndex) {
            return Integer.compare(leftIndex, rightIndex);
        }
        return leftSymbol.compareToIgnoreCase(rightSymbol);
    }

    private int symbolPriorityIndex(String symbol) {
        if (symbol.isBlank()) {
            return Integer.MAX_VALUE;
        }
        int index = symbolPriorityOrder.indexOf(symbol);
        return index < 0 ? Integer.MAX_VALUE - 1 : index;
    }

    private static String normalizeSymbol(Object value) {
        if (value == null) {
            return "";
        }
        String text = String.valueOf(value).trim();
        return "-".equals(text) ? "" : text;
    }

    private void updateSymbolFilterChoices() {
        String current = (String) symbolFilterCombo.getSelectedItem();
        Set<String> symbolsInRecords = new LinkedHashSet<>();
        for (ScreenshotRecord record : tableModel.records) {
            if (record.tableSymbol() != null && !record.tableSymbol().isBlank()) {
                symbolsInRecords.add(record.tableSymbol());
            }
        }

        List<String> choices = new ArrayList<>();
        choices.add(FILTER_ALL);
        for (String symbol : symbolPriorityOrder) {
            if (symbolsInRecords.contains(symbol)) {
                choices.add(symbol);
            }
        }
        for (String symbol : symbolsInRecords) {
            if (!symbolPriorityOrder.contains(symbol)) {
                choices.add(symbol);
            }
        }

        symbolFilterCombo.setModel(new DefaultComboBoxModel<>(choices.toArray(new String[0])));
        if (current != null && choices.contains(current)) {
            symbolFilterCombo.setSelectedItem(current);
        } else {
            symbolFilterCombo.setSelectedItem(FILTER_ALL);
        }
        applyFilters();
    }

    private void applyFilters() {
        String selectedSymbol = (String) symbolFilterCombo.getSelectedItem();
        String titleQuery = titleSearchField.getText().trim().toLowerCase(Locale.ROOT);
        boolean symbolFilterActive = selectedSymbol != null && !FILTER_ALL.equals(selectedSymbol);
        boolean titleFilterActive = !titleQuery.isEmpty();
        boolean flaggedFilterActive = flaggedOnlyFilterBox.isSelected();

        if (!symbolFilterActive && !titleFilterActive && !flaggedFilterActive) {
            sorter.setRowFilter(null);
            updateRowCountLabel();
            return;
        }
        sorter.setRowFilter(new RowFilter<DatabaseTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DatabaseTableModel, ? extends Integer> entry) {
                int modelIndex = entry.getIdentifier();
                ScreenshotRecord record = tableModel.getRecordAt(modelIndex);
                if (flaggedFilterActive && !record.flagged()) {
                    return false;
                }
                if (symbolFilterActive && !selectedSymbol.equals(record.tableSymbol())) {
                    return false;
                }
                if (titleFilterActive) {
                    String title = record.title();
                    if (title == null || !title.toLowerCase(Locale.ROOT).contains(titleQuery)) {
                        return false;
                    }
                }
                return true;
            }
        });
        updateRowCountLabel();
    }

    private void updateRowCountLabel() {
        int shown = table.getRowCount();
        int total = tableModel.getRowCount();
        rowCountLabel.setText(shown == total
                ? total + " 件"
                : shown + " / " + total + " 件");
    }

    /** Alternating row backgrounds, applied uniformly to every renderer in the table. */
    private static final class StripedTable extends JTable {
        private StripedTable(AbstractTableModel model) {
            super(model);
        }

        @Override
        public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
            Component component = super.prepareRenderer(renderer, row, column);
            if (!isRowSelected(row)) {
                component.setBackground(row % 2 == 0 ? getBackground() : UiTheme.alternateRow());
            }
            return component;
        }
    }

    private static final class TitleCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBorder(new EmptyBorder(0, 6, 0, 6));
            String text = value == null ? "" : String.valueOf(value);
            setToolTipText("-".equals(text) || text.isBlank() ? null : text);
            if ("-".equals(text)) {
                setForeground(UiTheme.mutedAgainst(getForeground(), getBackground()));
            }
            return this;
        }
    }

    private static final class LevelCellRenderer extends DefaultTableCellRenderer {
        private LevelCellRenderer() {
            setHorizontalAlignment(CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setFont(table.getFont().deriveFont(Font.BOLD));
            if ("-".equals(String.valueOf(value))) {
                setForeground(UiTheme.mutedAgainst(getForeground(), getBackground()));
            }
            return this;
        }
    }

    private class DatabaseTableModel extends AbstractTableModel {
        private final String[] columns = {
                "", "日付", "★", "タイトル", "記号", "レベル", "ランク", "ランプ", "投稿表記", "状態"
        };
        private List<ScreenshotRecord> records = new ArrayList<>();

        public void setRecords(List<ScreenshotRecord> records) {
            this.records = new ArrayList<>(records);
            fireTableDataChanged();
            updateRowCountLabel();
        }

        public ScreenshotRecord getRecordAt(int row) {
            return records.get(row);
        }

        @Override
        public int getRowCount() {
            return records.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return switch (columnIndex) {
                case COL_THUMBNAIL -> Path.class;
                case COL_CAPTURED_AT -> LocalDateTime.class;
                case COL_FLAG -> Boolean.class;
                case COL_STATE -> StateCell.class;
                default -> String.class;
            };
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == COL_FLAG || columnIndex == COL_SYMBOL || columnIndex == COL_NOTATION;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            ScreenshotRecord record = records.get(rowIndex);
            return switch (columnIndex) {
                case COL_THUMBNAIL -> record.filePath();
                case COL_CAPTURED_AT -> record.capturedAt();
                case COL_FLAG -> record.flagged();
                case COL_TITLE -> emptyToDash(record.title());
                case COL_SYMBOL -> emptyToDash(record.tableSymbol());
                case COL_LEVEL -> emptyToDash(record.displayLevel());
                case COL_RANK -> emptyToDash(record.rank());
                case COL_LAMP -> emptyToDash(record.clearType());
                case COL_NOTATION -> emptyToDash(record.postNotation());
                case COL_STATE -> toStateCell(record);
                default -> "";
            };
        }

        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            if (database == null) {
                return;
            }
            ScreenshotRecord record = records.get(rowIndex);
            String text = value == null ? "" : String.valueOf(value).trim();

            try {
                if (columnIndex == COL_FLAG) {
                    boolean flagged = value instanceof Boolean bool && bool;
                    database.updateFlagged(record.id(), flagged);
                } else if (columnIndex == COL_SYMBOL) {
                    database.updateNotation(record.id(), text, record.postNotation());
                } else if (columnIndex == COL_NOTATION) {
                    database.updateNotation(record.id(), record.tableSymbol(), text);
                } else {
                    return;
                }
                reload(database);
                if (notationChangeListener != null) {
                    notationChangeListener.onNotationChanged();
                }
            } catch (SQLException ex) {
                javax.swing.JOptionPane.showMessageDialog(DatabasePanel.this,
                        "保存に失敗しました: " + ex.getMessage(),
                        "エラー", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }

        private StateCell toStateCell(ScreenshotRecord record) {
            if (postedStateStore == null) {
                return new StateCell(record.stateLabel(), false, false);
            }
            PostedStateStore.PostedRecord posted = postedStateStore.get(record.fileName());
            return new StateCell(record.stateLabel(), posted.isTwitterPosted(), posted.isDiscordPosted());
        }

        private String emptyToDash(String value) {
            return value == null || value.isBlank() ? "-" : value;
        }
    }
}
