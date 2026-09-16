package com.bluenet.web.application.query.aitrace;

import org.springframework.data.domain.Pageable;

/**
 * AI 对话缺口查询参数。
 *
 * @param gapType
 *            缺口类型：CLARIFY（未听懂）、REFUSE（被拒答）、FALLBACK（标签未覆盖触发兜底检索）。
 * @param pageable
 *            分页参数。
 */
public record AiTraceGapQuery(
        String gapType,
        Pageable pageable) {
}
