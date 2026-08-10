package com.bluenet.web.infrastructure.github;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("GitHubOrgTeamResolver 单元测试")
class GitHubOrgTeamResolverTest {

    @Test
    @DisplayName("解析 GitHub teams 响应时应忽略未知字段如 node_id")
    void parseTeams_shouldIgnoreUnknownFields() throws Exception {
        String jsonBody = """
                [
                  {
                    "id": 10689452,
                    "node_id": "T_kwDOBNL1fM4AqP5Y",
                    "name": "Computer Vision",
                    "slug": "computer-vision",
                    "description": "CV team",
                    "privacy": "closed"
                  },
                  {
                    "id": 10689453,
                    "node_id": "T_kwDOBNL1fM4AqP5Z",
                    "name": "Embedded control"
                  }
                ]
                """;

        GitHubOrgTeamResolver resolver = new GitHubOrgTeamResolver(null, null);
        Method parseTeams = GitHubOrgTeamResolver.class.getDeclaredMethod("parseTeams", String.class);
        parseTeams.setAccessible(true);

        List<?> teams = (List<?>) parseTeams.invoke(resolver, jsonBody);

        assertEquals(2, teams.size());
    }
}
