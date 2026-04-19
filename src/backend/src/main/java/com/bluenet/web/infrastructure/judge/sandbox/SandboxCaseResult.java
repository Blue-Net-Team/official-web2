package com.bluenet.web.infrastructure.judge.sandbox;

import com.bluenet.web.domain.model.enumerate.JudgeCaseStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SandboxCaseResult {
    private Integer caseNo;
    private JudgeCaseStatus status;
    private String input;
    private String expectedOutput;
    private String actualOutput;
    private String stdout;
    private String stderr;
    private Integer timeUsedMs;
    private Integer memoryUsedKb;
    private String message;
}
