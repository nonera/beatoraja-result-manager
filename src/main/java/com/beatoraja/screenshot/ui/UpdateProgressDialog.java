package com.beatoraja.screenshot.ui;

import com.beatoraja.screenshot.util.AppIcons;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;

public class UpdateProgressDialog extends JDialog {

    private final JLabel statusLabel = new JLabel(" ");
    private final JProgressBar progressBar = new JProgressBar();

    public UpdateProgressDialog() {
        super((java.awt.Frame) null, "beatoraja Screenshot Manager", ModalityType.APPLICATION_MODAL);
        AppIcons.applyTo(this);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        buildUi();
        setSize(460, 130);
        setLocationRelativeTo(null);
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        statusLabel.setText("更新を確認しています...");
        root.add(statusLabel, BorderLayout.NORTH);
        progressBar.setIndeterminate(true);
        root.add(progressBar, BorderLayout.CENTER);
        setContentPane(root);
    }

    public void updateStatus(String message) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(message));
    }

    public void updateProgress(long downloadedBytes, long totalBytes) {
        SwingUtilities.invokeLater(() -> {
            if (totalBytes > 0) {
                progressBar.setIndeterminate(false);
                progressBar.setMaximum(100);
                int percent = (int) Math.min(100, (downloadedBytes * 100) / totalBytes);
                progressBar.setValue(percent);
                progressBar.setStringPainted(true);
                progressBar.setString(percent + "%");
            } else {
                progressBar.setIndeterminate(true);
            }
        });
    }
}
