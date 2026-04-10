package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.audit.EndpointLatencyDTO;
import com.bluenet.web.api.dto.audit.EndpointRankingDTO;
import com.bluenet.web.api.dto.audit.StatisticsPeriod;
import com.bluenet.web.api.dto.audit.TrendPointDTO;
import com.bluenet.web.application.service.AuditStatisticsService;
import com.bluenet.web.domain.repository.AuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 直接查询 PostgreSQL 的审计统计服务实现（Phase 1）。
 * <p>
 * 通过 SQL 聚合查询对 tb_audit 表进行实时统计，适用于百万级数据量。 未来可通过替换实现类切换为预聚合方案。
 * </p>
 */
@Service
@RequiredArgsConstructor
public class DirectAuditStatisticsService implements AuditStatisticsService {
    private final AuditRepository auditRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TrendPointDTO> getTrends(StatisticsPeriod period) {
        return auditRepository.queryTrends(period);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EndpointRankingDTO> getEndpointRanking(StatisticsPeriod period, int limit) {
        return auditRepository.queryEndpointRanking(period, limit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EndpointLatencyDTO> getEndpointLatencyRanking(StatisticsPeriod period, int limit) {
        return auditRepository.queryEndpointLatencyRanking(period, limit);
    }
}
