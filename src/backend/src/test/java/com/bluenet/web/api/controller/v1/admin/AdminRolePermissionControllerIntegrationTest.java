package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.permission.RolePermissionBatchRequestDTO;
import com.bluenet.web.domain.model.entity.Permission;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.repository.PermissionRepository;
import com.bluenet.web.domain.repository.RolePermissionRepository;
import com.bluenet.web.infrastructure.repository.dataobject.RoleDO;
import com.bluenet.web.infrastructure.repository.mapper.RoleMapper;
import com.bluenet.web.infrastructure.security.principal.WithSecurityPrincipal;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.bluenet.web.testconfig.TestSecurityConfig;
import com.bluenet.web.testsupport.fixture.PermissionFixture;
import com.bluenet.web.testsupport.fixture.RolePermissionFixture;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

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

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @AfterEach
    void tearDown() {
        UserCTX.clear();
    }

    private RoleDO createRole(String name) {
        RoleDO role = RoleDO.builder().name(name).build();
        roleMapper.insert(role);
        return role;
    }

    @Test
    @DisplayName("getRolePermissions: 超级管理员应返回角色已分配的权限标识符")
    @WithSecurityPrincipal(userId = 9999L, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "role:permission:list" })
    void getRolePermissions_asSuperAdmin_shouldReturnPermissionValues() throws Exception {
        RoleDO role = createRole("ROLE_CTRL_ROLE_PERM");
        Permission permission = PermissionFixture.save(permissionRepository, "ctrl-role-test:read");
        RolePermissionFixture.grant(rolePermissionRepository, role.getId(), permission.getId());

        mockMvc.perform(get("/api/v1/admin/roles/{roleName}/permissions", role.getName()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0]").value(permission.getValue()));
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
    @WithSecurityPrincipal(userId = 9999L, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "role:permission:assign" })
    void assignPermissionsToRole_asSuperAdmin_shouldReturnOk() throws Exception {
        RoleDO role = createRole("ROLE_CTRL_ASSIGN_PERM");
        Permission permissionA = PermissionFixture.save(permissionRepository, "ctrl-role-test:assign:a");
        Permission permissionB = PermissionFixture.save(permissionRepository, "ctrl-role-test:assign:b");
        RolePermissionBatchRequestDTO request = RolePermissionBatchRequestDTO.builder()
                .permissionIds(List.of(permissionA.getId(), permissionB.getId()))
                .build();

        mockMvc.perform(
                post("/api/v1/admin/roles/{roleName}/permissions/batch", role.getName())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.successCount").value(2))
                .andExpect(jsonPath("$.data.currentPermissions").isArray());
    }

    @Test
    @DisplayName("removePermissionsFromRole: 超级管理员应成功批量从角色移除权限")
    @WithSecurityPrincipal(userId = 9999L, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "role:permission:remove" })
    void removePermissionsFromRole_asSuperAdmin_shouldReturnOk() throws Exception {
        RoleDO role = createRole("ROLE_CTRL_REMOVE_PERM");
        Permission permissionA = PermissionFixture.save(permissionRepository, "ctrl-role-test:remove:a");
        Permission permissionB = PermissionFixture.save(permissionRepository, "ctrl-role-test:remove:b");
        RolePermissionFixture.grant(rolePermissionRepository, role.getId(), permissionA.getId(), permissionB.getId());

        RolePermissionBatchRequestDTO request = RolePermissionBatchRequestDTO.builder()
                .permissionIds(List.of(permissionA.getId()))
                .build();

        mockMvc.perform(
                delete("/api/v1/admin/roles/{roleName}/permissions/batch", role.getName())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.successCount").value(1))
                .andExpect(jsonPath("$.data.currentPermissions[0]").value(permissionB.getValue()));
    }
}
