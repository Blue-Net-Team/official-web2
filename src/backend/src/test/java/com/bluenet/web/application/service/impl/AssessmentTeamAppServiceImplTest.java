package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.TeamPreviewResult;
import com.bluenet.web.application.TeamResult;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.model.entity.AssessmentAnswer;
import com.bluenet.web.domain.model.entity.AssessmentQuestion;
import com.bluenet.web.domain.model.entity.AssessmentTeam;
import com.bluenet.web.domain.model.entity.AssessmentTeamMember;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.QuestionType;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.repository.AssessmentAnswerRepository;
import com.bluenet.web.domain.repository.AssessmentJudgementRepository;
import com.bluenet.web.domain.repository.AssessmentQuestionRepository;
import com.bluenet.web.domain.repository.AssessmentTeamRepository;
import com.bluenet.web.domain.repository.AssessmentTimeRepository;
import com.bluenet.web.domain.service.UserDomainService;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

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
    private UserDomainService userDomainService;

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

    private UserVO createTestUser() {
        return UserVO.builder()
                .id(TEST_USER_ID)
                .username("testuser")
                .roleName("MEMBER")
                .direction(Direction.COMPUTER_VISION)
                .build();
    }

    private UserVO createTestUser(Long userId, String username) {
        return UserVO.builder()
                .id(userId)
                .username(username)
                .roleName("MEMBER")
                .direction(Direction.COMPUTER_VISION)
                .build();
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
            try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
                UserVO user = createTestUser();
                mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(user);

                AssessmentTime time = createTestAssessmentTime(true);
                when(assessmentTimeRepository.findById(TEST_TIME_ID)).thenReturn(Optional.of(time));
                when(assessmentTeamRepository.existsByAssessmentTimeIdAndUserId(TEST_TIME_ID, TEST_USER_ID))
                        .thenReturn(false);
                when(assessmentQuestionRepository.findAllByTimeId(anyLong(), any()))
                        .thenReturn(new PageImpl<>(Collections.emptyList()));
                when(assessmentTeamRepository.findByInviteCode(anyString())).thenReturn(Optional.empty());
                doAnswer(invocation -> {
                    AssessmentTeam team = invocation.getArgument(0);
                    team.setId(TEST_TEAM_ID);
                    return null;
                }).when(assessmentTeamRepository).save(any(AssessmentTeam.class));
                when(assessmentTeamRepository.findMembersByTeamId(TEST_TEAM_ID))
                        .thenReturn(Collections.emptyList());

                TeamResult result = assessmentTeamAppService.createTeam(TEST_TIME_ID, TEST_TEAM_NAME);

                assertNotNull(result);
                assertEquals(TEST_TEAM_ID, result.id());
                assertEquals(TEST_TIME_ID, result.assessmentTimeId());
                assertEquals(TEST_USER_ID, result.leaderId());
                assertEquals(TEST_TEAM_NAME, result.name());
                verify(assessmentTeamRepository).save(any(AssessmentTeam.class));
            }
        }

        @Test
        @DisplayName("未登录：应抛出SecurityException")
        void createTeam_notAuthenticated_shouldThrow() {
            try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
                mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(null);

                SecurityException ex = assertThrows(
                        SecurityException.class,
                        () -> assessmentTeamAppService.createTeam(TEST_TIME_ID, TEST_TEAM_NAME));
                assertEquals("未登录", ex.getMessage());
            }
        }

        @Test
        @DisplayName("考核时间不存在：应抛出DataNotFound")
        void createTeam_timeNotFound_shouldThrow() {
            try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
                mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createTestUser());
                when(assessmentTimeRepository.findById(TEST_TIME_ID)).thenReturn(Optional.empty());

                assertThrows(
                        DataNotFound.class,
                        () -> assessmentTeamAppService.createTeam(TEST_TIME_ID, TEST_TEAM_NAME));
            }
        }

        @Test
        @DisplayName("考核不允许组队：应抛出BadRequest")
        void createTeam_notAllowTeam_shouldThrow() {
            try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
                mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createTestUser());
                AssessmentTime time = createTestAssessmentTime(false);
                when(assessmentTimeRepository.findById(TEST_TIME_ID)).thenReturn(Optional.of(time));

                BadRequest ex = assertThrows(
                        BadRequest.class,
                        () -> assessmentTeamAppService.createTeam(TEST_TIME_ID, TEST_TEAM_NAME));
                assertEquals("该考核不允许组队", ex.getMessage());
            }
        }

        @Test
        @DisplayName("考核已结束：应抛出BadRequest")
        void createTeam_timeEnded_shouldThrow() {
            try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
                mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createTestUser());
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
                        () -> assessmentTeamAppService.createTeam(TEST_TIME_ID, TEST_TEAM_NAME));
                assertEquals("考核时间已结束", ex.getMessage());
            }
        }

        @Test
        @DisplayName("已加入队伍：应抛出BadRequest")
        void createTeam_alreadyInTeam_shouldThrow() {
            try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
                mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createTestUser());
                AssessmentTime time = createTestAssessmentTime(true);
                when(assessmentTimeRepository.findById(TEST_TIME_ID)).thenReturn(Optional.of(time));
                when(assessmentTeamRepository.existsByAssessmentTimeIdAndUserId(TEST_TIME_ID, TEST_USER_ID))
                        .thenReturn(true);

                BadRequest ex = assertThrows(
                        BadRequest.class,
                        () -> assessmentTeamAppService.createTeam(TEST_TIME_ID, TEST_TEAM_NAME));
                assertEquals("您已加入该考核的队伍", ex.getMessage());
            }
        }

        @Test
        @DisplayName("已提交个人答案：应抛出BadRequest")
        void createTeam_hasPersonalAnswer_shouldThrow() {
            try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
                mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createTestUser());
                AssessmentTime time = createTestAssessmentTime(true);
                when(assessmentTimeRepository.findById(TEST_TIME_ID)).thenReturn(Optional.of(time));
                when(assessmentTeamRepository.existsByAssessmentTimeIdAndUserId(TEST_TIME_ID, TEST_USER_ID))
                        .thenReturn(false);

                AssessmentQuestion question = AssessmentQuestion.reconstruct(
                        TEST_QUESTION_ID,
                        TEST_TIME_ID,
                        1,
                        QuestionType.FILE_UPLOAD,
                        null,
                        null,
                        null,
                        null);
                when(assessmentQuestionRepository.findAllByTimeId(anyLong(), any()))
                        .thenReturn(new PageImpl<>(List.of(question)));
                AssessmentAnswer answer = AssessmentAnswer.reconstruct(
                        100L,
                        TEST_USER_ID,
                        TEST_QUESTION_ID,
                        "content",
                        null,
                        null,
                        LocalDateTime.now(),
                        null);
                when(assessmentAnswerRepository.findByUserIdAndQuestionId(TEST_USER_ID, TEST_QUESTION_ID))
                        .thenReturn(Optional.of(answer));

                BadRequest ex = assertThrows(
                        BadRequest.class,
                        () -> assessmentTeamAppService.createTeam(TEST_TIME_ID, TEST_TEAM_NAME));
                assertEquals("您已提交过个人答案，无法创建队伍", ex.getMessage());
            }
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
            when(userDomainService.getUser(TEST_USER_ID))
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
            try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
                mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createTestUser());

                AssessmentTeam team = createTestTeam();
                AssessmentTime time = createTestAssessmentTime(true);
                when(assessmentTeamRepository.findByInviteCode(TEST_INVITE_CODE)).thenReturn(Optional.of(team));
                when(assessmentTimeRepository.findById(TEST_TIME_ID)).thenReturn(Optional.of(time));
                when(assessmentTeamRepository.existsByAssessmentTimeIdAndUserId(TEST_TIME_ID, TEST_USER_ID))
                        .thenReturn(false);
                when(assessmentQuestionRepository.findAllByTimeId(anyLong(), any()))
                        .thenReturn(new PageImpl<>(Collections.emptyList()));
                when(assessmentTeamRepository.findById(TEST_TEAM_ID)).thenReturn(Optional.of(team));
                when(assessmentTeamRepository.findMembersByTeamId(TEST_TEAM_ID))
                        .thenReturn(List.of(createTestMember(1L, TEST_USER_ID)));

                TeamResult result = assessmentTeamAppService.joinTeam(TEST_INVITE_CODE);

                assertNotNull(result);
                verify(assessmentTeamRepository).addMember(TEST_TEAM_ID, TEST_USER_ID);
            }
        }

        @Test
        @DisplayName("队伍已解散：应抛出BadRequest")
        void joinTeam_disbanded_shouldThrow() {
            try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
                mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createTestUser());

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
                        () -> assessmentTeamAppService.joinTeam(TEST_INVITE_CODE));
                assertEquals("该队伍已解散", ex.getMessage());
            }
        }

        @Test
        @DisplayName("已加入队伍：应抛出BadRequest")
        void joinTeam_alreadyInTeam_shouldThrow() {
            try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
                mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createTestUser());

                AssessmentTeam team = createTestTeam();
                AssessmentTime time = createTestAssessmentTime(true);
                when(assessmentTeamRepository.findByInviteCode(TEST_INVITE_CODE)).thenReturn(Optional.of(team));
                when(assessmentTimeRepository.findById(TEST_TIME_ID)).thenReturn(Optional.of(time));
                when(assessmentTeamRepository.existsByAssessmentTimeIdAndUserId(TEST_TIME_ID, TEST_USER_ID))
                        .thenReturn(true);

                BadRequest ex = assertThrows(
                        BadRequest.class,
                        () -> assessmentTeamAppService.joinTeam(TEST_INVITE_CODE));
                assertEquals("您已加入该考核的队伍", ex.getMessage());
            }
        }

        @Test
        @DisplayName("已有队伍答案：应抛出BadRequest")
        void joinTeam_hasTeamAnswer_shouldThrow() {
            try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
                mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createTestUser());

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
                        () -> assessmentTeamAppService.joinTeam(TEST_INVITE_CODE));
                assertEquals("您已有队伍答案，无法加入其他队伍", ex.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("getMyTeam 方法测试")
    class GetMyTeamTests {

        @Test
        @DisplayName("已加入队伍：应返回TeamResult")
        void getMyTeam_hasTeam_shouldReturnResult() {
            try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
                mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createTestUser());

                AssessmentTeam team = createTestTeam();
                when(assessmentTeamRepository.findByAssessmentTimeIdAndUserId(TEST_TIME_ID, TEST_USER_ID))
                        .thenReturn(Optional.of(team));
                when(assessmentTeamRepository.findMembersByTeamId(TEST_TEAM_ID))
                        .thenReturn(List.of(createTestMember(1L, TEST_USER_ID)));

                TeamResult result = assessmentTeamAppService.getMyTeam(TEST_TIME_ID);

                assertNotNull(result);
                assertEquals(TEST_TEAM_ID, result.id());
            }
        }

        @Test
        @DisplayName("未加入队伍：应返回null")
        void getMyTeam_noTeam_shouldReturnNull() {
            try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
                mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createTestUser());
                when(assessmentTeamRepository.findByAssessmentTimeIdAndUserId(TEST_TIME_ID, TEST_USER_ID))
                        .thenReturn(Optional.empty());

                TeamResult result = assessmentTeamAppService.getMyTeam(TEST_TIME_ID);

                assertNull(result);
            }
        }

        @Test
        @DisplayName("未登录：应抛出SecurityException")
        void getMyTeam_notAuthenticated_shouldThrow() {
            try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
                mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(null);

                SecurityException ex = assertThrows(
                        SecurityException.class,
                        () -> assessmentTeamAppService.getMyTeam(TEST_TIME_ID));
                assertEquals("未登录", ex.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("leaveTeam 方法测试")
    class LeaveTeamTests {

        @Test
        @DisplayName("正常离开：应成功")
        void leaveTeam_member_shouldSucceed() {
            try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
                UserVO user = createTestUser(TEST_NEW_LEADER_ID, "member");
                mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(user);

                AssessmentTeam team = createTestTeam();
                when(assessmentTeamRepository.findById(TEST_TEAM_ID)).thenReturn(Optional.of(team));
                when(assessmentTeamRepository.isMember(TEST_TEAM_ID, TEST_NEW_LEADER_ID)).thenReturn(true);

                assessmentTeamAppService.leaveTeam(TEST_TEAM_ID);

                verify(assessmentTeamRepository).removeMember(TEST_TEAM_ID, TEST_NEW_LEADER_ID);
            }
        }

        @Test
        @DisplayName("队长离开：应抛出Forbidden")
        void leaveTeam_leader_shouldThrow() {
            try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
                mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createTestUser());

                AssessmentTeam team = createTestTeam();
                when(assessmentTeamRepository.findById(TEST_TEAM_ID)).thenReturn(Optional.of(team));

                Forbidden ex = assertThrows(
                        Forbidden.class,
                        () -> assessmentTeamAppService.leaveTeam(TEST_TEAM_ID));
                assertEquals("队长不能离开队伍，请先转让队长或解散队伍", ex.getMessage());
            }
        }

        @Test
        @DisplayName("队伍已解散：应抛出BadRequest")
        void leaveTeam_disbanded_shouldThrow() {
            try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
                UserVO user = createTestUser(TEST_NEW_LEADER_ID, "member");
                mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(user);

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
                        () -> assessmentTeamAppService.leaveTeam(TEST_TEAM_ID));
                assertEquals("该队伍已解散", ex.getMessage());
            }
        }

        @Test
        @DisplayName("不是队伍成员：应抛出BadRequest")
        void leaveTeam_notMember_shouldThrow() {
            try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
                UserVO user = createTestUser(TEST_NEW_LEADER_ID, "member");
                mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(user);

                AssessmentTeam team = createTestTeam();
                when(assessmentTeamRepository.findById(TEST_TEAM_ID)).thenReturn(Optional.of(team));
                when(assessmentTeamRepository.isMember(TEST_TEAM_ID, TEST_NEW_LEADER_ID)).thenReturn(false);

                BadRequest ex = assertThrows(
                        BadRequest.class,
                        () -> assessmentTeamAppService.leaveTeam(TEST_TEAM_ID));
                assertEquals("您不是该队伍的成员", ex.getMessage());
            }
        }

        @Test
        @DisplayName("队伍已提交答案：应抛出Forbidden")
        void leaveTeam_submittedAnswer_shouldThrowForbidden() {
            try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
                UserVO user = createTestUser(TEST_NEW_LEADER_ID, "member");
                mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(user);

                AssessmentTeam team = createTestTeam();
                when(assessmentTeamRepository.findById(TEST_TEAM_ID)).thenReturn(Optional.of(team));
                when(assessmentTeamRepository.isMember(TEST_TEAM_ID, TEST_NEW_LEADER_ID)).thenReturn(true);
                when(assessmentAnswerRepository.countByTeamId(TEST_TEAM_ID)).thenReturn(1);

                Forbidden ex = assertThrows(
                        Forbidden.class,
                        () -> assessmentTeamAppService.leaveTeam(TEST_TEAM_ID));
                assertEquals("队伍已提交答案，无法退出", ex.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("transferLeader 方法测试")
    class TransferLeaderTests {

        @Test
        @DisplayName("正常转让：应返回TeamResult")
        void transferLeader_valid_shouldReturnResult() {
            try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
                mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createTestUser());

                AssessmentTeam team = createTestTeam();
                when(assessmentTeamRepository.findById(TEST_TEAM_ID)).thenReturn(Optional.of(team));
                when(assessmentTeamRepository.isMember(TEST_TEAM_ID, TEST_NEW_LEADER_ID)).thenReturn(true);
                when(assessmentTeamRepository.findById(TEST_TEAM_ID)).thenReturn(Optional.of(team));
                when(assessmentTeamRepository.findMembersByTeamId(TEST_TEAM_ID))
                        .thenReturn(
                                List.of(
                                        createTestMember(1L, TEST_USER_ID),
                                        createTestMember(2L, TEST_NEW_LEADER_ID)));

                TeamResult result = assessmentTeamAppService.transferLeader(TEST_TEAM_ID, TEST_NEW_LEADER_ID);

                assertNotNull(result);
                verify(assessmentTeamRepository).updateLeader(TEST_TEAM_ID, TEST_NEW_LEADER_ID);
            }
        }

        @Test
        @DisplayName("不是队长：应抛出Forbidden")
        void transferLeader_notLeader_shouldThrow() {
            try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
                UserVO user = createTestUser(TEST_NEW_LEADER_ID, "member");
                mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(user);

                AssessmentTeam team = createTestTeam();
                when(assessmentTeamRepository.findById(TEST_TEAM_ID)).thenReturn(Optional.of(team));

                Forbidden ex = assertThrows(
                        Forbidden.class,
                        () -> assessmentTeamAppService.transferLeader(TEST_TEAM_ID, TEST_USER_ID));
                assertEquals("只有队长可以转让队长", ex.getMessage());
            }
        }

        @Test
        @DisplayName("新队长不是成员：应抛出BadRequest")
        void transferLeader_newLeaderNotMember_shouldThrow() {
            try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
                mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createTestUser());

                AssessmentTeam team = createTestTeam();
                when(assessmentTeamRepository.findById(TEST_TEAM_ID)).thenReturn(Optional.of(team));
                when(assessmentTeamRepository.isMember(TEST_TEAM_ID, TEST_NEW_LEADER_ID)).thenReturn(false);

                BadRequest ex = assertThrows(
                        BadRequest.class,
                        () -> assessmentTeamAppService.transferLeader(TEST_TEAM_ID, TEST_NEW_LEADER_ID));
                assertEquals("新队长必须是队伍成员", ex.getMessage());
            }
        }

        @Test
        @DisplayName("队伍已解散：应抛出BadRequest")
        void transferLeader_disbanded_shouldThrow() {
            try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
                mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createTestUser());

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
                        () -> assessmentTeamAppService.transferLeader(TEST_TEAM_ID, TEST_NEW_LEADER_ID));
                assertEquals("该队伍已解散", ex.getMessage());
            }
        }

        @Test
        @DisplayName("队伍已提交答案：应抛出Forbidden")
        void transferLeader_submittedAnswer_shouldThrowForbidden() {
            try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
                mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createTestUser());

                AssessmentTeam team = createTestTeam();
                when(assessmentTeamRepository.findById(TEST_TEAM_ID)).thenReturn(Optional.of(team));
                when(assessmentAnswerRepository.countByTeamId(TEST_TEAM_ID)).thenReturn(1);

                Forbidden ex = assertThrows(
                        Forbidden.class,
                        () -> assessmentTeamAppService.transferLeader(TEST_TEAM_ID, TEST_NEW_LEADER_ID));
                assertEquals("队伍已提交答案，无法转让队长", ex.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("disbandTeam 方法测试")
    class DisbandTeamTests {

        @Test
        @DisplayName("正常解散：应成功")
        void disbandTeam_leader_shouldSucceed() {
            try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
                mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createTestUser());

                AssessmentTeam team = createTestTeam();
                when(assessmentTeamRepository.findById(TEST_TEAM_ID)).thenReturn(Optional.of(team));

                assessmentTeamAppService.disbandTeam(TEST_TEAM_ID);

                verify(assessmentTeamRepository).update(any(AssessmentTeam.class));
            }
        }

        @Test
        @DisplayName("不是队长：应抛出Forbidden")
        void disbandTeam_notLeader_shouldThrow() {
            try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
                UserVO user = createTestUser(TEST_NEW_LEADER_ID, "member");
                mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(user);

                AssessmentTeam team = createTestTeam();
                when(assessmentTeamRepository.findById(TEST_TEAM_ID)).thenReturn(Optional.of(team));

                Forbidden ex = assertThrows(
                        Forbidden.class,
                        () -> assessmentTeamAppService.disbandTeam(TEST_TEAM_ID));
                assertEquals("只有队长可以解散队伍", ex.getMessage());
            }
        }

        @Test
        @DisplayName("队伍不存在：应抛出DataNotFound")
        void disbandTeam_notFound_shouldThrow() {
            try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
                mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createTestUser());
                when(assessmentTeamRepository.findById(TEST_TEAM_ID)).thenReturn(Optional.empty());

                assertThrows(
                        DataNotFound.class,
                        () -> assessmentTeamAppService.disbandTeam(TEST_TEAM_ID));
            }
        }

        @Test
        @DisplayName("队伍已提交答案：应抛出Forbidden")
        void disbandTeam_submittedAnswer_shouldThrowForbidden() {
            try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
                mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createTestUser());

                AssessmentTeam team = createTestTeam();
                when(assessmentTeamRepository.findById(TEST_TEAM_ID)).thenReturn(Optional.of(team));
                when(assessmentAnswerRepository.countByTeamId(TEST_TEAM_ID)).thenReturn(1);

                Forbidden ex = assertThrows(
                        Forbidden.class,
                        () -> assessmentTeamAppService.disbandTeam(TEST_TEAM_ID));
                assertEquals("队伍已提交答案，无法解散", ex.getMessage());
            }
        }

        @Test
        @DisplayName("正常解散：应清理答案和评审记录")
        void disbandTeam_leader_shouldCleanupAnswers() {
            try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
                mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createTestUser());

                AssessmentTeam team = createTestTeam();
                when(assessmentTeamRepository.findById(TEST_TEAM_ID)).thenReturn(Optional.of(team));
                when(assessmentAnswerRepository.countByTeamId(TEST_TEAM_ID)).thenReturn(0);
                List<Long> answerIds = List.of(100L, 101L);
                when(assessmentAnswerRepository.findAnswerIdsByTeamId(TEST_TEAM_ID)).thenReturn(answerIds);

                assessmentTeamAppService.disbandTeam(TEST_TEAM_ID);

                verify(assessmentJudgementRepository).deleteByAnswerIds(answerIds);
                verify(assessmentAnswerRepository).deleteByTeamId(TEST_TEAM_ID);
                verify(assessmentTeamRepository).update(any(AssessmentTeam.class));
            }
        }
    }
}
