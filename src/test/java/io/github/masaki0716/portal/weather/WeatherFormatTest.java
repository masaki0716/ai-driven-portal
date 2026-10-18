package io.github.masaki0716.portal.weather;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** 気温・降水確率の表示の形 */
class WeatherFormatTest {

	@ParameterizedTest
	@CsvSource({
			"22.5, 23℃",
			"22.4, 22℃",
			"-2.5, -3℃",
			"-2.4, -2℃",
			"-0.4, 0℃",
			"0.0, 0℃",
			"35, 35℃" })
	void 気温は0から遠い方へ四捨五入して度の記号を付ける(double value, String expected) {
		assertThat(WeatherFormat.temperature(value)).isEqualTo(expected);
	}

	@ParameterizedTest
	@CsvSource({ "0, 0%", "80, 80%", "100, 100%", "12.5, 13%" })
	void 降水確率は整数に空白なしでパーセントを付ける(double value, String expected) {
		assertThat(WeatherFormat.precipitation(value)).isEqualTo(expected);
	}

	@Test
	void 無い値はダッシュにする() {
		assertThat(WeatherFormat.temperature(null)).isEqualTo("—");
		assertThat(WeatherFormat.precipitation(null)).isEqualTo("—");
	}

}
