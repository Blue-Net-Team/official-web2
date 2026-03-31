package com.bluenet.web.infrastructure.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * MinIO 健康检查指示器。
 * <p>
 * 检查 MinIO 对象存储服务的连接状态。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBean(MinioClient.class)
public class MinioHealthIndicator implements HealthIndicator {

    private final MinioClient minioClient;

    @Override
    public Health health() {
        try {
            // 尝试列出存储桶以验证连接
            minioClient.listBuckets();
            log.debug("MinIO health check passed");
            return Health.up()
                    .withDetail("service", "MinIO")
                    .build();
        } catch (Exception e) {
            log.warn("MinIO health check failed: {}", e.getMessage());
            return Health.down()
                    .withDetail("service", "MinIO")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
