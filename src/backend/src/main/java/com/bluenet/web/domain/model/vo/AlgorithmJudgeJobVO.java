package com.bluenet.web.domain.model.vo;

import com.bluenet.web.domain.model.enumerate.AlgorithmTestcaseType;
import com.bluenet.web.domain.model.enumerate.JudgeJobStatus;
import com.bluenet.web.domain.model.enumerate.ProgrammingLanguage;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Domain view of an algorithm judge job.
 */
@Data
@Builder
public class AlgorithmJudgeJobVO {
    /**
     * 当前对象在系统中的唯一标识。
     */
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
     * 提交代码使用的编程语言。
     */
    private ProgrammingLanguage language;
    /**
     * 候选人提交的算法源代码。
     */
    private String sourceCode;
    /**
     * 评测用例类型，例如示例、自测或正式用例。
     */
    private AlgorithmTestcaseType testcaseType;
    /**
     * 候选人自定义运行算法代码时输入的数据。
     */
    private String customInput;
    /**
     * 当前业务流程、任务或记录的状态。
     */
    private JudgeJobStatus status;
    /**
     * 评测任务已经重试的次数。
     */
    private Integer retryCount;
    /**
     * 评测任务允许的最大重试次数。
     */
    private Integer maxRetryCount;
    /**
     * 任务状态的补充说明。
     */
    private String statusMessage;
    /**
     * 任务开始执行时间。
     */
    private LocalDateTime startedAt;
    /**
     * 任务执行完成时间。
     */
    private LocalDateTime finishedAt;
    /**
     * 记录创建时间。
     */
    private LocalDateTime createdAt;
    /**
     * 记录最后更新时间。
     */
    private LocalDateTime updatedAt;
}
