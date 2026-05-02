package com.bluenet.judge.application.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 标准解 benchmark 建议限时计算器。
 */
@Component
public class BenchmarkLimitSuggester {
    /**
     * 根据标准解 p95 耗时和配置公式计算建议限时。
     *
     * @param p95TimeMs
     *            标准解 p95 耗时，单位毫秒。
     * @param marginMultiplier
     *            限时倍率。
     * @param minExtraMs
     *            最小额外毫秒数。
     * @param roundToMs
     *            向上取整粒度。
     * @return 建议限时，单位毫秒。
     */
    public int suggestTimeLimitMs(int p95TimeMs, BigDecimal marginMultiplier, int minExtraMs, int roundToMs) {
        if (p95TimeMs < 0 || minExtraMs < 0 || roundToMs <= 0) {
            throw new IllegalArgumentException("性能基准测试限时参数必须非负，且取整粒度必须大于 0");
        }
        BigDecimal multiplied = BigDecimal.valueOf(p95TimeMs).multiply(marginMultiplier);
        BigDecimal withExtra = BigDecimal.valueOf((long) p95TimeMs + minExtraMs);
        int raw = multiplied.max(withExtra).setScale(0, RoundingMode.CEILING).intValueExact();
        return roundUp(raw, roundToMs);
    }

    /**
     * 将原始限时向上取整到指定粒度。
     *
     * @param value
     *            原始限时。
     * @param roundToMs
     *            取整粒度。
     * @return 取整后的限时。
     */
    private int roundUp(int value, int roundToMs) {
        // 取整为管理员更容易阅读和确认的 50ms 或 100ms 等稳定粒度。
        return ((value + roundToMs - 1) / roundToMs) * roundToMs;
    }
}
