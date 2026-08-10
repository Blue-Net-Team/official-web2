package com.bluenet.web.domain.model.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AssessmentTeamMember 领域实体单元测试。
 */
@DisplayName("AssessmentTeamMember 领域实体测试")
class AssessmentTeamMemberTest {

    @Test
    @DisplayName("create: 应创建新队伍成员")
    void create_shouldCreateMember() {
        LocalDateTime before = LocalDateTime.now();
        AssessmentTeamMember member = AssessmentTeamMember.create(1L, 2L);
        LocalDateTime after = LocalDateTime.now();

        assertNull(member.getId());
        assertEquals(1L, member.getTeamId());
        assertEquals(2L, member.getUserId());
        assertNotNull(member.getJoinedAt());
        assertFalse(member.getJoinedAt().isBefore(before));
        assertFalse(member.getJoinedAt().isAfter(after));
    }

    @Test
    @DisplayName("reconstruct: 应保留所有字段")
    void reconstruct_shouldPreserveFields() {
        LocalDateTime joinedAt = LocalDateTime.of(2024, 6, 1, 10, 30);

        AssessmentTeamMember member = AssessmentTeamMember.reconstruct(10L, 1L, 2L, joinedAt);

        assertEquals(10L, member.getId());
        assertEquals(1L, member.getTeamId());
        assertEquals(2L, member.getUserId());
        assertEquals(joinedAt, member.getJoinedAt());
    }
}
