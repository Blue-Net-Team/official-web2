package com.bluenet.web.application.result.team;

import com.bluenet.web.domain.model.entity.AssessmentTeam;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 考核队伍预览应用层结果对象。
 * <p>
 * 封装了通过邀请码预览队伍信息返回给 API 层的数据。
 * </p>
 */
public record TeamPreviewResult(
        /** 队伍ID */
        Long id,
        /** 考核时间ID */
        Long assessmentTimeId,
        /** 队长用户名 */
        String leaderUsername,
        /** 队伍名称 */
        String name,
        /** 队伍状态 */
        AssessmentTeam.TeamStatus status,
        /** 创建时间 */
        LocalDateTime createdAt,
        /** 当前成员数量 */
        int memberCount,
        /** 成员列表（仅包含用户名） */
        List<String> memberUsernames) {
}
