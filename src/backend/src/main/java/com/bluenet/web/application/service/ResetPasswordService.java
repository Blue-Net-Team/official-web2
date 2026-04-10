package com.bluenet.web.application.service;

/**
 * 密码重置服务接口
 * <p>
 * 编排密码重置的4步流程：学号验证 → 邮箱验证 → 发送验证码 → 重置密码
 * </p>
 */
public interface ResetPasswordService {

    /**
     * 步骤1：验证学号是否存在
     *
     * @param studentId
     *            学号
     * @return 重置令牌
     */
    String verifyStudent(String studentId);

    /**
     * 步骤2：验证邮箱是否与学号关联
     *
     * @param resetToken
     *            重置令牌
     * @param email
     *            邮箱地址
     * @return 重置令牌
     */
    String verifyEmail(String resetToken, String email);

    /**
     * 步骤3：发送验证码到已验证的邮箱
     *
     * @param resetToken
     *            重置令牌
     * @param clientIp
     *            客户端IP
     */
    void sendCode(String resetToken, String clientIp);

    /**
     * 步骤3.5：验证验证码是否正确
     *
     * @param resetToken
     *            重置令牌
     * @param code
     *            验证码
     */
    void verifyCode(String resetToken, String code);

    /**
     * 步骤4：重置密码（验证码已在步骤3.5验证通过）
     *
     * @param resetToken
     *            重置令牌
     * @param newPassword
     *            新密码
     */
    void resetPassword(String resetToken, String newPassword);
}
