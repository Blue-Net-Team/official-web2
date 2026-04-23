package com.bluenet.web.application.service;

import com.bluenet.web.application.AuditStatisticsResult;
import com.bluenet.web.application.command.auditstatistics.AuditStatisticsCommands;

import java.util.List;

/**
 * 审计统计应用服务接口。
 * <p>
 * 定义了审计统计聚合在应用层的所有业务操作。
 * </p>
 */
public interface AuditStatisticsAppService {

    /**
     * 获取审计趋势统计。
     *
     * @param command
     *            查询趋势命令
     * @return 趋势点列表
     */
    List<AuditStatisticsResult.TrendPoint> getTrends(AuditStatisticsCommands.GetTrendsCommand command);

    /**
     * 获取接口调用排行。
     *
     * @param command
     *            查询排行命令
     * @return 接口调用排行列表
     */
    List<AuditStatisticsResult.EndpointRanking> getEndpointRanking(
            AuditStatisticsCommands.GetEndpointRankingCommand command);

    /**
     * 获取接口延迟排行。
     *
     * @param command
     *            查询延迟排行命令
     * @return 接口延迟排行列表
     */
    List<AuditStatisticsResult.EndpointLatency> getEndpointLatencyRanking(
            AuditStatisticsCommands.GetEndpointLatencyRankingCommand command);
}
