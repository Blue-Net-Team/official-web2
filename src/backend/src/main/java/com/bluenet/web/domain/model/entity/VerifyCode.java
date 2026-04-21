package com.bluenet.web.domain.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
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
}
