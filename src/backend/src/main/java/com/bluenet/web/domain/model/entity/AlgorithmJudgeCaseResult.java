package com.bluenet.web.domain.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bluenet.web.domain.model.enumerate.AlgorithmTestcaseType;
import com.bluenet.web.domain.model.enumerate.JudgeCaseStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Stores the execution result for a single algorithm testcase.
 */
@Data
@TableName("tb_algorithm_judge_case_result")
public class AlgorithmJudgeCaseResult {
    @TableId(type = IdType.AUTO)
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
