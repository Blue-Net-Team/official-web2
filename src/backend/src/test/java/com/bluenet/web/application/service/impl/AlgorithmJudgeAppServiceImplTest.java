package com.bluenet.web.application.service.impl;

import com.bluenet.web.domain.model.enumerate.RoleType;

import com.bluenet.web.application.AlgorithmJudgeResult;
import com.bluenet.web.application.command.algorithm_judge.AlgorithmJudgeCommands;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.model.entity.AlgorithmJudgeCaseResult;
import com.bluenet.web.domain.model.entity.AlgorithmJudgeJob;
import com.bluenet.web.domain.model.enumerate.AlgorithmTestcaseType;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.JudgeCaseStatus;
import com.bluenet.web.domain.model.enumerate.JudgeJobStatus;
import com.bluenet.web.domain.model.enumerate.JudgementSource;
import com.bluenet.web.domain.model.enumerate.JudgementStatus;
import com.bluenet.web.domain.model.enumerate.ObjectiveResultCode;
import com.bluenet.web.domain.model.enumerate.ProgrammingLanguage;
import com.bluenet.web.domain.model.enumerate.QuestionType;
import com.bluenet.web.domain.model.entity.AssessmentAnswer;
import com.bluenet.web.domain.model.vo.AssessmentJudgementVO;
import com.bluenet.web.domain.model.entity.AssessmentQuestion;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.vo.evaluation.AlgorithmContent;
import com.bluenet.web.domain.repository.AlgorithmJudgeCaseResultRepository;
import com.bluenet.web.domain.repository.AlgorithmJudgeJobRepository;
import com.bluenet.web.domain.repository.AssessmentAnswerRepository;
import com.bluenet.web.domain.repository.AssessmentSessionRepository;
import com.bluenet.web.domain.service.AssessmentJudgementDomainService;
import com.bluenet.web.domain.repository.AssessmentQuestionRepository;
import com.bluenet.web.domain.repository.AssessmentTimeRepository;
import com.bluenet.web.infrastructure.judge.AlgorithmJudgeJobPublisher;
import com.bluenet.web.infrastructure.repository.mapper.JudgeLanguageLimitMapper;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("AlgorithmJudgeAppServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class AlgorithmJudgeAppServiceImplTest {
    private static final Long USER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;
    private static final Long QUESTION_ID = 10L;
    private static final Long ASSESSMENT_TIME_ID = 20L;
    private static final Long ANSWER_ID = 30L;
    private static final Long JOB_ID = 40L;

    @Mock
    private AssessmentQuestionRepository assessmentQuestionRepository;
    @Mock
    private AssessmentTimeRepository assessmentTimeRepository;

    @Mock
    private AssessmentJudgementDomainService assessmentJudgementDomainService;
    @Mock
    private AssessmentAnswerRepository assessmentAnswerRepository;
    @Mock
    private AssessmentSessionRepository assessmentSessionRepository;
    @Mock
    private AlgorithmJudgeJobRepository algorithmJudgeJobRepository;
    @Mock
    private AlgorithmJudgeCaseResultRepository algorithmJudgeCaseResultRepository;
    @Mock
    private AlgorithmJudgeJobPublisher algorithmJudgeJobPublisher;
    @Mock
    private JudgeLanguageLimitMapper judgeLanguageLimitMapper;

    private AlgorithmJudgeAppServiceImpl algorithmJudgeAppService;

    @BeforeEach
    void setUp() {
        algorithmJudgeAppService = new AlgorithmJudgeAppServiceImpl(
                assessmentQuestionRepository,
                assessmentTimeRepository,
                assessmentJudgementDomainService,
                assessmentAnswerRepository,
                assessmentSessionRepository,
                algorithmJudgeJobRepository,
                algorithmJudgeCaseResultRepository,
                algorithmJudgeJobPublisher,
                judgeLanguageLimitMapper);
    }

    @Test
    @DisplayName("默认运行：应创建 DEFAULT_RUN 任务并投递队列")
    void run_defaultTestcase_shouldCreateJobAndPublish() {
        try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
            mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createUser(USER_ID));
            stubQuestionAndTime(createAlgorithmQuestion());
            stubSaveJobId();

            AlgorithmJudgeCommands.RunCommand command = new AlgorithmJudgeCommands.RunCommand(
                    QUESTION_ID,
                    ProgrammingLanguage.PYTHON,
                    "print(input())",
                    AlgorithmTestcaseType.DEFAULT_RUN,
                    null);

            AlgorithmJudgeResult.SubmitResult result = algorithmJudgeAppService.run(command);

            assertEquals(JOB_ID, result.judgeJobId());
            assertEquals(AlgorithmTestcaseType.DEFAULT_RUN, result.testcaseType());
            assertNull(result.answerId());
            verify(algorithmJudgeJobRepository).save(any(AlgorithmJudgeJob.class));
            verify(algorithmJudgeJobPublisher).publish(JOB_ID, AlgorithmTestcaseType.DEFAULT_RUN);
        }
    }

    @Test
    @DisplayName("自定义运行：应允许没有期望输出的 CUSTOM_RUN 任务")
    void run_customInput_shouldCreateCustomRunJob() {
        try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
            mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createUser(USER_ID));
            stubQuestionAndTime(createAlgorithmQuestion());
            stubSaveJobId();

            AlgorithmJudgeCommands.RunCommand command = new AlgorithmJudgeCommands.RunCommand(
                    QUESTION_ID,
                    ProgrammingLanguage.PYTHON,
                    "print(input())",
                    AlgorithmTestcaseType.CUSTOM_RUN,
                    "hello");

            algorithmJudgeAppService.run(command);

            verify(algorithmJudgeJobRepository).save(any(AlgorithmJudgeJob.class));
            verify(algorithmJudgeJobPublisher).publish(JOB_ID, AlgorithmTestcaseType.CUSTOM_RUN);
        }
    }

    @Test
    @DisplayName("语言不在 starterCode 中：应拒绝运行")
    void run_unsupportedLanguage_shouldReject() {
        try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
            mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createUser(USER_ID));
            stubQuestionAndTime(createAlgorithmQuestion());

            AlgorithmJudgeCommands.RunCommand command = new AlgorithmJudgeCommands.RunCommand(
                    QUESTION_ID,
                    ProgrammingLanguage.JAVA,
                    "class Main {}",
                    AlgorithmTestcaseType.DEFAULT_RUN,
                    null);

            BadRequest ex = assertThrows(BadRequest.class, () -> algorithmJudgeAppService.run(command));
            assertEquals("该题不支持提交语言：java", ex.getMessage());
            verify(algorithmJudgeJobRepository, never()).save(any());
            verify(algorithmJudgeJobPublisher, never()).publish(any(), any());
        }
    }

    @Test
    @DisplayName("正式提交：应保存答案、创建 FORMAL 任务并投递队列")
    void submit_supportedLanguage_shouldSaveAnswerAndPublishFormalJob() {
        try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
            mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createUser(USER_ID));
            stubQuestionAndTime(createAlgorithmQuestion());
            when(assessmentAnswerRepository.findByUserIdAndQuestionId(USER_ID, QUESTION_ID))
                    .thenReturn(Optional.empty());
            when(
                    judgeLanguageLimitMapper
                            .countConfirmedByQuestionIdAndLanguage(QUESTION_ID, ProgrammingLanguage.PYTHON.getValue()))
                                    .thenReturn(1);
            doAnswer(invocation -> {
                AssessmentAnswer entity = invocation.getArgument(0);
                entity.setId(ANSWER_ID);
                return null;
            }).when(assessmentAnswerRepository).save(any(AssessmentAnswer.class));
            stubSaveJobId();

            AlgorithmJudgeCommands.SubmitCommand command = new AlgorithmJudgeCommands.SubmitCommand(
                    QUESTION_ID,
                    ProgrammingLanguage.PYTHON,
                    "print(input())");

            AlgorithmJudgeResult.SubmitResult result = algorithmJudgeAppService.submit(command);

            assertEquals(ANSWER_ID, result.answerId());
            assertEquals(JOB_ID, result.judgeJobId());
            assertEquals(AlgorithmTestcaseType.FORMAL, result.testcaseType());
            verify(assessmentAnswerRepository).save(any(AssessmentAnswer.class));
            ArgumentCaptor<AlgorithmJudgeJob> jobCaptor = ArgumentCaptor.forClass(AlgorithmJudgeJob.class);
            verify(algorithmJudgeJobRepository).save(jobCaptor.capture());
            assertEquals(AlgorithmTestcaseType.FORMAL, jobCaptor.getValue().getTestcaseType());
            verify(algorithmJudgeJobPublisher).publish(JOB_ID, AlgorithmTestcaseType.FORMAL);
        }
    }

    @Test
    @DisplayName("轮询运行中任务：不应返回最终用例")
    void getJob_running_shouldReturnStatusWithoutCaseResults() {
        try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
            mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createUser(USER_ID));
            when(algorithmJudgeJobRepository.findById(JOB_ID))
                    .thenReturn(Optional.of(createJob(JudgeJobStatus.RUNNING, null)));

            AlgorithmJudgeResult.PollResult result = algorithmJudgeAppService.getJob(JOB_ID);

            assertEquals(JOB_ID, result.judgeJobId());
            assertEquals(AlgorithmTestcaseType.DEFAULT_RUN, result.testcaseType());
            assertEquals(JudgeJobStatus.RUNNING, result.status());
            assertEquals(0, result.caseResults().size());
            assertNull(result.judgement());
            verify(algorithmJudgeCaseResultRepository, never()).findByJudgeJobId(any());
        }
    }

    @Test
    @DisplayName("轮询完成任务：应返回可见用例和正式评判")
    void getJob_succeededFormal_shouldReturnVisibleCasesAndJudgement() {
        try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
            mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createUser(USER_ID));
            when(algorithmJudgeJobRepository.findById(JOB_ID))
                    .thenReturn(Optional.of(createJob(JudgeJobStatus.SUCCEEDED, ANSWER_ID)));
            when(algorithmJudgeCaseResultRepository.findByJudgeJobId(JOB_ID))
                    .thenReturn(List.of(createCaseResult(true), createCaseResult(false)));
            when(assessmentJudgementDomainService.getLatestByAnswerId(ANSWER_ID))
                    .thenReturn(createJudgement());

            AlgorithmJudgeResult.PollResult result = algorithmJudgeAppService.getJob(JOB_ID);

            assertEquals(JudgeJobStatus.SUCCEEDED, result.status());
            assertEquals(AlgorithmTestcaseType.FORMAL, result.testcaseType());
            assertEquals(1, result.caseResults().size());
            assertEquals("1 2", result.caseResults().getFirst().input());
            assertNotNull(result.judgement());
            assertEquals(ObjectiveResultCode.WA, result.judgement().resultCode());
        }
    }

    @Test
    @DisplayName("轮询他人任务：应拒绝访问")
    void getJob_otherUserJob_shouldReject() {
        try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
            mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createUser(USER_ID));
            when(algorithmJudgeJobRepository.findById(JOB_ID))
                    .thenReturn(Optional.of(createJobForUser(OTHER_USER_ID)));

            Forbidden ex = assertThrows(Forbidden.class, () -> algorithmJudgeAppService.getJob(JOB_ID));
            assertEquals("无权查看该判题任务", ex.getMessage());
        }
    }

    private void stubQuestionAndTime(AssessmentQuestion question) {
        when(assessmentQuestionRepository.findById(QUESTION_ID)).thenReturn(Optional.of(question));
        when(assessmentTimeRepository.findById(ASSESSMENT_TIME_ID))
                .thenReturn(
                        Optional.of(
                                AssessmentTime.reconstruct(
                                        ASSESSMENT_TIME_ID,
                                        Direction.COMPUTER_VISION,
                                        null,
                                        null,
                                        null,
                                        null,
                                        false,
                                        null,
                                        null,
                                        null)));
    }

    private void stubSaveJobId() {
        doAnswer(invocation -> {
            AlgorithmJudgeJob job = invocation.getArgument(0);
            // 数据库自增 ID 在单元测试中由 mock 模拟回填。
            job.setId(JOB_ID);
            return null;
        }).when(algorithmJudgeJobRepository).save(any(AlgorithmJudgeJob.class));
    }

    private User createUser(Long userId) {
        User user = User.reconstruct(userId, "password");
        user.setRoleId((long) RoleType.CANDIDATE.getLevel());
        user.setDirection(Direction.COMPUTER_VISION);
        return user;
    }

    private AssessmentQuestion createAlgorithmQuestion() {
        AlgorithmContent content = new AlgorithmContent();
        content.setContent("A+B");
        content.setStarterCode(Map.of("python", "print(input())"));
        AlgorithmContent.TestCase runCase = new AlgorithmContent.TestCase();
        runCase.setInput("1 2");
        runCase.setExpectedOutput("3");
        content.setRunTestCases(List.of(runCase));
        AlgorithmContent.TestCase formalCase = new AlgorithmContent.TestCase();
        formalCase.setInput("2 3");
        formalCase.setExpectedOutput("5");
        content.setTestCases(List.of(formalCase));

        return AssessmentQuestion.reconstruct(
                QUESTION_ID,
                ASSESSMENT_TIME_ID,
                1,
                QuestionType.ALGORITHM,
                "A+B",
                content,
                null,
                BigDecimal.TEN);
    }

    private AssessmentAnswer createAnswer() {
        return AssessmentAnswer
                .reconstruct(
                        ANSWER_ID,
                        USER_ID,
                        QUESTION_ID,
                        "print(input())",
                        ProgrammingLanguage.PYTHON,
                        null,
                        null,
                        null);
    }

    private AlgorithmJudgeJob createJob(JudgeJobStatus status, Long answerId) {
        return AlgorithmJudgeJob.reconstruct(
                JOB_ID,
                answerId,
                QUESTION_ID,
                ASSESSMENT_TIME_ID,
                USER_ID,
                ProgrammingLanguage.PYTHON,
                null,
                answerId == null ? AlgorithmTestcaseType.DEFAULT_RUN : AlgorithmTestcaseType.FORMAL,
                null,
                status,
                0,
                3,
                "执行中",
                null,
                null,
                null,
                null);
    }

    private AlgorithmJudgeJob createJobForUser(Long userId) {
        return AlgorithmJudgeJob.reconstruct(
                JOB_ID,
                null,
                QUESTION_ID,
                ASSESSMENT_TIME_ID,
                userId,
                ProgrammingLanguage.PYTHON,
                null,
                AlgorithmTestcaseType.DEFAULT_RUN,
                null,
                JudgeJobStatus.PENDING,
                0,
                3,
                null,
                null,
                null,
                null,
                null);
    }

    private AlgorithmJudgeCaseResult createCaseResult(boolean visible) {
        return AlgorithmJudgeCaseResult.create(
                JOB_ID,
                1,
                AlgorithmTestcaseType.FORMAL,
                JudgeCaseStatus.WA,
                "1 2",
                "3",
                "4",
                null,
                null,
                null,
                null,
                null,
                visible);
    }

    private AssessmentJudgementVO createJudgement() {
        return AssessmentJudgementVO.builder()
                .id(99L)
                .answerId(ANSWER_ID)
                .questionId(QUESTION_ID)
                .assessmentTimeId(ASSESSMENT_TIME_ID)
                .userId(USER_ID)
                .score(BigDecimal.ZERO)
                .maxScore(BigDecimal.TEN)
                .status(JudgementStatus.JUDGED)
                .resultCode(ObjectiveResultCode.WA)
                .source(JudgementSource.AUTO)
                .build();
    }
}
