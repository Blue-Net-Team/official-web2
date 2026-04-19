package com.bluenet.web.infrastructure.judge;

import com.bluenet.web.domain.model.enumerate.AlgorithmTestcaseType;
import com.bluenet.web.domain.model.enumerate.JudgeCaseStatus;
import com.bluenet.web.domain.model.enumerate.JudgeJobStatus;
import com.bluenet.web.domain.model.enumerate.ObjectiveResultCode;
import com.bluenet.web.domain.model.enumerate.ProgrammingLanguage;
import com.bluenet.web.domain.model.enumerate.QuestionType;
import com.bluenet.web.domain.model.vo.AlgorithmJudgeCaseResultVO;
import com.bluenet.web.domain.model.vo.AlgorithmJudgeJobVO;
import com.bluenet.web.domain.model.vo.AssessmentJudgementVO;
import com.bluenet.web.domain.model.vo.AssessmentQuestionVO;
import com.bluenet.web.domain.model.vo.evaluation.AlgorithmContent;
import com.bluenet.web.domain.repository.AlgorithmJudgeCaseResultRepository;
import com.bluenet.web.domain.repository.AlgorithmJudgeJobRepository;
import com.bluenet.web.domain.service.AssessmentJudgementDomainService;
import com.bluenet.web.domain.service.AssessmentQuestionDomainService;
import com.bluenet.web.infrastructure.judge.sandbox.SandboxCaseResult;
import com.bluenet.web.infrastructure.judge.sandbox.SandboxExecutionRequest;
import com.bluenet.web.infrastructure.judge.sandbox.SandboxExecutionResult;
import com.bluenet.web.infrastructure.judge.sandbox.SandboxExecutor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("AlgorithmJudgeWorker 单元测试")
@ExtendWith(MockitoExtension.class)
class AlgorithmJudgeWorkerTest {
    private static final Long JOB_ID = 40L;
    private static final Long ANSWER_ID = 30L;
    private static final Long QUESTION_ID = 10L;
    private static final Long ASSESSMENT_TIME_ID = 20L;
    private static final Long USER_ID = 1L;

    @Mock
    private AlgorithmJudgeJobRepository algorithmJudgeJobRepository;
    @Mock
    private AlgorithmJudgeCaseResultRepository algorithmJudgeCaseResultRepository;
    @Mock
    private AssessmentQuestionDomainService assessmentQuestionDomainService;
    @Mock
    private AssessmentJudgementDomainService assessmentJudgementDomainService;
    @Mock
    private SandboxExecutor sandboxExecutor;

    @InjectMocks
    private AlgorithmJudgeWorker worker;

    @Test
    @DisplayName("正式判题 AC：应写入满分 AC judgement")
    void consume_formalAccepted_shouldCreateAcceptedJudgement() {
        runFormalStatusCase(JudgeCaseStatus.AC, ObjectiveResultCode.AC, BigDecimal.TEN);
    }

    @Test
    @DisplayName("正式判题 WA：应写入零分 WA judgement")
    void consume_formalWrongAnswer_shouldCreateWrongAnswerJudgement() {
        runFormalStatusCase(JudgeCaseStatus.WA, ObjectiveResultCode.WA, BigDecimal.ZERO);
    }

    @Test
    @DisplayName("正式判题 CE：应写入零分 CE judgement")
    void consume_formalCompileError_shouldCreateCompileErrorJudgement() {
        runFormalStatusCase(JudgeCaseStatus.CE, ObjectiveResultCode.CE, BigDecimal.ZERO);
    }

    @Test
    @DisplayName("正式判题 RE：应写入零分 RE judgement")
    void consume_formalRuntimeError_shouldCreateRuntimeErrorJudgement() {
        runFormalStatusCase(JudgeCaseStatus.RE, ObjectiveResultCode.RE, BigDecimal.ZERO);
    }

    @Test
    @DisplayName("正式判题 TLE：应写入零分 TLE judgement")
    void consume_formalTimeout_shouldCreateTimeoutJudgement() {
        runFormalStatusCase(JudgeCaseStatus.TLE, ObjectiveResultCode.TLE, BigDecimal.ZERO);
    }

    @Test
    @DisplayName("正式提交 AC 用例：应保存但不向考生展示")
    void consume_formalAccepted_shouldHideAcceptedCasesFromCandidate() {
        when(algorithmJudgeJobRepository.findById(JOB_ID)).thenReturn(Optional.of(createFormalJob()));
        when(assessmentQuestionDomainService.getQuestionById(QUESTION_ID)).thenReturn(createQuestion());
        when(sandboxExecutor.execute(any(SandboxExecutionRequest.class)))
                .thenReturn(
                        SandboxExecutionResult.builder()
                                .infrastructureFailure(false)
                                .caseResults(List.of(createCaseResult(JudgeCaseStatus.AC)))
                                .build());

        worker.consume(JOB_ID.toString());

        ArgumentCaptor<List<AlgorithmJudgeCaseResultVO>> casesCaptor = ArgumentCaptor.forClass(List.class);
        verify(algorithmJudgeCaseResultRepository).saveAll(casesCaptor.capture());
        assertFalse(casesCaptor.getValue().getFirst().getVisibleToCandidate());
    }

    @Test
    @DisplayName("沙盒基础设施异常：应保持任务可重试且不写 judgement")
    void consume_infrastructureFailure_shouldRetryWithoutJudgement() {
        AlgorithmJudgeJobVO job = createFormalJob();
        when(algorithmJudgeJobRepository.findById(JOB_ID)).thenReturn(Optional.of(job));
        when(assessmentQuestionDomainService.getQuestionById(QUESTION_ID)).thenReturn(createQuestion());
        when(sandboxExecutor.execute(any(SandboxExecutionRequest.class)))
                .thenReturn(
                        SandboxExecutionResult.builder()
                                .infrastructureFailure(true)
                                .infrastructureMessage("docker unavailable")
                                .caseResults(List.of())
                                .build());

        worker.consume(JOB_ID.toString());

        ArgumentCaptor<AlgorithmJudgeJobVO> jobCaptor = ArgumentCaptor.forClass(AlgorithmJudgeJobVO.class);
        verify(algorithmJudgeJobRepository, org.mockito.Mockito.times(2)).update(jobCaptor.capture());
        assertEquals(JudgeJobStatus.RETRYING, jobCaptor.getAllValues().getLast().getStatus());
        verify(algorithmJudgeCaseResultRepository, never()).saveAll(any());
        verify(assessmentJudgementDomainService, never()).createJudgement(any());
    }

    private void runFormalStatusCase(
            JudgeCaseStatus caseStatus,
            ObjectiveResultCode expectedCode,
            BigDecimal expectedScore) {
        when(algorithmJudgeJobRepository.findById(JOB_ID)).thenReturn(Optional.of(createFormalJob()));
        when(assessmentQuestionDomainService.getQuestionById(QUESTION_ID)).thenReturn(createQuestion());
        when(sandboxExecutor.execute(any(SandboxExecutionRequest.class)))
                .thenReturn(
                        SandboxExecutionResult.builder()
                                .infrastructureFailure(false)
                                .caseResults(List.of(createCaseResult(caseStatus)))
                                .build());

        worker.consume(JOB_ID.toString());

        ArgumentCaptor<AssessmentJudgementVO> judgementCaptor = ArgumentCaptor.forClass(AssessmentJudgementVO.class);
        verify(assessmentJudgementDomainService).createJudgement(judgementCaptor.capture());
        assertEquals(expectedCode, judgementCaptor.getValue().getResultCode());
        assertEquals(0, expectedScore.compareTo(judgementCaptor.getValue().getScore()));
        verify(algorithmJudgeCaseResultRepository).saveAll(any());
    }

    private AlgorithmJudgeJobVO createFormalJob() {
        return AlgorithmJudgeJobVO.builder()
                .id(JOB_ID)
                .answerId(ANSWER_ID)
                .questionId(QUESTION_ID)
                .assessmentTimeId(ASSESSMENT_TIME_ID)
                .userId(USER_ID)
                .language(ProgrammingLanguage.PYTHON)
                .sourceCode("print(input())")
                .testcaseType(AlgorithmTestcaseType.FORMAL)
                .status(JudgeJobStatus.PENDING)
                .retryCount(0)
                .maxRetryCount(3)
                .build();
    }

    private AssessmentQuestionVO createQuestion() {
        AlgorithmContent content = new AlgorithmContent();
        AlgorithmContent.TestCase testcase = new AlgorithmContent.TestCase();
        testcase.setInput("1 2");
        testcase.setExpectedOutput("3");
        content.setTestCases(List.of(testcase));
        content.setTimeLimit(1000);
        content.setMemoryLimit(262144);
        return AssessmentQuestionVO.builder()
                .id(QUESTION_ID)
                .assessmentTimeId(ASSESSMENT_TIME_ID)
                .questionType(QuestionType.ALGORITHM)
                .content(content)
                .score(BigDecimal.TEN)
                .build();
    }

    private SandboxCaseResult createCaseResult(JudgeCaseStatus status) {
        return SandboxCaseResult.builder()
                .caseNo(1)
                .status(status)
                .input("1 2")
                .expectedOutput("3")
                .actualOutput(status == JudgeCaseStatus.AC ? "3" : "4")
                .build();
    }
}
