package io.github.masaki0716.portal.support;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;

/**
 * Open-Meteo の差し替え。JVM で1つだけ起動し、テストごとに reset する。
 */
public final class WeatherApiStub {

	public static final String PATH = "/v1/forecast";

	/** 2026-10-17 を今日とする標準の応答。明日の最低気温は負の .5 */
	public static final String DEFAULT_DAILY = """
			"time": ["2026-10-17", "2026-10-18", "2026-10-19"],
			"weather_code": [1, 61, 3],
			"temperature_2m_max": [22.5, 18.4, 20.0],
			"temperature_2m_min": [14.4, -2.5, 12.0],
			"precipitation_probability_max": [10, 80, 30]
			""";

	private static final WireMockServer SERVER = start();

	private WeatherApiStub() {
	}

	private static WireMockServer start() {
		WireMockServer server = new WireMockServer(options().bindAddress("127.0.0.1").dynamicPort());
		server.start();
		Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
		return server;
	}

	public static String baseUrl() {
		return SERVER.baseUrl();
	}

	public static void reset() {
		SERVER.resetAll();
	}

	/** Open-Meteo の応答の形に、daily の中身を包む */
	public static String forecastJson(String daily) {
		return """
				{"latitude": 35.7, "longitude": 139.69, "timezone": "Asia/Tokyo", "daily": {%s}}
				""".formatted(daily);
	}

	public static void respondWithDaily(String daily) {
		respondWithBody(forecastJson(daily));
	}

	public static void respondWithBody(String json) {
		respond(okJson(json));
	}

	public static void respondWithStatus(int status) {
		respond(aResponse().withStatus(status));
	}

	public static void respondAfter(int delayMillis) {
		respond(okJson(forecastJson(DEFAULT_DAILY)).withFixedDelay(delayMillis));
	}

	public static void respond(ResponseDefinitionBuilder response) {
		SERVER.stubFor(get(urlPathEqualTo(PATH)).willReturn(response));
	}

	public static int callCount() {
		return SERVER.countRequestsMatching(getRequestedFor(urlPathEqualTo(PATH)).build()).getCount();
	}

	public static void verify(RequestPatternBuilder pattern) {
		SERVER.verify(pattern);
	}

}
