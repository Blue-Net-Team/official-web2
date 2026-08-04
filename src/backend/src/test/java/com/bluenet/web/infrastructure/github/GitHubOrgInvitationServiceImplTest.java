package com.bluenet.web.infrastructure.github;

import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.vo.GitHubOrgInvitationResult;
import com.bluenet.web.infrastructure.config.GitHubAppConfig;
import com.bluenet.web.infrastructure.config.GitHubAppType;
import com.bluenet.web.infrastructure.config.GitHubAppsProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("GitHubOrgInvitationServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class GitHubOrgInvitationServiceImplTest {

    @Mock
    private GitHubAppsProperties appsProperties;

    @Mock
    private GitHubOrgInvitationClient invitationClient;

    @Mock
    private GitHubOrgTeamResolver teamResolver;

    private GitHubOrgInvitationServiceImpl service;

    private GitHubAppConfig orgAppConfig;

    @BeforeEach
    void setUp() {
        orgAppConfig = new GitHubAppConfig();
        orgAppConfig.setAppId(654321L);
        orgAppConfig.setPrivateKeyPath("/tmp/key.pem");
        orgAppConfig.setType(GitHubAppType.ORGANIZATION);
        orgAppConfig.setOrg("Blue-Net-Team");
        orgAppConfig.setTeamMapping(
                Map.of(
                        "COMPUTER_VISION",
                        "Computer Vision",
                        "EMBEDDED",
                        "Embedded control",
                        "STRUCTURAL_DESIGN",
                        "Structure and Analysis"));

        service = new GitHubOrgInvitationServiceImpl(appsProperties, invitationClient, teamResolver);
    }

    private User buildUser(Long id, String githubId, String email, Direction direction) {
        User user = User.reconstruct(id, "password");
        user.setGithubId(githubId);
        user.setEmail(email);
        user.setDirection(direction);
        return user;
    }

    @Test
    @DisplayName("已绑定 GitHub 的用户应通过 invitee_id 邀请并分配方向团队")
    void invite_withGithubId_shouldUseInviteeIdAndTeam() {
        when(appsProperties.findApp(GitHubAppsProperties.ORG_INVITATION_APP_NAME)).thenReturn(orgAppConfig);
        when(teamResolver.resolveTeamId("Computer Vision")).thenReturn(Optional.of(111L));
        when(invitationClient.createInvitation(eq(12345L), isNull(), eq(List.of(111L))))
                .thenReturn(GitHubOrgInvitationStatus.SENT);

        User user = buildUser(1L, "12345", "a@example.com", Direction.COMPUTER_VISION);
        GitHubOrgInvitationResult result = service.invite(user);

        assertTrue(result.success());
        verify(invitationClient).createInvitation(12345L, null, List.of(111L));
    }

    @Test
    @DisplayName("未绑定 GitHub 的用户应回退到邮箱邀请")
    void invite_withoutGithubId_shouldFallbackToEmail() {
        when(appsProperties.findApp(GitHubAppsProperties.ORG_INVITATION_APP_NAME)).thenReturn(orgAppConfig);
        when(teamResolver.resolveTeamId("Embedded control")).thenReturn(Optional.of(222L));
        when(invitationClient.createInvitation(isNull(), eq("b@example.com"), eq(List.of(222L))))
                .thenReturn(GitHubOrgInvitationStatus.SENT);

        User user = buildUser(2L, null, "b@example.com", Direction.EMBEDDED);
        GitHubOrgInvitationResult result = service.invite(user);

        assertTrue(result.success());
        verify(invitationClient).createInvitation(null, "b@example.com", List.of(222L));
    }

    @Test
    @DisplayName("githubId 非数字时应回退到邮箱邀请")
    void invite_nonNumericGithubId_shouldFallbackToEmail() {
        when(appsProperties.findApp(GitHubAppsProperties.ORG_INVITATION_APP_NAME)).thenReturn(orgAppConfig);
        when(invitationClient.createInvitation(isNull(), eq("c@example.com"), anyList()))
                .thenReturn(GitHubOrgInvitationStatus.SENT);

        User user = buildUser(3L, "not-a-number", "c@example.com", null);
        GitHubOrgInvitationResult result = service.invite(user);

        assertTrue(result.success());
        verify(invitationClient).createInvitation(null, "c@example.com", List.of());
    }

    @Test
    @DisplayName("无 GitHub 绑定且无邮箱时应返回失败且不调用 API")
    void invite_noGithubIdNoEmail_shouldReturnFailure() {
        when(appsProperties.findApp(GitHubAppsProperties.ORG_INVITATION_APP_NAME)).thenReturn(orgAppConfig);

        User user = buildUser(4L, null, null, Direction.COMPUTER_VISION);
        GitHubOrgInvitationResult result = service.invite(user);

        assertFalse(result.success());
        assertEquals("未绑定 GitHub 且无邮箱，无法邀请", result.reason());
        verifyNoInteractions(invitationClient);
    }

    @Test
    @DisplayName("GitHub 返回 422（已受邀或已是成员）应视为成功")
    void invite_alreadyExists_shouldReturnSuccess() {
        when(appsProperties.findApp(GitHubAppsProperties.ORG_INVITATION_APP_NAME)).thenReturn(orgAppConfig);
        when(teamResolver.resolveTeamId(anyString())).thenReturn(Optional.of(111L));
        when(invitationClient.createInvitation(anyLong(), isNull(), anyList()))
                .thenReturn(GitHubOrgInvitationStatus.ALREADY_EXISTS);

        User user = buildUser(5L, "999", "d@example.com", Direction.COMPUTER_VISION);
        GitHubOrgInvitationResult result = service.invite(user);

        assertTrue(result.success());
        assertEquals("用户已在组织中或已被邀请", result.reason());
    }

    @Test
    @DisplayName("邀请 API 异常应转换为失败结果而不抛出异常")
    void invite_apiError_shouldReturnFailureWithoutThrowing() {
        when(appsProperties.findApp(GitHubAppsProperties.ORG_INVITATION_APP_NAME)).thenReturn(orgAppConfig);
        when(teamResolver.resolveTeamId(anyString())).thenReturn(Optional.of(111L));
        when(invitationClient.createInvitation(anyLong(), isNull(), anyList()))
                .thenThrow(new RuntimeException("connection timeout"));

        User user = buildUser(6L, "888", "e@example.com", Direction.COMPUTER_VISION);
        GitHubOrgInvitationResult result = service.invite(user);

        assertFalse(result.success());
        assertTrue(result.reason().contains("connection timeout"));
    }

    @Test
    @DisplayName("邀请功能未启用时应返回失败且不调用 API")
    void invite_notEnabled_shouldReturnFailure() {
        when(appsProperties.findApp(GitHubAppsProperties.ORG_INVITATION_APP_NAME)).thenReturn(null);

        User user = buildUser(7L, "777", "f@example.com", Direction.EMBEDDED);
        GitHubOrgInvitationResult result = service.invite(user);

        assertFalse(result.success());
        assertEquals("GitHub 组织邀请功能未启用", result.reason());
        verifyNoInteractions(invitationClient);
    }

    @Test
    @DisplayName("team 解析失败时仍应发送组织邀请（不带 team）")
    void invite_teamResolutionFails_shouldStillInviteWithoutTeam() {
        when(appsProperties.findApp(GitHubAppsProperties.ORG_INVITATION_APP_NAME)).thenReturn(orgAppConfig);
        when(teamResolver.resolveTeamId("Computer Vision"))
                .thenThrow(new RuntimeException("GitHub API error"));
        when(invitationClient.createInvitation(eq(12345L), isNull(), eq(List.of())))
                .thenReturn(GitHubOrgInvitationStatus.SENT);

        User user = buildUser(8L, "12345", "g@example.com", Direction.COMPUTER_VISION);
        GitHubOrgInvitationResult result = service.invite(user);

        assertTrue(result.success());
        verify(invitationClient).createInvitation(12345L, null, List.of());
    }

    @Test
    @DisplayName("方向无 team 映射时应只发送组织邀请")
    void invite_noTeamMapping_shouldInviteWithoutTeam() {
        orgAppConfig.setTeamMapping(Map.of());
        when(appsProperties.findApp(GitHubAppsProperties.ORG_INVITATION_APP_NAME)).thenReturn(orgAppConfig);
        when(invitationClient.createInvitation(eq(12345L), isNull(), eq(List.of())))
                .thenReturn(GitHubOrgInvitationStatus.SENT);

        User user = buildUser(9L, "12345", "h@example.com", Direction.COMPUTER_VISION);
        GitHubOrgInvitationResult result = service.invite(user);

        assertTrue(result.success());
        verifyNoInteractions(teamResolver);
        verify(invitationClient).createInvitation(12345L, null, List.of());
    }

    @Test
    @DisplayName("异步邀请失败不应抛出异常")
    void inviteAsync_failure_shouldNotThrow() {
        when(appsProperties.findApp(GitHubAppsProperties.ORG_INVITATION_APP_NAME))
                .thenThrow(new RuntimeException("unexpected"));

        User user = buildUser(10L, "555", "i@example.com", Direction.EMBEDDED);

        assertDoesNotThrow(() -> service.inviteAsync(user));
    }
}
