package com.bluenet.judge.application.service.workflow;

import com.bluenet.judge.application.dto.FormalTestcaseBundle;
import com.bluenet.judge.application.dto.SandboxExecutionResult;
import com.bluenet.judge.application.service.FormalTestcaseLoader;
import com.bluenet.judge.application.service.SandboxCodeRunner;
import com.bluenet.judge.infrastructure.repository.JudgeJobRepository;
import com.bluenet.judge.infrastructure.repository.JudgeMetadataRepository;
import com.bluenet.judge.infrastructure.repository.JudgeResultRepository;
import com.bluenet.judge.infrastructure.repository.dataobject.AssessmentJudgementWrite;
import com.bluenet.judge.infrastructure.repository.dataobject.JudgeCaseResultWrite;
import com.bluenet.judge.infrastructure.repository.dataobject.JudgeJobRecord;
import com.bluenet.judge.infrastructure.repository.dataobject.JudgeLanguageLimitRecord;
import com.bluenet.judge.infrastructure.repository.dataobject.JudgeTestCaseRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * FormalJudgeWorkflow 单元测试。
 */
@DisplayName("FormalJudgeWorkflow 单元测试")
@ExtendWith(MockitoExtension.class)
class FormalJudgeWorkflowTest {

    private static final Long JOB_ID = 1L;
    private static final Long QUESTION_ID = 10L;
    private static final Long ANSWER_ID = 100L;

    @Mock
    private JudgeJobRepository judgeJobRepository;
    @Mock
    private JudgeMetadataRepository judgeMetadataRepository;
    @Mock
    private FormalTestcaseLoader formalTestcaseLoader;
    @Mock
    private JudgeResultRepository judgeResultRepository;
    @Mock
    private SandboxCodeRunner sandboxCodeRunner;

    private FormalJudgeWorkflow workflow;

    @BeforeEach
    void setUp() {
        workflow = new FormalJudgeWorkflow(
                judgeJobRepository,
                judgeMetadataRepository,
                formalTestcaseLoader,
                judgeResultRepository,
                sandboxCodeRunner,
                new ObjectMapper());
    }

    @Test
    @DisplayName("正式判题全部 AC：应返回 AC 和满分")
    void handleFormalJob_allAccepted_shouldReturnAcAndFullScore() {
        stubFormalJobWithLimit();
        when(formalTestcaseLoader.load(any())).thenReturn(
                List.of(
                        createBundle("1 2", "3"),
                        createBundle("2 3", "5")));
        when(sandboxCodeRunner.run(eq("python"), any(), any(), anyInt(), anyInt(), anyInt()))
                .thenReturn(createResult(0, "3", false, 10, 1024, null))
                .thenReturn(createResult(0, "5", false, 15, 1024, null));

        workflow.handle(JOB_ID);

        verify(judgeJobRepository).markRunning(JOB_ID);
        verify(judgeResultRepository).insertAssessmentJudgement(any());
        verify(judgeJobRepository).markSucceeded(JOB_ID);
        ArgumentCaptor<JudgeCaseResultWrite> captor = ArgumentCaptor.forClass(JudgeCaseResultWrite.class);
        verify(judgeResultRepository, org.mockito.Mockito.times(2)).insertCaseResult(captor.capture());
        List<JudgeCaseResultWrite> results = captor.getAllValues();
        assertThat(results).extracting(JudgeCaseResultWrite::getStatus).containsOnly("AC");
    }

    @Test
    @DisplayName("正式判题 WA：应标记答案错误")
    void handleFormalJob_wrongAnswer_shouldReturnWa() {
        stubFormalJobWithLimit();
        when(formalTestcaseLoader.load(any())).thenReturn(List.of(createBundle("1 2", "3")));
        when(sandboxCodeRunner.run(eq("python"), any(), any(), anyInt(), anyInt(), anyInt()))
                .thenReturn(createResult(0, "4", false, 10, 1024, null));

        workflow.handle(JOB_ID);

        verify(judgeResultRepository).insertCaseResult(any());
        ArgumentCaptor<AssessmentJudgementWrite> judgementCaptor = ArgumentCaptor
                .forClass(AssessmentJudgementWrite.class);
        verify(judgeResultRepository).insertAssessmentJudgement(judgementCaptor.capture());
        assertThat(judgementCaptor.getValue().getResultCode()).isEqualTo("WA");
    }

    @Test
    @DisplayName("正式判题 TLE：应标记超时")
    void handleFormalJob_timeLimitExceeded_shouldReturnTle() {
        stubFormalJobWithLimit();
        when(formalTestcaseLoader.load(any())).thenReturn(List.of(createBundle("1 2", "3")));
        when(sandboxCodeRunner.run(eq("python"), any(), any(), anyInt(), anyInt(), anyInt()))
                .thenReturn(createResult(0, "", true, 5000, 1024, "TO"));

        workflow.handle(JOB_ID);

        ArgumentCaptor<JudgeCaseResultWrite> captor = ArgumentCaptor.forClass(JudgeCaseResultWrite.class);
        verify(judgeResultRepository).insertCaseResult(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("TLE");
    }

    @Test
    @DisplayName("正式判题 MLE：应标记内存超限")
    void handleFormalJob_memoryLimitExceeded_shouldReturnMle() {
        JudgeLanguageLimitRecord limit = new JudgeLanguageLimitRecord();
        limit.setTimeLimitMs(1000);
        limit.setMemoryLimitKb(256 * 1024);
        limit.setOutputLimitKb(1024);
        stubJob("FORMAL");
        when(judgeMetadataRepository.findConfirmedLimit(QUESTION_ID, "python")).thenReturn(Optional.of(limit));
        when(judgeMetadataRepository.findCurrentTestCases(QUESTION_ID)).thenReturn(List.of(createTestCaseRecord()));
        when(formalTestcaseLoader.load(any())).thenReturn(List.of(createBundle("1 2", "3")));
        when(sandboxCodeRunner.run(eq("python"), any(), any(), anyInt(), anyInt(), anyInt()))
                .thenReturn(createResult(0, "3", false, 10, 256 * 1024, "SG"));

        workflow.handle(JOB_ID);

        ArgumentCaptor<JudgeCaseResultWrite> captor = ArgumentCaptor.forClass(JudgeCaseResultWrite.class);
        verify(judgeResultRepository).insertCaseResult(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("MLE");
    }

    @Test
    @DisplayName("正式判题 RE：应标记运行错误")
    void handleFormalJob_runtimeError_shouldReturnRe() {
        stubFormalJobWithLimit();
        when(formalTestcaseLoader.load(any())).thenReturn(List.of(createBundle("1 2", "3")));
        when(sandboxCodeRunner.run(eq("python"), any(), any(), anyInt(), anyInt(), anyInt()))
                .thenReturn(createResult(1, "", false, 5, 1024, null));

        workflow.handle(JOB_ID);

        ArgumentCaptor<JudgeCaseResultWrite> captor = ArgumentCaptor.forClass(JudgeCaseResultWrite.class);
        verify(judgeResultRepository).insertCaseResult(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("RE");
    }

    @Test
    @DisplayName("正式判题 CE：应标记编译失败")
    void handleFormalJob_compileError_shouldReturnCe() {
        stubFormalJobWithLimit();
        when(formalTestcaseLoader.load(any())).thenReturn(List.of(createBundle("1 2", "3")));
        when(sandboxCodeRunner.run(eq("python"), any(), any(), anyInt(), anyInt(), anyInt()))
                .thenThrow(new IllegalStateException("源码编译失败：syntax error"));

        workflow.handle(JOB_ID);

        ArgumentCaptor<JudgeCaseResultWrite> captor = ArgumentCaptor.forClass(JudgeCaseResultWrite.class);
        verify(judgeResultRepository).insertCaseResult(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("CE");
    }

    @Test
    @DisplayName("缺少已确认语言限制：应标记复核")
    void handleFormalJob_missingLanguageLimit_shouldMarkReviewRequired() {
        stubJob("FORMAL");
        when(judgeMetadataRepository.findConfirmedLimit(QUESTION_ID, "python")).thenReturn(Optional.empty());

        workflow.handle(JOB_ID);

        verify(judgeJobRepository).markReviewRequired(eq(JOB_ID), org.mockito.ArgumentMatchers.contains("未确认判题资源限制"));
        verify(judgeResultRepository, never()).insertCaseResult(any());
    }

    @Test
    @DisplayName("缺少测试用例：应标记复核")
    void handleFormalJob_missingTestcases_shouldMarkReviewRequired() {
        stubJob("FORMAL");
        when(judgeMetadataRepository.findConfirmedLimit(QUESTION_ID, "python"))
                .thenReturn(Optional.of(createLimit()));
        when(judgeMetadataRepository.findCurrentTestCases(QUESTION_ID)).thenReturn(List.of());

        workflow.handle(JOB_ID);

        verify(judgeJobRepository).markReviewRequired(eq(JOB_ID), org.mockito.ArgumentMatchers.contains("未生成可用正式测试数据"));
    }

    @Test
    @DisplayName("OSS 加载失败：应标记复核")
    void handleFormalJob_ossLoadFailure_shouldMarkReviewRequired() {
        stubJob("FORMAL");
        when(judgeMetadataRepository.findConfirmedLimit(QUESTION_ID, "python"))
                .thenReturn(Optional.of(createLimit()));
        when(judgeMetadataRepository.findCurrentTestCases(QUESTION_ID)).thenReturn(List.of(createTestCaseRecord()));
        when(formalTestcaseLoader.load(any())).thenThrow(new RuntimeException("Object not found"));

        workflow.handle(JOB_ID);

        verify(judgeJobRepository).markReviewRequired(eq(JOB_ID), org.mockito.ArgumentMatchers.contains("OSS 文件缺失"));
    }

    private void stubJob(String testcaseType) {
        JudgeJobRecord job = new JudgeJobRecord();
        job.setId(JOB_ID);
        job.setAnswerId(ANSWER_ID);
        job.setQuestionId(QUESTION_ID);
        job.setAssessmentTimeId(20L);
        job.setUserId(1L);
        job.setLanguage("python");
        job.setSourceCode("print(input())");
        job.setTestcaseType(testcaseType);
        job.setQuestionMaxScore(BigDecimal.TEN);
        when(judgeJobRepository.findById(JOB_ID)).thenReturn(Optional.of(job));
    }

    private void stubFormalJobWithLimit() {
        stubJob("FORMAL");
        when(judgeMetadataRepository.findConfirmedLimit(QUESTION_ID, "python"))
                .thenReturn(Optional.of(createLimit()));
        when(judgeMetadataRepository.findCurrentTestCases(QUESTION_ID)).thenReturn(List.of(createTestCaseRecord()));
    }

    private JudgeLanguageLimitRecord createLimit() {
        JudgeLanguageLimitRecord limit = new JudgeLanguageLimitRecord();
        limit.setTimeLimitMs(1000);
        limit.setMemoryLimitKb(256 * 1024);
        limit.setOutputLimitKb(1024);
        return limit;
    }

    private JudgeTestCaseRecord createTestCaseRecord() {
        JudgeTestCaseRecord record = new JudgeTestCaseRecord();
        record.setId(1L);
        record.setCaseNo(1);
        record.setWeight(BigDecimal.ONE);
        record.setHidden(true);
        return record;
    }

    private FormalTestcaseBundle createBundle(String input, String expectedOutput) {
        return new FormalTestcaseBundle(
                createTestCaseRecord(),
                input.getBytes(StandardCharsets.UTF_8),
                expectedOutput.getBytes(StandardCharsets.UTF_8));
    }

    private SandboxExecutionResult createResult(int exitCode, String stdout, boolean timedOut,
            int timeUsedMs, int memoryUsedKb, String isolateStatus) {
        return new SandboxExecutionResult(
                exitCode,
                stdout.getBytes(StandardCharsets.UTF_8),
                new byte[0],
                timedOut,
                timeUsedMs,
                memoryUsedKb,
                isolateStatus);
    }
}
