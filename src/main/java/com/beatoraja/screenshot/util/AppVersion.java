package com.beatoraja.screenshot.util;

public final class AppVersion {

    private static final String VERSION = resolveVersion();

    private AppVersion() {
    }

    public static String get() {
        return VERSION;
    }

    private static String resolveVersion() {
        String version = AppVersion.class.getPackage().getImplementationVersion();
        return (version == null || version.isBlank()) ? "dev" : version;
    }
}
