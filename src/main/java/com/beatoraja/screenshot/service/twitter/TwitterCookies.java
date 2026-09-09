package com.beatoraja.screenshot.service.twitter;

import java.util.LinkedHashMap;
import java.util.Map;

public record TwitterCookies(Map<String, String> cookies) {

    public TwitterCookies {
        cookies = cookies == null ? Map.of() : Map.copyOf(cookies);
    }

    public String authToken() {
        return cookies.getOrDefault("auth_token", "");
    }

    public String ct0() {
        return cookies.getOrDefault("ct0", "");
    }

    public boolean isComplete() {
        return !authToken().isBlank() && !ct0().isBlank();
    }

    public Map<String, String> toMutableMap() {
        return new LinkedHashMap<>(cookies);
    }
}
