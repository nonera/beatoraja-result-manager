package com.beatoraja.screenshot.util;

import java.util.Locale;

/** Facts about the operating system the app runs on. */
public final class OsInfo {

    private OsInfo() {
    }

    public static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
    }
}
