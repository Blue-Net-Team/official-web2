package com.bluenet.judge.application.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 标准解 benchmark 建议限时计算器测试。
 */
class BenchmarkLimitSuggesterTest {
    private final BenchmarkLimitSuggester suggester = new BenchmarkLimitSuggester();

    /**
     * 验证倍率结果较大时使用倍率并向上取整。
     *
     * @return 无返回值。
     */
    @Test
    void usesMultiplierAndRoundsUp() {
        int result = suggester.suggestTimeLimitMs(123, new BigDecimal("1.5"), 20, 50);

        assertThat(result).isEqualTo(200);
    }

    /**
     * 验证最小额外时间较大时优先使用最小额外时间。
     *
     * @return 无返回值。
     */
    @Test
    void usesMinimumExtraWhenItIsLarger() {
        int result = suggester.suggestTimeLimitMs(20, new BigDecimal("1.1"), 50, 25);

        assertThat(result).isEqualTo(75);
    }

    /**
     * 验证非法取整粒度会被拒绝。
     *
     * @return 无返回值。
     */
    @Test
    void rejectsInvalidRounding() {
        assertThatThrownBy(() -> suggester.suggestTimeLimitMs(10, BigDecimal.ONE, 0, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
