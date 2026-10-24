package com.bluenet.judge.application.service.workflow;

import com.bluenet.judge.application.dto.JudgeManifestBundle;
import com.bluenet.judge.application.dto.SandboxExecutionResult;
import com.bluenet.judge.application.service.JudgeManifestLoader;
import com.bluenet.judge.application.service.SandboxCodeRunner;
import com.bluenet.judge.infrastructure.repository.JudgeMetadataRepository;
import com.bluenet.judge.infrastructure.repository.dataobject.GeneratedTestCaseWrite;
import com.bluenet.judge.infrastructure.repository.dataobject.JudgeStandardSolutionRecord;
import com.bluenet.judge.infrastructure.repository.dataobject.JudgeTestCaseRecord;
import com.bluenet.judge.infrastructure.storage.JudgeAssetStorage;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

/**
 * 测试数据生成工作流。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TestDataGenerationWorkflow {
    /** 生成后测试用例文本文件内容类型。 */
    private static final String CONTENT_TYPE_TEXT = "text/plain; charset=utf-8";

    /** manifest 和源码资产加载器。 */
    private final JudgeManifestLoader judgeManifestLoader;
    /** 判题元数据持久化访问入口。 */
    private final JudgeMetadataRepository judgeMetadataRepository;
    /** 判题资产对象存储。 */
    private final JudgeAssetStorage judgeAssetStorage;
    /** 沙箱源码运行器。 */
    private final SandboxCodeRunner sandboxCodeRunner;
    /** 标准解 benchmark 工作流。 */
    private final BenchmarkWorkflow benchmarkWorkflow;
    /** 事务管理器。 */
    private final PlatformTransactionManager transactionManager;

    /**
     * 处理单个测试数据生成任务。
     *
     * @param configId
     *            判题配置主键。
     * @return 无返回值。
     */
    public void handle(Long configId) {
        try {
            List<JudgeTestCaseRecord> oldTestCases = judgeMetadataRepository.findTestCasesByConfigId(configId);
            // 先清理上一版资产，再生成并写入本次对象；顺序不可倒置。
            deleteOldTestcaseAssets(oldTestCases);

            JudgeManifestBundle bundle = judgeManifestLoader.load(configId);
            List<GeneratedTestCaseWrite> generatedTestCases = generateTestCases(bundle);

            TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
            txTemplate.executeWithoutResult(status -> {
                judgeMetadataRepository.replaceGeneratedTestCases(configId, generatedTestCases);
            });

            log.info(
                    "测试数据生成完成，配置编号={}，标准解语言={}，测试用例数量={}",
                    configId,
                    bundle.standardSolutionSources().keySet(),
                    generatedTestCases.size());
            judgeMetadataRepository.markConfigStatus(configId, "GENERATED");
            benchmarkWorkflow.handle(configId);
        } catch (RuntimeException ex) {
            judgeMetadataRepository.markConfigStatus(configId, "FAILED");
            throw ex;
        }
    }

    /**
     * 生成前清理上一版测试用例 OSS 资产，失败仅记录日志不影响主流程。
     * <p>
     * 测试用例对象键是内容寻址的（{@code questions/{questionId}/current/testcases/{caseNo}-{sha256}.in|out}），
     * generator 源码与 generatorArgs 不变时新旧键完全相同，因此清理必须在写入之前完成，
     * 否则会把本次刚上传的活对象删掉，导致后续 benchmark 读取抛 NoSuchKey。
     * </p>
     * <p>
     * 该顺序依赖「清理之后本次生成必然写入对象」这一前提。若将来引入「内容哈希相同则跳过上传」
     * 的优化，必须重新评估此处顺序，否则会重现对象被删后不再写回的缺陷。
     * </p>
     *
     * @param oldTestCases
     *            上一版测试用例记录列表。
     */
    private void deleteOldTestcaseAssets(List<JudgeTestCaseRecord> oldTestCases) {
        if (oldTestCases == null || oldTestCases.isEmpty()) {
            return;
        }
        for (JudgeTestCaseRecord testcase : oldTestCases) {
            try {
                if (testcase.getInputObjectKey() != null) {
                    judgeAssetStorage.delete(testcase.getInputObjectKey());
                }
                if (testcase.getOutputObjectKey() != null) {
                    judgeAssetStorage.delete(testcase.getOutputObjectKey());
                }
            } catch (Exception ex) {
                log.warn(
                        "删除旧测试用例 OSS 对象失败，caseNo={}，inputKey={}，outputKey={}",
                        testcase.getCaseNo(),
                        testcase.getInputObjectKey(),
                        testcase.getOutputObjectKey(),
                        ex);
            }
        }
    }

    /**
     * 生成指定配置下的全部测试用例输入输出文件。
     *
     * @param bundle
     *            manifest 和源码资产加载结果。
     * @return 生成后的测试用例写入对象列表。
     */
    private List<GeneratedTestCaseWrite> generateTestCases(JudgeManifestBundle bundle) {
        List<GeneratedTestCaseWrite> generated = new ArrayList<>();
        byte[] primaryStandardSource = primaryStandardSource(bundle);
        for (JsonNode testcase : bundle.manifest().path("testcases")) {
            byte[] generatorInput = testcase.path("generatorArgs").toString().getBytes(StandardCharsets.UTF_8);
            SandboxExecutionResult inputResult = sandboxCodeRunner.run(
                    bundle.config().getGeneratorLanguage(),
                    bundle.generatorSource(),
                    generatorInput);
            ensureSuccess(inputResult, "generator", testcase.path("caseNo").asInt());

            SandboxExecutionResult outputResult = sandboxCodeRunner.run(
                    bundle.config().getPrimaryStandardLanguage(),
                    primaryStandardSource,
                    inputResult.stdout());
            ensureSuccess(outputResult, "主标准解", testcase.path("caseNo").asInt());

            generated.add(uploadAndBuildRecord(bundle, testcase, inputResult.stdout(), outputResult.stdout()));
        }
        return generated;
    }

    /**
     * 获取主标准解源码。
     *
     * @param bundle
     *            manifest 和源码资产加载结果。
     * @return 主标准解源码字节内容。
     */
    private byte[] primaryStandardSource(JudgeManifestBundle bundle) {
        String primaryLanguage = bundle.config().getPrimaryStandardLanguage();
        byte[] source = bundle.standardSolutionSources().get(primaryLanguage);
        if (source != null) {
            return source;
        }
        return bundle.standardSolutions()
                .stream()
                .filter(JudgeStandardSolutionRecord::getPrimarySolution)
                .findFirst()
                .map(solution -> bundle.standardSolutionSources().get(solution.getLanguage()))
                .orElseThrow(() -> new IllegalStateException("主标准解源码不存在：" + primaryLanguage));
    }

    /**
     * 校验沙箱执行结果。
     *
     * @param result
     *            沙箱执行结果。
     * @param stage
     *            当前执行阶段。
     * @param caseNo
     *            测试用例序号。
     * @return 无返回值。
     */
    private void ensureSuccess(SandboxExecutionResult result, String stage, int caseNo) {
        if (result.timedOut()) {
            throw new IllegalStateException("%s 执行超时，caseNo=%d".formatted(stage, caseNo));
        }
        if (result.exitCode() != 0) {
            throw new IllegalStateException("%s 执行失败，caseNo=%d，stderr=%s"
                    .formatted(stage, caseNo, new String(result.stderr(), StandardCharsets.UTF_8)));
        }
    }

    /**
     * 上传生成文件并构建数据库写入对象。
     *
     * @param bundle
     *            manifest 和源码资产加载结果。
     * @param testcase
     *            测试用例配置 JSON。
     * @param input
     *            生成后的输入文件字节内容。
     * @param output
     *            生成后的期望输出文件字节内容。
     * @return 生成后的测试用例写入对象。
     */
    private GeneratedTestCaseWrite uploadAndBuildRecord(
            JudgeManifestBundle bundle,
            JsonNode testcase,
            byte[] input,
            byte[] output) {
        int caseNo = testcase.path("caseNo").asInt();
        // 对象键内容寻址：内容相同则键相同。该特性依赖「清理旧键发生在本次写入之前」（见 deleteOldTestcaseAssets）。
        String baseKey = "questions/%d/current/testcases/%04d".formatted(bundle.config().getQuestionId(), caseNo);
        String inputHash = sha256(input);
        String outputHash = sha256(output);
        String inputKey = "%s-%s.in".formatted(baseKey, inputHash);
        String outputKey = "%s-%s.out".formatted(baseKey, outputHash);
        judgeAssetStorage.put(inputKey, input, CONTENT_TYPE_TEXT);
        judgeAssetStorage.put(outputKey, output, CONTENT_TYPE_TEXT);
        return GeneratedTestCaseWrite.builder()
                .configId(bundle.config().getId())
                .questionId(bundle.config().getQuestionId())
                .caseNo(caseNo)
                .category(testcase.path("category").asText())
                .inputObjectKey(inputKey)
                .inputObjectHash(inputHash)
                .outputObjectKey(outputKey)
                .outputObjectHash(outputHash)
                .inputSizeBytes((long) input.length)
                .outputSizeBytes((long) output.length)
                .weight(new BigDecimal(testcase.path("weight").asText("1")))
                .hidden(testcase.path("hidden").asBoolean(true))
                .sample(testcase.path("sample").asBoolean(false))
                .build();
    }

    /**
     * 计算内容 SHA-256 哈希。
     *
     * @param content
     *            文件字节内容。
     * @return 十六进制 SHA-256 哈希。
     */
    private String sha256(byte[] content) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        } catch (Exception ex) {
            throw new IllegalStateException("计算测试用例文件哈希失败", ex);
        }
    }
}
