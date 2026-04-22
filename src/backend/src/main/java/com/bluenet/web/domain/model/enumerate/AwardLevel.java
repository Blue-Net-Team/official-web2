package com.bluenet.web.domain.model.enumerate;

import lombok.Getter;

@Getter
public enum AwardLevel implements ValueEnum {
    NATIONAL("national", "国家级"),
    PROVINCIAL("provincial", "省级"),
    SCHOOL("school", "校级");

    private final String value;
    private final String description;

    AwardLevel(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
