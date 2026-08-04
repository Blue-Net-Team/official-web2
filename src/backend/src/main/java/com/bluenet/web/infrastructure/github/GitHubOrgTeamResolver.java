package com.bluenet.web.infrastructure.github;

import com.bluenet.web.infrastructure.config.GitHubAppConfig;
import com.bluenet.web.infrastructure.config.GitHubAppsProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GitHub Team 名称 -> Team ID 解析器。
 * <p>
 * 通过 {@code GET /orgs/{org}/teams} 获取组织内全部 team 并按名称建立索引，结果缓存在内存中。
 * 采用懒加载：首次解析时才请求 GitHub，避免组织邀请 App 未配置时影响应用启动。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GitHubOrgTeamResolver {

    private static final String TEAMS_URL_TEMPLATE = "%s/orgs/%s/teams?per_page=%d&page=%d";
    private static final int PER_PAGE = 100;
    private static final int MAX_PAGES = 10;

    private final GitHubAppsProperties appsProperties;
    private final GitHubAppTokenService tokenService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    /** team name -> team id 缓存 */
    private final Map<String, Long> teamIdCache = new ConcurrentHashMap<>();

    private volatile boolean cacheLoaded = false;

    /**
     * 按名称解析 team ID。
     *
     * @param teamName
     *            GitHub Team 名称
     * @return team ID；team 不存在或解析失败时返回 empty
     */
    public Optional<Long> resolveTeamId(String teamName) {
        if (teamName == null || teamName.isBlank()) {
            return Optional.empty();
        }
        ensureCacheLoaded();
        return Optional.ofNullable(teamIdCache.get(teamName));
    }

    /**
     * 清空缓存，下次解析时重新拉取。
     */
    public void refresh() {
        teamIdCache.clear();
        cacheLoaded = false;
    }

    private void ensureCacheLoaded() {
        if (cacheLoaded) {
            return;
        }
        synchronized (teamIdCache) {
            if (cacheLoaded) {
                return;
            }
            try {
                loadTeams();
                cacheLoaded = true;
            } catch (RuntimeException e) {
                // 加载失败不缓存部分结果，下次调用重试
                log.error("Failed to load GitHub org teams", e);
                throw e;
            }
        }
    }

    private void loadTeams() {
        GitHubAppConfig config = appsProperties.getApp(GitHubAppsProperties.ORG_INVITATION_APP_NAME);
        String accessToken = tokenService.getAccessToken(GitHubAppsProperties.ORG_INVITATION_APP_NAME);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set("User-Agent", "BlueNet-Backend");
        HttpEntity<Void> request = new HttpEntity<>(headers);

        int page = 1;
        while (page <= MAX_PAGES) {
            String url = String.format(TEAMS_URL_TEMPLATE, config.getApiBaseUrl(), config.getOrg(), PER_PAGE, page);

            ResponseEntity<String> response = createRestTemplate()
                    .exchange(url, HttpMethod.GET, request, String.class);

            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                throw new RuntimeException(
                        "GitHub API error: failed to list org teams, status=" + response.getStatusCode());
            }

            List<GitHubTeamRaw> teams = parseTeams(response.getBody());
            if (teams.isEmpty()) {
                break;
            }
            for (GitHubTeamRaw team : teams) {
                if (team.name() != null && team.id() != null) {
                    teamIdCache.put(team.name(), team.id());
                }
            }
            page++;
        }
        log.info("GitHub org teams loaded: org={}, count={}", config.getOrg(), teamIdCache.size());
    }

    private List<GitHubTeamRaw> parseTeams(String jsonBody) {
        try {
            return objectMapper.readValue(jsonBody, new TypeReference<List<GitHubTeamRaw>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse GitHub teams response", e);
        }
    }

    RestTemplate createRestTemplate() {
        return restTemplate;
    }

    /**
     * GitHub Team 原始响应结构（用于反序列化）。
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GitHubTeamRaw(@JsonProperty("id") Long id, @JsonProperty("name") String name) {
    }
}
