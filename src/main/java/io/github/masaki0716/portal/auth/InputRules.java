package io.github.masaki0716.portal.auth;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * 入力の扱い（受け入れ条件の「入力の扱い」）。
 * 空白は Unicode の White_Space 属性で判定する（String.strip() は U+00A0 などを除かないため使わない）。
 * 文字数は拡張書記素クラスタ（見た目の1文字）で数える。
 */
final class InputRules {

	private static final Pattern EDGE_WHITE_SPACE = Pattern.compile("\\A\\p{IsWhite_Space}+|\\p{IsWhite_Space}+\\z");

	private static final Pattern GRAPHEME = Pattern.compile("\\X");

	private InputRules() {
	}

	/** 前後の Unicode の空白を除く。null は空とみなす */
	static String strip(String value) {
		return (value != null) ? EDGE_WHITE_SPACE.matcher(value).replaceAll("") : "";
	}

	/** 空、または空白だけ */
	static boolean isBlank(String value) {
		return strip(value).isEmpty();
	}

	/** 見た目の文字数 */
	static int length(String value) {
		return (int) GRAPHEME.matcher(value).results().count();
	}

	static int utf8Bytes(String value) {
		return value.getBytes(StandardCharsets.UTF_8).length;
	}

	/** メールアドレスの比べ方: 前後の空白を除き、大文字と小文字を区別しない */
	static String normalizeEmail(String value) {
		return strip(value).toLowerCase(Locale.ROOT);
	}

}
