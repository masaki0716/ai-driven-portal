package io.github.masaki0716.portal.auth;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Service;

/**
 * 受け入れ条件 6: 同じ相手（接続元の IP アドレス）が、招待コードの誤りとログインの失敗を合わせて5回続けたら、
 * 5回目の失敗の時刻から15分は受け付けない。15分たったら回数を 0 から数え直す。
 * 時刻はアプリの時計（テスト用の設定では外から変えられる）で測る。
 */
@Service
public class LoginAttemptService {

	static final int MAX_FAILURES = 5;

	static final Duration LOCK_DURATION = Duration.ofMinutes(15);

	private final LoginAttemptRepository repository;

	private final Clock clock;

	public LoginAttemptService(LoginAttemptRepository repository, Clock clock) {
		this.repository = repository;
		this.clock = clock;
	}

	/** ロック中か。15分たったロックは記録ごと消す */
	boolean isLocked(String ip) {
		Optional<Instant> lockedAt = repository.findLockedAt(ip);
		if (lockedAt.isEmpty()) {
			return false;
		}
		if (clock.instant().isBefore(lockedAt.get().plus(LOCK_DURATION))) {
			return true;
		}
		repository.delete(ip);
		return false;
	}

	/** 失敗を記録し、この失敗でロックしたかを返す。ロック中は呼ばない（15分を延ばさない） */
	boolean recordFailure(String ip) {
		return repository.recordFailure(ip, clock.instant(), MAX_FAILURES);
	}

	/** ログインか登録に成功したら回数を 0 に戻す */
	void reset(String ip) {
		repository.delete(ip);
	}

}
