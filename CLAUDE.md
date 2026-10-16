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
- このリポジトリは public。仕様リポジトリの中身（特に案件の情報）をコード・コミット・PR に書き写さない

## 実装のルール
- テストを先に書く（ダイヤモンド型: 結合テストを厚く。外部 API は WireMock、DB は Testcontainers）
- 作業はブランチ（`feat/…`・`fix/…`・`chore/…`）で行い、PR を出して人間がマージする
