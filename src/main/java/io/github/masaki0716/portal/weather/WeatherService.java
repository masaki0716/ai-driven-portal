package io.github.masaki0716.portal.weather;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;

/**
 * 天気欄の内容を決める（受け入れ条件 4〜7・9）。
 * 時刻の判定と「今日」「明日」はアプリの時計（テスト用の設定では外から変えられる）で行う。
 */
@Service
public class WeatherService {

	private static final Logger log = LoggerFactory.getLogger(WeatherService.class);

	static final Duration FRESH_FOR = Duration.ofMinutes(30);

	static final Duration STALE_FOR = Duration.ofHours(24);

	static final Duration RETRY_AFTER_FAILURE = Duration.ofMinutes(5);

	private final OpenMeteoClient client;

	private final OpenMeteoResponseParser parser;

	private final WeatherCacheRepository repository;

	private final Clock clock;

	private final WeatherProperties.Location location;

	public WeatherService(OpenMeteoClient client, OpenMeteoResponseParser parser, WeatherCacheRepository repository,
			Clock clock, WeatherProperties properties) {
		this.client = client;
		this.parser = parser;
		this.repository = repository;
		this.clock = clock;
		this.location = properties.location();
	}

	public WeatherPanel currentWeather() {
		Instant now = clock.instant();
		LocalDate today = LocalDate.ofInstant(now, WeatherFormat.JST);
		Optional<WeatherCacheRepository.CachedForecast> cached = repository.findForecast(location);

		// 新しい予報のうちは、日付をまたいでも呼び直さない（今日・明日だけ探し直す）
		Optional<WeatherCacheRepository.CachedForecast> fresh = cached
			.filter(c -> isWithin(c.fetchedAt(), now, FRESH_FOR));
		if (fresh.isPresent()) {
			return storedPanel(fresh.get(), WeatherPanel.Status.FRESH, today);
		}

		boolean recentlyFailed = repository.findLastFailure(location)
			.filter(failedAt -> isWithin(failedAt, now, RETRY_AFTER_FAILURE))
			.isPresent();
		if (!recentlyFailed) {
			Optional<WeatherPanel> fetched = fetch(now, today);
			if (fetched.isPresent()) {
				return fetched.get();
			}
		}

		return cached.filter(c -> isWithin(c.fetchedAt(), now, STALE_FOR))
			.map(c -> storedPanel(c, WeatherPanel.Status.STALE, today))
			.orElseGet(() -> WeatherPanel.unavailable(location.name()));
	}

	/** 天気 API を呼ぶ。成功したら保持し、失敗したらログに残して失敗を記録する */
	private Optional<WeatherPanel> fetch(Instant now, LocalDate today) {
		String body;
		WeatherPanel panel;
		try {
			body = client.fetchForecast();
			panel = WeatherPanel.fetched(location.name(), now, parser.parse(body), today);
		}
		catch (RuntimeException ex) {
			log.error("weather api call failed: cause={}", describe(ex), ex);
			repository.recordFailure(location, now);
			return Optional.empty();
		}
		repository.saveForecast(location, now, body);
		return Optional.of(panel);
	}

	/** 保持している予報を出す。今日も明日も無ければ「取得できなかった」表示にする */
	private WeatherPanel storedPanel(WeatherCacheRepository.CachedForecast cached, WeatherPanel.Status status,
			LocalDate today) {
		return WeatherPanel.stored(status, location.name(), cached.fetchedAt(), parser.parse(cached.body()), today)
			.orElseGet(() -> WeatherPanel.unavailable(location.name()));
	}

	/** from から now までが 0 以上 duration 以下か。ちょうど duration は含む（時計を戻したときの未来の記録は使わない） */
	private static boolean isWithin(Instant from, Instant now, Duration duration) {
		return !now.isBefore(from) && Duration.between(from, now).compareTo(duration) <= 0;
	}

	/** ログに出す原因。ステータスコードか、いちばん元の例外のクラス名 */
	private static String describe(RuntimeException ex) {
		if (ex instanceof RestClientResponseException response) {
			return "status " + response.getStatusCode().value();
		}
		Throwable root = ex;
		while (root.getCause() != null && root.getCause() != root) {
			root = root.getCause();
		}
		return root.getClass().getName();
	}

}
