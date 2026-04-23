package com.bluenet.web.application.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bluenet.web.application.command.auditstatistics.AuditStatisticsCommands;
import com.bluenet.web.domain.model.enumerate.AuditStatisticsPeriod;
import com.bluenet.web.domain.model.vo.AuditEndpointLatencyVO;
import com.bluenet.web.domain.model.vo.AuditEndpointRankingVO;
import com.bluenet.web.domain.model.vo.AuditTrendPointVO;
import com.bluenet.web.domain.repository.AuditRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@DisplayName("AuditStatisticsAppServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class AuditStatisticsAppServiceImplTest {

    @Mock
    private AuditRepository auditRepository;

    private AuditStatisticsAppServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AuditStatisticsAppServiceImpl(auditRepository);
    }

    // ==================== getTrends ====================

    @Nested
    @DisplayName("getTrends")
    class GetTrends {

        @Test
        @DisplayName("应返回趋势数据并委托给 repository")
        void shouldDelegateToRepository() {
            AuditTrendPointVO point = AuditTrendPointVO.builder()
                    .time(LocalDateTime.of(2026, 4, 11, 10, 0))
                    .count(42L)
                    .build();
            when(auditRepository.queryTrends(AuditStatisticsPeriod.D7)).thenReturn(List.of(point));

            var result = service.getTrends(new AuditStatisticsCommands.GetTrendsCommand("D7"));

            assertEquals(1, result.size());
            assertEquals(42L, result.get(0).count());
            verify(auditRepository).queryTrends(AuditStatisticsPeriod.D7);
        }

        @Test
        @DisplayName("无数据时应返回空列表")
        void noData_shouldReturnEmptyList() {
            when(auditRepository.queryTrends(AuditStatisticsPeriod.H24)).thenReturn(Collections.emptyList());

            var result = service.getTrends(new AuditStatisticsCommands.GetTrendsCommand("H24"));

            assertTrue(result.isEmpty());
        }

        @ParameterizedTest
        @DisplayName("应正确传递不同的 period 参数")
        @ArgumentsSource(PeriodProvider.class)
        void shouldPassPeriodToRepository(String period) {
            AuditStatisticsPeriod domainPeriod = AuditStatisticsPeriod.valueOf(period);
            when(auditRepository.queryTrends(domainPeriod)).thenReturn(Collections.emptyList());

            service.getTrends(new AuditStatisticsCommands.GetTrendsCommand(period));

            verify(auditRepository).queryTrends(domainPeriod);
        }
    }

    // ==================== getEndpointRanking ====================

    @Nested
    @DisplayName("getEndpointRanking")
    class GetEndpointRanking {

        @Test
        @DisplayName("应返回排名数据并委托给 repository")
        void shouldDelegateToRepository() {
            AuditEndpointRankingVO vo = AuditEndpointRankingVO.builder()
                    .pattern("/api/v1/file/download/{fileId}")
                    .count(100L)
                    .avgDurationMs(45.5)
                    .errorCount(2L)
                    .build();
            when(auditRepository.queryEndpointRanking(AuditStatisticsPeriod.D7, 20)).thenReturn(List.of(vo));

            var result = service.getEndpointRanking(new AuditStatisticsCommands.GetEndpointRankingCommand("D7", 20));

            assertEquals(1, result.size());
            assertEquals("/api/v1/file/download/{fileId}", result.get(0).pattern());
            assertEquals(100L, result.get(0).count());
            assertEquals(45.5, result.get(0).avgDurationMs());
            assertEquals(2L, result.get(0).errorCount());
            verify(auditRepository).queryEndpointRanking(AuditStatisticsPeriod.D7, 20);
        }

        @Test
        @DisplayName("无数据时应返回空列表")
        void noData_shouldReturnEmptyList() {
            when(auditRepository.queryEndpointRanking(AuditStatisticsPeriod.D30, 10))
                    .thenReturn(Collections.emptyList());

            var result = service.getEndpointRanking(new AuditStatisticsCommands.GetEndpointRankingCommand("D30", 10));

            assertTrue(result.isEmpty());
        }
    }

    // ==================== getEndpointLatencyRanking ====================

    @Nested
    @DisplayName("getEndpointLatencyRanking")
    class GetEndpointLatencyRanking {

        @Test
        @DisplayName("应返回延迟排名数据并委托给 repository")
        void shouldDelegateToRepository() {
            AuditEndpointLatencyVO vo = AuditEndpointLatencyVO.builder()
                    .pattern("/api/v1/admin/competitions/{id}/images/{imageId}")
                    .avgDurationMs(320.8)
                    .maxDurationMs(1500L)
                    .count(50L)
                    .build();
            when(auditRepository.queryEndpointLatencyRanking(AuditStatisticsPeriod.D7, 20)).thenReturn(List.of(vo));

            var result = service
                    .getEndpointLatencyRanking(new AuditStatisticsCommands.GetEndpointLatencyRankingCommand("D7", 20));

            assertEquals(1, result.size());
            assertEquals(320.8, result.get(0).avgDurationMs());
            assertEquals(1500L, result.get(0).maxDurationMs());
            assertEquals(50L, result.get(0).count());
            verify(auditRepository).queryEndpointLatencyRanking(AuditStatisticsPeriod.D7, 20);
        }

        @Test
        @DisplayName("无数据时应返回空列表")
        void noData_shouldReturnEmptyList() {
            when(auditRepository.queryEndpointLatencyRanking(AuditStatisticsPeriod.H24, 5))
                    .thenReturn(Collections.emptyList());

            var result = service
                    .getEndpointLatencyRanking(new AuditStatisticsCommands.GetEndpointLatencyRankingCommand("H24", 5));

            assertTrue(result.isEmpty());
        }
    }

    static class PeriodProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    Arguments.of("H24"),
                    Arguments.of("D7"),
                    Arguments.of("D30"));
        }
    }
}
