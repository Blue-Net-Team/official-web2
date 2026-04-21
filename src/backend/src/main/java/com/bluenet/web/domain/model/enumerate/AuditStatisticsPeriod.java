package com.bluenet.web.domain.model.enumerate;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum AuditStatisticsPeriod {
    H24("H24", "最近24小时"),
    D7("D7", "最近7天"),
    D30("D30", "最近30天");

    @EnumValue
    private final String value;
    private final String description;

    AuditStatisticsPeriod(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
