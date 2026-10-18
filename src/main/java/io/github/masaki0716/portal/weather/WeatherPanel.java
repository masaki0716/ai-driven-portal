package io.github.masaki0716.portal.weather;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * トップページの天気欄に出す内容（文字列は表示の形に整えたもの）。
 */
public record WeatherPanel(Status status, String locationName, String fetchedAt, List<Day> days) {

	public enum Status {
		/** 取得から30分以内、または今取得した予報 */
		FRESH,
		/** 取得できず、30分を過ぎ24時間以内の予報を出している */
		STALE,
		/** 取得できず、出せる予報も無い */
		UNAVAILABLE
	}

	public record Day(String label, String date, String weather, String temperatureMax, String temperatureMin,
			String precipitationProbability) {

		static Day of(String label, LocalDate date, DailyWeather weather) {
			return new Day(label, date.format(WeatherFormat.DATE), WeatherCodeTable.describe(weather.weatherCode()),
					WeatherFormat.temperature(weather.temperatureMax()),
					WeatherFormat.temperature(weather.temperatureMin()),
					WeatherFormat.precipitation(weather.precipitationProbabilityMax()));
		}

	}

	/** 今日・明日の予報から作る。どちらかの天気そのものが無ければ UnexpectedWeatherResponseException */
	static WeatherPanel of(Status status, String locationName, Instant fetchedAt, Forecast forecast, LocalDate today) {
		LocalDate tomorrow = today.plusDays(1);
		List<Day> days = List.of(Day.of("今日", today, forecast.requireDay(today)),
				Day.of("明日", tomorrow, forecast.requireDay(tomorrow)));
		var format = (status == Status.STALE) ? WeatherFormat.DATE_TIME : WeatherFormat.TIME;
		return new WeatherPanel(status, locationName, fetchedAt.atZone(WeatherFormat.JST).format(format), days);
	}

	static WeatherPanel unavailable(String locationName) {
		return new WeatherPanel(Status.UNAVAILABLE, locationName, null, List.of());
	}

	public boolean stale() {
		return status == Status.STALE;
	}

	public boolean unavailable() {
		return status == Status.UNAVAILABLE;
	}

}
