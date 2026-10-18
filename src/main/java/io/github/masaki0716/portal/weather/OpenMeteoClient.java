package io.github.masaki0716.portal.weather;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Open-Meteo の予報 API を呼ぶ。打ち切りは実際の時間で5秒（テスト時計は使わない）。
 */
@Component
public class OpenMeteoClient {

	static final Duration TIMEOUT = Duration.ofSeconds(5);

	static final String DAILY = "weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max";

	private final RestClient restClient;

	private final WeatherProperties.Location location;

	public OpenMeteoClient(RestClient.Builder builder, WeatherProperties properties) {
		HttpClient httpClient = HttpClient.newBuilder()
			.connectTimeout(TIMEOUT)
			.version(HttpClient.Version.HTTP_1_1)
			.build();
		JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
		requestFactory.setReadTimeout(TIMEOUT);
		this.restClient = builder.clone()
			.baseUrl(properties.api().baseUrl())
			.requestFactory(requestFactory)
			.build();
		this.location = properties.location();
	}

	/** 応答の本文をそのまま返す。エラーのステータスや打ち切りは例外になる */
	public String fetchForecast() {
		return restClient.get()
			.uri(uri -> uri.path("/v1/forecast")
				.queryParam("latitude", location.latitude())
				.queryParam("longitude", location.longitude())
				.queryParam("daily", DAILY)
				.queryParam("timezone", "Asia/Tokyo")
				.build())
			.retrieve()
			.body(String.class);
	}

}
