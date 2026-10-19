package io.github.masaki0716.portal.weather;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

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

		/** weather が null（その日の予報が無い）なら、日付だけ出してほかは「—」 */
		static Day of(String label, LocalDate date, DailyWeather weather) {
			if (weather == null) {
				return new Day(label, date.format(WeatherFormat.DATE), WeatherFormat.MISSING, WeatherFormat.MISSING,
						WeatherFormat.MISSING, WeatherFormat.MISSING);
			}
			String description = (weather.weatherCode() == null) ? WeatherFormat.MISSING
					: WeatherCodeTable.describe(weather.weatherCode());
			return new Day(label, date.format(WeatherFormat.DATE), description,
					WeatherFormat.temperature(weather.temperatureMax()),
					WeatherFormat.temperature(weather.temperatureMin()),
					WeatherFormat.precipitation(weather.precipitationProbabilityMax()));
		}

	}

	/** 取得したばかりの予報から作る。今日か明日の天気そのものが無ければ UnexpectedWeatherResponseException */
	static WeatherPanel fetched(String locationName, Instant fetchedAt, Forecast forecast, LocalDate today) {
		LocalDate tomorrow = today.plusDays(1);
		List<Day> days = List.of(Day.of("今日", today, forecast.requireDay(today)),
				Day.of("明日", tomorrow, forecast.requireDay(tomorrow)));
		return new WeatherPanel(Status.FRESH, locationName, fetchedAtText(Status.FRESH, fetchedAt), days);
	}

	/**
	 * 保持している予報（新しい予報・古い予報）から作る。今日・明日は表示する時点の日付で探し直し、
	 * 片方だけ無ければその日の欄を「—」にする。両方とも無ければ空（取得できなかった扱い）
	 */
	static Optional<WeatherPanel> stored(Status status, String locationName, Instant fetchedAt, Forecast forecast,
			LocalDate today) {
		LocalDate tomorrow = today.plusDays(1);
		DailyWeather todayWeather = forecast.days().get(today);
		DailyWeather tomorrowWeather = forecast.days().get(tomorrow);
		if (todayWeather == null && tomorrowWeather == null) {
			return Optional.empty();
		}
		List<Day> days = List.of(Day.of("今日", today, todayWeather), Day.of("明日", tomorrow, tomorrowWeather));
		return Optional.of(new WeatherPanel(status, locationName, fetchedAtText(status, fetchedAt), days));
	}

	/** 新しい予報は HH:mm、古い予報は最大24時間前なので日付も付ける */
	private static String fetchedAtText(Status status, Instant fetchedAt) {
		DateTimeFormatter formatter = (status == Status.STALE) ? WeatherFormat.DATE_TIME : WeatherFormat.TIME;
		return fetchedAt.atZone(WeatherFormat.JST).format(formatter);
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
