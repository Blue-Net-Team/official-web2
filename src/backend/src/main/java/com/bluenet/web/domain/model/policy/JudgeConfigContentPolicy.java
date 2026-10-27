package com.bluenet.web.domain.model.policy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 判题配置内容指纹规则。
 *
 * <p>
 * 用于判断一次保存是否真正改动了「会影响已生成测试数据或 benchmark 结果」的内容： generator
 * 语言与源码、主标准解语言、各语言标准解源码、测试用例生成配置、 benchmark 参数。题目标题、分值、题面等不影响判题数据的字段不参与指纹。
 * </p>
 *
 * <p>
 * 指纹只做等值比较，不落库，因此两端（数据库现状与本次提交）必须用同一份规则生成。
 * </p>
 */
public final class JudgeConfigContentPolicy {

    private JudgeConfigContentPolicy() {
    }

    /**
     * 生成判题内容指纹。
     *
     * @param generatorLanguage
     *            generator 语言。
     * @param generatorHash
     *            generator 源码 SHA-256 哈希。
     * @param primaryStandardLanguage
     *            用于生成标准输出的主标准解语言。
     * @param benchmarkRepeatTimes
     *            benchmark 重复次数。
     * @param marginMultiplier
     *            建议限时倍率。
     * @param minExtraMs
     *            建议限时最小额外毫秒。
     * @param roundToMs
     *            建议限时向上取整粒度。
     * @param standardSolutions
     *            标准解内容列表，顺序无关。
     * @param testcases
     *            测试用例生成配置列表，顺序无关。
     * @return 可做等值比较的稳定文本指纹。
     */
    public static String fingerprint(
            String generatorLanguage,
            String generatorHash,
            String primaryStandardLanguage,
            Integer benchmarkRepeatTimes,
            BigDecimal marginMultiplier,
            Integer minExtraMs,
            Integer roundToMs,
            List<StandardSolutionContent> standardSolutions,
            List<TestcaseContent> testcases) {
        StringBuilder builder = new StringBuilder();
        append(builder, "generator-language", generatorLanguage);
        append(builder, "generator-hash", generatorHash);
        append(builder, "primary-standard-language", primaryStandardLanguage);
        append(
                builder,
                "benchmark",
                "%s|%s|%s|%s".formatted(
                        Objects.toString(benchmarkRepeatTimes, ""),
                        decimal(marginMultiplier),
                        Objects.toString(minExtraMs, ""),
                        Objects.toString(roundToMs, "")));

        List<String> solutionLines = new ArrayList<>();
        for (StandardSolutionContent solution : standardSolutions == null
                ? List.<StandardSolutionContent>of()
                : standardSolutions) {
            solutionLines.add(
                    "solution|%s|%s|%s".formatted(
                            Objects.toString(solution.language(), ""),
                            Objects.toString(solution.objectHash(), ""),
                            Boolean.TRUE.equals(solution.primarySolution())));
        }
        Collections.sort(solutionLines);
        solutionLines.forEach(line -> builder.append(line).append('\n'));

        List<String> testcaseLines = new ArrayList<>();
        for (TestcaseContent testcase : testcases == null ? List.<TestcaseContent>of() : testcases) {
            testcaseLines.add(
                    "testcase|%s|%s|%s|%s|%s|%s|%s".formatted(
                            Objects.toString(testcase.caseNo(), ""),
                            Objects.toString(testcase.category(), ""),
                            Objects.toString(testcase.generatorArgs(), ""),
                            decimal(testcase.weight()),
                            Boolean.TRUE.equals(testcase.hidden()),
                            Boolean.TRUE.equals(testcase.sample()),
                            Objects.toString(testcase.description(), "")));
        }
        Collections.sort(testcaseLines);
        testcaseLines.forEach(line -> builder.append(line).append('\n'));

        return builder.toString();
    }

    private static void append(StringBuilder builder, String key, String value) {
        builder.append(key).append('=').append(value == null ? "" : value).append('\n');
    }

    /**
     * 归一化小数，消除 1、1.0、1.00 之间的表示差异。
     *
     * @param value
     *            待归一化的小数。
     * @return 归一化后的文本；空值返回空串。
     */
    private static String decimal(BigDecimal value) {
        return value == null ? "" : value.stripTrailingZeros().toPlainString();
    }

    /**
     * 标准解内容，仅保留影响测试数据与 benchmark 的字段。
     *
     * @param language
     *            标准解语言。
     * @param objectHash
     *            标准解源码 SHA-256 哈希。
     * @param primarySolution
     *            是否为主标准解。
     */
    public record StandardSolutionContent(String language, String objectHash, Boolean primarySolution) {
    }

    /**
     * 测试用例生成配置，generatorArgs 需先归一化为稳定 JSON 文本。
     *
     * @param caseNo
     *            用例序号。
     * @param category
     *            用例分类。
     * @param generatorArgs
     *            归一化后的 generator 参数 JSON 文本。
     * @param weight
     *            用例权重。
     * @param hidden
     *            是否隐藏用例详情。
     * @param sample
     *            是否作为样例。
     * @param description
     *            用例说明。
     */
    public record TestcaseContent(
            Integer caseNo,
            String category,
            String generatorArgs,
            BigDecimal weight,
            Boolean hidden,
            Boolean sample,
            String description) {
    }
}
