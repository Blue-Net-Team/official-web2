package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.converter.auditstatistics.AuditStatisticsRequestConverter;
import com.bluenet.web.api.converter.auditstatistics.AuditStatisticsResponseConverter;
import com.bluenet.web.api.dto.audit.EndpointLatencyDTO;
import com.bluenet.web.api.dto.audit.EndpointRankingDTO;
import com.bluenet.web.api.dto.audit.TrendPointDTO;
import com.bluenet.web.application.result.audit.AuditEndpointLatency;
import com.bluenet.web.application.result.audit.AuditEndpointRanking;
import com.bluenet.web.application.result.audit.AuditTrendPoint;
import com.bluenet.web.application.service.AuditStatisticsAppService;
import com.bluenet.web.infrastructure.security.principal.WithSecurityPrincipal;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.bluenet.web.testconfig.TestSecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@DisplayName("AdminAuditStatisticsController 集成测试")
class AdminAuditStatisticsControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuditStatisticsAppService auditStatisticsAppService;

    @MockitoBean
    private AuditStatisticsResponseConverter responseConverter;

    @Autowired
    private AuditStatisticsRequestConverter requestConverter;

    @AfterEach
    void tearDown() {
        UserCTX.clear();
    }

    private static final long SUPER_ADMIN_USER_ID = 9999L;

    @Test
    @DisplayName("getTrends: 普通成员访问应返回 403")
    @WithSecurityPrincipal(roleType = "MEMBER", roleId = 3L, permissions = {})
    void getTrends_asMember_shouldReturn403() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/admin/audit/statistics/trends"))
                .andExpect(status().isForbidden())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(403);
    }

    @Test
    @DisplayName("getTrends: 超级管理员应返回请求量趋势")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "audit:statistics:trends" })
    void getTrends_asSuperAdmin_shouldReturnTrends() throws Exception {
        AuditTrendPoint trendPoint = AuditTrendPoint.builder()
                .time(LocalDateTime.now())
                .count(100L)
                .build();
        TrendPointDTO dto = new TrendPointDTO();
        dto.setTime(LocalDateTime.now());
        dto.setCount(100L);
        when(auditStatisticsAppService.getTrends(any())).thenReturn(List.of(trendPoint));
        when(responseConverter.toDTO(any(AuditTrendPoint.class))).thenReturn(dto);

        MvcResult result = mockMvc.perform(get("/api/v1/admin/audit/statistics/trends").param("period", "7d"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("getTrends: 无效时间范围应返回 400")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "audit:statistics:trends" })
    void getTrends_withInvalidPeriod_shouldReturn400() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/admin/audit/statistics/trends").param("period", "invalid"))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("getEndpointRanking: 超级管理员应返回接口访问排名")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "audit:statistics:endpoints" })
    void getEndpointRanking_asSuperAdmin_shouldReturnRanking() throws Exception {
        AuditEndpointRanking ranking = AuditEndpointRanking.builder()
                .pattern("/api/v1/test")
                .count(50L)
                .avgDurationMs(12.5)
                .errorCount(0L)
                .build();
        EndpointRankingDTO dto = new EndpointRankingDTO();
        dto.setPattern("/api/v1/test");
        dto.setCount(50L);
        dto.setAvgDurationMs(12.5);
        dto.setErrorCount(0L);
        when(auditStatisticsAppService.getEndpointRanking(any())).thenReturn(List.of(ranking));
        when(responseConverter.toDTO(any(AuditEndpointRanking.class))).thenReturn(dto);

        MvcResult result = mockMvc.perform(
                get("/api/v1/admin/audit/statistics/endpoints")
                        .param("period", "7d")
                        .param("limit", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("getEndpointLatencyRanking: 超级管理员应返回接口响应时间排名")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "audit:statistics:latency" })
    void getEndpointLatencyRanking_asSuperAdmin_shouldReturnLatencyRanking() throws Exception {
        AuditEndpointLatency latency = AuditEndpointLatency.builder()
                .pattern("/api/v1/test")
                .avgDurationMs(15.0)
                .maxDurationMs(100L)
                .count(50L)
                .build();
        EndpointLatencyDTO dto = new EndpointLatencyDTO();
        dto.setPattern("/api/v1/test");
        dto.setAvgDurationMs(15.0);
        dto.setMaxDurationMs(100L);
        dto.setCount(50L);
        when(auditStatisticsAppService.getEndpointLatencyRanking(any())).thenReturn(List.of(latency));
        when(responseConverter.toDTO(any(AuditEndpointLatency.class))).thenReturn(dto);

        MvcResult result = mockMvc.perform(
                get("/api/v1/admin/audit/statistics/latency")
                        .param("period", "7d")
                        .param("limit", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }
}
