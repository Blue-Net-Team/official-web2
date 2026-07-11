package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.application.result.audit.AuditEndpointLatency;
import com.bluenet.web.application.result.audit.AuditEndpointRanking;
import com.bluenet.web.application.result.audit.AuditTrendPoint;
import com.bluenet.web.domain.model.entity.Audit;
import com.bluenet.web.domain.model.enumerate.AuditStatisticsPeriod;
import com.bluenet.web.domain.repository.AuditRepository;
import com.bluenet.web.infrastructure.repository.dataobject.AuditDO;
import com.bluenet.web.infrastructure.repository.mapper.AuditMapper;
import com.bluenet.web.testsupport.fixture.AssessmentFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AuditRepositoryImpl 集成测试。
 */
@DisplayName("AuditRepositoryImpl 集成测试")
class AuditRepositoryImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private AuditMapper auditMapper;

    @Test
    @DisplayName("save: 应插入审计日志并回写ID")
    void save_shouldInsertAndReturnId() {
        Audit audit = AssessmentFixture.auditBuilder()
                .requestUriPattern("/api/v1/test")
                .build();

        auditRepository.save(audit);

        assertNotNull(audit.getId());
        AuditDO dataObject = auditMapper.selectById(audit.getId());
        assertNotNull(dataObject);
        assertEquals("/api/v1/test", dataObject.getRequestUriPattern());
        assertEquals("GET", dataObject.getRequestMethod());
    }

    @Test
    @DisplayName("queryTrends: 应返回最近24小时分组计数")
    void queryTrends_shouldReturnGroupedCounts() {
        Audit audit = AssessmentFixture.auditBuilder()
                .requestUriPattern("/api/v1/test")
                .build();
        auditRepository.save(audit);

        List<AuditTrendPoint> trends = auditRepository.queryTrends(AuditStatisticsPeriod.H24);

        assertFalse(trends.isEmpty());
        long totalCount = trends.stream().mapToLong(AuditTrendPoint::getCount).sum();
        assertTrue(totalCount >= 1L);
    }

    @Test
    @DisplayName("queryEndpointRanking: 应返回接口访问次数排行")
    void queryEndpointRanking_shouldReturnEndpointCounts() {
        Audit audit = AssessmentFixture.auditBuilder()
                .requestUri("/api/v1/ranking")
                .requestUriPattern("/api/v1/ranking")
                .build();
        audit.setDurationMs(100L);
        audit.setSuccessState(true);
        auditRepository.save(audit);

        List<AuditEndpointRanking> rankings = auditRepository.queryEndpointRanking(AuditStatisticsPeriod.H24, 10);

        assertFalse(rankings.isEmpty());
        assertTrue(rankings.stream().anyMatch(r -> "/api/v1/ranking".equals(r.getPattern())));
    }

    @Test
    @DisplayName("queryEndpointLatencyRanking: 应返回接口耗时统计")
    void queryEndpointLatencyRanking_shouldReturnLatencyStats() {
        Audit audit = AssessmentFixture.auditBuilder()
                .requestUri("/api/v1/latency")
                .requestUriPattern("/api/v1/latency")
                .build();
        audit.setDurationMs(200L);
        audit.setSuccessState(false);
        auditRepository.save(audit);

        List<AuditEndpointLatency> latencies = auditRepository.queryEndpointLatencyRanking(
                AuditStatisticsPeriod.H24,
                10);

        assertFalse(latencies.isEmpty());
        assertTrue(latencies.stream().anyMatch(l -> "/api/v1/latency".equals(l.getPattern())));
    }
}
