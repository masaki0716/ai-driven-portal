package io.github.masaki0716.portal.weather;

/**
 * ある日の予報。天気コード以外は API の結果に無ければ null。
 */
public record DailyWeather(Integer weatherCode, Double temperatureMax, Double temperatureMin,
		Double precipitationProbabilityMax) {
}
