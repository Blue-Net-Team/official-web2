package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.domain.model.entity.AssessmentSession;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.entity.College;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.repository.AssessmentSessionRepository;
import com.bluenet.web.domain.repository.AssessmentTimeRepository;
import com.bluenet.web.domain.repository.CollegeRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.infrastructure.repository.dataobject.AssessmentSessionDO;
import com.bluenet.web.infrastructure.repository.mapper.AssessmentSessionMapper;
import com.bluenet.web.testsupport.fixture.AssessmentFixture;
import com.bluenet.web.testsupport.fixture.CollegeFixture;
import com.bluenet.web.testsupport.fixture.TimeFixture;
import com.bluenet.web.testsupport.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AssessmentSessionRepositoryImpl 集成测试。
 * <p>
 * 验证考核会话仓储行为：save 插入并回写 ID、按用户与考核场次查询、deadline 持久化。
 * </p>
 */
@DisplayName("AssessmentSessionRepositoryImpl 集成测试")
class AssessmentSessionRepositoryImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AssessmentSessionRepository assessmentSessionRepository;

    @Autowired
    private AssessmentSessionMapper assessmentSessionMapper;

    @Autowired
    private AssessmentTimeRepository assessmentTimeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CollegeRepository collegeRepository;

    private AssessmentFixtureState prepareFixture(String studentId, Direction direction, Integer epoch, Integer grade) {
        College college = CollegeFixture.saveDefaultCollege(collegeRepository);
        User user = UserFixture.candidate(studentId)
                .withCollege(college)
                .withDirection(direction)
                .save(userRepository, passwordEncoder);
        AssessmentTime assessmentTime = AssessmentFixture.timeBuilder()
                .direction(direction)
                .epoch(epoch)
                .grade(grade)
                .save(assessmentTimeRepository);
        return new AssessmentFixtureState(user, assessmentTime);
    }

    @Test
    @DisplayName("save: 新会话应插入并回写ID")
    void save_newSession_shouldInsertAndReturnId() {
        AssessmentFixtureState state = prepareFixture("2024001001", Direction.COMPUTER_VISION, 1, 2024);
        LocalDateTime startTime = TimeFixture.now();
        LocalDateTime deadline = TimeFixture.plusMinutes(60);
        AssessmentSession session = AssessmentSession.create(
                state.user.getId(),
                state.assessmentTime.getId(),
                startTime,
                deadline);

        assessmentSessionRepository.save(session);

        assertNotNull(session.getId());
        AssessmentSessionDO dataObject = assessmentSessionMapper.selectById(session.getId());
        assertNotNull(dataObject);
        assertEquals(state.user.getId(), dataObject.getUserId());
        assertEquals(state.assessmentTime.getId(), dataObject.getAssessmentTimeId());
    }

    @Test
    @DisplayName("findByUserIdAndAssessmentTimeId: 存在返回实体，不存在返回空")
    void findByUserIdAndAssessmentTimeId_shouldReturnOptional() {
        AssessmentFixtureState state = prepareFixture("2024001002", Direction.STRUCTURAL_DESIGN, 1, 2024);
        AssessmentSession session = AssessmentFixture.sessionBuilder()
                .user(state.user)
                .assessmentTime(state.assessmentTime)
                .save(assessmentSessionRepository);

        Optional<AssessmentSession> found = assessmentSessionRepository
                .findByUserIdAndAssessmentTimeId(state.user.getId(), state.assessmentTime.getId());
        assertTrue(found.isPresent());
        assertEquals(session.getId(), found.get().getId());
        assertEquals(state.user.getId(), found.get().getUserId());
        assertEquals(state.assessmentTime.getId(), found.get().getAssessmentTimeId());

        Optional<AssessmentSession> notFound = assessmentSessionRepository.findByUserIdAndAssessmentTimeId(-1L, -1L);
        assertTrue(notFound.isEmpty());
    }

    @Test
    @DisplayName("save: deadline 应被正确持久化到数据库")
    void save_shouldPersistDeadline() {
        AssessmentFixtureState state = prepareFixture("2024001003", Direction.EMBEDDED, 1, 2024);
        LocalDateTime deadline = TimeFixture.plusMinutes(90).truncatedTo(ChronoUnit.MICROS);
        AssessmentSession session = AssessmentFixture.sessionBuilder()
                .user(state.user)
                .assessmentTime(state.assessmentTime)
                .deadline(deadline)
                .build();

        assessmentSessionRepository.save(session);

        AssessmentSessionDO dataObject = assessmentSessionMapper.selectById(session.getId());
        assertNotNull(dataObject);
        assertEquals(deadline, dataObject.getDeadline());
    }

    private record AssessmentFixtureState(User user, AssessmentTime assessmentTime) {
    }
}
