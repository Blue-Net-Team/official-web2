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
