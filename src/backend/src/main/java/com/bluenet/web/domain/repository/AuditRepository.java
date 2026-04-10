package com.bluenet.web.domain.repository;

import com.bluenet.web.api.dto.audit.EndpointLatencyDTO;
import com.bluenet.web.api.dto.audit.EndpointRankingDTO;
import com.bluenet.web.api.dto.audit.StatisticsPeriod;
import com.bluenet.web.api.dto.audit.TrendPointDTO;
import com.bluenet.web.domain.model.entity.Audit;

import java.util.List;

/**
 * 审计日志仓库接口
 */
public interface AuditRepository {
    void insert(Audit audit);

    List<TrendPointDTO> queryTrends(StatisticsPeriod period);

    List<EndpointRankingDTO> queryEndpointRanking(StatisticsPeriod period, int limit);

    List<EndpointLatencyDTO> queryEndpointLatencyRanking(StatisticsPeriod period, int limit);
}
