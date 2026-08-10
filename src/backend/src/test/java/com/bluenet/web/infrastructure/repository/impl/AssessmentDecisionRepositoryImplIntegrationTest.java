package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.domain.model.entity.AssessmentDecision;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.entity.College;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.repository.AssessmentDecisionRepository;
import com.bluenet.web.domain.repository.AssessmentTimeRepository;
import com.bluenet.web.domain.repository.CollegeRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.infrastructure.repository.dataobject.AssessmentDecisionDO;
import com.bluenet.web.infrastructure.repository.mapper.AssessmentDecisionMapper;
import com.bluenet.web.testsupport.fixture.AssessmentFixture;
import com.bluenet.web.testsupport.fixture.CollegeFixture;
import com.bluenet.web.testsupport.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AssessmentDecisionRepositoryImpl 集成测试。
 */
@DisplayName("AssessmentDecisionRepositoryImpl 集成测试")
class AssessmentDecisionRepositoryImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AssessmentDecisionRepository assessmentDecisionRepository;

    @Autowired
    private AssessmentDecisionMapper assessmentDecisionMapper;

    @Autowired
    private AssessmentTimeRepository assessmentTimeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CollegeRepository collegeRepository;

    private int timeCounter = 1;

    private College createCollege() {
        return CollegeFixture.saveDefaultCollege(collegeRepository);
    }

    private User createUser(String studentId) {
        College college = createCollege();
        return UserFixture.candidate(studentId)
                .withCollege(college)
                .save(userRepository, passwordEncoder);
    }

    private User createAdmin(String studentId) {
        College college = createCollege();
        return UserFixture.superAdmin(studentId)
                .withCollege(college)
                .save(userRepository, passwordEncoder);
    }

    private AssessmentTime createTime() {
        int epoch = timeCounter++;
        return AssessmentFixture.timeBuilder()
                .epoch(epoch)
                .grade(2020 + epoch)
                .save(assessmentTimeRepository);
    }

    @Test
    @DisplayName("save: 应插入新决策并回写 ID")
    void save_shouldInsertNewDecisionAndSetId() {
        User user = createUser("2024002001");
        User admin = createAdmin("2024002001A");
        AssessmentTime time = createTime();
        AssessmentDecision decision = AssessmentFixture.decisionBuilder()
                .user(user)
                .assessmentTime(time)
                .decidedBy(admin)
                .build();

        assessmentDecisionRepository.save(decision);

        assertNotNull(decision.getId());
        AssessmentDecisionDO dataObject = assessmentDecisionMapper.selectById(decision.getId());
        assertNotNull(dataObject);
        assertEquals(user.getId(), dataObject.getUserId());
        assertEquals(time.getId(), dataObject.getAssessmentTimeId());
        assertTrue(dataObject.getPassed());
        assertEquals(admin.getId(), dataObject.getDecidedBy());
    }

    @Test
    @DisplayName("save: 应更新已有决策的通过状态和说明")
    void save_shouldUpdateExistingDecision() {
        User user = createUser("2024002002");
        User admin = createAdmin("2024002002A");
        AssessmentTime time = createTime();
        AssessmentDecision decision = AssessmentFixture.decisionBuilder()
                .user(user)
                .assessmentTime(time)
                .decidedBy(admin)
                .build();
        assessmentDecisionRepository.save(decision);

        decision.updatePassed(false, admin.getId(), "更新为未通过");
        assessmentDecisionRepository.save(decision);

        AssessmentDecisionDO updated = assessmentDecisionMapper.selectById(decision.getId());
        assertFalse(updated.getPassed());
        assertEquals("更新为未通过", updated.getDecisionComment());
    }

    @Test
    @DisplayName("findById: 存在返回实体，不存在返回空")
    void findById_shouldReturnPresentOrEmpty() {
        User user = createUser("2024002003");
        User admin = createAdmin("2024002003A");
        AssessmentTime time = createTime();
        AssessmentDecision decision = AssessmentFixture.decisionBuilder()
                .user(user)
                .assessmentTime(time)
                .decidedBy(admin)
                .save(assessmentDecisionRepository);

        Optional<AssessmentDecision> found = assessmentDecisionRepository.findById(decision.getId());
        assertTrue(found.isPresent());
        assertEquals(decision.getUserId(), found.get().getUserId());

        assertTrue(assessmentDecisionRepository.findById(-1L).isEmpty());
    }

    @Test
    @DisplayName("findByUserIdAndAssessmentTimeId: 应按用户和考核场次查询决策")
    void findByUserIdAndAssessmentTimeId_shouldReturnDecision() {
        User user = createUser("2024002004");
        User admin = createAdmin("2024002004A");
        AssessmentTime time = createTime();
        AssessmentDecision decision = AssessmentFixture.decisionBuilder()
                .user(user)
                .assessmentTime(time)
                .decidedBy(admin)
                .save(assessmentDecisionRepository);

        Optional<AssessmentDecision> found = assessmentDecisionRepository
                .findByUserIdAndAssessmentTimeId(user.getId(), time.getId());
        assertTrue(found.isPresent());
        assertEquals(decision.getId(), found.get().getId());

        assertTrue(assessmentDecisionRepository.findByUserIdAndAssessmentTimeId(user.getId(), -1L).isEmpty());
    }

    @Test
    @DisplayName("findByAssessmentTimeId: 应返回指定考核场次下的所有决策")
    void findByAssessmentTimeId_shouldReturnDecisionsForTime() {
        User user1 = createUser("2024002005");
        User user2 = createUser("2024002006");
        User admin = createAdmin("2024002005A");
        AssessmentTime time1 = createTime();
        AssessmentTime time2 = createTime();
        AssessmentFixture.decisionBuilder()
                .user(user1)
                .assessmentTime(time1)
                .decidedBy(admin)
                .save(assessmentDecisionRepository);
        AssessmentFixture.decisionBuilder()
                .user(user2)
                .assessmentTime(time1)
                .decidedBy(admin)
                .decisionComment("同场次另一用户")
                .save(assessmentDecisionRepository);
        AssessmentFixture.decisionBuilder()
                .user(user1)
                .assessmentTime(time2)
                .decidedBy(admin)
                .decisionComment("其他场次")
                .save(assessmentDecisionRepository);

        List<AssessmentDecision> found = assessmentDecisionRepository.findByAssessmentTimeId(time1.getId());

        assertEquals(2, found.size());
    }

    @Test
    @DisplayName("findEliminatedDecisionsByUserId: 应只返回该用户被淘汰的决策")
    void findEliminatedDecisionsByUserId_shouldReturnOnlyFailedDecisions() {
        User user = createUser("2024002007");
        User otherUser = createUser("2024002008");
        User admin = createAdmin("2024002007A");
        AssessmentTime passedTime = createTime();
        AssessmentTime failedTime1 = createTime();
        AssessmentTime failedTime2 = createTime();
        AssessmentTime otherUserFailedTime = createTime();

        AssessmentFixture.decisionBuilder()
                .user(user)
                .assessmentTime(passedTime)
                .passed(true)
                .decidedBy(admin)
                .save(assessmentDecisionRepository);
        AssessmentFixture.decisionBuilder()
                .user(user)
                .assessmentTime(failedTime1)
                .passed(false)
                .decidedBy(admin)
                .decisionComment("淘汰一")
                .save(assessmentDecisionRepository);
        AssessmentFixture.decisionBuilder()
                .user(user)
                .assessmentTime(failedTime2)
                .passed(false)
                .decidedBy(admin)
                .decisionComment("淘汰二")
                .save(assessmentDecisionRepository);
        AssessmentFixture.decisionBuilder()
                .user(otherUser)
                .assessmentTime(otherUserFailedTime)
                .passed(false)
                .decidedBy(admin)
                .decisionComment("其他用户淘汰")
                .save(assessmentDecisionRepository);

        List<AssessmentDecision> eliminated = assessmentDecisionRepository
                .findEliminatedDecisionsByUserId(user.getId());

        assertEquals(2, eliminated.size());
        assertTrue(eliminated.stream().allMatch(d -> Boolean.FALSE.equals(d.getPassed())));
        assertTrue(eliminated.stream().noneMatch(d -> d.getUserId().equals(otherUser.getId())));
    }
}
