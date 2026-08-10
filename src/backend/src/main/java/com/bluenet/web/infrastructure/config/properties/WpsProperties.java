package com.bluenet.web.infrastructure.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * WPS 智能表单配置属性类。
 */
@Data
@Component
@ConfigurationProperties(prefix = "wps")
public class WpsProperties {

    /**
     * WPS 表单绑定验证码（应与 WPS 管理后台配置的 bind_code 一致）
     */
    private String bindCode = "";

    /**
     * Webhook 回调配置
     */
    private Webhook webhook = new Webhook();

    @Data
    public static class Webhook {

        /**
         * WPS 回调接口鉴权密钥（X-WPS-Secret 请求头校验）
         */
        private String secret = "";
    }
}
