package com.bluenet.web.api.controller.v1.assessment;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.converter.assessment_answer.AssessmentAnswerRequestConverter;
import com.bluenet.web.api.converter.assessment_answer.AssessmentAnswerResponseConverter;
import com.bluenet.web.api.dto.assessment_answer.AssessmentAnswerDTO;
import com.bluenet.web.api.dto.assessment_answer.CreateAnswerRequestDTO;
import com.bluenet.web.application.command.assessment_answer.AssessmentAnswerCommands;
import com.bluenet.web.application.result.assessment.AssessmentAnswerResult;
import com.bluenet.web.application.service.AssessmentAnswerAppService;
import com.bluenet.web.domain.exception.DataConflict;
import com.bluenet.web.domain.model.enumerate.ProgrammingLanguage;
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
class AssessmentAnswerControllerTest extends BaseIntegrationTest {

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

    @Test
    @DisplayName("createAnswer: 已登录用户提交答案应返回答案 DTO")
    @WithSecurityPrincipal(userId = 100L)
    void createAnswer_shouldReturnAnswerDto() throws Exception {
        CreateAnswerRequestDTO request = CreateAnswerRequestDTO.builder()
                .questionId(1L)
                .content("answer content")
                .language(ProgrammingLanguage.JAVA)
                .build();
        AssessmentAnswerCommands.CreateAssessmentAnswerCommand command = new AssessmentAnswerCommands.CreateAssessmentAnswerCommand(
                100L, 1L, "answer content", ProgrammingLanguage.JAVA, null);
        AssessmentAnswerResult result = new AssessmentAnswerResult(
                1L, 1L, null, "answer content", ProgrammingLanguage.JAVA,
                LocalDateTime.now(), null, null);
        AssessmentAnswerDTO dto = AssessmentAnswerDTO.builder()
                .id(1L)
                .questionId(1L)
                .content("answer content")
                .build();

        when(requestConverter.toCreateCommand(100L, request)).thenReturn(command);
        when(assessmentAnswerAppService.createAnswer(command)).thenReturn(result);
        when(responseConverter.toDTO(result)).thenReturn(dto);

        mockMvc.perform(
                post("/api/v1/assessment-answers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.questionId").value(1));
    }

    @Test
    @DisplayName("createAnswer: 参数校验失败应返回 400")
    @WithSecurityPrincipal(userId = 100L)
    void createAnswer_withInvalidRequest_shouldReturn400() throws Exception {
        CreateAnswerRequestDTO request = CreateAnswerRequestDTO.builder().build();

        mockMvc.perform(
                post("/api/v1/assessment-answers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("createAnswer: 重复提交冲突应返回 409")
    @WithSecurityPrincipal(userId = 100L)
    void createAnswer_whenConflict_shouldReturn409() throws Exception {
        CreateAnswerRequestDTO request = CreateAnswerRequestDTO.builder()
                .questionId(1L)
                .content("answer content")
                .build();
        AssessmentAnswerCommands.CreateAssessmentAnswerCommand command = new AssessmentAnswerCommands.CreateAssessmentAnswerCommand(
                100L, 1L, "answer content", null, null);

        when(requestConverter.toCreateCommand(100L, request)).thenReturn(command);
        when(assessmentAnswerAppService.createAnswer(any()))
                .thenThrow(new DataConflict("重复提交"));

        mockMvc.perform(
                post("/api/v1/assessment-answers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409));
    }

    @Test
    @DisplayName("updateAnswer: 已登录用户更新答案应返回答案 DTO")
    @WithSecurityPrincipal(userId = 100L)
    void updateAnswer_shouldReturnAnswerDto() throws Exception {
        CreateAnswerRequestDTO request = CreateAnswerRequestDTO.builder()
                .questionId(1L)
                .content("updated content")
                .build();
        AssessmentAnswerCommands.UpdateAssessmentAnswerCommand command = new AssessmentAnswerCommands.UpdateAssessmentAnswerCommand(
                100L, 1L, "updated content", null, null);
        AssessmentAnswerResult result = new AssessmentAnswerResult(
                1L, 1L, null, "updated content", null,
                LocalDateTime.now(), null, null);
        AssessmentAnswerDTO dto = AssessmentAnswerDTO.builder()
                .id(1L)
                .questionId(1L)
                .content("updated content")
                .build();

        when(requestConverter.toUpdateCommand(100L, request)).thenReturn(command);
        when(assessmentAnswerAppService.updateAnswer(command)).thenReturn(result);
        when(responseConverter.toDTO(result)).thenReturn(dto);

        mockMvc.perform(
                put("/api/v1/assessment-answers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").value("updated content"));
    }

    @Test
    @DisplayName("getMyAnswer: 未作答时应返回 404")
    @WithSecurityPrincipal(userId = 100L)
    void getMyAnswer_whenNotFound_shouldReturn404() throws Exception {
        when(assessmentAnswerAppService.getMyAnswer(100L, 1L)).thenReturn(null);

        mockMvc.perform(
                get("/api/v1/assessment-answers")
                        .param("questionId", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("getMyAnswer: 已作答时应返回答案 DTO")
    @WithSecurityPrincipal(userId = 100L)
    void getMyAnswer_whenExists_shouldReturnAnswerDto() throws Exception {
        AssessmentAnswerResult result = new AssessmentAnswerResult(
                1L, 1L, null, "content", null,
                LocalDateTime.now(), null, null);
        AssessmentAnswerDTO dto = AssessmentAnswerDTO.builder()
                .id(1L)
                .questionId(1L)
                .content("content")
                .build();

        when(assessmentAnswerAppService.getMyAnswer(100L, 1L)).thenReturn(result);
        when(responseConverter.toDTO(result)).thenReturn(dto);

        mockMvc.perform(
                get("/api/v1/assessment-answers")
                        .param("questionId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }
}
