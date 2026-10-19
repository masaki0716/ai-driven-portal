package io.github.masaki0716.portal.weather;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.Test;

/** 受け入れ条件 4・7: 保持している予報は、新しくても古くても表示する時点の日付で探し直す */
class WeatherPanelTest {

	private static final Instant FETCHED_AT = Instant.parse("2026-10-17T00:00:00Z");

	private static final Forecast TWO_DAYS = new Forecast(Map.of(
			LocalDate.of(2026, 10, 17), new DailyWeather(1, 22.5, 14.4, 10.0),
			LocalDate.of(2026, 10, 18), new DailyWeather(61, 18.4, -2.5, 80.0)));

	@ParameterizedTest
	@EnumSource(value = WeatherPanel.Status.class, names = { "FRESH", "STALE" })
	void 保持している予報に今日も明日も無ければ使わない(WeatherPanel.Status status) {
		assertThat(WeatherPanel.stored(status, "東京", FETCHED_AT, TWO_DAYS, LocalDate.of(2026, 10, 19))).isEmpty();
	}

	@ParameterizedTest
	@EnumSource(value = WeatherPanel.Status.class, names = { "FRESH", "STALE" })
	void 保持している予報に今日だけ無ければ今日の欄をダッシュにする(WeatherPanel.Status status) {
		WeatherPanel panel = WeatherPanel.stored(status, "東京", FETCHED_AT, TWO_DAYS, LocalDate.of(2026, 10, 16))
			.orElseThrow();

		assertThat(panel.days()).containsExactly(
				new WeatherPanel.Day("今日", "2026-10-16", "—", "—", "—", "—"),
				new WeatherPanel.Day("明日", "2026-10-17", "晴れ", "23℃", "14℃", "10%"));
	}

	@Test
	void 新しい予報の取得時刻は時刻だけで出す() {
		WeatherPanel panel = WeatherPanel.stored(WeatherPanel.Status.FRESH, "東京", FETCHED_AT, TWO_DAYS,
				LocalDate.of(2026, 10, 17)).orElseThrow();

		assertThat(panel.stale()).isFalse();
		assertThat(panel.fetchedAt()).isEqualTo("09:00");
	}

	@Test
	void 古い予報の取得時刻は日付つきで出す() {
		WeatherPanel panel = WeatherPanel.stored(WeatherPanel.Status.STALE, "東京", FETCHED_AT, TWO_DAYS,
				LocalDate.of(2026, 10, 17)).orElseThrow();

		assertThat(panel.stale()).isTrue();
		assertThat(panel.fetchedAt()).isEqualTo("2026-10-17 09:00");
	}

}
