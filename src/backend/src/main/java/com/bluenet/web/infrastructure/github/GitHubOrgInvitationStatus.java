package com.bluenet.web.infrastructure.github;

/**
 * GitHub 组织邀请结果。
 */
public enum GitHubOrgInvitationStatus {

    /** 邀请已发送 */
    SENT,

    /** 用户已在组织中或已被邀请（GitHub 返回 422），不视为错误 */
    ALREADY_EXISTS
}
