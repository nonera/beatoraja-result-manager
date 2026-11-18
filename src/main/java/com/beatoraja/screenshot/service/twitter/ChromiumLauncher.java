package com.beatoraja.screenshot.service.twitter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Starts Chrome/Edge on the app's own X profile, with the flags every launch shares. */
final class ChromiumLauncher {

    private ChromiumLauncher() {
    }

    /**
     * The shared command line: the browser, the profile folder, and flags that keep first-run
     * pages, sync and pop-ups out of the way. {@code visibleToUser} also hides the
     * "controlled by automated software" signal from pages, for windows the user works in.
     */
    static List<String> command(Path browser, Path profileDir, boolean visibleToUser) {
        List<String> command = new ArrayList<>();
        command.add(browser.toAbsolutePath().toString());
        command.add("--user-data-dir=" + profileDir.toAbsolutePath());
        command.add("--no-first-run");
        command.add("--no-default-browser-check");
        command.add("--disable-sync");
        command.add("--disable-features=ChromeWhatsNewUI,TranslateUI");
        if (visibleToUser) {
            command.add("--disable-blink-features=AutomationControlled");
        }
        return command;
    }

    /** Starts the browser with {@link #command} followed by {@code extraArgs}. */
    static Process launch(Path browser, Path profileDir, boolean visibleToUser, List<String> extraArgs)
            throws IOException {
        List<String> command = command(browser, profileDir, visibleToUser);
        command.addAll(extraArgs);
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);
        return builder.start();
    }
}
