package com.bluenet.web.domain.model.entity;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 考核会话聚合根
 * <p>
 * 承载考核会话相关的业务规则和行为
 * </p>
 */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AssessmentSession {
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
     * 经历、考核或有效期的开始时间。
     */
    private LocalDateTime startTime;
    /**
     * 报名、提交或任务处理的截止时间。
     */
    private LocalDateTime deadline;

    private AssessmentSession(Long id, Long userId, Long assessmentTimeId,
            LocalDateTime startTime, LocalDateTime deadline) {
        this.id = id;
        this.userId = userId;
        this.assessmentTimeId = assessmentTimeId;
        this.startTime = startTime;
        this.deadline = deadline;
    }

    /**
     * 构造新聚合根
     */
    public static AssessmentSession create(Long userId, Long assessmentTimeId,
            LocalDateTime startTime, LocalDateTime deadline) {
        return new AssessmentSession(null, userId, assessmentTimeId, startTime, deadline);
    }

    /**
     * 从数据库重建 —— 跳过创建校验
     */
    public static AssessmentSession reconstruct(Long id, Long userId, Long assessmentTimeId,
            LocalDateTime startTime, LocalDateTime deadline) {
        return new AssessmentSession(id, userId, assessmentTimeId, startTime, deadline);
    }
}
