package com.bluenet.web.domain.model.enumerate;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum QrcodeType {
    USER("user", "用户微信二维码"), GROUP("group", "群聊二维码");

    @EnumValue
    private final String value;
    private final String description;

    QrcodeType(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
