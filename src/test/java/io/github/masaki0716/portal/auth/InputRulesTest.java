package io.github.masaki0716.portal.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/** 受け入れ条件 3・4・13 の「入力の扱い」: Unicode の空白を除く・見た目の1文字で数える */
class InputRulesTest {

	@ParameterizedTest
	@ValueSource(strings = { " ", "\t", "\n", "\u3000", "\u00A0", "\u2007", "\u202F", "\u2028", "\u0085" })
	void 前後のUnicodeの空白を除く(String space) {
		assertThat(InputRules.strip(space + "山田 太郎" + space + space)).isEqualTo("山田 太郎");
	}

	@Test
	void White_Spaceでない制御文字は除かない() {
		// U+001F は Character.isWhitespace では空白だが、Unicode の White_Space ではない
		assertThat(InputRules.strip("\u001Fabc")).isEqualTo("\u001Fabc");
	}

	@Test
	void nullは空として扱う() {
		assertThat(InputRules.strip(null)).isEmpty();
		assertThat(InputRules.isBlank(null)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "", " ", "\u3000\u3000", " \t\n" })
	void 空白だけなら空とみなす(String value) {
		assertThat(InputRules.isBlank(value)).isTrue();
	}

	@ParameterizedTest
	@CsvSource(delimiter = '|', value = {
			"abc | 3",
			"山田 | 2",
			"\uD83D\uDE00 | 1",
			"\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC67 | 1",
			"\uD83C\uDDEF\uD83C\uDDF5 | 1",
			"か\u3099 | 1",
			"e\u0301 | 1" })
	void 見た目の1文字で数える(String value, int expected) {
		assertThat(InputRules.length(value)).isEqualTo(expected);
	}

	@Test
	void UTF8のバイト数を数える() {
		assertThat(InputRules.utf8Bytes("abc")).isEqualTo(3);
		assertThat(InputRules.utf8Bytes("あ")).isEqualTo(3);
		assertThat(InputRules.utf8Bytes("\uD83D\uDE00")).isEqualTo(4);
	}

	@Test
	void メールアドレスは空白を除いて小文字にして比べる() {
		assertThat(InputRules.normalizeEmail("\u3000User@Example.COM ")).isEqualTo("user@example.com");
	}

}
