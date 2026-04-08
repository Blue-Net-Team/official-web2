package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.model.entity.AssessmentAnswer;
import com.bluenet.web.domain.model.vo.AssessmentAnswerVO;
import com.bluenet.web.domain.repository.AssessmentAnswerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * AssessmentAnswerDomainServiceImpl 单元测试
 * <p>
 * 测试答题领域服务的业务逻辑，包括答案创建和重复提交检查
 * </p>
 */
@DisplayName("AssessmentAnswerDomainServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class AssessmentAnswerDomainServiceImplTest {

    @Mock
    private AssessmentAnswerRepository assessmentAnswerRepository;

    @InjectMocks
    private AssessmentAnswerDomainServiceImpl assessmentAnswerDomainService;

    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_QUESTION_ID = 10L;
    private static final Long TEST_FILE_ID = 50L;
    private static final Long TEST_ANSWER_ID = 100L;

    private AssessmentAnswerVO createTestAnswerVO() {
        return AssessmentAnswerVO.builder()
                .userId(TEST_USER_ID)
                .questionId(TEST_QUESTION_ID)
                .content("test answer content")
                .fileId(TEST_FILE_ID)
                .build();
    }

    // ==================== createAnswer 测试 ====================

    @Nested
    @DisplayName("createAnswer 方法测试")
    class CreateAnswerTests {

        @Test
        @DisplayName("正常创建：应保存实体并返回带id和submitTime的VO")
        void createAnswer_success_shouldReturnVOWithIdAndSubmitTime() {
            AssessmentAnswerVO inputVO = createTestAnswerVO();

            // 模拟 save 操作通过修改 entity 的 id 来模拟自增主键
            doAnswer(invocation -> {
                AssessmentAnswer entity = invocation.getArgument(0);
                entity.setId(TEST_ANSWER_ID);
                return null;
            }).when(assessmentAnswerRepository).save(any(AssessmentAnswer.class));

            when(assessmentAnswerRepository.existsByUserIdAndQuestionId(TEST_USER_ID, TEST_QUESTION_ID))
                    .thenReturn(false);

            AssessmentAnswerVO result = assessmentAnswerDomainService.createAnswer(inputVO);

            assertNotNull(result);
            assertEquals(TEST_ANSWER_ID, result.getId());
            assertEquals(TEST_USER_ID, result.getUserId());
            assertEquals(TEST_QUESTION_ID, result.getQuestionId());
            assertEquals("test answer content", result.getContent());
            assertEquals(TEST_FILE_ID, result.getFileId());
            assertNotNull(result.getSubmitTime());

            // 验证保存的实体字段正确
            ArgumentCaptor<AssessmentAnswer> entityCaptor = ArgumentCaptor.forClass(AssessmentAnswer.class);
            verify(assessmentAnswerRepository).save(entityCaptor.capture());
            AssessmentAnswer savedEntity = entityCaptor.getValue();
            assertEquals(TEST_USER_ID, savedEntity.getUserId());
            assertEquals(TEST_QUESTION_ID, savedEntity.getQuestionId());
            assertEquals("test answer content", savedEntity.getContent());
            assertEquals(TEST_FILE_ID, savedEntity.getFileId());
            assertNotNull(savedEntity.getSubmitTime());

            verify(assessmentAnswerRepository).existsByUserIdAndQuestionId(TEST_USER_ID, TEST_QUESTION_ID);
        }

        @Test
        @DisplayName("重复提交：应抛出IllegalStateException")
        void createAnswer_duplicate_shouldThrow() {
            AssessmentAnswerVO inputVO = createTestAnswerVO();

            when(assessmentAnswerRepository.existsByUserIdAndQuestionId(TEST_USER_ID, TEST_QUESTION_ID))
                    .thenReturn(true);

            IllegalStateException ex = assertThrows(
                    IllegalStateException.class,
                    () -> assessmentAnswerDomainService.createAnswer(inputVO));
            assertEquals("已经提交过该题目的答案", ex.getMessage());

            verify(assessmentAnswerRepository).existsByUserIdAndQuestionId(TEST_USER_ID, TEST_QUESTION_ID);
            verify(assessmentAnswerRepository, never()).save(any());
        }

        @Test
        @DisplayName("正常创建：submitTime不应为null")
        void createAnswer_success_submitTimeShouldBeSet() {
            AssessmentAnswerVO inputVO = createTestAnswerVO();

            doAnswer(invocation -> {
                AssessmentAnswer entity = invocation.getArgument(0);
                entity.setId(TEST_ANSWER_ID);
                return null;
            }).when(assessmentAnswerRepository).save(any(AssessmentAnswer.class));

            when(assessmentAnswerRepository.existsByUserIdAndQuestionId(TEST_USER_ID, TEST_QUESTION_ID))
                    .thenReturn(false);

            AssessmentAnswerVO result = assessmentAnswerDomainService.createAnswer(inputVO);

            assertNotNull(result.getSubmitTime());

            // 验证保存到数据库的实体也设置了submitTime
            ArgumentCaptor<AssessmentAnswer> entityCaptor = ArgumentCaptor.forClass(AssessmentAnswer.class);
            verify(assessmentAnswerRepository).save(entityCaptor.capture());
            assertNotNull(entityCaptor.getValue().getSubmitTime());
        }

        @Test
        @DisplayName("内容为null时：应正常创建")
        void createAnswer_nullContent_shouldSucceed() {
            AssessmentAnswerVO inputVO = AssessmentAnswerVO.builder()
                    .userId(TEST_USER_ID)
                    .questionId(TEST_QUESTION_ID)
                    .content(null)
                    .fileId(TEST_FILE_ID)
                    .build();

            doAnswer(invocation -> {
                AssessmentAnswer entity = invocation.getArgument(0);
                entity.setId(TEST_ANSWER_ID);
                return null;
            }).when(assessmentAnswerRepository).save(any(AssessmentAnswer.class));

            when(assessmentAnswerRepository.existsByUserIdAndQuestionId(TEST_USER_ID, TEST_QUESTION_ID))
                    .thenReturn(false);

            AssessmentAnswerVO result = assessmentAnswerDomainService.createAnswer(inputVO);

            assertNotNull(result);
            assertNull(result.getContent());
            assertEquals(TEST_FILE_ID, result.getFileId());
        }
    }
}
