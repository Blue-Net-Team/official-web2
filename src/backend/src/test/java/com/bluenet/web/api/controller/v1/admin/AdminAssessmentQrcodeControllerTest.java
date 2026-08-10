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
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AdminAssessmentQrcodeController 集成测试。
 *
 * <p>
 * 验证管理员考核群二维码管理接口的 HTTP 契约。
 * </p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@DisplayName("AdminAssessmentQrcodeController 集成测试")
class AdminAssessmentQrcodeControllerTest extends BaseIntegrationTest {

    private static final long ADMIN_USER_ID = 9999L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private QrcodeAppService qrcodeAppService;

    @MockitoBean
    private QrcodeRequestConverter requestConverter;

    @MockitoBean
    private QrcodeResponseConverter responseConverter;

    @AfterEach
    void tearDown() {
        UserCTX.clear();
    }

    @Test
    @DisplayName("getAssessmentQrcodes: 应返回二维码列表")
    @WithSecurityPrincipal(userId = ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "admin:qrcode:assessment:read" })
    void getAssessmentQrcodes_shouldReturnList() throws Exception {
        QrcodeResult result = new QrcodeResult(1L, 10L, "COMPUTER_VISION", 1, false);
        AssessmentQrcodeDTO dto = AssessmentQrcodeDTO.builder()
                .id(1L)
                .fileId(10L)
                .direction("COMPUTER_VISION")
                .epoch(1)
                .isShared(false)
                .build();

        when(qrcodeAppService.getAssessmentQrcodes(null, null)).thenReturn(List.of(result));
        when(responseConverter.toAssessmentDTOList(List.of(result))).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/admin/qrcodes/assessment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].fileId").value(10))
                .andExpect(jsonPath("$.data[0].direction").value("COMPUTER_VISION"));
    }

    @Test
    @DisplayName("createAssessmentQrcode: 应创建成功并返回 200")
    @WithSecurityPrincipal(userId = ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "admin:qrcode:assessment:create" })
    void createAssessmentQrcode_shouldReturnSuccess() throws Exception {
        CreateAssessmentQrcodeRequestDTO request = CreateAssessmentQrcodeRequestDTO.builder()
                .fileId(10L)
                .direction(Direction.COMPUTER_VISION)
                .epoch(1)
                .isShared(false)
                .build();
        QrcodeCommands.CreateAssessmentQrcodeCommand command = new QrcodeCommands.CreateAssessmentQrcodeCommand(
                10L, "COMPUTER_VISION", 1, false);

        when(requestConverter.toCreateAssessmentCommand(request)).thenReturn(command);

        mockMvc.perform(
                post("/api/v1/admin/qrcodes/assessment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("createAssessmentQrcode: 参数校验失败应返回 400")
    @WithSecurityPrincipal(userId = ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "admin:qrcode:assessment:create" })
    void createAssessmentQrcode_withInvalidRequest_shouldReturn400() throws Exception {
        CreateAssessmentQrcodeRequestDTO request = CreateAssessmentQrcodeRequestDTO.builder().build();

        mockMvc.perform(
                post("/api/v1/admin/qrcodes/assessment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("updateAssessmentQrcode: 应更新成功并返回 200")
    @WithSecurityPrincipal(userId = ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "admin:qrcode:assessment:update" })
    void updateAssessmentQrcode_shouldReturnSuccess() throws Exception {
        UpdateAssessmentQrcodeRequestDTO request = UpdateAssessmentQrcodeRequestDTO.builder()
                .fileId(20L)
                .direction(Direction.STRUCTURAL_DESIGN)
                .epoch(2)
                .isShared(true)
                .build();
        QrcodeCommands.UpdateAssessmentQrcodeCommand command = new QrcodeCommands.UpdateAssessmentQrcodeCommand(
                1L, 20L, "STRUCTURAL_DESIGN", 2, true);

        when(requestConverter.toUpdateAssessmentCommand(1L, request)).thenReturn(command);

        mockMvc.perform(
                put("/api/v1/admin/qrcodes/assessment/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("deleteAssessmentQrcode: 应删除成功并返回 200")
    @WithSecurityPrincipal(userId = ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "admin:qrcode:assessment:delete" })
    void deleteAssessmentQrcode_shouldReturnSuccess() throws Exception {
        QrcodeCommands.DeleteAssessmentQrcodeCommand command = new QrcodeCommands.DeleteAssessmentQrcodeCommand(1L);

        when(requestConverter.toDeleteAssessmentCommand(1L)).thenReturn(command);

        mockMvc.perform(delete("/api/v1/admin/qrcodes/assessment/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
