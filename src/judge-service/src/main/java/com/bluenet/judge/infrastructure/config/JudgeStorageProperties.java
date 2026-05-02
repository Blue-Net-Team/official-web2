package com.bluenet.judge.infrastructure.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * 判题资产存储配置。
 *
 * @param bucket
 *            同一 OSS 服务下的判题专用 bucket。
 */
@Validated
@ConfigurationProperties(prefix = "judge.storage")
public record JudgeStorageProperties(
        @NotBlank String bucket) {
}
