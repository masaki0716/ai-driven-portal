package io.github.masaki0716.portal.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import io.github.masaki0716.portal.support.AuthIntegrationTest;
import io.github.masaki0716.portal.support.Browser;
import io.github.masaki0716.portal.support.WeatherApiStub;

/** 受け入れ条件 15・18・19: ログインしていなくても開ける画面と、ログイン中の画面のキャッシュ */
class AccessControlTest extends AuthIntegrationTest {

	@ParameterizedTest
	@ValueSource(strings = { "/", "/login", "/signup", "/actuator/health" })
	void 一覧の画面はログインしていなくても開ける(String path) {
		WeatherApiStub.respondWithDaily(WeatherApiStub.DEFAULT_DAILY);

		assertThat(newBrowser().get(path).statusCode()).isEqualTo(200);
	}

	@Test
	void 天気予報はログインしていなくても今まで通り表示される() {
		WeatherApiStub.respondWithDaily(WeatherApiStub.DEFAULT_DAILY);

		String text = topPageText(newBrowser());

		assertThat(text).contains("天気予報（東京）", "晴れ");
	}

	@ParameterizedTest
	@ValueSource(strings = { "/settings/username", "/no-such-page", "/users", "/actuator/info", "/error" })
	void 一覧に無いパスはログインへ案内する(String path) {
		assertRedirectTo(newBrowser().get(path), "/login");
	}

	@Test
	void ログイン中に返す画面は共有キャッシュに保持させない() {
		WeatherApiStub.respondWithDaily(WeatherApiStub.DEFAULT_DAILY);
		Browser browser = loggedIn(registerMember("山田"));

		for (String path : new String[] { "/", "/settings/username" }) {
			HttpResponse<String> response = browser.get(path);
			assertThat(response.statusCode()).isEqualTo(200);
			assertThat(response.headers().firstValue("Cache-Control")).as(path)
				.hasValueSatisfying(value -> assertThat(value).containsAnyOf("private", "no-store"));
		}
	}

}
