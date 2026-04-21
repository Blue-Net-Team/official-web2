package com.bluenet.web.api.controller.v1.user;

import com.bluenet.web.infrastructure.repository.dataobject.*;

import com.bluenet.web.testsupport.RepositoryTestObjects;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.auth.StudentIdLoginRequestDTO;
import com.bluenet.web.api.dto.auth.UserAuthResponseDTO;
import com.bluenet.web.api.dto.user.TabCountsDTO;
import com.bluenet.web.api.dto.user.UpdateProfileRequestDTO;
import com.bluenet.web.api.dto.user.UserInfo;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.Gender;
import com.bluenet.web.infrastructure.repository.mapper.RoleMapper;
import com.bluenet.web.infrastructure.repository.mapper.UserMapper;
import com.bluenet.web.testcontainers.TestcontainersConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
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

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserProfileController 集成测试 测试用户信息相关接口：/api/v1/user/info,
 * /api/v1/user/tab-counts
 */
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@Import(TestcontainersConfiguration.class)
class UserProfileControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String TEST_STUDENT_ID = "2024001001";
    private static final String TEST_PASSWORD = "testPassword123";
    private Long testUserId;

    @BeforeEach
    void setUp() {
        // 查询 MEMBER 角色ID
        var memberRole = roleMapper.selectByName("MEMBER");
        assertNotNull(memberRole, "MEMBER 角色应存在");

        User user = new User();
        user.setStudentId(TEST_STUDENT_ID);
        user.setUsername("测试用户");
        user.setNickname("测试昵称");
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode(TEST_PASSWORD));
        user.setRoleId(memberRole.getId());
        user.setDisable(false);
        user.setDirection(Direction.COMPUTER_VISION);
        user.setBio("原个人简介");
        user.setGender(Gender.MALE);

        RepositoryTestObjects.insert(userMapper, user, UserDO.class);
        testUserId = user.getId();
    }

    /**
     * 登录并获取 Cookie 返回可用于后续请求的 HttpHeaders（包含 Cookie）
     */
    private HttpHeaders loginAndGetCookies() {
        return loginAndGetCookies(TEST_STUDENT_ID, TEST_PASSWORD);
    }

    /**
     * 登录并获取 Cookie 返回可用于后续请求的 HttpHeaders（包含 Cookie）
     */
    private HttpHeaders loginAndGetCookies(String studentId, String password) {
        StudentIdLoginRequestDTO loginRequest = new StudentIdLoginRequestDTO();
        loginRequest.setStudentId(studentId);
        loginRequest.setPassword(password);

        ResponseEntity<ResponseMessage<UserAuthResponseDTO>> loginResponse = restTemplate.exchange(
                "/api/v1/auth/login/student-id",
                HttpMethod.POST,
                new HttpEntity<>(loginRequest),
                new ParameterizedTypeReference<ResponseMessage<UserAuthResponseDTO>>() {
                });

        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());

        // 提取 Set-Cookie 并转换为正确的 Cookie header 格式
        List<String> setCookies = loginResponse.getHeaders().get(HttpHeaders.SET_COOKIE);
        assertNotNull(setCookies, "登录响应应包含 Set-Cookie");

        // 将 Set-Cookie 转换为 Cookie header 格式（只保留 name=value 部分）
        StringBuilder cookieBuilder = new StringBuilder();
        for (String setCookie : setCookies) {
            int semicolonIndex = setCookie.indexOf(';');
            String nameValue = semicolonIndex > 0 ? setCookie.substring(0, semicolonIndex) : setCookie;
            if (cookieBuilder.length() > 0) {
                cookieBuilder.append("; ");
            }
            cookieBuilder.append(nameValue);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.COOKIE, cookieBuilder.toString());
        // 存储 CSRF Token 以便后续使用
        String csrfToken = loginResponse.getBody().getData().getCsrfToken();
        headers.set("X-CSRF-Token-Stored", csrfToken);
        return headers;
    }

    /**
     * 从 HttpHeaders 中获取存储的 CSRF Token
     */
    private String getStoredCsrfToken(HttpHeaders headers) {
        return headers.getFirst("X-CSRF-Token-Stored");
    }

    /**
     * 从 Set-Cookie 中提取 auth_token 值
     */
    private String extractAuthTokenFromCookies(List<String> cookies) {
        for (String cookie : cookies) {
            if (cookie.startsWith("auth_token=")) {
                int end = cookie.indexOf(';');
                if (end > 0) {
                    return cookie.substring(11, end);
                }
                return cookie.substring(11);
            }
        }
        return null;
    }

    /**
     * 从 HttpHeaders 中提取 csrf_token 值
     */
    private String getCsrfTokenFromCookies(HttpHeaders headers) {
        List<String> cookies = headers.get(HttpHeaders.COOKIE);
        if (cookies == null)
            return null;
        for (String cookieHeader : cookies) {
            String[] cookieParts = cookieHeader.split(";");
            for (String part : cookieParts) {
                String trimmed = part.trim();
                if (trimmed.startsWith("csrf_token=")) {
                    return trimmed.substring(11);
                }
            }
        }
        return null;
    }

    private <T> HttpEntity<T> withAuth(HttpHeaders cookies, T body) {
        return new HttpEntity<>(body, cookies);
    }

    private HttpEntity<Void> withAuth(HttpHeaders cookies) {
        return withAuth(cookies, null);
    }

    @Test
    void getMyInfo_whenAuthenticated_returnsUserInfo() {
        HttpHeaders cookies = loginAndGetCookies();

        ResponseEntity<ResponseMessage<UserInfo>> response = restTemplate.exchange(
                "/api/v1/user/info",
                HttpMethod.GET,
                withAuth(cookies),
                new ParameterizedTypeReference<ResponseMessage<UserInfo>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());

        UserInfo userInfo = response.getBody().getData();
        assertNotNull(userInfo);
        assertEquals(testUserId, userInfo.getId());
        assertEquals("测试用户", userInfo.getUsername());
        assertEquals("测试昵称", userInfo.getNickname());
        assertEquals("原个人简介", userInfo.getBio());
    }

    @Test
    void getMyInfo_whenNotAuthenticated_returns401() {
        ResponseEntity<ResponseMessage> response = restTemplate.getForEntity(
                "/api/v1/user/info",
                ResponseMessage.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void updateProfile_whenAuthenticated_updatesSuccessfully() {
        HttpHeaders cookies = loginAndGetCookies();
        String csrfToken = getStoredCsrfToken(cookies);

        UpdateProfileRequestDTO request = new UpdateProfileRequestDTO();
        request.setUsername("新用户名");
        request.setNickname("新昵称");
        request.setBio("新的个人简介");

        HttpHeaders headersWithCsrf = new HttpHeaders();
        headersWithCsrf.set(HttpHeaders.COOKIE, cookies.getFirst(HttpHeaders.COOKIE));
        headersWithCsrf.set("X-CSRF-Token", csrfToken);

        ResponseEntity<ResponseMessage<Void>> response = restTemplate.exchange(
                "/api/v1/user/info",
                HttpMethod.PUT,
                new HttpEntity<>(request, headersWithCsrf),
                new ParameterizedTypeReference<ResponseMessage<Void>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());

        User updatedUser = RepositoryTestObjects.toDomain(userMapper.selectById(testUserId), User.class);
        assertEquals("新用户名", updatedUser.getUsername());
        assertEquals("新昵称", updatedUser.getNickname());
        assertEquals("新的个人简介", updatedUser.getBio());
    }

    @Test
    void updateProfile_partialUpdate_updatesOnlyNickname() {
        HttpHeaders cookies = loginAndGetCookies();
        String csrfToken = getStoredCsrfToken(cookies);

        UpdateProfileRequestDTO request = new UpdateProfileRequestDTO();
        request.setNickname("仅更新昵称");

        HttpHeaders headersWithCsrf = new HttpHeaders();
        headersWithCsrf.set(HttpHeaders.COOKIE, cookies.getFirst(HttpHeaders.COOKIE));
        headersWithCsrf.set("X-CSRF-Token", csrfToken);

        ResponseEntity<ResponseMessage<Void>> response = restTemplate.exchange(
                "/api/v1/user/info",
                HttpMethod.PUT,
                new HttpEntity<>(request, headersWithCsrf),
                new ParameterizedTypeReference<ResponseMessage<Void>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());

        User updatedUser = RepositoryTestObjects.toDomain(userMapper.selectById(testUserId), User.class);
        assertEquals("仅更新昵称", updatedUser.getNickname());
    }

    @Test
    void updateProfile_whenNotAuthenticated_returns401() {
        UpdateProfileRequestDTO request = new UpdateProfileRequestDTO();
        request.setNickname("新昵称");

        ResponseEntity<ResponseMessage> response = restTemplate.exchange(
                "/api/v1/user/info",
                HttpMethod.PUT,
                new HttpEntity<>(request),
                ResponseMessage.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void updateProfile_withAllFields_updatesAllFields() {
        // 查询 MEMBER 角色ID
        var memberRole = roleMapper.selectByName("MEMBER");
        assertNotNull(memberRole, "MEMBER 角色应存在");

        User memberUser = new User();
        memberUser.setStudentId("2024001002");
        memberUser.setUsername("成员用户");
        memberUser.setEmail("member@example.com");
        memberUser.setPassword(passwordEncoder.encode(TEST_PASSWORD));
        memberUser.setRoleId(memberRole.getId());
        memberUser.setDisable(false);
        memberUser.setDirection(Direction.COMPUTER_VISION);
        memberUser.setBio("原个人简介");
        memberUser.setGender(Gender.MALE);
        RepositoryTestObjects.insert(userMapper, memberUser, UserDO.class);

        // 登录获取 Cookie
        StudentIdLoginRequestDTO loginRequest = new StudentIdLoginRequestDTO();
        loginRequest.setStudentId("2024001002");
        loginRequest.setPassword(TEST_PASSWORD);

        ResponseEntity<ResponseMessage<UserAuthResponseDTO>> loginResponse = restTemplate.exchange(
                "/api/v1/auth/login/student-id",
                HttpMethod.POST,
                new HttpEntity<>(loginRequest),
                new ParameterizedTypeReference<ResponseMessage<UserAuthResponseDTO>>() {
                });

        List<String> cookies = loginResponse.getHeaders().get(HttpHeaders.SET_COOKIE);
        String csrfToken = loginResponse.getBody().getData().getCsrfToken();

        UpdateProfileRequestDTO request = new UpdateProfileRequestDTO();
        request.setUsername("完整更新用户名");
        request.setNickname("完整更新昵称");
        request.setBio("完整更新简介");
        request.setDirection(Direction.EMBEDDED);
        request.setGender(Gender.FEMALE);

        HttpHeaders headersWithCsrf = new HttpHeaders();
        headersWithCsrf.put(HttpHeaders.COOKIE, cookies);
        headersWithCsrf.set("X-CSRF-Token", csrfToken);

        ResponseEntity<ResponseMessage<Void>> response = restTemplate.exchange(
                "/api/v1/user/info",
                HttpMethod.PUT,
                new HttpEntity<>(request, headersWithCsrf),
                new ParameterizedTypeReference<ResponseMessage<Void>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());

        User updatedUser = RepositoryTestObjects.toDomain(userMapper.selectById(memberUser.getId()), User.class);
        assertEquals("完整更新用户名", updatedUser.getUsername());
        assertEquals("完整更新昵称", updatedUser.getNickname());
        assertEquals("完整更新简介", updatedUser.getBio());
        assertEquals(Direction.EMBEDDED, updatedUser.getDirection());
        assertEquals(Gender.FEMALE, updatedUser.getGender());
    }

    @Test
    void getTabCounts_whenAuthenticated_returnsCounts() {
        HttpHeaders cookies = loginAndGetCookies();

        ResponseEntity<ResponseMessage<TabCountsDTO>> response = restTemplate.exchange(
                "/api/v1/user/tab-counts",
                HttpMethod.GET,
                withAuth(cookies),
                new ParameterizedTypeReference<ResponseMessage<TabCountsDTO>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());

        TabCountsDTO counts = response.getBody().getData();
        assertNotNull(counts);
        assertEquals(0, counts.getProjects());
        assertEquals(0, counts.getCompetitions());
        assertEquals(0, counts.getInternships());
    }

    @Test
    void getTabCounts_whenNotAuthenticated_returns401() {
        ResponseEntity<ResponseMessage> response = restTemplate.getForEntity(
                "/api/v1/user/tab-counts",
                ResponseMessage.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
