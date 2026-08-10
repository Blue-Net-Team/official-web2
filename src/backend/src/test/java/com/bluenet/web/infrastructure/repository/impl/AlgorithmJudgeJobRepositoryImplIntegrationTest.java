package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.domain.model.entity.AlgorithmJudgeJob;
import com.bluenet.web.domain.model.entity.AssessmentQuestion;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.entity.College;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.JudgeJobStatus;
import com.bluenet.web.domain.repository.AlgorithmJudgeJobRepository;
import com.bluenet.web.domain.repository.AssessmentQuestionRepository;
import com.bluenet.web.domain.repository.AssessmentTimeRepository;
import com.bluenet.web.domain.repository.CollegeRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.infrastructure.repository.dataobject.AlgorithmJudgeJobDO;
import com.bluenet.web.infrastructure.repository.mapper.AlgorithmJudgeJobMapper;
import com.bluenet.web.testsupport.fixture.AssessmentFixture;
import com.bluenet.web.testsupport.fixture.CollegeFixture;
import com.bluenet.web.testsupport.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AlgorithmJudgeJobRepositoryImpl 集成测试。
 */
@DisplayName("AlgorithmJudgeJobRepositoryImpl 集成测试")
class AlgorithmJudgeJobRepositoryImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AlgorithmJudgeJobRepository algorithmJudgeJobRepository;

    @Autowired
    private AlgorithmJudgeJobMapper algorithmJudgeJobMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CollegeRepository collegeRepository;

    @Autowired
    private AssessmentTimeRepository assessmentTimeRepository;

    @Autowired
    private AssessmentQuestionRepository assessmentQuestionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private AlgorithmJudgeJobFixtureState prepareFixture(String studentId, Direction direction, Integer epoch,
            Integer grade) {
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
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTimeId(assessmentTime.getId())
                .algorithm()
                .save(assessmentQuestionRepository);
        return new AlgorithmJudgeJobFixtureState(user, assessmentTime, question);
    }

    @Test
    @DisplayName("save: 应插入新记录并回写ID")
    void save_shouldInsertAndReturnId() {
        AlgorithmJudgeJobFixtureState state = prepareFixture("2024001001", Direction.COMPUTER_VISION, 1, 2024);
        AlgorithmJudgeJob job = AssessmentFixture.algorithmJudgeJobBuilder()
                .question(state.question)
                .user(state.user)
                .sourceCode("int main() { return 0; }")
                .build();

        algorithmJudgeJobRepository.save(job);

        assertNotNull(job.getId());
        AlgorithmJudgeJobDO dataObject = algorithmJudgeJobMapper.selectById(job.getId());
        assertNotNull(dataObject);
        assertEquals(state.user.getId(), dataObject.getUserId());
        assertEquals(state.question.getId(), dataObject.getQuestionId());
        assertEquals(JudgeJobStatus.PENDING, dataObject.getStatus());
        assertEquals(0, Integer.valueOf(0).compareTo(dataObject.getRetryCount()));
    }

    @Test
    @DisplayName("save: 应更新已有任务状态")
    void save_shouldUpdateExistingJobStatus() {
        AlgorithmJudgeJobFixtureState state = prepareFixture("2024001002", Direction.STRUCTURAL_DESIGN, 1, 2024);
        AlgorithmJudgeJob job = AssessmentFixture.algorithmJudgeJobBuilder()
                .question(state.question)
                .user(state.user)
                .sourceCode("int main() { return 0; }")
                .build();
        algorithmJudgeJobRepository.save(job);
        Long jobId = job.getId();
        job.markRunning();

        algorithmJudgeJobRepository.save(job);

        AlgorithmJudgeJobDO dataObject = algorithmJudgeJobMapper.selectById(jobId);
        assertNotNull(dataObject);
        assertEquals(JudgeJobStatus.RUNNING, dataObject.getStatus());
        assertNotNull(dataObject.getStartedAt());
    }

    @Test
    @DisplayName("findById: 存在返回实体，不存在返回空")
    void findById_shouldReturnOptional() {
        AlgorithmJudgeJobFixtureState state = prepareFixture("2024001003", Direction.STRUCTURAL_DESIGN, 1, 2024);
        AlgorithmJudgeJob job = AssessmentFixture.algorithmJudgeJobBuilder()
                .question(state.question)
                .user(state.user)
                .sourceCode("int main() { return 0; }")
                .build();
        algorithmJudgeJobRepository.save(job);

        Optional<AlgorithmJudgeJob> found = algorithmJudgeJobRepository.findById(job.getId());
        assertTrue(found.isPresent());
        assertEquals(state.user.getId(), found.get().getUserId());
        assertEquals(state.question.getId(), found.get().getQuestionId());

        assertTrue(algorithmJudgeJobRepository.findById(-1L).isEmpty());
    }

    private record AlgorithmJudgeJobFixtureState(User user, AssessmentTime assessmentTime,
            AssessmentQuestion question) {
    }
}
