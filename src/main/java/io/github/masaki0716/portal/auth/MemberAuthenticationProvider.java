package io.github.masaki0716.portal.auth;

import java.util.List;
import java.util.Optional;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

/**
 * メールアドレスとパスワードでのログイン（受け入れ条件 5・6）。
 * ロック中は正しい値でも受け付けない。失敗の記録は SecurityConfiguration の失敗時の処理で行う。
 */
@Component
class MemberAuthenticationProvider implements AuthenticationProvider {

	/** ロック中のログイン */
	static class LoginLockedException extends AuthenticationException {

		LoginLockedException() {
			super(AuthMessages.LOCKED);
		}

	}

	private final MemberRepository memberRepository;

	private final LoginAttemptService loginAttemptService;

	private final PasswordEncoder passwordEncoder;

	/** 会員が見つからないときも同じだけ時間をかける（メールアドレスが登録済みかを応答時間で調べられないように） */
	private final String dummyHash;

	MemberAuthenticationProvider(MemberRepository memberRepository, LoginAttemptService loginAttemptService,
			PasswordEncoder passwordEncoder) {
		this.memberRepository = memberRepository;
		this.loginAttemptService = loginAttemptService;
		this.passwordEncoder = passwordEncoder;
		this.dummyHash = passwordEncoder.encode("dummy-password");
	}

	@Override
	public Authentication authenticate(Authentication authentication) {
		if (authentication.getDetails() instanceof WebAuthenticationDetails details
				&& loginAttemptService.isLocked(details.getRemoteAddress())) {
			throw new LoginLockedException();
		}
		String email = (String) authentication.getPrincipal();
		String password = (String) authentication.getCredentials();
		// BCrypt は 72 バイトを超えるパスワードを扱えない（登録もできないので、一致することはない）
		if (InputRules.isBlank(email) || InputRules.isBlank(password)
				|| InputRules.utf8Bytes(password) > MemberRules.MAX_PASSWORD_BYTES) {
			throw new BadCredentialsException(AuthMessages.LOGIN_FAILED);
		}
		Optional<MemberRepository.Credentials> credentials = memberRepository
			.findCredentials(InputRules.normalizeEmail(email));
		String hash = credentials.map(MemberRepository.Credentials::passwordHash).orElse(dummyHash);
		if (!passwordEncoder.matches(password, hash) || credentials.isEmpty()) {
			throw new BadCredentialsException(AuthMessages.LOGIN_FAILED);
		}
		return UsernamePasswordAuthenticationToken.authenticated(new MemberPrincipal(credentials.get().id()), null,
				List.of());
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
	}

}
