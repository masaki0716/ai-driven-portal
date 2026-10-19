package io.github.masaki0716.portal.auth;

import java.io.Serializable;
import java.util.UUID;

/**
 * ログイン中の会員。ユーザー名は変わるので持たず、表示のたびに ID で読む。
 */
public record MemberPrincipal(UUID id) implements Serializable {
}
