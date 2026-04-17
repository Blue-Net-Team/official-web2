package com.bluenet.web.api.controller.v1.admin;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;

import com.bluenet.web.domain.model.enumerate.AchievementType;
import com.bluenet.web.domain.model.enumerate.AwardLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.achievement.AchievementDTO;
import com.bluenet.web.api.dto.achievement.CreateAchievementRequestDTO;
import com.bluenet.web.api.dto.achievement.UpdateAchievementRequestDTO;
import com.bluenet.web.api.dto.auth.StudentIdLoginRequestDTO;
import com.bluenet.web.api.dto.auth.UserAuthResponseDTO;
import com.bluenet.web.domain.model.entity.*;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.infrastructure.repository.mapper.*;
import com.bluenet.web.infrastructure.security.cache.PermissionCache;
import com.bluenet.web.testcontainers.TestcontainersConfiguration;

@DisplayName("AdminAchievementController 集成测试")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@Import({ TestcontainersConfiguration.class, com.bluenet.web.testconfig.TestSecurityConfig.class })
class AdminAchievementControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AchievementMapper achievementMapper;

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
    private static final String TEST_FILE_URL = "http://example.com/achievement.jpg";
    private static final String TEST_FILE_NAME = "achievement.jpg";
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

        createPermission("achievement:create", "创建成就");
        createPermission("achievement:update", "更新成就");
        createPermission("achievement:delete", "删除成就");
        createPermission("achievement:list", "查看成就列表");

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
    @DisplayName("集成测试：创建竞赛成就应成功")
    void createCompetitionAchievement_shouldCreateSuccessfully() {
        CreateAchievementRequestDTO request = CreateAchievementRequestDTO.builder()
                .title("蓝桥杯全国一等奖")
                .type(AchievementType.COMPETITION)
                .relateTo("蓝桥杯")
                .achieveAt(LocalDate.of(2024, 4, 15))
                .awardLevel(AwardLevel.NATIONAL)
                .awardName("一等奖")
                .fileId(testFileId)
                .build();

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<CreateAchievementRequestDTO> entity = new HttpEntity<>(request, headers);

        ResponseEntity<ResponseMessage<AchievementDTO>> response = restTemplate.exchange(
                "/api/v1/admin/achievements",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ResponseMessage<AchievementDTO>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());

        AchievementDTO created = response.getBody().getData();
        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("蓝桥杯全国一等奖", created.getTitle());
        assertEquals(AchievementType.COMPETITION, created.getType());
        assertEquals(AwardLevel.NATIONAL, created.getAwardLevel());
        assertEquals(testFileId, created.getFileId());

        Achievement achievementEntity = achievementMapper.selectById(created.getId());
        assertNotNull(achievementEntity);
        assertEquals("蓝桥杯全国一等奖", achievementEntity.getTitle());
    }

    @Test
    @DisplayName("集成测试：创建论文成就应成功")
    void createPaperAchievement_shouldCreateSuccessfully() {
        CreateAchievementRequestDTO request = CreateAchievementRequestDTO.builder()
                .title("基于深度学习的图像识别研究")
                .type(AchievementType.PAPER)
                .relateTo("计算机学报")
                .achieveAt(LocalDate.of(2024, 3, 20))
                .fileId(testFileId)
                .build();

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<CreateAchievementRequestDTO> entity = new HttpEntity<>(request, headers);

        ResponseEntity<ResponseMessage<AchievementDTO>> response = restTemplate.exchange(
                "/api/v1/admin/achievements",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ResponseMessage<AchievementDTO>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());

        AchievementDTO created = response.getBody().getData();
        assertNotNull(created);
        assertEquals("基于深度学习的图像识别研究", created.getTitle());
        assertEquals(AchievementType.PAPER, created.getType());
        assertEquals("计算机学报", created.getRelateTo());
        assertNull(created.getAwardLevel());
        assertNull(created.getAwardName());
    }

    @Test
    @DisplayName("集成测试：创建成就时缺少必填字段应返回400")
    void createAchievement_withMissingRequiredFields_shouldReturn400() {
        CreateAchievementRequestDTO request = CreateAchievementRequestDTO.builder()
                .type(AchievementType.COMPETITION)
                .achieveAt(LocalDate.of(2024, 4, 15))
                .fileId(testFileId)
                .build();

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<CreateAchievementRequestDTO> entity = new HttpEntity<>(request, headers);

        ResponseEntity<ResponseMessage<AchievementDTO>> response = restTemplate.exchange(
                "/api/v1/admin/achievements",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ResponseMessage<AchievementDTO>>() {
                });

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getCode());
        assertEquals("成就标题不能为空", response.getBody().getMsg());
    }

    @Test
    @DisplayName("集成测试：创建竞赛成就缺少奖项级别应返回400")
    void createCompetitionAchievement_missingAwardLevel_shouldReturn400() {
        CreateAchievementRequestDTO request = CreateAchievementRequestDTO.builder()
                .title("蓝桥杯获奖")
                .type(AchievementType.COMPETITION)
                .achieveAt(LocalDate.of(2024, 4, 15))
                .fileId(testFileId)
                .build();

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<CreateAchievementRequestDTO> entity = new HttpEntity<>(request, headers);

        ResponseEntity<ResponseMessage<AchievementDTO>> response = restTemplate.exchange(
                "/api/v1/admin/achievements",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ResponseMessage<AchievementDTO>>() {
                });

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getCode());
        assertEquals("竞赛成就必须指定奖项级别", response.getBody().getMsg());
    }

    @Test
    @DisplayName("集成测试：未认证用户创建成就应返回401")
    void createAchievement_withoutAuth_shouldReturn401() {
        CreateAchievementRequestDTO request = CreateAchievementRequestDTO.builder()
                .title("测试成就")
                .type(AchievementType.PAPER)
                .achieveAt(LocalDate.of(2024, 4, 15))
                .fileId(testFileId)
                .build();

        ResponseEntity<ResponseMessage<AchievementDTO>> response = restTemplate.exchange(
                "/api/v1/admin/achievements",
                HttpMethod.POST,
                new HttpEntity<>(request),
                new ParameterizedTypeReference<ResponseMessage<AchievementDTO>>() {
                });

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("集成测试：更新成就应成功")
    void updateAchievement_shouldUpdateSuccessfully() {
        Achievement achievement = new Achievement();
        achievement.setTitle("原始标题");
        achievement.setType(AchievementType.COMPETITION);
        achievement.setRelateTo("原始关联项");
        achievement.setAchieveAt(LocalDate.of(2024, 1, 1));
        achievement.setAwardLevel(AwardLevel.NATIONAL);
        achievement.setAwardName("一等奖");
        achievement.setFileId(testFileId);
        achievementMapper.insert(achievement);
        Long achievementId = achievement.getId();

        UpdateAchievementRequestDTO request = UpdateAchievementRequestDTO.builder()
                .title("更新后的标题")
                .type(AchievementType.COMPETITION)
                .relateTo("更新后的关联项")
                .achieveAt(LocalDate.of(2024, 5, 1))
                .awardLevel(AwardLevel.PROVINCIAL)
                .awardName("二等奖")
                .fileId(testFileId)
                .build();

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<UpdateAchievementRequestDTO> entity = new HttpEntity<>(request, headers);

        ResponseEntity<ResponseMessage<AchievementDTO>> response = restTemplate.exchange(
                "/api/v1/admin/achievements/" + achievementId,
                HttpMethod.PUT,
                entity,
                new ParameterizedTypeReference<ResponseMessage<AchievementDTO>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());

        AchievementDTO updated = response.getBody().getData();
        assertNotNull(updated);
        assertEquals(achievementId, updated.getId());
        assertEquals("更新后的标题", updated.getTitle());
        assertEquals("更新后的关联项", updated.getRelateTo());
        assertEquals(AwardLevel.PROVINCIAL, updated.getAwardLevel());
        assertEquals("二等奖", updated.getAwardName());

        Achievement updatedEntity = achievementMapper.selectById(achievementId);
        assertNotNull(updatedEntity);
        assertEquals("更新后的标题", updatedEntity.getTitle());
        assertEquals(AwardLevel.PROVINCIAL, updatedEntity.getAwardLevel());
    }

    @Test
    @DisplayName("集成测试：更新不存在的成就应返回404")
    void updateAchievement_notFound_shouldReturn404() {
        UpdateAchievementRequestDTO request = UpdateAchievementRequestDTO.builder()
                .title("更新后的标题")
                .type(AchievementType.COMPETITION)
                .achieveAt(LocalDate.of(2024, 5, 1))
                .awardLevel(AwardLevel.NATIONAL)
                .fileId(testFileId)
                .build();

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<UpdateAchievementRequestDTO> entity = new HttpEntity<>(request, headers);

        ResponseEntity<ResponseMessage<AchievementDTO>> response = restTemplate.exchange(
                "/api/v1/admin/achievements/99999",
                HttpMethod.PUT,
                entity,
                new ParameterizedTypeReference<ResponseMessage<AchievementDTO>>() {
                });

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getCode());
        assertEquals("成就不存在", response.getBody().getMsg());
    }

    @Test
    @DisplayName("集成测试：删除成就应成功")
    void deleteAchievement_shouldDeleteSuccessfully() {
        Achievement achievement = new Achievement();
        achievement.setTitle("待删除成就");
        achievement.setType(AchievementType.PAPER);
        achievement.setRelateTo("待删除期刊");
        achievement.setAchieveAt(LocalDate.of(2024, 1, 1));
        achievement.setFileId(testFileId);
        achievementMapper.insert(achievement);
        Long achievementId = achievement.getId();

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<ResponseMessage<Void>> response = restTemplate.exchange(
                "/api/v1/admin/achievements/" + achievementId,
                HttpMethod.DELETE,
                entity,
                new ParameterizedTypeReference<ResponseMessage<Void>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(200, response.getBody().getCode());

        Achievement deleted = achievementMapper.selectById(achievementId);
        assertNull(deleted);
    }

    @Test
    @DisplayName("集成测试：删除不存在的成就应返回404")
    void deleteAchievement_notFound_shouldReturn404() {
        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<ResponseMessage<Void>> response = restTemplate.exchange(
                "/api/v1/admin/achievements/99999",
                HttpMethod.DELETE,
                entity,
                new ParameterizedTypeReference<ResponseMessage<Void>>() {
                });

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().getCode());
        assertEquals("成就不存在", response.getBody().getMsg());
    }

    @Test
    @DisplayName("集成测试：权限不足应返回403")
    void createAchievement_insufficientPermission_shouldReturn403() {
        // TODO: 实现权限不足的测试
    }
}
