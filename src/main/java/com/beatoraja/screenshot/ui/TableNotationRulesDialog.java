package com.beatoraja.screenshot.ui;

import com.beatoraja.screenshot.config.AppConfig;
import com.beatoraja.screenshot.table.DifficultyTableRegistry.TableInfo;

import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Per-difficulty-table notation rules. Most tables use one symbol for every
 * level (e.g. "sl1".."sl12" all share "sl"), so renaming is usually just one
 * row per table. Some tables switch symbol partway through their levels
 * (e.g. "A1".."A9" then "AA1".."AA9"), so each distinct raw symbol found in a
 * table gets its own editable row instead of forcing one substitution for the
 * whole table.
 *
 * The left side is a two-column shuttle: tables move between "表示する" (always
 * include this table's notation in the default post notation) and "しない" so
 * the whole set is visible at a glance instead of toggling a checkbox one
 * table at a time. The right side lists every table/symbol's rename field at
 * once (no per-table selection needed) since there are normally few enough to
 * just show them all.
 */
public class TableNotationRulesDialog extends JDialog {

    private final List<TableInfo> knownTables;

    private final DefaultListModel<TableInfo> includeModel = new DefaultListModel<>();
    private final DefaultListModel<TableInfo> excludeModel = new DefaultListModel<>();
    private final JList<TableInfo> includeList = new JList<>(includeModel);
    private final JList<TableInfo> excludeList = new JList<>(excludeModel);

    private final DefaultTableModel symbolTableModel = new DefaultTableModel(
            new Object[]{"難易度表", "記号", "表記例", "変更後の記号"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 3;
        }
    };
    private final JTable symbolTable = new JTable(symbolTableModel);
    private final List<String[]> rowKeys = new ArrayList<>(); // parallel to symbolTableModel rows: {tableTag, symbol}

    private List<AppConfig.TableNotationRule> result;
    private final int loadedFileCount;

    public TableNotationRulesDialog(Frame owner, List<AppConfig.TableNotationRule> savedRules,
            List<TableInfo> knownTables, Map<String, Map<String, List<String>>> notationsByTagAndSymbol,
            int loadedFileCount) {
        super(owner, "難易度表ごとの投稿表記ルール", true);
        this.knownTables = knownTables;
        this.loadedFileCount = loadedFileCount;

        Set<String> alwaysIncludeTags = new LinkedHashSet<>();
        Map<String, Map<String, String>> symbolOverridesByTag = new LinkedHashMap<>();
        for (AppConfig.TableNotationRule rule : savedRules) {
            if (rule.isAlwaysInclude()) {
                alwaysIncludeTags.add(rule.getTableTag());
            }
            if (!rule.getSymbolOverrides().isEmpty()) {
                symbolOverridesByTag.put(rule.getTableTag(), rule.getSymbolOverrides());
            }
        }

        for (TableInfo info : knownTables) {
            if (alwaysIncludeTags.contains(info.tag())) {
                includeModel.addElement(info);
            } else {
                excludeModel.addElement(info);
            }

            String label = info.name().isBlank() ? info.tag() : info.name();
            Map<String, String> overrides = symbolOverridesByTag.getOrDefault(info.tag(), Map.of());
            Map<String, List<String>> bySymbol = notationsByTagAndSymbol.getOrDefault(info.tag(), Map.of());

            if (bySymbol.isEmpty()) {
                addRow(label, "", "", overrides.getOrDefault("", ""), info.tag(), "");
                continue;
            }
            for (Map.Entry<String, List<String>> entry : bySymbol.entrySet()) {
                String symbol = entry.getKey();
                List<String> notations = entry.getValue();
                String preview = notations.stream().limit(4).collect(Collectors.joining(", "))
                        + (notations.size() > 4 ? " ..." : "");
                addRow(label, symbol, preview, overrides.getOrDefault(symbol, ""), info.tag(), symbol);
            }
        }

        buildUi();
        setSize(880, 480);
        setLocationRelativeTo(owner);
    }

    private void addRow(String label, String symbol, String preview, String override, String tag, String symbolKey) {
        symbolTableModel.addRow(new Object[]{label, symbol, preview, override});
        rowKeys.add(new String[]{tag, symbolKey});
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        JPanel header = new JPanel(new BorderLayout());
        header.add(new JLabel(
                "<html>左で難易度表を「表示する(常に投稿表記に含める)」「しない」に振り分けてください。"
                        + "ダブルクリックか矢印ボタンで移動できます。<br>"
                        + "右は難易度表(記号が途中で変わる表は記号ごとに複数行)の記号変更を一覧編集できます。</html>"),
                BorderLayout.NORTH);
        String countText = "難易度表: " + knownTables.size() + " 件"
                + (loadedFileCount != knownTables.size()
                        ? "（読み込んだ.bmtファイル " + loadedFileCount + " 件を同名/同タグでまとめた結果）"
                        : "");
        header.add(new JLabel(countText), BorderLayout.SOUTH);
        root.add(header, BorderLayout.NORTH);

        DefaultListCellRenderer renderer = new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> jList, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                TableInfo info = (TableInfo) value;
                String text = info.name().isBlank() ? info.tag() : info.name();
                return super.getListCellRendererComponent(jList, text, index, isSelected, cellHasFocus);
            }
        };
        includeList.setCellRenderer(renderer);
        excludeList.setCellRenderer(renderer);
        includeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        excludeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        includeList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    moveSelected(includeList, includeModel, excludeModel);
                }
            }
        });
        excludeList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    moveSelected(excludeList, excludeModel, includeModel);
                }
            }
        });

        JPanel shuttleButtons = new JPanel();
        shuttleButtons.setLayout(new BoxLayout(shuttleButtons, BoxLayout.Y_AXIS));
        shuttleButtons.add(arrowButton("→", () -> moveSelected(excludeList, excludeModel, includeModel)));
        shuttleButtons.add(arrowButton("←", () -> moveSelected(includeList, includeModel, excludeModel)));

        JPanel left = new JPanel(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.fill = java.awt.GridBagConstraints.BOTH;
        gbc.weighty = 1;
        gbc.gridy = 0;

        gbc.gridx = 0;
        gbc.weightx = 1;
        left.add(labeledList("表示する", includeList), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0;
        gbc.fill = java.awt.GridBagConstraints.VERTICAL;
        left.add(shuttleButtons, gbc);

        gbc.gridx = 2;
        gbc.weightx = 1;
        gbc.fill = java.awt.GridBagConstraints.BOTH;
        left.add(labeledList("しない", excludeList), gbc);

        JPanel right = new JPanel(new BorderLayout());
        right.add(new JScrollPane(symbolTable), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setResizeWeight(0.4);
        root.add(split, BorderLayout.CENTER);

        JPanel actions = new JPanel();
        JButton ok = new JButton("OK");
        ok.addActionListener(e -> {
            if (symbolTable.isEditing()) {
                symbolTable.getCellEditor().stopCellEditing();
            }
            result = buildResult();
            dispose();
        });
        JButton cancel = new JButton("キャンセル");
        cancel.addActionListener(e -> dispose());
        actions.add(ok);
        actions.add(cancel);
        root.add(actions, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel labeledList(String title, JList<TableInfo> list) {
        JPanel panel = new JPanel(new BorderLayout(2, 2));
        panel.add(new JLabel(title), BorderLayout.NORTH);
        panel.add(new JScrollPane(list), BorderLayout.CENTER);
        return panel;
    }

    private JButton arrowButton(String label, Runnable action) {
        JButton button = new JButton(label);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.addActionListener(e -> action.run());
        return button;
    }

    private void moveSelected(JList<TableInfo> sourceList, DefaultListModel<TableInfo> sourceModel,
            DefaultListModel<TableInfo> targetModel) {
        TableInfo selected = sourceList.getSelectedValue();
        if (selected == null) {
            return;
        }
        sourceModel.removeElement(selected);
        targetModel.addElement(selected);
    }

    private List<AppConfig.TableNotationRule> buildResult() {
        Set<String> alwaysInclude = new LinkedHashSet<>();
        for (int i = 0; i < includeModel.size(); i++) {
            alwaysInclude.add(includeModel.get(i).tag());
        }

        Map<String, Map<String, String>> overridesByTag = new LinkedHashMap<>();
        for (int i = 0; i < rowKeys.size(); i++) {
            String tag = rowKeys.get(i)[0];
            String symbol = rowKeys.get(i)[1];
            String override = String.valueOf(symbolTableModel.getValueAt(i, 3)).trim();
            if (symbol.isBlank() || override.isBlank() || override.equals(symbol)) {
                continue;
            }
            overridesByTag.computeIfAbsent(tag, ignored -> new LinkedHashMap<>()).put(symbol, override);
        }

        List<AppConfig.TableNotationRule> rules = new ArrayList<>();
        for (TableInfo info : knownTables) {
            boolean isAlwaysInclude = alwaysInclude.contains(info.tag());
            Map<String, String> overrides = overridesByTag.getOrDefault(info.tag(), Map.of());
            if (!isAlwaysInclude && overrides.isEmpty()) {
                continue;
            }
            AppConfig.TableNotationRule configRule = new AppConfig.TableNotationRule();
            configRule.setTableTag(info.tag());
            configRule.setAlwaysInclude(isAlwaysInclude);
            configRule.setSymbolOverrides(new LinkedHashMap<>(overrides));
            rules.add(configRule);
        }
        return rules;
    }

    public List<AppConfig.TableNotationRule> showDialog() {
        setVisible(true);
        return result;
    }
}
