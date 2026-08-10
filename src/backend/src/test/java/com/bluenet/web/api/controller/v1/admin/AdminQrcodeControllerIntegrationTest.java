package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.qrcode.ConsultationQrcodeDTO;
import com.bluenet.web.api.dto.qrcode.UpdateConsultationQrcodeRequestDTO;
import com.bluenet.web.api.converter.qrcode.QrcodeResponseConverter;
import com.bluenet.web.application.result.qrcode.QrcodeResult;
import com.bluenet.web.application.service.QrcodeAppService;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
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
@DisplayName("AdminQrcodeController 集成测试")
class AdminQrcodeControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private QrcodeAppService qrcodeAppService;

    @MockitoBean
    private QrcodeResponseConverter qrcodeResponseConverter;

    @AfterEach
    void tearDown() {
        UserCTX.clear();
    }

    private static final long SUPER_ADMIN_USER_ID = 9999L;

    @Test
    @DisplayName("getConsultationQrcodes: 超级管理员应返回咨询群二维码列表")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "admin:qrcode:consultation:read" })
    void getConsultationQrcodes_asSuperAdmin_shouldReturnQrcodeList() throws Exception {
        QrcodeResult result = new QrcodeResult(1L, 100L);
        ConsultationQrcodeDTO dto = ConsultationQrcodeDTO.builder()
                .id(1L)
                .fileId(100L)
                .build();
        when(qrcodeAppService.getConsultationQrcodes()).thenReturn(List.of(result));
        when(qrcodeResponseConverter.toConsultationDTOList(any())).thenReturn(List.of(dto));

        MvcResult mvcResult = mockMvc.perform(get("/api/v1/admin/qrcodes/consultation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("getConsultationQrcodes: 普通成员访问应返回 403")
    @WithSecurityPrincipal(roleType = "MEMBER", roleId = 3L, permissions = {})
    void getConsultationQrcodes_asMember_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/v1/admin/qrcodes/consultation"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("createConsultationQrcode: 超级管理员应成功创建咨询群二维码")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "admin:qrcode:consultation:create" })
    void createConsultationQrcode_asSuperAdmin_shouldReturnOk() throws Exception {
        doNothing().when(qrcodeAppService).createConsultationQrcode(any());

        MvcResult mvcResult = mockMvc.perform(
                post("/api/v1/admin/qrcodes/consultation")
                        .param("fileId", "100"))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("createConsultationQrcode: 缺少文件ID应返回 400")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "admin:qrcode:consultation:create" })
    void createConsultationQrcode_missingFileId_shouldReturn400() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/api/v1/admin/qrcodes/consultation"))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("updateConsultationQrcode: 超级管理员应成功更新咨询群二维码")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "admin:qrcode:consultation:update" })
    void updateConsultationQrcode_asSuperAdmin_shouldReturnOk() throws Exception {
        doNothing().when(qrcodeAppService).updateConsultationQrcode(any());

        UpdateConsultationQrcodeRequestDTO request = UpdateConsultationQrcodeRequestDTO.builder()
                .fileId(200L)
                .build();

        MvcResult mvcResult = mockMvc.perform(
                put("/api/v1/admin/qrcodes/consultation/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("updateConsultationQrcode: 二维码不存在应返回 404")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "admin:qrcode:consultation:update" })
    void updateConsultationQrcode_notFound_shouldReturn404() throws Exception {
        doThrow(new DataNotFound("二维码不存在")).when(qrcodeAppService).updateConsultationQrcode(any());

        UpdateConsultationQrcodeRequestDTO request = UpdateConsultationQrcodeRequestDTO.builder()
                .fileId(200L)
                .build();

        MvcResult mvcResult = mockMvc.perform(
                put("/api/v1/admin/qrcodes/consultation/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("deleteConsultationQrcode: 超级管理员应成功删除咨询群二维码")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "admin:qrcode:consultation:delete" })
    void deleteConsultationQrcode_asSuperAdmin_shouldReturnOk() throws Exception {
        doNothing().when(qrcodeAppService).deleteConsultationQrcode(any());

        MvcResult mvcResult = mockMvc.perform(delete("/api/v1/admin/qrcodes/consultation/{id}", 1L))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
    }
}
