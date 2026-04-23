package com.bluenet.web.application.command.auth;

/**
 * 认证聚合的命令对象集合。
 * <p>
 * 定义了该聚合下所有应用层操作所需的命令参数。
 * </p>
 */
public final class AuthCommands {

    /** 禁止实例化。 */
    private AuthCommands() {
    }

    /**
     * 学号登录命令。
     * <p>
     * 用于使用学号和密码进行登录。
     * </p>
     */
    public record StudentIdLoginCommand(
            /** 学号 */
            String studentId,
            /** 密码 */
            String password) {
        public StudentIdLoginCommand {
            if (studentId != null) {
                studentId = studentId.trim();
            }
        }
    }

    /**
     * 邮箱验证码登录命令。
     * <p>
     * 用于使用邮箱和验证码进行登录。
     * </p>
     */
    public record EmailLoginCommand(
            /** 邮箱 */
            String email,
            /** 验证码 */
            String verifyCode) {
        public EmailLoginCommand {
            if (email != null) {
                email = email.trim();
            }
            if (verifyCode != null) {
                verifyCode = verifyCode.trim();
            }
        }
    }

    /**
     * 发送验证码命令。
     * <p>
     * 用于向指定邮箱发送验证码。
     * </p>
     */
    public record SendVerificationCodeCommand(
            /** 邮箱 */
            String email,
            /** 场景 */
            String scene) {
        public SendVerificationCodeCommand {
            if (email != null) {
                email = email.trim();
            }
            if (scene != null) {
                scene = scene.trim();
            }
        }
    }
}
