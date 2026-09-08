package com.beatoraja.screenshot.ui;

import com.beatoraja.screenshot.db.ScreenshotRecord;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class NotationPickerDialog extends JDialog {

    public record Result(String tableSymbol, String postNotation) {
    }

    private final List<JCheckBox> checkBoxes = new ArrayList<>();
    private final JTextField customField = new JTextField(24);
    private Result result;

    public NotationPickerDialog(Frame owner, ScreenshotRecord record) {
        super(owner, "投稿表記を選択", true);
        buildUi(record);
        setSize(420, 320);
        setLocationRelativeTo(owner);
    }

    private void buildUi(ScreenshotRecord record) {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.add(new JLabel("候補から選ぶか、投稿表記を直接入力してください。"), BorderLayout.NORTH);

        JPanel checks = new JPanel(new GridLayout(0, 1, 4, 4));
        Set<String> options = new LinkedHashSet<>();
        if (record.availableNotations() != null) {
            options.addAll(record.availableNotations());
        }
        if (!record.postNotation().isBlank()) {
            options.add(record.postNotation());
        }
        if (options.isEmpty()) {
            checks.add(new JLabel("候補がありません。下の欄に直接入力してください。"));
        } else {
            for (String option : options) {
                JCheckBox box = new JCheckBox(option, record.postNotation().equals(option));
                checkBoxes.add(box);
                checks.add(box);
            }
        }
        root.add(checks, BorderLayout.CENTER);

        JPanel customPanel = new JPanel(new BorderLayout(6, 6));
        customPanel.add(new JLabel("投稿表記"), BorderLayout.NORTH);
        customField.setText(record.postNotation());
        customPanel.add(customField, BorderLayout.CENTER);
        root.add(customPanel, BorderLayout.SOUTH);

        JPanel actions = new JPanel();
        JButton ok = new JButton("OK");
        ok.addActionListener(e -> {
            result = buildResult(record);
            dispose();
        });
        JButton cancel = new JButton("キャンセル");
        cancel.addActionListener(e -> dispose());
        actions.add(ok);
        actions.add(cancel);
        root.add(actions, BorderLayout.EAST);

        setContentPane(root);
    }

    private Result buildResult(ScreenshotRecord record) {
        List<String> selected = new ArrayList<>();
        for (JCheckBox box : checkBoxes) {
            if (box.isSelected()) {
                selected.add(box.getText());
            }
        }

        String postNotation = customField.getText().trim();
        if (postNotation.isBlank() && !selected.isEmpty()) {
            postNotation = String.join("/", selected);
        }

        String tableSymbol = record.tableSymbol();
        if (!selected.isEmpty()) {
            tableSymbol = extractLeadingSymbol(selected.get(0));
        } else if (!postNotation.isBlank()) {
            tableSymbol = extractLeadingSymbol(postNotation.split("/")[0]);
        }

        return new Result(tableSymbol, postNotation);
    }

    private String extractLeadingSymbol(String notation) {
        if (notation == null || notation.isBlank()) {
            return "";
        }
        int index = 0;
        while (index < notation.length()) {
            char ch = notation.charAt(index);
            if (Character.isDigit(ch) || ch == '?') {
                break;
            }
            index++;
        }
        return notation.substring(0, index);
    }

    public Result showDialog() {
        setVisible(true);
        return result;
    }
}
