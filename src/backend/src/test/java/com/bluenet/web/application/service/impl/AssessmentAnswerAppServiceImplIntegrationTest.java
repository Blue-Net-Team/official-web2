package com.bluenet.web.application.service.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.application.command.assessment_answer.AssessmentAnswerCommands;
import com.bluenet.web.application.result.assessment.AssessmentAnswerResult;
import com.bluenet.web.application.service.AssessmentAnswerAppService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataConflict;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.model.entity.AssessmentAnswer;
import com.bluenet.web.domain.model.entity.AssessmentQuestion;
import com.bluenet.web.domain.model.entity.AssessmentTeam;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.ProgrammingLanguage;
import com.bluenet.web.domain.model.enumerate.QuestionType;
import com.bluenet.web.domain.repository.AssessmentAnswerRepository;
import com.bluenet.web.domain.repository.AssessmentQuestionRepository;
import com.bluenet.web.domain.repository.AssessmentTeamRepository;
import com.bluenet.web.domain.repository.AssessmentTimeRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.domain.service.AssessmentAnswerDomainService;
import com.bluenet.web.testsupport.fixture.AssessmentFixture;
import com.bluenet.web.testsupport.fixture.TimeFixture;
import com.bluenet.web.testsupport.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

/**
 * AssessmentAnswerAppServiceImpl 集成测试。
 *
 * <p>
 * 验证考核答案应用服务的提交、更新、查询及与领域服务的协作逻辑。 因
 * {@link com.bluenet.web.domain.service.impl.AssessmentAnswerDomainServiceImpl}
 * 未加 {@code @Transactional}，本测试通过 {@link MockitoBean} 将其替换为 mock，
 * 仅验证应用层对仓储与领域服务的编排行为。
 * </p>
 */
@DisplayName("AssessmentAnswerAppServiceImpl 集成测试")
class AssessmentAnswerAppServiceImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AssessmentAnswerAppService assessmentAnswerAppService;

    @Autowired
    private AssessmentAnswerRepository assessmentAnswerRepository;

    @Autowired
    private AssessmentQuestionRepository assessmentQuestionRepository;

    @Autowired
    private AssessmentTimeRepository assessmentTimeRepository;

    @Autowired
    private AssessmentTeamRepository assessmentTeamRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private AssessmentAnswerDomainService assessmentAnswerDomainService;

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

    private AssessmentTime createTeamAllowedTime(Direction direction, Integer grade) {
        return AssessmentFixture.timeBuilder()
                .direction(direction)
                .grade(grade)
                .withinNow()
                .allowTeam()
                .save(assessmentTimeRepository);
    }

    private AssessmentAnswer buildAnswer(User user, AssessmentQuestion question, String content,
            ProgrammingLanguage language, Long fileId, Long teamId) {
        return AssessmentAnswer.create(user.getId(), question.getId(), content, language, fileId, teamId);
    }

    private List<AssessmentAnswer> buildMemberAnswers(AssessmentAnswer leaderAnswer, AssessmentQuestion question,
            String content, ProgrammingLanguage language, Long fileId, Long... memberUserIds) {
        List<AssessmentAnswer> answers = new ArrayList<>();
        for (Long memberUserId : memberUserIds) {
            answers.add(
                    AssessmentAnswer.create(
                            memberUserId,
                            question.getId(),
                            content,
                            language,
                            fileId,
                            leaderAnswer.getTeamId()));
        }
        return answers;
    }

    private void stubPrepareAnswer(User user, AssessmentQuestion question, String content,
            ProgrammingLanguage language, Long fileId, Long teamId) {
        AssessmentAnswer answer = buildAnswer(user, question, content, language, fileId, teamId);
        when(
                assessmentAnswerDomainService.prepareAnswer(
                        ArgumentMatchers.any(User.class),
                        ArgumentMatchers.any(AssessmentQuestion.class),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.any()))
                                .thenReturn(answer);
    }

    private void stubPrepareAnswerThenThrow(RuntimeException exception) {
        when(
                assessmentAnswerDomainService.prepareAnswer(
                        ArgumentMatchers.any(User.class),
                        ArgumentMatchers.any(AssessmentQuestion.class),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.any()))
                                .thenThrow(exception);
    }

    private void stubPrepareAnswerReturnThenThrow(AssessmentAnswer answer, RuntimeException exception) {
        when(
                assessmentAnswerDomainService.prepareAnswer(
                        ArgumentMatchers.any(User.class),
                        ArgumentMatchers.any(AssessmentQuestion.class),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.any()))
                                .thenReturn(answer)
                                .thenThrow(exception);
    }

    private void stubPrepareTeamMemberAnswers(AssessmentAnswer leaderAnswer, AssessmentQuestion question,
            String content, ProgrammingLanguage language, Long fileId, Long... memberUserIds) {
        List<AssessmentAnswer> answers = buildMemberAnswers(
                leaderAnswer,
                question,
                content,
                language,
                fileId,
                memberUserIds);
        when(
                assessmentAnswerDomainService.prepareTeamMemberAnswers(
                        ArgumentMatchers.any(AssessmentAnswer.class),
                        ArgumentMatchers.any(AssessmentQuestion.class),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.any()))
                                .thenReturn(answers);
    }

    private void stubPrepareTeamMemberAnswersEmpty() {
        when(
                assessmentAnswerDomainService.prepareTeamMemberAnswers(
                        ArgumentMatchers.any(AssessmentAnswer.class),
                        ArgumentMatchers.any(AssessmentQuestion.class),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.any()))
                                .thenReturn(Collections.emptyList());
    }

    private void stubPrepareObjectiveJudgementNull() {
        when(
                assessmentAnswerDomainService.prepareObjectiveJudgement(
                        ArgumentMatchers.any(AssessmentAnswer.class),
                        ArgumentMatchers.any(AssessmentQuestion.class)))
                                .thenReturn(null);
    }

    private void stubPrepareUpdatedAnswers(User user, AssessmentQuestion question, String content,
            ProgrammingLanguage language, Long fileId) {
        AssessmentAnswer existingAnswer = assessmentAnswerRepository
                .findByUserIdAndQuestionId(user.getId(), question.getId())
                .orElseThrow();
        List<AssessmentAnswer> answers = new ArrayList<>();
        answers.add(
                AssessmentAnswer.reconstruct(
                        existingAnswer.getId(),
                        existingAnswer.getUserId(),
                        existingAnswer.getQuestionId(),
                        content,
                        language,
                        fileId,
                        existingAnswer.getSubmitTime(),
                        existingAnswer.getTeamId()));
        if (existingAnswer.getTeamId() != null) {
            List<AssessmentAnswer> teamAnswers = assessmentAnswerRepository
                    .findByTeamIdAndQuestionId(existingAnswer.getTeamId(), question.getId());
            for (AssessmentAnswer teamAnswer : teamAnswers) {
                if (teamAnswer.getUserId().equals(user.getId())) {
                    continue;
                }
                answers.add(
                        AssessmentAnswer.reconstruct(
                                teamAnswer.getId(),
                                teamAnswer.getUserId(),
                                teamAnswer.getQuestionId(),
                                content,
                                language,
                                fileId,
                                teamAnswer.getSubmitTime(),
                                teamAnswer.getTeamId()));
            }
        }
        when(
                assessmentAnswerDomainService.prepareUpdatedAnswers(
                        ArgumentMatchers.any(User.class),
                        ArgumentMatchers.any(AssessmentQuestion.class),
                        ArgumentMatchers.any(AssessmentAnswer.class),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.any()))
                                .thenReturn(answers);
    }

    @Test
    @DisplayName("createAnswer: 文件上传题队长应能提交答案")
    void createAnswer_fileUploadLeader_shouldCreate() {
        User leader = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTeamAllowedTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTime(time)
                .save(assessmentQuestionRepository);
        AssessmentFixture.teamBuilder()
                .assessmentTime(time)
                .leader(leader)
                .save(assessmentTeamRepository);

        stubPrepareAnswer(leader, question, "答案", ProgrammingLanguage.CPP, null, 1L);
        stubPrepareTeamMemberAnswersEmpty();
        stubPrepareObjectiveJudgementNull();

        AssessmentAnswerResult result = assessmentAnswerAppService.createAnswer(
                new AssessmentAnswerCommands.CreateAssessmentAnswerCommand(
                        leader.getId(), question.getId(), "答案", ProgrammingLanguage.CPP, null));

        assertNotNull(result);
        assertEquals(question.getId(), result.questionId());
        assertEquals("答案", result.content());
    }

    @Test
    @DisplayName("createAnswer: 单选题应自动评判")
    void createAnswer_singleChoice_shouldJudge() {
        User user = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTime(time)
                .singleChoice("A", "A", "B", "C")
                .save(assessmentQuestionRepository);

        stubPrepareAnswer(user, question, "A", ProgrammingLanguage.CPP, null, null);
        stubPrepareTeamMemberAnswersEmpty();
        stubPrepareObjectiveJudgementNull();

        AssessmentAnswerResult result = assessmentAnswerAppService.createAnswer(
                new AssessmentAnswerCommands.CreateAssessmentAnswerCommand(
                        user.getId(), question.getId(), "A", ProgrammingLanguage.CPP, null));

        assertNotNull(result);
        // 选择题返回时会抹除评判结果
        assertNull(result.judgement());
        assertTrue(assessmentAnswerRepository.existsByUserIdAndQuestionId(user.getId(), question.getId()));
    }

    @Test
    @DisplayName("createAnswer: 题目不存在应抛 DataNotFound")
    void createAnswer_questionNotFound_shouldThrow() {
        User user = createCandidate(Direction.COMPUTER_VISION, 2024);

        assertThrows(
                DataNotFound.class,
                () -> assessmentAnswerAppService.createAnswer(
                        new AssessmentAnswerCommands.CreateAssessmentAnswerCommand(
                                user.getId(), -1L, "答案", ProgrammingLanguage.CPP, null)));
    }

    @Test
    @DisplayName("createAnswer: 重复提交应抛 DataConflict")
    void createAnswer_duplicate_shouldThrowDataConflict() {
        User user = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTime(time)
                .save(assessmentQuestionRepository);

        AssessmentAnswer answer = buildAnswer(user, question, "答案", ProgrammingLanguage.CPP, null, null);
        stubPrepareAnswerReturnThenThrow(answer, new DataConflict("已经提交过该题目的答案"));
        stubPrepareTeamMemberAnswersEmpty();
        stubPrepareObjectiveJudgementNull();

        assessmentAnswerAppService.createAnswer(
                new AssessmentAnswerCommands.CreateAssessmentAnswerCommand(
                        user.getId(), question.getId(), "答案", ProgrammingLanguage.CPP, null));

        assertThrows(
                DataConflict.class,
                () -> assessmentAnswerAppService.createAnswer(
                        new AssessmentAnswerCommands.CreateAssessmentAnswerCommand(
                                user.getId(), question.getId(), "答案2", ProgrammingLanguage.CPP, null)));
    }

    @Test
    @DisplayName("createAnswer: 考核结束后不能提交")
    void createAnswer_afterEnd_shouldThrowBadRequest() {
        User user = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = AssessmentFixture.timeBuilder()
                .direction(Direction.COMPUTER_VISION)
                .grade(2024)
                .startTime(TimeFixture.minusMinutes(60))
                .endTime(TimeFixture.minusMinutes(5))
                .save(assessmentTimeRepository);
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTime(time)
                .save(assessmentQuestionRepository);

        stubPrepareAnswerThenThrow(new BadRequest("考核时间已结束，无法提交答案"));

        assertThrows(
                BadRequest.class,
                () -> assessmentAnswerAppService.createAnswer(
                        new AssessmentAnswerCommands.CreateAssessmentAnswerCommand(
                                user.getId(), question.getId(), "答案", ProgrammingLanguage.CPP, null)));
    }

    @Test
    @DisplayName("createAnswer: 文件上传题非队长不能提交")
    void createAnswer_fileUploadNonLeader_shouldThrowForbidden() {
        User leader = createCandidate(Direction.COMPUTER_VISION, 2024);
        User member = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTeamAllowedTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTime(time)
                .save(assessmentQuestionRepository);
        AssessmentTeam team = AssessmentFixture.teamBuilder()
                .assessmentTime(time)
                .leader(leader)
                .save(assessmentTeamRepository);
        assessmentTeamRepository.addMember(team.getId(), member.getId());

        stubPrepareAnswerThenThrow(new Forbidden("只有队长可以提交文件上传题的答案"));

        assertThrows(
                Forbidden.class,
                () -> assessmentAnswerAppService.createAnswer(
                        new AssessmentAnswerCommands.CreateAssessmentAnswerCommand(
                                member.getId(), question.getId(), "答案", ProgrammingLanguage.CPP, null)));
    }

    @Test
    @DisplayName("createAnswer: 队长提交后应为组员同步创建答案")
    void createAnswer_teamLeader_shouldCreateAnswersForMembers() {
        User leader = createCandidate(Direction.COMPUTER_VISION, 2024);
        User member = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTeamAllowedTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTime(time)
                .save(assessmentQuestionRepository);
        AssessmentTeam team = AssessmentFixture.teamBuilder()
                .assessmentTime(time)
                .leader(leader)
                .save(assessmentTeamRepository);
        assessmentTeamRepository.addMember(team.getId(), member.getId());

        stubPrepareAnswer(leader, question, "队伍答案", ProgrammingLanguage.CPP, null, team.getId());
        AssessmentAnswer leaderAnswer = buildAnswer(
                leader,
                question,
                "队伍答案",
                ProgrammingLanguage.CPP,
                null,
                team.getId());
        stubPrepareTeamMemberAnswers(leaderAnswer, question, "队伍答案", ProgrammingLanguage.CPP, null, member.getId());
        stubPrepareObjectiveJudgementNull();

        assessmentAnswerAppService.createAnswer(
                new AssessmentAnswerCommands.CreateAssessmentAnswerCommand(
                        leader.getId(), question.getId(), "队伍答案", ProgrammingLanguage.CPP, null));

        assertTrue(assessmentAnswerRepository.existsByUserIdAndQuestionId(member.getId(), question.getId()));
    }

    @Test
    @DisplayName("updateAnswer: 应更新个人答案")
    void updateAnswer_shouldUpdate() {
        User user = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTime(time)
                .save(assessmentQuestionRepository);

        stubPrepareAnswer(user, question, "旧答案", ProgrammingLanguage.CPP, null, null);
        stubPrepareTeamMemberAnswersEmpty();
        stubPrepareObjectiveJudgementNull();

        assessmentAnswerAppService.createAnswer(
                new AssessmentAnswerCommands.CreateAssessmentAnswerCommand(
                        user.getId(), question.getId(), "旧答案", ProgrammingLanguage.CPP, null));

        stubPrepareUpdatedAnswers(user, question, "新答案", ProgrammingLanguage.JAVA, null);
        stubPrepareObjectiveJudgementNull();

        AssessmentAnswerResult result = assessmentAnswerAppService.updateAnswer(
                new AssessmentAnswerCommands.UpdateAssessmentAnswerCommand(
                        user.getId(), question.getId(), "新答案", ProgrammingLanguage.JAVA, null));

        assertEquals("新答案", result.content());
        assertEquals(ProgrammingLanguage.JAVA, result.language());
    }

    @Test
    @DisplayName("updateAnswer: 应同步更新队伍成员答案")
    void updateAnswer_team_shouldSyncMembers() {
        User leader = createCandidate(Direction.COMPUTER_VISION, 2024);
        User member = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTeamAllowedTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTime(time)
                .save(assessmentQuestionRepository);
        AssessmentTeam team = AssessmentFixture.teamBuilder()
                .assessmentTime(time)
                .leader(leader)
                .save(assessmentTeamRepository);
        assessmentTeamRepository.addMember(team.getId(), member.getId());

        stubPrepareAnswer(leader, question, "旧答案", ProgrammingLanguage.CPP, null, team.getId());
        AssessmentAnswer leaderAnswer = buildAnswer(
                leader,
                question,
                "旧答案",
                ProgrammingLanguage.CPP,
                null,
                team.getId());
        stubPrepareTeamMemberAnswers(leaderAnswer, question, "旧答案", ProgrammingLanguage.CPP, null, member.getId());
        stubPrepareObjectiveJudgementNull();

        assessmentAnswerAppService.createAnswer(
                new AssessmentAnswerCommands.CreateAssessmentAnswerCommand(
                        leader.getId(), question.getId(), "旧答案", ProgrammingLanguage.CPP, null));

        stubPrepareUpdatedAnswers(leader, question, "新答案", ProgrammingLanguage.JAVA, null);
        stubPrepareObjectiveJudgementNull();

        assessmentAnswerAppService.updateAnswer(
                new AssessmentAnswerCommands.UpdateAssessmentAnswerCommand(
                        leader.getId(), question.getId(), "新答案", ProgrammingLanguage.JAVA, null));

        AssessmentAnswer memberAnswer = assessmentAnswerRepository
                .findByUserIdAndQuestionId(member.getId(), question.getId())
                .orElseThrow();
        assertEquals("新答案", memberAnswer.getContent());
        assertEquals(ProgrammingLanguage.JAVA, memberAnswer.getLanguage());
    }

    @Test
    @DisplayName("updateAnswer: 未提交过答案应抛 BadRequest")
    void updateAnswer_notSubmitted_shouldThrowBadRequest() {
        User user = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTime(time)
                .save(assessmentQuestionRepository);

        assertThrows(
                BadRequest.class,
                () -> assessmentAnswerAppService.updateAnswer(
                        new AssessmentAnswerCommands.UpdateAssessmentAnswerCommand(
                                user.getId(), question.getId(), "答案", ProgrammingLanguage.CPP, null)));
    }

    @Test
    @DisplayName("getMyAnswer: 应返回自己的答案")
    void getMyAnswer_shouldReturnAnswer() {
        User user = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTime(time)
                .save(assessmentQuestionRepository);

        stubPrepareAnswer(user, question, "我的答案", ProgrammingLanguage.CPP, null, null);
        stubPrepareTeamMemberAnswersEmpty();
        stubPrepareObjectiveJudgementNull();

        assessmentAnswerAppService.createAnswer(
                new AssessmentAnswerCommands.CreateAssessmentAnswerCommand(
                        user.getId(), question.getId(), "我的答案", ProgrammingLanguage.CPP, null));

        AssessmentAnswerResult result = assessmentAnswerAppService.getMyAnswer(user.getId(), question.getId());

        assertNotNull(result);
        assertEquals("我的答案", result.content());
    }

    @Test
    @DisplayName("getMyAnswer: 队员应能看到队长的文件上传答案")
    void getMyAnswer_teamMember_shouldReturnLeaderAnswer() {
        User leader = createCandidate(Direction.COMPUTER_VISION, 2024);
        User member = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTeamAllowedTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTime(time)
                .type(QuestionType.FILE_UPLOAD)
                .save(assessmentQuestionRepository);
        AssessmentTeam team = AssessmentFixture.teamBuilder()
                .assessmentTime(time)
                .leader(leader)
                .save(assessmentTeamRepository);
        assessmentTeamRepository.addMember(team.getId(), member.getId());

        stubPrepareAnswer(leader, question, "队长答案", ProgrammingLanguage.CPP, null, team.getId());
        stubPrepareTeamMemberAnswersEmpty();
        stubPrepareObjectiveJudgementNull();

        assessmentAnswerAppService.createAnswer(
                new AssessmentAnswerCommands.CreateAssessmentAnswerCommand(
                        leader.getId(), question.getId(), "队长答案", ProgrammingLanguage.CPP, null));

        AssessmentAnswerResult result = assessmentAnswerAppService.getMyAnswer(member.getId(), question.getId());

        assertNotNull(result);
        assertEquals("队长答案", result.content());
    }

    @Test
    @DisplayName("getMyAnswer: 无答案时应返回 null")
    void getMyAnswer_noAnswer_shouldReturnNull() {
        User user = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTime(time)
                .save(assessmentQuestionRepository);

        AssessmentAnswerResult result = assessmentAnswerAppService.getMyAnswer(user.getId(), question.getId());

        assertNull(result);
    }
}
