package com.bluenet.web.application.service.assessment;

import com.bluenet.web.application.message.MessageDispatcher;
import com.bluenet.web.application.message.MessageRequest;
import com.bluenet.web.application.message.MessageTemplateRegistry;
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
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 决策发布邮件内容集成测试：使用真实模板与注册表，验证最终渲染的主题与 HTML 内容。
 */
class AssessmentDecisionPublicationEmailContentTest {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private MessageDispatcher messageDispatcher;
    private GitHubOrgInvitationService gitHubOrgInvitationService;
    private RoleTypeResolver roleTypeResolver;
    private AssessmentDecisionPublicationService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        roleRepository = mock(RoleRepository.class);
        messageDispatcher = mock(MessageDispatcher.class);
        gitHubOrgInvitationService = mock(GitHubOrgInvitationService.class);
        roleTypeResolver = mock(RoleTypeResolver.class);

        MessageTemplateRegistry registry = new MessageTemplateRegistry();
        service = new AssessmentDecisionPublicationService(
                userRepository,
                roleRepository,
                messageDispatcher,
                new AssessmentDecisionNotificationTemplate(registry),
                new AdmissionWelcomeTemplate(registry),
                roleTypeResolver,
                gitHubOrgInvitationService);
    }

    private User candidateUser(Long userId) {
        User user = User.reconstruct(userId, "password");
        user.setRoleId(1L);
        user.setEmail("candidate@example.com");
        user.setNickname("考生甲");
        return user;
    }

    private AssessmentTime globalFinalAssessmentTime() {
        return AssessmentTime.reconstruct(10L, null, 0, null, null, null, null, null, null, null);
    }

    @Test
    @DisplayName("最终轮通过：邮件主题为欢迎主题，内容包含欢迎语、方向、轮次与 GitHub 邀请提示")
    void publish_globalFinalPassed_shouldSendWelcomeEmailContent() {
        User user = candidateUser(1L);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(roleTypeResolver.resolve(1L)).thenReturn(RoleType.CANDIDATE);
        when(roleRepository.findByName(RoleType.MEMBER.getName()))
                .thenReturn(Optional.of(Role.reconstruct(2L, RoleType.MEMBER.getName())));

        service.publish(
                AssessmentDecision.create(user.getId(), 10L, true, 99L, null),
                globalFinalAssessmentTime());

        ArgumentCaptor<MessageRequest> captor = ArgumentCaptor.forClass(MessageRequest.class);
        verify(messageDispatcher).dispatchAsync(captor.capture());
        MessageRequest request = captor.getValue();

        assertEquals("candidate@example.com", request.recipient());
        assertEquals("[蓝网] 欢迎加入蓝网团队", request.subject());
        assertTrue(request.content().contains("考生甲"));
        assertTrue(request.content().contains("全局"));
        assertTrue(request.content().contains("第0轮"));
        assertTrue(request.content().contains("欢迎加入蓝网团队"));
        assertTrue(request.content().contains("GitHub"));
        assertFalse(request.content().contains("录取"));
    }

    @Test
    @DisplayName("最终轮淘汰：仍发送结果通知邮件（淘汰），不发送欢迎邮件")
    void publish_globalFinalEliminated_shouldSendEliminationEmail() {
        User user = candidateUser(2L);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        service.publish(
                AssessmentDecision.create(user.getId(), 10L, false, 99L, null),
                globalFinalAssessmentTime());

        ArgumentCaptor<MessageRequest> captor = ArgumentCaptor.forClass(MessageRequest.class);
        verify(messageDispatcher).dispatchAsync(captor.capture());
        MessageRequest request = captor.getValue();

        assertEquals("[蓝网] 考核结果通知", request.subject());
        assertTrue(request.content().contains("淘汰"));
        assertFalse(request.content().contains("欢迎加入蓝网团队"));
    }

    @Test
    @DisplayName("非最终轮通过：发送结果通知邮件（通过）")
    void publish_nonFinalPassed_shouldSendPassEmail() {
        User user = candidateUser(3L);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        AssessmentTime directionTime = AssessmentTime.reconstruct(
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

        service.publish(AssessmentDecision.create(user.getId(), 11L, true, 99L, null), directionTime);

        ArgumentCaptor<MessageRequest> captor = ArgumentCaptor.forClass(MessageRequest.class);
        verify(messageDispatcher).dispatchAsync(captor.capture());
        MessageRequest request = captor.getValue();

        assertEquals("[蓝网] 考核结果通知", request.subject());
        assertTrue(request.content().contains("通过"));
        assertTrue(request.content().contains("计算机视觉"));
    }
}
