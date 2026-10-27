package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.APIIntegrationTest;
import com.bluenet.web.api.converter.assessment_question.AssessmentQuestionRequestConverter;
import com.bluenet.web.api.converter.assessment_question.AssessmentQuestionResponseConverter;
import com.bluenet.web.api.dto.assessment_question.AssessmentQuestionDTO;
import com.bluenet.web.api.dto.assessment_question.CloneQuestionRequestDTO;
import com.bluenet.web.api.dto.assessment_question.CreateQuestionRequestDTO;
import com.bluenet.web.api.dto.assessment_question.UpdateQuestionRequestDTO;
import com.bluenet.web.application.command.assessment_question.AssessmentQuestionCommands;
import com.bluenet.web.application.result.assessment.AssessmentQuestionResult;
import com.bluenet.web.application.service.AssessmentQuestionAppService;
import com.bluenet.web.domain.exception.DataConflict;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.model.enumerate.QuestionType;
import com.bluenet.web.infrastructure.security.principal.WithSecurityPrincipal;
import io.github.ivencn.infra.security.principal.UserCTX;
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

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
class AdminAssessmentQuestionControllerIntegrationTest extends APIIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AssessmentQuestionAppService assessmentQuestionAppService;

    @MockitoBean
    private AssessmentQuestionResponseConverter responseConverter;

    @Autowired
    private AssessmentQuestionRequestConverter requestConverter;

    @AfterEach
    void tearDown() {
        UserCTX.clear();
    }

    private static final long SUPER_ADMIN_USER_ID = 9999L;

    private AssessmentQuestionResult questionResult() {
        return new AssessmentQuestionResult(
                1L,
                1L,
                1,
                QuestionType.ALGORITHM,
                "题目一",
                null,
                10L,
                new BigDecimal("100.00"),
                false);
    }

    private AssessmentQuestionDTO questionDTO() {
        return AssessmentQuestionDTO.builder()
                .id(1L)
                .assessmentTimeId(1L)
                .questionNo(1)
                .questionType(QuestionType.ALGORITHM)
                .title("题目一")
                .attachmentId(10L)
                .score(new BigDecimal("100.00"))
                .answered(false)
                .build();
    }

    @Test
    @DisplayName("listQuestions: 普通成员访问应返回 403")
    @WithSecurityPrincipal(roleType = "MEMBER", roleId = 3L, permissions = {})
    void listQuestions_asMember_shouldReturn403() throws Exception {
        MvcResult result = mockMvc.perform(
                get("/api/v1/admin/assessment-questions").param("assessmentTimeId", "1"))
                .andExpect(status().isForbidden())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(403);
    }

    @Test
    @DisplayName("listQuestions: 超级管理员应返回分页考题列表")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-question:list" })
    void listQuestions_asSuperAdmin_shouldReturnPagedQuestions() throws Exception {
        when(assessmentQuestionAppService.listQuestionsForAdmin(1L, 0, 10))
                .thenReturn(new PageImpl<>(List.of(questionResult())));
        when(responseConverter.toDTO(any(AssessmentQuestionResult.class))).thenReturn(questionDTO());

        MvcResult result = mockMvc.perform(
                get("/api/v1/admin/assessment-questions")
                        .param("assessmentTimeId", "1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("createQuestion: 超级管理员应成功创建考题")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-question:create" })
    void createQuestion_asSuperAdmin_shouldReturnCreatedQuestion() throws Exception {
        when(
                assessmentQuestionAppService
                        .createQuestion(any(AssessmentQuestionCommands.CreateAssessmentQuestionCommand.class)))
                                .thenReturn(questionResult());
        when(responseConverter.toDTO(any(AssessmentQuestionResult.class))).thenReturn(questionDTO());

        CreateQuestionRequestDTO request = CreateQuestionRequestDTO.builder()
                .assessmentTimeId(1L)
                .questionNo(1)
                .questionType(QuestionType.ALGORITHM)
                .title("题目一")
                .score(new BigDecimal("100.00"))
                .build();

        MvcResult result = mockMvc.perform(
                post("/api/v1/admin/assessment-questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("createQuestion: 题号重复时应返回 409")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-question:create" })
    void createQuestion_whenDuplicate_shouldReturn409() throws Exception {
        when(
                assessmentQuestionAppService
                        .createQuestion(any(AssessmentQuestionCommands.CreateAssessmentQuestionCommand.class)))
                                .thenThrow(new DataConflict("题号重复"));

        CreateQuestionRequestDTO request = CreateQuestionRequestDTO.builder()
                .assessmentTimeId(1L)
                .questionNo(1)
                .questionType(QuestionType.ALGORITHM)
                .title("题目一")
                .score(new BigDecimal("100.00"))
                .build();

        MvcResult result = mockMvc.perform(
                post("/api/v1/admin/assessment-questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(409);
    }

    @Test
    @DisplayName("createQuestion: 缺少考核时间ID应返回 400")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-question:create" })
    void createQuestion_withMissingAssessmentTimeId_shouldReturn400() throws Exception {
        CreateQuestionRequestDTO request = CreateQuestionRequestDTO.builder()
                .questionNo(1)
                .questionType(QuestionType.ALGORITHM)
                .title("题目一")
                .score(new BigDecimal("100.00"))
                .build();

        MvcResult result = mockMvc.perform(
                post("/api/v1/admin/assessment-questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("updateQuestion: 超级管理员应成功更新考题")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-question:update" })
    void updateQuestion_asSuperAdmin_shouldReturnUpdatedQuestion() throws Exception {
        when(
                assessmentQuestionAppService
                        .updateQuestion(any(AssessmentQuestionCommands.UpdateAssessmentQuestionCommand.class)))
                                .thenReturn(questionResult());
        when(responseConverter.toDTO(any(AssessmentQuestionResult.class))).thenReturn(questionDTO());

        UpdateQuestionRequestDTO request = UpdateQuestionRequestDTO.builder()
                .title("更新题目")
                .score(new BigDecimal("90.00"))
                .build();

        MvcResult result = mockMvc.perform(
                put("/api/v1/admin/assessment-questions/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("updateQuestion: 考题不存在时应返回 404")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-question:update" })
    void updateQuestion_whenNotFound_shouldReturn404() throws Exception {
        when(
                assessmentQuestionAppService
                        .updateQuestion(any(AssessmentQuestionCommands.UpdateAssessmentQuestionCommand.class)))
                                .thenThrow(new DataNotFound("考题不存在"));

        UpdateQuestionRequestDTO request = UpdateQuestionRequestDTO.builder()
                .title("更新题目")
                .build();

        MvcResult result = mockMvc.perform(
                put("/api/v1/admin/assessment-questions/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("deleteQuestion: 超级管理员应成功删除考题")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-question:delete" })
    void deleteQuestion_asSuperAdmin_shouldReturnOk() throws Exception {
        doNothing().when(assessmentQuestionAppService).deleteQuestion(1L);

        MvcResult result = mockMvc.perform(delete("/api/v1/admin/assessment-questions/{id}", 1L))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("updateAttachment: 超级管理员应成功更新题目附件")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-question:update-attachment" })
    void updateAttachment_asSuperAdmin_shouldReturnOk() throws Exception {
        doNothing().when(assessmentQuestionAppService).updateAttachment(1L, 20L);

        MvcResult result = mockMvc.perform(
                put("/api/v1/admin/assessment-questions/{id}/attachment", 1L)
                        .param("fileId", "20"))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("cloneQuestion: 无权限用户应返回 403")
    @WithSecurityPrincipal(roleType = "MEMBER", roleId = 3L, permissions = {})
    void cloneQuestion_asMember_shouldReturn403() throws Exception {
        mockMvc.perform(
                post("/api/v1/admin/assessment-questions/{id}/clone", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cloneRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("cloneQuestion: 非法参数应返回 400")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-question:clone" })
    void cloneQuestion_withInvalidRequest_shouldReturn400() throws Exception {
        mockMvc.perform(
                post("/api/v1/admin/assessment-questions/{id}/clone", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CloneQuestionRequestDTO.builder().build())))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("cloneQuestion: 源或目标不存在应返回 404")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-question:clone" })
    void cloneQuestion_whenNotFound_shouldReturn404() throws Exception {
        when(
                assessmentQuestionAppService.cloneQuestion(
                        anyLong(),
                        any(AssessmentQuestionCommands.CloneAssessmentQuestionCommand.class)))
                                .thenThrow(new DataNotFound("目标考核时间不存在"));

        mockMvc.perform(
                post("/api/v1/admin/assessment-questions/{id}/clone", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cloneRequest())))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("cloneQuestion: 题号冲突应返回 409")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-question:clone" })
    void cloneQuestion_whenQuestionNoConflicts_shouldReturn409() throws Exception {
        when(
                assessmentQuestionAppService.cloneQuestion(
                        anyLong(),
                        any(AssessmentQuestionCommands.CloneAssessmentQuestionCommand.class)))
                                .thenThrow(new DataConflict("题号重复"));

        mockMvc.perform(
                post("/api/v1/admin/assessment-questions/{id}/clone", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cloneRequest())))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("cloneQuestion: 方向越权应返回 403")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-question:clone" })
    void cloneQuestion_whenDirectionForbidden_shouldReturn403() throws Exception {
        when(
                assessmentQuestionAppService.cloneQuestion(
                        anyLong(),
                        any(AssessmentQuestionCommands.CloneAssessmentQuestionCommand.class)))
                                .thenThrow(new Forbidden("只能在本方向克隆"));

        mockMvc.perform(
                post("/api/v1/admin/assessment-questions/{id}/clone", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cloneRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("cloneQuestion: 超级管理员应成功克隆并返回新题目")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-question:clone" })
    void cloneQuestion_asSuperAdmin_shouldReturnCreatedQuestion() throws Exception {
        when(
                assessmentQuestionAppService.cloneQuestion(
                        anyLong(),
                        any(AssessmentQuestionCommands.CloneAssessmentQuestionCommand.class)))
                                .thenReturn(
                                        new AssessmentQuestionResult(
                                                2L, 2L, 3, QuestionType.ALGORITHM, "题目一", null, null, BigDecimal.TEN,
                                                null));
        when(responseConverter.toDTO(any(AssessmentQuestionResult.class))).thenReturn(
                AssessmentQuestionDTO.builder().id(2L).assessmentTimeId(2L).questionNo(3).build());

        mockMvc.perform(
                post("/api/v1/admin/assessment-questions/{id}/clone", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cloneRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.data.assessmentTimeId").value(2))
                .andExpect(jsonPath("$.data.questionNo").value(3));
    }

    private CloneQuestionRequestDTO cloneRequest() {
        return CloneQuestionRequestDTO.builder()
                .targetAssessmentTimeId(2L)
                .targetQuestionNo(3)
                .build();
    }

    @Test
    @DisplayName("updateAttachment: 缺少文件ID应返回 400")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-question:update-attachment" })
    void updateAttachment_withMissingFileId_shouldReturn400() throws Exception {
        MvcResult result = mockMvc.perform(put("/api/v1/admin/assessment-questions/{id}/attachment", 1L))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(400);
    }
}
