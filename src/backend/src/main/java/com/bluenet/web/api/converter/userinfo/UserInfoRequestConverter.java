package com.bluenet.web.api.converter.userinfo;

import com.bluenet.web.api.dto.user.ChangeEmailRequestDTO;
import com.bluenet.web.api.dto.user.ChangePasswordRequestDTO;
import com.bluenet.web.api.dto.user.SendEmailVerificationCodeRequestDTO;
import com.bluenet.web.api.dto.user.UpdateAvatarRequestDTO;
import com.bluenet.web.api.dto.user.UpdateProfileRequestDTO;
import com.bluenet.web.api.dto.user.VerifyPasswordRequestDTO;
import com.bluenet.web.application.command.userinfo.UserInfoCommands;
import org.springframework.stereotype.Component;

/**
 * 用户信息请求转换器
 * <p>
 * 负责将 API 层的 RequestDTO 转换为应用层的 Command
 * </p>
 */
@Component
public class UserInfoRequestConverter {

    public UserInfoCommands.UpdateProfileCommand toCommand(UpdateProfileRequestDTO dto) {
        return new UserInfoCommands.UpdateProfileCommand(
                dto.getUsername(),
                dto.getNickname(),
                dto.getCollege(),
                dto.getMajor(),
                dto.getDirection(),
                dto.getGender(),
                dto.getBio(),
                dto.getQrcodeFileId());
    }

    public UserInfoCommands.SendEmailVerificationCodeCommand toCommand(SendEmailVerificationCodeRequestDTO dto) {
        return new UserInfoCommands.SendEmailVerificationCodeCommand(dto.getEmail(), dto.getScene());
    }

    public UserInfoCommands.ChangeEmailCommand toCommand(ChangeEmailRequestDTO dto) {
        return new UserInfoCommands.ChangeEmailCommand(
                dto.getOriginalEmailVerifyCode(),
                dto.getNewEmail(),
                dto.getNewEmailVerifyCode());
    }

    public UserInfoCommands.VerifyCurrentPasswordCommand toCommand(Long userId, VerifyPasswordRequestDTO dto) {
        return new UserInfoCommands.VerifyCurrentPasswordCommand(userId, dto.getCurrentPassword());
    }

    public UserInfoCommands.ChangePasswordCommand toCommand(Long userId, ChangePasswordRequestDTO dto) {
        return new UserInfoCommands.ChangePasswordCommand(
                userId,
                dto.getToken(),
                dto.getNewPassword(),
                dto.getConfirmPassword());
    }

    public UserInfoCommands.UpdateAvatarCommand toCommand(UpdateAvatarRequestDTO dto) {
        return new UserInfoCommands.UpdateAvatarCommand(dto.getFileId());
    }
}
