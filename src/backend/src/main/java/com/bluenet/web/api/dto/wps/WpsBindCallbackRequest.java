package com.bluenet.web.api.dto.wps;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * WPS 表单 bind 事件回调请求体。
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WpsBindCallbackRequest extends WpsCallbackRequest {

    private static final String EVENT = "bind";

    @Override
    public String getEvent() {
        return EVENT;
    }
}
