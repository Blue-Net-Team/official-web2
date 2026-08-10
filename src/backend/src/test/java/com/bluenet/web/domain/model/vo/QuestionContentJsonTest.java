package com.bluenet.web.domain.model.vo;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bluenet.web.domain.model.vo.question_content.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.bluenet.web.BaseIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * QuestionContent JSON 序列化/反序列化集成测试
 */
class QuestionContentJsonTest extends BaseIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testSingleChoiceContentSerialization() throws Exception {
        SingleChoiceContent content = new SingleChoiceContent();
        content.setContent("Java中接口可以继承几个父接口？");
        content.setOptions(Arrays.asList("A. 0个", "B. 1个", "C. 多个"));
        content.setCorrectAnswer("C");

        String json = objectMapper.writeValueAsString(content);

        assertTrue(json.contains("\"type\":\"single_choice\""));
        assertTrue(json.contains("Java中接口可以继承"));
        assertTrue(json.contains("A. 0个"));
        assertTrue(json.contains("\"correctAnswer\":\"C\""));
    }

    @Test
    void testSingleChoiceContentDeserialization() throws Exception {
        String json = "{\"type\":\"single_choice\",\"content\":\"测试题目\",\"options\":[\"A\",\"B\",\"C\"],\"correctAnswer\":\"B\"}";

        QuestionContent content = objectMapper.readValue(json, QuestionContent.class);

        assertTrue(content instanceof SingleChoiceContent);
        SingleChoiceContent singleChoice = (SingleChoiceContent) content;
        assertEquals("测试题目", singleChoice.getContent());
        assertEquals(3, singleChoice.getOptions().size());
        assertEquals("B", singleChoice.getCorrectAnswer());
    }

    @Test
    void testMultipleChoiceContentSerialization() throws Exception {
        MultipleChoiceContent content = new MultipleChoiceContent();
        content.setContent("多选题示例");
        content.setOptions(Arrays.asList("A. 选项1", "B. 选项2", "C. 选项3"));
        content.setCorrectAnswers(Arrays.asList("A", "C"));

        String json = objectMapper.writeValueAsString(content);

        assertTrue(json.contains("\"type\":\"multiple_choice\""));
        assertTrue(json.contains("correctAnswers"));
    }

    @Test
    void testMultipleChoiceContentDeserialization() throws Exception {
        String json = "{\"type\":\"multiple_choice\",\"content\":\"多选\",\"options\":[\"A\",\"B\"],\"correctAnswers\":[\"A\",\"B\"]}";

        QuestionContent content = objectMapper.readValue(json, QuestionContent.class);

        assertTrue(content instanceof MultipleChoiceContent);
        MultipleChoiceContent multiChoice = (MultipleChoiceContent) content;
        assertEquals(2, multiChoice.getCorrectAnswers().size());
    }

    @Test
    void testAlgorithmContentSerialization() throws Exception {
        AlgorithmContent content = new AlgorithmContent();
        content.setContent("两数之和");

        AlgorithmContent.TestCase testCase = new AlgorithmContent.TestCase();
        testCase.setInput("[2,7,11,15], 9");
        testCase.setExpectedOutput("[0,1]");
        content.setTestCases(Arrays.asList(testCase));

        content.setTimeLimit(1000);
        content.setMemoryLimit(256);

        String json = objectMapper.writeValueAsString(content);

        assertTrue(json.contains("\"type\":\"algorithm\""));
        assertTrue(json.contains("\"timeLimit\":1000"));
        assertTrue(json.contains("\"memoryLimit\":256"));
        assertTrue(json.contains("testCases"));
    }

    @Test
    void testAlgorithmContentDeserialization() throws Exception {
        String json = "{\"type\":\"algorithm\",\"content\":\"算法题\",\"testCases\":[{\"input\":\"1 2\",\"expectedOutput\":\"3\"}],\"timeLimit\":500,\"memoryLimit\":128}";

        QuestionContent content = objectMapper.readValue(json, QuestionContent.class);

        assertTrue(content instanceof AlgorithmContent);
        AlgorithmContent algorithm = (AlgorithmContent) content;
        assertEquals(500, algorithm.getTimeLimit());
        assertEquals(128, algorithm.getMemoryLimit());
        assertEquals(1, algorithm.getTestCases().size());
        assertEquals("1 2", algorithm.getTestCases().get(0).getInput());
    }

    @Test
    void testFileUploadContentSerialization() throws Exception {
        FileUploadContent content = new FileUploadContent();
        content.setContent("请上传压缩包");

        String json = objectMapper.writeValueAsString(content);

        assertTrue(json.contains("\"type\":\"file_upload\""));
        assertTrue(json.contains("请上传压缩包"));
    }

    @Test
    void testFileUploadContentDeserialization() throws Exception {
        String json = "{\"type\":\"file_upload\",\"content\":\"上传文件\"}";

        QuestionContent content = objectMapper.readValue(json, QuestionContent.class);

        assertTrue(content instanceof FileUploadContent);
        assertEquals("上传文件", content.getContent());
    }

    @Test
    void testPolymorphicTypeRecognition() throws Exception {
        // 测试所有类型都能正确识别
        String[] types = { "single_choice", "multiple_choice", "algorithm", "file_upload" };
        Class<?>[] expectedClasses = { SingleChoiceContent.class, MultipleChoiceContent.class, AlgorithmContent.class,
                FileUploadContent.class };

        for (int i = 0; i < types.length; i++) {
            String json = "{\"type\":\"" + types[i] + "\",\"content\":\"test\"}";
            QuestionContent content = objectMapper.readValue(json, QuestionContent.class);
            assertTrue(
                    expectedClasses[i].isInstance(content),
                    "Type " + types[i] + " should create " + expectedClasses[i].getSimpleName());
        }
    }

    @Test
    void testRoundTripSerialization() throws Exception {
        // 测试序列化后再反序列化能还原对象
        SingleChoiceContent original = new SingleChoiceContent();
        original.setContent("测试");
        original.setOptions(Arrays.asList("A", "B"));
        original.setCorrectAnswer("A");

        String json = objectMapper.writeValueAsString(original);
        QuestionContent deserialized = objectMapper.readValue(json, QuestionContent.class);

        assertTrue(deserialized instanceof SingleChoiceContent);
        assertEquals(original.getContent(), deserialized.getContent());
    }
}
