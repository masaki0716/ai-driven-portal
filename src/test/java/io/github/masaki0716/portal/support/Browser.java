package io.github.masaki0716.portal.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.CookieManager;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * クッキーを保持し、リダイレクトを追わない HTTP クライアント。受け入れテストと同じく、画面から取り出した値をフォームで送る。
 */
public final class Browser {

	private static final Pattern CSRF = Pattern.compile("<input[^>]*name=\"_csrf\"[^>]*value=\"([^\"]*)\"");

	private final String baseUrl;

	private final HttpClient http;

	Browser(String baseUrl) {
		this.baseUrl = baseUrl;
		this.http = HttpClient.newBuilder()
			.version(HttpClient.Version.HTTP_1_1)
			.cookieHandler(new CookieManager())
			.followRedirects(HttpClient.Redirect.NEVER)
			.build();
	}

	public HttpResponse<String> get(String path) {
		return send(request(path).GET());
	}

	/** フォームを送る。値は送る順に並べる（キーと値を交互に） */
	public HttpResponse<String> post(String path, String... keyValues) {
		Map<String, String> form = new LinkedHashMap<>();
		for (int i = 0; i < keyValues.length; i += 2) {
			form.put(keyValues[i], keyValues[i + 1]);
		}
		String body = form.entrySet()
			.stream()
			.map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
			.collect(Collectors.joining("&"));
		return send(request(path).header("Content-Type", "application/x-www-form-urlencoded")
			.POST(HttpRequest.BodyPublishers.ofString(body)));
	}

	/** 画面を開き、フォームの _csrf の値を取り出す */
	public String csrf(String path) {
		HttpResponse<String> response = get(path);
		assertThat(response.statusCode()).as("GET %s", path).isEqualTo(200);
		Matcher matcher = CSRF.matcher(response.body());
		assertThat(matcher.find()).as("%s に _csrf があること", path).isTrue();
		return matcher.group(1);
	}

	private HttpRequest.Builder request(String path) {
		return HttpRequest.newBuilder(URI.create(baseUrl + path));
	}

	private HttpResponse<String> send(HttpRequest.Builder builder) {
		try {
			return http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
		}
		catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(ex);
		}
	}

	private static String encode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}

}
