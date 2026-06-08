package com.bluenet.web.application.service;

import com.bluenet.web.application.TeamPreviewResult;
import com.bluenet.web.application.TeamResult;

/**
 * 考核队伍应用服务接口。
 * <p>
 * 定义了考核队伍聚合在应用层的所有业务操作。
 * </p>
 */
public interface AssessmentTeamAppService {

    /**
     * 创建队伍。
     *
     * @param userId
     *            当前用户ID
     * @param assessmentTimeId
     *            考核时间ID
     * @param name
     *            队伍名称
     * @return 创建后的队伍结果
     */
    TeamResult createTeam(Long userId, Long assessmentTimeId, String name);

    /**
     * 通过邀请码预览队伍信息。
     *
     * @param inviteCode
     *            邀请码
     * @return 队伍预览结果
     */
    TeamPreviewResult previewTeam(String inviteCode);

    /**
     * 通过邀请码加入队伍。
     *
     * @param userId
     *            当前用户ID
     * @param inviteCode
     *            邀请码
     * @return 加入后的队伍结果
     */
    TeamResult joinTeam(Long userId, String inviteCode);

    /**
     * 获取当前用户在指定考核时间下的队伍。
     *
     * @param userId
     *            当前用户ID
     * @param assessmentTimeId
     *            考核时间ID
     * @return 队伍结果，未加入队伍时返回 null
     */
    TeamResult getMyTeam(Long userId, Long assessmentTimeId);

    /**
     * 离开队伍。
     *
     * @param userId
     *            当前用户ID
     * @param teamId
     *            队伍ID
     */
    void leaveTeam(Long userId, Long teamId);

    /**
     * 转让队长。
     *
     * @param userId
     *            当前用户ID
     * @param teamId
     *            队伍ID
     * @param newLeaderId
     *            新队长用户ID
     * @return 转让后的队伍结果
     */
    TeamResult transferLeader(Long userId, Long teamId, Long newLeaderId);

    /**
     * 解散队伍。
     *
     * @param userId
     *            当前用户ID
     * @param teamId
     *            队伍ID
     */
    void disbandTeam(Long userId, Long teamId);
}
