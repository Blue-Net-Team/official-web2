package com.bluenet.web.domain.model.enumerate;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum ImageType {
    COMPETITION("competition", "竞赛介绍");

    @EnumValue
    private final String value;
    private final String description;

    ImageType(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
