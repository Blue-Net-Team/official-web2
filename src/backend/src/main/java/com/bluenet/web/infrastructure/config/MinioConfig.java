package com.bluenet.web.infrastructure.config;

import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.infrastructure.config.properties.MinioProperties;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO配置类 用于初始化MinIO客户端和自动创建bucket
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class MinioConfig {

    private final MinioProperties minioProperties;

    /**
     * 创建MinioClient Bean
     *
     * @return MinioClient实例
     */
    @Bean
    @ConditionalOnProperty(name = "minio.enabled", havingValue = "true")
    public MinioClient minioClient() {
        String endpoint = minioProperties.getUseSSL()
                ? "https://" + minioProperties.getEndpoint()
                : "http://" + minioProperties.getEndpoint();

        MinioClient client = MinioClient.builder()
                .endpoint(endpoint, minioProperties.getPort(), minioProperties.getUseSSL())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();

        log.info("MinIO client initialized: {}", minioProperties.getUrl());
        return client;
    }

    /**
     * 应用启动时自动创建bucket
     *
     * @param minioClient
     *            MinioClient
     * @return CommandLineRunner
     */
    @Bean
    @ConditionalOnProperty(name = "minio.enabled", havingValue = "true")
    public CommandLineRunner createBuckets(MinioClient minioClient) {
        return args -> {
            for (FileType fileType : FileType.values()) {
                String bucketName = fileType.getValue();
                try {
                    boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
                    if (!exists) {
                        minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                        log.info("Created MinIO bucket: {}", bucketName);
                    } else {
                        log.debug("MinIO bucket already exists: {}", bucketName);
                    }
                } catch (Exception e) {
                    log.error("Failed to create MinIO bucket: {}", bucketName, e);
                    throw new RuntimeException("Failed to initialize MinIO bucket: " + bucketName, e);
                }
            }
        };
    }
}
