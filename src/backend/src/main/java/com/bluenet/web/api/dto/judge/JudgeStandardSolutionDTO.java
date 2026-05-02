package com.bluenet.web.api.dto.judge;

/**
 * 管理端标准解元数据和 benchmark 结果响应。
 *
 * @param language
 *            标准解语言。
 * @param objectKey
 *            标准解源码在判题 bucket 中的 OSS 对象键。
 * @param objectHash
 *            标准解源码 SHA-256 哈希。
 * @param primarySolution
 *            是否为用于生成标准输出的主标准解。
 * @param benchmarkStatus
 *            benchmark 状态，例如 PENDING、RUNNING、SUCCEEDED、FAILED。
 * @param p95TimeMs
 *            多次 benchmark 的 p95 耗时，单位毫秒。
 * @param maxTimeMs
 *            多次 benchmark 的最大耗时，单位毫秒。
 * @param peakMemoryKb
 *            benchmark 过程中记录的峰值内存，单位 KB。
 * @param suggestedTimeLimitMs
 *            根据标准解耗时和配置公式推导出的建议时限，单位毫秒。
 * @param benchmarkMessage
 *            benchmark 状态说明或失败原因。
 */
public record JudgeStandardSolutionDTO(
        String language,
        String objectKey,
        String source,
        String objectHash,
        Boolean primarySolution,
        String benchmarkStatus,
        Integer p95TimeMs,
        Integer maxTimeMs,
        Integer peakMemoryKb,
        Integer suggestedTimeLimitMs,
        String benchmarkMessage) {
}
