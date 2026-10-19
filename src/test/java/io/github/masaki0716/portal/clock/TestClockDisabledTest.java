package io.github.masaki0716.portal.clock;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import io.github.masaki0716.portal.support.PortalIntegrationTest;

/** 天気予報の受け入れ条件 14・認証の 17・19: 本番の設定では口が効かない（ログインしていなくても /login へ移動させない） */
@TestPropertySource(properties = "portal.test-clock.enabled=false")
class TestClockDisabledTest extends PortalIntegrationTest {

	@Test
	void 時刻を変える口は404を返す() {
		assertThat(postJson("/api/test/clock", "{\"currentTime\": \"2026-10-17T00:00:00+09:00\"}").statusCode())
			.isEqualTo(404);
	}

	@Test
	void 形式が不正でも404を返す() {
		assertThat(postJson("/api/test/clock", "not json").statusCode()).isEqualTo(404);
	}

	@Test
	void 保持を消す口は404を返す() {
		assertThat(delete("/api/test/weather-cache").statusCode()).isEqualTo(404);
	}

	@Test
	void 失敗の記録を消す口は404を返す() {
		assertThat(delete("/api/test/login-attempts").statusCode()).isEqualTo(404);
	}

	@Test
	void 存在しない口も404を返す() {
		assertThat(get("/api/test/no-such").statusCode()).isEqualTo(404);
	}

}
