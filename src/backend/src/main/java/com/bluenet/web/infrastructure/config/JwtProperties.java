package com.bluenet.web.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * JWT配置属性类 用于从application.yml加载JWT相关配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * JWT密钥，建议至少256位
     */
    private String secret = "yoursecretkeyheremustbeatleast256bitslongforsecurity";

    /**
     * Token过期时间（秒），默认12小时
     */
    private Long expiration = 43200L;
}
