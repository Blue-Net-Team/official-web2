package com.bluenet.web.api.controller.v1.user;

import com.bluenet.web.infrastructure.repository.dataobject.*;

import com.bluenet.web.testsupport.RepositoryTestObjects;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.auth.StudentIdLoginRequestDTO;
import com.bluenet.web.api.dto.auth.UserAuthResponseDTO;
import com.bluenet.web.api.dto.experience.CreateExperienceRequestDTO;
import com.bluenet.web.api.dto.experience.ExperienceDTO;
import com.bluenet.web.api.dto.experience.UpdateExperienceRequestDTO;
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
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserExperienceController 集成测试 测试用户经历相关接口：/api/v1/user/experiences
 */
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@Import(TestcontainersConfiguration.class)
class UserExperienceControllerIntegrationTest extends BaseIntegrationTest {

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
    private PermissionCache permissionCache;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String TEST_STUDENT_ID = "2024001001";
    private static final String TEST_PASSWORD = "testPassword123";
    private Long testUserId;
    private Long memberRoleId;
    private Long candidateRoleId;

    @BeforeEach
    void setUp() {
        // 创建角色
        createRoles();
        createExperiencePermissions();

        // 创建测试用户（默认为MEMBER角色）
        User user = User.reconstruct(
                null,
                TEST_STUDENT_ID,
                "test@example.com",
                memberRoleId,
                passwordEncoder.encode(TEST_PASSWORD),
                "测试用户",
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

        RepositoryTestObjects.insert(userMapper, user, UserDO.class);
        testUserId = user.getId();
    }

    private void createExperiencePermissions() {
        assignPermissionToMemberRole("user:experience:create");
        assignPermissionToMemberRole("user:experience:update");
        assignPermissionToMemberRole("user:experience:delete");
        permissionCache.refresh();
    }

    private void assignPermissionToMemberRole(String value) {
        Permission permission = RepositoryTestObjects.toDomain(
                permissionMapper.selectOne(
                        new LambdaQueryWrapper<PermissionDO>().eq(PermissionDO::getValue, value)),
                Permission.class);
        if (permission == null) {
            permission = Permission.create(value, value, null, null, null);
            RepositoryTestObjects.insert(permissionMapper, permission, PermissionDO.class);
        }

        Long existingCount = rolePermissionMapper.selectCount(
                new LambdaQueryWrapper<RolePermissionDO>()
                        .eq(RolePermissionDO::getRoleId, memberRoleId)
                        .eq(RolePermissionDO::getPermissionId, permission.getId()));
        if (existingCount == 0) {
            RolePermission rolePermission = RolePermission.create(memberRoleId, permission.getId());
            RepositoryTestObjects.insert(rolePermissionMapper, rolePermission, RolePermissionDO.class);
        }
    }

    private void createRoles() {
        // 创建MEMBER角色
        Role memberRole = RepositoryTestObjects.toDomain(
                roleMapper.selectOne(
                        new LambdaQueryWrapper<RoleDO>().eq(RoleDO::getName, "MEMBER")),
                Role.class);
        if (memberRole == null) {
            memberRole = Role.create("MEMBER");
            RepositoryTestObjects.insert(roleMapper, memberRole, RoleDO.class);
        }
        memberRoleId = memberRole.getId();

        // 创建CANDIDATE角色
        Role candidateRole = RepositoryTestObjects.toDomain(
                roleMapper.selectOne(
                        new LambdaQueryWrapper<RoleDO>().eq(RoleDO::getName, "CANDIDATE")),
                Role.class);
        if (candidateRole == null) {
            candidateRole = Role.create("CANDIDATE");
            RepositoryTestObjects.insert(roleMapper, candidateRole, RoleDO.class);
        }
        candidateRoleId = candidateRole.getId();
    }

    /**
     * 创建指定角色的测试用户
     */
    private Long createUserWithRole(String studentId, String username, Long roleId) {
        User user = User.reconstruct(
                null,
                studentId,
                studentId + "@example.com",
                roleId,
                passwordEncoder.encode(TEST_PASSWORD),
                username,
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
        RepositoryTestObjects.insert(userMapper, user, UserDO.class);
        return user.getId();
    }

    /**
     * 登录并获取 Cookie 和 CSRF Token
     */
    private AuthCookies loginAndGetCookies() {
        return loginAndGetCookies(TEST_STUDENT_ID);
    }

    /**
     * 指定用户登录并获取 Cookie 和 CSRF Token
     */
    private AuthCookies loginAndGetCookies(String studentId) {
        StudentIdLoginRequestDTO loginRequest = new StudentIdLoginRequestDTO();
        loginRequest.setStudentId(studentId);
        loginRequest.setPassword(TEST_PASSWORD);

        ResponseEntity<ResponseMessage<UserAuthResponseDTO>> loginResponse = restTemplate.exchange(
                "/api/v1/auth/login/student-id",
                HttpMethod.POST,
                new HttpEntity<>(loginRequest),
                new ParameterizedTypeReference<ResponseMessage<UserAuthResponseDTO>>() {
                });

        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());

        List<String> cookies = loginResponse.getHeaders().get(HttpHeaders.SET_COOKIE);
        String csrfToken = loginResponse.getBody().getData().getCsrfToken();

        assertNotNull(cookies, "登录响应应包含 Set-Cookie");
        return new AuthCookies(cookies, csrfToken);
    }

    /**
     * 认证信息封装类
     */
    private static class AuthCookies {
        final List<String> cookies;
        final String csrfToken;

        AuthCookies(List<String> cookies, String csrfToken) {
            this.cookies = cookies;
            this.csrfToken = csrfToken;
        }
    }

    /**
     * 创建带认证头的 HttpEntity（GET 请求，不需要 CSRF）
     */
    private <T> HttpEntity<T> withAuth(AuthCookies auth, T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.put(HttpHeaders.COOKIE, auth.cookies);
        return new HttpEntity<>(body, headers);
    }

    private HttpEntity<Void> withAuth(AuthCookies auth) {
        return withAuth(auth, null);
    }

    /**
     * 创建带认证头和 CSRF Token 的 HttpEntity（用于 POST/PUT/DELETE 请求）
     */
    private <T> HttpEntity<T> withAuthAndCsrf(AuthCookies auth, T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.put(HttpHeaders.COOKIE, auth.cookies);
        headers.set("X-CSRF-Token", auth.csrfToken);
        return new HttpEntity<>(body, headers);
    }

    private HttpEntity<Void> withAuthAndCsrf(AuthCookies auth) {
        return withAuthAndCsrf(auth, null);
    }

    /**
     * 成功获取经历列表（空列表）
     */
    @Test
    void getExperiences_whenNoExperiences_returnsEmptyList() {
        AuthCookies auth = loginAndGetCookies();

        ResponseEntity<ResponseMessage<List<ExperienceDTO>>> response = restTemplate.exchange(
                "/api/v1/user/experiences",
                HttpMethod.GET,
                withAuth(auth),
                new ParameterizedTypeReference<ResponseMessage<List<ExperienceDTO>>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());

        List<ExperienceDTO> experiences = response.getBody().getData();
        assertNotNull(experiences);
        assertTrue(experiences.isEmpty());
    }

    /**
     * 未登录获取经历列表应返回401
     */
    @Test
    void getExperiences_whenNotAuthenticated_returns401() {
        ResponseEntity<ResponseMessage> response = restTemplate.getForEntity(
                "/api/v1/user/experiences",
                ResponseMessage.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /**
     * 成功创建项目经历
     */
    @Test
    void createExperience_whenValidProjectRequest_createsSuccessfully() {
        AuthCookies auth = loginAndGetCookies();

        CreateExperienceRequestDTO request = new CreateExperienceRequestDTO();
        request.setType("PROJECT");
        request.setName("测试项目");
        request.setRole("开发者");
        request.setStartDate("2024.01");
        request.setEndDate("2024.06");
        request.setDescription("这是一个测试项目");
        request.setTechStack(Arrays.asList("Java", "Spring Boot"));
        request.setDemoUrl("https://example.com/demo");

        ResponseEntity<ResponseMessage<ExperienceDTO>> response = restTemplate.exchange(
                "/api/v1/user/experiences",
                HttpMethod.POST,
                withAuthAndCsrf(auth, request),
                new ParameterizedTypeReference<ResponseMessage<ExperienceDTO>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());

        ExperienceDTO created = response.getBody().getData();
        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("PROJECT", created.getType());
        assertEquals("测试项目", created.getName());
        assertEquals("开发者", created.getRole());
        assertEquals("2024.01", created.getStartDate());
        assertEquals("2024.06", created.getEndDate());
    }

    /**
     * 成功创建竞赛经历
     */
    @Test
    void createExperience_whenValidCompetitionRequest_createsSuccessfully() {
        AuthCookies auth = loginAndGetCookies();

        CreateExperienceRequestDTO request = new CreateExperienceRequestDTO();
        request.setType("COMPETITION");
        request.setName("全国大学生创新创业大赛");
        request.setDate("2024年8月");
        request.setLevel("国家级");
        request.setAward("一等奖");
        request.setTeamSize(5);
        request.setRole("队长");

        ResponseEntity<ResponseMessage<ExperienceDTO>> response = restTemplate.exchange(
                "/api/v1/user/experiences",
                HttpMethod.POST,
                withAuthAndCsrf(auth, request),
                new ParameterizedTypeReference<ResponseMessage<ExperienceDTO>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());

        ExperienceDTO created = response.getBody().getData();
        assertNotNull(created);
        assertEquals("COMPETITION", created.getType());
        assertEquals("全国大学生创新创业大赛", created.getName());
        assertEquals("国家级", created.getLevel());
        assertEquals("一等奖", created.getAward());
    }

    /**
     * 成功创建实习经历
     */
    @Test
    void createExperience_whenValidInternshipRequest_createsSuccessfully() {
        AuthCookies auth = loginAndGetCookies();

        CreateExperienceRequestDTO request = new CreateExperienceRequestDTO();
        request.setType("INTERNSHIP");
        request.setName("字节跳动");
        request.setCompany("字节跳动");
        request.setPosition("后端开发实习生");
        request.setStartDate("2024.03");
        request.setEndDate("2024.09");
        request.setStatus("ACTIVE");
        request.setAchievements(Arrays.asList("完成了核心模块开发", "优化了系统性能"));

        ResponseEntity<ResponseMessage<ExperienceDTO>> response = restTemplate.exchange(
                "/api/v1/user/experiences",
                HttpMethod.POST,
                withAuthAndCsrf(auth, request),
                new ParameterizedTypeReference<ResponseMessage<ExperienceDTO>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());

        ExperienceDTO created = response.getBody().getData();
        assertNotNull(created);
        assertEquals("INTERNSHIP", created.getType());
        assertEquals("字节跳动", created.getName());
        assertEquals("字节跳动", created.getCompany());
        assertEquals("后端开发实习生", created.getPosition());
    }

    /**
     * 未登录创建经历应返回401
     */
    @Test
    void createExperience_whenNotAuthenticated_returns401() {
        CreateExperienceRequestDTO request = new CreateExperienceRequestDTO();
        request.setType("PROJECT");
        request.setName("测试项目");

        ResponseEntity<ResponseMessage> response = restTemplate.postForEntity(
                "/api/v1/user/experiences",
                request,
                ResponseMessage.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /**
     * 成功更新经历
     */
    @Test
    void updateExperience_whenOwner_updatesSuccessfully() {
        AuthCookies auth = loginAndGetCookies();

        // 先创建一个经历
        CreateExperienceRequestDTO createRequest = new CreateExperienceRequestDTO();
        createRequest.setType("PROJECT");
        createRequest.setName("原项目名");
        createRequest.setRole("开发者");

        ResponseEntity<ResponseMessage<ExperienceDTO>> createResponse = restTemplate.exchange(
                "/api/v1/user/experiences",
                HttpMethod.POST,
                withAuthAndCsrf(auth, createRequest),
                new ParameterizedTypeReference<ResponseMessage<ExperienceDTO>>() {
                });

        String experienceId = createResponse.getBody().getData().getId();

        // 更新经历
        UpdateExperienceRequestDTO updateRequest = new UpdateExperienceRequestDTO();
        updateRequest.setName("更新后的项目名");
        updateRequest.setRole("负责人");
        updateRequest.setDescription("更新后的描述");

        ResponseEntity<ResponseMessage<ExperienceDTO>> updateResponse = restTemplate.exchange(
                "/api/v1/user/experiences/" + experienceId,
                HttpMethod.PUT,
                withAuthAndCsrf(auth, updateRequest),
                new ParameterizedTypeReference<ResponseMessage<ExperienceDTO>>() {
                });

        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        assertNotNull(updateResponse.getBody());
        assertEquals(200, updateResponse.getBody().getCode());

        ExperienceDTO updated = updateResponse.getBody().getData();
        assertEquals("更新后的项目名", updated.getName());
        assertEquals("负责人", updated.getRole());
    }

    /**
     * 更新不存在的经历应返回错误
     */
    @Test
    void updateExperience_whenNotExists_returnsError() {
        AuthCookies auth = loginAndGetCookies();

        UpdateExperienceRequestDTO updateRequest = new UpdateExperienceRequestDTO();
        updateRequest.setName("更新后的项目名");

        ResponseEntity<ResponseMessage> response = restTemplate.exchange(
                "/api/v1/user/experiences/99999",
                HttpMethod.PUT,
                withAuthAndCsrf(auth, updateRequest),
                ResponseMessage.class);

        // 应该返回404或400
        assertTrue(
                response.getStatusCode() == HttpStatus.NOT_FOUND
                        || response.getStatusCode() == HttpStatus.BAD_REQUEST
                        || (response.getBody() != null && response.getBody().getCode() == 404));
    }

    /**
     * 成功删除经历
     */
    @Test
    void deleteExperience_whenOwner_deletesSuccessfully() {
        AuthCookies auth = loginAndGetCookies();

        // 先创建一个经历
        CreateExperienceRequestDTO createRequest = new CreateExperienceRequestDTO();
        createRequest.setType("PROJECT");
        createRequest.setName("要删除的项目");

        ResponseEntity<ResponseMessage<ExperienceDTO>> createResponse = restTemplate.exchange(
                "/api/v1/user/experiences",
                HttpMethod.POST,
                withAuthAndCsrf(auth, createRequest),
                new ParameterizedTypeReference<ResponseMessage<ExperienceDTO>>() {
                });

        String experienceId = createResponse.getBody().getData().getId();

        // 删除经历
        ResponseEntity<ResponseMessage<Void>> deleteResponse = restTemplate.exchange(
                "/api/v1/user/experiences/" + experienceId,
                HttpMethod.DELETE,
                withAuthAndCsrf(auth),
                new ParameterizedTypeReference<ResponseMessage<Void>>() {
                });

        assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());

        // 验证已删除 - 再次获取应该为空
        ResponseEntity<ResponseMessage<List<ExperienceDTO>>> listResponse = restTemplate.exchange(
                "/api/v1/user/experiences",
                HttpMethod.GET,
                withAuth(auth),
                new ParameterizedTypeReference<ResponseMessage<List<ExperienceDTO>>>() {
                });

        List<ExperienceDTO> experiences = listResponse.getBody().getData();
        assertTrue(experiences.isEmpty());
    }

    /**
     * 删除不存在的经历应返回错误
     */
    @Test
    void deleteExperience_whenNotExists_returnsError() {
        AuthCookies auth = loginAndGetCookies();

        ResponseEntity<ResponseMessage> response = restTemplate.exchange(
                "/api/v1/user/experiences/99999",
                HttpMethod.DELETE,
                withAuthAndCsrf(auth),
                ResponseMessage.class);

        // 应该返回404或400
        assertTrue(
                response.getStatusCode() == HttpStatus.NOT_FOUND
                        || response.getStatusCode() == HttpStatus.BAD_REQUEST
                        || (response.getBody() != null && response.getBody().getCode() == 404));
    }

    /**
     * 按类型过滤经历列表
     */
    @Test
    void getExperiences_withTypeFilter_returnsFilteredResults() {
        AuthCookies auth = loginAndGetCookies();

        // 创建项目经历
        CreateExperienceRequestDTO projectRequest = new CreateExperienceRequestDTO();
        projectRequest.setType("PROJECT");
        projectRequest.setName("项目1");
        restTemplate.exchange(
                "/api/v1/user/experiences",
                HttpMethod.POST,
                withAuthAndCsrf(auth, projectRequest),
                new ParameterizedTypeReference<ResponseMessage<ExperienceDTO>>() {
                });

        // 创建竞赛经历
        CreateExperienceRequestDTO competitionRequest = new CreateExperienceRequestDTO();
        competitionRequest.setType("COMPETITION");
        competitionRequest.setName("竞赛1");
        restTemplate.exchange(
                "/api/v1/user/experiences",
                HttpMethod.POST,
                withAuthAndCsrf(auth, competitionRequest),
                new ParameterizedTypeReference<ResponseMessage<ExperienceDTO>>() {
                });

        // 按类型过滤
        ResponseEntity<ResponseMessage<List<ExperienceDTO>>> response = restTemplate.exchange(
                "/api/v1/user/experiences?type=PROJECT",
                HttpMethod.GET,
                withAuth(auth),
                new ParameterizedTypeReference<ResponseMessage<List<ExperienceDTO>>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<ExperienceDTO> experiences = response.getBody().getData();
        assertNotNull(experiences);
        assertEquals(1, experiences.size());
        assertEquals("PROJECT", experiences.get(0).getType());
    }

    // ==================== 权限控制测试 ====================

    @Nested
    @DisplayName("权限控制测试")
    class PermissionControlTests {

        /**
         * CANDIDATE角色创建经历应返回403
         */
        @Test
        @DisplayName("CANDIDATE角色创建经历应返回403")
        void createExperience_whenCandidate_returns403() {
            // 创建CANDIDATE用户
            String candidateStudentId = "2024001002";
            createUserWithRole(candidateStudentId, "考生用户", candidateRoleId);
            AuthCookies auth = loginAndGetCookies(candidateStudentId);

            CreateExperienceRequestDTO request = new CreateExperienceRequestDTO();
            request.setType("PROJECT");
            request.setName("测试项目");
            request.setRole("开发者");

            ResponseEntity<ResponseMessage> response = restTemplate.exchange(
                    "/api/v1/user/experiences",
                    HttpMethod.POST,
                    withAuthAndCsrf(auth, request),
                    ResponseMessage.class);

            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        /**
         * MEMBER角色创建经历应成功
         */
        @Test
        @DisplayName("MEMBER角色创建经历应成功")
        void createExperience_whenMember_succeeds() {
            AuthCookies auth = loginAndGetCookies(); // 默认用户是MEMBER角色

            CreateExperienceRequestDTO request = new CreateExperienceRequestDTO();
            request.setType("PROJECT");
            request.setName("测试项目");
            request.setRole("开发者");

            ResponseEntity<ResponseMessage<ExperienceDTO>> response = restTemplate.exchange(
                    "/api/v1/user/experiences",
                    HttpMethod.POST,
                    withAuthAndCsrf(auth, request),
                    new ParameterizedTypeReference<ResponseMessage<ExperienceDTO>>() {
                    });

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody().getData());
        }

        /**
         * CANDIDATE角色更新经历应返回403
         */
        @Test
        @DisplayName("CANDIDATE角色更新经历应返回403")
        void updateExperience_whenCandidate_returns403() {
            // 先用MEMBER创建经历
            AuthCookies memberAuth = loginAndGetCookies();
            CreateExperienceRequestDTO createRequest = new CreateExperienceRequestDTO();
            createRequest.setType("PROJECT");
            createRequest.setName("原项目");

            ResponseEntity<ResponseMessage<ExperienceDTO>> createResponse = restTemplate.exchange(
                    "/api/v1/user/experiences",
                    HttpMethod.POST,
                    withAuthAndCsrf(memberAuth, createRequest),
                    new ParameterizedTypeReference<ResponseMessage<ExperienceDTO>>() {
                    });

            String experienceId = createResponse.getBody().getData().getId();

            // 用CANDIDATE尝试更新
            String candidateStudentId = "2024001003";
            createUserWithRole(candidateStudentId, "考生用户2", candidateRoleId);
            AuthCookies candidateAuth = loginAndGetCookies(candidateStudentId);

            UpdateExperienceRequestDTO updateRequest = new UpdateExperienceRequestDTO();
            updateRequest.setName("更新后的项目");

            ResponseEntity<ResponseMessage> response = restTemplate.exchange(
                    "/api/v1/user/experiences/" + experienceId,
                    HttpMethod.PUT,
                    withAuthAndCsrf(candidateAuth, updateRequest),
                    ResponseMessage.class);

            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        /**
         * CANDIDATE角色删除经历应返回403
         */
        @Test
        @DisplayName("CANDIDATE角色删除经历应返回403")
        void deleteExperience_whenCandidate_returns403() {
            // 先用MEMBER创建经历
            AuthCookies memberAuth = loginAndGetCookies();
            CreateExperienceRequestDTO createRequest = new CreateExperienceRequestDTO();
            createRequest.setType("PROJECT");
            createRequest.setName("要删除的项目");

            ResponseEntity<ResponseMessage<ExperienceDTO>> createResponse = restTemplate.exchange(
                    "/api/v1/user/experiences",
                    HttpMethod.POST,
                    withAuthAndCsrf(memberAuth, createRequest),
                    new ParameterizedTypeReference<ResponseMessage<ExperienceDTO>>() {
                    });

            String experienceId = createResponse.getBody().getData().getId();

            // 用CANDIDATE尝试删除
            String candidateStudentId = "2024001004";
            createUserWithRole(candidateStudentId, "考生用户3", candidateRoleId);
            AuthCookies candidateAuth = loginAndGetCookies(candidateStudentId);

            ResponseEntity<ResponseMessage> response = restTemplate.exchange(
                    "/api/v1/user/experiences/" + experienceId,
                    HttpMethod.DELETE,
                    withAuthAndCsrf(candidateAuth),
                    ResponseMessage.class);

            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        /**
         * CANDIDATE角色可以查看自己的经历列表（AUTHENTICATED级别）
         */
        @Test
        @DisplayName("CANDIDATE角色可以查看经历列表")
        void getExperiences_whenCandidate_succeeds() {
            String candidateStudentId = "2024001005";
            createUserWithRole(candidateStudentId, "考生用户4", candidateRoleId);
            AuthCookies auth = loginAndGetCookies(candidateStudentId);

            ResponseEntity<ResponseMessage<List<ExperienceDTO>>> response = restTemplate.exchange(
                    "/api/v1/user/experiences",
                    HttpMethod.GET,
                    withAuth(auth),
                    new ParameterizedTypeReference<ResponseMessage<List<ExperienceDTO>>>() {
                    });

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody().getData());
        }
    }
}
