package io.github.masaki0716.portal.auth;

import java.util.List;

/**
 * 会員登録とユーザー名の変更の入力の条件（受け入れ条件 2・3・13）。
 * 欄が空（空白だけを含む）のときは、必須の未入力の文言だけを返す。
 */
final class MemberRules {

	static final int MIN_PASSWORD_LENGTH = 8;

	/** BCrypt が扱える上限 */
	static final int MAX_PASSWORD_BYTES = 72;

	static final int MAX_USERNAME_LENGTH = 20;

	private MemberRules() {
	}

	/** メールアドレスは `@` を含むかだけを確かめる */
	static List<String> emailErrors(String email) {
		if (InputRules.isBlank(email)) {
			return List.of(AuthMessages.required("メールアドレス"));
		}
		return InputRules.strip(email).contains("@") ? List.of() : List.of(AuthMessages.EMAIL_FORMAT);
	}

	/** パスワードは空白を除かずに数える */
	static List<String> passwordErrors(String password) {
		if (InputRules.isBlank(password)) {
			return List.of(AuthMessages.required("パスワード"));
		}
		boolean valid = InputRules.length(password) >= MIN_PASSWORD_LENGTH
				&& InputRules.utf8Bytes(password) <= MAX_PASSWORD_BYTES;
		return valid ? List.of() : List.of(AuthMessages.PASSWORD_LENGTH);
	}

	/** ユーザー名は前後の空白を除いて数える（登録時と変更時の両方） */
	static List<String> usernameErrors(String username) {
		if (InputRules.isBlank(username)) {
			return List.of(AuthMessages.required("ユーザー名"));
		}
		return (InputRules.length(InputRules.strip(username)) <= MAX_USERNAME_LENGTH) ? List.of()
				: List.of(AuthMessages.USERNAME_LENGTH);
	}

}
