package com.beatoraja.screenshot.service;

import com.beatoraja.screenshot.config.AppConfig;
import com.beatoraja.screenshot.service.twitter.ClixAuthSupport;
import com.beatoraja.screenshot.service.twitter.TwitterBrowserPostService;
import com.beatoraja.screenshot.util.ClixLocator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClixService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final AppConfig config;

    public ClixService(AppConfig config) {
        this.config = config;
    }

    public AuthResult checkAuth() {
        if (resolveClixCommand() == null) {
            return AuthResult.failed(buildMissingCliMessage());
        }
        if (!config.hasManualTwitterAuth()) {
            return AuthResult.failed("Twitter に未ログインです。「Twitter にログイン」を実行してください。");
        }

        CommandResult result = run(List.of("auth", "status", "--json"), false);
        if (result.exitCode() == 0 && isAuthenticated(result.output())) {
            return AuthResult.ok("Twitter 認証 OK");
        }
        return AuthResult.failed(formatFailureMessage(result));
    }

    /**
     * Opens the X compose page in the user's logged-in Chrome/Edge profile,
     * pre-fills text and images, and waits for the user to click Post.
     */
    public PostResult post(String text, List<Path> imagePaths) {
        text = normalizeLineEndings(text);
        if (imagePaths == null || imagePaths.isEmpty()) {
            return PostResult.failed("画像が選択されていません。");
        }
        if (imagePaths.size() > 4) {
            return PostResult.failed("Twitter は最大4枚まで投稿できます。");
        }
        return new TwitterBrowserPostService().post(text, imagePaths);
    }

    private boolean isAuthenticated(String output) {
        try {
            JsonNode root = MAPPER.readTree(output);
            return root.path("authenticated").asBoolean(false);
        } catch (Exception ignored) {
            return false;
        }
    }

    private CommandResult run(List<String> args, boolean verbose) {
        Path clixExe = resolveClixCommand();
        if (clixExe == null) {
            return new CommandResult(2, buildMissingCliMessage());
        }
        List<String> command = new ArrayList<>();
        command.add(clixExe.toString());
        command.addAll(args);

        Path outputFile = null;
        try {
            outputFile = Files.createTempFile("clix-", ".out");
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            builder.redirectOutput(outputFile.toFile());
            try {
                applyEnvironment(builder.environment());
            } catch (IOException e) {
                return new CommandResult(1, "clix 認証ファイルの作成に失敗しました: " + e.getMessage());
            }

            Process process = builder.start();
            int exitCode = process.waitFor();
            String output = Files.readString(outputFile, StandardCharsets.UTF_8).trim();
            return new CommandResult(exitCode, output);
        } catch (IOException e) {
            if (e.getMessage() != null && e.getMessage().contains("Cannot run program")) {
                return new CommandResult(2, buildMissingCliMessage());
            }
            return new CommandResult(1, e.getMessage());
        } catch (Exception e) {
            return new CommandResult(1, e.getMessage());
        } finally {
            if (outputFile != null) {
                try {
                    Files.deleteIfExists(outputFile);
                } catch (IOException ignored) {
                }
            }
        }
    }

    private void applyEnvironment(Map<String, String> env) throws IOException {
        ClixAuthSupport.applyAuthEnvironment(config, env);
    }

    private String formatFailureMessage(CommandResult result) {
        String output = result.output() == null ? "" : result.output().trim();
        if (output.isBlank()) {
            return buildExitCodeMessage(result.exitCode());
        }

        try {
            JsonNode root = MAPPER.readTree(output);
            if (root.has("authenticated") && !root.path("authenticated").asBoolean(true)) {
                return buildAuthFailureMessage();
            }
            JsonNode errors = root.path("errors");
            if (errors.isArray() && !errors.isEmpty()) {
                String message = errors.get(0).path("message").asText("");
                if (!message.isBlank()) {
                    return enrichMessage(message, "", result.exitCode());
                }
            }
            String message = root.path("message").asText("");
            if (!message.isBlank()) {
                return enrichMessage(message, root.path("code").asText(""), result.exitCode());
            }
        } catch (Exception ignored) {
        }

        if (output.contains("Not authenticated")
                || output.contains("No Twitter/X credentials")
                || output.contains("not_authenticated")) {
            return buildAuthFailureMessage();
        }

        return enrichMessage(output, "", result.exitCode());
    }

    private String enrichMessage(String message, String code, int exitCode) {
        if ("not_authenticated".equalsIgnoreCase(code)
                || message.contains("Not authenticated")
                || message.contains("No Twitter/X credentials")
                || message.contains("not_authenticated")) {
            return buildAuthFailureMessage();
        }
        if (exitCode != 0) {
            return message + "\n(exit code: " + exitCode + ")";
        }
        return message;
    }

    private String buildExitCodeMessage(int exitCode) {
        if (exitCode == 2) {
            return buildAuthFailureMessage();
        }
        if (exitCode == 3) {
            return "X のレート制限に達しました。しばらく待ってから再試行してください。";
        }
        return "clix が失敗しました (exit code: " + exitCode + ")";
    }

    private String buildAuthFailureMessage() {
        return """
                Twitter 認証に失敗しました。

                設定画面の「Twitter にログイン」を再度実行してください。
                セッションの有效期限切れの可能性があります。
                """;
    }

    private String buildMissingCliMessage() {
        Path bundled = ClixLocator.expectedBundledPath();
        Path projectTools = Path.of(System.getProperty("user.dir")).resolve("tools").resolve("clix.exe");
        return """
                clix が見つかりません。

                次のいずれかを実行してください:
                1. 開発中: .\\scripts\\build-clix.ps1
                2. または: pip install clix0 （PATH に clix コマンドが通る状態）
                3. 配布版: tools\\clix.exe を %s に配置

                プロジェクト直下なら: %s
                """.formatted(bundled, projectTools.toAbsolutePath());
    }

    private Path resolveClixCommand() {
        return ClixLocator.locate().orElse(null);
    }

    static String normalizeLineEndings(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\r\n", "\n").replace('\r', '\n');
    }

    public record AuthResult(boolean success, String message) {
        public static AuthResult ok(String message) {
            return new AuthResult(true, message);
        }

        public static AuthResult failed(String message) {
            return new AuthResult(false, message);
        }
    }

    public record PostResult(boolean success, String tweetId, String message) {
        public static PostResult ok(String tweetId, String message) {
            return new PostResult(true, tweetId, message);
        }

        public static PostResult failed(String message) {
            return new PostResult(false, "", message);
        }
    }

    private record CommandResult(int exitCode, String output) {
    }
}
