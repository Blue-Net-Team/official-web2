package com.bluenet.web.domain.model.enumerate;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum ExperienceType {
    COMPETITION("competition", "竞赛"),
    PROJECT("project", "项目"),
    INTERNSHIP("internship", "实习");

    @EnumValue
    private final String value;
    private final String description;

    ExperienceType(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
