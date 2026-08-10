package com.bluenet.web.application.result.resetpassword;

/**
 * 密码重置聚合的应用层结果对象。
 * <p>
 * 封装了密码重置相关操作返回给 API 层的数据。
 * </p>
 */
public final class ResetPasswordResult {

    private ResetPasswordResult() {
        // 工具类，禁止实例化
    }

    /**
     * 验证学号结果。
     */
    public record VerifyStudent(
            /** 重置令牌 */
            String resetToken) {
    }

    /**
     * 验证邮箱结果。
     */
    public record VerifyEmail(
            /** 重置令牌 */
            String resetToken) {
    }
}
