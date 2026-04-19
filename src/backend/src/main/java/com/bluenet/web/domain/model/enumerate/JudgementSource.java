package com.bluenet.web.domain.model.enumerate;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * Identifies whether a judgement was produced by the system or by a reviewer.
 */
@Getter
public enum JudgementSource {
    AUTO("AUTO", "自动评判"),
    MANUAL("MANUAL", "人工评判");

    @EnumValue
    private final String value;
    private final String description;

    JudgementSource(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
