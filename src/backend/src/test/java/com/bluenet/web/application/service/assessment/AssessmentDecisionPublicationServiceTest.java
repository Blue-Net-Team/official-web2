package com.bluenet.web.application.service.assessment;

import com.bluenet.web.application.message.MessageDispatcher;
import com.bluenet.web.application.message.MessageRequest;
import com.bluenet.web.application.message.template.AssessmentDecisionNotificationTemplate;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.MessageChannel;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.vo.AssessmentDecisionVO;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.infrastructure.security.principal.RoleTypeResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * AssessmentDecisionPublicationService 单元测试。
 */
@DisplayName("AssessmentDecisionPublicationService 单元测试")
@ExtendWith(MockitoExtension.class)
class AssessmentDecisionPublicationServiceTest {

    private static final Long USER_ID = 40L;
    private static final Long ASSESSMENT_TIME_ID = 30L;
    private static final Long DECISION_ID = 200L;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MessageDispatcher messageDispatcher;

    @Mock
    private AssessmentDecisionNotificationTemplate notificationTemplate;

    @Mock
    private RoleTypeResolver roleTypeResolver;

    @InjectMocks
    private AssessmentDecisionPublicationService publicationService;

    /**
     * 验证全局最终考核通过且当前为 CANDIDATE 时，升级为 MEMBER 并发送邮件。
     */
    @Test
    @DisplayName("发布：全局最终考核通过应升级为 MEMBER 并发送邮件")
    void publish_globalFinalPassedCandidate_shouldPromoteAndSendEmail() {
        AssessmentTime assessmentTime = createGlobalFinalTime();
        AssessmentDecisionVO decision = createDecision(true);
        User user = createUserWithRoleType(RoleType.CANDIDATE, "candidate@test.com");
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(notificationTemplate.buildHtml("用户" + USER_ID, "全局", 0, "录取"))
                .thenReturn("<p>录取</p>");

        publicationService.publish(decision, assessmentTime);

        verify(userRepository).batchUpdateRole(List.of(USER_ID), RoleType.MEMBER);
        ArgumentCaptor<MessageRequest> captor = ArgumentCaptor.forClass(MessageRequest.class);
        verify(messageDispatcher).dispatchAsync(captor.capture());
        MessageRequest request = captor.getValue();
        assertEquals(MessageChannel.EMAIL, request.channel());
        assertEquals("candidate@test.com", request.recipient());
    }

    /**
     * 验证全局最终考核通过但当前已是 MEMBER 时，不重复升级，仍发送邮件。
     */
    @Test
    @DisplayName("发布：已升级用户应幂等处理")
    void publish_globalFinalPassedMember_shouldNotPromoteButSendEmail() {
        AssessmentTime assessmentTime = createGlobalFinalTime();
        AssessmentDecisionVO decision = createDecision(true);
        User user = createUserWithRoleType(RoleType.MEMBER, "member@test.com");
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(notificationTemplate.buildHtml("用户" + USER_ID, "全局", 0, "录取"))
                .thenReturn("<p>录取</p>");

        publicationService.publish(decision, assessmentTime);

        verify(userRepository, never()).batchUpdateRole(anyList(), any(RoleType.class));
        verify(messageDispatcher).dispatchAsync(any(MessageRequest.class));
    }

    /**
     * 验证全局最终考核通过但当前是方向管理员时，不降级为 MEMBER。
     */
    @Test
    @DisplayName("发布：方向管理员不应被降级")
    void publish_globalFinalPassedDirectionAdmin_shouldNotPromote() {
        AssessmentTime assessmentTime = createGlobalFinalTime();
        AssessmentDecisionVO decision = createDecision(true);
        User user = createUserWithRoleType(RoleType.DIRECTION_ADMIN, "admin@test.com");
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(notificationTemplate.buildHtml("用户" + USER_ID, "全局", 0, "录取"))
                .thenReturn("<p>录取</p>");

        publicationService.publish(decision, assessmentTime);

        verify(userRepository, never()).batchUpdateRole(anyList(), any(RoleType.class));
        verify(messageDispatcher).dispatchAsync(any(MessageRequest.class));
    }

    /**
     * 验证方向考核通过时不升级角色，仅发送邮件。
     */
    @Test
    @DisplayName("发布：方向考核通过不升级角色")
    void publish_directionPassed_shouldSendEmailWithoutPromote() {
        AssessmentTime assessmentTime = createDirectionTime();
        AssessmentDecisionVO decision = createDecision(true);
        User user = createUser(RoleType.CANDIDATE, "candidate@test.com");
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(notificationTemplate.buildHtml("用户" + USER_ID, "计算机视觉", 1, "通过"))
                .thenReturn("<p>通过</p>");

        publicationService.publish(decision, assessmentTime);

        verify(userRepository, never()).batchUpdateRole(anyList(), any(RoleType.class));
        verify(messageDispatcher).dispatchAsync(any(MessageRequest.class));
    }

    /**
     * 验证全局最终考核淘汰时不升级角色，仅发送邮件。
     */
    @Test
    @DisplayName("发布：全局最终考核淘汰不升级角色")
    void publish_globalFinalEliminated_shouldSendEmailWithoutPromote() {
        AssessmentTime assessmentTime = createGlobalFinalTime();
        AssessmentDecisionVO decision = createDecision(false);
        User user = createUser(RoleType.CANDIDATE, "candidate@test.com");
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(notificationTemplate.buildHtml("用户" + USER_ID, "全局", 0, "淘汰"))
                .thenReturn("<p>淘汰</p>");

        publicationService.publish(decision, assessmentTime);

        verify(userRepository, never()).batchUpdateRole(anyList(), any(RoleType.class));
        verify(messageDispatcher).dispatchAsync(any(MessageRequest.class));
    }

    /**
     * 验证用户不存在时抛出 DataNotFound。
     */
    @Test
    @DisplayName("发布：用户不存在应抛出异常")
    void publish_userNotFound_shouldThrowDataNotFound() {
        AssessmentTime assessmentTime = createGlobalFinalTime();
        AssessmentDecisionVO decision = createDecision(true);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThrows(DataNotFound.class, () -> publicationService.publish(decision, assessmentTime));
        verifyNoInteractions(messageDispatcher);
    }

    /**
     * 验证用户无邮箱时跳过邮件发送，但角色升级仍执行。
     */
    @Test
    @DisplayName("发布：无邮箱用户应跳过邮件但升级角色")
    void publish_userWithoutEmail_shouldPromoteButSkipEmail() {
        AssessmentTime assessmentTime = createGlobalFinalTime();
        AssessmentDecisionVO decision = createDecision(true);
        User user = createUserWithRoleType(RoleType.CANDIDATE, null);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        publicationService.publish(decision, assessmentTime);

        verify(userRepository).batchUpdateRole(List.of(USER_ID), RoleType.MEMBER);
        verifyNoInteractions(messageDispatcher);
    }

    private AssessmentDecisionVO createDecision(boolean passed) {
        return AssessmentDecisionVO.builder()
                .id(DECISION_ID)
                .userId(USER_ID)
                .assessmentTimeId(ASSESSMENT_TIME_ID)
                .passed(passed)
                .decidedBy(50L)
                .decidedAt(LocalDateTime.now())
                .build();
    }

    private User createUser(RoleType roleType, String email) {
        User user = User.reconstruct(USER_ID, "password");
        user.setEmail(email);
        user.setRoleId((long) roleType.getLevel());
        user.setNickname("用户" + USER_ID);
        user.setUsername("用户" + USER_ID);
        return user;
    }

    private User createUserWithRoleType(RoleType roleType, String email) {
        User user = createUser(roleType, email);
        when(roleTypeResolver.resolve(user.getRoleId())).thenReturn(roleType);
        return user;
    }

    private AssessmentTime createGlobalFinalTime() {
        return AssessmentTime.reconstruct(
                ASSESSMENT_TIME_ID,
                null,
                0,
                2026,
                null,
                null,
                false,
                null,
                null,
                false);
    }

    private AssessmentTime createDirectionTime() {
        return AssessmentTime.reconstruct(
                ASSESSMENT_TIME_ID,
                Direction.COMPUTER_VISION,
                1,
                2026,
                null,
                null,
                false,
                null,
                null,
                false);
    }
}
