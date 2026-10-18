package io.github.masaki0716.portal.weather;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/** 天気コードの日本語の変換表（約束の表のとおり） */
class WeatherCodeTableTest {

	@ParameterizedTest
	@CsvSource({
			"0, 晴れ", "1, 晴れ",
			"2, 晴れ時々曇り",
			"3, 曇り",
			"45, 霧", "48, 霧",
			"51, 霧雨", "57, 霧雨",
			"61, 雨", "67, 雨",
			"71, 雪", "77, 雪",
			"80, にわか雨", "82, にわか雨",
			"85, にわか雪", "86, にわか雪",
			"95, 雷雨", "99, 雷雨" })
	void 変換表のコードは日本語にする(int code, String expected) {
		assertThat(WeatherCodeTable.describe(code)).isEqualTo(expected);
	}

	@ParameterizedTest
	@ValueSource(ints = { -1, 4, 44, 46, 50, 58, 60, 68, 70, 78, 79, 83, 84, 87, 94, 100 })
	void 変換表に無いコードはダッシュにする(int code) {
		assertThat(WeatherCodeTable.describe(code)).isEqualTo("—");
	}

}
