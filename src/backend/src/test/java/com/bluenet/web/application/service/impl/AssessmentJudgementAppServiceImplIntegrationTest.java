package com.bluenet.web.application.service.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.application.command.assessment_judgement.AssessmentJudgementCommands;
import com.bluenet.web.application.result.assessment.AssessmentCandidateScoreboard;
import com.bluenet.web.application.result.assessment.AssessmentDecisionResult;
import com.bluenet.web.application.result.assessment.AssessmentDecisionWorkspace;
import com.bluenet.web.application.result.assessment.AssessmentJudgementResult;
import com.bluenet.web.application.result.assessment.AssessmentQuestionScoreboard;
import com.bluenet.web.application.service.AssessmentJudgementAppService;
import com.bluenet.web.application.service.assessment.AssessmentDecisionPublicationService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.model.entity.AssessmentAnswer;
import com.bluenet.web.domain.model.entity.AssessmentDecision;
import com.bluenet.web.domain.model.entity.AssessmentJudgement;
import com.bluenet.web.domain.model.entity.AssessmentQuestion;
import com.bluenet.web.domain.model.entity.AssessmentTeam;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.entity.Comment;
import com.bluenet.web.domain.model.entity.Enroll;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.Gender;
import com.bluenet.web.domain.model.enumerate.JudgementSource;
import com.bluenet.web.domain.model.enumerate.JudgementStatus;
import com.bluenet.web.domain.model.enumerate.ObjectiveResultCode;
import com.bluenet.web.domain.model.enumerate.QuestionType;
import com.bluenet.web.domain.model.enumerate.ReviewerType;
import com.bluenet.web.domain.model.readmodel.AssessmentQuestionSubmissionReadModel;
import com.bluenet.web.domain.repository.AssessmentAnswerRepository;
import com.bluenet.web.domain.repository.AssessmentDecisionRepository;
import com.bluenet.web.domain.repository.AssessmentJudgementRepository;
import com.bluenet.web.domain.repository.AssessmentQuestionRepository;
import com.bluenet.web.domain.repository.AssessmentTeamRepository;
import com.bluenet.web.domain.repository.AssessmentTimeRepository;
import com.bluenet.web.domain.repository.CommentRepository;
import com.bluenet.web.domain.repository.EnrollRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.domain.service.AssessmentDecisionDomainService;
import com.bluenet.web.infrastructure.security.principal.RoleTypeResolver;
import com.bluenet.web.infrastructure.security.principal.SecurityPrincipal;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.bluenet.web.testsupport.fixture.AssessmentFixture;
import com.bluenet.web.testsupport.fixture.UserFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * AssessmentJudgementAppServiceImpl 集成测试。
 *
 * <p>
 * 验证考核评判应用服务的查询、最终评分确认、录用决策及发布逻辑。
 * </p>
 */
@DisplayName("AssessmentJudgementAppServiceImpl 集成测试")
class AssessmentJudgementAppServiceImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AssessmentJudgementAppService assessmentJudgementAppService;

    @Autowired
    private AssessmentJudgementRepository assessmentJudgementRepository;

    @Autowired
    private AssessmentAnswerRepository assessmentAnswerRepository;

    @Autowired
    private AssessmentQuestionRepository assessmentQuestionRepository;

    @Autowired
    private AssessmentTimeRepository assessmentTimeRepository;

    @Autowired
    private AssessmentDecisionRepository assessmentDecisionRepository;

    @Autowired
    private AssessmentTeamRepository assessmentTeamRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EnrollRepository enrollRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleTypeResolver roleTypeResolver;

    @MockitoBean
    private AssessmentDecisionPublicationService publicationService;

    @MockitoBean
    private AssessmentDecisionDomainService assessmentDecisionDomainService;

    private long sequence = 1000;

    private String nextStudentId(String prefix) {
        return prefix + (++sequence);
    }

    private User createDirectionAdmin(Direction direction) {
        return UserFixture.directionAdmin(nextStudentId("DA"), direction).save(userRepository, passwordEncoder);
    }

    private User createMember(Direction direction) {
        return UserFixture.member(nextStudentId("SM"))
                .withDirection(direction)
                .save(userRepository, passwordEncoder);
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

    private void loginAs(User user) {
        SecurityPrincipal principal = new SecurityPrincipal(
                user,
                roleTypeResolver.resolve(user.getRoleId()),
                Collections.emptySet());
        UserCTX.setPrincipal(principal);
    }

    @AfterEach
    void cleanup() {
        UserCTX.clear();
    }

    @Test
    @DisplayName("getLatestByAnswerId: 应返回最新评判")
    void getLatestByAnswerId_shouldReturnLatest() {
        User user = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTime(time)
                .save(assessmentQuestionRepository);
        AssessmentAnswer answer = AssessmentFixture.answerBuilder()
                .user(user)
                .question(question)
                .save(assessmentAnswerRepository);
        AssessmentJudgement judgement = AssessmentFixture.judgementBuilder()
                .answer(answer)
                .question(question)
                .score(new BigDecimal("80"), question.getScore())
                .save(assessmentJudgementRepository);

        AssessmentJudgementResult result = assessmentJudgementAppService.getLatestByAnswerId(answer.getId());

        assertEquals(judgement.getId(), result.id());
        assertEquals(new BigDecimal("80.00"), result.score());
    }

    @Test
    @DisplayName("getLatestByAnswerId: 无评判记录应抛 DataNotFound")
    void getLatestByAnswerId_noJudgement_shouldThrow() {
        User user = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTime(time)
                .save(assessmentQuestionRepository);
        AssessmentAnswer answer = AssessmentFixture.answerBuilder()
                .user(user)
                .question(question)
                .save(assessmentAnswerRepository);

        assertThrows(DataNotFound.class, () -> assessmentJudgementAppService.getLatestByAnswerId(answer.getId()));
    }

    @Test
    @DisplayName("listByQuestionId: 应返回题目的所有评判")
    void listByQuestionId_shouldReturnJudgements() {
        User user = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTime(time)
                .save(assessmentQuestionRepository);
        AssessmentAnswer answer = AssessmentFixture.answerBuilder()
                .user(user)
                .question(question)
                .save(assessmentAnswerRepository);
        AssessmentFixture.judgementBuilder().answer(answer).question(question).save(assessmentJudgementRepository);

        List<AssessmentJudgementResult> result = assessmentJudgementAppService.listByQuestionId(question.getId());

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("finalizeScore: 文件上传题确认最终评分应创建评判")
    void finalizeScore_fileUpload_shouldCreateJudgement() {
        User admin = createDirectionAdmin(Direction.COMPUTER_VISION);
        User candidate = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTime(time)
                .type(QuestionType.FILE_UPLOAD)
                .score(new BigDecimal("100"))
                .save(assessmentQuestionRepository);
        AssessmentAnswer answer = AssessmentFixture.answerBuilder()
                .user(candidate)
                .question(question)
                .save(assessmentAnswerRepository);
        Comment comment = Comment.create(answer.getId(), admin.getId(), "评论", new BigDecimal("80"), false);
        commentRepository.save(comment);
        loginAs(admin);

        AssessmentJudgementResult result = assessmentJudgementAppService.finalizeScore(
                new AssessmentJudgementCommands.FinalizeScoreCommand(answer.getId(), new BigDecimal("85")));

        assertNotNull(result);
        assertEquals(new BigDecimal("85.00"), result.score());
        assertEquals(JudgementSource.ADMIN_FINALIZED, result.source());
    }

    @Test
    @DisplayName("finalizeScore: 未评论前不能确认最终评分")
    void finalizeScore_withoutComment_shouldThrowBadRequest() {
        User admin = createDirectionAdmin(Direction.COMPUTER_VISION);
        User candidate = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTime(time)
                .type(QuestionType.FILE_UPLOAD)
                .save(assessmentQuestionRepository);
        AssessmentAnswer answer = AssessmentFixture.answerBuilder()
                .user(candidate)
                .question(question)
                .save(assessmentAnswerRepository);
        loginAs(admin);

        assertThrows(
                BadRequest.class,
                () -> assessmentJudgementAppService.finalizeScore(
                        new AssessmentJudgementCommands.FinalizeScoreCommand(answer.getId(), new BigDecimal("80"))));
    }

    @Test
    @DisplayName("finalizeScore: 非文件上传题不能确认最终评分")
    void finalizeScore_nonFileUpload_shouldThrowBadRequest() {
        User admin = createDirectionAdmin(Direction.COMPUTER_VISION);
        User candidate = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTime(time)
                .singleChoice("A", "A", "B")
                .save(assessmentQuestionRepository);
        AssessmentAnswer answer = AssessmentFixture.answerBuilder()
                .user(candidate)
                .question(question)
                .save(assessmentAnswerRepository);
        Comment comment = Comment.create(answer.getId(), admin.getId(), "评论", new BigDecimal("80"), false);
        commentRepository.save(comment);
        loginAs(admin);

        assertThrows(
                BadRequest.class,
                () -> assessmentJudgementAppService.finalizeScore(
                        new AssessmentJudgementCommands.FinalizeScoreCommand(answer.getId(), new BigDecimal("80"))));
    }

    @Test
    @DisplayName("finalizeScore: 分数超出范围应抛 BadRequest")
    void finalizeScore_scoreOutOfRange_shouldThrowBadRequest() {
        User admin = createDirectionAdmin(Direction.COMPUTER_VISION);
        User candidate = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTime(time)
                .type(QuestionType.FILE_UPLOAD)
                .score(new BigDecimal("100"))
                .save(assessmentQuestionRepository);
        AssessmentAnswer answer = AssessmentFixture.answerBuilder()
                .user(candidate)
                .question(question)
                .save(assessmentAnswerRepository);
        Comment comment = Comment.create(answer.getId(), admin.getId(), "评论", new BigDecimal("80"), false);
        commentRepository.save(comment);
        loginAs(admin);

        assertThrows(
                BadRequest.class,
                () -> assessmentJudgementAppService.finalizeScore(
                        new AssessmentJudgementCommands.FinalizeScoreCommand(answer.getId(), new BigDecimal("120"))));
    }

    @Test
    @DisplayName("finalizeScore: 普通成员不能确认最终评分")
    void finalizeScore_member_shouldThrowForbidden() {
        User member = createMember(Direction.COMPUTER_VISION);
        User candidate = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTime(time)
                .type(QuestionType.FILE_UPLOAD)
                .save(assessmentQuestionRepository);
        AssessmentAnswer answer = AssessmentFixture.answerBuilder()
                .user(candidate)
                .question(question)
                .save(assessmentAnswerRepository);
        Comment comment = Comment.create(answer.getId(), member.getId(), "评论", new BigDecimal("80"), false);
        commentRepository.save(comment);
        loginAs(member);

        assertThrows(
                Forbidden.class,
                () -> assessmentJudgementAppService.finalizeScore(
                        new AssessmentJudgementCommands.FinalizeScoreCommand(answer.getId(), new BigDecimal("80"))));
    }

    @Test
    @DisplayName("decideAssessment: 方向管理员应能设置通过决策")
    void decideAssessment_shouldCreateDecision() {
        User admin = createDirectionAdmin(Direction.COMPUTER_VISION);
        User candidate = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        loginAs(admin);

        AssessmentDecisionResult result = assessmentJudgementAppService.decideAssessment(
                new AssessmentJudgementCommands.DecideAssessmentCommand(
                        candidate.getId(), time.getId(), true, "通过"));

        assertTrue(result.passed());
        AssessmentTime updated = assessmentTimeRepository.findById(time.getId()).orElseThrow();
        // 仅保存决策不应标记结果已发布，发布状态由 publishDecisions 设置
        assertFalse(updated.isResultsPublished());
    }

    @Test
    @DisplayName("decideAssessment: 普通成员不能设置决策")
    void decideAssessment_member_shouldThrowForbidden() {
        User member = createMember(Direction.COMPUTER_VISION);
        User candidate = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        loginAs(member);

        assertThrows(
                Forbidden.class,
                () -> assessmentJudgementAppService.decideAssessment(
                        new AssessmentJudgementCommands.DecideAssessmentCommand(
                                candidate.getId(), time.getId(), true, "通过")));
    }

    @Test
    @DisplayName("publishDecisions: 应调用发布服务发布决策")
    void publishDecisions_shouldCallPublicationService() {
        User admin = createDirectionAdmin(Direction.COMPUTER_VISION);
        User candidate = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentDecision decision = AssessmentDecision.create(
                candidate.getId(),
                time.getId(),
                true,
                admin.getId(),
                "通过");
        decision.decideNow();
        assessmentDecisionRepository.save(decision);
        loginAs(admin);

        int sentCount = assessmentJudgementAppService.publishDecisions(time.getId());

        assertEquals(1, sentCount);
        verify(publicationService).publish(any(AssessmentDecision.class), any(AssessmentTime.class));
        AssessmentTime updated = assessmentTimeRepository.findById(time.getId()).orElseThrow();
        assertTrue(updated.isResultsPublished());
    }

    @Test
    @DisplayName("finalizeScore: 队长首次评分应为全队批量创建同分评判")
    void finalizeScore_teamLeaderFirstTime_shouldPropagateToMembers() {
        User admin = createDirectionAdmin(Direction.COMPUTER_VISION);
        User leader = createCandidate(Direction.COMPUTER_VISION, 2024);
        User member = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTeamAllowedTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTime(time)
                .type(QuestionType.FILE_UPLOAD)
                .score(new BigDecimal("100"))
                .save(assessmentQuestionRepository);
        AssessmentTeam team = AssessmentFixture.teamBuilder()
                .assessmentTime(time)
                .leader(leader)
                .save(assessmentTeamRepository);
        assessmentTeamRepository.addMember(team.getId(), member.getId());
        AssessmentAnswer leaderAnswer = AssessmentFixture.answerBuilder()
                .user(leader)
                .question(question)
                .team(team)
                .save(assessmentAnswerRepository);
        AssessmentAnswer memberAnswer = AssessmentFixture.answerBuilder()
                .user(member)
                .question(question)
                .team(team)
                .save(assessmentAnswerRepository);
        Comment comment = Comment.create(leaderAnswer.getId(), admin.getId(), "评论", new BigDecimal("80"), false);
        commentRepository.save(comment);
        loginAs(admin);

        AssessmentJudgementResult result = assessmentJudgementAppService.finalizeScore(
                new AssessmentJudgementCommands.FinalizeScoreCommand(leaderAnswer.getId(), new BigDecimal("85")));

        assertEquals(new BigDecimal("85.00"), result.score());
        AssessmentJudgement leaderJudgement = assessmentJudgementRepository
                .findLatestByAnswerIdAndSource(leaderAnswer.getId(), JudgementSource.ADMIN_FINALIZED)
                .orElseThrow();
        assertEquals(new BigDecimal("85.00"), leaderJudgement.getScore());
        AssessmentJudgement memberJudgement = assessmentJudgementRepository
                .findLatestByAnswerIdAndSource(memberAnswer.getId(), JudgementSource.ADMIN_FINALIZED)
                .orElseThrow();
        assertEquals(new BigDecimal("85.00"), memberJudgement.getScore());
    }

    @Test
    @DisplayName("finalizeScore: 组队已有 ADMIN_FINALIZED 后再次评分只更新当前答案")
    void finalizeScore_teamReFinalized_shouldUpdateSingleAnswer() {
        User admin = createDirectionAdmin(Direction.COMPUTER_VISION);
        User leader = createCandidate(Direction.COMPUTER_VISION, 2024);
        User member = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTeamAllowedTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTime(time)
                .type(QuestionType.FILE_UPLOAD)
                .score(new BigDecimal("100"))
                .save(assessmentQuestionRepository);
        AssessmentTeam team = AssessmentFixture.teamBuilder()
                .assessmentTime(time)
                .leader(leader)
                .save(assessmentTeamRepository);
        assessmentTeamRepository.addMember(team.getId(), member.getId());
        AssessmentAnswer leaderAnswer = AssessmentFixture.answerBuilder()
                .user(leader)
                .question(question)
                .team(team)
                .save(assessmentAnswerRepository);
        AssessmentAnswer memberAnswer = AssessmentFixture.answerBuilder()
                .user(member)
                .question(question)
                .team(team)
                .save(assessmentAnswerRepository);
        Comment leaderComment = Comment.create(leaderAnswer.getId(), admin.getId(), "评论", new BigDecimal("80"), false);
        Comment memberComment = Comment.create(memberAnswer.getId(), admin.getId(), "评论", new BigDecimal("80"), false);
        commentRepository.save(leaderComment);
        commentRepository.save(memberComment);
        loginAs(admin);
        assessmentJudgementAppService.finalizeScore(
                new AssessmentJudgementCommands.FinalizeScoreCommand(leaderAnswer.getId(), new BigDecimal("85")));

        AssessmentJudgementResult result = assessmentJudgementAppService.finalizeScore(
                new AssessmentJudgementCommands.FinalizeScoreCommand(memberAnswer.getId(), new BigDecimal("90")));

        assertEquals(new BigDecimal("90.00"), result.score());
        AssessmentJudgement leaderJudgement = assessmentJudgementRepository
                .findLatestByAnswerIdAndSource(leaderAnswer.getId(), JudgementSource.ADMIN_FINALIZED)
                .orElseThrow();
        assertEquals(new BigDecimal("85.00"), leaderJudgement.getScore());
        AssessmentJudgement memberJudgement = assessmentJudgementRepository
                .findLatestByAnswerIdAndSource(memberAnswer.getId(), JudgementSource.ADMIN_FINALIZED)
                .orElseThrow();
        assertEquals(new BigDecimal("90.00"), memberJudgement.getScore());
    }

    @Test
    @DisplayName("finalizeScore: 非队长首次评分只更新自己答案")
    void finalizeScore_teamMember_shouldUpdateOwnAnswer() {
        User admin = createDirectionAdmin(Direction.COMPUTER_VISION);
        User leader = createCandidate(Direction.COMPUTER_VISION, 2024);
        User member = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTeamAllowedTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTime(time)
                .type(QuestionType.FILE_UPLOAD)
                .score(new BigDecimal("100"))
                .save(assessmentQuestionRepository);
        AssessmentTeam team = AssessmentFixture.teamBuilder()
                .assessmentTime(time)
                .leader(leader)
                .save(assessmentTeamRepository);
        assessmentTeamRepository.addMember(team.getId(), member.getId());
        AssessmentAnswer leaderAnswer = AssessmentFixture.answerBuilder()
                .user(leader)
                .question(question)
                .team(team)
                .save(assessmentAnswerRepository);
        AssessmentAnswer memberAnswer = AssessmentFixture.answerBuilder()
                .user(member)
                .question(question)
                .team(team)
                .save(assessmentAnswerRepository);
        Comment memberComment = Comment.create(memberAnswer.getId(), admin.getId(), "评论", new BigDecimal("80"), false);
        commentRepository.save(memberComment);
        loginAs(admin);

        AssessmentJudgementResult result = assessmentJudgementAppService.finalizeScore(
                new AssessmentJudgementCommands.FinalizeScoreCommand(memberAnswer.getId(), new BigDecimal("88")));

        assertEquals(new BigDecimal("88.00"), result.score());
        assertFalse(
                assessmentJudgementRepository
                        .findLatestByAnswerIdAndSource(leaderAnswer.getId(), JudgementSource.ADMIN_FINALIZED)
                        .isPresent());
        AssessmentJudgement memberJudgement = assessmentJudgementRepository
                .findLatestByAnswerIdAndSource(memberAnswer.getId(), JudgementSource.ADMIN_FINALIZED)
                .orElseThrow();
        assertEquals(new BigDecimal("88.00"), memberJudgement.getScore());
    }

    @Test
    @DisplayName("listQuestionSubmissions: 应返回提交及评判历史")
    void listQuestionSubmissions_shouldReturnWithHistories() {
        User member = createMember(Direction.COMPUTER_VISION);
        User candidate = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTime(time)
                .type(QuestionType.ALGORITHM)
                .score(new BigDecimal("100"))
                .save(assessmentQuestionRepository);
        AssessmentAnswer answer = AssessmentFixture.answerBuilder()
                .user(candidate)
                .question(question)
                .save(assessmentAnswerRepository);
        AssessmentJudgement first = AssessmentJudgement.create(
                answer.getId(),
                question.getId(),
                time.getId(),
                candidate.getId(),
                new BigDecimal("60"),
                question.getScore(),
                JudgementStatus.JUDGED,
                ObjectiveResultCode.WA,
                JudgementSource.AUTO,
                null,
                ReviewerType.SYSTEM,
                LocalDateTime.now().minusMinutes(5));
        AssessmentJudgement second = AssessmentJudgement.create(
                answer.getId(),
                question.getId(),
                time.getId(),
                candidate.getId(),
                new BigDecimal("100"),
                question.getScore(),
                JudgementStatus.JUDGED,
                ObjectiveResultCode.AC,
                JudgementSource.AUTO,
                null,
                ReviewerType.SYSTEM,
                LocalDateTime.now());
        assessmentJudgementRepository.save(first);
        assessmentJudgementRepository.save(second);
        loginAs(member);

        List<AssessmentQuestionSubmissionReadModel> result = assessmentJudgementAppService.listQuestionSubmissions(
                question.getId(),
                null,
                null);

        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getHistories().size());
    }

    @Test
    @DisplayName("listCandidateScoreboard: 应按考生聚合题目得分")
    void listCandidateScoreboard_shouldAggregateScores() {
        User member = createMember(Direction.COMPUTER_VISION);
        User candidate = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestion question1 = AssessmentFixture.questionBuilder()
                .assessmentTime(time)
                .questionNo(1)
                .singleChoice("A", "A", "B")
                .score(new BigDecimal("50"))
                .save(assessmentQuestionRepository);
        AssessmentQuestion question2 = AssessmentFixture.questionBuilder()
                .assessmentTime(time)
                .questionNo(2)
                .type(QuestionType.FILE_UPLOAD)
                .score(new BigDecimal("100"))
                .save(assessmentQuestionRepository);
        AssessmentAnswer answer1 = AssessmentFixture.answerBuilder()
                .user(candidate)
                .question(question1)
                .save(assessmentAnswerRepository);
        AssessmentAnswer answer2 = AssessmentFixture.answerBuilder()
                .user(candidate)
                .question(question2)
                .save(assessmentAnswerRepository);
        AssessmentJudgement judgement = AssessmentJudgement.create(
                answer1.getId(),
                question1.getId(),
                time.getId(),
                candidate.getId(),
                new BigDecimal("50"),
                question1.getScore(),
                JudgementStatus.JUDGED,
                ObjectiveResultCode.AC,
                JudgementSource.AUTO,
                null,
                ReviewerType.SYSTEM,
                LocalDateTime.now());
        assessmentJudgementRepository.save(judgement);
        loginAs(member);

        List<AssessmentCandidateScoreboard> result = assessmentJudgementAppService.listCandidateScoreboard(
                time.getId(),
                null);

        assertEquals(1, result.size());
        AssessmentCandidateScoreboard scoreboard = result.get(0);
        assertEquals(new BigDecimal("50.00"), scoreboard.getTotalScore());
        assertEquals(new BigDecimal("150.00"), scoreboard.getMaxScore());
        assertEquals(1L, scoreboard.getJudgedQuestionCount());
        assertEquals(1L, scoreboard.getPendingJudgementCount());
    }

    @Test
    @DisplayName("getDecisionWorkspace: 应过滤已被先前轮次淘汰的考生")
    void getDecisionWorkspace_shouldFilterEliminatedCandidates() {
        User admin = createDirectionAdmin(Direction.COMPUTER_VISION);
        User candidate = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTime(time)
                .singleChoice("A", "A", "B")
                .score(new BigDecimal("100"))
                .save(assessmentQuestionRepository);
        AssessmentAnswer answer = AssessmentFixture.answerBuilder()
                .user(candidate)
                .question(question)
                .save(assessmentAnswerRepository);
        AssessmentJudgement judgement = AssessmentJudgement.create(
                answer.getId(),
                question.getId(),
                time.getId(),
                candidate.getId(),
                new BigDecimal("100"),
                question.getScore(),
                JudgementStatus.JUDGED,
                ObjectiveResultCode.AC,
                JudgementSource.AUTO,
                null,
                ReviewerType.SYSTEM,
                LocalDateTime.now());
        assessmentJudgementRepository.save(judgement);
        when(assessmentDecisionDomainService.isEliminatedFromPriorEpoch(any(), any(AssessmentTime.class)))
                .thenReturn(true);
        loginAs(admin);

        AssessmentDecisionWorkspace workspace = assessmentJudgementAppService.getDecisionWorkspace(
                time.getId(),
                null,
                null);

        assertTrue(workspace.getCandidates().isEmpty());
        assertEquals(0L, workspace.getStatistics().getCandidates());
    }

    @Test
    @DisplayName("getDecisionWorkspace: 应按决策状态过滤候选人")
    void getDecisionWorkspace_shouldFilterByDecisionStatus() {
        User admin = createDirectionAdmin(Direction.COMPUTER_VISION);
        User passedCandidate = createCandidate(Direction.COMPUTER_VISION, 2024);
        User pendingCandidate = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTime(time)
                .singleChoice("A", "A", "B")
                .score(new BigDecimal("100"))
                .save(assessmentQuestionRepository);
        AssessmentAnswer passedAnswer = AssessmentFixture.answerBuilder()
                .user(passedCandidate)
                .question(question)
                .save(assessmentAnswerRepository);
        AssessmentAnswer pendingAnswer = AssessmentFixture.answerBuilder()
                .user(pendingCandidate)
                .question(question)
                .save(assessmentAnswerRepository);
        AssessmentJudgement judgement = AssessmentJudgement.create(
                passedAnswer.getId(),
                question.getId(),
                time.getId(),
                passedCandidate.getId(),
                new BigDecimal("100"),
                question.getScore(),
                JudgementStatus.JUDGED,
                ObjectiveResultCode.AC,
                JudgementSource.AUTO,
                null,
                ReviewerType.SYSTEM,
                LocalDateTime.now());
        assessmentJudgementRepository.save(judgement);
        AssessmentDecision decision = AssessmentDecision.create(
                passedCandidate.getId(),
                time.getId(),
                true,
                admin.getId(),
                "通过");
        decision.decideNow();
        assessmentDecisionRepository.save(decision);
        when(assessmentDecisionDomainService.isEliminatedFromPriorEpoch(any(), any(AssessmentTime.class)))
                .thenReturn(false);
        loginAs(admin);

        AssessmentDecisionWorkspace workspace = assessmentJudgementAppService.getDecisionWorkspace(
                time.getId(),
                null,
                "PASSED");

        assertEquals(1, workspace.getCandidates().size());
        assertEquals(passedCandidate.getId(), workspace.getCandidates().get(0).getCandidateUserId());
        assertTrue(workspace.getCandidates().get(0).getPassed());
    }

    @Test
    @DisplayName("decideAssessment: 应更新已有决策")
    void decideAssessment_shouldUpdateExistingDecision() {
        User admin = createDirectionAdmin(Direction.COMPUTER_VISION);
        User candidate = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        loginAs(admin);
        assessmentJudgementAppService.decideAssessment(
                new AssessmentJudgementCommands.DecideAssessmentCommand(
                        candidate.getId(), time.getId(), false, "淘汰"));

        AssessmentDecisionResult result = assessmentJudgementAppService.decideAssessment(
                new AssessmentJudgementCommands.DecideAssessmentCommand(
                        candidate.getId(), time.getId(), true, "通过"));

        assertTrue(result.passed());
        assertEquals("通过", result.decisionComment());
        AssessmentDecision updated = assessmentDecisionRepository
                .findByUserIdAndAssessmentTimeId(candidate.getId(), time.getId())
                .orElseThrow();
        assertTrue(updated.getPassed());
        assertEquals(admin.getId(), updated.getDecidedBy());
    }

    @Test
    @DisplayName("publishDecisions: 无决策时应返回 0")
    void publishDecisions_shouldReturnZeroWhenNoDecisions() {
        User admin = createDirectionAdmin(Direction.COMPUTER_VISION);
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        loginAs(admin);

        int sentCount = assessmentJudgementAppService.publishDecisions(time.getId());

        assertEquals(0, sentCount);
    }

    @Test
    @DisplayName("publishDecisions: 部分发布失败时应继续计数")
    void publishDecisions_shouldHandlePartialFailures() {
        User admin = createDirectionAdmin(Direction.COMPUTER_VISION);
        User candidate1 = createCandidate(Direction.COMPUTER_VISION, 2024);
        User candidate2 = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentDecision decision1 = AssessmentDecision.create(
                candidate1.getId(),
                time.getId(),
                true,
                admin.getId(),
                "通过");
        AssessmentDecision decision2 = AssessmentDecision.create(
                candidate2.getId(),
                time.getId(),
                true,
                admin.getId(),
                "通过");
        decision1.decideNow();
        decision2.decideNow();
        assessmentDecisionRepository.save(decision1);
        assessmentDecisionRepository.save(decision2);
        doThrow(new RuntimeException("发送失败"))
                .when(publicationService)
                .publish(argThat(d -> d.getUserId().equals(candidate1.getId())), any(AssessmentTime.class));
        loginAs(admin);

        int sentCount = assessmentJudgementAppService.publishDecisions(time.getId());

        assertEquals(1, sentCount);
        verify(publicationService, times(2)).publish(any(AssessmentDecision.class), any(AssessmentTime.class));
    }

    @Test
    @DisplayName("listQuestionScoreboard: 成员应能查看题目评分汇总")
    void listQuestionScoreboard_member_shouldReturnScoreboard() {
        User member = createMember(Direction.COMPUTER_VISION);
        User candidate = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTime(time)
                .singleChoice("A", "A", "B")
                .save(assessmentQuestionRepository);
        AssessmentAnswer answer = AssessmentFixture.answerBuilder()
                .user(candidate)
                .question(question)
                .save(assessmentAnswerRepository);
        AssessmentJudgement judgement = AssessmentJudgement.create(
                answer.getId(),
                question.getId(),
                time.getId(),
                candidate.getId(),
                new BigDecimal("100"),
                question.getScore(),
                JudgementStatus.JUDGED,
                ObjectiveResultCode.AC,
                JudgementSource.AUTO,
                null,
                ReviewerType.SYSTEM,
                LocalDateTime.now());
        assessmentJudgementRepository.save(judgement);
        loginAs(member);

        List<AssessmentQuestionScoreboard> result = assessmentJudgementAppService.listQuestionScoreboard(
                time.getId(),
                QuestionType.SINGLE_CHOICE,
                null);

        assertFalse(result.isEmpty());
    }

    /**
     * 为考生创建对应的报名记录，使考核查询能通过学号 JOIN 到内推信息。
     */
    private void createEnrollFor(User candidate, String referralCode) {
        Enroll enroll = Enroll.create(
                candidate.getUsername(),
                candidate.getStudentId(),
                "encodedPassword",
                referralCode,
                null,
                "计算机科学与技术",
                Gender.MALE,
                candidate.getDirection(),
                null,
                candidate.getEmail(),
                "自我介绍");
        enrollRepository.save(enroll);
    }

    @Test
    @DisplayName("listCandidateScoreboard: 组内内推优先于队长，独立考生组内内推优先")
    void listCandidateScoreboard_referredFirstWithinGroup() {
        User member = createMember(Direction.COMPUTER_VISION);
        User referrer = UserFixture.member(nextStudentId("RF"))
                .withDirection(Direction.COMPUTER_VISION)
                .withUsername("推荐人丁")
                .withInternalReferralCode("REFSB001")
                .save(userRepository, passwordEncoder);
        User leader = createCandidate(Direction.COMPUTER_VISION, 2024);
        User referredMember = createCandidate(Direction.COMPUTER_VISION, 2024);
        User referredIndependent = createCandidate(Direction.COMPUTER_VISION, 2024);
        User plainIndependent = createCandidate(Direction.COMPUTER_VISION, 2024);
        createEnrollFor(referredMember, "REFSB001");
        createEnrollFor(referredIndependent, "REFSB001");

        AssessmentTime time = createTeamAllowedTime(Direction.COMPUTER_VISION, 2024);
        AssessmentFixture.questionBuilder()
                .assessmentTime(time)
                .singleChoice("A", "A", "B")
                .score(new BigDecimal("100"))
                .save(assessmentQuestionRepository);
        AssessmentTeam team = AssessmentFixture.teamBuilder()
                .assessmentTime(time)
                .leader(leader)
                .save(assessmentTeamRepository);
        assessmentTeamRepository.addMember(team.getId(), referredMember.getId());
        loginAs(member);

        List<AssessmentCandidateScoreboard> result = assessmentJudgementAppService.listCandidateScoreboard(
                time.getId(),
                null);

        assertEquals(4, result.size());
        // 队伍组：内推队员排在队长之前
        assertEquals(referredMember.getId(), result.get(0).getCandidateUserId());
        assertEquals(leader.getId(), result.get(1).getCandidateUserId());
        // 独立考生组：内推优先
        assertEquals(referredIndependent.getId(), result.get(2).getCandidateUserId());
        assertEquals(plainIndependent.getId(), result.get(3).getCandidateUserId());
        // 内推字段透出
        assertEquals("REFSB001", result.get(0).getInternalReferralCode());
        assertEquals(referrer.getUsername(), result.get(0).getReferralUserName());
        assertNull(result.get(3).getInternalReferralCode());
        assertNull(result.get(3).getReferralUserName());
    }

    @Test
    @DisplayName("listQuestionSubmissions: 组内内推考生排在队长之前")
    void listQuestionSubmissions_referredFirstWithinTeam() {
        User member = createMember(Direction.COMPUTER_VISION);
        UserFixture.member(nextStudentId("RF"))
                .withDirection(Direction.COMPUTER_VISION)
                .withInternalReferralCode("REFSUB01")
                .save(userRepository, passwordEncoder);
        User leader = createCandidate(Direction.COMPUTER_VISION, 2024);
        User referredMember = createCandidate(Direction.COMPUTER_VISION, 2024);
        createEnrollFor(referredMember, "REFSUB01");

        AssessmentTime time = createTeamAllowedTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTime(time)
                .type(QuestionType.FILE_UPLOAD)
                .score(new BigDecimal("100"))
                .save(assessmentQuestionRepository);
        AssessmentTeam team = AssessmentFixture.teamBuilder()
                .assessmentTime(time)
                .leader(leader)
                .save(assessmentTeamRepository);
        assessmentTeamRepository.addMember(team.getId(), referredMember.getId());
        AssessmentFixture.answerBuilder().user(leader).question(question).save(assessmentAnswerRepository);
        AssessmentFixture.answerBuilder().user(referredMember).question(question).save(assessmentAnswerRepository);
        loginAs(member);

        List<AssessmentQuestionSubmissionReadModel> result = assessmentJudgementAppService.listQuestionSubmissions(
                question.getId(),
                null,
                null);

        assertEquals(2, result.size());
        assertEquals(referredMember.getId(), result.get(0).getCandidateUserId());
        assertEquals(leader.getId(), result.get(1).getCandidateUserId());
        assertEquals("REFSUB01", result.get(0).getInternalReferralCode());
        assertNull(result.get(1).getInternalReferralCode());
    }

    @Test
    @DisplayName("listCandidateScoreboard: 无报名记录考生字段为 null，无效码不视为内推")
    void listCandidateScoreboard_edgeCases() {
        User member = createMember(Direction.COMPUTER_VISION);
        User invalidCodeCandidate = createCandidate(Direction.COMPUTER_VISION, 2024);
        User noEnrollCandidate = createCandidate(Direction.COMPUTER_VISION, 2024);
        createEnrollFor(invalidCodeCandidate, "REFGONE1");
        // noEnrollCandidate 不创建报名记录

        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentFixture.questionBuilder()
                .assessmentTime(time)
                .singleChoice("A", "A", "B")
                .score(new BigDecimal("100"))
                .save(assessmentQuestionRepository);
        loginAs(member);

        List<AssessmentCandidateScoreboard> result = assessmentJudgementAppService.listCandidateScoreboard(
                time.getId(),
                null);

        assertEquals(2, result.size());
        // 无效码不视为内推：两人均按非内推处理，按学号升序
        assertEquals(invalidCodeCandidate.getId(), result.get(0).getCandidateUserId());
        assertEquals("REFGONE1", result.get(0).getInternalReferralCode());
        assertNull(result.get(0).getReferralUserName());
        // 无报名记录的考生字段为 null
        assertEquals(noEnrollCandidate.getId(), result.get(1).getCandidateUserId());
        assertNull(result.get(1).getInternalReferralCode());
        assertNull(result.get(1).getReferralUserName());
    }

    @Test
    @DisplayName("getDecisionWorkspace: 内推考生排序优先于学号")
    void getDecisionWorkspace_referredFirst() {
        User admin = createDirectionAdmin(Direction.COMPUTER_VISION);
        User referrer = UserFixture.member(nextStudentId("RF"))
                .withDirection(Direction.COMPUTER_VISION)
                .withUsername("推荐人戊")
                .withInternalReferralCode("REFDC001")
                .save(userRepository, passwordEncoder);
        // 非内推考生学号更小，若按学号排序应在前；内推优先生效后应在后
        User plainCandidate = createCandidate(Direction.COMPUTER_VISION, 2024);
        User referredCandidate = createCandidate(Direction.COMPUTER_VISION, 2024);
        createEnrollFor(referredCandidate, "REFDC001");

        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentFixture.questionBuilder()
                .assessmentTime(time)
                .singleChoice("A", "A", "B")
                .score(new BigDecimal("100"))
                .save(assessmentQuestionRepository);
        when(assessmentDecisionDomainService.isEliminatedFromPriorEpoch(any(), any(AssessmentTime.class)))
                .thenReturn(false);
        loginAs(admin);

        AssessmentDecisionWorkspace workspace = assessmentJudgementAppService.getDecisionWorkspace(
                time.getId(),
                null,
                null);

        assertEquals(2, workspace.getCandidates().size());
        assertEquals(referredCandidate.getId(), workspace.getCandidates().get(0).getCandidateUserId());
        assertEquals(plainCandidate.getId(), workspace.getCandidates().get(1).getCandidateUserId());
        assertEquals(referrer.getUsername(), workspace.getCandidates().get(0).getReferralUserName());
    }
}
