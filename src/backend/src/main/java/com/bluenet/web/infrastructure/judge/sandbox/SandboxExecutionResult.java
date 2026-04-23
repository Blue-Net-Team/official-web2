package com.bluenet.web.infrastructure.judge.sandbox;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 沙箱批量执行结果。
 * <p>
 * 封装了算法评测中所有测试用例的执行结果及基础设施状态。
 * </p>
 */
@Data
@Builder
public class SandboxExecutionResult {
    /** 各用例执行结果列表 */
    private List<SandboxCaseResult> caseResults;
    /** 是否发生基础设施故障 */
    private boolean infrastructureFailure;
    /** 基础设施故障消息 */
    private String infrastructureMessage;
}
