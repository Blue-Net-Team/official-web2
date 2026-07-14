package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.converter.assessment_judgement.AssessmentJudgementRequestConverter;
import com.bluenet.web.api.converter.assessment_judgement.AssessmentJudgementResponseConverter;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentCandidateScoreboardDTO;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentDecisionDTO;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentDecisionRequestDTO;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentDecisionWorkspaceDTO;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentJudgementDTO;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentQuestionScoreboardDTO;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentQuestionSubmissionDTO;
import com.bluenet.web.api.dto.assessment_judgement.FinalizeScoreRequestDTO;
import com.bluenet.web.application.command.assessment_judgement.AssessmentJudgementCommands;
import com.bluenet.web.application.result.assessment.AssessmentCandidateScoreboard;
import com.bluenet.web.application.result.assessment.AssessmentDecisionResult;
import com.bluenet.web.application.result.assessment.AssessmentDecisionWorkspace;
import com.bluenet.web.application.result.assessment.AssessmentJudgementResult;
import com.bluenet.web.application.result.assessment.AssessmentQuestionScoreboard;
import com.bluenet.web.application.service.AssessmentJudgementAppService;
import com.bluenet.web.domain.model.enumerate.JudgementSource;
import com.bluenet.web.domain.model.enumerate.JudgementStatus;
import com.bluenet.web.domain.model.enumerate.ObjectiveResultCode;
import com.bluenet.web.domain.model.enumerate.QuestionType;
import com.bluenet.web.domain.model.enumerate.ReviewerType;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.readmodel.AssessmentQuestionSubmissionReadModel;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@DisplayName("AdminAssessmentJudgementController 集成测试")
class AdminAssessmentJudgementControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AssessmentJudgementAppService assessmentJudgementAppService;

    @MockitoBean
    private AssessmentJudgementResponseConverter responseConverter;

    @Autowired
    private AssessmentJudgementRequestConverter requestConverter;

    @AfterEach
    void tearDown() {
        UserCTX.clear();
    }

    private static final long SUPER_ADMIN_USER_ID = 9999L;

    private AssessmentJudgementResult judgementResult() {
        return new AssessmentJudgementResult(
                1L,
                1L,
                1L,
                1L,
                1L,
                new BigDecimal("80.00"),
                new BigDecimal("100.00"),
                JudgementStatus.JUDGED,
                ObjectiveResultCode.AC,
                JudgementSource.MANUAL,
                SUPER_ADMIN_USER_ID,
                ReviewerType.SUPER_ADMIN,
                LocalDateTime.now());
    }

    private AssessmentJudgementDTO judgementDTO() {
        return AssessmentJudgementDTO.builder()
                .id(1L)
                .answerId(1L)
                .questionId(1L)
                .assessmentTimeId(1L)
                .userId(1L)
                .score(new BigDecimal("80.00"))
                .maxScore(new BigDecimal("100.00"))
                .status(JudgementStatus.JUDGED)
                .resultCode(ObjectiveResultCode.AC)
                .source(JudgementSource.MANUAL)
                .reviewerId(SUPER_ADMIN_USER_ID)
                .reviewerType(ReviewerType.SUPER_ADMIN)
                .judgedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("getLatestByAnswerId: 超级管理员应返回答案最新评判")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-judgement:list" })
    void getLatestByAnswerId_asSuperAdmin_shouldReturnLatestJudgement() throws Exception {
        when(assessmentJudgementAppService.getLatestByAnswerId(1L)).thenReturn(judgementResult());
        when(responseConverter.toDTO(any(AssessmentJudgementResult.class))).thenReturn(judgementDTO());

        MvcResult result = mockMvc.perform(get("/api/v1/admin/assessment-judgements/answers/{answerId}/latest", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("getLatestByAnswerId: 答案不存在时应返回 404")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-judgement:list" })
    void getLatestByAnswerId_whenNotFound_shouldReturn404() throws Exception {
        when(assessmentJudgementAppService.getLatestByAnswerId(1L)).thenThrow(new DataNotFound("答案不存在"));

        MvcResult result = mockMvc.perform(get("/api/v1/admin/assessment-judgements/answers/{answerId}/latest", 1L))
                .andExpect(status().isNotFound())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("listByQuestionId: 普通成员访问应返回 403")
    @WithSecurityPrincipal(roleType = "MEMBER", roleId = 3L, permissions = {})
    void listByQuestionId_asMember_shouldReturn403() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/admin/assessment-judgements").param("questionId", "1"))
                .andExpect(status().isForbidden())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(403);
    }

    @Test
    @DisplayName("listByQuestionId: 超级管理员应返回题目评判列表")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-judgement:query" })
    void listByQuestionId_asSuperAdmin_shouldReturnJudgements() throws Exception {
        when(assessmentJudgementAppService.listByQuestionId(1L)).thenReturn(List.of(judgementResult()));
        when(responseConverter.toDTO(any(AssessmentJudgementResult.class))).thenReturn(judgementDTO());

        MvcResult result = mockMvc.perform(get("/api/v1/admin/assessment-judgements").param("questionId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("decideAssessment: 超级管理员应设置考核通过决策")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-decision:set" })
    void decideAssessment_asSuperAdmin_shouldReturnDecision() throws Exception {
        AssessmentDecisionResult decisionResult = new AssessmentDecisionResult(
                1L, 1L, 1L, true, SUPER_ADMIN_USER_ID, "通过", LocalDateTime.now());
        AssessmentDecisionDTO decisionDTO = AssessmentDecisionDTO.builder()
                .id(1L)
                .userId(1L)
                .assessmentTimeId(1L)
                .passed(true)
                .decidedBy(SUPER_ADMIN_USER_ID)
                .decisionComment("通过")
                .decidedAt(LocalDateTime.now())
                .build();
        when(
                assessmentJudgementAppService
                        .decideAssessment(any(AssessmentJudgementCommands.DecideAssessmentCommand.class)))
                                .thenReturn(decisionResult);
        when(responseConverter.toDTO(any(AssessmentDecisionResult.class))).thenReturn(decisionDTO);

        AssessmentDecisionRequestDTO request = AssessmentDecisionRequestDTO.builder()
                .userId(1L)
                .assessmentTimeId(1L)
                .passed(true)
                .decisionComment("通过")
                .build();

        MvcResult result = mockMvc.perform(
                post("/api/v1/admin/assessment-judgements/decisions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("decideAssessment: 请求参数校验失败应返回 400")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-decision:set" })
    void decideAssessment_withInvalidRequest_shouldReturn400() throws Exception {
        AssessmentDecisionRequestDTO request = AssessmentDecisionRequestDTO.builder()
                .assessmentTimeId(1L)
                .passed(true)
                .build();

        MvcResult result = mockMvc.perform(
                post("/api/v1/admin/assessment-judgements/decisions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("listQuestionScoreboard: 超级管理员应返回题目评分汇总")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-scoreboard:question" })
    void listQuestionScoreboard_asSuperAdmin_shouldReturnScoreboard() throws Exception {
        AssessmentQuestionScoreboard scoreboard = AssessmentQuestionScoreboard.builder()
                .questionId(1L)
                .assessmentTimeId(1L)
                .questionNo(1)
                .questionType(QuestionType.ALGORITHM)
                .title("题目一")
                .build();
        AssessmentQuestionScoreboardDTO dto = AssessmentQuestionScoreboardDTO.builder()
                .questionId(1L)
                .assessmentTimeId(1L)
                .questionNo(1)
                .questionType(QuestionType.ALGORITHM)
                .title("题目一")
                .build();
        when(assessmentJudgementAppService.listQuestionScoreboard(1L, QuestionType.ALGORITHM, null))
                .thenReturn(List.of(scoreboard));
        when(responseConverter.convertScoreboardToDTO(any(AssessmentQuestionScoreboard.class))).thenReturn(dto);

        MvcResult result = mockMvc.perform(
                get("/api/v1/admin/assessment-judgements/scoreboard/questions")
                        .param("assessmentTimeId", "1")
                        .param("questionType", "ALGORITHM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("listQuestionSubmissions: 超级管理员应返回题目提交评分列表")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-scoreboard:submission" })
    void listQuestionSubmissions_asSuperAdmin_shouldReturnSubmissions() throws Exception {
        AssessmentQuestionSubmissionReadModel submission = AssessmentQuestionSubmissionReadModel.builder()
                .answerId(1L)
                .questionId(1L)
                .assessmentTimeId(1L)
                .build();
        AssessmentQuestionSubmissionDTO dto = AssessmentQuestionSubmissionDTO.builder()
                .answerId(1L)
                .questionId(1L)
                .assessmentTimeId(1L)
                .build();
        when(assessmentJudgementAppService.listQuestionSubmissions(1L, null, null))
                .thenReturn(List.of(submission));
        when(responseConverter.convertSubmissionToDTO(any(AssessmentQuestionSubmissionReadModel.class)))
                .thenReturn(dto);

        MvcResult result = mockMvc.perform(
                get("/api/v1/admin/assessment-judgements/scoreboard/questions/{questionId}/submissions", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("listCandidateScoreboard: 超级管理员应返回考生评分矩阵")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-scoreboard:candidate" })
    void listCandidateScoreboard_asSuperAdmin_shouldReturnCandidateScoreboard() throws Exception {
        AssessmentCandidateScoreboard scoreboard = AssessmentCandidateScoreboard.builder()
                .candidateUserId(1L)
                .build();
        AssessmentCandidateScoreboardDTO dto = AssessmentCandidateScoreboardDTO.builder()
                .candidateUserId(1L)
                .build();
        when(assessmentJudgementAppService.listCandidateScoreboard(1L, null)).thenReturn(List.of(scoreboard));
        when(responseConverter.convertCandidateScoreboardToDTO(any(AssessmentCandidateScoreboard.class)))
                .thenReturn(dto);

        MvcResult result = mockMvc.perform(
                get("/api/v1/admin/assessment-judgements/scoreboard/candidates").param("assessmentTimeId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("getDecisionWorkspace: 超级管理员应返回录用决策工作台")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-decision:query" })
    void getDecisionWorkspace_asSuperAdmin_shouldReturnWorkspace() throws Exception {
        AssessmentDecisionWorkspace workspace = AssessmentDecisionWorkspace.builder().build();
        AssessmentDecisionWorkspaceDTO dto = AssessmentDecisionWorkspaceDTO.builder().build();
        when(assessmentJudgementAppService.getDecisionWorkspace(1L, null, null)).thenReturn(workspace);
        when(responseConverter.convertDecisionWorkspaceToDTO(any(AssessmentDecisionWorkspace.class))).thenReturn(dto);

        MvcResult result = mockMvc.perform(
                get("/api/v1/admin/assessment-judgements/decisions").param("assessmentTimeId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("finalizeScore: 超级管理员应确认最终评分")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-judgement:finalize" })
    void finalizeScore_asSuperAdmin_shouldReturnJudgement() throws Exception {
        when(assessmentJudgementAppService.finalizeScore(any(AssessmentJudgementCommands.FinalizeScoreCommand.class)))
                .thenReturn(judgementResult());
        when(responseConverter.toDTO(any(AssessmentJudgementResult.class))).thenReturn(judgementDTO());

        FinalizeScoreRequestDTO request = new FinalizeScoreRequestDTO();
        request.setAnswerId(1L);
        request.setScore(new BigDecimal("90.00"));

        MvcResult result = mockMvc.perform(
                post("/api/v1/admin/assessment-judgements/finalize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("finalizeScore: 缺少评分应返回 400")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-judgement:finalize" })
    void finalizeScore_withMissingScore_shouldReturn400() throws Exception {
        FinalizeScoreRequestDTO request = new FinalizeScoreRequestDTO();
        request.setAnswerId(1L);

        MvcResult result = mockMvc.perform(
                post("/api/v1/admin/assessment-judgements/finalize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("publishDecisions: 超级管理员应发布考核决策结果")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-decision:publish" })
    void publishDecisions_asSuperAdmin_shouldReturnMailCount() throws Exception {
        when(assessmentJudgementAppService.publishDecisions(1L)).thenReturn(3);

        MvcResult result = mockMvc.perform(
                post("/api/v1/admin/assessment-judgements/decisions/publish").param("assessmentTimeId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(3))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }
}
