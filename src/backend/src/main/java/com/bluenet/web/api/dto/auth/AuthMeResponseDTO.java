package com.bluenet.web.api.dto.auth;

import com.bluenet.web.api.dto.user.UserInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 获取当前登录状态响应 用于页面刷新后恢复登录状态
 */
@Schema(description = "当前登录状态响应，包含用户信息和 CSRF Token")
@Data
public class AuthMeResponseDTO {

    @Schema(description = "是否已登录")
    private boolean authenticated;

    @Schema(description = "当前用户信息（未登录时为 null）")
    private UserInfo userInfo;

    @Schema(description = "CSRF Token，状态修改请求需在 X-CSRF-Token Header 中携带（未登录时为 null）")
    private String csrfToken;
}
