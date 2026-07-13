package com.bluenet.web.api.controller.v1.assessment;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.converter.assessment_time.AssessmentTimeResponseConverter;
import com.bluenet.web.api.dto.assessment_time.AssessmentProgressDTO;
import com.bluenet.web.api.dto.assessment_time.AssessmentTimeDTO;
import com.bluenet.web.application.result.assessment.AssessmentProgressResult;
import com.bluenet.web.application.result.assessment.AssessmentTimeResult;
import com.bluenet.web.application.service.AssessmentTimeAppService;
import com.bluenet.web.domain.model.enumerate.Direction;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@DisplayName("AssessmentTimeController 集成测试")
class AssessmentTimeControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AssessmentTimeAppService assessmentTimeAppService;

    @MockitoBean
    private AssessmentTimeResponseConverter responseConverter;

    @AfterEach
    void tearDown() {
        UserCTX.clear();
    }

    @Test
    @DisplayName("listAssessmentTimes: 已登录用户查询考核时间列表应返回分页")
    @WithSecurityPrincipal(userId = 100L)
    void listAssessmentTimes_shouldReturnPagedList() throws Exception {
        AssessmentTimeResult result = new AssessmentTimeResult(
                1L, Direction.COMPUTER_VISION, 1, 2024,
                LocalDateTime.now(), LocalDateTime.now().plusDays(1),
                false, null, false, 10, 0, false);
        AssessmentTimeDTO dto = AssessmentTimeDTO.builder()
                .id(1L)
                .direction(Direction.COMPUTER_VISION)
                .epoch(1)
                .build();

        when(assessmentTimeAppService.listAssessmentTimesForUser(100L, 0, 5))
                .thenReturn(new PageImpl<>(List.of(result)));
        when(responseConverter.toDTO(any(AssessmentTimeResult.class))).thenReturn(dto);

        mockMvc.perform(
                get("/api/v1/assessment-times")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content[0].id").value(1));
    }

    @Test
    @DisplayName("getAssessmentProgress: 已登录用户查询考核进度应返回进度 DTO")
    @WithSecurityPrincipal(userId = 100L)
    void getAssessmentProgress_shouldReturnProgressDto() throws Exception {
        AssessmentProgressResult result = new AssessmentProgressResult(1L, 10, 3);
        AssessmentProgressDTO dto = AssessmentProgressDTO.builder()
                .assessmentTimeId(1L)
                .totalQuestions(10)
                .completedQuestions(3)
                .build();

        when(assessmentTimeAppService.getAssessmentProgress(100L, 1L)).thenReturn(result);
        when(responseConverter.toDTO(result)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/assessment-times/{id}/progress", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalQuestions").value(10))
                .andExpect(jsonPath("$.data.completedQuestions").value(3));
    }
}
