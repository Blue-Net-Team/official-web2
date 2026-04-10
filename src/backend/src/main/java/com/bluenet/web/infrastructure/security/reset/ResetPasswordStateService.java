package com.bluenet.web.infrastructure.security.reset;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 密码重置流程状态管理服务
 * <p>
 * 使用 Redis Hash 存储密码重置流程的中间状态（学号、邮箱、当前步骤）， Key 格式为
 * {@code reset_pwd:{token}}，TTL 15 分钟。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResetPasswordStateService {

    private static final String KEY_PREFIX = "reset_pwd:";
    private static final long TTL_MINUTES = 15;

    private static final String FIELD_STUDENT_ID = "studentId";
    private static final String FIELD_EMAIL = "email";
    private static final String FIELD_STEP = "step";
    private static final String FIELD_USER_ID = "userId";

    private final StringRedisTemplate redisTemplate;

    /**
     * 创建新的密码重置流程
     *
     * @param studentId
     *            学号
     * @param userId
     *            用户ID
     * @return 重置令牌（UUID）
     */
    public String create(String studentId, Long userId) {
        String token = UUID.randomUUID().toString();
        Map<String, String> state = new HashMap<>();
        state.put(FIELD_STUDENT_ID, studentId);
        state.put(FIELD_USER_ID, userId.toString());
        state.put(FIELD_STEP, "1");
        redisTemplate.opsForHash().putAll(key(token), state);
        redisTemplate.expire(key(token), TTL_MINUTES, TimeUnit.MINUTES);
        log.debug("Reset password state created for student: {}", studentId);
        return token;
    }

    /**
     * 获取流程状态中的字段值
     *
     * @param token
     *            重置令牌
     * @param field
     *            字段名
     * @return 字段值，不存在返回 null
     */
    public String getField(String token, String field) {
        return (String) redisTemplate.opsForHash().get(key(token), field);
    }

    /**
     * 获取当前步骤
     *
     * @param token
     *            重置令牌
     * @return 步骤号，不存在返回 0
     */
    public int getStep(String token) {
        String step = getField(token, FIELD_STEP);
        return step != null ? Integer.parseInt(step) : 0;
    }

    /**
     * 更新流程状态字段
     *
     * @param token
     *            重置令牌
     * @param fields
     *            要更新的字段映射
     */
    public void update(String token, Map<String, String> fields) {
        redisTemplate.opsForHash().putAll(key(token), fields);
        // 刷新 TTL
        redisTemplate.expire(key(token), TTL_MINUTES, TimeUnit.MINUTES);
    }

    /**
     * 删除流程状态
     *
     * @param token
     *            重置令牌
     */
    public void delete(String token) {
        redisTemplate.delete(key(token));
        log.debug("Reset password state deleted for token: {}", token);
    }

    /**
     * 检查令牌是否存在
     *
     * @param token
     *            重置令牌
     * @return 是否存在
     */
    public boolean exists(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key(token)));
    }

    private String key(String token) {
        return KEY_PREFIX + token;
    }
}
