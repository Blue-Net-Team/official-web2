package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.api.dto.audit.EndpointLatencyDTO;
import com.bluenet.web.api.dto.audit.EndpointRankingDTO;
import com.bluenet.web.api.dto.audit.StatisticsPeriod;
import com.bluenet.web.api.dto.audit.TrendPointDTO;
import com.bluenet.web.domain.model.entity.Audit;
import com.bluenet.web.domain.repository.AuditRepository;
import com.bluenet.web.infrastructure.repository.mapper.AuditMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AuditRepositoryImpl implements AuditRepository {
    private final AuditMapper auditMapper;

    @Override
    public void insert(Audit audit) {
        auditMapper.insert(audit);
    }

    @Override
    public List<TrendPointDTO> queryTrends(StatisticsPeriod period) {
        return auditMapper.selectTrends(period);
    }

    @Override
    public List<EndpointRankingDTO> queryEndpointRanking(StatisticsPeriod period, int limit) {
        return auditMapper.selectEndpointRanking(period, limit);
    }

    @Override
    public List<EndpointLatencyDTO> queryEndpointLatencyRanking(StatisticsPeriod period, int limit) {
        return auditMapper.selectEndpointLatencyRanking(period, limit);
    }
}
