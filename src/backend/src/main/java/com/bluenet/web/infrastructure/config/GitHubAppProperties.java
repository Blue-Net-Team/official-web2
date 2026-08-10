package com.bluenet.web.infrastructure.config;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Issue 同步 App 配置的过渡层（兼容旧代码）。
 * <p>
 * 配置已迁移至 {@code github.apps.issue-sync.*}，由 {@link GitHubAppsProperties} 统一承载。
 * 本类仅为保留原有注入点与方法的委托 shim，新代码请直接使用 {@link GitHubAppsProperties}。
 * </p>
 */
@Component
@RequiredArgsConstructor
public class GitHubAppProperties {

    private static final GitHubAppConfig EMPTY = new GitHubAppConfig();

    private final GitHubAppsProperties appsProperties;

    private GitHubAppConfig config() {
        GitHubAppConfig config = appsProperties.findApp(GitHubAppsProperties.ISSUE_SYNC_APP_NAME);
        return config != null ? config : EMPTY;
    }

    public Long getAppId() {
        return config().getAppId();
    }

    public String getPrivateKeyPath() {
        return config().getPrivateKeyPath();
    }

    public String getOwner() {
        return config().getOwner();
    }

    public String getRepo() {
        return config().getRepo();
    }

    public String getApiBaseUrl() {
        return config().getApiBaseUrl();
    }

    /** 应用自身域名，用于生成截图下载链接 */
    public String getAppBaseUrl() {
        return config().getAppBaseUrl();
    }

    /** Webhook Secret，用于验证 GitHub Webhook 请求的 HMAC-SHA256 签名 */
    public String getWebhookSecret() {
        return config().getWebhookSecret();
    }

    public Boolean getPollingEnabled() {
        return config().getPollingEnabled();
    }

    /** 轮询回溯天数 */
    public Integer getPollingSinceDays() {
        return config().getPollingSinceDays();
    }

    /**
     * 是否启用 GitHub Issue 同步。当 appId 和 privateKeyPath 都配置时自动启用。
     */
    public boolean isEnabled() {
        return config().isEnabled();
    }

    /**
     * 是否启用 GitHub Webhook 接收。当 webhookSecret 配置时启用。
     */
    public boolean isWebhookEnabled() {
        String webhookSecret = config().getWebhookSecret();
        return webhookSecret != null && !webhookSecret.isBlank();
    }

    /**
     * 是否启用定时轮询同步。当 pollingEnabled 为 true 且 GitHub App 基本配置完整时启用。
     */
    public boolean isPollingEnabled() {
        return Boolean.TRUE.equals(config().getPollingEnabled()) && isEnabled();
    }
}
