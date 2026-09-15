package com.beatoraja.screenshot.util;

import javax.swing.ImageIcon;
import java.awt.Image;
import java.awt.Window;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class AppIcons {

    private static final Logger LOG = AppLogging.get(AppIcons.class);
    private static final String ICON_RESOURCE = "/icons/app-icon.png";
    private static volatile List<Image> iconImages;

    private AppIcons() {
    }

    public static void applyTo(Window window) {
        List<Image> images = iconImages();
        if (images.isEmpty()) {
            return;
        }
        window.setIconImages(images);
    }

    private static List<Image> iconImages() {
        List<Image> cached = iconImages;
        if (cached != null) {
            return cached;
        }
        synchronized (AppIcons.class) {
            cached = iconImages;
            if (cached != null) {
                return cached;
            }
            URL resource = AppIcons.class.getResource(ICON_RESOURCE);
            if (resource == null) {
                LOG.log(Level.FINE, "Application icon not found: " + ICON_RESOURCE);
                iconImages = List.of();
                return iconImages;
            }
            ImageIcon icon = new ImageIcon(resource);
            iconImages = List.of(icon.getImage());
            return iconImages;
        }
    }
}
