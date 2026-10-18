package io.github.masaki0716.portal.weather;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Open-Meteo の応答から daily.time と4項目だけを読む。
 * daily・daily.time が無い、配列の型が違う、要素が数値でも null でもない、は想定外の形式。
 * 4項目のキーが無い・値が null・配列が time より短い、は「無い項目」（null）。
 */
@Component
public class OpenMeteoResponseParser {

	private final JsonMapper jsonMapper;

	public OpenMeteoResponseParser(JsonMapper jsonMapper) {
		this.jsonMapper = jsonMapper;
	}

	public Forecast parse(String body) {
		JsonNode root = readTree(body);
		JsonNode daily = root.get("daily");
		if (daily == null || !daily.isObject()) {
			throw new UnexpectedWeatherResponseException("daily is missing");
		}
		List<LocalDate> dates = dates(daily.get("time"));
		List<Double> weatherCodes = numbers(daily, "weather_code", dates.size());
		List<Double> maxima = numbers(daily, "temperature_2m_max", dates.size());
		List<Double> minima = numbers(daily, "temperature_2m_min", dates.size());
		List<Double> precipitation = numbers(daily, "precipitation_probability_max", dates.size());

		Map<LocalDate, DailyWeather> days = new LinkedHashMap<>();
		for (int i = 0; i < dates.size(); i++) {
			days.putIfAbsent(dates.get(i), new DailyWeather(weatherCode(weatherCodes.get(i)), maxima.get(i),
					minima.get(i), precipitation.get(i)));
		}
		return new Forecast(days);
	}

	private JsonNode readTree(String body) {
		if (body == null) {
			throw new UnexpectedWeatherResponseException("body is empty");
		}
		try {
			JsonNode root = jsonMapper.readTree(body);
			if (root == null || !root.isObject()) {
				throw new UnexpectedWeatherResponseException("body is not a JSON object");
			}
			return root;
		}
		catch (JacksonException ex) {
			throw new UnexpectedWeatherResponseException("body is not JSON", ex);
		}
	}

	private static List<LocalDate> dates(JsonNode time) {
		if (time == null || !time.isArray()) {
			throw new UnexpectedWeatherResponseException("daily.time is not an array");
		}
		List<LocalDate> dates = new ArrayList<>();
		for (JsonNode element : time) {
			if (!element.isString()) {
				throw new UnexpectedWeatherResponseException("daily.time has a non-string element");
			}
			try {
				dates.add(LocalDate.parse(element.stringValue(), WeatherFormat.DATE));
			}
			catch (DateTimeParseException ex) {
				throw new UnexpectedWeatherResponseException("daily.time has an invalid date", ex);
			}
		}
		return dates;
	}

	/** 日付と同じ数の要素を返す。無い項目は null で埋める */
	private static List<Double> numbers(JsonNode daily, String name, int size) {
		List<Double> values = new ArrayList<>();
		JsonNode array = daily.get(name);
		if (array != null && !array.isNull()) {
			if (!array.isArray()) {
				throw new UnexpectedWeatherResponseException("daily." + name + " is not an array");
			}
			for (JsonNode element : array) {
				if (element.isNull()) {
					values.add(null);
				}
				else if (element.isNumber()) {
					values.add(element.doubleValue());
				}
				else {
					throw new UnexpectedWeatherResponseException("daily." + name + " has a non-number element");
				}
			}
		}
		while (values.size() < size) {
			values.add(null);
		}
		return values;
	}

	private static Integer weatherCode(Double value) {
		if (value == null) {
			return null;
		}
		if (value != Math.rint(value)) {
			throw new UnexpectedWeatherResponseException("daily.weather_code has a non-integer element");
		}
		return value.intValue();
	}

}
