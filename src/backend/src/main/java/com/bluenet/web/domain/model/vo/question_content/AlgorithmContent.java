package com.bluenet.web.domain.model.vo.question_content;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.HashMap;
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

    @Override
    public AlgorithmContent deepCopy() {
        AlgorithmContent copy = new AlgorithmContent();
        copy.setContent(getContent());
        copy.setInputDescription(inputDescription);
        copy.setOutputDescription(outputDescription);
        copy.setConstraints(constraints);
        copy.setExamples(copyExamples(examples));
        copy.setRunTestCases(copyTestCases(runTestCases));
        copy.setTestCases(copyTestCases(testCases));
        copy.setStarterCode(starterCode == null ? null : new HashMap<>(starterCode));
        copy.setTimeLimit(timeLimit);
        copy.setMemoryLimit(memoryLimit);
        return copy;
    }

    private List<Example> copyExamples(List<Example> source) {
        if (source == null) {
            return null;
        }
        List<Example> copies = new ArrayList<>(source.size());
        for (Example example : source) {
            if (example == null) {
                copies.add(null);
                continue;
            }
            Example copy = new Example();
            copy.setInput(example.getInput());
            copy.setExpectedOutput(example.getExpectedOutput());
            copy.setExplanation(example.getExplanation());
            copies.add(copy);
        }
        return copies;
    }

    private List<TestCase> copyTestCases(List<TestCase> source) {
        if (source == null) {
            return null;
        }
        List<TestCase> copies = new ArrayList<>(source.size());
        for (TestCase testCase : source) {
            if (testCase == null) {
                copies.add(null);
                continue;
            }
            TestCase copy = new TestCase();
            copy.setInput(testCase.getInput());
            copy.setExpectedOutput(testCase.getExpectedOutput());
            copy.setHidden(testCase.getHidden());
            copy.setWeight(testCase.getWeight());
            copies.add(copy);
        }
        return copies;
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
