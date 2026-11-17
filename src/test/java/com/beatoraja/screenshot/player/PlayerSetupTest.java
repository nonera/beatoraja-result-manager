package com.beatoraja.screenshot.player;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayerSetupTest {

    @TempDir
    Path tempDir;

    @Test
    void takesAnyOtherFolderAsBeatoraja() throws Exception {
        PlayerSetup setup = PlayerSetup.fromFolder(tempDir);

        assertEquals(PlayerKind.BEATORAJA, setup.kind());
        assertEquals(tempDir, setup.playerDirectory());
        assertEquals(tempDir.resolve("screenshot"), setup.screenshotDirectory());
    }

    @Test
    void resolvesABmzExeFolderToItsDataFolder() throws Exception {
        Path data = Files.createDirectories(tempDir.resolve("data"));
        Files.writeString(data.resolve("config.toml"), "version = 1\n");

        PlayerSetup setup = PlayerSetup.fromFolder(tempDir);

        assertEquals(PlayerKind.BMZ, setup.kind());
        assertEquals(data, setup.playerDirectory());
        assertEquals(data.resolve("screenshots"), setup.screenshotDirectory());
    }
}
