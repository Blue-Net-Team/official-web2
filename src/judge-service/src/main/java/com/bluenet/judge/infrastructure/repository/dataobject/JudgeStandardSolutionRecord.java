package com.bluenet.judge.infrastructure.repository.dataobject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 标准解文件记录。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JudgeStandardSolutionRecord {
    /** 标准解主键。 */
    private Long id;
    /** 判题配置主键。 */
    private Long configId;
    /** 算法题目主键。 */
    private Long questionId;
    /** 标准解语言。 */
    private String language;
    /** 标准解对象键。 */
    private String objectKey;
    /** 标准解 SHA-256 哈希。 */
    private String objectHash;
    /** 是否为主标准解。 */
    private Boolean primarySolution;
    /** benchmark 状态。 */
    private String benchmarkStatus;
    /** 多次运行 p95 耗时毫秒。 */
    private Integer p95TimeMs;
    /** 多次运行最大耗时毫秒。 */
    private Integer maxTimeMs;
    /** 峰值内存 KB。 */
    private Integer peakMemoryKb;
    /** 根据公式推导的建议限时毫秒。 */
    private Integer suggestedTimeLimitMs;
    /** benchmark 结果说明。 */
    private String benchmarkMessage;
}
