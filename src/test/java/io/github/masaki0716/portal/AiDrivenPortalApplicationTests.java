package io.github.masaki0716.portal;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.github.masaki0716.portal.support.PortalIntegrationTest;

class AiDrivenPortalApplicationTests extends PortalIntegrationTest {

	@Test
	void contextLoads() {
	}

	@Test
	void 起動したらヘルスチェックが200を返す() {
		assertThat(get("/actuator/health").statusCode()).isEqualTo(200);
	}

}
