package io.github.masaki0716.portal.weather;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

/** 受け入れ条件 10・11: 地点の設定が無い・不正なら起動時に止まる */
class WeatherPropertiesTest {

	private final ApplicationContextRunner runner = new ApplicationContextRunner()
		.withUserConfiguration(PropertiesConfig.class);

	@Test
	void 正しい設定なら起動し接続先は既定でOpenMeteoになる() {
		runner.withPropertyValues(
				"portal.weather.location.name=東京",
				"portal.weather.location.latitude=35.6895",
				"portal.weather.location.longitude=139.6917")
			.run(context -> {
				assertThat(context).hasNotFailed();
				WeatherProperties properties = context.getBean(WeatherProperties.class);
				assertThat(properties.location().name()).isEqualTo("東京");
				assertThat(properties.location().latitude()).isEqualTo(35.6895);
				assertThat(properties.api().baseUrl()).isEqualTo("https://api.open-meteo.com");
			});
	}

	@Test
	void 接続先は設定で差し替えられる() {
		runner.withPropertyValues(
				"portal.weather.location.name=東京",
				"portal.weather.location.latitude=35.6895",
				"portal.weather.location.longitude=139.6917",
				"portal.weather.api.base-url=http://localhost:9999")
			.run(context -> assertThat(context.getBean(WeatherProperties.class).api().baseUrl())
				.isEqualTo("http://localhost:9999"));
	}

	@ParameterizedTest
	@CsvSource(delimiter = '|', value = {
			"東京 | 90   | 180",
			"東京 | -90  | -180",
			"東京 | 0    | 0" })
	void 範囲の端は受け付ける(String name, String latitude, String longitude) {
		runner.withPropertyValues(location(name, latitude, longitude))
			.run(context -> assertThat(context).hasNotFailed());
	}

	@Test
	void 地点の設定が無ければ起動に失敗する() {
		runner.run(context -> assertThat(context).hasFailed());
	}

	@ParameterizedTest
	@CsvSource(delimiter = '|', quoteCharacter = '"', value = {
			"\"\"   | 35.6895 | 139.6917",
			"\"  \" | 35.6895 | 139.6917",
			"東京   | \"\"      | 139.6917",
			"東京   | 35.6895 | \"\"",
			"東京   | abc     | 139.6917",
			"東京   | 35.6895 | abc",
			"東京   | 90.1    | 139.6917",
			"東京   | -90.1   | 139.6917",
			"東京   | 35.6895 | 180.1",
			"東京   | 35.6895 | -180.1",
			"東京   | NaN     | 139.6917",
			"東京   | 35.6895 | NaN" })
	void 地点名緯度経度が空や数値でないや範囲外なら起動に失敗する(String name, String latitude, String longitude) {
		runner.withPropertyValues(location(name, latitude, longitude))
			.run(context -> assertThat(context).hasFailed());
	}

	@Test
	void 地点の項目が1つでも欠けていれば起動に失敗する() {
		runner.withPropertyValues(
				"portal.weather.location.name=東京",
				"portal.weather.location.latitude=35.6895")
			.run(context -> assertThat(context).hasFailed());
	}

	private static String[] location(String name, String latitude, String longitude) {
		return new String[] {
				"portal.weather.location.name=" + name,
				"portal.weather.location.latitude=" + latitude,
				"portal.weather.location.longitude=" + longitude };
	}

	@Configuration(proxyBeanMethods = false)
	@EnableConfigurationProperties(WeatherProperties.class)
	static class PropertiesConfig {

	}

}
