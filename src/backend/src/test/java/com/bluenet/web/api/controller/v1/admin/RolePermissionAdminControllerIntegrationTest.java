package com.bluenet.web.api.controller.v1.admin;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.auth.StudentIdLoginRequestDTO;
import com.bluenet.web.api.dto.auth.UserAuthResponseDTO;
import com.bluenet.web.api.dto.permission.PermissionRoleResponseDTO;
import com.bluenet.web.api.dto.permission.RolePermissionResponseDTO;
import com.bluenet.web.domain.model.entity.Permission;
import com.bluenet.web.domain.model.entity.Role;
import com.bluenet.web.domain.model.entity.RolePermission;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.infrastructure.repository.mapper.PermissionMapper;
import com.bluenet.web.infrastructure.repository.mapper.RoleMapper;
import com.bluenet.web.infrastructure.repository.mapper.RolePermissionMapper;
import com.bluenet.web.infrastructure.repository.mapper.UserMapper;
import com.bluenet.web.infrastructure.security.scanner.PermissionScanner;
import com.bluenet.web.testcontainers.TestcontainersConfiguration;

@DisplayName("角色权限管理 集成测试")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@Import(TestcontainersConfiguration.class)
class RolePermissionAdminControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private PermissionScanner permissionScanner;

    private static final String TEST_STUDENT_ID = "superadmin002";
    private static final String TEST_PASSWORD = "adminPassword123";

    private List<String> authCookies;
    private String csrfToken;

    @BeforeEach
    void setUp() {
        Role superAdminRole = roleMapper.selectByName("SUPER_ADMIN");
        User adminUser = User.builder()
                .studentId(TEST_STUDENT_ID)
                .username("权限管理员")
                .email("permadmin@example.com")
                .password(passwordEncoder.encode(TEST_PASSWORD))
                .roleId(superAdminRole.getId())
                .disable(false)
                .build();
        userMapper.insert(adminUser);
        loginAndGetCookies();
    }

    @Nested
    @DisplayName("角色权限管理 - GET /admin/roles/{roleName}/permissions")
    class GetRolePermissions {

        @Test
        @DisplayName("查询角色权限列表 - 成功")
        void getRolePermissions_Success() {
            Role memberRole = roleMapper.selectByName("MEMBER");
            Permission perm = createPermission("test:read", "测试读取");
            assignPermissionToRole(memberRole.getId(), perm.getId());

            HttpHeaders headers = createAuthHeadersWithCsrf();
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<ResponseMessage<List<String>>> response = restTemplate.exchange(
                    "/api/v1/admin/roles/MEMBER/permissions",
                    HttpMethod.GET,
                    request,
                    new ParameterizedTypeReference<ResponseMessage<List<String>>>() {
                    });

            assertEquals(HttpStatus.OK, response.getStatusCode());
            ResponseMessage<List<String>> body = response.getBody();
            assertNotNull(body);
            assertEquals(HttpStatus.OK.value(), body.getCode());
            assertNotNull(body.getData());
            assertTrue(body.getData().contains("test:read"));
        }

        @Test
        @DisplayName("查询SUPER_ADMIN角色权限 - 允许查询")
        void getSuperAdminPermissions_Success() {
            HttpHeaders headers = createAuthHeadersWithCsrf();
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<ResponseMessage<List<String>>> response = restTemplate.exchange(
                    "/api/v1/admin/roles/SUPER_ADMIN/permissions",
                    HttpMethod.GET,
                    request,
                    new ParameterizedTypeReference<ResponseMessage<List<String>>>() {
                    });

            assertEquals(HttpStatus.OK, response.getStatusCode());
        }

        @Test
        @DisplayName("查询不存在角色的权限 - 返回404")
        void getRolePermissions_RoleNotFound() {
            HttpHeaders headers = createAuthHeadersWithCsrf();
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<ResponseMessage<List<String>>> response = restTemplate.exchange(
                    "/api/v1/admin/roles/NON_EXISTENT_ROLE/permissions",
                    HttpMethod.GET,
                    request,
                    new ParameterizedTypeReference<ResponseMessage<List<String>>>() {
                    });

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("批量分配权限 - POST /admin/roles/{roleName}/permissions/batch")
    class AssignPermissions {

        @Test
        @DisplayName("批量分配权限给角色 - 成功")
        void assignPermissions_Success() {
            Permission perm1 = createPermission("test:assign1", "测试分配1");
            Permission perm2 = createPermission("test:assign2", "测试分配2");

            HttpHeaders headers = createAuthHeadersWithCsrf();
            String requestBody = "{\"permissionIds\": [" + perm1.getId() + ", " + perm2.getId() + "]}";
            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<ResponseMessage<RolePermissionResponseDTO>> response = restTemplate.exchange(
                    "/api/v1/admin/roles/MEMBER/permissions/batch",
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<ResponseMessage<RolePermissionResponseDTO>>() {
                    });

            assertEquals(HttpStatus.OK, response.getStatusCode());
            ResponseMessage<RolePermissionResponseDTO> body = response.getBody();
            assertNotNull(body);
            assertEquals(HttpStatus.OK.value(), body.getCode());
            RolePermissionResponseDTO data = body.getData();
            assertEquals(2, data.getSuccessCount());
            assertTrue(data.getCurrentPermissions().contains("test:assign1"));
            assertTrue(data.getCurrentPermissions().contains("test:assign2"));
        }

        @Test
        @DisplayName("为SUPER_ADMIN分配权限 - 返回400错误")
        void assignPermissions_SuperAdmin_Returns400() {
            Permission perm = createPermission("test:superassign", "超管测试");
            HttpHeaders headers = createAuthHeadersWithCsrf();
            String requestBody = "{\"permissionIds\": [" + perm.getId() + "]}";
            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<ResponseMessage<RolePermissionResponseDTO>> response = restTemplate.exchange(
                    "/api/v1/admin/roles/SUPER_ADMIN/permissions/batch",
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<ResponseMessage<RolePermissionResponseDTO>>() {
                    });

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("重复分配已有权限 - 自动跳过")
        void assignPermissions_Duplicate_Skipped() {
            Permission perm = createPermission("test:dup", "重复测试");
            Role memberRole = roleMapper.selectByName("MEMBER");
            assignPermissionToRole(memberRole.getId(), perm.getId());

            HttpHeaders headers = createAuthHeadersWithCsrf();
            String requestBody = "{\"permissionIds\": [" + perm.getId() + "]}";
            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<ResponseMessage<RolePermissionResponseDTO>> response = restTemplate.exchange(
                    "/api/v1/admin/roles/MEMBER/permissions/batch",
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<ResponseMessage<RolePermissionResponseDTO>>() {
                    });

            assertEquals(HttpStatus.OK, response.getStatusCode());
            ResponseMessage<RolePermissionResponseDTO> body = response.getBody();
            assertNotNull(body);
            assertEquals(0, body.getData().getSuccessCount());
        }
    }

    @Nested
    @DisplayName("批量移除权限 - DELETE /admin/roles/{roleName}/permissions/batch")
    class RemovePermissions {

        @Test
        @DisplayName("批量移除角色权限 - 成功")
        void removePermissions_Success() {
            Permission perm = createPermission("test:remove", "测试移除");
            Role memberRole = roleMapper.selectByName("MEMBER");
            assignPermissionToRole(memberRole.getId(), perm.getId());

            HttpHeaders headers = createAuthHeadersWithCsrf();
            String requestBody = "{\"permissionIds\": [" + perm.getId() + "]}";
            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<ResponseMessage<RolePermissionResponseDTO>> response = restTemplate.exchange(
                    "/api/v1/admin/roles/MEMBER/permissions/batch",
                    HttpMethod.DELETE,
                    request,
                    new ParameterizedTypeReference<ResponseMessage<RolePermissionResponseDTO>>() {
                    });

            assertEquals(HttpStatus.OK, response.getStatusCode());
            ResponseMessage<RolePermissionResponseDTO> body = response.getBody();
            assertNotNull(body);
            assertFalse(body.getData().getCurrentPermissions().contains("test:remove"));
        }

        @Test
        @DisplayName("从SUPER_ADMIN移除权限 - 返回400错误")
        void removePermissions_SuperAdmin_Returns400() {
            Permission perm = createPermission("test:superremove", "超管移除测试");
            HttpHeaders headers = createAuthHeadersWithCsrf();
            String requestBody = "{\"permissionIds\": [" + perm.getId() + "]}";
            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<ResponseMessage<RolePermissionResponseDTO>> response = restTemplate.exchange(
                    "/api/v1/admin/roles/SUPER_ADMIN/permissions/batch",
                    HttpMethod.DELETE,
                    request,
                    new ParameterizedTypeReference<ResponseMessage<RolePermissionResponseDTO>>() {
                    });

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("权限角色管理 - GET/POST/DELETE /admin/permissions/{id}/roles")
    class PermissionRoleManagement {

        @Test
        @DisplayName("查询权限对应的角色列表 - 成功")
        void getPermissionRoles_Success() {
            Permission perm = createPermission("test:proles", "权限角色测试");
            Role memberRole = roleMapper.selectByName("MEMBER");
            assignPermissionToRole(memberRole.getId(), perm.getId());

            HttpHeaders headers = createAuthHeadersWithCsrf();
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<ResponseMessage<List<String>>> response = restTemplate.exchange(
                    "/api/v1/admin/permissions/" + perm.getId() + "/roles",
                    HttpMethod.GET,
                    request,
                    new ParameterizedTypeReference<ResponseMessage<List<String>>>() {
                    });

            assertEquals(HttpStatus.OK, response.getStatusCode());
            ResponseMessage<List<String>> body = response.getBody();
            assertNotNull(body);
            assertTrue(body.getData().contains("MEMBER"));
        }

        @Test
        @DisplayName("批量添加角色到权限 - 成功")
        void assignRolesToPermission_Success() {
            Permission perm = createPermission("test:addroles", "添加角色测试");

            HttpHeaders headers = createAuthHeadersWithCsrf();
            String requestBody = "{\"roleNames\": [\"MEMBER\", \"CANDIDATE\"]}";
            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<ResponseMessage<PermissionRoleResponseDTO>> response = restTemplate.exchange(
                    "/api/v1/admin/permissions/" + perm.getId() + "/roles/batch",
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<ResponseMessage<PermissionRoleResponseDTO>>() {
                    });

            assertEquals(HttpStatus.OK, response.getStatusCode());
            ResponseMessage<PermissionRoleResponseDTO> body = response.getBody();
            assertNotNull(body);
            assertEquals(2, body.getData().getSuccessCount());
            assertTrue(body.getData().getCurrentRoles().contains("MEMBER"));
            assertTrue(body.getData().getCurrentRoles().contains("CANDIDATE"));
        }

        @Test
        @DisplayName("批量从权限移除角色 - 成功")
        void removeRolesFromPermission_Success() {
            Permission perm = createPermission("test:removeroles", "移除角色测试");
            Role memberRole = roleMapper.selectByName("MEMBER");
            Role candidateRole = roleMapper.selectByName("CANDIDATE");
            assignPermissionToRole(memberRole.getId(), perm.getId());
            assignPermissionToRole(candidateRole.getId(), perm.getId());

            HttpHeaders headers = createAuthHeadersWithCsrf();
            String requestBody = "{\"roleNames\": [\"CANDIDATE\"]}";
            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<ResponseMessage<PermissionRoleResponseDTO>> response = restTemplate.exchange(
                    "/api/v1/admin/permissions/" + perm.getId() + "/roles/batch",
                    HttpMethod.DELETE,
                    request,
                    new ParameterizedTypeReference<ResponseMessage<PermissionRoleResponseDTO>>() {
                    });

            assertEquals(HttpStatus.OK, response.getStatusCode());
            ResponseMessage<PermissionRoleResponseDTO> body = response.getBody();
            assertNotNull(body);
            assertTrue(body.getData().getCurrentRoles().contains("MEMBER"));
            assertFalse(body.getData().getCurrentRoles().contains("CANDIDATE"));
        }
    }

    private void loginAndGetCookies() {
        StudentIdLoginRequestDTO loginRequest = new StudentIdLoginRequestDTO();
        loginRequest.setStudentId(TEST_STUDENT_ID);
        loginRequest.setPassword(TEST_PASSWORD);

        ResponseEntity<ResponseMessage<UserAuthResponseDTO>> response = restTemplate.exchange(
                "/api/v1/auth/login/student-id",
                HttpMethod.POST,
                new HttpEntity<>(loginRequest),
                new ParameterizedTypeReference<ResponseMessage<UserAuthResponseDTO>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        authCookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        csrfToken = response.getBody().getData().getCsrfToken();
        assertNotNull(csrfToken, "登录响应应包含 CSRF Token");
    }

    private HttpHeaders createAuthHeadersWithCsrf() {
        HttpHeaders headers = new HttpHeaders();
        headers.put(HttpHeaders.COOKIE, authCookies);
        headers.set("X-CSRF-Token", csrfToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private Permission createPermission(String value, String name) {
        Permission permission = new Permission();
        permission.setValue(value);
        permission.setName(name);
        permission.setUrl("/api/v1/test");
        permission.setMethod("GET");
        permissionMapper.insert(permission);
        return permission;
    }

    private void assignPermissionToRole(Long roleId, Long permissionId) {
        RolePermission rp = new RolePermission();
        rp.setRoleId(roleId);
        rp.setPermissionId(permissionId);
        rolePermissionMapper.insert(rp);
    }
}
