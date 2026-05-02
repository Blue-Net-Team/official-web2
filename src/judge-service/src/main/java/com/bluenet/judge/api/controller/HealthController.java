package com.bluenet.judge.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/**
 * Judge Service 健康检查接口。
 */
@RestController
public class HealthController {
    /**
     * 返回应用级健康状态，便于简单探活。
     *
     * @return 健康状态响应 DTO。
     */
    @GetMapping("/api/v1/judge/health")
    public JudgeHealthResponse health() {
        // 轻量级应用探活接口，用于补充 Actuator health。
        return new JudgeHealthResponse("UP", "judge-service", Instant.now().toString());
    }

    /**
     * Judge Service 健康检查响应。
     *
     * @param status
     *            服务状态。
     * @param service
     *            服务名称。
     * @param checkedAt
     *            检查时间。
     */
    public record JudgeHealthResponse(String status, String service, String checkedAt) {
    }
}
