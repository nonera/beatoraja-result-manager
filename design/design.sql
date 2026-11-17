CREATE TABLE IF NOT EXISTS _class(
  name TEXT PRIMARY KEY,
  module TEXT NOT NULL,
  layer TEXT NOT NULL,
  kind TEXT NOT NULL DEFAULT '通常' CHECK(kind IN('通常','基底','アダプタ')),
  responsibility TEXT NOT NULL,
  status TEXT NOT NULL DEFAULT '設計済み' CHECK(status IN('設計済み','テスト済み','実装済み')));
CREATE TABLE IF NOT EXISTS _dependency(
  from_class TEXT NOT NULL REFERENCES _class(name),
  to_class TEXT NOT NULL REFERENCES _class(name),
  kind TEXT NOT NULL CHECK(kind IN('継承','利用','生成')),
  reason TEXT NOT NULL,
  PRIMARY KEY(from_class,to_class));
CREATE TABLE IF NOT EXISTS _test(
  class TEXT NOT NULL REFERENCES _class(name),
  name TEXT NOT NULL,
  method TEXT,
  checks TEXT NOT NULL,
  status TEXT NOT NULL DEFAULT '未作成' CHECK(status IN('未作成','失敗確認済み','成功')),
  PRIMARY KEY(class,name));
CREATE TABLE IF NOT EXISTS _refactor(
  id INTEGER PRIMARY KEY,
  class TEXT NOT NULL REFERENCES _class(name),
  title TEXT NOT NULL,
  detail TEXT NOT NULL,
  priority TEXT NOT NULL DEFAULT '中' CHECK(priority IN('高','中','低')),
  status TEXT NOT NULL DEFAULT '候補' CHECK(status IN('候補','対応中','完了','見送り')));
CREATE TABLE IF NOT EXISTS "AccentButton"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL);
CREATE TABLE IF NOT EXISTS "AppConfig"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_AppLogging" TEXT REFERENCES "AppLogging"(method),
  "利用_AppPaths" TEXT REFERENCES "AppPaths"(method),
  "利用_AtomicFiles" TEXT REFERENCES "AtomicFiles"(method),
  "利用_ClearLampLabels" TEXT REFERENCES "ClearLampLabels"(method),
  "利用_PlayerKind" TEXT REFERENCES "PlayerKind"(method),
  "利用_PostTextFormat" TEXT REFERENCES "PostTextFormat"(method));
CREATE TABLE IF NOT EXISTS "AppDataLocation"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL);
CREATE TABLE IF NOT EXISTS "AppIcons"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_AppLogging" TEXT REFERENCES "AppLogging"(method));
CREATE TABLE IF NOT EXISTS "AppLogging"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_AppDataLocation" TEXT REFERENCES "AppDataLocation"(method));
CREATE TABLE IF NOT EXISTS "AppPaths"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_AppDataLocation" TEXT REFERENCES "AppDataLocation"(method),
  "利用_AppLogging" TEXT REFERENCES "AppLogging"(method));
CREATE TABLE IF NOT EXISTS "AppUpdateService"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_AppLogging" TEXT REFERENCES "AppLogging"(method),
  "利用_AppPaths" TEXT REFERENCES "AppPaths"(method),
  "利用_AppVersion" TEXT REFERENCES "AppVersion"(method),
  "生成_GitHubReleaseClient" TEXT REFERENCES "GitHubReleaseClient"(method),
  "利用_OsInfo" TEXT REFERENCES "OsInfo"(method),
  "利用_VersionComparator" TEXT REFERENCES "VersionComparator"(method),
  "利用_WindowsUpdateLauncher" TEXT REFERENCES "WindowsUpdateLauncher"(method),
  "利用_ZipExtractUtils" TEXT REFERENCES "ZipExtractUtils"(method));
CREATE TABLE IF NOT EXISTS "AppVersion"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL);
CREATE TABLE IF NOT EXISTS "AtomicFiles"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL);
CREATE TABLE IF NOT EXISTS "Badge"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_ResultPalette" TEXT REFERENCES "ResultPalette"(method),
  "利用_UiTheme" TEXT REFERENCES "UiTheme"(method));
CREATE TABLE IF NOT EXISTS "BadgeCellRenderer"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_Badge" TEXT REFERENCES "Badge"(method),
  "利用_UiTheme" TEXT REFERENCES "UiTheme"(method));
CREATE TABLE IF NOT EXISTS "BatchPoster"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_ScreenshotEntry" TEXT REFERENCES "ScreenshotEntry"(method));
CREATE TABLE IF NOT EXISTS "BeatorajaPaths"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_AppLogging" TEXT REFERENCES "AppLogging"(method));
CREATE TABLE IF NOT EXISTS "BmtTableReader"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL);
CREATE TABLE IF NOT EXISTS "BmzPaths"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_AppLogging" TEXT REFERENCES "AppLogging"(method));
CREATE TABLE IF NOT EXISTS "BmzPlayLogService"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_ClearTypeMapper" TEXT REFERENCES "ClearTypeMapper"(method),
  "生成_PlayHistory" TEXT REFERENCES "PlayHistory"(method),
  "生成_PlayLogEntry" TEXT REFERENCES "PlayLogEntry"(method),
  "継承_PlayLogSource" TEXT REFERENCES "PlayLogSource"(method),
  "生成_PlayScore" TEXT REFERENCES "PlayScore"(method));
CREATE TABLE IF NOT EXISTS "BmzTableReader"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "生成_BmtTableReader" TEXT REFERENCES "BmtTableReader"(method));
CREATE TABLE IF NOT EXISTS "CapturedAtCellRenderer"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_UiTheme" TEXT REFERENCES "UiTheme"(method));
CREATE TABLE IF NOT EXISTS "CardPanel"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_UiTheme" TEXT REFERENCES "UiTheme"(method));
CREATE TABLE IF NOT EXISTS "ChartResolverService"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_AppConfig" TEXT REFERENCES "AppConfig"(method),
  "利用_AppLogging" TEXT REFERENCES "AppLogging"(method),
  "利用_BeatorajaPaths" TEXT REFERENCES "BeatorajaPaths"(method),
  "利用_BmzPaths" TEXT REFERENCES "BmzPaths"(method),
  "生成_BmzPlayLogService" TEXT REFERENCES "BmzPlayLogService"(method),
  "利用_BmzTableReader" TEXT REFERENCES "BmzTableReader"(method),
  "利用_ClearTypeMapper" TEXT REFERENCES "ClearTypeMapper"(method),
  "生成_DifficultyTableRegistry" TEXT REFERENCES "DifficultyTableRegistry"(method),
  "生成_EnrichedScreenshot" TEXT REFERENCES "EnrichedScreenshot"(method),
  "利用_PlayLogEntry" TEXT REFERENCES "PlayLogEntry"(method),
  "利用_PlayLogSource" TEXT REFERENCES "PlayLogSource"(method),
  "利用_PlayScore" TEXT REFERENCES "PlayScore"(method),
  "利用_PlayerKind" TEXT REFERENCES "PlayerKind"(method),
  "生成_PlayerPlayLogService" TEXT REFERENCES "PlayerPlayLogService"(method),
  "生成_ScreenshotEntry" TEXT REFERENCES "ScreenshotEntry"(method),
  "生成_SongDatabaseService" TEXT REFERENCES "SongDatabaseService"(method),
  "生成_TableLevelParser" TEXT REFERENCES "TableLevelParser"(method));
CREATE TABLE IF NOT EXISTS "ChromiumBrowserLocator"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL);
CREATE TABLE IF NOT EXISTS "ChromiumCdpSession"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_AppLogging" TEXT REFERENCES "AppLogging"(method));
CREATE TABLE IF NOT EXISTS "ChromiumDevToolsClient"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_ChromiumCdpSession" TEXT REFERENCES "ChromiumCdpSession"(method),
  "生成_TwitterCookies" TEXT REFERENCES "TwitterCookies"(method));
CREATE TABLE IF NOT EXISTS "ChromiumLauncher"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL);
CREATE TABLE IF NOT EXISTS "ChromiumProfileLock"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_AppLogging" TEXT REFERENCES "AppLogging"(method));
CREATE TABLE IF NOT EXISTS "ClearLamp"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL);
CREATE TABLE IF NOT EXISTS "ClearLampLabelTableModel"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_ClearLampLabels" TEXT REFERENCES "ClearLampLabels"(method));
CREATE TABLE IF NOT EXISTS "ClearLampLabels"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_ClearLamp" TEXT REFERENCES "ClearLamp"(method));
CREATE TABLE IF NOT EXISTS "ClearTypeMapper"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_ClearLamp" TEXT REFERENCES "ClearLamp"(method));
CREATE TABLE IF NOT EXISTS "ClixAuthSupport"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_AppConfig" TEXT REFERENCES "AppConfig"(method),
  "利用_AppPaths" TEXT REFERENCES "AppPaths"(method));
CREATE TABLE IF NOT EXISTS "ClixLocator"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_AppLogging" TEXT REFERENCES "AppLogging"(method),
  "利用_AppPaths" TEXT REFERENCES "AppPaths"(method));
CREATE TABLE IF NOT EXISTS "ClixService"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_AppConfig" TEXT REFERENCES "AppConfig"(method),
  "利用_AppLogging" TEXT REFERENCES "AppLogging"(method),
  "利用_ClixAuthSupport" TEXT REFERENCES "ClixAuthSupport"(method),
  "利用_ClixLocator" TEXT REFERENCES "ClixLocator"(method),
  "生成_TwitterBrowserPostService" TEXT REFERENCES "TwitterBrowserPostService"(method));
CREATE TABLE IF NOT EXISTS "DatabasePanel"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_AppConfig" TEXT REFERENCES "AppConfig"(method),
  "利用_Badge" TEXT REFERENCES "Badge"(method),
  "生成_BadgeCellRenderer" TEXT REFERENCES "BadgeCellRenderer"(method),
  "生成_CapturedAtCellRenderer" TEXT REFERENCES "CapturedAtCellRenderer"(method),
  "利用_ClearLampLabels" TEXT REFERENCES "ClearLampLabels"(method),
  "生成_MultiColumnSortSupport" TEXT REFERENCES "MultiColumnSortSupport"(method),
  "利用_PostedStateStore" TEXT REFERENCES "PostedStateStore"(method),
  "利用_ResultPalette" TEXT REFERENCES "ResultPalette"(method),
  "利用_ScreenshotDatabase" TEXT REFERENCES "ScreenshotDatabase"(method),
  "生成_ScreenshotEntry" TEXT REFERENCES "ScreenshotEntry"(method),
  "生成_ScreenshotFileRemover" TEXT REFERENCES "ScreenshotFileRemover"(method),
  "利用_ScreenshotRecord" TEXT REFERENCES "ScreenshotRecord"(method),
  "生成_StateCell" TEXT REFERENCES "StateCell"(method),
  "生成_StateCellRenderer" TEXT REFERENCES "StateCellRenderer"(method),
  "生成_ThumbnailCache" TEXT REFERENCES "ThumbnailCache"(method),
  "生成_ThumbnailCellRenderer" TEXT REFERENCES "ThumbnailCellRenderer"(method),
  "利用_UiTheme" TEXT REFERENCES "UiTheme"(method));
CREATE TABLE IF NOT EXISTS "DifficultyTableRegistry"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_AppConfig" TEXT REFERENCES "AppConfig"(method),
  "生成_BmtTableReader" TEXT REFERENCES "BmtTableReader"(method),
  "利用_NotationPrefixResolver" TEXT REFERENCES "NotationPrefixResolver"(method),
  "利用_TableLevelParser" TEXT REFERENCES "TableLevelParser"(method));
CREATE TABLE IF NOT EXISTS "DiscordAutoPostQueue"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_AppLogging" TEXT REFERENCES "AppLogging"(method),
  "利用_PostedStateStore" TEXT REFERENCES "PostedStateStore"(method),
  "利用_ScreenshotEntry" TEXT REFERENCES "ScreenshotEntry"(method));
CREATE TABLE IF NOT EXISTS "DiscordWebhookService"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_AppLogging" TEXT REFERENCES "AppLogging"(method));
CREATE TABLE IF NOT EXISTS "EnrichedScreenshot"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_PlayScore" TEXT REFERENCES "PlayScore"(method),
  "利用_ScreenshotEntry" TEXT REFERENCES "ScreenshotEntry"(method));
CREATE TABLE IF NOT EXISTS "FilenameParser"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_ClearLamp" TEXT REFERENCES "ClearLamp"(method),
  "利用_PlayerKind" TEXT REFERENCES "PlayerKind"(method),
  "生成_ScreenshotEntry" TEXT REFERENCES "ScreenshotEntry"(method));
CREATE TABLE IF NOT EXISTS "FirstRunWizard"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "生成_AppConfig" TEXT REFERENCES "AppConfig"(method),
  "利用_BmzPaths" TEXT REFERENCES "BmzPaths"(method),
  "利用_ClixService" TEXT REFERENCES "ClixService"(method),
  "利用_PlayerKind" TEXT REFERENCES "PlayerKind"(method),
  "生成_TwitterLoginDialog" TEXT REFERENCES "TwitterLoginDialog"(method));
CREATE TABLE IF NOT EXISTS "GitHubReleaseClient"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_AppLogging" TEXT REFERENCES "AppLogging"(method),
  "利用_VersionComparator" TEXT REFERENCES "VersionComparator"(method));
CREATE TABLE IF NOT EXISTS "HelpDialog"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_AppPaths" TEXT REFERENCES "AppPaths"(method),
  "利用_AppVersion" TEXT REFERENCES "AppVersion"(method));
CREATE TABLE IF NOT EXISTS "LampGroupedTweetText"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_ClearLampLabels" TEXT REFERENCES "ClearLampLabels"(method),
  "利用_TweetTextGenerator" TEXT REFERENCES "TweetTextGenerator"(method),
  "利用_TweetTextLimits" TEXT REFERENCES "TweetTextLimits"(method));
CREATE TABLE IF NOT EXISTS "Main"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_AppConfig" TEXT REFERENCES "AppConfig"(method),
  "利用_AppIcons" TEXT REFERENCES "AppIcons"(method),
  "利用_AppLogging" TEXT REFERENCES "AppLogging"(method),
  "利用_AppPaths" TEXT REFERENCES "AppPaths"(method),
  "生成_AppUpdateService" TEXT REFERENCES "AppUpdateService"(method),
  "利用_AppVersion" TEXT REFERENCES "AppVersion"(method),
  "生成_FirstRunWizard" TEXT REFERENCES "FirstRunWizard"(method),
  "生成_MainFrame" TEXT REFERENCES "MainFrame"(method),
  "利用_UiTheme" TEXT REFERENCES "UiTheme"(method),
  "生成_UpdateProgressDialog" TEXT REFERENCES "UpdateProgressDialog"(method));
CREATE TABLE IF NOT EXISTS "MainFrame"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "生成_AccentButton" TEXT REFERENCES "AccentButton"(method),
  "利用_AppConfig" TEXT REFERENCES "AppConfig"(method),
  "利用_AppIcons" TEXT REFERENCES "AppIcons"(method),
  "利用_AppLogging" TEXT REFERENCES "AppLogging"(method),
  "利用_AppVersion" TEXT REFERENCES "AppVersion"(method),
  "利用_BatchPoster" TEXT REFERENCES "BatchPoster"(method),
  "生成_ChartResolverService" TEXT REFERENCES "ChartResolverService"(method),
  "生成_ClixService" TEXT REFERENCES "ClixService"(method),
  "生成_DatabasePanel" TEXT REFERENCES "DatabasePanel"(method),
  "利用_DifficultyTableRegistry" TEXT REFERENCES "DifficultyTableRegistry"(method),
  "生成_DiscordAutoPostQueue" TEXT REFERENCES "DiscordAutoPostQueue"(method),
  "生成_DiscordWebhookService" TEXT REFERENCES "DiscordWebhookService"(method),
  "利用_EnrichedScreenshot" TEXT REFERENCES "EnrichedScreenshot"(method),
  "利用_HelpDialog" TEXT REFERENCES "HelpDialog"(method),
  "生成_LampGroupedTweetText" TEXT REFERENCES "LampGroupedTweetText"(method),
  "利用_PlayerKind" TEXT REFERENCES "PlayerKind"(method),
  "利用_PostBatchSplitter" TEXT REFERENCES "PostBatchSplitter"(method),
  "利用_PostConfirmDialog" TEXT REFERENCES "PostConfirmDialog"(method),
  "生成_PostTextComposer" TEXT REFERENCES "PostTextComposer"(method),
  "利用_PostTextFormat" TEXT REFERENCES "PostTextFormat"(method),
  "生成_PostTextFormatDialog" TEXT REFERENCES "PostTextFormatDialog"(method),
  "利用_PostedStateStore" TEXT REFERENCES "PostedStateStore"(method),
  "生成_PreviewPanel" TEXT REFERENCES "PreviewPanel"(method),
  "生成_ScreenshotDatabase" TEXT REFERENCES "ScreenshotDatabase"(method),
  "利用_ScreenshotEntry" TEXT REFERENCES "ScreenshotEntry"(method),
  "生成_ScreenshotFolderCleanupService" TEXT REFERENCES "ScreenshotFolderCleanupService"(method),
  "利用_ScreenshotRecord" TEXT REFERENCES "ScreenshotRecord"(method),
  "生成_ScreenshotScanner" TEXT REFERENCES "ScreenshotScanner"(method),
  "生成_ScreenshotWatcher" TEXT REFERENCES "ScreenshotWatcher"(method),
  "生成_SettingsDialog" TEXT REFERENCES "SettingsDialog"(method),
  "生成_TableNotationRulesDialog" TEXT REFERENCES "TableNotationRulesDialog"(method),
  "生成_TablePriorityDialog" TEXT REFERENCES "TablePriorityDialog"(method),
  "生成_TwitterAuthService" TEXT REFERENCES "TwitterAuthService"(method),
  "生成_TwitterLoginDialog" TEXT REFERENCES "TwitterLoginDialog"(method),
  "利用_UiTheme" TEXT REFERENCES "UiTheme"(method));
CREATE TABLE IF NOT EXISTS "MultiColumnSortSupport"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL);
CREATE TABLE IF NOT EXISTS "NotationPrefixResolver"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_TableLevelParser" TEXT REFERENCES "TableLevelParser"(method));
CREATE TABLE IF NOT EXISTS "OsInfo"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL);
CREATE TABLE IF NOT EXISTS "PlayHistory"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_PlayLogEntry" TEXT REFERENCES "PlayLogEntry"(method));
CREATE TABLE IF NOT EXISTS "PlayLogEntry"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_PlayScore" TEXT REFERENCES "PlayScore"(method));
CREATE TABLE IF NOT EXISTS "PlayLogSource"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_PlayLogEntry" TEXT REFERENCES "PlayLogEntry"(method));
CREATE TABLE IF NOT EXISTS "PlayScore"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL);
CREATE TABLE IF NOT EXISTS "PlayerKind"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_BmzPaths" TEXT REFERENCES "BmzPaths"(method));
CREATE TABLE IF NOT EXISTS "PlayerPlayLogService"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "生成_PlayHistory" TEXT REFERENCES "PlayHistory"(method),
  "生成_PlayLogEntry" TEXT REFERENCES "PlayLogEntry"(method),
  "継承_PlayLogSource" TEXT REFERENCES "PlayLogSource"(method),
  "生成_PlayScore" TEXT REFERENCES "PlayScore"(method));
CREATE TABLE IF NOT EXISTS "PostBatchSplitter"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL);
CREATE TABLE IF NOT EXISTS "PostConfirmDialog"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_TweetTextLimits" TEXT REFERENCES "TweetTextLimits"(method),
  "利用_UiTheme" TEXT REFERENCES "UiTheme"(method));
CREATE TABLE IF NOT EXISTS "PostTextComposer"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_AppConfig" TEXT REFERENCES "AppConfig"(method),
  "生成_LampGroupedTweetText" TEXT REFERENCES "LampGroupedTweetText"(method),
  "利用_PlayScore" TEXT REFERENCES "PlayScore"(method),
  "利用_ScreenshotEntry" TEXT REFERENCES "ScreenshotEntry"(method),
  "利用_TweetTextGenerator" TEXT REFERENCES "TweetTextGenerator"(method),
  "利用_TweetTextLimits" TEXT REFERENCES "TweetTextLimits"(method));
CREATE TABLE IF NOT EXISTS "PostTextFormat"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_PlayScore" TEXT REFERENCES "PlayScore"(method));
CREATE TABLE IF NOT EXISTS "PostTextFormatDialog"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_AppIcons" TEXT REFERENCES "AppIcons"(method),
  "生成_PlayScore" TEXT REFERENCES "PlayScore"(method),
  "生成_PostTextFormat" TEXT REFERENCES "PostTextFormat"(method),
  "生成_ScreenshotEntry" TEXT REFERENCES "ScreenshotEntry"(method),
  "利用_TweetTextGenerator" TEXT REFERENCES "TweetTextGenerator"(method));
CREATE TABLE IF NOT EXISTS "PostedStateStore"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_AppLogging" TEXT REFERENCES "AppLogging"(method),
  "利用_AppPaths" TEXT REFERENCES "AppPaths"(method),
  "利用_AtomicFiles" TEXT REFERENCES "AtomicFiles"(method));
CREATE TABLE IF NOT EXISTS "PreviewPanel"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_AppLogging" TEXT REFERENCES "AppLogging"(method),
  "利用_Badge" TEXT REFERENCES "Badge"(method),
  "生成_CardPanel" TEXT REFERENCES "CardPanel"(method),
  "利用_ClearLampLabels" TEXT REFERENCES "ClearLampLabels"(method),
  "利用_ResultPalette" TEXT REFERENCES "ResultPalette"(method),
  "利用_ScreenshotEntry" TEXT REFERENCES "ScreenshotEntry"(method),
  "利用_UiTheme" TEXT REFERENCES "UiTheme"(method));
CREATE TABLE IF NOT EXISTS "ProcessUtils"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_AppLogging" TEXT REFERENCES "AppLogging"(method),
  "利用_OsInfo" TEXT REFERENCES "OsInfo"(method));
CREATE TABLE IF NOT EXISTS "ResultPalette"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_ClearLamp" TEXT REFERENCES "ClearLamp"(method),
  "利用_UiTheme" TEXT REFERENCES "UiTheme"(method));
CREATE TABLE IF NOT EXISTS "ScreenshotDatabase"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_AppPaths" TEXT REFERENCES "AppPaths"(method),
  "利用_EnrichedScreenshot" TEXT REFERENCES "EnrichedScreenshot"(method),
  "生成_ScreenshotRecord" TEXT REFERENCES "ScreenshotRecord"(method));
CREATE TABLE IF NOT EXISTS "ScreenshotEntry"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL);
CREATE TABLE IF NOT EXISTS "ScreenshotFileRemover"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_ScreenshotRecord" TEXT REFERENCES "ScreenshotRecord"(method));
CREATE TABLE IF NOT EXISTS "ScreenshotFolderCleanupService"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_AppLogging" TEXT REFERENCES "AppLogging"(method),
  "利用_ScreenshotEntry" TEXT REFERENCES "ScreenshotEntry"(method),
  "生成_ScreenshotScanner" TEXT REFERENCES "ScreenshotScanner"(method));
CREATE TABLE IF NOT EXISTS "ScreenshotRecord"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL);
CREATE TABLE IF NOT EXISTS "ScreenshotScanner"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_FilenameParser" TEXT REFERENCES "FilenameParser"(method),
  "利用_PlayerKind" TEXT REFERENCES "PlayerKind"(method),
  "利用_ScreenshotEntry" TEXT REFERENCES "ScreenshotEntry"(method));
CREATE TABLE IF NOT EXISTS "ScreenshotWatcher"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_AppLogging" TEXT REFERENCES "AppLogging"(method),
  "利用_FilenameParser" TEXT REFERENCES "FilenameParser"(method),
  "利用_PlayerKind" TEXT REFERENCES "PlayerKind"(method),
  "利用_ScreenshotEntry" TEXT REFERENCES "ScreenshotEntry"(method));
CREATE TABLE IF NOT EXISTS "SettingsDialog"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "生成_AppConfig" TEXT REFERENCES "AppConfig"(method),
  "利用_AppIcons" TEXT REFERENCES "AppIcons"(method),
  "利用_AppLogging" TEXT REFERENCES "AppLogging"(method),
  "利用_AppPaths" TEXT REFERENCES "AppPaths"(method),
  "利用_BeatorajaPaths" TEXT REFERENCES "BeatorajaPaths"(method),
  "利用_BmzPaths" TEXT REFERENCES "BmzPaths"(method),
  "生成_ClearLampLabelTableModel" TEXT REFERENCES "ClearLampLabelTableModel"(method),
  "利用_ClixService" TEXT REFERENCES "ClixService"(method),
  "生成_PlayerKind" TEXT REFERENCES "PlayerKind"(method),
  "生成_TwitterAuthService" TEXT REFERENCES "TwitterAuthService"(method),
  "生成_TwitterLoginDialog" TEXT REFERENCES "TwitterLoginDialog"(method),
  "利用_UiTheme" TEXT REFERENCES "UiTheme"(method),
  "生成_WebhookTableModel" TEXT REFERENCES "WebhookTableModel"(method));
CREATE TABLE IF NOT EXISTS "SongDatabaseService"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL);
CREATE TABLE IF NOT EXISTS "StateCell"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL);
CREATE TABLE IF NOT EXISTS "StateCellRenderer"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_Badge" TEXT REFERENCES "Badge"(method),
  "利用_ResultPalette" TEXT REFERENCES "ResultPalette"(method),
  "生成_StateCell" TEXT REFERENCES "StateCell"(method),
  "利用_UiTheme" TEXT REFERENCES "UiTheme"(method));
CREATE TABLE IF NOT EXISTS "TableLevelParser"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL);
CREATE TABLE IF NOT EXISTS "TableNotationRulesDialog"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "生成_AppConfig" TEXT REFERENCES "AppConfig"(method),
  "利用_AppIcons" TEXT REFERENCES "AppIcons"(method),
  "利用_NotationPrefixResolver" TEXT REFERENCES "NotationPrefixResolver"(method));
CREATE TABLE IF NOT EXISTS "TablePriorityDialog"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_AppIcons" TEXT REFERENCES "AppIcons"(method));
CREATE TABLE IF NOT EXISTS "ThumbnailCache"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_AppLogging" TEXT REFERENCES "AppLogging"(method),
  "利用_UiTheme" TEXT REFERENCES "UiTheme"(method));
CREATE TABLE IF NOT EXISTS "ThumbnailCellRenderer"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_ThumbnailCache" TEXT REFERENCES "ThumbnailCache"(method),
  "利用_UiTheme" TEXT REFERENCES "UiTheme"(method));
CREATE TABLE IF NOT EXISTS "TweetTextGenerator"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_ClearLampLabels" TEXT REFERENCES "ClearLampLabels"(method),
  "利用_PlayScore" TEXT REFERENCES "PlayScore"(method),
  "利用_PostTextFormat" TEXT REFERENCES "PostTextFormat"(method),
  "利用_ScreenshotEntry" TEXT REFERENCES "ScreenshotEntry"(method),
  "利用_TweetTextLimits" TEXT REFERENCES "TweetTextLimits"(method));
CREATE TABLE IF NOT EXISTS "TweetTextLimits"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL);
CREATE TABLE IF NOT EXISTS "TwitterAuthService"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_AppConfig" TEXT REFERENCES "AppConfig"(method),
  "利用_AppLogging" TEXT REFERENCES "AppLogging"(method),
  "生成_ClixService" TEXT REFERENCES "ClixService"(method),
  "生成_TwitterChromeLoginService" TEXT REFERENCES "TwitterChromeLoginService"(method),
  "利用_TwitterCookies" TEXT REFERENCES "TwitterCookies"(method));
CREATE TABLE IF NOT EXISTS "TwitterBrowserPostService"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_AppLogging" TEXT REFERENCES "AppLogging"(method),
  "利用_AppPaths" TEXT REFERENCES "AppPaths"(method),
  "利用_ChromiumBrowserLocator" TEXT REFERENCES "ChromiumBrowserLocator"(method),
  "利用_ChromiumCdpSession" TEXT REFERENCES "ChromiumCdpSession"(method),
  "生成_ChromiumDevToolsClient" TEXT REFERENCES "ChromiumDevToolsClient"(method),
  "利用_ChromiumLauncher" TEXT REFERENCES "ChromiumLauncher"(method),
  "利用_ChromiumProfileLock" TEXT REFERENCES "ChromiumProfileLock"(method),
  "利用_ClixService" TEXT REFERENCES "ClixService"(method),
  "利用_ProcessUtils" TEXT REFERENCES "ProcessUtils"(method));
CREATE TABLE IF NOT EXISTS "TwitterChromeLoginService"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_AppLogging" TEXT REFERENCES "AppLogging"(method),
  "利用_AppPaths" TEXT REFERENCES "AppPaths"(method),
  "利用_ChromiumBrowserLocator" TEXT REFERENCES "ChromiumBrowserLocator"(method),
  "生成_ChromiumDevToolsClient" TEXT REFERENCES "ChromiumDevToolsClient"(method),
  "利用_ChromiumLauncher" TEXT REFERENCES "ChromiumLauncher"(method),
  "利用_ChromiumProfileLock" TEXT REFERENCES "ChromiumProfileLock"(method),
  "利用_ProcessUtils" TEXT REFERENCES "ProcessUtils"(method),
  "利用_TwitterCookies" TEXT REFERENCES "TwitterCookies"(method));
CREATE TABLE IF NOT EXISTS "TwitterCookies"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL);
CREATE TABLE IF NOT EXISTS "TwitterLoginDialog"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_AppConfig" TEXT REFERENCES "AppConfig"(method),
  "利用_AppIcons" TEXT REFERENCES "AppIcons"(method),
  "利用_ClixService" TEXT REFERENCES "ClixService"(method),
  "生成_TwitterAuthService" TEXT REFERENCES "TwitterAuthService"(method));
CREATE TABLE IF NOT EXISTS "UiTheme"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_AppConfig" TEXT REFERENCES "AppConfig"(method),
  "利用_AppLogging" TEXT REFERENCES "AppLogging"(method),
  "利用_ResultPalette" TEXT REFERENCES "ResultPalette"(method));
CREATE TABLE IF NOT EXISTS "UpdateProgressDialog"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_AppIcons" TEXT REFERENCES "AppIcons"(method));
CREATE TABLE IF NOT EXISTS "VersionComparator"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL);
CREATE TABLE IF NOT EXISTS "WebhookTableModel"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "生成_AppConfig" TEXT REFERENCES "AppConfig"(method));
CREATE TABLE IF NOT EXISTS "WindowsUpdateLauncher"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL,
  "利用_OsInfo" TEXT REFERENCES "OsInfo"(method));
CREATE TABLE IF NOT EXISTS "ZipExtractUtils"(
  method TEXT PRIMARY KEY,
  signature TEXT NOT NULL,
  behavior TEXT NOT NULL);
CREATE VIEW IF NOT EXISTS v_method AS
  SELECT c.layer AS 層, m.class AS クラス, m.method AS メソッド, m.signature AS 引数と戻り値, m.behavior AS 処理内容
  FROM (SELECT 'AccentButton' AS class, method, signature, behavior FROM "AccentButton"
  UNION ALL SELECT 'AppConfig' AS class, method, signature, behavior FROM "AppConfig"
  UNION ALL SELECT 'AppDataLocation' AS class, method, signature, behavior FROM "AppDataLocation"
  UNION ALL SELECT 'AppIcons' AS class, method, signature, behavior FROM "AppIcons"
  UNION ALL SELECT 'AppLogging' AS class, method, signature, behavior FROM "AppLogging"
  UNION ALL SELECT 'AppPaths' AS class, method, signature, behavior FROM "AppPaths"
  UNION ALL SELECT 'AppUpdateService' AS class, method, signature, behavior FROM "AppUpdateService"
  UNION ALL SELECT 'AppVersion' AS class, method, signature, behavior FROM "AppVersion"
  UNION ALL SELECT 'AtomicFiles' AS class, method, signature, behavior FROM "AtomicFiles"
  UNION ALL SELECT 'Badge' AS class, method, signature, behavior FROM "Badge"
  UNION ALL SELECT 'BadgeCellRenderer' AS class, method, signature, behavior FROM "BadgeCellRenderer"
  UNION ALL SELECT 'BatchPoster' AS class, method, signature, behavior FROM "BatchPoster"
  UNION ALL SELECT 'BeatorajaPaths' AS class, method, signature, behavior FROM "BeatorajaPaths"
  UNION ALL SELECT 'BmtTableReader' AS class, method, signature, behavior FROM "BmtTableReader"
  UNION ALL SELECT 'BmzPaths' AS class, method, signature, behavior FROM "BmzPaths"
  UNION ALL SELECT 'BmzPlayLogService' AS class, method, signature, behavior FROM "BmzPlayLogService"
  UNION ALL SELECT 'BmzTableReader' AS class, method, signature, behavior FROM "BmzTableReader"
  UNION ALL SELECT 'CapturedAtCellRenderer' AS class, method, signature, behavior FROM "CapturedAtCellRenderer"
  UNION ALL SELECT 'CardPanel' AS class, method, signature, behavior FROM "CardPanel"
  UNION ALL SELECT 'ChartResolverService' AS class, method, signature, behavior FROM "ChartResolverService"
  UNION ALL SELECT 'ChromiumBrowserLocator' AS class, method, signature, behavior FROM "ChromiumBrowserLocator"
  UNION ALL SELECT 'ChromiumCdpSession' AS class, method, signature, behavior FROM "ChromiumCdpSession"
  UNION ALL SELECT 'ChromiumDevToolsClient' AS class, method, signature, behavior FROM "ChromiumDevToolsClient"
  UNION ALL SELECT 'ChromiumLauncher' AS class, method, signature, behavior FROM "ChromiumLauncher"
  UNION ALL SELECT 'ChromiumProfileLock' AS class, method, signature, behavior FROM "ChromiumProfileLock"
  UNION ALL SELECT 'ClearLamp' AS class, method, signature, behavior FROM "ClearLamp"
  UNION ALL SELECT 'ClearLampLabelTableModel' AS class, method, signature, behavior FROM "ClearLampLabelTableModel"
  UNION ALL SELECT 'ClearLampLabels' AS class, method, signature, behavior FROM "ClearLampLabels"
  UNION ALL SELECT 'ClearTypeMapper' AS class, method, signature, behavior FROM "ClearTypeMapper"
  UNION ALL SELECT 'ClixAuthSupport' AS class, method, signature, behavior FROM "ClixAuthSupport"
  UNION ALL SELECT 'ClixLocator' AS class, method, signature, behavior FROM "ClixLocator"
  UNION ALL SELECT 'ClixService' AS class, method, signature, behavior FROM "ClixService"
  UNION ALL SELECT 'DatabasePanel' AS class, method, signature, behavior FROM "DatabasePanel"
  UNION ALL SELECT 'DifficultyTableRegistry' AS class, method, signature, behavior FROM "DifficultyTableRegistry"
  UNION ALL SELECT 'DiscordAutoPostQueue' AS class, method, signature, behavior FROM "DiscordAutoPostQueue"
  UNION ALL SELECT 'DiscordWebhookService' AS class, method, signature, behavior FROM "DiscordWebhookService"
  UNION ALL SELECT 'EnrichedScreenshot' AS class, method, signature, behavior FROM "EnrichedScreenshot"
  UNION ALL SELECT 'FilenameParser' AS class, method, signature, behavior FROM "FilenameParser"
  UNION ALL SELECT 'FirstRunWizard' AS class, method, signature, behavior FROM "FirstRunWizard"
  UNION ALL SELECT 'GitHubReleaseClient' AS class, method, signature, behavior FROM "GitHubReleaseClient"
  UNION ALL SELECT 'HelpDialog' AS class, method, signature, behavior FROM "HelpDialog"
  UNION ALL SELECT 'LampGroupedTweetText' AS class, method, signature, behavior FROM "LampGroupedTweetText"
  UNION ALL SELECT 'Main' AS class, method, signature, behavior FROM "Main"
  UNION ALL SELECT 'MainFrame' AS class, method, signature, behavior FROM "MainFrame"
  UNION ALL SELECT 'MultiColumnSortSupport' AS class, method, signature, behavior FROM "MultiColumnSortSupport"
  UNION ALL SELECT 'NotationPrefixResolver' AS class, method, signature, behavior FROM "NotationPrefixResolver"
  UNION ALL SELECT 'OsInfo' AS class, method, signature, behavior FROM "OsInfo"
  UNION ALL SELECT 'PlayHistory' AS class, method, signature, behavior FROM "PlayHistory"
  UNION ALL SELECT 'PlayLogEntry' AS class, method, signature, behavior FROM "PlayLogEntry"
  UNION ALL SELECT 'PlayLogSource' AS class, method, signature, behavior FROM "PlayLogSource"
  UNION ALL SELECT 'PlayScore' AS class, method, signature, behavior FROM "PlayScore"
  UNION ALL SELECT 'PlayerKind' AS class, method, signature, behavior FROM "PlayerKind"
  UNION ALL SELECT 'PlayerPlayLogService' AS class, method, signature, behavior FROM "PlayerPlayLogService"
  UNION ALL SELECT 'PostBatchSplitter' AS class, method, signature, behavior FROM "PostBatchSplitter"
  UNION ALL SELECT 'PostConfirmDialog' AS class, method, signature, behavior FROM "PostConfirmDialog"
  UNION ALL SELECT 'PostTextComposer' AS class, method, signature, behavior FROM "PostTextComposer"
  UNION ALL SELECT 'PostTextFormat' AS class, method, signature, behavior FROM "PostTextFormat"
  UNION ALL SELECT 'PostTextFormatDialog' AS class, method, signature, behavior FROM "PostTextFormatDialog"
  UNION ALL SELECT 'PostedStateStore' AS class, method, signature, behavior FROM "PostedStateStore"
  UNION ALL SELECT 'PreviewPanel' AS class, method, signature, behavior FROM "PreviewPanel"
  UNION ALL SELECT 'ProcessUtils' AS class, method, signature, behavior FROM "ProcessUtils"
  UNION ALL SELECT 'ResultPalette' AS class, method, signature, behavior FROM "ResultPalette"
  UNION ALL SELECT 'ScreenshotDatabase' AS class, method, signature, behavior FROM "ScreenshotDatabase"
  UNION ALL SELECT 'ScreenshotEntry' AS class, method, signature, behavior FROM "ScreenshotEntry"
  UNION ALL SELECT 'ScreenshotFileRemover' AS class, method, signature, behavior FROM "ScreenshotFileRemover"
  UNION ALL SELECT 'ScreenshotFolderCleanupService' AS class, method, signature, behavior FROM "ScreenshotFolderCleanupService"
  UNION ALL SELECT 'ScreenshotRecord' AS class, method, signature, behavior FROM "ScreenshotRecord"
  UNION ALL SELECT 'ScreenshotScanner' AS class, method, signature, behavior FROM "ScreenshotScanner"
  UNION ALL SELECT 'ScreenshotWatcher' AS class, method, signature, behavior FROM "ScreenshotWatcher"
  UNION ALL SELECT 'SettingsDialog' AS class, method, signature, behavior FROM "SettingsDialog"
  UNION ALL SELECT 'SongDatabaseService' AS class, method, signature, behavior FROM "SongDatabaseService"
  UNION ALL SELECT 'StateCell' AS class, method, signature, behavior FROM "StateCell"
  UNION ALL SELECT 'StateCellRenderer' AS class, method, signature, behavior FROM "StateCellRenderer"
  UNION ALL SELECT 'TableLevelParser' AS class, method, signature, behavior FROM "TableLevelParser"
  UNION ALL SELECT 'TableNotationRulesDialog' AS class, method, signature, behavior FROM "TableNotationRulesDialog"
  UNION ALL SELECT 'TablePriorityDialog' AS class, method, signature, behavior FROM "TablePriorityDialog"
  UNION ALL SELECT 'ThumbnailCache' AS class, method, signature, behavior FROM "ThumbnailCache"
  UNION ALL SELECT 'ThumbnailCellRenderer' AS class, method, signature, behavior FROM "ThumbnailCellRenderer"
  UNION ALL SELECT 'TweetTextGenerator' AS class, method, signature, behavior FROM "TweetTextGenerator"
  UNION ALL SELECT 'TweetTextLimits' AS class, method, signature, behavior FROM "TweetTextLimits"
  UNION ALL SELECT 'TwitterAuthService' AS class, method, signature, behavior FROM "TwitterAuthService"
  UNION ALL SELECT 'TwitterBrowserPostService' AS class, method, signature, behavior FROM "TwitterBrowserPostService"
  UNION ALL SELECT 'TwitterChromeLoginService' AS class, method, signature, behavior FROM "TwitterChromeLoginService"
  UNION ALL SELECT 'TwitterCookies' AS class, method, signature, behavior FROM "TwitterCookies"
  UNION ALL SELECT 'TwitterLoginDialog' AS class, method, signature, behavior FROM "TwitterLoginDialog"
  UNION ALL SELECT 'UiTheme' AS class, method, signature, behavior FROM "UiTheme"
  UNION ALL SELECT 'UpdateProgressDialog' AS class, method, signature, behavior FROM "UpdateProgressDialog"
  UNION ALL SELECT 'VersionComparator' AS class, method, signature, behavior FROM "VersionComparator"
  UNION ALL SELECT 'WebhookTableModel' AS class, method, signature, behavior FROM "WebhookTableModel"
  UNION ALL SELECT 'WindowsUpdateLauncher' AS class, method, signature, behavior FROM "WindowsUpdateLauncher"
  UNION ALL SELECT 'ZipExtractUtils' AS class, method, signature, behavior FROM "ZipExtractUtils") m JOIN _class c ON c.name=m.class ORDER BY c.layer, m.class, m.method;
CREATE VIEW IF NOT EXISTS v_class_detail AS
  SELECT c.layer AS 層, c.name AS クラス, c.kind AS 種類, c.status AS 状態, c.module AS モジュール, c.responsibility AS 責務,
    (SELECT group_concat(m.引数と戻り値 || ' … ' || m.処理内容, char(10)) FROM v_method m WHERE m.クラス=c.name) AS メソッド
  FROM _class c ORDER BY c.layer, c.name;
CREATE VIEW IF NOT EXISTS v_dependency AS
  SELECT d.from_class AS 依存元, f.layer AS 依存元の層, d.to_class AS 依存先, t.layer AS 依存先の層, d.kind AS 種類, d.reason AS 理由
  FROM _dependency d JOIN _class f ON f.name=d.from_class JOIN _class t ON t.name=d.to_class ORDER BY f.layer, d.from_class, d.to_class;
CREATE VIEW IF NOT EXISTS v_test AS
  SELECT t.class AS クラス, t.method AS メソッド, t.name AS テスト名, t.checks AS 確かめること, t.status AS 状態
  FROM _test t ORDER BY t.class, t.name;
CREATE VIEW IF NOT EXISTS v_refactor AS
  SELECT r.id AS 番号, r.priority AS 優先度, r.status AS 状態, c.layer AS 層, r.class AS クラス, r.title AS 件名, r.detail AS 内容
  FROM _refactor r JOIN _class c ON c.name=r.class ORDER BY CASE r.status WHEN '対応中' THEN 0 WHEN '候補' THEN 1 ELSE 2 END, CASE r.priority WHEN '高' THEN 0 WHEN '中' THEN 1 ELSE 2 END, r.id;
CREATE VIEW IF NOT EXISTS v_progress AS
  SELECT status AS 状態, count(*) AS クラス数 FROM _class GROUP BY status ORDER BY status;

INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('AccentButton','src/main/java/com/beatoraja/screenshot/ui/theme/AccentButton.java','ui.theme','通常','Twitter・Discordのブランド色などで塗りつぶした角丸ボタン。有効なときは指定色の背景に白文字で描き、無効にすると背景と文字色を外して標準の見た目に戻す。メイン画面の「Twitter に投稿」「Discord に送信」ボタンに使われる。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('AppConfig','src/main/java/com/beatoraja/screenshot/config/AppConfig.java','config','通常','アプリの設定一式を1つのオブジェクトに持ち、アプリデータフォルダの設定JSONファイルとの読み書きを行う。スクショフォルダ・プレイヤーフォルダ・使うプレイヤー種別、Discordのwebhookと自動投稿、Xのログイン情報(Cookie)、画面テーマとフォント、難易度表の優先順と表記ルール、投稿文の書式、クリアランプの表示名など、互いに関係の薄い設定をまとめて扱う。値の設定時には空値の補正や範囲の丸めを行い、古い形式の設定(個別保存のXトークン、未設定のプレイヤー種別)も読み替える。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('AppDataLocation','src/main/java/com/beatoraja/screenshot/util/AppDataLocation.java','util','通常','アプリのデータフォルダ(設定・投稿済み記録・ログ・ブラウザプロファイルの置き場)の場所を1か所で決める。状態を持たない小さなクラスにしてあり、ログの初期化が AppPaths の初期化(フォルダ作成など)を引き起こさずに場所を知れる。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('AppIcons','src/main/java/com/beatoraja/screenshot/util/AppIcons.java','util','通常','アプリのウィンドウに付けるアイコン画像を用意する。クラスパス上の /icons/app-icon.png を一度だけ読み込んで使い回し、画像が無ければ何もしない。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('AppLogging','src/main/java/com/beatoraja/screenshot/util/AppLogging.java','util','通常','アプリ全体のログ出力を設定し、ロガーを配る。初回呼び出し時にログフォルダへ最大1MB×5世代の回転するファイル出力と、INFO 以上の画面出力を設定する。あわせて、Discord の Webhook URL や X の auth_token・ct0 をログに残さないよう伏せ字にする関数を持つ。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('AppPaths','src/main/java/com/beatoraja/screenshot/util/AppPaths.java','util','通常','アプリが使うファイルやフォルダの置き場所を一か所で決める。設定ファイル・投稿済み記録・clix 用ホーム・X ログイン用ブラウザプロファイルは %APPDATA%\beatoraja-screenshot-manager の下に置き、同梱の clix.exe は実行ファイルのあるフォルダの tools の下とする。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('AppUpdateService','src/main/java/com/beatoraja/screenshot/service/update/AppUpdateService.java','service.update','アダプタ','アプリの自動更新の流れをまとめて受け持つ。Windows の配布版で自動更新が有効なときだけ、GitHub の最新リリースと今のバージョンを比べ、新しければ zip を一時フォルダにダウンロード・展開し、更新用スクリプトを起動して再起動に回す。途中で失敗したら一時フォルダを片付ける。進み具合は呼び出し側の通知先に文言とダウンロード量で知らせ、結果は対象外・最新・失敗・再起動中の4通りで返す。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('AppVersion','src/main/java/com/beatoraja/screenshot/util/AppVersion.java','util','通常','アプリの版番号を返す。JAR のマニフェストに書かれた実装版を読み、無ければ開発中を表す dev とする。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('AtomicFiles','src/main/java/com/beatoraja/screenshot/util/AtomicFiles.java','util','通常','書き込み途中で落ちても壊れたファイルを残さない書き込みと、読めないファイルを退避する処理を提供する。設定ファイルと投稿済み記録の保存で使う。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('Badge','src/main/java/com/beatoraja/screenshot/ui/theme/Badge.java','ui.theme','通常','ランプ・ランク・画面種別などを角丸の「ピル」として描く共通部品。指定色を背景色に薄く混ぜた塗りと枠を描き、背景の明るさに合わせて読みやすい文字色に補正する。表のセル描画用の静的な描画関数と、通常のレイアウトに置けるラベル部品の両方を提供する。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('BadgeCellRenderer','src/main/java/com/beatoraja/screenshot/ui/render/BadgeCellRenderer.java','ui.render','通常','表のセル値をピル1個として描くセル描画部品。クリアランプ・ランク・難易度表記号の列で使い、色はセルの生の値から、表示文字は別の変換関数（ユーザー設定の表示名など）から決める。値が空か「-」ならピルの代わりに薄い「-」を描く。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('BatchPoster','src/main/java/com/beatoraja/screenshot/service/BatchPoster.java','service','通常','スクショの組(バッチ)を順に1件ずつ送り、失敗した時点で止める。送信そのものは渡された関数 Sender(投稿1回を送り、成功なら null、失敗ならエラー内容を返す)に任せ、X と Discord の手動投稿で同じ手順を使う。送れた分のスクショと失敗の内容を返す。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('BeatorajaPaths','src/main/java/com/beatoraja/screenshot/player/BeatorajaPaths.java','player','通常','beatorajaのフォルダにある config_sys.json を読み、プレイヤーフォルダ・プレイログDB(scoredatalog.db / scorelog.db)・楽曲DB(songdata.db)・難易度表フォルダの場所を割り出して保持する。設定にある相対パスはbeatorajaフォルダ基準で絶対パスにする。設定画面向けに、プレイヤーフォルダ配下のプレイヤー名一覧も返す。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('BmtTableReader','src/main/java/com/beatoraja/screenshot/table/BmtTableReader.java','table','通常','beatorajaが難易度表フォルダに保存する .bmt ファイル(gzip圧縮のJSON)を読み、表名・タグ・レベルごとのフォルダ・曲(タイトル・md5・sha256)の形に変換する。フォルダ内の全 .bmt をまとめて読み、読めなかったファイル名も一緒に返す。ここで定義する表データの形は、BMZ Player の表を読むときにも共通で使われる。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('BmzPaths','src/main/java/com/beatoraja/screenshot/player/BmzPaths.java','player','通常','BMZ Player のデータフォルダ(config.toml があるフォルダ)を見つけ、config.toml から使用中のプロフィールとスクショ保存先を読んで、プロフィールの score.db・library.db・スクショフォルダの場所を割り出して保持する。exeのあるフォルダが選ばれた場合は、その下の data フォルダもデータフォルダとして探す。設定画面向けにプロフィールID一覧も返す。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('BmzPlayLogService','src/main/java/com/beatoraja/screenshot/player/BmzPlayLogService.java','player','通常','BMZ Player のプロフィールにある score.db の score_history 表を読み込み、プレイ履歴(譜面sha256・ランプ・終了時刻・判定数)を PlayHistory に時刻順で持つ。BMZのスクショ名には譜面を特定する情報がないため、撮影時刻の直前(10分以内)に終わった最新のプレイを、そのスクショのプレイとして返す。オートプレイの記録は除外する。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('BmzTableReader','src/main/java/com/beatoraja/screenshot/table/BmzTableReader.java','table','通常','BMZ Player の library.db にある難易度表(difficulty_tables / difficulty_table_entries 表)を読み、beatorajaの .bmt と同じ表データの形に変換する。表の記号(symbol)をタグとして使い、記号+レベルをフォルダ名(例: sl12)にする。これにより DifficultyTableRegistry が beatoraja と BMZ の表を同じように扱える。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('CapturedAtCellRenderer','src/main/java/com/beatoraja/screenshot/ui/render/CapturedAtCellRenderer.java','ui.render','通常','撮影日時の列を描くセル描画部品。日付（年/月/日）は通常の文字色、時刻（時:分）は薄い色で並べ、日付を目で追いやすくする。値が日時でなければ何も描かない。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('CardPanel','src/main/java/com/beatoraja/screenshot/ui/theme/CardPanel.java','ui.theme','通常','関連する内容をまとめるための、角丸で一段明るい面を持つパネル。テーマの面色で塗り、細い枠線を引いてから子部品を描く。プレビュー欄のヘッダーや各行のカードに使われる。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('ChartResolverService','src/main/java/com/beatoraja/screenshot/service/ChartResolverService.java','service','通常','スクショ1枚から、遊んだ譜面・難易度表の表記・スコアを割り出す中心のクラス。設定に応じて beatoraja か BMZ Player のプレイ履歴・曲データベース・難易度表を開き直し、撮影時刻に近いプレイを探して譜面のハッシュから表記を決める。プレイが見つからなければファイル名のタイトルとフォルダ名から表記を推定する。新しいスクショのたびには、重い難易度表は読み直さずプレイ履歴と曲データベースだけを読み直せる。読み込んだ難易度表の一覧や記号の問い合わせも難易度表の登録簿に取り次ぐ。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('ChromiumBrowserLocator','src/main/java/com/beatoraja/screenshot/service/twitter/ChromiumBrowserLocator.java','service.twitter','通常','PC に入っている Chrome または Edge の実行ファイルを探す。Program Files・Program Files (x86)・LOCALAPPDATA の決まった場所を Chrome 優先の順で調べ、最初に見つかったものを返す。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('ChromiumCdpSession','src/main/java/com/beatoraja/screenshot/service/twitter/ChromiumCdpSession.java','service.twitter','アダプタ','ブラウザの1つのタブと Chromium DevTools Protocol の WebSocket でつながり、命令を送って応答を受け取る。送った命令ごとに番号を振って応答待ちの表に入れ、届いた応答を番号で突き合わせて返す。エラー応答や時間切れは例外にし、ブラウザが閉じられて接続が切れたら待っている命令をその場で打ち切る。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('ChromiumDevToolsClient','src/main/java/com/beatoraja/screenshot/service/twitter/ChromiumDevToolsClient.java','service.twitter','アダプタ','デバッグポートを開いて起動したブラウザに HTTP と CDP で話しかける窓口。プロファイルに書かれた DevToolsActivePort からポート番号を得て接続可能になるまで待ち、タブを探すか新しく開いて CDP セッションをつなぐ。x.com・twitter.com の Cookie を読み出して TwitterCookies にまとめる機能も持つ。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('ChromiumLauncher','src/main/java/com/beatoraja/screenshot/service/twitter/ChromiumLauncher.java','service.twitter','アダプタ','アプリ専用の X 用プロファイルで Chrome / Edge を起動する。どの起動でも共通の引数(プロファイルフォルダ、初回画面・同期・案内の抑止)を組み立て、利用者が操作する画面では自動操作の印をページから隠す。ログイン・Cookie 取得・投稿の3か所から使う。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('ChromiumProfileLock','src/main/java/com/beatoraja/screenshot/service/twitter/ChromiumProfileLock.java','service.twitter','アダプタ','X ログイン用のブラウザプロファイルを短時間だけ使うブラウザ起動のために、プロファイルのロックを扱う補助。前回の起動が残したデバッグポート情報のファイルを消し、SingletonLock と lockfile が消えて前のブラウザが手放すまで最大25秒待つ。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('ClearLamp','src/main/java/com/beatoraja/screenshot/model/ClearLamp.java','model','通常','クリアランプ11種(NO PLAY〜MAX)の列挙。ランプごとの事実をここ1か所に持つ: beatoraja のクリアID(並び順そのもの)とスクショ名に書かれる名前、BMZ Player のラベル、ランプ別まとめ投稿の既定の短い名前と既定で載せるか、バッジの色。名前・ID・BMZラベルからの引き当てと、文字列に含まれるランプ名の検出(長い名前を先に試す)を提供する。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('ClearLampLabelTableModel','src/main/java/com/beatoraja/screenshot/ui/ClearLampLabelTableModel.java','ui','通常','設定画面のクリアランプ表の中身。ランプごとに1行の固定行で、表示名(俗称)・まとめ投稿用の略称・まとめ投稿に載せるかを編集できる。既定値と同じ値や空欄は保存せず、既定に従う。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('ClearLampLabels','src/main/java/com/beatoraja/screenshot/config/ClearLampLabels.java','config','通常','クリアランプの表示名(俗称)と、ランプ別にまとめた投稿文で使う短い名前・載せるかどうかについて、ClearLamp の既定値に利用者の上書き設定を重ねて解決する。上書きはランプ名を前後空白除去・大文字化したものをキーにする。表示名を変えるだけで、色分けやクリアIDの判定には元のランプ名を使う前提。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('ClearTypeMapper','src/main/java/com/beatoraja/screenshot/player/ClearTypeMapper.java','player','通常','クリアランプの名前と数値ID(beatorajaの番号付け: 0=NO PLAY 〜 10=MAX)を相互に変換する窓口。beatorajaのスクショ名に入るランプ名、BMZ Player の clear_type ラベル(Hard、FullCombo など)のどちらからでもIDを求められる。対応表そのものは ClearLamp にある。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('ClixAuthSupport','src/main/java/com/beatoraja/screenshot/service/twitter/ClixAuthSupport.java','service.twitter','アダプタ','clix CLI に X の Cookie を渡すための準備をする。アプリ専用の clix ホームの下に clix 用の auth.json を書き出し、子プロセスの USERPROFILE をそこへ向けることで、利用者自身の clix 設定に触れずに認証情報を使わせる。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('ClixLocator','src/main/java/com/beatoraja/screenshot/util/ClixLocator.java','util','通常','clix CLI の実行ファイルを探す。同梱の tools\clix.exe、作業フォルダや開発中のプロジェクト直下の tools、Python のインストール先の Scripts を順に調べ、無ければ Windows の where コマンドで PATH 上を探す。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('ClixService','src/main/java/com/beatoraja/screenshot/service/ClixService.java','service','アダプタ','X(Twitter) とのやりとりの入口。ログイン状態の確認は同梱の clix コマンドを別プロセスで動かしてその JSON 出力を読み、投稿はブラウザで X の投稿画面を開くクラスに任せる。clix が見つからない・認証切れ・レート制限などの失敗を利用者向けの日本語メッセージに直す。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('DatabasePanel','src/main/java/com/beatoraja/screenshot/ui/DatabasePanel.java','ui','通常','メイン画面左側の、取り込み済みスクショ一覧の表。DB の全レコードをサムネイル・撮影日時・フラグ・曲名・記号・レベル・ランク・ランプ・投稿表記・状態（リザルト種別と X / Discord 投稿済みか）の列で表示し、記号・曲名・フラグのみで絞り込み、複数列で並べ替えできる。フラグ・記号・投稿表記のセルはその場で編集でき、編集結果はすぐ DB に保存して変わった行だけを読み直す。右クリックメニューから選択行を削除でき、画像ファイルの削除は裏スレッドで行ってから、消えたファイルの DB 行を消して一覧を読み直す。選択が変わるたびに選択中のスクショを ScreenshotEntry に変換してメイン画面へ知らせる。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('DifficultyTableRegistry','src/main/java/com/beatoraja/screenshot/table/DifficultyTableRegistry.java','table','通常','読み込んだ難易度表を索引化し、曲のタイトルまたはハッシュ(sha256/md5)から難易度表記(例: sl12)を引けるようにする。複数の表に載っている曲は、設定された表の優先順で並べて第一候補を決め、表ごとの記号置き換えルールや「常に併記」設定を反映した投稿用表記と候補一覧を返す。表の一覧・タグ・記号など、設定画面で使う情報も提供する。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('DiscordAutoPostQueue','src/main/java/com/beatoraja/screenshot/service/DiscordAutoPostQueue.java','service','通常','フォルダ監視で見つかった Discord 未投稿のリザルト画面スクショを、設定枚数たまるまで溜めておく待ち行列。リザルト以外・投稿済み・すでに並んでいるものは入れず、先頭からまとめて取り出したり、送れなかった分を先頭に戻したり、手動で送った分を取り除いたりできる。操作はすべて同期化してあり、別スレッドから呼んでも壊れない。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('DiscordWebhookService','src/main/java/com/beatoraja/screenshot/service/DiscordWebhookService.java','service','アダプタ','Discord の Webhook URL に本文と画像を multipart 形式で HTTP 送信する。1件のメッセージに載せられる画像の上限(MAX_IMAGES_PER_POST = 10)を持ち、画像の種類は拡張子から決める。応答からメッセージ ID を取り出して成否と一緒に返す。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('EnrichedScreenshot','src/main/java/com/beatoraja/screenshot/model/EnrichedScreenshot.java','model','通常','スクショ1枚と、それについて割り出した情報(譜面のハッシュと曲名、難易度表の記号とレベル、投稿表記とその候補、プレイヤーのプレイデータから得たか、照合したプレイの判定数)をまとめた値。ChartResolverService が作り、ScreenshotDatabase が保存し、MainFrame が使う。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('FilenameParser','src/main/java/com/beatoraja/screenshot/parser/FilenameParser.java','parser','通常','スクショのファイル名から撮影時刻・画面種別(リザルト・プレイ・選曲など)・曲名・難易度表フォルダ名・レベル・ランプ・DJ RANKを読み取り、ScreenshotEntry を作る。beatoraja形式(yyyyMMdd_HHmmss_…)と BMZ Player 形式(yyyyMMdd_HHmmss_SSS_<本文>.png)の2通りを、指定されたプレイヤー種別で切り替えて解析する。形式に合わないファイルは画面種別を空にし、ファイル名を曲名として返す。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('FirstRunWizard','src/main/java/com/beatoraja/screenshot/ui/FirstRunWizard.java','ui','通常','初回起動時に表示する初期設定画面。beatoraja / BMZ Player のフォルダ、任意のDiscord Webhook（名前とURL）を入力させ、任意でTwitterログインも行える。保存時にフォルダからプレイヤー種別を判定してスクショフォルダを決め、設定を保存して完了通知を呼ぶ。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('GitHubReleaseClient','src/main/java/com/beatoraja/screenshot/service/update/GitHubReleaseClient.java','service.update','アダプタ','GitHub の Releases API からこのアプリの最新リリースを取得する。タグ名と配布用 zip のダウンロード URL を取り出し、タグ名を比較用のバージョン文字列に整えて返す。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('HelpDialog','src/main/java/com/beatoraja/screenshot/ui/HelpDialog.java','ui','通常','ヘルプメニューから呼ばれる表示・操作をまとめた静的クラス。使い方の説明、バージョン情報ダイアログの表示、ログフォルダをエクスプローラーで開く処理、GitHubリポジトリをブラウザで開く処理を持つ。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('LampGroupedTweetText','src/main/java/com/beatoraja/screenshot/service/LampGroupedTweetText.java','service','通常','画像が多くて1枚1行の投稿文が文字数に収まらないときに使う、クリアランプ別の投稿文を作る。ランプの良い順に1行ずつ、そのランプの譜面の表記を投稿順に並べる。表示しないランプの譜面は省き、表記がない譜面はタイトルを短く切って代わりに載せる。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('Main','src/main/java/com/beatoraja/screenshot/Main.java','app','通常','アプリの起動口。ログ初期化と未捕捉例外の記録を設定し、設定ファイルを読んで画面テーマを適用する。自動更新が有効なら進捗画面を出して更新を試し、更新のため再起動する場合はここで終了する。そうでなければ初回設定が済んでいるかに応じて初期設定ウィザードかメイン画面を開く。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('MainFrame','src/main/java/com/beatoraja/screenshot/ui/MainFrame.java','ui','通常','アプリのメインウィンドウ。画面を組み立て、全体の流れを取りまとめる。起動時に DB と難易度表・プレイ記録を読み込み、スクショフォルダを走査して裏で曲情報を照合し DB に同期し、その後はフォルダを監視して新しいスクショを1枚ずつ DB に登録する。左の一覧で選んだスクショを右のプレビューに投稿順で並べる。投稿ボタンでは、投稿文を PostTextComposer で作り、PostConfirmDialog で確認・編集させ、X(20枚ずつ)・Discord(10枚ずつ)の共通の手順 runManualPost で別スレッドから BatchPoster で送り、送れた分を投稿済みとして保存する。新しいリザルトのスクショが設定枚数たまると Discord へ自動投稿もする。メニューから設定・難易度表の優先順位・表記ルール・投稿文書式の各ダイアログを開いて設定を保存し、終了時には設定に応じてスクショフォルダの PNG を消す。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('MultiColumnSortSupport','src/main/java/com/beatoraja/screenshot/ui/MultiColumnSortSupport.java','ui','通常','表で複数列を組み合わせた並べ替えを行うための補助クラス。列見出しをクリックするたびにその列を「降順で追加→昇順→解除」と切り替え、クリック順を優先順位として並べ替えキーを設定する。見出しには▼/▲と優先順位の番号を表示し、条件が空になったら既定列の降順に戻す。データベース画面の表で使われる。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('NotationPrefixResolver','src/main/java/com/beatoraja/screenshot/table/NotationPrefixResolver.java','table','通常','同じ表・同じ記号に属する難易度表記の並び(例: sl1〜sl12)から、置き換えてよい共通の先頭部分(記号部分)を求める。表記の記号を差し替える設定のとき、小数を含むレベル部分を残して先頭だけを置き換えるために使う。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('OsInfo','src/main/java/com/beatoraja/screenshot/util/OsInfo.java','util','通常','動いている OS についての判定(Windows かどうか)を1か所にまとめる。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('PlayHistory','src/main/java/com/beatoraja/screenshot/player/PlayHistory.java','player','通常','プレイ履歴を時刻順に持ち、両プレイヤーのプレイ履歴が使う時間範囲の検索(範囲内のプレイ、ある時刻までの最新のプレイ)を二分探索で行う。履歴の表にどの列があるかを調べる処理も持つ。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('PlayLogEntry','src/main/java/com/beatoraja/screenshot/player/PlayLogEntry.java','player','通常','プレイ履歴1件。譜面の sha256、クリアID、プレイ日時(エポック秒)、判定数(記録がなければ null)を持つ。beatoraja と BMZ Player の両方の履歴で共通に使う。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('PlayLogSource','src/main/java/com/beatoraja/screenshot/player/PlayLogSource.java','player','基底','プレイヤーのプレイ履歴から、スクショが撮られたプレイを探すための共通の窓口。beatoraja用(PlayerPlayLogService)と BMZ Player 用(BmzPlayLogService)がこれを実装し、呼び出し側はプレイヤーの違いを意識せずに撮影時刻とランプで検索できる。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('PlayScore','src/main/java/com/beatoraja/screenshot/player/PlayScore.java','player','通常','1回のプレイの判定数(PGREAT〜空POORの FAST/SLOW 別とノーツ数)を持ち、そこからEXスコア・理論値・BP・スコアレート・DJ RANKを計算する。現在のランクの境目より何点上か、次のランクまで何点足りないかのうち近い方(例: AAA+12、MAX-30)も求める。ランクの境目はbeatorajaと同じく理論値の9分の k(切り上げ)で判定する。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('PlayerKind','src/main/java/com/beatoraja/screenshot/player/PlayerKind.java','player','通常','アプリが対象とするプレイヤー(beatoraja / BMZ Player / 不明)を表す列挙型。設定ファイルに保存するキーと画面表示名を持ち、保存値からの復元と、フォルダの中身(config_sys.json か config.toml か)からの自動判定を行う。自動判定は、プレイヤー設定がなかった古い設定ファイルの補完に使う。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('PlayerPlayLogService','src/main/java/com/beatoraja/screenshot/player/PlayerPlayLogService.java','player','通常','beatorajaのプレイヤーフォルダにある scoredatalog.db を読み込み、プレイ履歴(sha256・クリアID・日時・判定数)を PlayHistory に時刻順で持つ。撮影時刻の3分前から30秒後までのプレイを候補として、ランプが一致するものを優先し、撮影時刻に近い順に返す。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('PostBatchSplitter','src/main/java/com/beatoraja/screenshot/service/PostBatchSplitter.java','service','通常','投稿する画像の一覧を、1回の投稿に載せられる枚数ごとの束に分ける小さな道具。順番は保ったまま、最後の束だけ端数になる。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('PostConfirmDialog','src/main/java/com/beatoraja/screenshot/ui/PostConfirmDialog.java','ui','通常','投稿直前の確認ダイアログ。投稿1回ごとの本文を編集できる形で並べ、利用者が確定した本文を返す(キャンセルなら null)。X 用は文字数のカウンタを出し、全件が280以内に収まるまで確定させない(入力内容は保ったまま出し直す)。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('PostTextComposer','src/main/java/com/beatoraja/screenshot/service/PostTextComposer.java','service','通常','利用者の投稿文書式・ランプ表示名の設定から投稿文を組み立てる。スクショ1枚ごとの1行を作り、投稿1回分につなぐ。X 用には文字数制限に収まるよう、8枚以上ならランプ別まとめ形式にし、それ未満で超える場合は曲名を長いものから短くする。投稿表記とスコアは呼び出し側から渡された関数で引く(画面や DB を知らない)。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('PostTextFormat','src/main/java/com/beatoraja/screenshot/config/PostTextFormat.java','config','通常','自動生成する投稿文に、どの項目(難易度表記・曲名・ランプ・DJ RANK・BP・スコアレート・EXスコア・境目との差)をどの順で、どんな前後の文字を付けて入れるかと、DJ RANKの表示名を持つ。AppConfig の一部として保存される。古い設定で欠けている項目を補い、不明・重複した項目を取り除く正規化も行う。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('PostTextFormatDialog','src/main/java/com/beatoraja/screenshot/ui/PostTextFormatDialog.java','ui','通常','自動生成する投稿文の書式を編集するダイアログ。投稿文に含める項目の有無・並び順・前後に付ける文字と、DJランクごとの表示名を設定できる。固定のサンプル曲とサンプルのスコアで投稿文を生成し、編集のたびにプレビューを更新する。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('PostedStateStore','src/main/java/com/beatoraja/screenshot/service/PostedStateStore.java','service','通常','どのスクショを X と Discord に投稿済みかを、ファイル名ごとに投稿日時と投稿 ID で覚えておく。アプリのデータフォルダの posted.json から読み込み、一時ファイル経由で安全に書き戻す。読めないファイルは空扱いにせず退避して、その旨を呼び出し側に伝える(全件が未投稿に見えて二重投稿するのを防ぐ)。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('PreviewPanel','src/main/java/com/beatoraja/screenshot/ui/PreviewPanel.java','ui','通常','選択中のスクショを表示するメイン画面右側の欄。上部のヘッダーに曲名・撮影日時・記号・レベル・ランク・ランプのピルを出し、その下に画像と投稿文の先頭部分を並べた閲覧専用のプレビュー一覧を置く。プレビュー画像は裏スレッドで読み込んで縮小し、最近の64枚をキャッシュする。最下部の1行ずつの投稿順リストで、ドラッグや↑↓ボタンによる並び替え操作を受け付けて呼び出し側へ通知する。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('ProcessUtils','src/main/java/com/beatoraja/screenshot/util/ProcessUtils.java','util','アダプタ','外部プロセスを子プロセスごと確実に終わらせる補助。Windows では taskkill でプロセスツリーを強制終了し、それ以外では強制終了を使う。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('ResultPalette','src/main/java/com/beatoraja/screenshot/ui/theme/ResultPalette.java','ui.theme','通常','クリアランプ・DJランク・画面種別・難易度表記号に割り当てる色の表を持つ。色はダークテーマ向けに決めてあり、ライトテーマでは明るすぎる色を読める明るさまで暗くして返す。色を背景色に混ぜる計算も提供する。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('ScreenshotDatabase','src/main/java/com/beatoraja/screenshot/db/ScreenshotDatabase.java','db','通常','アプリデータフォルダの screenshots.db(SQLite)に、解析・照合済みのスクショ情報(撮影時刻・曲名・ハッシュ・難易度表記・ランプ・ランク・投稿用表記・フラグなど)を保存する。起動時に表を作成し、古いDBには足りない列を追加する。登録・一括同期(消えたファイルの行の削除を含む)・表記の手動変更・フラグ更新・検索・削除を行い、手で直した表記は再同期で上書きしない。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('ScreenshotEntry','src/main/java/com/beatoraja/screenshot/model/ScreenshotEntry.java','model','通常','ファイル名から読み取ったスクショ1枚分の情報(パス・ファイル名・撮影時刻・画面種別・曲名・難易度表フォルダ名・レベル・ランプ・ランク)を持つ値オブジェクト。リザルト画面かどうか、BMSのレベル表記だけで難易度表が分からないかどうかを判定できる。同一性はファイルパスで判断する。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('ScreenshotFileRemover','src/main/java/com/beatoraja/screenshot/service/ScreenshotFileRemover.java','service','通常','スクショの画像ファイルを削除する。DB にも画面にも触れないので裏スレッドで実行でき、消せたもの(もともと無かったものを含む)と失敗の理由を返す。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('ScreenshotFolderCleanupService','src/main/java/com/beatoraja/screenshot/service/ScreenshotFolderCleanupService.java','service','通常','スクショフォルダにある PNG をまとめて削除する。フォルダの一覧はスクショの読み取りクラスで取り、消せたファイルの数を返す。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('ScreenshotRecord','src/main/java/com/beatoraja/screenshot/db/ScreenshotRecord.java','db','通常','screenshots.db の1行を表すレコード。ID・パス・撮影時刻・曲名・ハッシュ・難易度表の記号とレベル・ランプ・ランク・投稿用表記・候補表記・手動編集済みか・プレイデータから特定したか・フラグを持ち、一覧画面に表示するレベル文字列を返す。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('ScreenshotScanner','src/main/java/com/beatoraja/screenshot/service/ScreenshotScanner.java','service','通常','スクショフォルダ内の PNG を一覧にする。各ファイル名をプレイヤー(beatoraja / BMZ Player)の命名規則で解析してスクショ情報にし、撮影時刻の新しい順に並べて返す。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('ScreenshotWatcher','src/main/java/com/beatoraja/screenshot/service/ScreenshotWatcher.java','service','アダプタ','スクショフォルダを OS のファイル監視で見張り、新しい PNG ができたら通知する。専用の裏スレッドで監視を回し、書き込みが終わるのを少し待ってからファイル名を解析し、呼び出し側の通知先に渡す。一時的な読み取り失敗や通知先の例外では止まらず監視を続ける。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('SettingsDialog','src/main/java/com/beatoraja/screenshot/ui/SettingsDialog.java','ui','通常','アプリ全体の設定を編集するモーダルダイアログ。使用プレイヤー（beatoraja / BMZ Player）、スクショフォルダ、プレイヤーのフォルダとプレイヤー名、X のログイン状態とログイン・ログアウト、Discord Webhook 一覧、クリアランプの表示名・略称・まとめ投稿に載せるか、Discord 自動投稿、起動時の自動更新、終了時のスクショ削除、テーマ・フォント・文字サイズを1画面にまとめて表示する。保存ボタンでスクショフォルダの存在を確かめてから全項目を AppConfig に書き込んでファイルに保存し、呼び出し元に保存を知らせて閉じる。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('SongDatabaseService','src/main/java/com/beatoraja/screenshot/player/SongDatabaseService.java','player','通常','プレイヤーの楽曲DB(beatorajaの songdata.db、または BMZ Player の library.db)を読み込み、sha256 をキーにして md5・タイトル・サブタイトル付きの表示タイトル・レベルを引けるようにする。同じ sha256 が複数あるときは最初の行を使う。ファイルがない場合は利用不可として何も返さない。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('StateCell','src/main/java/com/beatoraja/screenshot/ui/render/StateCell.java','ui.render','通常','表の「状態」列に入れる値で、画面種別の文字とX・Discordへの投稿済みフラグを1つにまとめたもの。表示用文字列と、その文字列による並べ替え順を提供する。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('StateCellRenderer','src/main/java/com/beatoraja/screenshot/ui/render/StateCellRenderer.java','ui.render','通常','「状態」列のセル描画部品。画面種別のピルに続けて、投稿済みなら「X」「Discord」のピルを横に並べて描く。どれもなければ薄い「-」を描く。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('TableLevelParser','src/main/java/com/beatoraja/screenshot/table/TableLevelParser.java','table','通常','難易度表のフォルダ名や表記(例: sl12、★★3、LEVEL12)を、記号部分とレベル部分に分ける。既定の記号一覧と読み込んだ表のタグを長い順に照合し、合わなければ末尾の数字・小数点・? をレベルとみなして分割する。LEVELnn の形はBMSのレベルだけの表記として区別する。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('TableNotationRulesDialog','src/main/java/com/beatoraja/screenshot/ui/TableNotationRulesDialog.java','ui','通常','難易度表ごとの投稿表記ルールを編集するダイアログ。左側で難易度表を「表示する（常に投稿表記に含める）」「しない」の2つのリストに振り分け、右側の表で難易度表ごとに記号の置き換え（例：★を別の記号に）を1行ずつ設定する。置き換え後の表記例を実際の表記から数件抜き出して見せ、OKで設定用のルール一覧を組み立てて返す。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('TablePriorityDialog','src/main/java/com/beatoraja/screenshot/ui/TablePriorityDialog.java','ui','通常','難易度表の優先順位を並べ替えるダイアログ。同じ曲が複数の表に載っているとき、上にある表の記号・投稿表記が優先される。保存済みの順序と読み込み済みの表を合わせて一覧にし、ドラッグ＆ドロップか最上位へ・上へ・下へ・最下位へボタンで並べ替えて、OKで表タグの順序を返す。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('ThumbnailCache','src/main/java/com/beatoraja/screenshot/ui/render/ThumbnailCache.java','ui.render','通常','表に出す角丸サムネイルを必要になった時点で作り、最大512件まで保持するキャッシュ。画像の読み込みと縮小は2本の裏スレッドで行い、でき上がったら画面スレッドで再描画用の処理を呼ぶ。最近使っていないものから捨てる。読めなかった画像はファイルが更新されるまで読み直さない。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('ThumbnailCellRenderer','src/main/java/com/beatoraja/screenshot/ui/render/ThumbnailCellRenderer.java','ui.render','通常','サムネイル列のセル描画部品。セル値の画像パスからキャッシュ済みのサムネイルを取り出してセル中央に描き、まだ読み込み中なら灰色の角丸の四角を代わりに描く。ツールチップにはファイル名を出す。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('TweetTextGenerator','src/main/java/com/beatoraja/screenshot/service/TweetTextGenerator.java','service','通常','スクショ1枚分の投稿文を組み立てる。投稿文の書式設定で有効な項目(表記・タイトル・ランプ・ランク・BP・レート・EX スコア・ランク差)を設定順に並べ、前後の文字を付けて空白でつなぐ。入口は2つで、書式画面の見本用(曲名そのまま・ランプ名の置き換えなし)と、投稿用(曲名を X の数え方で切り詰め、ランプ名を利用者の呼び名に置き換える)。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('TweetTextLimits','src/main/java/com/beatoraja/screenshot/service/TweetTextLimits.java','service','通常','X の投稿文字数のルールを扱う。上限は280で、漢字・かな・ハングル・全角記号などは1文字2、それ以外は1と数えた長さを計算する。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('TwitterAuthService','src/main/java/com/beatoraja/screenshot/service/twitter/TwitterAuthService.java','service.twitter','通常','X（Twitter）のログイン状態を管理する窓口。ブラウザでの対話ログインから Cookie を取り出して設定に保存し、clix による認証確認、保存済みプロファイルからの Cookie の静かな取り直し、ログアウトをまとめて提供する。ブラウザの操作そのものは TwitterChromeLoginService に任せる。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('TwitterBrowserPostService','src/main/java/com/beatoraja/screenshot/service/twitter/TwitterBrowserPostService.java','service.twitter','アダプタ','X への投稿を、ログイン済みプロファイルで普通に表示した Chrome / Edge から行う。投稿画面を開き、CDP で本文の入力と画像の添付を済ませたうえで、最後の「投稿」ボタンは利用者に押してもらい、その完了を見届ける。完全自動で押すと X の自動化検出（エラー226）で止められたため、この形になっている。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('TwitterChromeLoginService','src/main/java/com/beatoraja/screenshot/service/twitter/TwitterChromeLoginService.java','service.twitter','アダプタ','X ログイン用のブラウザプロファイルに対して Chrome / Edge を起動・停止する。利用者がログインするための表示ブラウザを開き、閉じられるのを待ち、その後に画面外へ置いたブラウザをデバッグポート付きで起動し直して CDP で Cookie を読み出す。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('TwitterCookies','src/main/java/com/beatoraja/screenshot/service/twitter/TwitterCookies.java','service.twitter','通常','X の Cookie 一式を変更できない形で持つ値。名前から値への表を保持し、認証に必要な auth_token と ct0 の取り出しと、両方そろっているかの判定を提供する。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('TwitterLoginDialog','src/main/java/com/beatoraja/screenshot/ui/TwitterLoginDialog.java','ui','通常','X（Twitter）へのログイン手順を案内するモーダルダイアログ。裏スレッドでログイン処理を走らせてChrome / Edgeを起動させ、進み具合を表示する。利用者が「ログイン完了」を押すとブラウザを閉じるよう促して処理を先へ進める。キャンセルボタンでも×で閉じても取り消しとして裏の処理に伝える。結果はスレッド間で安全に受け渡す。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('UiTheme','src/main/java/com/beatoraja/screenshot/ui/theme/UiTheme.java','ui.theme','通常','FlatLafのダーク／ライトテーマを組み込み、角丸・表・スクロールバーなどの見た目の既定値と、日本語を表示できるUIフォント・文字サイズを設定する。現在のテーマに応じた面色・枠線色・薄い文字色・アクセント色など、独自描画部品が使う色を提供する。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('UpdateProgressDialog','src/main/java/com/beatoraja/screenshot/ui/UpdateProgressDialog.java','ui','通常','起動時の自動更新の確認・ダウンロード中に出す進み具合のダイアログ。状態の文言と進捗バーを持ち、総バイト数が分かれば百分率で、分からなければ動き続ける表示にする。閉じるボタンでは閉じられない。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('VersionComparator','src/main/java/com/beatoraja/screenshot/util/VersionComparator.java','util','通常','アプリの版番号どうしを比べる。先頭の v を外し、ハイフン以降を捨てて点区切りの数字として先頭から順に比べ、足りない桁は0とみなす。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('WebhookTableModel','src/main/java/com/beatoraja/screenshot/ui/WebhookTableModel.java','ui','通常','設定画面の Discord Webhook 表の中身。名前と URL の行を追加・削除・編集できる。設定画面は設定の複製を渡すので、ここでの編集は保存するまで設定に反映されない。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('WindowsUpdateLauncher','src/main/java/com/beatoraja/screenshot/service/update/WindowsUpdateLauncher.java','service.update','アダプタ','自動更新の最後の段階として、PowerShell の更新スクリプトを一時ファイルに書き出して別プロセスで起動する。スクリプトは今のアプリの終了を待ってから、配布物が持つ app と runtime のフォルダだけを更新元と同じ内容にし(消えたファイルも消す)、それ以外は消さずに上書きコピーするので、利用者がインストール先に置いたファイルは残る。成否にかかわらず最後にダウンロード物と自分自身を消し、成功ならアプリを起動し直す。Windows 以外では例外を投げる。','実装済み');
INSERT INTO "_class"("name","module","layer","kind","responsibility","status") VALUES('ZipExtractUtils','src/main/java/com/beatoraja/screenshot/service/update/ZipExtractUtils.java','service.update','通常','ダウンロードした更新用 zip を指定フォルダへ展開し、中のアプリ本体フォルダの場所を返す。フォルダの外へ書き出そうとする危険な項目があれば中断する。','実装済み');

INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('AppConfig','AppLogging','利用','設定ファイルの読み込み失敗を警告ログに出すため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('AppConfig','AppPaths','利用','設定ファイルとアプリデータフォルダの場所を得るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('AppConfig','AtomicFiles','利用','設定ファイルを壊さずに保存するため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('AppConfig','ClearLampLabels','利用','ランプの表示名の上書きを解決する処理を任せるため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('AppConfig','PlayerKind','利用','保存したプレイヤー種別のキーを種別に戻し、未設定時はフォルダから判定するため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('AppConfig','PostTextFormat','利用','投稿文の書式を設定の一部として持ち、既定値や正規化を使うため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('AppIcons','AppLogging','利用','アイコン画像が見つからないときに記録を残すロガーを得るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('AppLogging','AppDataLocation','利用','ログフォルダの置き場所を知るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('AppPaths','AppDataLocation','利用','データフォルダの置き場所を知るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('AppPaths','AppLogging','利用','ログフォルダの場所を AppLogging の決め方にそろえて返すため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('AppUpdateService','AppLogging','利用','更新の確認結果や失敗をログに残すため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('AppUpdateService','AppPaths','利用','本体 exe の置き場所とインストール先フォルダを知るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('AppUpdateService','AppVersion','利用','今動いているアプリのバージョンを取り、開発版かを判定するため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('AppUpdateService','GitHubReleaseClient','生成','最新リリースのバージョンとダウンロード URL を取得するため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('AppUpdateService','OsInfo','利用','Windows でだけ自動更新するため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('AppUpdateService','VersionComparator','利用','今のバージョンを整え、最新リリースの方が新しいか比べるため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('AppUpdateService','WindowsUpdateLauncher','利用','展開した更新を適用して再起動する更新スクリプトを起動するため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('AppUpdateService','ZipExtractUtils','利用','ダウンロードした zip を展開してアプリ本体フォルダを得るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('Badge','ResultPalette','利用','ピルの塗りと枠の色を、指定色を背景色に混ぜて作るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('Badge','UiTheme','利用','ラベル部品に親がないとき、背景色としてテーマの面色を使うため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('BadgeCellRenderer','Badge','利用','セル内にピルを描き、その幅を測って配置を決めるため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('BadgeCellRenderer','UiTheme','利用','値が空のときの「-」を控えめな色で描くため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('BatchPoster','ScreenshotEntry','利用','バッチのスクショから画像のパスを取り、送れた分として返すため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('BeatorajaPaths','AppLogging','利用','config_sys.json やプレイヤーフォルダ一覧の読み込み失敗をログに出すため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('BmzPaths','AppLogging','利用','プロフィール一覧の読み込み失敗をログに出すため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('BmzPlayLogService','ClearTypeMapper','利用','score.db の clear_type ラベルをクリアIDに直すため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('BmzPlayLogService','PlayHistory','生成','読み込んだ履歴を時刻順に持ち、時刻で探すため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('BmzPlayLogService','PlayLogEntry','生成','score_history の1行をプレイ履歴1件にするため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('BmzPlayLogService','PlayLogSource','継承','プレイ履歴検索の共通窓口を BMZ Player 用に実装するため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('BmzPlayLogService','PlayScore','生成','履歴の判定数からスコア情報を作るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('BmzTableReader','BmtTableReader','生成','library.db の表を .bmt と同じ表データ・フォルダ・曲の形で作るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('CapturedAtCellRenderer','UiTheme','利用','時刻部分を背景に寄せた控えめな色で描くため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('CardPanel','UiTheme','利用','カードの面色と縁の枠線色を現在のテーマから得るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('ChartResolverService','AppConfig','利用','プレイヤーフォルダ・プレイヤー名・種類、表の優先順と表記ルールを設定から読むため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('ChartResolverService','AppLogging','利用','譜面特定の失敗や DB を閉じる際の失敗をログに残すため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('ChartResolverService','BeatorajaPaths','利用','beatoraja の難易度表フォルダと各 DB の場所を求めるため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('ChartResolverService','BmzPaths','利用','BMZ Player の library.db とスコア DB の場所を求めるため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('ChartResolverService','BmzPlayLogService','生成','BMZ Player のスコア DB からプレイ履歴を引く窓口を作るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('ChartResolverService','BmzTableReader','利用','BMZ Player の library.db から難易度表をまとめて読むため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('ChartResolverService','ClearTypeMapper','利用','ランプ名とクリア番号を相互に変換し、プレイ検索と BMZ のランプ表示に使うため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('ChartResolverService','DifficultyTableRegistry','生成','難易度表を読み込んで、ハッシュや曲名から表記を引くため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('ChartResolverService','EnrichedScreenshot','生成','割り出した譜面情報を返すため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('ChartResolverService','PlayLogEntry','利用','見つかったプレイの譜面ハッシュ・ランプ・スコアを読むため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('ChartResolverService','PlayLogSource','利用','beatoraja と BMZ Player のプレイ履歴を同じ形で扱うため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('ChartResolverService','PlayScore','利用','見つけたプレイの判定数を投稿用に持ち回るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('ChartResolverService','PlayerKind','利用','beatoraja と BMZ Player で読み込み先や結果の扱いを分けるため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('ChartResolverService','PlayerPlayLogService','生成','beatoraja の scorelog DB からプレイ履歴を引く窓口を作り、その履歴の型を使うため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('ChartResolverService','ScreenshotEntry','生成','スクショ情報を受け取り、BMZ では曲名・ランプ・ランクを埋めた新しい情報を作るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('ChartResolverService','SongDatabaseService','生成','譜面のハッシュから曲名・md5・レベルを引くため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('ChartResolverService','TableLevelParser','生成','フォルダ名や表記を記号とレベルに分けた結果を作るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('ChromiumCdpSession','AppLogging','利用','受信した CDP メッセージを解釈できなかったときに記録を残すため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('ChromiumDevToolsClient','ChromiumCdpSession','利用','見つけたタブへ CDP でつなぎ、命令を送るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('ChromiumDevToolsClient','TwitterCookies','生成','ブラウザから読み出した x.com の Cookie を TwitterCookies にまとめて返すため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('ChromiumProfileLock','AppLogging','利用','デバッグ用ファイルを消せなかったときに記録を残すため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('ClearLampLabelTableModel','ClearLampLabels','利用','ランプの一覧・既定の略称・既定で載せるかを取るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('ClearLampLabels','ClearLamp','利用','ランプの一覧と既定の短い名前・載せるかどうかを取るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('ClearTypeMapper','ClearLamp','利用','ランプ名・クリアID・BMZ ラベルの対応表として使うため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('ClixAuthSupport','AppConfig','利用','保存済みの X の Cookie を読み出して clix 用の認証ファイルに書くため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('ClixAuthSupport','AppPaths','利用','clix 用の専用ホームフォルダの場所を得るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('ClixLocator','AppLogging','利用','探索中のフォルダ走査や where の失敗を記録するため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('ClixLocator','AppPaths','利用','同梱 clix.exe の場所を探索の第一候補として得るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('ClixService','AppConfig','利用','手動ログイン済みかと clix の認証環境の設定値を読むため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('ClixService','AppLogging','利用','clix の出力が解析できないときなどをログに残すため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('ClixService','ClixAuthSupport','利用','clix を動かすときの認証用の環境変数を用意するため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('ClixService','ClixLocator','利用','clix 実行ファイルの場所を探し、見つからないときの案内に同梱先を示すため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('ClixService','TwitterBrowserPostService','生成','ブラウザで X の投稿画面を開いて実際に投稿させるため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('DatabasePanel','AppConfig','利用','既定の文字サイズを基準に列幅を今の文字サイズへ拡大縮小するため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('DatabasePanel','Badge','利用','記号・ランク・ランプ・状態の札の描画幅を測って列幅を決めるため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('DatabasePanel','BadgeCellRenderer','生成','記号・ランク・ランプ列を色付きの札で描くため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('DatabasePanel','CapturedAtCellRenderer','生成','撮影日時列を整形して描くため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('DatabasePanel','ClearLampLabels','利用','ランプ列の表示をユーザー設定の俗称に置き換えるため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('DatabasePanel','MultiColumnSortSupport','生成','見出しクリックで複数列の並べ替えを行い、既定で撮影日時の新しい順にするため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('DatabasePanel','PostedStateStore','利用','状態列に各スクショが X / Discord に投稿済みかを表示するため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('DatabasePanel','ResultPalette','利用','記号・ランク・ランプごとの札の色を得るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('DatabasePanel','ScreenshotDatabase','利用','一覧の読み込みと、フラグ・表記の更新、行の削除を DB に反映するため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('DatabasePanel','ScreenshotEntry','生成','選択中のレコードを投稿・プレビュー用の形に変換して渡すため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('DatabasePanel','ScreenshotFileRemover','生成','選択したスクショの画像ファイルを裏スレッドで消すため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('DatabasePanel','ScreenshotRecord','利用','表の各行の中身として DB の1件分を保持・表示するため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('DatabasePanel','StateCell','生成','状態列の値としてリザルト種別と投稿済みの有無をまとめて渡すため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('DatabasePanel','StateCellRenderer','生成','状態列をリザルト種別・X・Discord の札で描くため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('DatabasePanel','ThumbnailCache','生成','サムネイル画像を裏で縮小読み込みして使い回すため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('DatabasePanel','ThumbnailCellRenderer','生成','サムネイル列に縮小画像を描くため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('DatabasePanel','UiTheme','利用','控えめな文字色・縞模様の背景色・現在の文字サイズを得るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('DifficultyTableRegistry','AppConfig','利用','表全体の記号置き換えを表す空キーの定数を参照するため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('DifficultyTableRegistry','BmtTableReader','生成','表フォルダの .bmt を読み込み、その結果の形を受け取るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('DifficultyTableRegistry','NotationPrefixResolver','利用','記号置き換えの対象となる先頭部分を求め、差し替えるため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('DifficultyTableRegistry','TableLevelParser','利用','表のフォルダ名や表記を記号とレベルに分けるため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('DiscordAutoPostQueue','AppLogging','利用','行列に入れなかった理由をログに残すため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('DiscordAutoPostQueue','PostedStateStore','利用','Discord に投稿済みのスクショを行列に入れないよう確かめるため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('DiscordAutoPostQueue','ScreenshotEntry','利用','並べる対象のスクショ情報で、リザルト画面かとファイル名を見るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('DiscordWebhookService','AppLogging','利用','Discord の応答が解析できないときをログに残すため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('EnrichedScreenshot','PlayScore','利用','照合したプレイの判定数を持つため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('EnrichedScreenshot','ScreenshotEntry','利用','元のスクショ情報を持つため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('FilenameParser','ClearLamp','利用','beatoraja 名からランプ名を見つけ、BMZ ラベルを beatoraja のランプ名に直すため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('FilenameParser','PlayerKind','利用','どちらのプレイヤーの形式で解析するかを切り替えるため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('FilenameParser','ScreenshotEntry','生成','解析結果をスクショ情報として作って返すため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('FirstRunWizard','AppConfig','生成','入力内容をフォルダ・プレイヤー種別・Discord Webhookとして設定に書き込み保存するため（Webhook項目を新しく作る）。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('FirstRunWizard','BmzPaths','利用','BMZ Playerのフォルダが選ばれたとき、config.tomlからdataフォルダとスクショフォルダを求めるため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('FirstRunWizard','ClixService','利用','Twitterログインの結果（成否とメッセージ）を受け取って状態表示に反映するため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('FirstRunWizard','PlayerKind','利用','選ばれたフォルダがbeatorajaかBMZ Playerかを判定して設定するため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('FirstRunWizard','TwitterLoginDialog','生成','「Twitter にログイン」ボタンでログイン用ダイアログを開くため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('GitHubReleaseClient','AppLogging','利用','検出した最新リリースをログに残すため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('GitHubReleaseClient','VersionComparator','利用','タグ名を比較用のバージョン文字列に整えるため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('HelpDialog','AppPaths','利用','開くログフォルダの場所を得るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('HelpDialog','AppVersion','利用','バージョン情報にアプリのバージョン番号を表示するため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('LampGroupedTweetText','ClearLampLabels','利用','ランプの順番・短い名前・表示するかどうかを決めるため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('LampGroupedTweetText','TweetTextGenerator','利用','表記の無い譜面の曲名を X の数え方で切り詰めるため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('LampGroupedTweetText','TweetTextLimits','利用','投稿文字数の上限からこの形式を使うか判定するため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('Main','AppConfig','利用','起動時に設定を読み込み、テーマ・自動更新・初回設定の状態を調べるため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('Main','AppIcons','利用','初期設定ウィザードの窓にアプリのアイコンを付けるため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('Main','AppLogging','利用','起動直後にログを初期化し、未捕捉例外を記録するため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('Main','AppPaths','利用','解決した実行フォルダを起動時にログへ残すため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('Main','AppUpdateService','生成','起動時に自動更新が必要か判断し、更新を実行するため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('Main','AppVersion','利用','起動ログとウィザードの窓タイトルに版番号を出すため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('Main','FirstRunWizard','生成','初回設定が済んでいないときに初期設定画面を出すため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('Main','MainFrame','生成','設定済みのとき、またはウィザード完了後にメイン画面を開くため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('Main','UiTheme','利用','設定のテーマとフォントを画面全体に適用するため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('Main','UpdateProgressDialog','生成','自動更新中の状況とダウンロード進み具合を見せるため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('MainFrame','AccentButton','生成','Twitter 投稿・Discord 送信ボタンをそれぞれの色で目立たせるため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('MainFrame','AppConfig','利用','フォルダ・プレイヤー・Webhook・投稿文書式・自動投稿などの設定を読み、各ダイアログの結果を保存するため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('MainFrame','AppIcons','利用','ウィンドウにアプリのアイコンを付けるため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('MainFrame','AppLogging','利用','ログ出力と、Webhook URL やエラー文を伏せ字・無害化して記録するため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('MainFrame','AppVersion','利用','タイトルとステータスバーにアプリのバージョンを表示するため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('MainFrame','BatchPoster','利用','X・Discord の手動投稿でバッチを順に送るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('MainFrame','ChartResolverService','生成','難易度表・プレイ記録を読み込み、スクショに曲情報・既定の投稿表記・スコアを付け、表の一覧や記号の順位を得るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('MainFrame','ClixService','生成','ブラウザを操作して X に画像付きで投稿し、1投稿あたりの画像上限を得るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('MainFrame','DatabasePanel','生成','画面左側にスクショ一覧を置き、選択・編集・投稿済み状態をやりとりするため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('MainFrame','DifficultyTableRegistry','利用','優先順位・表記ルールのダイアログに渡す難易度表の情報の型として使うため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('MainFrame','DiscordAutoPostQueue','生成','新しいリザルトのスクショを自動投稿待ちとしてためておき、枚数分ずつ取り出すため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('MainFrame','DiscordWebhookService','生成','手動・自動の Discord 送信で Webhook に画像と文を送るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('MainFrame','EnrichedScreenshot','利用','照合結果を DB に保存し、投稿表記やスコアを読むため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('MainFrame','HelpDialog','利用','ヘルプメニューから使い方・ログフォルダ・リポジトリ・バージョン情報を開くため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('MainFrame','LampGroupedTweetText','生成','枚数が多いツイートの投稿文をランプ別のまとめ形式で作るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('MainFrame','PlayerKind','利用','BMZ Player のときだけ照合失敗したリザルトを少し待って再照合するため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('MainFrame','PostBatchSplitter','利用','選んだスクショを1投稿あたりの上限枚数ごとに分けるため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('MainFrame','PostConfirmDialog','利用','投稿直前に本文を確認・編集させるため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('MainFrame','PostTextComposer','生成','プレビューと投稿の本文を作るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('MainFrame','PostTextFormat','利用','投稿文書式ダイアログの結果を受け取って設定に保存するため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('MainFrame','PostTextFormatDialog','生成','設定メニューから投稿文の書式を編集させるため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('MainFrame','PostedStateStore','利用','投稿済みの記録を読み書き・保存し、二重投稿を避けるため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('MainFrame','PreviewPanel','生成','画面右側に選んだスクショと投稿文を投稿順に並べ、並べ替え操作を受けるため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('MainFrame','ScreenshotDatabase','生成','スクショ一覧の DB を開き、走査結果の同期・新規分の登録・保存済み投稿表記の取得をするため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('MainFrame','ScreenshotEntry','利用','選択・投稿順・投稿処理で扱うスクショ1枚分の情報として使うため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('MainFrame','ScreenshotFolderCleanupService','生成','終了時にスクショフォルダ内の PNG を削除するため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('MainFrame','ScreenshotRecord','利用','DB に保存されたユーザー指定の投稿表記を読むため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('MainFrame','ScreenshotScanner','生成','起動時・再読み込み時にスクショフォルダの画像を一覧にするため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('MainFrame','ScreenshotWatcher','生成','スクショフォルダを監視して新しい画像を検出するため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('MainFrame','SettingsDialog','生成','設定メニューから設定画面を開き、保存後に表示や監視をやり直すため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('MainFrame','TableNotationRulesDialog','生成','設定メニューから難易度表ごとの投稿表記ルールを編集させるため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('MainFrame','TablePriorityDialog','生成','設定メニューから難易度表の優先順位を編集させるため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('MainFrame','TwitterAuthService','生成','X 投稿の前にログイン情報が保存済みかを確かめるため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('MainFrame','TwitterLoginDialog','生成','未ログイン時や認証エラー時に X へのログインを促すため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('MainFrame','UiTheme','利用','ボタンや文字・枠線の色を得て、外観変更時にテーマとフォントを適用し直すため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('NotationPrefixResolver','TableLevelParser','利用','各表記から記号部分を取り出すため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('PlayHistory','PlayLogEntry','利用','時刻順に並べて探す対象として持つため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('PlayLogEntry','PlayScore','利用','プレイの判定数を持つため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('PlayLogSource','PlayLogEntry','利用','検索結果のプレイ履歴の型として使うため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('PlayerKind','BmzPaths','利用','フォルダが BMZ Player のデータフォルダかどうかを判定するため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('PlayerPlayLogService','PlayHistory','生成','読み込んだ履歴を時刻順に持ち、時刻で探すため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('PlayerPlayLogService','PlayLogEntry','生成','scoredatalog の1行をプレイ履歴1件にするため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('PlayerPlayLogService','PlayLogSource','継承','プレイ履歴検索の共通窓口を beatoraja 用に実装するため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('PlayerPlayLogService','PlayScore','生成','scoredatalog の判定数からスコア情報を作るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('PostConfirmDialog','TweetTextLimits','利用','X の文字数を数えてカウンタと上限の確認に使うため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('PostConfirmDialog','UiTheme','利用','文字数カウンタの通常時の色を取るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('PostTextComposer','AppConfig','利用','投稿文の書式・ランプ表示名・短い名前・載せるランプの設定を読むため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('PostTextComposer','LampGroupedTweetText','生成','枚数が多いバッチをランプ別まとめ形式にするため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('PostTextComposer','PlayScore','利用','スコア付きの行を作るためにスコアを受け渡す。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('PostTextComposer','ScreenshotEntry','利用','曲名・ランプ・ファイル名を行に使うため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('PostTextComposer','TweetTextGenerator','利用','1枚分の行を書式どおりに作るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('PostTextComposer','TweetTextLimits','利用','X の文字数を数えて上限と比べるため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('PostTextFormat','PlayScore','利用','正規化の際に有効なDJ RANKの一覧を参照するため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('PostTextFormatDialog','AppIcons','利用','ダイアログにアプリのアイコンを付けるため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('PostTextFormatDialog','PlayScore','生成','プレビュー用のサンプルの判定数を作り、ランク表示名の欄をランク一覧から並べるため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('PostTextFormatDialog','PostTextFormat','生成','現在の書式を読み込み、編集結果から新しい書式を組み立てて返すため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('PostTextFormatDialog','ScreenshotEntry','生成','プレビュー用のサンプル曲のエントリを作るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('PostTextFormatDialog','TweetTextGenerator','利用','編集中の書式でサンプルの投稿文を生成してプレビューに出すため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('PostedStateStore','AppLogging','利用','posted.json を読めなかったときにログに残すため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('PostedStateStore','AppPaths','利用','posted.json とデータフォルダの場所を知るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('PostedStateStore','AtomicFiles','利用','投稿済み記録を壊さずに保存し、読めないファイルを退避するため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('PreviewPanel','AppLogging','利用','プレビュー画像の読み込み失敗を記録するため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('PreviewPanel','Badge','利用','ヘッダーのランク・ランプ等のピルと、各行の番号のピルを作り、ピルの高さで行の高さを決めるため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('PreviewPanel','CardPanel','生成','ヘッダー、プレビュー行、投稿順リストの行をカード状の面として作るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('PreviewPanel','ClearLampLabels','利用','ヘッダーのランプのピルにユーザー設定の表示名を使うため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('PreviewPanel','ResultPalette','利用','記号・ランク・ランプのピルの色を決めるため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('PreviewPanel','ScreenshotEntry','利用','表示するスクショの曲名・撮影日時・ランク・ランプ・画像パスなどを読み出すため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('PreviewPanel','UiTheme','利用','控えめな文字色・アクセント色・基準の文字サイズを得るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('ProcessUtils','AppLogging','利用','プロセスツリーの終了に失敗したときに警告を記録するため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('ProcessUtils','OsInfo','利用','Windows では taskkill でプロセスツリーを止めるため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('ResultPalette','ClearLamp','利用','ランプの色を取るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('ResultPalette','UiTheme','利用','ダークかライトかを調べ、色の明るさを調整するため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('ScreenshotDatabase','AppPaths','利用','screenshots.db を置くアプリデータフォルダの場所を得るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('ScreenshotDatabase','EnrichedScreenshot','利用','照合結果を行として保存するため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('ScreenshotDatabase','ScreenshotRecord','生成','読み出した行をレコードにして返すため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('ScreenshotFileRemover','ScreenshotRecord','利用','消すファイルのパスと名前を記録から取るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('ScreenshotFolderCleanupService','AppLogging','利用','消せなかったファイルをログに残すため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('ScreenshotFolderCleanupService','ScreenshotEntry','利用','一覧の各スクショからファイルのパスを取るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('ScreenshotFolderCleanupService','ScreenshotScanner','生成','削除対象の PNG の一覧を取るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('ScreenshotScanner','FilenameParser','利用','ファイル名を解析してスクショ情報にするため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('ScreenshotScanner','PlayerKind','利用','どのプレイヤーの命名規則で解析するか指定するため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('ScreenshotScanner','ScreenshotEntry','利用','返す一覧の要素で、撮影時刻とファイル名で並べ替えるため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('ScreenshotWatcher','AppLogging','利用','ファイル監視を閉じる際の失敗をログに残すため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('ScreenshotWatcher','FilenameParser','利用','新しくできたファイルの名前を解析してスクショ情報にするため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('ScreenshotWatcher','PlayerKind','利用','どのプレイヤーの命名規則で解析するか受け取るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('ScreenshotWatcher','ScreenshotEntry','利用','見つけたスクショを通知先に渡す形にするため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('SettingsDialog','AppConfig','生成','設定値を読み込んで画面に出し、保存時に書き戻し、Webhook 行の追加で新しい Webhook 項目を作るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('SettingsDialog','AppIcons','利用','ダイアログにアプリのアイコンを付けるため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('SettingsDialog','AppLogging','利用','ログフォルダを開けなかったときの警告を記録するため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('SettingsDialog','AppPaths','利用','「ログフォルダを開く」でログフォルダの場所を得るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('SettingsDialog','BeatorajaPaths','利用','beatoraja フォルダからプレイヤー名の候補を集めるため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('SettingsDialog','BmzPaths','利用','BMZ Player の data フォルダからプロファイル ID の候補を集めるため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('SettingsDialog','ClearLampLabelTableModel','生成','クリアランプの表示名・略称・載せるかの表を作るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('SettingsDialog','ClixService','利用','X のログイン確認・ログインの結果（AuthResult）を受け取って状態表示に使うため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('SettingsDialog','PlayerKind','生成','使用プレイヤーの選択肢を並べ、選択に応じて表示文言と候補の集め方を切り替えるため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('SettingsDialog','TwitterAuthService','生成','保存済みの X ログインの確認・裏での再認証・ログアウトをするため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('SettingsDialog','TwitterLoginDialog','生成','「Twitter にログイン」ボタンでブラウザでのログインを行わせるため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('SettingsDialog','UiTheme','利用','フォントの選択肢として日本語を表示できるフォント一覧を得るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('SettingsDialog','WebhookTableModel','生成','Discord Webhook の表を作るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('StateCellRenderer','Badge','利用','画面種別・X・Discordのピルを横に並べて描くため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('StateCellRenderer','ResultPalette','利用','画面種別のピルの色を決めるため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('StateCellRenderer','StateCell','生成','セル値を状態セルとして読み、値がないときは空の状態セルを作って使うため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('StateCellRenderer','UiTheme','利用','XとDiscordのブランド色、空のときの控えめな色を得るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('TableNotationRulesDialog','AppConfig','生成','保存済みの表記ルールを読み、編集結果から新しい表記ルールを作って返すため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('TableNotationRulesDialog','AppIcons','利用','ダイアログにアプリのアイコンを付けるため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('TableNotationRulesDialog','NotationPrefixResolver','利用','置き換え対象の接頭辞を求め、記号を置き換えた表記例を作るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('TablePriorityDialog','AppIcons','利用','ダイアログにアプリのアイコンを付けるため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('ThumbnailCache','AppLogging','利用','サムネイルの読み込み失敗を記録するため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('ThumbnailCache','UiTheme','利用','サムネイルの縁取りの濃さをダークかライトかで変えるため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('ThumbnailCellRenderer','ThumbnailCache','利用','画像パスからサムネイルを取り出し、その大きさで配置を決めるため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('ThumbnailCellRenderer','UiTheme','利用','読み込み中の代わりの四角を枠線色で塗るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('TweetTextGenerator','ClearLampLabels','利用','ランプ名を利用者が設定した呼び名に置き換えるため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('TweetTextGenerator','PlayScore','利用','ランク・BP・レート・EX スコア・ランク差の値を取るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('TweetTextGenerator','PostTextFormat','利用','投稿文に並べる項目の順番・有効状態・前後の文字・ランク表示名を読むため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('TweetTextGenerator','ScreenshotEntry','利用','曲名・ランプ・ファイル名のランク・ファイル名を取るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('TweetTextGenerator','TweetTextLimits','利用','曲名を X の数え方で測って切り詰めるため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('TwitterAuthService','AppConfig','利用','X の Cookie の有無確認・保存・削除を設定に対して行うため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('TwitterAuthService','AppLogging','利用','Cookie を含む設定の保存に失敗したときに警告を記録するため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('TwitterAuthService','ClixService','生成','保存した Cookie が有効かを clix の認証状態確認で確かめるため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('TwitterAuthService','TwitterChromeLoginService','生成','ログイン用ブラウザの起動・終了待ち・Cookie 取り出しを任せるため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('TwitterAuthService','TwitterCookies','利用','取り出した Cookie 一式を受け取り設定へ保存するため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('TwitterBrowserPostService','AppLogging','利用','投稿後のプロファイルのロック解除待ちの失敗を記録するため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('TwitterBrowserPostService','AppPaths','利用','ログイン済みブラウザプロファイルの場所を得るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('TwitterBrowserPostService','ChromiumBrowserLocator','利用','投稿に使う Chrome / Edge の実行ファイルを探すため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('TwitterBrowserPostService','ChromiumCdpSession','利用','投稿画面の操作（移動・入力・キー送信・画像添付・状態確認）の命令を送るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('TwitterBrowserPostService','ChromiumDevToolsClient','生成','起動したブラウザのデバッグポートを待ち、投稿用タブの CDP セッションを開くため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('TwitterBrowserPostService','ChromiumLauncher','利用','投稿用のブラウザを共通の引数で起動するため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('TwitterBrowserPostService','ChromiumProfileLock','利用','起動前後にプロファイルのロック解除を待ち、古いデバッグ用ファイルを消すため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('TwitterBrowserPostService','ClixService','利用','投稿結果を ClixService の PostResult の形で返すため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('TwitterBrowserPostService','ProcessUtils','利用','投稿後に起動したブラウザを子プロセスごと終わらせるため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('TwitterChromeLoginService','AppLogging','利用','静かな Cookie 取り直しの失敗を記録するため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('TwitterChromeLoginService','AppPaths','利用','ログイン用ブラウザプロファイルの場所を得るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('TwitterChromeLoginService','ChromiumBrowserLocator','利用','起動する Chrome / Edge の実行ファイルを探すため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('TwitterChromeLoginService','ChromiumDevToolsClient','生成','Cookie 取得用ブラウザのデバッグポートを待ち、Cookie を読み出すため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('TwitterChromeLoginService','ChromiumLauncher','利用','ログイン用・Cookie 取得用のブラウザを共通の引数で起動するため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('TwitterChromeLoginService','ChromiumProfileLock','利用','ブラウザ起動前後にロック解除を待ち、古いデバッグ用ファイルを消すため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('TwitterChromeLoginService','ProcessUtils','利用','起動したブラウザを子プロセスごと終わらせるため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('TwitterChromeLoginService','TwitterCookies','利用','読み出した Cookie がそろっているか判定して返すため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('TwitterLoginDialog','AppConfig','利用','ログイン処理に設定を渡すため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('TwitterLoginDialog','AppIcons','利用','ダイアログにアプリのアイコンを付けるため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('TwitterLoginDialog','ClixService','利用','ログイン結果の型（成否とメッセージ）を使い、呼び出し元へ返すため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('TwitterLoginDialog','TwitterAuthService','生成','ブラウザを起動してXにログインし、Cookieを取得する処理を実行するため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('UiTheme','AppConfig','利用','文字サイズの既定値と上下限を得るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('UiTheme','AppLogging','利用','テーマ組み込みの失敗やフォントの代替を警告として記録するため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('UiTheme','ResultPalette','利用','控えめな文字色を、文字色と背景色を混ぜて作るため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('UpdateProgressDialog','AppIcons','利用','ダイアログにアプリのアイコンを付けるため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('WebhookTableModel','AppConfig','生成','Webhook の行(AppConfig.DiscordWebhookEntry)を作り、読み書きするため。');
INSERT INTO "_dependency"("from_class","to_class","kind","reason") VALUES('WindowsUpdateLauncher','OsInfo','利用','Windows 以外では更新スクリプトを起動しないため。');

INSERT INTO "_test"("class","name","method","checks","status") VALUES('AppLogging','maskWebhookUrlHidesToken','maskWebhookUrl','https://discord.com/api/webhooks/1234567890/ に続く秘密文字列が **** に置き換わり、番号部分はそのまま残る。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('AppLogging','sanitizeMasksWebhookInMessage','sanitize','「Failed: 」に続けて Webhook URL を含むメッセージでも、1234567890/**** の形に秘密部分が伏せられる。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('BatchPoster','sendsEveryBatchInOrder','postInOrder','2件のバッチが本文と画像枚数そのままに順に送られ、開始通知が 1、2 の順に来て、3枚とも送れた扱いになる。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('BatchPoster','stopsAtTheFirstFailureKeepingWhatWentOut','postInOrder','3件中2件目が失敗すると3件目は送られず、失敗内容と1件目のスクショだけが送れた分として返る。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('BmzPaths','defaultsWhenConfigTomlIsMinimal','resolve','version だけの config.toml でプロフィール指定が null のとき、プロフィールは default、スクショ保存先はデータフォルダ下の screenshots になる。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('BmzPaths','resolvesPathsFromConfigToml','resolve','active_profile が main、スクショ保存先が data/shots の config.toml から、プロフィール main の score.db・library.db・先頭の data/ を除いた shots フォルダが求まり、プロフィール指定 sub ではそれが使われ、プロフィール一覧は main・sub の順になる。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('BmzPlayLogService','findsLatestPlayBeforeScreenshot','findNear','時刻1000・1300・1310(オートプレイ)の履歴で、時刻1320の撮影には1300のHardのプレイ1件が返り(EXスコア175・BP3)、ランプID 6 指定でも見つかるが10指定では空になり、プレイから601秒後や最初のプレイより前の撮影では空になる。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('BmzTableReader','readsLibraryChartsAndDifficultyTables','readAll','library.db から楽曲を sha256 で引くと同じ sha256 の最初の行(表示タイトル「Song [ANOTHER]」、レベル12)が返り、難易度表はタグ sl の表1つ、空のレベル0を除いたフォルダ sl12 1つに、その曲が入って読み込まれる。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('ClearLamp','findsLongerNamesBeforeNamesTheyContain','findIn','EXHARD CLEAR・LIGHT ASSIST EASY CLEAR を含む文字列ではそれらが見つかり、曲名に MAX を含んでも CLEAR のスクショは CLEAR になる。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('ClearLamp','idsFollowBeatorajaNumbering','fromId','NO PLAY が 0、HARD CLEAR が 6、MAX が 10 で、7 は EXHARD、11 は該当なしになる。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('ClearLamp','looksUpByNameAndBmzLabel','fromDisplayName','前後空白・小文字の「 clear 」が CLEAR、LightAssistEasy が LIGHT ASSIST EASY CLEAR に引け、Hazard は該当なしになる。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('ClearTypeMapper','mapsBmzClearLabels','fromBmzLabel','BMZのラベル Normal が5、ExHard が7になり、知らない Hazard は null になり、ID 6 は HARD CLEAR、Normal 経由の ID は CLEAR というランプ名に戻る。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('DiscordAutoPostQueue','offerAcceptsOnlyResultScreenshots','offer','リザルト画面とプレイ画面のスクショを1枚ずつ入れると、行列に残るのはリザルトの1枚だけになる。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('DiscordAutoPostQueue','offerSkipsAlreadyPosted','offer','Discord 投稿済みの印が付いた result.png を入れても、行列は0枚のままになる。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('DiscordAutoPostQueue','pollBatchAndRequeueFront','pollBatch','3枚並べて2枚取り出すと残り1枚になり、その2枚を先頭に戻すと3枚に戻って、次に取り出す1枚目が a.png になる。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('DiscordAutoPostQueue','pollBatchNeverReturnsMoreThanAvailable','pollBatch','1枚しか並んでいない行列に4枚を求めても、返るのは1枚だけになる。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('DiscordAutoPostQueue','removePostedDropsMatchingEntry','removePosted','a.png と b.png を並べて a.png を取り除くと、残りは1枚で、取り出すと b.png になる。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('DiscordWebhookService','keepsFileNamesFromBreakingTheHeader','headerSafeFileName','二重引用符と改行を含むファイル名が、引用符は ''、改行は空白に置き換わる。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('DiscordWebhookService','picksTheImageTypeFromTheExtension','contentType','.PNG は image/png、.jpeg は image/jpeg、拡張子なしは image/png になる。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('DiscordWebhookService','refusesMoreImagesThanDiscordAllows','post','上限より1枚多い画像を渡すと、送信せずに失敗が返る。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('FilenameParser','beatorajaPlayerParsesMillisecondLikeNamesAsBeatoraja','parse','プレイヤー指定なし(beatoraja)では「20250908_141530_123_456 CLEAR AAA.png」のようなミリ秒風の名前も beatoraja 形式で読み、撮影時刻は秒まで、ランプ CLEAR、ランク AAA になる。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('FilenameParser','bmzPlayerIgnoresOtherNames','parse','BMZ指定で形式に合わない「bmz-screenshot-1761555600123.png」は画面種別が空になり、リザルト画面と判定されない。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('FilenameParser','bmzResultWithUnknownLampKeepsWholeBodyAsTitle','parse','BMZ形式で末尾が知らないラベル(Hazard)の「Song_Hazard」は、撮影時刻は取れたうえで、リザルト扱いのまま本文全体が曲名になりランプは空になる。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('FilenameParser','parseBmzNonResultScreenshots','parse','BMZ形式の Music_Select(フォルダ名付きも含む)・Decide_・Play_ の名前がそれぞれ Music Select・Decide・Play と判定され、Play では曲名と撮影時刻も取れる。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('FilenameParser','parseBmzResultScreenshot','parse','BMZ形式の「20261105_203045_123_Song_[ANOTHER]_Hard.png」を読むと、撮影時刻がミリ秒123まで入り、曲名 Song_[ANOTHER]、ランプ HARD CLEAR のリザルトになり、ランクと表フォルダ名は空になる。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('FilenameParser','parseBmzResultScreenshotWithDuplicateCounterAndLabelInTitle','parse','BMZ形式で末尾に重複番号 _1 が付いた名前は番号を除いて曲名とランプ(LIGHT ASSIST EASY CLEAR)が取れ、曲名にランプ名を含む「Song_Hard_Failed」では最後の Failed だけがランプ(FAILED)になり曲名は Song_Hard になる。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('FilenameParser','parseMusicSelectScreenshot','parse','「20250908_141530_Music_Select.png」を読むと、画面種別が Music Select になり、リザルト画面とは判定されない。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('FilenameParser','parsePlayScreenshot','parse','「20250908_141530_Play_LEVEL12 Song.png」を読むと、画面種別が Play になり、リザルト画面とは判定されない。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('FilenameParser','parseResultScreenshot','parse','「20250908_141530_LEVEL12 Song CLEAR AAA.png」を読むと、画面種別が Result、曲名が Song になり、リザルト画面と判定される。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('FilenameParser','parseTableResultScreenshot','parse','「20250908_141530_sl12 Song CLEAR AAA.png」を読むと、画面種別が Result、難易度表フォルダ名が sl12 になる。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('LampGroupedTweetText','groupsNotationsByLampBestFirstInPostOrder','generate','CLEAR と HARD CLEAR が交互の4譜面から、「HC : st3/集3 st2/★22」と「NC : st6/dy1 st4」の2行が良いランプ順・投稿順で作られる。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('LampGroupedTweetText','leavesOutAssistFailedAndNoPlayByDefault','generate','EASY・アシスト2種・FAILED・NO PLAY の5譜面を既定の表示設定で渡すと、「EC : st0」の1行だけになる。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('LampGroupedTweetText','shownLampsSharingAShortNameShareALine','generate','アシスト2種・FAILED・NO PLAY を表示、EASY を非表示にすると、「AS : st2 st1」「st4」「無し : st3」の3行になり、短い名前が同じアシストは1行、名前が空の FAILED は表記だけになる。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('LampGroupedTweetText','usedOnceAnImageGetsUnderFortyUnits','shouldUse','画像7枚では使わず、1枚あたり40単位を切る8枚と20枚では使うと判定される。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('LampGroupedTweetText','usesCustomShortNamesAndFallsBackToTitle','generate','HARD CLEAR の短い名前を「難」に上書きし表記なしの譜面を渡すと「難 : Song」となり、ランプ名が空の譜面は「? : st1」の行になる。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('PlayHistory','findsTheLatestPlayAtOrBeforeATime','latestAtOrBefore','299 までの最新は 200 のプレイ、300 までは 300 のプレイ(同時刻を含む)、99 までは該当なしになる。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('PlayHistory','returnsPlaysInsideTheWindowOldestFirst','between','時刻 100・200・300 の順不同の3件から、100〜250 の範囲は古い順に 100 と 200 の2件、301〜400 は0件になる。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('PlayScore','exScoreBpAndRate','exScore','1500ノーツの判定数から、EXスコア2620、理論値3000、BP45、スコアレート約87.333%、ランクAAになる。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('PlayScore','nearestBoundaryAheadOfCurrentRank','nearestBoundaryDiff','1000ノーツでEX1790のとき、MAX-210より近い AAA+12 になる。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('PlayScore','nearestBoundaryFromF','nearestBoundaryDiff','9ノーツでEX1のときは E-3 より近い F+1、EX3のときは E-1 になる。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('PlayScore','nearestBoundaryNearMax','nearestBoundaryDiff','1000ノーツでEX1990のときは MAX-10、理論値のときは MAX+0 になる。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('PlayScore','nearestBoundaryPicksCloserSide','nearestBoundaryDiff','1500ノーツでEX2620のとき、AA+286よりAAAまでの不足の方が近いため AAA-47 になる。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('PlayScore','nearestBoundaryTieGoesToPlusSide','nearestBoundaryDiff','9ノーツでEX15のとき、AA+1 と AAA-1 が同じ差なのでプラス側の AA+1 になる。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('PlayScore','rankBoundariesUseCeilingOfNinths','rank','1000ノーツではAAAの境目が切り上げで1778となり、EX1778はAAA、1776はAA、0はF、理論値でもランクとしてはAAAになる。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('PlayerKind','detectsPlayerKindFromFolderContents','detect','config_sys.json のあるフォルダは beatoraja、config.toml のある data フォルダとその親フォルダは BMZ、どちらもないフォルダは不明と判定される。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('PostBatchSplitter','partitionReturnsEmptyForEmptyInput','partition','空の一覧を渡すと空のリストが返る。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('PostBatchSplitter','partitionSplitsIntoFixedSizeBatches','partition','6件を最大4件で分けると、4件と2件の2束に順番どおり分かれる。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('PostTextComposer','joinsOneLinePerScreenshotInOrder','merged','2枚のスクショが既定の書式で「表記 曲名 ランプ ランク」の行になり、渡した順に改行でつながる。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('PostTextComposer','shortensLongTitlesUntilTheTweetFits','tweetTexts','長い日本語の曲名4枚のツイートは曲名が短くされ、短くした件数が数えられて280以内に収まる。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('PostTextComposer','usesTheLampGroupedFormForManyImages','tweetTexts','8枚のバッチはランプ別まとめ形式の「HC : …」「NC : …」の2行になり、曲名の短縮は数えない。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('PostTextFormat','normalizedAppendsMissingItemsDisabled','normalized','BP と未知の項目だけの書式を正規化すると、全項目の数にそろい、先頭は有効な BP のまま、補われた2番目の項目は無効になる。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('PostedStateStore','savesAndLoadsBackWithoutLeavingTemporaryFiles','save','Discord 投稿済みの印を保存して読み直すと印が残り、フォルダには posted.json 以外(一時ファイル)が残らない。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('PostedStateStore','setsAsideAnUnreadableFileAndSaysSo','load','壊れた posted.json を読むと説明文が返り、元のファイルは posted.json.broken-日時 に名前が変わって残る。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('TweetTextGenerator','allItemsWithAffixes','generate','全項目を有効にしてスコアを渡すと、「★12 Song FULL COMBO CLEAR AA BP45 87.33% EX2620 AAA-47」と前後の文字付きで並ぶ。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('TweetTextGenerator','defaultFormatMatchesOriginalLayout','generate','既定の書式では、スコアありでも表記だけの版でも「★12 Song FULL COMBO CLEAR AAA」になる。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('TweetTextGenerator','generateWithTitleLimitLeavesShortTitleUntouched','generate','「短いタイトル」を40単位に制限しても、曲名は切られず「…」も付かない。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('TweetTextGenerator','generateWithTitleLimitTruncatesLongTitle','generate','「あ」50文字の曲名を20単位に制限すると、投稿文に「…」が付き、元の曲名全体は含まれない。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('TweetTextGenerator','generateWithoutLimitKeepsFullTitle','generate','長さ制限なしで作ると、曲名「Short Title」がそのまま投稿文に入る。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('TweetTextGenerator','itemsFollowConfiguredOrderAndEnabledState','generate','BP を先頭に移して前後の文字を「ミス」「回」にし、表記を無効にすると、「ミス45回 Song FULL COMBO CLEAR AA 87.33% EX2620 AAA-47」になる。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('TweetTextGenerator','rankLabelsApplyToRankAndDiff','generate','ランク表示名を AA→ダブルA、AAA→トリプルA にすると、ランクとランク差の両方がその名前で出る。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('TweetTextGenerator','scoreItemsOmittedWithoutPlayData','generate','全項目有効でもスコアが無ければ BP・レート・EX・ランク差は省かれ、ランクはファイル名の AAA が表示名「3A」で出る。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('TweetTextGenerator','scoreRankOverridesFileNameRank','generate','ファイル名のランクが AAA でも、スコアから求めたランク AA が投稿文に使われる。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('TweetTextGenerator','titleTruncationWorksWithCustomFormat','generate','全項目有効の書式で曲名を20単位に制限すると、曲名に「…」が付き、末尾は「BP45 87.33% EX2620 AAA-47」のまま残る。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('TweetTextGenerator','truncatedTitleFitsWithinBudget','generate','ランプ・ランクの無いスクショで「漢」50文字の曲名を20単位に制限すると、投稿文全体が20単位以内に収まり、何度作っても同じ結果になる。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('TweetTextLimits','asciiCountsAsOneUnitPerCharacter','weightedLength','「Hello」の長さは5と数えられる。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('TweetTextLimits','exceedsLimitAtTheBoundary','exceedsLimit','半角280文字は上限内、281文字は上限超えと判定される。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('TweetTextLimits','japaneseCountsAsTwoUnitsPerCharacter','weightedLength','「あアー漢字」の5文字は1文字2として10と数えられる。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('TweetTextLimits','nullTextHasZeroLength','weightedLength','null の長さは0になる。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('VersionComparator','compareMinorVersions','isNewer','1.4.0 は 1.3.0 より、2.0.0 は 1.9.9 より新しいと判定され、上位の桁が優先される。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('VersionComparator','comparePatchVersions','isNewer','1.3.0 に対して 1.3.1 は新しく、同じ 1.3.0 と古い 1.2.9 は新しくないと判定される。','成功');
INSERT INTO "_test"("class","name","method","checks","status") VALUES('VersionComparator','normalizeStripsLeadingV','normalize','v1.3.0 から先頭の v が外れて 1.3.0 になる。','成功');

INSERT INTO "_refactor"("id","class","title","detail","priority","status") VALUES(1,'MainFrame','投稿処理をメイン画面から切り出す','MainFrame は画面組み立てに加え、X 投稿（postToTwitter、819〜926行）、Discord 手動送信（postToDiscord、928〜1010行）、Discord 自動投稿（tryAutoPostDiscord・postDiscordBatch、572〜660行）、投稿文の生成と曲名の自動短縮（resolveEntryMessage〜abbreviateBatchIfOverLimit、374〜721行）、確認ダイアログ（promptFinalPostTexts・promptFinalDiscordPostTexts）まで抱え、約1250行になっている。投稿文の組み立てを PostTextBuilder のような部品に、投稿の実行と投稿済み記録の更新を X 用・Discord 用の投稿クラスに、確認ダイアログを独立したダイアログクラスに移し、MainFrame はボタンから呼び出すだけにする。投稿文の作り方を画面なしで試験でき、変更の影響範囲も狭くなる。','高','完了');
INSERT INTO "_refactor"("id","class","title","detail","priority","status") VALUES(2,'MainFrame','X と Discord の投稿後処理の重複をまとめる','postToTwitter の 879〜924行と postToDiscord の 974〜1008行は、バッチを順に送り失敗で打ち切る流れ、成功分を投稿済みにして savePostedStateQuietly と databasePanel.setPostedStateStore を呼ぶ処理、「N 枚までは投稿済みです」の案内文作りがほぼ同じで、さらに成功時と失敗時の中でも投稿済みにする処理が二重に書かれている。送信関数と投稿済みの付け方だけを引数にした共通の手順に寄せれば、片方だけ直し忘れる事故を防げる。','中','完了');
INSERT INTO "_refactor"("id","class","title","detail","priority","status") VALUES(3,'MainFrame','選択変更のたびの照合を画面スレッドから外す','【対応済み】フォルダの走査を裏スレッドの索引作成に移し、新しいスクショ1枚ごとの読み直しを難易度表まで含めた全体から、プレイ履歴と曲データベースだけ(reloadPlayData)に絞った。【残り】resolvePostContext は選択が変わるたびに1枚ごとに DB 検索と照合(chartResolverService.enrich)を画面スレッドで行うため、多数選択時に固まりうる。裏で計算して結果を差し替える形にするには、ChartResolverService が裏の索引作成と画面スレッドの両方から使われている点(再読み込み中の接続の入れ替えと同時に使われうる)を先に整理する必要がある。','中','候補');
INSERT INTO "_refactor"("id","class","title","detail","priority","status") VALUES(4,'MainFrame','Discord の10枚上限の直書きを定数にする','Discord の1回あたりの画像上限が updateActionButtons（437〜438行の「11枚以上は10枚ずつ」）と postToDiscord（941行の PostBatchSplitter.partition の 10）に数値で直書きされている。X 側は ClixService.MAX_IMAGES_PER_POST を使っているので、Discord も DiscordWebhookService 側に定数を置いて参照すれば、上限変更時の直し漏れを防げる。','低','完了');
INSERT INTO "_refactor"("id","class","title","detail","priority","status") VALUES(5,'MainFrame','設定ダイアログ呼び出し後の保存処理を共通化する','openTablePriorityDialog・openPostTextFormatDialog・openTableNotationRulesDialog（1112〜1210行）は、結果が null なら終了、config に設定して config.save、失敗時に同じエラーダイアログ、という同じ手順を3回書いている。難易度表が未読込のときの案内と warnAboutFailedTableFiles の呼び出しも2回重複し、java.util や DifficultyTableRegistry の完全修飾名が散らばって読みにくい。保存とエラー表示を1つの補助メソッドにまとめ、import を整理する。','低','完了');
INSERT INTO "_refactor"("id","class","title","detail","priority","status") VALUES(6,'SettingsDialog','Webhook 編集が保存前に設定へ漏れるのを直す','loadValues で webhookTableModel.setRows(new ArrayList<>(config.getDiscordWebhooks())) としているが、コピーされるのは一覧だけで中の DiscordWebhookEntry は config と同じものなので、WebhookTableModel.setValueAt で名前や URL を書き換えると、保存せずにダイアログを閉じても実行中の設定が変わってしまう。読み込み時に各項目を複製してから表に渡すようにすれば、保存ボタンを押したときだけ設定が変わるようになる。','高','完了');
INSERT INTO "_refactor"("id","class","title","detail","priority","status") VALUES(7,'SettingsDialog','設定項目ごとのセクションを別クラスに分ける','【対応済み】2つの表モデル(ClearLampLabelTableModel、WebhookTableModel)を別ファイルにし、使われていない stretch を消し、javax.swing・java.awt などの完全修飾名を import に直した。【残り】フォルダ・X ログイン・Webhook・クリアランプ表示名・Discord 自動投稿・自動更新・外観の7区画の画面作り(build…Section)、読み込み(loadValues)、保存(saveSettings)が1クラスに同居しており、項目を1つ足すたびに3か所を直す必要がある。区画ごとに「画面を作る・設定から読む・設定へ書く」を持つ小さなパネルクラスに分ける。画面の見た目に関わる変更なので、実際に画面を開いて確認しながら進める。','中','候補');
INSERT INTO "_refactor"("id","class","title","detail","priority","status") VALUES(8,'DatabasePanel','削除と編集の DB・ファイル操作を画面から分ける','deleteSelectedRows（386〜435行）は画面スレッドで画像ファイルの削除と DB 行の削除を直接行い、DatabaseTableModel.setValueAt（667〜694行）はセル1つの編集ごとに DB 更新と全件の読み直し（reload）を行っている。ファイル削除と DB 更新をサービス側の1メソッドにまとめて裏で実行し、編集時は変わった行だけを差し替えるようにすると、件数が多いときの固まりを防ぎ、画面クラスがファイル操作を知らずに済む。あわせて、どこからも呼ばれていない getSelectedRecord を削除する。','中','完了');
INSERT INTO "_refactor"("id","class","title","detail","priority","status") VALUES(9,'PreviewPanel','プレビュー画像の読み込みを画面スレッドから外す','showEntries が各エントリについて buildPreviewRow から loadScaledImage を呼び、原寸のスクショを ImageIO.read で読み込んで縮小する処理を画面スレッド上で同期的に行っている。最大で数十枚を選ぶと選択のたびに画面が固まり、キャッシュもないので並び替えのたびに全画像を読み直す。ThumbnailCache と同様に裏スレッドで読み込み、プレビュー幅のサイズでキャッシュして、完成した行から差し替える形にすると操作が軽くなる。','高','完了');
INSERT INTO "_refactor"("id","class","title","detail","priority","status") VALUES(10,'DatabasePanel','使われていない ScreenshotListPanel を削除する','ScreenshotListPanel はどのクラスからも生成されておらず、表形式のデータベース画面(DatabasePanel)に置き換えられた旧部品として残っていた。中のサムネイル読み込みも画面スレッドで画像を読み、上限なしの HashMap に溜め続ける古い実装だった。【対応】クラスごと削除した。','中','完了');
INSERT INTO "_refactor"("id","class","title","detail","priority","status") VALUES(11,'ThumbnailCache','読み込みに失敗した画像を毎回読み直さないようにする','load は render が null（壊れた画像や読めないファイル）を返すと何も記録せず pending から外すだけなので、次の get で再び読み込みが依頼される。表が再描画されるたびに同じ壊れたファイルの読み込みが繰り返される。失敗したパスを専用の集合か代わりのアイコンで記録し、一定時間またはファイル更新まで再試行しないようにすると無駄な読み込みが防げる。また render で例外をすべて握りつぶしているので、原因を記録するとよい。','中','完了');
INSERT INTO "_refactor"("id","class","title","detail","priority","status") VALUES(12,'TablePriorityDialog','並べ替え・件数表示などダイアログ間の重複をまとめる','TablePriorityDialog と PostTextFormatDialog はどちらも、ボタンを作る button と選択行を1つ動かす moveBy をほぼ同じ形で持つ。さらに TablePriorityDialog と TableNotationRulesDialog は「難易度表: N 件（読み込んだ.bmtファイル M 件を同名/同タグでまとめた結果）」という件数表示や、OK・キャンセルの組み立て、表名がなければタグを出す表示処理を同じ文で書いている。共通の小さな部品（並べ替え可能なリスト、件数ラベル、OK/キャンセル付きダイアログの土台）に切り出すと文言や動作のずれを防げる。','低','候補');
INSERT INTO "_refactor"("id","class","title","detail","priority","status") VALUES(13,'TwitterLoginDialog','ログイン結果の受け渡しを安全にする','result フィールドは裏スレッドの runLogin で書き換え、画面スレッドの showAndLogin で読んでいるが、volatile などの同期がない。また、キャンセルや×で閉じると裏のログイン処理（ブラウザ操作）は続いたまま「未実行」の失敗が返り、その後に処理が成功しても呼び出し元には伝わらない。結果を AtomicReference などで受け渡し、閉じたときは処理の終了を待つか取り消しを確実に伝える形にすると、ログイン状態の表示とのずれを防げる。','中','完了');
INSERT INTO "_refactor"("id","class","title","detail","priority","status") VALUES(14,'UiTheme','テーマ依存の色定義を1か所にまとめる','surface・subtle・mutedText・alternateRow や applyDefaults の表の選択色は、呼ばれるたびに dark の真偽でダーク用・ライト用の16進値を直接書き分けている。ResultPalette の色表や Badge の readableText、ResultPalette の relativeLuminance と Badge の luminance（同じ計算式）にも同様の判定と計算が散らばっている。テーマごとの色をまとめた定義と、明るさ計算の共通関数にすると、色の調整や新しいテーマの追加が1か所で済む。','低','候補');
INSERT INTO "_refactor"("id","class","title","detail","priority","status") VALUES(15,'FirstRunWizard','初期設定のフォルダ解決処理を画面から分離する','completeSetup が、入力チェック、PlayerKind.detect によるプレイヤー判定、BmzPaths.resolve によるフォルダの解決、screenshot フォルダの作成確認、AppConfig への書き込みと保存までを1つのメソッドで行っている。「BMZ と判定されたら BMZ、それ以外は beatoraja」という判定は AppConfig 側にも同じ形で書かれており、beatoraja のスクショフォルダを screenshot サブフォルダと決め打ちする判断も画面の中にある。フォルダからプレイヤー種別とスクショフォルダを決める処理を画面外の共通関数に移すと、判定の重複がなくなり、画面なしでテストもできる。','低','候補');
INSERT INTO "_refactor"("id","class","title","detail","priority","status") VALUES(16,'TweetTextGenerator','generate の多重定義を整理する','generate が9通りの引数で多重定義されており、どれが最終的に使われるか分かりにくい。本番で使われているのは書式・スコア・最大長・ランプ名の上書き表を取る版だけで、データベースの記録を受け取る版と一覧を受け取る版は既定の書式で作るため、利用者の書式設定やランプ名の上書きが効かない。曲名の最大長とランプ名の上書き表などを1つの設定の値にまとめて入口を1〜2本に絞り、使われていない版は削除すると、呼び出し側の取り違えと設定の効き漏れを防げる。','中','完了');
INSERT INTO "_refactor"("id","class","title","detail","priority","status") VALUES(17,'WindowsUpdateLauncher','/MIR による削除範囲を限定する','launch が書き出す更新スクリプトは robocopy の /MIR でインストール先を更新元と同じ内容にするため、利用者が実行フォルダに後から置いたファイルは、配布 zip に無ければすべて消える。/MIR をやめて /E で上書きのみにするか、残すべきフォルダを /XD で除外すると、更新で利用者のファイルを失う危険をなくせる。','中','完了');
INSERT INTO "_refactor"("id","class","title","detail","priority","status") VALUES(18,'AppUpdateService','更新用の一時ファイルを片付ける','tryAutoUpdate が作る一時フォルダ(zip と展開先)と、WindowsUpdateLauncher.launch が作る ps1 スクリプトは、成功しても失敗しても削除されず一時フォルダに溜まる。失敗時は tryAutoUpdate で作業フォルダを消し、成功時は更新スクリプトの最後で自分と更新元フォルダを消すようにすると、更新のたびにディスクを無駄に使わなくなる。','低','完了');
INSERT INTO "_refactor"("id","class","title","detail","priority","status") VALUES(19,'AppUpdateService','Windows 判定を共通化する','AppUpdateService、WindowsUpdateLauncher、ProcessUtils にそれぞれ同じ内容の非公開メソッド isWindows があり、os.name に win を含むかを重複して調べている。共通の道具クラスに1つ置いて使い回すと、判定方法を変えるときの直し漏れを防げる。','低','完了');
INSERT INTO "_refactor"("id","class","title","detail","priority","status") VALUES(20,'PostedStateStore','posted.json を安全に書き換える','save は posted.json を直接上書きしており、書き込み中にアプリが落ちると JSON が壊れる。そのうえ load は読めないファイルを警告だけで空扱いにするため、次の起動で全件が未投稿に見え、Discord の自動投稿で同じスクショを再送しかねない。一時ファイルに書いてから置き換える方式にし、読めなかったファイルは退避して利用者に知らせると、二重投稿を防げる。','中','完了');
INSERT INTO "_refactor"("id","class","title","detail","priority","status") VALUES(21,'ChartResolverService','画面種別の判定を文字列比較から外す','resolveFromPlayerData は画面名が Play・Music Select・Decide・Config のどれかかを文字列で直接比べてプレイ検索を省いている。一方 DiscordAutoPostQueue は ScreenshotEntry.isResultScreenshot を使っており、判定の基準が2か所に分かれている。ScreenshotEntry 側に「プレイ結果を持ちうる画面か」を返すメソッドを用意してそれを使うと、画面名の追加や変更で片方だけ直し忘れることを防げる。','中','完了');
INSERT INTO "_refactor"("id","class","title","detail","priority","status") VALUES(22,'DiscordWebhookService','枚数上限と画像形式の決め打ちをなくす','post は上限10枚を数値の直書きで判定しており、ClixService.MAX_IMAGES_PER_POST のような定数になっていない。また buildMultipartBody はファイルの種類によらず image/png を付け、ファイル名もそのまま引用符の中に入れている。上限を公開定数にして呼び出し側の分割処理と共有し、Content-Type を拡張子から決め、ファイル名の引用符や改行を除くと、仕様変更と想定外のファイル名に強くなる。','低','完了');
INSERT INTO "_refactor"("id","class","title","detail","priority","status") VALUES(23,'ScreenshotWatcher','一時的な読み取り失敗でフォルダ監視が止まるのを直す','waitForFile は Files.size の IOException(書き込み途中のロックなど)でも割り込み旗を立てて抜けるため、次の watchService.poll が InterruptedException を出し、watchLoop が return して以後の新しいスクショを検知しなくなる。ログにも残らない。IOException では割り込み旗を立てずに待ちを続け(InterruptedException のときだけ立てる)、listener.onScreenshotAdded の呼び出しも try で囲んでログに残して続行するようにすれば、監視が黙って止まる事態を防げる。なお MainFrame の listener は画面スレッドへ処理を渡すだけなので、通知先の処理で出た例外はこのループには届かない。','高','完了');
INSERT INTO "_refactor"("id","class","title","detail","priority","status") VALUES(24,'TwitterAuthService','ログイン確認と投稿で使う仕組みがずれている','verifyStoredSession と loginInteractive は、設定に保存した Cookie を ClixAuthSupport で clix 用 auth.json に書き、clix の auth status で有効性を確かめている。一方で実際の投稿は TwitterBrowserPostService がブラウザプロファイルのログイン状態だけを使い、保存 Cookie も clix も使わない。そのため clix が無いと投稿できる状態でも「認証失敗」となり、逆に Cookie が有効でもプロファイル側が切れていれば投稿画面が開けない。確認方法を投稿と同じブラウザプロファイル側（例えば CDP で x.com の Cookie や投稿画面の有無を見る）にそろえ、clix 依存と Cookie の設定保存を整理すれば、判定と実際の結果が一致し外部 CLI への依存も減る。【保留の理由】ログイン確認の仕組みそのものを置き換える変更で、正しさは実際の X アカウントとブラウザでしか確かめられない。どの方式で確認するか(ブラウザを裏で起動するか、保存 Cookie の有無だけで判断するか)を決めてから着手する。','高','候補');
INSERT INTO "_refactor"("id","class","title","detail","priority","status") VALUES(25,'TwitterBrowserPostService','ブラウザ起動処理の重複をまとめる','TwitterBrowserPostService の post と TwitterChromeLoginService の openLoginBrowser・runCdpExtraction は、どちらも ChromiumBrowserLocator での実行ファイル探し、プロファイル作成、ChromiumProfileLock でのロック待ちとファイル消去、--user-data-dir などの起動引数の組み立て、browserProcess の保持と ProcessUtils での停止を別々に書いている。起動引数は post が baseLaunchArgs を使わず自前で並べており、片方だけ直す事故が起きやすい。プロファイルを指定してブラウザを起動しデバッグポートを返す共通部品を作り、両者から使うようにすれば、引数やロック手順の変更が一か所で済む。','中','完了');
INSERT INTO "_refactor"("id","class","title","detail","priority","status") VALUES(26,'AppLogging','データフォルダの決め方が二重にある','AppLogging.resolveLogDir と AppPaths.resolveAppData が、それぞれ APPDATA と user.home から beatoraja-screenshot-manager（または .beatoraja-screenshot-manager）を組み立てている。フォルダ名や置き場所を変えるときに片方だけ直すとログと設定が別の場所に分かれる。AppLogging が AppPaths の初期化に依存しないよう注意しつつ、場所の決め方を1つの関数にまとめる。','中','完了');
INSERT INTO "_refactor"("id","class","title","detail","priority","status") VALUES(27,'TwitterAuthService','使われていない ensureAuthenticated を整理する','ensureAuthenticated（確認→静かな取り直し→対話ログイン）は、どの画面からも呼ばれていない。SettingsDialog は verifyStoredSession と refreshSilently を自分で組み合わせて同じ流れを書いている。どちらかに寄せ、使わない方を消せば、認証の流れを読む人が迷わなくなる。','低','候補');
INSERT INTO "_refactor"("id","class","title","detail","priority","status") VALUES(28,'ChromiumCdpSession','接続切断時に応答待ちが時間切れまで残る','WebSocket.Listener は onError でしか待ち中の命令を打ち切らず、onClose を実装していない。ブラウザが閉じられて接続が正常に閉じた場合、send は COMMAND_TIMEOUT（15秒）まで待ってから時間切れになる。また待ち時間を toSeconds で秒に切り捨てているため1秒未満の指定は0秒になる。onClose でも待ち表を例外で打ち切り、待ち時間をミリ秒で扱えば、利用者がブラウザを閉じたときの反応が速くなり誤った時間切れも防げる。','中','完了');
INSERT INTO "_refactor"("id","class","title","detail","priority","status") VALUES(29,'ChromiumDevToolsClient','固定待ちと決め打ちの時間を見直す','readTwitterCookies は x.com/home へ移動したあと一律2.5秒眠ってから Cookie を読む。また isDebugPortReady は0.15秒ごとの確認のたびに新しい HttpClient を作り、close は中身が空である。ページの読み込み完了（Page.loadEventFired 等）を待つ形にし、HttpClient はインスタンスのものを使い回し、空の close は AutoCloseable をやめるか資源を閉じる実装にすれば、無駄な待ちと誤解を減らせる。','低','候補');
INSERT INTO "_refactor"("id","class","title","detail","priority","status") VALUES(30,'TwitterAuthService','loginInteractive の不要な null 判定を消す','loginInteractive は listener が null のとき早い段階で「ログイン UI が利用できません」を投げるのに、その後も listener != null の判定を何度も繰り返している。冒頭で null を弾いて失敗を返すようにすれば、ブラウザを無駄に起動することもなくなり、流れも読みやすくなる。','低','候補');
INSERT INTO "_refactor"("id","class","title","detail","priority","status") VALUES(31,'AppConfig','設定クラスを分野ごとに分割する','AppConfig はファイルの読み書き(load・save)と、プレイヤー・Discord・X認証・画面表示・難易度表・投稿文・ランプ表示名という無関係な設定の getter/setter 60個近くを1クラスに抱えている。分野ごとの小さな設定クラスに分けて AppConfig はそれらを束ねるだけにすると、変更の影響範囲が分かりやすくなる。【見送りの理由】分けると設定ファイル(config.json)の形が変わり、既存利用者の設定を読み替える移行処理が要るうえ、呼び出し側60か所近くを書き換えることになり、得られる効果に比べて設定が消える危険が大きい。保存を一時ファイル経由にする(AtomicFiles)ことと、フォルダ推定を PlayerKind へ移すことは今回済ませた。','中','見送り');
INSERT INTO "_refactor"("id","class","title","detail","priority","status") VALUES(32,'AppConfig','Xトークンの旧形式欄を移行後に廃止する','twitterAuthToken・twitterCt0 欄は、load 時の normalizeTwitterCookies で twitterCookies に移し替えた後は常に空になるのに、getTwitterAuthToken・getTwitterCt0 は今もそれを予備として読み、setTwitterCookies などでも毎回空にしている。移行処理だけに旧欄を残し(読み込み専用の項目にする)、getter はCookie一覧だけを見るようにすると、どこに値があるかの分岐が消えて読みやすくなる。','低','候補');
INSERT INTO "_refactor"("id","class","title","detail","priority","status") VALUES(33,'ClearTypeMapper','ランプ名の一覧を1か所にまとめる','beatorajaのランプ名11種が ClearTypeMapper の CLEAR_NAME_TO_ID と CLEAR_NAMES、ClearLampLabels の CANONICAL_ORDER・DEFAULT_SHORT_LABELS、FilenameParser の CLEAR_TYPES に別々に書かれている(FilenameParser は部分一致のため長い順という別の並び)。ランプをIDと名前・BMZラベル・既定の短い名前を持つ列挙型にまとめ、各クラスはそこから導くようにすると、追加や表記変更の漏れを防げる。','高','完了');
INSERT INTO "_refactor"("id","class","title","detail","priority","status") VALUES(34,'PlayerPlayLogService','PlayLogEntry を共通の場所へ移し、履歴読み込み処理を共通化する','PlayLogSource や BmzPlayLogService が使う PlayLogEntry が beatoraja 実装の PlayerPlayLogService の中で定義されており、BMZ側が beatoraja 側のクラスに依存している。また両クラスとも readColumns(PRAGMA で列確認)、時刻順の並べ替え、二分探索、available フラグと close を同じ形で重複して持つ。PlayLogEntry を独立したレコードにし、列名の違いだけを差し替える共通の基底クラスか補助クラスにまとめると、重複が減りプレイヤー追加も楽になる。','中','完了');
INSERT INTO "_refactor"("id","class","title","detail","priority","status") VALUES(35,'EnrichedScreenshot','非推奨の TableLookupService から照合結果と推定処理を移して削除する','非推奨の TableLookupService に、照合結果の EnrichedScreenshot と、スクショフォルダからプレイヤーフォルダを推定する deriveBeatorajaDirectory が残っていて消せなかった。【対応】EnrichedScreenshot を model パッケージの独立したレコードに、推定処理を PlayerKind.playerFolderOf に移し、TableLookupService を削除した。','中','完了');
INSERT INTO "_refactor"("id","class","title","detail","priority","status") VALUES(36,'DifficultyTableRegistry','表記ルールの型の二重定義をなくす','同じ内容の表記ルールが、保存用の AppConfig.TableNotationRule(可変クラス)と照合用の DifficultyTableRegistry.TableNotationRule(レコード)の2つあり、ChartResolverService の toRegistryRules で詰め替えている。さらに DifficultyTableRegistry は空キーの定数を AppConfig から参照しており、table パッケージが config に依存している。1つの型にまとめるか、定数を表記ルール側に置くと詰め替えと依存がなくなる。','低','候補');
INSERT INTO "_refactor"("id","class","title","detail","priority","status") VALUES(37,'DifficultyTableRegistry','表フォルダの場所の解決を BeatorajaPaths に任せる','DifficultyTableRegistry の resolveTablePath が config_sys.json を独自に読んで tablepath を解決しており、BeatorajaPaths.resolve と同じ処理が重複している(しかも config_sys.json がない場合の扱いが異なる)。BeatorajaPaths.tableDirectory を使うか、表フォルダを受け取る loadFromTableDirectory だけに絞ると、設定の読み方が1か所に揃う。また、表名を入れている変数や項目が tableTag という名前なのも、記号のタグと紛らわしいので tableKey などに改めるとよい。','低','候補');
INSERT INTO "_refactor"("id","class","title","detail","priority","status") VALUES(38,'ScreenshotEntry','使われていない getTableLevel を削除する','getTableLevel は非推奨で getRawTableFolder と同じ値を返すだけで、本体コードからはもう呼ばれていない。削除すれば、レベルを返すのかフォルダ名を返すのかという名前の紛らわしさがなくなる。','低','候補');
INSERT INTO "_refactor"("id","class","title","detail","priority","status") VALUES(39,'PlayerKind','UNKNOWN を移行用の判定結果に限定する','PlayerKind.UNKNOWN は、プレイヤー設定のない古い設定ファイルを読み替えるとき(AppConfig.playerKind の detect と fromConfigKey)だけ必要だが、通常の列挙値として並んでいるため、ChartResolverService の初期値など本来はあり得ない状態として使われ、呼び出し側ごとに不明時の扱いが必要になる。detect や fromConfigKey の結果を Optional などで返し、列挙値は beatoraja と BMZ の2つにすると、不明状態の分岐がなくなる。','低','候補');
INSERT INTO "_refactor"("id","class","title","detail","priority","status") VALUES(40,'PostTextFormat','項目表示名の並び順が保たれない','ITEM_DISPLAY_NAMES はコメント上「既定の順」とされているが、LinkedHashMap を Map.copyOf で複製しているため順序が保証されない。順序が必要な場面では ITEM_KEYS を使う必要があり紛らわしい。Collections.unmodifiableMap で包むなど、順序を保つ形にすると意図どおりになる。','低','候補');

INSERT INTO "AccentButton"("method","signature","behavior") VALUES('setEnabled','void setEnabled(boolean enabled)','有効・無効を切り替えたあと色を塗り直す。有効なら指定色の背景と白文字、無効なら背景・文字色を外して標準の無効表示にする。指定色がnullなら色には触らない。');

INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('clearTwitterAuth','void clearTwitterAuth()','保存しているXのCookieと旧形式のトークン(auth_token・ct0)をすべて消す。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('getBeatorajaDirectory','String getBeatorajaDirectory()','設定されたプレイヤーフォルダ(beatoraja または BMZ Player)のパス文字列を返す。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('getClearLampGroupedVisibility','Map<String, Boolean> getClearLampGroupedVisibility()','ランプ別まとめ投稿でランプを表示するかどうかの上書き設定(ランプ名→表示有無)を返す。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('getClearLampLabels','Map<String, String> getClearLampLabels()','ランプの表示名(俗称)の上書き設定(大文字のランプ名→表示名)を返す。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('getClearLampShortLabels','Map<String, String> getClearLampShortLabels()','ランプ別まとめ投稿で使う短いランプ名の上書き設定を返す。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('getDiscordAutoPostBatchSize','int getDiscordAutoPostBatchSize()','Discord自動投稿で1回にまとめる枚数(1〜10、既定4)を返す。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('getDiscordAutoPostWebhookName','String getDiscordAutoPostWebhookName()','Discord自動投稿に使うwebhookの名前を返す。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('getDiscordWebhooks','List<DiscordWebhookEntry> getDiscordWebhooks()','登録済みのDiscord webhook(名前とURL)の一覧を返す。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('getPlayer','String getPlayer()','保存されているプレイヤー種別のキー文字列(beatoraja / bmz、古い設定では空)をそのまま返す。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('getPlayerName','String getPlayerName()','beatorajaのプレイヤー名、またはBMZのプロフィールIDの指定を返す。空なら各プレイヤーの設定ファイルの値を使う。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('getPostTextFormat','PostTextFormat getPostTextFormat()','投稿文の書式設定を返す。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('getScreenshotDirectory','String getScreenshotDirectory()','スクショフォルダのパスを返す。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('getTableNotationRules','List<TableNotationRule> getTableNotationRules()','難易度表ごとの表記ルール(記号の置き換え・常に併記)の一覧を返す。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('getTablePriorityOrder','List<String> getTablePriorityOrder()','難易度表の優先順(表名の並び)を返す。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('getTwitterAuthToken','String getTwitterAuthToken()','XのCookie一覧に auth_token があればそれを、なければ旧形式で保存された値を返す。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('getTwitterBrowser','String getTwitterBrowser()','Xへの投稿に使うブラウザの指定を返す。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('getTwitterChromeProfile','String getTwitterChromeProfile()','Xへの投稿に使うChromeのプロフィール名を返す。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('getTwitterCookies','Map<String, String> getTwitterCookies()','保存しているXのCookie(名前→値)を返す。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('getTwitterCt0','String getTwitterCt0()','XのCookie一覧に ct0 があればそれを、なければ旧形式で保存された値を返す。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('getUiFontFamily','String getUiFontFamily()','画面のフォント名を返す。空のときは日本語を表示できる最初のフォントを使う意味になる。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('getUiFontSize','int getUiFontSize()','画面の文字サイズを返す。10〜24の範囲外(未設定の0を含む)なら既定の13を返す。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('getUiTheme','String getUiTheme()','画面テーマを返す。light 以外はすべて dark として返す。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('hasManualTwitterAuth','boolean hasManualTwitterAuth()','Xの auth_token と ct0 が両方そろっているかを返す。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('isAutoUpdateEnabled','boolean isAutoUpdateEnabled()','自動更新が有効かを返す。未設定(古い設定ファイル)の場合は有効として扱う。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('isDeleteScreenshotsOnExit','boolean isDeleteScreenshotsOnExit()','終了時にスクショを削除する設定かを返す。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('isDiscordAutoPostEnabled','boolean isDiscordAutoPostEnabled()','Discordへの自動投稿が有効かを返す。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('isFirstRunCompleted','boolean isFirstRunCompleted()','初回設定ウィザードを終えたかを返す。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('load','AppConfig load()','アプリデータフォルダの設定ファイルを読み込んで AppConfig を返す。ファイルがない、または読めない場合は既定値の設定を返し(読めないときは警告ログを出す)、旧形式のXトークンはCookie一覧へ移し替える。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('playerKind','PlayerKind playerKind()','使うプレイヤー種別を返す。保存値が beatoraja / bmz ならそれを返し、空や不明な値のときはプレイヤーフォルダの中身から判定して、BMZと判定できた場合だけ BMZ、それ以外は beatoraja を返す。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('resolveBeatorajaDirectory','java.nio.file.Path resolveBeatorajaDirectory()','プレイヤーフォルダのパスを返す。明示設定があればそれを、なければスクショフォルダから推定したフォルダを返し、どちらも空なら null を返す。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('resolveClearLampLabel','String resolveClearLampLabel(String clearType)','ランプ名に表示名の上書きがあればそれを、なければ元のランプ名を返す。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('resolveDiscordAutoPostWebhook','DiscordWebhookEntry resolveDiscordAutoPostWebhook()','自動投稿先のwebhookを決める。名前が設定されていて同名でURLのあるものがあればそれを、なければURLのある最初のwebhookを、どれもなければ null を返す。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('save','void save()','アプリデータフォルダを作ったうえで、設定全体を整形済みJSONとして設定ファイルに書き出す。書き込み失敗時は IOException を投げる。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('setAutoUpdateEnabled','void setAutoUpdateEnabled(boolean autoUpdateEnabled)','自動更新の有効・無効を設定する。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('setBeatorajaDirectory','void setBeatorajaDirectory(String beatorajaDirectory)','プレイヤーフォルダのパスを設定する。null は空文字にする。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('setClearLampGroupedVisibility','void setClearLampGroupedVisibility(Map<String, Boolean> clearLampGroupedVisibility)','ランプ別まとめ投稿での表示有無の上書きを、コピーして設定する。null は空にする。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('setClearLampLabels','void setClearLampLabels(Map<String, String> clearLampLabels)','ランプの表示名の上書きを、コピーして設定する。null は空にする。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('setClearLampShortLabels','void setClearLampShortLabels(Map<String, String> clearLampShortLabels)','短いランプ名の上書きを、コピーして設定する。null は空にする。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('setDeleteScreenshotsOnExit','void setDeleteScreenshotsOnExit(boolean deleteScreenshotsOnExit)','終了時にスクショを削除するかを設定する。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('setDiscordAutoPostBatchSize','void setDiscordAutoPostBatchSize(int discordAutoPostBatchSize)','Discord自動投稿のまとめ枚数を設定する。1未満は1、10超は10に丸める。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('setDiscordAutoPostEnabled','void setDiscordAutoPostEnabled(boolean discordAutoPostEnabled)','Discordへの自動投稿の有効・無効を設定する。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('setDiscordAutoPostWebhookName','void setDiscordAutoPostWebhookName(String discordAutoPostWebhookName)','自動投稿に使うwebhookの名前を設定する。null は空文字にする。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('setDiscordWebhooks','void setDiscordWebhooks(List<DiscordWebhookEntry> discordWebhooks)','Discord webhookの一覧を設定する。null は空の一覧にする。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('setFirstRunCompleted','void setFirstRunCompleted(boolean firstRunCompleted)','初回設定ウィザードを終えたかを設定する。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('setPlayer','void setPlayer(String player)','プレイヤー種別のキー文字列をそのまま設定する。null は空文字にする。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('setPlayerKind','void setPlayerKind(PlayerKind kind)','プレイヤー種別を受け取り、その保存用キーを設定する。null なら空文字にする。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('setPlayerName','void setPlayerName(String playerName)','プレイヤー名(BMZではプロフィールID)を設定する。null は空文字にする。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('setPostTextFormat','void setPostTextFormat(PostTextFormat postTextFormat)','投稿文の書式を正規化してから設定する。null なら既定の書式にする。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('setScreenshotDirectory','void setScreenshotDirectory(String screenshotDirectory)','スクショフォルダのパスを設定する。null は空文字にする。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('setTableNotationRules','void setTableNotationRules(List<TableNotationRule> tableNotationRules)','難易度表ごとの表記ルール一覧を設定する。null は空の一覧にする。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('setTablePriorityOrder','void setTablePriorityOrder(List<String> tablePriorityOrder)','難易度表の優先順を設定する。null は空の一覧にする。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('setTwitterAuthToken','void setTwitterAuthToken(String twitterAuthToken)','XのCookie一覧の auth_token を設定し、空なら削除する。旧形式の保存欄は空にする。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('setTwitterBrowser','void setTwitterBrowser(String twitterBrowser)','Xへの投稿に使うブラウザを設定する。null は空文字にする。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('setTwitterChromeProfile','void setTwitterChromeProfile(String twitterChromeProfile)','Xへの投稿に使うChromeのプロフィール名を設定する。null は空文字にする。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('setTwitterCookies','void setTwitterCookies(Map<String, String> twitterCookies)','XのCookie一覧をコピーして置き換え、旧形式の auth_token・ct0 欄は空にする。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('setTwitterCt0','void setTwitterCt0(String twitterCt0)','XのCookie一覧の ct0 を設定し、空なら削除する。旧形式の保存欄は空にする。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('setUiFontFamily','void setUiFontFamily(String uiFontFamily)','画面のフォント名を前後の空白を除いて設定する。null は空文字にする。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('setUiFontSize','void setUiFontSize(int uiFontSize)','画面の文字サイズを10〜24の範囲に丸めて設定する。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppConfig"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles","利用_ClearLampLabels","利用_PlayerKind","利用_PostTextFormat") VALUES('setUiTheme','void setUiTheme(String uiTheme)','画面テーマを設定する。light(大文字小文字を問わない)以外は dark にする。',NULL,NULL,NULL,NULL,NULL,NULL);

INSERT INTO "AppDataLocation"("method","signature","behavior") VALUES('resolve','Path resolve()','APPDATA があればその下の beatoraja-screenshot-manager、無ければホームフォルダの .beatoraja-screenshot-manager を返す。フォルダは作らない。');

INSERT INTO "AppIcons"("method","signature","behavior","利用_AppLogging") VALUES('applyTo','void applyTo(Window window)','渡されたウィンドウにアプリのアイコン画像を設定する。画像は初回だけ読み込んで保持し、リソースが見つからない場合は何もせず戻る。',NULL);

INSERT INTO "AppLogging"("method","signature","behavior","利用_AppDataLocation") VALUES('get','Logger get()
Logger get(Class<?> type)','ログ初期化を済ませたうえでロガーを返す。引数なしはアプリ全体の親ロガー com.beatoraja.screenshot を、クラスを渡した版はそのクラス名のロガーを返す。',NULL);
INSERT INTO "AppLogging"("method","signature","behavior","利用_AppDataLocation") VALUES('initialize','void initialize()','まだなら一度だけログを設定する。ログフォルダを作り、app.0.log から始まる最大1MB×5世代の追記ファイル出力（全レベル）と画面出力（INFO 以上）を親ロガーに付け、上位への流し込みを止める。フォルダやファイルが作れないときは標準エラーに一行出すだけで、未初期化のまま続く。',NULL);
INSERT INTO "AppLogging"("method","signature","behavior","利用_AppDataLocation") VALUES('maskWebhookUrl','String maskWebhookUrl(String url)','Discord の Webhook URL の秘密部分（webhooks/数字/ の後ろ）を **** に置き換えた文字列を返す。null や空文字はそのまま返す。',NULL);
INSERT INTO "AppLogging"("method","signature","behavior","利用_AppDataLocation") VALUES('resolveLogDir','Path resolveLogDir()','ログフォルダの場所として、AppDataLocation が決めるデータフォルダの下の logs を返す。フォルダは作らない。',NULL);
INSERT INTO "AppLogging"("method","signature","behavior","利用_AppDataLocation") VALUES('sanitize','String sanitize(String message)','任意のメッセージ中の秘密を伏せ字にして返す。Webhook URL の秘密部分、auth_token= と ct0= の値をそれぞれ **** に置き換え、null や空文字はそのまま返す。',NULL);

INSERT INTO "AppPaths"("method","signature","behavior","利用_AppDataLocation","利用_AppLogging") VALUES('appDataDir','Path appDataDir()','アプリのデータフォルダ(場所は AppDataLocation が決める)を返す。クラス読み込み時に作成を試みる。',NULL,NULL);
INSERT INTO "AppPaths"("method","signature","behavior","利用_AppDataLocation","利用_AppLogging") VALUES('bundledClixExe','Path bundledClixExe()','アプリに同梱された clix.exe の場所として、実行フォルダの tools\clix.exe を返す。存在するかは確かめない。',NULL,NULL);
INSERT INTO "AppPaths"("method","signature","behavior","利用_AppDataLocation","利用_AppLogging") VALUES('clixHomeDir','Path clixHomeDir()','clix に渡す専用のホームフォルダとして、データフォルダの下の clix-home を返す。',NULL,NULL);
INSERT INTO "AppPaths"("method","signature","behavior","利用_AppDataLocation","利用_AppLogging") VALUES('configFile','Path configFile()','設定ファイルの場所として、データフォルダの下の config.json を返す。',NULL,NULL);
INSERT INTO "AppPaths"("method","signature","behavior","利用_AppDataLocation","利用_AppLogging") VALUES('exeDir','Path exeDir()','アプリの実行フォルダを返す。システムプロパティ app.dir が実在するフォルダならそれを、無ければこのクラスを読み込んだ JAR のあるフォルダ（クラスフォルダならそのもの）を、それも取れなければ作業フォルダを使う。',NULL,NULL);
INSERT INTO "AppPaths"("method","signature","behavior","利用_AppDataLocation","利用_AppLogging") VALUES('logsDir','Path logsDir()','ログフォルダの場所を返す。中身は AppLogging の resolveLogDir と同じ。',NULL,NULL);
INSERT INTO "AppPaths"("method","signature","behavior","利用_AppDataLocation","利用_AppLogging") VALUES('postedStateFile','Path postedStateFile()','投稿済みスクリーンショットの記録ファイルの場所として、データフォルダの下の posted.json を返す。',NULL,NULL);
INSERT INTO "AppPaths"("method","signature","behavior","利用_AppDataLocation","利用_AppLogging") VALUES('twitterChromeProfileDir','Path twitterChromeProfileDir()','X ログイン用ブラウザのプロファイルフォルダとして、データフォルダの下の twitter-chrome-profile を返す。',NULL,NULL);

INSERT INTO "AppUpdateService"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AppVersion","生成_GitHubReleaseClient","利用_OsInfo","利用_VersionComparator","利用_WindowsUpdateLauncher","利用_ZipExtractUtils") VALUES('mainExecutable','Path mainExecutable()','アプリの実行フォルダにある本体 exe のパスを返す。ファイルが無ければ null を返す。',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppUpdateService"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AppVersion","生成_GitHubReleaseClient","利用_OsInfo","利用_VersionComparator","利用_WindowsUpdateLauncher","利用_ZipExtractUtils") VALUES('shouldCheckForUpdates','boolean shouldCheckForUpdates(boolean autoUpdateEnabled, String... args)','起動時に更新確認をするかを決める。自動更新が有効、Windows、バージョンが開発版でない、起動引数に --skip-update が無い、本体 exe が実在する、のすべてを満たすときだけ true を返す。',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "AppUpdateService"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AppVersion","生成_GitHubReleaseClient","利用_OsInfo","利用_VersionComparator","利用_WindowsUpdateLauncher","利用_ZipExtractUtils") VALUES('tryAutoUpdate','UpdateOutcome tryAutoUpdate(ProgressListener listener)','最新リリースを取得して今のバージョンより新しければ、一時フォルダへ zip をダウンロード・展開し、中に本体 exe があることを確かめてから更新スクリプトを起動して RESTARTING を返す(一時フォルダはスクリプトが消す)。最新なら UP_TO_DATE、バージョンが取れなければ NOT_APPLICABLE を返す。途中で失敗したら一時フォルダを消し、例外をログと通知先に流して FAILED を返す。本体 exe が無い・インストール先に書き込めない場合も失敗扱いになる。',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);

INSERT INTO "AppVersion"("method","signature","behavior") VALUES('get','String get()','アプリの版番号を返す。JAR に版番号が書かれていなければ dev を返す。');

INSERT INTO "AtomicFiles"("method","signature","behavior") VALUES('setAside','Path setAside(Path file)','読めないファイルを <名前>.broken-<年月日-時分秒> に名前を変えて残し、次の保存で上書きされないようにする。新しいパスを返す。');
INSERT INTO "AtomicFiles"("method","signature","behavior") VALUES('write','void write(Path target, byte[] content)','書き込み先と同じフォルダの一時ファイルに書いてから、書き込み先へ置き換えで移す(できれば一括置き換え)。途中で落ちても元のファイルは壊れない。一時ファイルは必ず消す。');

INSERT INTO "Badge"("method","signature","behavior","利用_ResultPalette","利用_UiTheme") VALUES('height','int height(FontMetrics metrics)','指定フォントの行の高さに上下の余白（各3ピクセル）を足したピルの高さを返す。UIの文字サイズ設定に合わせてピルの高さが変わる。',NULL,NULL);
INSERT INTO "Badge"("method","signature","behavior","利用_ResultPalette","利用_UiTheme") VALUES('label','JLabel label(String text, Color color)','文字と色を受け取り、ピルとして描かれるラベル部品を作って返す。文字は太字で一回り小さくし、大きさは文字幅に合わせて固定する。背景色には親部品の背景色（親がなければテーマの面色）を使う。',NULL,NULL);
INSERT INTO "Badge"("method","signature","behavior","利用_ResultPalette","利用_UiTheme") VALUES('paint','int paint(Graphics2D g2, int x, int y, int height, String text, Color color, Color background)','指定位置に、色を背景に22%混ぜた塗りと45%混ぜた枠のピルを描き、中に文字を描いて、使った幅を返す。ピルは与えられた高さの中で上下中央に置く。文字色は、明るい背景に明るい色なら暗く、暗い背景に暗い色なら明るく補正する。',NULL,NULL);
INSERT INTO "Badge"("method","signature","behavior","利用_ResultPalette","利用_UiTheme") VALUES('width','int width(FontMetrics metrics, String text)','文字の描画幅に左右の余白（各8ピクセル）を足したピルの幅を返す。',NULL,NULL);

INSERT INTO "BadgeCellRenderer"("method","signature","behavior","利用_Badge","利用_UiTheme") VALUES('getTableCellRendererComponent','Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)','セル値を前後の空白を除いた文字列にし、「-」なら空として扱う。生の値から色を、表示名変換関数から表示文字を決めて保持し、太字の一回り小さいフォントとツールチップを設定して自身を返す。',NULL,NULL);
INSERT INTO "BadgeCellRenderer"("method","signature","behavior","利用_Badge","利用_UiTheme") VALUES('paintComponent','void paintComponent(Graphics g)','通常の背景を描いたあと、保持した文字と色でピルを1個描く。中央寄せ指定なら中央、そうでなければ左端から6ピクセルの位置に描く。表示文字が空なら薄い色の「-」を描く。',NULL,NULL);

INSERT INTO "BatchPoster"("method","signature","behavior","利用_ScreenshotEntry") VALUES('Result','record Result(List<ScreenshotEntry> posted, String error)','送れたスクショと、失敗したときの内容(全部送れたら null)。succeeded で成否を返す。',NULL);
INSERT INTO "BatchPoster"("method","signature","behavior","利用_ScreenshotEntry") VALUES('postInOrder','Result postInOrder(List<List<ScreenshotEntry>> batches, List<String> texts, Sender sender, IntConsumer onBatchStart)','i 番目のバッチを i 番目の本文で順に送る。送る前に1始まりのバッチ番号を onBatchStart に知らせる。失敗したらそこで止め、それまでに送れた分と失敗内容を返す。',NULL);

INSERT INTO "BeatorajaPaths"("method","signature","behavior","利用_AppLogging") VALUES('beatorajaDirectory','Path beatorajaDirectory()','元になったbeatorajaフォルダのパスを返す。',NULL);
INSERT INTO "BeatorajaPaths"("method","signature","behavior","利用_AppLogging") VALUES('listPlayerNames','List<String> listPlayerNames(Path beatorajaDirectory)','beatorajaフォルダの config_sys.json からプレイヤーフォルダの場所を読み(読めなければ player を使う)、その下のフォルダ名を名前順で返す。フォルダが null や存在しない場合は空の一覧を返す。',NULL);
INSERT INTO "BeatorajaPaths"("method","signature","behavior","利用_AppLogging") VALUES('playerDirectory','Path playerDirectory()','使用するプレイヤーのフォルダのパスを返す。',NULL);
INSERT INTO "BeatorajaPaths"("method","signature","behavior","利用_AppLogging") VALUES('playerName','String playerName()','使用するプレイヤー名を返す。',NULL);
INSERT INTO "BeatorajaPaths"("method","signature","behavior","利用_AppLogging") VALUES('resolve','BeatorajaPaths resolve(Path beatorajaDirectory, String playerNameOverride)','beatorajaフォルダの config_sys.json を読み、プレイヤー名(指定がなければ設定の playername、それもなければ player1)と各DB・表フォルダのパスを割り出して返す。config_sys.json がなければ IOException を投げる。',NULL);
INSERT INTO "BeatorajaPaths"("method","signature","behavior","利用_AppLogging") VALUES('scoreDataLogDb','Path scoreDataLogDb()','プレイヤーフォルダの scoredatalog.db(1プレイごとの記録)のパスを返す。',NULL);
INSERT INTO "BeatorajaPaths"("method","signature","behavior","利用_AppLogging") VALUES('scoreLogDb','Path scoreLogDb()','プレイヤーフォルダの scorelog.db のパスを返す。',NULL);
INSERT INTO "BeatorajaPaths"("method","signature","behavior","利用_AppLogging") VALUES('songDatabase','Path songDatabase()','楽曲DB(songdata.db)のパスを返す。',NULL);
INSERT INTO "BeatorajaPaths"("method","signature","behavior","利用_AppLogging") VALUES('tableDirectory','Path tableDirectory()','難易度表(.bmt)フォルダのパスを返す。',NULL);

INSERT INTO "BmtTableReader"("method","signature","behavior") VALUES('ReadResult','record ReadResult(List<BmtTableData> tables, List<String> failedFiles)','まとめ読みの結果。読めた表データの一覧と、読めなかったファイル名の一覧を持つ。');
INSERT INTO "BmtTableReader"("method","signature","behavior") VALUES('read','BmtTableData read(Path path)','.bmt ファイル1つを読み、表データに変換して返す。拡張子が .bmt ならgzipを展開してからJSONとして読み、それ以外はそのままJSONとして読む。');
INSERT INTO "BmtTableReader"("method","signature","behavior") VALUES('readAll','List<BmtTableData> readAll(Path tablePath)','フォルダ内の全 .bmt を読み、読めた表データだけを返す。');
INSERT INTO "BmtTableReader"("method","signature","behavior") VALUES('readAllWithDiagnostics','ReadResult readAllWithDiagnostics(Path tablePath)','フォルダ内の拡張子 .bmt のファイルをすべて読み、読めた表データと、例外や空結果で読めなかったファイル名を返す。フォルダが null か存在しなければ両方空の結果を返す。');

INSERT INTO "BmzPaths"("method","signature","behavior","利用_AppLogging") VALUES('dataDirectory','Path dataDirectory()','BMZ Player のデータフォルダのパスを返す。',NULL);
INSERT INTO "BmzPaths"("method","signature","behavior","利用_AppLogging") VALUES('findDataDirectory','Path findDataDirectory(Path directory)','指定フォルダに config.toml があればそのフォルダを、なければその下の data フォルダに config.toml があれば data フォルダを返す。どちらもなければ null を返す。',NULL);
INSERT INTO "BmzPaths"("method","signature","behavior","利用_AppLogging") VALUES('libraryDb','Path libraryDb()','楽曲と難易度表を収めた library.db のパスを返す。',NULL);
INSERT INTO "BmzPaths"("method","signature","behavior","利用_AppLogging") VALUES('listProfileIds','List<String> listProfileIds(Path directory)','データフォルダの profiles 配下にあるフォルダ名(プロフィールID)を名前順で返す。データフォルダや profiles がなければ空の一覧を返す。',NULL);
INSERT INTO "BmzPaths"("method","signature","behavior","利用_AppLogging") VALUES('profileDirectory','Path profileDirectory()','使用するプロフィールのフォルダ(profiles/<ID>)のパスを返す。',NULL);
INSERT INTO "BmzPaths"("method","signature","behavior","利用_AppLogging") VALUES('profileId','String profileId()','使用するプロフィールIDを返す。',NULL);
INSERT INTO "BmzPaths"("method","signature","behavior","利用_AppLogging") VALUES('resolve','BmzPaths resolve(Path directory, String profileOverride)','データフォルダを探して config.toml を読み、プロフィールID(指定がなければ active_profile、それも空なら default)とスクショ保存先から各パスを割り出して返す。データフォルダが見つからなければ IOException を投げる。',NULL);
INSERT INTO "BmzPaths"("method","signature","behavior","利用_AppLogging") VALUES('resolveScreenshotDirectory','Path resolveScreenshotDirectory(Path dataDirectory, String configured)','BMZ Player と同じ規則でスクショ保存先を決める。空なら screenshots とし、絶対パスはそのまま、相対パスはデータフォルダ基準にし、先頭の data/ は古い書き方として取り除く。',NULL);
INSERT INTO "BmzPaths"("method","signature","behavior","利用_AppLogging") VALUES('scoreDb','Path scoreDb()','プロフィールのプレイ履歴DB(score.db)のパスを返す。',NULL);
INSERT INTO "BmzPaths"("method","signature","behavior","利用_AppLogging") VALUES('screenshotDirectory','Path screenshotDirectory()','BMZ Player のスクショ保存先のパスを返す。',NULL);

INSERT INTO "BmzPlayLogService"("method","signature","behavior","利用_ClearTypeMapper","生成_PlayHistory","生成_PlayLogEntry","継承_PlayLogSource","生成_PlayScore") VALUES('close','void close()','メモリ上のプレイ履歴を破棄する。',NULL,NULL,NULL,NULL,NULL);
INSERT INTO "BmzPlayLogService"("method","signature","behavior","利用_ClearTypeMapper","生成_PlayHistory","生成_PlayLogEntry","継承_PlayLogSource","生成_PlayScore") VALUES('findNear','List<PlayLogEntry> findNear(LocalDateTime capturedAt, Integer expectedClearId)','撮影時刻の10分前から5秒後までに終わったプレイのうち最も新しい1件を返す。該当がない、または期待するランプIDが指定されていてそのプレイのランプと違う場合は空の一覧を返す(オートプレイやコース結果などとみなす)。',NULL,NULL,NULL,NULL,NULL);
INSERT INTO "BmzPlayLogService"("method","signature","behavior","利用_ClearTypeMapper","生成_PlayHistory","生成_PlayLogEntry","継承_PlayLogSource","生成_PlayScore") VALUES('isAvailable','boolean isAvailable()','score.db を読み込めたかを返す。',NULL,NULL,NULL,NULL,NULL);

INSERT INTO "BmzTableReader"("method","signature","behavior","生成_BmtTableReader") VALUES('readAll','List<BmtTableReader.BmtTableData> readAll(Path libraryDb)','library.db の難易度表と表の曲を読み、表データの一覧にして返す。フォルダは表のレベル順(level_order)で並べ、曲のないフォルダは除く。ファイルや必要な表がなければ空の一覧を返す。',NULL);

INSERT INTO "CapturedAtCellRenderer"("method","signature","behavior","利用_UiTheme") VALUES('getTableCellRendererComponent','Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)','セル値が日時なら「年/月/日」と「時:分」の文字列に分けて保持し、両方をつなげたものをツールチップにする。日時でなければ両方を空にし、ツールチップも消す。',NULL);
INSERT INTO "CapturedAtCellRenderer"("method","signature","behavior","利用_UiTheme") VALUES('paintComponent','void paintComponent(Graphics g)','通常の背景を描いたあと、左端から日付を通常の文字色で、続けて時刻を背景に寄せた薄い色で描く。日付が空なら何も描かない。',NULL);

INSERT INTO "CardPanel"("method","signature","behavior","利用_UiTheme") VALUES('getBackground','java.awt.Color getBackground()','設定値にかかわらず常に現在テーマの面色を返す。中に置いたピルなどが背景色として参照するためである。',NULL);
INSERT INTO "CardPanel"("method","signature","behavior","利用_UiTheme") VALUES('paintComponent','void paintComponent(Graphics g)','パネル全体を面色の角丸四角（角の丸み10）で塗り、細い枠線色で縁取ってから、通常の描画処理を呼ぶ。',NULL);

INSERT INTO "ChartResolverService"("method","signature","behavior","利用_AppConfig","利用_AppLogging","利用_BeatorajaPaths","利用_BmzPaths","生成_BmzPlayLogService","利用_BmzTableReader","利用_ClearTypeMapper","生成_DifficultyTableRegistry","生成_EnrichedScreenshot","利用_PlayLogEntry","利用_PlayLogSource","利用_PlayScore","利用_PlayerKind","生成_PlayerPlayLogService","生成_ScreenshotEntry","生成_SongDatabaseService","生成_TableLevelParser") VALUES('close','void close()','開いているプレイ履歴と曲データベースを閉じ、難易度表の登録簿を空にする。',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "ChartResolverService"("method","signature","behavior","利用_AppConfig","利用_AppLogging","利用_BeatorajaPaths","利用_BmzPaths","生成_BmzPlayLogService","利用_BmzTableReader","利用_ClearTypeMapper","生成_DifficultyTableRegistry","生成_EnrichedScreenshot","利用_PlayLogEntry","利用_PlayLogSource","利用_PlayScore","利用_PlayerKind","生成_PlayerPlayLogService","生成_ScreenshotEntry","生成_SongDatabaseService","生成_TableLevelParser") VALUES('enrich','EnrichedScreenshot enrich(ScreenshotEntry entry)','スクショ1件に譜面情報を付けて返す。プレイ結果を持ちうる画面なら、プレイ履歴から撮影時刻に近いプレイを探し、見つかればそのハッシュで難易度表の表記を引き(見つからなければ曲名・サブタイトル付き曲名でも探す)、スコアも添える。BMZ Player ならファイル名に無い曲名・ランプ・ランクをプレイ結果で埋めた情報に差し替える。プレイ画面や選曲画面のスクショ、履歴が使えないとき、検索中の例外時は、ファイル名のタイトルとフォルダ名から表記を推定する。',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "ChartResolverService"("method","signature","behavior","利用_AppConfig","利用_AppLogging","利用_BeatorajaPaths","利用_BmzPaths","生成_BmzPlayLogService","利用_BmzTableReader","利用_ClearTypeMapper","生成_DifficultyTableRegistry","生成_EnrichedScreenshot","利用_PlayLogEntry","利用_PlayLogSource","利用_PlayScore","利用_PlayerKind","生成_PlayerPlayLogService","生成_ScreenshotEntry","生成_SongDatabaseService","生成_TableLevelParser") VALUES('getFailedTableFiles','List<String> getFailedTableFiles()','読み込みに失敗した難易度表ファイルの名前一覧を返す。',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "ChartResolverService"("method","signature","behavior","利用_AppConfig","利用_AppLogging","利用_BeatorajaPaths","利用_BmzPaths","生成_BmzPlayLogService","利用_BmzTableReader","利用_ClearTypeMapper","生成_DifficultyTableRegistry","生成_EnrichedScreenshot","利用_PlayLogEntry","利用_PlayLogSource","利用_PlayScore","利用_PlayerKind","生成_PlayerPlayLogService","生成_ScreenshotEntry","生成_SongDatabaseService","生成_TableLevelParser") VALUES('getKnownTables','List<DifficultyTableRegistry.TableInfo> getKnownTables()','読み込めた難易度表の一覧を返す。',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "ChartResolverService"("method","signature","behavior","利用_AppConfig","利用_AppLogging","利用_BeatorajaPaths","利用_BmzPaths","生成_BmzPlayLogService","利用_BmzTableReader","利用_ClearTypeMapper","生成_DifficultyTableRegistry","生成_EnrichedScreenshot","利用_PlayLogEntry","利用_PlayLogSource","利用_PlayScore","利用_PlayerKind","生成_PlayerPlayLogService","生成_ScreenshotEntry","生成_SongDatabaseService","生成_TableLevelParser") VALUES('getKnownTags','List<String> getKnownTags()','読み込んだ難易度表のタグの一覧を返す。',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "ChartResolverService"("method","signature","behavior","利用_AppConfig","利用_AppLogging","利用_BeatorajaPaths","利用_BmzPaths","生成_BmzPlayLogService","利用_BmzTableReader","利用_ClearTypeMapper","生成_DifficultyTableRegistry","生成_EnrichedScreenshot","利用_PlayLogEntry","利用_PlayLogSource","利用_PlayScore","利用_PlayerKind","生成_PlayerPlayLogService","生成_ScreenshotEntry","生成_SongDatabaseService","生成_TableLevelParser") VALUES('getLoadedFileCount','int getLoadedFileCount()','読み込めた難易度表ファイルの数を返す。',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "ChartResolverService"("method","signature","behavior","利用_AppConfig","利用_AppLogging","利用_BeatorajaPaths","利用_BmzPaths","生成_BmzPlayLogService","利用_BmzTableReader","利用_ClearTypeMapper","生成_DifficultyTableRegistry","生成_EnrichedScreenshot","利用_PlayLogEntry","利用_PlayLogSource","利用_PlayScore","利用_PlayerKind","生成_PlayerPlayLogService","生成_ScreenshotEntry","生成_SongDatabaseService","生成_TableLevelParser") VALUES('getNotationsForTagAndSymbol','List<String> getNotationsForTagAndSymbol(String tag, String symbol)','指定したタグと記号の組み合わせで使われる表記の一覧を返す。',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "ChartResolverService"("method","signature","behavior","利用_AppConfig","利用_AppLogging","利用_BeatorajaPaths","利用_BmzPaths","生成_BmzPlayLogService","利用_BmzTableReader","利用_ClearTypeMapper","生成_DifficultyTableRegistry","生成_EnrichedScreenshot","利用_PlayLogEntry","利用_PlayLogSource","利用_PlayScore","利用_PlayerKind","生成_PlayerPlayLogService","生成_ScreenshotEntry","生成_SongDatabaseService","生成_TableLevelParser") VALUES('getPlayerKind','PlayerKind getPlayerKind()','直近の再読み込みで設定から取ったプレイヤーの種類(beatoraja / BMZ Player / 不明)を返す。',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "ChartResolverService"("method","signature","behavior","利用_AppConfig","利用_AppLogging","利用_BeatorajaPaths","利用_BmzPaths","生成_BmzPlayLogService","利用_BmzTableReader","利用_ClearTypeMapper","生成_DifficultyTableRegistry","生成_EnrichedScreenshot","利用_PlayLogEntry","利用_PlayLogSource","利用_PlayScore","利用_PlayerKind","生成_PlayerPlayLogService","生成_ScreenshotEntry","生成_SongDatabaseService","生成_TableLevelParser") VALUES('getSymbolsForTag','List<String> getSymbolsForTag(String tag)','指定したタグの難易度表で使われる記号の一覧を返す。',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "ChartResolverService"("method","signature","behavior","利用_AppConfig","利用_AppLogging","利用_BeatorajaPaths","利用_BmzPaths","生成_BmzPlayLogService","利用_BmzTableReader","利用_ClearTypeMapper","生成_DifficultyTableRegistry","生成_EnrichedScreenshot","利用_PlayLogEntry","利用_PlayLogSource","利用_PlayScore","利用_PlayerKind","生成_PlayerPlayLogService","生成_ScreenshotEntry","生成_SongDatabaseService","生成_TableLevelParser") VALUES('getSymbolsInPriorityOrder','List<String> getSymbolsInPriorityOrder()','難易度表の優先順に並べた記号の一覧を返す。',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "ChartResolverService"("method","signature","behavior","利用_AppConfig","利用_AppLogging","利用_BeatorajaPaths","利用_BmzPaths","生成_BmzPlayLogService","利用_BmzTableReader","利用_ClearTypeMapper","生成_DifficultyTableRegistry","生成_EnrichedScreenshot","利用_PlayLogEntry","利用_PlayLogSource","利用_PlayScore","利用_PlayerKind","生成_PlayerPlayLogService","生成_ScreenshotEntry","生成_SongDatabaseService","生成_TableLevelParser") VALUES('getTablePrefix','String getTablePrefix(String tableKey)','指定した難易度表キーに対応する表記の頭の文字列を返す。',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "ChartResolverService"("method","signature","behavior","利用_AppConfig","利用_AppLogging","利用_BeatorajaPaths","利用_BmzPaths","生成_BmzPlayLogService","利用_BmzTableReader","利用_ClearTypeMapper","生成_DifficultyTableRegistry","生成_EnrichedScreenshot","利用_PlayLogEntry","利用_PlayLogSource","利用_PlayScore","利用_PlayerKind","生成_PlayerPlayLogService","生成_ScreenshotEntry","生成_SongDatabaseService","生成_TableLevelParser") VALUES('reload','void reload(AppConfig config)','設定を受け取り、開いていた接続を閉じてから読み込み直す。プレイヤーフォルダが無ければ難易度表を空にして終わる。BMZ Player なら library.db から難易度表と曲情報、スコア DB からプレイ履歴を開き、beatoraja なら難易度表フォルダと songdata・scorelog の DB を開く。どちらも表の優先順と表記ルールを設定から反映し、DB を開けないときは日本語メッセージ付きの IOException を投げる。',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "ChartResolverService"("method","signature","behavior","利用_AppConfig","利用_AppLogging","利用_BeatorajaPaths","利用_BmzPaths","生成_BmzPlayLogService","利用_BmzTableReader","利用_ClearTypeMapper","生成_DifficultyTableRegistry","生成_EnrichedScreenshot","利用_PlayLogEntry","利用_PlayLogSource","利用_PlayScore","利用_PlayerKind","生成_PlayerPlayLogService","生成_ScreenshotEntry","生成_SongDatabaseService","生成_TableLevelParser") VALUES('reloadPlayData','void reloadPlayData()','前回の reload で開いたプレイ履歴と曲データベースだけを開き直す(難易度表は読み直さない)。新しいスクショのプレイや追加された曲を拾うために、スクショ1枚ごとに呼ばれる。開けなければ IOException を投げる。',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);

INSERT INTO "ChromiumBrowserLocator"("method","signature","behavior") VALUES('locate','Optional<Path> locate()','Chrome（Program Files、Program Files (x86)、LOCALAPPDATA の順）、続いて Edge の決まった場所を調べ、最初に実在した実行ファイルの場所を返す。どれも無ければ空を返す。');

INSERT INTO "ChromiumCdpSession"("method","signature","behavior","利用_AppLogging") VALUES('close','void close()','応答待ちの命令をすべて「セッションを終了しました」の例外で打ち切り、以後の命令も即座に失敗させる。WebSocket がまだ開いていれば正常終了の合図を送る。',NULL);
INSERT INTO "ChromiumCdpSession"("method","signature","behavior","利用_AppLogging") VALUES('connect','ChromiumCdpSession connect(HttpClient httpClient, String webSocketUrl, Duration timeout)','指定の WebSocket URL へ接続したセッションを作って返す。接続が指定時間(ミリ秒単位で扱う)内に開かなければ例外を投げる。受信した応答は番号で待ち手に届け、エラー応答は例外として渡す。ブラウザ側が接続を閉じたりエラーになったりしたら、待っている命令をその場で打ち切る。',NULL);
INSERT INTO "ChromiumCdpSession"("method","signature","behavior","利用_AppLogging") VALUES('send','JsonNode send(String method, Map<String, Object> params, Duration timeout)','CDP の命令名と引数に番号を付けて送り、同じ番号の応答の result 部分を返す。接続が既に切れていればすぐ例外。エラー応答なら例外、指定時間(ミリ秒単位)内に応答が無ければ待ち表から外して「タイムアウトしました」の例外を投げる。',NULL);

INSERT INTO "ChromiumDevToolsClient"("method","signature","behavior","利用_ChromiumCdpSession","生成_TwitterCookies") VALUES('close','void close()','何もしない。try-with-resources で使えるように用意されているだけで、閉じるべき資源は持っていない。',NULL,NULL);
INSERT INTO "ChromiumDevToolsClient"("method","signature","behavior","利用_ChromiumCdpSession","生成_TwitterCookies") VALUES('openSession','ChromiumCdpSession openSession(String initialUrl)','ブラウザで開いているページを1つ選び（無ければ指定 URL で新しいタブを開き）、そのページにつないだ CDP セッションを返す。既存ページがあれば指定 URL は使われない。',NULL,NULL);
INSERT INTO "ChromiumDevToolsClient"("method","signature","behavior","利用_ChromiumCdpSession","生成_TwitterCookies") VALUES('readTwitterCookies','TwitterCookies readTwitterCookies()','ページにつないで https://x.com/home へ移動し、2.5秒待ってから x.com と twitter.com の Cookie を取得して TwitterCookies にまとめて返す。x.com・twitter.com とその下位ドメインの Cookie だけを残し、auth_token と ct0 がどちらも無ければ「ログイン状態が保存されていない可能性」の例外を投げる。',NULL,NULL);
INSERT INTO "ChromiumDevToolsClient"("method","signature","behavior","利用_ChromiumCdpSession","生成_TwitterCookies") VALUES('waitForDebugPort','int waitForDebugPort(Path userDataDir, Duration timeout)','プロファイルフォルダの DevToolsActivePort ファイルを0.15秒おきに見て、1行目のポート番号の /json/version に応答があればその番号を返す。指定時間内に得られなければ、最後の失敗理由を添えた例外を投げる。',NULL,NULL);

INSERT INTO "ChromiumLauncher"("method","signature","behavior") VALUES('command','List<String> command(Path browser, Path profileDir, boolean visibleToUser)','ブラウザ・プロファイルフォルダと、初回画面・既定ブラウザ確認・同期・新機能案内と翻訳の案内を止める共通の引数を並べる。利用者が操作する画面なら自動操作の印を隠す引数も足す。');
INSERT INTO "ChromiumLauncher"("method","signature","behavior") VALUES('launch','Process launch(Path browser, Path profileDir, boolean visibleToUser, List<String> extraArgs)','共通の引数に追加の引数(デバッグポート・窓の位置や大きさ・開くページなど)を続けてブラウザを起動し、そのプロセスを返す。');

INSERT INTO "ChromiumProfileLock"("method","signature","behavior","利用_AppLogging") VALUES('clearDebugArtifacts','void clearDebugArtifacts(Path profileDir)','プロファイルフォルダから前回の DevToolsActivePort とその lock ファイルを消す。消せなくても記録を残すだけで続ける。',NULL);
INSERT INTO "ChromiumProfileLock"("method","signature","behavior","利用_AppLogging") VALUES('waitForProfileUnlock','void waitForProfileUnlock(Path profileDir)','プロファイルの SingletonLock と lockfile がどちらも無い状態が0.4秒続くまで待つ。25秒たっても解けなければ「Chrome / Edge のウィンドウが残っていないか確認してください」の例外を投げる。',NULL);

INSERT INTO "ClearLamp"("method","signature","behavior") VALUES('bmzLabel','String bmzLabel()','BMZ Player の clear_type ラベル(例: Hard)を返す。');
INSERT INTO "ClearLamp"("method","signature","behavior") VALUES('colorRgb','int colorRgb()','バッジに使う色を RGB の整数で返す(ダークテーマ向けの値)。');
INSERT INTO "ClearLamp"("method","signature","behavior") VALUES('defaultShortName','String defaultShortName()','ランプ別まとめ投稿で使う既定の短い名前(HC、FC など。FAILED は空)を返す。');
INSERT INTO "ClearLamp"("method","signature","behavior") VALUES('displayName','String displayName()','beatoraja がスクショ名に書くランプ名(例: HARD CLEAR)を返す。');
INSERT INTO "ClearLamp"("method","signature","behavior") VALUES('findIn','Optional<ClearLamp> findIn(String text)','文字列に含まれる最初のランプ名を探して返す。名前の長い順に試すので、EXHARD CLEAR が HARD CLEAR や CLEAR と取り違えられない。');
INSERT INTO "ClearLamp"("method","signature","behavior") VALUES('fromBmzLabel','Optional<ClearLamp> fromBmzLabel(String label)','BMZ Player のラベル(前後空白は無視、大文字小文字は区別)に当たるランプを返す。null や不明なラベルなら空。');
INSERT INTO "ClearLamp"("method","signature","behavior") VALUES('fromDisplayName','Optional<ClearLamp> fromDisplayName(String name)','ランプ名(大文字小文字・前後空白は無視)に当たるランプを返す。null や不明な名前なら空。');
INSERT INTO "ClearLamp"("method","signature","behavior") VALUES('fromId','Optional<ClearLamp> fromId(int id)','クリアIDに当たるランプを返す。範囲外なら空。');
INSERT INTO "ClearLamp"("method","signature","behavior") VALUES('id','int id()','beatoraja のクリアID(0=NO PLAY 〜 10=MAX)を返す。列挙の並び順そのもの。');
INSERT INTO "ClearLamp"("method","signature","behavior") VALUES('shownInGroupedPostByDefault','boolean shownInGroupedPostByDefault()','ランプ別まとめ投稿に既定で載せるかを返す。アシスト系・FAILED・NO PLAY は false。');

INSERT INTO "ClearLampLabelTableModel"("method","signature","behavior","利用_ClearLampLabels") VALUES('getColumnClass','Class<?> getColumnClass(int columnIndex)','「まとめに載せる」列はチェックボックスで出すため Boolean、それ以外は文字列とする。',NULL);
INSERT INTO "ClearLampLabelTableModel"("method","signature","behavior","利用_ClearLampLabels") VALUES('getColumnCount','int getColumnCount()','列の数(ランプ・表示名・略称・載せるか の4)を返す。',NULL);
INSERT INTO "ClearLampLabelTableModel"("method","signature","behavior","利用_ClearLampLabels") VALUES('getColumnName','String getColumnName(int column)','列の見出しを返す。',NULL);
INSERT INTO "ClearLampLabelTableModel"("method","signature","behavior","利用_ClearLampLabels") VALUES('getGroupedVisibility','Map<String, Boolean> getGroupedVisibility()','既定と違う「載せるか」の指定だけを、ランプの並び順で返す。',NULL);
INSERT INTO "ClearLampLabelTableModel"("method","signature","behavior","利用_ClearLampLabels") VALUES('getOverrides','Map<String, String> getOverrides()','空でない表示名の上書きだけを、ランプの並び順で返す。',NULL);
INSERT INTO "ClearLampLabelTableModel"("method","signature","behavior","利用_ClearLampLabels") VALUES('getRowCount','int getRowCount()','ランプの数(11)を返す。',NULL);
INSERT INTO "ClearLampLabelTableModel"("method","signature","behavior","利用_ClearLampLabels") VALUES('getShortOverrides','Map<String, String> getShortOverrides()','既定と違う略称の上書きだけを、ランプの並び順で返す。',NULL);
INSERT INTO "ClearLampLabelTableModel"("method","signature","behavior","利用_ClearLampLabels") VALUES('getValueAt','Object getValueAt(int rowIndex, int columnIndex)','ランプ名、表示名の上書き(無ければ空)、略称(上書きか既定)、載せるか(上書きか既定)を返す。',NULL);
INSERT INTO "ClearLampLabelTableModel"("method","signature","behavior","利用_ClearLampLabels") VALUES('isCellEditable','boolean isCellEditable(int rowIndex, int columnIndex)','ランプ名の列以外を編集可能とする。',NULL);
INSERT INTO "ClearLampLabelTableModel"("method","signature","behavior","利用_ClearLampLabels") VALUES('setGroupedVisibility','void setGroupedVisibility(Map<String, Boolean> groupedVisibility)','まとめ投稿に載せるかの上書き表を受け取り、表を描き直す。',NULL);
INSERT INTO "ClearLampLabelTableModel"("method","signature","behavior","利用_ClearLampLabels") VALUES('setOverrides','void setOverrides(Map<String, String> overrides)','表示名(俗称)の上書き表を受け取り、表を描き直す。null は空の表。',NULL);
INSERT INTO "ClearLampLabelTableModel"("method","signature","behavior","利用_ClearLampLabels") VALUES('setShortOverrides','void setShortOverrides(Map<String, String> shortOverrides)','まとめ投稿用の略称の上書き表を受け取り、表を描き直す。',NULL);
INSERT INTO "ClearLampLabelTableModel"("method","signature","behavior","利用_ClearLampLabels") VALUES('setValueAt','void setValueAt(Object aValue, int rowIndex, int columnIndex)','編集された値を上書き表に入れる。空欄や既定と同じ値なら上書きを外して既定に従わせる。',NULL);

INSERT INTO "ClearLampLabels"("method","signature","behavior","利用_ClearLamp") VALUES('defaultShortName','String defaultShortName(String canonicalClearType)','ランプ別まとめ投稿での既定の短い名前を ClearLamp から返す。不明なランプなら空文字。',NULL);
INSERT INTO "ClearLampLabels"("method","signature","behavior","利用_ClearLamp") VALUES('isShownInGroupedPost','boolean isShownInGroupedPost(String canonicalClearType, Map<String, Boolean> overrides)','ランプ別まとめ投稿でそのランプを載せるかを返す。上書き設定があればそれに従い、なければ既定(アシスト系・FAILED・NO PLAY は載せない)に従う。',NULL);
INSERT INTO "ClearLampLabels"("method","signature","behavior","利用_ClearLamp") VALUES('isShownInGroupedPostByDefault','boolean isShownInGroupedPostByDefault(String canonicalClearType)','そのランプを利用者の指定がないときにランプ別まとめ投稿へ載せるかを ClearLamp から返す。不明なランプは載せる。',NULL);
INSERT INTO "ClearLampLabels"("method","signature","behavior","利用_ClearLamp") VALUES('resolve','String resolve(String clearType, Map<String, String> overrides)','ランプ名を大文字化したキーで上書き表示名を探し、空でない設定があればそれを、なければ元のランプ名を返す。null は空文字にする。',NULL);
INSERT INTO "ClearLampLabels"("method","signature","behavior","利用_ClearLamp") VALUES('resolveShort','String resolveShort(String clearType, Map<String, String> overrides)','ランプ別まとめ投稿用の短い名前を返す。ユーザーの上書き、ClearLamp の既定の短い名前(HC、FC など)、元のランプ名の順に使い、空のランプ名には空文字を返す。',NULL);

INSERT INTO "ClearTypeMapper"("method","signature","behavior","利用_ClearLamp") VALUES('fromBmzLabel','Integer fromBmzLabel(String label)','BMZ Player の clear_type ラベル(Normal、ExHard など)をbeatoraja番号のクリアIDに変換する。null や不明なラベルなら null を返す。',NULL);
INSERT INTO "ClearTypeMapper"("method","signature","behavior","利用_ClearLamp") VALUES('toClearId','Integer toClearId(String clearTypeName)','beatorajaのランプ名(大文字小文字・前後空白を問わない)をクリアIDに変換する。空や不明な名前なら null を返す。',NULL);
INSERT INTO "ClearTypeMapper"("method","signature","behavior","利用_ClearLamp") VALUES('toClearName','String toClearName(int clearId)','クリアIDを、beatorajaがスクショ名に書くランプ名(例: 6→HARD CLEAR)に変換する。範囲外なら空文字を返す。',NULL);

INSERT INTO "ClixAuthSupport"("method","signature","behavior","利用_AppConfig","利用_AppPaths") VALUES('applyAuthEnvironment','void applyAuthEnvironment(AppConfig config, Map<String, String> env)','設定に X の Cookie が保存されていれば、clix-home の下の .config\clix\auth.json に auth_token・ct0・Cookie 一式を default アカウントとして書き、渡された環境変数表の USERPROFILE を clix-home に書き換える。Cookie が無ければ何もしない。',NULL,NULL);

INSERT INTO "ClixLocator"("method","signature","behavior","利用_AppLogging","利用_AppPaths") VALUES('expectedBundledPath','Path expectedBundledPath()','同梱 clix.exe があるはずの場所を絶対パスで返す。見つからないときの案内文に使う。',NULL,NULL);
INSERT INTO "ClixLocator"("method","signature","behavior","利用_AppLogging","利用_AppPaths") VALUES('locate','Optional<Path> locate()','clix の実行ファイルを、同梱場所・作業フォルダの tools・最大6階層上までのプロジェクト直下の tools・Python の Scripts の順で探し、無ければ where コマンドで clix.exe、続いて clix を探して最初に見つかったものを返す。where は8秒で打ち切り、何も無ければ空を返す。',NULL,NULL);

INSERT INTO "ClixService"("method","signature","behavior","利用_AppConfig","利用_AppLogging","利用_ClixAuthSupport","利用_ClixLocator","生成_TwitterBrowserPostService") VALUES('AuthResult','record AuthResult(boolean success, String message)','ログイン確認の結果を表す値。成否と利用者向けメッセージを持ち、成功用と失敗用の作成メソッドがある。',NULL,NULL,NULL,NULL,NULL);
INSERT INTO "ClixService"("method","signature","behavior","利用_AppConfig","利用_AppLogging","利用_ClixAuthSupport","利用_ClixLocator","生成_TwitterBrowserPostService") VALUES('PostResult','record PostResult(boolean success, String tweetId, String message)','X 投稿の結果を表す値。成否・投稿 ID・メッセージを持ち、失敗時の投稿 ID は空文字になる。',NULL,NULL,NULL,NULL,NULL);
INSERT INTO "ClixService"("method","signature","behavior","利用_AppConfig","利用_AppLogging","利用_ClixAuthSupport","利用_ClixLocator","生成_TwitterBrowserPostService") VALUES('checkAuth','AuthResult checkAuth()','X のログイン状態を確かめる。clix が見つからなければ導入方法の案内、手動ログインの記録が無ければ未ログインの案内を失敗として返す。どちらも揃っていれば clix の auth status を実行し、終了コード0で JSON の authenticated が真なら成功、そうでなければ出力や終了コードから作った失敗メッセージを返す。',NULL,NULL,NULL,NULL,NULL);
INSERT INTO "ClixService"("method","signature","behavior","利用_AppConfig","利用_AppLogging","利用_ClixAuthSupport","利用_ClixLocator","生成_TwitterBrowserPostService") VALUES('normalizeLineEndings','String normalizeLineEndings(String text)','改行コードの CRLF と CR をすべて LF にそろえる。null は空文字にする。',NULL,NULL,NULL,NULL,NULL);
INSERT INTO "ClixService"("method","signature","behavior","利用_AppConfig","利用_AppLogging","利用_ClixAuthSupport","利用_ClixLocator","生成_TwitterBrowserPostService") VALUES('post','PostResult post(String text, List<Path> imagePaths)','本文の改行をそろえたうえで、画像が0枚なら、または20枚を超えるなら失敗を返す。条件を満たせば、ログイン済みのブラウザで X の投稿画面を開いて本文と画像を入れるクラスに投稿を任せ、その結果を返す。',NULL,NULL,NULL,NULL,NULL);

INSERT INTO "DatabasePanel"("method","signature","behavior","利用_AppConfig","利用_Badge","生成_BadgeCellRenderer","生成_CapturedAtCellRenderer","利用_ClearLampLabels","生成_MultiColumnSortSupport","利用_PostedStateStore","利用_ResultPalette","利用_ScreenshotDatabase","生成_ScreenshotEntry","生成_ScreenshotFileRemover","利用_ScreenshotRecord","生成_StateCell","生成_StateCellRenderer","生成_ThumbnailCache","生成_ThumbnailCellRenderer","利用_UiTheme") VALUES('getSelectedCount','int getSelectedCount()','表で選択されている行数を返す。',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "DatabasePanel"("method","signature","behavior","利用_AppConfig","利用_Badge","生成_BadgeCellRenderer","生成_CapturedAtCellRenderer","利用_ClearLampLabels","生成_MultiColumnSortSupport","利用_PostedStateStore","利用_ResultPalette","利用_ScreenshotDatabase","生成_ScreenshotEntry","生成_ScreenshotFileRemover","利用_ScreenshotRecord","生成_StateCell","生成_StateCellRenderer","生成_ThumbnailCache","生成_ThumbnailCellRenderer","利用_UiTheme") VALUES('getSelectedEntries','List<ScreenshotEntry> getSelectedEntries()','選択中の行を表示順（並べ替え・絞り込み後の順）で取り出し、それぞれのレコードを ScreenshotEntry に変換した一覧を返す。何も選ばれていなければ空の一覧になる。',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "DatabasePanel"("method","signature","behavior","利用_AppConfig","利用_Badge","生成_BadgeCellRenderer","生成_CapturedAtCellRenderer","利用_ClearLampLabels","生成_MultiColumnSortSupport","利用_PostedStateStore","利用_ResultPalette","利用_ScreenshotDatabase","生成_ScreenshotEntry","生成_ScreenshotFileRemover","利用_ScreenshotRecord","生成_StateCell","生成_StateCellRenderer","生成_ThumbnailCache","生成_ThumbnailCellRenderer","利用_UiTheme") VALUES('reload','void reload(ScreenshotDatabase database)','渡された DB を以後の編集・削除先として覚え、全レコードを読み直して表を作り直す。あわせて記号の絞り込み候補を更新し、絞り込みを掛け直す。DB の読み込みに失敗すると SQLException をそのまま投げる。',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "DatabasePanel"("method","signature","behavior","利用_AppConfig","利用_Badge","生成_BadgeCellRenderer","生成_CapturedAtCellRenderer","利用_ClearLampLabels","生成_MultiColumnSortSupport","利用_PostedStateStore","利用_ResultPalette","利用_ScreenshotDatabase","生成_ScreenshotEntry","生成_ScreenshotFileRemover","利用_ScreenshotRecord","生成_StateCell","生成_StateCellRenderer","生成_ThumbnailCache","生成_ThumbnailCellRenderer","利用_UiTheme") VALUES('selectAllFlagged','void selectAllFlagged()','選択を解除してから、いま表示されている行のうちフラグ付きの行をすべて選択し、選択変更を通知する。1件もなければ「フラグ付きのスクショがありません」と案内を出す。',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "DatabasePanel"("method","signature","behavior","利用_AppConfig","利用_Badge","生成_BadgeCellRenderer","生成_CapturedAtCellRenderer","利用_ClearLampLabels","生成_MultiColumnSortSupport","利用_PostedStateStore","利用_ResultPalette","利用_ScreenshotDatabase","生成_ScreenshotEntry","生成_ScreenshotFileRemover","利用_ScreenshotRecord","生成_StateCell","生成_StateCellRenderer","生成_ThumbnailCache","生成_ThumbnailCellRenderer","利用_UiTheme") VALUES('setClearLampLabels','void setClearLampLabels(Map<String, String> clearLampLabels)','ランプ列に表示するクリアランプの俗称（ランプ名から表示名への対応）を差し替えて表を再描画する。null は空の対応として扱う。',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "DatabasePanel"("method","signature","behavior","利用_AppConfig","利用_Badge","生成_BadgeCellRenderer","生成_CapturedAtCellRenderer","利用_ClearLampLabels","生成_MultiColumnSortSupport","利用_PostedStateStore","利用_ResultPalette","利用_ScreenshotDatabase","生成_ScreenshotEntry","生成_ScreenshotFileRemover","利用_ScreenshotRecord","生成_StateCell","生成_StateCellRenderer","生成_ThumbnailCache","生成_ThumbnailCellRenderer","利用_UiTheme") VALUES('setNotationChangeListener','void setNotationChangeListener(NotationChangeListener listener)','フラグ・記号・投稿表記のセルを編集して DB に保存したあとに呼ぶ通知先を登録する。',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "DatabasePanel"("method","signature","behavior","利用_AppConfig","利用_Badge","生成_BadgeCellRenderer","生成_CapturedAtCellRenderer","利用_ClearLampLabels","生成_MultiColumnSortSupport","利用_PostedStateStore","利用_ResultPalette","利用_ScreenshotDatabase","生成_ScreenshotEntry","生成_ScreenshotFileRemover","利用_ScreenshotRecord","生成_StateCell","生成_StateCellRenderer","生成_ThumbnailCache","生成_ThumbnailCellRenderer","利用_UiTheme") VALUES('setPostedStateStore','void setPostedStateStore(PostedStateStore postedStateStore)','状態列の X / Discord 投稿済み表示に使う投稿済み記録を差し替えて表を再描画する。投稿後に最新の状態を反映させるために呼ばれる。',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "DatabasePanel"("method","signature","behavior","利用_AppConfig","利用_Badge","生成_BadgeCellRenderer","生成_CapturedAtCellRenderer","利用_ClearLampLabels","生成_MultiColumnSortSupport","利用_PostedStateStore","利用_ResultPalette","利用_ScreenshotDatabase","生成_ScreenshotEntry","生成_ScreenshotFileRemover","利用_ScreenshotRecord","生成_StateCell","生成_StateCellRenderer","生成_ThumbnailCache","生成_ThumbnailCellRenderer","利用_UiTheme") VALUES('setSelectionListener','void setSelectionListener(SelectionListener listener)','行の選択が確定したとき（ドラッグ中は除く）、フラグ付き一括選択時、削除後に、選択中のスクショ一覧を受け取る通知先を登録する。',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "DatabasePanel"("method","signature","behavior","利用_AppConfig","利用_Badge","生成_BadgeCellRenderer","生成_CapturedAtCellRenderer","利用_ClearLampLabels","生成_MultiColumnSortSupport","利用_PostedStateStore","利用_ResultPalette","利用_ScreenshotDatabase","生成_ScreenshotEntry","生成_ScreenshotFileRemover","利用_ScreenshotRecord","生成_StateCell","生成_StateCellRenderer","生成_ThumbnailCache","生成_ThumbnailCellRenderer","利用_UiTheme") VALUES('setSymbolPriorityOrder','void setSymbolPriorityOrder(List<String> symbolPriorityOrder)','難易度表の優先順位から作った記号の並び順を受け取り、記号列の並べ替えをその順に従ってやり直し、記号の絞り込み候補もその順に並べ直す。順位にない記号は後ろ、記号なしは最後になる。',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "DatabasePanel"("method","signature","behavior","利用_AppConfig","利用_Badge","生成_BadgeCellRenderer","生成_CapturedAtCellRenderer","利用_ClearLampLabels","生成_MultiColumnSortSupport","利用_PostedStateStore","利用_ResultPalette","利用_ScreenshotDatabase","生成_ScreenshotEntry","生成_ScreenshotFileRemover","利用_ScreenshotRecord","生成_StateCell","生成_StateCellRenderer","生成_ThumbnailCache","生成_ThumbnailCellRenderer","利用_UiTheme") VALUES('shutdown','void shutdown()','サムネイルを裏で読み込むキャッシュの処理スレッドを止める。ウィンドウを閉じるときに呼ばれる。',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "DatabasePanel"("method","signature","behavior","利用_AppConfig","利用_Badge","生成_BadgeCellRenderer","生成_CapturedAtCellRenderer","利用_ClearLampLabels","生成_MultiColumnSortSupport","利用_PostedStateStore","利用_ResultPalette","利用_ScreenshotDatabase","生成_ScreenshotEntry","生成_ScreenshotFileRemover","利用_ScreenshotRecord","生成_StateCell","生成_StateCellRenderer","生成_ThumbnailCache","生成_ThumbnailCellRenderer","利用_UiTheme") VALUES('toEntry','ScreenshotEntry toEntry(ScreenshotRecord record)','DB のレコードから、ファイルパス・ファイル名・撮影日時・リザルト種別・曲名・記号・レベル・ランプ・ランクを写した ScreenshotEntry を作って返す。null を渡すと null を返す。投稿表記やフラグは引き継がない。',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);

INSERT INTO "DifficultyTableRegistry"("method","signature","behavior","利用_AppConfig","生成_BmtTableReader","利用_NotationPrefixResolver","利用_TableLevelParser") VALUES('NotationResolution','record NotationResolution(String primary, String defaultPostNotation, List<String> candidates)','表記の解決結果。第一候補の表記、投稿用の既定表記(第一候補に常に併記する表の表記を / でつないだもの)、全候補の一覧を持つ。',NULL,NULL,NULL,NULL);
INSERT INTO "DifficultyTableRegistry"("method","signature","behavior","利用_AppConfig","生成_BmtTableReader","利用_NotationPrefixResolver","利用_TableLevelParser") VALUES('TableInfo','record TableInfo(String tag, String name)','読み込んだ表の識別キー(表名)と表示名の組。',NULL,NULL,NULL,NULL);
INSERT INTO "DifficultyTableRegistry"("method","signature","behavior","利用_AppConfig","生成_BmtTableReader","利用_NotationPrefixResolver","利用_TableLevelParser") VALUES('TableMatch','record TableMatch(String tableName, String tableTag, String symbol, String level, String notation, String folderName, String md5, String sha256)','ある曲がある表のどのフォルダに載っているかの1件。表名・表キー・記号・レベル・表記・フォルダ名・md5・sha256を持つ。',NULL,NULL,NULL,NULL);
INSERT INTO "DifficultyTableRegistry"("method","signature","behavior","利用_AppConfig","生成_BmtTableReader","利用_NotationPrefixResolver","利用_TableLevelParser") VALUES('TableNotationRule','record TableNotationRule(String tableTag, boolean alwaysInclude, Map<String, String> symbolOverrides)','照合処理用の表ごとの表記ルール。表キー・常に併記するか・記号の置き換え(空キーは表全体の置き換え)を持ち、置き換えが null なら空にする。',NULL,NULL,NULL,NULL);
INSERT INTO "DifficultyTableRegistry"("method","signature","behavior","利用_AppConfig","生成_BmtTableReader","利用_NotationPrefixResolver","利用_TableLevelParser") VALUES('clear','void clear()','読み込んだ表・タグ・索引・読み込み件数・失敗ファイル一覧をすべて消す。優先順と表記ルールは残す。',NULL,NULL,NULL,NULL);
INSERT INTO "DifficultyTableRegistry"("method","signature","behavior","利用_AppConfig","生成_BmtTableReader","利用_NotationPrefixResolver","利用_TableLevelParser") VALUES('findNotationsByHash','List<String> findNotationsByHash(String sha256, String md5)','sha256 または md5 で曲を探し、優先順と表記ルールを反映した候補表記の一覧を返す。',NULL,NULL,NULL,NULL);
INSERT INTO "DifficultyTableRegistry"("method","signature","behavior","利用_AppConfig","生成_BmtTableReader","利用_NotationPrefixResolver","利用_TableLevelParser") VALUES('findNotationsForTitle','List<String> findNotationsForTitle(String title)','曲名(空白を詰めて小文字化したもの)の完全一致で曲を探し、候補表記の一覧を返す。曲名が空なら空の一覧を返す。',NULL,NULL,NULL,NULL);
INSERT INTO "DifficultyTableRegistry"("method","signature","behavior","利用_AppConfig","生成_BmtTableReader","利用_NotationPrefixResolver","利用_TableLevelParser") VALUES('getFailedTableFiles','List<String> getFailedTableFiles()','直前の読み込みで読めなかった .bmt のファイル名一覧を返す。',NULL,NULL,NULL,NULL);
INSERT INTO "DifficultyTableRegistry"("method","signature","behavior","利用_AppConfig","生成_BmtTableReader","利用_NotationPrefixResolver","利用_TableLevelParser") VALUES('getKnownTables','List<TableInfo> getKnownTables()','読み込んだ表を、重複を除いて表名と表示名の組の一覧で返す。',NULL,NULL,NULL,NULL);
INSERT INTO "DifficultyTableRegistry"("method","signature","behavior","利用_AppConfig","生成_BmtTableReader","利用_NotationPrefixResolver","利用_TableLevelParser") VALUES('getKnownTags','List<String> getKnownTags()','読み込んだ表のタグ(フォルダ名の先頭に付く記号)一覧のコピーを返す。',NULL,NULL,NULL,NULL);
INSERT INTO "DifficultyTableRegistry"("method","signature","behavior","利用_AppConfig","生成_BmtTableReader","利用_NotationPrefixResolver","利用_TableLevelParser") VALUES('getLoadedFileCount','int getLoadedFileCount()','直前の読み込みで読めた表データの数を返す。同名の表をまとめる前の数なので、表の数より大幅に多ければ古い .bmt が残っている目安になる。',NULL,NULL,NULL,NULL);
INSERT INTO "DifficultyTableRegistry"("method","signature","behavior","利用_AppConfig","生成_BmtTableReader","利用_NotationPrefixResolver","利用_TableLevelParser") VALUES('getNotationsForTagAndSymbol','List<String> getNotationsForTagAndSymbol(String tag, String symbol)','指定した表と記号に属する表記(例: A1〜A9)を、フォルダの出現順で返す。',NULL,NULL,NULL,NULL);
INSERT INTO "DifficultyTableRegistry"("method","signature","behavior","利用_AppConfig","生成_BmtTableReader","利用_NotationPrefixResolver","利用_TableLevelParser") VALUES('getSymbolsForTag','List<String> getSymbolsForTag(String tag)','指定した表に出てくる記号を、フォルダの出現順で重複なく返す。',NULL,NULL,NULL,NULL);
INSERT INTO "DifficultyTableRegistry"("method","signature","behavior","利用_AppConfig","生成_BmtTableReader","利用_NotationPrefixResolver","利用_TableLevelParser") VALUES('getSymbolsInPriorityOrder','List<String> getSymbolsInPriorityOrder()','全表の記号を、表の優先順(未設定の表はその後ろ)とフォルダの出現順で重複なく並べて返す。',NULL,NULL,NULL,NULL);
INSERT INTO "DifficultyTableRegistry"("method","signature","behavior","利用_AppConfig","生成_BmtTableReader","利用_NotationPrefixResolver","利用_TableLevelParser") VALUES('getTablePrefix','String getTablePrefix(String tableKey)','表の .bmt タグ(表記の先頭部分)を返す。なければ空文字を返す。',NULL,NULL,NULL,NULL);
INSERT INTO "DifficultyTableRegistry"("method","signature","behavior","利用_AppConfig","生成_BmtTableReader","利用_NotationPrefixResolver","利用_TableLevelParser") VALUES('loadFromBeatoraja','void loadFromBeatoraja(Path beatorajaDirectory)','現在の内容を消し、beatorajaフォルダの config_sys.json から表フォルダの場所を求めて(なければ table フォルダ)、その中の .bmt を読み込む。フォルダが存在しなければ空のままにする。',NULL,NULL,NULL,NULL);
INSERT INTO "DifficultyTableRegistry"("method","signature","behavior","利用_AppConfig","生成_BmtTableReader","利用_NotationPrefixResolver","利用_TableLevelParser") VALUES('loadFromTableDirectory','void loadFromTableDirectory(Path tablePath)','現在の内容を消し、指定した表フォルダの .bmt を読み込む。フォルダが存在しなければ空のままにする。',NULL,NULL,NULL,NULL);
INSERT INTO "DifficultyTableRegistry"("method","signature","behavior","利用_AppConfig","生成_BmtTableReader","利用_NotationPrefixResolver","利用_TableLevelParser") VALUES('loadFromTables','void loadFromTables(List<BmtTableReader.BmtTableData> tables)','現在の内容を消し、別の手段で読んだ表データ(BMZ Player の library.db など)を登録する。',NULL,NULL,NULL,NULL);
INSERT INTO "DifficultyTableRegistry"("method","signature","behavior","利用_AppConfig","生成_BmtTableReader","利用_NotationPrefixResolver","利用_TableLevelParser") VALUES('normalizeTitle','String normalizeTitle(String title)','曲名の前後の空白を除き、連続する空白を1つにまとめて小文字化した照合用キーを返す。',NULL,NULL,NULL,NULL);
INSERT INTO "DifficultyTableRegistry"("method","signature","behavior","利用_AppConfig","生成_BmtTableReader","利用_NotationPrefixResolver","利用_TableLevelParser") VALUES('parseFolderName','TableLevelParser.ParsedTableLevel parseFolderName(String folderName)','読み込んだ表のタグも使ってフォルダ名を記号とレベルに分ける。',NULL,NULL,NULL,NULL);
INSERT INTO "DifficultyTableRegistry"("method","signature","behavior","利用_AppConfig","生成_BmtTableReader","利用_NotationPrefixResolver","利用_TableLevelParser") VALUES('parseNotation','TableLevelParser.ParsedTableLevel parseNotation(String notation)','読み込んだ表のタグも使って表記を記号とレベルに分ける。',NULL,NULL,NULL,NULL);
INSERT INTO "DifficultyTableRegistry"("method","signature","behavior","利用_AppConfig","生成_BmtTableReader","利用_NotationPrefixResolver","利用_TableLevelParser") VALUES('resolveByHash','NotationResolution resolveByHash(String sha256, String md5)','sha256 または md5 で曲を探し、優先順で並べて記号置き換えを反映し、第一候補・投稿用既定表記・候補一覧を返す。見つからなければすべて空の結果を返す。',NULL,NULL,NULL,NULL);
INSERT INTO "DifficultyTableRegistry"("method","signature","behavior","利用_AppConfig","生成_BmtTableReader","利用_NotationPrefixResolver","利用_TableLevelParser") VALUES('resolveForTitle','NotationResolution resolveForTitle(String title)','曲名の完全一致で曲を探し、第一候補・投稿用既定表記・候補一覧を返す。曲名が空なら空の結果を返す。',NULL,NULL,NULL,NULL);
INSERT INTO "DifficultyTableRegistry"("method","signature","behavior","利用_AppConfig","生成_BmtTableReader","利用_NotationPrefixResolver","利用_TableLevelParser") VALUES('resolveRewritePrefix','String resolveRewritePrefix(TableMatch match)','記号の置き換えで差し替える先頭部分を決める。表の .bmt タグで表記が始まっていればそのタグを、そうでなければ同じ表・記号の表記の並びから求めた共通の先頭部分を返す。',NULL,NULL,NULL,NULL);
INSERT INTO "DifficultyTableRegistry"("method","signature","behavior","利用_AppConfig","生成_BmtTableReader","利用_NotationPrefixResolver","利用_TableLevelParser") VALUES('setNotationRules','void setNotationRules(List<TableNotationRule> rules)','表ごとの表記ルールを表キーで引けるように登録する。表キーが空のルールは無視する。',NULL,NULL,NULL,NULL);
INSERT INTO "DifficultyTableRegistry"("method","signature","behavior","利用_AppConfig","生成_BmtTableReader","利用_NotationPrefixResolver","利用_TableLevelParser") VALUES('setTablePriority','void setTablePriority(List<String> tagsInPriorityOrder)','曲が複数の表に載っているときに優先する表の順を設定する。載っていない表は最後に回る。',NULL,NULL,NULL,NULL);

INSERT INTO "DiscordAutoPostQueue"("method","signature","behavior","利用_AppLogging","利用_PostedStateStore","利用_ScreenshotEntry") VALUES('offer','void offer(ScreenshotEntry entry, PostedStateStore postedStateStore)','スクショを待ち行列の末尾に入れる。引数が null、リザルト画面でない、Discord に投稿済み、すでに並んでいる、のどれかなら何もしない。',NULL,NULL,NULL);
INSERT INTO "DiscordAutoPostQueue"("method","signature","behavior","利用_AppLogging","利用_PostedStateStore","利用_ScreenshotEntry") VALUES('pollBatch','List<ScreenshotEntry> pollBatch(int batchSize)','先頭から指定枚数(1未満なら1枚)まで取り出して返す。残りが少なければあるだけ返す。',NULL,NULL,NULL);
INSERT INTO "DiscordAutoPostQueue"("method","signature","behavior","利用_AppLogging","利用_PostedStateStore","利用_ScreenshotEntry") VALUES('removePosted','void removePosted(List<ScreenshotEntry> entries)','渡したスクショのうち行列に並んでいるものを、ファイル名で探して取り除く。手動送信した分が後の自動投稿で重複しないようにするために使う。',NULL,NULL,NULL);
INSERT INTO "DiscordAutoPostQueue"("method","signature","behavior","利用_AppLogging","利用_PostedStateStore","利用_ScreenshotEntry") VALUES('requeueFront','void requeueFront(List<ScreenshotEntry> entries)','取り出したが送れなかったスクショを、元の順番のまま行列の先頭へ戻す。',NULL,NULL,NULL);
INSERT INTO "DiscordAutoPostQueue"("method","signature","behavior","利用_AppLogging","利用_PostedStateStore","利用_ScreenshotEntry") VALUES('size','int size()','いま並んでいるスクショの枚数を返す。',NULL,NULL,NULL);

INSERT INTO "DiscordWebhookService"("method","signature","behavior","利用_AppLogging") VALUES('PostResult','record PostResult(boolean success, String messageId, String message)','Discord 送信の結果を表す値。成否・メッセージ ID・応答本文またはエラー内容を持つ。',NULL);
INSERT INTO "DiscordWebhookService"("method","signature","behavior","利用_AppLogging") VALUES('contentType','String contentType(String fileName)','ファイル名の拡張子から画像の種類(image/jpeg・gif・webp)を返す。それ以外や拡張子なしはスクショの形式である image/png。',NULL);
INSERT INTO "DiscordWebhookService"("method","signature","behavior","利用_AppLogging") VALUES('headerSafeFileName','String headerSafeFileName(String fileName)','multipart の filename="…" を壊さないよう、ファイル名の二重引用符を '' に、改行を空白に置き換える。',NULL);
INSERT INTO "DiscordWebhookService"("method","signature","behavior","利用_AppLogging") VALUES('post','PostResult post(String webhookUrl, String text, List<Path> imagePaths)','Webhook URL に wait=true を付け、本文を payload_json、画像を files[n] とした multipart で POST する。URL が空・画像0枚・上限(10枚)超なら送らずに失敗を返す。2xx 応答なら本文の id をメッセージ ID として成功を返し、それ以外は HTTP ステータスと本文、例外時はその内容を失敗として返す。',NULL);

INSERT INTO "EnrichedScreenshot"("method","signature","behavior","利用_PlayScore","利用_ScreenshotEntry") VALUES('mergedAvailableNotations','List<String> mergedAvailableNotations()','表記の候補一覧を返す。既定の表記が候補に無ければ先頭に足す。',NULL,NULL);

INSERT INTO "FilenameParser"("method","signature","behavior","利用_ClearLamp","利用_PlayerKind","生成_ScreenshotEntry") VALUES('parse','ScreenshotEntry parse(Path filePath)
ScreenshotEntry parse(Path filePath, PlayerKind player)','スクショのパスを解析して ScreenshotEntry を返す。引数1つの版は beatoraja 形式として、プレイヤー種別付きの版は BMZ なら BMZ 形式、それ以外は beatoraja 形式として解析する。BMZ形式ではミリ秒まで撮影時刻に入れ、末尾のランプラベルを beatoraja のランプ名に直し(ランクはファイル名にないので空)、形式に合わない名前は画面種別を空、曲名をファイル名にして返す。',NULL,NULL,NULL);


INSERT INTO "GitHubReleaseClient"("method","signature","behavior","利用_AppLogging","利用_VersionComparator") VALUES('ReleaseInfo','record ReleaseInfo(String tagName, String version, URI downloadUrl)','最新リリースの情報を表す値。タグ名・比較用に整えたバージョン・配布用 zip のダウンロード URL を持つ。',NULL,NULL);
INSERT INTO "GitHubReleaseClient"("method","signature","behavior","利用_AppLogging","利用_VersionComparator") VALUES('fetchLatestRelease','ReleaseInfo fetchLatestRelease()','GitHub API の最新リリースを取得し、タグ名と beatoraja-screenshot-manager.zip のダウンロード URL を取り出して返す。HTTP 200 以外、タグ名が無い、zip が添付されていないときは IOException を投げる。',NULL,NULL);

INSERT INTO "HelpDialog"("method","signature","behavior","利用_AppPaths","利用_AppVersion") VALUES('openLogFolder','void openLogFolder(Component parent)','アプリのログフォルダを（なければ作ってから）OSのファイル管理画面で開く。デスクトップ連携が使えない環境ではフォルダのパスをメッセージで表示し、失敗したらエラーメッセージを出す。',NULL,NULL);
INSERT INTO "HelpDialog"("method","signature","behavior","利用_AppPaths","利用_AppVersion") VALUES('openRepository','void openRepository(Component parent)','GitHubのリポジトリのURLを既定のブラウザで開く。ブラウザを開けない環境ではURLをメッセージで表示し、開くのに失敗したらエラーメッセージを出す。',NULL,NULL);
INSERT INTO "HelpDialog"("method","signature","behavior","利用_AppPaths","利用_AppVersion") VALUES('showAbout','void showAbout(Component parent)','アプリのバージョン、概要、リポジトリURLを書いたモーダルダイアログを表示する。「GitHub を開く」ボタンでリポジトリを開き、「閉じる」で閉じる。',NULL,NULL);
INSERT INTO "HelpDialog"("method","signature","behavior","利用_AppPaths","利用_AppVersion") VALUES('showUsage','void showUsage(Component parent)','基本的な使い方（選択・投稿順の変更・Twitterは1ツイート20枚まで・Discordは1回10枚まで・設定の保存場所・BMZ Playerでの使い方など）を書いた読み取り専用の文章を、スクロール付きのメッセージダイアログで表示する。',NULL,NULL);

INSERT INTO "LampGroupedTweetText"("method","signature","behavior","利用_ClearLampLabels","利用_TweetTextGenerator","利用_TweetTextLimits") VALUES('Item','record Item(String clearType, String notation, String title)','投稿1枚分の材料を表す値。beatoraja のクリア種別の文字列・投稿用の表記・曲名を持つ。',NULL,NULL,NULL);
INSERT INTO "LampGroupedTweetText"("method","signature","behavior","利用_ClearLampLabels","利用_TweetTextGenerator","利用_TweetTextLimits") VALUES('generate','String generate(List<Item> items, Map<String, String> shortLabelOverrides)
String generate(List<Item> items, Map<String, String> shortLabelOverrides, Map<String, Boolean> visibilityOverrides)','ランプを良い順にたどり、表示するランプごとに短い名前(利用者の上書きがあればそれ)で譜面の表記を投稿順に並べた行を作り、改行でつなぐ。短い名前が同じランプは1行にまとめ、短い名前が空のランプは名前なしで表記だけを並べる。知らないランプ名は名前そのもの、空なら「?」の行に入れ、表記の無い譜面は曲名を20単位に切って載せる。2引数版はランプの表示切替を既定のまま(アシスト系・FAILED・NO PLAY は非表示)にし、3引数版は表示切替の上書きを受け取る。',NULL,NULL,NULL);
INSERT INTO "LampGroupedTweetText"("method","signature","behavior","利用_ClearLampLabels","利用_TweetTextGenerator","利用_TweetTextLimits") VALUES('shouldUse','boolean shouldUse(int imageCount)','画像枚数で280を割った1枚あたりの文字数が40未満になるとき(8枚以上)に true を返す。0枚以下なら false。',NULL,NULL,NULL);

INSERT INTO "Main"("method","signature","behavior","利用_AppConfig","利用_AppIcons","利用_AppLogging","利用_AppPaths","生成_AppUpdateService","利用_AppVersion","生成_FirstRunWizard","生成_MainFrame","利用_UiTheme","生成_UpdateProgressDialog") VALUES('main','void main(String[] args)','ログを初期化して未捕捉例外を記録するようにし、設定を読み込んで画面テーマとフォントを適用する。自動更新の対象なら更新進捗画面を出して更新を試し、再起動となればプロセスを終了する。それ以外は初回設定が未完了またはスクリーンショットフォルダ未設定なら初期設定ウィザードを、そうでなければメイン画面を開く。',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);

INSERT INTO "MainFrame"("method","signature","behavior","生成_AccentButton","利用_AppConfig","利用_AppIcons","利用_AppLogging","利用_AppVersion","利用_BatchPoster","生成_ChartResolverService","生成_ClixService","生成_DatabasePanel","利用_DifficultyTableRegistry","生成_DiscordAutoPostQueue","生成_DiscordWebhookService","利用_EnrichedScreenshot","利用_HelpDialog","生成_LampGroupedTweetText","利用_PlayerKind","利用_PostBatchSplitter","利用_PostConfirmDialog","生成_PostTextComposer","利用_PostTextFormat","生成_PostTextFormatDialog","利用_PostedStateStore","生成_PreviewPanel","生成_ScreenshotDatabase","利用_ScreenshotEntry","生成_ScreenshotFolderCleanupService","利用_ScreenshotRecord","生成_ScreenshotScanner","生成_ScreenshotWatcher","生成_SettingsDialog","生成_TableNotationRulesDialog","生成_TablePriorityDialog","生成_TwitterAuthService","生成_TwitterLoginDialog","利用_UiTheme") VALUES('dispose','void dispose()','ウィンドウを閉じる前に、フォルダ監視を止め、DB を閉じ（失敗はログに残すだけ）、サムネイル読み込みを止め、曲情報の照合サービスを閉じてから画面を破棄する。終了時だけでなく、外観変更でウィンドウを作り直すときにも呼ばれる。',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);

INSERT INTO "MultiColumnSortSupport"("method","signature","behavior") VALUES('createHeaderRenderer','TableCellRenderer createHeaderRenderer()','列見出し用の描画部品を作って返す。見出しを中央寄せにし、並べ替え条件に入っている列には降順なら▼、昇順なら▲と優先順位の番号を付けて表示する。');
INSERT INTO "MultiColumnSortSupport"("method","signature","behavior") VALUES('getSortSpecs','List<SortSpec> getSortSpecs()','現在の並べ替え条件（列番号と昇順・降順）を優先順に並べた変更不可のコピーを返す。リスト自体はコピーだが、中の条件オブジェクトは内部と共有している。');
INSERT INTO "MultiColumnSortSupport"("method","signature","behavior") VALUES('setSortChangeListener','void setSortChangeListener(SortChangeListener listener)','見出しクリックで並べ替え条件が変わったときに呼ぶ通知先を1つ登録する。前に登録したものは置き換わる。');

INSERT INTO "NotationPrefixResolver"("method","signature","behavior","利用_TableLevelParser") VALUES('applySymbolOverride','String applySymbolOverride(String notation, String prefix, String override)','表記が指定の先頭部分で始まっていれば、その部分を置き換え文字列に差し替えて返す。表記や置き換えが空、先頭部分が空または一致しない場合は元の表記を返す。',NULL);
INSERT INTO "NotationPrefixResolver"("method","signature","behavior","利用_TableLevelParser") VALUES('longestCommonPrefix','String longestCommonPrefix(List<String> values)','文字列の並びに共通する最長の先頭部分を返す。空の並びや null を含む場合は空文字を返す。',NULL);
INSERT INTO "NotationPrefixResolver"("method","signature","behavior","利用_TableLevelParser") VALUES('resolveReplaceablePrefix','String resolveReplaceablePrefix(List<String> notations, List<String> knownTags, String fallbackSymbol)','表記の並びから置き換え対象の先頭部分を求める。各表記の記号部分の共通先頭部分があればそれを、なければ予備の記号を返し、記号が取れないときは表記全体の共通先頭部分を、どの表記にもその後ろに文字が残る長さまで縮めて返す。',NULL);

INSERT INTO "OsInfo"("method","signature","behavior") VALUES('isWindows','boolean isWindows()','OS 名に win が含まれていれば true を返す。');

INSERT INTO "PlayHistory"("method","signature","behavior","利用_PlayLogEntry") VALUES('between','List<PlayLogEntry> between(long fromInclusive, long toInclusive)','指定した範囲(両端を含むエポック秒)のプレイを古い順に返す。',NULL);
INSERT INTO "PlayHistory"("method","signature","behavior","利用_PlayLogEntry") VALUES('clear','void clear()','持っているプレイ履歴を捨てる。',NULL);
INSERT INTO "PlayHistory"("method","signature","behavior","利用_PlayLogEntry") VALUES('columns','Set<String> columns(Statement statement, String table)','SQLite の表の列名を小文字にした集合を返す。判定数などの列があるかの確認に使う。',NULL);
INSERT INTO "PlayHistory"("method","signature","behavior","利用_PlayLogEntry") VALUES('latestAtOrBefore','Optional<PlayLogEntry> latestAtOrBefore(long toInclusive)','指定時刻以前で最も新しいプレイを返す。無ければ空。',NULL);


INSERT INTO "PlayLogSource"("method","signature","behavior","利用_PlayLogEntry") VALUES('close','void close()','読み込んだプレイ履歴を手放す。例外は投げない。',NULL);
INSERT INTO "PlayLogSource"("method","signature","behavior","利用_PlayLogEntry") VALUES('findNear','List<PlayLogEntry> findNear(LocalDateTime capturedAt, Integer expectedClearId)','撮影時刻と、ファイル名から読んだランプのID(なければ null)を受け取り、そのスクショのプレイらしい履歴を良い順に返す。',NULL);
INSERT INTO "PlayLogSource"("method","signature","behavior","利用_PlayLogEntry") VALUES('isAvailable','boolean isAvailable()','プレイ履歴を読み込めて検索できる状態かを返す。',NULL);

INSERT INTO "PlayScore"("method","signature","behavior") VALUES('BoundaryDiff','record BoundaryDiff(String rankKey, int diff)','ランクの境目の名前(MAX・AAA〜F)と、そこからのEXスコアの差(上回れば正、足りなければ負)の組。');
INSERT INTO "PlayScore"("method","signature","behavior") VALUES('bp','int bp()','BP(BADとPOORと空POORの合計)を返す。');
INSERT INTO "PlayScore"("method","signature","behavior") VALUES('exScore','int exScore()','EXスコア(PGREAT×2+GREAT)を返す。');
INSERT INTO "PlayScore"("method","signature","behavior") VALUES('maxExScore','int maxExScore()','理論値(ノーツ数×2)を返す。');
INSERT INTO "PlayScore"("method","signature","behavior") VALUES('nearestBoundaryDiff','BoundaryDiff nearestBoundaryDiff()','今のランクの境目からの上回り分と、一つ上の境目までの不足分のうち小さい方を返す。同じならプラス側を選び、AAAの上の境目はMAXとし、理論値ならMAX+0を返す。');
INSERT INTO "PlayScore"("method","signature","behavior") VALUES('rank','String rank()','EXスコアが理論値の9分の8以上ならAAA、7以上ならAA…と2(E)まで調べて、どれにも届かなければFを返す。境目は切り上げで計算し、MAXはランクとしては返さない。');
INSERT INTO "PlayScore"("method","signature","behavior") VALUES('ratePercent','double ratePercent()','スコアレート(EXスコア÷理論値の百分率)を返す。ノーツ数が0なら0を返す。');

INSERT INTO "PlayerKind"("method","signature","behavior","利用_BmzPaths") VALUES('configKey','String configKey()','設定ファイルに保存するキー(beatoraja / bmz、不明は空)を返す。',NULL);
INSERT INTO "PlayerKind"("method","signature","behavior","利用_BmzPaths") VALUES('detect','PlayerKind detect(Path directory)','フォルダに config_sys.json があれば beatoraja、そのフォルダかその下の data に config.toml があれば BMZ、どちらでもなければ(フォルダが null・存在しない場合も)不明を返す。',NULL);
INSERT INTO "PlayerKind"("method","signature","behavior","利用_BmzPaths") VALUES('displayName','String displayName()','画面に出す名前(beatoraja / BMZ Player / 不明)を返す。',NULL);
INSERT INTO "PlayerKind"("method","signature","behavior","利用_BmzPaths") VALUES('fromConfigKey','PlayerKind fromConfigKey(String key)','保存されたキーを大文字小文字・前後空白を問わずに照合して種別を返す。空や知らない値なら不明を返す。',NULL);
INSERT INTO "PlayerKind"("method","signature","behavior","利用_BmzPaths") VALUES('playerFolderOf','Path playerFolderOf(Path screenshotDirectory)','スクショフォルダしか設定されていないときに、そのプレイヤーフォルダを推定する。beatoraja の screenshot フォルダならその親、BMZ Player の data フォルダ直下の screenshots ならその親、それ以外はそのフォルダ自身を返す。null なら null。',NULL);
INSERT INTO "PlayerKind"("method","signature","behavior","利用_BmzPaths") VALUES('toString','String toString()','画面表示名を返す(コンボボックス表示用)。',NULL);

INSERT INTO "PlayerPlayLogService"("method","signature","behavior","生成_PlayHistory","生成_PlayLogEntry","継承_PlayLogSource","生成_PlayScore") VALUES('close','void close()','メモリ上のプレイ履歴を破棄する。',NULL,NULL,NULL,NULL);
INSERT INTO "PlayerPlayLogService"("method","signature","behavior","生成_PlayHistory","生成_PlayLogEntry","継承_PlayLogSource","生成_PlayScore") VALUES('findNear','List<PlayLogEntry> findNear(LocalDateTime capturedAt, Integer expectedClearId)','撮影時刻の3分前から30秒後までのプレイを集め、期待するランプIDが指定されていて一致するものがあればそれだけに絞り、撮影時刻に近い順に返す。読み込めていない、または撮影時刻が null なら空の一覧を返す。',NULL,NULL,NULL,NULL);
INSERT INTO "PlayerPlayLogService"("method","signature","behavior","生成_PlayHistory","生成_PlayLogEntry","継承_PlayLogSource","生成_PlayScore") VALUES('isAvailable','boolean isAvailable()','scoredatalog.db を読み込めたかを返す。',NULL,NULL,NULL,NULL);

INSERT INTO "PostBatchSplitter"("method","signature","behavior") VALUES('partition','<T> List<List<T>> partition(List<T> items, int maxBatchSize)','一覧を先頭から最大枚数ずつ区切り、変更できない束のリストにして返す。入力が null か空なら空のリストを返し、最大枚数が1未満なら例外を投げる。');

INSERT INTO "PostConfirmDialog"("method","signature","behavior","利用_TweetTextLimits","利用_UiTheme") VALUES('confirmDiscord','List<String> confirmDiscord(Component parent, List<String> texts)','Discord の送信文を送信ごとに編集できる形で1回だけ見せ、確定した本文を返す。文字数の確認はしない。キャンセルなら null。',NULL,NULL);
INSERT INTO "PostConfirmDialog"("method","signature","behavior","利用_TweetTextLimits","利用_UiTheme") VALUES('confirmTweets','List<String> confirmTweets(Component parent, List<String> initialTexts, int abbreviatedCount)','X の投稿文をツイートごとに編集できる形で見せ、文字数のカウンタ(280超は赤)を付ける。曲名を短くした件数があれば最初の1回だけ注記する。確定時に280を超えるツイートがあれば警告して、入力内容を保ったまま出し直す。キャンセルなら null。',NULL,NULL);

INSERT INTO "PostTextComposer"("method","signature","behavior","利用_AppConfig","生成_LampGroupedTweetText","利用_PlayScore","利用_ScreenshotEntry","利用_TweetTextGenerator","利用_TweetTextLimits") VALUES('PostContext','record PostContext(String notation, PlayScore score)','スクショ1枚の行を作るのに要る、投稿表記と照合したプレイのスコア(無ければ null)。照合できないときは NONE(表記なし・スコアなし)を使う。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "PostTextComposer"("method","signature","behavior","利用_AppConfig","生成_LampGroupedTweetText","利用_PlayScore","利用_ScreenshotEntry","利用_TweetTextGenerator","利用_TweetTextLimits") VALUES('TweetTexts','record TweetTexts(List<String> texts, int abbreviatedCount)','X 用の投稿文(投稿1回につき1つ)と、文字数に収めるために曲名を短くした件数。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "PostTextComposer"("method","signature","behavior","利用_AppConfig","生成_LampGroupedTweetText","利用_PlayScore","利用_ScreenshotEntry","利用_TweetTextGenerator","利用_TweetTextLimits") VALUES('join','String join(List<ScreenshotEntry> batch, Map<String, String> messagesByFile)','バッチの各スクショの行を表から引き、改行でつなぐ。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "PostTextComposer"("method","signature","behavior","利用_AppConfig","生成_LampGroupedTweetText","利用_PlayScore","利用_ScreenshotEntry","利用_TweetTextGenerator","利用_TweetTextLimits") VALUES('merged','String merged(List<ScreenshotEntry> entries)','各スクショの行を改行でつないだ1回分の投稿文を返す(Discord の自動投稿で使う)。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "PostTextComposer"("method","signature","behavior","利用_AppConfig","生成_LampGroupedTweetText","利用_PlayScore","利用_ScreenshotEntry","利用_TweetTextGenerator","利用_TweetTextLimits") VALUES('message','String message(ScreenshotEntry entry)','スクショ1枚の行を利用者の投稿文書式とランプ表示名で作る。曲名は短くしない。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "PostTextComposer"("method","signature","behavior","利用_AppConfig","生成_LampGroupedTweetText","利用_PlayScore","利用_ScreenshotEntry","利用_TweetTextGenerator","利用_TweetTextLimits") VALUES('messagesByFile','Map<String, String> messagesByFile(List<ScreenshotEntry> entries)','各スクショの行をファイル名をキーにして、渡した順に並べた表で返す。',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "PostTextComposer"("method","signature","behavior","利用_AppConfig","生成_LampGroupedTweetText","利用_PlayScore","利用_ScreenshotEntry","利用_TweetTextGenerator","利用_TweetTextLimits") VALUES('tweetTexts','TweetTexts tweetTexts(List<List<ScreenshotEntry>> batches)','バッチごとに X の投稿文を作る。8枚以上のバッチはランプ別まとめ形式、それ未満は1枚1行で、280を超えるときは曲名を長いものから順に、30単位を下限として収まるまで短くする。短くした件数も返す。',NULL,NULL,NULL,NULL,NULL,NULL);

INSERT INTO "PostTextFormat"("method","signature","behavior","利用_PlayScore") VALUES('defaults','PostTextFormat defaults()','既定の書式を返す。難易度表記・曲名・ランプ・DJ RANKを有効、BP(前にBP)・スコアレート(後ろに%)・EXスコア(前にEX)・境目との差を無効にした並びになる。',NULL);
INSERT INTO "PostTextFormat"("method","signature","behavior","利用_PlayScore") VALUES('getItems','List<Item> getItems()','投稿文の項目(キー・有効か・前後の文字)の並びを返す。',NULL);
INSERT INTO "PostTextFormat"("method","signature","behavior","利用_PlayScore") VALUES('getRankLabels','Map<String, String> getRankLabels()','DJ RANKの表示名の上書き(ランク→表示名)を返す。',NULL);
INSERT INTO "PostTextFormat"("method","signature","behavior","利用_PlayScore") VALUES('normalized','PostTextFormat normalized()','知らない項目や重複した項目を除いて複製し、欠けている項目を既定の前後文字・無効の状態で末尾に足した新しい書式を返す。ランクの表示名も既知のランクで空でないものだけ残す。',NULL);
INSERT INTO "PostTextFormat"("method","signature","behavior","利用_PlayScore") VALUES('rankLabel','String rankLabel(String rankKey)','DJ RANKの表示名を返す。上書きがなければランク名そのもの、空のランクなら空文字を返す。',NULL);
INSERT INTO "PostTextFormat"("method","signature","behavior","利用_PlayScore") VALUES('setItems','void setItems(List<Item> items)','項目の並びをコピーして設定する。null は空にする。',NULL);
INSERT INTO "PostTextFormat"("method","signature","behavior","利用_PlayScore") VALUES('setRankLabels','void setRankLabels(Map<String, String> rankLabels)','DJ RANKの表示名の上書きをコピーして設定する。null は空にする。',NULL);

INSERT INTO "PostTextFormatDialog"("method","signature","behavior","利用_AppIcons","生成_PlayScore","生成_PostTextFormat","生成_ScreenshotEntry","利用_TweetTextGenerator") VALUES('showDialog','PostTextFormat showDialog()','ダイアログをモーダルで表示し、閉じられるまで待つ。OKで閉じたときは編集中のセルを確定し、項目と空欄でないランク表示名から作って整えた書式を返し、キャンセルや×で閉じたときはnullを返す。',NULL,NULL,NULL,NULL,NULL);

INSERT INTO "PostedStateStore"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles") VALUES('get','PostedRecord get(String fileName)','ファイル名の投稿記録を返す。記録が無ければ未投稿を表す空の記録を返す(内部には登録しない)。',NULL,NULL,NULL);
INSERT INTO "PostedStateStore"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles") VALUES('load','PostedStateStore load()
PostedStateStore load(Path file)','posted.json(引数付きの版は指定ファイル)を読み込んだ記録帳を作って返す。ファイルが無ければ空の記録帳。読めなければ警告をログに残し、ファイルを .broken-日時 に退避して空の記録帳を返し、loadProblem に利用者向けの説明を入れる。',NULL,NULL,NULL);
INSERT INTO "PostedStateStore"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles") VALUES('loadProblem','String loadProblem()','読み込み時に記録を読めなかった場合の利用者向けの説明を返す。問題なければ null。',NULL,NULL,NULL);
INSERT INTO "PostedStateStore"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles") VALUES('markDiscordPosted','void markDiscordPosted(String fileName, String messageId)','ファイル名に Discord 投稿済みの印を、現在時刻とメッセージ ID で付ける。保存は別途 save を呼ぶ必要がある。',NULL,NULL,NULL);
INSERT INTO "PostedStateStore"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles") VALUES('markTwitterPosted','void markTwitterPosted(String fileName, String tweetId)','ファイル名に X 投稿済みの印を、現在時刻と投稿 ID で付ける。保存は別途 save を呼ぶ必要がある。',NULL,NULL,NULL);
INSERT INTO "PostedStateStore"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_AtomicFiles") VALUES('save','void save()','すべての記録を整形した JSON にし、一時ファイル経由で posted.json に書き出す。書けなければ IOException を投げる。',NULL,NULL,NULL);

INSERT INTO "PreviewPanel"("method","signature","behavior","利用_AppLogging","利用_Badge","生成_CardPanel","利用_ClearLampLabels","利用_ResultPalette","利用_ScreenshotEntry","利用_UiTheme") VALUES('setClearLampLabels','void setClearLampLabels(Map<String, String> clearLampLabels)','クリアランプの表示名の置き換え表を受け取って保持し（nullなら空の表）、再描画を求める。ヘッダーのランプのピルは次にエントリを表示し直したときにこの表示名で作られる。',NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "PreviewPanel"("method","signature","behavior","利用_AppLogging","利用_Badge","生成_CardPanel","利用_ClearLampLabels","利用_ResultPalette","利用_ScreenshotEntry","利用_UiTheme") VALUES('setPostOrderChangeListener','void setPostOrderChangeListener(PostOrderChangeListener listener)','投稿順リストでの↑・↓ボタン操作とドラッグでの並び替えを受け取る通知先を登録する。この欄は並び替えを自分では行わず、通知先が順序を変えて表示し直す前提である。',NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "PreviewPanel"("method","signature","behavior","利用_AppLogging","利用_Badge","生成_CardPanel","利用_ClearLampLabels","利用_ResultPalette","利用_ScreenshotEntry","利用_UiTheme") VALUES('showEntries','void showEntries(List<ScreenshotEntry> entries, Map<String, String> messagesByFile)','表示内容をすべて作り直す。ヘッダーは未選択なら「未選択」、複数なら「N 枚を選択中」と先頭の曲名、1枚なら曲名・撮影日時と記号・レベル・ランク・ランプのピルにする。各エントリについてプレビュー行(番号・画像・投稿文の先頭40文字)と投稿順リストの行を追加する。画像はキャッシュにあればすぐ表示し、なければ場所を空けて裏スレッドで読み込み、読み終わった時点で差し替える(その間に表示し直していれば捨てる)。',NULL,NULL,NULL,NULL,NULL,NULL,NULL);

INSERT INTO "ProcessUtils"("method","signature","behavior","利用_AppLogging","利用_OsInfo") VALUES('destroyProcessTree','void destroyProcessTree(Process process)','渡されたプロセスが生きていれば子プロセスごと強制終了し、最大10秒終了を待つ。Windows では taskkill /F /T を最大15秒待ち、失敗や割り込み時は本体だけを強制終了する。null や終了済みなら何もしない。',NULL,NULL);

INSERT INTO "ResultPalette"("method","signature","behavior","利用_ClearLamp","利用_UiTheme") VALUES('lampColor','Color lampColor(String lamp)','クリアランプ名（大文字小文字・前後の空白は無視）に対応する色を ClearLamp から返す。未知の名前やnullは灰色を返し、ライトテーマでは明るすぎる色を暗くして返す。',NULL,NULL);
INSERT INTO "ResultPalette"("method","signature","behavior","利用_ClearLamp","利用_UiTheme") VALUES('rankColor','Color rankColor(String rank)','DJランク（MAX・AAA〜F、大文字小文字は無視）に対応する色を返す。未知の値やnullは灰色を返し、ライトテーマでは明るすぎる色を暗くして返す。',NULL,NULL);
INSERT INTO "ResultPalette"("method","signature","behavior","利用_ClearLamp","利用_UiTheme") VALUES('stateColor','Color stateColor(String state)','画面種別（Result・Course Result・Play・Music Select・Decide・Config）に対応する色を返す。大文字小文字は区別し前後の空白だけ除く。未知の値は灰色、nullはテーマ補正なしの灰色を返す。',NULL,NULL);
INSERT INTO "ResultPalette"("method","signature","behavior","利用_ClearLamp","利用_UiTheme") VALUES('symbolColor','Color symbolColor(String symbol)','難易度表の記号の文字列のハッシュ値から色相を決め、記号ごとに異なる色を返す。明るさはダークテーマでは高め、ライトテーマでは低めにする。空・null・「-」なら灰色を返す。',NULL,NULL);
INSERT INTO "ResultPalette"("method","signature","behavior","利用_ClearLamp","利用_UiTheme") VALUES('tint','Color tint(Color color, Color background, float alpha)','色を背景色に指定の割合で混ぜた不透明な色を返す。割合が1に近いほど元の色、0に近いほど背景色になる。',NULL,NULL);

INSERT INTO "ScreenshotDatabase"("method","signature","behavior","利用_AppPaths","利用_EnrichedScreenshot","生成_ScreenshotRecord") VALUES('close','void close()','DB接続を閉じる。',NULL,NULL,NULL);
INSERT INTO "ScreenshotDatabase"("method","signature","behavior","利用_AppPaths","利用_EnrichedScreenshot","生成_ScreenshotRecord") VALUES('delete','void delete(long id)','指定IDの行を削除する。',NULL,NULL,NULL);
INSERT INTO "ScreenshotDatabase"("method","signature","behavior","利用_AppPaths","利用_EnrichedScreenshot","生成_ScreenshotRecord") VALUES('findAll','List<ScreenshotRecord> findAll()','全行を撮影時刻の新しい順(同時刻はIDの大きい順)に読み、レコードの一覧で返す。',NULL,NULL,NULL);
INSERT INTO "ScreenshotDatabase"("method","signature","behavior","利用_AppPaths","利用_EnrichedScreenshot","生成_ScreenshotRecord") VALUES('findByFilePath','ScreenshotRecord findByFilePath(Path filePath)','ファイルの絶対パスで1行を探してレコードで返す。なければ null を返す。',NULL,NULL,NULL);
INSERT INTO "ScreenshotDatabase"("method","signature","behavior","利用_AppPaths","利用_EnrichedScreenshot","生成_ScreenshotRecord") VALUES('findById','ScreenshotRecord findById(long id)','指定IDの行をレコードで返す。なければ null。',NULL,NULL,NULL);
INSERT INTO "ScreenshotDatabase"("method","signature","behavior","利用_AppPaths","利用_EnrichedScreenshot","生成_ScreenshotRecord") VALUES('formatDate','String formatDate(LocalDateTime dateTime)','日時を「yyyy/MM/dd HH:mm」の表示用文字列にする。null なら空文字を返す。',NULL,NULL,NULL);
INSERT INTO "ScreenshotDatabase"("method","signature","behavior","利用_AppPaths","利用_EnrichedScreenshot","生成_ScreenshotRecord") VALUES('syncAll','void syncAll(List<EnrichedScreenshot> entries)','1つのトランザクションで、渡された照合結果をすべて登録・更新し、ファイルが消えた行を削除する。途中で失敗したら全体を取り消して例外を投げ直す。',NULL,NULL,NULL);
INSERT INTO "ScreenshotDatabase"("method","signature","behavior","利用_AppPaths","利用_EnrichedScreenshot","生成_ScreenshotRecord") VALUES('updateFlagged','void updateFlagged(long id, boolean flagged)','指定IDの行のフラグを設定し、更新日時を記録する。',NULL,NULL,NULL);
INSERT INTO "ScreenshotDatabase"("method","signature","behavior","利用_AppPaths","利用_EnrichedScreenshot","生成_ScreenshotRecord") VALUES('updateNotation','void updateNotation(long id, String tableSymbol, String postNotation)','指定IDの行の表記号と投稿用表記を手で変えた値にし、手動編集済みの印を付ける。null は空文字にする。',NULL,NULL,NULL);
INSERT INTO "ScreenshotDatabase"("method","signature","behavior","利用_AppPaths","利用_EnrichedScreenshot","生成_ScreenshotRecord") VALUES('upsert','void upsert(EnrichedScreenshot enriched)','照合結果1件をファイルパスをキーに登録し、既にあれば更新する。曲名は照合で得た名前を優先し、候補表記は既定表記を先頭に含めてJSONで保存する。手動編集済みの行では表記号と投稿用表記を上書きしない。',NULL,NULL,NULL);

INSERT INTO "ScreenshotEntry"("method","signature","behavior") VALUES('equals','boolean equals(Object o)','ファイルパスが同じなら等しいとみなす。');
INSERT INTO "ScreenshotEntry"("method","signature","behavior") VALUES('getCapturedAt','LocalDateTime getCapturedAt()','撮影時刻を返す。読み取れなかった場合は null。');
INSERT INTO "ScreenshotEntry"("method","signature","behavior") VALUES('getClearType','String getClearType()','ランプ名(beatorajaの表記)を返す。不明なら空文字。');
INSERT INTO "ScreenshotEntry"("method","signature","behavior") VALUES('getFileName','String getFileName()','ファイル名を返す。');
INSERT INTO "ScreenshotEntry"("method","signature","behavior") VALUES('getFilePath','Path getFilePath()','ファイルのパスを返す。');
INSERT INTO "ScreenshotEntry"("method","signature","behavior") VALUES('getLevel','String getLevel()','BMSのレベル(LEVELnn の数字部分)を返す。');
INSERT INTO "ScreenshotEntry"("method","signature","behavior") VALUES('getRank','String getRank()','DJ RANKを返す。BMZのスクショでは常に空文字。');
INSERT INTO "ScreenshotEntry"("method","signature","behavior") VALUES('getRawTableFolder','String getRawTableFolder()','ファイル名に入っていた難易度表フォルダ名(例: sl12)をそのまま返す。');
INSERT INTO "ScreenshotEntry"("method","signature","behavior") VALUES('getStateLabel','String getStateLabel()','画面種別(Result、Play、Music Select、Decide、Config、Course Result、不明なら空)を返す。');
INSERT INTO "ScreenshotEntry"("method","signature","behavior") VALUES('getTableLevel','String getTableLevel()','非推奨。getRawTableFolder と同じ難易度表フォルダ名を返す。');
INSERT INTO "ScreenshotEntry"("method","signature","behavior") VALUES('getTitle','String getTitle()','ファイル名から読んだ曲名を返す。');
INSERT INTO "ScreenshotEntry"("method","signature","behavior") VALUES('hashCode','int hashCode()','ファイルパスから計算したハッシュ値を返す。');
INSERT INTO "ScreenshotEntry"("method","signature","behavior") VALUES('isBmsLevelOnly','boolean isBmsLevelOnly()','難易度表フォルダ名がなく、BMSのレベルだけが分かっているかを返す。');
INSERT INTO "ScreenshotEntry"("method","signature","behavior") VALUES('isResultScreenshot','boolean isResultScreenshot()','画面種別が Result かを返す(Course Result は含まない)。');
INSERT INTO "ScreenshotEntry"("method","signature","behavior") VALUES('mayShowPlayResult','boolean mayShowPlayResult()','プレイ結果を引く意味がある画面かを返す。プレイ前やプレイ外と分かっている画面(選曲・決定・プレイ・設定)は false、リザルト・コースリザルト・判別できなかった画面は true。');
INSERT INTO "ScreenshotEntry"("method","signature","behavior") VALUES('toString','String toString()','ファイル名を返す。');

INSERT INTO "ScreenshotFileRemover"("method","signature","behavior","利用_ScreenshotRecord") VALUES('Result','record Result(List<ScreenshotRecord> removed, List<String> failures)','ファイルが無くなったスクショの記録と、消せなかったものの「ファイル名: 理由」の一覧。',NULL);
INSERT INTO "ScreenshotFileRemover"("method","signature","behavior","利用_ScreenshotRecord") VALUES('deleteFiles','Result deleteFiles(List<ScreenshotRecord> records)','各記録の画像ファイルを削除する。もともと無いものは消せた扱いにし、消せなかったものは理由を集める。',NULL);

INSERT INTO "ScreenshotFolderCleanupService"("method","signature","behavior","利用_AppLogging","利用_ScreenshotEntry","生成_ScreenshotScanner") VALUES('deleteAllPng','int deleteAllPng(Path directory)','フォルダ内の PNG を一覧で取り、1枚ずつ削除して消せた枚数を返す。個々の削除に失敗したものは警告をログに残して飛ばす。',NULL,NULL,NULL);

INSERT INTO "ScreenshotRecord"("method","signature","behavior") VALUES('displayLevel','String displayLevel()','表示用のレベルを返す。難易度表のレベルがあればそれを、なければBMSのレベルを、どちらもなければ空文字を返す。');

INSERT INTO "ScreenshotScanner"("method","signature","behavior","利用_FilenameParser","利用_PlayerKind","利用_ScreenshotEntry") VALUES('scan','List<ScreenshotEntry> scan(Path directory)
List<ScreenshotEntry> scan(Path directory, PlayerKind player)','フォルダ直下の PNG ファイルを、ファイル名を解析したスクショ情報にして撮影時刻の新しい順(時刻不明は最後、同時刻はファイル名の逆順)で返す。フォルダが null か存在しなければ空の一覧を返す。1引数版は beatoraja の命名規則で、2引数版は指定したプレイヤーの命名規則で解析する。',NULL,NULL,NULL);

INSERT INTO "ScreenshotWatcher"("method","signature","behavior","利用_AppLogging","利用_FilenameParser","利用_PlayerKind","利用_ScreenshotEntry") VALUES('close','void close()','監視を止め、監視用スレッドも終わらせる。',NULL,NULL,NULL,NULL);
INSERT INTO "ScreenshotWatcher"("method","signature","behavior","利用_AppLogging","利用_FilenameParser","利用_PlayerKind","利用_ScreenshotEntry") VALUES('start','void start(Path directory, PlayerKind player, Listener listener)','前の監視を止めてから、指定フォルダのファイル作成を監視し始める。裏スレッドが1秒ごとに通知を確かめ、拡張子が png の新規ファイルは中身が書かれるまで最大2秒待って(読み取りに失敗しても待ち続ける)から、指定したプレイヤーの命名規則で解析して通知先に渡す。通知先で例外が出てもログに残して監視を続ける。',NULL,NULL,NULL,NULL);
INSERT INTO "ScreenshotWatcher"("method","signature","behavior","利用_AppLogging","利用_FilenameParser","利用_PlayerKind","利用_ScreenshotEntry") VALUES('stop','void stop()','監視ループを止める印を立て、OS のファイル監視を閉じる。スレッド自体は残るので、再び start できる。',NULL,NULL,NULL,NULL);


INSERT INTO "SongDatabaseService"("method","signature","behavior") VALUES('SongRecord','record SongRecord(String md5, String sha256, String title, String fullTitle, int level)','楽曲1件。md5、sha256、タイトル、サブタイトルを空白でつないだ表示タイトル、レベルを持つ。表によって登録名がどちらか違うため、呼び出し側は両方で照合する。');
INSERT INTO "SongDatabaseService"("method","signature","behavior") VALUES('close','void close()','メモリ上の楽曲一覧を破棄する。');
INSERT INTO "SongDatabaseService"("method","signature","behavior") VALUES('findBySha256','SongRecord findBySha256(String sha256)','sha256 で楽曲を探して返す。読み込めていない、引数が空、見つからない場合は null を返す。');
INSERT INTO "SongDatabaseService"("method","signature","behavior") VALUES('forBmzLibrary','SongDatabaseService forBmzLibrary(Path libraryDb)','BMZ Player の library.db の charts 表(play_level をレベルとして読む)を読み込んだ楽曲DBを作る。通常のコンストラクタは beatoraja の songdata.db の song 表を読む。');
INSERT INTO "SongDatabaseService"("method","signature","behavior") VALUES('isAvailable','boolean isAvailable()','楽曲DBを読み込めたかを返す。');

INSERT INTO "StateCell"("method","signature","behavior") VALUES('compareTo','int compareTo(StateCell other)','表示用文字列どうしを大文字小文字を区別せずに比べた結果を返す。表の並べ替えに使われる。');
INSERT INTO "StateCell"("method","signature","behavior") VALUES('displayText','String displayText()','画面種別のあとに、投稿済みなら「[X]」「[Discord]」を空白区切りで付けた文字列を返す。何もなければ「-」を返す。');
INSERT INTO "StateCell"("method","signature","behavior") VALUES('state','String state()','画面種別の文字を前後の空白を除いて返す。nullや空白だけなら空文字を返す。');

INSERT INTO "StateCellRenderer"("method","signature","behavior","利用_Badge","利用_ResultPalette","生成_StateCell","利用_UiTheme") VALUES('getTableCellRendererComponent','Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)','セル値が状態セルならそれを、違えば空の状態セルを保持し、太字の一回り小さいフォントと、表示用文字列のツールチップを設定して自身を返す。',NULL,NULL,NULL,NULL);
INSERT INTO "StateCellRenderer"("method","signature","behavior","利用_Badge","利用_ResultPalette","生成_StateCell","利用_UiTheme") VALUES('paintComponent','void paintComponent(Graphics g)','通常の背景を描いたあと、左から画面種別のピル（画面種別ごとの色）、X投稿済みならXの色の「X」ピル、Discord投稿済みならDiscordの色の「Discord」ピルを4ピクセル間隔で並べる。どれもなければ薄い「-」を描く。',NULL,NULL,NULL,NULL);

INSERT INTO "TableLevelParser"("method","signature","behavior") VALUES('ParsedTableLevel','record ParsedTableLevel(String symbol, String level, boolean bmsLevelOnly)','分割結果。記号・レベル・BMSのレベルだけの表記かを持ち、表記に戻す(記号なしは LEVELnn)処理と、難易度表の記号を持つかの判定を備える。');
INSERT INTO "TableLevelParser"("method","signature","behavior") VALUES('parse','ParsedTableLevel parse(String folderName, List<String> knownTags)','フォルダ名を記号とレベルに分ける。LEVELnn や LEVEL??? はBMSレベルのみとして返し、既定の記号と渡されたタグを長い順に先頭一致で試して残りが数字(小数・??? 可)ならその組を返し、合わなければ末尾の数字部分で分割する。分けられなければ全体を記号として返す。');

INSERT INTO "TableNotationRulesDialog"("method","signature","behavior","生成_AppConfig","利用_AppIcons","利用_NotationPrefixResolver") VALUES('showDialog','List<AppConfig.TableNotationRule> showDialog()','ダイアログをモーダルで表示し、閉じられるまで待つ。OKなら「表示する」側の表と、空欄でなく元の接頭辞とも違う記号置き換えを持つ表だけについてルールを作った一覧を返し（置き換えは表全体に効く1件として保存）、キャンセルならnullを返す。',NULL,NULL,NULL);

INSERT INTO "TablePriorityDialog"("method","signature","behavior","利用_AppIcons") VALUES('showDialog','List<String> showDialog()','ダイアログをモーダルで表示し、閉じられるまで待つ。OKなら一覧の上から順に並べた難易度表タグのリストを返し、キャンセルならnullを返す。',NULL);

INSERT INTO "ThumbnailCache"("method","signature","behavior","利用_AppLogging","利用_UiTheme") VALUES('get','Icon get(Path path)','画像パスに対応するサムネイルがあればすぐ返す。なければ裏スレッドに読み込みを1回だけ依頼してnullを返し、完成後に再描画処理が呼ばれる。読み込みに失敗した画像は、ファイルの更新日時が変わるまで読み直さない。',NULL,NULL);
INSERT INTO "ThumbnailCache"("method","signature","behavior","利用_AppLogging","利用_UiTheme") VALUES('height','int height()','作るサムネイルの高さ（ピクセル）を返す。',NULL,NULL);
INSERT INTO "ThumbnailCache"("method","signature","behavior","利用_AppLogging","利用_UiTheme") VALUES('shutdown','void shutdown()','読み込み用の裏スレッドを即時に止め、待っている読み込みを捨てる。',NULL,NULL);
INSERT INTO "ThumbnailCache"("method","signature","behavior","利用_AppLogging","利用_UiTheme") VALUES('width','int width()','作るサムネイルの幅（ピクセル）を返す。',NULL,NULL);

INSERT INTO "ThumbnailCellRenderer"("method","signature","behavior","利用_ThumbnailCache","利用_UiTheme") VALUES('getTableCellRendererComponent','Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)','セル値が画像パスならキャッシュからサムネイルを取り出して保持し（未完成ならnull）、ファイル名をツールチップにして自身を返す。パスでなければ何も保持しない。',NULL,NULL);
INSERT INTO "ThumbnailCellRenderer"("method","signature","behavior","利用_ThumbnailCache","利用_UiTheme") VALUES('paintComponent','void paintComponent(Graphics g)','通常の背景を描いたあと、セル中央にサムネイルを描く。サムネイルがまだなければ同じ大きさの枠線色の角丸四角を代わりに描く。',NULL,NULL);

INSERT INTO "TweetTextGenerator"("method","signature","behavior","利用_ClearLampLabels","利用_PlayScore","利用_PostTextFormat","利用_ScreenshotEntry","利用_TweetTextLimits") VALUES('generate','String generate(ScreenshotEntry entry, String postNotation, PlayScore score, PostTextFormat format)
String generate(ScreenshotEntry entry, String postNotation, PlayScore score, PostTextFormat format, int maxTitleWeightedLength, Map<String, String> clearLampLabels)','スクショ1枚から投稿文を作る。表記・曲名・ランプ・ランク・スコア系の値を書式の有効な項目の順に、前後の文字を付けて空白でつなぎ、全部空ならファイル名を返す。スコアが無い、またはノーツ数0のときはスコア系の項目を省き、ランクはファイル名のものを使う。4引数の版は曲名そのまま・ランプ名の置き換えなし(書式画面の見本用)。6引数の版は曲名を X の数え方で指定の長さに切り詰め、ランプ名を利用者の呼び名に置き換える(投稿用)。',NULL,NULL,NULL,NULL,NULL);
INSERT INTO "TweetTextGenerator"("method","signature","behavior","利用_ClearLampLabels","利用_PlayScore","利用_PostTextFormat","利用_ScreenshotEntry","利用_TweetTextLimits") VALUES('truncate','String truncate(String text, int maxWeighted)','文字列を X の数え方で指定の長さに収める。超えるときは末尾の「…」の1単位を残して文字単位で切り、「…」を付ける。収まるか null ならそのまま返す。',NULL,NULL,NULL,NULL,NULL);

INSERT INTO "TweetTextLimits"("method","signature","behavior") VALUES('exceedsLimit','boolean exceedsLimit(String text)','文字列の X 換算の長さが280を超えるときに true を返す。');
INSERT INTO "TweetTextLimits"("method","signature","behavior") VALUES('weightedLength','int weightedLength(String text)','文字列の X 換算の長さを返す。漢字・かな・ハングル・全角記号などの文字を2、それ以外を1として合計し、null は0とする。');

INSERT INTO "TwitterAuthService"("method","signature","behavior","利用_AppConfig","利用_AppLogging","生成_ClixService","生成_TwitterChromeLoginService","利用_TwitterCookies") VALUES('clearStoredSession','void clearStoredSession()','設定から X の Cookie を消して設定ファイルに保存する。保存に失敗しても警告を記録するだけ。',NULL,NULL,NULL,NULL,NULL);
INSERT INTO "TwitterAuthService"("method","signature","behavior","利用_AppConfig","利用_AppLogging","生成_ClixService","生成_TwitterChromeLoginService","利用_TwitterCookies") VALUES('ensureAuthenticated','ClixService.AuthResult ensureAuthenticated(LoginProgressListener listener)','保存済み Cookie があれば clix で確認し、通らなければ静かな取り直しを試して再確認する。それでも駄目なら、進捗の受け手が渡されていれば対話ログインに進み、無ければ「ログインしてください」の失敗結果を返す。',NULL,NULL,NULL,NULL,NULL);
INSERT INTO "TwitterAuthService"("method","signature","behavior","利用_AppConfig","利用_AppLogging","生成_ClixService","生成_TwitterChromeLoginService","利用_TwitterCookies") VALUES('hasStoredSession','boolean hasStoredSession()','設定に X の Cookie（手動ログイン情報）が保存されているかを返す。有効かどうかは確かめない。',NULL,NULL,NULL,NULL,NULL);
INSERT INTO "TwitterAuthService"("method","signature","behavior","利用_AppConfig","利用_AppLogging","生成_ClixService","生成_TwitterChromeLoginService","利用_TwitterCookies") VALUES('loginInteractive','ClixService.AuthResult loginInteractive(LoginProgressListener listener)','ログイン用ブラウザを開き、利用者が「ログイン完了」を押してブラウザを閉じるのを待ってから、プロファイルから Cookie を取り出して保存し、clix で認証を確かめる。確認に失敗すれば保存した Cookie を消して失敗を返し、キャンセルや例外も失敗結果として返す。最後に必ずブラウザを止める。',NULL,NULL,NULL,NULL,NULL);
INSERT INTO "TwitterAuthService"("method","signature","behavior","利用_AppConfig","利用_AppLogging","生成_ClixService","生成_TwitterChromeLoginService","利用_TwitterCookies") VALUES('logout','void logout()','保存済みの Cookie を消し、起動中のログイン用ブラウザがあれば止める。ブラウザのプロファイル自体は消さない。',NULL,NULL,NULL,NULL,NULL);
INSERT INTO "TwitterAuthService"("method","signature","behavior","利用_AppConfig","利用_AppLogging","生成_ClixService","生成_TwitterChromeLoginService","利用_TwitterCookies") VALUES('refreshSilently','boolean refreshSilently()','利用者の操作なしにブラウザプロファイルから Cookie を取り直し、auth_token と ct0 がそろえば保存して true を返す。取れなければ false を返す。',NULL,NULL,NULL,NULL,NULL);
INSERT INTO "TwitterAuthService"("method","signature","behavior","利用_AppConfig","利用_AppLogging","生成_ClixService","生成_TwitterChromeLoginService","利用_TwitterCookies") VALUES('saveCookies','void saveCookies(TwitterCookies cookies)','渡された Cookie 一式を設定に入れて設定ファイルに保存する。保存に失敗しても警告を記録するだけ。',NULL,NULL,NULL,NULL,NULL);
INSERT INTO "TwitterAuthService"("method","signature","behavior","利用_AppConfig","利用_AppLogging","生成_ClixService","生成_TwitterChromeLoginService","利用_TwitterCookies") VALUES('verifyStoredSession','ClixService.AuthResult verifyStoredSession()','保存済み Cookie が無ければ「未ログイン」の失敗を返し、あれば clix の認証状態確認の結果を返す。',NULL,NULL,NULL,NULL,NULL);

INSERT INTO "TwitterBrowserPostService"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_ChromiumBrowserLocator","利用_ChromiumCdpSession","生成_ChromiumDevToolsClient","利用_ChromiumLauncher","利用_ChromiumProfileLock","利用_ClixService","利用_ProcessUtils") VALUES('post','ClixService.PostResult post(String text, List<Path> imagePaths)','Chrome / Edge をログイン用プロファイルとデバッグポート付きで表示起動し、X の投稿画面で下書きを消して本文を1行ずつ Enter を挟んで入力し、画像を添付してアップロード完了を待つ。そのうえで利用者が「投稿」を押すのを最大5分待ち、通知が出るか入力欄が空になれば成功、ブラウザが閉じられたら取り消しとして返す。ブラウザが無い、投稿画面が開けない、添付・アップロードに失敗した場合は理由付きの失敗を返し、最後に必ずブラウザを止めてプロファイルのロック解除を待つ。',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);

INSERT INTO "TwitterChromeLoginService"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_ChromiumBrowserLocator","生成_ChromiumDevToolsClient","利用_ChromiumLauncher","利用_ChromiumProfileLock","利用_ProcessUtils","利用_TwitterCookies") VALUES('browserExecutable','Optional<Path> browserExecutable()','使える Chrome / Edge の実行ファイルを ChromiumBrowserLocator で探して返す。',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "TwitterChromeLoginService"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_ChromiumBrowserLocator","生成_ChromiumDevToolsClient","利用_ChromiumLauncher","利用_ChromiumProfileLock","利用_ProcessUtils","利用_TwitterCookies") VALUES('extractCookiesFromProfile','TwitterCookies extractCookiesFromProfile(BooleanSupplier cancelled)','ログイン用ブラウザが閉じていることを確かめ、プロファイルのロック解除を待ってから、画面外に置いたブラウザをデバッグポート付きで起動して x.com の Cookie を最大35秒くり返し読み、auth_token と ct0 がそろったら返す。失敗すれば1回だけ起動からやり直し、キャンセルされたときや最後まで取れないときは例外を投げる。',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "TwitterChromeLoginService"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_ChromiumBrowserLocator","生成_ChromiumDevToolsClient","利用_ChromiumLauncher","利用_ChromiumProfileLock","利用_ProcessUtils","利用_TwitterCookies") VALUES('openLoginBrowser','void openLoginBrowser()','前のブラウザを止めてから、ログイン用プロファイルで Chrome / Edge を普通に表示起動し、X のログイン画面を開く。ブラウザが見つからなければ例外を投げる。',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "TwitterChromeLoginService"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_ChromiumBrowserLocator","生成_ChromiumDevToolsClient","利用_ChromiumLauncher","利用_ChromiumProfileLock","利用_ProcessUtils","利用_TwitterCookies") VALUES('profileDirectory','Path profileDirectory()','X ログイン用ブラウザのプロファイルフォルダを返す。',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "TwitterChromeLoginService"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_ChromiumBrowserLocator","生成_ChromiumDevToolsClient","利用_ChromiumLauncher","利用_ChromiumProfileLock","利用_ProcessUtils","利用_TwitterCookies") VALUES('silentRefresh','Optional<TwitterCookies> silentRefresh()','ログイン用ブラウザが開いていなければプロファイルから Cookie を取り出し、auth_token と ct0 がそろっていれば返す。開いている、取れない、そろわない場合は空を返す。',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "TwitterChromeLoginService"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_ChromiumBrowserLocator","生成_ChromiumDevToolsClient","利用_ChromiumLauncher","利用_ChromiumProfileLock","利用_ProcessUtils","利用_TwitterCookies") VALUES('stopBrowser','void stopBrowser()','このサービスが起動したブラウザがあれば子プロセスごと強制終了し、参照を消す。',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO "TwitterChromeLoginService"("method","signature","behavior","利用_AppLogging","利用_AppPaths","利用_ChromiumBrowserLocator","生成_ChromiumDevToolsClient","利用_ChromiumLauncher","利用_ChromiumProfileLock","利用_ProcessUtils","利用_TwitterCookies") VALUES('waitForLoginBrowserClosed','void waitForLoginBrowserClosed(BooleanSupplier cancelled)','ログイン用ブラウザを利用者が閉じるまで最大2分待つ。強制終了すると Cookie がディスクに書かれないことがあるため自分では閉じない。閉じたら2秒待ってロック解除を待ち、キャンセルされたときや2分たっても開いているときは例外を投げる。',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);

INSERT INTO "TwitterCookies"("method","signature","behavior") VALUES('authToken','String authToken()','auth_token の値を返す。無ければ空文字を返す。');
INSERT INTO "TwitterCookies"("method","signature","behavior") VALUES('ct0','String ct0()','ct0 の値を返す。無ければ空文字を返す。');
INSERT INTO "TwitterCookies"("method","signature","behavior") VALUES('isComplete','boolean isComplete()','auth_token と ct0 がどちらも空でないかを返す。');
INSERT INTO "TwitterCookies"("method","signature","behavior") VALUES('toMutableMap','Map<String, String> toMutableMap()','Cookie の表を書き換え可能な新しい表に写して返す。設定へ保存するときに使う。');

INSERT INTO "TwitterLoginDialog"("method","signature","behavior","利用_AppConfig","利用_AppIcons","利用_ClixService","生成_TwitterAuthService") VALUES('showAndLogin','ClixService.AuthResult showAndLogin()','ログイン処理を裏スレッドで始めてからダイアログをモーダルで表示し、閉じられたら結果を返す。成功ならダイアログは自動で閉じ、失敗時はメッセージを表示したまま残る。キャンセルや×で閉じた場合は裏の処理に取り消しを伝え、「ログインをキャンセルしました」の失敗を返す。',NULL,NULL,NULL,NULL);

INSERT INTO "UiTheme"("method","signature","behavior","利用_AppConfig","利用_AppLogging","利用_ResultPalette") VALUES('accent','Color accent()','アプリ共通のアクセント色（青）を返す。',NULL,NULL,NULL);
INSERT INTO "UiTheme"("method","signature","behavior","利用_AppConfig","利用_AppLogging","利用_ResultPalette") VALUES('alternateRow','Color alternateRow()','表の1行おきに使う背景色を、現在のテーマに合わせて返す。',NULL,NULL,NULL);
INSERT INTO "UiTheme"("method","signature","behavior","利用_AppConfig","利用_AppLogging","利用_ResultPalette") VALUES('apply','void apply(String themeId, String preferredFontFamily, int preferredFontSize)','テーマとフォントを組み込み直したうえで、開いているすべての画面の見た目を更新する。設定画面でテーマや文字サイズを変えたときに使う。',NULL,NULL,NULL);
INSERT INTO "UiTheme"("method","signature","behavior","利用_AppConfig","利用_AppLogging","利用_ResultPalette") VALUES('discordAccent','Color discordAccent()','Discordのブランド色を返す。',NULL,NULL,NULL);
INSERT INTO "UiTheme"("method","signature","behavior","利用_AppConfig","利用_AppLogging","利用_ResultPalette") VALUES('fontSize','int fontSize()','現在の基準となるUIの文字サイズ（ポイント）を返す。',NULL,NULL,NULL);
INSERT INTO "UiTheme"("method","signature","behavior","利用_AppConfig","利用_AppLogging","利用_ResultPalette") VALUES('install','void install(String themeId, String preferredFontFamily, int preferredFontSize)','テーマ名が「light」ならライト、それ以外はダークのFlatLafを組み込む。フォントは希望のものが使えればそれ、なければ日本語向けの候補から、どれもなければメイリオを選び、文字サイズは設定の上下限に収める。組み込みに失敗したら警告を記録して標準の見た目のまま戻り、成功したら角丸や表の選択色などの既定値を設定する。',NULL,NULL,NULL);
INSERT INTO "UiTheme"("method","signature","behavior","利用_AppConfig","利用_AppLogging","利用_ResultPalette") VALUES('isDark','boolean isDark()','現在ダークテーマかどうかを返す。',NULL,NULL,NULL);
INSERT INTO "UiTheme"("method","signature","behavior","利用_AppConfig","利用_AppLogging","利用_ResultPalette") VALUES('japaneseCapableFontFamilies','List<String> japaneseCapableFontFamilies()','PCに入っているフォントのうち、ひらがな・カタカナ・漢字の見本文字をすべて表示できるものの名前一覧を返す。設定画面のフォント選択に使う。',NULL,NULL,NULL);
INSERT INTO "UiTheme"("method","signature","behavior","利用_AppConfig","利用_AppLogging","利用_ResultPalette") VALUES('mutedAgainst','Color mutedAgainst(Color foreground, Color background)','文字色を背景色に寄せた（55%残した）控えめな色を返す。表の選択行でも補足文字が読めるように使う。',NULL,NULL,NULL);
INSERT INTO "UiTheme"("method","signature","behavior","利用_AppConfig","利用_AppLogging","利用_ResultPalette") VALUES('mutedText','Color mutedText()','見出しの補足や撮影日時などに使う控えめな文字色を、現在のテーマに合わせて返す。',NULL,NULL,NULL);
INSERT INTO "UiTheme"("method","signature","behavior","利用_AppConfig","利用_AppLogging","利用_ResultPalette") VALUES('subtle','Color subtle()','区切り線やカードの縁に使う細い枠線の色を、現在のテーマに合わせて返す。',NULL,NULL,NULL);
INSERT INTO "UiTheme"("method","signature","behavior","利用_AppConfig","利用_AppLogging","利用_ResultPalette") VALUES('surface','Color surface()','ウィンドウ背景より一段明るい、カード用の面の色を現在のテーマに合わせて返す。',NULL,NULL,NULL);
INSERT INTO "UiTheme"("method","signature","behavior","利用_AppConfig","利用_AppLogging","利用_ResultPalette") VALUES('twitterAccent','Color twitterAccent()','X（Twitter）のブランド色の水色を返す。',NULL,NULL,NULL);

INSERT INTO "UpdateProgressDialog"("method","signature","behavior","利用_AppIcons") VALUES('updateProgress','void updateProgress(long downloadedBytes, long totalBytes)','画面スレッドに切り替えて進捗バーを更新する。総バイト数が正なら取得済みバイト数の割合を0〜100%で表示し、総バイト数が不明（0以下）なら動き続ける表示に戻す。どのスレッドから呼んでもよい。',NULL);
INSERT INTO "UpdateProgressDialog"("method","signature","behavior","利用_AppIcons") VALUES('updateStatus','void updateStatus(String message)','画面スレッドに切り替えて状態の文言を差し替える。どのスレッドから呼んでもよい。',NULL);

INSERT INTO "VersionComparator"("method","signature","behavior") VALUES('compare','int compare(String left, String right)','2つの版番号を数字の並びとして先頭から比べ、左が古ければ負、同じなら0、新しければ正を返す。v の有無やハイフン以降の付記は無視し、桁数が違えば足りない桁を0とみなす。');
INSERT INTO "VersionComparator"("method","signature","behavior") VALUES('isNewer','boolean isNewer(String candidate, String current)','候補の版番号が現在の版番号より新しいときだけ true を返す。');
INSERT INTO "VersionComparator"("method","signature","behavior") VALUES('normalize','String normalize(String version)','前後の空白を除き、先頭の v または V を外した版番号を返す。null は空文字にする。');

INSERT INTO "WebhookTableModel"("method","signature","behavior","生成_AppConfig") VALUES('addRow','void addRow()','名前と URL が空の行を末尾に足す。',NULL);
INSERT INTO "WebhookTableModel"("method","signature","behavior","生成_AppConfig") VALUES('getColumnCount','int getColumnCount()','列の数(名前・URL の2)を返す。',NULL);
INSERT INTO "WebhookTableModel"("method","signature","behavior","生成_AppConfig") VALUES('getColumnName','String getColumnName(int column)','列の見出しを返す。',NULL);
INSERT INTO "WebhookTableModel"("method","signature","behavior","生成_AppConfig") VALUES('getRowCount','int getRowCount()','行数を返す。',NULL);
INSERT INTO "WebhookTableModel"("method","signature","behavior","生成_AppConfig") VALUES('getRows','List<AppConfig.DiscordWebhookEntry> getRows()','今の行の一覧(リストの複製)を返す。',NULL);
INSERT INTO "WebhookTableModel"("method","signature","behavior","生成_AppConfig") VALUES('getValueAt','Object getValueAt(int rowIndex, int columnIndex)','行の名前か URL を返す。',NULL);
INSERT INTO "WebhookTableModel"("method","signature","behavior","生成_AppConfig") VALUES('isCellEditable','boolean isCellEditable(int rowIndex, int columnIndex)','すべてのセルを編集可能とする。',NULL);
INSERT INTO "WebhookTableModel"("method","signature","behavior","生成_AppConfig") VALUES('removeRow','void removeRow(int index)','指定した行を消す。',NULL);
INSERT INTO "WebhookTableModel"("method","signature","behavior","生成_AppConfig") VALUES('setRows','void setRows(List<AppConfig.DiscordWebhookEntry> rows)','表に出す Webhook の行を受け取り、表を描き直す。',NULL);
INSERT INTO "WebhookTableModel"("method","signature","behavior","生成_AppConfig") VALUES('setValueAt','void setValueAt(Object aValue, int rowIndex, int columnIndex)','行の名前か URL を書き換える(行は設定の複製なので、保存するまで設定は変わらない)。',NULL);

INSERT INTO "WindowsUpdateLauncher"("method","signature","behavior","利用_OsInfo") VALUES('launch','void launch(Path sourceDir, Path targetDir, Path executablePath, long parentPid, Path workDir)','更新元フォルダ・インストール先・本体 exe・今のプロセス ID・作業フォルダを引数に、PowerShell の更新スクリプトを一時ファイルに書いて起動する。スクリプトは元のプロセスの終了を待ち、app と runtime は robocopy の /MIR で更新元と同じ内容にし、それ以外は /E で消さずに上書きしてから本体を起動する。最後に作業フォルダとスクリプトを消す。Windows 以外ではスクリプトを書かずに IOException を投げ、起動したプロセスの結果は待たない。',NULL);

INSERT INTO "ZipExtractUtils"("method","signature","behavior") VALUES('extractRootDirectory','Path extractRootDirectory(Path zipFile, Path destinationDir)','zip の全ファイルを展開先フォルダへ書き出し、その中の beatoraja-screenshot-manager フォルダのパスを返す。展開先の外を指す項目があれば IOException を投げ、展開後にアプリ本体フォルダが無いときも IOException を投げる。');
