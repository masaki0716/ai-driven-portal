package io.github.masaki0716.portal.auth;

import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * 会員。メールアドレスは小文字にした値（email_normalized）で探す。
 */
@Repository
public class MemberRepository {

	record Credentials(UUID id, String passwordHash) {
	}

	private final JdbcClient jdbc;

	public MemberRepository(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	Optional<Credentials> findCredentials(String normalizedEmail) {
		return jdbc.sql("select id, password_hash from member where email_normalized = :email")
			.param("email", normalizedEmail)
			.query((rs, rowNum) -> new Credentials(rs.getObject("id", UUID.class), rs.getString("password_hash")))
			.optional();
	}

	boolean existsByEmail(String normalizedEmail) {
		return jdbc.sql("select count(*) from member where email_normalized = :email")
			.param("email", normalizedEmail)
			.query(Long.class)
			.single() > 0;
	}

	/** 同じメールアドレスが同時に登録されたときは DuplicateKeyException */
	void insert(UUID id, String email, String normalizedEmail, String passwordHash, String username) {
		jdbc.sql("""
				insert into member (id, email, email_normalized, password_hash, username)
				values (:id, :email, :normalizedEmail, :passwordHash, :username)
				""")
			.param("id", id)
			.param("email", email)
			.param("normalizedEmail", normalizedEmail)
			.param("passwordHash", passwordHash)
			.param("username", username)
			.update();
	}

	public Optional<String> findUsername(UUID id) {
		return jdbc.sql("select username from member where id = :id")
			.param("id", id)
			.query(String.class)
			.optional();
	}

	void updateUsername(UUID id, String username) {
		jdbc.sql("update member set username = :username where id = :id")
			.param("id", id)
			.param("username", username)
			.update();
	}

}
