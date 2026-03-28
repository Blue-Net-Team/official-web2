package com.bluenet.web.infrastructure.security.jwt;

import com.bluenet.web.infrastructure.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * JwtUtil单元测试
 */
@DisplayName("JwtUtil 单元测试")
@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private JwtUtil jwtUtil;

    private static final String TEST_SECRET = "dGhpcyBpcyBhIHRlc3Qgc2VjcmV0IGtleSBmb3IganVuaXQgdGVzdHMgYW5kIG11c3QgYmUgMjU2IGJpdHM=";
    private static final Long TEST_EXPIRATION = 3600L; // 1小时
    private static final Long TEST_USER_ID = 12345L;

    @BeforeEach
    void setUp() {
        lenient().when(jwtProperties.getSecret()).thenReturn(TEST_SECRET);
        lenient().when(jwtProperties.getExpiration()).thenReturn(TEST_EXPIRATION);
        jwtUtil.init();
    }

    /**
     * 生成 Token：应创建有效的 Token
     */
    @Test
    @DisplayName("生成 Token：应创建有效的 Token")
    void generateToken_shouldCreateValidToken() {
        // 执行
        String token = jwtUtil.generateToken(TEST_USER_ID);

        // 验证
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    /**
     * 解析 Token：应返回正确的载荷
     */
    @Test
    @DisplayName("解析 Token：应返回正确的载荷")
    void parseToken_shouldReturnCorrectPayload() {
        // 准备
        String token = jwtUtil.generateToken(TEST_USER_ID);

        // 执行
        JwtPayload payload = jwtUtil.parseToken(token);

        // 验证
        assertNotNull(payload);
        assertEquals(TEST_USER_ID, payload.getUserId());
        assertNotNull(payload.getJti());
        assertNotNull(payload.getIssuedAt());
        assertNotNull(payload.getExpiration());
        assertTrue(payload.getExpiration() > payload.getIssuedAt());
    }

    /**
     * 解析 Token：无效 Token 应返回 null
     */
    @Test
    @DisplayName("解析 Token：无效 Token 应返回 null")
    void parseToken_shouldReturnNullForInvalidToken() {
        // 执行
        JwtPayload payload = jwtUtil.parseToken("invalid.token.here");

        // 验证
        assertNull(payload);
    }

    /**
     * 解析 Token：格式错误的 Token 应返回 null
     */
    @Test
    @DisplayName("解析 Token：格式错误的 Token 应返回 null")
    void parseToken_shouldReturnNullForMalformedToken() {
        // 执行
        JwtPayload payload = jwtUtil.parseToken("not.a.valid.jwt");

        // 验证
        assertNull(payload);
    }

    /**
     * 获取 JTI：应返回正确的 JTI
     */
    @Test
    @DisplayName("获取 JTI：应返回正确的 JTI")
    void getJti_shouldReturnCorrectJti() {
        // 准备
        String token = jwtUtil.generateToken(TEST_USER_ID);
        JwtPayload payload = jwtUtil.parseToken(token);
        String expectedJti = payload.getJti();

        // 执行
        String jti = jwtUtil.getJti(token);

        // 验证
        assertEquals(expectedJti, jti);
    }

    /**
     * 获取 JTI：无效 Token 应返回 null
     */
    @Test
    @DisplayName("获取 JTI：无效 Token 应返回 null")
    void getJti_shouldReturnNullForInvalidToken() {
        // 执行
        String jti = jwtUtil.getJti("invalid.token");

        // 验证
        assertNull(jti);
    }

    /**
     * 验证 Token：有效 Token 应返回 true
     */
    @Test
    @DisplayName("验证 Token：有效 Token 应返回 true")
    void validateToken_shouldReturnTrueForValidToken() {
        // 准备
        String token = jwtUtil.generateToken(TEST_USER_ID);

        // 执行
        boolean isValid = jwtUtil.validateToken(token);

        // 验证
        assertTrue(isValid);
    }

    /**
     * 验证 Token：无效 Token 应返回 false
     */
    @Test
    @DisplayName("验证 Token：无效 Token 应返回 false")
    void validateToken_shouldReturnFalseForInvalidToken() {
        // 执行
        boolean isValid = jwtUtil.validateToken("invalid.token.here");

        // 验证
        assertFalse(isValid);
    }

    /**
     * 验证 Token：空 Token 应返回 false
     */
    @Test
    @DisplayName("验证 Token：空 Token 应返回 false")
    void validateToken_shouldReturnFalseForNullToken() {
        // 执行
        boolean isValid = jwtUtil.validateToken(null);

        // 验证
        assertFalse(isValid);
    }

    /**
     * 生成 Token：应生成唯一的 JTI
     */
    @Test
    @DisplayName("生成 Token：应生成唯一的 JTI")
    void generateToken_shouldCreateUniqueJti() {
        // 执行
        String token1 = jwtUtil.generateToken(TEST_USER_ID);
        String token2 = jwtUtil.generateToken(TEST_USER_ID);

        // 验证
        String jti1 = jwtUtil.getJti(token1);
        String jti2 = jwtUtil.getJti(token2);

        assertNotNull(jti1);
        assertNotNull(jti2);
        assertNotEquals(jti1, jti2);
    }

    /**
     * 解析 Token：过期 Token 应返回 null
     */
    @Test
    @DisplayName("解析 Token：过期 Token 应返回 null")
    void parseToken_shouldReturnNullForExpiredToken() throws InterruptedException {
        // 准备 - 创建一个极短有效期的token
        when(jwtProperties.getExpiration()).thenReturn(1L); // 1秒
        jwtUtil.init();
        String token = jwtUtil.generateToken(TEST_USER_ID);

        // 等待token过期
        Thread.sleep(2000);

        // 执行
        JwtPayload payload = jwtUtil.parseToken(token);

        // 验证
        assertNull(payload);
    }
}
