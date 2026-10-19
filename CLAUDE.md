# ai-driven-portal — CLAUDE.md

AI 駆動開発の練習で作るポータルサイト。Claude Code はここで実装を担当する。

@AGENTS.md

## よく使うコマンド
- テスト: `./mvnw test`（PowerShell では `.\mvnw.cmd test`）
- 起動: `./mvnw spring-boot:run`
- PATH の `java` が古いことがある。`JAVA_HOME` が JDK 21 を指しているかを先に確かめる

## 仕様と決定の置き場所（別リポジトリ・private）
- 受け入れ条件: `C:\Users\masas\knowledgeBaseSystem\AI-Driven-Development\specs\NN-機能名\受け入れ条件.md`
- ADR: `C:\Users\masas\knowledgeBaseSystem\AI-Driven-Development\decisions\`
- 実装を始める前に、該当機能の受け入れ条件を読む。曖昧な所は推測で埋めず質問する
- このフォルダの外で読めるのは、仕様リポジトリの `specs/` だけ（`.claude/settings.json` の `blockReadsOutsideWorkingDirectories` でフォルダの外を止め、`specs/` だけをユーザー設定 `~/.claude/settings.json` の `additionalDirectories` で足している。リポジトリの設定で足したフォルダはこの制限の例外にならない仕様のため）。ADR は人間に内容を聞く
- このリポジトリは public。仕様リポジトリの中身（特に案件の情報）をコード・コミット・PR に書き写さない

## 実装のルール
- テストを先に書く（ダイヤモンド型: 結合テストを厚く。外部 API は WireMock、DB は Testcontainers）
- 作業はブランチ（`feat/…`・`fix/…`・`chore/…`）で行い、PR を出して人間がマージする

## テストの書き方の落とし穴
- テストクラス名は `*Test` にする。今の pom は failsafe を入れていないので、`*IT` は `./mvnw test` でも CI でも実行されない
- 結合テストは Testcontainers を使うので、Docker を起動しておく（pre-push の `verify` も同じ）
- ポートは固定しない。アプリは `RANDOM_PORT`、WireMock は `dynamicPort()`、DB は Testcontainers に任せる

## 手元の環境
- 手元の Docker には、このリポジトリと関係ない既存のコンテナ（`postgres` など）がある。止めない・消さない・使わない
- WSL 側でもコンテナが動いていることがある。固定のポートを使うときは、空いているか先に確かめる
- jar を手で動かして確かめるときは、専用の PostgreSQL コンテナを別ポート（例: `127.0.0.1:55432`）で立て、終わったらアプリを止めてそのコンテナだけを消す

## スタックの落とし穴（Spring Boot 4 系）
- Jackson は 3 系で、パッケージは `tools.jackson.*`（`com.fasterxml.jackson.*` ではない）。`JsonNode` は `isString()`・`stringValue()` を使う
- Testcontainers は 2 系。PostgreSQL は `org.testcontainers.postgresql.PostgreSQLContainer`、依存は `testcontainers-postgresql`
- WireMock は Boot の管理外なので、pom で版を明記する（Jetty・Jackson とぶつからないよう `wiremock-standalone` を使う）
- テストのメソッド名に `℃`・`%` などの記号は使えない（Java の識別子にならない）
- pre-push の `verify` の出力にスタックトレースが出るのは、失敗系のテストが意図して出す ERROR ログ。ビルドの成否は終了コードで判断する
