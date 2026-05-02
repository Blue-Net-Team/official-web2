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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 正式判题任务工作流。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FormalJudgeWorkflow {
    /** 判题任务持久化访问入口。 */
    private final JudgeJobRepository judgeJobRepository;
    /** 判题元数据持久化访问入口。 */
    private final JudgeMetadataRepository judgeMetadataRepository;
    /** 正式测试用例文件加载器。 */
    private final FormalTestcaseLoader formalTestcaseLoader;
    /** 判题结果持久化访问入口。 */
    private final JudgeResultRepository judgeResultRepository;
    /** 沙箱源码运行器。 */
    private final SandboxCodeRunner sandboxCodeRunner;
    /** JSON 解析器。 */
    private final ObjectMapper objectMapper;

    /**
     * 处理单个判题任务。
     *
     * @param jobId
     *            判题任务主键。
     * @return 无返回值。
     */
    @Transactional
    public void handle(Long jobId) {
        JudgeJobRecord job = judgeJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("判题任务不存在：" + jobId));
        if ("FORMAL".equals(job.getTestcaseType())) {
            handleFormalJob(job);
        } else {
            handleRunJob(job);
        }
    }

    private void handleFormalJob(JudgeJobRecord job) {
        JudgeLanguageLimitRecord limit = judgeMetadataRepository
                .findConfirmedLimit(job.getQuestionId(), job.getLanguage())
                .orElse(null);
        if (limit == null) {
            judgeJobRepository.markReviewRequired(job.getId(), "该语言未确认判题资源限制");
            return;
        }
        var testcases = judgeMetadataRepository.findCurrentTestCases(job.getQuestionId());
        if (testcases.isEmpty()) {
            judgeJobRepository.markReviewRequired(job.getId(), "题目未生成可用正式测试数据");
            return;
        }
        List<FormalTestcaseBundle> testcaseBundles;
        try {
            testcaseBundles = formalTestcaseLoader.load(testcases);
        } catch (RuntimeException ex) {
            String message = ex.getMessage() != null ? ex.getMessage() : "加载测试用例文件失败";
            judgeJobRepository.markReviewRequired(job.getId(), "OSS 文件缺失或加载失败：" + message);
            log.error("正式判题加载测试用例失败，任务编号={}，原因={}", job.getId(), message, ex);
            return;
        }

        judgeJobRepository.markRunning(job.getId());
        judgeResultRepository.deleteCaseResults(job.getId());
        JudgeSummary summary;
        try {
            summary = judgeAllCases(job, limit, testcaseBundles, true);
        } catch (IllegalStateException ex) {
            if (ex.getMessage() != null && ex.getMessage().startsWith("源码编译失败")) {
                throw ex;
            }
            judgeJobRepository.markReviewRequired(job.getId(), "判题基础设施错误：" + ex.getMessage());
            log.error("正式判题执行失败，任务编号={}，原因={}", job.getId(), ex.getMessage(), ex);
            return;
        }
        judgeResultRepository.insertAssessmentJudgement(buildJudgement(job, summary));
        judgeJobRepository.markSucceeded(job.getId());
        log.info(
                "正式判题完成，任务编号={}，题目编号={}，语言={}，结果={}，通过用例={}/{}",
                job.getId(),
                job.getQuestionId(),
                job.getLanguage(),
                summary.resultCode(),
                summary.acceptedCount(),
                testcaseBundles.size());
    }

    private void handleRunJob(JudgeJobRecord job) {
        // RUN 类型使用宽松默认限制，不需要管理员确认
        JudgeLanguageLimitRecord limit = new JudgeLanguageLimitRecord();
        limit.setTimeLimitMs(5000);
        limit.setMemoryLimitKb(256 * 1024);
        limit.setOutputLimitKb(1024);

        List<FormalTestcaseBundle> bundles;
        boolean compareOutput;
        if ("CUSTOM_RUN".equals(job.getTestcaseType())) {
            bundles = List.of(createCustomRunBundle(job.getCustomInput()));
            compareOutput = false;
        } else {
            // DEFAULT_RUN
            bundles = loadRunTestCasesFromQuestion(job.getQuestionId());
            if (bundles.isEmpty()) {
                judgeJobRepository.markReviewRequired(job.getId(), "题目未配置运行用例或题面样例");
                return;
            }
            compareOutput = true;
        }

        judgeJobRepository.markRunning(job.getId());
        judgeResultRepository.deleteCaseResults(job.getId());
        for (FormalTestcaseBundle bundle : bundles) {
            CaseVerdict verdict = judgeSingleCase(job, limit, bundle, compareOutput);
            judgeResultRepository.insertCaseResult(buildRunCaseResult(job, bundle, verdict));
        }
        judgeJobRepository.markSucceeded(job.getId());
        log.info(
                "运行判题完成，任务编号={}，类型={}，题目编号={}，语言={}，用例数={}",
                job.getId(),
                job.getTestcaseType(),
                job.getQuestionId(),
                job.getLanguage(),
                bundles.size());
    }

    /**
     * 从题目内容 JSON 中加载运行用例（优先 runTestCases，其次 examples）。
     */
    private List<FormalTestcaseBundle> loadRunTestCasesFromQuestion(Long questionId) {
        String contentJson = judgeMetadataRepository.findQuestionContent(questionId);
        if (contentJson == null || contentJson.isBlank()) {
            return List.of();
        }
        try {
            JsonNode root = objectMapper.readTree(contentJson);
            List<FormalTestcaseBundle> bundles = parseTestCaseArray(root.path("runTestCases"), 1);
            if (bundles.isEmpty()) {
                bundles = parseTestCaseArray(root.path("examples"), 1);
            }
            return bundles;
        } catch (Exception ex) {
            log.warn("解析题目内容失败，题目编号={}", questionId, ex);
            return List.of();
        }
    }

    private List<FormalTestcaseBundle> parseTestCaseArray(JsonNode arrayNode, int startCaseNo) {
        List<FormalTestcaseBundle> bundles = new ArrayList<>();
        if (!arrayNode.isArray()) {
            return bundles;
        }
        int caseNo = startCaseNo;
        for (JsonNode node : arrayNode) {
            String input = node.path("input").asText("");
            String expectedOutput = node.path("expectedOutput").asText("");
            JudgeTestCaseRecord record = new JudgeTestCaseRecord();
            record.setCaseNo(caseNo++);
            record.setWeight(BigDecimal.ONE);
            record.setHidden(false);
            bundles.add(
                    new FormalTestcaseBundle(
                            record,
                            input.getBytes(StandardCharsets.UTF_8),
                            expectedOutput.getBytes(StandardCharsets.UTF_8)));
        }
        return bundles;
    }

    private FormalTestcaseBundle createCustomRunBundle(String customInput) {
        JudgeTestCaseRecord record = new JudgeTestCaseRecord();
        record.setCaseNo(1);
        record.setWeight(BigDecimal.ONE);
        record.setHidden(false);
        String input = customInput == null ? "" : customInput;
        return new FormalTestcaseBundle(record, input.getBytes(StandardCharsets.UTF_8), new byte[0]);
    }

    /**
     * 执行全部正式测试用例。
     */
    private JudgeSummary judgeAllCases(
            JudgeJobRecord job,
            JudgeLanguageLimitRecord limit,
            List<FormalTestcaseBundle> testcaseBundles,
            boolean compareOutput) {
        BigDecimal totalWeight = BigDecimal.ZERO;
        BigDecimal acceptedWeight = BigDecimal.ZERO;
        String finalResultCode = "AC";
        int acceptedCount = 0;
        for (FormalTestcaseBundle bundle : testcaseBundles) {
            BigDecimal weight = testcaseWeight(bundle);
            totalWeight = totalWeight.add(weight);
            CaseVerdict verdict = judgeSingleCase(job, limit, bundle, compareOutput);
            if ("AC".equals(verdict.status())) {
                acceptedWeight = acceptedWeight.add(weight);
                acceptedCount++;
            } else if ("AC".equals(finalResultCode)) {
                finalResultCode = verdict.status();
            }
            judgeResultRepository.insertCaseResult(buildCaseResult(job, bundle, verdict));
        }
        BigDecimal score = calculateScore(job.getQuestionMaxScore(), totalWeight, acceptedWeight);
        return new JudgeSummary(finalResultCode, score, acceptedCount);
    }

    /**
     * 执行单个测试用例。
     */
    private CaseVerdict judgeSingleCase(JudgeJobRecord job, JudgeLanguageLimitRecord limit,
            FormalTestcaseBundle bundle, boolean compareOutput) {
        try {
            SandboxExecutionResult result = sandboxCodeRunner.run(
                    job.getLanguage(),
                    job.getSourceCode().getBytes(StandardCharsets.UTF_8),
                    bundle.input(),
                    limit.getTimeLimitMs(),
                    limit.getMemoryLimitKb(),
                    limit.getOutputLimitKb());
            if (limit.getMemoryLimitKb() != null
                    && (result.memoryUsedKb() >= limit.getMemoryLimitKb()
                            || ("SG".equals(result.isolateStatus())
                                    && result.memoryUsedKb() >= limit.getMemoryLimitKb() * 0.95))) {
                return new CaseVerdict("MLE", result, "内存超限");
            }
            if (result.timedOut() || "TO".equals(result.isolateStatus())) {
                return new CaseVerdict("TLE", result, "程序执行超时");
            }
            if (result.exitCode() != 0) {
                return new CaseVerdict("RE", result, "程序运行错误");
            }
            if (compareOutput && !normalizeOutput(result.stdout()).equals(normalizeOutput(bundle.expectedOutput()))) {
                return new CaseVerdict("WA", result, "答案错误");
            }
            return new CaseVerdict("AC", result, "通过");
        } catch (IllegalStateException ex) {
            if (ex.getMessage() != null && ex.getMessage().startsWith("源码编译失败")) {
                return new CaseVerdict("CE", new SandboxExecutionResult(1, new byte[0],
                        ex.getMessage().getBytes(StandardCharsets.UTF_8), false, 0, 0, null), "源码编译失败");
            }
            throw ex;
        }
    }

    /**
     * 构建正式判题单测试用例结果写入对象。
     */
    private JudgeCaseResultWrite buildCaseResult(JudgeJobRecord job, FormalTestcaseBundle bundle, CaseVerdict verdict) {
        SandboxExecutionResult result = verdict.result();
        return new JudgeCaseResultWrite(
                job.getId(),
                bundle.testcase().getCaseNo(),
                job.getTestcaseType(),
                verdict.status(),
                visibleText(bundle.input(), verdict.status()),
                visibleText(bundle.expectedOutput(), verdict.status()),
                visibleText(result.stdout(), verdict.status()),
                visibleText(result.stdout(), verdict.status()),
                visibleText(result.stderr(), verdict.status()),
                result.timeUsedMs(),
                result.memoryUsedKb(),
                verdict.message(),
                !"AC".equals(verdict.status()));
    }

    /**
     * 构建运行判题单测试用例结果写入对象（所有输出对候选人可见）。
     */
    private JudgeCaseResultWrite buildRunCaseResult(JudgeJobRecord job, FormalTestcaseBundle bundle,
            CaseVerdict verdict) {
        SandboxExecutionResult result = verdict.result();
        return new JudgeCaseResultWrite(
                job.getId(),
                bundle.testcase().getCaseNo(),
                job.getTestcaseType(),
                verdict.status(),
                new String(bundle.input(), StandardCharsets.UTF_8),
                new String(bundle.expectedOutput(), StandardCharsets.UTF_8),
                new String(result.stdout(), StandardCharsets.UTF_8),
                new String(result.stdout(), StandardCharsets.UTF_8),
                new String(result.stderr(), StandardCharsets.UTF_8),
                result.timeUsedMs(),
                result.memoryUsedKb(),
                verdict.message(),
                true);
    }

    /**
     * 构建正式提交自动评判结果写入对象。
     */
    private AssessmentJudgementWrite buildJudgement(JudgeJobRecord job, JudgeSummary summary) {
        return AssessmentJudgementWrite.builder()
                .answerId(job.getAnswerId())
                .questionId(job.getQuestionId())
                .assessmentTimeId(job.getAssessmentTimeId())
                .userId(job.getUserId())
                .score(summary.score())
                .maxScore(job.getQuestionMaxScore())
                .status("JUDGED")
                .resultCode(summary.resultCode())
                .source("AUTO")
                .reviewerType("SYSTEM")
                .comment("自动判题完成，结果：" + summary.resultCode())
                .build();
    }

    /**
     * 按权重计算正式提交得分。
     */
    private BigDecimal calculateScore(BigDecimal maxScore, BigDecimal totalWeight, BigDecimal acceptedWeight) {
        if (maxScore == null || totalWeight.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return maxScore.multiply(acceptedWeight)
                .divide(totalWeight, 2, RoundingMode.HALF_UP);
    }

    /**
     * 读取测试用例权重。
     */
    private BigDecimal testcaseWeight(FormalTestcaseBundle bundle) {
        return bundle.testcase().getWeight() == null ? BigDecimal.ONE : bundle.testcase().getWeight();
    }

    /**
     * 标准化输出，兼容不同系统换行并忽略末尾空白。
     */
    private String normalizeOutput(byte[] content) {
        return new String(content, StandardCharsets.UTF_8)
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .stripTrailing();
    }

    /**
     * 根据用例结果决定是否保存可见文本。
     */
    private String visibleText(byte[] content, String status) {
        if ("AC".equals(status)) {
            return "";
        }
        return new String(content, StandardCharsets.UTF_8);
    }

    /**
     * 单测试用例判定结果。
     */
    private record CaseVerdict(String status, SandboxExecutionResult result, String message) {
    }

    /**
     * 正式判题汇总结果。
     */
    private record JudgeSummary(String resultCode, BigDecimal score, int acceptedCount) {
    }
}
