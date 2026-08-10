package com.bluenet.web.infrastructure.config;

/**
 * GitHub App 安装类型，决定获取 Installation ID 的 API 路径。
 */
public enum GitHubAppType {

    /** 仓库级安装，使用 /repos/{owner}/{repo}/installation */
    REPOSITORY,

    /** 组织级安装，使用 /orgs/{org}/installation */
    ORGANIZATION
}
