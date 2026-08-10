package com.bluenet.web.application.service;

import com.bluenet.web.application.result.auth.AuthResult;
import com.bluenet.web.application.command.auth.AuthCommands;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 认证应用服务接口。
 * <p>
 * 定义了认证聚合在应用层的所有业务操作。
 * </p>
 */
public interface AuthAppService {

    /**
     * 学号密码登录。
     *
     * @param command
     *            学号登录命令
     * @param response
     *            HTTP响应
     * @return 登录结果
     */
    AuthResult.Login login(AuthCommands.StudentIdLoginCommand command, HttpServletResponse response);

    /**
     * 邮箱验证码登录。
     *
     * @param command
     *            邮箱登录命令
     * @param response
     *            HTTP响应，用于设置Cookie
     * @return 登录结果
     */
    AuthResult.Login loginWithEmail(AuthCommands.EmailLoginCommand command, HttpServletResponse response);

    /**
     * 发送邮箱验证码。
     *
     * @param command
     *            发送验证码命令
     */
    void sendVerificationCode(AuthCommands.SendVerificationCodeCommand command);

    /**
     * 用户登出。
     *
     * @param response
     *            HTTP响应
     */
    void logout(HttpServletResponse response);

    /**
     * 获取当前登录状态。
     *
     * @param response
     *            HTTP响应
     * @return 当前认证信息
     */
    AuthResult.AuthMe getAuthMe(HttpServletResponse response);

    /**
     * 发起GitHub OAuth登录。
     *
     * @param callbackBaseUrl
     *            后端回调基础URL
     * @return GitHub授权页面URL
     */
    String initiateGithubLogin(String callbackBaseUrl);

    /**
     * 发起GitHub账号绑定。
     *
     * @param callbackBaseUrl
     *            后端回调基础URL
     * @return GitHub授权页面URL
     */
    String initiateGithubBind(String callbackBaseUrl);

    /**
     * 处理GitHub OAuth回调（登录和绑定共用）。
     *
     * @param code
     *            授权码
     * @param state
     *            状态参数
     * @param callbackBaseUrl
     *            回调基础URL
     * @param response
     *            HTTP响应
     */
    void handleGithubCallback(String code, String state, String callbackBaseUrl, HttpServletResponse response);

    /**
     * 获取当前用户的GitHub绑定状态。
     *
     * @return GitHub用户名，未绑定返回null
     */
    String getGithubBindingStatus();

    /**
     * 解绑GitHub账号。
     */
    void unbindGithub();
}
