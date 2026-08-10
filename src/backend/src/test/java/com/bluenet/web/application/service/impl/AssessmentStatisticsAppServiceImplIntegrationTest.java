package com.bluenet.web.application.service.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.application.result.assessment.AssessmentStatisticsResult;
import com.bluenet.web.application.service.AssessmentQuestionAppService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.AssessmentAnswer;
import com.bluenet.web.domain.model.entity.AssessmentJudgement;
import com.bluenet.web.domain.model.entity.AssessmentQuestion;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.JudgementSource;
import com.bluenet.web.domain.model.enumerate.JudgementStatus;
import com.bluenet.web.domain.model.enumerate.ObjectiveResultCode;
import com.bluenet.web.domain.model.enumerate.QuestionType;
import com.bluenet.web.domain.model.enumerate.ReviewerType;
import com.bluenet.web.domain.repository.AssessmentAnswerRepository;
import com.bluenet.web.domain.repository.AssessmentJudgementRepository;
import com.bluenet.web.domain.repository.AssessmentQuestionRepository;
import com.bluenet.web.domain.repository.AssessmentTimeRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.testsupport.fixture.AssessmentFixture;
import com.bluenet.web.testsupport.fixture.TimeFixture;
import com.bluenet.web.testsupport.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AssessmentStatisticsAppServiceImpl 集成测试。
 *
 * <p>
 * 验证考核统计应用服务的题目通过率计算、结果分布、考生端可见性开关及最新评判选取逻辑。
 * </p>
 */
@DisplayName("AssessmentStatisticsAppServiceImpl 集成测试")
class AssessmentStatisticsAppServiceImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AssessmentStatisticsAppServiceImpl assessmentStatisticsAppService;

    @Autowired
    private AssessmentQuestionRepository assessmentQuestionRepository;

    @Autowired
    private AssessmentAnswerRepository assessmentAnswerRepository;

    @Autowired
    private AssessmentJudgementRepository assessmentJudgementRepository;

    @Autowired
    private AssessmentTimeRepository assessmentTimeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private AssessmentQuestionAppService assessmentQuestionAppService;

    private long sequence = 1000;

    private String nextStudentId(String prefix) {
        return prefix + (++sequence);
    }

    private User createSuperAdmin() {
        return UserFixture.superAdmin(nextStudentId("SA")).save(userRepository, passwordEncoder);
    }

    private User createCandidate(Direction direction, Integer gradeYear) {
        return UserFixture.candidate(nextStudentId("SC"))
                .withDirection(direction)
                .withAssessmentGradeYear(gradeYear)
                .save(userRepository, passwordEncoder);
    }

    private AssessmentTime createActiveTime() {
        return AssessmentFixture.timeBuilder()
                .direction(Direction.COMPUTER_VISION)
                .epoch(1)
                .grade(2024)
                .save(assessmentTimeRepository);
    }

    private AssessmentQuestion createQuestion(AssessmentTime time, QuestionType questionType) {
        AssessmentFixture.QuestionBuilder builder = AssessmentFixture.questionBuilder().assessmentTime(time);
        if (questionType == QuestionType.SINGLE_CHOICE) {
            builder.singleChoice("A", "A", "B", "C");
        } else if (questionType == QuestionType.ALGORITHM) {
            builder.algorithm();
        } else if (questionType == QuestionType.FILE_UPLOAD) {
            builder.fileUpload();
        }
        return builder.save(assessmentQuestionRepository);
    }

    private AssessmentAnswer createAnswer(AssessmentQuestion question, User candidate) {
        return AssessmentFixture.answerBuilder()
                .user(candidate)
                .question(question)
                .save(assessmentAnswerRepository);
    }

    private AssessmentJudgement createObjectiveJudgement(AssessmentQuestion question, User candidate,
            ObjectiveResultCode resultCode) {
        AssessmentAnswer answer = createAnswer(question, candidate);
        return createObjectiveJudgement(question, candidate, answer, resultCode);
    }

    private AssessmentJudgement createObjectiveJudgement(AssessmentQuestion question, User candidate,
            AssessmentAnswer answer, ObjectiveResultCode resultCode) {
        User admin = createSuperAdmin();
        AssessmentJudgement judgement = AssessmentJudgement.create(
                answer.getId(),
                question.getId(),
                question.getAssessmentTimeId(),
                candidate.getId(),
                new BigDecimal("100"),
                new BigDecimal("100"),
                JudgementStatus.JUDGED,
                resultCode,
                JudgementSource.AUTO,
                admin.getId(),
                ReviewerType.SYSTEM,
                TimeFixture.now());
        assessmentJudgementRepository.save(judgement);
        return judgement;
    }

    private void enableCandidateStatistics() {
        ReflectionTestUtils.setField(assessmentStatisticsAppService, "candidateStatisticsVisible", true);
    }

    private void assertPassRateEquals(BigDecimal expected, BigDecimal actual) {
        assertEquals(0, expected.compareTo(actual));
    }

    @Test
    @DisplayName("getQuestionStatistics: 题目不存在时应抛 DataNotFound")
    void getQuestionStatistics_questionNotFound_shouldThrowDataNotFound() {
        assertThrows(DataNotFound.class, () -> assessmentStatisticsAppService.getQuestionStatistics(999999L));
    }

    @Test
    @DisplayName("getQuestionStatistics: 文件上传题应抛 BadRequest")
    void getQuestionStatistics_fileUploadQuestion_shouldThrowBadRequest() {
        AssessmentTime time = createActiveTime();
        AssessmentQuestion question = createQuestion(time, QuestionType.FILE_UPLOAD);

        assertThrows(BadRequest.class, () -> assessmentStatisticsAppService.getQuestionStatistics(question.getId()));
    }

    @Test
    @DisplayName("getQuestionStatistics: 无评判记录时应返回零值分布")
    void getQuestionStatistics_noJudgements_shouldReturnZeroCounts() {
        AssessmentTime time = createActiveTime();
        AssessmentQuestion question = createQuestion(time, QuestionType.SINGLE_CHOICE);

        AssessmentStatisticsResult result = assessmentStatisticsAppService.getQuestionStatistics(question.getId());

        assertNotNull(result);
        assertEquals(question.getId(), result.questionId());
        assertEquals(QuestionType.SINGLE_CHOICE, result.questionType());
        assertEquals(0L, result.submittedCount());
        assertEquals(0L, result.acceptedCount());
        assertPassRateEquals(BigDecimal.ZERO, result.passRate());
        Map<ObjectiveResultCode, Long> distribution = result.resultDistribution();
        assertEquals(0L, distribution.get(ObjectiveResultCode.AC));
        assertEquals(0L, distribution.get(ObjectiveResultCode.WA));
    }

    @Test
    @DisplayName("getQuestionStatistics: 单选题应返回正确的通过率和结果分布")
    void getQuestionStatistics_singleChoiceWithJudgements_shouldReturnCorrectDistribution() {
        AssessmentTime time = createActiveTime();
        AssessmentQuestion question = createQuestion(time, QuestionType.SINGLE_CHOICE);
        User candidate1 = createCandidate(Direction.COMPUTER_VISION, 2024);
        User candidate2 = createCandidate(Direction.COMPUTER_VISION, 2024);
        User candidate3 = createCandidate(Direction.COMPUTER_VISION, 2024);
        createObjectiveJudgement(question, candidate1, ObjectiveResultCode.AC);
        createObjectiveJudgement(question, candidate2, ObjectiveResultCode.WA);
        createObjectiveJudgement(question, candidate3, ObjectiveResultCode.AC);

        AssessmentStatisticsResult result = assessmentStatisticsAppService.getQuestionStatistics(question.getId());

        assertEquals(3L, result.submittedCount());
        assertEquals(2L, result.acceptedCount());
        assertPassRateEquals(new BigDecimal("0.6667"), result.passRate());
        Map<ObjectiveResultCode, Long> distribution = result.resultDistribution();
        assertEquals(2L, distribution.get(ObjectiveResultCode.AC));
        assertEquals(1L, distribution.get(ObjectiveResultCode.WA));
    }

    @Test
    @DisplayName("getQuestionStatistics: 算法题应返回包含所有结果码的分布")
    void getQuestionStatistics_algorithmQuestion_shouldReturnAllResultCodes() {
        AssessmentTime time = createActiveTime();
        AssessmentQuestion question = createQuestion(time, QuestionType.ALGORITHM);
        createObjectiveJudgement(question, createCandidate(Direction.COMPUTER_VISION, 2024), ObjectiveResultCode.AC);
        createObjectiveJudgement(question, createCandidate(Direction.COMPUTER_VISION, 2024), ObjectiveResultCode.WA);
        createObjectiveJudgement(question, createCandidate(Direction.COMPUTER_VISION, 2024), ObjectiveResultCode.TLE);
        createObjectiveJudgement(question, createCandidate(Direction.COMPUTER_VISION, 2024), ObjectiveResultCode.RE);
        createObjectiveJudgement(question, createCandidate(Direction.COMPUTER_VISION, 2024), ObjectiveResultCode.CE);
        createObjectiveJudgement(question, createCandidate(Direction.COMPUTER_VISION, 2024), ObjectiveResultCode.MLE);

        AssessmentStatisticsResult result = assessmentStatisticsAppService.getQuestionStatistics(question.getId());

        assertEquals(6L, result.submittedCount());
        assertEquals(1L, result.acceptedCount());
        assertPassRateEquals(new BigDecimal("0.1667"), result.passRate());
        Map<ObjectiveResultCode, Long> distribution = result.resultDistribution();
        assertEquals(1L, distribution.get(ObjectiveResultCode.AC));
        assertEquals(1L, distribution.get(ObjectiveResultCode.WA));
        assertEquals(1L, distribution.get(ObjectiveResultCode.TLE));
        assertEquals(1L, distribution.get(ObjectiveResultCode.RE));
        assertEquals(1L, distribution.get(ObjectiveResultCode.CE));
        assertEquals(1L, distribution.get(ObjectiveResultCode.MLE));
    }

    @Test
    @DisplayName("getQuestionStatistics: 同一用户的多次评判应只取最新的一次")
    void getQuestionStatistics_multipleJudgementsPerUser_shouldUseLatestOnly() {
        AssessmentTime time = createActiveTime();
        AssessmentQuestion question = createQuestion(time, QuestionType.SINGLE_CHOICE);
        User candidate = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentAnswer answer = createAnswer(question, candidate);
        createObjectiveJudgement(question, candidate, answer, ObjectiveResultCode.WA);
        createObjectiveJudgement(question, candidate, answer, ObjectiveResultCode.AC);

        AssessmentStatisticsResult result = assessmentStatisticsAppService.getQuestionStatistics(question.getId());

        assertEquals(1L, result.submittedCount());
        assertEquals(1L, result.acceptedCount());
        assertPassRateEquals(new BigDecimal("1.0000"), result.passRate());
        Map<ObjectiveResultCode, Long> distribution = result.resultDistribution();
        assertEquals(1L, distribution.get(ObjectiveResultCode.AC));
        assertEquals(0L, distribution.get(ObjectiveResultCode.WA));
    }

    @Test
    @DisplayName("getCandidateQuestionStatistics: 考生端统计未开启时应抛 BadRequest")
    void getCandidateQuestionStatistics_disabled_shouldThrowBadRequest() {
        AssessmentTime time = createActiveTime();
        AssessmentQuestion question = createQuestion(time, QuestionType.SINGLE_CHOICE);

        assertThrows(
                BadRequest.class,
                () -> assessmentStatisticsAppService.getCandidateQuestionStatistics(question.getId()));
        verify(assessmentQuestionAppService, never()).getQuestionDetailForUser(any());
    }

    @Test
    @DisplayName("getCandidateQuestionStatistics: 开启且题目存在时应返回统计结果")
    void getCandidateQuestionStatistics_visibleAndQuestionExists_shouldReturnStatistics() {
        enableCandidateStatistics();
        AssessmentTime time = createActiveTime();
        AssessmentQuestion question = createQuestion(time, QuestionType.SINGLE_CHOICE);
        User candidate = createCandidate(Direction.COMPUTER_VISION, 2024);
        createObjectiveJudgement(question, candidate, ObjectiveResultCode.AC);

        AssessmentStatisticsResult result = assessmentStatisticsAppService
                .getCandidateQuestionStatistics(question.getId());

        assertNotNull(result);
        assertEquals(question.getId(), result.questionId());
        assertEquals(QuestionType.SINGLE_CHOICE, result.questionType());
        assertEquals(1L, result.submittedCount());
        assertEquals(1L, result.acceptedCount());
        assertPassRateEquals(new BigDecimal("1.0000"), result.passRate());
        verify(assessmentQuestionAppService).getQuestionDetailForUser(question.getId());
    }

    @Test
    @DisplayName("getCandidateQuestionStatistics: 开启但题目详情校验抛异常时应继续向上传播")
    void getCandidateQuestionStatistics_visibleButDetailThrows_shouldPropagateException() {
        enableCandidateStatistics();
        AssessmentTime time = createActiveTime();
        AssessmentQuestion question = createQuestion(time, QuestionType.SINGLE_CHOICE);
        when(assessmentQuestionAppService.getQuestionDetailForUser(question.getId()))
                .thenThrow(new DataNotFound("题目未开放"));

        DataNotFound exception = assertThrows(
                DataNotFound.class,
                () -> assessmentStatisticsAppService.getCandidateQuestionStatistics(question.getId()));
        assertTrue(exception.getMessage().contains("题目未开放"));
    }
}
