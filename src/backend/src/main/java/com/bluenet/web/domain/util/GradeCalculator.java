package com.bluenet.web.domain.util;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.Map;

/**
 * 年级计算工具类
 * <p>
 * 根据学号前4位（入学年份）和当前日期计算年级。 以每年9月1日为分界线：9月及之后视为新学年开始。
 * </p>
 */
@Slf4j
public class GradeCalculator {

    private static final Map<Integer, String> GRADE_LABELS = Map.of(
            1,
            "大一",
            2,
            "大二",
            3,
            "大三",
            4,
            "大四");

    private static final String GRADE_GRADUATED = "已毕业";

    private GradeCalculator() {
    }

    /**
     * 根据学号计算年级序号（内部使用，供 getGradeLabel 调用）
     *
     * @param studentId
     *            学号
     * @return 年级序号（1=大一, 2=大二, 3=大三, 4=大四, >4=已毕业），无法计算时返回null
     */
    private static Integer calculateGrade(String studentId) {
        Integer enrollmentYear = extractEnrollmentYear(studentId);
        if (enrollmentYear == null) {
            return null;
        }
        return calculateGrade(enrollmentYear, LocalDate.now());
    }

    /**
     * 根据入学年份和当前日期计算年级
     *
     * @param enrollmentYear
     *            入学年份
     * @param currentDate
     *            当前日期
     * @return 年级序号（1=大一, 2=大二, 3=大三, 4=大四, >4=已毕业）
     */
    static int calculateGrade(int enrollmentYear, LocalDate currentDate) {
        int currentYear = currentDate.getYear();
        int currentMonth = currentDate.getMonthValue();

        int referenceYear = currentMonth >= 9 ? currentYear : currentYear - 1;
        int grade = referenceYear - enrollmentYear + 1;
        return Math.max(1, grade);
    }

    /**
     * 根据学号计算年级标签
     *
     * @param studentId
     *            学号
     * @return 年级标签（如"大一"、"大二"、"已毕业"），无法计算时返回null
     */
    public static String getGradeLabel(String studentId) {
        Integer gradeNum = calculateGrade(studentId);
        if (gradeNum == null) {
            return null;
        }
        return gradeNum > 4 ? GRADE_GRADUATED : GRADE_LABELS.getOrDefault(gradeNum, gradeNum + "年级");
    }

    /**
     * 从学号中提取入学年份
     *
     * @param studentId
     *            学号
     * @return 入学年份，无法提取时返回null
     */
    public static Integer extractEnrollmentYear(String studentId) {
        if (studentId == null || studentId.length() < 4) {
            return null;
        }
        try {
            return Integer.parseInt(studentId.substring(0, 4));
        } catch (NumberFormatException e) {
            log.warn("Failed to extract enrollment year from studentId: {}", studentId);
            return null;
        }
    }
}
