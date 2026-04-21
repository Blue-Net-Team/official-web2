package com.bluenet.web.infrastructure.repository.projection;

import com.bluenet.web.domain.model.enumerate.QuestionType;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 题目评分汇总查询投影，仅用于仓储查询映射。
 */
@Data
public class AssessmentQuestionScoreboardProjection {
    /**
     * 考核题目标识。
     */
    private Long questionId;
    /**
     * 所属考核场次或考核时间配置标识。
     */
    private Long assessmentTimeId;
    /**
     * 题目在考核场次中的序号。
     */
    private Integer questionNo;
    /**
     * 题目类型，例如算法题、单选题或多选题。
     */
    private QuestionType questionType;
    /**
     * 标题或名称，用于列表和详情展示。
     */
    private String title;
    /**
     * 题目或评审项可获得的最高分。
     */
    private BigDecimal maxScore;
    /**
     * 已提交答案的候选人数量。
     */
    private Long submittedCount;
    /**
     * 已完成评审的提交数量。
     */
    private Long judgedCount;
    /**
     * 等待评审或待处理记录数量。
     */
    private Long pendingCount;
    /**
     * 已评审记录的平均得分。
     */
    private BigDecimal averageScore;
}
