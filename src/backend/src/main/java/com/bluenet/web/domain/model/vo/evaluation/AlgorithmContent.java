package com.bluenet.web.domain.model.vo.evaluation;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class AlgorithmContent extends QuestionContent {
    /**
     * 算法题输入格式说明。
     */
    private String inputDescription;
    /**
     * 算法题输出格式说明。
     */
    private String outputDescription;
    /**
     * 算法题输入、输出或复杂度限制说明。
     */
    private String constraints;
    /**
     * 算法题示例输入输出集合。
     */
    private List<Example> examples;
    /**
     * 候选人自测使用的运行用例集合。
     */
    private List<TestCase> runTestCases;
    /**
     * 算法题正式评测用例集合。
     */
    private List<TestCase> testCases;
    /**
     * 算法题提供给候选人的初始代码模板。
     */
    private Map<String, String> starterCode;
    /**
     * 算法题时间限制，通常以毫秒为单位。
     */
    private Integer timeLimit;
    /**
     * 算法题内存限制，通常以 KB 或 MB 表示。
     */
    private Integer memoryLimit;

    @Override
    public void sanitizeForUser() {
        this.testCases = null;
    }

    @Data
    public static class Example {
        /**
         * 算法示例或评测用例输入内容。
         */
        private String input;
        /**
         * 算法评测用例期望输出内容。
         */
        private String expectedOutput;
        /**
         * 示例、答案或评测结果的解释说明。
         */
        private String explanation;
    }

    @Data
    public static class TestCase {
        /**
         * 算法示例或评测用例输入内容。
         */
        private String input;
        /**
         * 算法评测用例期望输出内容。
         */
        private String expectedOutput;
        /**
         * 评测用例是否对候选人隐藏。
         */
        private Boolean hidden;
        /**
         * 评测用例在总分中的权重。
         */
        private Integer weight;
    }
}
