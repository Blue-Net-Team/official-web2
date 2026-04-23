package com.bluenet.web.application.converter;

import com.bluenet.web.api.dto.auth.AuthMeResponseDTO;
import com.bluenet.web.api.dto.auth.UserAuthResponseDTO;
import com.bluenet.web.application.AuthResult;
import org.springframework.stereotype.Component;

/**
 * 认证应用层转换器
 * <p>
 * 负责应用层 Result 与 API 层 DTO 之间的转换
 * </p>
 */
@Component
public class AuthAppConverter {

    private final UserAppConverter userConverter;

    public AuthAppConverter(UserAppConverter userConverter) {
        this.userConverter = userConverter;
    }

    /**
     * 将登录结果转换为 API 响应 DTO
     */
    public UserAuthResponseDTO toDTO(AuthResult.Login result) {
        UserAuthResponseDTO dto = new UserAuthResponseDTO();
        dto.setCsrfToken(result.csrfToken());
        dto.setUserInfo(userConverter.convertToUserInfo(result.user()));
        return dto;
    }

    /**
     * 将获取登录状态结果转换为 API 响应 DTO
     */
    public AuthMeResponseDTO toDTO(AuthResult.AuthMe result) {
        AuthMeResponseDTO dto = new AuthMeResponseDTO();
        dto.setAuthenticated(result.authenticated());
        dto.setUserInfo(result.user() != null ? userConverter.convertToUserInfo(result.user()) : null);
        dto.setCsrfToken(result.csrfToken());
        return dto;
    }
}
