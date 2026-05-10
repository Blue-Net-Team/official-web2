package com.bluenet.web.infrastructure.github;

import com.bluenet.web.infrastructure.config.GitHubAppProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubAppTokenService {

    private static final String GITHUB_INSTALLATION_URL_TEMPLATE = "%s/repos/%s/%s/installation";
    private static final String GITHUB_ACCESS_TOKEN_URL_TEMPLATE = "%s/app/installations/%s/access_tokens";

    private final GitHubAppProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 获取 GitHub App Installation Access Token。
     * <p>
     * 流程： 1. 使用私钥生成 JWT 2. 通过 JWT 查询仓库对应的 installation id 3. 通过 JWT + installation
     * id 换取 Access Token
     */
    public String getInstallationAccessToken() {
        if (!properties.isEnabled()) {
            throw new IllegalStateException("GitHub App configuration is not enabled");
        }

        PrivateKey privateKey = loadPrivateKey();
        String jwt = generateJwt(privateKey);
        RestTemplate restTemplate = createRestTemplate();

        try {
            // 1. 获取 installation id
            long installationId = fetchInstallationId(restTemplate, jwt);

            // 2. 换取 access token
            return fetchAccessToken(restTemplate, jwt, installationId);
        } catch (RuntimeException e) {
            log.error("Failed to get GitHub installation access token", e);
            throw e;
        }
    }

    private long fetchInstallationId(RestTemplate restTemplate, String jwt) {
        String url = String.format(
                GITHUB_INSTALLATION_URL_TEMPLATE,
                properties.getApiBaseUrl(),
                properties.getOwner(),
                properties.getRepo());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwt);
        headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("User-Agent", "BlueNet-Bug-Sync");

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            throw new RuntimeException(
                    "GitHub API error: failed to fetch installation info, status=" + response.getStatusCode());
        }

        try {
            Map<String, Object> body = objectMapper.readValue(response.getBody(), Map.class);
            Number id = (Number) body.get("id");
            if (id == null) {
                throw new RuntimeException("GitHub API response missing installation id");
            }
            return id.longValue();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse GitHub installation response", e);
        }
    }

    private String fetchAccessToken(RestTemplate restTemplate, String jwt, long installationId) {
        String url = String.format(
                GITHUB_ACCESS_TOKEN_URL_TEMPLATE,
                properties.getApiBaseUrl(),
                installationId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwt);
        headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("User-Agent", "BlueNet-Bug-Sync");

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

        if (response.getStatusCode() != HttpStatus.CREATED || response.getBody() == null) {
            throw new RuntimeException(
                    "GitHub API error: failed to fetch access token, status=" + response.getStatusCode());
        }

        try {
            Map<String, Object> body = objectMapper.readValue(response.getBody(), Map.class);
            String token = (String) body.get("token");
            if (token == null || token.isBlank()) {
                throw new RuntimeException("GitHub API response missing access token");
            }
            return token;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse GitHub access token response", e);
        }
    }

    private String generateJwt(PrivateKey privateKey) {
        Instant now = Instant.now();
        Instant issuedAt = now.minus(60, ChronoUnit.SECONDS);
        Instant expiration = now.plus(10, ChronoUnit.MINUTES);

        return Jwts.builder()
                .issuer(properties.getAppId().toString())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiration))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    PrivateKey loadPrivateKey() {
        Path path = Paths.get(properties.getPrivateKeyPath());
        if (!Files.exists(path)) {
            throw new IllegalStateException("Private key file not found: " + path);
        }

        try {
            String pem = Files.readString(path);
            String base64 = pem.replaceAll("-----BEGIN[^-]+-----", "")
                    .replaceAll("-----END[^-]+-----", "")
                    .replaceAll("\\s", "");

            byte[] decoded = Base64.getDecoder().decode(base64);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(spec);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load private key from: " + path, e);
        }
    }

    RestTemplate createRestTemplate() {
        return new RestTemplate();
    }
}
