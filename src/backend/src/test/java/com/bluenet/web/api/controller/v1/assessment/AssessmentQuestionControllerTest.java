package com.bluenet.web.api.controller.v1.assessment;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.converter.assessment_question.AssessmentQuestionResponseConverter;
import com.bluenet.web.api.dto.PageDTO;
import com.bluenet.web.api.dto.assessment_question.AssessmentQuestionDTO;
import com.bluenet.web.api.dto.assessment_question.UserQuestionListResponse;
import com.bluenet.web.application.result.assessment.AssessmentQuestionResult;
import com.bluenet.web.application.result.user.UserQuestionListResult;
import com.bluenet.web.application.service.AssessmentQuestionAppService;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.enumerate.QuestionType;
import com.bluenet.web.infrastructure.security.principal.WithSecurityPrincipal;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.bluenet.web.testconfig.TestSecurityConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@DisplayName("AssessmentQuestionController 集成测试")
class AssessmentQuestionControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AssessmentQuestionAppService assessmentQuestionAppService;

    @MockitoBean
    private AssessmentQuestionResponseConverter responseConverter;

    @AfterEach
    void tearDown() {
        UserCTX.clear();
    }

    @Test
    @DisplayName("listQuestions: 已登录用户查询考题目录应返回列表")
    @WithSecurityPrincipal(userId = 100L)
    void listQuestions_shouldReturnList() throws Exception {
        AssessmentQuestionResult result = new AssessmentQuestionResult(
                1L, 1L, 1, QuestionType.SINGLE_CHOICE, "title", null, null, BigDecimal.TEN, false);
        PageDTO<AssessmentQuestionDTO> pageDTO = new PageDTO<>(
                List.of(AssessmentQuestionDTO.builder().id(1L).title("title").build()),
                1, 1, 0, 10, 1, true, true, false);
        UserQuestionListResponse response = UserQuestionListResponse.builder()
                .questions(pageDTO)
                .ended(false)
                .build();

        when(assessmentQuestionAppService.listQuestionsForUser(1L, 0, 10))
                .thenReturn(new UserQuestionListResult(new PageImpl<>(List.of(result)), null, false));
        when(responseConverter.toResponse(any())).thenReturn(response);

        mockMvc.perform(
                get("/api/v1/assessment-questions")
                        .param("assessmentTimeId", "1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.questions.content[0].id").value(1));
    }

    @Test
    @DisplayName("getQuestionDetail: 已登录用户查询题目详情应返回 DTO")
    @WithSecurityPrincipal(userId = 100L)
    void getQuestionDetail_shouldReturnDto() throws Exception {
        AssessmentQuestionResult result = new AssessmentQuestionResult(
                1L, 1L, 1, QuestionType.SINGLE_CHOICE, "title", null, null, BigDecimal.TEN, false);
        AssessmentQuestionDTO dto = AssessmentQuestionDTO.builder()
                .id(1L)
                .title("title")
                .build();

        when(assessmentQuestionAppService.getQuestionDetailForUser(1L)).thenReturn(result);
        when(responseConverter.toDTOForUser(result)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/assessment-questions/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("title"));
    }

    @Test
    @DisplayName("getQuestionDetail: 题目不存在应返回 404")
    @WithSecurityPrincipal(userId = 100L)
    void getQuestionDetail_whenNotFound_shouldReturn404() throws Exception {
        when(assessmentQuestionAppService.getQuestionDetailForUser(99L))
                .thenThrow(new DataNotFound("题目不存在"));

        mockMvc.perform(get("/api/v1/assessment-questions/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("listQuestions: 缺少考核时间ID参数应返回 400")
    @WithSecurityPrincipal(userId = 100L)
    void listQuestions_withoutAssessmentTimeId_shouldReturn400() throws Exception {
        mockMvc.perform(get("/api/v1/assessment-questions"))
                .andExpect(status().isBadRequest());
    }
}
