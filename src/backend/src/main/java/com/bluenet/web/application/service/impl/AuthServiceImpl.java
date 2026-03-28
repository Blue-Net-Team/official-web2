package com.bluenet.web.application.service.impl;

import java.util.Optional;

import com.bluenet.web.api.dto.auth.AuthMeResponseDTO;
import com.bluenet.web.application.converter.UserConverter;
import com.bluenet.web.infrastructure.security.auth.AuthTokenService;
import com.bluenet.web.infrastructure.security.cookie.CookieService;
import com.bluenet.web.infrastructure.security.csrf.CsrfTokenService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bluenet.web.api.dto.auth.StudentIdLoginRequestDTO;
import com.bluenet.web.api.dto.auth.UserAuthResponseDTO;
import com.bluenet.web.application.service.AuthService;
import com.bluenet.web.domain.exception.Unauthorized;
import com.bluenet.web.domain.model.enumerate.LocalLoginType;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.service.AuthDomainService;
import com.bluenet.web.infrastructure.security.jwt.JwtPayload;
import com.bluenet.web.infrastructure.security.jwt.JwtUtil;
import com.bluenet.web.infrastructure.security.util.UserCTX;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 认证应用服务实现类
 * <p>
 * 应用层服务，负责： - 接收DTO参数并调用领域服务 - 协调多个领域服务完成用例 - 将领域对象转换为DTO返回 - 事务管理 - Cookie 设置
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthDomainService authDomainService;
    private final JwtUtil jwtUtil;
    private final AuthTokenService authTokenService;
    private final UserConverter userConverter;
    private final CookieService cookieService;
    private final CsrfTokenService csrfTokenService;

    @Override
    @Transactional(readOnly = true)
    public UserAuthResponseDTO login(StudentIdLoginRequestDTO requestDTO, HttpServletResponse response) {
        // 1. 调用领域服务验证登录
        Optional<UserVO> userVOOptional = authDomainService.checkLocalValid(
                requestDTO.getStudentId(),
                requestDTO.getPassword(),
                LocalLoginType.STUDENT_ID);

        // 2. 处理验证结果
        UserVO userVO = userVOOptional.orElseThrow(() -> {
            log.warn("Login failed: invalid credentials - {}", requestDTO.getStudentId());
            return new Unauthorized("学号或密码错误");
        });

        // 3. 生成JWT Token（使用用户ID作为subject）
        String jwtToken = jwtUtil.generateToken(extractUserId(userVO));

        // 4. 调用领域服务写入Token缓存（白名单）
        authTokenService.storeToken(jwtUtil.getJti(jwtToken), extractUserId(userVO));

        // 5. 生成 CSRF Token
        String csrfToken = csrfTokenService.generateCsrfToken();

        // 6. 设置 Cookie（auth_token + csrf_token）
        cookieService.setAuthCookies(response, jwtToken, csrfToken);

        // 7. 构建响应DTO（只返回 csrfToken，JWT 通过 HttpOnly Cookie 传递）
        UserAuthResponseDTO responseDTO = new UserAuthResponseDTO();
        responseDTO.setCsrfToken(csrfToken);
        responseDTO.setUserInfo(userConverter.convertToUserInfo(userVO));

        log.info("User logged in successfully: {}", requestDTO.getStudentId());
        return responseDTO;
    }

    @Override
    public void logout(HttpServletResponse response) {
        // 1. 获取当前用户的 JWT jti
        UserVO currentUser = UserCTX.getCurrentUser();
        if (currentUser != null) {
            // 从 SecurityContext 获取 JWT payload
            JwtPayload payload = (JwtPayload) org.springframework.security.core.context.SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getCredentials();

            if (payload != null && payload.getJti() != null) {
                // 2. 调用领域服务清除Token缓存
                authTokenService.revokeToken(payload.getJti());
                log.info("Token revoked successfully for jti: {}", payload.getJti());
            }
        }

        // 3. 清除 Cookie
        cookieService.clearAuthCookies(response);

        // 4. 清除当前安全上下文
        UserCTX.clear();

        log.info("User logged out successfully");
    }

    @Override
    public AuthMeResponseDTO getAuthMe(HttpServletResponse response) {
        AuthMeResponseDTO responseDTO = new AuthMeResponseDTO();

        // 1. 检查是否已登录
        UserVO currentUser = UserCTX.getCurrentUser();
        if (currentUser == null) {
            responseDTO.setAuthenticated(false);
            responseDTO.setUserInfo(null);
            responseDTO.setCsrfToken(null);
            return responseDTO;
        }

        // 2. 生成新的 CSRF Token（刷新）
        String csrfToken = csrfTokenService.generateCsrfToken();

        // 3. 设置 CSRF Token Cookie
        cookieService.setCsrfTokenCookie(response, csrfToken);

        // 4. 构建响应
        responseDTO.setAuthenticated(true);
        responseDTO.setUserInfo(userConverter.convertToUserInfo(currentUser));
        responseDTO.setCsrfToken(csrfToken);

        return responseDTO;
    }

    /**
     * 从UserVO中提取用户ID
     * <p>
     * 注意：当前UserVO中只有学号（studentId），而学号可能是非数字（如包含字母的13位学号）。
     * 为了正确使用数据库自增ID作为JWT的subject，需要在UserVO中添加userId字段。
     * </p>
     *
     * @param userVO
     *            用户领域值对象
     * @return 用户ID
     */
    private Long extractUserId(UserVO userVO) {
        return userVO.getId();
    }
}
