package com.bluenet.web.domain.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Stores the final pass decision for one candidate in one assessment time.
 */
@Data
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
}
