package com.bluenet.web.infrastructure.repository.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bluenet.web.domain.model.enumerate.JudgementSource;
import com.bluenet.web.domain.model.enumerate.JudgementStatus;
import com.bluenet.web.domain.model.enumerate.ObjectiveResultCode;
import com.bluenet.web.domain.model.enumerate.ReviewerType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mapper 专用数据对象，只承载数据库表字段，避免持久层依赖领域实体行为。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_assessment_judgement")
public class AssessmentJudgementDO {
    /**
     * 当前对象在系统中的唯一标识。
     */
    @TableId(type = IdType.AUTO)
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
     * 评价、留言或审批备注内容。
     */
    private String comment;

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
}
