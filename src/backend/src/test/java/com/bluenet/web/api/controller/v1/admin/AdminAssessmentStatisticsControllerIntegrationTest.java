package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.converter.assessment_statistics.AssessmentStatisticsResponseConverter;
import com.bluenet.web.api.dto.assessment_statistics.QuestionStatisticsDTO;
import com.bluenet.web.application.result.assessment.AssessmentStatisticsResult;
import com.bluenet.web.application.service.AssessmentStatisticsAppService;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.enumerate.ObjectiveResultCode;
import com.bluenet.web.domain.model.enumerate.QuestionType;
import com.bluenet.web.infrastructure.security.principal.WithSecurityPrincipal;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.bluenet.web.testconfig.TestSecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@DisplayName("AdminAssessmentStatisticsController 集成测试")
class AdminAssessmentStatisticsControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AssessmentStatisticsAppService assessmentStatisticsAppService;

    @MockitoBean
    private AssessmentStatisticsResponseConverter responseConverter;

    @AfterEach
    void tearDown() {
        UserCTX.clear();
    }

    private static final long SUPER_ADMIN_USER_ID = 9999L;

    @Test
    @DisplayName("getQuestionStatistics: 普通成员访问应返回 403")
    @WithSecurityPrincipal(roleType = "MEMBER", roleId = 3L, permissions = {})
    void getQuestionStatistics_asMember_shouldReturn403() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/admin/assessment-statistics/questions/{questionId}", 1L))
                .andExpect(status().isForbidden())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(403);
    }

    @Test
    @DisplayName("getQuestionStatistics: 超级管理员应返回题目统计")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-statistics:query" })
    void getQuestionStatistics_asSuperAdmin_shouldReturnStatistics() throws Exception {
        AssessmentStatisticsResult statisticsResult = new AssessmentStatisticsResult(
                1L,
                QuestionType.SINGLE_CHOICE,
                100L,
                80L,
                new BigDecimal("0.80"),
                Map.of(ObjectiveResultCode.AC, 80L, ObjectiveResultCode.WA, 20L));
        QuestionStatisticsDTO dto = QuestionStatisticsDTO.builder()
                .questionId(1L)
                .questionType(QuestionType.SINGLE_CHOICE)
                .submittedCount(100L)
                .acceptedCount(80L)
                .passRate(new BigDecimal("0.80"))
                .resultDistribution(Map.of(ObjectiveResultCode.AC, 80L, ObjectiveResultCode.WA, 20L))
                .build();
        when(assessmentStatisticsAppService.getQuestionStatistics(1L)).thenReturn(statisticsResult);
        when(responseConverter.toDTO(any(AssessmentStatisticsResult.class))).thenReturn(dto);

        MvcResult result = mockMvc.perform(get("/api/v1/admin/assessment-statistics/questions/{questionId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.questionId").value(1))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("getQuestionStatistics: 题目不存在时应返回 404")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-statistics:query" })
    void getQuestionStatistics_whenNotFound_shouldReturn404() throws Exception {
        when(assessmentStatisticsAppService.getQuestionStatistics(1L)).thenThrow(new DataNotFound("题目不存在"));

        MvcResult result = mockMvc.perform(get("/api/v1/admin/assessment-statistics/questions/{questionId}", 1L))
                .andExpect(status().isNotFound())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(404);
    }
}
