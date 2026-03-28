package com.bluenet.web.domain.model.enumerate;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum AchievementType {
    PAPER("paper", "论文"),
    PATENT("patent", "专利"),
    COMPETITION("competition", "竞赛");

    @EnumValue
    private final String value;
    private final String description;

    AchievementType(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
