package com.bluenet.judge.infrastructure.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * 对象存储连接配置。
 *
 * @param provider
 *            对象存储提供方，可选值为 minio、aliyun-oss。
 * @param minio
 *            MinIO 连接配置。
 * @param aliyunOss
 *            阿里云 OSS 连接配置。
 */
@Validated
@ConfigurationProperties(prefix = "storage")
public record ObjectStorageProperties(
        @NotBlank String provider,
        Minio minio,
        AliyunOss aliyunOss) {

    /**
     * MinIO 连接配置。
     *
     * @param endpoint
     *            MinIO 服务地址。
     * @param port
     *            MinIO 服务端口。
     * @param accessKey
     *            MinIO access key。
     * @param secretKey
     *            MinIO secret key。
     * @param useSsl
     *            是否使用 HTTPS。
     */
    public record Minio(
            @NotBlank String endpoint,
            int port,
            @NotBlank String accessKey,
            @NotBlank String secretKey,
            boolean useSsl) {
    }

    /**
     * 阿里云 OSS 连接配置。
     *
     * @param endpoint
     *            OSS endpoint。
     * @param accessKeyId
     *            AccessKey ID。
     * @param accessKeySecret
     *            AccessKey Secret。
     */
    public record AliyunOss(String endpoint, String accessKeyId, String accessKeySecret) {
    }
}
