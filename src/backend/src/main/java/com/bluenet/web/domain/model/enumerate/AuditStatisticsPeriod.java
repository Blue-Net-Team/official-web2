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

    public static AuditStatisticsPeriod fromString(String period) {
        if (period == null) {
            return null;
        }
        String upper = period.toUpperCase();
        // 先尝试直接按枚举名匹配（兼容 D7, H24, D30）
        for (AuditStatisticsPeriod p : values()) {
            if (p.name().equals(upper)) {
                return p;
            }
        }
        // 再按 API 参数格式匹配（兼容 7d, 24h, 30d）
        return switch (upper) {
            case "24H" -> H24;
            case "7D" -> D7;
            case "30D" -> D30;
            default -> throw new IllegalArgumentException("不支持的时间范围：" + period);
        };
    }
}
