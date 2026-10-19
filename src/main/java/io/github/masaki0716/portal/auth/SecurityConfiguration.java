package io.github.masaki0716.portal.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;

/**
 * ログインが要る画面（受け入れ条件 19）・ログインとログアウト（5・6）・CSRF（12）。
 * ログインしていなくても開けるのは、約束の一覧のパスだけ。ほかは、これから足す画面も含めてログインが要る。
 * CSRF の確認は Spring Security の標準どおりログインの確認より先に行う（未ログインでも 403）。
 */
@Configuration(proxyBeanMethods = false)
public class SecurityConfiguration {

	private static final RedirectStrategy REDIRECT = new DefaultRedirectStrategy();

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, LoginAttemptService loginAttemptService)
			throws Exception {
		http.authorizeHttpRequests(requests -> requests
			// 許可したパスで起きたエラー（テスト用の口が無効なときの 404 など）を /login に移動させない
			.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
			.requestMatchers(HttpMethod.GET, "/", "/actuator/health").permitAll()
			.requestMatchers("/login", "/signup", "/api/test/**").permitAll()
			.anyRequest().authenticated());
		// テスト用の口は、テスト用の設定のときだけ効くので _csrf を求めない
		http.csrf(csrf -> csrf.ignoringRequestMatchers("/api/test/**"));
		http.formLogin(login -> login.loginPage("/login")
			.usernameParameter("email")
			.passwordParameter("password")
			.successHandler((request, response, authentication) -> {
				loginAttemptService.reset(request.getRemoteAddr());
				// 直前に開こうとした画面には戻さない
				REDIRECT.sendRedirect(request, response, "/");
			})
			.failureHandler((request, response, exception) -> REDIRECT.sendRedirect(request, response,
					loginFailureUrl(request, exception, loginAttemptService))));
		http.logout(logout -> logout.logoutSuccessUrl("/"));
		// 直前に開こうとした画面は覚えない（ログイン後は常に /）
		http.requestCache(cache -> cache.disable());
		return http.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/** ロック中なら記録せずロックの文言だけ。それ以外は失敗を記録し、5回目ならロックの文言も並べる */
	private static String loginFailureUrl(HttpServletRequest request, AuthenticationException exception,
			LoginAttemptService loginAttemptService) {
		if (exception instanceof MemberAuthenticationProvider.LoginLockedException) {
			return "/login?locked";
		}
		return loginAttemptService.recordFailure(request.getRemoteAddr()) ? "/login?error&locked" : "/login?error";
	}

}
