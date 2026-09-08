package com.beatoraja.screenshot.ui;

import com.beatoraja.screenshot.db.ScreenshotDatabase;
import com.beatoraja.screenshot.db.ScreenshotRecord;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabasePanel extends JPanel {

    public interface NotationChangeListener {
        void onNotationChanged();
    }

    private final DatabaseTableModel tableModel = new DatabaseTableModel();
    private final JTable table = new JTable(tableModel);
    private ScreenshotDatabase database;
    private NotationChangeListener notationChangeListener;

    public DatabasePanel() {
        setLayout(new BorderLayout());

        table.setAutoCreateRowSorter(true);
        table.getColumnModel().getColumn(0).setPreferredWidth(130);
        table.getColumnModel().getColumn(1).setPreferredWidth(220);
        table.getColumnModel().getColumn(2).setPreferredWidth(50);
        table.getColumnModel().getColumn(3).setPreferredWidth(60);
        table.getColumnModel().getColumn(4).setPreferredWidth(50);
        table.getColumnModel().getColumn(5).setPreferredWidth(120);
        table.getColumnModel().getColumn(6).setPreferredWidth(100);
        table.getColumnModel().getColumn(7).setPreferredWidth(90);

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    public void setNotationChangeListener(NotationChangeListener listener) {
        this.notationChangeListener = listener;
    }

    public void reload(ScreenshotDatabase database) throws SQLException {
        this.database = database;
        tableModel.setRecords(database.findAll());
    }

    public ScreenshotRecord getSelectedRecord() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        return tableModel.getRecordAt(modelRow);
    }

    private class DatabaseTableModel extends AbstractTableModel {
        private final String[] columns = {
                "日付", "タイトル", "記号", "レベル", "ランク", "ランプ", "投稿表記", "状態"
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
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 2 || columnIndex == 6;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            ScreenshotRecord record = records.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> ScreenshotDatabase.formatDate(record.capturedAt());
                case 1 -> emptyToDash(record.title());
                case 2 -> emptyToDash(record.tableSymbol());
                case 3 -> emptyToDash(record.displayLevel());
                case 4 -> emptyToDash(record.rank());
                case 5 -> emptyToDash(record.clearType());
                case 6 -> emptyToDash(record.postNotation());
                case 7 -> emptyToDash(record.stateLabel());
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
                if (columnIndex == 2) {
                    database.updateNotation(record.id(), text, record.postNotation());
                } else if (columnIndex == 6) {
                    database.updateNotation(record.id(), record.tableSymbol(), text);
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

        private String emptyToDash(String value) {
            return value == null || value.isBlank() ? "-" : value;
        }
    }
}
