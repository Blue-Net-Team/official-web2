package com.bluenet.web.domain.model.enumerate;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum Gender {
    MALE("male", "男"),
    FEMALE("female", "女"),
    UNKNOWN("unknown", "未知");

    @EnumValue
    private final String value;
    private final String description;

    Gender(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
