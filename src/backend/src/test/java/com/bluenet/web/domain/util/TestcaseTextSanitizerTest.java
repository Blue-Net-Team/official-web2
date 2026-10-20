package com.bluenet.web.domain.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TestcaseTextSanitizer 单元测试
 * <p>
 * 测试判题用例文本的不可见字符净化规则
 * </p>
 */
@DisplayName("TestcaseTextSanitizer 单元测试")
class TestcaseTextSanitizerTest {

    // ==================== 不可见格式字符删除 ====================

    @Nested
    @DisplayName("Cf / Zl / Zp 类字符删除")
    class InvisibleCharacterRemovalTests {

        @Test
        @DisplayName("数字后跟零宽空格：应删除零宽空格")
        void sanitize_zeroWidthSpaceAfterNumber_shouldRemoveIt() {
            assertEquals("12", TestcaseTextSanitizer.sanitize("12\u200b"));
        }

        @ParameterizedTest(name = "码点 U+{0}")
        @ValueSource(strings = { "\u200b", "\u200c", "\u200d", "\u2060", "\ufeff", "\u00ad" })
        @DisplayName("Cf 类不可见字符：应全部删除")
        void sanitize_formatCharacter_shouldRemoveIt(String invisible) {
            String sanitized = TestcaseTextSanitizer.sanitize("a" + invisible + "b");
            assertEquals("ab", sanitized);
        }

        @Test
        @DisplayName("输入以 BOM 开头：应删除 BOM")
        void sanitize_leadingBom_shouldRemoveIt() {
            assertEquals("5", TestcaseTextSanitizer.sanitize("\ufeff5"));
        }

        @ParameterizedTest(name = "码点 U+{0}")
        @ValueSource(strings = { "\u2028", "\u2029" })
        @DisplayName("Zl / Zp 类分隔符：应全部删除")
        void sanitize_lineAndParagraphSeparator_shouldRemoveThem(String separator) {
            assertEquals("ab", TestcaseTextSanitizer.sanitize("a" + separator + "b"));
        }
    }

    // ==================== 非 ASCII 空格归一为 ASCII 空格 ====================

    @Nested
    @DisplayName("非 ASCII 空格归一")
    class SpaceNormalizationTests {

        @Test
        @DisplayName("不换行空格分隔两个数字：应转为 ASCII 空格且保持两个 token")
        void sanitize_nonBreakingSpace_shouldBecomeAsciiSpace() {
            String sanitized = TestcaseTextSanitizer.sanitize("12\u00a034");
            assertEquals("12 34", sanitized);
            assertEquals(2, sanitized.split("\\s+").length);
        }

        @Test
        @DisplayName("全角空格分隔两个 token：应转为 ASCII 空格")
        void sanitize_ideographicSpace_shouldBecomeAsciiSpace() {
            assertEquals("a b", TestcaseTextSanitizer.sanitize("a\u3000b"));
        }

        @ParameterizedTest(name = "码点 U+{0}")
        @ValueSource(strings = { "\u2000", "\u2001", "\u2002", "\u2003", "\u2004", "\u2005", "\u2006", "\u2007",
                "\u2008", "\u2009", "\u200a", "\u1680", "\u202f", "\u205f" })
        @DisplayName("其他 Unicode 空格：应转为 ASCII 空格")
        void sanitize_unicodeSpace_shouldBecomeAsciiSpace(String space) {
            assertEquals("a b", TestcaseTextSanitizer.sanitize("a" + space + "b"));
        }
    }

    // ==================== 行结束符与保留字符 ====================

    @Nested
    @DisplayName("行结束符归一与字符保留")
    class LineEndingTests {

        @Test
        @DisplayName("CRLF 行结束符：应归一为 LF")
        void sanitize_crlf_shouldBecomeLf() {
            assertEquals("a\nb", TestcaseTextSanitizer.sanitize("a\r\nb"));
        }

        @Test
        @DisplayName("单独 CR 行结束符：应归一为 LF")
        void sanitize_loneCr_shouldBecomeLf() {
            assertEquals("a\nb", TestcaseTextSanitizer.sanitize("a\rb"));
        }

        @Test
        @DisplayName("制表符、换行符、ASCII 空格：应原样保留")
        void sanitize_tabNewlineAsciiSpace_shouldBePreserved() {
            assertEquals("a\tb c\nd", TestcaseTextSanitizer.sanitize("a\tb c\nd"));
        }

        @Test
        @DisplayName("多行用例：行结构与缩进应保持")
        void sanitize_multiLineTestcase_shouldKeepLineStructure() {
            String input = "3\r\n1 2 3\r\n 4\t5\r\n";
            assertEquals("3\n1 2 3\n 4\t5\n", TestcaseTextSanitizer.sanitize(input));
        }
    }

    // ==================== 边界输入与幂等 ====================

    @Nested
    @DisplayName("边界输入与幂等性")
    class EdgeCaseTests {

        @Test
        @DisplayName("输入为 null：应返回 null")
        void sanitize_null_shouldReturnNull() {
            assertNull(TestcaseTextSanitizer.sanitize(null));
        }

        @Test
        @DisplayName("输入为空串：应返回空串")
        void sanitize_emptyString_shouldReturnEmptyString() {
            assertEquals("", TestcaseTextSanitizer.sanitize(""));
        }

        @Test
        @DisplayName("输入全为零宽字符：应返回空串")
        void sanitize_onlyInvisibleCharacters_shouldReturnEmptyString() {
            assertEquals("", TestcaseTextSanitizer.sanitize("\u200b\ufeff\u200c"));
        }

        @Test
        @DisplayName("输入全为非 ASCII 空格：应归一为 ASCII 空格而非删除")
        void sanitize_onlyNonAsciiSpaces_shouldBecomeAsciiSpaces() {
            assertEquals(" ", TestcaseTextSanitizer.sanitize("\u3000"));
            assertEquals(2, TestcaseTextSanitizer.sanitize("\u3000\u00a0").length());
        }

        @Test
        @DisplayName("二次净化：结果应保持不变")
        void sanitize_twice_shouldBeIdempotent() {
            String input = "\ufeff12\u200b 34\r\n5\u00a06\u30007";
            String once = TestcaseTextSanitizer.sanitize(input);
            String twice = TestcaseTextSanitizer.sanitize(once);
            assertEquals(once, twice);
            assertEquals("12 34\n5 6 7", once);
        }
    }
}
