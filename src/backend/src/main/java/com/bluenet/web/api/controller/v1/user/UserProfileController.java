package com.bluenet.web.api.controller.v1.user;

import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.user.ChangeEmailRequestDTO;
import com.bluenet.web.api.dto.user.ChangePasswordRequestDTO;
import com.bluenet.web.api.dto.user.SendEmailVerificationCodeRequestDTO;
import com.bluenet.web.api.dto.user.TabCountsDTO;
import com.bluenet.web.api.dto.user.UpdateAvatarRequestDTO;
import com.bluenet.web.api.dto.user.UpdateProfileRequestDTO;
import com.bluenet.web.api.dto.user.UserInfo;
import com.bluenet.web.api.dto.user.VerifyPasswordRequestDTO;
import com.bluenet.web.api.converter.userinfo.UserInfoRequestConverter;
import com.bluenet.web.application.converter.UserInfoAppConverter;
import com.bluenet.web.application.service.UserInfoAppService;
import com.bluenet.web.domain.exception.GlobalException;
import com.bluenet.web.infrastructure.security.annotation.AccessLevel;
import com.bluenet.web.infrastructure.security.annotation.RateLimit;
import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户信息接口
 */
@Tag(name = "用户信息", description = "当前登录用户信息管理")
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
class UserProfileController {
    private final UserInfoAppService userInfoAppService;
    private final UserInfoRequestConverter requestConverter;
    private final UserInfoAppConverter userInfoAppConverter;

    @Operation(summary = "获取当前用户信息", description = "返回当前登录用户的基本信息")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功获取用户信息", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserInfo.class))),
            @ApiResponse(responseCode = "401", description = "未登录或token无效", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @SecurityRequirement(name = "bearer-jwt")
    @RequiresPermission(name = "获取用户信息", value = "user:info:read", access = AccessLevel.AUTHENTICATED)
    @GetMapping("/info")
    public ResponseMessage<UserInfo> getMyInfo() {
        try {
            return ResponseMessage.success(userInfoAppConverter.toDTO(userInfoAppService.getMyInfo()));
        } catch (GlobalException e) {
            return ResponseMessage.error(e);
        }
    }

    @Operation(summary = "更新用户信息", description = "更新当前登录用户的基本信息，如昵称、个人简介")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功更新用户信息"),
            @ApiResponse(responseCode = "401", description = "未登录或token无效", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @SecurityRequirement(name = "bearer-jwt")
    @RequiresPermission(name = "更新用户信息", value = "user:info:update", access = AccessLevel.AUTHENTICATED)
    @PutMapping("/info")
    public ResponseMessage<Void> updateProfile(@RequestBody UpdateProfileRequestDTO request) {
        try {
            userInfoAppService.updateProfile(requestConverter.toCommand(request));
            return ResponseMessage.success();
        } catch (GlobalException e) {
            return ResponseMessage.error(e);
        }
    }

    @Operation(summary = "获取Tab计数", description = "返回当前用户的Tab计数（项目/竞赛/实习数量）")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功获取Tab计数"),
            @ApiResponse(responseCode = "401", description = "未登录或token无效", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @SecurityRequirement(name = "bearer-jwt")
    @RequiresPermission(name = "获取Tab计数", value = "user:tabcounts:read", access = AccessLevel.AUTHENTICATED)
    @GetMapping("/tab-counts")
    public ResponseMessage<TabCountsDTO> getTabCounts() {
        try {
            return ResponseMessage.success(userInfoAppConverter.toTabCountsDTO(userInfoAppService.getTabCounts()));
        } catch (GlobalException e) {
            return ResponseMessage.error(e);
        }
    }

    @Operation(summary = "发送修改邮箱验证码", description = "向指定邮箱发送修改邮箱场景的验证码，需要已登录。场景值：change-email-original（验证原邮箱）、change-email-new（验证新邮箱）")
    @SecurityRequirement(name = "cookie-auth")
    @RequiresPermission(name = "发送修改邮箱验证码", value = "user:email:send-code", access = AccessLevel.AUTHENTICATED)
    @RateLimit(interval = 60)
    @PostMapping("/email/verification-code/send")
    public ResponseMessage<Void> sendEmailVerificationCode(
            @Valid @RequestBody SendEmailVerificationCodeRequestDTO request) {
        try {
            userInfoAppService.sendEmailVerificationCode(requestConverter.toCommand(request));
            return ResponseMessage.success();
        } catch (GlobalException e) {
            return ResponseMessage.error(e);
        }
    }

    @Operation(summary = "修改邮箱", description = "通过验证原邮箱和新邮箱的验证码修改绑定邮箱")
    @SecurityRequirement(name = "cookie-auth")
    @RequiresPermission(name = "修改邮箱", value = "user:email:update", access = AccessLevel.AUTHENTICATED)
    @PutMapping("/email")
    public ResponseMessage<Void> changeEmail(@Valid @RequestBody ChangeEmailRequestDTO request) {
        try {
            userInfoAppService.changeEmail(requestConverter.toCommand(request));
            return ResponseMessage.success();
        } catch (GlobalException e) {
            return ResponseMessage.error(e);
        }
    }

    @Operation(summary = "验证当前密码", description = "已登录用户验证当前密码，验证通过返回令牌用于后续修改密码")
    @SecurityRequirement(name = "cookie-auth")
    @RequiresPermission(name = "验证当前密码", value = "user:password:verify", access = AccessLevel.AUTHENTICATED)
    @PostMapping("/password/verify")
    public ResponseMessage<String> verifyCurrentPassword(@Valid @RequestBody VerifyPasswordRequestDTO request) {
        try {
            Long userId = UserCTX.getCurrentUser().getId();
            String token = userInfoAppService.verifyCurrentPassword(requestConverter.toCommand(userId, request));
            return ResponseMessage.success(token);
        } catch (GlobalException e) {
            return ResponseMessage.error(e);
        }
    }

    @Operation(summary = "修改密码", description = "通过验证令牌提交新密码，修改成功后需重新登录")
    @SecurityRequirement(name = "cookie-auth")
    @RequiresPermission(name = "修改密码", value = "user:password:update", access = AccessLevel.AUTHENTICATED)
    @PutMapping("/password")
    public ResponseMessage<Void> changePassword(@Valid @RequestBody ChangePasswordRequestDTO request) {
        try {
            Long userId = UserCTX.getCurrentUser().getId();
            userInfoAppService.changePassword(requestConverter.toCommand(userId, request));
            return ResponseMessage.success();
        } catch (GlobalException e) {
            return ResponseMessage.error(e);
        }
    }

    @Operation(summary = "更新用户头像", description = "通过已上传的 fileId 更新当前用户头像，文件类型必须为 AVATAR")
    @SecurityRequirement(name = "cookie-auth")
    @RequiresPermission(name = "更新用户头像", value = "user:avatar:update", access = AccessLevel.AUTHENTICATED)
    @PutMapping("/avatar")
    public ResponseMessage<Void> updateAvatar(@Valid @RequestBody UpdateAvatarRequestDTO request) {
        try {
            userInfoAppService.updateAvatar(requestConverter.toCommand(request));
            return ResponseMessage.success();
        } catch (GlobalException e) {
            return ResponseMessage.error(e);
        }
    }
}
