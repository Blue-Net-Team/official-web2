package com.bluenet.web.domain.model.vo;

/**
 * GitHub 组织邀请结果。
 *
 * @param success
 *            是否成功（已受邀或已是成员也视为成功）
 * @param reason
 *            结果说明（成功或失败原因）
 */
public record GitHubOrgInvitationResult(boolean success, String reason) {

    public static GitHubOrgInvitationResult success(String reason) {
        return new GitHubOrgInvitationResult(true, reason);
    }

    public static GitHubOrgInvitationResult failure(String reason) {
        return new GitHubOrgInvitationResult(false, reason);
    }
}
