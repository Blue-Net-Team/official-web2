package com.bluenet.web.api.dto.wps;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WPS 表单回调响应。
 * <p>
 * bind 事件需要返回 bind_code 以完成地址验证。
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WpsCallbackResponseDTO {

    /**
     * 绑定验证码，仅 bind 事件需要返回
     */
    private String bindCode;
}
