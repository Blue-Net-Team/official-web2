package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.converter.assessment_judgement.AssessmentJudgementRequestConverter;
import com.bluenet.web.api.converter.assessment_judgement.AssessmentJudgementResponseConverter;
import com.bluenet.web.api.dto.assessment_judgement.*;
import com.bluenet.web.application.command.assessment_judgement.AssessmentJudgementCommands;
import com.bluenet.web.application.result.assessment.*;
import com.bluenet.web.application.service.AssessmentJudgementAppService;
import com.bluenet.web.domain.model.enumerate.QuestionType;
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
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
class AdminAssessmentJudgementControllerTest extends BaseIntegrationTest {

    private static final long ADMIN_USER_ID = 9999L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AssessmentJudgementAppService assessmentJudgementAppService;

    @MockitoBean
    private AssessmentJudgementRequestConverter assessmentJudgementRequestConverter;

    @MockitoBean
    private AssessmentJudgementResponseConverter responseConverter;

    @AfterEach
    void tearDown() {
        UserCTX.clear();
    }

    @Test
    @DisplayName("getLatestByAnswerId: 查询答案最新评判应返回评判 DTO")
    @WithSecurityPrincipal(userId = ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-judgement:list" })
    void getLatestByAnswerId_shouldReturnJudgementDto() throws Exception {
        AssessmentJudgementResult result = new AssessmentJudgementResult(
                1L, 1L, 1L, 1L, 100L, new BigDecimal("90"), new BigDecimal("100"), null, null, null, null, null, null);
        AssessmentJudgementDTO dto = AssessmentJudgementDTO.builder()
                .id(1L)
                .answerId(1L)
                .score(new BigDecimal("90"))
                .build();

        when(assessmentJudgementAppService.getLatestByAnswerId(1L)).thenReturn(result);
        when(responseConverter.toDTO(result)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/admin/assessment-judgements/answers/{answerId}/latest", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.score").value(90));
    }

    @Test
    @DisplayName("listByQuestionId: 查询题目评判列表应返回列表")
    @WithSecurityPrincipal(userId = ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-judgement:query" })
    void listByQuestionId_shouldReturnList() throws Exception {
        AssessmentJudgementResult result = new AssessmentJudgementResult(
                1L, 1L, 1L, 1L, 100L, new BigDecimal("90"), new BigDecimal("100"), null, null, null, null, null, null);
        AssessmentJudgementDTO dto = AssessmentJudgementDTO.builder()
                .id(1L)
                .answerId(1L)
                .build();

        when(assessmentJudgementAppService.listByQuestionId(1L)).thenReturn(List.of(result));
        when(responseConverter.toDTO(result)).thenReturn(dto);

        mockMvc.perform(
                get("/api/v1/admin/assessment-judgements")
                        .param("questionId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    @DisplayName("decideAssessment: 设置考核通过决策应返回决策 DTO")
    @WithSecurityPrincipal(userId = ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-decision:set" })
    void decideAssessment_shouldReturnDecisionDto() throws Exception {
        AssessmentDecisionRequestDTO request = AssessmentDecisionRequestDTO.builder()
                .userId(100L)
                .assessmentTimeId(1L)
                .passed(true)
                .build();
        AssessmentJudgementCommands.DecideAssessmentCommand command = new AssessmentJudgementCommands.DecideAssessmentCommand(
                100L, 1L, true, null);
        AssessmentDecisionResult result = new AssessmentDecisionResult(
                1L, 100L, 1L, true, ADMIN_USER_ID, null, LocalDateTime.now());
        AssessmentDecisionDTO dto = AssessmentDecisionDTO.builder()
                .id(1L)
                .userId(100L)
                .passed(true)
                .build();

        when(assessmentJudgementRequestConverter.toCommand(request)).thenReturn(command);
        when(assessmentJudgementAppService.decideAssessment(command)).thenReturn(result);
        when(responseConverter.toDTO(result)).thenReturn(dto);

        mockMvc.perform(
                post("/api/v1/admin/assessment-judgements/decisions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.passed").value(true));
    }

    @Test
    @DisplayName("listQuestionScoreboard: 查询题目评分汇总应返回列表")
    @WithSecurityPrincipal(userId = ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-scoreboard:question" })
    void listQuestionScoreboard_shouldReturnList() throws Exception {
        AssessmentQuestionScoreboard result = AssessmentQuestionScoreboard.builder()
                .questionId(1L)
                .assessmentTimeId(1L)
                .questionNo(1)
                .questionType(QuestionType.FILE_UPLOAD)
                .title("题目一")
                .maxScore(new BigDecimal("100"))
                .submittedCount(10L)
                .judgedCount(5L)
                .pendingCount(5L)
                .averageScore(new BigDecimal("80"))
                .build();
        AssessmentQuestionScoreboardDTO dto = AssessmentQuestionScoreboardDTO.builder()
                .questionId(1L)
                .submittedCount(10L)
                .build();

        when(assessmentJudgementAppService.listQuestionScoreboard(1L, QuestionType.FILE_UPLOAD, null))
                .thenReturn(List.of(result));
        when(responseConverter.convertScoreboardToDTO(result)).thenReturn(dto);

        mockMvc.perform(
                get("/api/v1/admin/assessment-judgements/scoreboard/questions")
                        .param("assessmentTimeId", "1")
                        .param("questionType", "FILE_UPLOAD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].questionId").value(1));
    }

    @Test
    @DisplayName("listQuestionSubmissions: 查询题目提交评分列表应返回列表")
    @WithSecurityPrincipal(userId = ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-scoreboard:submission" })
    void listQuestionSubmissions_shouldReturnList() throws Exception {
        AssessmentQuestionSubmissionReadModel result = AssessmentQuestionSubmissionReadModel.builder()
                .answerId(1L)
                .questionId(1L)
                .candidateUserId(100L)
                .build();
        AssessmentQuestionSubmissionDTO dto = AssessmentQuestionSubmissionDTO.builder()
                .answerId(1L)
                .candidateUserId(100L)
                .build();

        when(assessmentJudgementAppService.listQuestionSubmissions(1L, null, "JUDGED"))
                .thenReturn(List.of(result));
        when(responseConverter.convertSubmissionToDTO(result)).thenReturn(dto);

        mockMvc.perform(
                get("/api/v1/admin/assessment-judgements/scoreboard/questions/{questionId}/submissions", 1L)
                        .param("status", "JUDGED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].answerId").value(1));
    }

    @Test
    @DisplayName("listCandidateScoreboard: 查询考生评分矩阵应返回列表")
    @WithSecurityPrincipal(userId = ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-scoreboard:candidate" })
    void listCandidateScoreboard_shouldReturnList() throws Exception {
        AssessmentCandidateScoreboard result = AssessmentCandidateScoreboard.builder()
                .candidateUserId(100L)
                .username("考生A")
                .totalScore(new BigDecimal("180"))
                .questionScores(List.of())
                .build();
        AssessmentCandidateScoreboardDTO dto = AssessmentCandidateScoreboardDTO.builder()
                .candidateUserId(100L)
                .username("考生A")
                .build();

        when(assessmentJudgementAppService.listCandidateScoreboard(1L, null))
                .thenReturn(List.of(result));
        when(responseConverter.convertCandidateScoreboardToDTO(result)).thenReturn(dto);

        mockMvc.perform(
                get("/api/v1/admin/assessment-judgements/scoreboard/candidates")
                        .param("assessmentTimeId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].candidateUserId").value(100));
    }

    @Test
    @DisplayName("getDecisionWorkspace: 查询录用决策工作台应返回工作台 DTO")
    @WithSecurityPrincipal(userId = ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-decision:query" })
    void getDecisionWorkspace_shouldReturnWorkspaceDto() throws Exception {
        AssessmentDecisionStatistics statistics = AssessmentDecisionStatistics.builder()
                .candidates(10L)
                .pending(5L)
                .passed(3L)
                .eliminated(2L)
                .build();
        AssessmentDecisionCandidate candidate = AssessmentDecisionCandidate.builder()
                .candidateUserId(100L)
                .username("考生A")
                .build();
        AssessmentDecisionWorkspace result = AssessmentDecisionWorkspace.builder()
                .statistics(statistics)
                .candidates(List.of(candidate))
                .build();
        AssessmentDecisionWorkspaceDTO dto = AssessmentDecisionWorkspaceDTO.builder()
                .statistics(AssessmentDecisionStatisticsDTO.builder().candidates(10L).build())
                .candidates(List.of(AssessmentDecisionCandidateDTO.builder().candidateUserId(100L).build()))
                .build();

        when(assessmentJudgementAppService.getDecisionWorkspace(1L, null, "PENDING")).thenReturn(result);
        when(responseConverter.convertDecisionWorkspaceToDTO(result)).thenReturn(dto);

        mockMvc.perform(
                get("/api/v1/admin/assessment-judgements/decisions")
                        .param("assessmentTimeId", "1")
                        .param("decisionStatus", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.statistics.candidates").value(10))
                .andExpect(jsonPath("$.data.candidates[0].candidateUserId").value(100));
    }

    @Test
    @DisplayName("finalizeScore: 确认最终评分应返回评判 DTO")
    @WithSecurityPrincipal(userId = ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-judgement:finalize" })
    void finalizeScore_shouldReturnJudgementDto() throws Exception {
        FinalizeScoreRequestDTO request = new FinalizeScoreRequestDTO();
        request.setAnswerId(1L);
        request.setScore(new BigDecimal("85"));
        AssessmentJudgementResult result = new AssessmentJudgementResult(
                1L, 1L, 1L, 1L, 100L, new BigDecimal("85"), new BigDecimal("100"), null, null, null, null, null, null);
        AssessmentJudgementDTO dto = AssessmentJudgementDTO.builder()
                .id(1L)
                .score(new BigDecimal("85"))
                .build();

        when(assessmentJudgementAppService.finalizeScore(any(AssessmentJudgementCommands.FinalizeScoreCommand.class)))
                .thenReturn(result);
        when(responseConverter.toDTO(result)).thenReturn(dto);

        mockMvc.perform(
                post("/api/v1/admin/assessment-judgements/finalize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.score").value(85));
    }

    @Test
    @DisplayName("publishDecisions: 发布本轮考核结果应返回发送数量")
    @WithSecurityPrincipal(userId = ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-decision:publish" })
    void publishDecisions_shouldReturnCount() throws Exception {
        when(assessmentJudgementAppService.publishDecisions(1L)).thenReturn(5);

        mockMvc.perform(
                post("/api/v1/admin/assessment-judgements/decisions/publish")
                        .param("assessmentTimeId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(5));
    }
}
