package com.bluenet.web.infrastructure.security.reset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.bluenet.web.BaseIntegrationTest;

/**
 * ResetPasswordStateService 集成测试。
 * <p>
 * 验证密码重置流程状态在 Redis 中的创建、查询、更新、过期与删除生命周期。
 * </p>
 */
@DisplayName("ResetPasswordStateService 集成测试")
class ResetPasswordStateServiceIntegrationTest extends BaseIntegrationTest {

    private static final String KEY_PREFIX = "reset_pwd:";
    private static final String FIELD_STUDENT_ID = "studentId";
    private static final String FIELD_EMAIL = "email";
    private static final String FIELD_STEP = "step";
    private static final String FIELD_USER_ID = "userId";

    @Autowired
    private ResetPasswordStateService resetPasswordStateService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private String token;

    @BeforeEach
    void initToken() {
        token = resetPasswordStateService.create("2024001001", 42L);
    }

    @AfterEach
    void cleanupToken() {
        if (token != null) {
            redisTemplate.delete(KEY_PREFIX + token);
        }
    }

    @Test
    @DisplayName("create 应生成 UUID 令牌并存储初始状态")
    void create_shouldGenerateTokenAndStoreInitialState() {
        assertNotNull(token);
        assertFalse(token.isBlank());

        String studentId = resetPasswordStateService.getField(token, FIELD_STUDENT_ID);
        String userId = resetPasswordStateService.getField(token, FIELD_USER_ID);
        String step = resetPasswordStateService.getField(token, FIELD_STEP);

        assertEquals("2024001001", studentId);
        assertEquals("42", userId);
        assertEquals("1", step);

        Long ttlSeconds = redisTemplate.getExpire(KEY_PREFIX + token, TimeUnit.SECONDS);
        assertNotNull(ttlSeconds);
        assertTrue(ttlSeconds > 0);
        assertTrue(ttlSeconds <= 15 * 60);
    }

    @Test
    @DisplayName("exists 应在令牌存在时返回 true，删除后返回 false")
    void exists_shouldReflectTokenPresence() {
        assertTrue(resetPasswordStateService.exists(token));

        resetPasswordStateService.delete(token);

        assertFalse(resetPasswordStateService.exists(token));
    }

    @Test
    @DisplayName("getField 应返回指定字段值，不存在时返回 null")
    void getField_shouldReturnFieldValueOrNull() {
        String studentId = resetPasswordStateService.getField(token, FIELD_STUDENT_ID);
        assertEquals("2024001001", studentId);

        String absent = resetPasswordStateService.getField(token, FIELD_EMAIL);
        assertNull(absent);

        String unknownToken = "unknown-token";
        assertNull(resetPasswordStateService.getField(unknownToken, FIELD_STUDENT_ID));
    }

    @Test
    @DisplayName("getStep 应返回当前步骤或 0")
    void getStep_shouldReturnCurrentStepOrZero() {
        assertEquals(1, resetPasswordStateService.getStep(token));

        Map<String, String> updates = new HashMap<>();
        updates.put(FIELD_STEP, "2");
        resetPasswordStateService.update(token, updates);

        assertEquals(2, resetPasswordStateService.getStep(token));
    }

    @Test
    @DisplayName("update 应更新字段并刷新 TTL")
    void update_shouldUpdateFieldsAndRefreshTtl() throws InterruptedException {
        Map<String, String> updates = new HashMap<>();
        updates.put(FIELD_EMAIL, "test@example.com");
        updates.put(FIELD_STEP, "2");

        resetPasswordStateService.update(token, updates);

        assertEquals("test@example.com", resetPasswordStateService.getField(token, FIELD_EMAIL));
        assertEquals("2", resetPasswordStateService.getField(token, FIELD_STEP));
        assertEquals(2, resetPasswordStateService.getStep(token));

        Long ttlSeconds = redisTemplate.getExpire(KEY_PREFIX + token, TimeUnit.SECONDS);
        assertNotNull(ttlSeconds);
        assertTrue(ttlSeconds > 0);
        assertTrue(ttlSeconds <= 15 * 60);
    }

    @Test
    @DisplayName("delete 应移除令牌及所有字段")
    void delete_shouldRemoveTokenAndAllFields() {
        resetPasswordStateService.delete(token);

        assertFalse(resetPasswordStateService.exists(token));
        assertNull(resetPasswordStateService.getField(token, FIELD_STUDENT_ID));
        assertEquals(0, resetPasswordStateService.getStep(token));
    }
}
