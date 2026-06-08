package com.bluenet.web.domain.model.entity;

import com.bluenet.web.domain.model.enumerate.JudgementSource;
import com.bluenet.web.domain.model.enumerate.JudgementStatus;
import com.bluenet.web.domain.model.enumerate.ObjectiveResultCode;
import com.bluenet.web.domain.model.enumerate.ReviewerType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Persists the latest judgement outcome for a submitted assessment answer.
 */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AssessmentJudgement {
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
     * 答案、题目或评审记录在考核中的得分。
     */
    private BigDecimal score;
    /**
     * 题目或评审项可获得的最高分。
     */
    private BigDecimal maxScore;
    /**
     * 当前业务流程、任务或记录的状态。
     */
    private JudgementStatus status;
    /**
     * 算法评测或评审结果编码。
     */
    private ObjectiveResultCode resultCode;
    /**
     * 评审结果来源。
     */
    private JudgementSource source;
    /**
     * 执行评审的用户或系统标识。
     */
    private Long reviewerId;
    /**
     * 评审来源类型，例如人工评审或自动评测。
     */
    private ReviewerType reviewerType;
    /**
     * 评审完成时间。
     */
    private LocalDateTime judgedAt;
    /**
     * 记录创建时间。
     */
    private LocalDateTime createdAt;
    /**
     * 记录最后更新时间。
     */
    private LocalDateTime updatedAt;

    private AssessmentJudgement(Long id, Long answerId, Long questionId, Long assessmentTimeId, Long userId,
            BigDecimal score, BigDecimal maxScore, JudgementStatus status, ObjectiveResultCode resultCode,
            JudgementSource source, Long reviewerId, ReviewerType reviewerType,
            LocalDateTime judgedAt, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.answerId = answerId;
        this.questionId = questionId;
        this.assessmentTimeId = assessmentTimeId;
        this.userId = userId;
        this.score = score;
        this.maxScore = maxScore;
        this.status = status;
        this.resultCode = resultCode;
        this.source = source;
        this.reviewerId = reviewerId;
        this.reviewerType = reviewerType;
        this.judgedAt = judgedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 构造新评判记录 —— 带领域校验
     *
     * @param answerId
     *            作答ID
     * @param questionId
     *            题目ID
     * @param assessmentTimeId
     *            考核时间ID
     * @param userId
     *            用户ID
     * @param score
     *            得分
     * @param maxScore
     *            满分
     * @param status
     *            状态
     * @param resultCode
     *            结果码
     * @param source
     *            来源
     * @param reviewerId
     *            评审人ID
     * @param reviewerType
     *            评审人类型
     * @param judgedAt
     *            评审时间
     * @return 新的评判实体
     */
    public static AssessmentJudgement create(Long answerId, Long questionId, Long assessmentTimeId, Long userId,
            BigDecimal score, BigDecimal maxScore, JudgementStatus status, ObjectiveResultCode resultCode,
            JudgementSource source, Long reviewerId, ReviewerType reviewerType,
            LocalDateTime judgedAt) {
        return new AssessmentJudgement(null, answerId, questionId, assessmentTimeId, userId, score, maxScore, status,
                resultCode, source, reviewerId, reviewerType, judgedAt, null, null);
    }

    /**
     * 从数据库重建 —— 跳过创建校验
     *
     * @param id
     *            评判ID
     * @param answerId
     *            作答ID
     * @param questionId
     *            题目ID
     * @param assessmentTimeId
     *            考核时间ID
     * @param userId
     *            用户ID
     * @param score
     *            得分
     * @param maxScore
     *            满分
     * @param status
     *            状态
     * @param resultCode
     *            结果码
     * @param source
     *            来源
     * @param reviewerId
     *            评审人ID
     * @param reviewerType
     *            评审人类型
     * @param judgedAt
     *            评审时间
     * @param createdAt
     *            创建时间
     * @param updatedAt
     *            更新时间
     * @return 重建的评判实体
     */
    public static AssessmentJudgement reconstruct(Long id, Long answerId, Long questionId, Long assessmentTimeId,
            Long userId, BigDecimal score, BigDecimal maxScore, JudgementStatus status, ObjectiveResultCode resultCode,
            JudgementSource source, Long reviewerId, ReviewerType reviewerType,
            LocalDateTime judgedAt, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new AssessmentJudgement(id, answerId, questionId, assessmentTimeId, userId, score, maxScore, status,
                resultCode, source, reviewerId, reviewerType, judgedAt, createdAt, updatedAt);
    }

    /**
     * 应用评判结果 —— 更新评分和状态字段
     *
     * @param newScore
     *            新得分
     * @param newMaxScore
     *            新满分
     * @param newStatus
     *            新状态
     * @param newResultCode
     *            新结果码
     * @param newSource
     *            新来源
     * @param newReviewerId
     *            新评审人ID
     * @param newReviewerType
     *            新评审人类型
     * @param newJudgedAt
     *            新评审时间
     * @param newUpdatedAt
     *            新更新时间
     */
    public void applyJudgementResult(BigDecimal newScore, BigDecimal newMaxScore, JudgementStatus newStatus,
            ObjectiveResultCode newResultCode, JudgementSource newSource, Long newReviewerId,
            ReviewerType newReviewerType, LocalDateTime newJudgedAt, LocalDateTime newUpdatedAt) {
        this.score = newScore;
        this.maxScore = newMaxScore;
        this.status = newStatus;
        this.resultCode = newResultCode;
        this.source = newSource;
        this.reviewerId = newReviewerId;
        this.reviewerType = newReviewerType;
        this.judgedAt = newJudgedAt;
        this.updatedAt = newUpdatedAt;
    }
}
