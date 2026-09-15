package com.beatoraja.screenshot.ui;

import com.beatoraja.screenshot.table.DifficultyTableRegistry.TableInfo;
import com.beatoraja.screenshot.util.AppIcons;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.TransferHandler;
import javax.swing.BoxLayout;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Lets the user reorder difficulty table tags by preference. When a chart is listed
 * on multiple loaded tables, the symbol/notation from the highest-priority table wins.
 * Supports drag-and-drop reordering as well as move-up/down/top/bottom buttons.
 */
public class TablePriorityDialog extends JDialog {

    private final DefaultListModel<String> model = new DefaultListModel<>();
    private final JList<String> list = new JList<>(model);
    private List<String> result;

    public TablePriorityDialog(Frame owner, List<String> savedOrder, List<TableInfo> knownTables,
            java.util.Map<String, String> symbolOverrideByTag, int loadedFileCount) {
        super(owner, "難易度表の優先順位", true);
        AppIcons.applyTo(this);
        java.util.Map<String, String> namesByTag = new java.util.LinkedHashMap<>();
        List<String> knownTags = new ArrayList<>();
        for (TableInfo info : knownTables) {
            namesByTag.put(info.tag(), info.name());
            knownTags.add(info.tag());
        }
        for (String tag : mergeOrder(savedOrder, knownTags)) {
            model.addElement(tag);
        }
        buildUi(namesByTag, symbolOverrideByTag == null ? java.util.Map.of() : symbolOverrideByTag,
                knownTags.size(), loadedFileCount);
        setSize(460, 460);
        setLocationRelativeTo(owner);
    }

    private static List<String> mergeOrder(List<String> savedOrder, List<String> knownTags) {
        Set<String> known = new LinkedHashSet<>(knownTags == null ? List.of() : knownTags);
        List<String> merged = new ArrayList<>();
        if (savedOrder != null) {
            for (String tag : savedOrder) {
                if (known.remove(tag)) {
                    merged.add(tag);
                }
            }
        }
        merged.addAll(known);
        return merged;
    }

    private void buildUi(java.util.Map<String, String> namesByTag, java.util.Map<String, String> symbolOverrideByTag,
            int knownTableCount, int loadedFileCount) {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        JPanel header = new JPanel(new BorderLayout());
        header.add(new JLabel("上にあるほど優先されます。同じ曲が複数の表にある場合、記号・投稿表記は上位の表が優先されます。"
                + " ドラッグ＆ドロップでも並べ替えられます。"), BorderLayout.NORTH);
        String countText = "難易度表: " + knownTableCount + " 件"
                + (loadedFileCount != knownTableCount
                        ? "（読み込んだ.bmtファイル " + loadedFileCount + " 件を同名/同タグでまとめた結果）"
                        : "");
        header.add(new JLabel(countText), BorderLayout.SOUTH);
        root.add(header, BorderLayout.NORTH);

        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> jList, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                String tag = String.valueOf(value);
                String name = namesByTag.getOrDefault(tag, tag);
                String symbolOverride = symbolOverrideByTag.get(tag);
                // The identity key is the table's own name, so only show it combined
                // with a symbol when that symbol is an actual override (otherwise
                // it's just the name again).
                String text = (symbolOverride != null && !symbolOverride.isBlank())
                        ? symbolOverride + "  —  " + name
                        : name;
                return super.getListCellRendererComponent(jList, text, index, isSelected, cellHasFocus);
            }
        });
        list.setDragEnabled(true);
        list.setDropMode(DropMode.INSERT);
        list.setTransferHandler(new ReorderTransferHandler());
        root.add(new JScrollPane(list), BorderLayout.CENTER);

        JPanel moveButtons = new JPanel();
        moveButtons.setLayout(new BoxLayout(moveButtons, BoxLayout.Y_AXIS));
        moveButtons.add(button("最上位へ", () -> moveTo(0)));
        moveButtons.add(button("上へ", () -> moveBy(-1)));
        moveButtons.add(button("下へ", () -> moveBy(1)));
        moveButtons.add(button("最下位へ", () -> moveTo(model.size() - 1)));
        root.add(moveButtons, BorderLayout.EAST);

        JPanel actions = new JPanel();
        JButton ok = new JButton("OK");
        ok.addActionListener(e -> {
            result = new ArrayList<>();
            for (int i = 0; i < model.size(); i++) {
                result.add(model.get(i));
            }
            dispose();
        });
        JButton cancel = new JButton("キャンセル");
        cancel.addActionListener(e -> dispose());
        actions.add(ok);
        actions.add(cancel);
        root.add(actions, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JButton button(String label, Runnable action) {
        JButton button = new JButton(label);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.addActionListener(e -> action.run());
        return button;
    }

    private void moveBy(int direction) {
        int index = list.getSelectedIndex();
        int target = index + direction;
        if (index < 0 || target < 0 || target >= model.size()) {
            return;
        }
        String value = model.remove(index);
        model.add(target, value);
        list.setSelectedIndex(target);
    }

    private void moveTo(int target) {
        int index = list.getSelectedIndex();
        if (index < 0 || target < 0 || target >= model.size() || target == index) {
            return;
        }
        String value = model.remove(index);
        model.add(target, value);
        list.setSelectedIndex(target);
    }

    public List<String> showDialog() {
        setVisible(true);
        return result;
    }

    /** Reorders {@link #model} via drag-and-drop within the list. */
    private final class ReorderTransferHandler extends TransferHandler {
        @Override
        public int getSourceActions(javax.swing.JComponent c) {
            return MOVE;
        }

        @Override
        protected Transferable createTransferable(javax.swing.JComponent c) {
            return new StringSelection(String.valueOf(list.getSelectedIndex()));
        }

        @Override
        public boolean canImport(TransferSupport support) {
            return support.isDrop() && support.isDataFlavorSupported(DataFlavor.stringFlavor);
        }

        @Override
        public boolean importData(TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }
            try {
                JList.DropLocation dropLocation = (JList.DropLocation) support.getDropLocation();
                int dropIndex = dropLocation.getIndex();
                int fromIndex = Integer.parseInt(
                        (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor));
                if (fromIndex < 0 || fromIndex >= model.size()) {
                    return false;
                }
                if (dropIndex == fromIndex || dropIndex == fromIndex + 1) {
                    return false;
                }
                String value = model.remove(fromIndex);
                int insertAt = dropIndex > fromIndex ? dropIndex - 1 : dropIndex;
                model.add(insertAt, value);
                list.setSelectedIndex(insertAt);
                return true;
            } catch (UnsupportedFlavorException | IOException | NumberFormatException e) {
                return false;
            }
        }
    }
}
