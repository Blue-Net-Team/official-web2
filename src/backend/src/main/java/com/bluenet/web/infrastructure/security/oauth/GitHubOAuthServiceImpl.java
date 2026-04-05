package com.bluenet.web.infrastructure.security.oauth;

import com.bluenet.web.domain.model.vo.GitHubUserInfo;
import com.bluenet.web.domain.service.GitHubOAuthService;
import com.bluenet.web.infrastructure.config.GitHubOAuthProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubOAuthServiceImpl implements GitHubOAuthService {

    private static final String GITHUB_AUTHORIZE_URL = "https://github.com/login/oauth/authorize";
    private static final String GITHUB_TOKEN_URL = "https://github.com/login/oauth/access_token";
    private static final String GITHUB_USER_API = "https://api.github.com/user";

    private final GitHubOAuthProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    public String buildAuthorizeUrl(String state, String redirectUri) {
        return UriComponentsBuilder.fromHttpUrl(GITHUB_AUTHORIZE_URL)
                .queryParam("client_id", properties.getClientId())
                .queryParam("redirect_uri", redirectUri)
                .queryParam("scope", "read:user user:email")
                .queryParam("state", state)
                .toUriString();
    }

    @Override
    public String exchangeCodeForToken(String code, String redirectUri) {
        RestTemplate restTemplate = createRestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", properties.getClientId());
        body.add("client_secret", properties.getClientSecret());
        body.add("code", code);
        body.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    GITHUB_TOKEN_URL,
                    HttpMethod.POST,
                    request,
                    String.class);

            Map<String, Object> result = objectMapper.readValue(
                    response.getBody(),
                    new TypeReference<Map<String, Object>>() {
                    });

            if (result.containsKey("error")) {
                log.error("GitHub token exchange failed: {}", result.get("error_description"));
                throw new RuntimeException("GitHub token exchange failed: " + result.get("error"));
            }

            return (String) result.get("access_token");
        } catch (Exception e) {
            log.error("Failed to exchange GitHub code for token", e);
            throw new RuntimeException("GitHub OAuth token exchange failed", e);
        }
    }

    @Override
    public GitHubUserInfo getUserInfo(String accessToken) {
        RestTemplate restTemplate = createRestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<GitHubApiResponse> response = restTemplate.exchange(
                    GITHUB_USER_API,
                    HttpMethod.GET,
                    request,
                    GitHubApiResponse.class);

            GitHubApiResponse apiResponse = response.getBody();
            if (apiResponse == null) {
                throw new RuntimeException("GitHub API returned empty response");
            }

            String email = apiResponse.getEmail();
            if (email == null) {
                email = fetchPrimaryEmail(restTemplate, accessToken);
            }

            GitHubUserInfo userInfo = new GitHubUserInfo();
            userInfo.setId(apiResponse.getId());
            userInfo.setLogin(apiResponse.getLogin());
            userInfo.setName(apiResponse.getName());
            userInfo.setEmail(email);
            userInfo.setAvatarUrl(apiResponse.getAvatarUrl());
            return userInfo;
        } catch (Exception e) {
            log.error("Failed to fetch GitHub user info", e);
            throw new RuntimeException("Failed to fetch GitHub user info", e);
        }
    }

    protected RestTemplate createRestTemplate() {
        return new RestTemplate();
    }

    protected String fetchPrimaryEmail(RestTemplate restTemplate, String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    "https://api.github.com/user/emails",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    new org.springframework.core.ParameterizedTypeReference<List<Map<String, Object>>>() {
                    });

            if (response.getBody() != null) {
                for (Map<String, Object> emailEntry : response.getBody()) {
                    Boolean primary = (Boolean) emailEntry.get("primary");
                    if (Boolean.TRUE.equals(primary)) {
                        return (String) emailEntry.get("email");
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Failed to fetch GitHub user emails", e);
        }
        return null;
    }

    @Data
    static class GitHubApiResponse {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("login")
        private String login;

        @JsonProperty("name")
        private String name;

        @JsonProperty("email")
        private String email;

        @JsonProperty("avatar_url")
        private String avatarUrl;
    }
}
