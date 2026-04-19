package com.bluenet.web.domain.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Stores the final pass decision for one candidate in one assessment time.
 */
@Data
@TableName("tb_assessment_decision")
public class AssessmentDecision {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long assessmentTimeId;
    private Boolean passed;
    private Long decidedBy;
    private String decisionComment;
    private LocalDateTime decidedAt;
    private LocalDateTime updatedAt;
}
