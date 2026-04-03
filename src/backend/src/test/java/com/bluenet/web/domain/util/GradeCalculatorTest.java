package com.bluenet.web.domain.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GradeCalculator 单元测试
 * <p>
 * 测试年级计算逻辑
 * </p>
 */
@DisplayName("GradeCalculator 单元测试")
class GradeCalculatorTest {

    // ==================== calculateGrade(studentId) 测试 ====================

    @Nested
    @DisplayName("calculateGrade(studentId) 方法测试")
    class CalculateGradeByStudentIdTests {

        @Test
        @DisplayName("正常学号：应返回正确年级")
        void calculateGrade_validStudentId_shouldReturnGrade() {
            // 2023级学生，2026年4月（9月前），参考年=2025，年级=2025-2023+1=3
            Integer grade = GradeCalculator.calculateGrade("2023123456");
            assertNotNull(grade);
            assertTrue(grade >= 1);
        }

        @Test
        @DisplayName("学号为null：应返回null")
        void calculateGrade_nullStudentId_shouldReturnNull() {
            assertNull(GradeCalculator.calculateGrade(null));
        }

        @Test
        @DisplayName("学号长度不足4位：应返回null")
        void calculateGrade_shortStudentId_shouldReturnNull() {
            assertNull(GradeCalculator.calculateGrade("123"));
            assertNull(GradeCalculator.calculateGrade(""));
        }

        @Test
        @DisplayName("学号前4位非数字：应返回null")
        void calculateGrade_nonNumericStudentId_shouldReturnNull() {
            assertNull(GradeCalculator.calculateGrade("abcd5678"));
        }
    }

    // ==================== calculateGrade(enrollmentYear, date) 测试
    // ====================

    @Nested
    @DisplayName("calculateGrade(enrollmentYear, date) 方法测试")
    class CalculateGradeByYearAndDateTests {

        @Test
        @DisplayName("2023年入学，2026年4月（9月前）：应为大三(3)")
        void calculateGrade_2023enrollment_april2026_shouldBe3() {
            // 2026年4月 < 9月，参考年=2025，年级=2025-2023+1=3
            assertEquals(3, GradeCalculator.calculateGrade(2023, LocalDate.of(2026, 4, 1)));
        }

        @Test
        @DisplayName("2023年入学，2026年9月（9月）：应为大四(4)")
        void calculateGrade_2023enrollment_sep2026_shouldBe4() {
            // 2026年9月 >= 9月，参考年=2026，年级=2026-2023+1=4
            assertEquals(4, GradeCalculator.calculateGrade(2023, LocalDate.of(2026, 9, 1)));
        }

        @Test
        @DisplayName("2023年入学，2025年8月（9月前）：应为大二(2)")
        void calculateGrade_2023enrollment_aug2025_shouldBe2() {
            // 2025年8月 < 9月，参考年=2024，年级=2024-2023+1=2
            assertEquals(2, GradeCalculator.calculateGrade(2023, LocalDate.of(2025, 8, 31)));
        }

        @Test
        @DisplayName("2023年入学，2023年9月：应为大一(1)")
        void calculateGrade_2023enrollment_sep2023_shouldBe1() {
            // 2023年9月 >= 9月，参考年=2023，年级=2023-2023+1=1
            assertEquals(1, GradeCalculator.calculateGrade(2023, LocalDate.of(2023, 9, 1)));
        }

        @Test
        @DisplayName("2023年入学，2023年8月（刚入学前）：应为大一(1)")
        void calculateGrade_2023enrollment_aug2023_shouldBe1() {
            // 2023年8月 < 9月，参考年=2022，年级=2022-2023+1=0，max(1,0)=1
            assertEquals(1, GradeCalculator.calculateGrade(2023, LocalDate.of(2023, 8, 31)));
        }

        @Test
        @DisplayName("2025年入学，2026年4月：应为大一(1)")
        void calculateGrade_2025enrollment_apr2026_shouldBe1() {
            // 2026年4月 < 9月，参考年=2025，年级=2025-2025+1=1
            assertEquals(1, GradeCalculator.calculateGrade(2025, LocalDate.of(2026, 4, 1)));
        }

        @Test
        @DisplayName("2024年入学，2026年4月：应为大二(2)")
        void calculateGrade_2024enrollment_apr2026_shouldBe2() {
            // 2026年4月 < 9月，参考年=2025，年级=2025-2024+1=2
            assertEquals(2, GradeCalculator.calculateGrade(2024, LocalDate.of(2026, 4, 1)));
        }

        @ParameterizedTest(name = "入学年{0}在{1}年{2}月应为{3}年级")
        @CsvSource({
                "2023, 2025, 8, 2",
                "2023, 2025, 9, 3",
                "2024, 2026, 4, 2",
                "2025, 2026, 4, 1"
        })
        void calculateGrade_parameterized(int enrollmentYear, int year, int month, int expectedGrade) {
            assertEquals(expectedGrade, GradeCalculator.calculateGrade(enrollmentYear, LocalDate.of(year, month, 1)));
        }
    }

    // ==================== extractEnrollmentYear 测试 ====================

    @Nested
    @DisplayName("extractEnrollmentYear 方法测试")
    class ExtractEnrollmentYearTests {

        @Test
        @DisplayName("正常学号：应返回入学年份")
        void extractEnrollmentYear_validStudentId_shouldReturnYear() {
            assertEquals(2023, GradeCalculator.extractEnrollmentYear("2023123456"));
        }

        @Test
        @DisplayName("学号为null：应返回null")
        void extractEnrollmentYear_null_shouldReturnNull() {
            assertNull(GradeCalculator.extractEnrollmentYear(null));
        }

        @Test
        @DisplayName("学号长度不足4位：应返回null")
        void extractEnrollmentYear_shortId_shouldReturnNull() {
            assertNull(GradeCalculator.extractEnrollmentYear("123"));
            assertNull(GradeCalculator.extractEnrollmentYear(""));
        }

        @Test
        @DisplayName("学号刚好4位：应返回入学年份")
        void extractEnrollmentYear_exact4Digits_shouldReturnYear() {
            assertEquals(2024, GradeCalculator.extractEnrollmentYear("2024"));
        }

        @Test
        @DisplayName("学号前4位非数字：应返回null")
        void extractEnrollmentYear_nonNumeric_shouldReturnNull() {
            assertNull(GradeCalculator.extractEnrollmentYear("abcd1234"));
        }
    }
}
