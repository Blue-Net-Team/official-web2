package com.bluenet.web.api.controller.v1.assessment;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.assessment_question.AssessmentQuestionDTO;
import com.bluenet.web.api.dto.assessment_question.UserQuestionListResponse;
import com.bluenet.web.api.converter.assessment_question.AssessmentQuestionResponseConverter;
import com.bluenet.web.application.result.assessment.AssessmentQuestionResult;
import com.bluenet.web.application.result.user.UserQuestionListResult;
import com.bluenet.web.application.service.AssessmentQuestionAppService;
import com.bluenet.web.infrastructure.security.principal.WithSecurityPrincipal;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.bluenet.web.testconfig.TestSecurityConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@DisplayName("AssessmentQuestionController 集成测试")
class AssessmentQuestionControllerIntegrationTest extends BaseIntegrationTest {

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
    @DisplayName("listQuestions: 已登录用户应返回考题目录")
    @WithSecurityPrincipal(userId = 1L, roleType = "CANDIDATE", roleId = 4L)
    void listQuestions_authenticated_shouldReturnList() throws Exception {
        UserQuestionListResponse response = UserQuestionListResponse.builder()
                .ended(false)
                .build();
        UserQuestionListResult result = new UserQuestionListResult(null, null, false);
        when(assessmentQuestionAppService.listQuestionsForUser(1L, 0, 10)).thenReturn(result);
        when(responseConverter.toResponse(result)).thenReturn(response);

        mockMvc.perform(
                get("/api/v1/assessment-questions")
                        .param("assessmentTimeId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.ended").value(false));
    }

    @Test
    @DisplayName("listQuestions: 缺少 assessmentTimeId 应返回 400")
    @WithSecurityPrincipal(userId = 1L, roleType = "CANDIDATE", roleId = 4L)
    void listQuestions_missingParam_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/assessment-questions"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("getQuestionDetail: 已登录用户应返回题目详情")
    @WithSecurityPrincipal(userId = 1L, roleType = "CANDIDATE", roleId = 4L)
    void getQuestionDetail_authenticated_shouldReturnDetail() throws Exception {
        AssessmentQuestionDTO dto = AssessmentQuestionDTO.builder()
                .id(1L)
                .title("题目一")
                .build();
        AssessmentQuestionResult result = new AssessmentQuestionResult(
                1L, 1L, 1, null, "题目一", null, null, null, false);
        when(assessmentQuestionAppService.getQuestionDetailForUser(1L)).thenReturn(result);
        when(responseConverter.toDTOForUser(result)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/assessment-questions/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("题目一"));
    }

    @Test
    @DisplayName("getQuestionDetail: 未登录应返回 401")
    void getQuestionDetail_anonymous_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/assessment-questions/{id}", 1L))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }
}
