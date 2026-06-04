package com.bluenet.web.application.service.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.repository.AssessmentTimeRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.infrastructure.job.EliminatedUserDisableJob;
import com.bluenet.web.infrastructure.repository.dataobject.UserDO;
import com.bluenet.web.infrastructure.repository.mapper.AssessmentDecisionMapper;
import com.bluenet.web.infrastructure.repository.mapper.AssessmentTimeMapper;
import com.bluenet.web.infrastructure.repository.mapper.UserMapper;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

/**
 * 考核淘汰限制集成测试。
 */
@DisplayName("考核淘汰限制集成测试")
class EliminationRestrictionIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AssessmentTimeAppServiceImpl assessmentTimeAppService;

    @Autowired
    private AssessmentQuestionAppServiceImpl assessmentQuestionAppService;

    @Autowired
    private AssessmentTimeRepository assessmentTimeRepository;

    @Autowired
    private AssessmentTimeMapper assessmentTimeMapper;

    @Autowired
    private AssessmentDecisionMapper assessmentDecisionMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EliminatedUserDisableJob eliminatedUserDisableJob;

    private Long createCandidateUser(String studentId, Direction direction) {
        UserDO user = new UserDO();
        user.setStudentId(studentId);
        user.setUsername("test-candidate");
        user.setPassword("password");
        user.setEmail(studentId + "@example.com");
        user.setDirection(direction);
        user.setDisable(false);
        userMapper.insert(user);
        return user.getId();
    }

    private Long createAssessmentTime(Direction direction, int epoch, Integer grade) {
        com.bluenet.web.infrastructure.repository.dataobject.AssessmentTimeDO time = new com.bluenet.web.infrastructure.repository.dataobject.AssessmentTimeDO();
        time.setDirection(direction);
        time.setEpoch(epoch);
        time.setGrade(grade);
        time.setStartTime(LocalDateTime.now().minusDays(1));
        time.setEndTime(LocalDateTime.now().plusDays(1));
        time.setTimeLimit(false);
        assessmentTimeMapper.insert(time);
        return time.getId();
    }

    private void createDecision(Long userId, Long assessmentTimeId, boolean passed, LocalDateTime decidedAt) {
        com.bluenet.web.infrastructure.repository.dataobject.AssessmentDecisionDO decision = new com.bluenet.web.infrastructure.repository.dataobject.AssessmentDecisionDO();
        decision.setUserId(userId);
        decision.setAssessmentTimeId(assessmentTimeId);
        decision.setPassed(passed);
        decision.setDecidedBy(1L);
        decision.setDecidedAt(decidedAt);
        assessmentDecisionMapper.insert(decision);
    }

    @Test
    @DisplayName("淘汰考生不应看到后续轮次考核")
    void eliminatedCandidate_shouldNotSeeNextRound() {
        Long userId = createCandidateUser("2024123456", Direction.COMPUTER_VISION);
        Long epoch1TimeId = createAssessmentTime(Direction.COMPUTER_VISION, 1, 2024);
        Long epoch2TimeId = createAssessmentTime(Direction.COMPUTER_VISION, 2, 2024);

        createDecision(userId, epoch1TimeId, false, LocalDateTime.now());

        try (MockedStatic<UserCTX> mocked = mockStatic(UserCTX.class)) {
            UserVO userVO = UserVO.builder()
                    .id(userId)
                    .roleName("CANDIDATE")
                    .direction(Direction.COMPUTER_VISION)
                    .studentId("2024123456")
                    .build();
            mocked.when(UserCTX::getCurrentUser).thenReturn(userVO);

            Page<com.bluenet.web.application.AssessmentTimeResult> result = assessmentTimeAppService
                    .listAssessmentTimesForUser(0, 10);

            assertEquals(
                    1,
                    result.getContent().size(),
                    "被淘汰考生应只能看到自己参与的那一轮考核");
            assertEquals(
                    epoch1TimeId,
                    result.getContent().get(0).id(),
                    "应显示被淘汰的 epoch=1 考核");
        }
    }

    @Test
    @DisplayName("非淘汰考生应看到所有考核")
    void notEliminatedCandidate_shouldSeeAllRounds() {
        Long userId = createCandidateUser("2024123457", Direction.COMPUTER_VISION);
        createAssessmentTime(Direction.COMPUTER_VISION, 1, 2024);
        createAssessmentTime(Direction.COMPUTER_VISION, 2, 2024);

        try (MockedStatic<UserCTX> mocked = mockStatic(UserCTX.class)) {
            UserVO userVO = UserVO.builder()
                    .id(userId)
                    .roleName("CANDIDATE")
                    .direction(Direction.COMPUTER_VISION)
                    .studentId("2024123457")
                    .build();
            mocked.when(UserCTX::getCurrentUser).thenReturn(userVO);

            Page<com.bluenet.web.application.AssessmentTimeResult> result = assessmentTimeAppService
                    .listAssessmentTimesForUser(0, 10);

            assertEquals(
                    2,
                    result.getContent().size(),
                    "非淘汰考生应看到所有相关考核");
        }
    }

    @Test
    @DisplayName("淘汰超过7天后定时任务应禁用账号")
    void eliminatedOver7Days_shouldBeDisabledByJob() {
        Long userId = createCandidateUser("2024123458", Direction.COMPUTER_VISION);
        Long epoch1TimeId = createAssessmentTime(Direction.COMPUTER_VISION, 1, 2024);
        createDecision(userId, epoch1TimeId, false, LocalDateTime.now().minusDays(8));

        eliminatedUserDisableJob.disableEliminatedUsers();

        UserDO updated = userMapper.selectById(userId);
        assertTrue(updated.getDisable(), "淘汰超过7天的考生应被自动禁用");
    }

    @Test
    @DisplayName("淘汰未满7天不应被禁用")
    void eliminatedUnder7Days_shouldNotBeDisabled() {
        Long userId = createCandidateUser("2024123459", Direction.COMPUTER_VISION);
        Long epoch1TimeId = createAssessmentTime(Direction.COMPUTER_VISION, 1, 2024);
        createDecision(userId, epoch1TimeId, false, LocalDateTime.now().minusDays(3));

        eliminatedUserDisableJob.disableEliminatedUsers();

        UserDO updated = userMapper.selectById(userId);
        assertFalse(updated.getDisable(), "淘汰未满7天的考生不应被禁用");
    }

    @Test
    @DisplayName("已通过考生不应被禁用")
    void passedCandidate_shouldNotBeDisabled() {
        Long userId = createCandidateUser("2024123460", Direction.COMPUTER_VISION);
        Long epoch1TimeId = createAssessmentTime(Direction.COMPUTER_VISION, 1, 2024);
        createDecision(userId, epoch1TimeId, true, LocalDateTime.now().minusDays(10));

        eliminatedUserDisableJob.disableEliminatedUsers();

        UserDO updated = userMapper.selectById(userId);
        assertFalse(updated.getDisable(), "已通过考生不应被禁用");
    }

    @Test
    @DisplayName("已禁用用户不应被重复处理")
    void alreadyDisabledUser_shouldBeSkipped() {
        Long userId = createCandidateUser("2024123461", Direction.COMPUTER_VISION);
        UserDO user = userMapper.selectById(userId);
        user.setDisable(true);
        userMapper.updateById(user);

        Long epoch1TimeId = createAssessmentTime(Direction.COMPUTER_VISION, 1, 2024);
        createDecision(userId, epoch1TimeId, false, LocalDateTime.now().minusDays(10));

        eliminatedUserDisableJob.disableEliminatedUsers();

        // 测试应正常完成，不抛异常
        UserDO updated = userMapper.selectById(userId);
        assertTrue(updated.getDisable());
    }
}
