package com.bluenet.web.api.controller.v1.user;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.experience.CreateExperienceRequestDTO;
import com.bluenet.web.api.dto.experience.ExperienceDTO;
import com.bluenet.web.api.dto.experience.UpdateExperienceRequestDTO;
import com.bluenet.web.api.converter.userexperience.UserExperienceResponseConverter;
import com.bluenet.web.application.result.user.UserExperienceResult;
import com.bluenet.web.application.service.UserExperienceAppService;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.enumerate.ExperienceType;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
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
@DisplayName("UserExperienceController 集成测试")
class UserExperienceControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserExperienceAppService userExperienceAppService;

    @MockitoBean
    private UserExperienceResponseConverter responseConverter;

    @AfterEach
    void tearDown() {
        UserCTX.clear();
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleId = 3L, roleType = "MEMBER")
    void getExperiences_authenticated_returnsList() throws Exception {
        UserExperienceResult result = new UserExperienceResult(
                1L,
                ExperienceType.PROJECT,
                "项目经历",
                "2024.09",
                "2025.06",
                null);
        ExperienceDTO dto = ExperienceDTO.builder()
                .id("1")
                .type("PROJECT")
                .name("项目经历")
                .build();
        when(userExperienceAppService.getExperiences(null)).thenReturn(List.of(result));
        when(responseConverter.toDTOList(any())).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/user/experiences"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("项目经历"));
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleId = 3L, roleType = "MEMBER")
    void getExperiences_withTypeFilter_returnsFiltered() throws Exception {
        UserExperienceResult result = new UserExperienceResult(
                1L,
                ExperienceType.PROJECT,
                "项目经历",
                "2024.09",
                "2025.06",
                null);
        ExperienceDTO dto = ExperienceDTO.builder()
                .id("1")
                .type("PROJECT")
                .name("项目经历")
                .build();
        when(userExperienceAppService.getExperiences("PROJECT")).thenReturn(List.of(result));
        when(responseConverter.toDTOList(any())).thenReturn(List.of(dto));

        mockMvc.perform(
                get("/api/v1/user/experiences")
                        .param("type", "PROJECT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].type").value("PROJECT"));
    }

    @Test
    void getExperiences_anonymous_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/user/experiences"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleId = 3L, roleType = "MEMBER", permissions = { "user:experience:create" })
    void createExperience_authenticatedWithPermission_returnsCreated() throws Exception {
        UserExperienceResult result = new UserExperienceResult(
                1L,
                ExperienceType.PROJECT,
                "BlueNet",
                "2024.09",
                "2025.06",
                null);
        ExperienceDTO dto = ExperienceDTO.builder()
                .id("1")
                .type("PROJECT")
                .name("BlueNet")
                .build();
        when(userExperienceAppService.createExperience(any())).thenReturn(result);
        when(responseConverter.toDTO(any(UserExperienceResult.class))).thenReturn(dto);

        CreateExperienceRequestDTO request = new CreateExperienceRequestDTO();
        request.setType("PROJECT");
        request.setName("BlueNet");
        request.setRole("后端开发");
        request.setStartDate("2024.09");
        request.setEndDate("2025.06");
        request.setDescription("团队官网项目");
        request.setTechStack(List.of("Java", "Spring Boot"));
        request.setDemoUrl("https://example.com");

        mockMvc.perform(
                post("/api/v1/user/experiences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("BlueNet"))
                .andExpect(jsonPath("$.data.type").value("PROJECT"));
    }

    @Test
    void createExperience_anonymous_returnsUnauthorized() throws Exception {
        CreateExperienceRequestDTO request = new CreateExperienceRequestDTO();
        request.setType("PROJECT");
        request.setName("BlueNet");

        mockMvc.perform(
                post("/api/v1/user/experiences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleId = 3L, roleType = "MEMBER")
    void createExperience_authenticatedWithoutPermission_returnsForbidden() throws Exception {
        CreateExperienceRequestDTO request = new CreateExperienceRequestDTO();
        request.setType("PROJECT");
        request.setName("BlueNet");

        mockMvc.perform(
                post("/api/v1/user/experiences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleId = 3L, roleType = "MEMBER", permissions = { "user:experience:update" })
    void updateExperience_authenticatedWithPermission_returnsUpdated() throws Exception {
        UserExperienceResult result = new UserExperienceResult(
                1L,
                ExperienceType.PROJECT,
                "BlueNet v2",
                "2024.09",
                "2025.06",
                null);
        ExperienceDTO dto = ExperienceDTO.builder()
                .id("1")
                .type("PROJECT")
                .name("BlueNet v2")
                .build();
        when(userExperienceAppService.updateExperience(any())).thenReturn(result);
        when(responseConverter.toDTO(any(UserExperienceResult.class))).thenReturn(dto);

        UpdateExperienceRequestDTO request = new UpdateExperienceRequestDTO();
        request.setName("BlueNet v2");

        mockMvc.perform(
                put("/api/v1/user/experiences/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("BlueNet v2"));
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleId = 3L, roleType = "MEMBER", permissions = { "user:experience:update" })
    void updateExperience_nonExistent_returnsNotFound() throws Exception {
        when(userExperienceAppService.updateExperience(any()))
                .thenThrow(new DataNotFound("经历不存在"));

        UpdateExperienceRequestDTO request = new UpdateExperienceRequestDTO();
        request.setName("不存在");

        mockMvc.perform(
                put("/api/v1/user/experiences/{id}", 9999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void updateExperience_anonymous_returnsUnauthorized() throws Exception {
        UpdateExperienceRequestDTO request = new UpdateExperienceRequestDTO();
        request.setName("BlueNet");

        mockMvc.perform(
                put("/api/v1/user/experiences/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleId = 3L, roleType = "MEMBER")
    void updateExperience_authenticatedWithoutPermission_returnsForbidden() throws Exception {
        UpdateExperienceRequestDTO request = new UpdateExperienceRequestDTO();
        request.setName("BlueNet");

        mockMvc.perform(
                put("/api/v1/user/experiences/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleId = 3L, roleType = "MEMBER", permissions = { "user:experience:delete" })
    void deleteExperience_authenticatedWithPermission_returnsOk() throws Exception {
        mockMvc.perform(delete("/api/v1/user/experiences/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleId = 3L, roleType = "MEMBER", permissions = { "user:experience:delete" })
    void deleteExperience_nonExistent_returnsNotFound() throws Exception {
        doThrow(new DataNotFound("经历不存在"))
                .when(userExperienceAppService)
                .deleteExperience(9999L);

        mockMvc.perform(delete("/api/v1/user/experiences/{id}", 9999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void deleteExperience_anonymous_returnsUnauthorized() throws Exception {
        mockMvc.perform(delete("/api/v1/user/experiences/{id}", 1L))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleId = 3L, roleType = "MEMBER")
    void deleteExperience_authenticatedWithoutPermission_returnsForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/user/experiences/{id}", 1L))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }
}
