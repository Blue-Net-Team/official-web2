package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.permission.PermissionDTO;
import com.bluenet.web.api.dto.permission.PermissionRoleBatchRequestDTO;
import com.bluenet.web.api.dto.permission.PermissionTreeDTO;
import com.bluenet.web.api.converter.permission.PermissionResponseConverter;
import com.bluenet.web.application.result.permission.PermissionResult;
import com.bluenet.web.application.result.rolepermission.RolePermissionManageResult;
import com.bluenet.web.application.service.PermissionAppService;
import com.bluenet.web.application.service.RolePermissionManageAppService;
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
import org.springframework.data.domain.PageImpl;
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
@DisplayName("AdminPermissionController 集成测试")
class AdminPermissionControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PermissionAppService permissionAppService;

    @MockitoBean
    private RolePermissionManageAppService rolePermissionManageAppService;

    @MockitoBean
    private PermissionResponseConverter permissionResponseConverter;

    @AfterEach
    void tearDown() {
        UserCTX.clear();
    }

    private static final long SUPER_ADMIN_USER_ID = 9999L;

    @Test
    @DisplayName("getPermissions: 超级管理员应返回分页权限列表")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "permission:list" })
    void getPermissions_asSuperAdmin_shouldReturnPagedPermissions() throws Exception {
        PermissionResult result = new PermissionResult(1L, "ctrl-test:read", "读取", "/api/v1/test", "GET", "PROTECTED");
        PermissionDTO dto = PermissionDTO.builder()
                .id(1L)
                .value("ctrl-test:read")
                .name("读取")
                .build();
        when(permissionAppService.getPermissions(any())).thenReturn(new PageImpl<>(List.of(result)));
        when(permissionResponseConverter.toDTO(any(PermissionResult.class))).thenReturn(dto);

        mockMvc.perform(
                get("/api/v1/admin/permissions")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].id").value(1));
    }

    @Test
    @DisplayName("getPermissions: 普通成员访问应返回 403")
    @WithSecurityPrincipal(roleType = "MEMBER", roleId = 3L, permissions = {})
    void getPermissions_asMember_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/v1/admin/permissions"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("getPermissionDetail: 超级管理员应返回权限详情")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "permission:detail" })
    void getPermissionDetail_asSuperAdmin_shouldReturnDetail() throws Exception {
        PermissionResult result = new PermissionResult(1L, "ctrl-test:detail", "权限详情", "/api/v1/test", "GET",
                "PROTECTED");
        PermissionDTO dto = PermissionDTO.builder()
                .id(1L)
                .value("ctrl-test:detail")
                .name("权限详情")
                .build();
        when(permissionAppService.getPermissionDetail(1L)).thenReturn(result);
        when(permissionResponseConverter.toDTO(any(PermissionResult.class))).thenReturn(dto);

        mockMvc.perform(get("/api/v1/admin/permissions/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.value").value("ctrl-test:detail"));
    }

    @Test
    @DisplayName("getPermissionTree: 超级管理员应返回权限树")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "permission:tree" })
    void getPermissionTree_asSuperAdmin_shouldReturnTree() throws Exception {
        PermissionTreeDTO treeNode = PermissionTreeDTO.builder()
                .key("ctrl-test")
                .title("ctrl-test")
                .leaf(false)
                .build();
        when(permissionAppService.getPermissionTree()).thenReturn(List.of());
        when(permissionResponseConverter.buildPermissionTree(any())).thenReturn(List.of(treeNode));

        mockMvc.perform(get("/api/v1/admin/permissions/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].key").value("ctrl-test"));
    }

    @Test
    @DisplayName("getPermissionRoles: 超级管理员应返回权限已分配的角色名称")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "permission:role:list" })
    void getPermissionRoles_asSuperAdmin_shouldReturnRoleNames() throws Exception {
        when(rolePermissionManageAppService.getPermissionRoles(1L)).thenReturn(List.of("ROLE_CTRL_PERM_ROLE"));

        mockMvc.perform(get("/api/v1/admin/permissions/{id}/roles", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0]").value("ROLE_CTRL_PERM_ROLE"));
    }

    @Test
    @DisplayName("assignRolesToPermission: 超级管理员应成功批量分配角色到权限")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "permission:role:assign" })
    void assignRolesToPermission_asSuperAdmin_shouldReturnOk() throws Exception {
        RolePermissionManageResult result = RolePermissionManageResult
                .ofRoles(2, List.of("ROLE_CTRL_ASSIGN_A", "ROLE_CTRL_ASSIGN_B"));
        when(rolePermissionManageAppService.assignRolesToPermission(any())).thenReturn(result);

        PermissionRoleBatchRequestDTO request = PermissionRoleBatchRequestDTO.builder()
                .roleNames(List.of("ROLE_CTRL_ASSIGN_A", "ROLE_CTRL_ASSIGN_B"))
                .build();

        mockMvc.perform(
                post("/api/v1/admin/permissions/{id}/roles/batch", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.successCount").value(2))
                .andExpect(jsonPath("$.data.currentRoles").isArray());
    }

    @Test
    @DisplayName("removeRolesFromPermission: 超级管理员应成功批量从权限移除角色")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "permission:role:remove" })
    void removeRolesFromPermission_asSuperAdmin_shouldReturnOk() throws Exception {
        RolePermissionManageResult result = RolePermissionManageResult.ofRoles(1, List.of("ROLE_CTRL_REMOVE_B"));
        when(rolePermissionManageAppService.removeRolesFromPermission(any())).thenReturn(result);

        PermissionRoleBatchRequestDTO request = PermissionRoleBatchRequestDTO.builder()
                .roleNames(List.of("ROLE_CTRL_REMOVE_A"))
                .build();

        mockMvc.perform(
                delete("/api/v1/admin/permissions/{id}/roles/batch", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.successCount").value(1))
                .andExpect(jsonPath("$.data.currentRoles[0]").value("ROLE_CTRL_REMOVE_B"));
    }
}
