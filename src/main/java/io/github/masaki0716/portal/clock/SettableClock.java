package io.github.masaki0716.portal.clock;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicReference;

/**
 * テスト用の時計。時刻を設定するまでは実時間で進み、設定したらその時刻で止まる。
 */
public class SettableClock extends Clock {

	private final Clock systemClock;

	private final AtomicReference<Instant> fixedInstant;

	public SettableClock(Clock systemClock) {
		this(systemClock, new AtomicReference<>());
	}

	private SettableClock(Clock systemClock, AtomicReference<Instant> fixedInstant) {
		this.systemClock = systemClock;
		this.fixedInstant = fixedInstant;
	}

	public void set(Instant instant) {
		fixedInstant.set(instant);
	}

	@Override
	public Instant instant() {
		Instant fixed = fixedInstant.get();
		return (fixed != null) ? fixed : systemClock.instant();
	}

	@Override
	public ZoneId getZone() {
		return systemClock.getZone();
	}

	@Override
	public Clock withZone(ZoneId zone) {
		return new SettableClock(systemClock.withZone(zone), fixedInstant);
	}

}
