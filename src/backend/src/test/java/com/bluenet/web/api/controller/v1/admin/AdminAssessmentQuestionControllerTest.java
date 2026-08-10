package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.converter.assessment_question.AssessmentQuestionRequestConverter;
import com.bluenet.web.api.converter.assessment_question.AssessmentQuestionResponseConverter;
import com.bluenet.web.api.dto.assessment_question.AssessmentQuestionDTO;
import com.bluenet.web.api.dto.assessment_question.CreateQuestionRequestDTO;
import com.bluenet.web.api.dto.assessment_question.UpdateQuestionRequestDTO;
import com.bluenet.web.application.command.assessment_question.AssessmentQuestionCommands;
import com.bluenet.web.application.result.assessment.AssessmentQuestionResult;
import com.bluenet.web.application.service.AssessmentQuestionAppService;
import com.bluenet.web.domain.model.enumerate.QuestionType;
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

import java.math.BigDecimal;
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
@DisplayName("AdminAssessmentQuestionController 集成测试")
class AdminAssessmentQuestionControllerTest extends BaseIntegrationTest {

    private static final long ADMIN_USER_ID = 9999L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AssessmentQuestionAppService assessmentQuestionAppService;

    @MockitoBean
    private AssessmentQuestionRequestConverter assessmentQuestionRequestConverter;

    @MockitoBean
    private AssessmentQuestionResponseConverter responseConverter;

    @AfterEach
    void tearDown() {
        UserCTX.clear();
    }

    @Test
    @DisplayName("createQuestion: 管理员创建考题应返回考题 DTO")
    @WithSecurityPrincipal(userId = ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-question:create" })
    void createQuestion_shouldReturnQuestionDto() throws Exception {
        CreateQuestionRequestDTO request = CreateQuestionRequestDTO.builder()
                .assessmentTimeId(1L)
                .questionNo(1)
                .questionType(QuestionType.SINGLE_CHOICE)
                .title("题目一")
                .score(BigDecimal.TEN)
                .build();
        AssessmentQuestionCommands.CreateAssessmentQuestionCommand command = new AssessmentQuestionCommands.CreateAssessmentQuestionCommand(
                1L, 1, QuestionType.SINGLE_CHOICE, "题目一", null, null, BigDecimal.TEN);
        AssessmentQuestionResult result = new AssessmentQuestionResult(
                1L, 1L, 1, QuestionType.SINGLE_CHOICE, "题目一", null, null, BigDecimal.TEN, false);
        AssessmentQuestionDTO dto = AssessmentQuestionDTO.builder()
                .id(1L)
                .title("题目一")
                .build();

        when(assessmentQuestionRequestConverter.toCommand(request)).thenReturn(command);
        when(assessmentQuestionAppService.createQuestion(command)).thenReturn(result);
        when(responseConverter.toDTO(result)).thenReturn(dto);

        mockMvc.perform(
                post("/api/v1/admin/assessment-questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("updateQuestion: 管理员更新考题应返回考题 DTO")
    @WithSecurityPrincipal(userId = ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-question:update" })
    void updateQuestion_shouldReturnQuestionDto() throws Exception {
        UpdateQuestionRequestDTO request = UpdateQuestionRequestDTO.builder()
                .title("更新标题")
                .score(BigDecimal.TEN)
                .build();
        AssessmentQuestionCommands.UpdateAssessmentQuestionCommand command = new AssessmentQuestionCommands.UpdateAssessmentQuestionCommand(
                1L, null, null, "更新标题", null, null, BigDecimal.TEN);
        AssessmentQuestionResult result = new AssessmentQuestionResult(
                1L, 1L, 1, QuestionType.SINGLE_CHOICE, "更新标题", null, null, BigDecimal.TEN, false);
        AssessmentQuestionDTO dto = AssessmentQuestionDTO.builder()
                .id(1L)
                .title("更新标题")
                .build();

        when(assessmentQuestionRequestConverter.toCommand(1L, request)).thenReturn(command);
        when(assessmentQuestionAppService.updateQuestion(command)).thenReturn(result);
        when(responseConverter.toDTO(result)).thenReturn(dto);

        mockMvc.perform(
                put("/api/v1/admin/assessment-questions/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("更新标题"));
    }

    @Test
    @DisplayName("deleteQuestion: 管理员删除考题应返回 200")
    @WithSecurityPrincipal(userId = ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-question:delete" })
    void deleteQuestion_shouldReturnOk() throws Exception {
        doNothing().when(assessmentQuestionAppService).deleteQuestion(1L);

        mockMvc.perform(delete("/api/v1/admin/assessment-questions/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("listQuestions: 管理员分页查询考题列表应返回分页")
    @WithSecurityPrincipal(userId = ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-question:list" })
    void listQuestions_shouldReturnPagedList() throws Exception {
        AssessmentQuestionResult result = new AssessmentQuestionResult(
                1L, 1L, 1, QuestionType.SINGLE_CHOICE, "题目一", null, null, BigDecimal.TEN, false);
        AssessmentQuestionDTO dto = AssessmentQuestionDTO.builder()
                .id(1L)
                .title("题目一")
                .build();

        when(assessmentQuestionAppService.listQuestionsForAdmin(1L, 0, 10))
                .thenReturn(new PageImpl<>(List.of(result)));
        when(responseConverter.toDTO(any(AssessmentQuestionResult.class))).thenReturn(dto);

        mockMvc.perform(
                get("/api/v1/admin/assessment-questions")
                        .param("assessmentTimeId", "1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(1));
    }

    @Test
    @DisplayName("updateAttachment: 管理员更新题目附件应返回 200")
    @WithSecurityPrincipal(userId = ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-question:update-attachment" })
    void updateAttachment_shouldReturnOk() throws Exception {
        doNothing().when(assessmentQuestionAppService).updateAttachment(1L, 10L);

        mockMvc.perform(
                put("/api/v1/admin/assessment-questions/{id}/attachment", 1L)
                        .param("fileId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("createQuestion: 无权限用户应返回 403")
    @WithSecurityPrincipal(userId = ADMIN_USER_ID, roleType = "MEMBER", roleId = 3L, permissions = {})
    void createQuestion_withoutPermission_shouldReturn403() throws Exception {
        CreateQuestionRequestDTO request = CreateQuestionRequestDTO.builder()
                .assessmentTimeId(1L)
                .questionNo(1)
                .questionType(QuestionType.SINGLE_CHOICE)
                .title("题目一")
                .score(BigDecimal.TEN)
                .build();

        mockMvc.perform(
                post("/api/v1/admin/assessment-questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
