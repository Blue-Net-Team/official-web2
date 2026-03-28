package com.bluenet.web.application.service;

import com.bluenet.web.api.dto.auth.AuthMeResponseDTO;
import com.bluenet.web.api.dto.auth.StudentIdLoginRequestDTO;
import com.bluenet.web.api.dto.auth.UserAuthResponseDTO;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 认证服务接口 提供用户登录、登出等认证功能
 */
public interface AuthService {

    /**
     * 学号密码登录
     *
     * @param requestDTO
     *            登录请求DTO
     * @param response
     *            HTTP响应，用于设置Cookie
     * @return 登录响应DTO，包含CSRF Token和用户信息
     */
    UserAuthResponseDTO login(StudentIdLoginRequestDTO requestDTO, HttpServletResponse response);

    /**
     * 用户登出
     *
     * @param response
     *            HTTP响应，用于清除Cookie
     */
    void logout(HttpServletResponse response);

    /**
     * 获取当前登录状态 用于页面刷新后恢复登录状态
     *
     * @param response
     *            HTTP响应，用于刷新CSRF Token Cookie
     * @return 登录状态响应
     */
    AuthMeResponseDTO getAuthMe(HttpServletResponse response);
}
