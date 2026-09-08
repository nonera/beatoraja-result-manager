package com.beatoraja.screenshot.service.twitter;

import com.beatoraja.screenshot.config.AppConfig;
import com.beatoraja.screenshot.service.TwitterCliService;

import java.io.IOException;

public class TwitterAuthService {

    private final AppConfig config;
    private final TwitterChromeLoginService chromeLoginService = new TwitterChromeLoginService();

    public TwitterAuthService(AppConfig config) {
        this.config = config;
    }

    public boolean hasStoredSession() {
        return config.hasManualTwitterAuth();
    }

    public TwitterCliService.AuthResult verifyStoredSession() {
        if (!hasStoredSession()) {
            return TwitterCliService.AuthResult.failed("Twitter に未ログインです");
        }
        return new TwitterCliService(config).checkAuth();
    }

    public TwitterCliService.AuthResult loginInteractive(LoginProgressListener listener) {
        try {
            if (listener != null) {
                listener.onStatus("Chrome / Edge を起動しています...");
            }
            chromeLoginService.openLoginBrowser();

            if (listener != null) {
                listener.onBrowserOpened();
                listener.onStatus("表示されたブラウザで X にログインし、「ログイン完了」を押してください");
                if (!listener.awaitLoginComplete()) {
                    throw new IOException("ログインをキャンセルしました");
                }
            } else {
                throw new IOException("ログイン UI が利用できません");
            }

            if (listener != null) {
                listener.onStatus("開いた Chrome / Edge を閉じてください...");
            }
            chromeLoginService.waitForLoginBrowserClosed(() ->
                    listener != null && listener.isCancelled());

            if (listener != null) {
                listener.onStatus("Cookie を取得しています...");
            }
            TwitterCookies cookies = chromeLoginService.extractCookiesFromProfile(() ->
                    listener != null && listener.isCancelled());

            saveCookies(cookies);
            if (listener != null) {
                listener.onStatus("認証を確認しています...");
            }

            TwitterCliService.AuthResult result = new TwitterCliService(config).checkAuth();
            if (result.success()) {
                if (listener != null) {
                    listener.onStatus("ログイン完了");
                }
                return result;
            }
            clearStoredSession();
            return TwitterCliService.AuthResult.failed(
                    "Cookie は取得できましたが Twitter 認証に失敗しました: " + result.message());
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return TwitterCliService.AuthResult.failed(e.getMessage());
        } finally {
            chromeLoginService.stopBrowser();
        }
    }

    public TwitterCliService.AuthResult ensureAuthenticated(LoginProgressListener listener) {
        if (hasStoredSession()) {
            TwitterCliService.AuthResult verified = verifyStoredSession();
            if (verified.success()) {
                return verified;
            }
            if (listener != null) {
                listener.onStatus("保存済みセッションを更新しています...");
            }
            if (refreshSilently()) {
                verified = verifyStoredSession();
                if (verified.success()) {
                    return verified;
                }
            }
        }

        if (listener == null) {
            return TwitterCliService.AuthResult.failed(
                    "Twitter にログインしてください。設定画面から「Twitter にログイン」を実行してください。");
        }
        return loginInteractive(listener);
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
        config.setTwitterAuthToken(cookies.authToken());
        config.setTwitterCt0(cookies.ct0());
        try {
            config.save();
        } catch (IOException ignored) {
        }
    }

    public void clearStoredSession() {
        config.setTwitterAuthToken("");
        config.setTwitterCt0("");
        try {
            config.save();
        } catch (IOException ignored) {
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
