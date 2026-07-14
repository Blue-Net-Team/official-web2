package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.converter.judge.JudgeProblemConfigRequestConverter;
import com.bluenet.web.api.converter.judge.JudgeProblemConfigResponseConverter;
import com.bluenet.web.api.dto.judge.ConfirmJudgeLanguageLimitRequestDTO;
import com.bluenet.web.api.dto.judge.JudgeProblemConfigDTO;
import com.bluenet.web.api.dto.judge.UpsertJudgeProblemConfigRequestDTO;
import com.bluenet.web.application.command.judge.JudgeProblemConfigCommands;
import com.bluenet.web.application.result.judge.JudgeProblemConfigResult;
import com.bluenet.web.application.service.JudgeProblemConfigAdminService;
import com.bluenet.web.infrastructure.security.principal.WithSecurityPrincipal;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.bluenet.web.testconfig.TestSecurityConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@DisplayName("AdminJudgeProblemConfigController 集成测试")
class AdminJudgeProblemConfigControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JudgeProblemConfigAdminService judgeProblemConfigAdminService;

    @MockitoBean
    private JudgeProblemConfigResponseConverter judgeProblemConfigResponseConverter;

    @Autowired
    private JudgeProblemConfigRequestConverter judgeProblemConfigRequestConverter;

    @AfterEach
    void tearDown() {
        UserCTX.clear();
    }

    private static final long SUPER_ADMIN_USER_ID = 9999L;

    private JudgeProblemConfigResult configResult() {
        return new JudgeProblemConfigResult(
                1L,
                1L,
                "python",
                "generator-key",
                "print(1)",
                "manifest-key",
                "python",
                "DRAFT",
                5,
                1.2,
                100,
                10,
                List.of(),
                List.of());
    }

    private JudgeProblemConfigDTO configDTO() {
        return new JudgeProblemConfigDTO(
                1L,
                1L,
                "python",
                "generator-key",
                "print(1)",
                "manifest-key",
                "python",
                "DRAFT",
                5,
                1.2,
                100,
                10,
                List.of(),
                List.of());
    }

    private UpsertJudgeProblemConfigRequestDTO validUpsertRequest() {
        JsonNode generatorArgs = objectMapper.valueToTree(Map.of("n", 10));
        UpsertJudgeProblemConfigRequestDTO.StandardSolutionRequest solution = new UpsertJudgeProblemConfigRequestDTO.StandardSolutionRequest(
                "python", "print(1)", true);
        UpsertJudgeProblemConfigRequestDTO.TestcaseConfigRequest testcase = new UpsertJudgeProblemConfigRequestDTO.TestcaseConfigRequest(
                1, "SAMPLE", generatorArgs, BigDecimal.ONE, false, true, "样例");
        return new UpsertJudgeProblemConfigRequestDTO(
                "python",
                "print(1)",
                "python",
                5,
                new BigDecimal("1.2"),
                100,
                10,
                List.of(solution),
                List.of(testcase));
    }

    @Test
    @DisplayName("get: 普通成员访问应返回 403")
    @WithSecurityPrincipal(roleType = "MEMBER", roleId = 3L, permissions = {})
    void get_asMember_shouldReturn403() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/admin/judge/questions/{questionId}/config", 1L))
                .andExpect(status().isForbidden())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(403);
    }

    @Test
    @DisplayName("get: 超级管理员应返回判题配置")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "judge-problem-config:read" })
    void get_asSuperAdmin_shouldReturnConfig() throws Exception {
        when(judgeProblemConfigAdminService.findByQuestionId(1L)).thenReturn(Optional.of(configResult()));
        when(judgeProblemConfigResponseConverter.toDTO(any(JudgeProblemConfigResult.class))).thenReturn(configDTO());

        MvcResult result = mockMvc.perform(get("/api/v1/admin/judge/questions/{questionId}/config", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("get: 配置不存在时应返回 404")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "judge-problem-config:read" })
    void get_whenNotFound_shouldReturn404() throws Exception {
        when(judgeProblemConfigAdminService.findByQuestionId(1L)).thenReturn(Optional.empty());

        MvcResult result = mockMvc.perform(get("/api/v1/admin/judge/questions/{questionId}/config", 1L))
                .andExpect(status().isNotFound())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("upsert: 超级管理员应成功保存判题配置")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "judge-problem-config:upsert" })
    void upsert_asSuperAdmin_shouldReturnConfig() throws Exception {
        when(
                judgeProblemConfigAdminService.upsert(
                        any(Long.class),
                        any(JudgeProblemConfigCommands.UpsertCommand.class))).thenReturn(configResult());
        when(judgeProblemConfigResponseConverter.toDTO(any(JudgeProblemConfigResult.class))).thenReturn(configDTO());

        UpsertJudgeProblemConfigRequestDTO request = validUpsertRequest();

        MvcResult result = mockMvc.perform(
                put("/api/v1/admin/judge/questions/{questionId}/config", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("upsert: 缺少 generatorLanguage 应返回 400")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "judge-problem-config:upsert" })
    void upsert_withInvalidRequest_shouldReturn400() throws Exception {
        UpsertJudgeProblemConfigRequestDTO request = new UpsertJudgeProblemConfigRequestDTO(
                "",
                "print(1)",
                "python",
                5,
                new BigDecimal("1.2"),
                100,
                10,
                List.of(),
                List.of());

        MvcResult result = mockMvc.perform(
                put("/api/v1/admin/judge/questions/{questionId}/config", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("requestGeneration: 超级管理员应成功触发测试数据生成任务")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "judge-problem-config:generate-test-data" })
    void requestGeneration_asSuperAdmin_shouldReturnOk() throws Exception {
        doNothing().when(judgeProblemConfigAdminService).requestGeneration(1L);

        MvcResult result = mockMvc.perform(
                post("/api/v1/admin/judge/questions/{questionId}/config/generation-tasks", 1L))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("confirmLanguageLimit: 超级管理员应成功确认语言限制")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "judge-problem-config:confirm-language-limit" })
    void confirmLanguageLimit_asSuperAdmin_shouldReturnOk() throws Exception {
        doNothing().when(judgeProblemConfigAdminService)
                .confirmLanguageLimit(
                        any(Long.class),
                        any(String.class),
                        any(JudgeProblemConfigCommands.ConfirmLanguageLimitCommand.class));

        ConfirmJudgeLanguageLimitRequestDTO request = new ConfirmJudgeLanguageLimitRequestDTO(1000, 65536, 1024);

        MvcResult result = mockMvc.perform(
                put("/api/v1/admin/judge/questions/{questionId}/config/language-limits/{language}", 1L, "python")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }
}
