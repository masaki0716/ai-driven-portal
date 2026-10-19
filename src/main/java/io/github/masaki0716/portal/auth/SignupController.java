package io.github.masaki0716.portal.auth;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 会員登録（受け入れ条件 1〜4・6・13）。条件を1つでも満たさなければ登録せず、当てはまる理由をすべて表示する。
 */
@Controller
public class SignupController {

	private final AuthProperties authProperties;

	private final MemberRepository memberRepository;

	private final LoginAttemptService loginAttemptService;

	private final PasswordEncoder passwordEncoder;

	public SignupController(AuthProperties authProperties, MemberRepository memberRepository,
			LoginAttemptService loginAttemptService, PasswordEncoder passwordEncoder) {
		this.authProperties = authProperties;
		this.memberRepository = memberRepository;
		this.loginAttemptService = loginAttemptService;
		this.passwordEncoder = passwordEncoder;
	}

	@GetMapping("/signup")
	String form(Model model) {
		return render(model, "", "", List.of());
	}

	@PostMapping("/signup")
	String signup(@RequestParam(defaultValue = "") String email, @RequestParam(defaultValue = "") String password,
			@RequestParam(defaultValue = "") String username, @RequestParam(defaultValue = "") String inviteCode,
			HttpServletRequest request, Model model) {
		String ip = request.getRemoteAddr();
		// ロック中は入力を確かめず、ロックの文言だけを出す
		if (loginAttemptService.isLocked(ip)) {
			return render(model, email, username, List.of(AuthMessages.LOCKED));
		}

		List<String> errors = new ArrayList<>();
		errors.addAll(MemberRules.emailErrors(email));
		errors.addAll(MemberRules.passwordErrors(password));
		errors.addAll(MemberRules.usernameErrors(username));
		boolean inviteCodeBlank = InputRules.isBlank(inviteCode);
		boolean inviteCodeCorrect = !inviteCodeBlank && inviteCode.equals(authProperties.inviteCode());
		if (inviteCodeBlank) {
			errors.add(AuthMessages.required("招待コード"));
		}
		else if (!inviteCodeCorrect) {
			errors.add(AuthMessages.WRONG_INVITE_CODE);
		}
		// 招待されていない人が会員のメールアドレスを調べられないよう、登録済みかは正しい招待コードのときだけ判定する
		String normalizedEmail = InputRules.normalizeEmail(email);
		boolean emailValid = MemberRules.emailErrors(email).isEmpty();
		if (inviteCodeCorrect && emailValid && memberRepository.existsByEmail(normalizedEmail)) {
			errors.add(AuthMessages.EMAIL_TAKEN);
		}
		// 数えるのは、招待コードが空でなく誤りのときだけ
		if (!inviteCodeBlank && !inviteCodeCorrect && loginAttemptService.recordFailure(ip)) {
			errors.add(AuthMessages.LOCKED);
		}
		if (!errors.isEmpty()) {
			return render(model, email, username, errors);
		}

		try {
			memberRepository.insert(UUID.randomUUID(), InputRules.strip(email), normalizedEmail,
					passwordEncoder.encode(password), InputRules.strip(username));
		}
		catch (DuplicateKeyException ex) {
			// 同じメールアドレスが同時に登録された
			return render(model, email, username, List.of(AuthMessages.EMAIL_TAKEN));
		}
		loginAttemptService.reset(ip);
		return "redirect:/login";
	}

	/** パスワードと招待コードは画面に戻さない */
	private static String render(Model model, String email, String username, List<String> errors) {
		model.addAttribute("email", email);
		model.addAttribute("username", username);
		model.addAttribute("errors", errors);
		return "signup";
	}

}
