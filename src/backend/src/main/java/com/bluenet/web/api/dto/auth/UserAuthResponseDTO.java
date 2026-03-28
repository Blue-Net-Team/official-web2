package com.bluenet.web.api.dto.auth;

import com.bluenet.web.api.dto.user.UserInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** 登录成功响应：CSRF Token 与用户信息。JWT 通过 HttpOnly Cookie 自动传递。 */
@Schema(description = "登录成功响应，包含 CSRF Token 与用户信息。JWT 通过 HttpOnly Cookie 自动设置。")
@Data
public class UserAuthResponseDTO {

    @Schema(description = "CSRF Token，状态修改请求（POST/PUT/DELETE/PATCH）需在 X-CSRF-Token Header 中携带")
    private String csrfToken;

    @Schema(description = "当前用户信息")
    private UserInfo userInfo;
}
