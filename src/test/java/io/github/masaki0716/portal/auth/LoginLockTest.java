package io.github.masaki0716.portal.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.masaki0716.portal.support.AuthIntegrationTest;
import io.github.masaki0716.portal.support.Browser;

/**
 * 受け入れ条件 6・17: 招待コードの誤りとログインの失敗を合わせて5回続けたら15分受け付けない。
 * ロック中の試行は入力を確かめず、ロック中の文言だけを出す（ユーザーと合意した読み方。受け入れ条件への追記を提案中）。
 */
class LoginLockTest extends AuthIntegrationTest {

	private static final String LOGIN_FAILED = "メールアドレスまたはパスワードが正しくありません";

	private static final String LOCKED = "失敗が続いたため、15分間受け付けません";

	private static final String WRONG_INVITE_CODE = "招待コードが正しくありません";

	private Member member;

	@BeforeEach
	void registerTarget() {
		member = registerMember("山田");
	}

	@Test
	void ログインを5回続けて失敗すると5回目から失敗とロック中の文言を出す() {
		for (int i = 1; i <= 4; i++) {
			assertThat(failLogin()).as("%d回目", i).contains(LOGIN_FAILED).doesNotContain(LOCKED);
		}

		assertThat(failLogin()).contains(LOGIN_FAILED, LOCKED);
	}

	@Test
	void ロック中は正しい値でもログインできずロック中の文言だけを出す() {
		lockOut();

		Browser browser = newBrowser();
		HttpResponse<String> response = login(browser, member.email(), member.password());

		assertRedirectTo(response, "/login");
		assertThat(visibleText(follow(browser, response).body())).contains(LOCKED).doesNotContain(LOGIN_FAILED);
		assertThat(topPageText(browser)).doesNotContain("ログイン中:");
	}

	@Test
	void ロック中は正しい値でも登録できずロック中の文言だけを出す() {
		lockOut();
		String email = uniqueEmail();

		HttpResponse<String> response = signup(newBrowser(), email, "short", "山田", INVITE_CODE);

		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(visibleText(response.body())).contains(LOCKED)
			.doesNotContain("パスワードは8文字以上", WRONG_INVITE_CODE);
		setClock("2026-10-17T09:15:00+09:00");
		assertRedirectTo(login(newBrowser(), email, "short"), "/login");
	}

	@Test
	void 五回目の失敗から15分未満は受け付けず15分たったら受け付ける() {
		lockOut();

		setClock("2026-10-17T09:14:59+09:00");
		assertRedirectTo(login(newBrowser(), member.email(), member.password()), "/login");

		setClock("2026-10-17T09:15:00+09:00");
		assertRedirectTo(login(newBrowser(), member.email(), member.password()), "/");
	}

	@Test
	void ロック中の試行は15分を延ばさない() {
		lockOut();
		setClock("2026-10-17T09:10:00+09:00");
		failLogin();
		signup(newBrowser(), uniqueEmail(), PASSWORD, "山田", "wrong-code");

		setClock("2026-10-17T09:15:00+09:00");

		assertRedirectTo(login(newBrowser(), member.email(), member.password()), "/");
	}

	@Test
	void 十五分たったら回数は0から数え直す() {
		lockOut();
		setClock("2026-10-17T09:15:00+09:00");

		for (int i = 1; i <= 4; i++) {
			assertThat(failLogin()).as("%d回目", i).doesNotContain(LOCKED);
		}
		assertThat(failLogin()).contains(LOCKED);
	}

	@Test
	void ログインに成功すると回数を0に戻す() {
		failLogins(4);
		loggedIn(member);

		failLogins(4);
		assertThat(failLogin()).contains(LOCKED);
	}

	@Test
	void 登録に成功すると回数を0に戻す() {
		failLogins(4);
		registerMember("佐藤");

		assertThat(failLogin()).doesNotContain(LOCKED);
	}

	@Test
	void 招待コードの誤りとログインの失敗を合わせて数える() {
		failLogins(3);
		assertThat(wrongInviteCode()).contains(WRONG_INVITE_CODE).doesNotContain(LOCKED);

		assertThat(wrongInviteCode()).contains(WRONG_INVITE_CODE, LOCKED);
		assertRedirectTo(login(newBrowser(), member.email(), member.password()), "/login");
	}

	@Test
	void 空の招待コードと正しい招待コードでのほかの誤りは数えず0にも戻さない() {
		failLogins(4);

		signup(newBrowser(), uniqueEmail(), PASSWORD, "山田", " ");
		signup(newBrowser(), uniqueEmail(), "short", "山田", INVITE_CODE);
		assertThat(failLogin()).contains(LOCKED);
	}

	@Test
	void 失敗の記録を消すとロックが解ける() {
		lockOut();

		assertThat(delete("/api/test/login-attempts").statusCode()).isEqualTo(204);

		assertRedirectTo(login(newBrowser(), member.email(), member.password()), "/");
	}

	/** 5回続けて失敗させる（5回目は T0） */
	private void lockOut() {
		failLogins(5);
	}

	private void failLogins(int times) {
		for (int i = 0; i < times; i++) {
			failLogin();
		}
	}

	/** ログインに失敗し、戻った画面の表示テキストを返す */
	private String failLogin() {
		Browser browser = newBrowser();
		HttpResponse<String> response = login(browser, member.email(), "wrong-password");
		assertRedirectTo(response, "/login");
		return visibleText(follow(browser, response).body());
	}

	private String wrongInviteCode() {
		HttpResponse<String> response = signup(newBrowser(), uniqueEmail(), PASSWORD, "山田", "wrong-code");
		assertThat(response.statusCode()).isEqualTo(200);
		return visibleText(response.body());
	}

}
