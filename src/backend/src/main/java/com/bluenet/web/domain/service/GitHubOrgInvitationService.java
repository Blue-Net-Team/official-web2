package com.bluenet.web.domain.service;

import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.vo.GitHubOrgInvitationResult;

/**
 * GitHub 组织邀请领域服务。
 * <p>
 * 邀请用户加入 Blue-Net-Team 组织，并按用户方向分配到对应 GitHub Team。 优先使用用户绑定的 GitHub
 * ID（invitee_id）邀请，未绑定时回退到邮箱邀请。
 * </p>
 */
public interface GitHubOrgInvitationService {

    /**
     * 同步邀请用户加入 GitHub 组织。
     * <p>
     * 所有失败（配置未启用、API 异常等）都会转换为返回值，不抛出异常。
     * </p>
     *
     * @param user
     *            要邀请的用户
     * @return 邀请结果
     */
    GitHubOrgInvitationResult invite(User user);

    /**
     * 异步邀请用户加入 GitHub 组织。
     * <p>
     * 失败只记录日志，不影响调用方主流程。
     * </p>
     *
     * @param user
     *            要邀请的用户
     */
    void inviteAsync(User user);
}
