package com.bluenet.judge.infrastructure.repository.dataobject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 写入单个判题用例结果的数据载体。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JudgeCaseResultWrite {
    /** 判题任务主键。 */
    private Long judgeJobId;
    /** 测试用例序号。 */
    private Integer caseNo;
    /** 判题用例类型。 */
    private String testcaseType;
    /** 用例结果码。 */
    private String status;
    /** 用例输入内容。 */
    private String input;
    /** 期望输出内容。 */
    private String expectedOutput;
    /** 实际输出内容。 */
    private String actualOutput;
    /** 标准输出。 */
    private String stdout;
    /** 标准错误。 */
    private String stderr;
    /** 耗时，单位毫秒。 */
    private Integer timeUsedMs;
    /** 内存，单位 KB。 */
    private Integer memoryUsedKb;
    /** 结果说明。 */
    private String message;
    /** 是否对候选人可见。 */
    private Boolean visibleToCandidate;
}
