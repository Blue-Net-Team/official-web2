package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.assessment_answer.AssessmentAnswerDTO;
import com.bluenet.web.api.dto.assessment_answer.CreateAnswerRequestDTO;
import com.bluenet.web.domain.model.vo.AssessmentAnswerVO;
import com.bluenet.web.domain.model.vo.AssessmentQuestionVO;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.repository.AssessmentAnswerRepository;
import com.bluenet.web.domain.repository.AssessmentSessionRepository;
import com.bluenet.web.domain.service.AssessmentAnswerDomainService;
import com.bluenet.web.domain.service.AssessmentQuestionDomainService;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * AssessmentAnswerServiceImpl 单元测试
 * <p>
 * 测试答题应用服务的协调逻辑，包括认证校验、题目校验、答案创建和查询
 * </p>
 */
@DisplayName("AssessmentAnswerServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class AssessmentAnswerServiceImplTest {

    @Mock
    private AssessmentAnswerDomainService assessmentAnswerDomainService;

    @Mock
    private AssessmentQuestionDomainService assessmentQuestionDomainService;

    @Mock
    private AssessmentAnswerRepository assessmentAnswerRepository;

    @Mock
    private AssessmentSessionRepository assessmentSessionRepository;

    @InjectMocks
    private AssessmentAnswerServiceImpl assessmentAnswerService;

    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_QUESTION_ID = 10L;
    private static final Long TEST_ASSESSMENT_TIME_ID = 20L;
    private static final Long TEST_ANSWER_ID = 100L;
    private static final Long TEST_FILE_ID = 50L;
    private static final LocalDateTime TEST_SUBMIT_TIME = LocalDateTime.of(2026, 4, 5, 14, 30);

    private UserVO createTestUser() {
        return UserVO.builder()
                .id(TEST_USER_ID)
                .studentId("2024123456")
                .roleName("CANDIDATE")
                .build();
    }

    private CreateAnswerRequestDTO createTestRequest() {
        return CreateAnswerRequestDTO.builder()
                .questionId(TEST_QUESTION_ID)
                .fileId(TEST_FILE_ID)
                .content("test answer content")
                .build();
    }

    private AssessmentAnswerVO createTestAnswerVO() {
        return AssessmentAnswerVO.builder()
                .id(TEST_ANSWER_ID)
                .userId(TEST_USER_ID)
                .questionId(TEST_QUESTION_ID)
                .content("test answer content")
                .fileId(TEST_FILE_ID)
                .submitTime(TEST_SUBMIT_TIME)
                .build();
    }

    private AssessmentQuestionVO createTestQuestionVO() {
        return AssessmentQuestionVO.builder()
                .id(TEST_QUESTION_ID)
                .assessmentTimeId(TEST_ASSESSMENT_TIME_ID)
                .build();
    }

    // ==================== createAnswer 测试 ====================

    @Nested
    @DisplayName("createAnswer 方法测试")
    class CreateAnswerTests {

        @Test
        @DisplayName("正常创建：应返回DTO")
        void createAnswer_success_shouldReturnDTO() {
            try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
                UserVO user = createTestUser();
                mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(user);

                CreateAnswerRequestDTO request = createTestRequest();
                AssessmentQuestionVO questionVO = createTestQuestionVO();
                AssessmentAnswerVO createdVO = createTestAnswerVO();

                when(assessmentQuestionDomainService.getQuestionById(TEST_QUESTION_ID)).thenReturn(questionVO);
                when(assessmentSessionRepository.findByUserIdAndAssessmentTimeId(TEST_USER_ID, TEST_ASSESSMENT_TIME_ID))
                        .thenReturn(Optional.empty());
                when(assessmentAnswerDomainService.createAnswer(any(AssessmentAnswerVO.class))).thenReturn(createdVO);

                AssessmentAnswerDTO result = assessmentAnswerService.createAnswer(request);

                assertNotNull(result);
                assertEquals(TEST_ANSWER_ID, result.getId());
                assertEquals(TEST_QUESTION_ID, result.getQuestionId());
                assertEquals(TEST_FILE_ID, result.getFileId());
                assertEquals("test answer content", result.getContent());
                assertEquals(TEST_SUBMIT_TIME, result.getSubmitTime());

                verify(assessmentQuestionDomainService).getQuestionById(TEST_QUESTION_ID);
                verify(assessmentAnswerDomainService).createAnswer(any(AssessmentAnswerVO.class));
            }
        }

        @Test
        @DisplayName("未登录：应抛出SecurityException")
        void createAnswer_notAuthenticated_shouldThrowSecurityException() {
            try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
                mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(null);

                CreateAnswerRequestDTO request = createTestRequest();

                SecurityException ex = assertThrows(
                        SecurityException.class,
                        () -> assessmentAnswerService.createAnswer(request));
                assertEquals("未登录", ex.getMessage());

                verifyNoInteractions(assessmentQuestionDomainService);
                verifyNoInteractions(assessmentAnswerDomainService);
            }
        }

        @Test
        @DisplayName("题目不存在：应抛出RuntimeException")
        void createAnswer_questionNotFound_shouldThrow() {
            try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
                UserVO user = createTestUser();
                mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(user);

                CreateAnswerRequestDTO request = createTestRequest();

                when(assessmentQuestionDomainService.getQuestionById(TEST_QUESTION_ID))
                        .thenThrow(new RuntimeException("题目不存在"));

                RuntimeException ex = assertThrows(
                        RuntimeException.class,
                        () -> assessmentAnswerService.createAnswer(request));
                assertEquals("题目不存在", ex.getMessage());

                verify(assessmentAnswerDomainService, never()).createAnswer(any());
            }
        }

        @Test
        @DisplayName("重复提交：应抛出IllegalStateException")
        void createAnswer_duplicateSubmission_shouldThrow() {
            try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
                UserVO user = createTestUser();
                mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(user);

                CreateAnswerRequestDTO request = createTestRequest();
                AssessmentQuestionVO questionVO = createTestQuestionVO();

                when(assessmentQuestionDomainService.getQuestionById(TEST_QUESTION_ID)).thenReturn(questionVO);
                when(assessmentSessionRepository.findByUserIdAndAssessmentTimeId(TEST_USER_ID, TEST_ASSESSMENT_TIME_ID))
                        .thenReturn(Optional.empty());
                when(assessmentAnswerDomainService.createAnswer(any(AssessmentAnswerVO.class)))
                        .thenThrow(new IllegalStateException("已经提交过该题目的答案"));

                IllegalStateException ex = assertThrows(
                        IllegalStateException.class,
                        () -> assessmentAnswerService.createAnswer(request));
                assertEquals("已经提交过该题目的答案", ex.getMessage());
            }
        }

        @Test
        @DisplayName("创建答案应正确传递用户ID和题目ID")
        void createAnswer_shouldPassCorrectUserAndQuestionId() {
            try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
                UserVO user = createTestUser();
                mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(user);

                CreateAnswerRequestDTO request = createTestRequest();
                AssessmentQuestionVO questionVO = createTestQuestionVO();
                AssessmentAnswerVO createdVO = createTestAnswerVO();

                when(assessmentQuestionDomainService.getQuestionById(TEST_QUESTION_ID)).thenReturn(questionVO);
                when(assessmentSessionRepository.findByUserIdAndAssessmentTimeId(TEST_USER_ID, TEST_ASSESSMENT_TIME_ID))
                        .thenReturn(Optional.empty());
                when(assessmentAnswerDomainService.createAnswer(any(AssessmentAnswerVO.class))).thenReturn(createdVO);

                assessmentAnswerService.createAnswer(request);

                verify(assessmentAnswerDomainService).createAnswer(
                        argThat(
                                answerVO -> answerVO.getUserId().equals(TEST_USER_ID)
                                        && answerVO.getQuestionId().equals(TEST_QUESTION_ID)
                                        && answerVO.getFileId().equals(TEST_FILE_ID)
                                        && "test answer content".equals(answerVO.getContent())));
            }
        }
    }

    // ==================== getMyAnswer 测试 ====================

    @Nested
    @DisplayName("getMyAnswer 方法测试")
    class GetMyAnswerTests {

        @Test
        @DisplayName("答案存在：应返回DTO")
        void getMyAnswer_answerExists_shouldReturnDTO() {
            try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
                UserVO user = createTestUser();
                mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(user);

                AssessmentAnswerVO answerVO = createTestAnswerVO();
                when(assessmentAnswerRepository.findByUserIdAndQuestionId(TEST_USER_ID, TEST_QUESTION_ID))
                        .thenReturn(Optional.of(answerVO));

                AssessmentAnswerDTO result = assessmentAnswerService.getMyAnswer(TEST_QUESTION_ID);

                assertNotNull(result);
                assertEquals(TEST_ANSWER_ID, result.getId());
                assertEquals(TEST_QUESTION_ID, result.getQuestionId());
                assertEquals(TEST_FILE_ID, result.getFileId());
                assertEquals("test answer content", result.getContent());
                assertEquals(TEST_SUBMIT_TIME, result.getSubmitTime());

                verify(assessmentAnswerRepository).findByUserIdAndQuestionId(TEST_USER_ID, TEST_QUESTION_ID);
            }
        }

        @Test
        @DisplayName("答案不存在：应返回null")
        void getMyAnswer_answerNotExists_shouldReturnNull() {
            try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
                UserVO user = createTestUser();
                mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(user);

                when(assessmentAnswerRepository.findByUserIdAndQuestionId(TEST_USER_ID, TEST_QUESTION_ID))
                        .thenReturn(Optional.empty());

                AssessmentAnswerDTO result = assessmentAnswerService.getMyAnswer(TEST_QUESTION_ID);

                assertNull(result);
                verify(assessmentAnswerRepository).findByUserIdAndQuestionId(TEST_USER_ID, TEST_QUESTION_ID);
            }
        }

        @Test
        @DisplayName("未登录：应抛出SecurityException")
        void getMyAnswer_notAuthenticated_shouldThrowSecurityException() {
            try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
                mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(null);

                SecurityException ex = assertThrows(
                        SecurityException.class,
                        () -> assessmentAnswerService.getMyAnswer(TEST_QUESTION_ID));
                assertEquals("未登录", ex.getMessage());

                verifyNoInteractions(assessmentAnswerRepository);
            }
        }
    }
}
