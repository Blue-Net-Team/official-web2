package com.bluenet.web.api.dto.judge;

import java.time.LocalDateTime;

/**
 * 管理端语言资源限制响应。
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
public record JudgeLanguageLimitDTO(
        String language,
        Integer timeLimitMs,
        Integer memoryLimitKb,
        Integer outputLimitKb,
        Boolean confirmed,
        LocalDateTime confirmedAt) {
}
