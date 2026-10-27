package com.bluenet.web.application.service.impl;

import com.bluenet.web.DBIntegrationTest;
import com.bluenet.web.application.command.assessment_question.AssessmentQuestionCommands;
import com.bluenet.web.application.command.judge.JudgeProblemConfigCommands;
import com.bluenet.web.application.result.assessment.AssessmentQuestionResult;
import com.bluenet.web.application.result.judge.JudgeProblemConfigResult;
import com.bluenet.web.application.service.AssessmentQuestionAppService;
import com.bluenet.web.application.service.JudgeProblemConfigAdminService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataConflict;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.model.entity.AssessmentAnswer;
import com.bluenet.web.domain.model.entity.AssessmentQuestion;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.entity.JudgeLanguageLimit;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.QuestionType;
import com.bluenet.web.domain.model.vo.question_content.AlgorithmContent;
import com.bluenet.web.domain.model.vo.question_content.SingleChoiceContent;
import com.bluenet.web.domain.repository.AssessmentAnswerRepository;
import com.bluenet.web.domain.repository.AssessmentJudgementRepository;
import com.bluenet.web.domain.repository.AssessmentQuestionRepository;
import com.bluenet.web.domain.repository.AssessmentSessionRepository;
import com.bluenet.web.domain.repository.AssessmentTimeRepository;
import com.bluenet.web.domain.repository.JudgeLanguageLimitRepository;
import com.bluenet.web.domain.repository.JudgeProblemConfigRepository;
import com.bluenet.web.domain.repository.JudgeStandardSolutionRepository;
import com.bluenet.web.domain.repository.JudgeTestcaseConfigRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.testsupport.fixture.AssessmentFixture;
import com.bluenet.web.infrastructure.judge.JudgeTestDataGenerationPublisher;
import com.bluenet.web.infrastructure.storage.JudgeAssetStorage;
import com.bluenet.web.testsupport.fixture.UserFixture;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verifyNoInteractions;

@DisplayName("考题克隆应用服务集成测试")
class AssessmentQuestionCloneAppServiceImplIntegrationTest extends DBIntegrationTest {

    @Autowired
    private AssessmentQuestionAppService assessmentQuestionAppService;

    @Autowired
    private JudgeProblemConfigAdminService judgeProblemConfigAdminService;

    @Autowired
    private AssessmentQuestionRepository assessmentQuestionRepository;

    @Autowired
    private AssessmentTimeRepository assessmentTimeRepository;

    @Autowired
    private AssessmentAnswerRepository assessmentAnswerRepository;

    @Autowired
    private AssessmentJudgementRepository assessmentJudgementRepository;

    @Autowired
    private AssessmentSessionRepository assessmentSessionRepository;

    @Autowired
    private JudgeProblemConfigRepository judgeProblemConfigRepository;

    @Autowired
    private JudgeStandardSolutionRepository judgeStandardSolutionRepository;

    @Autowired
    private JudgeTestcaseConfigRepository judgeTestcaseConfigRepository;

    @Autowired
    private JudgeLanguageLimitRepository judgeLanguageLimitRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private JudgeAssetStorage judgeAssetStorage;

    @MockitoBean
    private JudgeTestDataGenerationPublisher testDataGenerationPublisher;

    private final Map<String, byte[]> judgeAssets = new HashMap<>();
    private boolean failJudgeWrites;
    private long sequence = 1000;

    @BeforeEach
    void setUpJudgeAssets() {
        judgeAssets.clear();
        failJudgeWrites = false;
        lenient().doAnswer(invocation -> {
            if (failJudgeWrites) {
                throw new IllegalStateException("模拟判题资产写入失败");
            }
            judgeAssets.put(invocation.getArgument(0), ((byte[]) invocation.getArgument(1)).clone());
            return null;
        }).when(judgeAssetStorage).put(anyString(), any(byte[].class), anyString());
        lenient().when(judgeAssetStorage.get(anyString()))
                .thenAnswer(invocation -> judgeAssets.getOrDefault(invocation.getArgument(0), new byte[0]));
        lenient().doAnswer(invocation -> {
            judgeAssets.remove(invocation.getArgument(0));
            return null;
        }).when(judgeAssetStorage).delete(anyString());
        lenient().doAnswer(invocation -> {
            String prefix = invocation.getArgument(0);
            judgeAssets.keySet().removeIf(key -> key.startsWith(prefix));
            return null;
        }).when(judgeAssetStorage).deleteByPrefix(anyString());
    }

    @Test
    @DisplayName("cloneQuestion: 普通选择题应完整复制且源题不变")
    void cloneQuestion_choiceQuestion_shouldCreateIndependentCopy() {
        User admin = createSuperAdmin();
        AssessmentTime sourceTime = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentTime targetTime = createTime(Direction.COMPUTER_VISION, 2025);
        SingleChoiceContent content = new SingleChoiceContent();
        content.setContent("题干");
        content.setOptions(new ArrayList<>(List.of("A", "B")));
        content.setCorrectAnswer("B");
        AssessmentQuestion source = AssessmentFixture.questionBuilder()
                .assessmentTime(sourceTime)
                .questionNo(2)
                .type(QuestionType.SINGLE_CHOICE)
                .title("源选择题")
                .content(content)
                .attachmentId(99L)
                .score(new BigDecimal("12.50"))
                .save(assessmentQuestionRepository);

        AssessmentQuestionResult result = assessmentQuestionAppService.cloneQuestion(
                admin.getId(),
                cloneCommand(source.getId(), targetTime.getId(), 7));

        AssessmentQuestion target = assessmentQuestionRepository.findById(result.id()).orElseThrow();
        SingleChoiceContent targetContent = (SingleChoiceContent) target.getContent();
        assertNotEquals(source.getId(), target.getId());
        assertEquals(targetTime.getId(), target.getAssessmentTimeId());
        assertEquals(7, target.getQuestionNo());
        assertEquals(QuestionType.SINGLE_CHOICE, target.getQuestionType());
        assertEquals("源选择题", target.getTitle());
        assertEquals("题干", targetContent.getContent());
        assertEquals(List.of("A", "B"), targetContent.getOptions());
        assertEquals("B", targetContent.getCorrectAnswer());
        assertEquals(99L, target.getAttachmentId());
        assertEquals(new BigDecimal("12.50"), target.getScore());

        AssessmentQuestion unchangedSource = assessmentQuestionRepository.findById(source.getId()).orElseThrow();
        assertEquals(sourceTime.getId(), unchangedSource.getAssessmentTimeId());
        assertEquals(2, unchangedSource.getQuestionNo());
        assertEquals("B", ((SingleChoiceContent) unchangedSource.getContent()).getCorrectAnswer());
    }

    @Test
    @DisplayName("cloneQuestion: 无判题配置的算法题应复制完整题面")
    void cloneQuestion_algorithmWithoutConfig_shouldCopyStatementOnly() {
        User admin = createSuperAdmin();
        AssessmentTime sourceTime = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentTime targetTime = createTime(Direction.COMPUTER_VISION, 2025);
        AssessmentQuestion source = AssessmentFixture.questionBuilder()
                .assessmentTime(sourceTime)
                .type(QuestionType.ALGORITHM)
                .title("源算法题")
                .content(algorithmContent())
                .save(assessmentQuestionRepository);

        AssessmentQuestionResult result = assessmentQuestionAppService.cloneQuestion(
                admin.getId(),
                cloneCommand(source.getId(), targetTime.getId(), 1));

        AssessmentQuestion target = assessmentQuestionRepository.findById(result.id()).orElseThrow();
        AlgorithmContent content = (AlgorithmContent) target.getContent();
        assertEquals("算法题面", content.getContent());
        assertEquals("输入", content.getInputDescription());
        assertEquals("输出", content.getOutputDescription());
        assertEquals("范围", content.getConstraints());
        assertEquals("1 2", content.getExamples().get(0).getInput());
        assertEquals("3", content.getRunTestCases().get(0).getExpectedOutput());
        assertEquals("print(1)", content.getStarterCode().get("python"));
        assertTrue(judgeProblemConfigRepository.findByQuestionId(target.getId()).isEmpty());
    }

    @Test
    @DisplayName("cloneQuestion: 已配置算法题应重建独立草稿配置")
    void cloneQuestion_configuredAlgorithm_shouldRebuildDraftConfig() throws Exception {
        User admin = createSuperAdmin();
        AssessmentTime sourceTime = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentTime targetTime = createTime(Direction.COMPUTER_VISION, 2025);
        AssessmentQuestion source = createAlgorithmQuestion(sourceTime);
        JudgeProblemConfigResult sourceConfig = createJudgeConfig(source);
        jdbcTemplate.update(
                "INSERT INTO tb_judge_test_case "
                        + "(config_id, question_id, case_no, category, input_object_key, input_object_hash, "
                        + "output_object_key, output_object_hash, input_size_bytes, output_size_bytes, weight, hidden, sample) "
                        + "VALUES (?, ?, 1, 'NORMAL', ?, 'in-hash', ?, 'out-hash', 1, 1, 1, TRUE, FALSE)",
                sourceConfig.id(),
                source.getId(),
                "questions/" + source.getId() + "/current/testcases/0001-in.in",
                "questions/" + source.getId() + "/current/testcases/0001-out.out");
        judgeLanguageLimitRepository.upsertConfirmedLimit(
                JudgeLanguageLimit.createConfirmed(
                        source.getId(),
                        "python",
                        1000,
                        65536,
                        1024,
                        sourceConfig.id()));

        AssessmentQuestionResult result = assessmentQuestionAppService.cloneQuestion(
                admin.getId(),
                cloneCommand(source.getId(), targetTime.getId(), 1));

        JudgeProblemConfigResult targetConfig = judgeProblemConfigAdminService
                .findByQuestionId(result.id())
                .orElseThrow();
        assertNotEquals(sourceConfig.id(), targetConfig.id());
        assertEquals(result.id(), targetConfig.questionId());
        assertEquals("DRAFT", targetConfig.status());
        assertEquals(sourceConfig.generatorLanguage(), targetConfig.generatorLanguage());
        assertEquals(sourceConfig.generatorSource(), targetConfig.generatorSource());
        assertEquals(sourceConfig.primaryStandardLanguage(), targetConfig.primaryStandardLanguage());
        assertEquals(sourceConfig.benchmarkRepeatTimes(), targetConfig.benchmarkRepeatTimes());
        assertEquals(sourceConfig.marginMultiplier(), targetConfig.marginMultiplier());
        assertEquals(sourceConfig.minExtraMs(), targetConfig.minExtraMs());
        assertEquals(sourceConfig.roundToMs(), targetConfig.roundToMs());
        assertEquals(
                sourceConfig.standardSolutions().get(0).source(),
                targetConfig.standardSolutions().get(0).source());
        assertEquals(
                sourceConfig.testcases().get(0).generatorArgs(),
                targetConfig.testcases().get(0).generatorArgs());
        assertEquals(
                "PENDING",
                judgeStandardSolutionRepository
                        .findByConfigId(targetConfig.id())
                        .get(0)
                        .getBenchmarkStatus());
        assertEquals(1, judgeTestcaseConfigRepository.findByConfigId(targetConfig.id()).size());
        assertNotEquals(sourceConfig.generatorObjectKey(), targetConfig.generatorObjectKey());
        assertTrue(targetConfig.generatorObjectKey().startsWith("questions/" + result.id() + "/current/"));
        assertTrue(judgeAssets.containsKey(targetConfig.generatorObjectKey()));
        assertTrue(judgeAssets.containsKey(targetConfig.manifestObjectKey()));
        assertEquals(
                0,
                jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM tb_judge_test_case WHERE config_id = ?",
                        Integer.class,
                        targetConfig.id()));
        assertFalse(judgeLanguageLimitRepository.existsConfirmedByQuestionIdAndLanguage(result.id(), "python"));
        assertTrue(judgeLanguageLimitRepository.existsConfirmedByQuestionIdAndLanguage(source.getId(), "python"));
        var manifest = objectMapper.readTree(judgeAssets.get(targetConfig.manifestObjectKey()));
        assertEquals(result.id(), manifest.path("questionId").asLong());
        assertEquals(targetConfig.id(), manifest.path("configId").asLong());
        verifyNoInteractions(testDataGenerationPublisher);
    }

    @Test
    @DisplayName("cloneQuestion: 删除源题不得删除副本判题资产")
    void cloneQuestion_deleteSource_shouldKeepTargetAssets() {
        User admin = createSuperAdmin();
        AssessmentTime sourceTime = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentTime targetTime = createTime(Direction.COMPUTER_VISION, 2025);
        AssessmentQuestion source = createAlgorithmQuestion(sourceTime);
        JudgeProblemConfigResult sourceConfig = createJudgeConfig(source);
        AssessmentQuestionResult result = assessmentQuestionAppService.cloneQuestion(
                admin.getId(),
                cloneCommand(source.getId(), targetTime.getId(), 1));
        JudgeProblemConfigResult targetConfig = judgeProblemConfigAdminService
                .findByQuestionId(result.id())
                .orElseThrow();

        assessmentQuestionAppService.deleteQuestion(source.getId());

        assertFalse(judgeAssets.containsKey(sourceConfig.generatorObjectKey()));
        assertTrue(judgeAssets.containsKey(targetConfig.generatorObjectKey()));
        assertTrue(judgeAssets.containsKey(targetConfig.manifestObjectKey()));
        assertTrue(judgeProblemConfigRepository.findByQuestionId(result.id()).isPresent());
        assertEquals(
                sourceConfig.generatorSource(),
                judgeProblemConfigAdminService
                        .findByQuestionId(result.id())
                        .orElseThrow()
                        .generatorSource());
    }

    @Test
    @DisplayName("cloneQuestion: 源判题源码缺失应失败且回滚目标题目")
    void cloneQuestion_missingSourceAsset_shouldRollbackTarget() {
        User admin = createSuperAdmin();
        AssessmentTime sourceTime = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentTime targetTime = createTime(Direction.COMPUTER_VISION, 2025);
        AssessmentQuestion source = createAlgorithmQuestion(sourceTime);
        JudgeProblemConfigResult sourceConfig = createJudgeConfig(source);
        judgeAssets.remove(sourceConfig.generatorObjectKey());

        assertThrows(
                IllegalStateException.class,
                () -> assessmentQuestionAppService.cloneQuestion(
                        admin.getId(),
                        cloneCommand(source.getId(), targetTime.getId(), 1)));

        assertEquals(0, assessmentQuestionRepository.countByAssessmentTimeId(targetTime.getId()));
        assertTrue(judgeProblemConfigRepository.findByQuestionId(source.getId()).isPresent());
    }

    @Test
    @DisplayName("cloneQuestion: 目标资产写入失败应回滚且不影响源资产")
    void cloneQuestion_targetAssetWriteFails_shouldRollbackAndKeepSource() {
        User admin = createSuperAdmin();
        AssessmentTime sourceTime = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentTime targetTime = createTime(Direction.COMPUTER_VISION, 2025);
        AssessmentQuestion source = createAlgorithmQuestion(sourceTime);
        JudgeProblemConfigResult sourceConfig = createJudgeConfig(source);
        failJudgeWrites = true;

        assertThrows(
                IllegalStateException.class,
                () -> assessmentQuestionAppService.cloneQuestion(
                        admin.getId(),
                        cloneCommand(source.getId(), targetTime.getId(), 1)));

        assertEquals(0, assessmentQuestionRepository.countByAssessmentTimeId(targetTime.getId()));
        assertTrue(judgeAssets.containsKey(sourceConfig.generatorObjectKey()));
        assertTrue(judgeAssets.containsKey(sourceConfig.manifestObjectKey()));
        assertTrue(judgeProblemConfigRepository.findByQuestionId(source.getId()).isPresent());
    }

    @Test
    @DisplayName("cloneQuestion: 作答评审和考核会话不得复制")
    void cloneQuestion_withRuntimeData_shouldNotCopyActivity() {
        User admin = createSuperAdmin();
        User candidate = UserFixture.candidate("SC" + (++sequence))
                .withDirection(Direction.COMPUTER_VISION)
                .save(userRepository, passwordEncoder);
        AssessmentTime sourceTime = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentTime targetTime = createTime(Direction.COMPUTER_VISION, 2025);
        AssessmentQuestion source = createSourceQuestion(sourceTime);
        AssessmentAnswer answer = AssessmentFixture.answerBuilder()
                .user(candidate)
                .question(source)
                .save(assessmentAnswerRepository);
        AssessmentFixture.judgementBuilder()
                .answer(answer)
                .question(source)
                .reviewer(admin)
                .save(assessmentJudgementRepository);
        AssessmentFixture.sessionBuilder()
                .user(candidate)
                .assessmentTime(sourceTime)
                .save(assessmentSessionRepository);

        AssessmentQuestionResult result = assessmentQuestionAppService.cloneQuestion(
                admin.getId(),
                cloneCommand(source.getId(), targetTime.getId(), 1));

        assertTrue(assessmentAnswerRepository.existsByUserIdAndQuestionId(candidate.getId(), source.getId()));
        assertFalse(assessmentAnswerRepository.existsByUserIdAndQuestionId(candidate.getId(), result.id()));
        assertEquals(1, assessmentJudgementRepository.findAllByQuestionId(source.getId()).size());
        assertTrue(assessmentJudgementRepository.findAllByQuestionId(result.id()).isEmpty());
        assertTrue(
                assessmentSessionRepository
                        .findByUserIdAndAssessmentTimeId(candidate.getId(), sourceTime.getId())
                        .isPresent());
        assertTrue(
                assessmentSessionRepository
                        .findByUserIdAndAssessmentTimeId(candidate.getId(), targetTime.getId())
                        .isEmpty());
    }

    @Test
    @DisplayName("cloneQuestion: 源题或目标考核时间不存在应拒绝")
    void cloneQuestion_missingSourceOrTarget_shouldThrowNotFound() {
        User admin = createSuperAdmin();
        AssessmentTime sourceTime = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentTime targetTime = createTime(Direction.COMPUTER_VISION, 2025);
        AssessmentQuestion source = createSourceQuestion(sourceTime);

        assertThrows(
                DataNotFound.class,
                () -> assessmentQuestionAppService.cloneQuestion(
                        admin.getId(),
                        cloneCommand(source.getId(), -1L, 1)));
        assertThrows(
                DataNotFound.class,
                () -> assessmentQuestionAppService.cloneQuestion(
                        admin.getId(),
                        cloneCommand(-1L, targetTime.getId(), 1)));
    }

    @Test
    @DisplayName("cloneQuestion: 目标与源考核时间相同应拒绝")
    void cloneQuestion_sameAssessmentTime_shouldThrowBadRequest() {
        User admin = createSuperAdmin();
        AssessmentTime sourceTime = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestion source = createSourceQuestion(sourceTime);

        assertThrows(
                BadRequest.class,
                () -> assessmentQuestionAppService.cloneQuestion(
                        admin.getId(),
                        cloneCommand(source.getId(), sourceTime.getId(), 2)));
    }

    @Test
    @DisplayName("cloneQuestion: 目标题号冲突应拒绝且不创建题目")
    void cloneQuestion_duplicateQuestionNo_shouldThrowConflict() {
        User admin = createSuperAdmin();
        AssessmentTime sourceTime = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentTime targetTime = createTime(Direction.COMPUTER_VISION, 2025);
        AssessmentQuestion source = createSourceQuestion(sourceTime);
        AssessmentFixture.questionBuilder()
                .assessmentTime(targetTime)
                .questionNo(3)
                .save(assessmentQuestionRepository);

        assertThrows(
                DataConflict.class,
                () -> assessmentQuestionAppService.cloneQuestion(
                        admin.getId(),
                        cloneCommand(source.getId(), targetTime.getId(), 3)));
        assertEquals(1, assessmentQuestionRepository.countByAssessmentTimeId(targetTime.getId()));
    }

    @Test
    @DisplayName("cloneQuestion: 方向管理员可同方向跨年级克隆")
    void cloneQuestion_directionAdminAcrossGrades_shouldSucceed() {
        User admin = UserFixture.directionAdmin("DA" + (++sequence), Direction.COMPUTER_VISION)
                .save(userRepository, passwordEncoder);
        AssessmentTime sourceTime = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentTime targetTime = createTime(Direction.COMPUTER_VISION, 2025);
        AssessmentQuestion source = createSourceQuestion(sourceTime);

        AssessmentQuestionResult result = assessmentQuestionAppService.cloneQuestion(
                admin.getId(),
                cloneCommand(source.getId(), targetTime.getId(), 4));

        assertEquals(targetTime.getId(), result.assessmentTimeId());
        assertEquals(4, result.questionNo());
    }

    @Test
    @DisplayName("cloneQuestion: 跨方向或方向越权应拒绝")
    void cloneQuestion_crossDirection_shouldThrowForbidden() {
        User admin = UserFixture.directionAdmin("DA" + (++sequence), Direction.COMPUTER_VISION)
                .save(userRepository, passwordEncoder);
        AssessmentTime sourceTime = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentTime targetTime = createTime(Direction.STRUCTURAL_DESIGN, 2025);
        AssessmentQuestion source = createSourceQuestion(sourceTime);

        assertThrows(
                Forbidden.class,
                () -> assessmentQuestionAppService.cloneQuestion(
                        admin.getId(),
                        cloneCommand(source.getId(), targetTime.getId(), 1)));
    }

    @Test
    @DisplayName("cloneQuestion: 非超级管理员不能克隆全局考核")
    void cloneQuestion_globalAsDirectionAdmin_shouldThrowForbidden() {
        User admin = UserFixture.directionAdmin("DA" + (++sequence), Direction.COMPUTER_VISION)
                .save(userRepository, passwordEncoder);
        AssessmentTime sourceTime = createTime(null, null);
        AssessmentTime targetTime = createTime(null, null);
        AssessmentQuestion source = createSourceQuestion(sourceTime);

        assertThrows(
                Forbidden.class,
                () -> assessmentQuestionAppService.cloneQuestion(
                        admin.getId(),
                        cloneCommand(source.getId(), targetTime.getId(), 1)));
    }

    @Test
    @DisplayName("cloneQuestion: 其他角色即使调用服务也应拒绝")
    void cloneQuestion_asMember_shouldThrowForbidden() {
        User member = UserFixture.member("MB" + (++sequence)).save(userRepository, passwordEncoder);
        AssessmentTime sourceTime = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentTime targetTime = createTime(Direction.COMPUTER_VISION, 2025);
        AssessmentQuestion source = createSourceQuestion(sourceTime);

        assertThrows(
                Forbidden.class,
                () -> assessmentQuestionAppService.cloneQuestion(
                        member.getId(),
                        cloneCommand(source.getId(), targetTime.getId(), 1)));
    }

    @Test
    @DisplayName("cloneQuestion: 超级管理员可在不同全局考核间克隆")
    void cloneQuestion_superAdminBetweenGlobalTimes_shouldSucceed() {
        User admin = createSuperAdmin();
        AssessmentTime sourceTime = createTime(null, null);
        AssessmentTime targetTime = createTime(null, null);
        AssessmentQuestion source = createSourceQuestion(sourceTime);

        AssessmentQuestionResult result = assessmentQuestionAppService.cloneQuestion(
                admin.getId(),
                cloneCommand(source.getId(), targetTime.getId(), 1));

        assertEquals(targetTime.getId(), result.assessmentTimeId());
    }

    private AssessmentQuestion createSourceQuestion(AssessmentTime sourceTime) {
        return AssessmentFixture.questionBuilder()
                .assessmentTime(sourceTime)
                .save(assessmentQuestionRepository);
    }

    private AssessmentQuestion createAlgorithmQuestion(AssessmentTime assessmentTime) {
        return AssessmentFixture.questionBuilder()
                .assessmentTime(assessmentTime)
                .type(QuestionType.ALGORITHM)
                .title("源算法题")
                .content(algorithmContent())
                .save(assessmentQuestionRepository);
    }

    private JudgeProblemConfigResult createJudgeConfig(AssessmentQuestion question) {
        JudgeProblemConfigCommands.StandardSolutionCommand solution = new JudgeProblemConfigCommands.StandardSolutionCommand(
                "python", "print(input())", true);
        JudgeProblemConfigCommands.TestcaseConfigCommand testcase = new JudgeProblemConfigCommands.TestcaseConfigCommand(
                1,
                "NORMAL",
                objectMapper.valueToTree(Map.of("n", 10)),
                BigDecimal.ONE,
                true,
                false,
                "常规用例");
        return judgeProblemConfigAdminService.upsert(
                question.getId(),
                new JudgeProblemConfigCommands.UpsertCommand(
                        "python",
                        "print(1)",
                        "python",
                        5,
                        new BigDecimal("1.2"),
                        50,
                        50,
                        List.of(solution),
                        List.of(testcase)));
    }

    private AssessmentQuestionCommands.CloneAssessmentQuestionCommand cloneCommand(
            Long sourceQuestionId, Long targetAssessmentTimeId, Integer targetQuestionNo) {
        return new AssessmentQuestionCommands.CloneAssessmentQuestionCommand(
                sourceQuestionId, targetAssessmentTimeId, targetQuestionNo);
    }

    private User createSuperAdmin() {
        return UserFixture.superAdmin("SA" + (++sequence)).save(userRepository, passwordEncoder);
    }

    private AssessmentTime createTime(Direction direction, Integer grade) {
        return AssessmentFixture.timeBuilder()
                .direction(direction)
                .grade(grade)
                .withinNow()
                .save(assessmentTimeRepository);
    }

    private AlgorithmContent algorithmContent() {
        AlgorithmContent content = new AlgorithmContent();
        content.setContent("算法题面");
        content.setInputDescription("输入");
        content.setOutputDescription("输出");
        content.setConstraints("范围");
        AlgorithmContent.Example example = new AlgorithmContent.Example();
        example.setInput("1 2");
        example.setExpectedOutput("3");
        example.setExplanation("样例说明");
        content.setExamples(new ArrayList<>(List.of(example)));
        AlgorithmContent.TestCase runTestCase = new AlgorithmContent.TestCase();
        runTestCase.setInput("1 2");
        runTestCase.setExpectedOutput("3");
        content.setRunTestCases(new ArrayList<>(List.of(runTestCase)));
        content.setStarterCode(new HashMap<>());
        content.getStarterCode().put("python", "print(1)");
        return content;
    }
}
