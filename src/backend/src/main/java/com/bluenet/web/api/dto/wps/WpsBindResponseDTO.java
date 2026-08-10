package com.bluenet.web.api.dto.wps;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WPS 回调响应消息。
 * <p>
 * WPS 平台要求特定的响应格式（如 bind 事件需精确返回 {"bind_code":"xxx"}）， 不能使用通用的
 * {@link com.bluenet.web.api.dto.ResponseMessage} 包装。
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WpsBindResponseDTO {

    /**
     * 绑定验证码（仅在 bind 事件返回，WPS 要求精确的 bind_code 字段名）
     */
    @JsonProperty("bind_code")
    private String bindCode;
}
