package com.bluenet.web.api.controller.v1.github;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.application.service.impl.GitHubWebhookService;
import com.bluenet.web.infrastructure.github.GitHubWebhookVerifier;
import com.bluenet.web.testconfig.TestSecurityConfig;
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

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@DisplayName("GitHubWebhookController 集成测试")
class GitHubWebhookControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GitHubWebhookVerifier webhookVerifier;

    @MockitoBean
    private GitHubWebhookService webhookService;

    @AfterEach
    void tearDown() {
        // Webhook 为公开接口，不依赖 UserCTX。
    }

    private static final String ISSUES_PAYLOAD = "{\"action\":\"opened\",\"issue\":{\"number\":1}}";

    @Test
    @DisplayName("receiveWebhook: 有效的 issues 事件签名应返回 200 并处理业务")
    void receiveWebhook_validIssuesSignature_shouldReturnOkAndProcess() throws Exception {
        doNothing().when(webhookVerifier).verify(ISSUES_PAYLOAD, "sha256=valid-signature");

        mockMvc.perform(
                post("/api/v1/github/webhook")
                        .header("X-GitHub-Event", "issues")
                        .header("X-Hub-Signature-256", "sha256=valid-signature")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ISSUES_PAYLOAD))
                .andExpect(status().isOk());

        verify(webhookService).processIssuesEvent(ISSUES_PAYLOAD);
    }

    @Test
    @DisplayName("receiveWebhook: 签名验证失败应返回 401 且不处理业务")
    void receiveWebhook_invalidSignature_shouldReturnUnauthorized() throws Exception {
        doThrow(new IllegalArgumentException("签名无效")).when(webhookVerifier)
                .verify(ISSUES_PAYLOAD, "sha256=bad-signature");

        mockMvc.perform(
                post("/api/v1/github/webhook")
                        .header("X-GitHub-Event", "issues")
                        .header("X-Hub-Signature-256", "sha256=bad-signature")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ISSUES_PAYLOAD))
                .andExpect(status().isUnauthorized());

        verify(webhookService, never()).processIssuesEvent(ISSUES_PAYLOAD);
    }

    @Test
    @DisplayName("receiveWebhook: 非 issues 事件应返回 200 但不处理业务")
    void receiveWebhook_nonIssuesEvent_shouldReturnOkWithoutProcessing() throws Exception {
        doNothing().when(webhookVerifier).verify(ISSUES_PAYLOAD, "sha256=valid-signature");

        mockMvc.perform(
                post("/api/v1/github/webhook")
                        .header("X-GitHub-Event", "push")
                        .header("X-Hub-Signature-256", "sha256=valid-signature")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ISSUES_PAYLOAD))
                .andExpect(status().isOk());

        verify(webhookService, never()).processIssuesEvent(ISSUES_PAYLOAD);
    }
}
