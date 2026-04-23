package com.bluenet.web.api.converter.resetpassword;

import com.bluenet.web.api.dto.auth.*;
import com.bluenet.web.application.command.resetpassword.ResetPasswordCommands;
import org.springframework.stereotype.Component;

/**
 * 密码重置请求转换器
 * <p>
 * 负责将 API 层的 RequestDTO 转换为应用层的 Command
 * </p>
 */
@Component
public class ResetPasswordRequestConverter {

    public ResetPasswordCommands.VerifyStudentCommand toCommand(VerifyStudentRequestDTO dto) {
        return new ResetPasswordCommands.VerifyStudentCommand(dto.getStudentId());
    }

    public ResetPasswordCommands.VerifyEmailCommand toCommand(VerifyEmailRequestDTO dto) {
        return new ResetPasswordCommands.VerifyEmailCommand(dto.getResetToken(), dto.getEmail());
    }

    public ResetPasswordCommands.SendCodeCommand toCommand(SendResetCodeRequestDTO dto) {
        return new ResetPasswordCommands.SendCodeCommand(dto.getResetToken());
    }

    public ResetPasswordCommands.VerifyCodeCommand toCommand(VerifyResetCodeRequestDTO dto) {
        return new ResetPasswordCommands.VerifyCodeCommand(dto.getResetToken(), dto.getCode());
    }

    public ResetPasswordCommands.ResetPasswordCommand toCommand(ResetPasswordRequestDTO dto) {
        return new ResetPasswordCommands.ResetPasswordCommand(dto.getResetToken(), dto.getNewPassword());
    }
}
