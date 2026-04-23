package com.bluenet.web.api.converter.auth;

import com.bluenet.web.api.dto.auth.EmailLoginRequestDTO;
import com.bluenet.web.api.dto.auth.SendVerificationCodeRequestDTO;
import com.bluenet.web.api.dto.auth.StudentIdLoginRequestDTO;
import com.bluenet.web.application.command.auth.AuthCommands;
import org.springframework.stereotype.Component;

/**
 * 认证请求转换器
 * <p>
 * 负责将 API 层的 RequestDTO 转换为应用层的 Command
 * </p>
 */
@Component
public class AuthRequestConverter {

    /**
     * 将学号登录请求 DTO 转换为命令
     */
    public AuthCommands.StudentIdLoginCommand toCommand(StudentIdLoginRequestDTO dto) {
        return new AuthCommands.StudentIdLoginCommand(dto.getStudentId(), dto.getPassword());
    }

    /**
     * 将邮箱登录请求 DTO 转换为命令
     */
    public AuthCommands.EmailLoginCommand toCommand(EmailLoginRequestDTO dto) {
        return new AuthCommands.EmailLoginCommand(dto.getEmail(), dto.getVerifyCode());
    }

    /**
     * 将发送验证码请求 DTO 转换为命令
     */
    public AuthCommands.SendVerificationCodeCommand toCommand(SendVerificationCodeRequestDTO dto) {
        return new AuthCommands.SendVerificationCodeCommand(dto.getEmail(), dto.getScene());
    }
}
