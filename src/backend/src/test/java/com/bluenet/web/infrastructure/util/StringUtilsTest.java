package com.bluenet.web.infrastructure.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("StringUtils 单元测试")
class StringUtilsTest {

    @Test
    @DisplayName("null 输入应返回 null")
    void truncateWithEllipsis_null_shouldReturnNull() {
        assertNull(StringUtils.truncateWithEllipsis(null, 10));
    }

    @Test
    @DisplayName("空串应返回空串")
    void truncateWithEllipsis_empty_shouldReturnEmpty() {
        assertEquals("", StringUtils.truncateWithEllipsis("", 10));
    }

    @Test
    @DisplayName("短串应原样返回")
    void truncateWithEllipsis_shorter_shouldReturnOriginal() {
        assertEquals("short", StringUtils.truncateWithEllipsis("short", 10));
    }

    @Test
    @DisplayName("正好长度应原样返回")
    void truncateWithEllipsis_exactLength_shouldReturnOriginal() {
        String value = "exactly-10";
        assertEquals(10, value.length());
        assertEquals(value, StringUtils.truncateWithEllipsis(value, 10));
    }

    @Test
    @DisplayName("超长字符串应截断并追加省略号")
    void truncateWithEllipsis_overflow_shouldTruncateWithEllipsis() {
        String value = "this-is-a-very-long-text";
        assertEquals("this-is-a-very-lo...", StringUtils.truncateWithEllipsis(value, 20));
    }

    @ParameterizedTest
    @DisplayName("maxLength 小于等于 3 时应直接截取前 maxLength 个字符")
    @CsvSource({
            "hello,0,''",
            "hello,1,h",
            "hello,2,he",
            "hello,3,hel"
    })
    void truncateWithEllipsis_maxLengthTooSmall_shouldTruncateWithoutEllipsis(String value, int maxLength,
            String expected) {
        assertEquals(expected, StringUtils.truncateWithEllipsis(value, maxLength));
    }

    @Test
    @DisplayName("maxLength 为 4 时应截断为 1 个字符加省略号")
    void truncateWithEllipsis_maxLengthFour_shouldTruncateToOneCharPlusEllipsis() {
        assertEquals("h...", StringUtils.truncateWithEllipsis("hello", 4));
    }

    @Test
    @DisplayName("maxLength 为负数时应抛出异常")
    void truncateWithEllipsis_negativeMaxLength_shouldThrow() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> StringUtils.truncateWithEllipsis("value", -1));
        assertEquals("maxLength 必须大于等于 0", exception.getMessage());
    }
}
