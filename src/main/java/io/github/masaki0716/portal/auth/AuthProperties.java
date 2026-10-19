package io.github.masaki0716.portal.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.AssertTrue;

/**
 * 認証の設定。招待コードには既定値を置かず、環境変数 PORTAL_AUTH_INVITE_CODE で渡す（受け入れ条件 1・16）。
 * 無い・空・空白だけなら起動時に止まる。比べるときは空白を除かずそのまま使う。
 */
@Validated
@ConfigurationProperties("portal.auth")
public record AuthProperties(String inviteCode) {

	@AssertTrue(message = "招待コード（portal.auth.invite-code）を設定してください")
	public boolean isInviteCodeSet() {
		return !InputRules.isBlank(inviteCode);
	}

}
