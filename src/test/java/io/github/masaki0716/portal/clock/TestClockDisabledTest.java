package io.github.masaki0716.portal.clock;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import io.github.masaki0716.portal.support.PortalIntegrationTest;

/** 受け入れ条件 14: 本番の設定では口が効かない */
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

}
