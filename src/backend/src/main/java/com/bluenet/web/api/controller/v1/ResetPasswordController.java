package com.bluenet.web.api.controller.v1;

import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.auth.*;
import com.bluenet.web.api.converter.resetpassword.ResetPasswordRequestConverter;
import com.bluenet.web.application.ResetPasswordResult;
import com.bluenet.web.application.service.ResetPasswordAppService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.infrastructure.security.annotation.AccessLevel;
import com.bluenet.web.infrastructure.security.annotation.RateLimit;
import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 密码重置接口：学号验证、邮箱验证、发送验证码、重置密码
 */
@Tag(name = "密码重置", description = "忘记密码 - 分步验证后重置密码")
@RestController
@RequestMapping("/api/v1/auth/reset-password")
@RequiredArgsConstructor
public class ResetPasswordController {

    private final ResetPasswordAppService resetPasswordAppService;
    private final ResetPasswordRequestConverter resetPasswordRequestConverter;

    @Operation(summary = "验证学号", description = "验证学号是否存在，返回 resetToken 用于后续步骤")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "学号验证通过"),
            @ApiResponse(responseCode = "400", description = "学号不存在", content = @Content(schema = @Schema(implementation = ResponseMessage.class))) })
    @RequiresPermission(value = "auth:reset-password:verify-student", name = "密码重置-验证学号", access = AccessLevel.PUBLIC)
    @PostMapping("/verify-student")
    public ResponseMessage<String> verifyStudent(
            @Valid @RequestBody VerifyStudentRequestDTO requestDTO) {
        ResetPasswordResult.VerifyStudent result = resetPasswordAppService
                .verifyStudent(resetPasswordRequestConverter.toCommand(requestDTO));
        return ResponseMessage.success(result.resetToken());
    }

    @Operation(summary = "验证邮箱", description = "验证邮箱是否与学号关联")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "邮箱验证通过"),
            @ApiResponse(responseCode = "400", description = "邮箱与学号不匹配或流程已过期", content = @Content(schema = @Schema(implementation = ResponseMessage.class))) })
    @RequiresPermission(value = "auth:reset-password:verify-email", name = "密码重置-验证邮箱", access = AccessLevel.PUBLIC)
    @PostMapping("/verify-email")
    public ResponseMessage<String> verifyEmail(
            @Valid @RequestBody VerifyEmailRequestDTO requestDTO) {
        ResetPasswordResult.VerifyEmail result = resetPasswordAppService
                .verifyEmail(resetPasswordRequestConverter.toCommand(requestDTO));
        return ResponseMessage.success(result.resetToken());
    }

    @Operation(summary = "发送验证码", description = "向已验证的邮箱发送6位验证码，有效期5分钟")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "验证码已发送"),
            @ApiResponse(responseCode = "400", description = "流程已过期或跳步访问", content = @Content(schema = @Schema(implementation = ResponseMessage.class))) })
    @RequiresPermission(value = "auth:reset-password:send-code", name = "密码重置-发送验证码", access = AccessLevel.PUBLIC)
    @RateLimit(interval = 60)
    @PostMapping("/send-code")
    public ResponseMessage<Void> sendCode(
            @Valid @RequestBody SendResetCodeRequestDTO requestDTO) {
        resetPasswordAppService.sendCode(resetPasswordRequestConverter.toCommand(requestDTO));
        return ResponseMessage.success();
    }

    @Operation(summary = "验证验证码", description = "验证用户输入的验证码是否正确")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "验证码正确"),
            @ApiResponse(responseCode = "400", description = "验证码错误或已过期", content = @Content(schema = @Schema(implementation = ResponseMessage.class))) })
    @RequiresPermission(value = "auth:reset-password:verify-code", name = "密码重置-验证验证码", access = AccessLevel.PUBLIC)
    @PostMapping("/verify-code")
    public ResponseMessage<Void> verifyCode(
            @Valid @RequestBody VerifyResetCodeRequestDTO requestDTO) {
        resetPasswordAppService.verifyCode(resetPasswordRequestConverter.toCommand(requestDTO));
        return ResponseMessage.success();
    }

    @Operation(summary = "重置密码", description = "验证码通过后设置新密码")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "密码重置成功"),
            @ApiResponse(responseCode = "400", description = "流程已过期或密码不一致", content = @Content(schema = @Schema(implementation = ResponseMessage.class))) })
    @RequiresPermission(value = "auth:reset-password:reset", name = "密码重置-重置密码", access = AccessLevel.PUBLIC)
    @PostMapping("/reset")
    public ResponseMessage<Void> resetPassword(
            @Valid @RequestBody ResetPasswordRequestDTO requestDTO) {
        if (!requestDTO.getNewPassword().equals(requestDTO.getConfirmPassword())) {
            throw new BadRequest("新密码与确认密码不一致");
        }
        resetPasswordAppService.resetPassword(resetPasswordRequestConverter.toCommand(requestDTO));
        return ResponseMessage.success();
    }
}
