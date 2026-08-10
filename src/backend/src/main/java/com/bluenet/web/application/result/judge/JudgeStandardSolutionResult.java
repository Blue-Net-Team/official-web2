package com.bluenet.web.application.result.judge;

/**
 * 判题标准解应用层结果。
 *
 * @param language
 *            标准解语言
 * @param objectKey
 *            OSS 对象键
 * @param source
 *            源码内容
 * @param objectHash
 *            对象哈希
 * @param primarySolution
 *            是否为主标准解
 * @param benchmarkStatus
 *            benchmark 状态
 * @param p95TimeMs
 *            p95 耗时
 * @param maxTimeMs
 *            最大耗时
 * @param peakMemoryKb
 *            峰值内存
 * @param suggestedTimeLimitMs
 *            建议时限
 * @param benchmarkMessage
 *            benchmark 消息
 */
public record JudgeStandardSolutionResult(
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
