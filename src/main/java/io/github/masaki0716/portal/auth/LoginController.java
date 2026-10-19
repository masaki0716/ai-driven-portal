package io.github.masaki0716.portal.auth;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * ログイン画面（受け入れ条件 5・6）。送られたフォームは Spring Security が受け、失敗したらクエリを付けてここへ戻す。
 */
@Controller
public class LoginController {

	@GetMapping("/login")
	String login(@RequestParam(required = false) String error, @RequestParam(required = false) String locked,
			Model model) {
		List<String> errors = new ArrayList<>();
		if (error != null) {
			errors.add(AuthMessages.LOGIN_FAILED);
		}
		if (locked != null) {
			errors.add(AuthMessages.LOCKED);
		}
		model.addAttribute("errors", errors);
		return "login";
	}

}
