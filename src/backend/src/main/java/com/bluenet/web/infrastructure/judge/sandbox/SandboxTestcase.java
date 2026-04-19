package com.bluenet.web.infrastructure.judge.sandbox;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SandboxTestcase {
    private Integer caseNo;
    private String input;
    private String expectedOutput;
}
