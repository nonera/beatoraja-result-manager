# beatoraja Screenshot Manager

beatoraja の F6 スクショを一覧表示し、Twitter（[clix](https://github.com/spideystreet/clix) 経由）と Discord（Webhook）へ投稿する Windows 向けデスクトップアプリです。

## 利用者向け（exe だけ使う場合）

1. `dist/beatoraja-screenshot-manager.zip` を解凍
2. `beatoraja-screenshot-manager.exe` を実行
3. 初回ウィザードで beatoraja フォルダを選択
4. 必要なら Discord Webhook URL を入力
5. Twitter 利用時は設定画面または初回ウィザードから **「Twitter にログイン」** を実行
6. 表示された Chrome / Edge で X にログイン（初回のみ。以後は自動でセッションを再利用）

**Twitter 認証について:**

Windows では clix 単体の Cookie 自動取得（Chrome 127+）が動かないため、このアプリは **Chrome DevTools Protocol** で X 向け Cookie をすべて取得します。利用者が DevTools を触る必要はありません。

1. 「Twitter にログイン」を押す
2. 通常の Chrome / Edge が起動（自動操作モードではない）
3. X へのログインを完了し、**「ログイン完了」** を押す
4. **開いたブラウザを閉じる**（Cookie を保存するため）
5. アプリが Cookie を取得して保存（以後は自動再利用）

セッション切れ時は「Twitter にログイン」を再度実行してください。

### 操作

- **ギャラリー** タブ: スクショ選択・投稿
- **データベース** タブ: 全スクショの一覧（日付・タイトル・記号・レベル・ランク・ランプ・投稿表記・状態）
  - **記号** … ★ / sl / δ など（表記号のみ）
  - **レベル** … 12 / ??? など。表外譜面は記号が空で LEVEL の数字のみ
  - **投稿表記** … 編集可（例: `★3/sl3`）。セル直接編集または「候補から投稿表記を選択」
- 左の一覧からスクショを選択（Ctrl / Shift で複数選択）
- **Twitter に投稿** … 最大4枚
- **Discord に送信** … 最大10枚（手動）

### Discord 自動投稿（v1.2.0+）

設定画面の **Discord 自動投稿** で有効化できます。

- フォルダ監視で検出した **リザルト（Result）** の未投稿スクショだけをキューに追加
- 設定した枚数（1〜10、デフォルト 4）に達したら Webhook へ自動送信
- プレイ中・選曲など **リザルト以外のスクショはキューに入りません**
- キューはメモリ上のみ（アプリ再起動で未送信分は消えます。未投稿のままなら再度検出時にキュー追加されます）
- 送信先 Webhook を指定しない場合は、先頭の有効 Webhook を使用

設定は `%APPDATA%\beatoraja-screenshot-manager\` に自動保存されます。

### ログ

不具合調査用のログは次に保存されます。

```
%APPDATA%\beatoraja-screenshot-manager\logs\app.log
```

設定画面の **ログフォルダを開く** からフォルダを開けます。Webhook URL などの秘密情報はログ上でマスクされます。

## 開発者向け

### 必要環境

- JDK 17+
- Gradle（wrapper 同梱）

### リポジトリの clone

```powershell
git clone git@github.com:nonera/beatoraja-result-manager.git
```

### 開発実行

```powershell
./gradlew run
```

Twitter 投稿には **clix** が必要です。初回のみ以下を実行してください:

```powershell
./scripts/build-clix.ps1
```

`tools/clix.exe` が生成されます。Python 3.11+ がある環境では `pip install clix0` でも可（PATH に `clix` が通っている場合）。

### リリース zip 生成

```powershell
./scripts/build-release.ps1
```

- Java アプリを `jpackage` で exe 化（JRE 同梱）
- Python 3.11+ がある場合、`clix0` を PyInstaller で `tools/clix.exe` に同梱
- 出力: `dist/beatoraja-screenshot-manager.zip`

Python がない環境では `tools/clix.exe` を手動配置してください。

## beatoraja 連携

beatoraja の F6 スクショは、beatoraja 起動フォルダ配下の `screenshot/` に保存されます。

```
screenshot/20250908_141530_LEVEL12 曲名 CLEAR AAA.png
```

ファイル名から曲名・クリアランプなどを読み取り、投稿文の初期値を生成します。

### 難易度表連携（BeMusicSeeker / beatoraja）

beatoraja の `table/` 配下にある `.bmt` を読み込み、曲名から所属難易度表を推定します。

- 複数表に属する譜面は **候補一覧** として DB に保存
- データベースタブで `★3/sl3` のような **投稿表記** を選択・編集可能
- 設定の **beatoraja フォルダ** で `.bmt` の読み込み元を指定

## 注意事項

- Twitter 投稿は非公式の clix 経由です。X 側の仕様変更で動かなくなる可能性があります
- Discord Webhook URL は秘密情報です。他人に共有しないでください
- 自動投稿の多用はアカウント制限の原因になる場合があります
