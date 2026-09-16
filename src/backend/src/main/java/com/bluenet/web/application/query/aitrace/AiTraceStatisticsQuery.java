package com.bluenet.web.application.query.aitrace;

import com.bluenet.web.domain.model.enumerate.AiTraceStatisticsPeriod;

/**
 * AI 对话统计查询参数。
 *
 * @param period
 *            统计周期。
 */
public record AiTraceStatisticsQuery(
        AiTraceStatisticsPeriod period) {
}
