package com.bluenet.web.application.query.auditstatistics;

import com.bluenet.web.domain.model.enumerate.AuditStatisticsPeriod;

/**
 * 获取接口延迟排行查询参数。
 */
public record GetEndpointLatencyRankingQuery(
        /** 周期 */
        AuditStatisticsPeriod period,
        /** 限制数量 */
        int limit) {
}
