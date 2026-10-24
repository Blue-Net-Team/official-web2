package com.bluenet.judge.application.service.workflow;

import com.bluenet.judge.application.dto.JudgeManifestBundle;
import com.bluenet.judge.application.dto.SandboxExecutionResult;
import com.bluenet.judge.application.service.JudgeManifestLoader;
import com.bluenet.judge.application.service.SandboxCodeRunner;
import com.bluenet.judge.infrastructure.repository.JudgeMetadataRepository;
import com.bluenet.judge.infrastructure.repository.dataobject.JudgeProblemConfigRecord;
import com.bluenet.judge.infrastructure.repository.dataobject.JudgeStandardSolutionRecord;
import com.bluenet.judge.infrastructure.repository.dataobject.GeneratedTestCaseWrite;
import com.bluenet.judge.infrastructure.repository.dataobject.JudgeTestCaseRecord;
import com.bluenet.judge.infrastructure.storage.JudgeAssetStorage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
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
    @DisplayName("存在旧测试用例：删除上一版对象必须早于本次生成的对象写入")
    void handle_withOldTestCases_shouldDeleteBeforeUpload() throws Exception {
        stubBundle();

        JudgeTestCaseRecord oldCase = new JudgeTestCaseRecord();
        oldCase.setCaseNo(1);
        oldCase.setInputObjectKey("questions/10/current/testcases/0001-aaaaaaaa.in");
        oldCase.setOutputObjectKey("questions/10/current/testcases/0001-bbbbbbbb.out");
        when(judgeMetadataRepository.findTestCasesByConfigId(CONFIG_ID)).thenReturn(List.of(oldCase));

        when(sandboxCodeRunner.run(eq("python"), any(), any()))
                .thenReturn(createResult(0, "1 2 3", false))
                .thenReturn(createResult(0, "6", false));
        when(transactionManager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));

        workflow.handle(CONFIG_ID);

        InOrder inOrder = inOrder(judgeAssetStorage);
        inOrder.verify(judgeAssetStorage).delete(oldCase.getInputObjectKey());
        inOrder.verify(judgeAssetStorage).delete(oldCase.getOutputObjectKey());
        // 两次对象写入必须全部发生在删除之后
        inOrder.verify(judgeAssetStorage, org.mockito.Mockito.times(2))
                .put(any(), any(), eq("text/plain; charset=utf-8"));
    }

    @Test
    @DisplayName("内容未变时连续生成两次：第二次生成的用例对象仍可读取")
    void handle_rerunWithUnchangedContent_shouldKeepGeneratedAssetsReadable() throws Exception {
        stubBundle();
        InMemoryJudgeAssetStorage storage = new InMemoryJudgeAssetStorage();
        List<JudgeTestCaseRecord> currentRecords = new ArrayList<>();

        when(judgeMetadataRepository.findTestCasesByConfigId(CONFIG_ID))
                .thenAnswer(invocation -> List.copyOf(currentRecords));
        doAnswer(invocation -> {
            List<GeneratedTestCaseWrite> writes = invocation.getArgument(1);
            currentRecords.clear();
            currentRecords.addAll(toRecords(writes));
            return null;
        }).when(judgeMetadataRepository).replaceGeneratedTestCases(eq(CONFIG_ID), any());
        when(transactionManager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));
        // 两次生成内容完全相同：内容寻址键不变
        when(sandboxCodeRunner.run(eq("python"), any(), any()))
                .thenReturn(createResult(0, "1 2 3", false))
                .thenReturn(createResult(0, "6", false))
                .thenReturn(createResult(0, "1 2 3", false))
                .thenReturn(createResult(0, "6", false));

        workflowWith(storage).handle(CONFIG_ID);
        workflowWith(storage).handle(CONFIG_ID);

        assertThat(currentRecords).hasSize(1);
        JudgeTestCaseRecord record = currentRecords.get(0);
        assertThat(storage.contains(record.getInputObjectKey())).isTrue();
        assertThat(storage.contains(record.getOutputObjectKey())).isTrue();
    }

    @Test
    @DisplayName("重跑时 Generator 失败：应标记 FAILED 且不保留上一版测试用例资产")
    void handle_rerunWhenGeneratorFails_shouldMarkFailedAndDiscardPreviousAssets() throws Exception {
        stubBundle();
        InMemoryJudgeAssetStorage storage = new InMemoryJudgeAssetStorage();
        String oldInputKey = "questions/10/current/testcases/0001-cccccccc.in";
        String oldOutputKey = "questions/10/current/testcases/0001-dddddddd.out";
        storage.put(oldInputKey, "1 2 3".getBytes(StandardCharsets.UTF_8), "text/plain; charset=utf-8");
        storage.put(oldOutputKey, "6".getBytes(StandardCharsets.UTF_8), "text/plain; charset=utf-8");

        JudgeTestCaseRecord oldCase = new JudgeTestCaseRecord();
        oldCase.setCaseNo(1);
        oldCase.setInputObjectKey(oldInputKey);
        oldCase.setOutputObjectKey(oldOutputKey);
        when(judgeMetadataRepository.findTestCasesByConfigId(CONFIG_ID)).thenReturn(List.of(oldCase));
        when(sandboxCodeRunner.run(eq("python"), any(), any()))
                .thenReturn(createResult(1, "", false));

        assertThatThrownBy(() -> workflowWith(storage).handle(CONFIG_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("generator 执行失败");

        verify(judgeMetadataRepository).markConfigStatus(CONFIG_ID, "FAILED");
        assertThat(storage.contains(oldInputKey)).isFalse();
        assertThat(storage.contains(oldOutputKey)).isFalse();
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
        stubBundle();
    }

    private void stubBundle() throws Exception {
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

    private TestDataGenerationWorkflow workflowWith(JudgeAssetStorage storage) {
        return new TestDataGenerationWorkflow(
                judgeManifestLoader,
                judgeMetadataRepository,
                storage,
                sandboxCodeRunner,
                benchmarkWorkflow,
                transactionManager);
    }

    private List<JudgeTestCaseRecord> toRecords(List<GeneratedTestCaseWrite> writes) {
        return writes.stream().map(write -> {
            JudgeTestCaseRecord record = new JudgeTestCaseRecord();
            record.setConfigId(write.getConfigId());
            record.setQuestionId(write.getQuestionId());
            record.setCaseNo(write.getCaseNo());
            record.setInputObjectKey(write.getInputObjectKey());
            record.setOutputObjectKey(write.getOutputObjectKey());
            return record;
        }).toList();
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

    /**
     * 记录状态的判题资产存储替身：delete 真的移除对象，put 真的写回对象。
     */
    private static final class InMemoryJudgeAssetStorage implements JudgeAssetStorage {
        /** 键到内容的映射。 */
        private final Map<String, byte[]> objects = new HashMap<>();

        @Override
        public byte[] get(String objectKey) {
            byte[] content = objects.get(objectKey);
            if (content == null) {
                throw new RuntimeException("读取判题资产失败：" + objectKey);
            }
            return content;
        }

        @Override
        public void put(String objectKey, byte[] content, String contentType) {
            objects.put(objectKey, content);
        }

        @Override
        public void delete(String objectKey) {
            objects.remove(objectKey);
        }

        boolean contains(String objectKey) {
            return objects.containsKey(objectKey);
        }
    }
}
