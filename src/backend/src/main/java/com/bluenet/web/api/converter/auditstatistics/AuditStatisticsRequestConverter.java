package com.bluenet.web.api.converter.auditstatistics;

import com.bluenet.web.application.command.auditstatistics.AuditStatisticsCommands;
import org.springframework.stereotype.Component;

/**
 * 审计统计请求转换器
 * <p>
 * 负责将 API 层的请求参数转换为应用层的 Command
 * </p>
 */
@Component
public class AuditStatisticsRequestConverter {

    public AuditStatisticsCommands.GetTrendsCommand toTrendsCommand(String period) {
        return new AuditStatisticsCommands.GetTrendsCommand(period);
    }

    public AuditStatisticsCommands.GetEndpointRankingCommand toEndpointRankingCommand(String period, int limit) {
        return new AuditStatisticsCommands.GetEndpointRankingCommand(period, limit);
    }

    public AuditStatisticsCommands.GetEndpointLatencyRankingCommand toEndpointLatencyRankingCommand(String period,
            int limit) {
        return new AuditStatisticsCommands.GetEndpointLatencyRankingCommand(period, limit);
    }
}
