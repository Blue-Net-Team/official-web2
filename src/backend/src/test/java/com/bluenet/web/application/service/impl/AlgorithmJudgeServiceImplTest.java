package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.algorithm_judge.AlgorithmRunRequestDTO;
import com.bluenet.web.api.dto.algorithm_judge.AlgorithmSubmitResponseDTO;
import com.bluenet.web.api.dto.algorithm_judge.JudgeJobPollingResponseDTO;
import com.bluenet.web.api.dto.assessment_answer.CreateAnswerRequestDTO;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.Forbidden;
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
import com.bluenet.web.domain.model.vo.AlgorithmJudgeCaseResultVO;
import com.bluenet.web.domain.model.vo.AlgorithmJudgeJobVO;
import com.bluenet.web.domain.model.vo.AssessmentAnswerVO;
import com.bluenet.web.domain.model.vo.AssessmentJudgementVO;
import com.bluenet.web.domain.model.vo.AssessmentQuestionVO;
import com.bluenet.web.domain.model.vo.AssessmentTimeVO;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.model.vo.evaluation.AlgorithmContent;
import com.bluenet.web.domain.repository.AlgorithmJudgeCaseResultRepository;
import com.bluenet.web.domain.repository.AlgorithmJudgeJobRepository;
import com.bluenet.web.domain.repository.AssessmentAnswerRepository;
import com.bluenet.web.domain.repository.AssessmentSessionRepository;
import com.bluenet.web.domain.service.AssessmentAnswerDomainService;
import com.bluenet.web.domain.service.AssessmentJudgementDomainService;
import com.bluenet.web.domain.service.AssessmentQuestionDomainService;
import com.bluenet.web.domain.service.AssessmentTimeDomainService;
import com.bluenet.web.infrastructure.judge.AlgorithmJudgeJobPublisher;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
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

@DisplayName("AlgorithmJudgeServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class AlgorithmJudgeServiceImplTest {
    private static final Long USER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;
    private static final Long QUESTION_ID = 10L;
    private static final Long ASSESSMENT_TIME_ID = 20L;
    private static final Long ANSWER_ID = 30L;
    private static final Long JOB_ID = 40L;

    @Mock
    private AssessmentQuestionDomainService assessmentQuestionDomainService;
    @Mock
    private AssessmentTimeDomainService assessmentTimeDomainService;
    @Mock
    private AssessmentAnswerDomainService assessmentAnswerDomainService;
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

    @InjectMocks
    private AlgorithmJudgeServiceImpl algorithmJudgeService;

    @Test
    @DisplayName("默认运行：应创建 DEFAULT_RUN 任务并投递队列")
    void run_defaultTestcase_shouldCreateJobAndPublish() {
        try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
            mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createUser(USER_ID));
            stubQuestionAndTime(createAlgorithmQuestion());
            stubSaveJobId();

            AlgorithmRunRequestDTO request = AlgorithmRunRequestDTO.builder()
                    .questionId(QUESTION_ID)
                    .language(ProgrammingLanguage.PYTHON)
                    .sourceCode("print(input())")
                    .testcaseType(AlgorithmTestcaseType.DEFAULT_RUN)
                    .build();

            AlgorithmSubmitResponseDTO response = algorithmJudgeService.run(request);

            assertEquals(JOB_ID, response.getJudgeJobId());
            assertEquals(AlgorithmTestcaseType.DEFAULT_RUN, response.getTestcaseType());
            assertNull(response.getAnswerId());
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

            AlgorithmRunRequestDTO request = AlgorithmRunRequestDTO.builder()
                    .questionId(QUESTION_ID)
                    .language(ProgrammingLanguage.PYTHON)
                    .sourceCode("print(input())")
                    .testcaseType(AlgorithmTestcaseType.CUSTOM_RUN)
                    .customInput("hello")
                    .build();

            algorithmJudgeService.run(request);

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

            AlgorithmRunRequestDTO request = AlgorithmRunRequestDTO.builder()
                    .questionId(QUESTION_ID)
                    .language(ProgrammingLanguage.JAVA)
                    .sourceCode("class Main {}")
                    .testcaseType(AlgorithmTestcaseType.DEFAULT_RUN)
                    .build();

            BadRequest ex = assertThrows(BadRequest.class, () -> algorithmJudgeService.run(request));
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
            when(assessmentAnswerDomainService.createAnswer(any(AssessmentAnswerVO.class)))
                    .thenReturn(createAnswer());
            stubSaveJobId();

            CreateAnswerRequestDTO request = CreateAnswerRequestDTO.builder()
                    .questionId(QUESTION_ID)
                    .language(ProgrammingLanguage.PYTHON)
                    .content("print(input())")
                    .build();

            AlgorithmSubmitResponseDTO response = algorithmJudgeService.submit(request);

            assertEquals(ANSWER_ID, response.getAnswerId());
            assertEquals(JOB_ID, response.getJudgeJobId());
            assertEquals(AlgorithmTestcaseType.FORMAL, response.getTestcaseType());
            verify(assessmentAnswerDomainService).createAnswer(any(AssessmentAnswerVO.class));
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

            JudgeJobPollingResponseDTO response = algorithmJudgeService.getJob(JOB_ID);

            assertEquals(JOB_ID, response.getJudgeJobId());
            assertEquals(AlgorithmTestcaseType.DEFAULT_RUN, response.getTestcaseType());
            assertEquals(JudgeJobStatus.RUNNING, response.getStatus());
            assertEquals(0, response.getCaseResults().size());
            assertNull(response.getJudgement());
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

            JudgeJobPollingResponseDTO response = algorithmJudgeService.getJob(JOB_ID);

            assertEquals(JudgeJobStatus.SUCCEEDED, response.getStatus());
            assertEquals(AlgorithmTestcaseType.FORMAL, response.getTestcaseType());
            assertEquals(1, response.getCaseResults().size());
            assertEquals("1 2", response.getCaseResults().getFirst().getInput());
            assertNotNull(response.getJudgement());
            assertEquals(ObjectiveResultCode.WA, response.getJudgement().getResultCode());
        }
    }

    @Test
    @DisplayName("轮询他人任务：应拒绝访问")
    void getJob_otherUserJob_shouldReject() {
        try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
            mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createUser(USER_ID));
            when(algorithmJudgeJobRepository.findById(JOB_ID))
                    .thenReturn(Optional.of(createJobForUser(OTHER_USER_ID)));

            Forbidden ex = assertThrows(Forbidden.class, () -> algorithmJudgeService.getJob(JOB_ID));
            assertEquals("无权查看该判题任务", ex.getMessage());
        }
    }

    private void stubQuestionAndTime(AssessmentQuestionVO question) {
        when(assessmentQuestionDomainService.getQuestionById(QUESTION_ID)).thenReturn(question);
        when(assessmentTimeDomainService.getById(ASSESSMENT_TIME_ID))
                .thenReturn(
                        Optional.of(
                                AssessmentTimeVO.builder()
                                        .id(ASSESSMENT_TIME_ID)
                                        .direction(Direction.COMPUTER_VISION)
                                        .timeLimit(false)
                                        .build()));
    }

    private void stubSaveJobId() {
        doAnswer(invocation -> {
            AlgorithmJudgeJob job = invocation.getArgument(0);
            // 数据库自增 ID 在单元测试中由 mock 模拟回填。
            job.setId(JOB_ID);
            return null;
        }).when(algorithmJudgeJobRepository).save(any(AlgorithmJudgeJob.class));
    }

    private UserVO createUser(Long userId) {
        return UserVO.builder()
                .id(userId)
                .roleName("CANDIDATE")
                .direction(Direction.COMPUTER_VISION)
                .build();
    }

    private AssessmentQuestionVO createAlgorithmQuestion() {
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

        return AssessmentQuestionVO.builder()
                .id(QUESTION_ID)
                .assessmentTimeId(ASSESSMENT_TIME_ID)
                .questionType(QuestionType.ALGORITHM)
                .content(content)
                .score(BigDecimal.TEN)
                .build();
    }

    private AssessmentAnswerVO createAnswer() {
        return AssessmentAnswerVO.builder()
                .id(ANSWER_ID)
                .userId(USER_ID)
                .questionId(QUESTION_ID)
                .content("print(input())")
                .language(ProgrammingLanguage.PYTHON)
                .build();
    }

    private AlgorithmJudgeJobVO createJob(JudgeJobStatus status, Long answerId) {
        return AlgorithmJudgeJobVO.builder()
                .id(JOB_ID)
                .answerId(answerId)
                .questionId(QUESTION_ID)
                .assessmentTimeId(ASSESSMENT_TIME_ID)
                .userId(USER_ID)
                .language(ProgrammingLanguage.PYTHON)
                .testcaseType(answerId == null ? AlgorithmTestcaseType.DEFAULT_RUN : AlgorithmTestcaseType.FORMAL)
                .status(status)
                .statusMessage("执行中")
                .build();
    }

    private AlgorithmJudgeJobVO createJobForUser(Long userId) {
        return AlgorithmJudgeJobVO.builder()
                .id(JOB_ID)
                .questionId(QUESTION_ID)
                .assessmentTimeId(ASSESSMENT_TIME_ID)
                .userId(userId)
                .language(ProgrammingLanguage.PYTHON)
                .status(JudgeJobStatus.PENDING)
                .build();
    }

    private AlgorithmJudgeCaseResultVO createCaseResult(boolean visible) {
        return AlgorithmJudgeCaseResultVO.builder()
                .judgeJobId(JOB_ID)
                .caseNo(1)
                .testcaseType(AlgorithmTestcaseType.FORMAL)
                .status(JudgeCaseStatus.WA)
                .input("1 2")
                .expectedOutput("3")
                .actualOutput("4")
                .visibleToCandidate(visible)
                .build();
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
