package com.bluenet.web.infrastructure.github;

import com.bluenet.web.infrastructure.config.GitHubAppProperties;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GitHub API 端到端验证。
 *
 * 运行前需确保： 1. 私钥文件存在且有效 2. GitHub App 已安装到目标仓库 3. 环境变量或 .env 中配置了 github.app.*
 *
 * 验证完成后请删除创建的测试 Issue。
 */
@Slf4j
class GitHubIntegrationVerification {

    @Test
    @DisplayName("验证 GitHub App Token 生成")
    void verifyTokenGeneration() {
        GitHubAppProperties properties = createProperties();
        GitHubAppTokenService tokenService = new GitHubAppTokenService(properties);

        String token = tokenService.getInstallationAccessToken();

        assertNotNull(token);
        assertTrue(token.startsWith("ghs_"));
        log.info("Installation Access Token: {}", token);
    }

    @Test
    @DisplayName("验证 GitHub Issue 创建")
    void verifyIssueCreation() {
        GitHubAppProperties properties = createProperties();
        GitHubAppTokenService tokenService = new GitHubAppTokenService(properties);
        GitHubIssueClient client = new GitHubIssueClient(properties, tokenService);

        GitHubIssueCreateResult result = client.createIssue(
                "[Test] Integration verification from BlueNet",
                "## Test Body\n\nThis issue was created automatically for integration testing.\n\n- App ID: "
                        + properties.getAppId() + "\n- Time: " + java.time.Instant.now());

        assertNotNull(result);
        assertNotNull(result.number());
        assertNotNull(result.htmlUrl());
        log.info("Created Issue: #{} -> {}", result.number(), result.htmlUrl());
    }

    private GitHubAppProperties createProperties() {
        GitHubAppProperties properties = new GitHubAppProperties();
        properties.setAppId(3615796L);
        properties.setPrivateKeyPath("E:\\code\\code_project\\bluenet_web2.2\\develop\\bluenet-web-bug-sync-pkcs8.pem");
        properties.setOwner("Blue-Net-Team");
        properties.setRepo("official-web2");
        properties.setApiBaseUrl("https://api.github.com");
        properties.setAppBaseUrl("http://localhost:8080");
        return properties;
    }
}
