package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.AssessmentAnswerResult;
import com.bluenet.web.application.command.assessment_answer.AssessmentAnswerCommands;
import com.bluenet.web.domain.exception.DataConflict;
import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.model.enumerate.*;
import com.bluenet.web.domain.model.entity.AssessmentAnswer;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.vo.AssessmentJudgementVO;
import com.bluenet.web.domain.model.entity.AssessmentQuestion;

import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.model.vo.FileVO;
import com.bluenet.web.domain.model.vo.evaluation.MultipleChoiceContent;
import com.bluenet.web.domain.model.vo.evaluation.SingleChoiceContent;
import com.bluenet.web.domain.repository.AssessmentAnswerRepository;
import com.bluenet.web.domain.repository.AssessmentSessionRepository;
import com.bluenet.web.domain.repository.AssessmentTeamRepository;
import com.bluenet.web.domain.service.AssessmentDecisionDomainService;
import com.bluenet.web.domain.service.AssessmentJudgementDomainService;
import com.bluenet.web.domain.repository.AssessmentQuestionRepository;
import com.bluenet.web.domain.repository.AssessmentTimeRepository;
import com.bluenet.web.domain.service.CommentDomainService;
import com.bluenet.web.domain.service.FileDomainService;
import com.bluenet.web.domain.service.UserDomainService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("AssessmentAnswerAppServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class AssessmentAnswerAppServiceImplTest {

    @Mock
    private AssessmentQuestionRepository assessmentQuestionRepository;
    @Mock
    private AssessmentTimeRepository assessmentTimeRepository;
    @Mock
    private FileDomainService fileDomainService;
    @Mock
    private AssessmentJudgementDomainService assessmentJudgementDomainService;
    @Mock
    private AssessmentAnswerRepository assessmentAnswerRepository;
    @Mock
    private AssessmentSessionRepository assessmentSessionRepository;
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();
    @Mock
    private UserDomainService userDomainService;
    @Mock
    private CommentDomainService commentDomainService;
    @Mock
    private AssessmentDecisionDomainService assessmentDecisionDomainService;
    @Mock
    private AssessmentTeamRepository assessmentTeamRepository;
    @InjectMocks
    private AssessmentAnswerAppServiceImpl assessmentAnswerAppService;

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
                .direction(Direction.COMPUTER_VISION)
                .build();
    }

    private AssessmentAnswerCommands.CreateAssessmentAnswerCommand createTestCreateCommand() {
        return new AssessmentAnswerCommands.CreateAssessmentAnswerCommand(TEST_USER_ID, TEST_QUESTION_ID,
                "test answer content", null, TEST_FILE_ID);
    }

    private AssessmentAnswerCommands.UpdateAssessmentAnswerCommand createTestUpdateCommand() {
        return new AssessmentAnswerCommands.UpdateAssessmentAnswerCommand(TEST_USER_ID, TEST_QUESTION_ID,
                "test answer content", null, TEST_FILE_ID);
    }

    private AssessmentAnswer createTestAnswer() {
        return AssessmentAnswer.reconstruct(
                TEST_ANSWER_ID,
                TEST_USER_ID,
                TEST_QUESTION_ID,
                "test answer content",
                null,
                TEST_FILE_ID,
                TEST_SUBMIT_TIME,
                null);
    }

    private AssessmentQuestion createTestQuestion() {
        return AssessmentQuestion
                .reconstruct(
                        TEST_QUESTION_ID,
                        TEST_ASSESSMENT_TIME_ID,
                        1,
                        QuestionType.ALGORITHM,
                        null,
                        null,
                        null,
                        null);
    }

    private AssessmentQuestion createSingleChoiceQuestion(String correctAnswer) {
        SingleChoiceContent content = new SingleChoiceContent();
        content.setContent("单选题");
        content.setOptions(Arrays.asList("A", "B", "C"));
        content.setCorrectAnswer(correctAnswer);
        return AssessmentQuestion.reconstruct(
                TEST_QUESTION_ID,
                TEST_ASSESSMENT_TIME_ID,
                1,
                QuestionType.SINGLE_CHOICE,
                "单选题",
                content,
                null,
                BigDecimal.TEN);
    }

    private AssessmentQuestion createMultipleChoiceQuestion(String... correctAnswers) {
        MultipleChoiceContent content = new MultipleChoiceContent();
        content.setContent("多选题");
        content.setOptions(Arrays.asList("A", "B", "C"));
        content.setCorrectAnswers(Arrays.asList(correctAnswers));
        return AssessmentQuestion.reconstruct(
                TEST_QUESTION_ID,
                TEST_ASSESSMENT_TIME_ID,
                1,
                QuestionType.MULTIPLE_CHOICE,
                "多选题",
                content,
                null,
                BigDecimal.TEN);
    }

    private AssessmentJudgementVO createJudgementVO(ObjectiveResultCode resultCode) {
        return AssessmentJudgementVO.builder()
                .id(900L)
                .answerId(TEST_ANSWER_ID)
                .questionId(TEST_QUESTION_ID)
                .assessmentTimeId(TEST_ASSESSMENT_TIME_ID)
                .userId(TEST_USER_ID)
                .score(resultCode == ObjectiveResultCode.AC ? BigDecimal.TEN : BigDecimal.ZERO)
                .maxScore(BigDecimal.TEN)
                .status(JudgementStatus.JUDGED)
                .resultCode(resultCode)
                .source(JudgementSource.AUTO)
                .build();
    }

    private AssessmentTime createTestTime() {
        return AssessmentTime
                .reconstruct(
                        TEST_ASSESSMENT_TIME_ID,
                        Direction.COMPUTER_VISION,
                        1,
                        2024,
                        null,
                        null,
                        true,
                        90,
                        null,
                        false);
    }

    private AssessmentTime createNonTimedTime() {
        return AssessmentTime
                .reconstruct(
                        TEST_ASSESSMENT_TIME_ID,
                        Direction.COMPUTER_VISION,
                        1,
                        2024,
                        null,
                        null,
                        false,
                        null,
                        null,
                        false);
    }

    private void stubDirectionAndFileValidation() {
        when(assessmentTimeRepository.findById(TEST_ASSESSMENT_TIME_ID)).thenReturn(Optional.of(createTestTime()));
        when(fileDomainService.getFileById(TEST_FILE_ID))
                .thenReturn(FileVO.builder().id(TEST_FILE_ID).name("work.zip").type(FileType.WORK).build());
    }

    @Nested
    @DisplayName("createAnswer 方法测试")
    class CreateAnswerTests {
        @Test
        @DisplayName("正常创建：应返回Result")
        void createAnswer_success_shouldReturnResult() {
            UserVO user = createTestUser();
            when(userDomainService.getUser(TEST_USER_ID)).thenReturn(Optional.of(user));
            AssessmentAnswerCommands.CreateAssessmentAnswerCommand command = createTestCreateCommand();
            AssessmentQuestion question = createTestQuestion();
            when(assessmentQuestionRepository.findById(TEST_QUESTION_ID)).thenReturn(Optional.of(question));
            stubDirectionAndFileValidation();
            when(assessmentSessionRepository.findByUserIdAndAssessmentTimeId(TEST_USER_ID, TEST_ASSESSMENT_TIME_ID))
                    .thenReturn(Optional.empty());
            when(assessmentAnswerRepository.existsByUserIdAndQuestionId(TEST_USER_ID, TEST_QUESTION_ID))
                    .thenReturn(false);
            doAnswer(invocation -> {
                AssessmentAnswer entity = invocation.getArgument(0);
                entity.setId(TEST_ANSWER_ID);
                return null;
            }).when(assessmentAnswerRepository).save(any(AssessmentAnswer.class));

            AssessmentAnswerResult result = assessmentAnswerAppService.createAnswer(command);

            assertNotNull(result);
            assertEquals(TEST_ANSWER_ID, result.id());
            assertEquals(TEST_QUESTION_ID, result.questionId());
            assertEquals(TEST_FILE_ID, result.fileId());
            assertEquals("test answer content", result.content());
            assertNotNull(result.submitTime());
            verify(assessmentQuestionRepository).findById(TEST_QUESTION_ID);
            verify(assessmentAnswerRepository).save(any(AssessmentAnswer.class));
        }

        @Test
        @DisplayName("未登录：应抛出SecurityException")
        void createAnswer_notAuthenticated_shouldThrowSecurityException() {
            when(userDomainService.getUser(TEST_USER_ID)).thenReturn(Optional.empty());
            AssessmentAnswerCommands.CreateAssessmentAnswerCommand command = createTestCreateCommand();
            SecurityException ex = assertThrows(
                    SecurityException.class,
                    () -> assessmentAnswerAppService.createAnswer(command));
            assertEquals("未登录", ex.getMessage());
            verifyNoInteractions(assessmentQuestionRepository);
            verifyNoInteractions(assessmentAnswerRepository);
        }

        @Test
        @DisplayName("题目不存在：应抛出RuntimeException")
        void createAnswer_questionNotFound_shouldThrow() {
            UserVO user = createTestUser();
            when(userDomainService.getUser(TEST_USER_ID)).thenReturn(Optional.of(user));
            when(assessmentQuestionRepository.findById(TEST_QUESTION_ID)).thenThrow(new RuntimeException("题目不存在"));
            RuntimeException ex = assertThrows(
                    RuntimeException.class,
                    () -> assessmentAnswerAppService.createAnswer(createTestCreateCommand()));
            assertEquals("题目不存在", ex.getMessage());
            verify(assessmentAnswerRepository, never()).save(any());
        }

        @Test
        @DisplayName("重复提交：应抛出DataConflict")
        void createAnswer_duplicateSubmission_shouldThrow() {
            UserVO user = createTestUser();
            when(userDomainService.getUser(TEST_USER_ID)).thenReturn(Optional.of(user));
            AssessmentQuestion question = createTestQuestion();
            when(assessmentQuestionRepository.findById(TEST_QUESTION_ID)).thenReturn(Optional.of(question));
            stubDirectionAndFileValidation();
            when(assessmentSessionRepository.findByUserIdAndAssessmentTimeId(TEST_USER_ID, TEST_ASSESSMENT_TIME_ID))
                    .thenReturn(Optional.empty());
            when(assessmentAnswerRepository.existsByUserIdAndQuestionId(TEST_USER_ID, TEST_QUESTION_ID))
                    .thenReturn(true);
            DataConflict ex = assertThrows(
                    DataConflict.class,
                    () -> assessmentAnswerAppService.createAnswer(createTestCreateCommand()));
            assertEquals("已经提交过该题目的答案", ex.getMessage());
        }

        @Test
        @DisplayName("不限时考核存在过期旧会话：创建答案不应被deadline拦截")
        void createAnswer_nonTimedWithExpiredOldSession_shouldIgnoreDeadline() {
            UserVO user = createTestUser();
            when(userDomainService.getUser(TEST_USER_ID)).thenReturn(Optional.of(user));
            AssessmentAnswerCommands.CreateAssessmentAnswerCommand command = createTestCreateCommand();
            AssessmentQuestion question = createTestQuestion();
            when(assessmentQuestionRepository.findById(TEST_QUESTION_ID)).thenReturn(Optional.of(question));
            when(assessmentTimeRepository.findById(TEST_ASSESSMENT_TIME_ID))
                    .thenReturn(Optional.of(createNonTimedTime()));
            when(fileDomainService.getFileById(TEST_FILE_ID))
                    .thenReturn(FileVO.builder().id(TEST_FILE_ID).name("work.zip").type(FileType.WORK).build());
            when(assessmentAnswerRepository.existsByUserIdAndQuestionId(TEST_USER_ID, TEST_QUESTION_ID))
                    .thenReturn(false);
            doAnswer(invocation -> {
                AssessmentAnswer entity = invocation.getArgument(0);
                entity.setId(TEST_ANSWER_ID);
                return null;
            }).when(assessmentAnswerRepository).save(any(AssessmentAnswer.class));

            AssessmentAnswerResult result = assessmentAnswerAppService.createAnswer(command);
            assertNotNull(result);
            verify(assessmentSessionRepository, never())
                    .findByUserIdAndAssessmentTimeId(TEST_USER_ID, TEST_ASSESSMENT_TIME_ID);
            verify(assessmentAnswerRepository).save(any(AssessmentAnswer.class));
        }

        @Test
        @DisplayName("单选题回答正确：应同步写入AC评判但返回结果中擦除")
        void createAnswer_singleChoiceCorrect_shouldCreateAcceptedJudgementButErased() {
            UserVO user = createTestUser();
            when(userDomainService.getUser(TEST_USER_ID)).thenReturn(Optional.of(user));
            AssessmentAnswerCommands.CreateAssessmentAnswerCommand command = new AssessmentAnswerCommands.CreateAssessmentAnswerCommand(
                    TEST_USER_ID, TEST_QUESTION_ID, "B", null, null);
            AssessmentQuestion question = createSingleChoiceQuestion("B");
            when(assessmentQuestionRepository.findById(TEST_QUESTION_ID)).thenReturn(Optional.of(question));
            when(assessmentTimeRepository.findById(TEST_ASSESSMENT_TIME_ID)).thenReturn(Optional.of(createTestTime()));
            when(assessmentSessionRepository.findByUserIdAndAssessmentTimeId(TEST_USER_ID, TEST_ASSESSMENT_TIME_ID))
                    .thenReturn(Optional.empty());
            when(assessmentAnswerRepository.existsByUserIdAndQuestionId(TEST_USER_ID, TEST_QUESTION_ID))
                    .thenReturn(false);
            doAnswer(invocation -> {
                AssessmentAnswer entity = invocation.getArgument(0);
                entity.setId(TEST_ANSWER_ID);
                return null;
            }).when(assessmentAnswerRepository).save(any(AssessmentAnswer.class));
            when(assessmentJudgementDomainService.createJudgement(any(AssessmentJudgementVO.class)))
                    .thenReturn(createJudgementVO(ObjectiveResultCode.AC));

            AssessmentAnswerResult result = assessmentAnswerAppService.createAnswer(command);
            assertNull(result.judgement());
        }

        @Test
        @DisplayName("单选题回答错误：应同步写入WA评判但返回结果中擦除")
        void createAnswer_singleChoiceWrong_shouldCreateWrongAnswerJudgementButErased() {
            UserVO user = createTestUser();
            when(userDomainService.getUser(TEST_USER_ID)).thenReturn(Optional.of(user));
            AssessmentAnswerCommands.CreateAssessmentAnswerCommand command = new AssessmentAnswerCommands.CreateAssessmentAnswerCommand(
                    TEST_USER_ID, TEST_QUESTION_ID, "A", null, null);
            AssessmentQuestion question = createSingleChoiceQuestion("B");
            when(assessmentQuestionRepository.findById(TEST_QUESTION_ID)).thenReturn(Optional.of(question));
            when(assessmentTimeRepository.findById(TEST_ASSESSMENT_TIME_ID)).thenReturn(Optional.of(createTestTime()));
            when(assessmentSessionRepository.findByUserIdAndAssessmentTimeId(TEST_USER_ID, TEST_ASSESSMENT_TIME_ID))
                    .thenReturn(Optional.empty());
            when(assessmentAnswerRepository.existsByUserIdAndQuestionId(TEST_USER_ID, TEST_QUESTION_ID))
                    .thenReturn(false);
            doAnswer(invocation -> {
                AssessmentAnswer entity = invocation.getArgument(0);
                entity.setId(TEST_ANSWER_ID);
                return null;
            }).when(assessmentAnswerRepository).save(any(AssessmentAnswer.class));
            when(assessmentJudgementDomainService.createJudgement(any(AssessmentJudgementVO.class)))
                    .thenReturn(createJudgementVO(ObjectiveResultCode.WA));

            AssessmentAnswerResult result = assessmentAnswerAppService.createAnswer(command);
            assertNull(result.judgement());
        }
    }

    @Nested
    @DisplayName("updateAnswer 方法测试")
    class UpdateAnswerTests {
        @Test
        @DisplayName("不限时考核存在过期旧会话：修改答案不应被deadline拦截")
        void updateAnswer_nonTimedWithExpiredOldSession_shouldIgnoreDeadline() {
            UserVO user = createTestUser();
            when(userDomainService.getUser(TEST_USER_ID)).thenReturn(Optional.of(user));
            AssessmentAnswerCommands.UpdateAssessmentAnswerCommand command = createTestUpdateCommand();
            AssessmentQuestion question = createTestQuestion();
            AssessmentAnswer existing = createTestAnswer();
            AssessmentAnswer updated = createTestAnswer();
            when(assessmentQuestionRepository.findById(TEST_QUESTION_ID)).thenReturn(Optional.of(question));
            when(assessmentTimeRepository.findById(TEST_ASSESSMENT_TIME_ID))
                    .thenReturn(Optional.of(createNonTimedTime()));
            when(fileDomainService.getFileById(TEST_FILE_ID))
                    .thenReturn(FileVO.builder().id(TEST_FILE_ID).name("work.zip").type(FileType.WORK).build());
            when(assessmentAnswerRepository.findByUserIdAndQuestionId(TEST_USER_ID, TEST_QUESTION_ID))
                    .thenReturn(Optional.of(existing));
            when(assessmentAnswerRepository.findByUserIdAndQuestionId(TEST_USER_ID, TEST_QUESTION_ID))
                    .thenReturn(Optional.of(updated));

            AssessmentAnswerResult result = assessmentAnswerAppService.updateAnswer(command);
            assertNotNull(result);
            verify(assessmentSessionRepository, never())
                    .findByUserIdAndAssessmentTimeId(TEST_USER_ID, TEST_ASSESSMENT_TIME_ID);
        }

        @Test
        @DisplayName("多选题回答顺序不同但集合一致：应同步写入AC评判但返回结果中擦除")
        void updateAnswer_multipleChoiceSameSet_shouldCreateAcceptedJudgementButErased() {
            UserVO user = createTestUser();
            when(userDomainService.getUser(TEST_USER_ID)).thenReturn(Optional.of(user));
            AssessmentAnswerCommands.UpdateAssessmentAnswerCommand command = new AssessmentAnswerCommands.UpdateAssessmentAnswerCommand(
                    TEST_USER_ID, TEST_QUESTION_ID, "[\"B\",\"A\"]", null, null);
            AssessmentQuestion question = createMultipleChoiceQuestion("A", "B");
            AssessmentAnswer existing = createTestAnswer();
            AssessmentAnswer updated = AssessmentAnswer.reconstruct(
                    TEST_ANSWER_ID,
                    TEST_USER_ID,
                    TEST_QUESTION_ID,
                    "[\"B\",\"A\"]",
                    null,
                    TEST_FILE_ID,
                    TEST_SUBMIT_TIME,
                    null);
            when(assessmentQuestionRepository.findById(TEST_QUESTION_ID)).thenReturn(Optional.of(question));
            when(assessmentTimeRepository.findById(TEST_ASSESSMENT_TIME_ID)).thenReturn(Optional.of(createTestTime()));
            when(assessmentSessionRepository.findByUserIdAndAssessmentTimeId(TEST_USER_ID, TEST_ASSESSMENT_TIME_ID))
                    .thenReturn(Optional.empty());
            when(assessmentAnswerRepository.findByUserIdAndQuestionId(TEST_USER_ID, TEST_QUESTION_ID))
                    .thenReturn(Optional.of(existing), Optional.of(updated));
            when(assessmentJudgementDomainService.createJudgement(any(AssessmentJudgementVO.class)))
                    .thenReturn(createJudgementVO(ObjectiveResultCode.AC));

            AssessmentAnswerResult result = assessmentAnswerAppService.updateAnswer(command);
            assertNull(result.judgement());
        }

        @Test
        @DisplayName("多选题答案格式错误：应拒绝并抛出BadRequest")
        void updateAnswer_multipleChoiceInvalidJson_shouldThrowBadRequest() {
            UserVO user = createTestUser();
            when(userDomainService.getUser(TEST_USER_ID)).thenReturn(Optional.of(user));
            AssessmentAnswerCommands.UpdateAssessmentAnswerCommand command = new AssessmentAnswerCommands.UpdateAssessmentAnswerCommand(
                    TEST_USER_ID, TEST_QUESTION_ID, "A,B", null, null);
            AssessmentQuestion question = createMultipleChoiceQuestion("A", "B");
            AssessmentAnswer existing = createTestAnswer();
            AssessmentAnswer updated = AssessmentAnswer.reconstruct(
                    TEST_ANSWER_ID,
                    TEST_USER_ID,
                    TEST_QUESTION_ID,
                    "A,B",
                    null,
                    TEST_FILE_ID,
                    TEST_SUBMIT_TIME,
                    null);
            when(assessmentQuestionRepository.findById(TEST_QUESTION_ID)).thenReturn(Optional.of(question));
            when(assessmentTimeRepository.findById(TEST_ASSESSMENT_TIME_ID)).thenReturn(Optional.of(createTestTime()));
            when(assessmentSessionRepository.findByUserIdAndAssessmentTimeId(TEST_USER_ID, TEST_ASSESSMENT_TIME_ID))
                    .thenReturn(Optional.empty());
            when(assessmentAnswerRepository.findByUserIdAndQuestionId(TEST_USER_ID, TEST_QUESTION_ID))
                    .thenReturn(Optional.of(existing), Optional.of(updated));

            com.bluenet.web.domain.exception.BadRequest ex = assertThrows(
                    com.bluenet.web.domain.exception.BadRequest.class,
                    () -> assessmentAnswerAppService.updateAnswer(command));
            assertEquals("多选题答案格式错误", ex.getMessage());
            verify(assessmentJudgementDomainService, never()).createJudgement(any());
        }

        @Test
        @DisplayName("队长更新 FILE_UPLOAD 答案：应同步组员答案")
        void updateAnswer_teamLeaderFileUpload_shouldSyncMemberAnswers() {
            UserVO user = createTestUser();
            when(userDomainService.getUser(TEST_USER_ID)).thenReturn(Optional.of(user));

            AssessmentQuestion question = AssessmentQuestion.reconstruct(
                    TEST_QUESTION_ID,
                    TEST_ASSESSMENT_TIME_ID,
                    1,
                    QuestionType.FILE_UPLOAD,
                    "作品提交",
                    null,
                    null,
                    BigDecimal.TEN);
            when(assessmentQuestionRepository.findById(TEST_QUESTION_ID)).thenReturn(Optional.of(question));

            AssessmentTime time = AssessmentTime.reconstruct(
                    TEST_ASSESSMENT_TIME_ID,
                    Direction.COMPUTER_VISION,
                    1,
                    2024,
                    null,
                    null,
                    false,
                    null,
                    null,
                    true);
            when(assessmentTimeRepository.findById(TEST_ASSESSMENT_TIME_ID)).thenReturn(Optional.of(time));
            when(fileDomainService.getFileById(TEST_FILE_ID))
                    .thenReturn(FileVO.builder().id(TEST_FILE_ID).name("work.zip").type(FileType.WORK).build());

            Long teamId = 50L;
            AssessmentAnswer existing = AssessmentAnswer.reconstruct(
                    TEST_ANSWER_ID,
                    TEST_USER_ID,
                    TEST_QUESTION_ID,
                    "old content",
                    null,
                    TEST_FILE_ID,
                    TEST_SUBMIT_TIME,
                    teamId);
            AssessmentAnswer updated = AssessmentAnswer.reconstruct(
                    TEST_ANSWER_ID,
                    TEST_USER_ID,
                    TEST_QUESTION_ID,
                    "new content",
                    null,
                    TEST_FILE_ID,
                    LocalDateTime.now(),
                    teamId);
            when(assessmentAnswerRepository.findByUserIdAndQuestionId(TEST_USER_ID, TEST_QUESTION_ID))
                    .thenReturn(Optional.of(existing), Optional.of(updated));

            com.bluenet.web.domain.model.entity.AssessmentTeam team = com.bluenet.web.domain.model.entity.AssessmentTeam
                    .reconstruct(
                            teamId,
                            TEST_ASSESSMENT_TIME_ID,
                            TEST_USER_ID,
                            "测试队",
                            "ABC123",
                            com.bluenet.web.domain.model.entity.AssessmentTeam.TeamStatus.ACTIVE,
                            LocalDateTime.now());
            when(assessmentTeamRepository.findByAssessmentTimeIdAndUserId(TEST_ASSESSMENT_TIME_ID, TEST_USER_ID))
                    .thenReturn(Optional.of(team));
            when(
                    assessmentAnswerRepository.updateTeamMemberAnswers(
                            eq(teamId),
                            eq(TEST_QUESTION_ID),
                            eq(TEST_FILE_ID),
                            eq("new content"),
                            isNull(),
                            any(LocalDateTime.class)))
                                    .thenReturn(3);

            AssessmentAnswerCommands.UpdateAssessmentAnswerCommand command = new AssessmentAnswerCommands.UpdateAssessmentAnswerCommand(
                    TEST_USER_ID, TEST_QUESTION_ID, "new content", null, TEST_FILE_ID);
            AssessmentAnswerResult result = assessmentAnswerAppService.updateAnswer(command);

            assertNotNull(result);
            verify(assessmentAnswerRepository).updateTeamMemberAnswers(
                    eq(teamId),
                    eq(TEST_QUESTION_ID),
                    eq(TEST_FILE_ID),
                    eq("new content"),
                    isNull(),
                    any(LocalDateTime.class));
            verify(assessmentAnswerRepository, never()).update(any(AssessmentAnswer.class));
        }
    }

    @Nested
    @DisplayName("方向匹配和 fileId 校验测试")
    class ValidationTests {
        @Test
        @DisplayName("TC-402: 方向不匹配应抛出Forbidden")
        void createAnswer_directionMismatch_shouldThrowForbidden() {
            UserVO user = UserVO.builder()
                    .id(TEST_USER_ID)
                    .studentId("2024123456")
                    .roleName("CANDIDATE")
                    .direction(Direction.EMBEDDED)
                    .build();
            when(userDomainService.getUser(TEST_USER_ID)).thenReturn(Optional.of(user));
            AssessmentAnswerCommands.CreateAssessmentAnswerCommand command = new AssessmentAnswerCommands.CreateAssessmentAnswerCommand(
                    TEST_USER_ID, TEST_QUESTION_ID, "answer", null, null);
            AssessmentQuestion question = createTestQuestion();
            when(assessmentQuestionRepository.findById(TEST_QUESTION_ID)).thenReturn(Optional.of(question));
            when(assessmentTimeRepository.findById(TEST_ASSESSMENT_TIME_ID)).thenReturn(Optional.of(createTestTime()));
            Forbidden ex = assertThrows(Forbidden.class, () -> assessmentAnswerAppService.createAnswer(command));
            assertEquals("方向不匹配", ex.getMessage());
            verify(assessmentAnswerRepository, never()).save(any());
        }

        @Test
        @DisplayName("TC-403: fileId 对应文件不存在应抛出BadRequest")
        void createAnswer_fileNotFound_shouldThrowBadRequest() {
            UserVO user = createTestUser();
            when(userDomainService.getUser(TEST_USER_ID)).thenReturn(Optional.of(user));
            AssessmentAnswerCommands.CreateAssessmentAnswerCommand command = new AssessmentAnswerCommands.CreateAssessmentAnswerCommand(
                    TEST_USER_ID, TEST_QUESTION_ID, "answer", null, 9999L);
            AssessmentQuestion question = createTestQuestion();
            when(assessmentQuestionRepository.findById(TEST_QUESTION_ID)).thenReturn(Optional.of(question));
            when(assessmentTimeRepository.findById(TEST_ASSESSMENT_TIME_ID)).thenReturn(Optional.of(createTestTime()));
            when(fileDomainService.getFileById(9999L)).thenReturn(null);
            com.bluenet.web.domain.exception.BadRequest ex = assertThrows(
                    com.bluenet.web.domain.exception.BadRequest.class,
                    () -> assessmentAnswerAppService.createAnswer(command));
            assertEquals("文件不存在", ex.getMessage());
            verify(assessmentAnswerRepository, never()).save(any());
        }

        @Test
        @DisplayName("TC-404: fileId 类型不是 WORK 应抛出BadRequest")
        void createAnswer_fileTypeMismatch_shouldThrowBadRequest() {
            UserVO user = createTestUser();
            when(userDomainService.getUser(TEST_USER_ID)).thenReturn(Optional.of(user));
            AssessmentAnswerCommands.CreateAssessmentAnswerCommand command = createTestCreateCommand();
            AssessmentQuestion question = createTestQuestion();
            when(assessmentQuestionRepository.findById(TEST_QUESTION_ID)).thenReturn(Optional.of(question));
            when(assessmentTimeRepository.findById(TEST_ASSESSMENT_TIME_ID)).thenReturn(Optional.of(createTestTime()));
            when(fileDomainService.getFileById(TEST_FILE_ID))
                    .thenReturn(FileVO.builder().id(TEST_FILE_ID).name("avatar.jpg").type(FileType.AVATAR).build());
            com.bluenet.web.domain.exception.BadRequest ex = assertThrows(
                    com.bluenet.web.domain.exception.BadRequest.class,
                    () -> assessmentAnswerAppService.createAnswer(command));
            assertEquals("文件类型不匹配，期望 WORK", ex.getMessage());
            verify(assessmentAnswerRepository, never()).save(any());
        }

        @Test
        @DisplayName("TC-405: fileId 为 null 但 content 有值应正常创建")
        void createAnswer_noFileId_shouldSucceed() {
            UserVO user = createTestUser();
            when(userDomainService.getUser(TEST_USER_ID)).thenReturn(Optional.of(user));
            AssessmentAnswerCommands.CreateAssessmentAnswerCommand command = new AssessmentAnswerCommands.CreateAssessmentAnswerCommand(
                    TEST_USER_ID, TEST_QUESTION_ID, "text answer", null, null);
            AssessmentQuestion question = createTestQuestion();
            when(assessmentQuestionRepository.findById(TEST_QUESTION_ID)).thenReturn(Optional.of(question));
            when(assessmentTimeRepository.findById(TEST_ASSESSMENT_TIME_ID)).thenReturn(Optional.of(createTestTime()));
            when(assessmentSessionRepository.findByUserIdAndAssessmentTimeId(TEST_USER_ID, TEST_ASSESSMENT_TIME_ID))
                    .thenReturn(Optional.empty());
            when(assessmentAnswerRepository.existsByUserIdAndQuestionId(TEST_USER_ID, TEST_QUESTION_ID))
                    .thenReturn(false);
            doAnswer(invocation -> {
                AssessmentAnswer entity = invocation.getArgument(0);
                entity.setId(TEST_ANSWER_ID);
                return null;
            }).when(assessmentAnswerRepository).save(any(AssessmentAnswer.class));

            AssessmentAnswerResult result = assessmentAnswerAppService.createAnswer(command);
            assertNotNull(result);
            verify(fileDomainService, never()).getFileById(anyLong());
        }
    }

    @Nested
    @DisplayName("getMyAnswer 方法测试")
    class GetMyAnswerTests {
        @Test
        @DisplayName("答案存在：应返回Result")
        void getMyAnswer_answerExists_shouldReturnResult() {
            AssessmentAnswer answer = createTestAnswer();
            when(assessmentAnswerRepository.findByUserIdAndQuestionId(TEST_USER_ID, TEST_QUESTION_ID))
                    .thenReturn(Optional.of(answer));
            when(commentDomainService.listCommentsByAnswerId(TEST_ANSWER_ID))
                    .thenReturn(java.util.Collections.emptyList());
            AssessmentAnswerResult result = assessmentAnswerAppService.getMyAnswer(TEST_USER_ID, TEST_QUESTION_ID);
            assertNotNull(result);
            assertEquals(TEST_ANSWER_ID, result.id());
            assertEquals(TEST_QUESTION_ID, result.questionId());
            assertEquals(TEST_FILE_ID, result.fileId());
            assertEquals("test answer content", result.content());
            assertEquals(TEST_SUBMIT_TIME, result.submitTime());
            verify(assessmentAnswerRepository).findByUserIdAndQuestionId(TEST_USER_ID, TEST_QUESTION_ID);
        }

        @Test
        @DisplayName("非选择题答案存在且已有评判：应返回最新评判结果")
        void getMyAnswer_nonChoiceQuestionWithJudgement_shouldReturnLatestJudgement() {
            AssessmentAnswer answer = createTestAnswer();
            AssessmentQuestion question = createTestQuestion();
            when(assessmentAnswerRepository.findByUserIdAndQuestionId(TEST_USER_ID, TEST_QUESTION_ID))
                    .thenReturn(Optional.of(answer));
            when(assessmentQuestionRepository.findById(TEST_QUESTION_ID))
                    .thenReturn(Optional.of(question));
            when(assessmentJudgementDomainService.getLatestByAnswerId(TEST_ANSWER_ID))
                    .thenReturn(createJudgementVO(ObjectiveResultCode.AC));
            when(commentDomainService.listCommentsByAnswerId(TEST_ANSWER_ID))
                    .thenReturn(java.util.Collections.emptyList());
            AssessmentAnswerResult result = assessmentAnswerAppService.getMyAnswer(TEST_USER_ID, TEST_QUESTION_ID);
            assertNotNull(result);
            assertNotNull(result.judgement());
            assertEquals(ObjectiveResultCode.AC, result.judgement().resultCode());
        }

        @Test
        @DisplayName("单选题答案存在且已有评判：应擦除评判结果")
        void getMyAnswer_singleChoiceWithJudgement_shouldEraseJudgement() {
            AssessmentAnswer answer = createTestAnswer();
            AssessmentQuestion question = createSingleChoiceQuestion("B");
            when(assessmentAnswerRepository.findByUserIdAndQuestionId(TEST_USER_ID, TEST_QUESTION_ID))
                    .thenReturn(Optional.of(answer));
            when(assessmentQuestionRepository.findById(TEST_QUESTION_ID))
                    .thenReturn(Optional.of(question));
            when(assessmentJudgementDomainService.getLatestByAnswerId(TEST_ANSWER_ID))
                    .thenReturn(createJudgementVO(ObjectiveResultCode.AC));
            when(commentDomainService.listCommentsByAnswerId(TEST_ANSWER_ID))
                    .thenReturn(java.util.Collections.emptyList());
            AssessmentAnswerResult result = assessmentAnswerAppService.getMyAnswer(TEST_USER_ID, TEST_QUESTION_ID);
            assertNotNull(result);
            assertNull(result.judgement());
        }

        @Test
        @DisplayName("答案不存在：应返回null")
        void getMyAnswer_answerNotExists_shouldReturnNull() {
            when(assessmentAnswerRepository.findByUserIdAndQuestionId(TEST_USER_ID, TEST_QUESTION_ID))
                    .thenReturn(Optional.empty());
            AssessmentAnswerResult result = assessmentAnswerAppService.getMyAnswer(TEST_USER_ID, TEST_QUESTION_ID);
            assertNull(result);
            verify(assessmentAnswerRepository).findByUserIdAndQuestionId(TEST_USER_ID, TEST_QUESTION_ID);
        }
    }

    @Nested
    @DisplayName("淘汰检查测试")
    class EliminationTests {

        @Test
        @DisplayName("被淘汰考生创建答案：应抛出Forbidden")
        void createAnswer_eliminatedCandidate_shouldThrowForbidden() {
            UserVO user = createTestUser();
            when(userDomainService.getUser(TEST_USER_ID)).thenReturn(Optional.of(user));
            AssessmentQuestion question = createTestQuestion();
            when(assessmentQuestionRepository.findById(TEST_QUESTION_ID)).thenReturn(Optional.of(question));
            when(assessmentTimeRepository.findById(TEST_ASSESSMENT_TIME_ID)).thenReturn(Optional.of(createTestTime()));
            when(assessmentDecisionDomainService.isEliminatedFromPriorEpoch(TEST_USER_ID, createTestTime()))
                    .thenReturn(true);

            AssessmentAnswerCommands.CreateAssessmentAnswerCommand command = createTestCreateCommand();
            Forbidden ex = assertThrows(
                    Forbidden.class,
                    () -> assessmentAnswerAppService.createAnswer(command));
            assertEquals("已在该方向考核中被淘汰，无法提交答案", ex.getMessage());
            verify(assessmentAnswerRepository, never()).save(any());
        }

        @Test
        @DisplayName("被淘汰考生更新答案：应抛出Forbidden")
        void updateAnswer_eliminatedCandidate_shouldThrowForbidden() {
            UserVO user = createTestUser();
            when(userDomainService.getUser(TEST_USER_ID)).thenReturn(Optional.of(user));
            AssessmentQuestion question = createTestQuestion();
            when(assessmentQuestionRepository.findById(TEST_QUESTION_ID)).thenReturn(Optional.of(question));
            when(assessmentTimeRepository.findById(TEST_ASSESSMENT_TIME_ID)).thenReturn(Optional.of(createTestTime()));
            when(assessmentDecisionDomainService.isEliminatedFromPriorEpoch(TEST_USER_ID, createTestTime()))
                    .thenReturn(true);

            AssessmentAnswerCommands.UpdateAssessmentAnswerCommand command = createTestUpdateCommand();
            Forbidden ex = assertThrows(
                    Forbidden.class,
                    () -> assessmentAnswerAppService.updateAnswer(command));
            assertEquals("已在该方向考核中被淘汰，无法提交答案", ex.getMessage());
            verify(assessmentAnswerRepository, never()).update(any());
        }

        @Test
        @DisplayName("非考生角色提交答案：淘汰检查应跳过")
        void createAnswer_nonCandidate_shouldSkipEliminationCheck() {
            UserVO user = UserVO.builder()
                    .id(TEST_USER_ID)
                    .studentId("2024123456")
                    .roleName("MEMBER")
                    .direction(Direction.COMPUTER_VISION)
                    .build();
            when(userDomainService.getUser(TEST_USER_ID)).thenReturn(Optional.of(user));
            AssessmentQuestion question = createTestQuestion();
            when(assessmentQuestionRepository.findById(TEST_QUESTION_ID)).thenReturn(Optional.of(question));
            when(assessmentTimeRepository.findById(TEST_ASSESSMENT_TIME_ID)).thenReturn(Optional.of(createTestTime()));
            when(assessmentSessionRepository.findByUserIdAndAssessmentTimeId(TEST_USER_ID, TEST_ASSESSMENT_TIME_ID))
                    .thenReturn(Optional.empty());
            when(assessmentAnswerRepository.existsByUserIdAndQuestionId(TEST_USER_ID, TEST_QUESTION_ID))
                    .thenReturn(false);
            doAnswer(invocation -> {
                AssessmentAnswer entity = invocation.getArgument(0);
                entity.setId(TEST_ANSWER_ID);
                return null;
            }).when(assessmentAnswerRepository).save(any(AssessmentAnswer.class));

            AssessmentAnswerCommands.CreateAssessmentAnswerCommand command = new AssessmentAnswerCommands.CreateAssessmentAnswerCommand(
                    TEST_USER_ID, TEST_QUESTION_ID, "test answer", null, null);
            AssessmentAnswerResult result = assessmentAnswerAppService.createAnswer(command);

            assertNotNull(result);
            verify(assessmentDecisionDomainService, never()).isEliminatedFromPriorEpoch(any(), any());
        }

        @Test
        @DisplayName("队长提交 FILE_UPLOAD 答案：应为组员批量创建答案")
        void createAnswer_teamLeaderFileUpload_shouldBatchCreateMemberAnswers() {
            UserVO user = createTestUser();
            when(userDomainService.getUser(TEST_USER_ID)).thenReturn(Optional.of(user));

            AssessmentQuestion question = AssessmentQuestion.reconstruct(
                    TEST_QUESTION_ID,
                    TEST_ASSESSMENT_TIME_ID,
                    1,
                    QuestionType.FILE_UPLOAD,
                    "作品提交",
                    null,
                    null,
                    BigDecimal.TEN);
            when(assessmentQuestionRepository.findById(TEST_QUESTION_ID)).thenReturn(Optional.of(question));

            AssessmentTime time = AssessmentTime.reconstruct(
                    TEST_ASSESSMENT_TIME_ID,
                    Direction.COMPUTER_VISION,
                    1,
                    2024,
                    null,
                    null,
                    false,
                    null,
                    null,
                    true);
            when(assessmentTimeRepository.findById(TEST_ASSESSMENT_TIME_ID)).thenReturn(Optional.of(time));
            when(fileDomainService.getFileById(TEST_FILE_ID))
                    .thenReturn(FileVO.builder().id(TEST_FILE_ID).name("work.zip").type(FileType.WORK).build());
            when(assessmentAnswerRepository.existsByUserIdAndQuestionId(TEST_USER_ID, TEST_QUESTION_ID))
                    .thenReturn(false);

            Long teamId = 50L;
            com.bluenet.web.domain.model.entity.AssessmentTeam team = com.bluenet.web.domain.model.entity.AssessmentTeam
                    .reconstruct(
                            teamId,
                            TEST_ASSESSMENT_TIME_ID,
                            TEST_USER_ID,
                            "测试队",
                            "ABC123",
                            com.bluenet.web.domain.model.entity.AssessmentTeam.TeamStatus.ACTIVE,
                            LocalDateTime.now());
            when(assessmentTeamRepository.findByAssessmentTimeIdAndUserId(TEST_ASSESSMENT_TIME_ID, TEST_USER_ID))
                    .thenReturn(Optional.of(team));

            Long memberId1 = 2L;
            Long memberId2 = 3L;
            when(assessmentTeamRepository.findMembersByTeamId(teamId))
                    .thenReturn(
                            List.of(
                                    com.bluenet.web.domain.model.entity.AssessmentTeamMember.reconstruct(
                                            1L,
                                            teamId,
                                            TEST_USER_ID,
                                            LocalDateTime.now()),
                                    com.bluenet.web.domain.model.entity.AssessmentTeamMember.reconstruct(
                                            2L,
                                            teamId,
                                            memberId1,
                                            LocalDateTime.now()),
                                    com.bluenet.web.domain.model.entity.AssessmentTeamMember.reconstruct(
                                            3L,
                                            teamId,
                                            memberId2,
                                            LocalDateTime.now())));
            when(assessmentAnswerRepository.findExistingAnswerUserIds(List.of(memberId1, memberId2), TEST_QUESTION_ID))
                    .thenReturn(List.of());

            doAnswer(invocation -> {
                AssessmentAnswer entity = invocation.getArgument(0);
                entity.setId(TEST_ANSWER_ID);
                return null;
            }).when(assessmentAnswerRepository).save(any(AssessmentAnswer.class));

            AssessmentAnswerCommands.CreateAssessmentAnswerCommand command = new AssessmentAnswerCommands.CreateAssessmentAnswerCommand(
                    TEST_USER_ID, TEST_QUESTION_ID, "content", null, TEST_FILE_ID);
            AssessmentAnswerResult result = assessmentAnswerAppService.createAnswer(command);

            assertNotNull(result);
            ArgumentCaptor<List<AssessmentAnswer>> captor = ArgumentCaptor.forClass(List.class);
            verify(assessmentAnswerRepository).batchInsert(captor.capture());
            List<AssessmentAnswer> inserted = captor.getValue();
            assertEquals(2, inserted.size());
            assertTrue(inserted.stream().allMatch(a -> a.getTeamId().equals(teamId)));
            assertTrue(inserted.stream().anyMatch(a -> a.getUserId().equals(memberId1)));
            assertTrue(inserted.stream().anyMatch(a -> a.getUserId().equals(memberId2)));
        }
    }
}
