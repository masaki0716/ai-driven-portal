package io.github.masaki0716.portal.weather;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import io.github.masaki0716.portal.support.WeatherApiStub;
import io.github.masaki0716.portal.support.WeatherIntegrationTest;

/** 受け入れ条件 7〜9: 失敗してもページは返り、ERROR ログが残る */
@ExtendWith(OutputCaptureExtension.class)
class FailureTest extends WeatherIntegrationTest {

	@Test
	void エラーを返したときは取得できなかった旨を表示しステータスコードをログに残す(CapturedOutput output) {
		WeatherApiStub.respondWithStatus(500);

		String text = topPageText();

		assertThat(text).contains("天気を取得できませんでした", "東京");
		assertFailureLogged(output, "500");
	}

	@Test
	void 応答しないときは7秒以内にページを返し例外のクラス名をログに残す(CapturedOutput output) {
		WeatherApiStub.respondAfter(6_000);

		long started = System.nanoTime();
		String text = topPageText();
		Duration elapsed = Duration.ofNanos(System.nanoTime() - started);

		assertThat(elapsed).isLessThan(Duration.ofSeconds(7));
		assertThat(text).contains("天気を取得できませんでした");
		assertFailureLogged(output, "Timeout");
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"not json",
			"[1, 2, 3]",
			"{\"latitude\": 35.7}",
			"{\"daily\": {\"weather_code\": [1, 61]}}",
			"{\"daily\": {\"time\": \"2026-10-17\", \"weather_code\": [1, 61]}}",
			"{\"daily\": {\"time\": [\"2026-10-17\", \"2026-10-18\"], \"weather_code\": [1, 61], \"temperature_2m_max\": \"hot\"}}",
			"{\"daily\": {\"time\": [\"2026-10-17\", \"2026-10-18\"], \"weather_code\": [1, 61], \"temperature_2m_max\": [\"22.5\", 18.4]}}",
			"{\"daily\": {\"time\": [\"2026-10-17\", \"2026-10-18\"], \"weather_code\": [\"晴れ\", 61]}}" })
	void 想定外の形式のときは取得できなかった旨を表示しログに残す(String body, CapturedOutput output) {
		WeatherApiStub.respondWithBody(body);

		String text = topPageText();

		assertThat(text).contains("天気を取得できませんでした");
		assertFailureLogged(output, "Exception");
	}

	@ParameterizedTest
	@ValueSource(strings = {
			// 今日の weather_code が null
			"\"time\": [\"2026-10-17\", \"2026-10-18\"], \"weather_code\": [null, 61]",
			// 明日の weather_code が足りない
			"\"time\": [\"2026-10-17\", \"2026-10-18\"], \"weather_code\": [1]",
			// weather_code がキーごと無い
			"\"time\": [\"2026-10-17\", \"2026-10-18\"], \"temperature_2m_max\": [22.5, 18.4]",
			// 今日の日付が time に無い
			"\"time\": [\"2026-10-18\", \"2026-10-19\"], \"weather_code\": [61, 3]" })
	void 今日か明日の天気そのものが無いときは失敗として扱う(String daily, CapturedOutput output) {
		WeatherApiStub.respondWithDaily(daily);

		String text = topPageText();

		assertThat(text).contains("天気を取得できませんでした");
		assertFailureLogged(output, "Exception");
	}

	@Test
	void 天気そのものが無い応答は保持せず失敗として記録する() {
		WeatherApiStub.respondWithDaily("\"time\": [\"2026-10-17\", \"2026-10-18\"], \"weather_code\": [null, 61]");
		topPageText();

		// 失敗の記録があるので5分間は呼ばない。保持していないので古い予報も無い
		WeatherApiStub.respondWithDaily(WeatherApiStub.DEFAULT_DAILY);
		setClock("2026-10-17T09:01:00+09:00");
		String text = topPageText();

		assertThat(WeatherApiStub.callCount()).isEqualTo(1);
		assertThat(text).contains("天気を取得できませんでした").doesNotContain("古い予報");
	}

	private static void assertFailureLogged(CapturedOutput output, String cause) {
		assertThat(Arrays.stream(output.getOut().split("\\R")))
			.as("ERROR と「weather api call failed」と原因（%s）を含む行", cause)
			.anyMatch(line -> line.contains("ERROR") && line.contains("weather api call failed")
					&& line.contains(cause));
	}

}
