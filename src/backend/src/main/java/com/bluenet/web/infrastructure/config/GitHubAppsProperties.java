package com.bluenet.web.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * GitHub Apps 统一配置中心。
 * <p>
 * 所有 GitHub App 配置位于 {@code github.apps.{name}.*} 下，通过名称获取对应配置。
 * </p>
 */
@Data
@Component
@ConfigurationProperties(prefix = "github")
public class GitHubAppsProperties {

    /** Issue 同步 App 的配置名称 */
    public static final String ISSUE_SYNC_APP_NAME = "issue-sync";

    /** 组织邀请 App 的配置名称 */
    public static final String ORG_INVITATION_APP_NAME = "org-invitation";

    /** App 名称 -> App 配置 */
    private Map<String, GitHubAppConfig> apps = new HashMap<>();

    /**
     * 按名称获取 App 配置。
     *
     * @param name
     *            App 配置名称（如 issue-sync、org-invitation）
     * @return 对应配置
     * @throws IllegalStateException
     *             配置不存在时抛出
     */
    public GitHubAppConfig getApp(String name) {
        GitHubAppConfig config = apps.get(name);
        if (config == null) {
            throw new IllegalStateException("GitHub App not configured: github.apps." + name);
        }
        return config;
    }

    /**
     * 按名称查找 App 配置，不存在时返回 null。
     */
    public GitHubAppConfig findApp(String name) {
        return apps.get(name);
    }
}
