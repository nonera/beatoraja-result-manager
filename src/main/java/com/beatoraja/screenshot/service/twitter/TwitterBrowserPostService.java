package com.beatoraja.screenshot.service.twitter;

import com.beatoraja.screenshot.service.ClixService;
import com.beatoraja.screenshot.util.AppPaths;
import com.beatoraja.screenshot.util.ProcessUtils;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Prepares a tweet by opening a normal, visible Chrome/Edge window on the
 * persisted login profile and auto-filling the x.com compose UI (text +
 * images) through CDP. The actual "Post" click is left to the user, so the
 * request that reaches X is a genuine, human-initiated action rather than a
 * fully scripted one.
 *
 * This is the primary posting path for {@link ClixService}. A fully automated
 * version (auto-clicking Post too) was tried first and got blocked by X's
 * automation detection (error 226), which is why the final action is left to
 * a human.
 */
public class TwitterBrowserPostService {

    private static final String COMPOSE_URL = "https://x.com/compose/post";
    private static final String TEXTAREA_SELECTOR = "div[data-testid^=\"tweetTextarea_\"]";
    private static final String POST_BUTTON_SELECTOR = "[data-testid=\"tweetButton\"], [data-testid=\"tweetButtonInline\"]";
    private static final String FILE_INPUT_SELECTOR = "input[data-testid=\"fileInput\"]";

    private static final Duration STARTUP_TIMEOUT = Duration.ofSeconds(20);
    private static final Duration COMPOSE_LOAD_TIMEOUT = Duration.ofSeconds(20);
    private static final Duration UPLOAD_TIMEOUT = Duration.ofSeconds(45);
    private static final Duration MANUAL_POST_TIMEOUT = Duration.ofMinutes(5);
    private static final Duration COMMAND_TIMEOUT = Duration.ofSeconds(15);
    private static final Duration POLL_INTERVAL = Duration.ofMillis(500);

    private Process browserProcess;

    public ClixService.PostResult post(String text, List<Path> imagePaths) {
        Path browser = ChromiumBrowserLocator.locate().orElse(null);
        if (browser == null) {
            return ClixService.PostResult.failed("Chrome または Edge が見つかりません。");
        }

        Path profileDir = AppPaths.twitterChromeProfileDir();
        try {
            Files.createDirectories(profileDir);
            ChromiumProfileLock.waitForProfileUnlock(profileDir);
            ChromiumProfileLock.clearDebugArtifacts(profileDir);

            List<String> command = new ArrayList<>();
            command.add(browser.toAbsolutePath().toString());
            command.add("--user-data-dir=" + profileDir.toAbsolutePath());
            command.add("--no-first-run");
            command.add("--no-default-browser-check");
            command.add("--disable-sync");
            command.add("--disable-features=ChromeWhatsNewUI,TranslateUI");
            command.add("--disable-blink-features=AutomationControlled");
            command.add("--remote-debugging-port=0");
            command.add("--window-size=1000,800");
            command.add("about:blank");

            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            browserProcess = builder.start();

            int debugPort = ChromiumDevToolsClient.waitForDebugPort(profileDir, STARTUP_TIMEOUT);
            return runComposeFlow(debugPort, text, imagePaths);
        } catch (Exception e) {
            return ClixService.PostResult.failed("ブラウザ投稿に失敗しました: " + e.getMessage());
        } finally {
            stopBrowser();
            try {
                ChromiumProfileLock.waitForProfileUnlock(profileDir);
            } catch (Exception ignored) {
            }
        }
    }

    private ClixService.PostResult runComposeFlow(int debugPort, String text, List<Path> imagePaths) throws Exception {
        ChromiumDevToolsClient devTools = new ChromiumDevToolsClient(debugPort);
        try (ChromiumCdpSession session = devTools.openSession("about:blank")) {
            session.send("Page.navigate", Map.of("url", COMPOSE_URL), COMMAND_TIMEOUT);

            if (!waitUntil(session, COMPOSE_LOAD_TIMEOUT,
                    "!!document.querySelector('" + TEXTAREA_SELECTOR + "')")) {
                return ClixService.PostResult.failed(
                        "投稿画面を開けませんでした。Twitter に再ログインが必要な可能性があります。");
            }

            boolean focused = evaluateBoolean(session,
                    "(function(){var el=document.querySelector('" + TEXTAREA_SELECTOR + "');"
                            + "if(el){el.focus();return true;}return false;})()");
            if (!focused) {
                return ClixService.PostResult.failed("投稿本文の入力欄を操作できませんでした。");
            }
            // X restores an unsent draft when the compose page loads. Input.insertText
            // only inserts at the caret, so without clearing first, leftover text from
            // a previous attempt gets stuck together with the new text.
            evaluateBoolean(session,
                    "(function(){var el=document.querySelector('" + TEXTAREA_SELECTOR + "');"
                            + "if(!el){return false;}el.focus();"
                            + "document.execCommand('selectAll',false,null);"
                            + "document.execCommand('delete',false,null);"
                            + "return true;})()");
            insertComposeText(session, text);

            if (imagePaths != null && !imagePaths.isEmpty()) {
                if (!attachImages(session, imagePaths)) {
                    return ClixService.PostResult.failed("画像の添付に失敗しました。");
                }
                if (!waitUntil(session, UPLOAD_TIMEOUT,
                        "(function(){var b=document.querySelector('" + POST_BUTTON_SELECTOR + "');"
                                + "return !!b && b.getAttribute('aria-disabled')!=='true';})()")) {
                    return ClixService.PostResult.failed("画像のアップロード完了を確認できませんでした。");
                }
            }

            session.send("Page.bringToFront", Map.of(), COMMAND_TIMEOUT);

            return waitForManualPost(session);
        }
    }

    /**
     * The composer is pre-filled; the human at the keyboard reviews the tweet
     * and clicks Post themselves. We just watch for that to happen (or for
     * the window to be closed, meaning the user cancelled).
     */
    private ClixService.PostResult waitForManualPost(ChromiumCdpSession session) throws Exception {
        String confirmExpression =
                "(function(){var el=document.querySelector('" + TEXTAREA_SELECTOR + "');"
                        + "var toast=document.querySelector('[data-testid=\"toast\"]');"
                        + "return !!toast || !el || (el.innerText||'').trim().length===0;})()";

        long deadline = System.currentTimeMillis() + MANUAL_POST_TIMEOUT.toMillis();
        while (System.currentTimeMillis() < deadline) {
            if (browserProcess == null || !browserProcess.isAlive()) {
                return ClixService.PostResult.failed("ブラウザが閉じられたため投稿をキャンセルしました。");
            }
            if (evaluateBoolean(session, confirmExpression)) {
                // Give the in-flight request a moment to actually complete before we
                // close the browser out from under it.
                Thread.sleep(1500);
                return ClixService.PostResult.ok("", "投稿完了（ブラウザで手動投稿）");
            }
            Thread.sleep(POLL_INTERVAL.toMillis());
        }
        return ClixService.PostResult.failed(
                "投稿の完了を確認できませんでした（" + MANUAL_POST_TIMEOUT.toMinutes() + "分待機）。"
                        + "ブラウザで「投稿」を押したか確認してください。");
    }

    /**
     * X's compose box is a contenteditable div. A single {@code Input.insertText}
     * with embedded {@code \n} characters does not reliably create line breaks,
     * so each line is inserted separately with a paragraph break between them.
     *
     * Raw {@code Input.dispatchKeyEvent} keyDown/keyUp for Enter was tried first:
     * the final posted text came out correct, but the visible compose box only
     * ever rendered the first line. That is a synthetic keyboard event — it is up
     * to X's own JS to react to it and there is no guarantee the resulting
     * re-render (new block, caret move, box auto-grow) is done by the time the
     * CDP call returns, even with an added delay. {@code execCommand('insertParagraph')}
     * instead performs the paragraph split as a real, synchronous DOM/editing-host
     * operation with its own native {@code beforeinput}/{@code input} events, which
     * X's editor and box-height logic pick up reliably.
     */
    private void insertComposeText(ChromiumCdpSession session, String text) throws Exception {
        if (text == null || text.isEmpty()) {
            return;
        }
        String normalized = text.replace("\r\n", "\n").replace('\r', '\n');
        String[] lines = normalized.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) {
                insertParagraphBreak(session);
            }
            if (!lines[i].isEmpty()) {
                session.send("Input.insertText", Map.of("text", lines[i]), COMMAND_TIMEOUT);
            }
        }
    }

    private void insertParagraphBreak(ChromiumCdpSession session) throws Exception {
        evaluateBoolean(session,
                "(function(){var el=document.querySelector('" + TEXTAREA_SELECTOR + "');"
                        + "if(!el){return false;}el.focus();"
                        + "return document.execCommand('insertParagraph', false, null);})()");
    }

    private boolean attachImages(ChromiumCdpSession session, List<Path> imagePaths) throws Exception {
        session.send("DOM.enable", Map.of(), COMMAND_TIMEOUT);
        JsonNode document = session.send("DOM.getDocument", Map.of(), COMMAND_TIMEOUT);
        int rootNodeId = document.path("root").path("nodeId").asInt(0);
        if (rootNodeId == 0) {
            return false;
        }
        JsonNode queryResult = session.send("DOM.querySelector",
                Map.of("nodeId", rootNodeId, "selector", FILE_INPUT_SELECTOR), COMMAND_TIMEOUT);
        int fileInputNodeId = queryResult.path("nodeId").asInt(0);
        if (fileInputNodeId == 0) {
            return false;
        }

        List<String> absolutePaths = new ArrayList<>();
        for (Path path : imagePaths) {
            absolutePaths.add(path.toAbsolutePath().toString());
        }
        session.send("DOM.setFileInputFiles",
                Map.of("nodeId", fileInputNodeId, "files", absolutePaths), COMMAND_TIMEOUT);
        return true;
    }

    private boolean waitUntil(ChromiumCdpSession session, Duration timeout, String expression)
            throws Exception {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        while (System.currentTimeMillis() < deadline) {
            if (evaluateBoolean(session, expression)) {
                return true;
            }
            Thread.sleep(POLL_INTERVAL.toMillis());
        }
        return evaluateBoolean(session, expression);
    }

    private boolean evaluateBoolean(ChromiumCdpSession session, String expression) throws Exception {
        try {
            JsonNode result = session.send("Runtime.evaluate",
                    Map.of("expression", expression, "returnByValue", true), COMMAND_TIMEOUT);
            return result.path("result").path("value").asBoolean(false);
        } catch (Exception e) {
            return false;
        }
    }

    private void stopBrowser() {
        ProcessUtils.destroyProcessTree(browserProcess);
        browserProcess = null;
    }
}
