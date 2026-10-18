package io.github.masaki0716.portal.weather;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.github.masaki0716.portal.support.WeatherApiStub;
import io.github.masaki0716.portal.support.WeatherIntegrationTest;

/** 受け入れ条件 1・2: 今日・明日の予報と、いつ時点か・取得元の表示 */
class TopPageTest extends WeatherIntegrationTest {

	@Test
	void 地点名と今日明日の天気気温降水確率を表示する() {
		setClock("2026-10-17T09:15:00+09:00");
		WeatherApiStub.respondWithDaily(WeatherApiStub.DEFAULT_DAILY);

		String text = topPageText();

		assertThat(text).contains("東京");
		assertInOrder(text,
				"2026-10-17", "晴れ", "23℃", "14℃", "10%",
				"2026-10-18", "雨", "18℃", "-3℃", "80%");
		assertThat(text).doesNotContain("古い予報", "天気を取得できませんでした");
	}

	@Test
	void 取得時刻と取得元を表示する() {
		setClock("2026-10-17T09:15:00+09:00");
		WeatherApiStub.respondWithDaily(WeatherApiStub.DEFAULT_DAILY);

		String text = topPageText();

		assertThat(text).contains("09:15", "Weather data by Open-Meteo.com");
	}

	@Test
	void 今日と明日は日本時間の日付で決める() {
		// UTC では 2026-10-16 だが、日本時間では 2026-10-17 00:30
		setClock("2026-10-16T15:30:00Z");
		WeatherApiStub.respondWithDaily("""
				"time": ["2026-10-16", "2026-10-17", "2026-10-18"],
				"weather_code": [3, 1, 61],
				"temperature_2m_max": [20.0, 22.5, 18.4],
				"temperature_2m_min": [10.0, 14.4, -2.5],
				"precipitation_probability_max": [50, 10, 80]
				""");

		String text = topPageText();

		assertInOrder(text, "2026-10-17", "晴れ", "2026-10-18", "雨");
		assertThat(text).doesNotContain("2026-10-16", "曇り");
		assertThat(text).contains("00:30");
	}

	@Test
	void 約束どおりのクエリで天気APIを呼ぶ() {
		WeatherApiStub.respondWithDaily(WeatherApiStub.DEFAULT_DAILY);

		topPageText();

		WeatherApiStub.verify(getRequestedFor(urlPathEqualTo(WeatherApiStub.PATH))
			.withQueryParam("latitude", equalTo("35.6895"))
			.withQueryParam("longitude", equalTo("139.6917"))
			.withQueryParam("daily",
					equalTo("weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max"))
			.withQueryParam("timezone", equalTo("Asia/Tokyo")));
	}

}
