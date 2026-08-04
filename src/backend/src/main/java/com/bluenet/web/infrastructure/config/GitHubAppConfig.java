package com.bluenet.web.infrastructure.config;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 单个 GitHub App 的配置项。
 * <p>
 * 通用字段：appId、privateKeyPath、type、enabled、apiBaseUrl。 仓库级 App（type=repository）使用
 * owner/repo；组织级 App（type=organization）使用 org/teamMapping。
 * webhookSecret、polling*、appBaseUrl 为 Issue 同步 App 专用字段。
 * </p>
 */
@Data
public class GitHubAppConfig {

    private Long appId;

    private String privateKeyPath;

    /** 安装类型，决定 Installation ID 查询路径 */
    private GitHubAppType type = GitHubAppType.REPOSITORY;

    /** 是否启用，默认 true。设为 false 时强制禁用该 App */
    private Boolean enabled = true;

    /** 仓库所有者（type=repository 时使用） */
    private String owner;

    /** 仓库名（type=repository 时使用） */
    private String repo;

    /** 组织名（type=organization 时使用） */
    private String org;

    /** 方向枚举名 -> GitHub Team 名称映射（type=organization 时使用） */
    private Map<String, String> teamMapping = new HashMap<>();

    private String apiBaseUrl = "https://api.github.com";

    /** 应用自身域名，用于生成截图下载链接（Issue 同步专用） */
    private String appBaseUrl = "http://localhost:8080";

    /** Webhook Secret，用于验证 GitHub Webhook 请求的 HMAC-SHA256 签名（Issue 同步专用） */
    private String webhookSecret;

    /** 是否启用定时轮询同步。默认 true（Issue 同步专用） */
    private Boolean pollingEnabled = true;

    /** 轮询回溯天数。默认 7 天（Issue 同步专用） */
    private Integer pollingSinceDays = 7;

    /**
     * 该 App 是否可用。要求显式启用且必要配置完整。
     */
    public boolean isEnabled() {
        if (Boolean.FALSE.equals(enabled)) {
            return false;
        }
        boolean baseComplete = appId != null && appId > 0
                && privateKeyPath != null && !privateKeyPath.isBlank();
        if (!baseComplete) {
            return false;
        }
        GitHubAppType actualType = type == null ? GitHubAppType.REPOSITORY : type;
        return switch (actualType) {
            case REPOSITORY -> owner != null && !owner.isBlank()
                    && repo != null && !repo.isBlank();
            case ORGANIZATION -> org != null && !org.isBlank();
        };
    }
}
