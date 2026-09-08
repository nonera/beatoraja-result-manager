package com.beatoraja.screenshot.service.twitter;

public record TwitterCookies(String authToken, String ct0) {
    public boolean isComplete() {
        return authToken != null && !authToken.isBlank()
                && ct0 != null && !ct0.isBlank();
    }
}
