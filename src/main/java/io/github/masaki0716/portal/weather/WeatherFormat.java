package io.github.masaki0716.portal.weather;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 画面に出す値の形（受け入れ条件の「画面の表示」のとおり）。
 */
public final class WeatherFormat {

	/** 無い項目（U+2014） */
	public static final String MISSING = "—";

	public static final ZoneId JST = ZoneId.of("Asia/Tokyo");

	public static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	public static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

	public static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	private WeatherFormat() {
	}

	/** 0 から遠い方へ四捨五入した整数に「℃」 */
	public static String temperature(Double value) {
		return value == null ? MISSING : round(value) + "℃";
	}

	/** 整数に「%」 */
	public static String precipitation(Double value) {
		return value == null ? MISSING : round(value) + "%";
	}

	private static String round(double value) {
		return BigDecimal.valueOf(value).setScale(0, RoundingMode.HALF_UP).toPlainString();
	}

}
