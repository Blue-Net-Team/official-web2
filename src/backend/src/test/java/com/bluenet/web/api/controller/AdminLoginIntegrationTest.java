package com.bluenet.web.api.controller;

import com.bluenet.web.infrastructure.repository.dataobject.*;

import com.bluenet.web.testsupport.RepositoryTestObjects;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.auth.StudentIdLoginRequestDTO;
import com.bluenet.web.api.dto.auth.UserAuthResponseDTO;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.infrastructure.repository.mapper.UserMapper;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@Import(TestcontainersConfiguration.class)
class AdminLoginIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String ADMIN_STUDENT_ID = "000000000000";
    private static final String ADMIN_PASSWORD = "admin123";

    @BeforeEach
    void setUp() {
        // Mapper 返回 DO，测试夹具转换为领域对象后再断言/清理。
        User existingUser = RepositoryTestObjects.toDomain(
                userMapper.selectOne(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserDO>()
                                .eq(UserDO::getStudentId, ADMIN_STUDENT_ID)),
                User.class);
        if (existingUser != null) {
            userMapper.deleteById(existingUser.getId());
        }
    }

    @Nested
    @DisplayName("管理员登录流程测试")
    class AdminLoginFlowTest {

        @Test
        @DisplayName("使用SHA-256预哈希密码的管理员应能成功登录")
        void login_withSha256PreHashedPassword_shouldSucceed() {
            String sha256HashedPassword = sha256Hash(ADMIN_PASSWORD);
            String encodedPassword = passwordEncoder.encode(sha256HashedPassword);

            User adminUser = new User();
            adminUser.setStudentId(ADMIN_STUDENT_ID);
            adminUser.setUsername("系统管理员");
            adminUser.setPassword(encodedPassword);
            adminUser.setRoleId(1L);
            adminUser.setDisable(false);
            RepositoryTestObjects.insert(userMapper, adminUser, UserDO.class);

            StudentIdLoginRequestDTO request = new StudentIdLoginRequestDTO();
            request.setStudentId(ADMIN_STUDENT_ID);
            request.setPassword(sha256HashedPassword);

            ResponseEntity<ResponseMessage<UserAuthResponseDTO>> response = restTemplate.exchange(
                    "/api/v1/auth/login/student-id",
                    HttpMethod.POST,
                    new HttpEntity<>(request),
                    new ParameterizedTypeReference<ResponseMessage<UserAuthResponseDTO>>() {
                    });

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(200, response.getBody().getCode());

            UserAuthResponseDTO data = response.getBody().getData();
            assertNotNull(data);
            assertNotNull(data.getCsrfToken());
            assertNotNull(data.getUserInfo());
            assertEquals("系统管理员", data.getUserInfo().getUsername());

            // 验证 Cookie 被设置
            assertTrue(response.getHeaders().containsKey("Set-Cookie"), "响应应包含 Set-Cookie");
        }

        @Test
        @DisplayName("使用明文密码登录SHA-256预哈希存储的账号应失败")
        void login_withPlaintextPassword_whenSha256PreHashedStored_shouldFail() {
            String sha256HashedPassword = sha256Hash(ADMIN_PASSWORD);
            String encodedPassword = passwordEncoder.encode(sha256HashedPassword);

            User adminUser = new User();
            adminUser.setStudentId(ADMIN_STUDENT_ID);
            adminUser.setUsername("系统管理员");
            adminUser.setPassword(encodedPassword);
            adminUser.setRoleId(1L);
            adminUser.setDisable(false);
            RepositoryTestObjects.insert(userMapper, adminUser, UserDO.class);

            StudentIdLoginRequestDTO request = new StudentIdLoginRequestDTO();
            request.setStudentId(ADMIN_STUDENT_ID);
            request.setPassword(ADMIN_PASSWORD);

            ResponseEntity<ResponseMessage> response = restTemplate.postForEntity(
                    "/api/v1/auth/login/student-id",
                    request,
                    ResponseMessage.class);

            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        }

        @Test
        @DisplayName("使用错误的SHA-256哈希密码应登录失败")
        void login_withWrongSha256Hash_shouldFail() {
            String sha256HashedPassword = sha256Hash(ADMIN_PASSWORD);
            String encodedPassword = passwordEncoder.encode(sha256HashedPassword);

            User adminUser = new User();
            adminUser.setStudentId(ADMIN_STUDENT_ID);
            adminUser.setUsername("系统管理员");
            adminUser.setPassword(encodedPassword);
            adminUser.setRoleId(1L);
            adminUser.setDisable(false);
            RepositoryTestObjects.insert(userMapper, adminUser, UserDO.class);

            String wrongSha256Hash = sha256Hash("wrongPassword");
            StudentIdLoginRequestDTO request = new StudentIdLoginRequestDTO();
            request.setStudentId(ADMIN_STUDENT_ID);
            request.setPassword(wrongSha256Hash);

            ResponseEntity<ResponseMessage> response = restTemplate.postForEntity(
                    "/api/v1/auth/login/student-id",
                    request,
                    ResponseMessage.class);

            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        }
    }

    private String sha256Hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
