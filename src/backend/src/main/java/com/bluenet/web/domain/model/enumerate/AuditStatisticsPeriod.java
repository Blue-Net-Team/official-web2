package com.bluenet.web.domain.model.enumerate;

import lombok.Getter;

@Getter
public enum AuditStatisticsPeriod implements ValueEnum {
    H24("H24", "最近24小时"),
    D7("D7", "最近7天"),
    D30("D30", "最近30天");

    private final String value;
    private final String description;

    AuditStatisticsPeriod(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
