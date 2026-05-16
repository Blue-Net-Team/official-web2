package com.bluenet.web.domain.model.entity;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 考核队伍成员值对象
 */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AssessmentTeamMember {
    private Long id;
    private Long teamId;
    private Long userId;
    private LocalDateTime joinedAt;

    private AssessmentTeamMember(Long id, Long teamId, Long userId, LocalDateTime joinedAt) {
        this.id = id;
        this.teamId = teamId;
        this.userId = userId;
        this.joinedAt = joinedAt;
    }

    public static AssessmentTeamMember create(Long teamId, Long userId) {
        return new AssessmentTeamMember(null, teamId, userId, LocalDateTime.now());
    }

    public static AssessmentTeamMember reconstruct(Long id, Long teamId, Long userId, LocalDateTime joinedAt) {
        return new AssessmentTeamMember(id, teamId, userId, joinedAt);
    }
}
