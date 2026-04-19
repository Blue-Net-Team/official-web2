package com.bluenet.web.infrastructure.config.properties;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

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

    @Data
    public static class Minio {
        private String endpoint = "localhost";
        private Integer port = 9000;
        private String accessKey = "admin";
        private String secretKey = "admin1234";
        private Boolean useSSL = false;
    }

    @Data
    public static class AliyunOss {
        private String endpoint = "";
        private String accessKeyId = "";
        private String accessKeySecret = "";
    }
}
