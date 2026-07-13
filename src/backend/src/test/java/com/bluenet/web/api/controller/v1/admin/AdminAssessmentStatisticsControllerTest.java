package com.bluenet.web.api.controller.v1.admin;

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
@DisplayName("AdminAssessmentStatisticsController 集成测试")
class AdminAssessmentStatisticsControllerTest extends BaseIntegrationTest {

    private static final long ADMIN_USER_ID = 9999L;

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
    @DisplayName("getQuestionStatistics: 管理员查询题目统计应返回 DTO")
    @WithSecurityPrincipal(userId = ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-statistics:query" })
    void getQuestionStatistics_shouldReturnDto() throws Exception {
        AssessmentStatisticsResult result = new AssessmentStatisticsResult(
                1L, QuestionType.ALGORITHM, 20L, 15L,
                new BigDecimal("0.75"), Map.of(ObjectiveResultCode.AC, 15L, ObjectiveResultCode.WA, 5L));
        QuestionStatisticsDTO dto = QuestionStatisticsDTO.builder()
                .questionId(1L)
                .questionType(QuestionType.ALGORITHM)
                .submittedCount(20L)
                .acceptedCount(15L)
                .passRate(new BigDecimal("0.75"))
                .resultDistribution(Map.of(ObjectiveResultCode.AC, 15L, ObjectiveResultCode.WA, 5L))
                .build();

        when(assessmentStatisticsAppService.getQuestionStatistics(1L)).thenReturn(result);
        when(responseConverter.toDTO(result)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/admin/assessment-statistics/questions/{questionId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.questionId").value(1))
                .andExpect(jsonPath("$.data.submittedCount").value(20));
    }

    @Test
    @DisplayName("getQuestionStatistics: 无权限应返回 403")
    @WithSecurityPrincipal(userId = ADMIN_USER_ID, roleType = "MEMBER", roleId = 3L, permissions = {})
    void getQuestionStatistics_withoutPermission_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/v1/admin/assessment-statistics/questions/{questionId}", 1L))
                .andExpect(status().isForbidden());
    }
}
