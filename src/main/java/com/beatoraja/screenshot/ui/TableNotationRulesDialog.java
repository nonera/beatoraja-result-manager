package com.beatoraja.screenshot.ui;

import com.beatoraja.screenshot.config.AppConfig;
import com.beatoraja.screenshot.table.DifficultyTableRegistry.TableInfo;
import com.beatoraja.screenshot.util.AppIcons;
import com.beatoraja.screenshot.table.NotationPrefixResolver;

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
 * Per-difficulty-table notation rules. Each loaded table gets one row for its
 * symbol override, even when folder names switch symbol partway through levels
 * (e.g. "★10" then "★★1") — the .bmt tag prefix is replaced as a whole.
 */
public class TableNotationRulesDialog extends JDialog {

    private final List<TableInfo> knownTables;

    private final DefaultListModel<TableInfo> includeModel = new DefaultListModel<>();
    private final DefaultListModel<TableInfo> excludeModel = new DefaultListModel<>();
    private final JList<TableInfo> includeList = new JList<>(includeModel);
    private final JList<TableInfo> excludeList = new JList<>(excludeModel);

    private final DefaultTableModel symbolTableModel = new DefaultTableModel(
            new Object[]{"難易度表", "接頭辞", "変更後の例", "変更後の記号"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 3;
        }
    };
    private final JTable symbolTable = new JTable(symbolTableModel);
    private final List<String> rowTableTags = new ArrayList<>();

    private List<AppConfig.TableNotationRule> result;
    private final int loadedFileCount;
    private final List<String> knownTags;
    private final Map<String, String> tablePrefixByTag;
    private final Map<String, List<String>> allNotationsByTag = new LinkedHashMap<>();

    public TableNotationRulesDialog(Frame owner, List<AppConfig.TableNotationRule> savedRules,
            List<TableInfo> knownTables, Map<String, Map<String, List<String>>> notationsByTagAndSymbol,
            int loadedFileCount, List<String> knownTags, Map<String, String> tablePrefixByTag) {
        super(owner, "難易度表ごとの投稿表記ルール", true);
        AppIcons.applyTo(this);
        this.knownTables = knownTables;
        this.loadedFileCount = loadedFileCount;
        this.knownTags = knownTags == null ? List.of() : List.copyOf(knownTags);
        this.tablePrefixByTag = tablePrefixByTag == null ? Map.of() : Map.copyOf(tablePrefixByTag);

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
            List<String> allNotations = collectNotations(bySymbol);
            allNotationsByTag.put(info.tag(), allNotations);

            String tablePrefix = tablePrefixByTag.getOrDefault(info.tag(), "");
            if (tablePrefix.isBlank() && !bySymbol.isEmpty()) {
                tablePrefix = bySymbol.keySet().iterator().next();
            }
            String override = loadTableOverride(overrides);
            String preview = formatPreview(allNotations, override, info.tag());
            addRow(label, tablePrefix, preview, override, info.tag());
        }

        buildUi();
        setSize(880, 480);
        setLocationRelativeTo(owner);
    }

    private static List<String> collectNotations(Map<String, List<String>> bySymbol) {
        LinkedHashSet<String> notations = new LinkedHashSet<>();
        for (List<String> group : bySymbol.values()) {
            notations.addAll(group);
        }
        return new ArrayList<>(notations);
    }

    private static String loadTableOverride(Map<String, String> overrides) {
        String tableWide = overrides.getOrDefault(AppConfig.TableNotationRule.TABLE_WIDE_OVERRIDE_KEY, "").trim();
        if (!tableWide.isBlank()) {
            return tableWide;
        }
        List<String> distinct = overrides.values().stream()
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .distinct()
                .toList();
        return distinct.size() == 1 ? distinct.get(0) : "";
    }

    private void addRow(String label, String prefix, String preview, String override, String tag) {
        symbolTableModel.addRow(new Object[]{label, prefix, preview, override});
        rowTableTags.add(tag);
    }

    private String formatPreview(List<String> notations, String override, String tableTag) {
        if (notations.isEmpty()) {
            return "";
        }
        List<String> samples = sampleNotations(notations);
        if (override.isBlank()) {
            return String.join(", ", samples) + (notations.size() > samples.size() ? " ..." : "");
        }
        String tablePrefix = tablePrefixByTag.getOrDefault(tableTag, "");
        String examples = samples.stream()
                .map(notation -> {
                    String prefix = !tablePrefix.isBlank() && notation.startsWith(tablePrefix)
                            ? tablePrefix
                            : NotationPrefixResolver.resolveReplaceablePrefix(notations, knownTags, "");
                    return NotationPrefixResolver.applySymbolOverride(notation, prefix, override);
                })
                .collect(Collectors.joining(", "));
        return examples + (notations.size() > samples.size() ? " ..." : "");
    }

    private static List<String> sampleNotations(List<String> notations) {
        List<String> samples = new ArrayList<>();
        samples.add(notations.get(0));
        if (notations.size() > 1) {
            samples.add(notations.get(notations.size() / 2));
        }
        if (notations.size() > 2) {
            samples.add(notations.get(notations.size() - 1));
        }
        for (String notation : notations) {
            if (samples.size() >= 4) {
                break;
            }
            if (!samples.contains(notation)) {
                samples.add(notation);
            }
        }
        return samples.stream().limit(4).toList();
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        JPanel header = new JPanel(new BorderLayout());
        header.add(new JLabel(
                "<html>左で難易度表を「表示する(常に投稿表記に含める)」「しない」に振り分けてください。"
                        + "ダブルクリックか矢印ボタンで移動できます。<br>"
                        + "右は難易度表ごとに1行ずつ、接頭辞の置き換えを設定できます（★と★★が混在する表も1行です）。</html>"),
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

        Map<String, String> overridesByTag = new LinkedHashMap<>();
        for (int i = 0; i < rowTableTags.size(); i++) {
            String tag = rowTableTags.get(i);
            String override = String.valueOf(symbolTableModel.getValueAt(i, 3)).trim();
            String tablePrefix = String.valueOf(symbolTableModel.getValueAt(i, 1)).trim();
            if (override.isBlank() || override.equals(tablePrefix)) {
                continue;
            }
            overridesByTag.put(tag, override);
        }

        List<AppConfig.TableNotationRule> rules = new ArrayList<>();
        for (TableInfo info : knownTables) {
            boolean isAlwaysInclude = alwaysInclude.contains(info.tag());
            String override = overridesByTag.get(info.tag());
            if (!isAlwaysInclude && (override == null || override.isBlank())) {
                continue;
            }
            AppConfig.TableNotationRule configRule = new AppConfig.TableNotationRule();
            configRule.setTableTag(info.tag());
            configRule.setAlwaysInclude(isAlwaysInclude);
            if (override != null && !override.isBlank()) {
                Map<String, String> overrides = new LinkedHashMap<>();
                overrides.put(AppConfig.TableNotationRule.TABLE_WIDE_OVERRIDE_KEY, override);
                configRule.setSymbolOverrides(overrides);
            }
            rules.add(configRule);
        }
        return rules;
    }

    public List<AppConfig.TableNotationRule> showDialog() {
        setVisible(true);
        return result;
    }
}
