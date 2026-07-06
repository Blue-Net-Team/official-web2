package com.bluenet.web.application.service.impl;

import com.bluenet.web.domain.model.enumerate.RoleType;

import com.bluenet.web.application.TeamPreviewResult;
import com.bluenet.web.application.TeamResult;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.model.entity.AssessmentTeam;
import com.bluenet.web.domain.model.entity.AssessmentTeamMember;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.repository.AssessmentAnswerRepository;
import com.bluenet.web.domain.repository.AssessmentJudgementRepository;
import com.bluenet.web.domain.repository.AssessmentQuestionRepository;
import com.bluenet.web.domain.repository.AssessmentTeamRepository;
import com.bluenet.web.domain.repository.AssessmentTimeRepository;
import com.bluenet.web.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("AssessmentTeamAppServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class AssessmentTeamAppServiceImplTest {

    @Mock
    private AssessmentTeamRepository assessmentTeamRepository;

    @Mock
    private AssessmentTimeRepository assessmentTimeRepository;

    @Mock
    private AssessmentAnswerRepository assessmentAnswerRepository;

    @Mock
    private AssessmentQuestionRepository assessmentQuestionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AssessmentJudgementRepository assessmentJudgementRepository;

    @InjectMocks
    private AssessmentTeamAppServiceImpl assessmentTeamAppService;

    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_TEAM_ID = 10L;
    private static final Long TEST_TIME_ID = 20L;
    private static final Long TEST_QUESTION_ID = 30L;
    private static final Long TEST_NEW_LEADER_ID = 2L;
    private static final String TEST_INVITE_CODE = "ABC123";
    private static final String TEST_TEAM_NAME = "测试队伍";

    private User createTestUser() {
        User user = User.reconstruct(TEST_USER_ID, "password");
        user.setUsername("testuser");
        user.setRoleId((long) RoleType.fromName("MEMBER").getLevel());
        user.setDirection(Direction.COMPUTER_VISION);
        return user;
    }

    private User createTestUser(Long userId, String username) {
        User user = User.reconstruct(userId, "password");
        user.setUsername(username);
        user.setRoleId((long) RoleType.fromName("MEMBER").getLevel());
        user.setDirection(Direction.COMPUTER_VISION);
        return user;
    }

    private AssessmentTime createTestAssessmentTime(Boolean allowTeam) {
        return AssessmentTime.reconstruct(
                TEST_TIME_ID,
                Direction.COMPUTER_VISION,
                1,
                2024,
                LocalDateTime.of(2099, 1, 1, 9, 0),
                LocalDateTime.of(2099, 1, 1, 11, 0),
                false,
                null,
                null,
                allowTeam);
    }

    private AssessmentTeam createTestTeam() {
        return AssessmentTeam.reconstruct(
                TEST_TEAM_ID,
                TEST_TIME_ID,
                TEST_USER_ID,
                TEST_TEAM_NAME,
                TEST_INVITE_CODE,
                AssessmentTeam.TeamStatus.ACTIVE,
                LocalDateTime.now());
    }

    private AssessmentTeamMember createTestMember(Long id, Long userId) {
        return AssessmentTeamMember.reconstruct(id, TEST_TEAM_ID, userId, LocalDateTime.now());
    }

    @Nested
    @DisplayName("createTeam 方法测试")
    class CreateTeamTests {

        @Test
        @DisplayName("正常创建：应返回TeamResult")
        void createTeam_valid_shouldReturnResult() {
            AssessmentTime time = createTestAssessmentTime(true);
            when(assessmentTimeRepository.findById(TEST_TIME_ID)).thenReturn(Optional.of(time));
            when(assessmentTeamRepository.existsByAssessmentTimeIdAndUserId(TEST_TIME_ID, TEST_USER_ID))
                    .thenReturn(false);
            when(
                    assessmentAnswerRepository
                            .countPersonalAnswersByUserIdAndAssessmentTimeId(TEST_USER_ID, TEST_TIME_ID))
                                    .thenReturn(0);
            when(assessmentTeamRepository.findByInviteCode(anyString())).thenReturn(Optional.empty());
            doAnswer(invocation -> {
                AssessmentTeam team = invocation.getArgument(0);
                team.setId(TEST_TEAM_ID);
                return null;
            }).when(assessmentTeamRepository).save(any(AssessmentTeam.class));
            when(assessmentTeamRepository.findMembersByTeamId(TEST_TEAM_ID))
                    .thenReturn(Collections.emptyList());

            TeamResult result = assessmentTeamAppService.createTeam(TEST_USER_ID, TEST_TIME_ID, TEST_TEAM_NAME);

            assertNotNull(result);
            assertEquals(TEST_TEAM_ID, result.id());
            assertEquals(TEST_TIME_ID, result.assessmentTimeId());
            assertEquals(TEST_USER_ID, result.leaderId());
            assertEquals(TEST_TEAM_NAME, result.name());
            verify(assessmentTeamRepository).save(any(AssessmentTeam.class));
        }

        @Test
        @DisplayName("考核时间不存在：应抛出DataNotFound")
        void createTeam_timeNotFound_shouldThrow() {
            when(assessmentTimeRepository.findById(TEST_TIME_ID)).thenReturn(Optional.empty());

            assertThrows(
                    DataNotFound.class,
                    () -> assessmentTeamAppService.createTeam(TEST_USER_ID, TEST_TIME_ID, TEST_TEAM_NAME));
        }

        @Test
        @DisplayName("考核不允许组队：应抛出BadRequest")
        void createTeam_notAllowTeam_shouldThrow() {
            AssessmentTime time = createTestAssessmentTime(false);
            when(assessmentTimeRepository.findById(TEST_TIME_ID)).thenReturn(Optional.of(time));

            BadRequest ex = assertThrows(
                    BadRequest.class,
                    () -> assessmentTeamAppService.createTeam(TEST_USER_ID, TEST_TIME_ID, TEST_TEAM_NAME));
            assertEquals("该考核不允许组队", ex.getMessage());
        }

        @Test
        @DisplayName("考核已结束：应抛出BadRequest")
        void createTeam_timeEnded_shouldThrow() {
            AssessmentTime time = AssessmentTime.reconstruct(
                    TEST_TIME_ID,
                    Direction.COMPUTER_VISION,
                    1,
                    2024,
                    LocalDateTime.of(2020, 1, 1, 9, 0),
                    LocalDateTime.of(2020, 1, 1, 11, 0),
                    false,
                    null,
                    null,
                    true);
            when(assessmentTimeRepository.findById(TEST_TIME_ID)).thenReturn(Optional.of(time));

            BadRequest ex = assertThrows(
                    BadRequest.class,
                    () -> assessmentTeamAppService.createTeam(TEST_USER_ID, TEST_TIME_ID, TEST_TEAM_NAME));
            assertEquals("考核时间已结束", ex.getMessage());
        }

        @Test
        @DisplayName("已加入队伍：应抛出BadRequest")
        void createTeam_alreadyInTeam_shouldThrow() {
            AssessmentTime time = createTestAssessmentTime(true);
            when(assessmentTimeRepository.findById(TEST_TIME_ID)).thenReturn(Optional.of(time));
            when(assessmentTeamRepository.existsByAssessmentTimeIdAndUserId(TEST_TIME_ID, TEST_USER_ID))
                    .thenReturn(true);

            BadRequest ex = assertThrows(
                    BadRequest.class,
                    () -> assessmentTeamAppService.createTeam(TEST_USER_ID, TEST_TIME_ID, TEST_TEAM_NAME));
            assertEquals("您已加入该考核的队伍", ex.getMessage());
        }

        @Test
        @DisplayName("已提交个人答案：应抛出BadRequest")
        void createTeam_hasPersonalAnswer_shouldThrow() {
            AssessmentTime time = createTestAssessmentTime(true);
            when(assessmentTimeRepository.findById(TEST_TIME_ID)).thenReturn(Optional.of(time));
            when(assessmentTeamRepository.existsByAssessmentTimeIdAndUserId(TEST_TIME_ID, TEST_USER_ID))
                    .thenReturn(false);

            when(
                    assessmentAnswerRepository
                            .countPersonalAnswersByUserIdAndAssessmentTimeId(TEST_USER_ID, TEST_TIME_ID))
                                    .thenReturn(1);

            BadRequest ex = assertThrows(
                    BadRequest.class,
                    () -> assessmentTeamAppService.createTeam(TEST_USER_ID, TEST_TIME_ID, TEST_TEAM_NAME));
            assertEquals("您已提交过个人答案，无法创建队伍", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("previewTeam 方法测试")
    class PreviewTeamTests {

        @Test
        @DisplayName("正常预览：应返回TeamPreviewResult")
        void previewTeam_valid_shouldReturnResult() {
            AssessmentTeam team = createTestTeam();
            AssessmentTime time = createTestAssessmentTime(true);
            when(assessmentTeamRepository.findByInviteCode(TEST_INVITE_CODE)).thenReturn(Optional.of(team));
            when(assessmentTimeRepository.findById(TEST_TIME_ID)).thenReturn(Optional.of(time));
            when(assessmentTeamRepository.findMembersByTeamId(TEST_TEAM_ID))
                    .thenReturn(List.of(createTestMember(1L, TEST_USER_ID)));
            when(userRepository.findById(TEST_USER_ID))
                    .thenReturn(Optional.of(createTestUser()));

            TeamPreviewResult result = assessmentTeamAppService.previewTeam(TEST_INVITE_CODE);

            assertNotNull(result);
            assertEquals(TEST_TEAM_ID, result.id());
            assertEquals(TEST_TEAM_NAME, result.name());
            assertEquals(1, result.memberCount());
        }

        @Test
        @DisplayName("邀请码无效：应抛出DataNotFound")
        void previewTeam_invalidCode_shouldThrow() {
            when(assessmentTeamRepository.findByInviteCode("INVALID")).thenReturn(Optional.empty());

            assertThrows(
                    DataNotFound.class,
                    () -> assessmentTeamAppService.previewTeam("INVALID"));
        }

        @Test
        @DisplayName("考核已结束：应抛出BadRequest")
        void previewTeam_timeEnded_shouldThrow() {
            AssessmentTeam team = createTestTeam();
            AssessmentTime time = AssessmentTime.reconstruct(
                    TEST_TIME_ID,
                    Direction.COMPUTER_VISION,
                    1,
                    2024,
                    LocalDateTime.of(2020, 1, 1, 9, 0),
                    LocalDateTime.of(2020, 1, 1, 11, 0),
                    false,
                    null,
                    null,
                    true);
            when(assessmentTeamRepository.findByInviteCode(TEST_INVITE_CODE)).thenReturn(Optional.of(team));
            when(assessmentTimeRepository.findById(TEST_TIME_ID)).thenReturn(Optional.of(time));

            BadRequest ex = assertThrows(
                    BadRequest.class,
                    () -> assessmentTeamAppService.previewTeam(TEST_INVITE_CODE));
            assertEquals("考核时间已结束", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("joinTeam 方法测试")
    class JoinTeamTests {

        @Test
        @DisplayName("正常加入：应返回TeamResult")
        void joinTeam_valid_shouldReturnResult() {
            AssessmentTeam team = createTestTeam();
            AssessmentTime time = createTestAssessmentTime(true);
            when(assessmentTeamRepository.findByInviteCode(TEST_INVITE_CODE)).thenReturn(Optional.of(team));
            when(assessmentTimeRepository.findById(TEST_TIME_ID)).thenReturn(Optional.of(time));
            when(assessmentTeamRepository.existsByAssessmentTimeIdAndUserId(TEST_TIME_ID, TEST_USER_ID))
                    .thenReturn(false);
            when(
                    assessmentAnswerRepository
                            .countPersonalAnswersByUserIdAndAssessmentTimeId(TEST_USER_ID, TEST_TIME_ID))
                                    .thenReturn(0);
            when(assessmentAnswerRepository.countTeamAnswersByUserIdAndAssessmentTimeId(TEST_USER_ID, TEST_TIME_ID))
                    .thenReturn(0);
            when(assessmentTeamRepository.findById(TEST_TEAM_ID)).thenReturn(Optional.of(team));
            when(assessmentTeamRepository.findMembersByTeamId(TEST_TEAM_ID))
                    .thenReturn(List.of(createTestMember(1L, TEST_USER_ID)));

            TeamResult result = assessmentTeamAppService.joinTeam(TEST_USER_ID, TEST_INVITE_CODE);

            assertNotNull(result);
            verify(assessmentTeamRepository).addMember(TEST_TEAM_ID, TEST_USER_ID);
        }

        @Test
        @DisplayName("队伍已解散：应抛出BadRequest")
        void joinTeam_disbanded_shouldThrow() {
            AssessmentTeam team = AssessmentTeam.reconstruct(
                    TEST_TEAM_ID,
                    TEST_TIME_ID,
                    TEST_USER_ID,
                    TEST_TEAM_NAME,
                    TEST_INVITE_CODE,
                    AssessmentTeam.TeamStatus.DISBANDED,
                    LocalDateTime.now());
            when(assessmentTeamRepository.findByInviteCode(TEST_INVITE_CODE)).thenReturn(Optional.of(team));

            BadRequest ex = assertThrows(
                    BadRequest.class,
                    () -> assessmentTeamAppService.joinTeam(TEST_USER_ID, TEST_INVITE_CODE));
            assertEquals("该队伍已解散", ex.getMessage());
        }

        @Test
        @DisplayName("已加入队伍：应抛出BadRequest")
        void joinTeam_alreadyInTeam_shouldThrow() {
            AssessmentTeam team = createTestTeam();
            AssessmentTime time = createTestAssessmentTime(true);
            when(assessmentTeamRepository.findByInviteCode(TEST_INVITE_CODE)).thenReturn(Optional.of(team));
            when(assessmentTimeRepository.findById(TEST_TIME_ID)).thenReturn(Optional.of(time));
            when(assessmentTeamRepository.existsByAssessmentTimeIdAndUserId(TEST_TIME_ID, TEST_USER_ID))
                    .thenReturn(true);

            BadRequest ex = assertThrows(
                    BadRequest.class,
                    () -> assessmentTeamAppService.joinTeam(TEST_USER_ID, TEST_INVITE_CODE));
            assertEquals("您已加入该考核的队伍", ex.getMessage());
        }

        @Test
        @DisplayName("已有队伍答案：应抛出BadRequest")
        void joinTeam_hasTeamAnswer_shouldThrow() {
            AssessmentTeam team = createTestTeam();
            AssessmentTime time = createTestAssessmentTime(true);
            when(assessmentTeamRepository.findByInviteCode(TEST_INVITE_CODE)).thenReturn(Optional.of(team));
            when(assessmentTimeRepository.findById(TEST_TIME_ID)).thenReturn(Optional.of(time));
            when(assessmentTeamRepository.existsByAssessmentTimeIdAndUserId(TEST_TIME_ID, TEST_USER_ID))
                    .thenReturn(false);
            when(
                    assessmentAnswerRepository
                            .countPersonalAnswersByUserIdAndAssessmentTimeId(TEST_USER_ID, TEST_TIME_ID))
                                    .thenReturn(0);
            when(assessmentAnswerRepository.countTeamAnswersByUserIdAndAssessmentTimeId(TEST_USER_ID, TEST_TIME_ID))
                    .thenReturn(1);

            BadRequest ex = assertThrows(
                    BadRequest.class,
                    () -> assessmentTeamAppService.joinTeam(TEST_USER_ID, TEST_INVITE_CODE));
            assertEquals("您已有队伍答案，无法加入其他队伍", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("getMyTeam 方法测试")
    class GetMyTeamTests {

        @Test
        @DisplayName("已加入队伍：应返回TeamResult")
        void getMyTeam_hasTeam_shouldReturnResult() {
            AssessmentTeam team = createTestTeam();
            when(assessmentTeamRepository.findByAssessmentTimeIdAndUserId(TEST_TIME_ID, TEST_USER_ID))
                    .thenReturn(Optional.of(team));
            when(assessmentTeamRepository.findMembersByTeamId(TEST_TEAM_ID))
                    .thenReturn(List.of(createTestMember(1L, TEST_USER_ID)));

            TeamResult result = assessmentTeamAppService.getMyTeam(TEST_USER_ID, TEST_TIME_ID);

            assertNotNull(result);
            assertEquals(TEST_TEAM_ID, result.id());
        }

        @Test
        @DisplayName("未加入队伍：应返回null")
        void getMyTeam_noTeam_shouldReturnNull() {
            when(assessmentTeamRepository.findByAssessmentTimeIdAndUserId(TEST_TIME_ID, TEST_USER_ID))
                    .thenReturn(Optional.empty());

            TeamResult result = assessmentTeamAppService.getMyTeam(TEST_USER_ID, TEST_TIME_ID);

            assertNull(result);
        }
    }

    @Nested
    @DisplayName("leaveTeam 方法测试")
    class LeaveTeamTests {

        @Test
        @DisplayName("正常离开：应成功")
        void leaveTeam_member_shouldSucceed() {
            AssessmentTeam team = createTestTeam();
            when(assessmentTeamRepository.findById(TEST_TEAM_ID)).thenReturn(Optional.of(team));
            when(assessmentTeamRepository.isMember(TEST_TEAM_ID, TEST_NEW_LEADER_ID)).thenReturn(true);

            assessmentTeamAppService.leaveTeam(TEST_NEW_LEADER_ID, TEST_TEAM_ID);

            verify(assessmentTeamRepository).removeMember(TEST_TEAM_ID, TEST_NEW_LEADER_ID);
        }

        @Test
        @DisplayName("队长离开：应抛出Forbidden")
        void leaveTeam_leader_shouldThrow() {
            AssessmentTeam team = createTestTeam();
            when(assessmentTeamRepository.findById(TEST_TEAM_ID)).thenReturn(Optional.of(team));

            Forbidden ex = assertThrows(
                    Forbidden.class,
                    () -> assessmentTeamAppService.leaveTeam(TEST_USER_ID, TEST_TEAM_ID));
            assertEquals("队长不能离开队伍，请先转让队长或解散队伍", ex.getMessage());
        }

        @Test
        @DisplayName("队伍已解散：应抛出BadRequest")
        void leaveTeam_disbanded_shouldThrow() {
            AssessmentTeam team = AssessmentTeam.reconstruct(
                    TEST_TEAM_ID,
                    TEST_TIME_ID,
                    TEST_USER_ID,
                    TEST_TEAM_NAME,
                    TEST_INVITE_CODE,
                    AssessmentTeam.TeamStatus.DISBANDED,
                    LocalDateTime.now());
            when(assessmentTeamRepository.findById(TEST_TEAM_ID)).thenReturn(Optional.of(team));

            BadRequest ex = assertThrows(
                    BadRequest.class,
                    () -> assessmentTeamAppService.leaveTeam(TEST_NEW_LEADER_ID, TEST_TEAM_ID));
            assertEquals("该队伍已解散", ex.getMessage());
        }

        @Test
        @DisplayName("不是队伍成员：应抛出BadRequest")
        void leaveTeam_notMember_shouldThrow() {
            AssessmentTeam team = createTestTeam();
            when(assessmentTeamRepository.findById(TEST_TEAM_ID)).thenReturn(Optional.of(team));
            when(assessmentTeamRepository.isMember(TEST_TEAM_ID, TEST_NEW_LEADER_ID)).thenReturn(false);

            BadRequest ex = assertThrows(
                    BadRequest.class,
                    () -> assessmentTeamAppService.leaveTeam(TEST_NEW_LEADER_ID, TEST_TEAM_ID));
            assertEquals("您不是该队伍的成员", ex.getMessage());
        }

        @Test
        @DisplayName("队伍已提交答案：应抛出Forbidden")
        void leaveTeam_submittedAnswer_shouldThrowForbidden() {
            AssessmentTeam team = createTestTeam();
            when(assessmentTeamRepository.findById(TEST_TEAM_ID)).thenReturn(Optional.of(team));
            when(assessmentTeamRepository.isMember(TEST_TEAM_ID, TEST_NEW_LEADER_ID)).thenReturn(true);
            when(assessmentAnswerRepository.countByTeamId(TEST_TEAM_ID)).thenReturn(1);

            Forbidden ex = assertThrows(
                    Forbidden.class,
                    () -> assessmentTeamAppService.leaveTeam(TEST_NEW_LEADER_ID, TEST_TEAM_ID));
            assertEquals("队伍已提交答案，无法退出", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("transferLeader 方法测试")
    class TransferLeaderTests {

        @Test
        @DisplayName("正常转让：应返回TeamResult")
        void transferLeader_valid_shouldReturnResult() {
            AssessmentTeam team = createTestTeam();
            when(assessmentTeamRepository.findById(TEST_TEAM_ID)).thenReturn(Optional.of(team));
            when(assessmentTeamRepository.isMember(TEST_TEAM_ID, TEST_NEW_LEADER_ID)).thenReturn(true);
            when(assessmentTeamRepository.findById(TEST_TEAM_ID)).thenReturn(Optional.of(team));
            when(assessmentTeamRepository.findMembersByTeamId(TEST_TEAM_ID))
                    .thenReturn(
                            List.of(
                                    createTestMember(1L, TEST_USER_ID),
                                    createTestMember(2L, TEST_NEW_LEADER_ID)));

            TeamResult result = assessmentTeamAppService.transferLeader(TEST_USER_ID, TEST_TEAM_ID, TEST_NEW_LEADER_ID);

            assertNotNull(result);
            verify(assessmentTeamRepository).updateLeader(TEST_TEAM_ID, TEST_NEW_LEADER_ID);
        }

        @Test
        @DisplayName("不是队长：应抛出Forbidden")
        void transferLeader_notLeader_shouldThrow() {
            AssessmentTeam team = createTestTeam();
            when(assessmentTeamRepository.findById(TEST_TEAM_ID)).thenReturn(Optional.of(team));

            Forbidden ex = assertThrows(
                    Forbidden.class,
                    () -> assessmentTeamAppService.transferLeader(TEST_NEW_LEADER_ID, TEST_TEAM_ID, TEST_USER_ID));
            assertEquals("只有队长可以转让队长", ex.getMessage());
        }

        @Test
        @DisplayName("新队长不是成员：应抛出BadRequest")
        void transferLeader_newLeaderNotMember_shouldThrow() {
            AssessmentTeam team = createTestTeam();
            when(assessmentTeamRepository.findById(TEST_TEAM_ID)).thenReturn(Optional.of(team));
            when(assessmentTeamRepository.isMember(TEST_TEAM_ID, TEST_NEW_LEADER_ID)).thenReturn(false);

            BadRequest ex = assertThrows(
                    BadRequest.class,
                    () -> assessmentTeamAppService.transferLeader(TEST_USER_ID, TEST_TEAM_ID, TEST_NEW_LEADER_ID));
            assertEquals("新队长必须是队伍成员", ex.getMessage());
        }

        @Test
        @DisplayName("队伍已解散：应抛出BadRequest")
        void transferLeader_disbanded_shouldThrow() {
            AssessmentTeam team = AssessmentTeam.reconstruct(
                    TEST_TEAM_ID,
                    TEST_TIME_ID,
                    TEST_USER_ID,
                    TEST_TEAM_NAME,
                    TEST_INVITE_CODE,
                    AssessmentTeam.TeamStatus.DISBANDED,
                    LocalDateTime.now());
            when(assessmentTeamRepository.findById(TEST_TEAM_ID)).thenReturn(Optional.of(team));

            BadRequest ex = assertThrows(
                    BadRequest.class,
                    () -> assessmentTeamAppService.transferLeader(TEST_USER_ID, TEST_TEAM_ID, TEST_NEW_LEADER_ID));
            assertEquals("该队伍已解散", ex.getMessage());
        }

        @Test
        @DisplayName("队伍已提交答案：应抛出Forbidden")
        void transferLeader_submittedAnswer_shouldThrowForbidden() {
            AssessmentTeam team = createTestTeam();
            when(assessmentTeamRepository.findById(TEST_TEAM_ID)).thenReturn(Optional.of(team));
            when(assessmentAnswerRepository.countByTeamId(TEST_TEAM_ID)).thenReturn(1);

            Forbidden ex = assertThrows(
                    Forbidden.class,
                    () -> assessmentTeamAppService.transferLeader(TEST_USER_ID, TEST_TEAM_ID, TEST_NEW_LEADER_ID));
            assertEquals("队伍已提交答案，无法转让队长", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("disbandTeam 方法测试")
    class DisbandTeamTests {

        @Test
        @DisplayName("正常解散：应成功")
        void disbandTeam_leader_shouldSucceed() {
            AssessmentTeam team = createTestTeam();
            when(assessmentTeamRepository.findById(TEST_TEAM_ID)).thenReturn(Optional.of(team));

            assessmentTeamAppService.disbandTeam(TEST_USER_ID, TEST_TEAM_ID);

            verify(assessmentTeamRepository).update(any(AssessmentTeam.class));
        }

        @Test
        @DisplayName("不是队长：应抛出Forbidden")
        void disbandTeam_notLeader_shouldThrow() {
            AssessmentTeam team = createTestTeam();
            when(assessmentTeamRepository.findById(TEST_TEAM_ID)).thenReturn(Optional.of(team));

            Forbidden ex = assertThrows(
                    Forbidden.class,
                    () -> assessmentTeamAppService.disbandTeam(TEST_NEW_LEADER_ID, TEST_TEAM_ID));
            assertEquals("只有队长可以解散队伍", ex.getMessage());
        }

        @Test
        @DisplayName("队伍不存在：应抛出DataNotFound")
        void disbandTeam_notFound_shouldThrow() {
            when(assessmentTeamRepository.findById(TEST_TEAM_ID)).thenReturn(Optional.empty());

            assertThrows(
                    DataNotFound.class,
                    () -> assessmentTeamAppService.disbandTeam(TEST_USER_ID, TEST_TEAM_ID));
        }

        @Test
        @DisplayName("队伍已提交答案：应抛出Forbidden")
        void disbandTeam_submittedAnswer_shouldThrowForbidden() {
            AssessmentTeam team = createTestTeam();
            when(assessmentTeamRepository.findById(TEST_TEAM_ID)).thenReturn(Optional.of(team));
            when(assessmentAnswerRepository.countByTeamId(TEST_TEAM_ID)).thenReturn(1);

            Forbidden ex = assertThrows(
                    Forbidden.class,
                    () -> assessmentTeamAppService.disbandTeam(TEST_USER_ID, TEST_TEAM_ID));
            assertEquals("队伍已提交答案，无法解散", ex.getMessage());
        }

        @Test
        @DisplayName("正常解散：应清理答案和评审记录")
        void disbandTeam_leader_shouldCleanupAnswers() {
            AssessmentTeam team = createTestTeam();
            when(assessmentTeamRepository.findById(TEST_TEAM_ID)).thenReturn(Optional.of(team));
            when(assessmentAnswerRepository.countByTeamId(TEST_TEAM_ID)).thenReturn(0);
            List<Long> answerIds = List.of(100L, 101L);
            when(assessmentAnswerRepository.findAnswerIdsByTeamId(TEST_TEAM_ID)).thenReturn(answerIds);

            assessmentTeamAppService.disbandTeam(TEST_USER_ID, TEST_TEAM_ID);

            verify(assessmentJudgementRepository).deleteByAnswerIds(answerIds);
            verify(assessmentAnswerRepository).deleteByTeamId(TEST_TEAM_ID);
            verify(assessmentTeamRepository).update(any(AssessmentTeam.class));
        }
    }
}
