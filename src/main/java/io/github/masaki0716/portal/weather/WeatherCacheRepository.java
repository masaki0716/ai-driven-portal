package io.github.masaki0716.portal.weather;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 取得した予報（受け入れ条件 4・5）と天気 API の失敗の記録（6）。地点の緯度・経度ごとに最新の1件だけを持つ。
 */
@Repository
public class WeatherCacheRepository {

	public record CachedForecast(Instant fetchedAt, String body) {
	}

	private final JdbcClient jdbc;

	public WeatherCacheRepository(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	public Optional<CachedForecast> findForecast(WeatherProperties.Location location) {
		return jdbc.sql("""
				select fetched_at, body from weather_forecast
				where latitude = :latitude and longitude = :longitude
				""")
			.param("latitude", location.latitude())
			.param("longitude", location.longitude())
			.query((rs, rowNum) -> new CachedForecast(rs.getObject("fetched_at", OffsetDateTime.class).toInstant(),
					rs.getString("body")))
			.optional();
	}

	public void saveForecast(WeatherProperties.Location location, Instant fetchedAt, String body) {
		jdbc.sql("""
				insert into weather_forecast (latitude, longitude, fetched_at, body)
				values (:latitude, :longitude, :fetchedAt, :body)
				on conflict (latitude, longitude) do update set fetched_at = excluded.fetched_at, body = excluded.body
				""")
			.param("latitude", location.latitude())
			.param("longitude", location.longitude())
			.param("fetchedAt", toOffsetDateTime(fetchedAt))
			.param("body", body)
			.update();
	}

	public Optional<Instant> findLastFailure(WeatherProperties.Location location) {
		return jdbc.sql("""
				select failed_at from weather_api_failure
				where latitude = :latitude and longitude = :longitude
				""")
			.param("latitude", location.latitude())
			.param("longitude", location.longitude())
			.query((rs, rowNum) -> rs.getObject("failed_at", OffsetDateTime.class).toInstant())
			.optional();
	}

	public void recordFailure(WeatherProperties.Location location, Instant failedAt) {
		jdbc.sql("""
				insert into weather_api_failure (latitude, longitude, failed_at)
				values (:latitude, :longitude, :failedAt)
				on conflict (latitude, longitude) do update set failed_at = excluded.failed_at
				""")
			.param("latitude", location.latitude())
			.param("longitude", location.longitude())
			.param("failedAt", toOffsetDateTime(failedAt))
			.update();
	}

	@Transactional
	public void deleteAll() {
		jdbc.sql("delete from weather_forecast").update();
		jdbc.sql("delete from weather_api_failure").update();
	}

	private static OffsetDateTime toOffsetDateTime(Instant instant) {
		return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
	}

}
