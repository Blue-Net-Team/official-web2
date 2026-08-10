package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.college.CollegeDTO;
import com.bluenet.web.api.dto.college.CreateCollegeRequestDTO;
import com.bluenet.web.api.dto.college.UpdateCollegeRequestDTO;
import com.bluenet.web.api.converter.college.CollegeResponseConverter;
import com.bluenet.web.application.result.college.CollegeResult;
import com.bluenet.web.application.service.CollegeAppService;
import com.bluenet.web.domain.exception.DataNotFound;
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
@DisplayName("AdminCollegeController 集成测试")
class AdminCollegeControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CollegeAppService collegeAppService;

    @MockitoBean
    private CollegeResponseConverter collegeResponseConverter;

    @AfterEach
    void tearDown() {
        UserCTX.clear();
    }

    private static final long SUPER_ADMIN_USER_ID = 9999L;

    private CollegeResult stubResult() {
        return new CollegeResult(1L, "计算机科学与技术学院");
    }

    private CollegeDTO stubDTO() {
        return CollegeDTO.builder()
                .id(1L)
                .name("计算机科学与技术学院")
                .build();
    }

    @Test
    @DisplayName("createCollege: 超级管理员应成功创建学院")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "college:create" })
    void createCollege_asSuperAdmin_shouldReturnCreatedCollege() throws Exception {
        CollegeResult result = stubResult();
        CollegeDTO dto = stubDTO();
        when(collegeAppService.createCollege(any())).thenReturn(result);
        when(collegeResponseConverter.toDTO(any(CollegeResult.class))).thenReturn(dto);

        CreateCollegeRequestDTO request = CreateCollegeRequestDTO.builder()
                .name("计算机科学与技术学院")
                .build();

        MvcResult mvcResult = mockMvc.perform(
                post("/api/v1/admin/colleges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("计算机科学与技术学院"))
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("createCollege: 学院名称为空应返回 400")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "college:create" })
    void createCollege_withBlankName_shouldReturn400() throws Exception {
        CreateCollegeRequestDTO request = CreateCollegeRequestDTO.builder()
                .name("")
                .build();

        MvcResult mvcResult = mockMvc.perform(
                post("/api/v1/admin/colleges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("createCollege: 普通成员访问应返回 403")
    @WithSecurityPrincipal(roleType = "MEMBER", roleId = 3L, permissions = {})
    void createCollege_asMember_shouldReturn403() throws Exception {
        CreateCollegeRequestDTO request = CreateCollegeRequestDTO.builder()
                .name("测试学院")
                .build();

        mockMvc.perform(
                post("/api/v1/admin/colleges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("updateCollege: 超级管理员应成功更新学院")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "college:update" })
    void updateCollege_asSuperAdmin_shouldReturnUpdatedCollege() throws Exception {
        CollegeResult result = stubResult();
        CollegeDTO dto = stubDTO();
        when(collegeAppService.updateCollege(any())).thenReturn(result);
        when(collegeResponseConverter.toDTO(any(CollegeResult.class))).thenReturn(dto);

        UpdateCollegeRequestDTO request = UpdateCollegeRequestDTO.builder()
                .name("计算机科学与技术学院")
                .build();

        MvcResult mvcResult = mockMvc.perform(
                put("/api/v1/admin/colleges/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("updateCollege: 学院不存在应返回 404")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "college:update" })
    void updateCollege_notFound_shouldReturn404() throws Exception {
        when(collegeAppService.updateCollege(any())).thenThrow(new DataNotFound("学院不存在"));

        UpdateCollegeRequestDTO request = UpdateCollegeRequestDTO.builder()
                .name("计算机科学与技术学院")
                .build();

        MvcResult mvcResult = mockMvc.perform(
                put("/api/v1/admin/colleges/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("deleteCollege: 超级管理员应成功删除学院")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "college:delete" })
    void deleteCollege_asSuperAdmin_shouldReturnOk() throws Exception {
        doNothing().when(collegeAppService).deleteCollege(1L);

        MvcResult mvcResult = mockMvc.perform(delete("/api/v1/admin/colleges/{id}", 1L))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
    }
}
