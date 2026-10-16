# ai-driven-portal

AI 駆動開発の練習として作るポータルサイト。最初の機能は天気予報、次に認証とユーザー名、メモの投稿と閲覧。

## 技術
- Java 21 / Spring Boot / Maven（Maven Wrapper 同梱）
- デプロイ先: Cloud Run、DB: Neon（PostgreSQL）
- CI: GitHub Actions
- テスト: JUnit 5・Testcontainers・WireMock。受け入れテストは別リポジトリ（private）から HTTP で外側だけを叩く

## 動かし方
JDK 21 を入れて `JAVA_HOME` を設定したうえで:

```
./mvnw test             # テスト
./mvnw spring-boot:run  # 起動（http://localhost:8080）
```

Windows の PowerShell では `.\mvnw.cmd test` のように実行する。

## 開発の進め方
- 実装は Claude Code、受け入れテストは Antigravity が担当する。実装する AI は受け入れテストを見ない
- 仕様（受け入れ条件）と意思決定記録（ADR）は別のリポジトリ（private）で管理している
- main には直接 push しない。ブランチで作業し、CI が通った PR だけをマージする
