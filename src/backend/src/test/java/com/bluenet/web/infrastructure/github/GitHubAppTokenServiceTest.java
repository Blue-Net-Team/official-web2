package com.bluenet.web.infrastructure.github;

import com.bluenet.web.infrastructure.config.GitHubAppProperties;
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
    private GitHubAppProperties properties;

    private GitHubAppTokenService service;

    @TempDir
    Path tempDir;

    private Path validPrivateKeyPath;

    @BeforeEach
    void setUp() throws Exception {
        lenient().when(properties.getAppId()).thenReturn(123456L);
        lenient().when(properties.getOwner()).thenReturn("bluenet-team");
        lenient().when(properties.getRepo()).thenReturn("bluenet-issues");
        lenient().when(properties.getApiBaseUrl()).thenReturn("https://api.github.com");
        lenient().when(properties.isEnabled()).thenReturn(true);

        validPrivateKeyPath = generateTestPrivateKeyFile();
        lenient().when(properties.getPrivateKeyPath()).thenReturn(validPrivateKeyPath.toString());

        service = new GitHubAppTokenService(properties);
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

    @Nested
    @DisplayName("getInstallationAccessToken 方法测试")
    class GetInstallationAccessTokenTest {

        @Test
        @DisplayName("TC-005: 有效私钥应成功生成 Installation Access Token")
        void getInstallationAccessToken_validKey_shouldReturnToken() {
            RestTemplate mockRestTemplate = mock(RestTemplate.class);

            // Mock GET /repos/{owner}/{repo}/installation
            String installationResponse = "{\"id\": 98765432}";
            ResponseEntity<String> installationEntity = new ResponseEntity<>(installationResponse, HttpStatus.OK);
            when(
                    mockRestTemplate.exchange(
                            eq("https://api.github.com/repos/bluenet-team/bluenet-issues/installation"),
                            eq(HttpMethod.GET),
                            any(HttpEntity.class),
                            eq(String.class)))
                                    .thenReturn(installationEntity);

            // Mock POST /app/installations/{id}/access_tokens
            String tokenResponse = "{\"token\": \"ghs_testInstallationToken123\", \"expires_at\": \"2099-01-01T00:00:00Z\"}";
            ResponseEntity<String> tokenEntity = new ResponseEntity<>(tokenResponse, HttpStatus.CREATED);
            when(
                    mockRestTemplate.exchange(
                            eq("https://api.github.com/app/installations/98765432/access_tokens"),
                            eq(HttpMethod.POST),
                            any(HttpEntity.class),
                            eq(String.class)))
                                    .thenReturn(tokenEntity);

            GitHubAppTokenService spyService = spy(service);
            doReturn(mockRestTemplate).when(spyService).createRestTemplate();

            String token = spyService.getInstallationAccessToken();

            assertNotNull(token);
            assertEquals("ghs_testInstallationToken123", token);
        }

        @Test
        @DisplayName("TC-009: GitHub API 返回 401 应抛出 RuntimeException")
        void getInstallationAccessToken_unauthorized_shouldThrowException() {
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
                    () -> spyService.getInstallationAccessToken());
            assertTrue(ex.getMessage().contains("GitHub API error"));
        }
    }

    @Nested
    @DisplayName("私钥文件异常测试")
    class PrivateKeyErrorTest {

        @Test
        @DisplayName("TC-006: 私钥文件不存在应抛出 IllegalStateException")
        void privateKeyFileNotExists_shouldThrowException() {
            when(properties.getPrivateKeyPath()).thenReturn(tempDir.resolve("nonexistent.pem").toString());
            service = new GitHubAppTokenService(properties);

            IllegalStateException ex = assertThrows(
                    IllegalStateException.class,
                    () -> service.getInstallationAccessToken());
            assertTrue(ex.getMessage().contains("Private key file not found"));
        }

        @Test
        @DisplayName("TC-007: 无效私钥内容应抛出 IllegalStateException")
        void invalidPrivateKeyContent_shouldThrowException() throws Exception {
            Path invalidKeyPath = tempDir.resolve("invalid.pem");
            Files.writeString(invalidKeyPath, "not-a-valid-pem");
            when(properties.getPrivateKeyPath()).thenReturn(invalidKeyPath.toString());
            service = new GitHubAppTokenService(properties);

            IllegalStateException ex = assertThrows(
                    IllegalStateException.class,
                    () -> service.getInstallationAccessToken());
            assertTrue(ex.getMessage().contains("Failed to load private key"));
        }
    }
}
