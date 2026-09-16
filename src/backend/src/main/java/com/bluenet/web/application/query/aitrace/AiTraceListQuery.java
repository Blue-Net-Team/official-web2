package com.bluenet.web.application.query.aitrace;

import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

/**
 * AI 对话会话列表查询参数。
 *
 * @param intent
 *            意图筛选，为空表示不限。
 * @param action
 *            动作筛选（RETRIEVE / REFUSE / DIRECT），为空表示不限。
 * @param keyword
 *            用户提问关键词，仅作用于采集时的原始提问文本。
 * @param startTime
 *            会话最后活跃时间下界，为空表示不限。
 * @param endTime
 *            会话最后活跃时间上界，为空表示不限。
 * @param pageable
 *            分页参数。
 */
public record AiTraceListQuery(
        String intent,
        String action,
        String keyword,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Pageable pageable) {
}
