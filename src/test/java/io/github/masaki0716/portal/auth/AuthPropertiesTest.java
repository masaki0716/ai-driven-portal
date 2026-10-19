package io.github.masaki0716.portal.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

/** 受け入れ条件 16: 招待コードの設定が無い・空なら起動時に止まる */
class AuthPropertiesTest {

	private final ApplicationContextRunner runner = new ApplicationContextRunner()
		.withUserConfiguration(PropertiesConfig.class);

	@Test
	void 招待コードがあれば起動する() {
		runner.withPropertyValues("portal.auth.invite-code=secret")
			.run(context -> {
				assertThat(context).hasNotFailed();
				assertThat(context.getBean(AuthProperties.class).inviteCode()).isEqualTo("secret");
			});
	}

	@Test
	void 招待コードの設定が無ければ起動に失敗する() {
		runner.run(context -> assertThat(context).hasFailed());
	}

	@ParameterizedTest
	@ValueSource(strings = { "", " ", "\u3000", "\u00A0" })
	void 招待コードが空や空白だけなら起動に失敗する(String inviteCode) {
		runner.withPropertyValues("portal.auth.invite-code=" + inviteCode)
			.run(context -> assertThat(context).hasFailed());
	}

	@Configuration(proxyBeanMethods = false)
	@EnableConfigurationProperties(AuthProperties.class)
	static class PropertiesConfig {

	}

}
