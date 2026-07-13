package com.bluenet.web.domain.model.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 算法题标准解领域实体。
 */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class JudgeStandardSolution {
    /**
     * 标准解主键。
     */
    private Long id;
    /**
     * 判题配置主键。
     */
    private Long configId;
    /**
     * 算法题目主键。
     */
    private Long questionId;
    /**
     * 标准解语言。
     */
    private String language;
    /**
     * 标准解 OSS 对象键。
     */
    private String objectKey;
    /**
     * 标准解 SHA-256 哈希。
     */
    private String objectHash;
    /**
     * 是否为生成标准输出的主标准解。
     */
    private Boolean primarySolution;
    /**
     * benchmark 状态。
     */
    private String benchmarkStatus;
    /**
     * benchmark p95 耗时。
     */
    private Integer p95TimeMs;
    /**
     * benchmark 最大耗时。
     */
    private Integer maxTimeMs;
    /**
     * benchmark 峰值内存。
     */
    private Integer peakMemoryKb;
    /**
     * 建议正式判题限时。
     */
    private Integer suggestedTimeLimitMs;
    /**
     * benchmark 说明。
     */
    private String benchmarkMessage;

    public static JudgeStandardSolution create(
            Long configId,
            Long questionId,
            String language,
            String objectKey,
            String objectHash,
            Boolean primarySolution,
            String benchmarkStatus) {
        return new JudgeStandardSolution(
                null,
                configId,
                questionId,
                language,
                objectKey,
                objectHash,
                primarySolution,
                benchmarkStatus,
                null,
                null,
                null,
                null,
                null);
    }

    public static JudgeStandardSolution reconstruct(
            Long id,
            Long configId,
            Long questionId,
            String language,
            String objectKey,
            String objectHash,
            Boolean primarySolution,
            String benchmarkStatus,
            Integer p95TimeMs,
            Integer maxTimeMs,
            Integer peakMemoryKb,
            Integer suggestedTimeLimitMs,
            String benchmarkMessage) {
        return new JudgeStandardSolution(
                id,
                configId,
                questionId,
                language,
                objectKey,
                objectHash,
                primarySolution,
                benchmarkStatus,
                p95TimeMs,
                maxTimeMs,
                peakMemoryKb,
                suggestedTimeLimitMs,
                benchmarkMessage);
    }
}
