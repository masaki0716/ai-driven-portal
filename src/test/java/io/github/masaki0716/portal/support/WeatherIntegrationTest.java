package io.github.masaki0716.portal.support;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;

/**
 * テスト時計を有効にした結合テスト。毎回、時計を T0 に合わせ、保持している予報と失敗の記録を消す
 * （受け入れテストと同じ外側の口を使い、テストの順番に左右されないようにする）。
 */
public abstract class WeatherIntegrationTest extends PortalIntegrationTest {

	/** 2026-10-17 09:00（日本時間） */
	protected static final String T0 = "2026-10-17T09:00:00+09:00";

	@BeforeEach
	void resetClockAndCache() {
		setClock(T0);
		clearWeatherCache();
	}

	protected void setClock(String currentTime) {
		assertThat(postJson("/api/test/clock", "{\"currentTime\": \"" + currentTime + "\"}").statusCode())
			.isEqualTo(204);
	}

	protected void clearWeatherCache() {
		assertThat(delete("/api/test/weather-cache").statusCode()).isEqualTo(204);
	}

}
