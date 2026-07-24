package com.bluenet.web.api.converter.auth;

import com.bluenet.web.api.dto.auth.AuthMeResponseDTO;
import com.bluenet.web.api.dto.auth.UserAuthResponseDTO;
import com.bluenet.web.api.dto.user.UserInfo;
import com.bluenet.web.application.result.auth.AuthResult;
import com.bluenet.web.application.result.user.UserInfoResult;
import org.springframework.stereotype.Component;

/**
 * 认证响应转换器
 * <p>
 * 负责将应用层认证结果转换为 API 响应 DTO
 * </p>
 */
@Component
public class AuthResponseConverter {

    /**
     * 将登录结果转换为 API 响应 DTO
     */
    public UserAuthResponseDTO toDTO(AuthResult.Login result, UserInfoResult userInfoResult) {
        UserAuthResponseDTO dto = new UserAuthResponseDTO();
        dto.setCsrfToken(result.csrfToken());
        dto.setUserInfo(convertToUserInfo(userInfoResult));
        return dto;
    }

    /**
     * 将获取登录状态结果转换为 API 响应 DTO
     */
    public AuthMeResponseDTO toDTO(AuthResult.AuthMe result, UserInfoResult userInfoResult) {
        AuthMeResponseDTO dto = new AuthMeResponseDTO();
        dto.setAuthenticated(result.authenticated());
        dto.setUserInfo(userInfoResult != null ? convertToUserInfo(userInfoResult) : null);
        dto.setCsrfToken(result.csrfToken());
        return dto;
    }

    /**
     * 将应用层用户信息结果转换为 API 响应 DTO
     */
    private UserInfo convertToUserInfo(UserInfoResult result) {
        return UserInfo.builder()
                .id(result.id())
                .username(result.username())
                .nickname(result.nickname())
                .college(result.college())
                .major(result.major())
                .grade(result.grade())
                .email(result.email())
                .avatarFileId(result.avatarFileId())
                .roleName(result.roleName())
                .direction(result.direction())
                .gender(result.gender())
                .bio(result.bio())
                .githubUsername(result.githubUsername())
                .qrcodeFileId(result.wechatQrcode())
                .internalReferralCode(result.internalReferralCode())
                .build();
    }
}
