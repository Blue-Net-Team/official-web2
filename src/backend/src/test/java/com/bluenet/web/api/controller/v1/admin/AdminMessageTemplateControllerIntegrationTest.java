package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.application.message.MessageTemplateInfo;
import com.bluenet.web.application.service.MessageTemplateAppService;
import com.bluenet.web.domain.exception.DataNotFound;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;

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
@DisplayName("AdminMessageTemplateController 集成测试")
class AdminMessageTemplateControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MessageTemplateAppService messageTemplateAppService;

    @AfterEach
    void tearDown() {
        UserCTX.clear();
    }

    private static final long SUPER_ADMIN_USER_ID = 9999L;

    private MessageTemplateInfo templateInfo() {
        return new MessageTemplateInfo(
                "WELCOME",
                "欢迎模板",
                "欢迎",
                "欢迎邮件",
                List.of("name"),
                "<p>欢迎</p>",
                "<p>欢迎</p>",
                true);
    }

    @Test
    @DisplayName("listTemplates: 普通成员访问应返回 403")
    @WithSecurityPrincipal(roleType = "MEMBER", roleId = 3L, permissions = {})
    void listTemplates_asMember_shouldReturn403() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/admin/message-templates"))
                .andExpect(status().isForbidden())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(403);
    }

    @Test
    @DisplayName("listTemplates: 超级管理员应返回模板列表")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "message-template:list" })
    void listTemplates_asSuperAdmin_shouldReturnTemplates() throws Exception {
        when(messageTemplateAppService.listTemplates()).thenReturn(List.of(templateInfo()));

        MvcResult result = mockMvc.perform(get("/api/v1/admin/message-templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("getTemplate: 超级管理员应返回模板详情")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "message-template:detail" })
    void getTemplate_asSuperAdmin_shouldReturnTemplate() throws Exception {
        when(messageTemplateAppService.getTemplate("WELCOME")).thenReturn(templateInfo());

        MvcResult result = mockMvc.perform(get("/api/v1/admin/message-templates/{code}", "WELCOME"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("WELCOME"))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("getTemplate: 模板不存在时应返回 404")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "message-template:detail" })
    void getTemplate_whenNotFound_shouldReturn404() throws Exception {
        when(messageTemplateAppService.getTemplate("UNKNOWN")).thenThrow(new DataNotFound("模板不存在"));

        MvcResult result = mockMvc.perform(get("/api/v1/admin/message-templates/{code}", "UNKNOWN"))
                .andExpect(status().isNotFound())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("updateTemplate: 超级管理员应成功更新模板")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "message-template:update" })
    void updateTemplate_asSuperAdmin_shouldReturnOk() throws Exception {
        doNothing().when(messageTemplateAppService).updateTemplate("WELCOME", "新主题", "<p>新内容</p>");

        AdminMessageTemplateController.UpdateTemplateRequest request = new AdminMessageTemplateController.UpdateTemplateRequest(
                "新主题", "<p>新内容</p>");

        MvcResult result = mockMvc.perform(
                put("/api/v1/admin/message-templates/{code}", "WELCOME")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("toggleTemplate: 超级管理员应成功切换模板状态")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "message-template:toggle" })
    void toggleTemplate_asSuperAdmin_shouldReturnEnabled() throws Exception {
        doNothing().when(messageTemplateAppService).toggleTemplate("WELCOME", true);

        MvcResult result = mockMvc.perform(
                post("/api/v1/admin/message-templates/{code}/toggle", "WELCOME")
                        .param("enabled", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("toggleTemplate: 缺少 enabled 参数应返回 400")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "message-template:toggle" })
    void toggleTemplate_withMissingEnabled_shouldReturn400() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/admin/message-templates/{code}/toggle", "WELCOME"))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("previewTemplate: 超级管理员应返回模板渲染结果")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "message-template:preview" })
    void previewTemplate_asSuperAdmin_shouldReturnHtml() throws Exception {
        when(messageTemplateAppService.previewTemplate(any(String.class), any(Map.class))).thenReturn("<p>预览</p>");

        Map<String, String> variables = Map.of("name", "测试");

        MvcResult result = mockMvc.perform(
                post("/api/v1/admin/message-templates/{code}/preview", "WELCOME")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(variables)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("<p>预览</p>"))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }
}
