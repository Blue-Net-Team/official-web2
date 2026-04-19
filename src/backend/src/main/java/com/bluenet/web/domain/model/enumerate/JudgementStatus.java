package com.bluenet.web.domain.model.enumerate;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * Tracks the lifecycle of a question-level judgement.
 */
@Getter
public enum JudgementStatus {
    PENDING_MANUAL("PENDING_MANUAL", "等待人工评判"),
    JUDGED("JUDGED", "已评判"),
    CANCELLED("CANCELLED", "已取消");

    @EnumValue
    private final String value;
    private final String description;

    JudgementStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
