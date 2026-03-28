package com.bluenet.web.infrastructure.security.auth;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.bluenet.web.infrastructure.config.JwtProperties;

/**
 * AuthTokenServiceImpl单元测试
 */
@DisplayName("AuthTokenServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class AuthTokenServiceImplTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private SetOperations<String, String> setOperations;

    @InjectMocks
    private AuthTokenServiceImpl authTokenService;

    private static final Long TEST_USER_ID = 12345L;
    private static final String TEST_JTI = "test-jti-123";
    private static final Long TEST_EXPIRATION = 43200L; // 12小时

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(redisTemplate.opsForSet()).thenReturn(setOperations);
        lenient().when(jwtProperties.getExpiration()).thenReturn(TEST_EXPIRATION);
    }

    /**
     * 存储 Token：应将 Token 存入 Redis
     */
    @Test
    @DisplayName("存储 Token：应将 Token 存入 Redis")
    void storeToken_shouldStoreTokenInRedis() {
        // 执行
        authTokenService.storeToken(TEST_JTI, TEST_USER_ID);

        // 验证
        verify(valueOperations).set(
                eq("auth:token:" + TEST_JTI),
                eq(TEST_USER_ID.toString()),
                eq(TEST_EXPIRATION),
                eq(TimeUnit.SECONDS));
        verify(setOperations).add(eq("auth:user:tokens:" + TEST_USER_ID), eq(TEST_JTI));
        verify(redisTemplate).expire(
                eq("auth:user:tokens:" + TEST_USER_ID),
                eq(TEST_EXPIRATION + 3600),
                eq(TimeUnit.SECONDS));
    }

    /**
     * 验证 Token：Token 存在时应返回用户 ID
     */
    @Test
    @DisplayName("验证 Token：Token 存在时应返回用户 ID")
    void validateToken_shouldReturnUserIdWhenTokenExists() {
        // 准备
        when(valueOperations.get("auth:token:" + TEST_JTI)).thenReturn(TEST_USER_ID.toString());

        // 执行
        Optional<Long> result = authTokenService.validateToken(TEST_JTI);

        // 验证
        assertTrue(result.isPresent());
        assertEquals(TEST_USER_ID, result.get());
    }

    /**
     * 验证 Token：Token 不存在时应返回空
     */
    @Test
    @DisplayName("验证 Token：Token 不存在时应返回空")
    void validateToken_shouldReturnEmptyWhenTokenNotExists() {
        // 准备
        when(valueOperations.get("auth:token:" + TEST_JTI)).thenReturn(null);

        // 执行
        Optional<Long> result = authTokenService.validateToken(TEST_JTI);

        // 验证
        assertFalse(result.isPresent());
    }

    /**
     * 验证 Token：用户 ID 格式无效时应返回空
     */
    @Test
    @DisplayName("验证 Token：用户 ID 格式无效时应返回空")
    void validateToken_shouldReturnEmptyWhenInvalidUserIdFormat() {
        // 准备
        when(valueOperations.get("auth:token:" + TEST_JTI)).thenReturn("invalid-user-id");

        // 执行
        Optional<Long> result = authTokenService.validateToken(TEST_JTI);

        // 验证
        assertFalse(result.isPresent());
    }

    /**
     * 撤销 Token：应从 Redis 中移除 Token
     */
    @Test
    @DisplayName("撤销 Token：应从 Redis 中移除 Token")
    void revokeToken_shouldRemoveTokenFromRedis() {
        // 准备
        when(valueOperations.get("auth:token:" + TEST_JTI)).thenReturn(TEST_USER_ID.toString());

        // 执行
        boolean result = authTokenService.revokeToken(TEST_JTI);

        // 验证
        assertTrue(result);
        verify(redisTemplate).delete("auth:token:" + TEST_JTI);
        verify(setOperations).remove("auth:user:tokens:" + TEST_USER_ID, TEST_JTI);
    }

    /**
     * 撤销 Token：Token 不存在时应返回 false
     */
    @Test
    @DisplayName("撤销 Token：Token 不存在时应返回 false")
    void revokeToken_shouldReturnFalseWhenTokenNotExists() {
        // 准备
        when(valueOperations.get("auth:token:" + TEST_JTI)).thenReturn(null);

        // 执行
        boolean result = authTokenService.revokeToken(TEST_JTI);

        // 验证
        assertFalse(result);
        verify(redisTemplate, never()).delete(anyString());
    }

    /**
     * 撤销所有 Token：应移除该用户的所有 Token
     */
    @Test
    @DisplayName("撤销所有 Token：应移除该用户的所有 Token")
    void revokeAllUserTokens_shouldRemoveAllTokens() {
        // 准备
        Set<String> tokens = Set.of("jti-1", "jti-2", "jti-3");
        when(setOperations.members("auth:user:tokens:" + TEST_USER_ID)).thenReturn(tokens);

        // 执行
        authTokenService.revokeAllUserTokens(TEST_USER_ID);

        // 验证
        verify(redisTemplate).delete("auth:token:jti-1");
        verify(redisTemplate).delete("auth:token:jti-2");
        verify(redisTemplate).delete("auth:token:jti-3");
        verify(redisTemplate).delete("auth:user:tokens:" + TEST_USER_ID);
    }

    /**
     * 撤销所有 Token：用户无 Token 时不应执行删除操作
     */
    @Test
    @DisplayName("撤销所有 Token：用户无 Token 时不应执行删除操作")
    void revokeAllUserTokens_shouldDoNothingWhenNoTokens() {
        // 准备
        when(setOperations.members("auth:user:tokens:" + TEST_USER_ID)).thenReturn(null);

        // 执行
        authTokenService.revokeAllUserTokens(TEST_USER_ID);

        // 验证 - 不应该调用delete
        verify(redisTemplate, never()).delete(anyString());
    }

    /**
     * 存储 Token：应清理该用户的旧 Token
     */
    @Test
    @DisplayName("存储 Token：应清理该用户的旧 Token")
    void storeToken_shouldRevokeOldTokens() {
        // 准备
        Set<String> oldTokens = Set.of("old-jti-1", "old-jti-2");
        when(setOperations.members("auth:user:tokens:" + TEST_USER_ID)).thenReturn(oldTokens);

        // 执行
        authTokenService.storeToken(TEST_JTI, TEST_USER_ID);

        // 验证旧token被删除
        verify(redisTemplate).delete("auth:token:old-jti-1");
        verify(redisTemplate).delete("auth:token:old-jti-2");
        verify(setOperations).remove("auth:user:tokens:" + TEST_USER_ID, "old-jti-1");
        verify(setOperations).remove("auth:user:tokens:" + TEST_USER_ID, "old-jti-2");
    }
}
