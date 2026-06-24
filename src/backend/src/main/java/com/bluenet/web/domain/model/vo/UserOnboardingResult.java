package com.bluenet.web.domain.model.vo;

import lombok.Builder;

/**
 * 用户入职结果值对象。
 */
@Builder
public record UserOnboardingResult(
        /** 用户 ID */
        Long userId,
        /** 初始密码（明文） */
        String initialPassword,
        /** 内推码 */
        String referralCode) {
}
