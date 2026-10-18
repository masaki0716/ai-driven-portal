package io.github.masaki0716.portal.clock;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

import io.github.masaki0716.portal.support.WeatherApiStub;
import io.github.masaki0716.portal.support.WeatherIntegrationTest;

/** 受け入れ条件 14: テスト用の設定のときの時刻を変える口と保持を消す口 */
class TestClockApiTest extends WeatherIntegrationTest {

	@Test
	void 時刻を変えると本文なしの204を返しアプリの時刻がその時刻になる() {
		HttpResponse<String> response = postJson("/api/test/clock",
				"{\"currentTime\": \"2026-10-17T21:45:00+09:00\"}");

		assertThat(response.statusCode()).isEqualTo(204);
		assertThat(response.body()).isEmpty();

		WeatherApiStub.respondWithDaily(WeatherApiStub.DEFAULT_DAILY);
		assertThat(topPageText()).contains("21:45");
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"{\"currentTime\": \"2026-10-17T00:00:00\"}",
			"{\"currentTime\": \"2026-10-17\"}",
			"{\"currentTime\": \"not a time\"}",
			"{\"currentTime\": 1760000000}",
			"{}",
			"not json" })
	void 形式が不正なら400を返す(String body) {
		assertThat(postJson("/api/test/clock", body).statusCode()).isEqualTo(400);
	}

	@Test
	void 保持を消すと本文なしの204を返し次は天気APIを呼び直す() {
		WeatherApiStub.respondWithDaily(WeatherApiStub.DEFAULT_DAILY);
		topPageText();

		HttpResponse<String> response = delete("/api/test/weather-cache");
		topPageText();

		assertThat(response.statusCode()).isEqualTo(204);
		assertThat(response.body()).isEmpty();
		assertThat(WeatherApiStub.callCount()).isEqualTo(2);
	}

	@Test
	void 保持を消すと失敗の記録も消える() {
		WeatherApiStub.respondWithStatus(500);
		topPageText();

		clearWeatherCache();
		WeatherApiStub.respondWithDaily(WeatherApiStub.DEFAULT_DAILY);
		String text = topPageText();

		assertThat(WeatherApiStub.callCount()).isEqualTo(2);
		assertThat(text).contains("晴れ");
	}

	@Test
	void 保持を消しても時計は戻さない() {
		setClock("2026-10-17T21:45:00+09:00");

		clearWeatherCache();
		WeatherApiStub.respondWithDaily(WeatherApiStub.DEFAULT_DAILY);

		assertThat(topPageText()).contains("21:45");
	}

}
