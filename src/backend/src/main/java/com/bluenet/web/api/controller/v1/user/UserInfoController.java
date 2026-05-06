package com.bluenet.web.api.controller.v1.user;

import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.user.ResponseMessageUserInfo;
import com.bluenet.web.api.dto.user.UserInfo;
import com.bluenet.web.api.converter.userinfo.UserInfoResponseConverter;
import com.bluenet.web.application.service.UserInfoAppService;
import com.bluenet.web.domain.exception.Unauthorized;
import com.bluenet.web.infrastructure.security.annotation.AccessLevel;
import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 用户信息接口。需在请求头携带 Bearer JWT。 */
@Tag(name = "用户信息", description = "当前登录用户信息")
@RestController
@RequestMapping("/api/v1/user/info")
@RequiredArgsConstructor
class UserInfoController {
    private final UserInfoAppService userInfoAppService;
    private final UserInfoResponseConverter userInfoResponseConverter;

    @Operation(summary = "获取当前用户信息", description = "返回当前登录用户的基本信息。请求头必须携带 Authorization: Bearer <token>。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功，body 为 ResponseMessage，code=200，data 为当前用户信息", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageUserInfo.class))),
            @ApiResponse(responseCode = "401", description = "未登录或 token 无效", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class), examples = {
                    @ExampleObject(value = "{\"code\":401,\"msg\":\"未登录或 token 无效\",\"data\":null}") })) })
    @SecurityRequirement(name = "bearer-jwt")
    @RequiresPermission(name = "获取自身信息", value = "user:info:me", access = AccessLevel.AUTHENTICATED)
    @GetMapping("/me")
    public ResponseMessage<UserInfo> getMyInfo() {
        // 调用应用层
        try {
            return ResponseMessage.success(userInfoResponseConverter.toDTO(userInfoAppService.getMyInfo()));
        } catch (Unauthorized unauthorized) {
            return ResponseMessage.error(unauthorized);
        }
    }
}
