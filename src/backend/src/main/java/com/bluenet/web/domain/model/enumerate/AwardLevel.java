package com.bluenet.web.domain.model.enumerate;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum AwardLevel {
    NATIONAL("national", "国家级"),
    PROVINCIAL("provincial", "省级"),
    SCHOOL("school", "校级");

    @EnumValue
    private final String value;
    private final String description;

    AwardLevel(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
