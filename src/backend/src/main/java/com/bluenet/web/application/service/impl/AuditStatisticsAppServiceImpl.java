package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.AuditStatisticsResult;
import com.bluenet.web.application.command.auditstatistics.AuditStatisticsCommands;
import com.bluenet.web.application.service.AuditStatisticsAppService;
import com.bluenet.web.domain.model.enumerate.AuditStatisticsPeriod;
import com.bluenet.web.domain.model.vo.AuditEndpointLatencyVO;
import com.bluenet.web.domain.model.vo.AuditEndpointRankingVO;
import com.bluenet.web.domain.model.vo.AuditTrendPointVO;
import com.bluenet.web.domain.repository.AuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 审计统计应用服务实现。
 * <p>
 * 实现审计统计聚合在应用层的业务逻辑编排。
 * </p>
 */
@Service
@RequiredArgsConstructor
public class AuditStatisticsAppServiceImpl implements AuditStatisticsAppService {

    private final AuditRepository auditRepository;

    /**
     * 查询趋势统计。
     *
     * @param command
     *            查询趋势命令
     * @return 趋势点列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<AuditStatisticsResult.TrendPoint> getTrends(AuditStatisticsCommands.GetTrendsCommand command) {
        AuditStatisticsPeriod period = AuditStatisticsPeriod.valueOf(command.period().toUpperCase());
        return auditRepository.queryTrends(period)
                .stream()
                .map(this::toTrendPoint)
                .toList();
    }

    /**
     * 查询端点排行。
     *
     * @param command
     *            查询端点排行命令
     * @return 端点排行列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<AuditStatisticsResult.EndpointRanking> getEndpointRanking(
            AuditStatisticsCommands.GetEndpointRankingCommand command) {
        AuditStatisticsPeriod period = AuditStatisticsPeriod.valueOf(command.period().toUpperCase());
        return auditRepository.queryEndpointRanking(period, command.limit())
                .stream()
                .map(this::toEndpointRanking)
                .toList();
    }

    /**
     * 查询端点延迟排行。
     *
     * @param command
     *            查询端点延迟命令
     * @return 端点延迟排行列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<AuditStatisticsResult.EndpointLatency> getEndpointLatencyRanking(
            AuditStatisticsCommands.GetEndpointLatencyRankingCommand command) {
        AuditStatisticsPeriod period = AuditStatisticsPeriod.valueOf(command.period().toUpperCase());
        return auditRepository.queryEndpointLatencyRanking(period, command.limit())
                .stream()
                .map(this::toEndpointLatency)
                .toList();
    }

    private AuditStatisticsResult.TrendPoint toTrendPoint(AuditTrendPointVO vo) {
        return new AuditStatisticsResult.TrendPoint(vo.getTime(), vo.getCount());
    }

    private AuditStatisticsResult.EndpointRanking toEndpointRanking(AuditEndpointRankingVO vo) {
        return new AuditStatisticsResult.EndpointRanking(vo.getPattern(), vo.getCount(), vo.getAvgDurationMs(),
                vo.getErrorCount());
    }

    private AuditStatisticsResult.EndpointLatency toEndpointLatency(AuditEndpointLatencyVO vo) {
        return new AuditStatisticsResult.EndpointLatency(vo.getPattern(), vo.getAvgDurationMs(), vo.getMaxDurationMs(),
                vo.getCount());
    }
}
