package com.bluenet.web.api.controller.v1;

import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.converter.auth.AuthRequestConverter;
import com.bluenet.web.api.dto.auth.AuthMeResponseDTO;
import com.bluenet.web.api.dto.auth.EmailLoginRequestDTO;
import com.bluenet.web.api.dto.auth.ResponseMessageUserAuthResponseDTO;
import com.bluenet.web.api.dto.auth.SendVerificationCodeRequestDTO;
import com.bluenet.web.api.dto.auth.StudentIdLoginRequestDTO;
import com.bluenet.web.application.AuthResult;
import com.bluenet.web.api.converter.auth.AuthResponseConverter;
import com.bluenet.web.application.service.AuthAppService;
import com.bluenet.web.domain.exception.Unauthorized;
import com.bluenet.web.infrastructure.security.annotation.AccessLevel;
import com.bluenet.web.infrastructure.security.annotation.RateLimit;
import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 认证接口：登录、登出、获取登录状态、GitHub OAuth
 */
@Tag(name = "认证", description = "登录、登出等认证相关接口")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthAppService authAppService;
    private final AuthRequestConverter requestConverter;
    private final AuthResponseConverter authResponseConverter;

    @Value("${github.oauth.callback-base-url:http://localhost:8080}")
    private String callbackBaseUrl;

    @Operation(summary = "学号登录", description = "使用学号与密码登录。JWT 通过 HttpOnly Cookie 自动设置，响应体返回 CSRF Token 与用户信息。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "登录成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageUserAuthResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "学号或密码错误", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class), examples = @ExampleObject(value = "{\"code\":401,\"msg\":\"学号或密码错误\",\"data\":null}"))) })
    @RequiresPermission(value = "auth:login:student-id", name = "学号登录", access = AccessLevel.PUBLIC)
    @PostMapping("/login/student-id")
    public ResponseEntity<ResponseMessage<?>> studentIdLogin(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "学号与密码", required = true, content = @Content(schema = @Schema(implementation = StudentIdLoginRequestDTO.class))) @Valid @RequestBody StudentIdLoginRequestDTO requestDTO,
            HttpServletResponse response) {
        try {
            AuthResult.Login result = authAppService.login(requestConverter.toCommand(requestDTO), response);
            return ResponseEntity.ok(ResponseMessage.success(authResponseConverter.toDTO(result)));
        } catch (Unauthorized unauthorized) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResponseMessage.error(unauthorized));
        }
    }

    @Operation(summary = "邮箱验证码登录", description = "使用邮箱与验证码登录。JWT 通过 HttpOnly Cookie 自动设置，响应体返回 CSRF Token 与用户信息。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "登录成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageUserAuthResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "邮箱或验证码错误", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class), examples = @ExampleObject(value = "{\"code\":401,\"msg\":\"邮箱或验证码错误\",\"data\":null}"))) })
    @RequiresPermission(value = "auth:login:email", name = "邮箱登录", access = AccessLevel.PUBLIC)
    @PostMapping("/login/email")
    public ResponseEntity<ResponseMessage<?>> emailLogin(
            @Valid @RequestBody EmailLoginRequestDTO requestDTO,
            HttpServletResponse response) {
        try {
            AuthResult.Login result = authAppService.loginWithEmail(
                    requestConverter.toCommand(requestDTO),
                    response);
            return ResponseEntity.ok(ResponseMessage.success(authResponseConverter.toDTO(result)));
        } catch (Unauthorized unauthorized) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResponseMessage.error(unauthorized));
        }
    }

    @Operation(summary = "发送邮箱验证码", description = "向指定邮箱发送6位数字验证码，有效期5分钟。60秒内同一邮箱只能发送一次。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "发送成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "400", description = "发送过于频繁", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class), examples = @ExampleObject(value = "{\"code\":400,\"msg\":\"发送过于频繁，请稍后再试\",\"data\":null}"))) })
    @RequiresPermission(value = "auth:verification-code:send", name = "发送验证码", access = AccessLevel.PUBLIC)
    @RateLimit(interval = 60)
    @PostMapping("/verification-code/send")
    public ResponseMessage<Void> sendVerificationCode(
            @Valid @RequestBody SendVerificationCodeRequestDTO requestDTO) {
        authAppService.sendVerificationCode(requestConverter.toCommand(requestDTO));
        return ResponseMessage.success();
    }

    @Operation(summary = "用户登出", description = "使当前 JWT 失效并清除 Cookie。需要已登录状态（通过 Cookie 认证）。")
    @SecurityRequirement(name = "cookie-auth")
    @RequiresPermission(value = "auth:logout", name = "用户登出", access = AccessLevel.AUTHENTICATED)
    @PostMapping("/logout")
    public ResponseMessage<Void> logout(HttpServletResponse response) {
        authAppService.logout(response);
        return ResponseMessage.success();
    }

    @Operation(summary = "获取当前登录状态", description = "检查当前用户是否已登录。页面刷新后调用此接口恢复登录状态和获取 CSRF Token。")
    @RequiresPermission(value = "auth:me", name = "获取登录状态", access = AccessLevel.PUBLIC)
    @GetMapping("/me")
    public ResponseMessage<AuthMeResponseDTO> getAuthMe(HttpServletResponse response) {
        AuthResult.AuthMe result = authAppService.getAuthMe(response);
        return ResponseMessage.success(authResponseConverter.toDTO(result));
    }

    // ==================== GitHub OAuth ====================

    @Operation(summary = "发起 GitHub 登录", description = "获取 GitHub OAuth 授权页面 URL，前端应将用户重定向到该 URL")
    @RequiresPermission(value = "auth:github:login", name = "GitHub登录", access = AccessLevel.PUBLIC)
    @GetMapping("/github")
    public ResponseMessage<String> initiateGithubLogin() {
        String authorizeUrl = authAppService.initiateGithubLogin(callbackBaseUrl);
        return ResponseMessage.success(authorizeUrl);
    }

    @Operation(summary = "GitHub OAuth 回调", description = "GitHub 授权后的回调端点，处理登录和绑定流程")
    @RequiresPermission(value = "auth:github:callback", name = "GitHub回调", access = AccessLevel.PUBLIC)
    @GetMapping("/github/callback")
    public void handleGithubCallback(
            @Parameter(description = "GitHub 返回的授权码") @RequestParam String code,
            @Parameter(description = "防 CSRF 的 state 参数") @RequestParam String state,
            HttpServletResponse response) {
        authAppService.handleGithubCallback(code, state, callbackBaseUrl, response);
    }

    @Operation(summary = "查询 GitHub 绑定状态", description = "获取当前用户的 GitHub 绑定状态")
    @SecurityRequirement(name = "cookie-auth")
    @RequiresPermission(value = "auth:github:status", name = "GitHub绑定状态", access = AccessLevel.AUTHENTICATED)
    @GetMapping("/github/status")
    public ResponseMessage<String> getGithubBindingStatus() {
        String githubUsername = authAppService.getGithubBindingStatus();
        return ResponseMessage.success(githubUsername);
    }

    @Operation(summary = "发起 GitHub 绑定", description = "获取 GitHub OAuth 授权页面 URL 用于绑定账号")
    @SecurityRequirement(name = "cookie-auth")
    @RequiresPermission(value = "auth:github:bind", name = "GitHub绑定", access = AccessLevel.AUTHENTICATED)
    @GetMapping("/github/bind")
    public ResponseMessage<String> initiateGithubBind() {
        String authorizeUrl = authAppService.initiateGithubBind(callbackBaseUrl);
        return ResponseMessage.success(authorizeUrl);
    }

    @Operation(summary = "解绑 GitHub 账号", description = "解除当前用户的 GitHub 账号绑定")
    @SecurityRequirement(name = "cookie-auth")
    @RequiresPermission(value = "auth:github:unbind", name = "GitHub解绑", access = AccessLevel.AUTHENTICATED)
    @DeleteMapping("/github/bind")
    public ResponseMessage<Void> unbindGithub() {
        authAppService.unbindGithub();
        return ResponseMessage.success();
    }
}
