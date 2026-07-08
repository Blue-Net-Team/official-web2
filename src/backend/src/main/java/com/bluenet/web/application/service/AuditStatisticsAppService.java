package com.bluenet.web.application.service;

import com.bluenet.web.application.result.audit.AuditEndpointLatency;
import com.bluenet.web.application.result.audit.AuditEndpointRanking;
import com.bluenet.web.application.result.audit.AuditTrendPoint;
import com.bluenet.web.application.query.auditstatistics.GetEndpointLatencyRankingQuery;
import com.bluenet.web.application.query.auditstatistics.GetEndpointRankingQuery;
import com.bluenet.web.application.query.auditstatistics.GetTrendsQuery;

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
     * @param query
     *            查询趋势参数
     * @return 趋势点列表
     */
    List<AuditTrendPoint> getTrends(GetTrendsQuery query);

    /**
     * 获取接口调用排行。
     *
     * @param query
     *            查询排行参数
     * @return 接口调用排行列表
     */
    List<AuditEndpointRanking> getEndpointRanking(
            GetEndpointRankingQuery query);

    /**
     * 获取接口延迟排行。
     *
     * @param query
     *            查询延迟排行参数
     * @return 接口延迟排行列表
     */
    List<AuditEndpointLatency> getEndpointLatencyRanking(
            GetEndpointLatencyRankingQuery query);
}
