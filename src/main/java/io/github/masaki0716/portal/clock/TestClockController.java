package io.github.masaki0716.portal.clock;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.masaki0716.portal.weather.WeatherCacheRepository;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * 受け入れ条件 14 の口。テスト用の設定でないときは 404 を返す。
 * コントローラ自体は常に登録し、無効かどうかを先に見る（本文の形式より 404 を優先するため、本文は文字列で受けて自分で読む）。
 */
@RestController
@RequestMapping("/api/test")
public class TestClockController {

	private final TestClockProperties properties;

	private final Clock clock;

	private final WeatherCacheRepository weatherCacheRepository;

	private final JsonMapper jsonMapper;

	public TestClockController(TestClockProperties properties, Clock clock,
			WeatherCacheRepository weatherCacheRepository, JsonMapper jsonMapper) {
		this.properties = properties;
		this.clock = clock;
		this.weatherCacheRepository = weatherCacheRepository;
		this.jsonMapper = jsonMapper;
	}

	@PostMapping("/clock")
	ResponseEntity<Void> setClock(@RequestBody(required = false) String body) {
		if (!properties.enabled() || !(clock instanceof SettableClock settableClock)) {
			return ResponseEntity.notFound().build();
		}
		OffsetDateTime currentTime = parseCurrentTime(body);
		if (currentTime == null) {
			return ResponseEntity.badRequest().build();
		}
		settableClock.set(currentTime.toInstant());
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/weather-cache")
	ResponseEntity<Void> clearWeatherCache() {
		if (!properties.enabled()) {
			return ResponseEntity.notFound().build();
		}
		weatherCacheRepository.deleteAll();
		return ResponseEntity.noContent().build();
	}

	/** {"currentTime": "<タイムゾーン付きの ISO 8601>"} を読む。形式が不正なら null */
	private OffsetDateTime parseCurrentTime(String body) {
		if (body == null) {
			return null;
		}
		try {
			JsonNode currentTime = jsonMapper.readTree(body).get("currentTime");
			if (currentTime == null || !currentTime.isString()) {
				return null;
			}
			return OffsetDateTime.parse(currentTime.stringValue());
		}
		catch (JacksonException | DateTimeParseException ex) {
			return null;
		}
	}

}
