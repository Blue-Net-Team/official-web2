package com.bluenet.web.domain.model.enumerate;

import lombok.Getter;

@Getter
public enum AchievementType implements ValueEnum {
    PAPER("paper", "论文"),
    PATENT("patent", "专利"),
    COMPETITION("competition", "竞赛");

    private final String value;
    private final String description;

    AchievementType(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
