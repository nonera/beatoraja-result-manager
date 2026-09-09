package com.beatoraja.screenshot.service.twitter;

import com.beatoraja.screenshot.config.AppConfig;
import com.beatoraja.screenshot.util.AppPaths;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Writes clix's {@code ~/.config/clix/auth.json} under an app-owned home directory
 * and points the child process at it via {@code USERPROFILE}, so full cookie jars
 * are used without touching the user's global clix config.
 */
public final class ClixAuthSupport {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ClixAuthSupport() {
    }

    public static void applyAuthEnvironment(AppConfig config, Map<String, String> env) throws IOException {
        if (!config.hasManualTwitterAuth()) {
            return;
        }

        Path homeDir = AppPaths.clixHomeDir();
        Path authFile = homeDir.resolve(".config").resolve("clix").resolve("auth.json");
        Files.createDirectories(authFile.getParent());

        Map<String, Object> account = new LinkedHashMap<>();
        account.put("auth_token", config.getTwitterAuthToken().trim());
        account.put("ct0", config.getTwitterCt0().trim());
        account.put("cookies", new LinkedHashMap<>(config.getTwitterCookies()));
        account.put("account_name", "default");

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("accounts", Map.of("default", account));
        root.put("default", "default");

        MAPPER.writerWithDefaultPrettyPrinter().writeValue(authFile.toFile(), root);
        env.put("USERPROFILE", homeDir.toAbsolutePath().normalize().toString());
    }
}
