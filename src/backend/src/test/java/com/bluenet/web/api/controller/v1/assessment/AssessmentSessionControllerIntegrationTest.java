package com.bluenet.web.api.controller.v1.assessment;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.assessment_session.AssessmentSessionDTO;
import com.bluenet.web.api.converter.assessment_session.AssessmentSessionRequestConverter;
import com.bluenet.web.api.converter.assessment_session.AssessmentSessionResponseConverter;
import com.bluenet.web.application.command.assessment_session.AssessmentSessionCommands;
import com.bluenet.web.application.result.assessment.AssessmentSessionResult;
import com.bluenet.web.application.service.AssessmentSessionAppService;
import com.bluenet.web.infrastructure.security.principal.WithSecurityPrincipal;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.bluenet.web.testconfig.TestSecurityConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
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
class AssessmentSessionControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AssessmentSessionAppService assessmentSessionAppService;

    @MockitoBean
    private AssessmentSessionRequestConverter requestConverter;

    @MockitoBean
    private AssessmentSessionResponseConverter responseConverter;

    @AfterEach
    void tearDown() {
        UserCTX.clear();
    }

    @Test
    @DisplayName("getSession: 已登录用户应返回考核会话")
    @WithSecurityPrincipal(userId = 1L, roleType = "CANDIDATE", roleId = 4L)
    void getSession_authenticated_shouldReturnSession() throws Exception {
        AssessmentSessionCommands.GetOrCreateSessionCommand command = new AssessmentSessionCommands.GetOrCreateSessionCommand(
                1L, 1L);
        AssessmentSessionResult result = new AssessmentSessionResult(
                1L,
                1L,
                1L,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(2));
        AssessmentSessionDTO dto = AssessmentSessionDTO.builder()
                .id(1L)
                .assessmentTimeId(1L)
                .build();
        when(requestConverter.toCommand(1L, 1L)).thenReturn(command);
        when(assessmentSessionAppService.getOrCreateSession(command)).thenReturn(result);
        when(responseConverter.toDTO(result)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/assessment-sessions/{assessmentTimeId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.assessmentTimeId").value(1));
    }

    @Test
    @DisplayName("getSession: 未登录应返回 401")
    void getSession_anonymous_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/assessment-sessions/{assessmentTimeId}", 1L))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }
}
