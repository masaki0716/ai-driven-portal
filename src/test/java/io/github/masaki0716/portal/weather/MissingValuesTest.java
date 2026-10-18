package io.github.masaki0716.portal.weather;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.github.masaki0716.portal.support.WeatherApiStub;
import io.github.masaki0716.portal.support.WeatherIntegrationTest;

/** 受け入れ条件 3: 無い項目は「—」、ほかは通常どおり */
class MissingValuesTest extends WeatherIntegrationTest {

	@Test
	void 値がnullの項目はダッシュにする() {
		WeatherApiStub.respondWithDaily("""
				"time": ["2026-10-17", "2026-10-18"],
				"weather_code": [1, 61],
				"temperature_2m_max": [null, 18.4],
				"temperature_2m_min": [14.4, null],
				"precipitation_probability_max": [null, 80]
				""");

		String text = topPageText();

		assertInOrder(text,
				"2026-10-17", "晴れ", "—", "14℃", "—",
				"2026-10-18", "雨", "18℃", "—", "80%");
		assertThat(text).doesNotContain("天気を取得できませんでした");
	}

	@Test
	void キーごと無い項目はダッシュにする() {
		WeatherApiStub.respondWithDaily("""
				"time": ["2026-10-17", "2026-10-18"],
				"weather_code": [1, 61],
				"temperature_2m_min": [14.4, -2.5]
				""");

		String text = topPageText();

		assertInOrder(text,
				"2026-10-17", "晴れ", "—", "14℃", "—",
				"2026-10-18", "雨", "—", "-3℃", "—");
		assertThat(text).doesNotContain("天気を取得できませんでした");
	}

	@Test
	void 配列がtimeより短いときは足りない日をダッシュにする() {
		WeatherApiStub.respondWithDaily("""
				"time": ["2026-10-17", "2026-10-18"],
				"weather_code": [1, 61],
				"temperature_2m_max": [22.5],
				"temperature_2m_min": [14.4, -2.5],
				"precipitation_probability_max": [10]
				""");

		String text = topPageText();

		assertInOrder(text,
				"2026-10-17", "晴れ", "23℃", "14℃", "10%",
				"2026-10-18", "雨", "—", "-3℃", "—");
	}

	@Test
	void 変換表に無い天気コードは天気だけダッシュにする() {
		WeatherApiStub.respondWithDaily("""
				"time": ["2026-10-17", "2026-10-18"],
				"weather_code": [4, 61],
				"temperature_2m_max": [22.5, 18.4],
				"temperature_2m_min": [14.4, -2.5],
				"precipitation_probability_max": [10, 80]
				""");

		String text = topPageText();

		assertInOrder(text,
				"2026-10-17", "—", "23℃", "14℃", "10%",
				"2026-10-18", "雨", "18℃", "-3℃", "80%");
		assertThat(text).doesNotContain("天気を取得できませんでした");
	}

}
