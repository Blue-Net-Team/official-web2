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
     * 验证码目标，如邮箱或者学号
     */
    private String target;

    /**
     * 验证码值
     */
    private String code;

    /**
     * 验证码过期时间
     */
    private LocalDateTime expireAt;

    /**
     * 验证码是否已使用
     */
    private boolean used;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expireAt);
    }
}
