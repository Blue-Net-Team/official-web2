package com.bluenet.web.domain.model.entity;

import com.bluenet.web.testsupport.fixture.AssessmentFixture;
import com.bluenet.web.testsupport.fixture.TimeFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * AssessmentSession 领域实体单元测试。
 */
@DisplayName("AssessmentSession 领域实体测试")
class AssessmentSessionTest {

    @Test
    @DisplayName("create: 应创建新的考核会话")
    void create_shouldCreateSession() {
        LocalDateTime startTime = TimeFixture.now();
        LocalDateTime deadline = TimeFixture.plusMinutes(60);

        AssessmentSession session = AssessmentSession.create(
                1L,
                2L,
                startTime,
                deadline);

        assertNull(session.getId());
        assertEquals(Long.valueOf(1L), session.getUserId());
        assertEquals(Long.valueOf(2L), session.getAssessmentTimeId());
        assertEquals(startTime, session.getStartTime());
        assertEquals(deadline, session.getDeadline());
    }

    @Test
    @DisplayName("create: 应允许开始时间和截止时间为 null")
    void create_shouldAllowNullTimes() {
        AssessmentSession session = AssessmentSession.create(
                1L,
                2L,
                null,
                null);

        assertEquals(Long.valueOf(1L), session.getUserId());
        assertEquals(Long.valueOf(2L), session.getAssessmentTimeId());
        assertNull(session.getStartTime());
        assertNull(session.getDeadline());
    }

    @Test
    @DisplayName("reconstruct: 应保留所有字段")
    void reconstruct_shouldPreserveFields() {
        LocalDateTime startTime = LocalDateTime.of(2026, 7, 11, 9, 0);
        LocalDateTime deadline = LocalDateTime.of(2026, 7, 11, 10, 0);

        AssessmentSession session = AssessmentSession.reconstruct(
                100L,
                1L,
                2L,
                startTime,
                deadline);

        assertEquals(Long.valueOf(100L), session.getId());
        assertEquals(Long.valueOf(1L), session.getUserId());
        assertEquals(Long.valueOf(2L), session.getAssessmentTimeId());
        assertEquals(startTime, session.getStartTime());
        assertEquals(deadline, session.getDeadline());
    }

    @Test
    @DisplayName("会话对象应可通过夹具构造")
    void session_shouldBeBuiltByFixture() {
        AssessmentSession session = AssessmentFixture.sessionBuilder()
                .userId(1L)
                .assessmentTimeId(2L)
                .build();

        assertNull(session.getId());
        assertEquals(Long.valueOf(1L), session.getUserId());
        assertEquals(Long.valueOf(2L), session.getAssessmentTimeId());
        assertEquals(
                Integer.valueOf(60),
                Integer.valueOf(
                        (int) java.time.Duration.between(
                                session.getStartTime(),
                                session.getDeadline()).toMinutes()));
    }
}
