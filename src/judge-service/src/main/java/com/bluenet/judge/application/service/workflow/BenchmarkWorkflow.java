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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 标准解 benchmark 工作流。
 * <p>
 * 在测试数据生成成功后，对每个语言的标准解运行多次，收集 CPU 时间和内存峰值， 计算建议限时并写入数据库。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BenchmarkWorkflow {
    /** benchmark 阶段宽松时间限制，单位毫秒。 */
    private static final int BENCHMARK_TIME_LIMIT_MS = 30000;
    /** benchmark 阶段宽松内存限制，单位 KB。 */
    private static final int BENCHMARK_MEMORY_LIMIT_KB = 512 * 1024;
    /** benchmark 阶段输出限制，单位 KB。 */
    private static final int BENCHMARK_OUTPUT_LIMIT_KB = 1024;

    private final JudgeMetadataRepository judgeMetadataRepository;
    private final FormalTestcaseLoader formalTestcaseLoader;
    private final SandboxCodeRunner sandboxCodeRunner;
    private final JudgeAssetStorage judgeAssetStorage;
    private final BenchmarkLimitSuggester benchmarkLimitSuggester;

    /**
     * 处理单个配置的 benchmark 任务。
     *
     * @param configId
     *            判题配置主键。
     */
    @Transactional
    public void handle(Long configId) {
        JudgeProblemConfigRecord config = judgeMetadataRepository.findConfig(configId)
                .orElseThrow(() -> new IllegalArgumentException("判题配置不存在：" + configId));

        List<JudgeStandardSolutionRecord> solutions = judgeMetadataRepository.findStandardSolutions(configId);
        if (solutions.isEmpty()) {
            judgeMetadataRepository.markConfigStatus(configId, "READY");
            log.info("配置无标准解，跳过 benchmark，configId={}", configId);
            return;
        }

        List<JudgeTestCaseRecord> testcases = judgeMetadataRepository.findTestCasesByConfigId(configId);
        if (testcases.isEmpty()) {
            log.warn("配置无测试用例，跳过 benchmark，configId={}", configId);
            judgeMetadataRepository.markConfigStatus(configId, "FAILED");
            return;
        }

        judgeMetadataRepository.markConfigStatus(configId, "BENCHMARKING");
        List<FormalTestcaseBundle> bundles = formalTestcaseLoader.load(testcases);

        boolean allSuccess = true;
        for (JudgeStandardSolutionRecord solution : solutions) {
            boolean success = benchmarkSingleSolution(config, solution, bundles);
            if (!success) {
                allSuccess = false;
            }
        }

        judgeMetadataRepository.markConfigStatus(configId, allSuccess ? "READY" : "FAILED");
        log.info("Benchmark 完成，configId={}，结果={}", configId, allSuccess ? "READY" : "FAILED");
    }

    private boolean benchmarkSingleSolution(JudgeProblemConfigRecord config,
            JudgeStandardSolutionRecord solution,
            List<FormalTestcaseBundle> bundles) {
        byte[] source = judgeAssetStorage.get(solution.getObjectKey());
        int repeatTimes = config.getBenchmarkRepeatTimes() != null ? config.getBenchmarkRepeatTimes() : 5;

        List<Integer> allCpuTimes = new ArrayList<>();
        int maxMemoryKb = 0;
        String errorMessage = null;

        for (FormalTestcaseBundle bundle : bundles) {
            for (int i = 0; i < repeatTimes; i++) {
                try {
                    SandboxExecutionResult result = sandboxCodeRunner.run(
                            solution.getLanguage(),
                            source,
                            bundle.input(),
                            BENCHMARK_TIME_LIMIT_MS,
                            BENCHMARK_MEMORY_LIMIT_KB,
                            BENCHMARK_OUTPUT_LIMIT_KB);
                    if (result.timedOut()) {
                        errorMessage = "Benchmark 超时，caseNo=" + bundle.testcase().getCaseNo();
                        break;
                    }
                    if (result.exitCode() != 0) {
                        errorMessage = "Benchmark 运行错误，caseNo=" + bundle.testcase().getCaseNo()
                                + "，stderr=" + new String(result.stderr());
                        break;
                    }
                    allCpuTimes.add(result.timeUsedMs());
                    if (result.memoryUsedKb() > maxMemoryKb) {
                        maxMemoryKb = result.memoryUsedKb();
                    }
                } catch (Exception ex) {
                    errorMessage = "Benchmark 异常，caseNo=" + bundle.testcase().getCaseNo() + "，" + ex.getMessage();
                    break;
                }
            }
            if (errorMessage != null) {
                break;
            }
        }

        if (errorMessage != null) {
            judgeMetadataRepository.updateBenchmarkResult(
                    solution.getId(),
                    "FAILED",
                    null,
                    null,
                    null,
                    null,
                    errorMessage);
            log.warn(
                    "标准解 benchmark 失败，solutionId={}，language={}，原因={}",
                    solution.getId(),
                    solution.getLanguage(),
                    errorMessage);
            return false;
        }

        if (allCpuTimes.isEmpty()) {
            judgeMetadataRepository.updateBenchmarkResult(
                    solution.getId(),
                    "FAILED",
                    null,
                    null,
                    null,
                    null,
                    "无有效 benchmark 数据");
            return false;
        }

        Collections.sort(allCpuTimes);
        int p95Index = (int) Math.ceil(allCpuTimes.size() * 0.95) - 1;
        int p95TimeMs = allCpuTimes.get(Math.max(0, p95Index));
        int maxTimeMs = allCpuTimes.get(allCpuTimes.size() - 1);

        int suggestedTimeLimitMs = benchmarkLimitSuggester.suggestTimeLimitMs(
                p95TimeMs,
                config.getMarginMultiplier() != null ? config.getMarginMultiplier() : BigDecimal.valueOf(1.5),
                config.getMinExtraMs() != null ? config.getMinExtraMs() : 50,
                config.getRoundToMs() != null ? config.getRoundToMs() : 50);

        judgeMetadataRepository.updateBenchmarkResult(
                solution.getId(),
                "DONE",
                p95TimeMs,
                maxTimeMs,
                maxMemoryKb,
                suggestedTimeLimitMs,
                "Benchmark 完成，共运行 " + allCpuTimes.size() + " 次");

        log.info(
                "标准解 benchmark 完成，solutionId={}，language={}，p95={}ms，max={}ms，peakMem={}KB，suggestLimit={}ms",
                solution.getId(),
                solution.getLanguage(),
                p95TimeMs,
                maxTimeMs,
                maxMemoryKb,
                suggestedTimeLimitMs);
        return true;
    }
}
