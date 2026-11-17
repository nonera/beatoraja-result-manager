package com.beatoraja.screenshot.ui;

import com.beatoraja.screenshot.service.TweetTextLimits;
import com.beatoraja.screenshot.ui.theme.UiTheme;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

/**
 * The last stop before posting: shows the exact text of each post, editable, and returns what
 * the user confirmed. Returns null when they cancel.
 */
public final class PostConfirmDialog {

    private static final Color OVER_LIMIT = new Color(0xE8484F);

    private PostConfirmDialog() {
    }

    /**
     * X posts. Each text shows a live length counter, and confirming is refused - keeping what
     * was typed - until every post fits the limit.
     */
    public static List<String> confirmTweets(Component parent, List<String> initialTexts, int abbreviatedCount) {
        List<String> texts = new ArrayList<>(initialTexts);
        String note = abbreviatedCount > 0
                ? "※文字数制限のため、" + abbreviatedCount + " 件の曲名を自動的に省略しています。"
                : null;
        while (true) {
            List<String> edited = show(parent, texts, note, "件目のツイート", "この内容で投稿しますか？", true);
            if (edited == null) {
                return null;
            }
            texts = edited;
            List<String> overLimit = new ArrayList<>();
            for (int i = 0; i < texts.size(); i++) {
                int weighted = TweetTextLimits.weightedLength(texts.get(i));
                if (weighted > TweetTextLimits.WEIGHTED_LIMIT) {
                    overLimit.add((i + 1) + "件目: " + weighted + " / " + TweetTextLimits.WEIGHTED_LIMIT + " 文字");
                }
            }
            if (overLimit.isEmpty()) {
                return texts;
            }
            note = null; // already shown once; don't repeat it on every retry
            JOptionPane.showMessageDialog(parent,
                    "文字数制限を超えています。修正してください。\n\n" + String.join("\n", overLimit),
                    "文字数オーバー", JOptionPane.WARNING_MESSAGE);
        }
    }

    /** Discord messages: no practical length limit, so a single pass. */
    public static List<String> confirmDiscord(Component parent, List<String> texts) {
        return show(parent, texts, null, "件目の送信", "この内容で送信しますか？", false);
    }

    private static List<String> show(Component parent, List<String> texts, String note, String itemLabel,
            String title, boolean countLength) {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        if (note != null) {
            content.add(new JLabel(note));
            content.add(Box.createVerticalStrut(8));
        }

        List<JTextArea> areas = new ArrayList<>();
        for (int i = 0; i < texts.size(); i++) {
            if (texts.size() > 1) {
                content.add(new JLabel((i + 1) + " " + itemLabel));
            }
            JTextArea area = new JTextArea(texts.get(i), 6, 40);
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            areas.add(area);
            content.add(new JScrollPane(area));
            if (countLength) {
                JLabel counter = new JLabel();
                updateCounter(counter, area.getText());
                area.getDocument().addDocumentListener((ChangeListener) () -> updateCounter(counter, area.getText()));
                content.add(counter);
            }
            content.add(Box.createVerticalStrut(8));
        }

        JScrollPane outer = new JScrollPane(content);
        outer.setPreferredSize(new Dimension(460, 360));
        int result = JOptionPane.showConfirmDialog(parent, outer, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return null;
        }
        List<String> edited = new ArrayList<>();
        for (JTextArea area : areas) {
            edited.add(area.getText());
        }
        return edited;
    }

    private static void updateCounter(JLabel label, String text) {
        int weighted = TweetTextLimits.weightedLength(text);
        label.setText(weighted + " / " + TweetTextLimits.WEIGHTED_LIMIT);
        label.setForeground(weighted > TweetTextLimits.WEIGHTED_LIMIT ? OVER_LIMIT : UiTheme.mutedText());
    }

    @FunctionalInterface
    private interface ChangeListener extends DocumentListener {
        void onChange();

        @Override
        default void insertUpdate(DocumentEvent e) {
            onChange();
        }

        @Override
        default void removeUpdate(DocumentEvent e) {
            onChange();
        }

        @Override
        default void changedUpdate(DocumentEvent e) {
            onChange();
        }
    }
}
