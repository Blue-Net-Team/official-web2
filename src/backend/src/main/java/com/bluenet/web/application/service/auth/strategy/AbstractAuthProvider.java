package com.bluenet.web.application.service.auth.strategy;

/**
 * 认证 provider 抽象基类，固定策略识别逻辑，具体凭证校验交给子类实现。
 *
 * @param <C>
 *            provider 接收的凭证上下文类型。
 * @param <R>
 *            provider 认证后的结果类型。
 */
public abstract class AbstractAuthProvider<C, R> implements AuthProviderStrategy<C, R> {
    private final AuthProviderType providerType;

    protected AbstractAuthProvider(AuthProviderType providerType) {
        this.providerType = providerType;
    }

    @Override
    public final AuthProviderType providerType() {
        return providerType;
    }

    /**
     * 判断该 provider 是否支持指定认证类型。
     *
     * @param type
     *            待匹配的认证类型。
     * @return 类型匹配时返回 true。
     */
    public final boolean supports(AuthProviderType type) {
        return providerType == type;
    }
}
