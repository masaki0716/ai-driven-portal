package io.github.masaki0716.portal.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;

import io.github.masaki0716.portal.support.WeatherApiStub;
import io.github.masaki0716.portal.support.WeatherIntegrationTest;

/** 受け入れ条件 13: ページ全体を共有キャッシュに保持させない */
class CacheControlTest extends WeatherIntegrationTest {

	@Test
	void トップページは共有キャッシュに保持させない() {
		WeatherApiStub.respondWithDaily(WeatherApiStub.DEFAULT_DAILY);

		HttpResponse<String> response = get("/");

		assertThat(response.headers().firstValue("Cache-Control")).hasValueSatisfying(
				value -> assertThat(value).containsAnyOf("private", "no-store"));
	}

	@Test
	void 天気APIが失敗したときも共有キャッシュに保持させない() {
		WeatherApiStub.respondWithStatus(500);

		HttpResponse<String> response = get("/");

		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.headers().firstValue("Cache-Control")).hasValueSatisfying(
				value -> assertThat(value).containsAnyOf("private", "no-store"));
	}

}
