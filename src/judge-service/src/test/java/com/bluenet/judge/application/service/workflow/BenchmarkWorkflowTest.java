package com.bluenet.judge.application.service.workflow;

import com.bluenet.judge.application.dto.FormalTestcaseBundle;
import com.bluenet.judge.application.dto.SandboxExecutionResult;
import com.bluenet.judge.application.service.BenchmarkLimitSuggester;
import com.bluenet.judge.application.service.FormalTestcaseLoader;
import com.bluenet.judge.application.service.SandboxCodeRunner;
import com.bluenet.judge.infrastructure.repository.JudgeMetadataRepository;
import com.bluenet.judge.infrastructure.repository.dataobject.JudgeProblemConfigRecord;
import com.bluenet.judge.infrastructure.repository.dataobject.JudgeStandardSolutionRecord;
import com.bluenet.judge.infrastructure.repository.dataobject.JudgeTestCaseRecord;
import com.bluenet.judge.infrastructure.storage.JudgeAssetStorage;
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
 * BenchmarkWorkflow 单元测试。
 */
@DisplayName("BenchmarkWorkflow 单元测试")
@ExtendWith(MockitoExtension.class)
class BenchmarkWorkflowTest {

    private static final Long CONFIG_ID = 1L;
    private static final Long QUESTION_ID = 10L;
    private static final Long SOLUTION_ID = 100L;

    @Mock
    private JudgeMetadataRepository judgeMetadataRepository;
    @Mock
    private FormalTestcaseLoader formalTestcaseLoader;
    @Mock
    private SandboxCodeRunner sandboxCodeRunner;
    @Mock
    private JudgeAssetStorage judgeAssetStorage;

    private BenchmarkWorkflow workflow;

    @BeforeEach
    void setUp() {
        workflow = new BenchmarkWorkflow(
                judgeMetadataRepository,
                formalTestcaseLoader,
                sandboxCodeRunner,
                judgeAssetStorage,
                new BenchmarkLimitSuggester());
    }

    @Test
    @DisplayName("Benchmark 成功：应计算 p95、max time、peak memory 和 suggested limit")
    void handle_success_shouldCalculateAndPersist() {
        stubConfigAndTestcases();
        when(judgeAssetStorage.get(any()))
                .thenReturn("print(sum(map(int,input().split())))".getBytes(StandardCharsets.UTF_8));
        // 5 repeats x 2 testcases = 10 runs, cpu times: 10ms ~ 55ms
        when(sandboxCodeRunner.run(eq("python"), any(), any(), anyInt(), anyInt(), anyInt()))
                .thenReturn(createResult(0, false, 10, 2048))
                .thenReturn(createResult(0, false, 20, 2048))
                .thenReturn(createResult(0, false, 15, 2048))
                .thenReturn(createResult(0, false, 30, 3072))
                .thenReturn(createResult(0, false, 25, 2048))
                .thenReturn(createResult(0, false, 40, 2048))
                .thenReturn(createResult(0, false, 35, 2048))
                .thenReturn(createResult(0, false, 50, 4096))
                .thenReturn(createResult(0, false, 45, 2048))
                .thenReturn(createResult(0, false, 55, 2048));

        workflow.handle(CONFIG_ID);

        verify(judgeMetadataRepository).markConfigStatus(CONFIG_ID, "BENCHMARKING");
        ArgumentCaptor<Integer> p95Captor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> maxCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> memCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> suggestCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(judgeMetadataRepository).updateBenchmarkResult(
                eq(SOLUTION_ID),
                eq("DONE"),
                p95Captor.capture(),
                maxCaptor.capture(),
                memCaptor.capture(),
                suggestCaptor.capture(),
                any());

        // p95 of 10 sorted values: [10,15,20,25,30,35,40,45,50,55] -> index 9
        // (ceil(10*0.95)-1 = 9) = 55
        assertThat(p95Captor.getValue()).isEqualTo(55);
        assertThat(maxCaptor.getValue()).isEqualTo(55);
        assertThat(memCaptor.getValue()).isEqualTo(4096);
        assertThat(suggestCaptor.getValue()).isGreaterThanOrEqualTo(55);
        verify(judgeMetadataRepository).markConfigStatus(CONFIG_ID, "READY");
    }

    @Test
    @DisplayName("Benchmark 超时：应标记 FAILED")
    void handle_timedOut_shouldMarkFailed() {
        stubConfigAndTestcases();
        when(judgeAssetStorage.get(any())).thenReturn("print(1)".getBytes(StandardCharsets.UTF_8));
        when(sandboxCodeRunner.run(eq("python"), any(), any(), anyInt(), anyInt(), anyInt()))
                .thenReturn(createResult(0, true, 30000, 1024));

        workflow.handle(CONFIG_ID);

        verify(judgeMetadataRepository).updateBenchmarkResult(
                eq(SOLUTION_ID),
                eq("FAILED"),
                any(),
                any(),
                any(),
                any(),
                org.mockito.ArgumentMatchers.contains("超时"));
        verify(judgeMetadataRepository).markConfigStatus(CONFIG_ID, "FAILED");
    }

    @Test
    @DisplayName("Benchmark 运行错误：应标记 FAILED")
    void handle_runtimeError_shouldMarkFailed() {
        stubConfigAndTestcases();
        when(judgeAssetStorage.get(any())).thenReturn("print(1)".getBytes(StandardCharsets.UTF_8));
        when(sandboxCodeRunner.run(eq("python"), any(), any(), anyInt(), anyInt(), anyInt()))
                .thenReturn(createResult(1, false, 5, 1024));

        workflow.handle(CONFIG_ID);

        verify(judgeMetadataRepository).updateBenchmarkResult(
                eq(SOLUTION_ID),
                eq("FAILED"),
                any(),
                any(),
                any(),
                any(),
                org.mockito.ArgumentMatchers.contains("运行错误"));
        verify(judgeMetadataRepository).markConfigStatus(CONFIG_ID, "FAILED");
    }

    @Test
    @DisplayName("无标准解：应直接标记 READY")
    void handle_noSolutions_shouldMarkReady() {
        JudgeProblemConfigRecord config = new JudgeProblemConfigRecord();
        config.setId(CONFIG_ID);
        when(judgeMetadataRepository.findConfig(CONFIG_ID)).thenReturn(Optional.of(config));
        when(judgeMetadataRepository.findStandardSolutions(CONFIG_ID)).thenReturn(List.of());

        workflow.handle(CONFIG_ID);

        verify(judgeMetadataRepository).markConfigStatus(CONFIG_ID, "READY");
        verify(sandboxCodeRunner, never()).run(any(), any(), any(), anyInt(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("无测试用例：应标记 FAILED")
    void handle_noTestcases_shouldMarkFailed() {
        JudgeProblemConfigRecord config = new JudgeProblemConfigRecord();
        config.setId(CONFIG_ID);
        when(judgeMetadataRepository.findConfig(CONFIG_ID)).thenReturn(Optional.of(config));
        when(judgeMetadataRepository.findStandardSolutions(CONFIG_ID))
                .thenReturn(List.of(createSolution()));
        when(judgeMetadataRepository.findTestCasesByConfigId(CONFIG_ID)).thenReturn(List.of());

        workflow.handle(CONFIG_ID);

        verify(judgeMetadataRepository).markConfigStatus(CONFIG_ID, "FAILED");
        verify(sandboxCodeRunner, never()).run(any(), any(), any(), anyInt(), anyInt(), anyInt());
    }

    private void stubConfigAndTestcases() {
        JudgeProblemConfigRecord config = new JudgeProblemConfigRecord();
        config.setId(CONFIG_ID);
        config.setQuestionId(QUESTION_ID);
        config.setBenchmarkRepeatTimes(5);
        config.setMarginMultiplier(new BigDecimal("1.5"));
        config.setMinExtraMs(50);
        config.setRoundToMs(50);

        when(judgeMetadataRepository.findConfig(CONFIG_ID)).thenReturn(Optional.of(config));
        when(judgeMetadataRepository.findStandardSolutions(CONFIG_ID)).thenReturn(List.of(createSolution()));
        when(judgeMetadataRepository.findTestCasesByConfigId(CONFIG_ID))
                .thenReturn(List.of(createTestCase(1), createTestCase(2)));
        when(formalTestcaseLoader.load(any())).thenReturn(
                List.of(
                        new FormalTestcaseBundle(createTestCase(1), "1 2".getBytes(StandardCharsets.UTF_8),
                                "3".getBytes(StandardCharsets.UTF_8)),
                        new FormalTestcaseBundle(createTestCase(2), "2 3".getBytes(StandardCharsets.UTF_8),
                                "5".getBytes(StandardCharsets.UTF_8))));
    }

    private JudgeStandardSolutionRecord createSolution() {
        JudgeStandardSolutionRecord solution = new JudgeStandardSolutionRecord();
        solution.setId(SOLUTION_ID);
        solution.setLanguage("python");
        solution.setObjectKey("questions/" + QUESTION_ID + "/current/standard/python-abc.py");
        return solution;
    }

    private JudgeTestCaseRecord createTestCase(int caseNo) {
        JudgeTestCaseRecord record = new JudgeTestCaseRecord();
        record.setCaseNo(caseNo);
        record.setWeight(BigDecimal.ONE);
        return record;
    }

    private SandboxExecutionResult createResult(int exitCode, boolean timedOut, int timeUsedMs, int memoryUsedKb) {
        return new SandboxExecutionResult(
                exitCode,
                new byte[0],
                new byte[0],
                timedOut,
                timeUsedMs,
                memoryUsedKb,
                null);
    }
}
