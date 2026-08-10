package com.bluenet.web.infrastructure.github;

import com.bluenet.web.infrastructure.config.GitHubAppConfig;
import com.bluenet.web.infrastructure.config.GitHubAppType;
import com.bluenet.web.infrastructure.config.GitHubAppsProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("GitHubAppTokenService 单元测试")
@ExtendWith(MockitoExtension.class)
class GitHubAppTokenServiceTest {

    @Mock
    private GitHubAppsProperties appsProperties;

    private final GitHubJwtGenerator jwtGenerator = new GitHubJwtGenerator();

    private GitHubAppTokenService service;

    @TempDir
    Path tempDir;

    private Path validPrivateKeyPath;

    private GitHubAppConfig repoAppConfig;

    @BeforeEach
    void setUp() throws Exception {
        validPrivateKeyPath = generateTestPrivateKeyFile();

        repoAppConfig = new GitHubAppConfig();
        repoAppConfig.setAppId(123456L);
        repoAppConfig.setPrivateKeyPath(validPrivateKeyPath.toString());
        repoAppConfig.setType(GitHubAppType.REPOSITORY);
        repoAppConfig.setOwner("bluenet-team");
        repoAppConfig.setRepo("bluenet-issues");
        repoAppConfig.setApiBaseUrl("https://api.github.com");

        lenient().when(appsProperties.getApp("issue-sync")).thenReturn(repoAppConfig);

        service = new GitHubAppTokenService(appsProperties, jwtGenerator);
    }

    private Path generateTestPrivateKeyFile() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();

        String base64 = Base64.getEncoder().encodeToString(privateKey.getEncoded());
        StringBuilder pem = new StringBuilder();
        pem.append("-----BEGIN PRIVATE KEY-----\n");
        for (int i = 0; i < base64.length(); i += 64) {
            pem.append(base64, i, Math.min(i + 64, base64.length())).append("\n");
        }
        pem.append("-----END PRIVATE KEY-----\n");

        Path path = tempDir.resolve("test-private-key.pem");
        Files.writeString(path, pem.toString());
        return path;
    }

    private RestTemplate mockRestTemplateWithToken(String installationUrl) {
        RestTemplate mockRestTemplate = mock(RestTemplate.class);

        String installationResponse = "{\"id\": 98765432}";
        ResponseEntity<String> installationEntity = new ResponseEntity<>(installationResponse, HttpStatus.OK);
        when(
                mockRestTemplate.exchange(
                        eq(installationUrl),
                        eq(HttpMethod.GET),
                        any(HttpEntity.class),
                        eq(String.class)))
                                .thenReturn(installationEntity);

        String tokenResponse = "{\"token\": \"ghs_testInstallationToken123\", \"expires_at\": \"2099-01-01T00:00:00Z\"}";
        ResponseEntity<String> tokenEntity = new ResponseEntity<>(tokenResponse, HttpStatus.CREATED);
        when(
                mockRestTemplate.exchange(
                        eq("https://api.github.com/app/installations/98765432/access_tokens"),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        eq(String.class)))
                                .thenReturn(tokenEntity);

        return mockRestTemplate;
    }

    @Nested
    @DisplayName("getAccessToken 方法测试")
    class GetAccessTokenTest {

        @Test
        @DisplayName("TC-005: 仓库级 App 应通过 repos installation 路径获取 Token")
        void getAccessToken_repositoryApp_shouldReturnToken() {
            RestTemplate mockRestTemplate = mockRestTemplateWithToken(
                    "https://api.github.com/repos/bluenet-team/bluenet-issues/installation");

            GitHubAppTokenService spyService = spy(service);
            doReturn(mockRestTemplate).when(spyService).createRestTemplate();

            String token = spyService.getAccessToken("issue-sync");

            assertNotNull(token);
            assertEquals("ghs_testInstallationToken123", token);
        }

        @Test
        @DisplayName("组织级 App 应通过 orgs installation 路径获取 Token")
        void getAccessToken_organizationApp_shouldUseOrgInstallationUrl() {
            GitHubAppConfig orgAppConfig = new GitHubAppConfig();
            orgAppConfig.setAppId(654321L);
            orgAppConfig.setPrivateKeyPath(validPrivateKeyPath.toString());
            orgAppConfig.setType(GitHubAppType.ORGANIZATION);
            orgAppConfig.setOrg("Blue-Net-Team");
            orgAppConfig.setApiBaseUrl("https://api.github.com");
            when(appsProperties.getApp("org-invitation")).thenReturn(orgAppConfig);

            RestTemplate mockRestTemplate = mockRestTemplateWithToken(
                    "https://api.github.com/orgs/Blue-Net-Team/installation");

            GitHubAppTokenService spyService = spy(service);
            doReturn(mockRestTemplate).when(spyService).createRestTemplate();

            String token = spyService.getAccessToken("org-invitation");

            assertEquals("ghs_testInstallationToken123", token);
            verify(mockRestTemplate).exchange(
                    eq("https://api.github.com/orgs/Blue-Net-Team/installation"),
                    eq(HttpMethod.GET),
                    any(HttpEntity.class),
                    eq(String.class));
        }

        @Test
        @DisplayName("App 配置不完整时应抛出 IllegalStateException")
        void getAccessToken_appNotEnabled_shouldThrowException() {
            GitHubAppConfig incompleteConfig = new GitHubAppConfig();
            incompleteConfig.setAppId(null);
            when(appsProperties.getApp("issue-sync")).thenReturn(incompleteConfig);

            IllegalStateException ex = assertThrows(
                    IllegalStateException.class,
                    () -> service.getAccessToken("issue-sync"));
            assertTrue(ex.getMessage().contains("not enabled"));
        }

        @Test
        @DisplayName("TC-009: GitHub API 返回 401 应抛出 RuntimeException")
        void getAccessToken_unauthorized_shouldThrowException() {
            RestTemplate mockRestTemplate = mock(RestTemplate.class);

            String installationResponse = "{\"id\": 98765432}";
            ResponseEntity<String> installationEntity = new ResponseEntity<>(installationResponse, HttpStatus.OK);
            when(
                    mockRestTemplate.exchange(
                            eq("https://api.github.com/repos/bluenet-team/bluenet-issues/installation"),
                            eq(HttpMethod.GET),
                            any(HttpEntity.class),
                            eq(String.class)))
                                    .thenReturn(installationEntity);

            ResponseEntity<String> tokenEntity = new ResponseEntity<>("Bad credentials", HttpStatus.UNAUTHORIZED);
            when(
                    mockRestTemplate.exchange(
                            eq("https://api.github.com/app/installations/98765432/access_tokens"),
                            eq(HttpMethod.POST),
                            any(HttpEntity.class),
                            eq(String.class)))
                                    .thenReturn(tokenEntity);

            GitHubAppTokenService spyService = spy(service);
            doReturn(mockRestTemplate).when(spyService).createRestTemplate();

            RuntimeException ex = assertThrows(
                    RuntimeException.class,
                    () -> spyService.getAccessToken("issue-sync"));
            assertTrue(ex.getMessage().contains("GitHub API error"));
        }
    }

    @Nested
    @DisplayName("私钥文件异常测试")
    class PrivateKeyErrorTest {

        @Test
        @DisplayName("TC-006: 私钥文件不存在应抛出 IllegalStateException")
        void privateKeyFileNotExists_shouldThrowException() {
            repoAppConfig.setPrivateKeyPath(tempDir.resolve("nonexistent.pem").toString());

            IllegalStateException ex = assertThrows(
                    IllegalStateException.class,
                    () -> service.getAccessToken("issue-sync"));
            assertTrue(ex.getMessage().contains("Private key file not found"));
        }

        @Test
        @DisplayName("TC-007: 无效私钥内容应抛出 IllegalStateException")
        void invalidPrivateKeyContent_shouldThrowException() throws Exception {
            Path invalidKeyPath = tempDir.resolve("invalid.pem");
            Files.writeString(invalidKeyPath, "not-a-valid-pem");
            repoAppConfig.setPrivateKeyPath(invalidKeyPath.toString());

            IllegalStateException ex = assertThrows(
                    IllegalStateException.class,
                    () -> service.getAccessToken("issue-sync"));
            assertTrue(ex.getMessage().contains("Failed to load private key"));
        }
    }
}
