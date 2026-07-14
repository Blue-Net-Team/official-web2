package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.converter.qrcode.QrcodeRequestConverter;
import com.bluenet.web.api.converter.qrcode.QrcodeResponseConverter;
import com.bluenet.web.api.dto.qrcode.AssessmentQrcodeDTO;
import com.bluenet.web.api.dto.qrcode.CreateAssessmentQrcodeRequestDTO;
import com.bluenet.web.api.dto.qrcode.UpdateAssessmentQrcodeRequestDTO;
import com.bluenet.web.application.command.qrcode.QrcodeCommands;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
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
@DisplayName("AdminAssessmentQrcodeController 集成测试")
class AdminAssessmentQrcodeControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private QrcodeAppService qrcodeAppService;

    @MockitoBean
    private QrcodeResponseConverter qrcodeResponseConverter;

    @Autowired
    private QrcodeRequestConverter qrcodeRequestConverter;

    @AfterEach
    void tearDown() {
        UserCTX.clear();
    }

    private static final long SUPER_ADMIN_USER_ID = 9999L;

    @Test
    @DisplayName("getAssessmentQrcodes: 普通成员访问应返回 403")
    @WithSecurityPrincipal(roleType = "MEMBER", roleId = 3L, permissions = {})
    void getAssessmentQrcodes_asMember_shouldReturn403() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/admin/qrcodes/assessment"))
                .andExpect(status().isForbidden())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(403);
    }

    @Test
    @DisplayName("getAssessmentQrcodes: 超级管理员应返回考核群二维码列表")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "admin:qrcode:assessment:read" })
    void getAssessmentQrcodes_asSuperAdmin_shouldReturnQrcodes() throws Exception {
        QrcodeResult result = new QrcodeResult(1L, 10L, "COMPUTER_VISION", 1, false);
        AssessmentQrcodeDTO dto = AssessmentQrcodeDTO.builder()
                .id(1L)
                .fileId(10L)
                .direction("COMPUTER_VISION")
                .epoch(1)
                .isShared(false)
                .build();
        when(qrcodeAppService.getAssessmentQrcodes(null, null)).thenReturn(List.of(result));
        when(qrcodeResponseConverter.toAssessmentDTOList(any())).thenReturn(List.of(dto));

        MvcResult mvcResult = mockMvc.perform(get("/api/v1/admin/qrcodes/assessment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("createAssessmentQrcode: 超级管理员应成功创建考核群二维码")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "admin:qrcode:assessment:create" })
    void createAssessmentQrcode_asSuperAdmin_shouldReturnOk() throws Exception {
        doNothing().when(qrcodeAppService)
                .createAssessmentQrcode(any(QrcodeCommands.CreateAssessmentQrcodeCommand.class));

        CreateAssessmentQrcodeRequestDTO request = CreateAssessmentQrcodeRequestDTO.builder()
                .fileId(10L)
                .epoch(1)
                .isShared(false)
                .build();

        MvcResult result = mockMvc.perform(
                post("/api/v1/admin/qrcodes/assessment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("createAssessmentQrcode: 缺少文件ID应返回 400")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "admin:qrcode:assessment:create" })
    void createAssessmentQrcode_withMissingFileId_shouldReturn400() throws Exception {
        CreateAssessmentQrcodeRequestDTO request = CreateAssessmentQrcodeRequestDTO.builder()
                .epoch(1)
                .build();

        MvcResult result = mockMvc.perform(
                post("/api/v1/admin/qrcodes/assessment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("updateAssessmentQrcode: 超级管理员应成功更新考核群二维码")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "admin:qrcode:assessment:update" })
    void updateAssessmentQrcode_asSuperAdmin_shouldReturnOk() throws Exception {
        doNothing().when(qrcodeAppService)
                .updateAssessmentQrcode(any(QrcodeCommands.UpdateAssessmentQrcodeCommand.class));

        UpdateAssessmentQrcodeRequestDTO request = UpdateAssessmentQrcodeRequestDTO.builder()
                .fileId(11L)
                .epoch(2)
                .build();

        MvcResult result = mockMvc.perform(
                put("/api/v1/admin/qrcodes/assessment/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("updateAssessmentQrcode: 二维码不存在时应返回 404")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "admin:qrcode:assessment:update" })
    void updateAssessmentQrcode_whenNotFound_shouldReturn404() throws Exception {
        doThrow(new DataNotFound("二维码不存在"))
                .when(qrcodeAppService)
                .updateAssessmentQrcode(any(QrcodeCommands.UpdateAssessmentQrcodeCommand.class));

        UpdateAssessmentQrcodeRequestDTO request = UpdateAssessmentQrcodeRequestDTO.builder()
                .fileId(11L)
                .build();

        MvcResult result = mockMvc.perform(
                put("/api/v1/admin/qrcodes/assessment/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("deleteAssessmentQrcode: 超级管理员应成功删除考核群二维码")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "admin:qrcode:assessment:delete" })
    void deleteAssessmentQrcode_asSuperAdmin_shouldReturnOk() throws Exception {
        doNothing().when(qrcodeAppService)
                .deleteAssessmentQrcode(any(QrcodeCommands.DeleteAssessmentQrcodeCommand.class));

        MvcResult result = mockMvc.perform(delete("/api/v1/admin/qrcodes/assessment/{id}", 1L))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }
}
