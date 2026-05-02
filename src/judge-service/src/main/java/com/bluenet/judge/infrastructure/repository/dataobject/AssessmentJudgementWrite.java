package com.bluenet.judge.infrastructure.repository.dataobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 自动评判结果写入对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentJudgementWrite {
    /** 正式提交答案主键。 */
    private Long answerId;
    /** 算法题目主键。 */
    private Long questionId;
    /** 考核时间主键。 */
    private Long assessmentTimeId;
    /** 提交用户主键。 */
    private Long userId;
    /** 本次自动评判得分。 */
    private BigDecimal score;
    /** 题目满分。 */
    private BigDecimal maxScore;
    /** 评判状态。 */
    private String status;
    /** 客观结果码。 */
    private String resultCode;
    /** 评判来源。 */
    private String source;
    /** 评判人类型。 */
    private String reviewerType;
    /** 评判说明。 */
    private String comment;
}
