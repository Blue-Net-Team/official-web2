package com.bluenet.web.application.service.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.application.command.algorithm_judge.AlgorithmJudgeCommands;
import com.bluenet.web.application.result.algorithm_judge.AlgorithmJudgeResult;
import com.bluenet.web.application.service.AlgorithmJudgeAppService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.model.entity.AlgorithmJudgeCaseResult;
import com.bluenet.web.domain.model.entity.AlgorithmJudgeJob;
import com.bluenet.web.domain.model.entity.AssessmentAnswer;
import com.bluenet.web.domain.model.entity.AssessmentJudgement;
import com.bluenet.web.domain.model.entity.AssessmentQuestion;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.AlgorithmTestcaseType;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.JudgeCaseStatus;
import com.bluenet.web.domain.model.enumerate.JudgeJobStatus;
import com.bluenet.web.domain.model.enumerate.JudgementSource;
import com.bluenet.web.domain.model.enumerate.JudgementStatus;
import com.bluenet.web.domain.model.enumerate.ObjectiveResultCode;
import com.bluenet.web.domain.model.enumerate.ProgrammingLanguage;
import com.bluenet.web.domain.model.enumerate.QuestionType;
import com.bluenet.web.domain.model.enumerate.ReviewerType;
import com.bluenet.web.domain.model.vo.question_content.AlgorithmContent;
import com.bluenet.web.domain.repository.AlgorithmJudgeCaseResultRepository;
import com.bluenet.web.domain.repository.AlgorithmJudgeJobRepository;
import com.bluenet.web.domain.repository.AssessmentAnswerRepository;
import com.bluenet.web.domain.repository.AssessmentJudgementRepository;
import com.bluenet.web.domain.repository.AssessmentQuestionRepository;
import com.bluenet.web.domain.repository.AssessmentSessionRepository;
import com.bluenet.web.domain.repository.AssessmentTimeRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.infrastructure.judge.AlgorithmJudgeJobPublisher;
import com.bluenet.web.infrastructure.repository.dataobject.JudgeProblemConfigDO;
import com.bluenet.web.infrastructure.repository.mapper.JudgeLanguageLimitMapper;
import com.bluenet.web.infrastructure.repository.mapper.JudgeProblemConfigMapper;
import com.bluenet.web.testsupport.fixture.AssessmentFixture;
import com.bluenet.web.testsupport.fixture.SecurityContextFixture;
import com.bluenet.web.testsupport.fixture.TimeFixture;
import com.bluenet.web.testsupport.fixture.UserFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * AlgorithmJudgeAppServiceImpl 集成测试。
 *
 * <p>
 * 验证算法判题应用服务的运行、提交及轮询结果逻辑。
 * </p>
 */
@DisplayName("AlgorithmJudgeAppServiceImpl 集成测试")
class AlgorithmJudgeAppServiceImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AlgorithmJudgeAppService algorithmJudgeAppService;

    @Autowired
    private AssessmentQuestionRepository assessmentQuestionRepository;

    @Autowired
    private AssessmentTimeRepository assessmentTimeRepository;

    @Autowired
    private AssessmentAnswerRepository assessmentAnswerRepository;

    @Autowired
    private AssessmentSessionRepository assessmentSessionRepository;

    @Autowired
    private AssessmentJudgementRepository assessmentJudgementRepository;

    @Autowired
    private AlgorithmJudgeJobRepository algorithmJudgeJobRepository;

    @Autowired
    private AlgorithmJudgeCaseResultRepository algorithmJudgeCaseResultRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JudgeProblemConfigMapper judgeProblemConfigMapper;

    @Autowired
    private JudgeLanguageLimitMapper judgeLanguageLimitMapper;

    @MockitoBean
    private AlgorithmJudgeJobPublisher algorithmJudgeJobPublisher;

    private long sequence = 1000;

    private String nextStudentId(String prefix) {
        return prefix + (++sequence);
    }

    private User createCandidate(Direction direction, Integer gradeYear) {
        return UserFixture.candidate(nextStudentId("SC"))
                .withDirection(direction)
                .withAssessmentGradeYear(gradeYear)
                .save(userRepository, passwordEncoder);
    }

    private AssessmentTime createTime(Direction direction, Integer grade) {
        return AssessmentFixture.timeBuilder()
                .direction(direction)
                .grade(grade)
                .withinNow()
                .save(assessmentTimeRepository);
    }

    private AssessmentQuestion createAlgorithmQuestion(AssessmentTime time) {
        AlgorithmContent content = new AlgorithmContent();
        content.setInputDescription("输入描述");
        content.setOutputDescription("输出描述");
        content.setConstraints("约束说明");

        AlgorithmContent.Example example = new AlgorithmContent.Example();
        example.setInput("1 2");
        example.setExpectedOutput("3");
        example.setExplanation("示例说明");
        content.setExamples(List.of(example));

        AlgorithmContent.TestCase runCase = new AlgorithmContent.TestCase();
        runCase.setInput("3 4");
        runCase.setExpectedOutput("7");
        runCase.setHidden(false);
        runCase.setWeight(1);
        content.setRunTestCases(List.of(runCase));

        content.setStarterCode(Map.of("cpp", "#include <iostream>\nint main() { return 0; }"));
        content.setTimeLimit(1000);
        content.setMemoryLimit(65536);

        return AssessmentFixture.questionBuilder()
                .assessmentTime(time)
                .type(QuestionType.ALGORITHM)
                .content(content)
                .save(assessmentQuestionRepository);
    }

    private AssessmentQuestion createAlgorithmQuestionWithoutCases(AssessmentTime time) {
        AlgorithmContent content = new AlgorithmContent();
        content.setInputDescription("输入描述");
        content.setOutputDescription("输出描述");
        content.setStarterCode(Map.of("cpp", "#include <iostream>\nint main() { return 0; }"));
        content.setTimeLimit(1000);
        content.setMemoryLimit(65536);

        return AssessmentFixture.questionBuilder()
                .assessmentTime(time)
                .type(QuestionType.ALGORITHM)
                .content(content)
                .save(assessmentQuestionRepository);
    }

    private void createConfirmedLanguageLimit(Long questionId, ProgrammingLanguage language) {
        JudgeProblemConfigDO config = new JudgeProblemConfigDO();
        config.setQuestionId(questionId);
        config.setGeneratorLanguage("cpp");
        config.setGeneratorObjectKey("generator");
        config.setGeneratorObjectHash("hash");
        config.setBenchmarkRepeatTimes(5);
        config.setMarginMultiplier(new BigDecimal("1.5000"));
        config.setMinExtraMs(50);
        config.setRoundToMs(50);
        Long configId = judgeProblemConfigMapper.upsertCurrentConfig(config);
        judgeLanguageLimitMapper.upsertConfirmedLimit(
                questionId,
                language.getValue(),
                1000,
                65536,
                1024,
                configId);
    }

    private void loginAs(User user) {
        SecurityContextFixture.asCandidate(user);
    }

    @AfterEach
    void cleanup() {
        SecurityContextFixture.clear();
    }

    @Test
    @DisplayName("run: 应创建默认运行任务并发布判题消息")
    void run_defaultRun_shouldCreateJobAndPublish() {
        User user = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestion question = createAlgorithmQuestion(time);
        loginAs(user);

        AlgorithmJudgeResult.SubmitResult result = algorithmJudgeAppService.run(
                new AlgorithmJudgeCommands.RunCommand(
                        question.getId(), ProgrammingLanguage.CPP, "int main() { return 0; }", null, null));

        assertNotNull(result);
        assertNotNull(result.judgeJobId());
        assertNull(result.answerId());
        assertEquals(AlgorithmTestcaseType.DEFAULT_RUN, result.testcaseType());
        verify(algorithmJudgeJobPublisher).publish(result.judgeJobId(), AlgorithmTestcaseType.DEFAULT_RUN);
    }

    @Test
    @DisplayName("run: 自定义输入应创建 CUSTOM_RUN 任务")
    void run_customInput_shouldCreateCustomRunJob() {
        User user = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestion question = createAlgorithmQuestion(time);
        loginAs(user);

        AlgorithmJudgeResult.SubmitResult result = algorithmJudgeAppService.run(
                new AlgorithmJudgeCommands.RunCommand(
                        question.getId(), ProgrammingLanguage.CPP, "int main() { return 0; }",
                        AlgorithmTestcaseType.CUSTOM_RUN, "1 2 3"));

        assertEquals(AlgorithmTestcaseType.CUSTOM_RUN, result.testcaseType());
        verify(algorithmJudgeJobPublisher).publish(result.judgeJobId(), AlgorithmTestcaseType.CUSTOM_RUN);
    }

    @Test
    @DisplayName("run: 题目不存在应抛 DataNotFound")
    void run_questionNotFound_shouldThrowDataNotFound() {
        User user = createCandidate(Direction.COMPUTER_VISION, 2024);
        loginAs(user);

        assertThrows(
                DataNotFound.class,
                () -> algorithmJudgeAppService.run(
                        new AlgorithmJudgeCommands.RunCommand(
                                -1L, ProgrammingLanguage.CPP, "int main() { return 0; }", null, null)));
    }

    @Test
    @DisplayName("run: 非算法题应抛 BadRequest")
    void run_nonAlgorithmQuestion_shouldThrowBadRequest() {
        User user = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTime(time)
                .singleChoice("A", "A", "B")
                .save(assessmentQuestionRepository);
        loginAs(user);

        assertThrows(
                BadRequest.class,
                () -> algorithmJudgeAppService.run(
                        new AlgorithmJudgeCommands.RunCommand(
                                question.getId(), ProgrammingLanguage.CPP, "int main() { return 0; }", null, null)));
    }

    @Test
    @DisplayName("run: 用户方向不匹配应抛 Forbidden")
    void run_directionMismatch_shouldThrowForbidden() {
        User user = createCandidate(Direction.STRUCTURAL_DESIGN, 2024);
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestion question = createAlgorithmQuestion(time);
        loginAs(user);

        assertThrows(
                Forbidden.class,
                () -> algorithmJudgeAppService.run(
                        new AlgorithmJudgeCommands.RunCommand(
                                question.getId(), ProgrammingLanguage.CPP, "int main() { return 0; }", null, null)));
    }

    @Test
    @DisplayName("run: 考核结束后不能运行")
    void run_afterEndTime_shouldThrowBadRequest() {
        User user = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = AssessmentFixture.timeBuilder()
                .direction(Direction.COMPUTER_VISION)
                .grade(2024)
                .ended()
                .save(assessmentTimeRepository);
        AssessmentQuestion question = createAlgorithmQuestion(time);
        loginAs(user);

        assertThrows(
                BadRequest.class,
                () -> algorithmJudgeAppService.run(
                        new AlgorithmJudgeCommands.RunCommand(
                                question.getId(), ProgrammingLanguage.CPP, "int main() { return 0; }", null, null)));
    }

    @Test
    @DisplayName("run: 限时考核会话截止后不能运行")
    void run_afterSessionDeadline_shouldThrowBadRequest() {
        User user = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = AssessmentFixture.timeBuilder()
                .direction(Direction.COMPUTER_VISION)
                .grade(2024)
                .withinNow()
                .timeLimit(30)
                .save(assessmentTimeRepository);
        AssessmentQuestion question = createAlgorithmQuestion(time);
        AssessmentFixture.sessionBuilder()
                .user(user)
                .assessmentTime(time)
                .deadline(TimeFixture.minusMinutes(1))
                .save(assessmentSessionRepository);
        loginAs(user);

        assertThrows(
                BadRequest.class,
                () -> algorithmJudgeAppService.run(
                        new AlgorithmJudgeCommands.RunCommand(
                                question.getId(), ProgrammingLanguage.CPP, "int main() { return 0; }", null, null)));
    }

    @Test
    @DisplayName("run: 正式用例类型不能用于运行接口")
    void run_formalTestcaseType_shouldThrowBadRequest() {
        User user = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestion question = createAlgorithmQuestion(time);
        loginAs(user);

        assertThrows(
                BadRequest.class,
                () -> algorithmJudgeAppService.run(
                        new AlgorithmJudgeCommands.RunCommand(
                                question.getId(), ProgrammingLanguage.CPP, "int main() { return 0; }",
                                AlgorithmTestcaseType.FORMAL, null)));
    }

    @Test
    @DisplayName("run: 无运行用例和示例应抛 BadRequest")
    void run_noRunCasesOrExamples_shouldThrowBadRequest() {
        User user = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestion question = createAlgorithmQuestionWithoutCases(time);
        loginAs(user);

        assertThrows(
                BadRequest.class,
                () -> algorithmJudgeAppService.run(
                        new AlgorithmJudgeCommands.RunCommand(
                                question.getId(), ProgrammingLanguage.CPP, "int main() { return 0; }", null, null)));
    }

    @Test
    @DisplayName("run: 不支持的语言应抛 BadRequest")
    void run_unsupportedLanguage_shouldThrowBadRequest() {
        User user = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestion question = createAlgorithmQuestion(time);
        loginAs(user);

        assertThrows(
                BadRequest.class,
                () -> algorithmJudgeAppService.run(
                        new AlgorithmJudgeCommands.RunCommand(
                                question.getId(), ProgrammingLanguage.JAVA, "public class Main {}", null, null)));
    }

    @Test
    @DisplayName("run: 未登录应抛 SecurityException")
    void run_notAuthenticated_shouldThrowSecurityException() {
        User user = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestion question = createAlgorithmQuestion(time);
        SecurityContextFixture.clear();

        assertThrows(
                SecurityException.class,
                () -> algorithmJudgeAppService.run(
                        new AlgorithmJudgeCommands.RunCommand(
                                question.getId(), ProgrammingLanguage.CPP, "int main() { return 0; }", null, null)));
    }

    @Test
    @DisplayName("submit: 应创建答案和正式判题任务")
    void submit_shouldCreateAnswerAndFormalJob() {
        User user = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestion question = createAlgorithmQuestion(time);
        createConfirmedLanguageLimit(question.getId(), ProgrammingLanguage.CPP);
        loginAs(user);

        AlgorithmJudgeResult.SubmitResult result = algorithmJudgeAppService.submit(
                new AlgorithmJudgeCommands.SubmitCommand(
                        question.getId(), ProgrammingLanguage.CPP, "int main() { return 0; }"));

        assertNotNull(result);
        assertNotNull(result.judgeJobId());
        assertNotNull(result.answerId());
        assertEquals(AlgorithmTestcaseType.FORMAL, result.testcaseType());
        assertTrue(assessmentAnswerRepository.existsByUserIdAndQuestionId(user.getId(), question.getId()));
        verify(algorithmJudgeJobPublisher).publish(result.judgeJobId(), AlgorithmTestcaseType.FORMAL);
    }

    @Test
    @DisplayName("submit: 重复提交应更新答案并创建新任务")
    void submit_duplicate_shouldUpdateAnswerAndCreateNewJob() {
        User user = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestion question = createAlgorithmQuestion(time);
        createConfirmedLanguageLimit(question.getId(), ProgrammingLanguage.CPP);
        loginAs(user);
        AlgorithmJudgeResult.SubmitResult first = algorithmJudgeAppService.submit(
                new AlgorithmJudgeCommands.SubmitCommand(
                        question.getId(), ProgrammingLanguage.CPP, "int main() { return 0; }"));

        AlgorithmJudgeResult.SubmitResult second = algorithmJudgeAppService.submit(
                new AlgorithmJudgeCommands.SubmitCommand(
                        question.getId(), ProgrammingLanguage.CPP, "int main() { return 1; }"));

        assertEquals(first.answerId(), second.answerId());
        AssessmentAnswer answer = assessmentAnswerRepository.findById(second.answerId()).orElseThrow();
        assertEquals("int main() { return 1; }", answer.getContent());
        assertNotEquals(first.judgeJobId(), second.judgeJobId());
    }

    @Test
    @DisplayName("submit: 题目不存在应抛 DataNotFound")
    void submit_questionNotFound_shouldThrowDataNotFound() {
        User user = createCandidate(Direction.COMPUTER_VISION, 2024);
        loginAs(user);

        assertThrows(
                DataNotFound.class,
                () -> algorithmJudgeAppService.submit(
                        new AlgorithmJudgeCommands.SubmitCommand(
                                -1L, ProgrammingLanguage.CPP, "int main() { return 0; }")));
    }

    @Test
    @DisplayName("submit: 非算法题应抛 BadRequest")
    void submit_nonAlgorithmQuestion_shouldThrowBadRequest() {
        User user = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTime(time)
                .singleChoice("A", "A", "B")
                .save(assessmentQuestionRepository);
        loginAs(user);

        assertThrows(
                BadRequest.class,
                () -> algorithmJudgeAppService.submit(
                        new AlgorithmJudgeCommands.SubmitCommand(
                                question.getId(), ProgrammingLanguage.CPP, "int main() { return 0; }")));
    }

    @Test
    @DisplayName("submit: 用户方向不匹配应抛 Forbidden")
    void submit_directionMismatch_shouldThrowForbidden() {
        User user = createCandidate(Direction.STRUCTURAL_DESIGN, 2024);
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestion question = createAlgorithmQuestion(time);
        createConfirmedLanguageLimit(question.getId(), ProgrammingLanguage.CPP);
        loginAs(user);

        assertThrows(
                Forbidden.class,
                () -> algorithmJudgeAppService.submit(
                        new AlgorithmJudgeCommands.SubmitCommand(
                                question.getId(), ProgrammingLanguage.CPP, "int main() { return 0; }")));
    }

    @Test
    @DisplayName("submit: 考核结束后不能提交")
    void submit_afterEndTime_shouldThrowBadRequest() {
        User user = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = AssessmentFixture.timeBuilder()
                .direction(Direction.COMPUTER_VISION)
                .grade(2024)
                .ended()
                .save(assessmentTimeRepository);
        AssessmentQuestion question = createAlgorithmQuestion(time);
        createConfirmedLanguageLimit(question.getId(), ProgrammingLanguage.CPP);
        loginAs(user);

        assertThrows(
                BadRequest.class,
                () -> algorithmJudgeAppService.submit(
                        new AlgorithmJudgeCommands.SubmitCommand(
                                question.getId(), ProgrammingLanguage.CPP, "int main() { return 0; }")));
    }

    @Test
    @DisplayName("submit: 未确认资源限制的语言应抛 BadRequest")
    void submit_languageLimitNotConfirmed_shouldThrowBadRequest() {
        User user = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestion question = createAlgorithmQuestion(time);
        loginAs(user);

        assertThrows(
                BadRequest.class,
                () -> algorithmJudgeAppService.submit(
                        new AlgorithmJudgeCommands.SubmitCommand(
                                question.getId(), ProgrammingLanguage.CPP, "int main() { return 0; }")));
    }

    @Test
    @DisplayName("submit: 空源代码应抛 BadRequest")
    void submit_blankSourceCode_shouldThrowBadRequest() {
        User user = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestion question = createAlgorithmQuestion(time);
        createConfirmedLanguageLimit(question.getId(), ProgrammingLanguage.CPP);
        loginAs(user);

        assertThrows(
                BadRequest.class,
                () -> algorithmJudgeAppService.submit(
                        new AlgorithmJudgeCommands.SubmitCommand(
                                question.getId(), ProgrammingLanguage.CPP, "   ")));
    }

    @Test
    @DisplayName("submit: 不支持的语言应抛 BadRequest")
    void submit_unsupportedLanguage_shouldThrowBadRequest() {
        User user = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestion question = createAlgorithmQuestion(time);
        loginAs(user);

        assertThrows(
                BadRequest.class,
                () -> algorithmJudgeAppService.submit(
                        new AlgorithmJudgeCommands.SubmitCommand(
                                question.getId(), ProgrammingLanguage.JAVA, "public class Main {}")));
    }

    @Test
    @DisplayName("getJob: 应返回 pending 任务且无用例结果")
    void getJob_pending_shouldReturnEmptyCaseResults() {
        User user = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestion question = createAlgorithmQuestion(time);
        loginAs(user);
        AlgorithmJudgeResult.SubmitResult submitResult = algorithmJudgeAppService.run(
                new AlgorithmJudgeCommands.RunCommand(
                        question.getId(), ProgrammingLanguage.CPP, "int main() { return 0; }", null, null));

        AlgorithmJudgeResult.PollResult result = algorithmJudgeAppService.getJob(submitResult.judgeJobId());

        assertNotNull(result);
        assertEquals(submitResult.judgeJobId(), result.judgeJobId());
        assertEquals(JudgeJobStatus.PENDING, result.status());
        assertTrue(result.caseResults().isEmpty());
        assertNull(result.judgement());
    }

    @Test
    @DisplayName("getJob: 正式任务成功时应过滤 AC 用例仅返回失败用例")
    void getJob_succeededFormal_shouldFilterAcCases() {
        User user = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestion question = createAlgorithmQuestion(time);
        AssessmentAnswer answer = AssessmentAnswer.create(
                user.getId(),
                question.getId(),
                "int main() { return 0; }",
                ProgrammingLanguage.CPP,
                null);
        assessmentAnswerRepository.save(answer);

        AlgorithmJudgeJob job = AlgorithmJudgeJob.create(
                answer.getId(),
                question.getId(),
                time.getId(),
                user.getId(),
                ProgrammingLanguage.CPP,
                "int main() { return 0; }",
                AlgorithmTestcaseType.FORMAL,
                null);
        job.markSucceeded();
        algorithmJudgeJobRepository.save(job);

        AlgorithmJudgeCaseResult acResult = AssessmentFixture.algorithmJudgeCaseResultBuilder()
                .judgeJob(job)
                .caseNo(1)
                .status(JudgeCaseStatus.AC)
                .visibleToCandidate(true)
                .save(algorithmJudgeCaseResultRepository);
        AlgorithmJudgeCaseResult waResult = AssessmentFixture.algorithmJudgeCaseResultBuilder()
                .judgeJob(job)
                .caseNo(2)
                .status(JudgeCaseStatus.WA)
                .visibleToCandidate(true)
                .save(algorithmJudgeCaseResultRepository);
        loginAs(user);

        AlgorithmJudgeResult.PollResult result = algorithmJudgeAppService.getJob(job.getId());

        assertEquals(JudgeJobStatus.SUCCEEDED, result.status());
        assertEquals(1, result.caseResults().size());
        assertEquals(waResult.getCaseNo(), result.caseResults().get(0).caseNo());
        assertEquals(JudgeCaseStatus.WA, result.caseResults().get(0).status());
    }

    @Test
    @DisplayName("getJob: 成功任务应返回关联评判信息")
    void getJob_succeededWithJudgement_shouldReturnJudgementInfo() {
        User user = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestion question = createAlgorithmQuestion(time);
        AssessmentAnswer answer = AssessmentAnswer.create(
                user.getId(),
                question.getId(),
                "int main() { return 0; }",
                ProgrammingLanguage.CPP,
                null);
        assessmentAnswerRepository.save(answer);

        AlgorithmJudgeJob job = AlgorithmJudgeJob.create(
                answer.getId(),
                question.getId(),
                time.getId(),
                user.getId(),
                ProgrammingLanguage.CPP,
                "int main() { return 0; }",
                AlgorithmTestcaseType.FORMAL,
                null);
        job.markSucceeded();
        algorithmJudgeJobRepository.save(job);

        AssessmentJudgement judgement = AssessmentJudgement.create(
                answer.getId(),
                question.getId(),
                time.getId(),
                user.getId(),
                new BigDecimal("100"),
                question.getScore(),
                JudgementStatus.JUDGED,
                ObjectiveResultCode.AC,
                JudgementSource.AUTO,
                null,
                ReviewerType.SYSTEM,
                LocalDateTime.now());
        assessmentJudgementRepository.save(judgement);
        loginAs(user);

        AlgorithmJudgeResult.PollResult result = algorithmJudgeAppService.getJob(job.getId());

        assertNotNull(result.judgement());
        assertEquals(judgement.getId(), result.judgement().id());
        assertEquals(answer.getId(), result.judgement().answerId());
    }

    @Test
    @DisplayName("getJob: 任务不存在应抛 DataNotFound")
    void getJob_notFound_shouldThrowDataNotFound() {
        User user = createCandidate(Direction.COMPUTER_VISION, 2024);
        loginAs(user);

        assertThrows(DataNotFound.class, () -> algorithmJudgeAppService.getJob(-1L));
    }

    @Test
    @DisplayName("getJob: 不能查看其他用户的判题任务")
    void getJob_otherUser_shouldThrowForbidden() {
        User owner = createCandidate(Direction.COMPUTER_VISION, 2024);
        User other = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestion question = createAlgorithmQuestion(time);
        loginAs(owner);
        AlgorithmJudgeResult.SubmitResult submitResult = algorithmJudgeAppService.run(
                new AlgorithmJudgeCommands.RunCommand(
                        question.getId(), ProgrammingLanguage.CPP, "int main() { return 0; }", null, null));
        loginAs(other);

        assertThrows(Forbidden.class, () -> algorithmJudgeAppService.getJob(submitResult.judgeJobId()));
    }
}
