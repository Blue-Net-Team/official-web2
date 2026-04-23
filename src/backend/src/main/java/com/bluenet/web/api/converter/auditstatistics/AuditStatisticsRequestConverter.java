package com.bluenet.web.api.converter.auditstatistics;

import com.bluenet.web.application.command.auditstatistics.AuditStatisticsCommands;
import com.bluenet.web.domain.model.enumerate.AuditStatisticsPeriod;
import org.springframework.stereotype.Component;

/**
 * 审计统计请求转换器
 * <p>
 * 负责将 API 层的请求参数转换为应用层的 Command
 * </p>
 */
@Component
public class AuditStatisticsRequestConverter {

    public AuditStatisticsCommands.GetTrendsCommand toTrendsCommand(AuditStatisticsPeriod period) {
        return new AuditStatisticsCommands.GetTrendsCommand(period);
    }

    public AuditStatisticsCommands.GetEndpointRankingCommand toEndpointRankingCommand(AuditStatisticsPeriod period,
            int limit) {
        return new AuditStatisticsCommands.GetEndpointRankingCommand(period, limit);
    }

    public AuditStatisticsCommands.GetEndpointLatencyRankingCommand toEndpointLatencyRankingCommand(
            AuditStatisticsPeriod period, int limit) {
        return new AuditStatisticsCommands.GetEndpointLatencyRankingCommand(period, limit);
    }
}
