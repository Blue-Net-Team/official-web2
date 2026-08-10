package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.githuborg.GitHubOrgBatchInviteRequestDTO;
import com.bluenet.web.application.result.githuborg.GitHubOrgInvitationAdminResult;
import com.bluenet.web.application.service.GitHubOrgInvitationAdminAppService;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.infrastructure.security.principal.WithSecurityPrincipal;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.bluenet.web.testconfig.TestSecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.stream.LongStream;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@DisplayName("AdminGitHubOrgInvitationController 集成测试")
class AdminGitHubOrgInvitationControllerTest extends BaseIntegrationTest {

    private static final long ADMIN_USER_ID = 9999L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GitHubOrgInvitationAdminAppService invitationAdminAppService;

    @AfterEach
    void tearDown() {
        UserCTX.clear();
    }

    @Test
    @DisplayName("inviteUser: 管理员邀请单个用户应返回邀请结果")
    @WithSecurityPrincipal(userId = ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "github-org-invitation:invite" })
    void inviteUser_shouldReturnResult() throws Exception {
        when(invitationAdminAppService.inviteUser(100L))
                .thenReturn(new GitHubOrgInvitationAdminResult.Detail(100L, true, "邀请已发送"));

        mockMvc.perform(post("/api/v1/admin/github-org-invitations/users/{userId}", 100L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(100))
                .andExpect(jsonPath("$.data.success").value(true))
                .andExpect(jsonPath("$.data.reason").value("邀请已发送"));
    }

    @Test
    @DisplayName("inviteUser: 用户不存在应返回 404")
    @WithSecurityPrincipal(userId = ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "github-org-invitation:invite" })
    void inviteUser_userNotFound_shouldReturn404() throws Exception {
        when(invitationAdminAppService.inviteUser(404L))
                .thenThrow(new DataNotFound("用户不存在，ID: 404"));

        mockMvc.perform(post("/api/v1/admin/github-org-invitations/users/{userId}", 404L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("inviteUser: 普通成员访问应返回 403")
    @WithSecurityPrincipal(roleType = "MEMBER", roleId = 3L, permissions = {})
    void inviteUser_asMember_shouldReturn403() throws Exception {
        mockMvc.perform(post("/api/v1/admin/github-org-invitations/users/{userId}", 100L))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("inviteBatch: 批量邀请应返回统一格式的汇总结果")
    @WithSecurityPrincipal(userId = ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "github-org-invitation:invite-batch" })
    void inviteBatch_shouldReturnBatchResult() throws Exception {
        GitHubOrgInvitationAdminResult.Batch batch = new GitHubOrgInvitationAdminResult.Batch(
                3,
                2,
                1,
                List.of(
                        new GitHubOrgInvitationAdminResult.Detail(1L, true, "邀请已发送"),
                        new GitHubOrgInvitationAdminResult.Detail(2L, true, "用户已在组织中或已被邀请"),
                        new GitHubOrgInvitationAdminResult.Detail(3L, false, "未绑定 GitHub 且无邮箱，无法邀请")));
        when(invitationAdminAppService.inviteBatch(List.of(1L, 2L, 3L))).thenReturn(batch);

        GitHubOrgBatchInviteRequestDTO request = GitHubOrgBatchInviteRequestDTO.builder()
                .userIds(List.of(1L, 2L, 3L))
                .build();

        mockMvc.perform(
                post("/api/v1/admin/github-org-invitations/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(3))
                .andExpect(jsonPath("$.data.succeeded").value(2))
                .andExpect(jsonPath("$.data.failed").value(1))
                .andExpect(jsonPath("$.data.details[0].userId").value(1))
                .andExpect(jsonPath("$.data.details[0].success").value(true))
                .andExpect(jsonPath("$.data.details[2].success").value(false))
                .andExpect(jsonPath("$.data.details[2].reason").value("未绑定 GitHub 且无邮箱，无法邀请"));
    }

    @Test
    @DisplayName("inviteBatch: 超过 50 个用户应返回 400")
    @WithSecurityPrincipal(userId = ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "github-org-invitation:invite-batch" })
    void inviteBatch_exceedLimit_shouldReturn400() throws Exception {
        List<Long> userIds = LongStream.rangeClosed(1, 51).boxed().toList();
        GitHubOrgBatchInviteRequestDTO request = GitHubOrgBatchInviteRequestDTO.builder()
                .userIds(userIds)
                .build();

        mockMvc.perform(
                post("/api/v1/admin/github-org-invitations/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("inviteBatch: 空列表应返回 400")
    @WithSecurityPrincipal(userId = ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "github-org-invitation:invite-batch" })
    void inviteBatch_emptyList_shouldReturn400() throws Exception {
        GitHubOrgBatchInviteRequestDTO request = GitHubOrgBatchInviteRequestDTO.builder()
                .userIds(List.of())
                .build();

        mockMvc.perform(
                post("/api/v1/admin/github-org-invitations/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("inviteBatch: 普通成员访问应返回 403")
    @WithSecurityPrincipal(roleType = "MEMBER", roleId = 3L, permissions = {})
    void inviteBatch_asMember_shouldReturn403() throws Exception {
        GitHubOrgBatchInviteRequestDTO request = GitHubOrgBatchInviteRequestDTO.builder()
                .userIds(List.of(1L))
                .build();

        mockMvc.perform(
                post("/api/v1/admin/github-org-invitations/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
