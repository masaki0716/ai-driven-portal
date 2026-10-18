package io.github.masaki0716.portal.weather;

/**
 * 天気 API の応答が想定外の形式だった、または今日・明日の天気そのものが無かった。
 */
public class UnexpectedWeatherResponseException extends RuntimeException {

	public UnexpectedWeatherResponseException(String message) {
		super(message);
	}

	public UnexpectedWeatherResponseException(String message, Throwable cause) {
		super(message, cause);
	}

}
