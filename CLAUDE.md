# beatoraja-result-manager

beatoraja / BMZ Player のリザルトスクショを一覧・管理し、X(Twitter)・Discord に投稿する Java Swing アプリ。

## 設計 DB(必ず守る)

- クラスの依存関係と各クラスの実装内容は設計 DB(`design/design.db`)に記録する。ユーザーは実装内容をこの DB で読む。
- コードを変えたら同じ作業の中で設計 DB も更新する(クラス・メソッド・依存・テストケース・リファクタリング候補)。処理内容の文章が実装と合っているか見直す。
- 作業の最後に `python tools/designdb.py audit` を実行し「問題なし」になるまで直す。

## 設計 DB の形

- クラス1つ = クラス名と同じ名前の表1つ。行がメソッド(`method`、`signature`、`behavior`)。対象はトップレベルの型の private でないメソッド。オーバーロードは1行にまとめる。
- 依存 = 依存元の表にある外部キーの列(`継承_X`、`利用_X`、`生成_X`。値は入れない。線として見るためのもの)。
- 補助の表: `_class`(責務・層・種類・状態)、`_dependency`(依存の理由)、`_test`(テストケース)、`_refactor`(リファクタリング候補。優先度 高/中/低、状態 候補/対応中/完了/見送り)。
- 読むときはビュー `v_class_detail`、`v_method`、`v_dependency`、`v_test`、`v_refactor`、`v_progress` を使う。
- 層 = `com.beatoraja.screenshot` 以下のパッケージ名。種類「アダプタ」= 外部(X/ブラウザ、Discord、GitHub、OS プロセス)と直接やりとりするクラス。model / parser / player / table / config / db の層はアダプタに依存しない。
- 表や列は手で作らず、必ず `tools/designdb.py apply` で更新する(毎回 DB 全体を作り直す)。

## コマンド

- 設計 DB: `python tools/designdb.py apply <json|-> | status <クラス>... <状態> | test-status <クラス> <テスト名|*> <状態> | refactor-status <番号> <状態> | audit | scaffold | dump | restore`
  - `apply` の JSON: `{"class":[…],"method":[…],"dependency":[…],"test_case":[…],"refactor":[…]}`。同じキーの行は上書き、`remove_<種類>` で削除。`refactor` は `id` を省くと採番する。
  - `scaffold`: コードにあって DB に無いクラス・メソッド・依存・テストを、文章を `TODO` にした JSON で出す。埋めてから `apply` する。
  - `audit`: DB とコードのずれ(クラス、モジュール、メソッド、依存、テスト名、層の向き)を表示する。
- DB を更新すると同じ内容のテキストダンプ `design/design.sql` が自動で書き出される(差分はこちらで見る)。DB を直接編集した場合は `dump`、`design.sql` から作り直すときは `restore`。
- ビルド・テスト: `.\gradlew.bat test`
