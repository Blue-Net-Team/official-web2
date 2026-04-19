package com.bluenet.web.infrastructure.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.bluenet.web.infrastructure.config.properties.StorageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云 OSS 配置类，用于按 {@code storage.provider=aliyun-oss} 初始化 OSS 客户端。
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "storage.enabled", havingValue = "true", matchIfMissing = true)
public class AliyunOssConfig {

    private final StorageProperties storageProperties;

    /**
     * 创建阿里云 OSS 客户端 Bean。
     *
     * @return OSS 客户端实例
     */
    @Bean(destroyMethod = "shutdown")
    @ConditionalOnProperty(name = "storage.provider", havingValue = "aliyun-oss")
    public OSS aliyunOssClient() {
        storageProperties.validateAliyunOss();
        StorageProperties.AliyunOss aliyunOss = storageProperties.getAliyunOss();
        OSS client = new OSSClientBuilder().build(
                aliyunOss.getEndpoint(),
                aliyunOss.getAccessKeyId(),
                aliyunOss.getAccessKeySecret());
        log.info("Aliyun OSS client initialized: {}", aliyunOss.getEndpoint());
        return client;
    }
}
