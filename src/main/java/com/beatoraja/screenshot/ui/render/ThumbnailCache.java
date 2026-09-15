package com.beatoraja.screenshot.ui.render;

import com.beatoraja.screenshot.ui.theme.UiTheme;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Bounded, lazily populated cache of rounded table thumbnails. Decoding happens on
 * background threads; {@code repaintCallback} is invoked on the EDT once an image lands.
 */
public final class ThumbnailCache {

    private static final int CAPACITY = 512;
    private static final int ARC = 6;

    private final int width;
    private final int height;
    private final Runnable repaintCallback;
    private final Map<Path, Icon> cache;
    private final Set<Path> pending = ConcurrentHashMap.newKeySet();
    private final ExecutorService executor = Executors.newFixedThreadPool(2, runnable -> {
        Thread thread = new Thread(runnable, "thumbnail-loader");
        thread.setDaemon(true);
        return thread;
    });

    public ThumbnailCache(int width, int height, Runnable repaintCallback) {
        this.width = width;
        this.height = height;
        this.repaintCallback = repaintCallback;
        this.cache = Collections.synchronizedMap(new LinkedHashMap<>(64, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Path, Icon> eldest) {
                return size() > CAPACITY;
            }
        });
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    /** Returns the cached icon, or {@code null} while the image is still being decoded. */
    public Icon get(Path path) {
        if (path == null) {
            return null;
        }
        Icon cached = cache.get(path);
        if (cached != null) {
            return cached;
        }
        if (pending.add(path)) {
            executor.execute(() -> load(path));
        }
        return null;
    }

    public void shutdown() {
        executor.shutdownNow();
    }

    private void load(Path path) {
        Icon icon = render(path);
        if (icon != null) {
            cache.put(path, icon);
        }
        pending.remove(path);
        if (icon != null) {
            SwingUtilities.invokeLater(repaintCallback);
        }
    }

    private Icon render(Path path) {
        try {
            BufferedImage original = ImageIO.read(path.toFile());
            if (original == null) {
                return null;
            }
            double scale = Math.max(
                    (double) width / original.getWidth(),
                    (double) height / original.getHeight()
            );
            int scaledWidth = Math.max(1, (int) Math.round(original.getWidth() * scale));
            int scaledHeight = Math.max(1, (int) Math.round(original.getHeight() * scale));

            BufferedImage target = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = target.createGraphics();
            try {
                graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                graphics.setClip(new RoundRectangle2D.Float(0, 0, width, height, ARC, ARC));
                // Center-crop so every thumbnail keeps the same footprint in the table.
                graphics.drawImage(original, (width - scaledWidth) / 2, (height - scaledHeight) / 2,
                        scaledWidth, scaledHeight, null);
                graphics.setClip(null);
                graphics.setColor(new Color(0, 0, 0, UiTheme.isDark() ? 90 : 40));
                graphics.drawRoundRect(0, 0, width - 1, height - 1, ARC, ARC);
            } finally {
                graphics.dispose();
            }
            return new ImageIcon(target);
        } catch (Exception e) {
            return null;
        }
    }
}
