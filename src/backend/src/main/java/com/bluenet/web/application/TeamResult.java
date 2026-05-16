package com.bluenet.web.application;

import com.bluenet.web.domain.model.entity.AssessmentTeam;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 考核队伍应用层结果对象。
 * <p>
 * 封装了考核队伍相关操作返回给 API 层的数据。
 * </p>
 */
public record TeamResult(
        /** 队伍ID */
        Long id,
        /** 考核时间ID */
        Long assessmentTimeId,
        /** 队长ID */
        Long leaderId,
        /** 队伍名称 */
        String name,
        /** 邀请码 */
        String inviteCode,
        /** 队伍状态 */
        AssessmentTeam.TeamStatus status,
        /** 创建时间 */
        LocalDateTime createdAt,
        /** 成员列表 */
        List<TeamMemberResult> members) {

    /**
     * 队伍成员结果对象。
     */
    public record TeamMemberResult(
            /** 成员ID */
            Long id,
            /** 用户ID */
            Long userId,
            /** 用户名 */
            String username,
            /** 方向 */
            String direction,
            /** 头像文件ID */
            Long avatarFileId,
            /** 加入时间 */
            LocalDateTime joinedAt,
            /** 是否为队长 */
            boolean isLeader) {
    }
}
