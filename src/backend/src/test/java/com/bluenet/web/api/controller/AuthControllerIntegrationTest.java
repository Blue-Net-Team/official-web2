package com.bluenet.web.api.controller;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.auth.StudentIdLoginRequestDTO;
import com.bluenet.web.api.dto.auth.UserAuthResponseDTO;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.infrastructure.repository.mapper.UserMapper;
import com.bluenet.web.infrastructure.security.auth.AuthTokenService;
import com.bluenet.web.infrastructure.security.jwt.JwtUtil;
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

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@Import(TestcontainersConfiguration.class)
class AuthControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthTokenService authTokenService;

    private static final String TEST_STUDENT_ID = "2024001001";
    private static final String TEST_PASSWORD = "testPassword123";

    @BeforeEach
    void setUp() {
        // 创建测试用户
        User user = new User();
        user.setStudentId(TEST_STUDENT_ID);
        user.setUsername("测试用户");
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode(TEST_PASSWORD));
        user.setRoleId(1L);
        user.setDisable(false);
        user.setDirection(Direction.COMPUTER_VISION);

        // 如果用户已存在则删除
        User existingUser = userMapper
                .selectOne(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                                .eq(User::getStudentId, TEST_STUDENT_ID));
        if (existingUser != null) {
            userMapper.deleteById(existingUser.getId());
        }

        userMapper.insert(user);
    }

    /**
     * 成功登录后应该返回CSRF Token并设置Cookie
     */
    @Test
    void login_withValidCredentials_shouldReturnCsrfTokenAndSetCookies() {
        // 准备
        StudentIdLoginRequestDTO request = new StudentIdLoginRequestDTO();
        request.setStudentId(TEST_STUDENT_ID);
        request.setPassword(TEST_PASSWORD);

        // 执行
        ResponseEntity<ResponseMessage<UserAuthResponseDTO>> response = restTemplate.exchange(
                "/api/v1/auth/login/student-id",
                HttpMethod.POST,
                new HttpEntity<>(request),
                new ParameterizedTypeReference<ResponseMessage<UserAuthResponseDTO>>() {
                });

        // 验证
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());

        UserAuthResponseDTO data = response.getBody().getData();
        assertNotNull(data);
        assertNotNull(data.getCsrfToken(), "应该返回 CSRF Token");
        assertNotNull(data.getUserInfo(), "应该返回用户信息");

        // 验证 Cookie 被设置
        List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        assertNotNull(cookies, "应该设置 Cookie");
        assertTrue(cookies.stream().anyMatch(c -> c.contains("auth_token=")), "应该设置 auth_token Cookie");
        assertTrue(cookies.stream().anyMatch(c -> c.contains("csrf_token=")), "应该设置 csrf_token Cookie");
    }

    /**
     * 登录时使用错误密码应该返回401
     */
    @Test
    void login_withInvalidPassword_shouldReturn401() {
        // 准备
        StudentIdLoginRequestDTO request = new StudentIdLoginRequestDTO();
        request.setStudentId(TEST_STUDENT_ID);
        request.setPassword("wrongPassword");

        // 执行
        ResponseEntity<ResponseMessage> response = restTemplate.postForEntity(
                "/api/v1/auth/login/student-id",
                request,
                ResponseMessage.class);

        // 验证
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /**
     * 登录时使用不存在的学号应该返回401
     */
    @Test
    void login_withNonExistentStudentId_shouldReturn401() {
        // 准备
        StudentIdLoginRequestDTO request = new StudentIdLoginRequestDTO();
        request.setStudentId("nonexistent");
        request.setPassword(TEST_PASSWORD);

        // 执行
        ResponseEntity<ResponseMessage> response = restTemplate.postForEntity(
                "/api/v1/auth/login/student-id",
                request,
                ResponseMessage.class);

        // 验证
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /**
     * 成功登出后应该清除Cookie并吊销Token
     */
    @Test
    void logout_withValidCookie_shouldClearCookiesAndRevokeToken() {
        // 准备 - 先登录获取 Cookie
        StudentIdLoginRequestDTO loginRequest = new StudentIdLoginRequestDTO();
        loginRequest.setStudentId(TEST_STUDENT_ID);
        loginRequest.setPassword(TEST_PASSWORD);

        ResponseEntity<ResponseMessage<UserAuthResponseDTO>> loginResponse = restTemplate.exchange(
                "/api/v1/auth/login/student-id",
                HttpMethod.POST,
                new HttpEntity<>(loginRequest),
                new ParameterizedTypeReference<ResponseMessage<UserAuthResponseDTO>>() {
                });

        // 提取 Cookie
        List<String> cookies = loginResponse.getHeaders().get(HttpHeaders.SET_COOKIE);
        assertNotNull(cookies);

        // 执行登出 - 携带 Cookie
        log.info("执行登出");
        HttpHeaders headers = new HttpHeaders();
        headers.put(HttpHeaders.COOKIE, cookies);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<ResponseMessage> logoutResponse = restTemplate.exchange(
                "/api/v1/auth/logout",
                HttpMethod.POST,
                entity,
                ResponseMessage.class);

        // 验证
        assertEquals(HttpStatus.OK, logoutResponse.getStatusCode());

        // 验证 Cookie 被清除
        List<String> clearedCookies = logoutResponse.getHeaders().get(HttpHeaders.SET_COOKIE);
        if (clearedCookies != null) {
            // Cookie 应该被设置为 Max-Age=0 或已过期
            assertTrue(
                    clearedCookies.stream().anyMatch(c -> c.contains("Max-Age=0") || c.contains("auth_token=;")),
                    "Cookie 应该被清除");
        }
    }

}
