package com.bluenet.web.infrastructure.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.bluenet.web.infrastructure.config.properties.StorageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

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
     * 创建阿里云 OSS 内网客户端 Bean（用于后端读写操作）。
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

    /**
     * 创建阿里云 OSS 外网客户端 Bean（仅用于生成预签名 URL）。
     * <p>
     * 当配置了 {@code publicEndpoint} 且与内网 {@code endpoint} 不同时创建独立实例， 否则复用内网客户端实例。
     * </p>
     *
     * @return 外网 OSS 客户端实例
     */
    @Bean(name = "publicOssClient")
    @ConditionalOnProperty(name = "storage.provider", havingValue = "aliyun-oss")
    public OSS publicAliyunOssClient() {
        StorageProperties.AliyunOss aliyunOss = storageProperties.getAliyunOss();
        String publicEndpoint = aliyunOss.getPublicEndpoint();
        String endpoint = aliyunOss.getEndpoint();
        if (!StringUtils.hasText(publicEndpoint) || publicEndpoint.equals(endpoint)) {
            log.debug("Aliyun OSS publicEndpoint not configured or same as endpoint, reusing internal client");
            return aliyunOssClient();
        }
        OSS client = new OSSClientBuilder().build(
                publicEndpoint,
                aliyunOss.getAccessKeyId(),
                aliyunOss.getAccessKeySecret());
        log.info("Aliyun OSS public client initialized: {}", publicEndpoint);
        return client;
    }
}
