package com.bluenet.web.application.service;

import com.bluenet.web.application.result.resetpassword.ResetPasswordResult;
import com.bluenet.web.application.command.resetpassword.ResetPasswordCommands;

/**
 * 密码重置应用服务接口。
 * <p>
 * 定义了密码重置聚合在应用层的所有业务操作。
 * </p>
 */
public interface ResetPasswordAppService {

    /**
     * 验证学号是否存在。
     *
     * @param command
     *            验证学号命令
     * @return 学号验证结果
     */
    ResetPasswordResult.VerifyStudent verifyStudent(ResetPasswordCommands.VerifyStudentCommand command);

    /**
     * 验证邮箱是否与学号关联。
     *
     * @param command
     *            验证邮箱命令
     * @return 邮箱验证结果
     */
    ResetPasswordResult.VerifyEmail verifyEmail(ResetPasswordCommands.VerifyEmailCommand command);

    /**
     * 发送验证码到已验证的邮箱。
     *
     * @param command
     *            发送验证码命令
     */
    void sendCode(ResetPasswordCommands.SendCodeCommand command);

    /**
     * 验证验证码是否正确。
     *
     * @param command
     *            验证验证码命令
     */
    void verifyCode(ResetPasswordCommands.VerifyCodeCommand command);

    /**
     * 重置密码。
     *
     * @param command
     *            重置密码命令
     */
    void resetPassword(ResetPasswordCommands.ResetPasswordCommand command);
}
