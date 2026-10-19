package io.github.masaki0716.portal.auth;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

/**
 * ユーザー名の変更（受け入れ条件 8〜13）。ログインが要ることは SecurityConfiguration が決める。
 * 会員の ID を URL に入れ、ログイン中の会員の ID と一致しなければ 403 にする（リソースごとの認可チェック、11）。
 */
@Controller
public class UsernameController {

	private final MemberRepository memberRepository;

	public UsernameController(MemberRepository memberRepository) {
		this.memberRepository = memberRepository;
	}

	@GetMapping("/settings/username")
	String form(@AuthenticationPrincipal MemberPrincipal member, Model model) {
		String current = currentUsername(member);
		return render(model, member, current, current, List.of());
	}

	@PostMapping("/users/{id}/username")
	String change(@PathVariable String id, @RequestParam(defaultValue = "") String username,
			@AuthenticationPrincipal MemberPrincipal member, Model model) {
		// 入力の検証より先に、自分の ID かを確かめる（形の違う値・存在しない ID も 403）
		if (!member.id().toString().equals(id)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}
		List<String> errors = MemberRules.usernameErrors(username);
		if (!errors.isEmpty()) {
			return render(model, member, currentUsername(member), username, errors);
		}
		memberRepository.updateUsername(member.id(), InputRules.strip(username));
		return "redirect:/";
	}

	private String currentUsername(MemberPrincipal member) {
		return memberRepository.findUsername(member.id()).orElseThrow();
	}

	/** loginUsername は上部の表示、input は入力欄に戻す値 */
	private static String render(Model model, MemberPrincipal member, String loginUsername, String input,
			List<String> errors) {
		model.addAttribute("loginUsername", loginUsername);
		model.addAttribute("memberId", member.id().toString());
		model.addAttribute("username", input);
		model.addAttribute("errors", errors);
		return "username";
	}

}
