package com.bluenet.judge.infrastructure.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云 OSS 判题服务配置类，用于按 {@code storage.provider=aliyun-oss} 初始化 OSS 客户端。
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class AliyunOssJudgeConfig {

    private final ObjectStorageProperties objectStorageProperties;

    /**
     * 创建阿里云 OSS 客户端 Bean。
     *
     * @return OSS 客户端实例
     */
    @Bean(destroyMethod = "shutdown")
    @ConditionalOnProperty(name = "storage.provider", havingValue = "aliyun-oss")
    public OSS aliyunOssClient() {
        ObjectStorageProperties.AliyunOss aliyunOss = objectStorageProperties.aliyunOss();
        if (aliyunOss == null
                || aliyunOss.endpoint() == null
                || aliyunOss.endpoint().isBlank()
                || aliyunOss.accessKeyId() == null
                || aliyunOss.accessKeyId().isBlank()
                || aliyunOss.accessKeySecret() == null
                || aliyunOss.accessKeySecret().isBlank()) {
            throw new IllegalStateException(
                    "Aliyun OSS configuration is incomplete: endpoint, accessKeyId, and accessKeySecret are required");
        }
        OSS client = new OSSClientBuilder().build(
                aliyunOss.endpoint(),
                aliyunOss.accessKeyId(),
                aliyunOss.accessKeySecret());
        log.info("Aliyun OSS client initialized for judge service: {}", aliyunOss.endpoint());
        return client;
    }
}
