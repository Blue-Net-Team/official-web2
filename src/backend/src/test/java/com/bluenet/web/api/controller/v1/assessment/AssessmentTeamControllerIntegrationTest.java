package com.bluenet.web.api.controller.v1.assessment;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.converter.assessment_team.AssessmentTeamResponseConverter;
import com.bluenet.web.api.dto.assessment_team.AssessmentTeamDTO;
import com.bluenet.web.api.dto.assessment_team.CreateTeamRequestDTO;
import com.bluenet.web.api.dto.assessment_team.JoinTeamRequestDTO;
import com.bluenet.web.api.dto.assessment_team.LeaveTeamRequestDTO;
import com.bluenet.web.api.dto.assessment_team.PreviewTeamRequestDTO;
import com.bluenet.web.api.dto.assessment_team.TransferLeaderRequestDTO;
import com.bluenet.web.application.result.team.TeamPreviewResult;
import com.bluenet.web.application.result.team.TeamResult;
import com.bluenet.web.application.service.AssessmentTeamAppService;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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
class AssessmentTeamControllerIntegrationTest extends BaseIntegrationTest {

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
    @DisplayName("createTeam: 已登录用户创建队伍应成功")
    @WithSecurityPrincipal(userId = 1L, roleType = "MEMBER", roleId = 3L)
    void createTeam_authenticated_shouldReturnCreatedTeam() throws Exception {
        TeamResult result = new TeamResult(
                1L,
                1L,
                1L,
                "Blue Team",
                "INVITE_CODE",
                AssessmentTeam.TeamStatus.ACTIVE,
                LocalDateTime.now(),
                List.of());
        AssessmentTeamDTO dto = AssessmentTeamDTO.builder()
                .id(1L)
                .assessmentTimeId(1L)
                .leaderId(1L)
                .name("Blue Team")
                .inviteCode("INVITE_CODE")
                .status(AssessmentTeam.TeamStatus.ACTIVE)
                .build();
        when(assessmentTeamAppService.createTeam(anyLong(), anyLong(), anyString())).thenReturn(result);
        when(responseConverter.toDTO(any(TeamResult.class))).thenReturn(dto);

        CreateTeamRequestDTO request = CreateTeamRequestDTO.builder()
                .assessmentTimeId(1L)
                .name("Blue Team")
                .build();

        mockMvc.perform(
                post("/api/v1/assessment-teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Blue Team"));
    }

    @Test
    @DisplayName("createTeam: 未登录用户应返回 401")
    void createTeam_anonymous_shouldReturnUnauthorized() throws Exception {
        CreateTeamRequestDTO request = CreateTeamRequestDTO.builder()
                .assessmentTimeId(1L)
                .name("Blue Team")
                .build();

        mockMvc.perform(
                post("/api/v1/assessment-teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("createTeam: 队伍名称为空应返回 400")
    @WithSecurityPrincipal(userId = 1L, roleType = "MEMBER", roleId = 3L)
    void createTeam_blankName_shouldReturnBadRequest() throws Exception {
        CreateTeamRequestDTO request = CreateTeamRequestDTO.builder()
                .assessmentTimeId(1L)
                .name(" ")
                .build();

        mockMvc.perform(
                post("/api/v1/assessment-teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("previewTeam: 已登录用户预览队伍应成功")
    @WithSecurityPrincipal(userId = 2L, roleType = "MEMBER", roleId = 3L)
    void previewTeam_authenticated_shouldReturnPreview() throws Exception {
        TeamPreviewResult result = new TeamPreviewResult(
                1L,
                1L,
                "leader",
                "Blue Team",
                AssessmentTeam.TeamStatus.ACTIVE,
                LocalDateTime.now(),
                1,
                List.of("leader"));
        when(assessmentTeamAppService.previewTeam("INVITE_CODE")).thenReturn(result);

        PreviewTeamRequestDTO request = PreviewTeamRequestDTO.builder()
                .inviteCode("INVITE_CODE")
                .build();

        mockMvc.perform(
                post("/api/v1/assessment-teams/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.leaderUsername").value("leader"))
                .andExpect(jsonPath("$.data.memberCount").value(1));
    }

    @Test
    @DisplayName("previewTeam: 未登录用户应返回 401")
    void previewTeam_anonymous_shouldReturnUnauthorized() throws Exception {
        PreviewTeamRequestDTO request = PreviewTeamRequestDTO.builder()
                .inviteCode("INVITE_CODE")
                .build();

        mockMvc.perform(
                post("/api/v1/assessment-teams/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("previewTeam: 邀请码为空应返回 400")
    @WithSecurityPrincipal(userId = 2L, roleType = "MEMBER", roleId = 3L)
    void previewTeam_blankInviteCode_shouldReturnBadRequest() throws Exception {
        PreviewTeamRequestDTO request = PreviewTeamRequestDTO.builder()
                .inviteCode(" ")
                .build();

        mockMvc.perform(
                post("/api/v1/assessment-teams/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("joinTeam: 已登录用户加入队伍应成功")
    @WithSecurityPrincipal(userId = 2L, roleType = "MEMBER", roleId = 3L)
    void joinTeam_authenticated_shouldReturnTeam() throws Exception {
        TeamResult result = new TeamResult(
                1L,
                1L,
                1L,
                "Blue Team",
                "INVITE_CODE",
                AssessmentTeam.TeamStatus.ACTIVE,
                LocalDateTime.now(),
                List.of());
        AssessmentTeamDTO dto = AssessmentTeamDTO.builder()
                .id(1L)
                .name("Blue Team")
                .status(AssessmentTeam.TeamStatus.ACTIVE)
                .build();
        when(assessmentTeamAppService.joinTeam(anyLong(), anyString())).thenReturn(result);
        when(responseConverter.toDTO(any(TeamResult.class))).thenReturn(dto);

        JoinTeamRequestDTO request = JoinTeamRequestDTO.builder()
                .inviteCode("INVITE_CODE")
                .build();

        mockMvc.perform(
                post("/api/v1/assessment-teams/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("joinTeam: 未登录用户应返回 401")
    void joinTeam_anonymous_shouldReturnUnauthorized() throws Exception {
        JoinTeamRequestDTO request = JoinTeamRequestDTO.builder()
                .inviteCode("INVITE_CODE")
                .build();

        mockMvc.perform(
                post("/api/v1/assessment-teams/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("getMyTeam: 已登录用户查询队伍应成功")
    @WithSecurityPrincipal(userId = 1L, roleType = "MEMBER", roleId = 3L)
    void getMyTeam_authenticated_shouldReturnTeam() throws Exception {
        TeamResult result = new TeamResult(
                1L,
                1L,
                1L,
                "Blue Team",
                "INVITE_CODE",
                AssessmentTeam.TeamStatus.ACTIVE,
                LocalDateTime.now(),
                List.of());
        AssessmentTeamDTO dto = AssessmentTeamDTO.builder()
                .id(1L)
                .name("Blue Team")
                .status(AssessmentTeam.TeamStatus.ACTIVE)
                .build();
        when(assessmentTeamAppService.getMyTeam(1L, 1L)).thenReturn(result);
        when(responseConverter.toDTO(any(TeamResult.class))).thenReturn(dto);

        mockMvc.perform(get("/api/v1/assessment-teams/my-team").param("assessmentTimeId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("getMyTeam: 未加入队伍应返回 404")
    @WithSecurityPrincipal(userId = 1L, roleType = "MEMBER", roleId = 3L)
    void getMyTeam_notJoined_shouldReturn404() throws Exception {
        when(assessmentTeamAppService.getMyTeam(1L, 1L)).thenReturn(null);

        mockMvc.perform(get("/api/v1/assessment-teams/my-team").param("assessmentTimeId", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.msg").value("未加入队伍"));
    }

    @Test
    @DisplayName("getMyTeam: 未登录用户应返回 401")
    void getMyTeam_anonymous_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/assessment-teams/my-team").param("assessmentTimeId", "1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("leaveTeam: 已登录用户离开队伍应成功")
    @WithSecurityPrincipal(userId = 2L, roleType = "MEMBER", roleId = 3L)
    void leaveTeam_authenticated_shouldReturnOk() throws Exception {
        LeaveTeamRequestDTO request = LeaveTeamRequestDTO.builder()
                .teamId(1L)
                .build();

        mockMvc.perform(
                post("/api/v1/assessment-teams/leave")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("leaveTeam: 未登录用户应返回 401")
    void leaveTeam_anonymous_shouldReturnUnauthorized() throws Exception {
        LeaveTeamRequestDTO request = LeaveTeamRequestDTO.builder()
                .teamId(1L)
                .build();

        mockMvc.perform(
                post("/api/v1/assessment-teams/leave")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("transferLeader: 已登录用户转让队长应成功")
    @WithSecurityPrincipal(userId = 1L, roleType = "MEMBER", roleId = 3L)
    void transferLeader_authenticated_shouldReturnTeam() throws Exception {
        TeamResult result = new TeamResult(
                1L,
                1L,
                2L,
                "Blue Team",
                "INVITE_CODE",
                AssessmentTeam.TeamStatus.ACTIVE,
                LocalDateTime.now(),
                List.of());
        AssessmentTeamDTO dto = AssessmentTeamDTO.builder()
                .id(1L)
                .leaderId(2L)
                .name("Blue Team")
                .status(AssessmentTeam.TeamStatus.ACTIVE)
                .build();
        when(assessmentTeamAppService.transferLeader(anyLong(), anyLong(), anyLong())).thenReturn(result);
        when(responseConverter.toDTO(any(TeamResult.class))).thenReturn(dto);

        TransferLeaderRequestDTO request = TransferLeaderRequestDTO.builder()
                .teamId(1L)
                .newLeaderId(2L)
                .build();

        mockMvc.perform(
                post("/api/v1/assessment-teams/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.leaderId").value(2));
    }

    @Test
    @DisplayName("transferLeader: 未登录用户应返回 401")
    void transferLeader_anonymous_shouldReturnUnauthorized() throws Exception {
        TransferLeaderRequestDTO request = TransferLeaderRequestDTO.builder()
                .teamId(1L)
                .newLeaderId(2L)
                .build();

        mockMvc.perform(
                post("/api/v1/assessment-teams/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("disbandTeam: 已登录用户解散队伍应成功")
    @WithSecurityPrincipal(userId = 1L, roleType = "MEMBER", roleId = 3L)
    void disbandTeam_authenticated_shouldReturnOk() throws Exception {
        mockMvc.perform(delete("/api/v1/assessment-teams/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("disbandTeam: 未登录用户应返回 401")
    void disbandTeam_anonymous_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(delete("/api/v1/assessment-teams/{id}", 1L))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }
}
