package com.bluenet.web.application.command.resetpassword;

/**
 * 密码重置聚合的命令对象集合。
 * <p>
 * 定义了该聚合下所有应用层操作所需的命令参数。
 * </p>
 */
public class ResetPasswordCommands {

    /** 禁止实例化。 */
    private ResetPasswordCommands() {
    }

    /**
     * 验证学号命令。
     * <p>
     * 用于验证学号是否存在。
     * </p>
     */
    public record VerifyStudentCommand(
            /** 学号 */
            String studentId) {
        public VerifyStudentCommand {
            if (studentId != null) {
                studentId = studentId.trim();
            }
        }
    }

    /**
     * 验证邮箱命令。
     * <p>
     * 用于验证邮箱并获取重置令牌。
     * </p>
     */
    public record VerifyEmailCommand(
            /** 重置令牌 */
            String resetToken,
            /** 邮箱 */
            String email) {
        public VerifyEmailCommand {
            if (email != null) {
                email = email.trim();
            }
        }
    }

    /**
     * 发送验证码命令。
     * <p>
     * 用于发送密码重置验证码。
     * </p>
     */
    public record SendCodeCommand(
            /** 重置令牌 */
            String resetToken) {
    }

    /**
     * 验证验证码命令。
     * <p>
     * 用于验证密码重置验证码。
     * </p>
     */
    public record VerifyCodeCommand(
            /** 重置令牌 */
            String resetToken,
            /** 验证码 */
            String code) {
        public VerifyCodeCommand {
            if (code != null) {
                code = code.trim();
            }
        }
    }

    /**
     * 重置密码命令。
     * <p>
     * 用于重置用户密码。
     * </p>
     */
    public record ResetPasswordCommand(
            /** 重置令牌 */
            String resetToken,
            /** 新密码 */
            String newPassword) {
    }
}
