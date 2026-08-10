package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.permission.RolePermissionBatchRequestDTO;
import com.bluenet.web.application.result.rolepermission.RolePermissionManageResult;
import com.bluenet.web.application.service.RolePermissionManageAppService;
import com.bluenet.web.domain.model.enumerate.RoleType;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@DisplayName("AdminRolePermissionController 集成测试")
class AdminRolePermissionControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RolePermissionManageAppService rolePermissionManageAppService;

    @AfterEach
    void tearDown() {
        UserCTX.clear();
    }

    private static final long SUPER_ADMIN_USER_ID = 9999L;

    @Test
    @DisplayName("getRolePermissions: 超级管理员应返回角色已分配的权限标识符")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "role:permission:list" })
    void getRolePermissions_asSuperAdmin_shouldReturnPermissionValues() throws Exception {
        when(rolePermissionManageAppService.getRolePermissions("ROLE_CTRL_ROLE_PERM"))
                .thenReturn(List.of("ctrl-role-test:read"));

        mockMvc.perform(get("/api/v1/admin/roles/{roleName}/permissions", "ROLE_CTRL_ROLE_PERM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0]").value("ctrl-role-test:read"));
    }

    @Test
    @DisplayName("getRolePermissions: 普通成员访问应返回 403")
    @WithSecurityPrincipal(roleType = "MEMBER", roleId = 3L, permissions = {})
    void getRolePermissions_asMember_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/v1/admin/roles/{roleName}/permissions", RoleType.MEMBER.getName()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("assignPermissionsToRole: 超级管理员应成功批量分配权限给角色")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "role:permission:assign" })
    void assignPermissionsToRole_asSuperAdmin_shouldReturnOk() throws Exception {
        RolePermissionManageResult result = RolePermissionManageResult.ofPermissions(
                2,
                List.of("ctrl-role-test:assign:a", "ctrl-role-test:assign:b"));
        when(rolePermissionManageAppService.assignPermissionsToRole(any())).thenReturn(result);

        RolePermissionBatchRequestDTO request = RolePermissionBatchRequestDTO.builder()
                .permissionIds(List.of(1L, 2L))
                .build();

        mockMvc.perform(
                post("/api/v1/admin/roles/{roleName}/permissions/batch", "ROLE_CTRL_ASSIGN_PERM")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.successCount").value(2))
                .andExpect(jsonPath("$.data.currentPermissions").isArray());
    }

    @Test
    @DisplayName("removePermissionsFromRole: 超级管理员应成功批量从角色移除权限")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "role:permission:remove" })
    void removePermissionsFromRole_asSuperAdmin_shouldReturnOk() throws Exception {
        RolePermissionManageResult result = RolePermissionManageResult.ofPermissions(
                1,
                List.of("ctrl-role-test:remove:b"));
        when(rolePermissionManageAppService.removePermissionsFromRole(any())).thenReturn(result);

        RolePermissionBatchRequestDTO request = RolePermissionBatchRequestDTO.builder()
                .permissionIds(List.of(1L))
                .build();

        mockMvc.perform(
                delete("/api/v1/admin/roles/{roleName}/permissions/batch", "ROLE_CTRL_REMOVE_PERM")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.successCount").value(1))
                .andExpect(jsonPath("$.data.currentPermissions[0]").value("ctrl-role-test:remove:b"));
    }
}
