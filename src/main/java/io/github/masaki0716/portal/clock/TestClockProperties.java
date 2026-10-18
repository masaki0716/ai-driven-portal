package io.github.masaki0716.portal.clock;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * テスト用の時計の設定。true のときだけ、時刻を変える口と保持を消す口が効く（本番は未指定・false）。
 */
@ConfigurationProperties("portal.test-clock")
public record TestClockProperties(@DefaultValue("false") boolean enabled) {
}
