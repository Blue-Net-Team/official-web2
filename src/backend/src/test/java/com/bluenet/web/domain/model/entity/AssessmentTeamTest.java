package com.bluenet.web.domain.model.entity;

import com.bluenet.web.testsupport.fixture.AssessmentFixture;
import com.bluenet.web.testsupport.fixture.TimeFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AssessmentTeam 领域实体单元测试。
 */
@DisplayName("AssessmentTeam 领域实体测试")
class AssessmentTeamTest {

    @Test
    @DisplayName("create: 应创建状态为 ACTIVE 的新队伍")
    void create_shouldCreateActiveTeam() {
        LocalDateTime before = LocalDateTime.now();
        AssessmentTeam team = AssessmentFixture.teamBuilder()
                .assessmentTimeId(1L)
                .leaderId(2L)
                .name("BlueTeam")
                .inviteCode("BLUE")
                .build();
        LocalDateTime after = LocalDateTime.now();

        assertNull(team.getId());
        assertEquals(1L, team.getAssessmentTimeId());
        assertEquals(2L, team.getLeaderId());
        assertEquals("BlueTeam", team.getName());
        assertEquals("BLUE", team.getInviteCode());
        assertEquals(AssessmentTeam.TeamStatus.ACTIVE, team.getStatus());
        assertNotNull(team.getCreatedAt());
        assertFalse(team.getCreatedAt().isBefore(before));
        assertFalse(team.getCreatedAt().isAfter(after));
    }

    @Test
    @DisplayName("reconstruct: 应保留所有字段")
    void reconstruct_shouldPreserveFields() {
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 12, 0);

        AssessmentTeam team = AssessmentTeam.reconstruct(
                10L,
                1L,
                2L,
                "TeamA",
                "INVITE",
                AssessmentTeam.TeamStatus.DISBANDED,
                createdAt);

        assertEquals(10L, team.getId());
        assertEquals(1L, team.getAssessmentTimeId());
        assertEquals(2L, team.getLeaderId());
        assertEquals("TeamA", team.getName());
        assertEquals("INVITE", team.getInviteCode());
        assertEquals(AssessmentTeam.TeamStatus.DISBANDED, team.getStatus());
        assertEquals(createdAt, team.getCreatedAt());
    }

    @Test
    @DisplayName("updateLeader: 应更新队长标识")
    void updateLeader_shouldUpdateLeaderId() {
        AssessmentTeam team = AssessmentFixture.teamBuilder()
                .assessmentTimeId(1L)
                .leaderId(2L)
                .build();

        team.updateLeader(5L);

        assertEquals(5L, team.getLeaderId());
    }

    @Test
    @DisplayName("disband: 应将状态置为 DISBANDED")
    void disband_shouldSetStatusDisbanded() {
        AssessmentTeam team = AssessmentFixture.teamBuilder()
                .assessmentTimeId(1L)
                .leaderId(2L)
                .build();

        team.disband();

        assertEquals(AssessmentTeam.TeamStatus.DISBANDED, team.getStatus());
    }

    @Test
    @DisplayName("isActive: ACTIVE 队伍应返回 true")
    void isActive_activeTeam_shouldReturnTrue() {
        AssessmentTeam team = AssessmentFixture.teamBuilder()
                .assessmentTimeId(1L)
                .leaderId(2L)
                .build();

        assertTrue(team.isActive());
    }

    @Test
    @DisplayName("isActive: DISBANDED 队伍应返回 false")
    void isActive_disbandedTeam_shouldReturnFalse() {
        AssessmentTeam team = AssessmentTeam.reconstruct(
                1L,
                1L,
                2L,
                "T",
                "C",
                AssessmentTeam.TeamStatus.DISBANDED,
                TimeFixture.now());

        assertFalse(team.isActive());
    }

    @Test
    @DisplayName("isLeader: 队长标识匹配应返回 true")
    void isLeader_matchingLeader_shouldReturnTrue() {
        AssessmentTeam team = AssessmentFixture.teamBuilder()
                .assessmentTimeId(1L)
                .leaderId(2L)
                .build();

        assertTrue(team.isLeader(2L));
    }

    @Test
    @DisplayName("isLeader: 非队长标识应返回 false")
    void isLeader_nonMatchingLeader_shouldReturnFalse() {
        AssessmentTeam team = AssessmentFixture.teamBuilder()
                .assessmentTimeId(1L)
                .leaderId(2L)
                .build();

        assertFalse(team.isLeader(3L));
    }
}
