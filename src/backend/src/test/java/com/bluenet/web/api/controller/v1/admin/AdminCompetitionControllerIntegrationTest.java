package com.bluenet.web.api.controller.v1.admin;

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
import com.bluenet.web.api.dto.competition.*;
import com.bluenet.web.domain.model.entity.*;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.FileType;

import com.bluenet.web.infrastructure.repository.mapper.*;
import com.bluenet.web.infrastructure.security.cache.PermissionCache;
import com.bluenet.web.testcontainers.TestcontainersConfiguration;

/**
 * AdminCompetitionController集成测试
 */
@DisplayName("AdminCompetitionController 集成测试")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@Import(TestcontainersConfiguration.class)
class AdminCompetitionControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CompetitionMapper competitionMapper;

    @Autowired
    private FileMapper fileMapper;

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
    private static final String TEST_FILE_URL = "http://example.com/logo.png";
    private static final String TEST_FILE_NAME = "logo.png";
    private static final FileType TEST_FILE_TYPE = FileType.NORMAL_IMG;

    private List<String> authCookies;
    private String csrfToken;
    private Long adminRoleId;
    private Long testFileId;

    @BeforeEach
    void setUpTestData() {
        // 创建测试文件（不指定ID，让数据库自动生成）
        File file = File.builder().name(TEST_FILE_NAME).url(TEST_FILE_URL).type(TEST_FILE_TYPE).build();
        fileMapper.insert(file);
        testFileId = file.getId();

        // 查询已存在的管理员角色（由 Flyway 迁移脚本初始化）
        Role adminRole = roleMapper.selectByName("SUPER_ADMIN");
        if (adminRole == null) {
            throw new IllegalStateException("SUPER_ADMIN 角色不存在，请检查数据库迁移脚本");
        }
        adminRoleId = adminRole.getId();

        // 创建竞赛相关权限
        createPermission("competition:create", "创建竞赛");
        createPermission("competition:update", "更新竞赛");
        createPermission("competition:delete", "删除竞赛");
        createPermission("competition:sort", "调整竞赛排序");
        createPermission("competition:update-logo", "更新竞赛Logo");
        createPermission("competition:update-cover", "更新竞赛封面");

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
        userMapper.insert(adminUser);

        // 登录获取 Cookie 和 CSRF Token
        loginAndGetCookies();
    }

    private void createPermission(String value, String name) {
        Permission permission = new Permission();
        permission.setName(name);
        permission.setValue(value);
        permissionMapper.insert(permission);

        // 关联到管理员角色
        RolePermission rolePermission = new RolePermission();
        rolePermission.setRoleId(adminRoleId);
        rolePermission.setPermissionId(permission.getId());
        rolePermissionMapper.insert(rolePermission);
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

    // ==================== POST /api/v1/admin/competitions ====================

    /**
     * 集成测试：创建竞赛应成功
     */
    @Test
    @DisplayName("集成测试：创建竞赛应成功")
    void createCompetition_shouldCreateSuccessfully() {
        // 准备
        CreateCompetitionRequestDTO request = CreateCompetitionRequestDTO.builder()
                .name("新竞赛")
                .shortName("NEW")
                .logoFileId(testFileId)
                .summary("新竞赛简介")
                .detail("新竞赛详细介绍")
                .build();

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<CreateCompetitionRequestDTO> entity = new HttpEntity<>(request, headers);

        // 执行
        ResponseEntity<ResponseMessage<CompetitionBriefDTO>> response = restTemplate.exchange(
                "/api/v1/admin/competitions",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ResponseMessage<CompetitionBriefDTO>>() {
                });

        // 验证
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());

        CompetitionBriefDTO created = response.getBody().getData();
        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("新竞赛", created.getName());
        assertEquals("NEW", created.getShortName());
    }

    /**
     * 集成测试：创建竞赛时名称为空应返回400
     */
    @Test
    @DisplayName("集成测试：创建竞赛时名称为空应返回400")
    void createCompetition_withEmptyName_shouldReturn400() {
        // 准备
        CreateCompetitionRequestDTO request = CreateCompetitionRequestDTO.builder().name("").build();

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<CreateCompetitionRequestDTO> entity = new HttpEntity<>(request, headers);

        // 执行
        ResponseEntity<ResponseMessage<CompetitionBriefDTO>> response = restTemplate.exchange(
                "/api/v1/admin/competitions",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ResponseMessage<CompetitionBriefDTO>>() {
                });

        // 验证
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    /**
     * 集成测试：未认证用户创建竞赛应返回401
     */
    @Test
    @DisplayName("集成测试：未认证用户创建竞赛应返回401")
    void createCompetition_withoutAuth_shouldReturn401() {
        // 准备
        CreateCompetitionRequestDTO request = CreateCompetitionRequestDTO.builder().name("新竞赛").build();

        HttpEntity<CreateCompetitionRequestDTO> entity = new HttpEntity<>(request);

        // 执行
        ResponseEntity<ResponseMessage<CompetitionBriefDTO>> response = restTemplate.exchange(
                "/api/v1/admin/competitions",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ResponseMessage<CompetitionBriefDTO>>() {
                });

        // 验证
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    // ==================== PUT /api/v1/admin/competitions/{id} ====================

    /**
     * 集成测试：更新竞赛应成功
     */
    @Test
    @DisplayName("集成测试：更新竞赛应成功")
    void updateCompetition_shouldUpdateSuccessfully() {
        // 准备：先创建一个竞赛
        Competition competition = new Competition();
        competition.setName("原竞赛名");
        competition.setShortName("OLD");
        competition.setSummary("原简介");
        competition.setSortOrder(0);
        competitionMapper.insert(competition);

        UpdateCompetitionRequestDTO request = UpdateCompetitionRequestDTO.builder()
                .name("更新后的竞赛名")
                .shortName("NEW")
                .summary("更新后的简介")
                .build();

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<UpdateCompetitionRequestDTO> entity = new HttpEntity<>(request, headers);

        // 执行
        ResponseEntity<ResponseMessage<CompetitionBriefDTO>> response = restTemplate.exchange(
                "/api/v1/admin/competitions/" + competition.getId(),
                HttpMethod.PUT,
                entity,
                new ParameterizedTypeReference<ResponseMessage<CompetitionBriefDTO>>() {
                });

        // 验证
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());

        CompetitionBriefDTO updated = response.getBody().getData();
        assertEquals("更新后的竞赛名", updated.getName());
    }

    /**
     * 集成测试：更新不存在的竞赛应返回404
     */
    @Test
    @DisplayName("集成测试：更新不存在的竞赛应返回404")
    void updateCompetition_notFound_shouldReturn404() {
        // 准备
        UpdateCompetitionRequestDTO request = UpdateCompetitionRequestDTO.builder().name("更新后的竞赛名").build();

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<UpdateCompetitionRequestDTO> entity = new HttpEntity<>(request, headers);

        // 执行
        ResponseEntity<ResponseMessage<CompetitionBriefDTO>> response = restTemplate.exchange(
                "/api/v1/admin/competitions/99999",
                HttpMethod.PUT,
                entity,
                new ParameterizedTypeReference<ResponseMessage<CompetitionBriefDTO>>() {
                });

        // 验证
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().getCode());
    }

    // ==================== DELETE /api/v1/admin/competitions/{id}
    // ====================

    /**
     * 集成测试：删除竞赛应成功
     */
    @Test
    @DisplayName("集成测试：删除竞赛应成功")
    void deleteCompetition_shouldDeleteSuccessfully() {
        // 准备：先创建一个竞赛
        Competition competition = new Competition();
        competition.setName("要删除的竞赛");
        competition.setSortOrder(0);
        competitionMapper.insert(competition);
        Long competitionId = competition.getId();

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // 执行
        ResponseEntity<ResponseMessage<Void>> response = restTemplate.exchange(
                "/api/v1/admin/competitions/" + competitionId,
                HttpMethod.DELETE,
                entity,
                new ParameterizedTypeReference<ResponseMessage<Void>>() {
                });

        // 验证
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(200, response.getBody().getCode());

        // 验证数据库中已删除
        Competition deleted = competitionMapper.selectById(competitionId);
        assertNull(deleted);
    }

    /**
     * 集成测试：删除不存在的竞赛应返回404
     */
    @Test
    @DisplayName("集成测试：删除不存在的竞赛应返回404")
    void deleteCompetition_notFound_shouldReturn404() {
        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // 执行
        ResponseEntity<ResponseMessage<Void>> response = restTemplate.exchange(
                "/api/v1/admin/competitions/99999",
                HttpMethod.DELETE,
                entity,
                new ParameterizedTypeReference<ResponseMessage<Void>>() {
                });

        // 验证
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().getCode());
    }

    /**
     * 集成测试：删除竞赛应成功
     */
    @Test
    @DisplayName("集成测试：删除竞赛应成功")
    void deleteCompetition_shouldSucceed() {
        // 准备：创建竞赛
        Competition competition = new Competition();
        competition.setName("要删除的竞赛");
        competition.setSortOrder(0);
        competitionMapper.insert(competition);
        Long competitionId = competition.getId();

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // 执行
        ResponseEntity<ResponseMessage<Void>> response = restTemplate.exchange(
                "/api/v1/admin/competitions/" + competitionId,
                HttpMethod.DELETE,
                entity,
                new ParameterizedTypeReference<ResponseMessage<Void>>() {
                });

        // 验证
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // 验证竞赛已被删除
        Competition deletedCompetition = competitionMapper.selectById(competitionId);
        assertNull(deletedCompetition);
    }

    // ==================== PUT /api/v1/admin/competitions/{id}/sort
    // ====================

    /**
     * 集成测试：调整排序应成功
     */
    @Test
    @DisplayName("集成测试：调整排序应成功")
    void updateSortOrder_shouldUpdateSuccessfully() {
        // 准备：先创建一个竞赛
        Competition competition = new Competition();
        competition.setName("要调整排序的竞赛");
        competition.setSortOrder(0);
        competitionMapper.insert(competition);

        UpdateSortOrderRequestDTO request = UpdateSortOrderRequestDTO.builder().sortOrder(100).build();

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<UpdateSortOrderRequestDTO> entity = new HttpEntity<>(request, headers);

        // 执行
        ResponseEntity<ResponseMessage<Void>> response = restTemplate.exchange(
                "/api/v1/admin/competitions/" + competition.getId() + "/sort",
                HttpMethod.PUT,
                entity,
                new ParameterizedTypeReference<ResponseMessage<Void>>() {
                });

        // 验证
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(200, response.getBody().getCode());

        // 验证数据库中排序已更新
        Competition updated = competitionMapper.selectById(competition.getId());
        assertEquals(100, updated.getSortOrder());
    }

    /**
     * 集成测试：调整不存在的竞赛排序应返回404
     */
    @Test
    @DisplayName("集成测试：调整不存在的竞赛排序应返回404")
    void updateSortOrder_notFound_shouldReturn404() {
        UpdateSortOrderRequestDTO request = UpdateSortOrderRequestDTO.builder().sortOrder(100).build();

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<UpdateSortOrderRequestDTO> entity = new HttpEntity<>(request, headers);

        // 执行
        ResponseEntity<ResponseMessage<Void>> response = restTemplate.exchange(
                "/api/v1/admin/competitions/99999/sort",
                HttpMethod.PUT,
                entity,
                new ParameterizedTypeReference<ResponseMessage<Void>>() {
                });

        // 验证
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().getCode());
    }

    // ==================== PUT /api/v1/admin/competitions/{id}/logo
    // ====================

    /**
     * 集成测试：更新竞赛Logo应成功
     */
    @Test
    @DisplayName("集成测试：更新竞赛Logo应成功")
    void updateLogo_shouldUpdateSuccessfully() {
        // 准备：创建竞赛
        Competition competition = new Competition();
        competition.setName("要更新Logo的竞赛");
        competition.setSortOrder(0);
        competitionMapper.insert(competition);

        // 创建Logo文件
        File logoFile = File.builder()
                .name("new-logo.png")
                .url("http://example.com/new-logo.png")
                .type(FileType.NORMAL_IMG)
                .build();
        fileMapper.insert(logoFile);

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // 执行
        ResponseEntity<ResponseMessage<Void>> response = restTemplate.exchange(
                "/api/v1/admin/competitions/" + competition.getId() + "/logo?fileId=" + logoFile.getId(),
                HttpMethod.PUT,
                entity,
                new ParameterizedTypeReference<ResponseMessage<Void>>() {
                });

        // 验证
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // 验证数据库中Logo已更新
        Competition updated = competitionMapper.selectById(competition.getId());
        assertEquals(logoFile.getId(), updated.getLogoFileId());
    }

    /**
     * 集成测试：为不存在的竞赛更新Logo应返回404
     */
    @Test
    @DisplayName("集成测试：为不存在的竞赛更新Logo应返回404")
    void updateLogo_competitionNotFound_shouldReturn404() {
        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // 执行
        ResponseEntity<ResponseMessage<Void>> response = restTemplate.exchange(
                "/api/v1/admin/competitions/99999/logo?fileId=" + testFileId,
                HttpMethod.PUT,
                entity,
                new ParameterizedTypeReference<ResponseMessage<Void>>() {
                });

        // 验证
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    /**
     * 集成测试：使用不存在的文件更新Logo应返回404
     */
    @Test
    @DisplayName("集成测试：使用不存在的文件更新Logo应返回404")
    void updateLogo_fileNotFound_shouldReturn404() {
        // 准备：创建竞赛
        Competition competition = new Competition();
        competition.setName("竞赛");
        competition.setSortOrder(0);
        competitionMapper.insert(competition);

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // 执行：使用不存在的文件ID
        ResponseEntity<ResponseMessage<Void>> response = restTemplate.exchange(
                "/api/v1/admin/competitions/" + competition.getId() + "/logo?fileId=99999",
                HttpMethod.PUT,
                entity,
                new ParameterizedTypeReference<ResponseMessage<Void>>() {
                });

        // 验证
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ==================== PUT /api/v1/admin/competitions/{id}/cover
    // ====================

    /**
     * 集成测试：更新竞赛封面应成功
     */
    @Test
    @DisplayName("集成测试：更新竞赛封面应成功")
    void updateCover_shouldUpdateSuccessfully() {
        // 准备：创建竞赛
        Competition competition = new Competition();
        competition.setName("要更新封面的竞赛");
        competition.setSortOrder(0);
        competitionMapper.insert(competition);

        // 创建封面文件
        File coverFile = File.builder()
                .name("cover.jpg")
                .url("http://example.com/cover.jpg")
                .type(FileType.NORMAL_IMG)
                .build();
        fileMapper.insert(coverFile);

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // 执行
        ResponseEntity<ResponseMessage<Void>> response = restTemplate.exchange(
                "/api/v1/admin/competitions/" + competition.getId() + "/cover?fileId=" + coverFile.getId(),
                HttpMethod.PUT,
                entity,
                new ParameterizedTypeReference<ResponseMessage<Void>>() {
                });

        // 验证
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // 验证数据库中封面已更新
        Competition updated = competitionMapper.selectById(competition.getId());
        assertEquals(coverFile.getId(), updated.getCoverFileId());
    }

    /**
     * 集成测试：为不存在的竞赛更新封面应返回404
     */
    @Test
    @DisplayName("集成测试：为不存在的竞赛更新封面应返回404")
    void updateCover_competitionNotFound_shouldReturn404() {
        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // 执行
        ResponseEntity<ResponseMessage<Void>> response = restTemplate.exchange(
                "/api/v1/admin/competitions/99999/cover?fileId=" + testFileId,
                HttpMethod.PUT,
                entity,
                new ParameterizedTypeReference<ResponseMessage<Void>>() {
                });

        // 验证
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    /**
     * 集成测试：使用不存在的文件更新封面应返回404
     */
    @Test
    @DisplayName("集成测试：使用不存在的文件更新封面应返回404")
    void updateCover_fileNotFound_shouldReturn404() {
        // 准备：创建竞赛
        Competition competition = new Competition();
        competition.setName("竞赛");
        competition.setSortOrder(0);
        competitionMapper.insert(competition);

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // 执行：使用不存在的文件ID
        ResponseEntity<ResponseMessage<Void>> response = restTemplate.exchange(
                "/api/v1/admin/competitions/" + competition.getId() + "/cover?fileId=99999",
                HttpMethod.PUT,
                entity,
                new ParameterizedTypeReference<ResponseMessage<Void>>() {
                });

        // 验证
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

}
