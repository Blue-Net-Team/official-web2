package com.bluenet.web.infrastructure.judge.sandbox;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SandboxExecutionResult {
    private List<SandboxCaseResult> caseResults;
    private boolean infrastructureFailure;
    private String infrastructureMessage;
}
