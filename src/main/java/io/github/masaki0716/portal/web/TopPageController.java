package io.github.masaki0716.portal.web;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import io.github.masaki0716.portal.weather.WeatherService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * トップページ。天気 API が失敗しても 200 で返す。
 */
@Controller
public class TopPageController {

	/** ページ全体を共有キャッシュに保持させない（受け入れ条件 13） */
	static final String CACHE_CONTROL = "no-store, private";

	private final WeatherService weatherService;

	public TopPageController(WeatherService weatherService) {
		this.weatherService = weatherService;
	}

	@GetMapping("/")
	String index(Model model, HttpServletResponse response) {
		response.setHeader(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL);
		model.addAttribute("weather", weatherService.currentWeather());
		return "index";
	}

}
