package com.beatoraja.screenshot.ui;

import com.beatoraja.screenshot.config.AppConfig;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/** Editable rows of the Discord webhooks (name, URL) in the settings dialog. */
final class WebhookTableModel extends AbstractTableModel {
    private final String[] columns = {"名前", "Webhook URL"};
    private List<AppConfig.DiscordWebhookEntry> rows = new ArrayList<>();

    public void setRows(List<AppConfig.DiscordWebhookEntry> rows) {
        this.rows = new ArrayList<>(rows);
        fireTableDataChanged();
    }

    public List<AppConfig.DiscordWebhookEntry> getRows() {
        return new ArrayList<>(rows);
    }

    public void addRow() {
        rows.add(new AppConfig.DiscordWebhookEntry("", ""));
        fireTableRowsInserted(rows.size() - 1, rows.size() - 1);
    }

    public void removeRow(int index) {
        rows.remove(index);
        fireTableRowsDeleted(index, index);
    }

    @Override
    public int getRowCount() {
        return rows.size();
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
        return true;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        AppConfig.DiscordWebhookEntry row = rows.get(rowIndex);
        return columnIndex == 0 ? row.getName() : row.getUrl();
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        AppConfig.DiscordWebhookEntry row = rows.get(rowIndex);
        if (columnIndex == 0) {
            row.setName(String.valueOf(aValue));
        } else {
            row.setUrl(String.valueOf(aValue));
        }
        fireTableCellUpdated(rowIndex, columnIndex);
    }
}
