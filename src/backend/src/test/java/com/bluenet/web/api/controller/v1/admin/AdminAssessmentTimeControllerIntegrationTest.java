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
import com.bluenet.web.domain.exception.DataConflict;
import com.bluenet.web.domain.exception.DataNotFound;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
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
class AdminAssessmentTimeControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AssessmentTimeAppService assessmentTimeAppService;

    @MockitoBean
    private AssessmentTimeResponseConverter responseConverter;

    @Autowired
    private AssessmentTimeRequestConverter requestConverter;

    @AfterEach
    void tearDown() {
        UserCTX.clear();
    }

    private static final long SUPER_ADMIN_USER_ID = 9999L;

    private AssessmentTimeResult timeResult() {
        return new AssessmentTimeResult(
                1L,
                Direction.COMPUTER_VISION,
                1,
                2025,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(1),
                false,
                null,
                false,
                5,
                0,
                false);
    }

    private AssessmentTimeDTO timeDTO() {
        return AssessmentTimeDTO.builder()
                .id(1L)
                .direction(Direction.COMPUTER_VISION)
                .epoch(1)
                .grade(2025)
                .timeLimit(false)
                .allowTeam(false)
                .totalQuestions(5)
                .completedQuestions(0)
                .eliminated(false)
                .build();
    }

    @Test
    @DisplayName("listAssessmentTimes: 普通成员访问应返回 403")
    @WithSecurityPrincipal(roleType = "MEMBER", roleId = 3L, permissions = {})
    void listAssessmentTimes_asMember_shouldReturn403() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/admin/assessment-times"))
                .andExpect(status().isForbidden())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(403);
    }

    @Test
    @DisplayName("listAssessmentTimes: 超级管理员应返回分页考核时间列表")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-time:list" })
    void listAssessmentTimes_asSuperAdmin_shouldReturnPagedTimes() throws Exception {
        when(assessmentTimeAppService.listAssessmentTimes(SUPER_ADMIN_USER_ID, 0, 5))
                .thenReturn(new PageImpl<>(List.of(timeResult())));
        when(responseConverter.toDTO(any(AssessmentTimeResult.class))).thenReturn(timeDTO());

        MvcResult result = mockMvc.perform(
                get("/api/v1/admin/assessment-times")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("createAssessmentTime: 超级管理员应成功创建考核时间")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-time:create" })
    void createAssessmentTime_asSuperAdmin_shouldReturnCreatedTime() throws Exception {
        when(
                assessmentTimeAppService.createAssessmentTime(
                        any(Long.class),
                        any(AssessmentTimeCommands.CreateAssessmentTimeCommand.class))).thenReturn(timeResult());
        when(responseConverter.toDTO(any(AssessmentTimeResult.class))).thenReturn(timeDTO());

        CreateAssessmentTimeRequestDTO request = CreateAssessmentTimeRequestDTO.builder()
                .direction(Direction.COMPUTER_VISION)
                .epoch(1)
                .grade(2025)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusDays(1))
                .timeLimit(false)
                .allowTeam(false)
                .build();

        MvcResult result = mockMvc.perform(
                post("/api/v1/admin/assessment-times")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("createAssessmentTime: 缺少届次应返回 400")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-time:create" })
    void createAssessmentTime_withMissingEpoch_shouldReturn400() throws Exception {
        CreateAssessmentTimeRequestDTO request = CreateAssessmentTimeRequestDTO.builder()
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusDays(1))
                .timeLimit(false)
                .build();

        MvcResult result = mockMvc.perform(
                post("/api/v1/admin/assessment-times")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("updateAssessmentTime: 超级管理员应成功更新考核时间")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-time:update" })
    void updateAssessmentTime_asSuperAdmin_shouldReturnUpdatedTime() throws Exception {
        when(
                assessmentTimeAppService.updateAssessmentTime(
                        any(Long.class),
                        any(AssessmentTimeCommands.UpdateAssessmentTimeCommand.class))).thenReturn(timeResult());
        when(responseConverter.toDTO(any(AssessmentTimeResult.class))).thenReturn(timeDTO());

        UpdateAssessmentTimeRequestDTO request = UpdateAssessmentTimeRequestDTO.builder()
                .grade(2026)
                .build();

        MvcResult result = mockMvc.perform(
                put("/api/v1/admin/assessment-times/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("updateAssessmentTime: 考核时间不存在时应返回 404")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-time:update" })
    void updateAssessmentTime_whenNotFound_shouldReturn404() throws Exception {
        when(
                assessmentTimeAppService.updateAssessmentTime(
                        any(Long.class),
                        any(AssessmentTimeCommands.UpdateAssessmentTimeCommand.class)))
                                .thenThrow(new DataNotFound("考核时间不存在"));

        UpdateAssessmentTimeRequestDTO request = UpdateAssessmentTimeRequestDTO.builder()
                .grade(2026)
                .build();

        MvcResult result = mockMvc.perform(
                put("/api/v1/admin/assessment-times/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("deleteAssessmentTime: 超级管理员应成功删除考核时间")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-time:delete" })
    void deleteAssessmentTime_asSuperAdmin_shouldReturnOk() throws Exception {
        doNothing().when(assessmentTimeAppService).deleteAssessmentTime(SUPER_ADMIN_USER_ID, 1L);

        MvcResult result = mockMvc.perform(delete("/api/v1/admin/assessment-times/{id}", 1L))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("deleteAssessmentTime: 存在关联题目时应返回 409")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-time:delete" })
    void deleteAssessmentTime_whenHasQuestions_shouldReturn409() throws Exception {
        doThrow(new DataConflict("存在关联的考核题目，需先删除相关题目"))
                .when(assessmentTimeAppService)
                .deleteAssessmentTime(SUPER_ADMIN_USER_ID, 1L);

        MvcResult result = mockMvc.perform(delete("/api/v1/admin/assessment-times/{id}", 1L))
                .andExpect(status().isConflict())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(409);
    }
}
