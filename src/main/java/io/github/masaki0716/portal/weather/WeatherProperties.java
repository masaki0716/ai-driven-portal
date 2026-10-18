package io.github.masaki0716.portal.weather;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 天気の設定。地点には既定値を置かない（無い・不正なら起動時に止まる）。
 */
@Validated
@ConfigurationProperties("portal.weather")
public record WeatherProperties(@NotNull @Valid Location location, @DefaultValue @Valid Api api) {

	public record Location(
			@NotBlank String name,
			@NotNull @DecimalMin("-90") @DecimalMax("90") Double latitude,
			@NotNull @DecimalMin("-180") @DecimalMax("180") Double longitude) {

		/** NaN は範囲の検証をすり抜けるので、別に弾く */
		@AssertTrue(message = "緯度・経度は数値で指定してください")
		public boolean isCoordinatesFinite() {
			return (latitude == null || Double.isFinite(latitude)) && (longitude == null || Double.isFinite(longitude));
		}

	}

	public record Api(@DefaultValue("https://api.open-meteo.com") @NotBlank String baseUrl) {
	}

}
