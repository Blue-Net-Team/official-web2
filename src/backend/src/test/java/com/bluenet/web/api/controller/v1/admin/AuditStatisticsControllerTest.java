package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.application.AuditStatisticsResult;
import com.bluenet.web.application.service.AuditStatisticsAppService;
import com.bluenet.web.infrastructure.security.WithUserVO;
import com.bluenet.web.testcontainers.TestcontainersConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("AuditStatisticsController 单元测试")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@Import(TestcontainersConfiguration.class)
class AuditStatisticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditStatisticsAppService auditStatisticsAppService;

    @AfterEach
    void tearDown() {
        com.bluenet.web.infrastructure.security.util.UserCTX.clear();
    }

    // ==================== GET /api/v1/admin/audit/statistics/trends
    // ====================

    @Nested
    @DisplayName("请求量趋势接口")
    class TrendsEndpoint {

        @Test
        @DisplayName("已认证用户请求趋势应返回数据")
        @WithUserVO(roleName = "MEMBER", permissions = "audit:statistics:trends")
        void getTrends_withAuth_shouldReturnData() throws Exception {
            AuditStatisticsResult.TrendPoint point = new AuditStatisticsResult.TrendPoint(
                    LocalDateTime.of(2026, 4, 11, 10, 0), 42L);
            when(auditStatisticsAppService.getTrends(any())).thenReturn(List.of(point));

            mockMvc.perform(
                    get("/api/v1/admin/audit/statistics/trends")
                            .param("period", "7d")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].count").value(42));

            verify(auditStatisticsAppService).getTrends(any());
        }

        @Test
        @DisplayName("无 period 参数应使用默认值 7d")
        @WithUserVO(roleName = "MEMBER", permissions = "audit:statistics:trends")
        void getTrends_withoutPeriod_shouldUseDefault() throws Exception {
            when(auditStatisticsAppService.getTrends(any())).thenReturn(new ArrayList<>());

            mockMvc.perform(
                    get("/api/v1/admin/audit/statistics/trends")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(auditStatisticsAppService).getTrends(any());
        }

        @Test
        @DisplayName("空数据时应返回空数组")
        @WithUserVO(roleName = "MEMBER", permissions = "audit:statistics:trends")
        void getTrends_emptyData_shouldReturnEmptyArray() throws Exception {
            when(auditStatisticsAppService.getTrends(any())).thenReturn(new ArrayList<>());

            mockMvc.perform(
                    get("/api/v1/admin/audit/statistics/trends")
                            .param("period", "24h")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    // ==================== GET /api/v1/admin/audit/statistics/endpoints
    // ====================

    @Nested
    @DisplayName("接口访问排名接口")
    class EndpointsEndpoint {

        @Test
        @DisplayName("已认证用户请求排名应返回数据")
        @WithUserVO(roleName = "MEMBER", permissions = "audit:statistics:endpoints")
        void getEndpoints_withAuth_shouldReturnData() throws Exception {
            AuditStatisticsResult.EndpointRanking ranking = new AuditStatisticsResult.EndpointRanking(
                    "/api/v1/file/download/{fileId}", 100L, 45.5, 2L);
            when(auditStatisticsAppService.getEndpointRanking(any())).thenReturn(List.of(ranking));

            mockMvc.perform(
                    get("/api/v1/admin/audit/statistics/endpoints")
                            .param("period", "7d")
                            .param("limit", "20")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].pattern").value("/api/v1/file/download/{fileId}"))
                    .andExpect(jsonPath("$.data[0].count").value(100));
        }

        @Test
        @DisplayName("无参数应使用默认值 period=7d, limit=20")
        @WithUserVO(roleName = "MEMBER", permissions = "audit:statistics:endpoints")
        void getEndpoints_withoutParams_shouldUseDefaults() throws Exception {
            when(auditStatisticsAppService.getEndpointRanking(any())).thenReturn(new ArrayList<>());

            mockMvc.perform(
                    get("/api/v1/admin/audit/statistics/endpoints")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(auditStatisticsAppService).getEndpointRanking(any());
        }
    }

    // ==================== GET /api/v1/admin/audit/statistics/latency
    // ====================

    @Nested
    @DisplayName("接口响应时间排名接口")
    class LatencyEndpoint {

        @Test
        @DisplayName("已认证用户请求延迟排名应返回数据")
        @WithUserVO(roleName = "MEMBER", permissions = "audit:statistics:latency")
        void getLatency_withAuth_shouldReturnData() throws Exception {
            AuditStatisticsResult.EndpointLatency latency = new AuditStatisticsResult.EndpointLatency(
                    "/api/v1/admin/competitions/{id}/images/{imageId}", 320.8, 1500L, 50L);
            when(auditStatisticsAppService.getEndpointLatencyRanking(any())).thenReturn(List.of(latency));

            mockMvc.perform(
                    get("/api/v1/admin/audit/statistics/latency")
                            .param("period", "30d")
                            .param("limit", "10")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].pattern").value("/api/v1/admin/competitions/{id}/images/{imageId}"))
                    .andExpect(jsonPath("$.data[0].avgDurationMs").value(320.8))
                    .andExpect(jsonPath("$.data[0].maxDurationMs").value(1500));
        }

        @Test
        @DisplayName("无参数应使用默认值 period=7d, limit=20")
        @WithUserVO(roleName = "MEMBER", permissions = "audit:statistics:latency")
        void getLatency_withoutParams_shouldUseDefaults() throws Exception {
            when(auditStatisticsAppService.getEndpointLatencyRanking(any())).thenReturn(new ArrayList<>());

            mockMvc.perform(
                    get("/api/v1/admin/audit/statistics/latency")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(auditStatisticsAppService).getEndpointLatencyRanking(any());
        }
    }
}
