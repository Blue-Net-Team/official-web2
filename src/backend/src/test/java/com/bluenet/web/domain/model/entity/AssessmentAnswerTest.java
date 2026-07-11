package com.bluenet.web.domain.model.entity;

import com.bluenet.web.domain.model.enumerate.ProgrammingLanguage;
import com.bluenet.web.testsupport.fixture.AssessmentFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AssessmentAnswer 领域实体单元测试。
 */
@DisplayName("AssessmentAnswer 领域实体测试")
class AssessmentAnswerTest {

    @Test
    @DisplayName("create: 不带队伍标识时应创建个人答案")
    void create_withoutTeam_shouldCreateIndividualAnswer() {
        LocalDateTime before = LocalDateTime.now();
        AssessmentAnswer answer = AssessmentFixture.answerBuilder()
                .userId(1L)
                .questionId(2L)
                .content("answer")
                .language(ProgrammingLanguage.CPP)
                .fileId(3L)
                .build();
        LocalDateTime after = LocalDateTime.now();

        assertNull(answer.getId());
        assertEquals(1L, answer.getUserId());
        assertEquals(2L, answer.getQuestionId());
        assertEquals("answer", answer.getContent());
        assertEquals(ProgrammingLanguage.CPP, answer.getLanguage());
        assertEquals(3L, answer.getFileId());
        assertNotNull(answer.getSubmitTime());
        assertFalse(answer.getSubmitTime().isBefore(before));
        assertFalse(answer.getSubmitTime().isAfter(after));
        assertNull(answer.getTeamId());
    }

    @Test
    @DisplayName("create: 带队伍标识时应创建队伍答案")
    void create_withTeam_shouldCreateTeamAnswer() {
        LocalDateTime before = LocalDateTime.now();
        AssessmentAnswer answer = AssessmentAnswer.create(
                1L,
                2L,
                "team answer",
                ProgrammingLanguage.JAVA,
                4L,
                5L);
        LocalDateTime after = LocalDateTime.now();

        assertNull(answer.getId());
        assertEquals(1L, answer.getUserId());
        assertEquals(2L, answer.getQuestionId());
        assertEquals("team answer", answer.getContent());
        assertEquals(ProgrammingLanguage.JAVA, answer.getLanguage());
        assertEquals(4L, answer.getFileId());
        assertEquals(5L, answer.getTeamId());
        assertNotNull(answer.getSubmitTime());
        assertFalse(answer.getSubmitTime().isBefore(before));
        assertFalse(answer.getSubmitTime().isAfter(after));
    }

    @Test
    @DisplayName("reconstruct: 应保留所有字段")
    void reconstruct_shouldPreserveFields() {
        LocalDateTime submitTime = LocalDateTime.of(2024, 3, 15, 9, 0);

        AssessmentAnswer answer = AssessmentAnswer.reconstruct(
                10L,
                1L,
                2L,
                "content",
                ProgrammingLanguage.PYTHON,
                3L,
                submitTime,
                5L);

        assertEquals(10L, answer.getId());
        assertEquals(1L, answer.getUserId());
        assertEquals(2L, answer.getQuestionId());
        assertEquals("content", answer.getContent());
        assertEquals(ProgrammingLanguage.PYTHON, answer.getLanguage());
        assertEquals(3L, answer.getFileId());
        assertEquals(submitTime, answer.getSubmitTime());
        assertEquals(5L, answer.getTeamId());
    }

    @Test
    @DisplayName("update: 应更新非 null 字段并刷新提交时间")
    void update_shouldUpdateNonNullFieldsAndRefreshSubmitTime() {
        LocalDateTime originalSubmitTime = LocalDateTime.of(2024, 1, 1, 0, 0);
        AssessmentAnswer answer = AssessmentAnswer.reconstruct(
                1L,
                1L,
                2L,
                "old content",
                ProgrammingLanguage.CPP,
                3L,
                originalSubmitTime,
                null);

        answer.update("new content", ProgrammingLanguage.JAVA, 4L);

        assertEquals("new content", answer.getContent());
        assertEquals(ProgrammingLanguage.JAVA, answer.getLanguage());
        assertEquals(4L, answer.getFileId());
        assertNotEquals(originalSubmitTime, answer.getSubmitTime());
    }

    @Test
    @DisplayName("update: null 字段不应覆盖已有值")
    void update_withNullFields_shouldNotOverwriteExistingValues() {
        LocalDateTime originalSubmitTime = LocalDateTime.of(2024, 1, 1, 0, 0);
        AssessmentAnswer answer = AssessmentAnswer.reconstruct(
                1L,
                1L,
                2L,
                "old content",
                ProgrammingLanguage.CPP,
                3L,
                originalSubmitTime,
                null);

        answer.update(null, ProgrammingLanguage.PYTHON, null);

        assertEquals("old content", answer.getContent());
        assertEquals(ProgrammingLanguage.PYTHON, answer.getLanguage());
        assertEquals(3L, answer.getFileId());
        assertNotEquals(originalSubmitTime, answer.getSubmitTime());
    }
}
