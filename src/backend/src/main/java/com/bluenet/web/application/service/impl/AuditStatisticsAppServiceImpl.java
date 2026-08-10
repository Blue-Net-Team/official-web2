package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.query.auditstatistics.GetEndpointLatencyRankingQuery;
import com.bluenet.web.application.query.auditstatistics.GetEndpointRankingQuery;
import com.bluenet.web.application.query.auditstatistics.GetTrendsQuery;
import com.bluenet.web.application.service.AuditStatisticsAppService;
import com.bluenet.web.application.result.audit.AuditEndpointLatency;
import com.bluenet.web.application.result.audit.AuditEndpointRanking;
import com.bluenet.web.application.result.audit.AuditTrendPoint;
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
     * @param query
     *            查询趋势参数
     * @return 趋势点列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<AuditTrendPoint> getTrends(GetTrendsQuery query) {
        return auditRepository.queryTrends(query.period());
    }

    /**
     * 查询端点排行。
     *
     * @param query
     *            查询端点排行参数
     * @return 端点排行列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<AuditEndpointRanking> getEndpointRanking(
            GetEndpointRankingQuery query) {
        return auditRepository.queryEndpointRanking(query.period(), query.limit());
    }

    /**
     * 查询端点延迟排行。
     *
     * @param query
     *            查询端点延迟参数
     * @return 端点延迟排行列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<AuditEndpointLatency> getEndpointLatencyRanking(
            GetEndpointLatencyRankingQuery query) {
        return auditRepository.queryEndpointLatencyRanking(query.period(), query.limit());
    }
}
