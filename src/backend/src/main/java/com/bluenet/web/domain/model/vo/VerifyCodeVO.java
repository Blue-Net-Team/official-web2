package com.bluenet.web.domain.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class VerifyCodeVO {
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
     * 验证码是否已被使用。
     */
    private boolean used;
    /**
     * 验证码或模板适用的业务场景。
     */
    private String scene;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expireAt);
    }
}
