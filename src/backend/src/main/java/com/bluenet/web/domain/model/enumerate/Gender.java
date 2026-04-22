package com.bluenet.web.domain.model.enumerate;

import lombok.Getter;

@Getter
public enum Gender implements ValueEnum {
    MALE("male", "男"),
    FEMALE("female", "女"),
    UNKNOWN("unknown", "未知");

    private final String value;
    private final String description;

    Gender(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
