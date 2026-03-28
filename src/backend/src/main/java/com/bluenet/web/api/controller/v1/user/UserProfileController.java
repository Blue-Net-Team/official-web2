package com.bluenet.web.api.controller.v1.user;

import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.user.TabCountsDTO;
import com.bluenet.web.api.dto.user.UpdateProfileRequestDTO;
import com.bluenet.web.api.dto.user.UserInfo;
import com.bluenet.web.application.service.UserInfoService;
import com.bluenet.web.domain.exception.GlobalException;
import com.bluenet.web.infrastructure.security.annotation.AccessLevel;
import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
    private final UserInfoService userInfoService;

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
            return ResponseMessage.success(userInfoService.getMyInfo());
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
            userInfoService.updateProfile(request);
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
            return ResponseMessage.success(userInfoService.getTabCounts());
        } catch (GlobalException e) {
            return ResponseMessage.error(e);
        }
    }
}
