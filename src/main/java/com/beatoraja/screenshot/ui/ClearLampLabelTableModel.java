package com.beatoraja.screenshot.ui;

import com.beatoraja.screenshot.config.ClearLampLabels;

import javax.swing.table.AbstractTableModel;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Fixed rows, one per canonical clear lamp type - unlike {@link WebhookTableModel} there is
 * nothing to add or remove, only each row's custom display name to edit. A blank value means
 * "use the default beatoraja name" and is dropped rather than stored.
 */
final class ClearLampLabelTableModel extends AbstractTableModel {
    private final String[] columns = {"クリアランプ", "表示名（空欄でデフォルト）", "略称（まとめ投稿用）", "まとめに載せる"};
    /** Visibility overrides; a value equal to the default is not stored. */
    private Map<String, Boolean> groupedVisibility = new LinkedHashMap<>();
    private final List<String> canonicalTypes = ClearLampLabels.CANONICAL_ORDER;
    private Map<String, String> overrides = new LinkedHashMap<>();
    /** Short-name overrides; a value equal to the default is not stored. */
    private Map<String, String> shortOverrides = new LinkedHashMap<>();

    public void setOverrides(Map<String, String> overrides) {
        this.overrides = new LinkedHashMap<>(overrides == null ? Map.of() : overrides);
        fireTableDataChanged();
    }

    public void setShortOverrides(Map<String, String> shortOverrides) {
        this.shortOverrides = new LinkedHashMap<>(shortOverrides == null ? Map.of() : shortOverrides);
        fireTableDataChanged();
    }

    public void setGroupedVisibility(Map<String, Boolean> groupedVisibility) {
        this.groupedVisibility = new LinkedHashMap<>(groupedVisibility == null ? Map.of() : groupedVisibility);
        fireTableDataChanged();
    }

    public Map<String, Boolean> getGroupedVisibility() {
        Map<String, Boolean> result = new LinkedHashMap<>();
        for (String type : canonicalTypes) {
            Boolean value = groupedVisibility.get(type);
            if (value != null) {
                result.put(type, value);
            }
        }
        return result;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnIndex == 3 ? Boolean.class : String.class;
    }

    public Map<String, String> getOverrides() {
        return nonBlank(overrides);
    }

    public Map<String, String> getShortOverrides() {
        return nonBlank(shortOverrides);
    }

    private Map<String, String> nonBlank(Map<String, String> values) {
        Map<String, String> result = new LinkedHashMap<>();
        for (String type : canonicalTypes) {
            String value = values.get(type);
            if (value != null && !value.isBlank()) {
                result.put(type, value.trim());
            }
        }
        return result;
    }

    @Override
    public int getRowCount() {
        return canonicalTypes.size();
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
        return columnIndex >= 1;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        String type = canonicalTypes.get(rowIndex);
        if (columnIndex == 0) {
            return type;
        }
        if (columnIndex == 2) {
            return ClearLampLabels.resolveShort(type, shortOverrides);
        }
        if (columnIndex == 3) {
            return ClearLampLabels.isShownInGroupedPost(type, groupedVisibility);
        }
        String value = overrides.get(type);
        return value == null ? "" : value;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex < 1) {
            return;
        }
        String type = canonicalTypes.get(rowIndex);
        if (columnIndex == 3) {
            boolean shown = Boolean.TRUE.equals(aValue);
            if (shown == ClearLampLabels.isShownInGroupedPostByDefault(type)) {
                groupedVisibility.remove(type);
            } else {
                groupedVisibility.put(type, shown);
            }
            fireTableCellUpdated(rowIndex, columnIndex);
            return;
        }
        String text = aValue == null ? "" : String.valueOf(aValue).trim();
        if (columnIndex == 2) {
            // Blank or the default itself: keep following the default.
            if (text.isBlank() || text.equals(ClearLampLabels.defaultShortName(type))) {
                shortOverrides.remove(type);
            } else {
                shortOverrides.put(type, text);
            }
        } else if (text.isBlank()) {
            overrides.remove(type);
        } else {
            overrides.put(type, text);
        }
        fireTableCellUpdated(rowIndex, columnIndex);
    }
}
