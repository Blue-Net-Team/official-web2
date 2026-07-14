package com.bluenet.web.api.controller.v1.assessment;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.assessment_answer.AssessmentAnswerDTO;
import com.bluenet.web.api.dto.assessment_answer.CreateAnswerRequestDTO;
import com.bluenet.web.api.converter.assessment_answer.AssessmentAnswerRequestConverter;
import com.bluenet.web.api.converter.assessment_answer.AssessmentAnswerResponseConverter;
import com.bluenet.web.application.command.assessment_answer.AssessmentAnswerCommands;
import com.bluenet.web.application.result.assessment.AssessmentAnswerResult;
import com.bluenet.web.application.service.AssessmentAnswerAppService;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@DisplayName("AssessmentAnswerController 集成测试")
class AssessmentAnswerControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AssessmentAnswerAppService assessmentAnswerAppService;

    @MockitoBean
    private AssessmentAnswerRequestConverter requestConverter;

    @MockitoBean
    private AssessmentAnswerResponseConverter responseConverter;

    @AfterEach
    void tearDown() {
        UserCTX.clear();
    }

    private CreateAnswerRequestDTO buildValidRequest() {
        return CreateAnswerRequestDTO.builder()
                .questionId(1L)
                .content("答案内容")
                .build();
    }

    @Test
    @DisplayName("createAnswer: 已登录用户提交答案应成功")
    @WithSecurityPrincipal(userId = 1L, roleType = "CANDIDATE", roleId = 4L)
    void createAnswer_authenticated_shouldReturnCreated() throws Exception {
        CreateAnswerRequestDTO request = buildValidRequest();
        AssessmentAnswerCommands.CreateAssessmentAnswerCommand command = new AssessmentAnswerCommands.CreateAssessmentAnswerCommand(
                1L, 1L, "答案内容", null, null);
        AssessmentAnswerResult result = new AssessmentAnswerResult(1L, 1L, null, "答案内容", null, null, null, null);
        AssessmentAnswerDTO dto = AssessmentAnswerDTO.builder()
                .id(1L)
                .questionId(1L)
                .content("答案内容")
                .build();
        when(requestConverter.toCreateCommand(1L, request)).thenReturn(command);
        when(assessmentAnswerAppService.createAnswer(command)).thenReturn(result);
        when(responseConverter.toDTO(result)).thenReturn(dto);

        mockMvc.perform(
                post("/api/v1/assessment-answers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.content").value("答案内容"));
    }

    @Test
    @DisplayName("createAnswer: 缺少 questionId 应返回 400")
    @WithSecurityPrincipal(userId = 1L, roleType = "CANDIDATE", roleId = 4L)
    void createAnswer_missingQuestionId_shouldReturnBadRequest() throws Exception {
        CreateAnswerRequestDTO request = CreateAnswerRequestDTO.builder().build();

        mockMvc.perform(
                post("/api/v1/assessment-answers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("updateAnswer: 已登录用户更新答案应成功")
    @WithSecurityPrincipal(userId = 1L, roleType = "CANDIDATE", roleId = 4L)
    void updateAnswer_authenticated_shouldReturnOk() throws Exception {
        CreateAnswerRequestDTO request = buildValidRequest();
        AssessmentAnswerCommands.UpdateAssessmentAnswerCommand command = new AssessmentAnswerCommands.UpdateAssessmentAnswerCommand(
                1L, 1L, "更新答案", null, null);
        AssessmentAnswerResult result = new AssessmentAnswerResult(1L, 1L, null, "更新答案", null, null, null, null);
        AssessmentAnswerDTO dto = AssessmentAnswerDTO.builder()
                .id(1L)
                .content("更新答案")
                .build();
        when(requestConverter.toUpdateCommand(1L, request)).thenReturn(command);
        when(assessmentAnswerAppService.updateAnswer(command)).thenReturn(result);
        when(responseConverter.toDTO(result)).thenReturn(dto);

        mockMvc.perform(
                put("/api/v1/assessment-answers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").value("更新答案"));
    }

    @Test
    @DisplayName("getMyAnswer: 已登录用户应返回答案")
    @WithSecurityPrincipal(userId = 1L, roleType = "CANDIDATE", roleId = 4L)
    void getMyAnswer_authenticated_shouldReturnAnswer() throws Exception {
        AssessmentAnswerResult result = new AssessmentAnswerResult(1L, 1L, null, "我的答案", null, null, null, null);
        AssessmentAnswerDTO dto = AssessmentAnswerDTO.builder()
                .id(1L)
                .questionId(1L)
                .content("我的答案")
                .build();
        when(assessmentAnswerAppService.getMyAnswer(1L, 1L)).thenReturn(result);
        when(responseConverter.toDTO(result)).thenReturn(dto);

        mockMvc.perform(
                get("/api/v1/assessment-answers")
                        .param("questionId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.content").value("我的答案"));
    }

    @Test
    @DisplayName("getMyAnswer: 未作答时应返回 404")
    @WithSecurityPrincipal(userId = 1L, roleType = "CANDIDATE", roleId = 4L)
    void getMyAnswer_noAnswer_shouldReturnNotFound() throws Exception {
        when(assessmentAnswerAppService.getMyAnswer(1L, 1L)).thenReturn(null);

        mockMvc.perform(
                get("/api/v1/assessment-answers")
                        .param("questionId", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("getMyAnswer: 未登录应返回 401")
    void getMyAnswer_anonymous_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(
                get("/api/v1/assessment-answers")
                        .param("questionId", "1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }
}
