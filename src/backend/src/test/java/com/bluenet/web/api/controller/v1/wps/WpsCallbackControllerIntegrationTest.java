package com.bluenet.web.api.controller.v1.wps;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.converter.wpsform.WpsFormRequestConverter;
import com.bluenet.web.api.dto.wps.WpsBindCallbackRequestDTO;
import com.bluenet.web.api.dto.wps.WpsCreateAnswerCallbackRequestDTO;
import com.bluenet.web.application.command.wpsform.WpsFormCommands;
import com.bluenet.web.application.service.WpsFormAppService;
import com.bluenet.web.infrastructure.config.properties.WpsProperties;
import com.bluenet.web.testconfig.TestSecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@DisplayName("WpsCallbackController 集成测试")
class WpsCallbackControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WpsFormAppService wpsFormAppService;

    @MockitoBean
    private WpsFormRequestConverter wpsFormRequestConverter;

    @MockitoBean
    private WpsProperties wpsProperties;

    @AfterEach
    void tearDown() {
        // WPS 回调为公开接口，不依赖 UserCTX。
    }

    @Test
    @DisplayName("handleCallback: bind 事件应返回绑定验证码")
    void handleCallback_bindEvent_shouldReturnBindCode() throws Exception {
        when(wpsProperties.getBindCode()).thenReturn("bind-code-123");
        when(wpsFormAppService.resolveBindCode("rid-123")).thenReturn("bind-code-123");

        WpsBindCallbackRequestDTO request = new WpsBindCallbackRequestDTO();
        request.setRid("rid-123");
        request.setFormId("");
        request.setEventTs(1L);

        mockMvc.perform(
                post("/api/v1/wps/callback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bind_code").value("bind-code-123"));
    }

    @Test
    @DisplayName("handleCallback: create_answer 事件且未配置 secret 时应成功处理")
    void handleCallback_createAnswerWithoutSecret_shouldReturnOk() throws Exception {
        WpsProperties.Webhook webhook = new WpsProperties.Webhook();
        webhook.setSecret("");
        when(wpsProperties.getWebhook()).thenReturn(webhook);
        when(wpsFormRequestConverter.toCreateUserCommand(any())).thenReturn(
                mock(WpsFormCommands.CreateUserFromWpsFormCommand.class));
        doNothing().when(wpsFormAppService).createUserFromWpsForm(any());

        WpsCreateAnswerCallbackRequestDTO request = new WpsCreateAnswerCallbackRequestDTO();
        request.setRid("rid-456");
        request.setFormId("form-456");
        request.setEventTs(1L);
        request.setAnswerContents(
                List.of(
                        new WpsCreateAnswerCallbackRequestDTO.AnswerContent("q1", "text", "学号", "2024001001")));

        mockMvc.perform(
                post("/api/v1/wps/callback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bind_code").doesNotExist());
    }

    @Test
    @DisplayName("handleCallback: create_answer 事件且 secret 不匹配时应返回 401")
    void handleCallback_createAnswerWithWrongSecret_shouldReturnUnauthorized() throws Exception {
        WpsProperties.Webhook webhook = new WpsProperties.Webhook();
        webhook.setSecret("expected-secret");
        when(wpsProperties.getWebhook()).thenReturn(webhook);

        WpsCreateAnswerCallbackRequestDTO request = new WpsCreateAnswerCallbackRequestDTO();
        request.setRid("rid-789");
        request.setFormId("form-789");
        request.setEventTs(1L);
        request.setAnswerContents(
                List.of(
                        new WpsCreateAnswerCallbackRequestDTO.AnswerContent("q1", "text", "学号", "2024001001")));

        mockMvc.perform(
                post("/api/v1/wps/callback")
                        .header("X-WPS-Secret", "wrong-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("handleCallback: 无效事件类型应返回 400")
    void handleCallback_invalidEvent_shouldReturnBadRequest() throws Exception {
        WpsProperties.Webhook webhook = new WpsProperties.Webhook();
        webhook.setSecret("");
        when(wpsProperties.getWebhook()).thenReturn(webhook);

        String payload = "{\"rid\":\"rid-000\",\"formId\":\"form-000\",\"event\":\"unknown_event\"}";

        mockMvc.perform(
                post("/api/v1/wps/callback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }
}
