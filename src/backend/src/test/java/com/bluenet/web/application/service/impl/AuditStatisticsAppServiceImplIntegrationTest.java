package com.bluenet.web.application.service.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.application.query.auditstatistics.GetEndpointLatencyRankingQuery;
import com.bluenet.web.application.query.auditstatistics.GetEndpointRankingQuery;
import com.bluenet.web.application.query.auditstatistics.GetTrendsQuery;
import com.bluenet.web.application.result.audit.AuditEndpointLatency;
import com.bluenet.web.application.result.audit.AuditEndpointRanking;
import com.bluenet.web.application.result.audit.AuditTrendPoint;
import com.bluenet.web.application.service.AuditStatisticsAppService;
import com.bluenet.web.domain.model.entity.Audit;
import com.bluenet.web.domain.model.enumerate.AuditStatisticsPeriod;
import com.bluenet.web.domain.repository.AuditRepository;
import com.bluenet.web.infrastructure.security.principal.WithSecurityPrincipal;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.bluenet.web.testsupport.fixture.AssessmentFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * AuditStatisticsAppServiceImpl 集成测试。
 *
 * <p>
 * 验证审计统计应用服务的趋势聚合、端点访问排行及端点延迟排行查询逻辑。
 * </p>
 */
@DisplayName("AuditStatisticsAppServiceImpl 集成测试")
class AuditStatisticsAppServiceImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AuditStatisticsAppService auditStatisticsAppService;

    @Autowired
    private AuditRepository auditRepository;

    @AfterEach
    void cleanupSecurityContext() {
        UserCTX.clear();
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "MEMBER")
    @DisplayName("getTrends: 同一小时周期内的多条记录应聚合为单一趋势点计数")
    void getTrends_shouldAggregateRecordsInSamePeriod() {
        saveAudit("/api/v1/trend-a");
        saveAudit("/api/v1/trend-b");
        saveAudit("/api/v1/trend-c");

        List<AuditTrendPoint> result = auditStatisticsAppService.getTrends(
                new GetTrendsQuery(AuditStatisticsPeriod.H24));

        assertThat(result).hasSize(24);
        assertThat(result)
                .filteredOn(point -> point.getCount() > 0)
                .hasSize(1)
                .first()
                .satisfies(point -> assertThat(point.getCount()).isEqualTo(3L));
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "MEMBER")
    @DisplayName("getEndpointRanking: 应按访问次数降序返回端点排行")
    void getEndpointRanking_shouldReturnEndpointsOrderedByCount() {
        saveSuccessfulAudit("/api/v1/popular", 100L);
        saveSuccessfulAudit("/api/v1/popular", 100L);
        saveSuccessfulAudit("/api/v1/popular", 100L);
        saveSuccessfulAudit("/api/v1/less", 100L);
        saveSuccessfulAudit("/api/v1/less", 100L);
        saveSuccessfulAudit("/api/v1/rare", 100L);

        List<AuditEndpointRanking> result = auditStatisticsAppService.getEndpointRanking(
                new GetEndpointRankingQuery(AuditStatisticsPeriod.H24, 10));

        assertThat(result)
                .extracting(AuditEndpointRanking::getPattern, AuditEndpointRanking::getCount)
                .containsExactly(
                        tuple("/api/v1/popular", 3L),
                        tuple("/api/v1/less", 2L),
                        tuple("/api/v1/rare", 1L));
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "MEMBER")
    @DisplayName("getEndpointLatencyRanking: 应按平均延迟降序返回端点排行")
    void getEndpointLatencyRanking_shouldReturnEndpointsOrderedByAverageLatency() {
        saveSuccessfulAudit("/api/v1/slow", 300L);
        saveSuccessfulAudit("/api/v1/medium", 200L);
        saveSuccessfulAudit("/api/v1/fast", 100L);

        List<AuditEndpointLatency> result = auditStatisticsAppService.getEndpointLatencyRanking(
                new GetEndpointLatencyRankingQuery(AuditStatisticsPeriod.H24, 10));

        assertThat(result)
                .extracting(AuditEndpointLatency::getPattern, AuditEndpointLatency::getAvgDurationMs)
                .containsExactly(
                        tuple("/api/v1/slow", 300.0),
                        tuple("/api/v1/medium", 200.0),
                        tuple("/api/v1/fast", 100.0));
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "MEMBER")
    @DisplayName("getEndpointRanking: 应尊重 limit 参数限制返回数量")
    void getEndpointRanking_shouldRespectLimit() {
        saveSuccessfulAudit("/api/v1/ep-1", 50L);
        saveSuccessfulAudit("/api/v1/ep-2", 50L);
        saveSuccessfulAudit("/api/v1/ep-3", 50L);
        saveSuccessfulAudit("/api/v1/ep-4", 50L);
        saveSuccessfulAudit("/api/v1/ep-5", 50L);

        List<AuditEndpointRanking> result = auditStatisticsAppService.getEndpointRanking(
                new GetEndpointRankingQuery(AuditStatisticsPeriod.H24, 3));

        assertThat(result).hasSize(3);
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "MEMBER")
    @DisplayName("getEndpointLatencyRanking: 应尊重 limit 参数限制返回数量")
    void getEndpointLatencyRanking_shouldRespectLimit() {
        saveSuccessfulAudit("/api/v1/latency-1", 50L);
        saveSuccessfulAudit("/api/v1/latency-2", 100L);
        saveSuccessfulAudit("/api/v1/latency-3", 150L);
        saveSuccessfulAudit("/api/v1/latency-4", 200L);
        saveSuccessfulAudit("/api/v1/latency-5", 250L);

        List<AuditEndpointLatency> result = auditStatisticsAppService.getEndpointLatencyRanking(
                new GetEndpointLatencyRankingQuery(AuditStatisticsPeriod.H24, 3));

        assertThat(result).hasSize(3);
    }

    private Audit saveAudit(String requestUri) {
        return buildAndSaveAudit(requestUri, null, null);
    }

    private Audit saveSuccessfulAudit(String requestUri, Long durationMs) {
        return buildAndSaveAudit(requestUri, durationMs, true);
    }

    private Audit buildAndSaveAudit(String requestUri, Long durationMs, Boolean successState) {
        Audit audit = AssessmentFixture.auditBuilder()
                .requestUri(requestUri)
                .build();
        audit.setDurationMs(durationMs);
        audit.setSuccessState(successState);
        auditRepository.save(audit);
        return audit;
    }
}
