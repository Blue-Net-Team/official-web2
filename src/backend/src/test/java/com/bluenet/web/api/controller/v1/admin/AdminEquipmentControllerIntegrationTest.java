package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.equipment.CreateEquipmentRequestDTO;
import com.bluenet.web.api.dto.equipment.EquipmentDTO;
import com.bluenet.web.api.dto.equipment.UpdateEquipmentRequestDTO;
import com.bluenet.web.api.converter.equipment.EquipmentResponseConverter;
import com.bluenet.web.application.result.equipment.EquipmentResult;
import com.bluenet.web.application.service.EquipmentAppService;
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
@DisplayName("AdminEquipmentController 集成测试")
class AdminEquipmentControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EquipmentAppService equipmentAppService;

    @MockitoBean
    private EquipmentResponseConverter equipmentResponseConverter;

    @AfterEach
    void tearDown() {
        UserCTX.clear();
    }

    private static final long SUPER_ADMIN_USER_ID = 9999L;

    private EquipmentResult stubResult() {
        return new EquipmentResult(
                1L,
                "3D打印机",
                "泰尔时代",
                "高精度FDM 3D打印设备",
                "https://example.com/3dprinter.jpg",
                100L,
                10);
    }

    private EquipmentDTO stubDTO() {
        return EquipmentDTO.builder()
                .id(1L)
                .name("3D打印机")
                .brand("泰尔时代")
                .build();
    }

    @Test
    @DisplayName("createEquipment: 超级管理员应成功创建设备")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "equipment:create" })
    void createEquipment_asSuperAdmin_shouldReturnCreatedEquipment() throws Exception {
        EquipmentResult result = stubResult();
        EquipmentDTO dto = stubDTO();
        when(equipmentAppService.createEquipment(any())).thenReturn(result);
        when(equipmentResponseConverter.toDTO(any(EquipmentResult.class))).thenReturn(dto);

        CreateEquipmentRequestDTO request = CreateEquipmentRequestDTO.builder()
                .name("3D打印机")
                .brand("泰尔时代")
                .description("高精度FDM 3D打印设备")
                .imageFileId(100L)
                .sortOrder(10)
                .build();

        MvcResult mvcResult = mockMvc.perform(
                post("/api/v1/admin/equipments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("3D打印机"))
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("createEquipment: 设备名称为空应返回 400")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "equipment:create" })
    void createEquipment_withBlankName_shouldReturn400() throws Exception {
        CreateEquipmentRequestDTO request = CreateEquipmentRequestDTO.builder()
                .name("")
                .build();

        MvcResult mvcResult = mockMvc.perform(
                post("/api/v1/admin/equipments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("createEquipment: 普通成员访问应返回 403")
    @WithSecurityPrincipal(roleType = "MEMBER", roleId = 3L, permissions = {})
    void createEquipment_asMember_shouldReturn403() throws Exception {
        CreateEquipmentRequestDTO request = CreateEquipmentRequestDTO.builder()
                .name("测试设备")
                .build();

        mockMvc.perform(
                post("/api/v1/admin/equipments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("updateEquipment: 超级管理员应成功更新设备")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "equipment:update" })
    void updateEquipment_asSuperAdmin_shouldReturnUpdatedEquipment() throws Exception {
        EquipmentResult result = stubResult();
        EquipmentDTO dto = stubDTO();
        when(equipmentAppService.updateEquipment(any())).thenReturn(result);
        when(equipmentResponseConverter.toDTO(any(EquipmentResult.class))).thenReturn(dto);

        UpdateEquipmentRequestDTO request = UpdateEquipmentRequestDTO.builder()
                .name("3D打印机")
                .brand("泰尔时代")
                .description("高精度FDM 3D打印设备")
                .imageFileId(100L)
                .sortOrder(10)
                .build();

        MvcResult mvcResult = mockMvc.perform(
                put("/api/v1/admin/equipments/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("updateEquipment: 设备不存在应返回 404")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "equipment:update" })
    void updateEquipment_notFound_shouldReturn404() throws Exception {
        when(equipmentAppService.updateEquipment(any())).thenThrow(new DataNotFound("设备不存在"));

        UpdateEquipmentRequestDTO request = UpdateEquipmentRequestDTO.builder()
                .name("3D打印机")
                .build();

        MvcResult mvcResult = mockMvc.perform(
                put("/api/v1/admin/equipments/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("deleteEquipment: 超级管理员应成功删除设备")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "equipment:delete" })
    void deleteEquipment_asSuperAdmin_shouldReturnOk() throws Exception {
        doNothing().when(equipmentAppService).deleteEquipment(1L);

        MvcResult mvcResult = mockMvc.perform(delete("/api/v1/admin/equipments/{id}", 1L))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("updateEquipmentImage: 超级管理员应成功更新设备图片")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "equipment:update-image" })
    void updateEquipmentImage_asSuperAdmin_shouldReturnOk() throws Exception {
        doNothing().when(equipmentAppService).updateEquipmentImage(1L, 200L);

        MvcResult mvcResult = mockMvc.perform(
                put("/api/v1/admin/equipments/{id}/image", 1L)
                        .param("imageFileId", "200"))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("updateEquipmentImage: 缺少图片文件ID应返回 400")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "equipment:update-image" })
    void updateEquipmentImage_missingFileId_shouldReturn400() throws Exception {
        MvcResult mvcResult = mockMvc.perform(put("/api/v1/admin/equipments/{id}/image", 1L))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(400);
    }
}
