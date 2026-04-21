package com.bluenet.web.domain.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
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
}
