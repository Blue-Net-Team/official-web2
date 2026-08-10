package com.bluenet.web.application.service;

import com.bluenet.web.application.result.githuborg.GitHubOrgInvitationAdminResult;

import java.util.List;

/**
 * GitHub 组织邀请管理应用服务。
 */
public interface GitHubOrgInvitationAdminAppService {

    /**
     * 邀请单个用户加入 GitHub 组织。
     *
     * @param userId
     *            用户 ID
     * @return 邀请结果
     */
    GitHubOrgInvitationAdminResult.Detail inviteUser(Long userId);

    /**
     * 批量邀请用户加入 GitHub 组织。
     * <p>
     * 逐个处理，单个失败不影响其他用户。
     * </p>
     *
     * @param userIds
     *            用户 ID 列表
     * @return 批量邀请汇总结果
     */
    GitHubOrgInvitationAdminResult.Batch inviteBatch(List<Long> userIds);
}
