package io.github.masaki0716.portal.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * 結合テスト用の PostgreSQL。ポートは Testcontainers が空いているものを選ぶので、手元の PostgreSQL とはぶつからない。
 * テスト時計の有効・無効でコンテキストが分かれても、コンテナは1つを使い回す。
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

	private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17-alpine");

	@Bean
	@ServiceConnection
	PostgreSQLContainer postgresContainer() {
		return POSTGRES;
	}

}
