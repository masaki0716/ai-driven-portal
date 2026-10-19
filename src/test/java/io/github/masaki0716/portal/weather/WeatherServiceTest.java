package io.github.masaki0716.portal.weather;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.ResourceAccessException;

import tools.jackson.databind.json.JsonMapper;

/** 受け入れ条件 6: 5分は、アプリの時計で失敗を記録した時刻から測る */
class WeatherServiceTest {

	private static final WeatherProperties PROPERTIES = new WeatherProperties(
			new WeatherProperties.Location("東京", 35.6895, 139.6917),
			new WeatherProperties.Api("https://api.open-meteo.com"));

	@Test
	void 失敗はアプリの時計で記録した時刻で残す() {
		Instant requested = Instant.parse("2026-10-17T00:00:00Z");
		Instant recorded = Instant.parse("2026-10-17T00:00:05Z");
		OpenMeteoClient client = mock(OpenMeteoClient.class);
		WeatherCacheRepository repository = mock(WeatherCacheRepository.class);
		given(repository.findForecast(any())).willReturn(Optional.empty());
		given(repository.findLastFailure(any())).willReturn(Optional.empty());
		given(client.fetchForecast()).willThrow(new ResourceAccessException("request timed out"));
		WeatherService service = new WeatherService(client, new OpenMeteoResponseParser(JsonMapper.builder().build()),
				repository, new SequenceClock(requested, recorded), PROPERTIES);

		service.currentWeather();

		verify(repository).recordFailure(PROPERTIES.location(), recorded);
	}

	/** 呼ばれるたびに、渡した時刻を順に返す時計（最後の時刻で止まる） */
	private static final class SequenceClock extends Clock {

		private final Deque<Instant> instants;

		SequenceClock(Instant... instants) {
			this.instants = new ArrayDeque<>(List.of(instants));
		}

		@Override
		public Instant instant() {
			return (instants.size() > 1) ? instants.pop() : instants.peek();
		}

		@Override
		public ZoneId getZone() {
			return ZoneOffset.UTC;
		}

		@Override
		public Clock withZone(ZoneId zone) {
			return this;
		}

	}

}
