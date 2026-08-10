package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataConflict;
import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.exception.GlobalException;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.FileStatus;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.enumerate.JudgementSource;
import com.bluenet.web.domain.model.enumerate.JudgementStatus;
import com.bluenet.web.domain.model.enumerate.ObjectiveResultCode;
import com.bluenet.web.domain.model.enumerate.ProgrammingLanguage;
import com.bluenet.web.domain.model.enumerate.QuestionType;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.model.entity.AssessmentAnswer;
import com.bluenet.web.domain.model.entity.AssessmentJudgement;
import com.bluenet.web.domain.model.entity.AssessmentQuestion;
import com.bluenet.web.domain.model.entity.AssessmentSession;
import com.bluenet.web.domain.model.entity.AssessmentTeam;
import com.bluenet.web.domain.model.entity.AssessmentTeamMember;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.repository.AssessmentAnswerRepository;
import com.bluenet.web.domain.repository.AssessmentSessionRepository;
import com.bluenet.web.domain.repository.AssessmentTeamRepository;
import com.bluenet.web.domain.repository.AssessmentTimeRepository;
import com.bluenet.web.domain.service.AssessmentDecisionDomainService;
import com.bluenet.web.domain.service.FileDomainService;
import com.bluenet.web.infrastructure.security.principal.RoleTypeResolver;
import com.bluenet.web.testsupport.fixture.AssessmentFixture;
import com.bluenet.web.testsupport.fixture.UserFixture;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AssessmentAnswerDomainServiceImpl 单元测试。
 */
@DisplayName("AssessmentAnswerDomainServiceImpl 测试")
@ExtendWith(MockitoExtension.class)
class AssessmentAnswerDomainServiceImplTest {

    private static final LocalDateTime FIXED_NOW = LocalDateTime.of(2026, 7, 11, 12, 0, 0);

    @Mock
    private AssessmentTimeRepository assessmentTimeRepository;
    @Mock
    private AssessmentSessionRepository assessmentSessionRepository;
    @Mock
    private AssessmentAnswerRepository assessmentAnswerRepository;
    @Mock
    private AssessmentTeamRepository assessmentTeamRepository;
    @Mock
    private FileDomainService fileDomainService;
    @Mock
    private AssessmentDecisionDomainService assessmentDecisionDomainService;
    @Mock
    private RoleTypeResolver roleTypeResolver;

    private ObjectMapper objectMapper;
    private AssessmentAnswerDomainServiceImpl domainService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        domainService = new AssessmentAnswerDomainServiceImpl(
                assessmentTimeRepository,
                assessmentSessionRepository,
                assessmentAnswerRepository,
                assessmentTeamRepository,
                fileDomainService,
                assessmentDecisionDomainService,
                roleTypeResolver,
                objectMapper);
    }

    @Test
    @DisplayName("prepareAnswer: 非组队题应创建不带 teamId 的答案")
    void prepareAnswer_shouldCreateAnswerWithoutTeamIdForNonTeamQuestion() {
        User user = memberWithId(1L, "2024001001");
        AssessmentTime time = AssessmentFixture.timeBuilder().build();
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTimeId(time.getId())
                .fileUpload()
                .build();
        when(assessmentTimeRepository.findById(time.getId())).thenReturn(Optional.of(time));
        when(assessmentAnswerRepository.existsByUserIdAndQuestionId(user.getId(), question.getId())).thenReturn(false);

        AssessmentAnswer answer = domainService.prepareAnswer(user, question, "测试答案", ProgrammingLanguage.CPP, null);

        assertNotNull(answer);
        assertEquals(user.getId(), answer.getUserId());
        assertEquals(question.getId(), answer.getQuestionId());
        assertNull(answer.getTeamId());
        assertNotNull(answer.getSubmitTime());
    }

    @Test
    @DisplayName("prepareAnswer: 组队文件上传题应由队长创建带 teamId 的答案")
    void prepareAnswer_shouldCreateAnswerWithTeamIdForTeamLeader() {
        User user = memberWithId(1L, "2024001001");
        AssessmentTime time = AssessmentFixture.timeBuilder().allowTeam().build();
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTimeId(time.getId())
                .fileUpload()
                .build();
        AssessmentTeam team = AssessmentFixture.teamBuilder()
                .assessmentTimeId(time.getId())
                .leaderId(user.getId())
                .build();
        team.setId(1L);
        team.setId(1L);
        when(assessmentTimeRepository.findById(time.getId())).thenReturn(Optional.of(time));
        when(assessmentAnswerRepository.existsByUserIdAndQuestionId(user.getId(), question.getId())).thenReturn(false);
        when(assessmentTeamRepository.findByAssessmentTimeIdAndUserId(time.getId(), user.getId()))
                .thenReturn(Optional.of(team));

        AssessmentAnswer answer = domainService.prepareAnswer(user, question, "作品", ProgrammingLanguage.CPP, null);

        assertEquals(team.getId(), answer.getTeamId());
    }

    @Test
    @DisplayName("prepareAnswer: 方向不匹配应抛出 Forbidden")
    void prepareAnswer_shouldThrowForbiddenWhenDirectionMismatch() {
        User user = memberWithId(1L, "2024001001");
        user.setDirection(Direction.STRUCTURAL_DESIGN);
        AssessmentTime time = AssessmentFixture.timeBuilder().direction(Direction.COMPUTER_VISION).build();
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTimeId(time.getId())
                .build();
        when(assessmentTimeRepository.findById(time.getId())).thenReturn(Optional.of(time));

        Forbidden exception = assertThrows(
                Forbidden.class,
                () -> domainService.prepareAnswer(user, question, "答案", ProgrammingLanguage.CPP, null));
        assertEquals("方向不匹配", exception.getMessage());
    }

    @Test
    @DisplayName("prepareAnswer: 考核时间已结束应抛出 BadRequest")
    void prepareAnswer_shouldThrowBadRequestWhenTimeEnded() {
        User user = memberWithId(1L, "2024001001");
        AssessmentTime time = AssessmentFixture.timeBuilder().ended().build();
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTimeId(time.getId())
                .build();
        when(assessmentTimeRepository.findById(time.getId())).thenReturn(Optional.of(time));

        BadRequest exception = assertThrows(
                BadRequest.class,
                () -> domainService.prepareAnswer(user, question, "答案", ProgrammingLanguage.CPP, null));
        assertEquals("考核时间已结束，无法提交答案", exception.getMessage());
    }

    @Test
    @DisplayName("prepareAnswer: 考生已被淘汰应抛出 Forbidden")
    void prepareAnswer_shouldThrowForbiddenWhenEliminated() {
        User user = candidateWithId(1L, "2024001001");
        AssessmentTime time = AssessmentFixture.timeBuilder().build();
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTimeId(time.getId())
                .build();
        when(assessmentTimeRepository.findById(time.getId())).thenReturn(Optional.of(time));
        when(roleTypeResolver.resolve(user.getRoleId())).thenReturn(RoleType.CANDIDATE);
        when(assessmentDecisionDomainService.isEliminatedFromPriorEpoch(user.getId(), time)).thenReturn(true);

        Forbidden exception = assertThrows(
                Forbidden.class,
                () -> domainService.prepareAnswer(user, question, "答案", ProgrammingLanguage.CPP, null));
        assertEquals("已在该方向考核中被淘汰，无法提交答案", exception.getMessage());
    }

    @Test
    @DisplayName("prepareAnswer: 文件不存在应抛出 BadRequest")
    void prepareAnswer_shouldThrowBadRequestWhenFileNotFound() {
        User user = memberWithId(1L, "2024001001");
        AssessmentTime time = AssessmentFixture.timeBuilder().build();
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTimeId(time.getId())
                .build();
        Long fileId = 999L;
        when(assessmentTimeRepository.findById(time.getId())).thenReturn(Optional.of(time));
        when(fileDomainService.getFileById(fileId)).thenReturn(null);

        BadRequest exception = assertThrows(
                BadRequest.class,
                () -> domainService.prepareAnswer(user, question, "答案", ProgrammingLanguage.CPP, fileId));
        assertEquals("文件不存在", exception.getMessage());
    }

    @Test
    @DisplayName("prepareAnswer: 文件类型不是 WORK 应抛出 BadRequest")
    void prepareAnswer_shouldThrowBadRequestWhenFileTypeMismatch() {
        User user = memberWithId(1L, "2024001001");
        AssessmentTime time = AssessmentFixture.timeBuilder().build();
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTimeId(time.getId())
                .build();
        Long fileId = 999L;
        File file = File.reconstruct(fileId, "avatar.png", FileType.AVATAR, null, FileStatus.ACTIVE, FIXED_NOW);
        when(assessmentTimeRepository.findById(time.getId())).thenReturn(Optional.of(time));
        when(fileDomainService.getFileById(fileId)).thenReturn(file);

        BadRequest exception = assertThrows(
                BadRequest.class,
                () -> domainService.prepareAnswer(user, question, "答案", ProgrammingLanguage.CPP, fileId));
        assertEquals("文件类型不匹配，期望 WORK", exception.getMessage());
    }

    @Test
    @DisplayName("prepareAnswer: 会话截止时间已过应抛出 BadRequest")
    void prepareAnswer_shouldThrowBadRequestWhenSessionDeadlineExceeded() {
        User user = memberWithId(1L, "2024001001");
        AssessmentTime time = AssessmentFixture.timeBuilder().timeLimit(60).build();
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTimeId(time.getId())
                .build();
        AssessmentSession session = AssessmentFixture.sessionBuilder()
                .userId(user.getId())
                .assessmentTimeId(time.getId())
                .deadline(FIXED_NOW.minusMinutes(1))
                .build();
        when(assessmentTimeRepository.findById(time.getId())).thenReturn(Optional.of(time));
        when(assessmentSessionRepository.findByUserIdAndAssessmentTimeId(user.getId(), time.getId()))
                .thenReturn(Optional.of(session));

        BadRequest exception = assertThrows(
                BadRequest.class,
                () -> domainService.prepareAnswer(user, question, "答案", ProgrammingLanguage.CPP, null));
        assertEquals("考核时间已到，无法提交答案", exception.getMessage());
    }

    @Test
    @DisplayName("prepareAnswer: 已提交过答案应抛出 DataConflict")
    void prepareAnswer_shouldThrowDataConflictWhenAlreadySubmitted() {
        User user = memberWithId(1L, "2024001001");
        AssessmentTime time = AssessmentFixture.timeBuilder().build();
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTimeId(time.getId())
                .build();
        when(assessmentTimeRepository.findById(time.getId())).thenReturn(Optional.of(time));
        when(assessmentAnswerRepository.existsByUserIdAndQuestionId(user.getId(), question.getId())).thenReturn(true);

        DataConflict exception = assertThrows(
                DataConflict.class,
                () -> domainService.prepareAnswer(user, question, "答案", ProgrammingLanguage.CPP, null));
        assertEquals("已经提交过该题目的答案", exception.getMessage());
    }

    @Test
    @DisplayName("prepareAnswer: 非队长提交组队文件上传题应抛出 Forbidden")
    void prepareAnswer_shouldThrowForbiddenWhenNotTeamLeaderForTeamFileUpload() {
        User user = memberWithId(1L, "2024001001");
        AssessmentTime time = AssessmentFixture.timeBuilder().allowTeam().build();
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTimeId(time.getId())
                .fileUpload()
                .build();
        AssessmentTeam team = AssessmentFixture.teamBuilder()
                .assessmentTimeId(time.getId())
                .leaderId(user.getId() + 1)
                .build();
        team.setId(1L);
        when(assessmentTimeRepository.findById(time.getId())).thenReturn(Optional.of(time));
        when(assessmentAnswerRepository.existsByUserIdAndQuestionId(user.getId(), question.getId())).thenReturn(false);
        when(assessmentTeamRepository.findByAssessmentTimeIdAndUserId(time.getId(), user.getId()))
                .thenReturn(Optional.of(team));

        Forbidden exception = assertThrows(
                Forbidden.class,
                () -> domainService.prepareAnswer(user, question, "作品", ProgrammingLanguage.CPP, null));
        assertEquals("只有队长可以提交文件上传题的答案", exception.getMessage());
    }

    @Test
    @DisplayName("prepareAnswer: 队伍已解散应抛出 BadRequest")
    void prepareAnswer_shouldThrowBadRequestWhenTeamDisbanded() {
        User user = memberWithId(1L, "2024001001");
        AssessmentTime time = AssessmentFixture.timeBuilder().allowTeam().build();
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTimeId(time.getId())
                .fileUpload()
                .build();
        AssessmentTeam team = AssessmentFixture.teamBuilder()
                .assessmentTimeId(time.getId())
                .leaderId(user.getId())
                .build();
        team.setId(1L);
        team.disband();
        when(assessmentTimeRepository.findById(time.getId())).thenReturn(Optional.of(time));
        when(assessmentAnswerRepository.existsByUserIdAndQuestionId(user.getId(), question.getId())).thenReturn(false);
        when(assessmentTeamRepository.findByAssessmentTimeIdAndUserId(time.getId(), user.getId()))
                .thenReturn(Optional.of(team));

        BadRequest exception = assertThrows(
                BadRequest.class,
                () -> domainService.prepareAnswer(user, question, "作品", ProgrammingLanguage.CPP, null));
        assertEquals("队伍已解散，无法提交答案", exception.getMessage());
    }

    @Test
    @DisplayName("prepareTeamMemberAnswers: 无 teamId 时应返回空列表")
    void prepareTeamMemberAnswers_shouldReturnEmptyWhenNoTeamId() {
        AssessmentAnswer leaderAnswer = AssessmentFixture.answerBuilder().teamId(null).build();
        AssessmentQuestion question = AssessmentFixture.questionBuilder().build();

        List<AssessmentAnswer> answers = domainService.prepareTeamMemberAnswers(
                leaderAnswer,
                question,
                "答案",
                ProgrammingLanguage.CPP,
                null);

        assertTrue(answers.isEmpty());
        verify(assessmentTeamRepository, never()).findMembersByTeamId(null);
    }

    @Test
    @DisplayName("prepareTeamMemberAnswers: 无其他成员时应返回空列表")
    void prepareTeamMemberAnswers_shouldReturnEmptyWhenNoOtherMembers() {
        Long leaderId = 1L;
        AssessmentTeam team = AssessmentFixture.teamBuilder().leaderId(leaderId).build();
        team.setId(1L);
        AssessmentAnswer leaderAnswer = AssessmentFixture.answerBuilder()
                .userId(leaderId)
                .team(team)
                .build();
        AssessmentQuestion question = AssessmentFixture.questionBuilder().build();
        when(assessmentTeamRepository.findMembersByTeamId(team.getId())).thenReturn(Collections.emptyList());

        List<AssessmentAnswer> answers = domainService.prepareTeamMemberAnswers(
                leaderAnswer,
                question,
                "答案",
                ProgrammingLanguage.CPP,
                null);

        assertTrue(answers.isEmpty());
    }

    @Test
    @DisplayName("prepareTeamMemberAnswers: 应过滤已有答案的队员")
    void prepareTeamMemberAnswers_shouldFilterMembersWithExistingAnswers() {
        Long leaderId = 1L;
        Long memberOneId = 2L;
        Long memberTwoId = 3L;
        AssessmentTeam team = AssessmentFixture.teamBuilder().leaderId(leaderId).build();
        team.setId(1L);
        AssessmentAnswer leaderAnswer = AssessmentFixture.answerBuilder()
                .userId(leaderId)
                .team(team)
                .build();
        AssessmentQuestion question = AssessmentFixture.questionBuilder().build();
        AssessmentTeamMember memberOne = AssessmentTeamMember.create(team.getId(), memberOneId);
        AssessmentTeamMember memberTwo = AssessmentTeamMember.create(team.getId(), memberTwoId);
        List<AssessmentTeamMember> members = List.of(memberOne, memberTwo);
        when(assessmentTeamRepository.findMembersByTeamId(team.getId())).thenReturn(members);
        when(assessmentAnswerRepository.findExistingAnswerUserIds(List.of(memberOneId, memberTwoId), question.getId()))
                .thenReturn(List.of(memberOneId));

        List<AssessmentAnswer> answers = domainService.prepareTeamMemberAnswers(
                leaderAnswer,
                question,
                "答案",
                ProgrammingLanguage.CPP,
                null);

        assertEquals(1, answers.size());
        assertEquals(memberTwoId, answers.get(0).getUserId());
        assertEquals(team.getId(), answers.get(0).getTeamId());
    }

    @Test
    @DisplayName("prepareTeamMemberAnswers: 应创建所有无答案队员的答案")
    void prepareTeamMemberAnswers_shouldCreateAnswersForAllMembersWithoutAnswers() {
        Long leaderId = 1L;
        Long memberOneId = 2L;
        Long memberTwoId = 3L;
        AssessmentTeam team = AssessmentFixture.teamBuilder().leaderId(leaderId).build();
        team.setId(1L);
        AssessmentAnswer leaderAnswer = AssessmentFixture.answerBuilder()
                .userId(leaderId)
                .team(team)
                .build();
        AssessmentQuestion question = AssessmentFixture.questionBuilder().build();
        AssessmentTeamMember memberOne = AssessmentTeamMember.create(team.getId(), memberOneId);
        AssessmentTeamMember memberTwo = AssessmentTeamMember.create(team.getId(), memberTwoId);
        List<AssessmentTeamMember> members = List.of(memberOne, memberTwo);
        when(assessmentTeamRepository.findMembersByTeamId(team.getId())).thenReturn(members);
        when(assessmentAnswerRepository.findExistingAnswerUserIds(List.of(memberOneId, memberTwoId), question.getId()))
                .thenReturn(Collections.emptyList());

        List<AssessmentAnswer> answers = domainService.prepareTeamMemberAnswers(
                leaderAnswer,
                question,
                "答案",
                ProgrammingLanguage.CPP,
                null);

        assertEquals(2, answers.size());
        assertEquals(memberOneId, answers.get(0).getUserId());
        assertEquals(memberTwoId, answers.get(1).getUserId());
    }

    @Test
    @DisplayName("prepareObjectiveJudgement: 非客观题应返回 null")
    void prepareObjectiveJudgement_shouldReturnNullForNonObjectiveQuestion() {
        AssessmentAnswer answer = AssessmentFixture.answerBuilder().content("作品链接").build();
        AssessmentQuestion question = AssessmentFixture.questionBuilder().fileUpload().build();

        AssessmentJudgement judgement = domainService.prepareObjectiveJudgement(answer, question);

        assertNull(judgement);
    }

    @Test
    @DisplayName("prepareObjectiveJudgement: 单选题回答正确应返回 AC")
    void prepareObjectiveJudgement_shouldReturnAcForCorrectSingleChoice() {
        AssessmentAnswer answer = AssessmentFixture.answerBuilder().content("A").build();
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .singleChoice("A", "A", "B", "C")
                .score(new BigDecimal("10"))
                .build();

        AssessmentJudgement judgement = domainService.prepareObjectiveJudgement(answer, question);

        assertNotNull(judgement);
        assertEquals(ObjectiveResultCode.AC, judgement.getResultCode());
        assertEquals(new BigDecimal("10"), judgement.getScore());
        assertEquals(new BigDecimal("10"), judgement.getMaxScore());
        assertEquals(JudgementStatus.JUDGED, judgement.getStatus());
        assertEquals(JudgementSource.AUTO, judgement.getSource());
    }

    @Test
    @DisplayName("prepareObjectiveJudgement: 单选题回答错误应返回 WA")
    void prepareObjectiveJudgement_shouldReturnWaForWrongSingleChoice() {
        AssessmentAnswer answer = AssessmentFixture.answerBuilder().content("B").build();
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .singleChoice("A", "A", "B", "C")
                .score(new BigDecimal("10"))
                .build();

        AssessmentJudgement judgement = domainService.prepareObjectiveJudgement(answer, question);

        assertNotNull(judgement);
        assertEquals(ObjectiveResultCode.WA, judgement.getResultCode());
        assertEquals(BigDecimal.ZERO, judgement.getScore());
    }

    @Test
    @DisplayName("prepareObjectiveJudgement: 单选题内容类型不匹配应抛出 GlobalException")
    void prepareObjectiveJudgement_shouldThrowGlobalExceptionForSingleChoiceContentMismatch() {
        AssessmentAnswer answer = AssessmentFixture.answerBuilder().content("A").build();
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .fileUpload()
                .build();
        question.setQuestionType(QuestionType.SINGLE_CHOICE);

        GlobalException exception = assertThrows(
                GlobalException.class,
                () -> domainService.prepareObjectiveJudgement(answer, question));
        assertEquals("单选题内容配置错误", exception.getMessage());
    }

    @Test
    @DisplayName("prepareObjectiveJudgement: 多选题回答正确应返回 AC")
    void prepareObjectiveJudgement_shouldReturnAcForCorrectMultipleChoice() {
        AssessmentAnswer answer = AssessmentFixture.answerBuilder().content("[\"A\", \"B\"]").build();
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .multipleChoice(List.of("A", "B"), "A", "B", "C")
                .score(new BigDecimal("10"))
                .build();

        AssessmentJudgement judgement = domainService.prepareObjectiveJudgement(answer, question);

        assertNotNull(judgement);
        assertEquals(ObjectiveResultCode.AC, judgement.getResultCode());
        assertEquals(new BigDecimal("10"), judgement.getScore());
    }

    @Test
    @DisplayName("prepareObjectiveJudgement: 多选题回答错误应返回 WA")
    void prepareObjectiveJudgement_shouldReturnWaForWrongMultipleChoice() {
        AssessmentAnswer answer = AssessmentFixture.answerBuilder().content("[\"A\"]").build();
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .multipleChoice(List.of("A", "B"), "A", "B", "C")
                .score(new BigDecimal("10"))
                .build();

        AssessmentJudgement judgement = domainService.prepareObjectiveJudgement(answer, question);

        assertNotNull(judgement);
        assertEquals(ObjectiveResultCode.WA, judgement.getResultCode());
        assertEquals(BigDecimal.ZERO, judgement.getScore());
    }

    @Test
    @DisplayName("prepareObjectiveJudgement: 多选题答案格式错误应抛出 BadRequest")
    void prepareObjectiveJudgement_shouldThrowBadRequestForInvalidMultipleChoiceFormat() {
        AssessmentAnswer answer = AssessmentFixture.answerBuilder().content("不是 JSON").build();
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .multipleChoice(List.of("A", "B"), "A", "B", "C")
                .build();

        BadRequest exception = assertThrows(
                BadRequest.class,
                () -> domainService.prepareObjectiveJudgement(answer, question));
        assertEquals("多选题答案格式错误", exception.getMessage());
    }

    @Test
    @DisplayName("prepareObjectiveJudgement: 多选题内容类型不匹配应抛出 GlobalException")
    void prepareObjectiveJudgement_shouldThrowGlobalExceptionForMultipleChoiceContentMismatch() {
        AssessmentAnswer answer = AssessmentFixture.answerBuilder().content("[\"A\"]").build();
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .fileUpload()
                .build();
        question.setQuestionType(QuestionType.MULTIPLE_CHOICE);

        GlobalException exception = assertThrows(
                GlobalException.class,
                () -> domainService.prepareObjectiveJudgement(answer, question));
        assertEquals("多选题内容配置错误", exception.getMessage());
    }

    @Test
    @DisplayName("prepareUpdatedAnswers: 非组队场景应只更新当前答案")
    void prepareUpdatedAnswers_shouldUpdateCurrentAnswerForNonTeam() {
        User user = memberWithId(1L, "2024001001");
        AssessmentTime time = AssessmentFixture.timeBuilder().build();
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTimeId(time.getId())
                .build();
        AssessmentAnswer existingAnswer = AssessmentFixture.answerBuilder()
                .user(user)
                .question(question)
                .teamId(null)
                .build();
        when(assessmentTimeRepository.findById(time.getId())).thenReturn(Optional.of(time));

        List<AssessmentAnswer> answers = domainService.prepareUpdatedAnswers(
                user,
                question,
                existingAnswer,
                "更新后的答案",
                ProgrammingLanguage.JAVA,
                null);

        assertEquals(1, answers.size());
        assertEquals(existingAnswer, answers.get(0));
        assertEquals("更新后的答案", existingAnswer.getContent());
        assertEquals(ProgrammingLanguage.JAVA, existingAnswer.getLanguage());
        assertNotNull(existingAnswer.getSubmitTime());
    }

    @Test
    @DisplayName("prepareUpdatedAnswers: 组队场景应同步更新全队答案")
    void prepareUpdatedAnswers_shouldUpdateAllTeamAnswers() {
        User user = memberWithId(1L, "2024001001");
        AssessmentTime time = AssessmentFixture.timeBuilder().allowTeam().build();
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTimeId(time.getId())
                .fileUpload()
                .build();
        AssessmentTeam team = AssessmentFixture.teamBuilder()
                .assessmentTimeId(time.getId())
                .leaderId(user.getId())
                .build();
        team.setId(1L);
        AssessmentAnswer existingAnswer = AssessmentFixture.answerBuilder()
                .user(user)
                .question(question)
                .team(team)
                .build();
        AssessmentAnswer memberAnswer = AssessmentFixture.answerBuilder()
                .userId(user.getId() + 1)
                .question(question)
                .team(team)
                .build();
        when(assessmentTimeRepository.findById(time.getId())).thenReturn(Optional.of(time));
        when(assessmentTeamRepository.findByAssessmentTimeIdAndUserId(time.getId(), user.getId()))
                .thenReturn(Optional.of(team));
        when(assessmentAnswerRepository.findByTeamIdAndQuestionId(team.getId(), question.getId()))
                .thenReturn(List.of(existingAnswer, memberAnswer));

        List<AssessmentAnswer> answers = domainService.prepareUpdatedAnswers(
                user,
                question,
                existingAnswer,
                "同步答案",
                ProgrammingLanguage.PYTHON,
                null);

        assertEquals(2, answers.size());
        assertEquals("同步答案", existingAnswer.getContent());
        assertEquals("同步答案", memberAnswer.getContent());
        assertEquals(ProgrammingLanguage.PYTHON, memberAnswer.getLanguage());
    }

    @Test
    @DisplayName("prepareUpdatedAnswers: 组队文件上传题非队长应抛出 Forbidden")
    void prepareUpdatedAnswers_shouldThrowForbiddenWhenNotLeaderForTeamFileUpload() {
        User user = memberWithId(1L, "2024001001");
        AssessmentTime time = AssessmentFixture.timeBuilder().allowTeam().build();
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTimeId(time.getId())
                .fileUpload()
                .build();
        AssessmentTeam team = AssessmentFixture.teamBuilder()
                .assessmentTimeId(time.getId())
                .leaderId(user.getId() + 1)
                .build();
        team.setId(1L);
        AssessmentAnswer existingAnswer = AssessmentFixture.answerBuilder()
                .user(user)
                .question(question)
                .team(team)
                .build();
        when(assessmentTimeRepository.findById(time.getId())).thenReturn(Optional.of(time));
        when(assessmentTeamRepository.findByAssessmentTimeIdAndUserId(time.getId(), user.getId()))
                .thenReturn(Optional.of(team));

        Forbidden exception = assertThrows(
                Forbidden.class,
                () -> domainService
                        .prepareUpdatedAnswers(user, question, existingAnswer, "答案", ProgrammingLanguage.CPP, null));
        assertEquals("只有队长可以提交文件上传题的答案", exception.getMessage());
    }

    @Test
    @DisplayName("prepareUpdatedAnswers: 考核时间已结束应抛出 BadRequest")
    void prepareUpdatedAnswers_shouldThrowBadRequestWhenTimeEnded() {
        User user = memberWithId(1L, "2024001001");
        AssessmentTime time = AssessmentFixture.timeBuilder().ended().build();
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTimeId(time.getId())
                .build();
        AssessmentAnswer existingAnswer = AssessmentFixture.answerBuilder()
                .user(user)
                .question(question)
                .build();
        when(assessmentTimeRepository.findById(time.getId())).thenReturn(Optional.of(time));

        BadRequest exception = assertThrows(
                BadRequest.class,
                () -> domainService
                        .prepareUpdatedAnswers(user, question, existingAnswer, "答案", ProgrammingLanguage.CPP, null));
        assertEquals("考核时间已结束，无法提交答案", exception.getMessage());
    }

    private User memberWithId(Long id, String studentId) {
        User user = UserFixture.member(studentId).build();
        user.setId(id);
        return user;
    }

    private User candidateWithId(Long id, String studentId) {
        User user = UserFixture.candidate(studentId).build();
        user.setId(id);
        return user;
    }
}
