package com.bluenet.web.infrastructure.judge.sandbox;

import com.bluenet.web.domain.model.enumerate.ProgrammingLanguage;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SandboxExecutionRequest {
    private ProgrammingLanguage language;
    private String sourceCode;
    private Integer timeLimitMs;
    private Integer memoryLimitKb;
    private List<SandboxTestcase> testcases;
}
