package com.bluenet.web.infrastructure.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "system-user")
public class SystemUserProperties {

    private String username = "system";

    private String password = "admin123";

    private String studentId = "000000000000";
}
