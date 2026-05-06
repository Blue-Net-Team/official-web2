package com.bluenet.web.infrastructure.github;

/**
 * GitHub Issue 创建结果
 */
public record GitHubIssueCreateResult(
        /** Issue 编号 */
        Integer number,
        /** Issue HTML URL */
        String htmlUrl,
        /** Issue 标题 */
        String title) {
}
