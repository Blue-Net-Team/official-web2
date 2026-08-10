package com.bluenet.web.domain.model.entity;

import com.bluenet.web.domain.model.enumerate.QuestionType;
import com.bluenet.web.domain.model.vo.question_content.FileUploadContent;
import com.bluenet.web.domain.model.vo.question_content.MultipleChoiceContent;
import com.bluenet.web.domain.model.vo.question_content.SingleChoiceContent;
import com.bluenet.web.testsupport.fixture.AssessmentFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * AssessmentQuestion 领域实体单元测试。
 */
@DisplayName("AssessmentQuestion 领域实体测试")
class AssessmentQuestionTest {

    @Test
    @DisplayName("create: 应创建新的考核题目")
    void create_shouldCreateQuestion() {
        FileUploadContent content = new FileUploadContent();
        BigDecimal score = new BigDecimal("100");

        AssessmentQuestion question = AssessmentQuestion.create(
                1L,
                1,
                QuestionType.FILE_UPLOAD,
                "测试题目",
                content,
                2L,
                score);

        assertNull(question.getId());
        assertEquals(Long.valueOf(1L), question.getAssessmentTimeId());
        assertEquals(Integer.valueOf(1), question.getQuestionNo());
        assertEquals(QuestionType.FILE_UPLOAD, question.getQuestionType());
        assertEquals("测试题目", question.getTitle());
        assertEquals(content, question.getContent());
        assertEquals(Long.valueOf(2L), question.getAttachmentId());
        assertEquals(score, question.getScore());
    }

    @Test
    @DisplayName("create: 应支持选择题类型")
    void create_shouldSupportChoiceQuestionTypes() {
        SingleChoiceContent singleChoiceContent = new SingleChoiceContent();
        singleChoiceContent.setOptions(List.of("A", "B", "C"));
        singleChoiceContent.setCorrectAnswer("A");

        AssessmentQuestion singleChoiceQuestion = AssessmentQuestion.create(
                1L,
                1,
                QuestionType.SINGLE_CHOICE,
                "单选题",
                singleChoiceContent,
                null,
                new BigDecimal("10"));

        assertEquals(QuestionType.SINGLE_CHOICE, singleChoiceQuestion.getQuestionType());
        assertTrue(singleChoiceQuestion.getContent() instanceof SingleChoiceContent);

        MultipleChoiceContent multipleChoiceContent = new MultipleChoiceContent();
        multipleChoiceContent.setOptions(List.of("A", "B", "C"));
        multipleChoiceContent.setCorrectAnswers(List.of("A", "B"));

        AssessmentQuestion multipleChoiceQuestion = AssessmentQuestion.create(
                1L,
                2,
                QuestionType.MULTIPLE_CHOICE,
                "多选题",
                multipleChoiceContent,
                null,
                new BigDecimal("20"));

        assertEquals(QuestionType.MULTIPLE_CHOICE, multipleChoiceQuestion.getQuestionType());
        assertTrue(multipleChoiceQuestion.getContent() instanceof MultipleChoiceContent);
    }

    @Test
    @DisplayName("reconstruct: 应保留所有字段")
    void reconstruct_shouldPreserveFields() {
        FileUploadContent content = new FileUploadContent();
        BigDecimal score = new BigDecimal("50");

        AssessmentQuestion question = AssessmentQuestion.reconstruct(
                100L,
                1L,
                2,
                QuestionType.ALGORITHM,
                "算法题",
                content,
                3L,
                score);

        assertEquals(Long.valueOf(100L), question.getId());
        assertEquals(Long.valueOf(1L), question.getAssessmentTimeId());
        assertEquals(Integer.valueOf(2), question.getQuestionNo());
        assertEquals(QuestionType.ALGORITHM, question.getQuestionType());
        assertEquals("算法题", question.getTitle());
        assertEquals(content, question.getContent());
        assertEquals(Long.valueOf(3L), question.getAttachmentId());
        assertEquals(score, question.getScore());
    }

    @Test
    @DisplayName("update: 应更新所有非 null 字段")
    void update_shouldUpdateNonNullFields() {
        AssessmentQuestion question = AssessmentFixture.questionBuilder().build();

        SingleChoiceContent newContent = new SingleChoiceContent();
        newContent.setOptions(List.of("A", "B", "C", "D"));
        newContent.setCorrectAnswer("B");
        BigDecimal newScore = new BigDecimal("80");

        question.update(
                2,
                QuestionType.SINGLE_CHOICE,
                "更新后的题目",
                newContent,
                5L,
                newScore);

        assertEquals(Integer.valueOf(2), question.getQuestionNo());
        assertEquals(QuestionType.SINGLE_CHOICE, question.getQuestionType());
        assertEquals("更新后的题目", question.getTitle());
        assertEquals(newContent, question.getContent());
        assertEquals(Long.valueOf(5L), question.getAttachmentId());
        assertEquals(newScore, question.getScore());
    }

    @Test
    @DisplayName("update: 应忽略所有 null 字段")
    void update_shouldIgnoreNullFields() {
        AssessmentQuestion question = AssessmentFixture.questionBuilder().build();
        Integer originalQuestionNo = question.getQuestionNo();
        QuestionType originalQuestionType = question.getQuestionType();
        String originalTitle = question.getTitle();
        BigDecimal originalScore = question.getScore();

        question.update(null, null, null, null, null, null);

        assertEquals(originalQuestionNo, question.getQuestionNo());
        assertEquals(originalQuestionType, question.getQuestionType());
        assertEquals(originalTitle, question.getTitle());
        assertEquals(originalScore, question.getScore());
    }
}
