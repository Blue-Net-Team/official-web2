package com.bluenet.web.api.controller.v1;

import com.bluenet.web.api.dto.auth.AuthMeResponseDTO;
import com.bluenet.web.domain.exception.Unauthorized;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.auth.ResponseMessageUserAuthResponseDTO;
import com.bluenet.web.api.dto.auth.StudentIdLoginRequestDTO;
import com.bluenet.web.api.dto.auth.UserAuthResponseDTO;
import com.bluenet.web.application.service.AuthService;
import com.bluenet.web.infrastructure.security.annotation.AccessLevel;
import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 认证接口：登录、登出、获取登录状态
 * <p>
 * 登录成功后，JWT 通过 HttpOnly Cookie 自动设置，CSRF Token 通过响应体返回。 登出时清除 Cookie。
 * </p>
 */
@Tag(name = "认证", description = "登录、登出等认证相关接口")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

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
            UserAuthResponseDTO responseDTO = authService.login(requestDTO, response);
            return ResponseEntity.ok(ResponseMessage.success(responseDTO));
        } catch (Unauthorized unauthorized) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResponseMessage.error(unauthorized));
        }
    }

    @Operation(summary = "用户登出", description = "使当前 JWT 失效并清除 Cookie。需要已登录状态（通过 Cookie 认证）。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "登出成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "401", description = "未登录", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class), examples = @ExampleObject(value = "{\"code\":401,\"msg\":\"未登录\",\"data\":null}"))) })
    @SecurityRequirement(name = "cookie-auth")
    @RequiresPermission(value = "auth:logout", name = "用户登出", access = AccessLevel.AUTHENTICATED)
    @PostMapping("/logout")
    public ResponseMessage<Void> logout(HttpServletResponse response) {
        authService.logout(response);
        return ResponseMessage.success();
    }

    @Operation(summary = "获取当前登录状态", description = "检查当前用户是否已登录。页面刷新后调用此接口恢复登录状态和获取 CSRF Token。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功获取登录状态", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))) })
    @RequiresPermission(value = "auth:me", name = "获取登录状态", access = AccessLevel.PUBLIC)
    @GetMapping("/me")
    public ResponseMessage<AuthMeResponseDTO> getAuthMe(HttpServletResponse response) {
        AuthMeResponseDTO responseDTO = authService.getAuthMe(response);
        return ResponseMessage.success(responseDTO);
    }
}
