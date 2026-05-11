package com.bluenet.web.domain.model.enumerate;

import lombok.Getter;

/**
 * Identifies whether a judgement was produced by the system or by a reviewer.
 */
@Getter
public enum JudgementSource implements ValueEnum {
    AUTO("AUTO", "自动评判"),
    MANUAL("MANUAL", "人工评判"),
    ADMIN_FINALIZED("ADMIN_FINALIZED", "管理员最终评定");

    private final String value;
    private final String description;

    JudgementSource(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
