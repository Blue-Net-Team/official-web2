package com.bluenet.web.api.controller.v1.github;

import com.bluenet.web.application.service.impl.GitHubWebhookService;
import com.bluenet.web.infrastructure.github.GitHubWebhookVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("GitHubWebhookController 单元测试")
@ExtendWith(MockitoExtension.class)
class GitHubWebhookControllerTest {

    @Mock
    private GitHubWebhookVerifier webhookVerifier;

    @Mock
    private GitHubWebhookService webhookService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private GitHubWebhookController controller;

    @BeforeEach
    void setUp() {
        // controller 通过 @InjectMocks 自动创建
    }

    private ServletInputStream createServletInputStream(String payload) {
        ByteArrayInputStream bis = new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8));
        return new ServletInputStream() {
            @Override
            public int read() {
                return bis.read();
            }

            @Override
            public boolean isFinished() {
                return bis.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
            }
        };
    }

    @Nested
    @DisplayName("receiveWebhook 方法测试")
    class ReceiveWebhookTest {

        @Test
        @DisplayName("TC-014: 有效签名 + issues 事件 → 200")
        void validSignatureAndIssuesEvent_shouldReturn200() throws Exception {
            String payload = "{\"action\":\"closed\",\"issue\":{\"number\":42}}";

            when(request.getHeader("X-GitHub-Event")).thenReturn("issues");
            when(request.getHeader("X-Hub-Signature-256")).thenReturn("sha256=valid-signature");
            when(request.getInputStream()).thenReturn(createServletInputStream(payload));
            doNothing().when(webhookVerifier).verify(payload, "sha256=valid-signature");
            doNothing().when(webhookService).processIssuesEvent(payload);

            ResponseEntity<Void> response = controller.receiveWebhook(request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(webhookVerifier).verify(payload, "sha256=valid-signature");
            verify(webhookService).processIssuesEvent(payload);
        }

        @Test
        @DisplayName("TC-015: 无效签名 → 401")
        void invalidSignature_shouldReturn401() throws Exception {
            String payload = "{\"action\":\"closed\"}";

            when(request.getHeader("X-GitHub-Event")).thenReturn("issues");
            when(request.getHeader("X-Hub-Signature-256")).thenReturn("sha256=invalid-signature");
            when(request.getInputStream()).thenReturn(createServletInputStream(payload));
            doThrow(new IllegalArgumentException("签名验证失败"))
                    .when(webhookVerifier)
                    .verify(payload, "sha256=invalid-signature");

            ResponseEntity<Void> response = controller.receiveWebhook(request);

            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            verify(webhookService, never()).processIssuesEvent(any());
        }

        @Test
        @DisplayName("TC-016: 非 issues 事件类型 → 200 但不处理")
        void nonIssuesEvent_shouldReturn200WithoutProcessing() throws Exception {
            String payload = "{\"action\":\"opened\"}";

            when(request.getHeader("X-GitHub-Event")).thenReturn("pull_request");
            when(request.getHeader("X-Hub-Signature-256")).thenReturn("sha256=valid-signature");
            when(request.getInputStream()).thenReturn(createServletInputStream(payload));
            doNothing().when(webhookVerifier).verify(payload, "sha256=valid-signature");

            ResponseEntity<Void> response = controller.receiveWebhook(request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(webhookService, never()).processIssuesEvent(any());
        }

        @Test
        @DisplayName("缺失 X-GitHub-Event header → 200（视为非 issues 事件）")
        void missingEventHeader_shouldReturn200() throws Exception {
            String payload = "{\"action\":\"closed\"}";

            when(request.getHeader("X-GitHub-Event")).thenReturn(null);
            when(request.getHeader("X-Hub-Signature-256")).thenReturn("sha256=valid-signature");
            when(request.getInputStream()).thenReturn(createServletInputStream(payload));
            doNothing().when(webhookVerifier).verify(payload, "sha256=valid-signature");

            ResponseEntity<Void> response = controller.receiveWebhook(request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(webhookService, never()).processIssuesEvent(any());
        }

        @Test
        @DisplayName("Webhook Service 异常 → 仍返回 200")
        void serviceException_shouldStillReturn200() throws Exception {
            String payload = "{\"action\":\"closed\",\"issue\":{\"number\":42}}";

            when(request.getHeader("X-GitHub-Event")).thenReturn("issues");
            when(request.getHeader("X-Hub-Signature-256")).thenReturn("sha256=valid-signature");
            when(request.getInputStream()).thenReturn(createServletInputStream(payload));
            doNothing().when(webhookVerifier).verify(payload, "sha256=valid-signature");
            doThrow(new RuntimeException("模拟业务异常")).when(webhookService).processIssuesEvent(payload);

            ResponseEntity<Void> response = controller.receiveWebhook(request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
        }
    }
}
