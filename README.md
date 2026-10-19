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
./mvnw test             # テスト（Testcontainers を使うので Docker を起動しておく）
./mvnw spring-boot:run  # 起動（http://localhost:8080）
```

Windows の PowerShell では `.\mvnw.cmd test` のように実行する。

起動には天気の地点・招待コード・PostgreSQL の接続先を環境変数で渡す（地点と招待コードは設定ファイルに既定値が無く、無いと起動時に止まる）。

```
export PORTAL_WEATHER_LOCATION_NAME=東京
export PORTAL_WEATHER_LOCATION_LATITUDE=35.6895
export PORTAL_WEATHER_LOCATION_LONGITUDE=139.6917
export PORTAL_AUTH_INVITE_CODE=...
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/portal
export SPRING_DATASOURCE_USERNAME=...
export SPRING_DATASOURCE_PASSWORD=...
./mvnw spring-boot:run
```

| 環境変数 | 中身 |
|---|---|
| `PORTAL_WEATHER_LOCATION_NAME` / `_LATITUDE` / `_LONGITUDE` | 天気の地点（必須） |
| `PORTAL_WEATHER_API_BASE_URL` | 天気 API の接続先（既定 `https://api.open-meteo.com`） |
| `PORTAL_AUTH_INVITE_CODE` | 会員登録の招待コード（必須。空白だけも不可）。リポジトリには書かない |
| `PORTAL_TEST_CLOCK_ENABLED` | `true` のときだけ `POST /api/test/clock`・`DELETE /api/test/weather-cache`・`DELETE /api/test/login-attempts` が効く（受け入れテスト用。本番では指定しない） |
| `SPRING_DATASOURCE_URL` / `_USERNAME` / `_PASSWORD` | PostgreSQL の接続先 |

## clone したら最初に1回
git フックを有効にする（フックは `.githooks/` にあり、git の設定は clone ごとに必要）。

```
git config core.hooksPath .githooks
```

- pre-commit: テストを弱める変更（テストファイルの削除・`@Disabled` の追加・assert の減少）を止める
- pre-push: main への直接 push を止め、CI と同じ `./mvnw verify` を回す

## 開発の進め方
- 実装と受け入れテストは、それぞれ別の Claude Code セッションが担当する。実装する AI は受け入れテストを見ない
- 仕様（受け入れ条件）と意思決定記録（ADR）は別のリポジトリ（private）で管理している
- main には直接 push しない。ブランチで作業し、CI が通った PR だけをマージする
