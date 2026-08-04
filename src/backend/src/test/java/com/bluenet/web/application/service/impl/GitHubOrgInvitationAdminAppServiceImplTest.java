package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.result.githuborg.GitHubOrgInvitationAdminResult;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.vo.GitHubOrgInvitationResult;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.domain.service.GitHubOrgInvitationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@DisplayName("GitHubOrgInvitationAdminAppServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class GitHubOrgInvitationAdminAppServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private GitHubOrgInvitationService gitHubOrgInvitationService;

    private GitHubOrgInvitationAdminAppServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new GitHubOrgInvitationAdminAppServiceImpl(userRepository, gitHubOrgInvitationService);
    }

    private User user(Long id) {
        return User.reconstruct(id, "password");
    }

    @Test
    @DisplayName("inviteUser: 应返回领域服务的邀请结果")
    void inviteUser_shouldReturnResult() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(gitHubOrgInvitationService.invite(user(1L)))
                .thenReturn(GitHubOrgInvitationResult.success("邀请已发送"));

        GitHubOrgInvitationAdminResult.Detail detail = service.inviteUser(1L);

        assertEquals(1L, detail.userId());
        assertTrue(detail.success());
        assertEquals("邀请已发送", detail.reason());
    }

    @Test
    @DisplayName("inviteBatch: 混合结果应逐个处理并统计")
    void inviteBatch_mixedResults_shouldSummarize() {
        User user1 = user(1L);
        User user3 = user(3L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        when(userRepository.findById(3L)).thenReturn(Optional.of(user3));
        when(gitHubOrgInvitationService.invite(user1))
                .thenReturn(GitHubOrgInvitationResult.success("邀请已发送"));
        when(gitHubOrgInvitationService.invite(user3))
                .thenReturn(GitHubOrgInvitationResult.failure("未绑定 GitHub 且无邮箱，无法邀请"));

        GitHubOrgInvitationAdminResult.Batch batch = service.inviteBatch(List.of(1L, 2L, 3L));

        assertEquals(3, batch.total());
        assertEquals(1, batch.succeeded());
        assertEquals(2, batch.failed());
        assertEquals(3, batch.details().size());
        assertTrue(batch.details().get(0).success());
        assertFalse(batch.details().get(1).success());
        assertEquals("用户不存在", batch.details().get(1).reason());
        assertFalse(batch.details().get(2).success());
    }
}
