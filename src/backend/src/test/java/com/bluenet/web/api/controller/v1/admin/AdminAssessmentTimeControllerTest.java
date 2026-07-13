package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.converter.assessment_time.AssessmentTimeRequestConverter;
import com.bluenet.web.api.converter.assessment_time.AssessmentTimeResponseConverter;
import com.bluenet.web.api.dto.assessment_time.AssessmentTimeDTO;
import com.bluenet.web.api.dto.assessment_time.CreateAssessmentTimeRequestDTO;
import com.bluenet.web.api.dto.assessment_time.UpdateAssessmentTimeRequestDTO;
import com.bluenet.web.application.command.assessment_time.AssessmentTimeCommands;
import com.bluenet.web.application.result.assessment.AssessmentTimeResult;
import com.bluenet.web.application.service.AssessmentTimeAppService;
import com.bluenet.web.domain.model.enumerate.Direction;
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
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@DisplayName("AdminAssessmentTimeController 集成测试")
class AdminAssessmentTimeControllerTest extends BaseIntegrationTest {

    private static final long ADMIN_USER_ID = 9999L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AssessmentTimeAppService assessmentTimeAppService;

    @MockitoBean
    private AssessmentTimeRequestConverter assessmentTimeRequestConverter;

    @MockitoBean
    private AssessmentTimeResponseConverter responseConverter;

    @AfterEach
    void tearDown() {
        UserCTX.clear();
    }

    @Test
    @DisplayName("createAssessmentTime: 管理员创建考核时间应返回 DTO")
    @WithSecurityPrincipal(userId = ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-time:create" })
    void createAssessmentTime_shouldReturnDto() throws Exception {
        LocalDateTime startTime = LocalDateTime.of(2026, 7, 1, 9, 0);
        LocalDateTime endTime = startTime.plusDays(7);
        CreateAssessmentTimeRequestDTO request = CreateAssessmentTimeRequestDTO.builder()
                .direction(Direction.COMPUTER_VISION)
                .epoch(1)
                .grade(2024)
                .startTime(startTime)
                .endTime(endTime)
                .timeLimit(false)
                .build();
        AssessmentTimeCommands.CreateAssessmentTimeCommand command = new AssessmentTimeCommands.CreateAssessmentTimeCommand(
                Direction.COMPUTER_VISION, 1, 2024, startTime, endTime, false, null, null);
        AssessmentTimeResult result = new AssessmentTimeResult(
                1L, Direction.COMPUTER_VISION, 1, 2024, startTime, endTime, false, null, false, 0, 0, false);
        AssessmentTimeDTO dto = AssessmentTimeDTO.builder()
                .id(1L)
                .direction(Direction.COMPUTER_VISION)
                .build();

        when(assessmentTimeRequestConverter.toCommand(request)).thenReturn(command);
        when(assessmentTimeAppService.createAssessmentTime(ADMIN_USER_ID, command)).thenReturn(result);
        when(responseConverter.toDTO(result)).thenReturn(dto);

        mockMvc.perform(
                post("/api/v1/admin/assessment-times")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("updateAssessmentTime: 管理员更新考核时间应返回 DTO")
    @WithSecurityPrincipal(userId = ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-time:update" })
    void updateAssessmentTime_shouldReturnDto() throws Exception {
        UpdateAssessmentTimeRequestDTO request = UpdateAssessmentTimeRequestDTO.builder()
                .allowTeam(true)
                .build();
        AssessmentTimeCommands.UpdateAssessmentTimeCommand command = new AssessmentTimeCommands.UpdateAssessmentTimeCommand(
                1L, null, null, null, null, null, null, null, true);
        AssessmentTimeResult result = new AssessmentTimeResult(
                1L, Direction.COMPUTER_VISION, 1, 2024,
                LocalDateTime.now(), LocalDateTime.now().plusDays(1),
                false, null, true, 0, 0, false);
        AssessmentTimeDTO dto = AssessmentTimeDTO.builder()
                .id(1L)
                .allowTeam(true)
                .build();

        when(assessmentTimeRequestConverter.toCommand(1L, request)).thenReturn(command);
        when(assessmentTimeAppService.updateAssessmentTime(ADMIN_USER_ID, command)).thenReturn(result);
        when(responseConverter.toDTO(result)).thenReturn(dto);

        mockMvc.perform(
                put("/api/v1/admin/assessment-times/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.allowTeam").value(true));
    }

    @Test
    @DisplayName("deleteAssessmentTime: 管理员删除考核时间应返回 200")
    @WithSecurityPrincipal(userId = ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-time:delete" })
    void deleteAssessmentTime_shouldReturnOk() throws Exception {
        doNothing().when(assessmentTimeAppService).deleteAssessmentTime(ADMIN_USER_ID, 1L);

        mockMvc.perform(delete("/api/v1/admin/assessment-times/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("listAssessmentTimes: 管理员分页查询考核时间应返回分页")
    @WithSecurityPrincipal(userId = ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-time:list" })
    void listAssessmentTimes_shouldReturnPagedList() throws Exception {
        AssessmentTimeResult result = new AssessmentTimeResult(
                1L, Direction.COMPUTER_VISION, 1, 2024,
                LocalDateTime.now(), LocalDateTime.now().plusDays(1),
                false, null, false, 0, 0, false);
        AssessmentTimeDTO dto = AssessmentTimeDTO.builder()
                .id(1L)
                .direction(Direction.COMPUTER_VISION)
                .build();

        when(assessmentTimeAppService.listAssessmentTimes(ADMIN_USER_ID, 0, 5))
                .thenReturn(new PageImpl<>(List.of(result)));
        when(responseConverter.toDTO(any(AssessmentTimeResult.class))).thenReturn(dto);

        mockMvc.perform(
                get("/api/v1/admin/assessment-times")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(1));
    }
}
