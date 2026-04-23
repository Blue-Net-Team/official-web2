package com.bluenet.web.domain.model.entity;

import com.bluenet.web.domain.model.enumerate.AlgorithmTestcaseType;
import com.bluenet.web.domain.model.enumerate.JudgeJobStatus;
import com.bluenet.web.domain.model.enumerate.ProgrammingLanguage;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Tracks algorithm run and formal submission jobs at queue/worker level.
 */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AlgorithmJudgeJob {
    /**
     * 当前对象在系统中的唯一标识。
     */
    private Long id;
    /**
     * 考核作答记录标识。
     */
    private Long answerId;
    /**
     * 考核题目标识。
     */
    private Long questionId;
    /**
     * 所属考核场次或考核时间配置标识。
     */
    private Long assessmentTimeId;
    /**
     * 关联用户标识。
     */
    private Long userId;
    /**
     * 提交代码使用的编程语言。
     */
    private ProgrammingLanguage language;
    /**
     * 候选人提交的算法源代码。
     */
    private String sourceCode;
    /**
     * 评测用例类型，例如示例、自测或正式用例。
     */
    private AlgorithmTestcaseType testcaseType;
    /**
     * 候选人自定义运行算法代码时输入的数据。
     */
    private String customInput;
    /**
     * 当前业务流程、任务或记录的状态。
     */
    private JudgeJobStatus status;
    /**
     * 评测任务已经重试的次数。
     */
    private Integer retryCount;
    /**
     * 评测任务允许的最大重试次数。
     */
    private Integer maxRetryCount;
    /**
     * 任务状态的补充说明。
     */
    private String statusMessage;
    /**
     * 任务开始执行时间。
     */
    private LocalDateTime startedAt;
    /**
     * 任务执行完成时间。
     */
    private LocalDateTime finishedAt;
    /**
     * 记录创建时间。
     */
    private LocalDateTime createdAt;
    /**
     * 记录最后更新时间。
     */
    private LocalDateTime updatedAt;

    private AlgorithmJudgeJob(
            Long id,
            Long answerId,
            Long questionId,
            Long assessmentTimeId,
            Long userId,
            ProgrammingLanguage language,
            String sourceCode,
            AlgorithmTestcaseType testcaseType,
            String customInput,
            JudgeJobStatus status,
            Integer retryCount,
            Integer maxRetryCount,
            String statusMessage,
            LocalDateTime startedAt,
            LocalDateTime finishedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.id = id;
        this.answerId = answerId;
        this.questionId = questionId;
        this.assessmentTimeId = assessmentTimeId;
        this.userId = userId;
        this.language = language;
        this.sourceCode = sourceCode;
        this.testcaseType = testcaseType;
        this.customInput = customInput;
        this.status = status;
        this.retryCount = retryCount;
        this.maxRetryCount = maxRetryCount;
        this.statusMessage = statusMessage;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 构造新算法评测任务 —— 带领域校验
     */
    public static AlgorithmJudgeJob create(
            Long answerId,
            Long questionId,
            Long assessmentTimeId,
            Long userId,
            ProgrammingLanguage language,
            String sourceCode,
            AlgorithmTestcaseType testcaseType,
            String customInput) {
        if (language == null) {
            throw new IllegalArgumentException("编程语言不能为空");
        }
        if (sourceCode == null || sourceCode.isBlank()) {
            throw new IllegalArgumentException("源代码不能为空");
        }
        return new AlgorithmJudgeJob(
                null,
                answerId,
                questionId,
                assessmentTimeId,
                userId,
                language,
                sourceCode,
                testcaseType,
                customInput,
                JudgeJobStatus.PENDING,
                0,
                3,
                "等待判题",
                null,
                null,
                null,
                null);
    }

    /**
     * 从数据库重建 —— 跳过创建校验
     */
    public static AlgorithmJudgeJob reconstruct(
            Long id,
            Long answerId,
            Long questionId,
            Long assessmentTimeId,
            Long userId,
            ProgrammingLanguage language,
            String sourceCode,
            AlgorithmTestcaseType testcaseType,
            String customInput,
            JudgeJobStatus status,
            Integer retryCount,
            Integer maxRetryCount,
            String statusMessage,
            LocalDateTime startedAt,
            LocalDateTime finishedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        return new AlgorithmJudgeJob(
                id,
                answerId,
                questionId,
                assessmentTimeId,
                userId,
                language,
                sourceCode,
                testcaseType,
                customInput,
                status,
                retryCount,
                maxRetryCount,
                statusMessage,
                startedAt,
                finishedAt,
                createdAt,
                updatedAt);
    }

    /**
     * 标记任务为运行中
     */
    public void markRunning() {
        this.status = JudgeJobStatus.RUNNING;
        this.startedAt = LocalDateTime.now();
        this.statusMessage = "正在判题";
    }

    /**
     * 标记任务为成功完成
     */
    public void markSucceeded() {
        this.status = JudgeJobStatus.SUCCEEDED;
        this.finishedAt = LocalDateTime.now();
        this.statusMessage = "判题完成";
    }

    /**
     * 标记任务为需要重试或人工复核
     */
    public void markRetryableOrReview(String message) {
        int currentRetry = this.retryCount == null ? 0 : this.retryCount;
        int maxRetry = this.maxRetryCount == null ? 3 : this.maxRetryCount;
        this.retryCount = currentRetry + 1;
        this.status = currentRetry + 1 >= maxRetry ? JudgeJobStatus.FAILED_REVIEW_REQUIRED : JudgeJobStatus.RETRYING;
        this.statusMessage = message;
    }

    /**
     * 标记任务为需要人工复核
     */
    public void markReviewRequired(String message) {
        this.status = JudgeJobStatus.FAILED_REVIEW_REQUIRED;
        this.statusMessage = message;
    }
}
