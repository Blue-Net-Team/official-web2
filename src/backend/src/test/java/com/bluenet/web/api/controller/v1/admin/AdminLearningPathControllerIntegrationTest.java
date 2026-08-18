package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.learningpath.CreateLearningStepRequestDTO;
import com.bluenet.web.api.dto.learningpath.LearningStepDTO;
import com.bluenet.web.api.dto.learningpath.UpdateLearningStepRequestDTO;
import com.bluenet.web.api.converter.learningpath.LearningPathResponseConverter;
import com.bluenet.web.application.result.learningpath.LearningPathResult;
import com.bluenet.web.application.service.LearningPathAppService;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.enumerate.Direction;
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
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@DisplayName("AdminLearningPathController 集成测试")
class AdminLearningPathControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LearningPathAppService learningPathAppService;

    @MockitoBean
    private LearningPathResponseConverter learningPathResponseConverter;

    @AfterEach
    void tearDown() {
        UserCTX.clear();
    }

    private static final long SUPER_ADMIN_USER_ID = 9999L;

    private LearningPathResult stubResult() {
        return new LearningPathResult(
                1L,
                Direction.COMPUTER_VISION,
                1,
                "Python基础",
                "https://example.com/video.mp4");
    }

    private LearningStepDTO stubDTO() {
        return LearningStepDTO.builder()
                .id(1L)
                .stepNumber(1)
                .title("Python基础")
                .build();
    }

    @Test
    @DisplayName("createStep: 超级管理员应成功创建学习步骤")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "direction-learning-path:create" })
    void createStep_asSuperAdmin_shouldReturnCreatedStep() throws Exception {
        LearningPathResult result = stubResult();
        LearningStepDTO dto = stubDTO();
        when(learningPathAppService.createStep(any())).thenReturn(result);
        when(learningPathResponseConverter.toDTO(any(LearningPathResult.class))).thenReturn(dto);

        CreateLearningStepRequestDTO request = CreateLearningStepRequestDTO.builder()
                .stepNumber(1)
                .title("Python基础")
                .relatedLink("https://example.com/video.mp4")
                .build();

        MvcResult mvcResult = mockMvc.perform(
                post("/api/v1/admin/directions/{slug}/learning-steps", "cv")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("Python基础"))
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("createStep: 步骤标题为空应返回 400")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "direction-learning-path:create" })
    void createStep_withBlankTitle_shouldReturn400() throws Exception {
        CreateLearningStepRequestDTO request = CreateLearningStepRequestDTO.builder()
                .stepNumber(1)
                .title("")
                .build();

        MvcResult mvcResult = mockMvc.perform(
                post("/api/v1/admin/directions/{slug}/learning-steps", "cv")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("createStep: 普通成员访问应返回 403")
    @WithSecurityPrincipal(roleType = "MEMBER", roleId = 3L, permissions = {})
    void createStep_asMember_shouldReturn403() throws Exception {
        CreateLearningStepRequestDTO request = CreateLearningStepRequestDTO.builder()
                .stepNumber(1)
                .title("Python基础")
                .build();

        mockMvc.perform(
                post("/api/v1/admin/directions/{slug}/learning-steps", "cv")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("updateStep: 超级管理员应成功更新学习步骤")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "direction-learning-path:update" })
    void updateStep_asSuperAdmin_shouldReturnUpdatedStep() throws Exception {
        LearningPathResult result = stubResult();
        LearningStepDTO dto = stubDTO();
        when(learningPathAppService.updateStep(any())).thenReturn(result);
        when(learningPathResponseConverter.toDTO(any(LearningPathResult.class))).thenReturn(dto);

        UpdateLearningStepRequestDTO request = UpdateLearningStepRequestDTO.builder()
                .stepNumber(1)
                .title("Python基础")
                .relatedLink("https://example.com/video.mp4")
                .build();

        MvcResult mvcResult = mockMvc.perform(
                put("/api/v1/admin/directions/learning-steps/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("updateStep: 学习步骤不存在应返回 404")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "direction-learning-path:update" })
    void updateStep_notFound_shouldReturn404() throws Exception {
        when(learningPathAppService.updateStep(any())).thenThrow(new DataNotFound("学习步骤不存在"));

        UpdateLearningStepRequestDTO request = UpdateLearningStepRequestDTO.builder()
                .stepNumber(1)
                .title("Python基础")
                .build();

        MvcResult mvcResult = mockMvc.perform(
                put("/api/v1/admin/directions/learning-steps/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("deleteStep: 超级管理员应成功删除学习步骤")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "direction-learning-path:delete" })
    void deleteStep_asSuperAdmin_shouldReturnOk() throws Exception {
        doNothing().when(learningPathAppService).deleteStep(1L);

        MvcResult mvcResult = mockMvc.perform(delete("/api/v1/admin/directions/learning-steps/{id}", 1L))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
    }
}
