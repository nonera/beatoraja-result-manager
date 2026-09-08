package com.beatoraja.screenshot.service;

import com.beatoraja.screenshot.config.AppConfig;
import com.beatoraja.screenshot.service.twitter.TwitterAuthService;
import com.beatoraja.screenshot.util.TwitterCliLocator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TwitterCliService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final AppConfig config;

    public TwitterCliService(AppConfig config) {
        this.config = config;
    }

    public AuthResult checkAuth() {
        Path twitterCommand = resolveTwitterCommand();
        if (twitterCommand == null) {
            return AuthResult.failed(buildMissingCliMessage());
        }
        if (!config.hasManualTwitterAuth()) {
            return AuthResult.failed("Twitter に未ログインです。「Twitter にログイン」を実行してください。");
        }

        CommandResult result = run(List.of("feed", "--max", "1", "--json"), true);
        if (result.exitCode() == 0) {
            return AuthResult.ok("Twitter 認証 OK");
        }
        return AuthResult.failed(formatFailureMessage(result));
    }

    public PostResult post(String text, List<Path> imagePaths) {
        if (imagePaths == null || imagePaths.isEmpty()) {
            return PostResult.failed("画像が選択されていません。");
        }
        if (imagePaths.size() > 4) {
            return PostResult.failed("Twitter は最大4枚まで投稿できます。");
        }
        if (resolveTwitterCommand() == null) {
            return PostResult.failed(buildMissingCliMessage());
        }
        if (!config.hasManualTwitterAuth()) {
            return PostResult.failed("Twitter に未ログインです。設定画面から「Twitter にログイン」を実行してください。");
        }

        List<String> command = new ArrayList<>();
        command.add("post");
        command.add(text == null ? "" : text);
        for (Path imagePath : imagePaths) {
            command.add("-i");
            command.add(imagePath.toAbsolutePath().toString());
        }
        command.add("--json");

        CommandResult result = run(command, false);
        if (result.exitCode() != 0) {
            if (shouldRetryAfterRefresh(result)) {
                new TwitterAuthService(config).refreshSilently();
                result = run(command, false);
            }
            if (result.exitCode() != 0) {
                return PostResult.failed(formatFailureMessage(result));
            }
        }

        String tweetId = extractTweetId(result.output());
        return PostResult.ok(tweetId, result.output());
    }

    private boolean shouldRetryAfterRefresh(CommandResult result) {
        String output = result.output() == null ? "" : result.output();
        return output.contains("not_authenticated")
                || output.contains("401")
                || result.exitCode() == 2;
    }

    private String extractTweetId(String output) {
        try {
            JsonNode root = MAPPER.readTree(output);
            if (root.has("id")) {
                return root.get("id").asText();
            }
            if (root.has("data") && root.get("data").has("id")) {
                return root.get("data").get("id").asText();
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    private CommandResult run(List<String> args, boolean verbose) {
        Path twitterExe = resolveTwitterCommand();
        if (twitterExe == null) {
            return new CommandResult(2, buildMissingCliMessage());
        }
        List<String> command = new ArrayList<>();
        command.add(twitterExe.toString());
        if (verbose) {
            command.add("-v");
        }
        command.addAll(args);

        Path outputFile = null;
        try {
            outputFile = Files.createTempFile("twitter-cli-", ".out");
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            builder.redirectOutput(outputFile.toFile());
            applyEnvironment(builder.environment());

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

    private void applyEnvironment(Map<String, String> env) {
        if (config.hasManualTwitterAuth()) {
            env.put("TWITTER_AUTH_TOKEN", config.getTwitterAuthToken().trim());
            env.put("TWITTER_CT0", config.getTwitterCt0().trim());
        }
    }

    private String formatFailureMessage(CommandResult result) {
        String output = result.output() == null ? "" : result.output().trim();
        if (output.isBlank()) {
            return buildExitCodeMessage(result.exitCode());
        }

        try {
            JsonNode root = MAPPER.readTree(output);
            String code = root.path("code").asText("");
            String message = root.path("message").asText("");
            if (!message.isBlank()) {
                return enrichMessage(message, code, result.exitCode());
            }
            if (!code.isBlank()) {
                return enrichMessage(code, code, result.exitCode());
            }
        } catch (Exception ignored) {
        }

        if (output.contains("not_authenticated")) {
            return buildAuthFailureMessage();
        }

        return enrichMessage(output, "", result.exitCode());
    }

    private String enrichMessage(String message, String code, int exitCode) {
        if ("not_authenticated".equalsIgnoreCase(code) || message.contains("not_authenticated")) {
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
        return "twitter-cli が失敗しました (exit code: " + exitCode + ")";
    }

    private String buildAuthFailureMessage() {
        return """
                Twitter 認証に失敗しました。

                設定画面の「Twitter にログイン」を再度実行してください。
                セッションの有効期限切れの可能性があります。
                """;
    }

    private String buildMissingCliMessage() {
        Path bundled = TwitterCliLocator.expectedBundledPath();
        Path projectTools = java.nio.file.Path.of(System.getProperty("user.dir")).resolve("tools").resolve("twitter.exe");
        return """
                twitter-cli が見つかりません。

                次のいずれかを実行してください:
                1. 開発中: .\\scripts\\build-twitter-cli.ps1
                2. または: pip install twitter-cli （PATH に twitter コマンドが通る状態）
                3. 配布版: tools\\twitter.exe を %s に配置

                プロジェクト直下なら: %s
                """.formatted(bundled, projectTools.toAbsolutePath());
    }

    private Path resolveTwitterCommand() {
        return TwitterCliLocator.locate().orElse(null);
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
