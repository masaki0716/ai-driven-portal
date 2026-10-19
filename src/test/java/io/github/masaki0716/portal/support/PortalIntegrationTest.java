package io.github.masaki0716.portal.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

/**
 * アプリを実際のポートで起動し、外から HTTP で叩く結合テストの土台。
 * テスト時計の有効・無効は、サブクラスの @TestPropertySource で上書きする。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = {
		"portal.weather.location.name=東京",
		"portal.weather.location.latitude=35.6895",
		"portal.weather.location.longitude=139.6917",
		"portal.auth.invite-code=" + PortalIntegrationTest.INVITE_CODE,
		"portal.test-clock.enabled=true" })
public abstract class PortalIntegrationTest {

	/** テストで使う招待コード */
	public static final String INVITE_CODE = "test-invite-code";

	private static final HttpClient HTTP = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();

	@Value("${local.server.port}")
	private int port;

	@DynamicPropertySource
	static void weatherApi(DynamicPropertyRegistry registry) {
		registry.add("portal.weather.api.base-url", WeatherApiStub::baseUrl);
	}

	@BeforeEach
	void resetWeatherApi() {
		WeatherApiStub.reset();
	}

	/** クッキーを持つ、新しいブラウザ（別の相手のつもりで使う） */
	protected Browser newBrowser() {
		return new Browser("http://localhost:" + port);
	}

	protected HttpResponse<String> get(String path) {
		return send(request(path).GET());
	}

	protected HttpResponse<String> postJson(String path, String json) {
		return send(request(path).header("Content-Type", "application/json")
			.POST(HttpRequest.BodyPublishers.ofString(json)));
	}

	protected HttpResponse<String> delete(String path) {
		return send(request(path).DELETE());
	}

	/** トップページを開き、200 であることを確かめて表示テキストを返す */
	protected String topPageText() {
		HttpResponse<String> response = get("/");
		assertThat(response.statusCode()).isEqualTo(200);
		return visibleText(response.body());
	}

	/** タグを除いた表示テキスト（空白は1つにまとめる） */
	protected static String visibleText(String html) {
		return html.replaceAll("(?s)<head>.*?</head>", " ")
			.replaceAll("<[^>]+>", " ")
			.replaceAll("\\s+", " ")
			.trim();
	}

	/** text の中に parts がこの順に現れることを確かめる */
	protected static void assertInOrder(String text, String... parts) {
		int from = 0;
		for (String part : parts) {
			int found = text.indexOf(part, from);
			assertThat(found).as("「%s」が順番どおりに現れること: %s", part, text).isGreaterThanOrEqualTo(0);
			from = found + part.length();
		}
	}

	private HttpRequest.Builder request(String path) {
		return HttpRequest.newBuilder(URI.create("http://localhost:" + port + path));
	}

	private static HttpResponse<String> send(HttpRequest.Builder builder) {
		try {
			return HTTP.send(builder.build(), HttpResponse.BodyHandlers.ofString());
		}
		catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(ex);
		}
	}

}
