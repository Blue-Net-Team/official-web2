package com.bluenet.web.application.service.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.application.command.assessment_time.AssessmentTimeCommands;
import com.bluenet.web.application.result.assessment.AssessmentProgressResult;
import com.bluenet.web.application.result.assessment.AssessmentTimeResult;
import com.bluenet.web.application.service.AssessmentTimeAppService;
import com.bluenet.web.domain.exception.DataConflict;
import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.repository.AssessmentAnswerRepository;
import com.bluenet.web.domain.repository.AssessmentDecisionRepository;
import com.bluenet.web.domain.repository.AssessmentJudgementRepository;
import com.bluenet.web.domain.repository.AssessmentQuestionRepository;
import com.bluenet.web.domain.repository.AssessmentSessionRepository;
import com.bluenet.web.domain.repository.AssessmentTeamRepository;
import com.bluenet.web.domain.repository.AssessmentTimeRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.domain.service.AssessmentDecisionDomainService;
import com.bluenet.web.testsupport.fixture.AssessmentFixture;
import com.bluenet.web.testsupport.fixture.AssessmentFixture.AssessmentScenario;
import com.bluenet.web.testsupport.fixture.TimeFixture;
import com.bluenet.web.testsupport.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AssessmentTimeAppServiceImpl 集成测试。
 *
 * <p>
 * 验证考核时间应用服务的编排、方向权限、时间窗口校验及列表查询。
 * </p>
 */
@DisplayName("AssessmentTimeAppServiceImpl 集成测试")
class AssessmentTimeAppServiceImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AssessmentTimeAppService assessmentTimeAppService;

    @Autowired
    private AssessmentTimeRepository assessmentTimeRepository;

    @Autowired
    private AssessmentQuestionRepository assessmentQuestionRepository;

    @Autowired
    private AssessmentAnswerRepository assessmentAnswerRepository;

    @Autowired
    private AssessmentSessionRepository assessmentSessionRepository;

    @Autowired
    private AssessmentTeamRepository assessmentTeamRepository;

    @Autowired
    private AssessmentJudgementRepository assessmentJudgementRepository;

    @Autowired
    private AssessmentDecisionRepository assessmentDecisionRepository;

    @MockitoBean
    private AssessmentDecisionDomainService assessmentDecisionDomainService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private long sequence = 1000;

    private String nextStudentId(String prefix) {
        return prefix + (++sequence);
    }

    private User createSuperAdmin() {
        return UserFixture.superAdmin(nextStudentId("SA")).save(userRepository, passwordEncoder);
    }

    private User createDirectionAdmin(Direction direction) {
        return UserFixture.directionAdmin(nextStudentId("DA"), direction).save(userRepository, passwordEncoder);
    }

    private User createCandidate(Direction direction, Integer gradeYear) {
        return UserFixture.candidate(nextStudentId("SC"))
                .withDirection(direction)
                .withAssessmentGradeYear(gradeYear)
                .save(userRepository, passwordEncoder);
    }

    private AssessmentTimeCommands.CreateAssessmentTimeCommand createCommand(Direction direction, Integer epoch,
            Integer grade) {
        LocalDateTime[] window = TimeFixture.withinNow();
        return new AssessmentTimeCommands.CreateAssessmentTimeCommand(
                direction,
                epoch,
                grade,
                window[0],
                window[1],
                false,
                null,
                false);
    }

    @Test
    @DisplayName("createAssessmentTime: 超级管理员应能创建全局考核时间")
    void createAssessmentTime_superAdmin_shouldCreateGlobalTime() {
        User admin = createSuperAdmin();
        LocalDateTime[] window = TimeFixture.withinNow();
        AssessmentTimeCommands.CreateAssessmentTimeCommand command = new AssessmentTimeCommands.CreateAssessmentTimeCommand(
                null, 1, 2024, window[0], window[1], false, null, false);

        AssessmentTimeResult result = assessmentTimeAppService.createAssessmentTime(admin.getId(), command);

        assertNotNull(result);
        assertNotNull(result.id());
        assertNull(result.direction());
        assertEquals(1, result.epoch());
        assertEquals(2024, result.grade());
    }

    @Test
    @DisplayName("createAssessmentTime: 方向管理员应能创建本方向考核时间")
    void createAssessmentTime_directionAdmin_shouldCreateDirectionTime() {
        User admin = createDirectionAdmin(Direction.COMPUTER_VISION);
        AssessmentTimeCommands.CreateAssessmentTimeCommand command = createCommand(Direction.COMPUTER_VISION, 1, 2024);

        AssessmentTimeResult result = assessmentTimeAppService.createAssessmentTime(admin.getId(), command);

        assertNotNull(result);
        assertEquals(Direction.COMPUTER_VISION, result.direction());
    }

    @Test
    @DisplayName("createAssessmentTime: 方向管理员不能创建跨方向考核时间")
    void createAssessmentTime_directionAdminCrossDirection_shouldThrowForbidden() {
        User admin = createDirectionAdmin(Direction.STRUCTURAL_DESIGN);
        AssessmentTimeCommands.CreateAssessmentTimeCommand command = createCommand(Direction.COMPUTER_VISION, 1, 2024);

        assertThrows(Forbidden.class, () -> assessmentTimeAppService.createAssessmentTime(admin.getId(), command));
    }

    @Test
    @DisplayName("createAssessmentTime: 方向管理员不能创建全局考核时间")
    void createAssessmentTime_directionAdminGlobal_shouldThrowForbidden() {
        User admin = createDirectionAdmin(Direction.COMPUTER_VISION);
        LocalDateTime[] window = TimeFixture.withinNow();
        AssessmentTimeCommands.CreateAssessmentTimeCommand command = new AssessmentTimeCommands.CreateAssessmentTimeCommand(
                null, 1, 2024, window[0], window[1], false, null, false);

        assertThrows(Forbidden.class, () -> assessmentTimeAppService.createAssessmentTime(admin.getId(), command));
    }

    @Test
    @DisplayName("createAssessmentTime: 同方向同轮次同年级重复应抛异常")
    void createAssessmentTime_duplicate_shouldThrow() {
        User admin = createSuperAdmin();
        AssessmentTimeCommands.CreateAssessmentTimeCommand command = createCommand(Direction.COMPUTER_VISION, 1, 2024);
        assessmentTimeAppService.createAssessmentTime(admin.getId(), command);

        assertThrows(
                IllegalArgumentException.class,
                () -> assessmentTimeAppService.createAssessmentTime(admin.getId(), command));
    }

    @Test
    @DisplayName("createAssessmentTime: 同方向同轮次年级形式冲突应抛 DataConflict")
    void createAssessmentTime_conflictingGrade_shouldThrowDataConflict() {
        User admin = createSuperAdmin();
        AssessmentTimeCommands.CreateAssessmentTimeCommand withGrade = createCommand(
                Direction.COMPUTER_VISION,
                1,
                2024);
        assessmentTimeAppService.createAssessmentTime(admin.getId(), withGrade);

        LocalDateTime[] window = TimeFixture.withinNow();
        AssessmentTimeCommands.CreateAssessmentTimeCommand withoutGrade = new AssessmentTimeCommands.CreateAssessmentTimeCommand(
                Direction.COMPUTER_VISION, 1, null, window[0], window[1], false, null, false);

        assertThrows(
                DataConflict.class,
                () -> assessmentTimeAppService.createAssessmentTime(admin.getId(), withoutGrade));
    }

    @Test
    @DisplayName("updateAssessmentTime: 超级管理员应能更新考核时间")
    void updateAssessmentTime_superAdmin_shouldUpdate() {
        User admin = createSuperAdmin();
        AssessmentTimeCommands.CreateAssessmentTimeCommand createCommand = createCommand(
                Direction.COMPUTER_VISION,
                1,
                2024);
        AssessmentTimeResult created = assessmentTimeAppService.createAssessmentTime(admin.getId(), createCommand);

        LocalDateTime newEndTime = TimeFixture.plusMinutes(120);
        AssessmentTimeCommands.UpdateAssessmentTimeCommand updateCommand = new AssessmentTimeCommands.UpdateAssessmentTimeCommand(
                created.id(), null, null, null, null, newEndTime, true, 90, true);

        AssessmentTimeResult updated = assessmentTimeAppService.updateAssessmentTime(admin.getId(), updateCommand);

        assertEquals(newEndTime, updated.endTime());
        assertTrue(updated.timeLimit());
        assertEquals(90, updated.timeLimitMinutes());
        assertTrue(updated.allowTeam());
    }

    @Test
    @DisplayName("updateAssessmentTime: 已开始考核不允许修改开始时间")
    void updateAssessmentTime_started_shouldNotChangeStartTime() {
        User admin = createSuperAdmin();
        AssessmentTime time = AssessmentFixture.timeBuilder()
                .direction(Direction.COMPUTER_VISION)
                .epoch(1)
                .grade(2024)
                .startTime(TimeFixture.minusMinutes(10))
                .endTime(TimeFixture.plusMinutes(50))
                .build();
        assessmentTimeRepository.save(time);

        AssessmentTimeCommands.UpdateAssessmentTimeCommand command = new AssessmentTimeCommands.UpdateAssessmentTimeCommand(
                time.getId(), null, null, null, TimeFixture.minusMinutes(20), null, null, null, null);

        assertThrows(
                IllegalArgumentException.class,
                () -> assessmentTimeAppService.updateAssessmentTime(admin.getId(), command));
    }

    @Test
    @DisplayName("updateAssessmentTime: 开始时间晚于结束时间应抛异常")
    void updateAssessmentTime_invalidWindow_shouldThrow() {
        User admin = createSuperAdmin();
        AssessmentTimeCommands.CreateAssessmentTimeCommand createCommand = createCommand(
                Direction.COMPUTER_VISION,
                1,
                2024);
        AssessmentTimeResult created = assessmentTimeAppService.createAssessmentTime(admin.getId(), createCommand);

        LocalDateTime startTime = TimeFixture.plusMinutes(60);
        LocalDateTime endTime = TimeFixture.plusMinutes(10);
        AssessmentTimeCommands.UpdateAssessmentTimeCommand command = new AssessmentTimeCommands.UpdateAssessmentTimeCommand(
                created.id(), null, null, null, startTime, endTime, null, null, null);

        assertThrows(
                IllegalArgumentException.class,
                () -> assessmentTimeAppService.updateAssessmentTime(admin.getId(), command));
    }

    @Test
    @DisplayName("deleteAssessmentTime: 超级管理员应能删除无关联题目的考核时间")
    void deleteAssessmentTime_superAdmin_shouldDelete() {
        User admin = createSuperAdmin();
        AssessmentTimeResult created = assessmentTimeAppService.createAssessmentTime(
                admin.getId(),
                createCommand(Direction.COMPUTER_VISION, 1, 2024));

        assessmentTimeAppService.deleteAssessmentTime(admin.getId(), created.id());

        assertFalse(assessmentTimeRepository.existsById(created.id()));
    }

    @Test
    @DisplayName("deleteAssessmentTime: 存在关联题目时应抛 DataConflict")
    void deleteAssessmentTime_withQuestions_shouldThrowDataConflict() {
        User admin = createSuperAdmin();
        AssessmentTime time = AssessmentFixture.timeBuilder().save(assessmentTimeRepository);
        AssessmentFixture.questionBuilder().assessmentTime(time).save(assessmentQuestionRepository);

        assertThrows(
                DataConflict.class,
                () -> assessmentTimeAppService.deleteAssessmentTime(admin.getId(), time.getId()));
    }

    @Test
    @DisplayName("listAssessmentTimes: 超级管理员应返回全部考核时间")
    void listAssessmentTimes_superAdmin_shouldReturnAll() {
        User admin = createSuperAdmin();
        assessmentTimeAppService.createAssessmentTime(admin.getId(), createCommand(Direction.COMPUTER_VISION, 1, 2024));
        assessmentTimeAppService.createAssessmentTime(
                admin.getId(),
                createCommand(Direction.STRUCTURAL_DESIGN, 1, 2024));

        Page<AssessmentTimeResult> result = assessmentTimeAppService.listAssessmentTimes(admin.getId(), 0, 10);

        assertEquals(2, result.getTotalElements());
    }

    @Test
    @DisplayName("listAssessmentTimes: 考生应只能看到本方向本年级的考核时间")
    void listAssessmentTimes_candidate_shouldFilterByDirectionAndGrade() {
        User admin = createSuperAdmin();
        User candidate = createCandidate(Direction.COMPUTER_VISION, 2024);
        assessmentTimeAppService.createAssessmentTime(admin.getId(), createCommand(Direction.COMPUTER_VISION, 1, 2024));
        assessmentTimeAppService.createAssessmentTime(admin.getId(), createCommand(Direction.COMPUTER_VISION, 1, 2025));
        assessmentTimeAppService.createAssessmentTime(
                admin.getId(),
                createCommand(Direction.STRUCTURAL_DESIGN, 1, 2024));

        Page<AssessmentTimeResult> result = assessmentTimeAppService.listAssessmentTimes(candidate.getId(), 0, 10);

        assertEquals(1, result.getTotalElements());
        assertEquals(Direction.COMPUTER_VISION, result.getContent().get(0).direction());
        assertEquals(2024, result.getContent().get(0).grade());
    }

    @Test
    @DisplayName("listAssessmentTimesForUser: 应返回用户参与且包含进度信息的考核时间")
    void listAssessmentTimesForUser_shouldReturnParticipatedWithProgress() {
        AssessmentScenario scenario = AssessmentFixture.scenarioBuilder()
                .direction(Direction.COMPUTER_VISION)
                .epoch(1)
                .grade(2024)
                .withAnswer(true)
                .save(
                        assessmentTimeRepository,
                        assessmentQuestionRepository,
                        assessmentSessionRepository,
                        assessmentTeamRepository,
                        assessmentAnswerRepository,
                        assessmentJudgementRepository,
                        assessmentDecisionRepository,
                        userRepository,
                        passwordEncoder);

        Page<AssessmentTimeResult> result = assessmentTimeAppService.listAssessmentTimesForUser(
                scenario.candidate().getId(),
                0,
                10);

        assertEquals(1, result.getTotalElements());
        AssessmentTimeResult timeResult = result.getContent().get(0);
        assertEquals(scenario.time().getId(), timeResult.id());
        assertEquals(1, timeResult.totalQuestions());
        assertEquals(1, timeResult.completedQuestions());
        assertFalse(timeResult.eliminated());
    }

    @Test
    @DisplayName("getAssessmentProgress: 应返回指定考核的题目总数与完成数")
    void getAssessmentProgress_shouldReturnCounts() {
        AssessmentScenario scenario = AssessmentFixture.scenarioBuilder()
                .direction(Direction.COMPUTER_VISION)
                .epoch(1)
                .grade(2024)
                .withAnswer(true)
                .save(
                        assessmentTimeRepository,
                        assessmentQuestionRepository,
                        assessmentSessionRepository,
                        assessmentTeamRepository,
                        assessmentAnswerRepository,
                        assessmentJudgementRepository,
                        assessmentDecisionRepository,
                        userRepository,
                        passwordEncoder);

        AssessmentProgressResult progress = assessmentTimeAppService.getAssessmentProgress(
                scenario.candidate().getId(),
                scenario.time().getId());

        assertEquals(scenario.time().getId(), progress.assessmentTimeId());
        assertEquals(1, progress.totalQuestions());
        assertEquals(1, progress.completedQuestions());
    }
}
