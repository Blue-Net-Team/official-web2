package com.bluenet.web.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "github.oauth")
public class GitHubOAuthProperties {

    private String clientId;

    private String clientSecret;

    private String frontendBaseUrl = "http://localhost:3000";
}
