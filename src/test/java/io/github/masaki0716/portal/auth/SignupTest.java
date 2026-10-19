package io.github.masaki0716.portal.auth;

import static io.github.masaki0716.portal.auth.AuthMessages.EMAIL_FORMAT;
import static io.github.masaki0716.portal.auth.AuthMessages.EMAIL_TAKEN;
import static io.github.masaki0716.portal.auth.AuthMessages.PASSWORD_LENGTH;
import static io.github.masaki0716.portal.auth.AuthMessages.USERNAME_LENGTH;
import static io.github.masaki0716.portal.auth.AuthMessages.WRONG_INVITE_CODE;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import io.github.masaki0716.portal.support.AuthIntegrationTest;
import io.github.masaki0716.portal.support.Browser;

/**
 * 受け入れ条件 1〜4・13・14: 会員登録。
 * 文言は受け入れ条件の「画面の表示」と同じものを、テストでは文字列で直接書く（実装の定数を写しただけにならないように）。
 */
class SignupTest extends AuthIntegrationTest {

	private static final String REQUIRED_EMAIL = "メールアドレスを入力してください";

	private static final String REQUIRED_PASSWORD = "パスワードを入力してください";

	private static final String REQUIRED_USERNAME = "ユーザー名を入力してください";

	private static final String REQUIRED_INVITE_CODE = "招待コードを入力してください";

	private static final String SMILE = "😀";

	@Test
	void 文言は受け入れ条件のとおり() {
		assertThat(WRONG_INVITE_CODE).isEqualTo("招待コードが正しくありません");
		assertThat(PASSWORD_LENGTH).isEqualTo("パスワードは8文字以上、72バイト以下にしてください");
		assertThat(EMAIL_FORMAT).isEqualTo("メールアドレスの形式が正しくありません");
		assertThat(EMAIL_TAKEN).isEqualTo("このメールアドレスは登録済みです");
		assertThat(USERNAME_LENGTH).isEqualTo("ユーザー名は1文字以上20文字以下にしてください");
	}

	@Test
	void 正しい招待コードで登録するとログインへ移動し自動ではログインしない() {
		Browser browser = newBrowser();
		String email = uniqueEmail();

		HttpResponse<String> response = signup(browser, email, PASSWORD, "山田", INVITE_CODE);

		assertRedirectTo(response, "/login");
		assertThat(topPageText(browser)).doesNotContain("ログイン中:");
		assertRedirectTo(login(newBrowser(), email, PASSWORD), "/");
	}

	@Test
	void 招待コードが誤りなら登録されない() {
		String email = uniqueEmail();

		String text = rejected(signup(newBrowser(), email, PASSWORD, "山田", "wrong-code"));

		assertThat(text).contains(WRONG_INVITE_CODE);
		assertCannotLogin(email);
	}

	@Test
	void 招待コードは空白を除かずに比べる() {
		String text = rejected(signup(newBrowser(), uniqueEmail(), PASSWORD, "山田", " " + INVITE_CODE));

		assertThat(text).contains(WRONG_INVITE_CODE);
	}

	@ParameterizedTest
	@ValueSource(strings = { "", " ", "　" })
	void どの項目も空や空白だけなら必須の文言だけを出す(String blank) {
		String text = rejected(signup(newBrowser(), blank, blank, blank, blank));

		assertThat(text).contains(REQUIRED_EMAIL, REQUIRED_PASSWORD, REQUIRED_USERNAME, REQUIRED_INVITE_CODE);
		assertThat(text).doesNotContain(EMAIL_FORMAT, PASSWORD_LENGTH, USERNAME_LENGTH, WRONG_INVITE_CODE);
	}

	@Test
	void メールアドレスにアットが無ければ登録されない() {
		String text = rejected(signup(newBrowser(), "user.example.com", PASSWORD, "山田", INVITE_CODE));

		assertThat(text).contains(EMAIL_FORMAT);
	}

	@ParameterizedTest
	@ValueSource(strings = { "pass123", SMILE + SMILE + SMILE + SMILE + SMILE + SMILE + SMILE })
	void パスワードが7文字なら登録されない(String password) {
		String email = uniqueEmail();

		String text = rejected(signup(newBrowser(), email, password, "山田", INVITE_CODE));

		assertThat(text).contains(PASSWORD_LENGTH);
		assertCannotLogin(email);
	}

	@ParameterizedTest
	@ValueSource(strings = { "pass1234", "ああああああああああああああああああああああああ" })
	void パスワードは8文字以上72バイト以下なら登録できる(String password) {
		String email = uniqueEmail();

		assertRedirectTo(signup(newBrowser(), email, password, "山田", INVITE_CODE), "/login");
		assertRedirectTo(login(newBrowser(), email, password), "/");
	}

	@Test
	void パスワードが73バイトなら登録されない() {
		// 25文字・73バイト
		String text = rejected(signup(newBrowser(), uniqueEmail(), "あ".repeat(24) + "a", "山田", INVITE_CODE));

		assertThat(text).contains(PASSWORD_LENGTH);
	}

	@Test
	void パスワードの前後の空白も文字として扱う() {
		String email = uniqueEmail();
		String password = " pass12 ";

		assertRedirectTo(signup(newBrowser(), email, password, "山田", INVITE_CODE), "/login");
		assertRedirectTo(login(newBrowser(), email, password), "/");
		assertCannotLogin(email, "pass12");
	}

	@Test
	void 登録済みのメールアドレスは大文字小文字と前後の空白を区別せずに弾く() {
		String email = uniqueEmail();
		assertRedirectTo(signup(newBrowser(), email, PASSWORD, "山田", INVITE_CODE), "/login");

		String text = rejected(signup(newBrowser(), "　" + email.toUpperCase() + " ", PASSWORD, "佐藤",
				INVITE_CODE));

		assertThat(text).contains(EMAIL_TAKEN);
	}

	@Test
	void 招待コードが誤りや空なら登録済みかを判定しない() {
		String email = uniqueEmail();
		assertRedirectTo(signup(newBrowser(), email, PASSWORD, "山田", INVITE_CODE), "/login");

		String wrong = rejected(signup(newBrowser(), email, "short", "佐藤", "wrong-code"));
		String blank = rejected(signup(newBrowser(), email, PASSWORD, "佐藤", ""));

		assertThat(wrong).contains(WRONG_INVITE_CODE, PASSWORD_LENGTH).doesNotContain(EMAIL_TAKEN);
		assertThat(blank).contains(REQUIRED_INVITE_CODE).doesNotContain(EMAIL_TAKEN);
	}

	@Test
	void 当てはまる理由をすべて表示する() {
		String email = uniqueEmail();
		assertRedirectTo(signup(newBrowser(), email, PASSWORD, "山田", INVITE_CODE), "/login");

		String text = rejected(signup(newBrowser(), email, "short", "あ".repeat(21), INVITE_CODE));

		assertThat(text).contains(EMAIL_TAKEN, PASSWORD_LENGTH, USERNAME_LENGTH);
	}

	@Test
	void ユーザー名は前後の空白を除いて見た目で20文字まで登録できる() {
		String twenty = SMILE.repeat(19) + "が";
		Member member = new Member(uniqueEmail(), PASSWORD, "　" + twenty + " ");

		assertRedirectTo(signup(newBrowser(), member.email(), member.password(), member.username(), INVITE_CODE),
				"/login");

		assertThat(topPageText(loggedIn(member))).contains("ログイン中: " + twenty);
	}

	@Test
	void ユーザー名が21文字なら登録されない() {
		String text = rejected(signup(newBrowser(), uniqueEmail(), PASSWORD, "あ".repeat(21), INVITE_CODE));

		assertThat(text).contains(USERNAME_LENGTH);
	}

	@Test
	void ユーザー名はほかの会員と重複してよい() {
		registerMember("同じ名前");
		registerMember("同じ名前");
	}

	@Test
	void ユーザー名のHTMLは文字として表示する() {
		String username = "<script>x</script>";
		Member member = registerMember(username);

		HttpResponse<String> response = loggedIn(member).get("/");

		assertThat(response.body()).doesNotContain(username).contains("&lt;script&gt;");
		assertThat(visibleText(response.body())).contains("ログイン中: " + "&lt;script&gt;");
	}

	@Test
	void 登録できなかったとき入力したHTMLは文字として返す() {
		HttpResponse<String> response = signup(newBrowser(), "<b>x</b>", PASSWORD, "<i>" + "あ".repeat(20) + "</i>",
				INVITE_CODE);

		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.body()).doesNotContain("<b>x</b>", "<i>");
	}

	/** 登録されず、200 で登録画面が返ることを確かめて表示テキストを返す */
	private static String rejected(HttpResponse<String> response) {
		assertThat(response.statusCode()).isEqualTo(200);
		return visibleText(response.body());
	}

	private void assertCannotLogin(String email) {
		assertCannotLogin(email, PASSWORD);
	}

	private void assertCannotLogin(String email, String password) {
		assertRedirectTo(login(newBrowser(), email, password), "/login");
	}

}
