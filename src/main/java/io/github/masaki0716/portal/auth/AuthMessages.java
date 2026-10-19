package io.github.masaki0716.portal.auth;

/**
 * 画面に出す文言（受け入れ条件の「画面の表示」）。受け入れテストはこの文言が表示テキストに含まれるかで確かめる。
 */
final class AuthMessages {

	static final String WRONG_INVITE_CODE = "招待コードが正しくありません";

	static final String PASSWORD_LENGTH = "パスワードは8文字以上、72バイト以下にしてください";

	static final String EMAIL_FORMAT = "メールアドレスの形式が正しくありません";

	static final String EMAIL_TAKEN = "このメールアドレスは登録済みです";

	static final String LOGIN_FAILED = "メールアドレスまたはパスワードが正しくありません";

	static final String LOCKED = "失敗が続いたため、15分間受け付けません";

	static final String USERNAME_LENGTH = "ユーザー名は1文字以上20文字以下にしてください";

	private AuthMessages() {
	}

	/** 必須の未入力。項目名は「メールアドレス」「パスワード」「ユーザー名」「招待コード」 */
	static String required(String fieldName) {
		return fieldName + "を入力してください";
	}

}
