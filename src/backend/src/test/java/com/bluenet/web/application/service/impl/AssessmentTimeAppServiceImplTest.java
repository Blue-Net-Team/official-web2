package com.bluenet.web.application.service.impl;

import com.bluenet.web.domain.model.enumerate.RoleType;

import com.bluenet.web.application.AssessmentTimeResult;
import com.bluenet.web.application.command.assessment_time.AssessmentTimeCommands;
import com.bluenet.web.domain.exception.DataConflict;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.exception.Unauthorized;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.repository.AssessmentAnswerRepository;
import com.bluenet.web.domain.repository.AssessmentDecisionRepository;
import com.bluenet.web.domain.repository.AssessmentQuestionRepository;
import com.bluenet.web.domain.repository.AssessmentTimeRepository;
import com.bluenet.web.domain.service.AssessmentDecisionDomainService;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.infrastructure.security.principal.RoleTypeResolver;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AssessmentTimeAppServiceImpl 单元测试
 * <p>
 * 测试考核时间应用服务的协调逻辑
 * </p>
 */
@DisplayName("AssessmentTimeAppServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class AssessmentTimeAppServiceImplTest {

    @Mock
    private AssessmentTimeRepository assessmentTimeRepository;

    @Mock
    private AssessmentQuestionRepository assessmentQuestionRepository;

    @Mock
    private AssessmentAnswerRepository assessmentAnswerRepository;

    @Mock
    private AssessmentDecisionRepository assessmentDecisionRepository;

    @Mock
    private AssessmentDecisionDomainService assessmentDecisionDomainService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleTypeResolver roleTypeResolver;

    @InjectMocks
    private AssessmentTimeAppServiceImpl assessmentTimeAppService;

    @BeforeEach
    void setUp() {
        lenient().when(roleTypeResolver.resolve(anyLong())).thenAnswer(invocation -> {
            Long roleId = invocation.getArgument(0);
            return Arrays.stream(RoleType.values())
                    .filter(rt -> (long) rt.getLevel() == roleId)
                    .findFirst()
                    .orElse(null);
        });
    }

    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_ID = 10L;
    private final LocalDateTime futureStart = LocalDateTime.of(2099, 1, 1, 9, 0);
    private final LocalDateTime futureEnd = LocalDateTime.of(2099, 1, 1, 11, 0);

    private AssessmentTime createTestEntity() {
        return AssessmentTime.reconstruct(
                TEST_ID,
                Direction.COMPUTER_VISION,
                1,
                2024,
                futureStart,
                futureEnd,
                true,
                120,
                null,
                false);
    }

    private User createUser(String roleName, Direction direction) {
        User user = User.reconstruct(TEST_USER_ID, "password");
        user.setRoleId((long) RoleType.fromName(roleName).getLevel());
        user.setDirection(direction);
        user.setStudentId("2024123456");
        return user;
    }

    private User createUser(String roleName, Direction direction, String studentId) {
        User user = User.reconstruct(TEST_USER_ID, "password");
        user.setRoleId((long) RoleType.fromName(roleName).getLevel());
        user.setDirection(direction);
        user.setStudentId(studentId);
        return user;
    }

    // ==================== createAssessmentTime 测试 ====================

    @Nested
    @DisplayName("createAssessmentTime 方法测试")
    class CreateTests {

        @Test
        @DisplayName("正常创建：应返回Result")
        void create_validCommand_shouldReturnResult() {
            when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(createUser("SUPER_ADMIN", null)));
            when(assessmentTimeRepository.existsByDirectionAndEpochAndGrade(any(), any(), any())).thenReturn(false);

            AssessmentTimeCommands.CreateAssessmentTimeCommand command = new AssessmentTimeCommands.CreateAssessmentTimeCommand(
                    Direction.COMPUTER_VISION, 1, 2024, futureStart, futureEnd, true, 120, false);

            AssessmentTimeResult result = assessmentTimeAppService.createAssessmentTime(TEST_USER_ID, command);

            assertNotNull(result);
            assertEquals(Direction.COMPUTER_VISION, result.direction());
            verify(assessmentTimeRepository).save(any(AssessmentTime.class));
        }

        @Test
        @DisplayName("重复组合：应抛出IllegalArgumentException")
        void create_duplicateCombination_shouldThrow() {
            when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(createUser("SUPER_ADMIN", null)));
            when(assessmentTimeRepository.existsByDirectionAndEpochAndGrade(any(), any(), any())).thenReturn(true);

            AssessmentTimeCommands.CreateAssessmentTimeCommand command = new AssessmentTimeCommands.CreateAssessmentTimeCommand(
                    Direction.COMPUTER_VISION, 1, 2024, futureStart, futureEnd, false, null, false);

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> assessmentTimeAppService.createAssessmentTime(TEST_USER_ID, command));
            assertEquals("该方向轮次年级的考核时间已存在", ex.getMessage());
        }

        @Test
        @DisplayName("开始时间不早于结束时间：应抛出IllegalArgumentException")
        void create_startTimeNotBeforeEndTime_shouldThrow() {
            when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(createUser("SUPER_ADMIN", null)));

            AssessmentTimeCommands.CreateAssessmentTimeCommand command = new AssessmentTimeCommands.CreateAssessmentTimeCommand(
                    Direction.COMPUTER_VISION, 1, 2024, futureEnd, futureStart, false, null, false);

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> assessmentTimeAppService.createAssessmentTime(TEST_USER_ID, command));
            assertEquals("开始时间必须早于结束时间", ex.getMessage());
        }

        @Test
        @DisplayName("限时考核未设置限时分钟数：应抛出IllegalArgumentException")
        void create_timeLimitWithoutMinutes_shouldThrow() {
            when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(createUser("SUPER_ADMIN", null)));

            AssessmentTimeCommands.CreateAssessmentTimeCommand command = new AssessmentTimeCommands.CreateAssessmentTimeCommand(
                    Direction.COMPUTER_VISION, 1, 2024, futureStart, futureEnd, true, null, false);

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> assessmentTimeAppService.createAssessmentTime(TEST_USER_ID, command));
            assertEquals("限时考核必须设置有效的限时分钟数", ex.getMessage());
        }
    }

    // ==================== updateAssessmentTime 测试 ====================

    @Nested
    @DisplayName("updateAssessmentTime 方法测试")
    class UpdateTests {

        @Test
        @DisplayName("正常更新：应返回Result")
        void update_validCommand_shouldReturnResult() {
            AssessmentTime existing = createTestEntity();
            when(assessmentTimeRepository.findById(TEST_ID)).thenReturn(Optional.of(existing));
            when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(createUser("SUPER_ADMIN", null)));

            AssessmentTimeCommands.UpdateAssessmentTimeCommand command = new AssessmentTimeCommands.UpdateAssessmentTimeCommand(
                    TEST_ID, null, null, null, futureStart, futureEnd, true, 90, false);

            AssessmentTimeResult result = assessmentTimeAppService.updateAssessmentTime(TEST_USER_ID, command);

            assertNotNull(result);
            verify(assessmentTimeRepository).update(any(AssessmentTime.class));
        }

        @Test
        @DisplayName("更新不存在记录：应抛出DataNotFound")
        void update_notFound_shouldThrow() {
            AssessmentTimeCommands.UpdateAssessmentTimeCommand command = new AssessmentTimeCommands.UpdateAssessmentTimeCommand(
                    TEST_ID, null, null, null, null, null, null, null, null);

            when(assessmentTimeRepository.findById(TEST_ID)).thenReturn(Optional.empty());

            assertThrows(
                    DataNotFound.class,
                    () -> assessmentTimeAppService.updateAssessmentTime(TEST_USER_ID, command));
        }

        @Test
        @DisplayName("已开始的考核修改开始时间：应抛出IllegalArgumentException")
        void update_startedModifyStartTime_shouldThrow() {
            AssessmentTime existing = AssessmentTime.reconstruct(
                    TEST_ID,
                    Direction.COMPUTER_VISION,
                    1,
                    2024,
                    LocalDateTime.of(2020, 1, 1, 9, 0),
                    futureEnd,
                    false,
                    null,
                    null,
                    false);

            when(assessmentTimeRepository.findById(TEST_ID)).thenReturn(Optional.of(existing));
            when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(createUser("SUPER_ADMIN", null)));

            AssessmentTimeCommands.UpdateAssessmentTimeCommand command = new AssessmentTimeCommands.UpdateAssessmentTimeCommand(
                    TEST_ID, null, null, null, LocalDateTime.of(2025, 6, 1, 9, 0), null, null, null, null);

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> assessmentTimeAppService.updateAssessmentTime(TEST_USER_ID, command));
            assertEquals("已开始的考核不允许修改开始时间", ex.getMessage());
        }
    }

    // ==================== deleteAssessmentTime 测试 ====================

    @Nested
    @DisplayName("deleteAssessmentTime 方法测试")
    class DeleteTests {

        @Test
        @DisplayName("正常删除：应成功")
        void delete_valid_shouldSucceed() {
            AssessmentTime existing = createTestEntity();
            when(assessmentTimeRepository.findById(TEST_ID)).thenReturn(Optional.of(existing));
            when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(createUser("SUPER_ADMIN", null)));
            when(assessmentTimeRepository.hasAssociatedQuestions(TEST_ID)).thenReturn(false);

            assessmentTimeAppService.deleteAssessmentTime(TEST_USER_ID, TEST_ID);

            verify(assessmentTimeRepository).deleteById(TEST_ID);
        }

        @Test
        @DisplayName("有关联题目：应抛出DataConflict")
        void delete_withQuestions_shouldThrow() {
            AssessmentTime existing = createTestEntity();
            when(assessmentTimeRepository.findById(TEST_ID)).thenReturn(Optional.of(existing));
            when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(createUser("SUPER_ADMIN", null)));
            when(assessmentTimeRepository.hasAssociatedQuestions(TEST_ID)).thenReturn(true);

            assertThrows(
                    DataConflict.class,
                    () -> assessmentTimeAppService.deleteAssessmentTime(TEST_USER_ID, TEST_ID));
        }

        @Test
        @DisplayName("考核时间不存在：应抛出DataNotFound")
        void delete_notFound_shouldThrow() {
            when(assessmentTimeRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(
                    DataNotFound.class,
                    () -> assessmentTimeAppService.deleteAssessmentTime(TEST_USER_ID, 999L));
        }
    }

    // ==================== DIRECTION_ADMIN 方向权限校验测试 ====================

    @Nested
    @DisplayName("DIRECTION_ADMIN 方向权限校验测试")
    class DirectionPermissionTests {

        @Test
        @DisplayName("DIRECTION_ADMIN 创建自己方向的考核时间：应成功")
        void create_ownDirection_shouldSucceed() {
            when(userRepository.findById(TEST_USER_ID))
                    .thenReturn(Optional.of(createUser("DIRECTION_ADMIN", Direction.COMPUTER_VISION)));
            when(assessmentTimeRepository.existsByDirectionAndEpochAndGrade(any(), any(), any())).thenReturn(false);

            AssessmentTimeCommands.CreateAssessmentTimeCommand command = new AssessmentTimeCommands.CreateAssessmentTimeCommand(
                    Direction.COMPUTER_VISION, 1, 2025, futureStart, futureEnd, false, null, false);

            AssessmentTimeResult result = assessmentTimeAppService.createAssessmentTime(TEST_USER_ID, command);

            assertNotNull(result);
            verify(assessmentTimeRepository).save(any(AssessmentTime.class));
        }

        @Test
        @DisplayName("DIRECTION_ADMIN 创建其他方向的考核时间：应抛出Forbidden")
        void create_otherDirection_shouldThrowForbidden() {
            when(userRepository.findById(TEST_USER_ID))
                    .thenReturn(Optional.of(createUser("DIRECTION_ADMIN", Direction.COMPUTER_VISION)));

            AssessmentTimeCommands.CreateAssessmentTimeCommand command = new AssessmentTimeCommands.CreateAssessmentTimeCommand(
                    Direction.STRUCTURAL_DESIGN, 1, 2025, futureStart, futureEnd, false, null, false);

            Forbidden ex = assertThrows(
                    Forbidden.class,
                    () -> assessmentTimeAppService.createAssessmentTime(TEST_USER_ID, command));
            assertEquals("只能操作本方向的考核时间", ex.getMessage());
        }

        @Test
        @DisplayName("DIRECTION_ADMIN 更新自己方向的考核时间：应成功")
        void update_ownDirection_shouldSucceed() {
            AssessmentTime existing = createTestEntity();
            when(assessmentTimeRepository.findById(TEST_ID)).thenReturn(Optional.of(existing));
            when(userRepository.findById(TEST_USER_ID))
                    .thenReturn(Optional.of(createUser("DIRECTION_ADMIN", Direction.COMPUTER_VISION)));

            AssessmentTimeCommands.UpdateAssessmentTimeCommand command = new AssessmentTimeCommands.UpdateAssessmentTimeCommand(
                    TEST_ID, null, null, null, null, null, null, 90, false);

            AssessmentTimeResult result = assessmentTimeAppService.updateAssessmentTime(TEST_USER_ID, command);

            assertNotNull(result);
        }

        @Test
        @DisplayName("DIRECTION_ADMIN 更新其他方向的考核时间：应抛出Forbidden")
        void update_otherDirection_shouldThrowForbidden() {
            AssessmentTime existing = AssessmentTime.reconstruct(
                    TEST_ID,
                    Direction.STRUCTURAL_DESIGN,
                    1,
                    2025,
                    futureStart,
                    futureEnd,
                    false,
                    null,
                    null,
                    false);
            when(assessmentTimeRepository.findById(TEST_ID)).thenReturn(Optional.of(existing));
            when(userRepository.findById(TEST_USER_ID))
                    .thenReturn(Optional.of(createUser("DIRECTION_ADMIN", Direction.COMPUTER_VISION)));

            AssessmentTimeCommands.UpdateAssessmentTimeCommand command = new AssessmentTimeCommands.UpdateAssessmentTimeCommand(
                    TEST_ID, null, null, null, null, null, null, 90, false);

            Forbidden ex = assertThrows(
                    Forbidden.class,
                    () -> assessmentTimeAppService.updateAssessmentTime(TEST_USER_ID, command));
            assertEquals("只能操作本方向的考核时间", ex.getMessage());
        }

        @Test
        @DisplayName("DIRECTION_ADMIN 删除自己方向的考核时间：应成功")
        void delete_ownDirection_shouldSucceed() {
            AssessmentTime existing = createTestEntity();
            when(assessmentTimeRepository.findById(TEST_ID)).thenReturn(Optional.of(existing));
            when(userRepository.findById(TEST_USER_ID))
                    .thenReturn(Optional.of(createUser("DIRECTION_ADMIN", Direction.COMPUTER_VISION)));
            when(assessmentTimeRepository.hasAssociatedQuestions(TEST_ID)).thenReturn(false);

            assessmentTimeAppService.deleteAssessmentTime(TEST_USER_ID, TEST_ID);

            verify(assessmentTimeRepository).deleteById(TEST_ID);
        }

        @Test
        @DisplayName("DIRECTION_ADMIN 删除其他方向的考核时间：应抛出Forbidden")
        void delete_otherDirection_shouldThrowForbidden() {
            AssessmentTime existing = AssessmentTime.reconstruct(
                    TEST_ID,
                    Direction.EMBEDDED,
                    1,
                    2025,
                    futureStart,
                    futureEnd,
                    false,
                    null,
                    null,
                    false);
            when(assessmentTimeRepository.findById(TEST_ID)).thenReturn(Optional.of(existing));
            when(userRepository.findById(TEST_USER_ID))
                    .thenReturn(Optional.of(createUser("DIRECTION_ADMIN", Direction.COMPUTER_VISION)));

            Forbidden ex = assertThrows(
                    Forbidden.class,
                    () -> assessmentTimeAppService.deleteAssessmentTime(TEST_USER_ID, TEST_ID));
            assertEquals("只能操作本方向的考核时间", ex.getMessage());
        }
    }

    // ==================== listAssessmentTimes 测试 ====================

    @Nested
    @DisplayName("listAssessmentTimes 方法测试")
    class ListTests {

        @Test
        @DisplayName("方向管理员以上角色：应返回全部数据")
        void list_directionAdmin_shouldReturnAll() {
            when(userRepository.findById(TEST_USER_ID))
                    .thenReturn(Optional.of(createUser("DIRECTION_ADMIN", null)));

            List<AssessmentTime> entityList = List.of(createTestEntity());
            Page<AssessmentTime> entityPage = new PageImpl<>(entityList);
            when(assessmentTimeRepository.findByFilters(isNull(), isNull(), any()))
                    .thenReturn(entityPage);

            Page<AssessmentTimeResult> result = assessmentTimeAppService.listAssessmentTimes(TEST_USER_ID, 0, 5);

            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            verify(assessmentTimeRepository).findByFilters(isNull(), isNull(), any());
        }

        @Test
        @DisplayName("MEMBER角色：应按方向过滤")
        void list_member_shouldFilterByDirection() {
            when(userRepository.findById(TEST_USER_ID))
                    .thenReturn(Optional.of(createUser("MEMBER", Direction.COMPUTER_VISION)));

            List<AssessmentTime> entityList = List.of(createTestEntity());
            Page<AssessmentTime> entityPage = new PageImpl<>(entityList);
            when(assessmentTimeRepository.findByFilters(eq(Direction.COMPUTER_VISION), isNull(), any()))
                    .thenReturn(entityPage);

            Page<AssessmentTimeResult> result = assessmentTimeAppService.listAssessmentTimes(TEST_USER_ID, 0, 5);

            assertNotNull(result);
            verify(assessmentTimeRepository).findByFilters(eq(Direction.COMPUTER_VISION), isNull(), any());
        }

        @Test
        @DisplayName("CANDIDATE角色：应按方向和入学年份过滤")
        void list_candidate_shouldFilterByDirectionAndEnrollmentYear() {
            when(userRepository.findById(TEST_USER_ID))
                    .thenReturn(Optional.of(createUser("CANDIDATE", Direction.COMPUTER_VISION, "2024123456")));

            List<AssessmentTime> entityList = List.of(createTestEntity());
            Page<AssessmentTime> entityPage = new PageImpl<>(entityList);
            when(assessmentTimeRepository.findByFilters(eq(Direction.COMPUTER_VISION), eq(2024), any()))
                    .thenReturn(entityPage);

            Page<AssessmentTimeResult> result = assessmentTimeAppService.listAssessmentTimes(TEST_USER_ID, 0, 5);

            assertNotNull(result);
            verify(assessmentTimeRepository)
                    .findByFilters(eq(Direction.COMPUTER_VISION), eq(2024), any());
        }
    }

    // ==================== listAssessmentTimesForUser 测试 ====================

    @Nested
    @DisplayName("listAssessmentTimesForUser 方法测试")
    class ListForUserTests {

        @Test
        @DisplayName("CANDIDATE用户：应调用findByUserParticipation并填充进度")
        void listForUser_candidate_shouldUseParticipationQuery() {
            when(userRepository.findById(TEST_USER_ID))
                    .thenReturn(Optional.of(createUser("CANDIDATE", Direction.COMPUTER_VISION, "2024123456")));

            AssessmentTime entity = createTestEntity();
            List<AssessmentTime> entityList = List.of(entity);
            Page<AssessmentTime> entityPage = new PageImpl<>(entityList);
            when(
                    assessmentTimeRepository.findByUserParticipation(
                            eq(TEST_USER_ID),
                            eq(Direction.COMPUTER_VISION),
                            eq(2024),
                            any()))
                                    .thenReturn(entityPage);

            when(assessmentQuestionRepository.countByAssessmentTimeIds(List.of(TEST_ID)))
                    .thenReturn(Map.of(TEST_ID, 8));
            when(assessmentAnswerRepository.countByUserIdAndAssessmentTimeIds(TEST_USER_ID, List.of(TEST_ID)))
                    .thenReturn(Map.of(TEST_ID, 5));
            when(assessmentDecisionRepository.findEliminatedDecisionsByUserId(TEST_USER_ID))
                    .thenReturn(Collections.emptyList());
            when(assessmentTimeRepository.findAllById(Collections.emptyList()))
                    .thenReturn(Collections.emptyList());
            when(assessmentDecisionDomainService.isEliminatedFromPriorEpoch(eq(entity), anyList(), anyMap()))
                    .thenReturn(false);

            Page<AssessmentTimeResult> result = assessmentTimeAppService.listAssessmentTimesForUser(TEST_USER_ID, 0, 5);

            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            assertEquals(8, result.getContent().get(0).totalQuestions());
            assertEquals(5, result.getContent().get(0).completedQuestions());
            assertEquals(Boolean.FALSE, result.getContent().get(0).eliminated());
            verify(assessmentTimeRepository).findByUserParticipation(
                    eq(TEST_USER_ID),
                    eq(Direction.COMPUTER_VISION),
                    eq(2024),
                    any());
            verify(assessmentQuestionRepository).countByAssessmentTimeIds(List.of(TEST_ID));
            verify(assessmentAnswerRepository).countByUserIdAndAssessmentTimeIds(TEST_USER_ID, List.of(TEST_ID));
        }

        @Test
        @DisplayName("被淘汰CANDIDATE：后续轮次应标记eliminated=true")
        void listForUser_eliminatedCandidate_shouldMarkSubsequentEliminated() {
            when(userRepository.findById(TEST_USER_ID))
                    .thenReturn(Optional.of(createUser("CANDIDATE", Direction.COMPUTER_VISION, "2024123456")));

            AssessmentTime entity = createTestEntity();
            List<AssessmentTime> entityList = List.of(entity);
            Page<AssessmentTime> entityPage = new PageImpl<>(entityList);
            when(
                    assessmentTimeRepository.findByUserParticipation(
                            eq(TEST_USER_ID),
                            eq(Direction.COMPUTER_VISION),
                            eq(2024),
                            any()))
                                    .thenReturn(entityPage);

            when(assessmentQuestionRepository.countByAssessmentTimeIds(List.of(TEST_ID)))
                    .thenReturn(Map.of(TEST_ID, 8));
            when(assessmentAnswerRepository.countByUserIdAndAssessmentTimeIds(TEST_USER_ID, List.of(TEST_ID)))
                    .thenReturn(Map.of(TEST_ID, 5));
            when(assessmentDecisionRepository.findEliminatedDecisionsByUserId(TEST_USER_ID))
                    .thenReturn(Collections.emptyList());
            when(assessmentTimeRepository.findAllById(Collections.emptyList()))
                    .thenReturn(Collections.emptyList());
            when(assessmentDecisionDomainService.isEliminatedFromPriorEpoch(eq(entity), anyList(), anyMap()))
                    .thenReturn(true);

            Page<AssessmentTimeResult> result = assessmentTimeAppService.listAssessmentTimesForUser(TEST_USER_ID, 0, 5);

            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            assertEquals(Boolean.TRUE, result.getContent().get(0).eliminated());
        }

        @Test
        @DisplayName("用户不存在：应抛出Unauthorized")
        void listForUser_userNotFound_shouldThrow() {
            when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

            assertThrows(
                    Unauthorized.class,
                    () -> assessmentTimeAppService.listAssessmentTimesForUser(TEST_USER_ID, 0, 5));
        }
    }

    // ==================== getAssessmentProgress 测试 ====================

    @Nested
    @DisplayName("getAssessmentProgress 方法测试")
    class GetAssessmentProgressTests {

        @Test
        @DisplayName("考核时间存在：应返回进度数据")
        void getProgress_existing_shouldReturnProgress() {
            when(assessmentTimeRepository.findById(TEST_ID)).thenReturn(Optional.of(createTestEntity()));
            when(assessmentQuestionRepository.countByAssessmentTimeId(TEST_ID)).thenReturn(8);
            when(assessmentAnswerRepository.countByUserIdAndAssessmentTimeId(TEST_USER_ID, TEST_ID)).thenReturn(5);

            com.bluenet.web.application.AssessmentProgressResult result = assessmentTimeAppService
                    .getAssessmentProgress(TEST_USER_ID, TEST_ID);

            assertNotNull(result);
            assertEquals(TEST_ID, result.assessmentTimeId());
            assertEquals(8, result.totalQuestions());
            assertEquals(5, result.completedQuestions());
        }

        @Test
        @DisplayName("考核时间不存在：应抛出IllegalArgumentException")
        void getProgress_notExisting_shouldThrow() {
            when(assessmentTimeRepository.findById(999L)).thenReturn(Optional.empty());

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> assessmentTimeAppService.getAssessmentProgress(TEST_USER_ID, 999L));
            assertEquals("考核时间不存在", ex.getMessage());
        }
    }
}
