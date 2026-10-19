# CI では ./mvnw -B package -DskipTests のあとに docker build . を実行する。jar はビルド済みのものを入れるだけ
# 設定はイメージに焼き込まず、起動時に環境変数で渡す（README の表）
FROM eclipse-temurin:21-jre
RUN useradd --system --no-create-home portal
WORKDIR /app
COPY target/ai-driven-portal-0.0.1-SNAPSHOT.jar app.jar
USER portal
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
