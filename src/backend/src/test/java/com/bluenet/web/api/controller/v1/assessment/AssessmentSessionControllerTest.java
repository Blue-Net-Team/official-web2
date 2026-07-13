package com.bluenet.web.api.controller.v1.assessment;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.converter.assessment_session.AssessmentSessionRequestConverter;
import com.bluenet.web.api.converter.assessment_session.AssessmentSessionResponseConverter;
import com.bluenet.web.api.dto.assessment_session.AssessmentSessionDTO;
import com.bluenet.web.application.command.assessment_session.AssessmentSessionCommands;
import com.bluenet.web.application.result.assessment.AssessmentSessionResult;
import com.bluenet.web.application.service.AssessmentSessionAppService;
import com.bluenet.web.domain.exception.DataNotFound;
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

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@DisplayName("AssessmentSessionController 集成测试")
class AssessmentSessionControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AssessmentSessionAppService assessmentSessionAppService;

    @MockitoBean
    private AssessmentSessionRequestConverter assessmentSessionRequestConverter;

    @MockitoBean
    private AssessmentSessionResponseConverter assessmentSessionResponseConverter;

    @AfterEach
    void tearDown() {
        UserCTX.clear();
    }

    @Test
    @DisplayName("getSession: 已登录用户获取考核会话应返回会话 DTO")
    @WithSecurityPrincipal(userId = 100L)
    void getSession_shouldReturnSessionDto() throws Exception {
        AssessmentSessionCommands.GetOrCreateSessionCommand command = new AssessmentSessionCommands.GetOrCreateSessionCommand(
                100L, 1L);
        AssessmentSessionResult result = new AssessmentSessionResult(
                1L, 100L, 1L, LocalDateTime.now(), LocalDateTime.now().plusMinutes(60));
        AssessmentSessionDTO dto = AssessmentSessionDTO.builder()
                .id(1L)
                .userId(100L)
                .assessmentTimeId(1L)
                .deadline(result.deadline())
                .build();

        when(assessmentSessionRequestConverter.toCommand(100L, 1L)).thenReturn(command);
        when(assessmentSessionAppService.getOrCreateSession(command)).thenReturn(result);
        when(assessmentSessionResponseConverter.toDTO(result)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/assessment-sessions/{assessmentTimeId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.userId").value(100));
    }

    @Test
    @DisplayName("getSession: 考核时间不存在应返回 404")
    @WithSecurityPrincipal(userId = 100L)
    void getSession_whenNotFound_shouldReturn404() throws Exception {
        AssessmentSessionCommands.GetOrCreateSessionCommand command = new AssessmentSessionCommands.GetOrCreateSessionCommand(
                100L, 99L);

        when(assessmentSessionRequestConverter.toCommand(100L, 99L)).thenReturn(command);
        when(assessmentSessionAppService.getOrCreateSession(command))
                .thenThrow(new DataNotFound("考核时间不存在"));

        mockMvc.perform(get("/api/v1/assessment-sessions/{assessmentTimeId}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }
}
