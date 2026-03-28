package com.bluenet.web.api.dto.auth;

import com.bluenet.web.api.dto.ResponseMessage;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 仅用于 OpenAPI 文档：登录接口 200 响应体为 ResponseMessage&lt;UserAuthResponseDTO&gt;。
 * 实际接口返回
 * {@link ResponseMessage}{@code <}{@link UserAuthResponseDTO}{@code >}，code 为
 * 200。
 */
@Schema(description = "登录成功：code=200，data 为 JWT 与用户信息")
public class ResponseMessageUserAuthResponseDTO extends ResponseMessage<UserAuthResponseDTO> {
}
