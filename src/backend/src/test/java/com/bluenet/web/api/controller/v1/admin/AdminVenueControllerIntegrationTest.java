package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.venue.CreateVenueRequestDTO;
import com.bluenet.web.api.dto.venue.UpdateVenueRequestDTO;
import com.bluenet.web.api.dto.venue.VenueDTO;
import com.bluenet.web.api.converter.venue.VenueResponseConverter;
import com.bluenet.web.application.result.venue.VenueResult;
import com.bluenet.web.application.service.VenueAppService;
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
@DisplayName("AdminVenueController 集成测试")
class AdminVenueControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private VenueAppService venueAppService;

    @MockitoBean
    private VenueResponseConverter venueResponseConverter;

    @AfterEach
    void tearDown() {
        UserCTX.clear();
    }

    private static final long SUPER_ADMIN_USER_ID = 9999L;

    private VenueResult stubResult() {
        return new VenueResult(
                1L,
                "办公区域",
                "团队协作空间",
                "宽敞明亮的办公区域",
                "https://example.com/venue.jpg",
                100L,
                10);
    }

    private VenueDTO stubDTO() {
        return VenueDTO.builder()
                .id(1L)
                .name("办公区域")
                .subtitle("团队协作空间")
                .build();
    }

    @Test
    @DisplayName("createVenue: 超级管理员应成功创建场地")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "venue:create" })
    void createVenue_asSuperAdmin_shouldReturnCreatedVenue() throws Exception {
        VenueResult result = stubResult();
        VenueDTO dto = stubDTO();
        when(venueAppService.createVenue(any())).thenReturn(result);
        when(venueResponseConverter.toDTO(any(VenueResult.class))).thenReturn(dto);

        CreateVenueRequestDTO request = CreateVenueRequestDTO.builder()
                .name("办公区域")
                .subtitle("团队协作空间")
                .description("宽敞明亮的办公区域")
                .imageFileId(100L)
                .sortOrder(10)
                .build();

        MvcResult mvcResult = mockMvc.perform(
                post("/api/v1/admin/venues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("办公区域"))
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("createVenue: 场地名称为空应返回 400")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "venue:create" })
    void createVenue_withBlankName_shouldReturn400() throws Exception {
        CreateVenueRequestDTO request = CreateVenueRequestDTO.builder()
                .name("")
                .build();

        MvcResult mvcResult = mockMvc.perform(
                post("/api/v1/admin/venues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("createVenue: 普通成员访问应返回 403")
    @WithSecurityPrincipal(roleType = "MEMBER", roleId = 3L, permissions = {})
    void createVenue_asMember_shouldReturn403() throws Exception {
        CreateVenueRequestDTO request = CreateVenueRequestDTO.builder()
                .name("测试场地")
                .build();

        mockMvc.perform(
                post("/api/v1/admin/venues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("updateVenue: 超级管理员应成功更新场地")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "venue:update" })
    void updateVenue_asSuperAdmin_shouldReturnUpdatedVenue() throws Exception {
        VenueResult result = stubResult();
        VenueDTO dto = stubDTO();
        when(venueAppService.updateVenue(any())).thenReturn(result);
        when(venueResponseConverter.toDTO(any(VenueResult.class))).thenReturn(dto);

        UpdateVenueRequestDTO request = UpdateVenueRequestDTO.builder()
                .name("办公区域")
                .subtitle("团队协作空间")
                .description("宽敞明亮的办公区域")
                .imageFileId(100L)
                .sortOrder(10)
                .build();

        MvcResult mvcResult = mockMvc.perform(
                put("/api/v1/admin/venues/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("updateVenue: 场地不存在应返回 404")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "venue:update" })
    void updateVenue_notFound_shouldReturn404() throws Exception {
        when(venueAppService.updateVenue(any())).thenThrow(new DataNotFound("场地不存在"));

        UpdateVenueRequestDTO request = UpdateVenueRequestDTO.builder()
                .name("办公区域")
                .build();

        MvcResult mvcResult = mockMvc.perform(
                put("/api/v1/admin/venues/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("deleteVenue: 超级管理员应成功删除场地")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "venue:delete" })
    void deleteVenue_asSuperAdmin_shouldReturnOk() throws Exception {
        doNothing().when(venueAppService).deleteVenue(1L);

        MvcResult mvcResult = mockMvc.perform(delete("/api/v1/admin/venues/{id}", 1L))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("updateVenueImage: 超级管理员应成功更新场地图片")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "venue:update-image" })
    void updateVenueImage_asSuperAdmin_shouldReturnOk() throws Exception {
        doNothing().when(venueAppService).updateVenueImage(1L, 200L);

        MvcResult mvcResult = mockMvc.perform(
                put("/api/v1/admin/venues/{id}/image", 1L)
                        .param("imageFileId", "200"))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("updateVenueImage: 缺少图片文件ID应返回 400")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "venue:update-image" })
    void updateVenueImage_missingFileId_shouldReturn400() throws Exception {
        MvcResult mvcResult = mockMvc.perform(put("/api/v1/admin/venues/{id}/image", 1L))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(400);
    }
}
