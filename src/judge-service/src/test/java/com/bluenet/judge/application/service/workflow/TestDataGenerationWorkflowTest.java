package com.bluenet.judge.application.service.workflow;

import com.bluenet.judge.application.dto.JudgeManifestBundle;
import com.bluenet.judge.application.dto.SandboxExecutionResult;
import com.bluenet.judge.application.service.JudgeManifestLoader;
import com.bluenet.judge.application.service.SandboxCodeRunner;
import com.bluenet.judge.infrastructure.repository.JudgeMetadataRepository;
import com.bluenet.judge.infrastructure.repository.dataobject.JudgeProblemConfigRecord;
import com.bluenet.judge.infrastructure.repository.dataobject.JudgeStandardSolutionRecord;
import com.bluenet.judge.infrastructure.repository.dataobject.JudgeTestCaseRecord;
import com.bluenet.judge.infrastructure.storage.JudgeAssetStorage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TestDataGenerationWorkflow 单元测试。
 */
@DisplayName("TestDataGenerationWorkflow 单元测试")
@ExtendWith(MockitoExtension.class)
class TestDataGenerationWorkflowTest {

    private static final Long CONFIG_ID = 1L;
    private static final Long QUESTION_ID = 10L;

    @Mock
    private JudgeManifestLoader judgeManifestLoader;
    @Mock
    private JudgeMetadataRepository judgeMetadataRepository;
    @Mock
    private JudgeAssetStorage judgeAssetStorage;
    @Mock
    private SandboxCodeRunner sandboxCodeRunner;
    @Mock
    private BenchmarkWorkflow benchmarkWorkflow;
    @Mock
    private PlatformTransactionManager transactionManager;

    private TestDataGenerationWorkflow workflow;

    @BeforeEach
    void setUp() {
        workflow = new TestDataGenerationWorkflow(
                judgeManifestLoader,
                judgeMetadataRepository,
                judgeAssetStorage,
                sandboxCodeRunner,
                benchmarkWorkflow,
                transactionManager);
    }

    @Test
    @DisplayName("生成测试数据成功：应上传 in/out 文件并持久化记录")
    void handle_success_shouldUploadAndPersist() throws Exception {
        stubManifestAndLoader();
        when(judgeMetadataRepository.findTestCasesByConfigId(CONFIG_ID)).thenReturn(List.of());
        when(sandboxCodeRunner.run(eq("python"), any(), any()))
                .thenReturn(createResult(0, "1 2 3", false))
                .thenReturn(createResult(0, "6", false));

        when(transactionManager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));

        workflow.handle(CONFIG_ID);

        verify(judgeAssetStorage, org.mockito.Mockito.times(2)).put(any(), any(), eq("text/plain; charset=utf-8"));
        verify(judgeAssetStorage, never()).delete(any());
        verify(judgeMetadataRepository).markConfigStatus(CONFIG_ID, "GENERATED");
        verify(benchmarkWorkflow).handle(CONFIG_ID);
    }

    @Test
    @DisplayName("存在旧测试用例：生成成功后应删除旧 OSS 对象")
    void handle_withOldTestCases_shouldDeleteOldAssets() throws Exception {
        stubManifestAndLoader();

        JudgeTestCaseRecord oldCase = new JudgeTestCaseRecord();
        oldCase.setCaseNo(1);
        oldCase.setInputObjectKey("questions/10/current/testcases/0001-old.in");
        oldCase.setOutputObjectKey("questions/10/current/testcases/0001-old.out");
        when(judgeMetadataRepository.findTestCasesByConfigId(CONFIG_ID)).thenReturn(List.of(oldCase));

        when(sandboxCodeRunner.run(eq("python"), any(), any()))
                .thenReturn(createResult(0, "1 2 3", false))
                .thenReturn(createResult(0, "6", false));
        when(transactionManager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));

        workflow.handle(CONFIG_ID);

        verify(judgeAssetStorage).delete("questions/10/current/testcases/0001-old.in");
        verify(judgeAssetStorage).delete("questions/10/current/testcases/0001-old.out");
    }

    @Test
    @DisplayName("Generator 执行失败：应标记 FAILED 并抛出异常")
    void handle_generatorFails_shouldMarkFailedAndThrow() throws Exception {
        stubManifestAndLoader();
        when(sandboxCodeRunner.run(eq("python"), any(), any()))
                .thenReturn(createResult(1, "", false));

        assertThatThrownBy(() -> workflow.handle(CONFIG_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("generator 执行失败");

        verify(judgeMetadataRepository).markConfigStatus(CONFIG_ID, "FAILED");
        verify(judgeAssetStorage, never()).put(any(), any(), any());
    }

    @Test
    @DisplayName("Generator 超时：应标记 FAILED 并抛出异常")
    void handle_generatorTimedOut_shouldMarkFailedAndThrow() throws Exception {
        stubManifestAndLoader();
        when(sandboxCodeRunner.run(eq("python"), any(), any()))
                .thenReturn(createResult(0, "", true));

        assertThatThrownBy(() -> workflow.handle(CONFIG_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("generator 执行超时");

        verify(judgeMetadataRepository).markConfigStatus(CONFIG_ID, "FAILED");
    }

    @Test
    @DisplayName("主标准解执行失败：应标记 FAILED 并抛出异常")
    void handle_primarySolutionFails_shouldMarkFailedAndThrow() throws Exception {
        stubManifestAndLoader();
        when(sandboxCodeRunner.run(eq("python"), any(), any()))
                .thenReturn(createResult(0, "1 2 3", false))
                .thenReturn(createResult(1, "error", false));

        assertThatThrownBy(() -> workflow.handle(CONFIG_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("主标准解");

        verify(judgeMetadataRepository).markConfigStatus(CONFIG_ID, "FAILED");
    }

    private void stubManifestAndLoader() throws Exception {
        when(judgeMetadataRepository.findTestCasesByConfigId(CONFIG_ID)).thenReturn(List.of());

        ObjectMapper objectMapper = new ObjectMapper();
        String manifestJson = """
                {
                  "testcases": [
                    {"caseNo": 1, "category": "NORMAL", "generatorArgs": {"n": 3}, "weight": "1", "hidden": true, "sample": false}
                  ]
                }
                """;
        JsonNode manifest = objectMapper.readTree(manifestJson);

        JudgeProblemConfigRecord config = new JudgeProblemConfigRecord();
        config.setId(CONFIG_ID);
        config.setQuestionId(QUESTION_ID);
        config.setGeneratorLanguage("python");
        config.setPrimaryStandardLanguage("python");

        JudgeStandardSolutionRecord solution = new JudgeStandardSolutionRecord();
        solution.setLanguage("python");
        solution.setPrimarySolution(true);

        JudgeManifestBundle bundle = new JudgeManifestBundle(
                config,
                manifest,
                "print('hello')".getBytes(StandardCharsets.UTF_8),
                Map.of("python", "print(input())".getBytes(StandardCharsets.UTF_8)),
                List.of(solution));

        when(judgeManifestLoader.load(CONFIG_ID)).thenReturn(bundle);
    }

    private SandboxExecutionResult createResult(int exitCode, String stdout, boolean timedOut) {
        return new SandboxExecutionResult(
                exitCode,
                stdout.getBytes(StandardCharsets.UTF_8),
                new byte[0],
                timedOut,
                10,
                1024,
                null);
    }
}
