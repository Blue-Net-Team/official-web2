package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.infrastructure.repository.dataobject.*;

import com.bluenet.web.testsupport.RepositoryTestObjects;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

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

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.auth.StudentIdLoginRequestDTO;
import com.bluenet.web.api.dto.auth.UserAuthResponseDTO;
import com.bluenet.web.api.dto.college.CollegeDTO;
import com.bluenet.web.api.dto.college.CreateCollegeRequestDTO;
import com.bluenet.web.api.dto.college.UpdateCollegeRequestDTO;
import com.bluenet.web.domain.model.entity.College;
import com.bluenet.web.domain.model.entity.Enroll;
import com.bluenet.web.domain.model.entity.Permission;
import com.bluenet.web.domain.model.entity.Role;
import com.bluenet.web.domain.model.entity.RolePermission;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.EnrollStatus;
import com.bluenet.web.infrastructure.repository.mapper.CollegeMapper;
import com.bluenet.web.infrastructure.repository.mapper.EnrollMapper;
import com.bluenet.web.infrastructure.repository.mapper.PermissionMapper;
import com.bluenet.web.infrastructure.repository.mapper.RoleMapper;
import com.bluenet.web.infrastructure.repository.mapper.RolePermissionMapper;
import com.bluenet.web.infrastructure.repository.mapper.UserMapper;
import com.bluenet.web.infrastructure.security.cache.PermissionCache;
import com.bluenet.web.testcontainers.TestcontainersConfiguration;

/**
 * AdminCollegeController 集成测试
 * <p>
 * 测试管理接口 CRUD 操作，包括权限验证、名称唯一性校验和删除时关联检查
 * </p>
 */
@DisplayName("AdminCollegeController 集成测试")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@Import(TestcontainersConfiguration.class)
class AdminCollegeControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CollegeMapper collegeMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private EnrollMapper enrollMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PermissionCache permissionCache;

    private static final String TEST_STUDENT_ID = "admin001";
    private static final String TEST_PASSWORD = "adminPassword123";
    private static final String TEST_NAME_1 = "计算机科学与技术学院";
    private static final String TEST_NAME_2 = "软件学院";
    private static final String TEST_NAME_3 = "人工智能学院";

    private List<String> authCookies;
    private String csrfToken;
    private Long adminRoleId;

    @BeforeEach
    void setUpTestData() {
        // 查询已存在的管理员角色（由 Flyway 迁移脚本初始化）
        Role adminRole = RepositoryTestObjects.toDomain(roleMapper.selectByName("SUPER_ADMIN"), Role.class);
        if (adminRole == null) {
            throw new IllegalStateException("SUPER_ADMIN 角色不存在，请检查数据库迁移脚本");
        }
        adminRoleId = adminRole.getId();

        // 创建学院相关权限
        createPermission("college:create", "创建学院");
        createPermission("college:update", "更新学院");
        createPermission("college:delete", "删除学院");
        createPermission("college:list", "获取学院列表");

        // 刷新权限缓存
        permissionCache.refresh();

        // 创建管理员用户
        User adminUser = new User();
        adminUser.setStudentId(TEST_STUDENT_ID);
        adminUser.setUsername("管理员");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword(passwordEncoder.encode(TEST_PASSWORD));
        adminUser.setRoleId(adminRoleId);
        adminUser.setDisable(false);
        adminUser.setDirection(Direction.COMPUTER_VISION);
        RepositoryTestObjects.insert(userMapper, adminUser, UserDO.class);

        // 登录获取 Cookie 和 CSRF Token
        loginAndGetCookies();
    }

    private void createPermission(String value, String name) {
        Permission permission = new Permission();
        permission.setName(name);
        permission.setValue(value);
        RepositoryTestObjects.insert(permissionMapper, permission, PermissionDO.class);

        // 关联到管理员角色
        RolePermission rolePermission = new RolePermission();
        rolePermission.setRoleId(adminRoleId);
        rolePermission.setPermissionId(permission.getId());
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

    private College createTestCollege(String name) {
        College college = new College();
        college.setName(name);
        return college;
    }

    // ==================== POST /api/v1/admin/colleges ====================

    /**
     * 集成测试：创建学院应成功
     */
    @Test
    @DisplayName("集成测试：创建学院应成功")
    void createCollege_shouldCreateSuccessfully() {
        // 准备
        CreateCollegeRequestDTO request = CreateCollegeRequestDTO.builder()
                .name(TEST_NAME_1)
                .build();

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<CreateCollegeRequestDTO> entity = new HttpEntity<>(request, headers);

        // 执行
        ResponseEntity<ResponseMessage<CollegeDTO>> response = restTemplate.exchange(
                "/api/v1/admin/colleges",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ResponseMessage<CollegeDTO>>() {
                });

        // 验证
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());

        CollegeDTO created = response.getBody().getData();
        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals(TEST_NAME_1, created.getName());
    }

    /**
     * 集成测试：创建学院时名称重复应返回400
     */
    @Test
    @DisplayName("集成测试：创建学院时名称重复应返回400")
    void createCollege_duplicateName_shouldReturn400() {
        // 准备：先创建一个学院
        RepositoryTestObjects.insert(collegeMapper, createTestCollege(TEST_NAME_1), CollegeDO.class);

        CreateCollegeRequestDTO request = CreateCollegeRequestDTO.builder()
                .name(TEST_NAME_1)
                .build();

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<CreateCollegeRequestDTO> entity = new HttpEntity<>(request, headers);

        // 执行
        ResponseEntity<ResponseMessage<CollegeDTO>> response = restTemplate.exchange(
                "/api/v1/admin/colleges",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ResponseMessage<CollegeDTO>>() {
                });

        // 验证
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getCode());
        assertTrue(response.getBody().getMsg().contains("已存在"));
    }

    /**
     * 集成测试：创建学院时名称为空应返回400
     */
    @Test
    @DisplayName("集成测试：创建学院时名称为空应返回400")
    void createCollege_emptyName_shouldReturn400() {
        // 准备
        CreateCollegeRequestDTO request = CreateCollegeRequestDTO.builder()
                .name("")
                .build();

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<CreateCollegeRequestDTO> entity = new HttpEntity<>(request, headers);

        // 执行
        ResponseEntity<ResponseMessage<CollegeDTO>> response = restTemplate.exchange(
                "/api/v1/admin/colleges",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ResponseMessage<CollegeDTO>>() {
                });

        // 验证
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    /**
     * 集成测试：未认证用户创建学院应返回401
     */
    @Test
    @DisplayName("集成测试：未认证用户创建学院应返回401")
    void createCollege_withoutAuth_shouldReturn401() {
        // 准备
        CreateCollegeRequestDTO request = CreateCollegeRequestDTO.builder()
                .name(TEST_NAME_1)
                .build();

        HttpEntity<CreateCollegeRequestDTO> entity = new HttpEntity<>(request);

        // 执行
        ResponseEntity<ResponseMessage<CollegeDTO>> response = restTemplate.exchange(
                "/api/v1/admin/colleges",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ResponseMessage<CollegeDTO>>() {
                });

        // 验证
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    // ==================== PUT /api/v1/admin/colleges/{id} ====================

    /**
     * 集成测试：更新学院应成功
     */
    @Test
    @DisplayName("集成测试：更新学院应成功")
    void updateCollege_shouldUpdateSuccessfully() {
        // 准备：先创建一个学院
        College college = createTestCollege(TEST_NAME_1);
        RepositoryTestObjects.insert(collegeMapper, college, CollegeDO.class);
        Long collegeId = college.getId();

        UpdateCollegeRequestDTO request = UpdateCollegeRequestDTO.builder()
                .name(TEST_NAME_2)
                .build();

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<UpdateCollegeRequestDTO> entity = new HttpEntity<>(request, headers);

        // 执行
        ResponseEntity<ResponseMessage<CollegeDTO>> response = restTemplate.exchange(
                "/api/v1/admin/colleges/" + collegeId,
                HttpMethod.PUT,
                entity,
                new ParameterizedTypeReference<ResponseMessage<CollegeDTO>>() {
                });

        // 验证
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());

        CollegeDTO updated = response.getBody().getData();
        assertEquals(collegeId, updated.getId());
        assertEquals(TEST_NAME_2, updated.getName());
    }

    /**
     * 集成测试：更新不存在的学院应返回404
     */
    @Test
    @DisplayName("集成测试：更新不存在的学院应返回404")
    void updateCollege_notFound_shouldReturn404() {
        // 准备
        UpdateCollegeRequestDTO request = UpdateCollegeRequestDTO.builder()
                .name(TEST_NAME_2)
                .build();

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<UpdateCollegeRequestDTO> entity = new HttpEntity<>(request, headers);

        // 执行
        ResponseEntity<ResponseMessage<CollegeDTO>> response = restTemplate.exchange(
                "/api/v1/admin/colleges/99999",
                HttpMethod.PUT,
                entity,
                new ParameterizedTypeReference<ResponseMessage<CollegeDTO>>() {
                });

        // 验证
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().getCode());
    }

    /**
     * 集成测试：更新学院时名称重复应返回400
     */
    @Test
    @DisplayName("集成测试：更新学院时名称重复应返回400")
    void updateCollege_duplicateName_shouldReturn400() {
        // 准备：创建两个学院
        College college1 = createTestCollege(TEST_NAME_1);
        RepositoryTestObjects.insert(collegeMapper, college1, CollegeDO.class);

        College college2 = createTestCollege(TEST_NAME_2);
        RepositoryTestObjects.insert(collegeMapper, college2, CollegeDO.class);

        // 尝试将college2的名称改为college1的名称
        UpdateCollegeRequestDTO request = UpdateCollegeRequestDTO.builder()
                .name(TEST_NAME_1)
                .build();

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<UpdateCollegeRequestDTO> entity = new HttpEntity<>(request, headers);

        // 执行
        ResponseEntity<ResponseMessage<CollegeDTO>> response = restTemplate.exchange(
                "/api/v1/admin/colleges/" + college2.getId(),
                HttpMethod.PUT,
                entity,
                new ParameterizedTypeReference<ResponseMessage<CollegeDTO>>() {
                });

        // 验证
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().getCode());
        assertTrue(response.getBody().getMsg().contains("已存在"));
    }

    /**
     * 集成测试：更新学院为相同名称应成功
     */
    @Test
    @DisplayName("集成测试：更新学院为相同名称应成功")
    void updateCollege_sameName_shouldSucceed() {
        // 准备：创建一个学院
        College college = createTestCollege(TEST_NAME_1);
        RepositoryTestObjects.insert(collegeMapper, college, CollegeDO.class);
        Long collegeId = college.getId();

        // 更新为相同名称
        UpdateCollegeRequestDTO request = UpdateCollegeRequestDTO.builder()
                .name(TEST_NAME_1)
                .build();

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<UpdateCollegeRequestDTO> entity = new HttpEntity<>(request, headers);

        // 执行
        ResponseEntity<ResponseMessage<CollegeDTO>> response = restTemplate.exchange(
                "/api/v1/admin/colleges/" + collegeId,
                HttpMethod.PUT,
                entity,
                new ParameterizedTypeReference<ResponseMessage<CollegeDTO>>() {
                });

        // 验证
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(200, response.getBody().getCode());
    }

    // ==================== DELETE /api/v1/admin/colleges/{id} ====================

    /**
     * 集成测试：删除学院应成功
     */
    @Test
    @DisplayName("集成测试：删除学院应成功")
    void deleteCollege_shouldDeleteSuccessfully() {
        // 准备：创建一个学院
        College college = createTestCollege(TEST_NAME_1);
        RepositoryTestObjects.insert(collegeMapper, college, CollegeDO.class);
        Long collegeId = college.getId();

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // 执行
        ResponseEntity<ResponseMessage<Void>> response = restTemplate.exchange(
                "/api/v1/admin/colleges/" + collegeId,
                HttpMethod.DELETE,
                entity,
                new ParameterizedTypeReference<ResponseMessage<Void>>() {
                });

        // 验证
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(200, response.getBody().getCode());

        // 验证数据库中已删除
        College deleted = RepositoryTestObjects.toDomain(collegeMapper.selectById(collegeId), College.class);
        assertNull(deleted);
    }

    /**
     * 集成测试：删除不存在的学院应返回404
     */
    @Test
    @DisplayName("集成测试：删除不存在的学院应返回404")
    void deleteCollege_notFound_shouldReturn404() {
        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // 执行
        ResponseEntity<ResponseMessage<Void>> response = restTemplate.exchange(
                "/api/v1/admin/colleges/99999",
                HttpMethod.DELETE,
                entity,
                new ParameterizedTypeReference<ResponseMessage<Void>>() {
                });

        // 验证
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().getCode());
    }

    /**
     * 集成测试：删除有关联用户的学院应返回400
     */
    @Test
    @DisplayName("集成测试：删除有关联用户的学院应返回400")
    void deleteCollege_withAssociatedUsers_shouldReturn400() {
        // 准备：创建学院
        College college = createTestCollege(TEST_NAME_1);
        RepositoryTestObjects.insert(collegeMapper, college, CollegeDO.class);
        Long collegeId = college.getId();

        // 创建关联用户
        User user = new User();
        user.setStudentId("test001");
        user.setUsername("测试用户");
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("password"));
        user.setCollegeId(collegeId);
        user.setDirection(Direction.COMPUTER_VISION);
        RepositoryTestObjects.insert(userMapper, user, UserDO.class);

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // 执行
        ResponseEntity<ResponseMessage<Void>> response = restTemplate.exchange(
                "/api/v1/admin/colleges/" + collegeId,
                HttpMethod.DELETE,
                entity,
                new ParameterizedTypeReference<ResponseMessage<Void>>() {
                });

        // 验证
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().getCode());
        assertTrue(response.getBody().getMsg().contains("关联用户"));

        // 验证学院未被删除
        College notDeleted = RepositoryTestObjects.toDomain(collegeMapper.selectById(collegeId), College.class);
        assertNotNull(notDeleted);
    }

    /**
     * 集成测试：删除有关联报名记录的学院应返回400
     */
    @Test
    @DisplayName("集成测试：删除有关联报名记录的学院应返回400")
    void deleteCollege_withAssociatedEnrolls_shouldReturn400() {
        // 准备：创建学院
        College college = createTestCollege(TEST_NAME_1);
        RepositoryTestObjects.insert(collegeMapper, college, CollegeDO.class);
        Long collegeId = college.getId();

        // 创建关联报名记录
        Enroll enroll = Enroll.builder()
                .username("测试报名者")
                .studentId("test002")
                .collegeId(collegeId)
                .major("计算机科学与技术")
                .direction(Direction.COMPUTER_VISION)
                .status(EnrollStatus.PENDING)
                .build();
        RepositoryTestObjects.insert(enrollMapper, enroll, EnrollDO.class);

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // 执行
        ResponseEntity<ResponseMessage<Void>> response = restTemplate.exchange(
                "/api/v1/admin/colleges/" + collegeId,
                HttpMethod.DELETE,
                entity,
                new ParameterizedTypeReference<ResponseMessage<Void>>() {
                });

        // 验证
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().getCode());
        assertTrue(response.getBody().getMsg().contains("关联报名记录"));

        // 验证学院未被删除
        College notDeleted = RepositoryTestObjects.toDomain(collegeMapper.selectById(collegeId), College.class);
        assertNotNull(notDeleted);
    }

    /**
     * 集成测试：未认证用户删除学院应返回401
     */
    @Test
    @DisplayName("集成测试：未认证用户删除学院应返回401")
    void deleteCollege_withoutAuth_shouldReturn401() {
        // 准备：创建一个学院
        College college = createTestCollege(TEST_NAME_1);
        RepositoryTestObjects.insert(collegeMapper, college, CollegeDO.class);

        // 执行：不携带认证信息
        HttpEntity<Void> entity = new HttpEntity<>(null);

        ResponseEntity<ResponseMessage<Void>> response = restTemplate.exchange(
                "/api/v1/admin/colleges/" + college.getId(),
                HttpMethod.DELETE,
                entity,
                new ParameterizedTypeReference<ResponseMessage<Void>>() {
                });

        // 验证
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
