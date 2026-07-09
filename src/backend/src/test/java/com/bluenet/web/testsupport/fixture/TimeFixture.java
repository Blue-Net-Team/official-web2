package com.bluenet.web.testsupport.fixture;

import java.time.LocalDateTime;

/**
 * 测试用时间构造工具，用于构造相对当前时间的考核窗口，避免脆弱断言。
 */
public final class TimeFixture {

    private TimeFixture() {
    }

    /**
     * 当前时间。
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * 当前时间减去指定分钟数。
     */
    public static LocalDateTime minusMinutes(long minutes) {
        return LocalDateTime.now().minusMinutes(minutes);
    }

    /**
     * 当前时间加上指定分钟数。
     */
    public static LocalDateTime plusMinutes(long minutes) {
        return LocalDateTime.now().plusMinutes(minutes);
    }

    /**
     * 构造一个已经结束的考核窗口结束时间（当前时间前 5 分钟）。
     */
    public static LocalDateTime ended() {
        return minusMinutes(5);
    }

    /**
     * 构造一个尚未开始的考核窗口开始时间（当前时间后 10 分钟）。
     */
    public static LocalDateTime notStarted() {
        return plusMinutes(10);
    }

    /**
     * 构造一个处于进行中的考核时间窗口：开始于 5 分钟前，结束于 60 分钟后。
     */
    public static LocalDateTime[] withinNow() {
        return new LocalDateTime[] { minusMinutes(5), plusMinutes(60) };
    }
}
