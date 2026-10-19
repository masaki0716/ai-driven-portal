package io.github.masaki0716.portal.auth;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * 失敗の記録（受け入れ条件 6）。接続元の IP アドレスごとに、失敗の回数とロックした時刻を持つ。
 */
@Repository
public class LoginAttemptRepository {

	private final JdbcClient jdbc;

	public LoginAttemptRepository(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	Optional<Instant> findLockedAt(String ip) {
		return jdbc.sql("select locked_at from login_attempt where ip = :ip and locked_at is not null")
			.param("ip", ip)
			.query((rs, rowNum) -> rs.getObject("locked_at", OffsetDateTime.class).toInstant())
			.optional();
	}

	/** 失敗を1回足す。maxFailures 回目（2 以上）なら failedAt でロックし、ロックしているかを返す */
	boolean recordFailure(String ip, Instant failedAt, int maxFailures) {
		return jdbc.sql("""
				insert into login_attempt (ip, failure_count, locked_at)
				values (:ip, 1, null)
				on conflict (ip) do update set
				    failure_count = login_attempt.failure_count + 1,
				    locked_at = coalesce(login_attempt.locked_at,
				        case when login_attempt.failure_count + 1 >= cast(:maxFailures as integer)
				            then cast(:failedAt as timestamptz) end)
				returning locked_at is not null
				""")
			.param("ip", ip)
			.param("failedAt", OffsetDateTime.ofInstant(failedAt, ZoneOffset.UTC))
			.param("maxFailures", maxFailures)
			.query(Boolean.class)
			.single();
	}

	void delete(String ip) {
		jdbc.sql("delete from login_attempt where ip = :ip").param("ip", ip).update();
	}

	public void deleteAll() {
		jdbc.sql("delete from login_attempt").update();
	}

}
