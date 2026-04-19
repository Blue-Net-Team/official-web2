package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.assessment_question.AssessmentQuestionDTO;
import com.bluenet.web.api.dto.assessment_question.CreateQuestionRequestDTO;
import com.bluenet.web.api.dto.assessment_question.UpdateQuestionRequestDTO;
import com.bluenet.web.application.converter.AssessmentQuestionConverter;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.QuestionType;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.model.vo.AssessmentQuestionVO;
import com.bluenet.web.domain.model.vo.AssessmentTimeVO;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.model.vo.evaluation.AlgorithmContent;
import com.bluenet.web.domain.model.vo.evaluation.SingleChoiceContent;
import com.bluenet.web.domain.repository.AssessmentAnswerRepository;
import com.bluenet.web.domain.service.AssessmentQuestionDomainService;
import com.bluenet.web.domain.service.AssessmentSessionDomainService;
import com.bluenet.web.domain.service.AssessmentTimeDomainService;
import com.bluenet.web.domain.service.FileDomainService;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * AssessmentQuestionServiceImpl 单元测试。
 */
@DisplayName("AssessmentQuestionServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class AssessmentQuestionServiceImplTest {
    private static final Long QUESTION_ID = 10L;
    private static final Long ASSESSMENT_TIME_ID = 20L;
    private static final Long USER_ID = 30L;

    @Mock
    private AssessmentQuestionDomainService assessmentQuestionDomainService;

    @Mock
    private AssessmentTimeDomainService assessmentTimeDomainService;

    @Mock
    private AssessmentSessionDomainService assessmentSessionDomainService;

    @Spy
    private AssessmentQuestionConverter assessmentQuestionConverter = new AssessmentQuestionConverter();

    @Mock
    private AssessmentAnswerRepository assessmentAnswerRepository;

    @Mock
    private FileDomainService fileDomainService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private AssessmentQuestionServiceImpl assessmentQuestionService;

    @Test
    @DisplayName("创建算法题缺少语言模板：应拒绝")
    void createQuestion_algorithmWithoutStarterCode_shouldThrow() {
        when(assessmentTimeDomainService.getById(ASSESSMENT_TIME_ID)).thenReturn(Optional.of(createTimeVO()));
        AlgorithmContent content = createValidAlgorithmContent();
        content.setStarterCode(null);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> assessmentQuestionService.createQuestion(createAlgorithmRequest(content)));

        assertEquals("算法题至少需要配置一个语言模板", ex.getMessage());
        verify(assessmentQuestionDomainService, never()).createQuestion(any());
    }

    @Test
    @DisplayName("创建算法题正式用例缺少期望输出：应拒绝")
    void createQuestion_algorithmFormalCaseMissingExpectedOutput_shouldThrow() {
        when(assessmentTimeDomainService.getById(ASSESSMENT_TIME_ID)).thenReturn(Optional.of(createTimeVO()));
        AlgorithmContent content = createValidAlgorithmContent();
        content.getTestCases().get(0).setExpectedOutput(" ");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> assessmentQuestionService.createQuestion(createAlgorithmRequest(content)));

        assertEquals("正式测试用例第1个用例必须包含输入和期望输出", ex.getMessage());
        verify(assessmentQuestionDomainService, never()).createQuestion(any());
    }

    @Test
    @DisplayName("创建算法题默认运行用例缺少输入：应拒绝")
    void createQuestion_algorithmRunCaseMissingInput_shouldThrow() {
        when(assessmentTimeDomainService.getById(ASSESSMENT_TIME_ID)).thenReturn(Optional.of(createTimeVO()));
        AlgorithmContent content = createValidAlgorithmContent();
        content.getRunTestCases().get(0).setInput("");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> assessmentQuestionService.createQuestion(createAlgorithmRequest(content)));

        assertEquals("默认运行测试用例第1个用例必须包含输入和期望输出", ex.getMessage());
        verify(assessmentQuestionDomainService, never()).createQuestion(any());
    }

    @Test
    @DisplayName("创建合法算法题：应调用领域服务保存")
    void createQuestion_validAlgorithm_shouldCreateQuestion() {
        when(assessmentTimeDomainService.getById(ASSESSMENT_TIME_ID)).thenReturn(Optional.of(createTimeVO()));
        when(assessmentQuestionDomainService.createQuestion(any(AssessmentQuestionVO.class)))
                .thenReturn(createQuestionVO(QuestionType.ALGORITHM, createValidAlgorithmContent()));

        AssessmentQuestionDTO result = assessmentQuestionService
                .createQuestion(createAlgorithmRequest(createValidAlgorithmContent()));

        assertEquals(QUESTION_ID, result.getId());
        assertEquals(QuestionType.ALGORITHM, result.getQuestionType());
        verify(assessmentQuestionDomainService).createQuestion(
                argThat(
                        question -> question.getQuestionType() == QuestionType.ALGORITHM
                                && question.getContent() instanceof AlgorithmContent));
    }

    @Test
    @DisplayName("更新为算法题缺少正式用例：应拒绝")
    void updateQuestion_algorithmWithoutFormalCases_shouldThrow() {
        AssessmentQuestionVO existing = createQuestionVO(QuestionType.SINGLE_CHOICE, createSingleChoiceContent());
        when(assessmentQuestionDomainService.getQuestionById(QUESTION_ID)).thenReturn(existing);
        AlgorithmContent content = createValidAlgorithmContent();
        content.setTestCases(List.of());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> assessmentQuestionService.updateQuestion(
                        QUESTION_ID,
                        UpdateQuestionRequestDTO.builder()
                                .questionType(QuestionType.ALGORITHM)
                                .content(content)
                                .build()));

        assertEquals("算法题至少需要配置一个正式测试用例", ex.getMessage());
        verify(assessmentQuestionDomainService, never()).updateQuestion(any());
    }

    @Test
    @DisplayName("考生查看算法题详情：不应暴露正式测试用例")
    void getQuestionDetailForUser_algorithm_shouldHideFormalTestCases() {
        try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
            mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createCandidateUser());
            when(assessmentQuestionDomainService.getQuestionById(QUESTION_ID))
                    .thenReturn(createQuestionVO(QuestionType.ALGORITHM, createValidAlgorithmContent()));
            when(assessmentTimeDomainService.getById(ASSESSMENT_TIME_ID)).thenReturn(Optional.of(createTimeVO()));

            AssessmentQuestionDTO result = assessmentQuestionService.getQuestionDetailForUser(QUESTION_ID);

            assertTrue(result.getContent() instanceof AlgorithmContent);
            AlgorithmContent content = (AlgorithmContent) result.getContent();
            assertNull(content.getTestCases());
            assertNotNull(content.getRunTestCases());
            assertNotNull(content.getStarterCode());
        }
    }

    @Test
    @DisplayName("管理端查询算法题：应保留正式测试用例")
    void listQuestionsForAdmin_algorithm_shouldKeepFormalTestCases() {
        org.springframework.data.domain.Page<AssessmentQuestionVO> page = new org.springframework.data.domain.PageImpl<>(
                List.of(createQuestionVO(QuestionType.ALGORITHM, createValidAlgorithmContent())));
        when(assessmentQuestionDomainService.listQuestions(eq(ASSESSMENT_TIME_ID), any())).thenReturn(page);

        AssessmentQuestionDTO result = assessmentQuestionService
                .listQuestionsForAdmin(ASSESSMENT_TIME_ID, 0, 10)
                .getContent()
                .get(0);

        AlgorithmContent content = (AlgorithmContent) result.getContent();
        assertNotNull(content.getTestCases());
        assertEquals(1, content.getTestCases().size());
    }

    private CreateQuestionRequestDTO createAlgorithmRequest(AlgorithmContent content) {
        return CreateQuestionRequestDTO.builder()
                .assessmentTimeId(ASSESSMENT_TIME_ID)
                .questionNo(1)
                .questionType(QuestionType.ALGORITHM)
                .title("两数之和")
                .content(content)
                .score(BigDecimal.TEN)
                .build();
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

    private SingleChoiceContent createSingleChoiceContent() {
        SingleChoiceContent content = new SingleChoiceContent();
        content.setContent("单选题");
        content.setOptions(List.of("A", "B"));
        content.setCorrectAnswer("A");
        return content;
    }

    private AssessmentQuestionVO createQuestionVO(QuestionType questionType, Object content) {
        return AssessmentQuestionVO.builder()
                .id(QUESTION_ID)
                .assessmentTimeId(ASSESSMENT_TIME_ID)
                .questionNo(1)
                .questionType(questionType)
                .title("题目")
                .content((com.bluenet.web.domain.model.vo.evaluation.QuestionContent) content)
                .score(BigDecimal.TEN)
                .build();
    }

    private AssessmentTimeVO createTimeVO() {
        return AssessmentTimeVO.builder()
                .id(ASSESSMENT_TIME_ID)
                .direction(Direction.COMPUTER_VISION)
                .grade(2024)
                .build();
    }

    private UserVO createCandidateUser() {
        return UserVO.builder()
                .id(USER_ID)
                .roleName(RoleType.CANDIDATE.getName())
                .studentId("2024123456")
                .assessmentGradeYear(2024)
                .direction(Direction.COMPUTER_VISION)
                .build();
    }
}
