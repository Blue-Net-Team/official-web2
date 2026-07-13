package com.bluenet.web.api.controller.v1.assessment;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.converter.assessment_statistics.AssessmentStatisticsResponseConverter;
import com.bluenet.web.api.dto.assessment_statistics.QuestionStatisticsDTO;
import com.bluenet.web.application.result.assessment.AssessmentStatisticsResult;
import com.bluenet.web.application.service.AssessmentStatisticsAppService;
import com.bluenet.web.domain.model.enumerate.ObjectiveResultCode;
import com.bluenet.web.domain.model.enumerate.QuestionType;
import com.bluenet.web.infrastructure.security.principal.WithSecurityPrincipal;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.bluenet.web.testconfig.TestSecurityConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@DisplayName("AssessmentStatisticsController 集成测试")
class AssessmentStatisticsControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AssessmentStatisticsAppService assessmentStatisticsAppService;

    @MockitoBean
    private AssessmentStatisticsResponseConverter responseConverter;

    @AfterEach
    void tearDown() {
        UserCTX.clear();
    }

    @Test
    @DisplayName("getCandidateQuestionStatistics: 已登录用户查询题目统计应返回 DTO")
    @WithSecurityPrincipal(userId = 100L)
    void getCandidateQuestionStatistics_shouldReturnDto() throws Exception {
        AssessmentStatisticsResult result = new AssessmentStatisticsResult(
                1L, QuestionType.SINGLE_CHOICE, 10L, 8L,
                new BigDecimal("0.80"), Map.of(ObjectiveResultCode.AC, 8L, ObjectiveResultCode.WA, 2L));
        QuestionStatisticsDTO dto = QuestionStatisticsDTO.builder()
                .questionId(1L)
                .questionType(QuestionType.SINGLE_CHOICE)
                .submittedCount(10L)
                .acceptedCount(8L)
                .passRate(new BigDecimal("0.80"))
                .resultDistribution(Map.of(ObjectiveResultCode.AC, 8L, ObjectiveResultCode.WA, 2L))
                .build();

        when(assessmentStatisticsAppService.getCandidateQuestionStatistics(1L)).thenReturn(result);
        when(responseConverter.toDTO(result)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/assessment-statistics/questions/{questionId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.questionId").value(1))
                .andExpect(jsonPath("$.data.passRate").value(0.80));
    }
}
