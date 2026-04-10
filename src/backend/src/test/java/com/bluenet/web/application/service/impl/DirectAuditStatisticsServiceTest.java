package com.bluenet.web.application.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bluenet.web.api.dto.audit.EndpointLatencyDTO;
import com.bluenet.web.api.dto.audit.EndpointRankingDTO;
import com.bluenet.web.api.dto.audit.StatisticsPeriod;
import com.bluenet.web.api.dto.audit.TrendPointDTO;
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

@DisplayName("DirectAuditStatisticsService 单元测试")
@ExtendWith(MockitoExtension.class)
class DirectAuditStatisticsServiceTest {

    @Mock
    private AuditRepository auditRepository;

    private DirectAuditStatisticsService service;

    @BeforeEach
    void setUp() {
        service = new DirectAuditStatisticsService(auditRepository);
    }

    // ==================== getTrends ====================

    @Nested
    @DisplayName("getTrends")
    class GetTrends {

        @Test
        @DisplayName("应返回趋势数据并委托给 repository")
        void shouldDelegateToRepository() {
            TrendPointDTO point = new TrendPointDTO();
            point.setTime(LocalDateTime.of(2026, 4, 11, 10, 0));
            point.setCount(42L);
            when(auditRepository.queryTrends(StatisticsPeriod.D7)).thenReturn(List.of(point));

            var result = service.getTrends(StatisticsPeriod.D7);

            assertEquals(1, result.size());
            assertEquals(42L, result.get(0).getCount());
            verify(auditRepository).queryTrends(StatisticsPeriod.D7);
        }

        @Test
        @DisplayName("无数据时应返回空列表")
        void noData_shouldReturnEmptyList() {
            when(auditRepository.queryTrends(StatisticsPeriod.H24)).thenReturn(Collections.emptyList());

            var result = service.getTrends(StatisticsPeriod.H24);

            assertTrue(result.isEmpty());
        }

        @ParameterizedTest
        @DisplayName("应正确传递不同的 period 参数")
        @ArgumentsSource(PeriodProvider.class)
        void shouldPassPeriodToRepository(StatisticsPeriod period) {
            when(auditRepository.queryTrends(period)).thenReturn(Collections.emptyList());

            service.getTrends(period);

            verify(auditRepository).queryTrends(period);
        }
    }

    // ==================== getEndpointRanking ====================

    @Nested
    @DisplayName("getEndpointRanking")
    class GetEndpointRanking {

        @Test
        @DisplayName("应返回排名数据并委托给 repository")
        void shouldDelegateToRepository() {
            EndpointRankingDTO dto = new EndpointRankingDTO();
            dto.setPattern("/api/v1/file/download/{fileId}");
            dto.setCount(100L);
            dto.setAvgDurationMs(45.5);
            dto.setErrorCount(2L);
            when(auditRepository.queryEndpointRanking(StatisticsPeriod.D7, 20)).thenReturn(List.of(dto));

            var result = service.getEndpointRanking(StatisticsPeriod.D7, 20);

            assertEquals(1, result.size());
            assertEquals("/api/v1/file/download/{fileId}", result.get(0).getPattern());
            assertEquals(100L, result.get(0).getCount());
            assertEquals(45.5, result.get(0).getAvgDurationMs());
            assertEquals(2L, result.get(0).getErrorCount());
            verify(auditRepository).queryEndpointRanking(StatisticsPeriod.D7, 20);
        }

        @Test
        @DisplayName("无数据时应返回空列表")
        void noData_shouldReturnEmptyList() {
            when(auditRepository.queryEndpointRanking(StatisticsPeriod.D30, 10)).thenReturn(Collections.emptyList());

            var result = service.getEndpointRanking(StatisticsPeriod.D30, 10);

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
            EndpointLatencyDTO dto = new EndpointLatencyDTO();
            dto.setPattern("/api/v1/admin/competitions/{id}/images/{imageId}");
            dto.setAvgDurationMs(320.8);
            dto.setMaxDurationMs(1500L);
            dto.setCount(50L);
            when(auditRepository.queryEndpointLatencyRanking(StatisticsPeriod.D7, 20)).thenReturn(List.of(dto));

            var result = service.getEndpointLatencyRanking(StatisticsPeriod.D7, 20);

            assertEquals(1, result.size());
            assertEquals(320.8, result.get(0).getAvgDurationMs());
            assertEquals(1500L, result.get(0).getMaxDurationMs());
            assertEquals(50L, result.get(0).getCount());
            verify(auditRepository).queryEndpointLatencyRanking(StatisticsPeriod.D7, 20);
        }

        @Test
        @DisplayName("无数据时应返回空列表")
        void noData_shouldReturnEmptyList() {
            when(auditRepository.queryEndpointLatencyRanking(StatisticsPeriod.H24, 5))
                    .thenReturn(Collections.emptyList());

            var result = service.getEndpointLatencyRanking(StatisticsPeriod.H24, 5);

            assertTrue(result.isEmpty());
        }
    }

    static class PeriodProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    Arguments.of(StatisticsPeriod.H24),
                    Arguments.of(StatisticsPeriod.D7),
                    Arguments.of(StatisticsPeriod.D30));
        }
    }
}
