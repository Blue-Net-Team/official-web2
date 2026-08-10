package com.bluenet.web.domain.model.entity;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VerifyCode {
    /**
     * 当前对象在系统中的唯一标识。
     */
    private Long id;
    /**
     * 验证码发送目标，例如邮箱地址。
     */
    private String target;
    /**
     * 验证码、模板编码或业务唯一编码。
     */
    private String code;
    /**
     * 验证码或临时凭证过期时间。
     */
    private LocalDateTime expireAt;
    /**
     * 验证码实际使用时间。
     */
    private LocalDateTime usedAt;
    /**
     * 验证码或模板适用的业务场景。
     */
    private String scene;

    private VerifyCode(Long id, String target, String code, LocalDateTime expireAt,
            LocalDateTime usedAt, String scene) {
        this.id = id;
        this.target = target;
        this.code = code;
        this.expireAt = expireAt;
        this.usedAt = usedAt;
        this.scene = scene;
    }

    /**
     * 构造新验证码聚合根
     *
     * @param target
     *            发送目标
     * @param code
     *            验证码
     * @param expireAt
     *            过期时间
     * @param scene
     *            业务场景
     * @return 新的验证码实体
     */
    public static VerifyCode create(String target, String code, LocalDateTime expireAt, String scene) {
        return new VerifyCode(null, target, code, expireAt, null, scene);
    }

    /**
     * 从数据库重建 —— 跳过创建校验
     *
     * @param id
     *            验证码ID
     * @param target
     *            发送目标
     * @param code
     *            验证码
     * @param expireAt
     *            过期时间
     * @param usedAt
     *            使用时间
     * @param scene
     *            业务场景
     * @return 重建的验证码实体
     */
    public static VerifyCode reconstruct(Long id, String target, String code, LocalDateTime expireAt,
            LocalDateTime usedAt, String scene) {
        return new VerifyCode(id, target, code, expireAt, usedAt, scene);
    }

    /**
     * 判断验证码是否已被使用。
     *
     * @return 已使用时返回 true
     */
    public boolean isUsed() {
        return usedAt != null;
    }

    /**
     * 判断验证码是否已过期。
     *
     * @return 已过期时返回 true
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expireAt);
    }
}
