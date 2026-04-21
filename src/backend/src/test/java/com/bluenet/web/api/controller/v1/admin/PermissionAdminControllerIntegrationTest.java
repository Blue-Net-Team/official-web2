package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.infrastructure.repository.dataobject.*;

import com.bluenet.web.testsupport.RepositoryTestObjects;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.PageDTO;
import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.auth.StudentIdLoginRequestDTO;
import com.bluenet.web.api.dto.auth.UserAuthResponseDTO;
import com.bluenet.web.api.dto.permission.PermissionDTO;
import com.bluenet.web.api.dto.permission.PermissionTreeDTO;
import com.bluenet.web.domain.model.entity.Permission;
import com.bluenet.web.domain.model.entity.Role;
import com.bluenet.web.domain.model.entity.RolePermission;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.infrastructure.repository.mapper.PermissionMapper;
import com.bluenet.web.infrastructure.repository.mapper.RoleMapper;
import com.bluenet.web.infrastructure.repository.mapper.RolePermissionMapper;
import com.bluenet.web.infrastructure.repository.mapper.UserMapper;
import com.bluenet.web.testcontainers.TestcontainersConfiguration;

@DisplayName("PermissionAdminController 集成测试")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@Import(TestcontainersConfiguration.class)
class PermissionAdminControllerIntegrationTest extends BaseIntegrationTest {

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
    private com.bluenet.web.infrastructure.security.scanner.PermissionScanner permissionScanner;

    private static final String TEST_STUDENT_ID = "superadmin001";
    private static final String TEST_PASSWORD = "adminPassword123";

    private List<String> authCookies;
    private String csrfToken;
    private Long superAdminRoleId;

    @BeforeEach
    void setUpTestData() {
        // 查找SUPER_ADMIN角色
        Role superAdminRole = RepositoryTestObjects.toDomain(roleMapper.selectByName("SUPER_ADMIN"), Role.class);
        if (superAdminRole == null) {
            throw new IllegalStateException("SUPER_ADMIN 角色不存在，请检查数据库迁移脚本");
        }
        superAdminRoleId = superAdminRole.getId();

        // 创建SUPER_ADMIN用户
        User adminUser = User.builder()
                .studentId(TEST_STUDENT_ID)
                .username("超级管理员")
                .email("superadmin@example.com")
                .password(passwordEncoder.encode(TEST_PASSWORD))
                .roleId(superAdminRoleId)
                .disable(false)
                .build();
        RepositoryTestObjects.insert(userMapper, adminUser, UserDO.class);

        // 登录获取认证信息
        loginAndGetCookies();
    }

    @Test
    @DisplayName("分页查询权限列表 - 成功")
    void getPermissions_Success() {
        // 准备测试数据：插入一些权限
        Permission permission1 = createPermission("assessment:create", "创建考核", "/api/v1/admin/assessments", "POST");
        Permission permission2 = createPermission("assessment:read", "查看考核", "/api/v1/admin/assessments", "GET");

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // 发送请求
        ResponseEntity<ResponseMessage<PageDTO<PermissionDTO>>> response = restTemplate.exchange(
                "/api/v1/admin/permissions?page=0&size=20",
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<ResponseMessage<PageDTO<PermissionDTO>>>() {
                });

        // 验证响应
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ResponseMessage<PageDTO<PermissionDTO>> body = response.getBody();
        assertNotNull(body);
        assertEquals(HttpStatus.OK.value(), body.getCode());

        PageDTO<PermissionDTO> page = body.getData();
        assertNotNull(page);
        assertTrue(page.getTotalElements() >= 2);
        assertFalse(page.getContent().isEmpty());

        // 验证返回的权限数据
        List<PermissionDTO> permissions = page.getContent();
        assertTrue(permissions.stream().anyMatch(p -> "assessment:create".equals(p.getValue())));
        assertTrue(permissions.stream().anyMatch(p -> "assessment:read".equals(p.getValue())));
    }

    @Test
    @DisplayName("搜索权限标识符 - 成功")
    void getPermissions_WithKeyword_Success() {
        // 准备测试数据：一个包含关键词，一个不包含
        Permission includedPermission = createPermission(
                "assessment:create",
                "创建考核",
                "/api/v1/admin/assessments",
                "POST");
        Permission excludedPermission = createPermission("user:create", "创建用户", "/api/v1/admin/users", "POST");

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // 构建带查询参数的URL
        String url = "/api/v1/admin/permissions?page=0&size=20&keyword=assessment";

        ResponseEntity<ResponseMessage<PageDTO<PermissionDTO>>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<ResponseMessage<PageDTO<PermissionDTO>>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ResponseMessage<PageDTO<PermissionDTO>> body = response.getBody();
        assertNotNull(body);
        assertEquals(HttpStatus.OK.value(), body.getCode());
        PageDTO<PermissionDTO> page = body.getData();

        // 验证包含关键词的权限在结果中
        assertTrue(page.getContent().stream().anyMatch(p -> includedPermission.getId().equals(p.getId())));

        // 验证不包含关键词的权限不在结果中
        assertFalse(page.getContent().stream().anyMatch(p -> excludedPermission.getId().equals(p.getId())));

        // 验证所有返回的权限都包含关键词"assessment"（忽略大小写）
        for (PermissionDTO permission : page.getContent()) {
            assertTrue(
                    permission.getValue().toLowerCase().contains("assessment") ||
                            (permission.getName() != null
                                    && permission.getName().toLowerCase().contains("assessment")));
        }
    }

    @Test
    @DisplayName("获取权限详情 - 成功")
    void getPermissionDetail_Success() {
        // 准备测试数据
        Permission permission = createPermission("assessment:create", "创建考核", "/api/v1/admin/assessments", "POST");

        // 分配权限给角色
        Role role = RepositoryTestObjects
                .toDomain(roleMapper.selectByName(RoleType.DIRECTION_ADMIN.name()), Role.class);
        RolePermission rp = new RolePermission();
        rp.setRoleId(role.getId());
        rp.setPermissionId(permission.getId());
        RepositoryTestObjects.insert(rolePermissionMapper, rp, RolePermissionDO.class);

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<ResponseMessage<PermissionDTO>> response = restTemplate.exchange(
                "/api/v1/admin/permissions/" + permission.getId(),
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<ResponseMessage<PermissionDTO>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ResponseMessage<PermissionDTO> body = response.getBody();
        assertNotNull(body);
        assertEquals(HttpStatus.OK.value(), body.getCode());
        PermissionDTO detail = body.getData();

        assertEquals(permission.getId(), detail.getId());
        assertEquals("assessment:create", detail.getValue());
        assertEquals("创建考核", detail.getName());

        // 验证已分配角色包含DIRECTION_ADMIN
        assertTrue(detail.getAssignedRoles().contains("DIRECTION_ADMIN"));
    }

    @Test
    @DisplayName("获取权限树 - 成功")
    void getPermissionTree_Success() {
        // 准备多级权限数据
        createPermission("assessment:create", "创建考核", "/api/v1/admin/assessments", "POST");
        createPermission("assessment:question:create", "创建考题", "/api/v1/admin/assessment-questions", "POST");

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<ResponseMessage<List<PermissionTreeDTO>>> response = restTemplate.exchange(
                "/api/v1/admin/permissions/tree",
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<ResponseMessage<List<PermissionTreeDTO>>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ResponseMessage<List<PermissionTreeDTO>> body = response.getBody();
        assertNotNull(body);
        assertEquals(HttpStatus.OK.value(), body.getCode());
        List<PermissionTreeDTO> tree = body.getData();

        assertNotNull(tree);
        // 应该包含assessment节点
        assertTrue(tree.stream().anyMatch(node -> "assessment".equals(node.getTitle())));
    }

    @Test
    @DisplayName("获取权限详情 - 权限不存在返回404")
    void getPermissionDetail_NotFound() {
        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<ResponseMessage<PermissionDTO>> response = restTemplate.exchange(
                "/api/v1/admin/permissions/999999",
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<ResponseMessage<PermissionDTO>>() {
                });

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Nested
    @DisplayName("权限访问控制 - 非SUPER_ADMIN被拒绝")
    class AccessControl {

        private List<String> memberCookies;
        private String memberCsrfToken;

        @BeforeEach
        void setUpMemberUser() {
            // 创建普通 MEMBER 用户
            Role memberRole = RepositoryTestObjects.toDomain(roleMapper.selectByName("MEMBER"), Role.class);
            User memberUser = User.builder()
                    .studentId("member001")
                    .username("普通成员")
                    .email("member@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .roleId(memberRole.getId())
                    .disable(false)
                    .build();
            RepositoryTestObjects.insert(userMapper, memberUser, UserDO.class);

            // 用 MEMBER 登录
            StudentIdLoginRequestDTO loginRequest = new StudentIdLoginRequestDTO();
            loginRequest.setStudentId("member001");
            loginRequest.setPassword("password123");

            ResponseEntity<ResponseMessage<UserAuthResponseDTO>> loginResponse = restTemplate.exchange(
                    "/api/v1/auth/login/student-id",
                    HttpMethod.POST,
                    new HttpEntity<>(loginRequest),
                    new ParameterizedTypeReference<ResponseMessage<UserAuthResponseDTO>>() {
                    });

            assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
            memberCookies = loginResponse.getHeaders().get(HttpHeaders.SET_COOKIE);
            memberCsrfToken = loginResponse.getBody().getData().getCsrfToken();
        }

        @Test
        @DisplayName("MEMBER访问权限列表 - 返回403")
        void memberAccessPermissions_Forbidden() {
            HttpHeaders headers = new HttpHeaders();
            headers.put(HttpHeaders.COOKIE, memberCookies);
            headers.set("X-CSRF-Token", memberCsrfToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<ResponseMessage<PageDTO<PermissionDTO>>> response = restTemplate.exchange(
                    "/api/v1/admin/permissions?page=0&size=20",
                    HttpMethod.GET,
                    request,
                    new ParameterizedTypeReference<ResponseMessage<PageDTO<PermissionDTO>>>() {
                    });

            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }
    }

    // 辅助方法

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
        assertNotNull(authCookies, "登录响应应包含 Set-Cookie");
        assertNotNull(csrfToken, "登录响应应包含 CSRF Token");
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.put(HttpHeaders.COOKIE, authCookies);
        return headers;
    }

    private HttpHeaders createAuthHeadersWithCsrf() {
        HttpHeaders headers = createAuthHeaders();
        headers.set("X-CSRF-Token", csrfToken);
        return headers;
    }

    private Permission createPermission(String value, String name, String url, String method) {
        Permission permission = new Permission();
        permission.setValue(value);
        permission.setName(name);
        permission.setUrl(url);
        permission.setMethod(method);
        RepositoryTestObjects.insert(permissionMapper, permission, PermissionDO.class);
        return permission;
    }
}
