package com.bluenet.web.api.converter.auth;

import com.bluenet.web.api.dto.auth.AuthMeResponseDTO;
import com.bluenet.web.api.dto.auth.UserAuthResponseDTO;
import com.bluenet.web.api.dto.user.UserInfo;
import com.bluenet.web.application.AuthResult;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.util.GradeCalculator;
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
    public UserAuthResponseDTO toDTO(AuthResult.Login result) {
        UserAuthResponseDTO dto = new UserAuthResponseDTO();
        dto.setCsrfToken(result.csrfToken());
        dto.setUserInfo(convertToUserInfo(result.user()));
        return dto;
    }

    /**
     * 将获取登录状态结果转换为 API 响应 DTO
     */
    public AuthMeResponseDTO toDTO(AuthResult.AuthMe result) {
        AuthMeResponseDTO dto = new AuthMeResponseDTO();
        dto.setAuthenticated(result.authenticated());
        dto.setUserInfo(result.user() != null ? convertToUserInfo(result.user()) : null);
        dto.setCsrfToken(result.csrfToken());
        return dto;
    }

    /**
     * 将UserVO领域值对象转换为UserInfo DTO
     */
    private UserInfo convertToUserInfo(UserVO userVO) {
        String gradeLabel = GradeCalculator.getGradeLabel(userVO.getStudentId(), userVO.getAssessmentGradeYear());

        return UserInfo.builder()
                .id(userVO.getId())
                .username(userVO.getUsername())
                .nickname(userVO.getNickname())
                .email(userVO.getEmail())
                .college(userVO.getCollege())
                .major(userVO.getMajor())
                .grade(gradeLabel)
                .direction(userVO.getDirection())
                .gender(userVO.getGender())
                .avatarFileId(userVO.getAvatarFileId())
                .roleName(userVO.getRoleName())
                .bio(userVO.getBio())
                .githubUsername(userVO.getGithubUsername())
                .build();
    }
}
