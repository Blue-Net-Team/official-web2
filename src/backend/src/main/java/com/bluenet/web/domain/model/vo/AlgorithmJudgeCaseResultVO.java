package com.bluenet.web.domain.model.vo;

import com.bluenet.web.domain.model.enumerate.AlgorithmTestcaseType;
import com.bluenet.web.domain.model.enumerate.JudgeCaseStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Domain view of one algorithm judge testcase result.
 */
@Data
@Builder
public class AlgorithmJudgeCaseResultVO {
    private Long id;
    private Long judgeJobId;
    private Integer caseNo;
    private AlgorithmTestcaseType testcaseType;
    private JudgeCaseStatus status;
    private String input;
    private String expectedOutput;
    private String actualOutput;
    private String stdout;
    private String stderr;
    private Integer timeUsedMs;
    private Integer memoryUsedKb;
    private String message;
    private Boolean visibleToCandidate;
    private LocalDateTime createdAt;
}
