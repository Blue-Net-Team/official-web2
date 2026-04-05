package com.bluenet.web.application.service;

import com.bluenet.web.api.dto.auth.AuthMeResponseDTO;
import com.bluenet.web.api.dto.auth.SendVerificationCodeRequestDTO;
import com.bluenet.web.api.dto.auth.StudentIdLoginRequestDTO;
import com.bluenet.web.api.dto.auth.UserAuthResponseDTO;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 认证服务接口 提供用户登录、登出等认证功能
 */
public interface AuthService {

    /**
     * 学号密码登录
     */
    UserAuthResponseDTO login(StudentIdLoginRequestDTO requestDTO, HttpServletResponse response);

    /**
     * 邮箱验证码登录
     *
     * @param email
     *            邮箱
     * @param verifyCode
     *            验证码
     * @param response
     *            HTTP响应，用于设置Cookie
     * @return 登录响应DTO，包含CSRF Token和用户信息
     */
    UserAuthResponseDTO loginWithEmail(String email, String verifyCode, HttpServletResponse response);

    /**
     * 发送邮箱验证码
     *
     * @param requestDTO
     *            发送验证码请求DTO
     */
    void sendVerificationCode(SendVerificationCodeRequestDTO requestDTO);

    /**
     * 用户登出
     */
    void logout(HttpServletResponse response);

    /**
     * 获取当前登录状态
     */
    AuthMeResponseDTO getAuthMe(HttpServletResponse response);

    /**
     * 发起 GitHub OAuth 登录
     *
     * @param callbackBaseUrl
     *            后端回调基础 URL（如 http://localhost:8080）
     * @return GitHub 授权页面 URL
     */
    String initiateGithubLogin(String callbackBaseUrl);

    /**
     * 发起 GitHub 账号绑定
     *
     * @param callbackBaseUrl
     *            后端回调基础 URL
     * @return GitHub 授权页面 URL
     */
    String initiateGithubBind(String callbackBaseUrl);

    /**
     * 处理 GitHub OAuth 回调（登录和绑定共用）
     */
    void handleGithubCallback(String code, String state, String callbackBaseUrl, HttpServletResponse response);

    /**
     * 获取当前用户的 GitHub 绑定状态
     *
     * @return GitHub 用户名，未绑定返回 null
     */
    String getGithubBindingStatus();

    /**
     * 解绑 GitHub 账号
     */
    void unbindGithub();
}
