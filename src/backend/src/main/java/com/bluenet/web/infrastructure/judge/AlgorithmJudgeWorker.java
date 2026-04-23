package com.bluenet.web.infrastructure.judge;

import com.bluenet.web.domain.model.enumerate.AlgorithmTestcaseType;
import com.bluenet.web.domain.model.enumerate.JudgeCaseStatus;
import com.bluenet.web.domain.model.enumerate.JudgementSource;
import com.bluenet.web.domain.model.enumerate.JudgementStatus;
import com.bluenet.web.domain.model.enumerate.ObjectiveResultCode;
import com.bluenet.web.domain.model.entity.AlgorithmJudgeCaseResult;
import com.bluenet.web.domain.model.entity.AlgorithmJudgeJob;
import com.bluenet.web.domain.model.vo.AssessmentJudgementVO;
import com.bluenet.web.domain.model.entity.AssessmentQuestion;
import com.bluenet.web.domain.model.vo.evaluation.AlgorithmContent;
import com.bluenet.web.domain.repository.AlgorithmJudgeCaseResultRepository;
import com.bluenet.web.domain.repository.AlgorithmJudgeJobRepository;
import com.bluenet.web.domain.service.AssessmentJudgementDomainService;
import com.bluenet.web.domain.repository.AssessmentQuestionRepository;
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
    private final AssessmentQuestionRepository assessmentQuestionRepository;
    private final AssessmentJudgementDomainService assessmentJudgementDomainService;
    private final SandboxExecutor sandboxExecutor;

    @RabbitListener(queues = AlgorithmJudgeQueueConfig.ALGORITHM_JUDGE_QUEUE)
    @Transactional
    public void consume(String judgeJobIdMessage) {
        Long judgeJobId = parseJudgeJobId(judgeJobIdMessage);
        AlgorithmJudgeJob job = algorithmJudgeJobRepository.findById(judgeJobId)
                .orElseThrow(() -> new IllegalStateException("判题任务不存在：" + judgeJobId));
        AssessmentQuestion question = assessmentQuestionRepository.findById(job.getQuestionId())
                .orElseThrow(() -> new IllegalStateException("题目不存在：" + job.getQuestionId()));
        if (!(question.getContent()instanceof AlgorithmContent content)) {
            job.markReviewRequired("算法题内容配置错误");
            algorithmJudgeJobRepository.update(job);
            return;
        }

        job.markRunning();
        algorithmJudgeJobRepository.update(job);
        SandboxExecutionResult result = sandboxExecutor.execute(
                SandboxExecutionRequest.builder()
                        .language(job.getLanguage())
                        .sourceCode(job.getSourceCode())
                        .timeLimitMs(content.getTimeLimit())
                        .memoryLimitKb(content.getMemoryLimit())
                        .testcases(resolveTestcases(job, content))
                        .build());

        if (result.isInfrastructureFailure()) {
            job.markRetryableOrReview(result.getInfrastructureMessage());
            algorithmJudgeJobRepository.update(job);
            return;
        }

        List<AlgorithmJudgeCaseResult> caseResults = result.getCaseResults()
                .stream()
                .map(caseResult -> convertToEntity(job, caseResult))
                .toList();
        algorithmJudgeCaseResultRepository.saveAll(caseResults);
        job.markSucceeded();
        algorithmJudgeJobRepository.update(job);
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

    private List<SandboxTestcase> resolveTestcases(AlgorithmJudgeJob job, AlgorithmContent content) {
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

    private AlgorithmJudgeCaseResult convertToEntity(AlgorithmJudgeJob job, SandboxCaseResult result) {
        return AlgorithmJudgeCaseResult.create(
                job.getId(),
                result.getCaseNo(),
                job.getTestcaseType(),
                result.getStatus(),
                result.getInput(),
                result.getExpectedOutput(),
                result.getActualOutput(),
                result.getStdout(),
                result.getStderr(),
                result.getTimeUsedMs(),
                result.getMemoryUsedKb(),
                result.getMessage(),
                job.getTestcaseType() != AlgorithmTestcaseType.FORMAL
                        || result.getStatus() != JudgeCaseStatus.AC);
    }

    private void createFormalJudgement(
            AlgorithmJudgeJob job,
            AssessmentQuestion question,
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
