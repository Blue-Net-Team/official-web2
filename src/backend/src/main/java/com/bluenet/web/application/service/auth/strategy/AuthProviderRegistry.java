package com.bluenet.web.application.service.auth.strategy;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 认证 provider 策略注册表，集中完成策略选择。
 */
public class AuthProviderRegistry {
    private final Map<AuthProviderType, AuthProviderStrategy<?, ?>> providers = new EnumMap<>(AuthProviderType.class);

    public AuthProviderRegistry(List<? extends AuthProviderStrategy<?, ?>> providers) {
        providers.forEach(provider -> this.providers.put(provider.providerType(), provider));
    }

    /**
     * 根据认证类型选择 provider 并执行认证。
     *
     * @param type
     *            认证类型。
     * @param credential
     *            凭证上下文。
     * @param <C>
     *            凭证上下文类型。
     * @param <R>
     *            认证结果类型。
     * @return 认证结果。
     */
    @SuppressWarnings("unchecked")
    public <C, R> R authenticate(AuthProviderType type, C credential) {
        AuthProviderStrategy<C, R> provider = (AuthProviderStrategy<C, R>) providers.get(type);
        if (provider == null) {
            throw new IllegalArgumentException("Unsupported auth provider: " + type);
        }
        return provider.authenticate(credential);
    }

    /**
     * 获取特定类型的 provider，用于 GitHub 绑定等 provider 扩展能力。
     *
     * @param type
     *            认证类型。
     * @param providerClass
     *            provider 实现类型。
     * @param <T>
     *            provider 实现类型。
     * @return provider 实例。
     */
    public <T extends AuthProviderStrategy<?, ?>> T get(AuthProviderType type, Class<T> providerClass) {
        AuthProviderStrategy<?, ?> provider = providers.get(type);
        if (!providerClass.isInstance(provider)) {
            throw new IllegalArgumentException("Unsupported auth provider: " + type);
        }
        return providerClass.cast(provider);
    }
}
