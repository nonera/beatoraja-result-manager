package com.beatoraja.screenshot.service;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class DiscordWebhookServiceTest {

    @Test
    void picksTheImageTypeFromTheExtension() {
        assertEquals("image/png", DiscordWebhookService.contentType("a.PNG"));
        assertEquals("image/jpeg", DiscordWebhookService.contentType("a.jpeg"));
        assertEquals("image/png", DiscordWebhookService.contentType("noextension"));
    }

    @Test
    void keepsFileNamesFromBreakingTheHeader() {
        assertEquals("a'b' c.png", DiscordWebhookService.headerSafeFileName("a\"b\"\nc.png"));
    }

    @Test
    void refusesMoreImagesThanDiscordAllows() {
        DiscordWebhookService.PostResult result = new DiscordWebhookService().post("https://example.invalid/hook", "",
                Collections.nCopies(DiscordWebhookService.MAX_IMAGES_PER_POST + 1, Path.of("x.png")));

        assertFalse(result.success());
    }
}
