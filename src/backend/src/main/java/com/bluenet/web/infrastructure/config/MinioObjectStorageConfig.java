package com.bluenet.web.infrastructure.config;

import com.bluenet.web.infrastructure.config.properties.StorageProperties;
import com.bluenet.web.infrastructure.storage.MinioObjectStorage;
import com.bluenet.web.infrastructure.storage.ObjectLocationResolver;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "storage.enabled", havingValue = "true", matchIfMissing = true)
public class MinioObjectStorageConfig {

    private final StorageProperties storageProperties;

    @Bean
    @ConditionalOnProperty(name = "storage.provider", havingValue = "minio", matchIfMissing = true)
    @ConditionalOnBean(MinioClient.class)
    public MinioObjectStorage minioObjectStorage(MinioClient minioClient,
            ObjectLocationResolver objectLocationResolver) {
        log.info("Creating MinioObjectStorage bean");
        return new MinioObjectStorage(minioClient, objectLocationResolver, storageProperties);
    }
}
