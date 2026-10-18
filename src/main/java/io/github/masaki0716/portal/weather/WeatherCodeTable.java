package io.github.masaki0716.portal.weather;

/**
 * WMO の天気コードを日本語にする（受け入れ条件の変換表のとおり）。
 */
public final class WeatherCodeTable {

	private WeatherCodeTable() {
	}

	public static String describe(int code) {
		return switch (code) {
			case 0, 1 -> "晴れ";
			case 2 -> "晴れ時々曇り";
			case 3 -> "曇り";
			case 45, 48 -> "霧";
			case 51, 52, 53, 54, 55, 56, 57 -> "霧雨";
			case 61, 62, 63, 64, 65, 66, 67 -> "雨";
			case 71, 72, 73, 74, 75, 76, 77 -> "雪";
			case 80, 81, 82 -> "にわか雨";
			case 85, 86 -> "にわか雪";
			case 95, 96, 97, 98, 99 -> "雷雨";
			default -> WeatherFormat.MISSING;
		};
	}

}
