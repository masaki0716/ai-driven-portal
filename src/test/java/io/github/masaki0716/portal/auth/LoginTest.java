package io.github.masaki0716.portal.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;

import io.github.masaki0716.portal.support.AuthIntegrationTest;
import io.github.masaki0716.portal.support.Browser;

/** 受け入れ条件 5・7: ログイン・ログアウト */
class LoginTest extends AuthIntegrationTest {

	private static final String LOGIN_FAILED = "メールアドレスまたはパスワードが正しくありません";

	@Test
	void ログインすると上部にユーザー名が表示されログアウトすると消える() {
		Member member = registerMember("山田");
		Browser browser = newBrowser();
		assertThat(topPageText(browser)).doesNotContain("ログイン中:", "山田");

		assertRedirectTo(login(browser, member.email(), member.password()), "/");
		assertThat(topPageText(browser)).contains("ログイン中: 山田");

		assertRedirectTo(logout(browser), "/");
		assertThat(topPageText(browser)).doesNotContain("ログイン中:", "山田");
	}

	@Test
	void ユーザー名の変更画面にも上部の表示とログアウトのフォームがある() {
		Browser browser = loggedIn(registerMember("山田"));

		HttpResponse<String> response = browser.get("/settings/username");

		assertThat(visibleText(response.body())).contains("ログイン中: 山田");
		assertThat(response.body()).contains("action=\"/logout\"");
	}

	@Test
	void メールアドレスは前後の空白と大文字小文字を区別せずにログインできる() {
		Member member = registerMember("山田");

		assertRedirectTo(login(newBrowser(), "　" + member.email().toUpperCase() + " ", member.password()), "/");
	}

	@Test
	void 直前に開こうとした画面には戻さずトップへ移動する() {
		Member member = registerMember("山田");
		Browser browser = newBrowser();
		assertRedirectTo(browser.get("/settings/username"), "/login");

		assertRedirectTo(login(browser, member.email(), member.password()), "/");
	}

	@Test
	void パスワードが違うとログインへ戻り失敗の文言を出す() {
		Member member = registerMember("山田");
		Browser browser = newBrowser();

		assertFailed(browser, login(browser, member.email(), "wrong-password"));
		assertThat(topPageText(browser)).doesNotContain("ログイン中:");
	}

	@Test
	void 登録されていないメールアドレスや空欄ではログインできない() {
		Browser browser = newBrowser();

		assertFailed(browser, login(browser, uniqueEmail(), PASSWORD));
		assertFailed(browser, login(browser, "", ""));
		assertFailed(browser, login(browser, " ", "　"));
	}

	@Test
	void パスワードは空白を除かずに比べる() {
		Member member = registerMember("山田");
		Browser browser = newBrowser();

		assertFailed(browser, login(browser, member.email(), " " + member.password()));
	}

	@Test
	void 長すぎるパスワードでもログインの失敗として扱う() {
		Member member = registerMember("山田");
		Browser browser = newBrowser();

		assertFailed(browser, login(browser, member.email(), "a".repeat(100)));
	}

	@Test
	void ログアウトしたあとのセッションではユーザー名を変更できない() {
		Member member = registerMember("山田");
		Browser browser = loggedIn(member);
		String id = memberId(browser);
		String csrf = browser.csrf("/settings/username");

		assertRedirectTo(logout(browser), "/");
		HttpResponse<String> response = changeUsername(browser, id, "佐藤", csrf);

		assertThat(response.statusCode()).isIn(302, 403);
		if (response.statusCode() == 302) {
			assertRedirectTo(response, "/login");
		}
		assertThat(topPageText(loggedIn(member))).contains("ログイン中: 山田");
	}

	@Test
	void ログインのたびにCSRFの値が変わる() {
		Member member = registerMember("山田");
		Browser browser = newBrowser();
		String before = browser.csrf("/login");

		assertRedirectTo(browser.post("/login", "email", member.email(), "password", member.password(), "_csrf",
				before), "/");
		String id = memberId(browser);

		assertThat(changeUsername(browser, id, "佐藤", before).statusCode()).isEqualTo(403);
	}

	private static void assertFailed(Browser browser, HttpResponse<String> response) {
		assertRedirectTo(response, "/login");
		HttpResponse<String> page = follow(browser, response);
		assertThat(page.statusCode()).isEqualTo(200);
		assertThat(visibleText(page.body())).contains(LOGIN_FAILED);
	}

}
