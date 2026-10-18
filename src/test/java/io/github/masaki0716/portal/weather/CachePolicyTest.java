package io.github.masaki0716.portal.weather;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.github.masaki0716.portal.support.WeatherApiStub;
import io.github.masaki0716.portal.support.WeatherIntegrationTest;

/** 受け入れ条件 4〜6（と 7 の古い予報の表示）。T0 = 2026-10-17 09:00 */
class CachePolicyTest extends WeatherIntegrationTest {

	@Test
	void 取得から30分以内は天気APIを呼び直さない() {
		WeatherApiStub.respondWithDaily(WeatherApiStub.DEFAULT_DAILY);
		topPageText();

		setClock("2026-10-17T09:30:00+09:00");
		String text = topPageText();

		assertThat(WeatherApiStub.callCount()).isEqualTo(1);
		assertThat(text).contains("09:00", "晴れ").doesNotContain("古い予報");
	}

	@Test
	void 取得から30分を過ぎたら呼び直す() {
		WeatherApiStub.respondWithDaily(WeatherApiStub.DEFAULT_DAILY);
		topPageText();

		setClock("2026-10-17T09:30:01+09:00");
		String text = topPageText();

		assertThat(WeatherApiStub.callCount()).isEqualTo(2);
		assertThat(text).contains("09:30").doesNotContain("古い予報");
	}

	@Test
	void 呼び直しに失敗したら古い予報をいつ時点かを添えて表示する() {
		WeatherApiStub.respondWithDaily(WeatherApiStub.DEFAULT_DAILY);
		topPageText();

		WeatherApiStub.respondWithStatus(500);
		setClock("2026-10-17T09:31:00+09:00");
		String text = topPageText();

		assertThat(WeatherApiStub.callCount()).isEqualTo(2);
		assertThat(text).contains("古い予報", "2026-10-17 09:00", "晴れ", "23℃")
			.doesNotContain("天気を取得できませんでした");
	}

	@Test
	void 取得から24時間までは古い予報を使う() {
		WeatherApiStub.respondWithDaily(WeatherApiStub.DEFAULT_DAILY);
		topPageText();

		WeatherApiStub.respondWithStatus(500);
		setClock("2026-10-18T09:00:00+09:00");
		String text = topPageText();

		// 時計が進んだので、今日は 2026-10-18
		assertThat(text).contains("古い予報", "2026-10-17 09:00");
		assertInOrder(text, "2026-10-18", "雨", "2026-10-19", "曇り");
	}

	@Test
	void 取得から24時間を過ぎた予報は使わない() {
		WeatherApiStub.respondWithDaily(WeatherApiStub.DEFAULT_DAILY);
		topPageText();

		WeatherApiStub.respondWithStatus(500);
		setClock("2026-10-18T09:00:01+09:00");
		String text = topPageText();

		assertThat(text).contains("天気を取得できませんでした").doesNotContain("古い予報");
	}

	@Test
	void 失敗のあと5分間は天気APIを呼び直さない() {
		WeatherApiStub.respondWithStatus(500);
		topPageText();
		assertThat(WeatherApiStub.callCount()).isEqualTo(1);

		WeatherApiStub.respondWithDaily(WeatherApiStub.DEFAULT_DAILY);
		setClock("2026-10-17T09:04:59+09:00");
		String text = topPageText();

		assertThat(WeatherApiStub.callCount()).isEqualTo(1);
		assertThat(text).contains("天気を取得できませんでした");
	}

	@Test
	void 失敗から5分たったら呼び直す() {
		WeatherApiStub.respondWithStatus(500);
		topPageText();

		WeatherApiStub.respondWithDaily(WeatherApiStub.DEFAULT_DAILY);
		setClock("2026-10-17T09:05:00+09:00");
		String text = topPageText();

		assertThat(WeatherApiStub.callCount()).isEqualTo(2);
		assertThat(text).contains("晴れ").doesNotContain("天気を取得できませんでした");
	}

	@Test
	void 失敗のあと5分間は古い予報があればそれを表示する() {
		WeatherApiStub.respondWithDaily(WeatherApiStub.DEFAULT_DAILY);
		topPageText();
		WeatherApiStub.respondWithStatus(500);
		setClock("2026-10-17T09:31:00+09:00");
		topPageText();

		setClock("2026-10-17T09:33:00+09:00");
		String text = topPageText();

		assertThat(WeatherApiStub.callCount()).isEqualTo(2);
		assertThat(text).contains("古い予報", "2026-10-17 09:00");
	}

}
