package com.bluenet.web.domain.model.enumerate;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum QrcodeType {
    USER("user", "用户微信二维码"),
    @Deprecated
    GROUP("group", "群聊二维码（已废弃，请使用 CONSULTATION）"),
    CONSULTATION("consultation", "咨询群二维码"),
    ASSESSMENT("assessment", "考核群二维码");

    @EnumValue
    private final String value;
    private final String description;

    QrcodeType(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
