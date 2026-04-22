package com.bluenet.web.domain.model.enumerate;

import lombok.Getter;

@Getter
public enum ExperienceType implements ValueEnum {
    COMPETITION("competition", "竞赛"),
    PROJECT("project", "项目"),
    INTERNSHIP("internship", "实习");

    private final String value;
    private final String description;

    ExperienceType(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
