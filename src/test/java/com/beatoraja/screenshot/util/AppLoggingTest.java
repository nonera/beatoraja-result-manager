package com.beatoraja.screenshot.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppLoggingTest {

    @Test
    void maskWebhookUrlHidesToken() {
        String masked = AppLogging.maskWebhookUrl(
                "https://discord.com/api/webhooks/1234567890/abcdefghijklmnopqrstuvwxyz");

        assertEquals("https://discord.com/api/webhooks/1234567890/****", masked);
    }

    @Test
    void sanitizeMasksWebhookInMessage() {
        String sanitized = AppLogging.sanitize(
                "Failed: https://discord.com/api/webhooks/1234567890/abcdefghijklmnopqrstuvwxyz");

        assertTrue(sanitized.contains("/****"));
        assertTrue(sanitized.contains("1234567890/****"));
    }
}
