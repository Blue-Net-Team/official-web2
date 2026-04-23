package com.bluenet.web.application.converter;

import com.bluenet.web.application.ResetPasswordResult;
import org.springframework.stereotype.Component;

/**
 * 密码重置应用层转换器
 * <p>
 * 负责应用层 Result 与 API 层响应之间的转换
 * </p>
 */
@Component
public class ResetPasswordAppConverter {

    public String toDTO(ResetPasswordResult.VerifyStudent result) {
        return result.resetToken();
    }

    public String toDTO(ResetPasswordResult.VerifyEmail result) {
        return result.resetToken();
    }
}
