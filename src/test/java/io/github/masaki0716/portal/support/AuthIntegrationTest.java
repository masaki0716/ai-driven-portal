package io.github.masaki0716.portal.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;

/**
 * 会員を扱う結合テスト。毎回、時計を T0 に合わせ、失敗の記録を消す。
 * 会員のデータは消さないので、テストごとに別のメールアドレスで登録する（受け入れ条件の「テストの間の状態」と同じ）。
 */
public abstract class AuthIntegrationTest extends WeatherIntegrationTest {

	protected static final String PASSWORD = "password1";

	private static final Pattern USERNAME_FORM = Pattern.compile("action=\"/users/([^/\"]+)/username\"");

	/** 登録した会員。id はログインして変更画面から読み取る */
	public record Member(String email, String password, String username) {
	}

	@BeforeEach
	void clearLoginAttempts() {
		assertThat(delete("/api/test/login-attempts").statusCode()).isEqualTo(204);
	}

	protected static String uniqueEmail() {
		return "user-" + UUID.randomUUID() + "@example.com";
	}

	protected HttpResponse<String> signup(Browser browser, String email, String password, String username,
			String inviteCode) {
		String csrf = browser.csrf("/signup");
		return browser.post("/signup", "email", email, "password", password, "username", username, "inviteCode",
				inviteCode, "_csrf", csrf);
	}

	/** 新しい会員を登録する（ログインはしない） */
	protected Member registerMember(String username) {
		Member member = new Member(uniqueEmail(), PASSWORD, username);
		HttpResponse<String> response = signup(newBrowser(), member.email(), member.password(), member.username(),
				INVITE_CODE);
		assertRedirectTo(response, "/login");
		return member;
	}

	protected HttpResponse<String> login(Browser browser, String email, String password) {
		String csrf = browser.csrf("/login");
		return browser.post("/login", "email", email, "password", password, "_csrf", csrf);
	}

	/** 会員でログインしたブラウザ */
	protected Browser loggedIn(Member member) {
		Browser browser = newBrowser();
		assertRedirectTo(login(browser, member.email(), member.password()), "/");
		return browser;
	}

	protected HttpResponse<String> logout(Browser browser) {
		String csrf = browser.csrf("/");
		return browser.post("/logout", "_csrf", csrf);
	}

	/** ログイン中のブラウザで変更画面を開き、フォームの送り先から自分の ID を読み取る */
	protected static String memberId(Browser browser) {
		HttpResponse<String> response = browser.get("/settings/username");
		assertThat(response.statusCode()).isEqualTo(200);
		Matcher matcher = USERNAME_FORM.matcher(response.body());
		assertThat(matcher.find()).as("ユーザー名の変更フォームがあること").isTrue();
		return matcher.group(1);
	}

	protected static HttpResponse<String> changeUsername(Browser browser, String id, String username, String csrf) {
		return browser.post("/users/" + id + "/username", "username", username, "_csrf", csrf);
	}

	/** ブラウザで開いたトップページの表示テキスト */
	protected static String topPageText(Browser browser) {
		HttpResponse<String> response = browser.get("/");
		assertThat(response.statusCode()).isEqualTo(200);
		return visibleText(response.body());
	}

	/** リダイレクト先を同じブラウザで開く */
	protected static HttpResponse<String> follow(Browser browser, HttpResponse<String> redirect) {
		URI location = URI.create(redirect.headers().firstValue("Location").orElseThrow());
		String query = location.getRawQuery();
		return browser.get(location.getRawPath() + ((query == null) ? "" : "?" + query));
	}

	protected static void assertRedirectTo(HttpResponse<String> response, String path) {
		assertThat(response.statusCode()).as("本文: %s", response.body()).isEqualTo(302);
		assertThat(response.headers().firstValue("Location"))
			.hasValueSatisfying(location -> assertThat(URI.create(location).getPath()).isEqualTo(path));
	}

}
