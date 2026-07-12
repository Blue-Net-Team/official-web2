package com.bluenet.web.application.service.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.application.command.assessment_session.AssessmentSessionCommands;
import com.bluenet.web.application.result.assessment.AssessmentSessionResult;
import com.bluenet.web.application.service.AssessmentSessionAppService;
import com.bluenet.web.domain.model.entity.AssessmentSession;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.repository.AssessmentSessionRepository;
import com.bluenet.web.domain.repository.AssessmentTimeRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.testsupport.fixture.AssessmentFixture;
import com.bluenet.web.testsupport.fixture.TimeFixture;
import com.bluenet.web.testsupport.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AssessmentSessionAppServiceImpl 集成测试。
 *
 * <p>
 * 验证考核会话应用服务的获取与创建逻辑，包括限时、非限时及截止时间裁剪。
 * </p>
 */
@DisplayName("AssessmentSessionAppServiceImpl 集成测试")
class AssessmentSessionAppServiceImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AssessmentSessionAppService assessmentSessionAppService;

    @Autowired
    private AssessmentSessionRepository assessmentSessionRepository;

    @Autowired
    private AssessmentTimeRepository assessmentTimeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private long sequence = 1000;

    private String nextStudentId(String prefix) {
        return prefix + (++sequence);
    }

    private User createCandidate() {
        return UserFixture.candidate(nextStudentId("SC"))
                .withDirection(Direction.COMPUTER_VISION)
                .withAssessmentGradeYear(2024)
                .save(userRepository, passwordEncoder);
    }

    @Test
    @DisplayName("getOrCreateSession: 非限时考核应返回 null")
    void getOrCreateSession_nonTimed_shouldReturnNull() {
        User user = createCandidate();
        AssessmentTime time = AssessmentFixture.timeBuilder()
                .direction(Direction.COMPUTER_VISION)
                .grade(2024)
                .withinNow()
                .save(assessmentTimeRepository);

        AssessmentSessionResult result = assessmentSessionAppService.getOrCreateSession(
                new AssessmentSessionCommands.GetOrCreateSessionCommand(user.getId(), time.getId()));

        assertNull(result);
    }

    @Test
    @DisplayName("getOrCreateSession: 限时考核应创建新会话")
    void getOrCreateSession_timed_shouldCreateSession() {
        User user = createCandidate();
        AssessmentTime time = AssessmentFixture.timeBuilder()
                .direction(Direction.COMPUTER_VISION)
                .grade(2024)
                .withinNow()
                .timeLimit(30)
                .save(assessmentTimeRepository);

        AssessmentSessionResult result = assessmentSessionAppService.getOrCreateSession(
                new AssessmentSessionCommands.GetOrCreateSessionCommand(user.getId(), time.getId()));

        assertNotNull(result);
        assertNotNull(result.id());
        assertEquals(user.getId(), result.userId());
        assertEquals(time.getId(), result.assessmentTimeId());
        assertNotNull(result.deadline());
        assertTrue(result.deadline().isBefore(time.getEndTime()) || result.deadline().isEqual(time.getEndTime()));
    }

    @Test
    @DisplayName("getOrCreateSession: 已存在会话应直接返回")
    void getOrCreateSession_existing_shouldReturnExisting() {
        User user = createCandidate();
        AssessmentTime time = AssessmentFixture.timeBuilder()
                .direction(Direction.COMPUTER_VISION)
                .grade(2024)
                .withinNow()
                .timeLimit(30)
                .save(assessmentTimeRepository);
        AssessmentSession existing = AssessmentFixture.sessionBuilder()
                .user(user)
                .assessmentTime(time)
                .deadline(TimeFixture.plusMinutes(25))
                .save(assessmentSessionRepository);

        AssessmentSessionResult result = assessmentSessionAppService.getOrCreateSession(
                new AssessmentSessionCommands.GetOrCreateSessionCommand(user.getId(), time.getId()));

        assertEquals(existing.getId(), result.id());
        assertEquals(
                existing.getDeadline().truncatedTo(ChronoUnit.MILLIS),
                result.deadline().truncatedTo(ChronoUnit.MILLIS));
    }

    @Test
    @DisplayName("getOrCreateSession: 考核时间不存在应抛异常")
    void getOrCreateSession_timeNotFound_shouldThrow() {
        User user = createCandidate();

        assertThrows(
                IllegalArgumentException.class,
                () -> assessmentSessionAppService.getOrCreateSession(
                        new AssessmentSessionCommands.GetOrCreateSessionCommand(user.getId(), -1L)));
    }

    @Test
    @DisplayName("getOrCreateSession: 限时分钟数无效应抛异常")
    void getOrCreateSession_invalidTimeLimitMinutes_shouldThrow() {
        User user = createCandidate();
        LocalDateTime startTime = TimeFixture.minusMinutes(5);
        LocalDateTime endTime = TimeFixture.plusMinutes(60);
        AssessmentTime time = AssessmentTime.reconstruct(
                null,
                Direction.COMPUTER_VISION,
                1,
                2024,
                startTime,
                endTime,
                true,
                0,
                null,
                false);
        assessmentTimeRepository.save(time);

        assertThrows(
                IllegalArgumentException.class,
                () -> assessmentSessionAppService.getOrCreateSession(
                        new AssessmentSessionCommands.GetOrCreateSessionCommand(user.getId(), time.getId())));
    }

    @Test
    @DisplayName("getOrCreateSession: 截止时间不应晚于考核结束时间")
    void getOrCreateSession_deadlineShouldBeCappedByEndTime() {
        User user = createCandidate();
        LocalDateTime startTime = TimeFixture.minusMinutes(5);
        LocalDateTime endTime = TimeFixture.plusMinutes(10);
        AssessmentTime time = AssessmentTime.reconstruct(
                null,
                Direction.COMPUTER_VISION,
                1,
                2024,
                startTime,
                endTime,
                true,
                60,
                null,
                false);
        assessmentTimeRepository.save(time);

        AssessmentSessionResult result = assessmentSessionAppService.getOrCreateSession(
                new AssessmentSessionCommands.GetOrCreateSessionCommand(user.getId(), time.getId()));

        assertNotNull(result);
        assertEquals(endTime.truncatedTo(ChronoUnit.MILLIS), result.deadline().truncatedTo(ChronoUnit.MILLIS));
    }
}
