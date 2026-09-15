package com.beatoraja.screenshot.ui;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

final class MultiColumnSortSupport {

    static final class SortSpec {
        final int column;
        SortOrder order;

        SortSpec(int column, SortOrder order) {
            this.column = column;
            this.order = order;
        }
    }

    interface SortChangeListener {
        void onSortChanged();
    }

    private final JTable table;
    private final RowSorter<? extends TableModel> sorter;
    private final List<SortSpec> sortSpecs = new ArrayList<>();
    private final int defaultColumn;
    private SortChangeListener sortChangeListener;

    MultiColumnSortSupport(JTable table, RowSorter<? extends TableModel> sorter, int defaultColumn) {
        this.table = table;
        this.sorter = sorter;
        this.defaultColumn = defaultColumn;
        sortSpecs.add(new SortSpec(defaultColumn, SortOrder.DESCENDING));
        applySortKeys();

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                int column = header.columnAtPoint(event.getPoint());
                if (column < 0) {
                    return;
                }
                cycleSort(column);
            }
        });
    }

    void setSortChangeListener(SortChangeListener listener) {
        this.sortChangeListener = listener;
    }

    List<SortSpec> getSortSpecs() {
        return List.copyOf(sortSpecs);
    }

    TableCellRenderer createHeaderRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table,
                    Object value,
                    boolean isSelected,
                    boolean hasFocus,
                    int row,
                    int column
            ) {
                Component component = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column
                );
                if (!(component instanceof JLabel label) || value == null) {
                    return component;
                }
                label.setHorizontalAlignment(JLabel.CENTER);
                Optional<SortSpec> spec = findSpec(column);
                if (spec.isEmpty()) {
                    label.setText(String.valueOf(value));
                    return label;
                }
                int priority = indexOf(column) + 1;
                String arrow = spec.get().order == SortOrder.DESCENDING ? " ▼" : " ▲";
                label.setText(value + arrow + priority);
                return label;
            }
        };
    }

    private void cycleSort(int column) {
        Optional<SortSpec> existing = findSpec(column);
        if (existing.isEmpty()) {
            sortSpecs.add(new SortSpec(column, SortOrder.DESCENDING));
        } else {
            SortSpec spec = existing.get();
            if (spec.order == SortOrder.DESCENDING) {
                spec.order = SortOrder.ASCENDING;
            } else {
                sortSpecs.remove(spec);
            }
        }
        applySortKeys();
        table.getTableHeader().repaint();
        if (sortChangeListener != null) {
            sortChangeListener.onSortChanged();
        }
    }

    private void applySortKeys() {
        List<RowSorter.SortKey> keys = new ArrayList<>();
        for (SortSpec spec : sortSpecs) {
            keys.add(new RowSorter.SortKey(spec.column, spec.order));
        }
        if (keys.isEmpty()) {
            keys.add(new RowSorter.SortKey(defaultColumn, SortOrder.DESCENDING));
            sortSpecs.clear();
            sortSpecs.add(new SortSpec(defaultColumn, SortOrder.DESCENDING));
        }
        sorter.setSortKeys(keys);
    }

    private Optional<SortSpec> findSpec(int column) {
        return sortSpecs.stream().filter(spec -> spec.column == column).findFirst();
    }

    private int indexOf(int column) {
        for (int i = 0; i < sortSpecs.size(); i++) {
            if (sortSpecs.get(i).column == column) {
                return i;
            }
        }
        return -1;
    }
}
