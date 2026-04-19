package com.bluenet.web.infrastructure.config;

import com.bluenet.web.infrastructure.config.properties.StorageProperties;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO配置类，用于按 {@code storage.provider=minio} 初始化 MinIO 客户端。
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "storage.enabled", havingValue = "true", matchIfMissing = true)
public class MinioConfig {

    private final StorageProperties storageProperties;

    /**
     * 创建 MinioClient Bean。
     *
     * @return MinioClient 实例
     */
    @Bean
    @ConditionalOnProperty(name = "storage.provider", havingValue = "minio", matchIfMissing = true)
    public MinioClient minioClient() {
        storageProperties.validateMinio();
        StorageProperties.Minio minioProperties = storageProperties.getMinio();
        String endpoint = Boolean.TRUE.equals(minioProperties.getUseSSL())
                ? "https://" + minioProperties.getEndpoint()
                : "http://" + minioProperties.getEndpoint();

        MinioClient client = MinioClient.builder()
                .endpoint(endpoint, minioProperties.getPort(), minioProperties.getUseSSL())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();

        log.info("MinIO client initialized: {}:{}", minioProperties.getEndpoint(), minioProperties.getPort());
        return client;
    }
}
