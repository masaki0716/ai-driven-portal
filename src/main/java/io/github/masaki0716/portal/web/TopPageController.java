package io.github.masaki0716.portal.web;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import io.github.masaki0716.portal.auth.MemberPrincipal;
import io.github.masaki0716.portal.auth.MemberRepository;
import io.github.masaki0716.portal.weather.WeatherService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * トップページ。天気 API が失敗しても 200 で返す。ログインしていなくても開ける（認証の受け入れ条件 15）。
 */
@Controller
public class TopPageController {

	/** ページ全体を共有キャッシュに保持させない（受け入れ条件 13） */
	static final String CACHE_CONTROL = "no-store, private";

	private final WeatherService weatherService;

	private final MemberRepository memberRepository;

	public TopPageController(WeatherService weatherService, MemberRepository memberRepository) {
		this.weatherService = weatherService;
		this.memberRepository = memberRepository;
	}

	@GetMapping("/")
	String index(@AuthenticationPrincipal MemberPrincipal member, Model model, HttpServletResponse response) {
		response.setHeader(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL);
		model.addAttribute("weather", weatherService.currentWeather());
		// 上部の表示（認証の受け入れ条件 5・10）。変更がすぐ反映されるよう、毎回読む
		if (member != null) {
			model.addAttribute("loginUsername", memberRepository.findUsername(member.id()).orElse(null));
		}
		return "index";
	}

}
