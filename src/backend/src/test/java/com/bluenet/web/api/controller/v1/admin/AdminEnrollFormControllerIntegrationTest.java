package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.application.service.EnrollFormAppService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.infrastructure.security.principal.WithSecurityPrincipal;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.bluenet.web.testconfig.TestSecurityConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@DisplayName("AdminEnrollFormController 集成测试")
class AdminEnrollFormControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EnrollFormAppService enrollFormAppService;

    @AfterEach
    void tearDown() {
        UserCTX.clear();
    }

    private static final long SUPER_ADMIN_USER_ID = 9999L;

    @Test
    @DisplayName("setEnrollForm: 超级管理员应成功设置报名表")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "admin:enroll-form:update" })
    void setEnrollForm_asSuperAdmin_shouldReturnOk() throws Exception {
        doNothing().when(enrollFormAppService).setEnrollForm(any());

        mockMvc.perform(post("/api/v1/admin/enroll-form").param("fileId", "100"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("setEnrollForm: 缺少文件ID应返回 400")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "admin:enroll-form:update" })
    void setEnrollForm_missingFileId_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/v1/admin/enroll-form"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("setEnrollForm: 文件不存在应返回 404")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "admin:enroll-form:update" })
    void setEnrollForm_fileNotFound_shouldReturn404() throws Exception {
        doThrow(new DataNotFound("文件不存在")).when(enrollFormAppService).setEnrollForm(any());

        mockMvc.perform(post("/api/v1/admin/enroll-form").param("fileId", "100"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("setEnrollForm: 文件类型不匹配应返回 400")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "admin:enroll-form:update" })
    void setEnrollForm_wrongType_shouldReturn400() throws Exception {
        doThrow(new BadRequest("文件类型不匹配，期望 ENROLL_FORM"))
                .when(enrollFormAppService)
                .setEnrollForm(any());

        mockMvc.perform(post("/api/v1/admin/enroll-form").param("fileId", "100"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("setEnrollForm: 普通成员访问应返回 403")
    @WithSecurityPrincipal(roleType = "MEMBER", roleId = 3L, permissions = {})
    void setEnrollForm_asMember_shouldReturn403() throws Exception {
        mockMvc.perform(post("/api/v1/admin/enroll-form").param("fileId", "100"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("deleteEnrollForm: 超级管理员应成功删除报名表")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "admin:enroll-form:delete" })
    void deleteEnrollForm_asSuperAdmin_shouldReturnOk() throws Exception {
        doNothing().when(enrollFormAppService).deleteEnrollForm();

        mockMvc.perform(delete("/api/v1/admin/enroll-form"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("deleteEnrollForm: 无报名表时应返回 404")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "admin:enroll-form:delete" })
    void deleteEnrollForm_noForm_shouldReturn404() throws Exception {
        doThrow(new DataNotFound("当前没有报名表")).when(enrollFormAppService).deleteEnrollForm();

        mockMvc.perform(delete("/api/v1/admin/enroll-form"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("deleteEnrollForm: 普通成员访问应返回 403")
    @WithSecurityPrincipal(roleType = "MEMBER", roleId = 3L, permissions = {})
    void deleteEnrollForm_asMember_shouldReturn403() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/enroll-form"))
                .andExpect(status().isForbidden());
    }
}
