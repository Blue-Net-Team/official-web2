package com.bluenet.web.application.service.auth.strategy;

/**
 * 认证 provider 策略接口。
 *
 * @param <C>
 *            provider 接收的凭证上下文类型。
 * @param <R>
 *            provider 认证后的结果类型。
 */
public interface AuthProviderStrategy<C, R> {

    /**
     * 当前策略支持的认证类型。
     *
     * @return 认证类型。
     */
    AuthProviderType providerType();

    /**
     * 执行具体认证策略。
     *
     * @param credential
     *            凭证上下文。
     * @return 认证结果。
     */
    R authenticate(C credential);
}
