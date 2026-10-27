package com.bluenet.web.domain.model.vo;

import com.bluenet.web.domain.model.vo.question_content.AlgorithmContent;
import com.bluenet.web.domain.model.vo.question_content.MultipleChoiceContent;
import com.bluenet.web.domain.model.vo.question_content.SingleChoiceContent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

@DisplayName("QuestionContent 深复制测试")
class QuestionContentCopyTest {

    @Test
    @DisplayName("deepCopy: 单选题选项列表应独立")
    void deepCopy_singleChoice_shouldCopyOptions() {
        SingleChoiceContent source = new SingleChoiceContent();
        source.setContent("选择一项");
        source.setOptions(new ArrayList<>(List.of("A", "B")));
        source.setCorrectAnswer("A");

        SingleChoiceContent copy = (SingleChoiceContent) source.deepCopy();
        copy.getOptions().set(0, "C");

        assertNotSame(source, copy);
        assertNotSame(source.getOptions(), copy.getOptions());
        assertEquals(List.of("A", "B"), source.getOptions());
        assertEquals("A", copy.getCorrectAnswer());
        assertEquals("选择一项", copy.getContent());
    }

    @Test
    @DisplayName("deepCopy: 多选题选项和正确答案列表应独立")
    void deepCopy_multipleChoice_shouldCopyNestedLists() {
        MultipleChoiceContent source = new MultipleChoiceContent();
        source.setContent("选择多项");
        source.setOptions(new ArrayList<>(List.of("A", "B", "C")));
        source.setCorrectAnswers(new ArrayList<>(List.of("A", "C")));

        MultipleChoiceContent copy = (MultipleChoiceContent) source.deepCopy();
        copy.getOptions().remove(0);
        copy.getCorrectAnswers().clear();

        assertNotSame(source.getOptions(), copy.getOptions());
        assertNotSame(source.getCorrectAnswers(), copy.getCorrectAnswers());
        assertEquals(List.of("A", "B", "C"), source.getOptions());
        assertEquals(List.of("A", "C"), source.getCorrectAnswers());
    }

    @Test
    @DisplayName("deepCopy: 算法题全部嵌套内容应独立")
    void deepCopy_algorithm_shouldCopyAllNestedContent() {
        AlgorithmContent source = new AlgorithmContent();
        source.setContent("算法题面");
        source.setInputDescription("输入说明");
        source.setOutputDescription("输出说明");
        source.setConstraints("数据范围");
        source.setTimeLimit(1000);
        source.setMemoryLimit(262144);
        source.setExamples(new ArrayList<>(List.of(example("1 2", "3"))));
        source.setRunTestCases(new ArrayList<>(List.of(testCase("2 3", "5"))));
        source.setTestCases(new ArrayList<>(List.of(testCase("3 4", "7"))));
        source.setStarterCode(new HashMap<>());
        source.getStarterCode().put("python", "print(1)");

        AlgorithmContent copy = (AlgorithmContent) source.deepCopy();
        copy.getExamples().get(0).setInput("changed");
        copy.getRunTestCases().get(0).setExpectedOutput("changed");
        copy.getTestCases().get(0).setWeight(99);
        copy.getStarterCode().put("python", "changed");

        assertNotSame(source.getExamples(), copy.getExamples());
        assertNotSame(source.getExamples().get(0), copy.getExamples().get(0));
        assertNotSame(source.getRunTestCases(), copy.getRunTestCases());
        assertNotSame(source.getRunTestCases().get(0), copy.getRunTestCases().get(0));
        assertNotSame(source.getTestCases(), copy.getTestCases());
        assertNotSame(source.getTestCases().get(0), copy.getTestCases().get(0));
        assertNotSame(source.getStarterCode(), copy.getStarterCode());
        assertEquals("1 2", source.getExamples().get(0).getInput());
        assertEquals("5", source.getRunTestCases().get(0).getExpectedOutput());
        assertEquals(1, source.getTestCases().get(0).getWeight());
        assertEquals("print(1)", source.getStarterCode().get("python"));
        assertEquals("输入说明", copy.getInputDescription());
        assertEquals("输出说明", copy.getOutputDescription());
        assertEquals("数据范围", copy.getConstraints());
        assertEquals(1000, copy.getTimeLimit());
        assertEquals(262144, copy.getMemoryLimit());
    }

    private AlgorithmContent.Example example(String input, String output) {
        AlgorithmContent.Example example = new AlgorithmContent.Example();
        example.setInput(input);
        example.setExpectedOutput(output);
        example.setExplanation("说明");
        return example;
    }

    private AlgorithmContent.TestCase testCase(String input, String output) {
        AlgorithmContent.TestCase testCase = new AlgorithmContent.TestCase();
        testCase.setInput(input);
        testCase.setExpectedOutput(output);
        testCase.setHidden(true);
        testCase.setWeight(1);
        return testCase;
    }
}
