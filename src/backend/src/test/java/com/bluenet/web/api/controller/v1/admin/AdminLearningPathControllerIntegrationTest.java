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
import com.bluenet.web.api.dto.learningpath.CreateLearningStepRequestDTO;
import com.bluenet.web.api.dto.learningpath.DirectionLearningPathDTO;
import com.bluenet.web.api.dto.learningpath.LearningStepDTO;
import com.bluenet.web.api.dto.learningpath.UpdateLearningStepRequestDTO;
import com.bluenet.web.domain.model.entity.Permission;
import com.bluenet.web.domain.model.entity.Role;
import com.bluenet.web.domain.model.entity.RolePermission;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.infrastructure.repository.mapper.PermissionMapper;
import com.bluenet.web.infrastructure.repository.mapper.RoleMapper;
import com.bluenet.web.infrastructure.repository.mapper.RolePermissionMapper;
import com.bluenet.web.infrastructure.repository.mapper.UserMapper;
import com.bluenet.web.infrastructure.security.cache.PermissionCache;
import com.bluenet.web.testcontainers.TestcontainersConfiguration;

/**
 * AdminLearningPathController 集成测试
 * <p>
 * 测试管理接口 CRUD 操作，包括权限验证、参数校验和业务异常处理
 * </p>
 */
@DisplayName("AdminLearningPathController 集成测试")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@Import(TestcontainersConfiguration.class)
class AdminLearningPathControllerIntegrationTest extends BaseIntegrationTest {

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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PermissionCache permissionCache;

    private static final String TEST_STUDENT_ID = "admin001";
    private static final String TEST_PASSWORD = "adminPassword123";
    private static final String CV_SLUG = "cv";
    private static final String INVALID_SLUG = "invalid";

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

        // 创建学习路径相关权限
        createPermission("direction-learning-path:create", "创建学习步骤");
        createPermission("direction-learning-path:update", "更新学习步骤");
        createPermission("direction-learning-path:delete", "删除学习步骤");
        createPermission("direction-learning-path:view", "查看学习路径");

        // 刷新权限缓存
        permissionCache.refresh();

        // 创建管理员用户
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

        // 登录获取 Cookie 和 CSRF Token
        loginAndGetCookies();
    }

    private void createPermission(String value, String name) {
        Permission permission = Permission.create(name, value, null, null, null);
        RepositoryTestObjects.insert(permissionMapper, permission, PermissionDO.class);

        // 关联到管理员角色
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

    private LearningStepDTO getFirstStepFromCV() {
        ResponseEntity<ResponseMessage<DirectionLearningPathDTO>> response = restTemplate.exchange(
                "/api/v1/directions/" + CV_SLUG + "/learning-path",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<DirectionLearningPathDTO>>() {
                });
        return response.getBody().getData().getSteps().get(0);
    }

    private List<LearningStepDTO> getAllStepsFromCV() {
        ResponseEntity<ResponseMessage<DirectionLearningPathDTO>> response = restTemplate.exchange(
                "/api/v1/directions/" + CV_SLUG + "/learning-path",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<DirectionLearningPathDTO>>() {
                });
        return response.getBody().getData().getSteps();
    }

    // ==================== POST /api/v1/admin/directions/{slug}/learning-steps
    // ====================

    /**
     * 集成测试：创建学习步骤时无效的方向标识应返回404
     */
    @Test
    @DisplayName("集成测试：创建学习步骤时无效的方向标识应返回404")
    void createStep_invalidSlug_shouldReturn404() {
        // 准备
        CreateLearningStepRequestDTO request = CreateLearningStepRequestDTO.builder()
                .stepNumber(1)
                .title("测试步骤")
                .videoUrl("https://example.com/test.mp4")
                .build();

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<CreateLearningStepRequestDTO> entity = new HttpEntity<>(request, headers);

        // 执行
        ResponseEntity<ResponseMessage<LearningStepDTO>> response = restTemplate.exchange(
                "/api/v1/admin/directions/" + INVALID_SLUG + "/learning-steps",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ResponseMessage<LearningStepDTO>>() {
                });

        // 验证：实际返回404状态码
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    /**
     * 集成测试：创建学习步骤时步骤序号为空应返回400
     */
    @Test
    @DisplayName("集成测试：创建学习步骤时步骤序号为空应返回400")
    void createStep_nullStepNumber_shouldReturn400() {
        // 准备
        CreateLearningStepRequestDTO request = CreateLearningStepRequestDTO.builder()
                .stepNumber(null)
                .title("测试步骤")
                .videoUrl("https://example.com/test.mp4")
                .build();

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<CreateLearningStepRequestDTO> entity = new HttpEntity<>(request, headers);

        // 执行
        ResponseEntity<ResponseMessage<LearningStepDTO>> response = restTemplate.exchange(
                "/api/v1/admin/directions/" + CV_SLUG + "/learning-steps",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ResponseMessage<LearningStepDTO>>() {
                });

        // 验证
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    /**
     * 集成测试：创建学习步骤时步骤序号小于1应返回400
     */
    @Test
    @DisplayName("集成测试：创建学习步骤时步骤序号小于1应返回400")
    void createStep_stepNumberLessThanOne_shouldReturn400() {
        // 准备
        CreateLearningStepRequestDTO request = CreateLearningStepRequestDTO.builder()
                .stepNumber(0)
                .title("测试步骤")
                .videoUrl("https://example.com/test.mp4")
                .build();

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<CreateLearningStepRequestDTO> entity = new HttpEntity<>(request, headers);

        // 执行
        ResponseEntity<ResponseMessage<LearningStepDTO>> response = restTemplate.exchange(
                "/api/v1/admin/directions/" + CV_SLUG + "/learning-steps",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ResponseMessage<LearningStepDTO>>() {
                });

        // 验证
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    /**
     * 集成测试：创建学习步骤时步骤序号大于100应返回400
     */
    @Test
    @DisplayName("集成测试：创建学习步骤时步骤序号大于100应返回400")
    void createStep_stepNumberGreaterThan100_shouldReturn400() {
        // 准备
        CreateLearningStepRequestDTO request = CreateLearningStepRequestDTO.builder()
                .stepNumber(101)
                .title("测试步骤")
                .videoUrl("https://example.com/test.mp4")
                .build();

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<CreateLearningStepRequestDTO> entity = new HttpEntity<>(request, headers);

        // 执行
        ResponseEntity<ResponseMessage<LearningStepDTO>> response = restTemplate.exchange(
                "/api/v1/admin/directions/" + CV_SLUG + "/learning-steps",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ResponseMessage<LearningStepDTO>>() {
                });

        // 验证
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    /**
     * 集成测试：创建学习步骤时标题为空应返回400
     */
    @Test
    @DisplayName("集成测试：创建学习步骤时标题为空应返回400")
    void createStep_emptyTitle_shouldReturn400() {
        // 准备：使用序号5（因为1-4已被迁移脚本占用）
        CreateLearningStepRequestDTO request = CreateLearningStepRequestDTO.builder()
                .stepNumber(5)
                .title("")
                .videoUrl("https://example.com/test.mp4")
                .build();

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<CreateLearningStepRequestDTO> entity = new HttpEntity<>(request, headers);

        // 执行
        ResponseEntity<ResponseMessage<LearningStepDTO>> response = restTemplate.exchange(
                "/api/v1/admin/directions/" + CV_SLUG + "/learning-steps",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ResponseMessage<LearningStepDTO>>() {
                });

        // 验证
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    /**
     * 集成测试：未认证用户创建学习步骤应返回401
     */
    @Test
    @DisplayName("集成测试：未认证用户创建学习步骤应返回401")
    void createStep_withoutAuth_shouldReturn401() {
        // 准备：使用序号5（因为1-4已被迁移脚本占用）
        CreateLearningStepRequestDTO request = CreateLearningStepRequestDTO.builder()
                .stepNumber(5)
                .title("测试步骤")
                .videoUrl("https://example.com/test.mp4")
                .build();

        HttpEntity<CreateLearningStepRequestDTO> entity = new HttpEntity<>(request);

        // 执行
        ResponseEntity<ResponseMessage<LearningStepDTO>> response = restTemplate.exchange(
                "/api/v1/admin/directions/" + CV_SLUG + "/learning-steps",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ResponseMessage<LearningStepDTO>>() {
                });

        // 验证
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    // ==================== PUT /api/v1/admin/directions/learning-steps/{id}
    // ====================

    /**
     * 集成测试：更新学习步骤应成功
     */
    @Test
    @DisplayName("集成测试：更新学习步骤应成功")
    void updateStep_shouldUpdateSuccessfully() {
        // 准备：获取已存在的步骤
        LearningStepDTO existingStep = getFirstStepFromCV();
        Long stepId = existingStep.getId();

        UpdateLearningStepRequestDTO request = UpdateLearningStepRequestDTO.builder()
                .stepNumber(1)
                .title("Python基础（更新）")
                .videoUrl("https://example.com/cv1_updated.mp4")
                .build();

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<UpdateLearningStepRequestDTO> entity = new HttpEntity<>(request, headers);

        // 执行
        ResponseEntity<ResponseMessage<LearningStepDTO>> response = restTemplate.exchange(
                "/api/v1/admin/directions/learning-steps/" + stepId,
                HttpMethod.PUT,
                entity,
                new ParameterizedTypeReference<ResponseMessage<LearningStepDTO>>() {
                });

        // 验证
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());

        LearningStepDTO updated = response.getBody().getData();
        assertEquals(stepId, updated.getId());
        assertEquals(1, updated.getStepNumber());
        assertEquals("Python基础（更新）", updated.getTitle());
        assertEquals("https://example.com/cv1_updated.mp4", updated.getVideoLink());
    }

    /**
     * 集成测试：更新学习步骤时步骤序号冲突应返回400
     */
    @Test
    @DisplayName("集成测试：更新学习步骤时步骤序号冲突应返回400")
    void updateStep_duplicateStepNumber_shouldReturn400() {
        // 准备：获取两个已存在的步骤
        List<LearningStepDTO> steps = getAllStepsFromCV();
        LearningStepDTO step1 = steps.get(0);
        LearningStepDTO step2 = steps.get(1);

        // 尝试将step2的序号改为step1的序号
        UpdateLearningStepRequestDTO request = UpdateLearningStepRequestDTO.builder()
                .stepNumber(step1.getStepNumber())
                .title("测试步骤")
                .videoUrl("https://example.com/test.mp4")
                .build();

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<UpdateLearningStepRequestDTO> entity = new HttpEntity<>(request, headers);

        // 执行
        ResponseEntity<ResponseMessage<LearningStepDTO>> response = restTemplate.exchange(
                "/api/v1/admin/directions/learning-steps/" + step2.getId(),
                HttpMethod.PUT,
                entity,
                new ParameterizedTypeReference<ResponseMessage<LearningStepDTO>>() {
                });

        // 验证
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().getCode());
        assertTrue(response.getBody().getMsg().contains("已存在"));
    }

    /**
     * 集成测试：更新学习步骤为相同序号应成功
     */
    @Test
    @DisplayName("集成测试：更新学习步骤为相同序号应成功")
    void updateStep_sameStepNumber_shouldSucceed() {
        // 准备：获取已存在的步骤
        LearningStepDTO existingStep = getFirstStepFromCV();
        Long stepId = existingStep.getId();

        // 更新为相同序号
        UpdateLearningStepRequestDTO request = UpdateLearningStepRequestDTO.builder()
                .stepNumber(existingStep.getStepNumber())
                .title("Python基础（更新）")
                .videoUrl("https://example.com/cv1_updated.mp4")
                .build();

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<UpdateLearningStepRequestDTO> entity = new HttpEntity<>(request, headers);

        // 执行
        ResponseEntity<ResponseMessage<LearningStepDTO>> response = restTemplate.exchange(
                "/api/v1/admin/directions/learning-steps/" + stepId,
                HttpMethod.PUT,
                entity,
                new ParameterizedTypeReference<ResponseMessage<LearningStepDTO>>() {
                });

        // 验证
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(200, response.getBody().getCode());
    }

    /**
     * 集成测试：更新学习步骤时参数验证失败应返回400
     */
    @Test
    @DisplayName("集成测试：更新学习步骤时参数验证失败应返回400")
    void updateStep_invalidParams_shouldReturn400() {
        // 准备：获取已存在的步骤
        LearningStepDTO existingStep = getFirstStepFromCV();
        Long stepId = existingStep.getId();

        UpdateLearningStepRequestDTO request = UpdateLearningStepRequestDTO.builder()
                .stepNumber(null)
                .title("")
                .videoUrl("https://example.com/test.mp4")
                .build();

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<UpdateLearningStepRequestDTO> entity = new HttpEntity<>(request, headers);

        // 执行
        ResponseEntity<ResponseMessage<LearningStepDTO>> response = restTemplate.exchange(
                "/api/v1/admin/directions/learning-steps/" + stepId,
                HttpMethod.PUT,
                entity,
                new ParameterizedTypeReference<ResponseMessage<LearningStepDTO>>() {
                });

        // 验证
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    /**
     * 集成测试：未认证用户更新学习步骤应返回401
     */
    @Test
    @DisplayName("集成测试：未认证用户更新学习步骤应返回401")
    void updateStep_withoutAuth_shouldReturn401() {
        // 准备
        UpdateLearningStepRequestDTO request = UpdateLearningStepRequestDTO.builder()
                .stepNumber(1)
                .title("测试步骤")
                .videoUrl("https://example.com/test.mp4")
                .build();

        HttpEntity<UpdateLearningStepRequestDTO> entity = new HttpEntity<>(request);

        // 执行
        ResponseEntity<ResponseMessage<LearningStepDTO>> response = restTemplate.exchange(
                "/api/v1/admin/directions/learning-steps/1",
                HttpMethod.PUT,
                entity,
                new ParameterizedTypeReference<ResponseMessage<LearningStepDTO>>() {
                });

        // 验证
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    // ==================== DELETE /api/v1/admin/directions/learning-steps/{id}
    // ====================

    /**
     * 集成测试：删除学习步骤应成功
     */
    @Test
    @DisplayName("集成测试：删除学习步骤应成功")
    void deleteStep_shouldDeleteSuccessfully() {
        // 准备：获取已存在的步骤
        LearningStepDTO existingStep = getFirstStepFromCV();
        Long stepId = existingStep.getId();

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // 执行
        ResponseEntity<ResponseMessage<Void>> response = restTemplate.exchange(
                "/api/v1/admin/directions/learning-steps/" + stepId,
                HttpMethod.DELETE,
                entity,
                new ParameterizedTypeReference<ResponseMessage<Void>>() {
                });

        // 验证
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(200, response.getBody().getCode());
    }

    /**
     * 集成测试：未认证用户删除学习步骤应返回401
     */
    @Test
    @DisplayName("集成测试：未认证用户删除学习步骤应返回401")
    void deleteStep_withoutAuth_shouldReturn401() {
        // 执行：不携带认证信息
        HttpEntity<Void> entity = new HttpEntity<>(null);

        ResponseEntity<ResponseMessage<Void>> response = restTemplate.exchange(
                "/api/v1/admin/directions/learning-steps/1",
                HttpMethod.DELETE,
                entity,
                new ParameterizedTypeReference<ResponseMessage<Void>>() {
                });

        // 验证
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
