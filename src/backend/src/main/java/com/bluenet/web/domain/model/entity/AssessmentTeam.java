package com.bluenet.web.domain.model.entity;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 考核队伍聚合根
 * <p>
 * 承载考核队伍相关的业务规则和行为
 * </p>
 */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AssessmentTeam {
    private Long id;
    private Long assessmentTimeId;
    private Long leaderId;
    private String name;
    private String inviteCode;
    private TeamStatus status;
    private LocalDateTime createdAt;

    private AssessmentTeam(Long id, Long assessmentTimeId, Long leaderId, String name,
            String inviteCode, TeamStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.assessmentTimeId = assessmentTimeId;
        this.leaderId = leaderId;
        this.name = name;
        this.inviteCode = inviteCode;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static AssessmentTeam create(Long assessmentTimeId, Long leaderId, String name, String inviteCode) {
        return new AssessmentTeam(null, assessmentTimeId, leaderId, name, inviteCode, TeamStatus.ACTIVE,
                LocalDateTime.now());
    }

    public static AssessmentTeam reconstruct(Long id, Long assessmentTimeId, Long leaderId, String name,
            String inviteCode, TeamStatus status, LocalDateTime createdAt) {
        return new AssessmentTeam(id, assessmentTimeId, leaderId, name, inviteCode, status, createdAt);
    }

    public void updateLeader(Long newLeaderId) {
        this.leaderId = newLeaderId;
    }

    public void disband() {
        this.status = TeamStatus.DISBANDED;
    }

    public boolean isActive() {
        return this.status == TeamStatus.ACTIVE;
    }

    public boolean isLeader(Long userId) {
        return this.leaderId.equals(userId);
    }

    public enum TeamStatus {
        ACTIVE, DISBANDED
    }
}
