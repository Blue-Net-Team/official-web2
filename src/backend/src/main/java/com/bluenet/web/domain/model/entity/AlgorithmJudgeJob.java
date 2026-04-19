package com.bluenet.web.domain.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bluenet.web.domain.model.enumerate.AlgorithmTestcaseType;
import com.bluenet.web.domain.model.enumerate.JudgeJobStatus;
import com.bluenet.web.domain.model.enumerate.ProgrammingLanguage;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Tracks algorithm run and formal submission jobs at queue/worker level.
 */
@Data
@TableName("tb_algorithm_judge_job")
public class AlgorithmJudgeJob {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long answerId;
    private Long questionId;
    private Long assessmentTimeId;
    private Long userId;
    private ProgrammingLanguage language;
    private String sourceCode;
    private AlgorithmTestcaseType testcaseType;
    private String customInput;
    private JudgeJobStatus status;
    private Integer retryCount;
    private Integer maxRetryCount;
    private String statusMessage;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
