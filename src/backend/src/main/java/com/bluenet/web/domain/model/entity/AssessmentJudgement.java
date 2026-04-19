package com.bluenet.web.domain.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bluenet.web.domain.model.enumerate.JudgementSource;
import com.bluenet.web.domain.model.enumerate.JudgementStatus;
import com.bluenet.web.domain.model.enumerate.ObjectiveResultCode;
import com.bluenet.web.domain.model.enumerate.ReviewerType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Persists the latest judgement outcome for a submitted assessment answer.
 */
@Data
@TableName("tb_assessment_judgement")
public class AssessmentJudgement {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long answerId;
    private Long questionId;
    private Long assessmentTimeId;
    private Long userId;
    private BigDecimal score;
    private BigDecimal maxScore;
    private JudgementStatus status;
    private ObjectiveResultCode resultCode;
    private JudgementSource source;
    private Long reviewerId;
    private ReviewerType reviewerType;
    private String comment;
    private LocalDateTime judgedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
