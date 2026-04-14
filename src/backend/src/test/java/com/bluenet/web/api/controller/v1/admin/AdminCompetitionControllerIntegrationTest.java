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
        File file = File.builder().name(TEST_FILE_NAME).url(TEST_FILE_URL).type(TEST_FILE_TYPE).build();
        fileMapper.insert(file);
        testFileId = file.getId();

        Role adminRole = roleMapper.selectByName("SUPER_ADMIN");
        if (adminRole == null) {
            throw new IllegalStateException("SUPER_ADMIN 角色不存在，请检查数据库迁移脚本");
        }
        adminRoleId = adminRole.getId();

        createPermission("competition:create", "创建竞赛");
        createPermission("competition:update", "更新竞赛");
        createPermission("competition:delete", "删除竞赛");
        createPermission("competition:sort", "调整竞赛排序");

        permissionCache.refresh();

        User adminUser = new User();
        adminUser.setStudentId(TEST_STUDENT_ID);
        adminUser.setUsername("管理员");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword(passwordEncoder.encode(TEST_PASSWORD));
        adminUser.setRoleId(adminRoleId);
        adminUser.setDisable(false);
        adminUser.setDirection(Direction.COMPUTER_VISION);
        userMapper.insert(adminUser);

        loginAndGetCookies();
    }

    private void createPermission(String value, String name) {
        Permission permission = new Permission();
        permission.setName(name);
        permission.setValue(value);
        permissionMapper.insert(permission);

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

    @Test
    @DisplayName("集成测试：创建竞赛应成功")
    void createCompetition_shouldCreateSuccessfully() {
        CompetitionRequestDTO request = CompetitionRequestDTO.builder()
                .name("新竞赛")
                .shortName("NEW")
                .logoFileId(testFileId)
                .summary("新竞赛简介")
                .build();

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<CompetitionRequestDTO> entity = new HttpEntity<>(request, headers);

        ResponseEntity<ResponseMessage<CompetitionResponseDTO>> response = restTemplate.exchange(
                "/api/v1/admin/competitions",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ResponseMessage<CompetitionResponseDTO>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());

        CompetitionResponseDTO created = response.getBody().getData();
        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("新竞赛", created.getName());
        assertEquals("NEW", created.getShortName());
    }

    @Test
    @DisplayName("集成测试：创建竞赛时名称为空应返回400")
    void createCompetition_withEmptyName_shouldReturn400() {
        CompetitionRequestDTO request = CompetitionRequestDTO.builder().name("").build();

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<CompetitionRequestDTO> entity = new HttpEntity<>(request, headers);

        ResponseEntity<ResponseMessage<CompetitionResponseDTO>> response = restTemplate.exchange(
                "/api/v1/admin/competitions",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ResponseMessage<CompetitionResponseDTO>>() {
                });

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("集成测试：未认证用户创建竞赛应返回401")
    void createCompetition_withoutAuth_shouldReturn401() {
        CompetitionRequestDTO request = CompetitionRequestDTO.builder().name("新竞赛").build();

        HttpEntity<CompetitionRequestDTO> entity = new HttpEntity<>(request);

        ResponseEntity<ResponseMessage<CompetitionResponseDTO>> response = restTemplate.exchange(
                "/api/v1/admin/competitions",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ResponseMessage<CompetitionResponseDTO>>() {
                });

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("集成测试：更新竞赛应成功")
    void updateCompetition_shouldUpdateSuccessfully() {
        Competition competition = new Competition();
        competition.setName("原竞赛名");
        competition.setShortName("OLD");
        competition.setSummary("原简介");
        competition.setSortOrder(0);
        competitionMapper.insert(competition);

        CompetitionRequestDTO request = CompetitionRequestDTO.builder()
                .name("更新后的竞赛名")
                .shortName("NEW")
                .summary("更新后的简介")
                .build();

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<CompetitionRequestDTO> entity = new HttpEntity<>(request, headers);

        ResponseEntity<ResponseMessage<CompetitionResponseDTO>> response = restTemplate.exchange(
                "/api/v1/admin/competitions/" + competition.getId(),
                HttpMethod.PUT,
                entity,
                new ParameterizedTypeReference<ResponseMessage<CompetitionResponseDTO>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());

        CompetitionResponseDTO updated = response.getBody().getData();
        assertEquals("更新后的竞赛名", updated.getName());
    }

    @Test
    @DisplayName("集成测试：更新不存在的竞赛应返回404")
    void updateCompetition_notFound_shouldReturn404() {
        CompetitionRequestDTO request = CompetitionRequestDTO.builder().name("更新后的竞赛名").build();

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<CompetitionRequestDTO> entity = new HttpEntity<>(request, headers);

        ResponseEntity<ResponseMessage<CompetitionResponseDTO>> response = restTemplate.exchange(
                "/api/v1/admin/competitions/99999",
                HttpMethod.PUT,
                entity,
                new ParameterizedTypeReference<ResponseMessage<CompetitionResponseDTO>>() {
                });

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().getCode());
    }

    @Test
    @DisplayName("集成测试：删除竞赛应成功")
    void deleteCompetition_shouldDeleteSuccessfully() {
        Competition competition = new Competition();
        competition.setName("要删除的竞赛");
        competition.setSortOrder(0);
        competitionMapper.insert(competition);
        Long competitionId = competition.getId();

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<ResponseMessage<Void>> response = restTemplate.exchange(
                "/api/v1/admin/competitions/" + competitionId,
                HttpMethod.DELETE,
                entity,
                new ParameterizedTypeReference<ResponseMessage<Void>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(200, response.getBody().getCode());

        Competition deleted = competitionMapper.selectById(competitionId);
        assertNull(deleted);
    }

    @Test
    @DisplayName("集成测试：删除不存在的竞赛应返回404")
    void deleteCompetition_notFound_shouldReturn404() {
        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<ResponseMessage<Void>> response = restTemplate.exchange(
                "/api/v1/admin/competitions/99999",
                HttpMethod.DELETE,
                entity,
                new ParameterizedTypeReference<ResponseMessage<Void>>() {
                });

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().getCode());
    }

    @Test
    @DisplayName("集成测试：删除竞赛应成功")
    void deleteCompetition_shouldSucceed() {
        Competition competition = new Competition();
        competition.setName("要删除的竞赛");
        competition.setSortOrder(0);
        competitionMapper.insert(competition);
        Long competitionId = competition.getId();

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<ResponseMessage<Void>> response = restTemplate.exchange(
                "/api/v1/admin/competitions/" + competitionId,
                HttpMethod.DELETE,
                entity,
                new ParameterizedTypeReference<ResponseMessage<Void>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());

        Competition deletedCompetition = competitionMapper.selectById(competitionId);
        assertNull(deletedCompetition);
    }

    @Test
    @DisplayName("集成测试：调整排序应成功")
    void updateSortOrder_shouldUpdateSuccessfully() {
        Competition competition = new Competition();
        competition.setName("要调整排序的竞赛");
        competition.setSortOrder(0);
        competitionMapper.insert(competition);

        UpdateSortOrderRequestDTO request = UpdateSortOrderRequestDTO.builder().sortOrder(100).build();

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<UpdateSortOrderRequestDTO> entity = new HttpEntity<>(request, headers);

        ResponseEntity<ResponseMessage<Void>> response = restTemplate.exchange(
                "/api/v1/admin/competitions/" + competition.getId() + "/sort",
                HttpMethod.PUT,
                entity,
                new ParameterizedTypeReference<ResponseMessage<Void>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(200, response.getBody().getCode());

        Competition updated = competitionMapper.selectById(competition.getId());
        assertEquals(100, updated.getSortOrder());
    }

    @Test
    @DisplayName("集成测试：调整不存在的竞赛排序应返回404")
    void updateSortOrder_notFound_shouldReturn404() {
        UpdateSortOrderRequestDTO request = UpdateSortOrderRequestDTO.builder().sortOrder(100).build();

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<UpdateSortOrderRequestDTO> entity = new HttpEntity<>(request, headers);

        ResponseEntity<ResponseMessage<Void>> response = restTemplate.exchange(
                "/api/v1/admin/competitions/99999/sort",
                HttpMethod.PUT,
                entity,
                new ParameterizedTypeReference<ResponseMessage<Void>>() {
                });

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().getCode());
    }

}
