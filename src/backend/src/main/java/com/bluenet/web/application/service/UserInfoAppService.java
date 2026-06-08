package com.bluenet.web.application.service;

import com.bluenet.web.application.UserInfoResult;
import com.bluenet.web.application.command.userinfo.UserInfoCommands;

/**
 * 用户信息应用服务接口。
 * <p>
 * 定义了用户信息聚合在应用层的所有业务操作。
 * </p>
 */
public interface UserInfoAppService {

    /**
     * 获取当前用户信息。
     *
     * @param userId
     *            当前用户ID
     * @return 用户信息结果
     */
    UserInfoResult getMyInfo(Long userId);

    /**
     * 更新用户个人资料。
     *
     * @param userId
     *            当前用户ID
     * @param command
     *            更新个人资料命令
     */
    void updateProfile(Long userId, UserInfoCommands.UpdateProfileCommand command);

    /**
     * 获取当前用户各标签页计数。
     *
     * @param userId
     *            当前用户ID
     * @return 标签页计数结果
     */
    UserInfoResult.TabCounts getTabCounts(Long userId);

    /**
     * 发送邮箱验证码。
     *
     * @param command
     *            发送邮箱验证码命令
     */
    void sendEmailVerificationCode(UserInfoCommands.SendEmailVerificationCodeCommand command);

    /**
     * 修改邮箱。
     *
     * @param userId
     *            当前用户ID
     * @param command
     *            修改邮箱命令
     */
    void changeEmail(Long userId, UserInfoCommands.ChangeEmailCommand command);

    /**
     * 校验当前密码。
     *
     * @param command
     *            校验密码命令
     * @return 校验结果
     */
    String verifyCurrentPassword(UserInfoCommands.VerifyCurrentPasswordCommand command);

    /**
     * 修改密码。
     *
     * @param command
     *            修改密码命令
     */
    void changePassword(UserInfoCommands.ChangePasswordCommand command);

    /**
     * 更新用户头像。
     *
     * @param userId
     *            当前用户ID
     * @param command
     *            更新头像命令
     */
    void updateAvatar(Long userId, UserInfoCommands.UpdateAvatarCommand command);
}
