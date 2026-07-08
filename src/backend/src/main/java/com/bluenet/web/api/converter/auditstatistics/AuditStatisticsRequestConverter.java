package com.bluenet.web.api.converter.auditstatistics;

import com.bluenet.web.application.query.auditstatistics.GetEndpointLatencyRankingQuery;
import com.bluenet.web.application.query.auditstatistics.GetEndpointRankingQuery;
import com.bluenet.web.application.query.auditstatistics.GetTrendsQuery;
import com.bluenet.web.domain.model.enumerate.AuditStatisticsPeriod;
import org.springframework.stereotype.Component;

/**
 * 审计统计请求转换器
 * <p>
 * 负责将 API 层的请求参数转换为应用层的 Query
 * </p>
 */
@Component
public class AuditStatisticsRequestConverter {

    public GetTrendsQuery toTrendsQuery(AuditStatisticsPeriod period) {
        return new GetTrendsQuery(period);
    }

    public GetEndpointRankingQuery toEndpointRankingQuery(AuditStatisticsPeriod period,
            int limit) {
        return new GetEndpointRankingQuery(period, limit);
    }

    public GetEndpointLatencyRankingQuery toEndpointLatencyRankingQuery(
            AuditStatisticsPeriod period, int limit) {
        return new GetEndpointLatencyRankingQuery(period, limit);
    }
}
