package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.DBIntegrationTest;
import com.bluenet.web.infrastructure.security.principal.WithSecurityPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AdminAiTraceController 集成测试。
 *
 * <p>
 * 覆盖权限控制与只读约束。权限标识的唯一性由 PermissionScanner 在应用启动时校验： 若四个标识存在重复，本测试类（乃至所有 Spring
 * 上下文测试）将无法启动。
 * </p>
 */
@AutoConfigureMockMvc
@DisplayName("AdminAiTraceController 集成测试")
class AdminAiTraceControllerIntegrationTest extends DBIntegrationTest {

    private static final long ADMIN_USER_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping handlerMapping;

    private void insertSampleConversation() {
        LocalDateTime now = LocalDateTime.now();
        jdbc.update(
                "INSERT INTO tb_ai_conversation (id, created_at, last_active_at) VALUES (?, ?, ?)",
                "conv-1",
                Timestamp.valueOf(now.minusMinutes(5)),
                Timestamp.valueOf(now.minusMinutes(1)));
        jdbc.update(
                "INSERT INTO tb_ai_turn (conversation_id, seq, user_input, answer, events, degraded, "
                        + "duration_ms, created_at) VALUES (?, ?, ?, ?, ?::jsonb, FALSE, ?, NOW())",
                "conv-1",
                1,
                "测试提问",
                "测试答案",
                "[{\"type\":\"intent\",\"intent\":\"REGISTRATION\",\"confidence\":0.9,\"action\":\"RETRIEVE\"}]",
                1200);
    }

    @Test
    @DisplayName("未认证访问任意轨迹接口均被拒绝")
    void traceEndpoints_shouldRejectUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/admin/ai-traces/conversations"))
                .andExpect(status().is4xxClientError());
        mockMvc.perform(get("/api/v1/admin/ai-traces/statistics"))
                .andExpect(status().is4xxClientError());
        mockMvc.perform(get("/api/v1/admin/ai-traces/gaps").param("gapType", "REFUSE"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("已登录但无权限时返回 403 且不返回轨迹数据")
    @WithSecurityPrincipal(userId = ADMIN_USER_ID, roleType = "MEMBER", roleId = 3L, permissions = {})
    void traceEndpoints_shouldRejectWithoutPermission() throws Exception {
        insertSampleConversation();
        mockMvc.perform(get("/api/v1/admin/ai-traces/conversations"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("具备 ai-trace:list 时可查询会话列表")
    @WithSecurityPrincipal(userId = ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "ai-trace:list" })
    void listConversations_shouldSucceedWithPermission() throws Exception {
        insertSampleConversation();
        mockMvc.perform(get("/api/v1/admin/ai-traces/conversations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content[0].id").value("conv-1"))
                .andExpect(jsonPath("$.data.content[0].messageCount").value(1))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("会话不存在时返回成功状态码与 null 数据，而非 500")
    @WithSecurityPrincipal(userId = ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "ai-trace:detail" })
    void getConversationDetail_shouldReturnNullDataForUnknownId() throws Exception {
        mockMvc.perform(get("/api/v1/admin/ai-traces/conversations/unknown-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("统计接口在无数据时返回零值与空列表")
    @WithSecurityPrincipal(userId = ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "ai-trace:statistics" })
    void statistics_shouldReturnEmptyWithPermission() throws Exception {
        mockMvc.perform(get("/api/v1/admin/ai-traces/statistics").param("period", "7d"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.overview.questionCount").value(0))
                .andExpect(jsonPath("$.data.overview.fallbackRate").value(0.0))
                .andExpect(jsonPath("$.data.intents").isArray())
                .andExpect(jsonPath("$.data.trend").isArray());
    }

    @Test
    @DisplayName("缺口接口需要 ai-trace:gap 权限")
    @WithSecurityPrincipal(userId = ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "ai-trace:gap" })
    void listGapTurns_shouldSucceedWithPermission() throws Exception {
        mockMvc.perform(get("/api/v1/admin/ai-traces/gaps").param("gapType", "REFUSE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("后台为只读：轨迹所有路由均为 GET，不存在任何写入能力")
    void traceEndpoints_shouldBeGetOnly() {
        Map<String, Set<RequestMethod>> traceRoutes = new LinkedHashMap<>();
        for (RequestMappingInfo info : handlerMapping.getHandlerMethods().keySet()) {
            for (String pattern : info.getPatternValues()) {
                if (pattern.startsWith("/api/v1/admin/ai-traces")) {
                    traceRoutes.put(pattern, info.getMethodsCondition().getMethods());
                }
            }
        }

        // 确实注册了轨迹路由，避免空集合让后面的断言空转通过
        assertThat(traceRoutes.keySet())
                .contains("/api/v1/admin/ai-traces/conversations")
                .contains("/api/v1/admin/ai-traces/conversations/{id}")
                .contains("/api/v1/admin/ai-traces/statistics")
                .contains("/api/v1/admin/ai-traces/gaps");

        traceRoutes.forEach(
                (pattern, methods) -> assertThat(methods)
                        .as("轨迹路由 %s 必须且只能是 GET", pattern)
                        .containsExactly(RequestMethod.GET));
    }
}
