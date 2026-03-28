package com.bluenet.web.infrastructure.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * MinIO配置属性类 用于从application.yml加载MinIO相关配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    /**
     * 是否启用MinIO
     */
    private Boolean enabled = true;

    /**
     * MinIO服务器地址
     */
    private String endpoint = "localhost";

    /**
     * MinIO服务器端口
     */
    private Integer port = 9000;

    /**
     * 访问密钥
     */
    private String accessKey = "admin";

    /**
     * 密钥
     */
    private String secretKey = "admin1234";

    /**
     * 是否使用SSL
     */
    private Boolean useSSL = false;

    /**
     * 获取MinIO服务URL
     *
     * @return MinIO服务完整URL
     */
    public String getUrl() {
        return useSSL ? "https://" : "http://" + endpoint + ":" + port;
    }
}
