package io.github.masaki0716.portal.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import io.github.masaki0716.portal.support.AuthIntegrationTest;
import io.github.masaki0716.portal.support.Browser;

/** 受け入れ条件 8〜14: ユーザー名の変更 */
class UsernameChangeTest extends AuthIntegrationTest {

	private static final String USERNAME_LENGTH = "ユーザー名は1文字以上20文字以下にしてください";

	private static final String REQUIRED_USERNAME = "ユーザー名を入力してください";

	@Test
	void 会員のIDは小文字ハイフン区切りの36文字() {
		String id = memberId(loggedIn(registerMember("山田")));

		assertThat(id).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
	}

	@Test
	void ログインしていないと変更画面はログインへ案内し中身を返さない() {
		HttpResponse<String> response = newBrowser().get("/settings/username");

		assertRedirectTo(response, "/login");
		assertThat(response.body()).doesNotContain("/username\"");
	}

	@Test
	void ログインしていないと正しいCSRFの値を付けて変更を送ってもログインへ案内しデータは変わらない() {
		Member member = registerMember("山田");
		String id = memberId(loggedIn(member));
		Browser anonymous = newBrowser();
		String csrf = anonymous.csrf("/login");

		assertRedirectTo(changeUsername(anonymous, id, "佐藤", csrf), "/login");

		assertThat(topPageText(loggedIn(member))).contains("ログイン中: 山田");
	}

	@Test
	void 自分のユーザー名を変更すると上部に新しいユーザー名が表示される() {
		Browser browser = loggedIn(registerMember("山田"));
		String id = memberId(browser);

		assertRedirectTo(changeUsername(browser, id, "　佐藤 花子 ", browser.csrf("/settings/username")), "/");

		assertThat(topPageText(browser)).contains("ログイン中: 佐藤 花子").doesNotContain("山田");
	}

	@Test
	void 別の会員のIDに送ると403で別の会員のユーザー名は変わらない() {
		Member other = registerMember("他人");
		String otherId = memberId(loggedIn(other));
		Browser browser = loggedIn(registerMember("山田"));

		HttpResponse<String> response = changeUsername(browser, otherId, "乗っ取り", browser.csrf("/settings/username"));

		assertThat(response.statusCode()).isEqualTo(403);
		assertThat(topPageText(loggedIn(other))).contains("ログイン中: 他人");
		assertThat(topPageText(browser)).contains("ログイン中: 山田");
	}

	@Test
	void 別の会員のIDに条件を満たさない名前を送っても403() {
		String otherId = memberId(loggedIn(registerMember("他人")));
		Browser browser = loggedIn(registerMember("山田"));

		assertThat(changeUsername(browser, otherId, "", browser.csrf("/settings/username")).statusCode())
			.isEqualTo(403);
	}

	@ParameterizedTest
	@ValueSource(strings = { "nonexistent", "not-a-uuid" })
	void 存在しないIDや形の違う値に送ると403でどのユーザー名も変わらない(String kind) {
		Browser browser = loggedIn(registerMember("山田"));
		String id = memberId(browser);
		String target = kind.equals("nonexistent") ? UUID.randomUUID().toString() : id.toUpperCase();

		HttpResponse<String> response = changeUsername(browser, target, "佐藤", browser.csrf("/settings/username"));

		assertThat(response.statusCode()).isEqualTo(403);
		assertThat(topPageText(browser)).contains("ログイン中: 山田");
	}

	@Test
	void CSRFの値が無いか違うと403でデータは変わらない() {
		Member member = registerMember("山田");
		Browser browser = loggedIn(member);
		String id = memberId(browser);

		assertThat(browser.post("/users/" + id + "/username", "username", "佐藤").statusCode()).isEqualTo(403);
		assertThat(changeUsername(browser, id, "佐藤", "wrong-token").statusCode()).isEqualTo(403);

		assertThat(topPageText(browser)).contains("ログイン中: 山田");
	}

	@Test
	void ログインしていなくてもCSRFの値が無いか違うと403でログインへ案内しない() {
		Member member = registerMember("山田");
		String id = memberId(loggedIn(member));
		Browser anonymous = newBrowser();
		anonymous.csrf("/login");

		assertThat(anonymous.post("/users/" + id + "/username", "username", "佐藤").statusCode()).isEqualTo(403);
		assertThat(changeUsername(anonymous, id, "佐藤", "wrong-token").statusCode()).isEqualTo(403);
		assertThat(changeUsername(newBrowser(), id, "佐藤", "wrong-token").statusCode()).isEqualTo(403);

		assertThat(topPageText(loggedIn(member))).contains("ログイン中: 山田");
	}

	@ParameterizedTest
	@ValueSource(strings = { "", " ", "　 " })
	void 空や空白だけなら必須の文言だけを出し保存しない(String username) {
		Browser browser = loggedIn(registerMember("山田"));

		String text = rejected(browser, username);

		assertThat(text).contains(REQUIRED_USERNAME).doesNotContain(USERNAME_LENGTH);
		assertThat(topPageText(browser)).contains("ログイン中: 山田");
	}

	@Test
	void 二十一文字なら文字数の文言を出し保存しない() {
		Browser browser = loggedIn(registerMember("山田"));

		String text = rejected(browser, "😀".repeat(21));

		assertThat(text).contains(USERNAME_LENGTH);
		assertThat(topPageText(browser)).contains("ログイン中: 山田");
	}

	@Test
	void 二十文字なら保存する() {
		Browser browser = loggedIn(registerMember("山田"));
		String twenty = "👨‍👩‍👧".repeat(20);

		assertRedirectTo(changeUsername(browser, memberId(browser), twenty, browser.csrf("/settings/username")), "/");

		assertThat(topPageText(browser)).contains("ログイン中: " + twenty);
	}

	@Test
	void HTMLやスクリプトは文字として表示する() {
		Browser browser = loggedIn(registerMember("山田"));
		String username = "<img src=x>";

		assertRedirectTo(changeUsername(browser, memberId(browser), username, browser.csrf("/settings/username")),
				"/");

		for (String path : new String[] { "/", "/settings/username" }) {
			String body = browser.get(path).body();
			assertThat(body).as(path).doesNotContain("<img").contains("&lt;img src=x&gt;");
		}
	}

	@Test
	void 条件を満たさないときに返す画面でも入力したHTMLは文字として返す() {
		Browser browser = loggedIn(registerMember("山田"));

		HttpResponse<String> response = changeUsername(browser, memberId(browser), "<b>" + "あ".repeat(20) + "</b>",
				browser.csrf("/settings/username"));

		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.body()).doesNotContain("<b>");
	}

	/** 条件を満たさない名前を送り、200 で変更画面が返ることを確かめて表示テキストを返す */
	private static String rejected(Browser browser, String username) {
		HttpResponse<String> response = changeUsername(browser, memberId(browser), username,
				browser.csrf("/settings/username"));
		assertThat(response.statusCode()).isEqualTo(200);
		String text = visibleText(response.body());
		assertThat(text).contains("ログイン中: 山田");
		return text;
	}

}
