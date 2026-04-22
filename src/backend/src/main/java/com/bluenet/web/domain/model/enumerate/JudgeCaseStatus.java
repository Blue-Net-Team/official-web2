package com.bluenet.web.domain.model.enumerate;

import lombok.Getter;

/**
 * Stores ACM-style per-case execution outcomes.
 */
@Getter
public enum JudgeCaseStatus implements ValueEnum {
    AC("AC", "通过"),
    WA("WA", "答案错误"),
    TLE("TLE", "超时"),
    RE("RE", "运行错误"),
    CE("CE", "编译错误"),
    MLE("MLE", "内存超限");

    private final String value;
    private final String description;

    JudgeCaseStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
