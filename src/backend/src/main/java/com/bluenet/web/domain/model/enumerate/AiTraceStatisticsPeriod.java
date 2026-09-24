package com.bluenet.web.domain.model.enumerate;

import io.github.ivencn.infra.web.enumx.ValueEnum;

import lombok.Getter;

/**
 * AI 对话轨迹统计周期。
 *
 * <p>
 * 趋势类指标按会话聚合，时间桶粒度随周期自适应：24 小时按小时，7 天与 30 天按天。
 * </p>
 */
@Getter
public enum AiTraceStatisticsPeriod implements ValueEnum {
    H24("H24", "最近24小时"),
    D7("D7", "最近7天"),
    D30("D30", "最近30天");

    private final String value;
    private final String description;

    AiTraceStatisticsPeriod(String value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 从接口参数解析统计周期。
     *
     * @param period
     *            接口传入的周期字符串，兼容 24h/7d/30d 与 H24/D7/D30。
     * @return 对应的统计周期枚举。
     */
    public static AiTraceStatisticsPeriod fromString(String period) {
        if (period == null) {
            return null;
        }
        String upper = period.toUpperCase();
        for (AiTraceStatisticsPeriod p : values()) {
            if (p.name().equals(upper)) {
                return p;
            }
        }
        return switch (upper) {
            case "24H" -> H24;
            case "7D" -> D7;
            case "30D" -> D30;
            default -> throw new IllegalArgumentException("不支持的时间范围：" + period);
        };
    }
}
