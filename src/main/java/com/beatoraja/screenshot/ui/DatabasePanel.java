package com.beatoraja.screenshot.ui;

import com.beatoraja.screenshot.db.ScreenshotDatabase;
import com.beatoraja.screenshot.db.ScreenshotRecord;
import com.beatoraja.screenshot.model.ScreenshotEntry;
import com.beatoraja.screenshot.service.PostedStateStore;

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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
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

    public interface NotationChangeListener {
        void onNotationChanged();
    }

    public interface SelectionListener {
        void onSelectionChanged(List<ScreenshotEntry> selectedEntries);
    }

    private final DatabaseTableModel tableModel = new DatabaseTableModel();
    private final JTable table = new JTable(tableModel);
    private final TableRowSorter<DatabaseTableModel> sorter = new TableRowSorter<>(tableModel);
    private final MultiColumnSortSupport sortSupport = new MultiColumnSortSupport(table, sorter, 0);
    private final JComboBox<String> symbolFilterCombo = new JComboBox<>();
    private final JTextField titleSearchField = new JTextField(20);
    private final JCheckBox flaggedOnlyFilterBox = new JCheckBox("フラグのみ");
    private List<String> symbolPriorityOrder = List.of();
    private ScreenshotDatabase database;
    private NotationChangeListener notationChangeListener;
    private SelectionListener selectionListener;
    private PostedStateStore postedStateStore;

    public DatabasePanel() {
        setLayout(new BorderLayout(8, 8));

        sorter.setComparator(0, Comparator.comparing(
                (LocalDateTime dateTime) -> dateTime,
                Comparator.nullsLast(Comparator.naturalOrder())
        ));
        sorter.setComparator(3, this::compareSymbols);
        table.setRowSorter(sorter);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.getTableHeader().setDefaultRenderer(sortSupport.createHeaderRenderer());

        table.getColumnModel().getColumn(1).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setMaxWidth(48);
        table.getColumnModel().getColumn(0).setPreferredWidth(130);
        table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                if (value instanceof LocalDateTime dateTime) {
                    setText(ScreenshotDatabase.formatDate(dateTime));
                } else {
                    super.setValue(value);
                }
            }
        });
        table.getColumnModel().getColumn(2).setPreferredWidth(220);
        table.getColumnModel().getColumn(3).setPreferredWidth(50);
        table.getColumnModel().getColumn(4).setPreferredWidth(60);
        table.getColumnModel().getColumn(5).setPreferredWidth(50);
        table.getColumnModel().getColumn(6).setPreferredWidth(120);
        table.getColumnModel().getColumn(7).setPreferredWidth(100);
        table.getColumnModel().getColumn(8).setPreferredWidth(90);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && selectionListener != null) {
                selectionListener.onSelectionChanged(getSelectedEntries());
            }
        });

        symbolFilterCombo.addActionListener(e -> applyFilters());
        flaggedOnlyFilterBox.addActionListener(e -> applyFilters());
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

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filterPanel.add(new JLabel("記号:"));
        filterPanel.add(symbolFilterCombo);
        filterPanel.add(new JLabel("曲名検索:"));
        filterPanel.add(titleSearchField);
        filterPanel.add(flaggedOnlyFilterBox);

        add(filterPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
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
    }

    private class DatabaseTableModel extends AbstractTableModel {
        private final String[] columns = {
                "日付", "★", "タイトル", "記号", "レベル", "ランク", "ランプ", "投稿表記", "状態"
        };
        private List<ScreenshotRecord> records = new ArrayList<>();

        public void setRecords(List<ScreenshotRecord> records) {
            this.records = new ArrayList<>(records);
            fireTableDataChanged();
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
            if (columnIndex == 0) {
                return LocalDateTime.class;
            }
            if (columnIndex == 1) {
                return Boolean.class;
            }
            return String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 1 || columnIndex == 3 || columnIndex == 7;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            ScreenshotRecord record = records.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> record.capturedAt();
                case 1 -> record.flagged();
                case 2 -> emptyToDash(record.title());
                case 3 -> emptyToDash(record.tableSymbol());
                case 4 -> emptyToDash(record.displayLevel());
                case 5 -> emptyToDash(record.rank());
                case 6 -> emptyToDash(record.clearType());
                case 7 -> emptyToDash(record.postNotation());
                case 8 -> formatState(record);
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
                if (columnIndex == 1) {
                    boolean flagged = value instanceof Boolean bool && bool;
                    database.updateFlagged(record.id(), flagged);
                } else if (columnIndex == 3) {
                    database.updateNotation(record.id(), text, record.postNotation());
                } else if (columnIndex == 7) {
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

        private String formatState(ScreenshotRecord record) {
            StringBuilder builder = new StringBuilder();
            String stateLabel = record.stateLabel();
            if (stateLabel != null && !stateLabel.isBlank()) {
                builder.append(stateLabel);
            }
            if (postedStateStore != null) {
                PostedStateStore.PostedRecord posted = postedStateStore.get(record.fileName());
                if (posted.isTwitterPosted()) {
                    appendBadge(builder, "Twitter");
                }
                if (posted.isDiscordPosted()) {
                    appendBadge(builder, "Discord");
                }
            }
            return builder.isEmpty() ? "-" : builder.toString();
        }

        private void appendBadge(StringBuilder builder, String label) {
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append('[').append(label).append(']');
        }

        private String emptyToDash(String value) {
            return value == null || value.isBlank() ? "-" : value;
        }
    }
}
