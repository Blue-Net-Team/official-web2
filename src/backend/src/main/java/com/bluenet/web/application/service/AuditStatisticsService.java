package com.bluenet.web.application.service;

import com.bluenet.web.api.dto.audit.EndpointLatencyDTO;
import com.bluenet.web.api.dto.audit.EndpointRankingDTO;
import com.bluenet.web.api.dto.audit.StatisticsPeriod;
import com.bluenet.web.api.dto.audit.TrendPointDTO;

import java.util.List;

/**
 * 审计统计服务接口，提供基于审计数据的聚合统计查询。
 * <p>
 * 当前实现直接查询 PostgreSQL，未来可通过预聚合方案替换实现。
 * </p>
 */
public interface AuditStatisticsService {
    List<TrendPointDTO> getTrends(StatisticsPeriod period);

    List<EndpointRankingDTO> getEndpointRanking(StatisticsPeriod period, int limit);

    List<EndpointLatencyDTO> getEndpointLatencyRanking(StatisticsPeriod period, int limit);
}
