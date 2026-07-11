package com.bluenet.web.api.controller.v1.user;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.experience.CreateExperienceRequestDTO;
import com.bluenet.web.api.dto.experience.UpdateExperienceRequestDTO;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.entity.UserExperience;
import com.bluenet.web.domain.model.enumerate.ExperienceType;
import com.bluenet.web.domain.repository.UserExperienceRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.infrastructure.security.principal.WithSecurityPrincipal;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.bluenet.web.testconfig.TestSecurityConfig;
import com.bluenet.web.testsupport.fixture.UserFixture;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserExperienceRepository userExperienceRepository;

    @AfterEach
    void tearDown() {
        UserCTX.clear();
    }

    private User saveMemberUser() {
        return UserFixture.member("2024001001").save(userRepository, passwordEncoder);
    }

    @Test
    @WithSecurityPrincipal(roleId = 3L, roleType = "MEMBER")
    void getExperiences_authenticated_returnsList() throws Exception {
        User user = saveMemberUser();
        UserExperience project = UserExperience.create(
                user.getId(),
                ExperienceType.PROJECT,
                "项目经历",
                "{}",
                null,
                null);
        userExperienceRepository.save(project);

        mockMvc.perform(get("/api/v1/user/experiences"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("项目经历"));
    }

    @Test
    @WithSecurityPrincipal(roleId = 3L, roleType = "MEMBER")
    void getExperiences_withTypeFilter_returnsFiltered() throws Exception {
        User user = saveMemberUser();
        UserExperience project = UserExperience.create(
                user.getId(),
                ExperienceType.PROJECT,
                "项目经历",
                "{}",
                null,
                null);
        UserExperience competition = UserExperience.create(
                user.getId(),
                ExperienceType.COMPETITION,
                "竞赛经历",
                "{}",
                null,
                null);
        userExperienceRepository.save(project);
        userExperienceRepository.save(competition);

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
    @WithSecurityPrincipal(roleId = 3L, roleType = "MEMBER", permissions = { "user:experience:create" })
    void createExperience_authenticatedWithPermission_returnsCreated() throws Exception {
        saveMemberUser();

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
    @WithSecurityPrincipal(roleId = 3L, roleType = "MEMBER")
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
    @WithSecurityPrincipal(roleId = 3L, roleType = "MEMBER", permissions = { "user:experience:update" })
    void updateExperience_authenticatedWithPermission_returnsUpdated() throws Exception {
        User user = saveMemberUser();
        UserExperience project = UserExperience.create(
                user.getId(),
                ExperienceType.PROJECT,
                "BlueNet",
                "{}",
                null,
                null);
        userExperienceRepository.save(project);

        UpdateExperienceRequestDTO request = new UpdateExperienceRequestDTO();
        request.setName("BlueNet v2");

        mockMvc.perform(
                put("/api/v1/user/experiences/{id}", project.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("BlueNet v2"));
    }

    @Test
    @WithSecurityPrincipal(roleId = 3L, roleType = "MEMBER", permissions = { "user:experience:update" })
    void updateExperience_nonExistent_returnsNotFound() throws Exception {
        saveMemberUser();

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
    @WithSecurityPrincipal(roleId = 3L, roleType = "MEMBER")
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
    @WithSecurityPrincipal(roleId = 3L, roleType = "MEMBER", permissions = { "user:experience:delete" })
    void deleteExperience_authenticatedWithPermission_returnsOk() throws Exception {
        User user = saveMemberUser();
        UserExperience project = UserExperience.create(
                user.getId(),
                ExperienceType.PROJECT,
                "BlueNet",
                "{}",
                null,
                null);
        userExperienceRepository.save(project);

        mockMvc.perform(delete("/api/v1/user/experiences/{id}", project.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithSecurityPrincipal(roleId = 3L, roleType = "MEMBER", permissions = { "user:experience:delete" })
    void deleteExperience_nonExistent_returnsNotFound() throws Exception {
        saveMemberUser();

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
    @WithSecurityPrincipal(roleId = 3L, roleType = "MEMBER")
    void deleteExperience_authenticatedWithoutPermission_returnsForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/user/experiences/{id}", 1L))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }
}
