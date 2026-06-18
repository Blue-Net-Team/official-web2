package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.PageDTO;
import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.auth.StudentIdLoginRequestDTO;
import com.bluenet.web.api.dto.auth.UserAuthResponseDTO;
import com.bluenet.web.api.dto.softwareresource.CreateSoftwareResourceRequestDTO;
import com.bluenet.web.api.dto.softwareresource.SoftwareResourceDTO;
import com.bluenet.web.api.dto.softwareresource.UpdateSoftwareResourceRequestDTO;
import com.bluenet.web.domain.model.entity.Permission;
import com.bluenet.web.domain.model.entity.Role;
import com.bluenet.web.domain.model.entity.RolePermission;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.SoftwareResourceStatus;
import com.bluenet.web.infrastructure.repository.dataobject.PermissionDO;
import com.bluenet.web.infrastructure.repository.dataobject.RolePermissionDO;
import com.bluenet.web.infrastructure.repository.dataobject.SoftwareResourceDO;
import com.bluenet.web.infrastructure.repository.dataobject.UserDO;
import com.bluenet.web.infrastructure.repository.mapper.PermissionMapper;
import com.bluenet.web.infrastructure.repository.mapper.RoleMapper;
import com.bluenet.web.infrastructure.repository.mapper.RolePermissionMapper;
import com.bluenet.web.infrastructure.repository.mapper.SoftwareResourceMapper;
import com.bluenet.web.infrastructure.repository.mapper.UserMapper;
import com.bluenet.web.infrastructure.security.cache.PermissionCache;
import com.bluenet.web.testcontainers.TestcontainersConfiguration;
import com.bluenet.web.testsupport.RepositoryTestObjects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("AdminSoftwareResourceController 集成测试")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@Import(TestcontainersConfiguration.class)
class AdminSoftwareResourceControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    @Autowired
    private SoftwareResourceMapper softwareResourceMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PermissionCache permissionCache;

    private static final String TEST_STUDENT_ID = "admin001";
    private static final String TEST_PASSWORD = "adminPassword123";

    private List<String> authCookies;
    private String csrfToken;
    private Long adminRoleId;

    @BeforeEach
    void setUpTestData() {
        Role adminRole = RepositoryTestObjects.toDomain(roleMapper.selectByName("SUPER_ADMIN"), Role.class);
        if (adminRole == null) {
            throw new IllegalStateException("SUPER_ADMIN 角色不存在，请检查数据库迁移脚本");
        }
        adminRoleId = adminRole.getId();

        createPermission("software-resource:admin-list", "查询软件资源列表");
        createPermission("software-resource:create", "创建软件资源");
        createPermission("software-resource:update", "更新软件资源");
        createPermission("software-resource:delete", "删除软件资源");
        createPermission("software-resource:list", "获取软件资源列表");

        permissionCache.refresh();

        User adminUser = User.reconstruct(
                null,
                TEST_STUDENT_ID,
                "admin@example.com",
                adminRoleId,
                passwordEncoder.encode(TEST_PASSWORD),
                "管理员",
                null,
                null,
                null,
                null,
                Direction.COMPUTER_VISION,
                null,
                null,
                null,
                false,
                null,
                null,
                null,
                null,
                null);
        RepositoryTestObjects.insert(userMapper, adminUser, UserDO.class);

        loginAndGetCookies();
    }

    private void createPermission(String value, String name) {
        Permission permission = Permission.create(name, value, null, null, null);
        RepositoryTestObjects.insert(permissionMapper, permission, PermissionDO.class);
        RolePermission rolePermission = RolePermission.create(adminRoleId, permission.getId());
        RepositoryTestObjects.insert(rolePermissionMapper, rolePermission, RolePermissionDO.class);
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
        assertNotNull(authCookies);
        assertNotNull(csrfToken);
    }

    private HttpHeaders createAuthHeadersWithCsrf() {
        HttpHeaders headers = new HttpHeaders();
        headers.put(HttpHeaders.COOKIE, authCookies);
        headers.set("X-CSRF-Token", csrfToken);
        return headers;
    }

    @Test
    @DisplayName("集成测试：创建软件资源应成功")
    void create_shouldSucceed() {
        CreateSoftwareResourceRequestDTO request = CreateSoftwareResourceRequestDTO.builder()
                .name("New Tool")
                .direction(Direction.GENERAL)
                .category("tool")
                .description("desc")
                .externalUrl("https://example.com/new")
                .sortOrder(1)
                .build();

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<CreateSoftwareResourceRequestDTO> entity = new HttpEntity<>(request, headers);

        ResponseEntity<ResponseMessage<SoftwareResourceDTO>> response = restTemplate.exchange(
                "/api/v1/admin/software-resources",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ResponseMessage<SoftwareResourceDTO>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());
        assertEquals("New Tool", response.getBody().getData().getName());
    }

    @Test
    @DisplayName("集成测试：未认证用户创建应返回401")
    void create_withoutAuth_shouldReturn401() {
        CreateSoftwareResourceRequestDTO request = CreateSoftwareResourceRequestDTO.builder()
                .name("New Tool")
                .direction(Direction.GENERAL)
                .externalUrl("https://example.com/new")
                .build();

        HttpEntity<CreateSoftwareResourceRequestDTO> entity = new HttpEntity<>(request);

        ResponseEntity<ResponseMessage<SoftwareResourceDTO>> response = restTemplate.exchange(
                "/api/v1/admin/software-resources",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ResponseMessage<SoftwareResourceDTO>>() {
                });

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("集成测试：更新软件资源应成功")
    void update_shouldSucceed() {
        SoftwareResourceDO existing = SoftwareResourceDO.builder()
                .name("Old Tool")
                .direction(Direction.GENERAL)
                .externalUrl("https://example.com/old")
                .sortOrder(1)
                .status(SoftwareResourceStatus.ACTIVE)
                .build();
        softwareResourceMapper.insert(existing);

        UpdateSoftwareResourceRequestDTO request = UpdateSoftwareResourceRequestDTO.builder()
                .name("Updated Tool")
                .direction(Direction.EMBEDDED)
                .category("IDE")
                .description("updated")
                .externalUrl("https://example.com/updated")
                .sortOrder(2)
                .status(SoftwareResourceStatus.DISABLED)
                .build();

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<UpdateSoftwareResourceRequestDTO> entity = new HttpEntity<>(request, headers);

        ResponseEntity<ResponseMessage<SoftwareResourceDTO>> response = restTemplate.exchange(
                "/api/v1/admin/software-resources/" + existing.getId(),
                HttpMethod.PUT,
                entity,
                new ParameterizedTypeReference<ResponseMessage<SoftwareResourceDTO>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        SoftwareResourceDTO updated = response.getBody().getData();
        assertEquals("Updated Tool", updated.getName());
        assertEquals(Direction.EMBEDDED, updated.getDirection());
        assertEquals(SoftwareResourceStatus.DISABLED, updated.getStatus());
    }

    @Test
    @DisplayName("集成测试：删除软件资源应成功")
    void delete_shouldSucceed() {
        SoftwareResourceDO existing = SoftwareResourceDO.builder()
                .name("To Delete")
                .direction(Direction.GENERAL)
                .externalUrl("https://example.com/del")
                .sortOrder(1)
                .status(SoftwareResourceStatus.ACTIVE)
                .build();
        softwareResourceMapper.insert(existing);

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<ResponseMessage<Void>> response = restTemplate.exchange(
                "/api/v1/admin/software-resources/" + existing.getId(),
                HttpMethod.DELETE,
                entity,
                new ParameterizedTypeReference<ResponseMessage<Void>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(200, response.getBody().getCode());
    }

    @Test
    @DisplayName("集成测试：管理后台查询列表应成功")
    void listAdmin_shouldSucceed() {
        SoftwareResourceDO existing = SoftwareResourceDO.builder()
                .name("Admin List")
                .direction(Direction.GENERAL)
                .externalUrl("https://example.com/list")
                .sortOrder(1)
                .status(SoftwareResourceStatus.ACTIVE)
                .build();
        softwareResourceMapper.insert(existing);

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<ResponseMessage<PageDTO<SoftwareResourceDTO>>> response = restTemplate.exchange(
                "/api/v1/admin/software-resources?page=0&size=10",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<ResponseMessage<PageDTO<SoftwareResourceDTO>>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getData().getTotalElements());
    }
}
