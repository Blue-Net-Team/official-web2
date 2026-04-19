package com.bluenet.web.domain.model.vo.evaluation;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class AlgorithmContent extends QuestionContent {
    private String inputDescription;
    private String outputDescription;
    private String constraints;
    private List<Example> examples;
    private List<TestCase> runTestCases;
    private List<TestCase> testCases;
    private Map<String, String> starterCode;
    private Integer timeLimit;
    private Integer memoryLimit;

    @Data
    public static class Example {
        private String input;
        private String expectedOutput;
        private String explanation;
    }

    @Data
    public static class TestCase {
        private String input;
        private String expectedOutput;
        private Boolean hidden;
        private Integer weight;
    }
}
