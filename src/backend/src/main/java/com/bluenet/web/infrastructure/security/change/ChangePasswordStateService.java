package com.bluenet.web.infrastructure.security.change;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 修改密码流程状态管理服务
 * <p>
 * 使用 Redis Hash 存储修改密码流程的中间状态（userId、步骤、验证标记）， Key 格式为
 * {@code change_pwd:{token}}，TTL 15 分钟。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChangePasswordStateService {

    private static final String KEY_PREFIX = "change_pwd:";
    private static final long TTL_MINUTES = 15;

    private static final String FIELD_USER_ID = "userId";
    private static final String FIELD_STEP = "step";
    private static final String FIELD_VERIFIED = "verified";

    private final StringRedisTemplate redisTemplate;

    /**
     * 创建新的修改密码流程
     *
     * @param userId
     *            用户ID
     * @return 修改令牌（UUID）
     */
    public String create(Long userId) {
        String token = UUID.randomUUID().toString();
        Map<String, String> state = new HashMap<>();
        state.put(FIELD_USER_ID, userId.toString());
        state.put(FIELD_STEP, "1");
        state.put(FIELD_VERIFIED, "true");
        redisTemplate.opsForHash().putAll(key(token), state);
        redisTemplate.expire(key(token), TTL_MINUTES, TimeUnit.MINUTES);
        log.debug("Change password state created for userId: {}", userId);
        return token;
    }

    /**
     * 获取流程状态中的字段值
     *
     * @param token
     *            修改令牌
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
     *            修改令牌
     * @return 步骤号，不存在返回 0
     */
    public int getStep(String token) {
        String step = getField(token, FIELD_STEP);
        return step != null ? Integer.parseInt(step) : 0;
    }

    /**
     * 检查令牌是否存在
     *
     * @param token
     *            修改令牌
     * @return 是否存在
     */
    public boolean exists(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key(token)));
    }

    /**
     * 删除流程状态
     *
     * @param token
     *            修改令牌
     */
    public void delete(String token) {
        redisTemplate.delete(key(token));
        log.debug("Change password state deleted for token: {}", token);
    }

    private String key(String token) {
        return KEY_PREFIX + token;
    }
}
