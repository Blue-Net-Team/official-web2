package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.audit.EndpointLatencyDTO;
import com.bluenet.web.api.dto.audit.EndpointRankingDTO;
import com.bluenet.web.api.dto.audit.StatisticsPeriod;
import com.bluenet.web.api.dto.audit.TrendPointDTO;
import com.bluenet.web.application.service.AuditStatisticsService;
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
        return auditRepository.queryTrends(toDomainPeriod(period))
                .stream()
                .map(this::toTrendPointDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EndpointRankingDTO> getEndpointRanking(StatisticsPeriod period, int limit) {
        return auditRepository.queryEndpointRanking(toDomainPeriod(period), limit)
                .stream()
                .map(this::toEndpointRankingDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EndpointLatencyDTO> getEndpointLatencyRanking(StatisticsPeriod period, int limit) {
        return auditRepository.queryEndpointLatencyRanking(toDomainPeriod(period), limit)
                .stream()
                .map(this::toEndpointLatencyDTO)
                .toList();
    }

    private AuditStatisticsPeriod toDomainPeriod(StatisticsPeriod period) {
        return AuditStatisticsPeriod.valueOf(period.name());
    }

    private TrendPointDTO toTrendPointDTO(AuditTrendPointVO vo) {
        TrendPointDTO dto = new TrendPointDTO();
        dto.setTime(vo.getTime());
        dto.setCount(vo.getCount());
        return dto;
    }

    private EndpointRankingDTO toEndpointRankingDTO(AuditEndpointRankingVO vo) {
        EndpointRankingDTO dto = new EndpointRankingDTO();
        dto.setPattern(vo.getPattern());
        dto.setCount(vo.getCount());
        dto.setAvgDurationMs(vo.getAvgDurationMs());
        dto.setErrorCount(vo.getErrorCount());
        return dto;
    }

    private EndpointLatencyDTO toEndpointLatencyDTO(AuditEndpointLatencyVO vo) {
        EndpointLatencyDTO dto = new EndpointLatencyDTO();
        dto.setPattern(vo.getPattern());
        dto.setAvgDurationMs(vo.getAvgDurationMs());
        dto.setMaxDurationMs(vo.getMaxDurationMs());
        dto.setCount(vo.getCount());
        return dto;
    }
}
