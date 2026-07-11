package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.domain.model.entity.AlgorithmJudgeCaseResult;
import com.bluenet.web.domain.model.entity.AlgorithmJudgeJob;
import com.bluenet.web.domain.model.entity.AssessmentQuestion;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.entity.College;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.JudgeCaseStatus;
import com.bluenet.web.domain.repository.AlgorithmJudgeCaseResultRepository;
import com.bluenet.web.domain.repository.AlgorithmJudgeJobRepository;
import com.bluenet.web.domain.repository.AssessmentQuestionRepository;
import com.bluenet.web.domain.repository.AssessmentTimeRepository;
import com.bluenet.web.domain.repository.CollegeRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.testsupport.fixture.AssessmentFixture;
import com.bluenet.web.testsupport.fixture.CollegeFixture;
import com.bluenet.web.testsupport.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AlgorithmJudgeCaseResultRepositoryImpl 集成测试。
 */
@DisplayName("AlgorithmJudgeCaseResultRepositoryImpl 集成测试")
class AlgorithmJudgeCaseResultRepositoryImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AlgorithmJudgeCaseResultRepository algorithmJudgeCaseResultRepository;

    @Autowired
    private AlgorithmJudgeJobRepository algorithmJudgeJobRepository;

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

    private AlgorithmJudgeCaseResultFixtureState prepareFixture(String studentId, Direction direction, Integer epoch,
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
        AlgorithmJudgeJob job = AssessmentFixture.algorithmJudgeJobBuilder()
                .question(question)
                .user(user)
                .sourceCode("int main() { return 0; }")
                .save(algorithmJudgeJobRepository);
        return new AlgorithmJudgeCaseResultFixtureState(user, assessmentTime, question, job);
    }

    @Test
    @DisplayName("saveAll: 应插入多条用例结果")
    void saveAll_shouldInsertCaseResults() {
        AlgorithmJudgeCaseResultFixtureState state = prepareFixture("2024001001", Direction.COMPUTER_VISION, 1, 2024);
        AlgorithmJudgeCaseResult result1 = AssessmentFixture.algorithmJudgeCaseResultBuilder()
                .judgeJob(state.job)
                .caseNo(1)
                .build();
        AlgorithmJudgeCaseResult result2 = AssessmentFixture.algorithmJudgeCaseResultBuilder()
                .judgeJob(state.job)
                .caseNo(2)
                .status(JudgeCaseStatus.WA)
                .build();

        algorithmJudgeCaseResultRepository.saveAll(List.of(result1, result2));

        List<AlgorithmJudgeCaseResult> found = algorithmJudgeCaseResultRepository.findByJudgeJobId(state.job.getId());
        assertEquals(2, found.size());
        assertTrue(found.stream().anyMatch(r -> r.getCaseNo() == 1));
        assertTrue(found.stream().anyMatch(r -> r.getCaseNo() == 2));
    }

    @Test
    @DisplayName("findByJudgeJobId: 应返回指定任务的用例结果并按 caseNo 排序")
    void findByJudgeJobId_shouldReturnResultsOrderedByCaseNo() {
        AlgorithmJudgeCaseResultFixtureState state = prepareFixture("2024001002", Direction.STRUCTURAL_DESIGN, 1, 2024);
        AlgorithmJudgeCaseResult result1 = AssessmentFixture.algorithmJudgeCaseResultBuilder()
                .judgeJob(state.job)
                .caseNo(2)
                .build();
        AlgorithmJudgeCaseResult result2 = AssessmentFixture.algorithmJudgeCaseResultBuilder()
                .judgeJob(state.job)
                .caseNo(1)
                .build();
        algorithmJudgeCaseResultRepository.saveAll(List.of(result1, result2));

        List<AlgorithmJudgeCaseResult> found = algorithmJudgeCaseResultRepository.findByJudgeJobId(state.job.getId());

        assertEquals(2, found.size());
        assertEquals(1, found.get(0).getCaseNo());
        assertEquals(2, found.get(1).getCaseNo());
    }

    private record AlgorithmJudgeCaseResultFixtureState(User user, AssessmentTime assessmentTime,
            AssessmentQuestion question, AlgorithmJudgeJob job) {
    }
}
