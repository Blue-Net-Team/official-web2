package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.controller.v1.AssessmentStatisticsController;
import com.bluenet.web.api.controller.v1.admin.AdminAssessmentStatisticsController;
import com.bluenet.web.application.AssessmentStatisticsResult;
import com.bluenet.web.application.service.AssessmentQuestionAppService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.model.enumerate.ObjectiveResultCode;
import com.bluenet.web.domain.model.enumerate.QuestionType;
import com.bluenet.web.domain.model.entity.AssessmentJudgement;
import com.bluenet.web.domain.model.entity.AssessmentQuestion;
import com.bluenet.web.domain.repository.AssessmentJudgementRepository;
import com.bluenet.web.domain.repository.AssessmentQuestionRepository;
import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@DisplayName("AssessmentStatisticsAppServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class AssessmentStatisticsAppServiceImplTest {
    private static final Long QUESTION_ID = 10L;

    @Mock
    private AssessmentQuestionRepository assessmentQuestionRepository;
    @Mock
    private AssessmentQuestionAppService assessmentQuestionAppService;
    @Mock
    private AssessmentJudgementRepository assessmentJudgementRepository;

    @InjectMocks
    private AssessmentStatisticsAppServiceImpl assessmentStatisticsAppService;

    @Test
    @DisplayName("选择题统计：只包含 AC/WA 分布")
    void getQuestionStatistics_choice_shouldAggregateAcWaOnly() {
        when(assessmentQuestionRepository.findById(QUESTION_ID))
                .thenReturn(Optional.of(createQuestion(QuestionType.SINGLE_CHOICE)));
        when(assessmentJudgementRepository.findLatestObjectiveByQuestionId(QUESTION_ID))
                .thenReturn(
                        List.of(
                                createJudgement(ObjectiveResultCode.AC),
                                createJudgement(ObjectiveResultCode.WA),
                                createJudgement(ObjectiveResultCode.WA)));

        AssessmentStatisticsResult result = assessmentStatisticsAppService.getQuestionStatistics(QUESTION_ID);

        assertEquals(3L, result.submittedCount());
        assertEquals(1L, result.acceptedCount());
        assertEquals(new BigDecimal("0.3333"), result.passRate());
        assertEquals(1L, result.resultDistribution().get(ObjectiveResultCode.AC));
        assertEquals(2L, result.resultDistribution().get(ObjectiveResultCode.WA));
        assertEquals(2, result.resultDistribution().size());
    }

    @Test
    @DisplayName("算法题统计：应保留 ACM 错误类型分布")
    void getQuestionStatistics_algorithm_shouldAggregateAcmDistribution() {
        when(assessmentQuestionRepository.findById(QUESTION_ID))
                .thenReturn(Optional.of(createQuestion(QuestionType.ALGORITHM)));
        when(assessmentJudgementRepository.findLatestObjectiveByQuestionId(QUESTION_ID))
                .thenReturn(
                        List.of(
                                createJudgement(ObjectiveResultCode.AC),
                                createJudgement(ObjectiveResultCode.WA),
                                createJudgement(ObjectiveResultCode.TLE),
                                createJudgement(ObjectiveResultCode.RE),
                                createJudgement(ObjectiveResultCode.CE),
                                createJudgement(ObjectiveResultCode.MLE)));

        AssessmentStatisticsResult result = assessmentStatisticsAppService.getQuestionStatistics(QUESTION_ID);

        assertEquals(6L, result.submittedCount());
        assertEquals(1L, result.acceptedCount());
        assertEquals(new BigDecimal("0.1667"), result.passRate());
        assertEquals(1L, result.resultDistribution().get(ObjectiveResultCode.TLE));
        assertEquals(1L, result.resultDistribution().get(ObjectiveResultCode.RE));
        assertEquals(1L, result.resultDistribution().get(ObjectiveResultCode.CE));
        assertEquals(1L, result.resultDistribution().get(ObjectiveResultCode.MLE));
    }

    @Test
    @DisplayName("没有正式评判：通过率应为 0")
    void getQuestionStatistics_empty_shouldReturnZeroPassRate() {
        when(assessmentQuestionRepository.findById(QUESTION_ID))
                .thenReturn(Optional.of(createQuestion(QuestionType.MULTIPLE_CHOICE)));
        when(assessmentJudgementRepository.findLatestObjectiveByQuestionId(QUESTION_ID))
                .thenReturn(List.of());

        AssessmentStatisticsResult result = assessmentStatisticsAppService.getQuestionStatistics(QUESTION_ID);

        assertEquals(0L, result.submittedCount());
        assertEquals(BigDecimal.ZERO, result.passRate());
    }

    @Test
    @DisplayName("文件上传题：应拒绝客观题统计")
    void getQuestionStatistics_fileUpload_shouldReject() {
        when(assessmentQuestionRepository.findById(QUESTION_ID))
                .thenReturn(Optional.of(createQuestion(QuestionType.FILE_UPLOAD)));

        BadRequest ex = assertThrows(
                BadRequest.class,
                () -> assessmentStatisticsAppService.getQuestionStatistics(QUESTION_ID));
        assertEquals("文件上传题不参与客观题通过率统计", ex.getMessage());
    }

    @Test
    @DisplayName("考生端统计：配置未开启时应拒绝展示")
    void getCandidateQuestionStatistics_disabled_shouldReject() {
        BadRequest ex = assertThrows(
                BadRequest.class,
                () -> assessmentStatisticsAppService.getCandidateQuestionStatistics(QUESTION_ID));

        assertEquals("考生端题目通过率展示未开启", ex.getMessage());
    }

    @Test
    @DisplayName("考生端统计：配置开启时应复用题目详情权限并返回通过率")
    void getCandidateQuestionStatistics_enabled_shouldCheckCandidateScopeAndAggregate() {
        ReflectionTestUtils.setField(assessmentStatisticsAppService, "candidateStatisticsVisible", true);
        when(assessmentQuestionAppService.getQuestionDetailForUser(QUESTION_ID))
                .thenReturn(null);
        when(assessmentQuestionRepository.findById(QUESTION_ID))
                .thenReturn(Optional.of(createQuestion(QuestionType.ALGORITHM)));
        when(assessmentJudgementRepository.findLatestObjectiveByQuestionId(QUESTION_ID))
                .thenReturn(
                        List.of(
                                createJudgement(ObjectiveResultCode.AC),
                                createJudgement(ObjectiveResultCode.WA)));

        AssessmentStatisticsResult result = assessmentStatisticsAppService.getCandidateQuestionStatistics(QUESTION_ID);

        assertEquals(2L, result.submittedCount());
        assertEquals(1L, result.acceptedCount());
        assertEquals(new BigDecimal("0.5000"), result.passRate());
    }

    @Test
    @DisplayName("统计接口：应要求受保护权限")
    void controllerPermission_shouldBeProtected() throws NoSuchMethodException {
        Method method = AdminAssessmentStatisticsController.class
                .getMethod("getQuestionStatistics", Long.class);
        RequiresPermission permission = method.getAnnotation(RequiresPermission.class);

        assertEquals("assessment-statistics:query", permission.value());
        assertEquals(com.bluenet.web.infrastructure.security.annotation.AccessLevel.PROTECTED, permission.access());
        assertTrue(permission.name().contains("统计"));
    }

    @Test
    @DisplayName("考生端统计接口：应要求登录权限")
    void candidateControllerPermission_shouldBeAuthenticated() throws NoSuchMethodException {
        Method method = AssessmentStatisticsController.class
                .getMethod("getCandidateQuestionStatistics", Long.class);
        RequiresPermission permission = method.getAnnotation(RequiresPermission.class);

        assertEquals("assessment-statistics:candidate-query", permission.value());
        assertEquals(com.bluenet.web.infrastructure.security.annotation.AccessLevel.AUTHENTICATED, permission.access());
        assertTrue(permission.name().contains("统计"));
    }

    private AssessmentQuestion createQuestion(QuestionType questionType) {
        return AssessmentQuestion.reconstruct(
                QUESTION_ID,
                null,
                null,
                questionType,
                null,
                null,
                null,
                null);
    }

    private AssessmentJudgement createJudgement(ObjectiveResultCode resultCode) {
        return AssessmentJudgement.reconstruct(
                null,
                null,
                QUESTION_ID,
                null,
                null,
                null,
                null,
                null,
                resultCode,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }
}
