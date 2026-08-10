package com.bluenet.web.application.result.githuborg;

import java.util.List;

/**
 * GitHub 组织邀请管理结果对象。
 */
public class GitHubOrgInvitationAdminResult {

    private GitHubOrgInvitationAdminResult() {
    }

    /**
     * 单个用户邀请结果。所有结果统一包含 userId/success/reason 三个字段。
     */
    public record Detail(
            Long userId,
            boolean success,
            String reason) {
    }

    /**
     * 批量邀请汇总结果。
     */
    public record Batch(
            int total,
            int succeeded,
            int failed,
            List<Detail> details) {
    }
}
