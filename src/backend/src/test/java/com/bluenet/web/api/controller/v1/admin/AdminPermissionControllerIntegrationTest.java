package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.permission.PermissionRoleBatchRequestDTO;
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
import com.bluenet.web.testsupport.fixture.RoleFixture;
import com.bluenet.web.testsupport.fixture.RolePermissionFixture;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
@DisplayName("AdminPermissionController 集成测试")
class AdminPermissionControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @Autowired
    private RoleMapper roleMapper;

    private Long memberRoleId;
    private Long directionAdminRoleId;

    @BeforeEach
    void prepare() {
        memberRoleId = RoleFixture.roleId(roleMapper, RoleType.MEMBER);
        directionAdminRoleId = RoleFixture.roleId(roleMapper, RoleType.DIRECTION_ADMIN);
    }

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
    @DisplayName("getPermissions: 超级管理员应返回分页权限列表")
    @WithSecurityPrincipal(userId = 9999L, roleType = "SUPER_ADMIN", roleId = 1L, permissions = { "permission:list" })
    void getPermissions_asSuperAdmin_shouldReturnPagedPermissions() throws Exception {
        PermissionFixture.save(permissionRepository, "ctrl-test:read");
        PermissionFixture.save(permissionRepository, "ctrl-test:write");

        mockMvc.perform(
                get("/api/v1/admin/permissions")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray());
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
    @WithSecurityPrincipal(userId = 9999L, roleType = "SUPER_ADMIN", roleId = 1L, permissions = { "permission:detail" })
    void getPermissionDetail_asSuperAdmin_shouldReturnDetail() throws Exception {
        Permission permission = PermissionFixture.save(permissionRepository, "权限详情", "ctrl-test:detail");

        mockMvc.perform(get("/api/v1/admin/permissions/{id}", permission.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(permission.getId()))
                .andExpect(jsonPath("$.data.value").value(permission.getValue()));
    }

    @Test
    @DisplayName("getPermissionTree: 超级管理员应返回权限树")
    @WithSecurityPrincipal(userId = 9999L, roleType = "SUPER_ADMIN", roleId = 1L, permissions = { "permission:tree" })
    void getPermissionTree_asSuperAdmin_shouldReturnTree() throws Exception {
        PermissionFixture.save(permissionRepository, "ctrl-test:tree:first");
        PermissionFixture.save(permissionRepository, "ctrl-test:tree:second");

        mockMvc.perform(get("/api/v1/admin/permissions/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("getPermissionRoles: 超级管理员应返回权限已分配的角色名称")
    @WithSecurityPrincipal(userId = 9999L, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "permission:role:list" })
    void getPermissionRoles_asSuperAdmin_shouldReturnRoleNames() throws Exception {
        RoleDO role = createRole("ROLE_CTRL_PERM_ROLE");
        Permission permission = PermissionFixture.save(permissionRepository, "ctrl-test:roles");
        RolePermissionFixture.grant(rolePermissionRepository, role.getId(), permission.getId());

        mockMvc.perform(get("/api/v1/admin/permissions/{id}/roles", permission.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0]").value(role.getName()));
    }

    @Test
    @DisplayName("assignRolesToPermission: 超级管理员应成功批量分配角色到权限")
    @WithSecurityPrincipal(userId = 9999L, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "permission:role:assign" })
    void assignRolesToPermission_asSuperAdmin_shouldReturnOk() throws Exception {
        Permission permission = PermissionFixture.save(permissionRepository, "ctrl-test:assign");
        RoleDO roleA = createRole("ROLE_CTRL_ASSIGN_A");
        RoleDO roleB = createRole("ROLE_CTRL_ASSIGN_B");
        PermissionRoleBatchRequestDTO request = PermissionRoleBatchRequestDTO.builder()
                .roleNames(List.of(roleA.getName(), roleB.getName()))
                .build();

        mockMvc.perform(
                post("/api/v1/admin/permissions/{id}/roles/batch", permission.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.successCount").value(2))
                .andExpect(jsonPath("$.data.currentRoles").isArray());
    }

    @Test
    @DisplayName("removeRolesFromPermission: 超级管理员应成功批量从权限移除角色")
    @WithSecurityPrincipal(userId = 9999L, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "permission:role:remove" })
    void removeRolesFromPermission_asSuperAdmin_shouldReturnOk() throws Exception {
        Permission permission = PermissionFixture.save(permissionRepository, "ctrl-test:remove");
        RoleDO roleA = createRole("ROLE_CTRL_REMOVE_A");
        RoleDO roleB = createRole("ROLE_CTRL_REMOVE_B");
        RolePermissionFixture.grant(rolePermissionRepository, roleA.getId(), permission.getId());
        RolePermissionFixture.grant(rolePermissionRepository, roleB.getId(), permission.getId());

        PermissionRoleBatchRequestDTO request = PermissionRoleBatchRequestDTO.builder()
                .roleNames(List.of(roleA.getName()))
                .build();

        mockMvc.perform(
                delete("/api/v1/admin/permissions/{id}/roles/batch", permission.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.successCount").value(1))
                .andExpect(jsonPath("$.data.currentRoles[0]").value(roleB.getName()));
    }
}
