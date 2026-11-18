package com.beatoraja.screenshot.service.twitter;

import com.beatoraja.screenshot.config.AppConfig;
import com.beatoraja.screenshot.service.ClixService;
import com.beatoraja.screenshot.util.AppLogging;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TwitterAuthService {

    private static final Logger LOG = AppLogging.get(TwitterAuthService.class);

    private final AppConfig config;
    private final TwitterChromeLoginService chromeLoginService = new TwitterChromeLoginService();

    public TwitterAuthService(AppConfig config) {
        this.config = config;
    }

    public boolean hasStoredSession() {
        return config.hasManualTwitterAuth();
    }

    public ClixService.AuthResult verifyStoredSession() {
        if (!hasStoredSession()) {
            return ClixService.AuthResult.failed("Twitter に未ログインです");
        }
        return new ClixService(config).checkAuth();
    }

    public ClixService.AuthResult loginInteractive(LoginProgressListener listener) {
        if (listener == null) {
            // The user has to log in in the browser and confirm here; without a UI there is no point starting it.
            return ClixService.AuthResult.failed("ログイン UI が利用できません");
        }
        try {
            listener.onStatus("Chrome / Edge を起動しています...");
            chromeLoginService.openLoginBrowser();

            listener.onBrowserOpened();
            listener.onStatus("表示されたブラウザで X にログインし、「ログイン完了」を押してください");
            if (!listener.awaitLoginComplete()) {
                throw new IOException("ログインをキャンセルしました");
            }

            listener.onStatus("開いた Chrome / Edge を閉じてください...");
            chromeLoginService.waitForLoginBrowserClosed(listener::isCancelled);

            listener.onStatus("Cookie を取得しています...");
            TwitterCookies cookies = chromeLoginService.extractCookiesFromProfile(listener::isCancelled);

            saveCookies(cookies);
            listener.onStatus("認証を確認しています...");

            ClixService.AuthResult result = new ClixService(config).checkAuth();
            if (result.success()) {
                listener.onStatus("ログイン完了");
                return result;
            }
            clearStoredSession();
            return ClixService.AuthResult.failed(
                    "Cookie は取得できましたが Twitter 認証に失敗しました: " + result.message());
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return ClixService.AuthResult.failed(e.getMessage());
        } finally {
            chromeLoginService.stopBrowser();
        }
    }

    /**
     * Checks the stored session; if that fails, re-reads the cookies from the browser profile
     * without showing anything and checks again.
     */
    public ClixService.AuthResult verifyOrRefresh() {
        ClixService.AuthResult result = verifyStoredSession();
        if (!result.success() && refreshSilently()) {
            result = verifyStoredSession();
        }
        return result;
    }

    public boolean refreshSilently() {
        return chromeLoginService.silentRefresh()
                .map(cookies -> {
                    saveCookies(cookies);
                    return true;
                })
                .orElse(false);
    }

    public void saveCookies(TwitterCookies cookies) {
        config.setTwitterCookies(cookies.toMutableMap());
        try {
            config.save();
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Failed to save Twitter cookies", e);
        }
    }

    public void clearStoredSession() {
        config.clearTwitterAuth();
        try {
            config.save();
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Failed to save config after clearing Twitter session", e);
        }
    }

    public void logout() {
        clearStoredSession();
        chromeLoginService.stopBrowser();
    }

    public interface LoginProgressListener {
        void onStatus(String status);

        default void onBrowserOpened() {
        }

        boolean awaitLoginComplete() throws InterruptedException;

        default boolean isCancelled() {
            return false;
        }
    }
}
