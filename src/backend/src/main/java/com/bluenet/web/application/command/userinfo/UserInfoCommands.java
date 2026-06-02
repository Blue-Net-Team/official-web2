package com.bluenet.web.application.command.userinfo;

import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.Gender;

/**
 * 用户信息聚合的命令对象集合。
 * <p>
 * 定义了该聚合下所有应用层操作所需的命令参数。
 * </p>
 */
public class UserInfoCommands {

    /** 禁止实例化。 */
    private UserInfoCommands() {
    }

    /**
     * 更新个人资料命令。
     * <p>
     * 用于更新用户的个人资料信息。
     * </p>
     */
    public record UpdateProfileCommand(
            /** 用户名 */
            String username,
            /** 昵称 */
            String nickname,
            /** 学院 */
            String college,
            /** 专业 */
            String major,
            /** 方向 */
            Direction direction,
            /** 性别 */
            Gender gender,
            /** 个人简介 */
            String bio,
            /** 微信二维码文件ID */
            Long qrcodeFileId) {
    }

    /**
     * 发送邮箱验证码命令。
     * <p>
     * 用于向邮箱发送验证码。
     * </p>
     */
    public record SendEmailVerificationCodeCommand(
            /** 邮箱 */
            String email,
            /** 场景 */
            String scene) {
    }

    /**
     * 更换邮箱命令。
     * <p>
     * 用于更换用户绑定的邮箱。
     * </p>
     */
    public record ChangeEmailCommand(
            /** 原邮箱验证码 */
            String originalEmailVerifyCode,
            /** 新邮箱 */
            String newEmail,
            /** 新邮箱验证码 */
            String newEmailVerifyCode) {
    }

    /**
     * 验证当前密码命令。
     * <p>
     * 用于验证用户当前密码。
     * </p>
     */
    public record VerifyCurrentPasswordCommand(
            /** 用户ID */
            Long userId,
            /** 当前密码 */
            String currentPassword) {
    }

    /**
     * 修改密码命令。
     * <p>
     * 用于修改用户密码。
     * </p>
     */
    public record ChangePasswordCommand(
            /** 用户ID */
            Long userId,
            /** 令牌 */
            String token,
            /** 新密码 */
            String newPassword,
            /** 确认密码 */
            String confirmPassword) {
    }

    /**
     * 更新头像命令。
     * <p>
     * 用于更新用户头像。
     * </p>
     */
    public record UpdateAvatarCommand(
            /** 文件ID */
            Long fileId) {
    }
}
