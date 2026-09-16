package com.beatoraja.screenshot.ui.theme;

import com.beatoraja.screenshot.config.AppConfig;
import com.beatoraja.screenshot.util.AppLogging;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Installs the FlatLaf look and feel and exposes the handful of accent colors
 * the custom renderers need. Japanese glyphs are missing from FlatLaf's default
 * Segoe UI, so a CJK-capable UI font is selected explicitly.
 */
public final class UiTheme {

    public static final String DARK = "dark";
    public static final String LIGHT = "light";

    private static final Logger LOG = AppLogging.get(UiTheme.class);

    private static final String ACCENT_HEX = "#4F8CFF";
    private static final String[] PREFERRED_FONTS = {
            "Yu Gothic UI", "Meiryo UI", "Meiryo", "MS UI Gothic", "Noto Sans JP"
    };
    /** Hiragana / katakana / kanji probe used to weed out Latin-only font families. */
    private static final String JAPANESE_SAMPLE = "あアー漢字";
    /**
     * Last-resort fallback when none of {@link #PREFERRED_FONTS} are reported as available.
     * Meiryo ships with every Windows release since Vista, so it's used instead of the
     * logical {@link Font#SANS_SERIF} font, which renders CJK text as tofu boxes.
     */
    private static final String FALLBACK_FONT = "Meiryo";

    private static boolean dark = true;
    private static String fontFamily = Font.SANS_SERIF;
    private static int fontSize = AppConfig.DEFAULT_UI_FONT_SIZE;

    private UiTheme() {
    }

    public static void install(String themeId, String preferredFontFamily, int preferredFontSize) {
        dark = !LIGHT.equalsIgnoreCase(themeId);
        fontFamily = resolveUiFontFamily(preferredFontFamily);
        fontSize = Math.min(AppConfig.MAX_UI_FONT_SIZE,
                Math.max(AppConfig.MIN_UI_FONT_SIZE, preferredFontSize));

        Map<String, String> extraDefaults = new LinkedHashMap<>();
        extraDefaults.put("@accentColor", ACCENT_HEX);
        FlatLaf.setGlobalExtraDefaults(extraDefaults);

        try {
            if (dark) {
                FlatDarkLaf.setup();
            } else {
                FlatLightLaf.setup();
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to install FlatLaf, falling back to the default look and feel", e);
            return;
        }

        applyDefaults();
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);
    }

    /** Re-applies the look and feel and repaints every open window. */
    public static void apply(String themeId, String preferredFontFamily, int preferredFontSize) {
        install(themeId, preferredFontFamily, preferredFontSize);
        FlatLaf.updateUI();
    }

    public static boolean isDark() {
        return dark;
    }

    /** Base UI font size in points; every derived size is expressed relative to this. */
    public static int fontSize() {
        return fontSize;
    }

    /** Font families that can render Japanese, for the font picker in the settings dialog. */
    public static List<String> japaneseCapableFontFamilies() {
        List<String> families = new ArrayList<>();
        for (String family : GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames(Locale.JAPAN)) {
            Font candidate = new Font(family, Font.PLAIN, 12);
            if (candidate.canDisplayUpTo(JAPANESE_SAMPLE) < 0) {
                families.add(family);
            }
        }
        return families;
    }

    private static void applyDefaults() {
        UIManager.put("defaultFont", new FontUIResource(fontFamily, Font.PLAIN, fontSize));

        UIManager.put("Component.arc", 8);
        UIManager.put("Button.arc", 10);
        UIManager.put("CheckBox.arc", 5);
        UIManager.put("ProgressBar.arc", 8);
        UIManager.put("TextComponent.arc", 8);
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Component.innerFocusWidth", 1);

        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
        UIManager.put("ScrollBar.width", 12);
        UIManager.put("ScrollPane.smoothScrolling", true);

        UIManager.put("Table.showHorizontalLines", false);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("Table.intercellSpacing", new java.awt.Dimension(0, 0));
        UIManager.put("Table.selectionArc", 6);
        // A muted selection keeps the colored lamp / rank badges legible on the selected row.
        UIManager.put("Table.selectionBackground", dark ? new Color(0x33456B) : new Color(0xD3E3FF));
        UIManager.put("Table.selectionForeground", dark ? new Color(0xEAEEF5) : new Color(0x1B1F26));
        UIManager.put("Table.selectionInactiveBackground", dark ? new Color(0x333841) : new Color(0xE2E6ED));
        UIManager.put("Table.selectionInactiveForeground", dark ? new Color(0xEAEEF5) : new Color(0x1B1F26));
        UIManager.put("TableHeader.height", 30);
        UIManager.put("TableHeader.separatorColor", subtle());

        UIManager.put("TabbedPane.tabArc", 8);
        UIManager.put("TabbedPane.showTabSeparators", true);

        UIManager.put("SplitPane.oneTouchButtonSize", 0);
        UIManager.put("SplitPaneDivider.gripDotCount", 3);

        UIManager.put("TitlePane.unifiedBackground", true);
        UIManager.put("TitlePane.menuBarEmbedded", true);
        UIManager.put("MenuBar.borderColor", subtle());
    }

    /** Surface color one step above the window background, used for cards. */
    public static Color surface() {
        return dark ? new Color(0x2B2E33) : new Color(0xF4F6FA);
    }

    /** Hairline border color that reads as a divider in both themes. */
    public static Color subtle() {
        return dark ? new Color(0x3C4047) : new Color(0xD6DAE1);
    }

    /** De-emphasized text color for captions and secondary metadata. */
    public static Color mutedText() {
        return dark ? new Color(0x9AA1AC) : new Color(0x6B7280);
    }

    /**
     * De-emphasized variant of {@code foreground} against {@code background}. Table cells use
     * this instead of {@link #mutedText()} so secondary text stays readable on selected rows.
     */
    public static Color mutedAgainst(Color foreground, Color background) {
        return ResultPalette.tint(foreground, background, 0.55f);
    }

    public static Color accent() {
        return Color.decode(ACCENT_HEX);
    }

    public static Color twitterAccent() {
        return new Color(0x1D9BF0);
    }

    public static Color discordAccent() {
        return new Color(0x5865F2);
    }

    /** Row background used for every other table row. */
    public static Color alternateRow() {
        return dark ? new Color(0x2A2D32) : new Color(0xF7F8FA);
    }

    private static String resolveUiFontFamily(String preferred) {
        Set<String> available = new HashSet<>(Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames(Locale.JAPAN)));
        if (preferred != null && !preferred.isBlank() && available.contains(preferred)) {
            return preferred;
        }
        for (String family : PREFERRED_FONTS) {
            if (available.contains(family)) {
                return family;
            }
        }
        LOG.warning("No preferred Japanese-capable font family was reported as available; "
                + "falling back to " + FALLBACK_FONT);
        return FALLBACK_FONT;
    }
}
