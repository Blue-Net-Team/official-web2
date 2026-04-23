package com.bluenet.web.domain.model.entity;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Stores the final pass decision for one candidate in one assessment time.
 */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AssessmentDecision {
    /**
     * 当前对象在系统中的唯一标识。
     */
    private Long id;
    /**
     * 关联用户标识。
     */
    private Long userId;
    /**
     * 所属考核场次或考核时间配置标识。
     */
    private Long assessmentTimeId;
    /**
     * 候选人最终是否通过考核。
     */
    private Boolean passed;
    /**
     * 做出最终决策的管理员用户标识。
     */
    private Long decidedBy;
    /**
     * 最终决策的说明或原因。
     */
    private String decisionComment;
    /**
     * 最终录用或通过决策生成时间。
     */
    private LocalDateTime decidedAt;
    /**
     * 记录最后更新时间。
     */
    private LocalDateTime updatedAt;

    private AssessmentDecision(Long id, Long userId, Long assessmentTimeId, Boolean passed, Long decidedBy,
            String decisionComment, LocalDateTime decidedAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.assessmentTimeId = assessmentTimeId;
        this.passed = passed;
        this.decidedBy = decidedBy;
        this.decisionComment = decisionComment;
        this.decidedAt = decidedAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 构造新考核决策聚合根
     *
     * @param userId
     *            关联用户标识
     * @param assessmentTimeId
     *            所属考核场次标识
     * @param passed
     *            是否通过
     * @param decidedBy
     *            决策人标识
     * @param decisionComment
     *            决策说明
     * @return 新的考核决策实体
     */
    public static AssessmentDecision create(Long userId, Long assessmentTimeId, Boolean passed,
            Long decidedBy, String decisionComment) {
        return new AssessmentDecision(null, userId, assessmentTimeId, passed, decidedBy,
                decisionComment, null, null);
    }

    /**
     * 从数据库重建 —— 跳过创建校验
     *
     * @param id
     *            决策ID
     * @param userId
     *            关联用户标识
     * @param assessmentTimeId
     *            所属考核场次标识
     * @param passed
     *            是否通过
     * @param decidedBy
     *            决策人标识
     * @param decisionComment
     *            决策说明
     * @param decidedAt
     *            决策时间
     * @param updatedAt
     *            更新时间
     * @return 重建的考核决策实体
     */
    public static AssessmentDecision reconstruct(Long id, Long userId, Long assessmentTimeId, Boolean passed,
            Long decidedBy, String decisionComment,
            LocalDateTime decidedAt, LocalDateTime updatedAt) {
        return new AssessmentDecision(id, userId, assessmentTimeId, passed, decidedBy,
                decisionComment, decidedAt, updatedAt);
    }
}
