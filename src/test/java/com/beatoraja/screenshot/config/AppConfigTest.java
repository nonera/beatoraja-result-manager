package com.beatoraja.screenshot.config;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppConfigTest {

    @Test
    void movesOldFormatTokensIntoTheCookiesAndDoesNotWriteThemBack() throws Exception {
        AppConfig config = AppConfig.parse("""
                {"twitterAuthToken": "tok", "twitterCt0": "ct"}
                """.getBytes(StandardCharsets.UTF_8));

        assertEquals("tok", config.twitterAuthToken());
        assertEquals("ct", config.twitterCt0());
        assertTrue(config.hasManualTwitterAuth());
        String saved = new String(config.toJson(), StandardCharsets.UTF_8);
        assertFalse(saved.contains("twitterAuthToken"), saved);
        assertTrue(saved.contains("\"auth_token\" : \"tok\""), saved);
    }

    @Test
    void keepsTheCookieMapWhenBothAreThere() throws Exception {
        AppConfig config = AppConfig.parse("""
                {"twitterAuthToken": "old", "twitterCookies": {"auth_token": "new", "ct0": "c"}}
                """.getBytes(StandardCharsets.UTF_8));

        assertEquals("new", config.twitterAuthToken());
        config.clearTwitterAuth();
        assertFalse(config.hasManualTwitterAuth());
    }
}
