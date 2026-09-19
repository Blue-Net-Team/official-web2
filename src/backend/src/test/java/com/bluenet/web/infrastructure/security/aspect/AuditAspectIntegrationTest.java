package com.bluenet.web.infrastructure.security.aspect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import com.bluenet.web.DBIntegrationTest;
import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.infrastructure.repository.dataobject.AuditDO;
import com.bluenet.web.infrastructure.repository.mapper.AuditMapper;
import com.bluenet.web.infrastructure.security.annotation.AccessLevel;
import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;
import com.bluenet.web.infrastructure.security.principal.WithSecurityPrincipal;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.test.web.servlet.MockMvc;

/**
 * AuditAspect 集成测试。
 * <p>
 * 验证经 @RequiresPermission 注解的接口在包含 java.time 参数时，审计记录仍以合法 JSON 落库、且敏感字段被脱敏（回归
 * issue：审计参数序列化失败导致 action_arg 退化为 {@code {"error":"serialization failed"}}）。
 * </p>
 */
@DisplayName("AuditAspect 集成测试")
@AutoConfigureMockMvc
@Import(AuditAspectIntegrationTest.AuditTestController.class)
class AuditAspectIntegrationTest extends DBIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuditMapper auditMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithSecurityPrincipal(userId = 7L, roleType = "MEMBER")
    @DisplayName("含 LocalDate 的请求参数应记录为合法嵌套 JSON 且敏感字段被脱敏")
    void auditRequestWithLocalDate_shouldStoreNestedJson() throws Exception {
        mockMvc.perform(
                post("/api/v1/test/audit/achievement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"蓝桥杯全国一等奖\",\"achieveAt\":\"2024-04-15\",\"password\":\"secretHash\"}"))
                .andExpect(status().isOk());

        Thread.sleep(500L);

        AuditDO saved = auditMapper.selectList(null).get(0);
        String actionArg = saved.getActionArg();

        assertThat(actionArg)
                .doesNotContain("serialization failed")
                .doesNotContain("secretHash")
                .contains("2024-04-15")
                .contains("***");

        JsonNode node = objectMapper.readTree(actionArg);
        assertThat(node.get("request").isObject()).isTrue();
        assertThat(node.get("request").get("title").asText()).isEqualTo("蓝桥杯全国一等奖");
        assertThat(node.get("request").get("achieveAt").asText()).isEqualTo("2024-04-15");
        assertThat(node.get("request").get("password").asText()).isEqualTo("***");
        assertThat(saved.getRequestUriPattern()).isEqualTo("/api/v1/test/audit/achievement");
        assertThat(saved.getActionUserId()).isEqualTo(7L);
    }

    /** 测试用的内部 Controller，参数 DTO 同时包含 java.time 字段与敏感字段 */
    @RestController
    static class AuditTestController {

        @PostMapping("/api/v1/test/audit/achievement")
        @RequiresPermission(value = "test:audit:create", access = AccessLevel.AUTHENTICATED)
        public ResponseMessage<Void> createAchievement(@RequestBody AchievementRequest request) {
            return ResponseMessage.success();
        }
    }

    record AchievementRequest(String title, LocalDate achieveAt, String password) {
    }
}
