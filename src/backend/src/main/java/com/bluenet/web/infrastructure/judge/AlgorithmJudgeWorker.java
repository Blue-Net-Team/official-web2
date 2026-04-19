package com.bluenet.web.infrastructure.judge;

import com.bluenet.web.domain.model.enumerate.AlgorithmTestcaseType;
import com.bluenet.web.domain.model.enumerate.JudgeCaseStatus;
import com.bluenet.web.domain.model.enumerate.JudgeJobStatus;
import com.bluenet.web.domain.model.enumerate.JudgementSource;
import com.bluenet.web.domain.model.enumerate.JudgementStatus;
import com.bluenet.web.domain.model.enumerate.ObjectiveResultCode;
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
import com.bluenet.web.infrastructure.judge.sandbox.SandboxTestcase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "bluenet.algorithm-judge", name = "worker-enabled", havingValue = "true")
public class AlgorithmJudgeWorker {
    private final AlgorithmJudgeJobRepository algorithmJudgeJobRepository;
    private final AlgorithmJudgeCaseResultRepository algorithmJudgeCaseResultRepository;
    private final AssessmentQuestionDomainService assessmentQuestionDomainService;
    private final AssessmentJudgementDomainService assessmentJudgementDomainService;
    private final SandboxExecutor sandboxExecutor;

    @RabbitListener(queues = AlgorithmJudgeQueueConfig.ALGORITHM_JUDGE_QUEUE)
    @Transactional
    public void consume(String judgeJobIdMessage) {
        Long judgeJobId = parseJudgeJobId(judgeJobIdMessage);
        AlgorithmJudgeJobVO job = algorithmJudgeJobRepository.findById(judgeJobId)
                .orElseThrow(() -> new IllegalStateException("判题任务不存在：" + judgeJobId));
        AssessmentQuestionVO question = assessmentQuestionDomainService.getQuestionById(job.getQuestionId());
        if (!(question.getContent()instanceof AlgorithmContent content)) {
            markReviewRequired(job, "算法题内容配置错误");
            return;
        }

        markRunning(job);
        SandboxExecutionResult result = sandboxExecutor.execute(
                SandboxExecutionRequest.builder()
                        .language(job.getLanguage())
                        .sourceCode(job.getSourceCode())
                        .timeLimitMs(content.getTimeLimit())
                        .memoryLimitKb(content.getMemoryLimit())
                        .testcases(resolveTestcases(job, content))
                        .build());

        if (result.isInfrastructureFailure()) {
            markRetryableOrReview(job, result.getInfrastructureMessage());
            return;
        }

        List<AlgorithmJudgeCaseResultVO> caseResults = result.getCaseResults()
                .stream()
                .map(caseResult -> convertToVO(job, caseResult))
                .toList();
        algorithmJudgeCaseResultRepository.saveAll(caseResults);
        markSucceeded(job);
        if (job.getTestcaseType() == AlgorithmTestcaseType.FORMAL) {
            createFormalJudgement(job, question, result.getCaseResults());
        }
    }

    private Long parseJudgeJobId(String judgeJobIdMessage) {
        try {
            return Long.valueOf(judgeJobIdMessage);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("判题任务消息格式错误：" + judgeJobIdMessage, e);
        }
    }

    private List<SandboxTestcase> resolveTestcases(AlgorithmJudgeJobVO job, AlgorithmContent content) {
        if (job.getTestcaseType() == AlgorithmTestcaseType.CUSTOM_RUN) {
            return List.of(
                    SandboxTestcase.builder()
                            .caseNo(1)
                            .input(job.getCustomInput())
                            .build());
        }
        List<AlgorithmContent.TestCase> cases = job.getTestcaseType() == AlgorithmTestcaseType.DEFAULT_RUN
                ? content.getRunTestCases()
                : content.getTestCases();
        return java.util.stream.IntStream.range(0, cases.size())
                .mapToObj(
                        index -> SandboxTestcase.builder()
                                .caseNo(index + 1)
                                .input(cases.get(index).getInput())
                                .expectedOutput(cases.get(index).getExpectedOutput())
                                .build())
                .toList();
    }

    private void markRunning(AlgorithmJudgeJobVO job) {
        job.setStatus(JudgeJobStatus.RUNNING);
        job.setStartedAt(LocalDateTime.now());
        job.setStatusMessage("正在判题");
        algorithmJudgeJobRepository.update(job);
    }

    private void markSucceeded(AlgorithmJudgeJobVO job) {
        job.setStatus(JudgeJobStatus.SUCCEEDED);
        job.setFinishedAt(LocalDateTime.now());
        job.setStatusMessage("判题完成");
        algorithmJudgeJobRepository.update(job);
    }

    private void markRetryableOrReview(AlgorithmJudgeJobVO job, String message) {
        int retryCount = job.getRetryCount() == null ? 0 : job.getRetryCount();
        int maxRetryCount = job.getMaxRetryCount() == null ? 3 : job.getMaxRetryCount();
        job.setRetryCount(retryCount + 1);
        job.setStatus(
                retryCount + 1 >= maxRetryCount ? JudgeJobStatus.FAILED_REVIEW_REQUIRED : JudgeJobStatus.RETRYING);
        job.setStatusMessage(message);
        algorithmJudgeJobRepository.update(job);
    }

    private void markReviewRequired(AlgorithmJudgeJobVO job, String message) {
        job.setStatus(JudgeJobStatus.FAILED_REVIEW_REQUIRED);
        job.setStatusMessage(message);
        algorithmJudgeJobRepository.update(job);
    }

    private AlgorithmJudgeCaseResultVO convertToVO(AlgorithmJudgeJobVO job, SandboxCaseResult result) {
        return AlgorithmJudgeCaseResultVO.builder()
                .judgeJobId(job.getId())
                .caseNo(result.getCaseNo())
                .testcaseType(job.getTestcaseType())
                .status(result.getStatus())
                .input(result.getInput())
                .expectedOutput(result.getExpectedOutput())
                .actualOutput(result.getActualOutput())
                .stdout(result.getStdout())
                .stderr(result.getStderr())
                .timeUsedMs(result.getTimeUsedMs())
                .memoryUsedKb(result.getMemoryUsedKb())
                .message(result.getMessage())
                // 正式提交只向考生展示失败用例；运行调试仍展示全部用例，方便定位默认/自定义输入结果。
                .visibleToCandidate(
                        job.getTestcaseType() != AlgorithmTestcaseType.FORMAL
                                || result.getStatus() != JudgeCaseStatus.AC)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private void createFormalJudgement(
            AlgorithmJudgeJobVO job,
            AssessmentQuestionVO question,
            List<SandboxCaseResult> caseResults) {
        ObjectiveResultCode resultCode = resolveResultCode(caseResults);
        BigDecimal maxScore = question.getScore() == null ? BigDecimal.ZERO : question.getScore();
        AssessmentJudgementVO judgement = AssessmentJudgementVO.builder()
                .answerId(job.getAnswerId())
                .questionId(job.getQuestionId())
                .assessmentTimeId(job.getAssessmentTimeId())
                .userId(job.getUserId())
                .score(resultCode == ObjectiveResultCode.AC ? maxScore : BigDecimal.ZERO)
                .maxScore(maxScore)
                .status(JudgementStatus.JUDGED)
                .resultCode(resultCode)
                .source(JudgementSource.AUTO)
                .judgedAt(LocalDateTime.now())
                .build();
        assessmentJudgementDomainService.createJudgement(judgement);
    }

    private ObjectiveResultCode resolveResultCode(List<SandboxCaseResult> results) {
        if (results.stream().allMatch(result -> result.getStatus() == JudgeCaseStatus.AC)) {
            return ObjectiveResultCode.AC;
        }
        if (results.stream().anyMatch(result -> result.getStatus() == JudgeCaseStatus.CE)) {
            return ObjectiveResultCode.CE;
        }
        if (results.stream().anyMatch(result -> result.getStatus() == JudgeCaseStatus.TLE)) {
            return ObjectiveResultCode.TLE;
        }
        if (results.stream().anyMatch(result -> result.getStatus() == JudgeCaseStatus.MLE)) {
            return ObjectiveResultCode.MLE;
        }
        if (results.stream().anyMatch(result -> result.getStatus() == JudgeCaseStatus.RE)) {
            return ObjectiveResultCode.RE;
        }
        return ObjectiveResultCode.WA;
    }
}
