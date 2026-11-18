package com.beatoraja.screenshot.player;

import java.io.IOException;
import java.nio.file.Path;

/**
 * The first-run choice of player made from a folder the user picked: which player it is, the
 * folder to read its data from, and where it saves screenshots.
 */
public record PlayerSetup(PlayerKind kind, Path playerDirectory, Path screenshotDirectory) {

    /**
     * Works out the setup for {@code chosen}. A BMZ Player folder (its data folder, or the
     * portable install's exe folder) is resolved to its data folder and the screenshot folder
     * from config.toml; anything else is taken as a beatoraja folder with screenshots in
     * {@code screenshot/}. Throws when a BMZ Player config.toml cannot be read.
     */
    public static PlayerSetup fromFolder(Path chosen) throws IOException {
        PlayerKind kind = PlayerKind.detect(chosen).orElse(PlayerKind.BEATORAJA);
        if (kind == PlayerKind.BMZ) {
            BmzPaths paths = BmzPaths.resolve(chosen, "");
            return new PlayerSetup(kind, paths.dataDirectory(), paths.screenshotDirectory());
        }
        return new PlayerSetup(kind, chosen, chosen.resolve("screenshot"));
    }
}
