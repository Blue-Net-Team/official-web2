package com.bluenet.web.api.dto.user;

import com.bluenet.web.api.dto.ResponseMessage;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 仅用于 OpenAPI 文档：获取当前用户信息接口 200 响应体为 ResponseMessage&lt;UserInfo&gt;。
 */
@Schema(description = "成功：code=200，data 为当前用户信息")
public class ResponseMessageUserInfo extends ResponseMessage<UserInfo> {
}
