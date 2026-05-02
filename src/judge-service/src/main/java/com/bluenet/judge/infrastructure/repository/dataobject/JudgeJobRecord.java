package com.bluenet.judge.infrastructure.repository.dataobject;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 判题任务数据记录。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JudgeJobRecord {
    /**
     * 判题任务主键。
     */
    private Long id;
    /**
     * 正式提交答案主键。
     */
    private Long answerId;
    /**
     * 算法题目主键。
     */
    private Long questionId;
    /**
     * 考核时间主键。
     */
    private Long assessmentTimeId;
    /**
     * 提交用户主键。
     */
    private Long userId;
    /**
     * 提交语言。
     */
    private String language;
    /**
     * 入队时保存的源代码快照。
     */
    private String sourceCode;
    /**
     * 自定义输入内容（运行判题时使用）。
     */
    private String customInput;
    /**
     * 判题用例类型。
     */
    private String testcaseType;
    /**
     * 任务状态。
     */
    private String status;
    /**
     * 当前重试次数。
     */
    private Integer retryCount;
    /**
     * 最大重试次数。
     */
    private Integer maxRetryCount;
    /**
     * 任务创建时间。
     */
    private LocalDateTime createdAt;
    /**
     * 题目满分。
     */
    private BigDecimal questionMaxScore;
}
