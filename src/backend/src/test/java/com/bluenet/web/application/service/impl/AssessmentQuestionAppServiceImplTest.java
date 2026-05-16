package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.AssessmentQuestionResult;
import com.bluenet.web.application.command.assessment_question.AssessmentQuestionCommands;
import com.bluenet.web.domain.exception.DataConflict;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.AssessmentQuestion;
import com.bluenet.web.domain.model.enumerate.QuestionType;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.vo.evaluation.AlgorithmContent;
import com.bluenet.web.domain.repository.AssessmentAnswerRepository;
import com.bluenet.web.domain.repository.AssessmentQuestionRepository;
import com.bluenet.web.domain.repository.AssessmentTimeRepository;
import com.bluenet.web.infrastructure.repository.dataobject.JudgeProblemConfigDO;
import com.bluenet.web.infrastructure.repository.mapper.JudgeProblemConfigMapper;
import com.bluenet.web.infrastructure.storage.JudgeAssetStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * AssessmentQuestionAppServiceImpl 单元测试。
 */
@DisplayName("AssessmentQuestionAppServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class AssessmentQuestionAppServiceImplTest {

    private static final Long QUESTION_ID = 10L;
    private static final Long ASSESSMENT_TIME_ID = 20L;

    @Mock
    private AssessmentQuestionRepository assessmentQuestionRepository;

    @Mock
    private AssessmentTimeRepository assessmentTimeRepository;

    @Mock
    private AssessmentAnswerRepository assessmentAnswerRepository;
    @Mock
    private JudgeProblemConfigMapper judgeProblemConfigMapper;
    @Mock
    private JudgeAssetStorage judgeAssetStorage;

    private AssessmentQuestionAppServiceImpl assessmentQuestionAppService;

    @BeforeEach
    void setUp() {
        assessmentQuestionAppService = new AssessmentQuestionAppServiceImpl(
                assessmentQuestionRepository,
                assessmentTimeRepository,
                null,
                assessmentAnswerRepository,
                null,
                judgeProblemConfigMapper,
                judgeAssetStorage);
    }

    private AssessmentTime createTime() {
        return AssessmentTime.reconstruct(
                ASSESSMENT_TIME_ID,
                Direction.COMPUTER_VISION,
                1,
                2024,
                null,
                null,
                true,
                120,
                null,
                false);
    }

    @Test
    @DisplayName("创建算法题缺少语言模板：应拒绝")
    void createQuestion_algorithmWithoutStarterCode_shouldThrow() {
        when(assessmentTimeRepository.findById(ASSESSMENT_TIME_ID))
                .thenReturn(Optional.of(createTime()));

        AlgorithmContent content = createValidAlgorithmContent();
        content.setStarterCode(null);

        AssessmentQuestionCommands.CreateAssessmentQuestionCommand command = new AssessmentQuestionCommands.CreateAssessmentQuestionCommand(
                ASSESSMENT_TIME_ID, 1, QuestionType.ALGORITHM, "两数之和", content, null, BigDecimal.TEN);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> assessmentQuestionAppService.createQuestion(command));

        assertEquals("算法题至少需要配置一个语言模板", ex.getMessage());
        verify(assessmentQuestionRepository, never()).save(any());
    }

    @Test
    @DisplayName("创建算法题正式用例缺少期望输出：应拒绝")
    void createQuestion_algorithmFormalCaseMissingExpectedOutput_shouldThrow() {
        when(assessmentTimeRepository.findById(ASSESSMENT_TIME_ID))
                .thenReturn(Optional.of(createTime()));

        AlgorithmContent content = createValidAlgorithmContent();
        content.getTestCases().get(0).setExpectedOutput(" ");

        AssessmentQuestionCommands.CreateAssessmentQuestionCommand command = new AssessmentQuestionCommands.CreateAssessmentQuestionCommand(
                ASSESSMENT_TIME_ID, 1, QuestionType.ALGORITHM, "两数之和", content, null, BigDecimal.TEN);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> assessmentQuestionAppService.createQuestion(command));

        assertEquals("正式测试用例第1个用例必须包含输入和期望输出", ex.getMessage());
        verify(assessmentQuestionRepository, never()).save(any());
    }

    @Test
    @DisplayName("创建算法题默认运行用例缺少输入：应拒绝")
    void createQuestion_algorithmRunCaseMissingInput_shouldThrow() {
        when(assessmentTimeRepository.findById(ASSESSMENT_TIME_ID))
                .thenReturn(Optional.of(createTime()));

        AlgorithmContent content = createValidAlgorithmContent();
        content.getRunTestCases().get(0).setInput("");

        AssessmentQuestionCommands.CreateAssessmentQuestionCommand command = new AssessmentQuestionCommands.CreateAssessmentQuestionCommand(
                ASSESSMENT_TIME_ID, 1, QuestionType.ALGORITHM, "两数之和", content, null, BigDecimal.TEN);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> assessmentQuestionAppService.createQuestion(command));

        assertEquals("默认运行测试用例第1个用例必须包含输入和期望输出", ex.getMessage());
        verify(assessmentQuestionRepository, never()).save(any());
    }

    @Test
    @DisplayName("创建合法算法题：应调用仓储保存")
    void createQuestion_validAlgorithm_shouldCreateQuestion() {
        when(assessmentTimeRepository.findById(ASSESSMENT_TIME_ID))
                .thenReturn(Optional.of(createTime()));
        when(assessmentQuestionRepository.findByTimeIdAndQuestionNo(ASSESSMENT_TIME_ID, 1))
                .thenReturn(Optional.empty());

        AssessmentQuestionCommands.CreateAssessmentQuestionCommand command = new AssessmentQuestionCommands.CreateAssessmentQuestionCommand(
                ASSESSMENT_TIME_ID, 1, QuestionType.ALGORITHM, "两数之和",
                createValidAlgorithmContent(), null, BigDecimal.TEN);

        AssessmentQuestionResult result = assessmentQuestionAppService.createQuestion(command);

        assertNotNull(result);
        assertEquals(QuestionType.ALGORITHM, result.questionType());
        verify(assessmentQuestionRepository).save(any(AssessmentQuestion.class));
    }

    @Test
    @DisplayName("创建考题题号重复：应抛出冲突异常")
    void createQuestion_duplicateQuestionNo_shouldThrowDataConflict() {
        when(assessmentTimeRepository.findById(ASSESSMENT_TIME_ID))
                .thenReturn(Optional.of(createTime()));
        when(assessmentQuestionRepository.findByTimeIdAndQuestionNo(ASSESSMENT_TIME_ID, 1))
                .thenReturn(Optional.of(createQuestionEntity(QuestionType.SINGLE_CHOICE, null)));

        AssessmentQuestionCommands.CreateAssessmentQuestionCommand command = new AssessmentQuestionCommands.CreateAssessmentQuestionCommand(
                ASSESSMENT_TIME_ID, 1, QuestionType.SINGLE_CHOICE, "单选题", null, null, BigDecimal.TEN);

        DataConflict ex = assertThrows(
                DataConflict.class,
                () -> assessmentQuestionAppService.createQuestion(command));

        assertEquals("该考核时间下题号 1 已存在", ex.getMessage());
        verify(assessmentQuestionRepository, never()).save(any());
    }

    @Test
    @DisplayName("更新为算法题缺少正式用例：应允许")
    void updateQuestion_algorithmWithoutFormalCases_shouldUpdate() {
        AssessmentQuestion existing = createQuestionEntity(QuestionType.SINGLE_CHOICE, null);
        when(assessmentQuestionRepository.findById(QUESTION_ID))
                .thenReturn(Optional.of(existing));

        AlgorithmContent content = createValidAlgorithmContent();
        content.setTestCases(List.of());

        AssessmentQuestionCommands.UpdateAssessmentQuestionCommand command = new AssessmentQuestionCommands.UpdateAssessmentQuestionCommand(
                QUESTION_ID, null, QuestionType.ALGORITHM, null, content, null, null);

        AssessmentQuestionResult result = assessmentQuestionAppService.updateQuestion(command);

        assertEquals(QuestionType.ALGORITHM, result.questionType());
        verify(assessmentQuestionRepository).update(any());
    }

    @Test
    @DisplayName("更新考题题号重复：应抛出冲突异常")
    void updateQuestion_duplicateQuestionNo_shouldThrowDataConflict() {
        AssessmentQuestion existing = createQuestionEntity(QuestionType.SINGLE_CHOICE, null);
        existing.setAssessmentTimeId(ASSESSMENT_TIME_ID);
        when(assessmentQuestionRepository.findById(QUESTION_ID))
                .thenReturn(Optional.of(existing));

        AssessmentQuestion other = createQuestionEntity(QuestionType.SINGLE_CHOICE, null);
        other.setId(99L);
        when(assessmentQuestionRepository.findByTimeIdAndQuestionNo(ASSESSMENT_TIME_ID, 2))
                .thenReturn(Optional.of(other));

        AssessmentQuestionCommands.UpdateAssessmentQuestionCommand command = new AssessmentQuestionCommands.UpdateAssessmentQuestionCommand(
                QUESTION_ID, 2, null, null, null, null, null);

        DataConflict ex = assertThrows(
                DataConflict.class,
                () -> assessmentQuestionAppService.updateQuestion(command));

        assertEquals("该考核时间下题号 2 已存在", ex.getMessage());
        verify(assessmentQuestionRepository, never()).update(any());
    }

    @Test
    @DisplayName("更新不存在的考题：应抛出未找到异常")
    void updateQuestion_nonExisting_shouldThrowDataNotFound() {
        when(assessmentQuestionRepository.findById(QUESTION_ID))
                .thenReturn(Optional.empty());

        AssessmentQuestionCommands.UpdateAssessmentQuestionCommand command = new AssessmentQuestionCommands.UpdateAssessmentQuestionCommand(
                QUESTION_ID, null, null, null, null, null, null);

        DataNotFound ex = assertThrows(
                DataNotFound.class,
                () -> assessmentQuestionAppService.updateQuestion(command));

        assertEquals("考题不存在，ID: " + QUESTION_ID, ex.getMessage());
    }

    @Test
    @DisplayName("删除不存在的考题：应抛出未找到异常")
    void deleteQuestion_nonExisting_shouldThrowDataNotFound() {
        when(assessmentQuestionRepository.findById(QUESTION_ID))
                .thenReturn(Optional.empty());

        DataNotFound ex = assertThrows(
                DataNotFound.class,
                () -> assessmentQuestionAppService.deleteQuestion(QUESTION_ID));

        assertEquals("考题不存在，ID: " + QUESTION_ID, ex.getMessage());
    }

    @Test
    @DisplayName("管理端分页查询：应返回分页结果")
    void listQuestionsForAdmin_shouldReturnPagedResult() {
        Page<AssessmentQuestion> page = new PageImpl<>(
                List.of(createQuestionEntity(QuestionType.ALGORITHM, createValidAlgorithmContent())));
        when(assessmentQuestionRepository.findAllByTimeId(eq(ASSESSMENT_TIME_ID), any(PageRequest.class)))
                .thenReturn(page);

        Page<AssessmentQuestionResult> result = assessmentQuestionAppService
                .listQuestionsForAdmin(ASSESSMENT_TIME_ID, 0, 10);

        assertEquals(1, result.getContent().size());
        assertEquals(QuestionType.ALGORITHM, result.getContent().get(0).questionType());
    }

    @Test
    @DisplayName("用户端查询算法题详情：不应暴露正式测试用例")
    void getQuestionDetailForUser_algorithm_shouldHideFormalTestCases() {
        AssessmentQuestion entity = createQuestionEntity(QuestionType.ALGORITHM, createValidAlgorithmContent());
        entity.setAssessmentTimeId(ASSESSMENT_TIME_ID);
        when(assessmentQuestionRepository.findById(QUESTION_ID))
                .thenReturn(Optional.of(entity));

        AssessmentQuestionResult result = assessmentQuestionAppService.getQuestionDetailForUser(QUESTION_ID);

        assertTrue(result.content() instanceof AlgorithmContent);
        AlgorithmContent content = (AlgorithmContent) result.content();
        assertNull(content.getTestCases());
        assertNotNull(content.getRunTestCases());
        assertNotNull(content.getStarterCode());
    }

    @Test
    @DisplayName("管理端查询算法题：应保留正式测试用例")
    void listQuestionsForAdmin_algorithm_shouldKeepFormalTestCases() {
        Page<AssessmentQuestion> page = new PageImpl<>(
                List.of(createQuestionEntity(QuestionType.ALGORITHM, createValidAlgorithmContent())));
        when(assessmentQuestionRepository.findAllByTimeId(eq(ASSESSMENT_TIME_ID), any(PageRequest.class)))
                .thenReturn(page);

        AssessmentQuestionResult result = assessmentQuestionAppService
                .listQuestionsForAdmin(ASSESSMENT_TIME_ID, 0, 10)
                .getContent()
                .get(0);

        assertTrue(result.content() instanceof AlgorithmContent);
        AlgorithmContent content = (AlgorithmContent) result.content();
        assertNotNull(content.getTestCases());
        assertEquals(1, content.getTestCases().size());
    }

    private AlgorithmContent createValidAlgorithmContent() {
        AlgorithmContent content = new AlgorithmContent();
        content.setContent("计算 A+B");
        content.setStarterCode(Map.of("PYTHON", "print(input())"));
        content.setRunTestCases(List.of(createTestCase("1 2", "3")));
        content.setTestCases(List.of(createTestCase("10 20", "30")));
        content.setTimeLimit(1000);
        content.setMemoryLimit(256);
        return content;
    }

    private AlgorithmContent.TestCase createTestCase(String input, String expectedOutput) {
        AlgorithmContent.TestCase testCase = new AlgorithmContent.TestCase();
        testCase.setInput(input);
        testCase.setExpectedOutput(expectedOutput);
        testCase.setHidden(true);
        testCase.setWeight(1);
        return testCase;
    }

    private AssessmentQuestion createQuestionEntity(QuestionType questionType, Object content) {
        return AssessmentQuestion.reconstruct(
                QUESTION_ID,
                ASSESSMENT_TIME_ID,
                1,
                questionType,
                "题目",
                (com.bluenet.web.domain.model.vo.evaluation.QuestionContent) content,
                null,
                BigDecimal.TEN);
    }

    @Test
    @DisplayName("删除算法题：应级联删除判题配置和 OSS 资产")
    void deleteQuestion_algorithm_shouldCascadeDeleteJudgeConfig() {
        AssessmentQuestion existing = createQuestionEntity(QuestionType.ALGORITHM, createValidAlgorithmContent());
        when(assessmentQuestionRepository.findById(QUESTION_ID))
                .thenReturn(Optional.of(existing));

        JudgeProblemConfigDO config = new JudgeProblemConfigDO();
        config.setId(100L);
        config.setQuestionId(QUESTION_ID);
        when(judgeProblemConfigMapper.selectByQuestionId(QUESTION_ID)).thenReturn(config);

        assessmentQuestionAppService.deleteQuestion(QUESTION_ID);

        verify(judgeAssetStorage).deleteByPrefix("questions/" + QUESTION_ID + "/current/generator/");
        verify(judgeAssetStorage).deleteByPrefix("questions/" + QUESTION_ID + "/current/manifest/");
        verify(judgeAssetStorage).deleteByPrefix("questions/" + QUESTION_ID + "/current/standard/");
        verify(judgeAssetStorage).deleteByPrefix("questions/" + QUESTION_ID + "/current/testcases/");
        verify(judgeProblemConfigMapper).deleteByQuestionId(QUESTION_ID);
        verify(assessmentQuestionRepository).deleteById(QUESTION_ID);
    }

    @Test
    @DisplayName("删除非算法题：不应尝试删除判题配置")
    void deleteQuestion_nonAlgorithm_shouldNotDeleteJudgeConfig() {
        AssessmentQuestion existing = createQuestionEntity(QuestionType.SINGLE_CHOICE, null);
        when(assessmentQuestionRepository.findById(QUESTION_ID))
                .thenReturn(Optional.of(existing));

        assessmentQuestionAppService.deleteQuestion(QUESTION_ID);

        verify(judgeProblemConfigMapper, never()).selectByQuestionId(any());
        verify(judgeAssetStorage, never()).deleteByPrefix(any());
        verify(assessmentQuestionRepository).deleteById(QUESTION_ID);
    }
}
