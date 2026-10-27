package com.bluenet.web.application.result.judge;

import java.time.LocalDateTime;

/**
 * 语言资源限制应用层结果。
 *
 * @param language
 *            编程语言值
 * @param timeLimitMs
 *            时间限制，单位毫秒
 * @param memoryLimitKb
 *            内存限制，单位 KB
 * @param outputLimitKb
 *            输出限制，单位 KB
 * @param confirmed
 *            是否已确认
 * @param confirmedAt
 *            确认时间
 */
public record JudgeLanguageLimitResult(
        String language,
        Integer timeLimitMs,
        Integer memoryLimitKb,
        Integer outputLimitKb,
        Boolean confirmed,
        LocalDateTime confirmedAt) {
}
