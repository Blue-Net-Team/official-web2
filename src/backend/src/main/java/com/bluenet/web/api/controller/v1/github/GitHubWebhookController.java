package com.bluenet.web.api.controller.v1.github;

import com.bluenet.web.application.service.impl.GitHubWebhookService;
import com.bluenet.web.infrastructure.github.GitHubWebhookVerifier;
import com.bluenet.web.infrastructure.security.annotation.AccessLevel;
import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * GitHub Webhook 控制器。
 * <p>
 * 接收 GitHub 发送的 Webhook 事件，处理 Issue 状态变更和反向同步。 使用
 * {@code @RequiresPermission(access = AccessLevel.PUBLIC)} 满足权限扫描规范，实际访问控制由
 * HMAC-SHA256 签名验证完成。
 * </p>
 */
@Hidden
@Slf4j
@RestController
@RequestMapping("/api/v1/github")
@RequiredArgsConstructor
public class GitHubWebhookController {

    private static final String SIGNATURE_HEADER = "X-Hub-Signature-256";
    private static final String EVENT_HEADER = "X-GitHub-Event";

    private final GitHubWebhookVerifier webhookVerifier;
    private final GitHubWebhookService webhookService;

    /**
     * 接收 GitHub Webhook 事件。
     * <p>
     * 仅处理 {@code issues} 类型事件，其他类型事件被忽略。 签名验证失败返回 401，业务处理异常始终返回 200（避免 GitHub 重试）。
     * </p>
     * <p>
     * 注意：此端点返回 {@link ResponseEntity} 而非 {@code ResponseMessage}，因为 GitHub Webhook
     * 调用方只关心 HTTP 状态码，不解析响应体。
     * </p>
     *
     * @param request
     *            HTTP 请求对象
     * @return 200 OK（业务异常不向外传播）
     */
    @RequiresPermission(value = "github:webhook-receive", name = "接收 GitHub Webhook", access = AccessLevel.PUBLIC)
    @PostMapping("/webhook")
    public ResponseEntity<Void> receiveWebhook(HttpServletRequest request) {
        String eventType = request.getHeader(EVENT_HEADER);
        String signature = request.getHeader(SIGNATURE_HEADER);
        String payload;

        try {
            payload = readBody(request);
        } catch (IOException e) {
            log.error("读取 Webhook 请求体失败", e);
            return ResponseEntity.badRequest().build();
        }

        log.debug(
                "收到 GitHub Webhook: eventType={}, signature={}",
                eventType,
                signature != null ? "present" : "missing");

        // 签名验证
        try {
            webhookVerifier.verify(payload, signature);
        } catch (IllegalArgumentException e) {
            log.warn("Webhook 签名验证失败: {}", e.getMessage());
            return ResponseEntity.status(401).build();
        }

        // 仅处理 issues 事件
        if (!"issues".equals(eventType)) {
            log.debug("忽略非 issues 类型 Webhook 事件: {}", eventType);
            return ResponseEntity.ok().build();
        }

        // 处理业务逻辑（异常已在 service 内捕获，此处再做一层防御）
        try {
            webhookService.processIssuesEvent(payload);
        } catch (Exception e) {
            log.error("Webhook 业务处理异常", e);
        }

        return ResponseEntity.ok().build();
    }

    String readBody(HttpServletRequest request) throws IOException {
        try (InputStream is = request.getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
