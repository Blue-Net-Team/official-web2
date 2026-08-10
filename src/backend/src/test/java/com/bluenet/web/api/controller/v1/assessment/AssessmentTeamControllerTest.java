package com.bluenet.web.api.controller.v1.assessment;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.converter.assessment_team.AssessmentTeamResponseConverter;
import com.bluenet.web.api.dto.assessment_team.*;
import com.bluenet.web.application.result.team.TeamPreviewResult;
import com.bluenet.web.application.result.team.TeamResult;
import com.bluenet.web.application.service.AssessmentTeamAppService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.model.entity.AssessmentTeam;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@DisplayName("AssessmentTeamController 集成测试")
class AssessmentTeamControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AssessmentTeamAppService assessmentTeamAppService;

    @MockitoBean
    private AssessmentTeamResponseConverter responseConverter;

    @AfterEach
    void tearDown() {
        UserCTX.clear();
    }

    @Test
    @DisplayName("createTeam: 已登录用户创建队伍应返回队伍 DTO")
    @WithSecurityPrincipal(userId = 100L)
    void createTeam_shouldReturnTeamDto() throws Exception {
        CreateTeamRequestDTO request = CreateTeamRequestDTO.builder()
                .assessmentTimeId(1L)
                .name("先锋队")
                .build();
        TeamResult result = new TeamResult(
                1L, 1L, 100L, "先锋队", "CODE123", AssessmentTeam.TeamStatus.ACTIVE,
                LocalDateTime.now(), null);
        AssessmentTeamDTO dto = AssessmentTeamDTO.builder()
                .id(1L)
                .assessmentTimeId(1L)
                .leaderId(100L)
                .name("先锋队")
                .build();

        when(assessmentTeamAppService.createTeam(100L, 1L, "先锋队")).thenReturn(result);
        when(responseConverter.toDTO(result)).thenReturn(dto);

        mockMvc.perform(
                post("/api/v1/assessment-teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("先锋队"));
    }

    @Test
    @DisplayName("previewTeam: 通过邀请码预览队伍应返回预览 DTO")
    @WithSecurityPrincipal(userId = 100L)
    void previewTeam_shouldReturnPreviewDto() throws Exception {
        PreviewTeamRequestDTO request = PreviewTeamRequestDTO.builder()
                .inviteCode("CODE123")
                .build();
        TeamPreviewResult result = new TeamPreviewResult(
                1L, 1L, "队长A", "先锋队", AssessmentTeam.TeamStatus.ACTIVE,
                LocalDateTime.now(), 3, null);

        when(assessmentTeamAppService.previewTeam("CODE123")).thenReturn(result);

        mockMvc.perform(
                post("/api/v1/assessment-teams/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.leaderUsername").value("队长A"))
                .andExpect(jsonPath("$.data.name").value("先锋队"));
    }

    @Test
    @DisplayName("joinTeam: 通过邀请码加入队伍应返回队伍 DTO")
    @WithSecurityPrincipal(userId = 100L)
    void joinTeam_shouldReturnTeamDto() throws Exception {
        JoinTeamRequestDTO request = JoinTeamRequestDTO.builder()
                .inviteCode("CODE123")
                .build();
        TeamResult result = new TeamResult(
                1L, 1L, 200L, "先锋队", "CODE123", AssessmentTeam.TeamStatus.ACTIVE,
                LocalDateTime.now(), null);
        AssessmentTeamDTO dto = AssessmentTeamDTO.builder()
                .id(1L)
                .name("先锋队")
                .build();

        when(assessmentTeamAppService.joinTeam(100L, "CODE123")).thenReturn(result);
        when(responseConverter.toDTO(result)).thenReturn(dto);

        mockMvc.perform(
                post("/api/v1/assessment-teams/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("先锋队"));
    }

    @Test
    @DisplayName("getMyTeam: 未加入队伍应返回 404")
    @WithSecurityPrincipal(userId = 100L)
    void getMyTeam_whenNotJoined_shouldReturn404() throws Exception {
        when(assessmentTeamAppService.getMyTeam(100L, 1L)).thenReturn(null);

        mockMvc.perform(
                get("/api/v1/assessment-teams/my-team")
                        .param("assessmentTimeId", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("leaveTeam: 成员离开队伍应返回 200")
    @WithSecurityPrincipal(userId = 100L)
    void leaveTeam_shouldReturnOk() throws Exception {
        LeaveTeamRequestDTO request = LeaveTeamRequestDTO.builder().teamId(1L).build();

        mockMvc.perform(
                post("/api/v1/assessment-teams/leave")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("transferLeader: 队长转让队长应返回队伍 DTO")
    @WithSecurityPrincipal(userId = 100L)
    void transferLeader_shouldReturnTeamDto() throws Exception {
        TransferLeaderRequestDTO request = TransferLeaderRequestDTO.builder()
                .teamId(1L)
                .newLeaderId(200L)
                .build();
        TeamResult result = new TeamResult(
                1L, 1L, 200L, "先锋队", "CODE123", AssessmentTeam.TeamStatus.ACTIVE,
                LocalDateTime.now(), null);
        AssessmentTeamDTO dto = AssessmentTeamDTO.builder()
                .id(1L)
                .leaderId(200L)
                .build();

        when(assessmentTeamAppService.transferLeader(100L, 1L, 200L)).thenReturn(result);
        when(responseConverter.toDTO(result)).thenReturn(dto);

        mockMvc.perform(
                post("/api/v1/assessment-teams/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.leaderId").value(200));
    }

    @Test
    @DisplayName("disbandTeam: 非队长解散队伍应返回 403")
    @WithSecurityPrincipal(userId = 100L)
    void disbandTeam_whenForbidden_shouldReturn403() throws Exception {
        doThrow(new Forbidden("只有队长可以解散队伍"))
                .when(assessmentTeamAppService)
                .disbandTeam(100L, 1L);

        mockMvc.perform(delete("/api/v1/assessment-teams/{id}", 1L))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @DisplayName("createTeam: 该考核不允许组队应返回 400")
    @WithSecurityPrincipal(userId = 100L)
    void createTeam_whenTeamNotAllowed_shouldReturn400() throws Exception {
        CreateTeamRequestDTO request = CreateTeamRequestDTO.builder()
                .assessmentTimeId(1L)
                .name("先锋队")
                .build();

        when(assessmentTeamAppService.createTeam(any(), any(), any()))
                .thenThrow(new BadRequest("该考核不允许组队"));

        mockMvc.perform(
                post("/api/v1/assessment-teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }
}
