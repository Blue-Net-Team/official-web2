package com.bluenet.web.infrastructure.github;

/**
 * GitHub Issue 列表查询结果项
 */
public record GitHubIssueListResult(
        /** Issue 编号 */
        Integer number,
        /** Issue 标题 */
        String title,
        /** Issue Body */
        String body,
        /** Issue 状态：open / closed */
        String state,
        /** Issue HTML URL */
        String htmlUrl) {
}
