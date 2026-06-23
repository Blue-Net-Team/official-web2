package com.bluenet.web.api.dto.wps;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * WPS 表单无 event 探针请求。
 * <p>
 * WPS 绑定验证时可能直接 POST {@code {"bind_code":"xxx"}}，没有 {@code event} 字段。 作为
 * {@link WpsCallbackRequestDTO} 的默认反序列化类型兜底处理。
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WpsProbeCallbackRequestDTO extends WpsCallbackRequestDTO {

    /**
     * 绑定验证码
     */
    private String bindCode;

    @Override
    public String getEvent() {
        return null;
    }
}
