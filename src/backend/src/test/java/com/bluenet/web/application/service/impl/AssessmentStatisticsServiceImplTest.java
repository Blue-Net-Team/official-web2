package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.controller.v1.AssessmentStatisticsController;
import com.bluenet.web.api.controller.v1.admin.AdminAssessmentStatisticsController;
import com.bluenet.web.api.dto.assessment_question.AssessmentQuestionDTO;
import com.bluenet.web.api.dto.assessment_statistics.QuestionStatisticsDTO;
import com.bluenet.web.application.service.AssessmentQuestionService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.model.enumerate.ObjectiveResultCode;
import com.bluenet.web.domain.model.enumerate.QuestionType;
import com.bluenet.web.domain.model.vo.AssessmentJudgementVO;
import com.bluenet.web.domain.model.vo.AssessmentQuestionVO;
import com.bluenet.web.domain.repository.AssessmentJudgementRepository;
import com.bluenet.web.domain.service.AssessmentQuestionDomainService;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@DisplayName("AssessmentStatisticsServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class AssessmentStatisticsServiceImplTest {
    private static final Long QUESTION_ID = 10L;

    @Mock
    private AssessmentQuestionDomainService assessmentQuestionDomainService;
    @Mock
    private AssessmentQuestionService assessmentQuestionService;
    @Mock
    private AssessmentJudgementRepository assessmentJudgementRepository;

    @InjectMocks
    private AssessmentStatisticsServiceImpl assessmentStatisticsService;

    @Test
    @DisplayName("选择题统计：只包含 AC/WA 分布")
    void getQuestionStatistics_choice_shouldAggregateAcWaOnly() {
        when(assessmentQuestionDomainService.getQuestionById(QUESTION_ID))
                .thenReturn(createQuestion(QuestionType.SINGLE_CHOICE));
        when(assessmentJudgementRepository.findLatestObjectiveByQuestionId(QUESTION_ID))
                .thenReturn(
                        List.of(
                                createJudgement(ObjectiveResultCode.AC),
                                createJudgement(ObjectiveResultCode.WA),
                                createJudgement(ObjectiveResultCode.WA)));

        QuestionStatisticsDTO result = assessmentStatisticsService.getQuestionStatistics(QUESTION_ID);

        assertEquals(3L, result.getSubmittedCount());
        assertEquals(1L, result.getAcceptedCount());
        assertEquals(new BigDecimal("0.3333"), result.getPassRate());
        assertEquals(1L, result.getResultDistribution().get(ObjectiveResultCode.AC));
        assertEquals(2L, result.getResultDistribution().get(ObjectiveResultCode.WA));
        assertEquals(2, result.getResultDistribution().size());
    }

    @Test
    @DisplayName("算法题统计：应保留 ACM 错误类型分布")
    void getQuestionStatistics_algorithm_shouldAggregateAcmDistribution() {
        when(assessmentQuestionDomainService.getQuestionById(QUESTION_ID))
                .thenReturn(createQuestion(QuestionType.ALGORITHM));
        when(assessmentJudgementRepository.findLatestObjectiveByQuestionId(QUESTION_ID))
                .thenReturn(
                        List.of(
                                createJudgement(ObjectiveResultCode.AC),
                                createJudgement(ObjectiveResultCode.WA),
                                createJudgement(ObjectiveResultCode.TLE),
                                createJudgement(ObjectiveResultCode.RE),
                                createJudgement(ObjectiveResultCode.CE),
                                createJudgement(ObjectiveResultCode.MLE)));

        QuestionStatisticsDTO result = assessmentStatisticsService.getQuestionStatistics(QUESTION_ID);

        assertEquals(6L, result.getSubmittedCount());
        assertEquals(1L, result.getAcceptedCount());
        assertEquals(new BigDecimal("0.1667"), result.getPassRate());
        assertEquals(1L, result.getResultDistribution().get(ObjectiveResultCode.TLE));
        assertEquals(1L, result.getResultDistribution().get(ObjectiveResultCode.RE));
        assertEquals(1L, result.getResultDistribution().get(ObjectiveResultCode.CE));
        assertEquals(1L, result.getResultDistribution().get(ObjectiveResultCode.MLE));
    }

    @Test
    @DisplayName("没有正式评判：通过率应为 0")
    void getQuestionStatistics_empty_shouldReturnZeroPassRate() {
        when(assessmentQuestionDomainService.getQuestionById(QUESTION_ID))
                .thenReturn(createQuestion(QuestionType.MULTIPLE_CHOICE));
        when(assessmentJudgementRepository.findLatestObjectiveByQuestionId(QUESTION_ID))
                .thenReturn(List.of());

        QuestionStatisticsDTO result = assessmentStatisticsService.getQuestionStatistics(QUESTION_ID);

        assertEquals(0L, result.getSubmittedCount());
        assertEquals(BigDecimal.ZERO, result.getPassRate());
    }

    @Test
    @DisplayName("文件上传题：应拒绝客观题统计")
    void getQuestionStatistics_fileUpload_shouldReject() {
        when(assessmentQuestionDomainService.getQuestionById(QUESTION_ID))
                .thenReturn(createQuestion(QuestionType.FILE_UPLOAD));

        BadRequest ex = assertThrows(
                BadRequest.class,
                () -> assessmentStatisticsService.getQuestionStatistics(QUESTION_ID));
        assertEquals("文件上传题不参与客观题通过率统计", ex.getMessage());
    }

    @Test
    @DisplayName("考生端统计：配置未开启时应拒绝展示")
    void getCandidateQuestionStatistics_disabled_shouldReject() {
        BadRequest ex = assertThrows(
                BadRequest.class,
                () -> assessmentStatisticsService.getCandidateQuestionStatistics(QUESTION_ID));

        assertEquals("考生端题目通过率展示未开启", ex.getMessage());
    }

    @Test
    @DisplayName("考生端统计：配置开启时应复用题目详情权限并返回通过率")
    void getCandidateQuestionStatistics_enabled_shouldCheckCandidateScopeAndAggregate() {
        ReflectionTestUtils.setField(assessmentStatisticsService, "candidateStatisticsVisible", true);
        when(assessmentQuestionService.getQuestionDetailForUser(QUESTION_ID))
                .thenReturn(AssessmentQuestionDTO.builder().id(QUESTION_ID).build());
        when(assessmentQuestionDomainService.getQuestionById(QUESTION_ID))
                .thenReturn(createQuestion(QuestionType.ALGORITHM));
        when(assessmentJudgementRepository.findLatestObjectiveByQuestionId(QUESTION_ID))
                .thenReturn(
                        List.of(
                                createJudgement(ObjectiveResultCode.AC),
                                createJudgement(ObjectiveResultCode.WA)));

        QuestionStatisticsDTO result = assessmentStatisticsService.getCandidateQuestionStatistics(QUESTION_ID);

        assertEquals(2L, result.getSubmittedCount());
        assertEquals(1L, result.getAcceptedCount());
        assertEquals(new BigDecimal("0.5000"), result.getPassRate());
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

    private AssessmentQuestionVO createQuestion(QuestionType questionType) {
        return AssessmentQuestionVO.builder()
                .id(QUESTION_ID)
                .questionType(questionType)
                .build();
    }

    private AssessmentJudgementVO createJudgement(ObjectiveResultCode resultCode) {
        return AssessmentJudgementVO.builder()
                .questionId(QUESTION_ID)
                .resultCode(resultCode)
                .build();
    }
}
