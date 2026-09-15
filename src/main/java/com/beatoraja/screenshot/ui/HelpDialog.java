package com.beatoraja.screenshot.ui;

import com.beatoraja.screenshot.util.AppVersion;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

public final class HelpDialog {

    private static final String REPOSITORY_URL = "https://github.com/nonera/beatoraja-result-manager";

    private HelpDialog() {
    }

    public static void showUsage(Component parent) {
        String text = """
                beatoraja Screenshot Manager の基本的な使い方

                1. 一覧からスクショを選択（Ctrl / Shift で複数選択）
                2. プレビューで投稿文を確認
                   複数枚のとき ↑↓ で投稿順を変更できます
                3. Twitter に投稿 … 最大4枚/ツイート。5枚以上は別ツイートとして順番に投稿
                   （ブラウザで「投稿」を押してください）
                4. Discord に送信 … 最大10枚/回。11枚以上は分割送信

                その他:
                ・★列でフラグ付け。「選択」メニューからフラグ付きを一括選択
                ・「設定」でスクショフォルダ、Discord Webhook、Twitter ログインなど
                ・設定は %APPDATA%\\beatoraja-screenshot-manager\\ に保存されます
                """;

        JTextArea area = createReadOnlyTextArea(text, 18, 52);
        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.setPreferredSize(new Dimension(520, 360));

        JOptionPane.showMessageDialog(parent, scrollPane, "使い方", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showAbout(Component parent) {
        String version = AppVersion.get();
        String text = """
                beatoraja Screenshot Manager v%s

                beatoraja の F6 スクショを管理し、
                Twitter / Discord へ投稿する Windows 向けアプリです。

                %s
                """.formatted(version, REPOSITORY_URL);

        JTextArea area = createReadOnlyTextArea(text, 8, 46);
        area.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        JDialog dialog = new JDialog(JOptionPane.getFrameForComponent(parent), "バージョン情報", true);
        dialog.setLayout(new BorderLayout(8, 8));
        dialog.add(area, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        javax.swing.JButton repositoryButton = new javax.swing.JButton("GitHub を開く");
        repositoryButton.addActionListener(e -> openRepository(parent));
        javax.swing.JButton closeButton = new javax.swing.JButton("閉じる");
        closeButton.addActionListener(e -> dialog.dispose());
        buttons.add(repositoryButton);
        buttons.add(closeButton);
        dialog.add(buttons, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    public static void openLogFolder(Component parent) {
        try {
            Path logDir = com.beatoraja.screenshot.util.AppPaths.logsDir();
            Files.createDirectories(logDir);
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(logDir.toFile());
            } else {
                JOptionPane.showMessageDialog(parent, logDir.toAbsolutePath(), "ログフォルダ",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parent, "ログフォルダを開けませんでした: " + e.getMessage(),
                    "エラー", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void openRepository(Component parent) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create(REPOSITORY_URL));
            } else {
                JOptionPane.showMessageDialog(parent, REPOSITORY_URL, "GitHub リポジトリ",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(parent, "ブラウザを開けませんでした: " + e.getMessage(),
                    "エラー", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static JTextArea createReadOnlyTextArea(String text, int rows, int columns) {
        JTextArea area = new JTextArea(text, rows, columns);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setCaretPosition(0);
        area.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        return area;
    }
}
