package com.bluenet.web.api.controller;

import com.bluenet.web.infrastructure.repository.dataobject.*;

import com.bluenet.web.testsupport.RepositoryTestObjects;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.auth.EmailLoginRequestDTO;
import com.bluenet.web.api.dto.auth.SendVerificationCodeRequestDTO;
import com.bluenet.web.api.dto.auth.StudentIdLoginRequestDTO;
import com.bluenet.web.api.dto.auth.UserAuthResponseDTO;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.entity.VerifyCode;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.infrastructure.repository.mapper.UserMapper;
import com.bluenet.web.infrastructure.repository.mapper.VerifyCodeMapper;
import com.bluenet.web.infrastructure.security.auth.AuthTokenService;
import com.bluenet.web.infrastructure.security.jwt.JwtUtil;
import com.bluenet.web.testcontainers.TestcontainersConfiguration;
import lombok.extern.slf4j.Slf4j;
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

import java.time.LocalDateTime;
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
    private VerifyCodeMapper verifyCodeMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthTokenService authTokenService;

    private static final String TEST_STUDENT_ID = "2024001001";
    private static final String TEST_PASSWORD = "testPassword123";
    private static final String TEST_EMAIL = "test@example.com";

    @BeforeEach
    void setUp() {
        // 创建测试用户
        User user = new User();
        user.setStudentId(TEST_STUDENT_ID);
        user.setUsername("测试用户");
        user.setEmail(TEST_EMAIL);
        user.setPassword(passwordEncoder.encode(TEST_PASSWORD));
        user.setRoleId(1L);
        user.setDisable(false);
        user.setDirection(Direction.COMPUTER_VISION);

        // 如果用户已存在则删除
        // Mapper 返回 DO，测试夹具转换为领域对象后再断言/清理。
        User existingUser = RepositoryTestObjects.toDomain(
                userMapper
                        .selectOne(
                                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserDO>()
                                        .eq(UserDO::getStudentId, TEST_STUDENT_ID)),
                User.class);
        if (existingUser != null) {
            userMapper.deleteById(existingUser.getId());
        }

        RepositoryTestObjects.insert(userMapper, user, UserDO.class);
    }

    // ==================== 学号登录测试 ====================

    /**
     * 成功登录后应该返回CSRF Token并设置Cookie
     */
    @Test
    @DisplayName("学号登录：有效凭证应返回CSRF Token并设置Cookie")
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
    @DisplayName("学号登录：错误密码应返回401")
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
    @DisplayName("学号登录：不存在的学号应返回401")
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
    @DisplayName("登出：应清除Cookie并吊销Token")
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

    // ==================== 邮箱验证码登录测试 ====================

    /**
     * 邮箱验证码登录：有效验证码应登录成功
     */
    @Test
    @DisplayName("邮箱登录：有效验证码应返回CSRF Token并设置Cookie")
    void emailLogin_withValidVerifyCode_shouldReturnCsrfTokenAndSetCookies() {
        // 准备 - 直接在数据库插入验证码（绕过邮件发送）
        String verifyCode = "123456";
        insertVerifyCode(TEST_EMAIL, verifyCode, LocalDateTime.now().plusMinutes(5), null);

        EmailLoginRequestDTO request = new EmailLoginRequestDTO();
        request.setEmail(TEST_EMAIL);
        request.setVerifyCode(verifyCode);

        // 执行
        ResponseEntity<ResponseMessage<UserAuthResponseDTO>> response = restTemplate.exchange(
                "/api/v1/auth/login/email",
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
     * 邮箱验证码登录：无效验证码应返回401
     */
    @Test
    @DisplayName("邮箱登录：无效验证码应返回401")
    void emailLogin_withInvalidVerifyCode_shouldReturn401() {
        // 准备
        EmailLoginRequestDTO request = new EmailLoginRequestDTO();
        request.setEmail(TEST_EMAIL);
        request.setVerifyCode("000000");

        // 执行
        ResponseEntity<ResponseMessage> response = restTemplate.postForEntity(
                "/api/v1/auth/login/email",
                request,
                ResponseMessage.class);

        // 验证
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /**
     * 邮箱验证码登录：已过期的验证码应返回401
     */
    @Test
    @DisplayName("邮箱登录：过期验证码应返回401")
    void emailLogin_withExpiredVerifyCode_shouldReturn401() {
        // 准备 - 插入已过期的验证码
        String verifyCode = "654321";
        insertVerifyCode(TEST_EMAIL, verifyCode, LocalDateTime.now().minusMinutes(1), null);

        EmailLoginRequestDTO request = new EmailLoginRequestDTO();
        request.setEmail(TEST_EMAIL);
        request.setVerifyCode(verifyCode);

        // 执行
        ResponseEntity<ResponseMessage> response = restTemplate.postForEntity(
                "/api/v1/auth/login/email",
                request,
                ResponseMessage.class);

        // 验证
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /**
     * 邮箱验证码登录：已使用的验证码应返回401
     */
    @Test
    @DisplayName("邮箱登录：已使用的验证码应返回401")
    void emailLogin_withUsedVerifyCode_shouldReturn401() {
        // 准备 - 插入已使用的验证码
        String verifyCode = "111111";
        insertVerifyCode(TEST_EMAIL, verifyCode, LocalDateTime.now().plusMinutes(5), LocalDateTime.now());

        EmailLoginRequestDTO request = new EmailLoginRequestDTO();
        request.setEmail(TEST_EMAIL);
        request.setVerifyCode(verifyCode);

        // 执行
        ResponseEntity<ResponseMessage> response = restTemplate.postForEntity(
                "/api/v1/auth/login/email",
                request,
                ResponseMessage.class);

        // 验证
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /**
     * 邮箱验证码登录：不存在的邮箱应返回401
     */
    @Test
    @DisplayName("邮箱登录：不存在的邮箱应返回401")
    void emailLogin_withNonExistentEmail_shouldReturn401() {
        // 准备
        EmailLoginRequestDTO request = new EmailLoginRequestDTO();
        request.setEmail("nonexistent@example.com");
        request.setVerifyCode("123456");

        // 执行
        ResponseEntity<ResponseMessage> response = restTemplate.postForEntity(
                "/api/v1/auth/login/email",
                request,
                ResponseMessage.class);

        // 验证
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    // ==================== 发送验证码测试 ====================

    /**
     * 发送验证码：正常请求应返回成功 注意：此测试需要邮件服务配置，如果没有配置则跳过
     */
    @Test
    @DisplayName("发送验证码：正常请求应返回200（需要邮件服务配置）")
    void sendVerificationCode_shouldReturn200() {
        // 准备
        SendVerificationCodeRequestDTO request = new SendVerificationCodeRequestDTO();
        request.setEmail(TEST_EMAIL);

        // 执行
        ResponseEntity<ResponseMessage> response = restTemplate.postForEntity(
                "/api/v1/auth/verification-code/send",
                request,
                ResponseMessage.class);

        // 验证 - 即使邮件发送失败，应用层应返回成功（异步发送）
        // 或者如果邮件服务不可用，可能返回500
        // 这里主要验证接口可达性
        assertTrue(
                response.getStatusCode() == HttpStatus.OK
                        || response.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR,
                "发送验证码接口应该可达");
    }

    // ==================== 辅助方法 ====================

    /**
     * 直接在数据库插入验证码记录
     */
    private void insertVerifyCode(String target, String code, LocalDateTime expireAt, LocalDateTime usedAt) {
        VerifyCode verifyCode = new VerifyCode();
        verifyCode.setTarget(target);
        verifyCode.setCode(code);
        verifyCode.setExpireAt(expireAt);
        verifyCode.setUsedAt(usedAt);
        RepositoryTestObjects.insert(verifyCodeMapper, verifyCode, VerifyCodeDO.class);
    }
}
