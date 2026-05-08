package com.bluenet.web.infrastructure.config.properties;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;

/**
 * 对象存储配置属性。
 * <p>
 * {@code storage.provider} 决定启用 MinIO 或阿里云 OSS，{@code storage.bucket} 是统一
 * bucket 名称，文件类型只作为 bucket 内的对象 key 前缀。
 * </p>
 */
@Data
@Component
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {

    public static final String PROVIDER_MINIO = "minio";
    public static final String PROVIDER_ALIYUN_OSS = "aliyun-oss";

    private Boolean enabled = true;
    private String provider = PROVIDER_MINIO;
    private String bucket = "bluenet";
    private Minio minio = new Minio();
    private AliyunOss aliyunOss = new AliyunOss();
    private Duration presignedUploadExpiry = Duration.ofMinutes(15);
    private Duration presignedDownloadExpiry = Duration.ofMinutes(10);

    /**
     * 校验跨 provider 共享配置。
     */
    @PostConstruct
    public void validate() {
        if (!Boolean.TRUE.equals(enabled)) {
            return;
        }
        if (!StringUtils.hasText(bucket)) {
            throw new IllegalStateException("storage.bucket must not be empty");
        }
        if (!isMinio() && !isAliyunOss()) {
            throw new IllegalStateException("Unsupported storage.provider: " + provider);
        }
    }

    public boolean isMinio() {
        return PROVIDER_MINIO.equals(normalizedProvider());
    }

    public boolean isAliyunOss() {
        return PROVIDER_ALIYUN_OSS.equals(normalizedProvider());
    }

    public String normalizedProvider() {
        return provider == null ? "" : provider.trim().toLowerCase();
    }

    /**
     * 校验 MinIO 客户端创建所需配置。
     */
    public void validateMinio() {
        if (!StringUtils.hasText(minio.endpoint)) {
            throw new IllegalStateException("storage.minio.endpoint must not be empty");
        }
        if (minio.port == null) {
            throw new IllegalStateException("storage.minio.port must not be empty");
        }
        if (!StringUtils.hasText(minio.accessKey)) {
            throw new IllegalStateException("storage.minio.accessKey must not be empty");
        }
        if (!StringUtils.hasText(minio.secretKey)) {
            throw new IllegalStateException("storage.minio.secretKey must not be empty");
        }
    }

    /**
     * 校验阿里云 OSS 客户端创建所需配置。
     */
    public void validateAliyunOss() {
        if (!StringUtils.hasText(aliyunOss.endpoint)) {
            throw new IllegalStateException("storage.aliyun-oss.endpoint must not be empty");
        }
        if (!StringUtils.hasText(aliyunOss.accessKeyId)) {
            throw new IllegalStateException("storage.aliyun-oss.accessKeyId must not be empty");
        }
        if (!StringUtils.hasText(aliyunOss.accessKeySecret)) {
            throw new IllegalStateException("storage.aliyun-oss.accessKeySecret must not be empty");
        }
    }

    /**
     * MinIO 连接配置。
     */
    @Data
    public static class Minio {
        private String endpoint = "localhost";
        private Integer port = 9000;
        private String accessKey = "admin";
        private String secretKey = "admin1234";
        private Boolean useSSL = false;
        /**
         * 对外可访问的公共 URL，用于替换预签名 URL 中的内部地址。 例如：https://minio.example.com 或
         * http://localhost:9000
         */
        private String publicUrl;
    }

    /**
     * 阿里云 OSS 连接配置。
     */
    @Data
    public static class AliyunOss {
        private String endpoint = "";
        private String accessKeyId = "";
        private String accessKeySecret = "";
        /**
         * 对外可访问的公共 endpoint，用于替换预签名 URL 中的内部地址。 例如：https://oss-cn-hangzhou.aliyuncs.com
         */
        private String publicEndpoint;
    }
}
