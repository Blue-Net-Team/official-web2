package com.bluenet.web.application.service.assessment;

import com.bluenet.web.application.message.MessageDispatcher;
import com.bluenet.web.application.message.MessageRequest;
import com.bluenet.web.application.message.template.AdmissionWelcomeTemplate;
import com.bluenet.web.application.message.template.AssessmentDecisionNotificationTemplate;
import com.bluenet.web.domain.model.entity.AssessmentDecision;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.entity.Role;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.repository.RoleRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.domain.service.GitHubOrgInvitationService;
import com.bluenet.web.infrastructure.security.principal.RoleTypeResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("AssessmentDecisionPublicationService 单元测试")
@ExtendWith(MockitoExtension.class)
class AssessmentDecisionPublicationServiceTest {

    private static final Long CANDIDATE_ROLE_ID = 1L;
    private static final Long MEMBER_ROLE_ID = 2L;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private MessageDispatcher messageDispatcher;

    @Mock
    private AssessmentDecisionNotificationTemplate notificationTemplate;

    @Mock
    private AdmissionWelcomeTemplate admissionWelcomeTemplate;

    @Mock
    private RoleTypeResolver roleTypeResolver;

    @Mock
    private GitHubOrgInvitationService gitHubOrgInvitationService;

    private AssessmentDecisionPublicationService service;

    @BeforeEach
    void setUp() {
        service = new AssessmentDecisionPublicationService(
                userRepository,
                roleRepository,
                messageDispatcher,
                notificationTemplate,
                admissionWelcomeTemplate,
                roleTypeResolver,
                gitHubOrgInvitationService);

        lenient().when(notificationTemplate.buildHtml(anyString(), anyString(), anyInt(), anyString()))
                .thenReturn("<html>notification</html>");
        lenient().when(admissionWelcomeTemplate.buildHtml(anyString(), anyString(), anyInt()))
                .thenReturn("<html>welcome</html>");
        lenient().when(admissionWelcomeTemplate.getSubject())
                .thenReturn("[蓝网] 欢迎加入蓝网团队");
    }

    private AssessmentTime globalFinalAssessmentTime() {
        // direction=null 且 epoch=0 表示全局最终考核
        return AssessmentTime.reconstruct(
                10L,
                null,
                0,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    private AssessmentTime directionAssessmentTime() {
        return AssessmentTime.reconstruct(
                11L,
                Direction.COMPUTER_VISION,
                1,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    private AssessmentDecision passedDecision(Long userId) {
        return AssessmentDecision.create(userId, 10L, true, 99L, null);
    }

    private User candidateUser(Long userId) {
        User user = User.reconstruct(userId, "password");
        user.setRoleId(CANDIDATE_ROLE_ID);
        user.setEmail("candidate@example.com");
        user.setNickname("考生");
        return user;
    }

    private void stubCandidateLookup(User user) {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        // 非全局最终考核场景下不会触发角色解析，使用 lenient 避免严格校验
        lenient().when(roleTypeResolver.resolve(CANDIDATE_ROLE_ID)).thenReturn(RoleType.CANDIDATE);
    }

    @Test
    @DisplayName("全局最终考核通过：角色升级为 MEMBER 并触发异步 GitHub 邀请")
    void publish_globalFinalPassed_shouldPromoteAndInvite() {
        User user = candidateUser(1L);
        stubCandidateLookup(user);
        when(roleRepository.findByName(RoleType.MEMBER.getName()))
                .thenReturn(Optional.of(Role.reconstruct(MEMBER_ROLE_ID, RoleType.MEMBER.getName())));

        service.publish(passedDecision(user.getId()), globalFinalAssessmentTime());

        assertEquals(MEMBER_ROLE_ID, user.getRoleId());
        verify(userRepository).save(user);
        verify(gitHubOrgInvitationService).inviteAsync(user);
        // 最终轮通过：发送录取欢迎邮件，且不再发送结果通知邮件
        verify(admissionWelcomeTemplate).buildHtml("考生", "全局", 0);
        verify(notificationTemplate, never()).buildHtml(anyString(), anyString(), anyInt(), anyString());
        ArgumentCaptor<MessageRequest> captor = ArgumentCaptor.forClass(MessageRequest.class);
        verify(messageDispatcher).dispatchAsync(captor.capture());
        assertEquals("[蓝网] 欢迎加入蓝网团队", captor.getValue().subject());
        assertEquals("<html>welcome</html>", captor.getValue().content());
        assertEquals("candidate@example.com", captor.getValue().recipient());
    }

    @Test
    @DisplayName("全局最终考核淘汰：仍发送结果通知邮件（淘汰），不发送欢迎邮件")
    void publish_globalFinalEliminated_shouldSendResultNotification() {
        User user = candidateUser(5L);
        stubCandidateLookup(user);
        AssessmentDecision failedDecision = AssessmentDecision.create(user.getId(), 10L, false, 99L, null);

        service.publish(failedDecision, globalFinalAssessmentTime());

        verify(notificationTemplate).buildHtml("考生", "全局", 0, "淘汰");
        verify(admissionWelcomeTemplate, never()).buildHtml(anyString(), anyString(), anyInt());
        ArgumentCaptor<MessageRequest> captor = ArgumentCaptor.forClass(MessageRequest.class);
        verify(messageDispatcher).dispatchAsync(captor.capture());
        assertEquals("<html>notification</html>", captor.getValue().content());
    }

    @Test
    @DisplayName("非最终考核通过：发送结果通知邮件（通过），不发送欢迎邮件")
    void publish_nonFinalPassed_shouldSendResultNotification() {
        User user = candidateUser(6L);
        stubCandidateLookup(user);

        service.publish(passedDecision(user.getId()), directionAssessmentTime());

        verify(notificationTemplate).buildHtml("考生", "计算机视觉", 1, "通过");
        verify(admissionWelcomeTemplate, never()).buildHtml(anyString(), anyString(), anyInt());
    }

    @Test
    @DisplayName("非最终考核通过：不升级角色，不触发 GitHub 邀请")
    void publish_nonFinalPassed_shouldNotInvite() {
        User user = candidateUser(2L);
        stubCandidateLookup(user);

        service.publish(passedDecision(user.getId()), directionAssessmentTime());

        assertEquals(CANDIDATE_ROLE_ID, user.getRoleId());
        verify(userRepository, never()).save(any());
        verify(gitHubOrgInvitationService, never()).inviteAsync(any());
        verify(messageDispatcher).dispatchAsync(any(MessageRequest.class));
    }

    @Test
    @DisplayName("无邮箱考生：跳过欢迎邮件但角色升级仍完成")
    void publish_globalFinalPassedWithoutEmail_shouldSkipEmailButPromote() {
        User user = candidateUser(7L);
        user.setEmail(null);
        stubCandidateLookup(user);
        when(roleRepository.findByName(RoleType.MEMBER.getName()))
                .thenReturn(Optional.of(Role.reconstruct(MEMBER_ROLE_ID, RoleType.MEMBER.getName())));

        service.publish(passedDecision(user.getId()), globalFinalAssessmentTime());

        assertEquals(MEMBER_ROLE_ID, user.getRoleId());
        verify(messageDispatcher, never()).dispatchAsync(any(MessageRequest.class));
    }

    @Test
    @DisplayName("全局最终考核未通过：不升级角色，不触发 GitHub 邀请")
    void publish_globalFinalFailed_shouldNotInvite() {
        User user = candidateUser(3L);
        stubCandidateLookup(user);
        AssessmentDecision failedDecision = AssessmentDecision.create(user.getId(), 10L, false, 99L, null);

        service.publish(failedDecision, globalFinalAssessmentTime());

        assertEquals(CANDIDATE_ROLE_ID, user.getRoleId());
        verify(gitHubOrgInvitationService, never()).inviteAsync(any());
        verify(messageDispatcher).dispatchAsync(any(MessageRequest.class));
    }

    @Test
    @DisplayName("GitHub 邀请异常不应阻塞角色升级与邮件通知")
    void publish_invitationThrows_shouldNotBlockPublish() {
        User user = candidateUser(4L);
        stubCandidateLookup(user);
        when(roleRepository.findByName(RoleType.MEMBER.getName()))
                .thenReturn(Optional.of(Role.reconstruct(MEMBER_ROLE_ID, RoleType.MEMBER.getName())));
        doThrow(new RuntimeException("GitHub API unavailable"))
                .when(gitHubOrgInvitationService)
                .inviteAsync(any());

        assertDoesNotThrow(() -> service.publish(passedDecision(user.getId()), globalFinalAssessmentTime()));

        assertEquals(MEMBER_ROLE_ID, user.getRoleId());
        verify(userRepository).save(user);
        verify(messageDispatcher).dispatchAsync(any(MessageRequest.class));
    }
}
