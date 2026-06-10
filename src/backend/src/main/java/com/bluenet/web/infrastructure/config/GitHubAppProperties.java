package com.bluenet.web.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "github.app")
public class GitHubAppProperties {

    private Long appId;

    private String privateKeyPath;

    private String owner;

    private String repo;

    private String apiBaseUrl = "https://api.github.com";

    /** 应用自身域名，用于生成截图下载链接 */
    private String appBaseUrl = "http://localhost:8080";

    /** Webhook Secret，用于验证 GitHub Webhook 请求的 HMAC-SHA256 签名 */
    private String webhookSecret;

    /** 是否启用定时轮询同步。默认 true */
    private Boolean pollingEnabled = true;

    /** 轮询回溯天数。默认 7 天 */
    private Integer pollingSinceDays = 7;

    /**
     * 是否启用 GitHub Issue 同步。 当 appId 和 privateKeyPath 都配置时自动启用。
     */
    public boolean isEnabled() {
        return appId != null && appId > 0
                && privateKeyPath != null && !privateKeyPath.isBlank()
                && owner != null && !owner.isBlank()
                && repo != null && !repo.isBlank();
    }

    /**
     * 是否启用 GitHub Webhook 接收。当 webhookSecret 配置时启用。
     */
    public boolean isWebhookEnabled() {
        return webhookSecret != null && !webhookSecret.isBlank();
    }

    /**
     * 是否启用定时轮询同步。当 pollingEnabled 为 true 且 GitHub App 基本配置完整时启用。
     */
    public boolean isPollingEnabled() {
        return Boolean.TRUE.equals(pollingEnabled) && isEnabled();
    }
}
