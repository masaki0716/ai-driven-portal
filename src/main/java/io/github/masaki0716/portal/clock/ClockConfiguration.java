package io.github.masaki0716.portal.clock;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * アプリが使う現在時刻。テスト用の設定のときだけ外から変えられる時計にする。
 */
@Configuration(proxyBeanMethods = false)
public class ClockConfiguration {

	@Bean
	Clock clock(TestClockProperties properties) {
		return properties.enabled() ? new SettableClock(Clock.systemUTC()) : Clock.systemUTC();
	}

}
