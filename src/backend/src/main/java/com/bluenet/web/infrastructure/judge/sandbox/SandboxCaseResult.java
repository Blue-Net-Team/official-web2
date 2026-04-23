package com.bluenet.web.infrastructure.judge.sandbox;

import com.bluenet.web.domain.model.enumerate.JudgeCaseStatus;
import lombok.Builder;
import lombok.Data;

/**
 * 沙箱单用例执行结果。
 * <p>
 * 封装了算法评测中单个测试用例的执行结果数据。
 * </p>
 */
@Data
@Builder
public class SandboxCaseResult {
    /** 用例编号 */
    private Integer caseNo;
    /** 用例执行状态 */
    private JudgeCaseStatus status;
    /** 输入数据 */
    private String input;
    /** 期望输出 */
    private String expectedOutput;
    /** 实际输出 */
    private String actualOutput;
    /** 标准输出 */
    private String stdout;
    /** 标准错误 */
    private String stderr;
    /** 耗时（毫秒） */
    private Integer timeUsedMs;
    /** 内存使用（KB） */
    private Integer memoryUsedKb;
    /** 执行消息 */
    private String message;
}
