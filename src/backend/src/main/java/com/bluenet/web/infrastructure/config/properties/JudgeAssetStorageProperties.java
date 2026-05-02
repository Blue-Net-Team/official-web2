package com.bluenet.web.infrastructure.config.properties;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 判题资产对象存储配置。
 * <p>
 * 判题资产复用主应用 OSS 连接信息，只使用独立 bucket 隔离 generator、标准解、manifest 和生成后的 .in/.out。
 * </p>
 */
@Data
@Component
@ConfigurationProperties(prefix = "judge.storage")
public class JudgeAssetStorageProperties {
    private String bucket = "bluenet-judge";

    @PostConstruct
    public void validate() {
        if (!StringUtils.hasText(bucket)) {
            throw new IllegalStateException("judge.storage.bucket must not be empty");
        }
    }
}
