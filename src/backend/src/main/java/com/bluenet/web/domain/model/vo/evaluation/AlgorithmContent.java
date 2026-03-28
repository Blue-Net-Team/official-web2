package com.bluenet.web.domain.model.vo.evaluation;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class AlgorithmContent extends QuestionContent {
    private List<TestCase> testCases;
    private Integer timeLimit;
    private Integer memoryLimit;

    @Data
    public static class TestCase {
        private String input;
        private String expectedOutput;
    }
}
