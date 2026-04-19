package com.bluenet.web.infrastructure.config;

import com.bluenet.web.infrastructure.config.properties.StorageProperties;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "storage.enabled", havingValue = "true", matchIfMissing = true)
public class MinioConfig {

    private final StorageProperties storageProperties;

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
