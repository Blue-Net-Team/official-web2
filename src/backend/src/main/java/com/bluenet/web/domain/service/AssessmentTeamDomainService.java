package com.bluenet.web.domain.service;

import com.bluenet.web.domain.model.entity.AssessmentTeam;
import com.bluenet.web.domain.model.entity.AssessmentTime;

/**
 * 考核队伍领域服务。
 * <p>
 * 封装组队生命周期的跨聚合业务规则。本服务只负责校验、生成/修改领域实体，不直接调用 Repository
 * 进行持久化；持久化与事务由应用服务控制，以保证原子性。
 * </p>
 */
public interface AssessmentTeamDomainService {

    /**
     * 校验并准备待创建的队伍实体（未持久化，含唯一邀请码）。
     *
     * @param userId
     *            当前用户ID
     * @param assessmentTime
     *            考核时间
     * @param name
     *            队伍名称
     * @return 创建后的队伍实体
     */
    AssessmentTeam prepareNewTeam(Long userId, AssessmentTime assessmentTime, String name);

    /**
     * 校验当前用户是否可以加入指定队伍。
     *
     * @param userId
     *            当前用户ID
     * @param team
     *            目标队伍
     * @param assessmentTime
     *            队伍所属考核时间
     */
    void validateCanJoinTeam(Long userId, AssessmentTeam team, AssessmentTime assessmentTime);

    /**
     * 校验当前用户是否可以离开指定队伍。
     *
     * @param userId
     *            当前用户ID
     * @param team
     *            目标队伍
     */
    void validateCanLeaveTeam(Long userId, AssessmentTeam team);

    /**
     * 校验并准备队长转让后的队伍实体（未持久化）。
     *
     * @param userId
     *            当前用户ID
     * @param team
     *            目标队伍
     * @param newLeaderId
     *            新队长用户ID
     * @return 队长已更新的队伍实体
     */
    AssessmentTeam prepareLeaderTransfer(Long userId, AssessmentTeam team, Long newLeaderId);

    /**
     * 校验并准备解散后的队伍实体（未持久化）。
     *
     * @param userId
     *            当前用户ID
     * @param team
     *            目标队伍
     * @return 状态已更新为解散的队伍实体
     */
    AssessmentTeam prepareDisband(Long userId, AssessmentTeam team);

    /**
     * 校验预览队伍时考核是否仍可进行。
     *
     * @param team
     *            目标队伍
     * @param assessmentTime
     *            队伍所属考核时间
     */
    void validateTeamPreviewable(AssessmentTeam team, AssessmentTime assessmentTime);
}
