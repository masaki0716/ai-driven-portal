package io.github.masaki0716.portal.weather;

import java.time.LocalDate;
import java.util.Map;

/**
 * 天気 API の応答から読んだ日ごとの予報。
 */
public record Forecast(Map<LocalDate, DailyWeather> days) {

	/** その日の予報。日付が無い・天気コードが無いときは「天気そのものが無い」として失敗にする */
	public DailyWeather requireDay(LocalDate date) {
		DailyWeather day = days.get(date);
		if (day == null || day.weatherCode() == null) {
			throw new UnexpectedWeatherResponseException("weather for " + date + " is missing");
		}
		return day;
	}

}
