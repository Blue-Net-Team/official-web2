package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.AssessmentDecisionResult;
import com.bluenet.web.application.AssessmentJudgementResult;
import com.bluenet.web.application.command.assessment_judgement.AssessmentJudgementCommands;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.model.enumerate.JudgementSource;
import com.bluenet.web.domain.model.enumerate.JudgementStatus;
import com.bluenet.web.domain.model.enumerate.QuestionType;
import com.bluenet.web.domain.model.enumerate.ReviewerType;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.model.entity.AssessmentAnswer;
import com.bluenet.web.domain.model.entity.AssessmentJudgement;
import com.bluenet.web.domain.model.entity.AssessmentTeam;
import com.bluenet.web.domain.model.vo.AssessmentCandidateScoreRowVO;
import com.bluenet.web.domain.model.vo.AssessmentCandidateScoreboardVO;
import com.bluenet.web.domain.model.vo.AssessmentDecisionCandidateVO;
import com.bluenet.web.domain.model.vo.AssessmentDecisionVO;
import com.bluenet.web.domain.model.vo.AssessmentDecisionWorkspaceVO;
import com.bluenet.web.domain.model.vo.AssessmentJudgementVO;
import com.bluenet.web.domain.model.vo.AssessmentQuestionScoreboardVO;
import com.bluenet.web.domain.model.vo.AssessmentQuestionSubmissionHistoryVO;
import com.bluenet.web.domain.model.vo.AssessmentQuestionSubmissionVO;
import com.bluenet.web.domain.model.entity.AssessmentQuestion;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.repository.AssessmentAnswerRepository;
import com.bluenet.web.domain.repository.AssessmentDecisionRepository;
import com.bluenet.web.domain.repository.AssessmentJudgementRepository;
import com.bluenet.web.domain.repository.AssessmentTeamRepository;
import com.bluenet.web.domain.repository.CommentRepository;
import com.bluenet.web.domain.service.AssessmentDecisionDomainService;
import com.bluenet.web.domain.service.AssessmentJudgementDomainService;
import com.bluenet.web.domain.repository.AssessmentQuestionRepository;
import com.bluenet.web.domain.repository.AssessmentTimeRepository;
import com.bluenet.web.domain.service.UserDomainService;
import com.bluenet.web.domain.model.enumerate.MessageChannel;
import com.bluenet.web.domain.model.enumerate.MessageContentType;
import com.bluenet.web.application.message.MessageDispatcher;
import com.bluenet.web.application.message.MessageRequest;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * AssessmentJudgementAppServiceImpl 单元测试。
 */
@DisplayName("AssessmentJudgementAppServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class AssessmentJudgementAppServiceImplTest {
    private static final Long ANSWER_ID = 10L;
    private static final Long QUESTION_ID = 20L;
    private static final Long ASSESSMENT_TIME_ID = 30L;
    private static final Long CANDIDATE_ID = 40L;
    private static final Long REVIEWER_ID = 50L;

    @Mock
    private AssessmentJudgementDomainService assessmentJudgementDomainService;

    @Mock
    private AssessmentDecisionDomainService assessmentDecisionDomainService;

    @Mock
    private AssessmentAnswerRepository assessmentAnswerRepository;

    @Mock
    private AssessmentQuestionRepository assessmentQuestionRepository;

    @Mock
    private AssessmentTimeRepository assessmentTimeRepository;

    @Mock
    private AssessmentJudgementRepository assessmentJudgementRepository;

    @Mock
    private AssessmentDecisionRepository assessmentDecisionRepository;

    @Mock
    private UserDomainService userDomainService;

    @Mock
    private MessageDispatcher messageDispatcher;

    @Mock
    private com.bluenet.web.application.message.MessageTemplateRegistry messageTemplateRegistry;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private AssessmentTeamRepository assessmentTeamRepository;

    @InjectMocks
    private AssessmentJudgementAppServiceImpl assessmentJudgementAppService;

    /**
     * 验证答案最新评判查询会转换为应用层结果。
     */
    @Test
    @DisplayName("查询答案最新评判：应返回Result")
    void getLatestByAnswerId_existing_shouldReturnResult() {
        when(assessmentJudgementDomainService.getLatestByAnswerId(ANSWER_ID))
                .thenReturn(createJudgementVO(JudgementSource.AUTO, null));

        AssessmentJudgementResult result = assessmentJudgementAppService.getLatestByAnswerId(ANSWER_ID);

        assertEquals(ANSWER_ID, result.answerId());
        assertEquals(JudgementSource.AUTO, result.source());
    }

    /**
     * 验证题目评判列表查询会转换为 Result 列表。
     */
    @Test
    @DisplayName("查询题目评判列表：应返回Result列表")
    void listByQuestionId_shouldReturnResultList() {
        when(assessmentJudgementDomainService.listByQuestionId(QUESTION_ID))
                .thenReturn(List.of(createJudgementVO(JudgementSource.AUTO, null)));

        List<AssessmentJudgementResult> result = assessmentJudgementAppService.listByQuestionId(QUESTION_ID);

        assertEquals(1, result.size());
        assertEquals(QUESTION_ID, result.get(0).questionId());
    }

    /**
     * 验证方向管理员可以保存最终录用决策。
     */
    @Test
    @DisplayName("方向管理员设置最终通过：应保存决策")
    void decideAssessment_directionAdmin_shouldSaveDecision() {
        try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
            mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createUser(RoleType.DIRECTION_ADMIN));
            when(assessmentDecisionDomainService.saveDecision(any(AssessmentDecisionVO.class)))
                    .thenReturn(createDecisionVO(true));
            when(assessmentTimeRepository.findById(ASSESSMENT_TIME_ID)).thenReturn(Optional.of(createTime()));

            AssessmentDecisionResult result = assessmentJudgementAppService.decideAssessment(
                    new AssessmentJudgementCommands.DecideAssessmentCommand(
                            CANDIDATE_ID, ASSESSMENT_TIME_ID, true, "通过"));

            assertTrue(result.passed());
            ArgumentCaptor<AssessmentDecisionVO> captor = ArgumentCaptor.forClass(AssessmentDecisionVO.class);
            verify(assessmentDecisionDomainService).saveDecision(captor.capture());
            assertEquals(REVIEWER_ID, captor.getValue().getDecidedBy());
        }
    }

    /**
     * 验证普通成员不能保存最终录用决策。
     */
    @Test
    @DisplayName("成员设置最终通过：应拒绝")
    void decideAssessment_member_shouldThrowForbidden() {
        try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
            mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createUser(RoleType.MEMBER));

            assertThrows(
                    Forbidden.class,
                    () -> assessmentJudgementAppService.decideAssessment(
                            new AssessmentJudgementCommands.DecideAssessmentCommand(
                                    CANDIDATE_ID, ASSESSMENT_TIME_ID, true, null)));
            verifyNoInteractions(assessmentDecisionDomainService);
        }
    }

    /**
     * 验证题目评分汇总会返回提交和待评统计。
     */
    @Test
    @DisplayName("题目评分汇总：应返回提交、已评和待评统计")
    void listQuestionScoreboard_shouldReturnAggregation() {
        try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
            mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createUser(RoleType.MEMBER));
            when(assessmentTimeRepository.findById(ASSESSMENT_TIME_ID)).thenReturn(Optional.of(createTime()));
            when(
                    assessmentJudgementRepository.findQuestionScoreboard(
                            ASSESSMENT_TIME_ID,
                            QuestionType.FILE_UPLOAD,
                            "简历"))
                                    .thenReturn(
                                            List.of(
                                                    AssessmentQuestionScoreboardVO.builder()
                                                            .questionId(QUESTION_ID)
                                                            .submittedCount(3L)
                                                            .judgedCount(2L)
                                                            .pendingCount(1L)
                                                            .build()));

            List<AssessmentQuestionScoreboardVO> result = assessmentJudgementAppService
                    .listQuestionScoreboard(ASSESSMENT_TIME_ID, QuestionType.FILE_UPLOAD, " 简历 ");

            assertEquals(1, result.size());
            assertEquals(1L, result.get(0).getPendingCount());
        }
    }

    /**
     * 验证题目提交列表会组装最新评判和历史记录。
     */
    @Test
    @DisplayName("题目提交列表：应组装考生信息和最新评判")
    void listQuestionSubmissions_shouldReturnLatestJudgement() {
        try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
            mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createUser(RoleType.MEMBER));
            when(assessmentQuestionRepository.findById(QUESTION_ID))
                    .thenReturn(Optional.of(createQuestion(QuestionType.FILE_UPLOAD)));
            when(assessmentTimeRepository.findById(ASSESSMENT_TIME_ID)).thenReturn(Optional.of(createTime()));
            when(assessmentJudgementRepository.findQuestionSubmissions(QUESTION_ID, null, "JUDGED"))
                    .thenReturn(List.of(createSubmissionVO(true)));
            when(assessmentJudgementRepository.findQuestionSubmissionHistories(QUESTION_ID, List.of(CANDIDATE_ID)))
                    .thenReturn(List.of(createSubmissionHistoryVO(100L, BigDecimal.TEN, true)));

            List<AssessmentQuestionSubmissionVO> result = assessmentJudgementAppService
                    .listQuestionSubmissions(QUESTION_ID, null, "judged");

            assertEquals(1, result.size());
            assertEquals(CANDIDATE_ID, result.get(0).getCandidateUserId());
            assertNotNull(result.get(0).getJudgementScore());
            assertEquals(BigDecimal.TEN, result.get(0).getJudgementScore());
            assertEquals(1, result.get(0).getHistories().size());
            assertTrue(result.get(0).getHistories().get(0).getSelectedBest());
        }
    }

    /**
     * 验证人员评分矩阵会计算总分和待评分数量。
     */
    @Test
    @DisplayName("考生评分矩阵：应计算总分和待评分数量")
    void listCandidateScoreboard_shouldCalculateTotalsAndPending() {
        try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
            mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createUser(RoleType.MEMBER));
            when(assessmentTimeRepository.findById(ASSESSMENT_TIME_ID)).thenReturn(Optional.of(createTime()));
            when(assessmentJudgementRepository.findCandidateScoreRows(ASSESSMENT_TIME_ID, null))
                    .thenReturn(
                            List.of(
                                    createCandidateScoreRowVO(QUESTION_ID, 1, true, true),
                                    createCandidateScoreRowVO(21L, 2, true, false)));

            List<AssessmentCandidateScoreboardVO> result = assessmentJudgementAppService
                    .listCandidateScoreboard(ASSESSMENT_TIME_ID, null);

            assertEquals(1, result.size());
            assertEquals(BigDecimal.TEN, result.get(0).getTotalScore());
            assertEquals(1L, result.get(0).getJudgedQuestionCount());
            assertEquals(1L, result.get(0).getPendingJudgementCount());
        }
    }

    /**
     * 验证当考核时间 grade 为 null（不限年级）时，人员评分矩阵仍能正确返回考生数据。
     */
    @Test
    @DisplayName("考生评分矩阵：grade=null 时应返回考生评分数据")
    void listCandidateScoreboard_nullGrade_shouldReturnCandidates() {
        try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
            mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createUser(RoleType.MEMBER));
            when(assessmentTimeRepository.findById(ASSESSMENT_TIME_ID))
                    .thenReturn(Optional.of(createTimeWithNullGrade()));
            when(assessmentJudgementRepository.findCandidateScoreRows(ASSESSMENT_TIME_ID, null))
                    .thenReturn(
                            List.of(
                                    createCandidateScoreRowVO(QUESTION_ID, 1, true, true),
                                    createCandidateScoreRowVO(21L, 2, true, false)));

            List<AssessmentCandidateScoreboardVO> result = assessmentJudgementAppService
                    .listCandidateScoreboard(ASSESSMENT_TIME_ID, null);

            assertEquals(1, result.size());
            assertEquals(BigDecimal.TEN, result.get(0).getTotalScore());
            assertEquals(1L, result.get(0).getJudgedQuestionCount());
            assertEquals(1L, result.get(0).getPendingJudgementCount());
        }
    }

    /**
     * 验证录用决策工作台会计算候选人状态统计。
     */
    @Test
    @DisplayName("录用决策工作台：应统计候选人、待决策、通过和淘汰")
    void getDecisionWorkspace_shouldCalculateStatistics() {
        try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
            mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createUser(RoleType.DIRECTION_ADMIN));
            when(assessmentTimeRepository.findById(ASSESSMENT_TIME_ID)).thenReturn(Optional.of(createTime()));
            when(assessmentJudgementRepository.findCandidateScoreRows(ASSESSMENT_TIME_ID, null))
                    .thenReturn(
                            List.of(
                                    createCandidateScoreRowVOForUser(
                                            CANDIDATE_ID,
                                            "20260001",
                                            QUESTION_ID,
                                            1,
                                            true,
                                            true),
                                    createCandidateScoreRowVOForUser(41L, "20260002", 21L, 1, true, true),
                                    createCandidateScoreRowVOForUser(42L, "20260003", 22L, 1, true, true)));
            when(assessmentDecisionRepository.findByAssessmentTimeId(ASSESSMENT_TIME_ID))
                    .thenReturn(List.of(createDecisionVOForUser(40L, true), createDecisionVOForUser(41L, false)));

            AssessmentDecisionWorkspaceVO result = assessmentJudgementAppService
                    .getDecisionWorkspace(ASSESSMENT_TIME_ID, null, null);

            assertEquals(3L, result.getStatistics().getCandidates());
            assertEquals(1L, result.getStatistics().getPending());
            assertEquals(1L, result.getStatistics().getPassed());
            assertEquals(1L, result.getStatistics().getEliminated());
        }
    }

    /**
     * 验证在相同 direction+grade 的前序轮次被淘汰的考生不会出现在当前轮次决策工作台中。
     */
    @Test
    @DisplayName("录用决策工作台：应排除前序轮次被淘汰的考生")
    void getDecisionWorkspace_priorEpochEliminated_shouldFilterOut() {
        try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
            mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createUser(RoleType.DIRECTION_ADMIN));
            AssessmentTime currentTime = createTime(2);
            when(assessmentTimeRepository.findById(ASSESSMENT_TIME_ID)).thenReturn(Optional.of(currentTime));
            when(assessmentJudgementRepository.findCandidateScoreRows(ASSESSMENT_TIME_ID, null))
                    .thenReturn(
                            List.of(
                                    createCandidateScoreRowVOForUser(40L, "20260001", QUESTION_ID, 1, true, true),
                                    createCandidateScoreRowVOForUser(41L, "20260002", 21L, 1, true, true),
                                    createCandidateScoreRowVOForUser(42L, "20260003", 22L, 1, true, true)));
            when(assessmentDecisionRepository.findByAssessmentTimeId(ASSESSMENT_TIME_ID))
                    .thenReturn(List.of(createDecisionVOForUser(41L, true), createDecisionVOForUser(42L, false)));
            when(assessmentDecisionDomainService.isEliminatedFromPriorEpoch(40L, currentTime)).thenReturn(true);
            when(assessmentDecisionDomainService.isEliminatedFromPriorEpoch(41L, currentTime)).thenReturn(false);
            when(assessmentDecisionDomainService.isEliminatedFromPriorEpoch(42L, currentTime)).thenReturn(false);

            AssessmentDecisionWorkspaceVO result = assessmentJudgementAppService
                    .getDecisionWorkspace(ASSESSMENT_TIME_ID, null, null);

            List<Long> actualUserIds = result.getCandidates()
                    .stream()
                    .map(AssessmentDecisionCandidateVO::getCandidateUserId)
                    .toList();
            assertEquals(List.of(41L, 42L), actualUserIds);
            assertEquals(2L, result.getStatistics().getCandidates());
            assertEquals(0L, result.getStatistics().getPending());
            assertEquals(1L, result.getStatistics().getPassed());
            assertEquals(1L, result.getStatistics().getEliminated());
        }
    }

    /**
     * 验证当前轮次被淘汰的考生仍应保留在工作台中，避免「淘汰」筛选丢失数据。
     */
    @Test
    @DisplayName("录用决策工作台：当前轮次被淘汰的考生应保留")
    void getDecisionWorkspace_currentEpochEliminated_shouldKeep() {
        try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
            mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createUser(RoleType.DIRECTION_ADMIN));
            AssessmentTime currentTime = createTime(1);
            when(assessmentTimeRepository.findById(ASSESSMENT_TIME_ID)).thenReturn(Optional.of(currentTime));
            when(assessmentJudgementRepository.findCandidateScoreRows(ASSESSMENT_TIME_ID, null))
                    .thenReturn(
                            List.of(
                                    createCandidateScoreRowVOForUser(40L, "20260001", QUESTION_ID, 1, true, true),
                                    createCandidateScoreRowVOForUser(41L, "20260002", 21L, 1, true, true)));
            when(assessmentDecisionRepository.findByAssessmentTimeId(ASSESSMENT_TIME_ID))
                    .thenReturn(List.of(createDecisionVOForUser(40L, false)));
            when(assessmentDecisionDomainService.isEliminatedFromPriorEpoch(40L, currentTime)).thenReturn(false);
            when(assessmentDecisionDomainService.isEliminatedFromPriorEpoch(41L, currentTime)).thenReturn(false);

            AssessmentDecisionWorkspaceVO result = assessmentJudgementAppService
                    .getDecisionWorkspace(ASSESSMENT_TIME_ID, null, "ELIMINATED");

            List<Long> actualUserIds = result.getCandidates()
                    .stream()
                    .map(AssessmentDecisionCandidateVO::getCandidateUserId)
                    .toList();
            assertEquals(List.of(40L), actualUserIds);
            assertEquals(2L, result.getStatistics().getCandidates());
            assertEquals(1L, result.getStatistics().getPending());
            assertEquals(0L, result.getStatistics().getPassed());
            assertEquals(1L, result.getStatistics().getEliminated());
        }
    }

    /**
     * 验证方向管理员不能跨方向查看评分数据。
     */
    @Test
    @DisplayName("方向管理员查询其他方向：应拒绝")
    void listCandidateScoreboard_directionAdminOtherDirection_shouldThrowForbidden() {
        try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
            mockedUserCTX.when(UserCTX::getCurrentUser)
                    .thenReturn(
                            UserVO.builder()
                                    .id(REVIEWER_ID)
                                    .roleName(RoleType.DIRECTION_ADMIN.getName())
                                    .direction(com.bluenet.web.domain.model.enumerate.Direction.EMBEDDED)
                                    .build());
            when(assessmentTimeRepository.findById(ASSESSMENT_TIME_ID)).thenReturn(Optional.of(createTime()));

            assertThrows(
                    Forbidden.class,
                    () -> assessmentJudgementAppService.listCandidateScoreboard(ASSESSMENT_TIME_ID, null));
            verify(assessmentJudgementRepository, never()).findCandidateScoreRows(any(), any());
        }
    }

    // ========== 发布决策邮件测试 ==========

    /**
     * 验证发布决策会向已决策考生发送邮件并返回发送数量。
     */
    @Test
    @DisplayName("发布决策：应向已决策考生发送邮件")
    void publishDecisions_withDecidedCandidates_shouldSendEmails() {
        try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
            mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createUser(RoleType.DIRECTION_ADMIN));
            when(assessmentTimeRepository.findById(ASSESSMENT_TIME_ID)).thenReturn(Optional.of(createTime()));
            when(assessmentDecisionRepository.findByAssessmentTimeId(ASSESSMENT_TIME_ID))
                    .thenReturn(
                            List.of(
                                    createDecisionVOForUser(CANDIDATE_ID, true),
                                    createDecisionVOForUser(41L, false)));
            when(userDomainService.getUser(CANDIDATE_ID))
                    .thenReturn(Optional.of(createUserWithEmail(CANDIDATE_ID, "a@test.com")));
            when(userDomainService.getUser(41L))
                    .thenReturn(Optional.of(createUserWithEmail(41L, "b@test.com")));

            int result = assessmentJudgementAppService.publishDecisions(ASSESSMENT_TIME_ID);

            assertEquals(2, result);
            ArgumentCaptor<MessageRequest> messageCaptor = ArgumentCaptor.forClass(MessageRequest.class);
            verify(messageDispatcher, times(2)).dispatchAsync(messageCaptor.capture());
            assertTrue(
                    messageCaptor.getAllValues()
                            .stream()
                            .allMatch(
                                    request -> request.channel() == MessageChannel.EMAIL
                                            && request.contentType() == MessageContentType.HTML));
        }
    }

    /**
     * 验证无已决策考生时返回 0 且不发送邮件。
     */
    @Test
    @DisplayName("发布决策：无已决策考生时应返回0")
    void publishDecisions_noDecisions_shouldReturnZero() {
        try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
            mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createUser(RoleType.DIRECTION_ADMIN));
            when(assessmentTimeRepository.findById(ASSESSMENT_TIME_ID)).thenReturn(Optional.of(createTime()));
            when(assessmentDecisionRepository.findByAssessmentTimeId(ASSESSMENT_TIME_ID))
                    .thenReturn(List.of());

            int result = assessmentJudgementAppService.publishDecisions(ASSESSMENT_TIME_ID);

            assertEquals(0, result);
            verifyNoInteractions(messageDispatcher);
        }
    }

    /**
     * 验证考核时间不存在时抛出异常。
     */
    @Test
    @DisplayName("发布决策：考核时间不存在应抛出DataNotFound")
    void publishDecisions_timeNotFound_shouldThrowDataNotFound() {
        try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
            mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createUser(RoleType.DIRECTION_ADMIN));
            when(assessmentTimeRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(
                    com.bluenet.web.domain.exception.DataNotFound.class,
                    () -> assessmentJudgementAppService.publishDecisions(999L));
            verifyNoInteractions(messageDispatcher);
        }
    }

    /**
     * 验证普通成员不能发布决策。
     */
    @Test
    @DisplayName("发布决策：普通成员应被拒绝")
    void publishDecisions_member_shouldThrowForbidden() {
        try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
            mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createUser(RoleType.MEMBER));

            assertThrows(
                    Forbidden.class,
                    () -> assessmentJudgementAppService.publishDecisions(ASSESSMENT_TIME_ID));
            verifyNoInteractions(messageDispatcher);
        }
    }

    /**
     * 验证用户无邮箱时跳过发送。
     */
    @Test
    @DisplayName("发布决策：无邮箱用户应跳过")
    void publishDecisions_userWithoutEmail_shouldSkip() {
        try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
            mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createUser(RoleType.DIRECTION_ADMIN));
            when(assessmentTimeRepository.findById(ASSESSMENT_TIME_ID)).thenReturn(Optional.of(createTime()));
            when(assessmentDecisionRepository.findByAssessmentTimeId(ASSESSMENT_TIME_ID))
                    .thenReturn(
                            List.of(
                                    createDecisionVOForUser(CANDIDATE_ID, true),
                                    createDecisionVOForUser(41L, false)));
            when(userDomainService.getUser(CANDIDATE_ID))
                    .thenReturn(Optional.of(createUserWithEmail(CANDIDATE_ID, null)));
            when(userDomainService.getUser(41L))
                    .thenReturn(Optional.of(createUserWithEmail(41L, "b@test.com")));

            int result = assessmentJudgementAppService.publishDecisions(ASSESSMENT_TIME_ID);

            assertEquals(1, result);
            ArgumentCaptor<MessageRequest> messageCaptor = ArgumentCaptor.forClass(MessageRequest.class);
            verify(messageDispatcher, times(1)).dispatchAsync(messageCaptor.capture());
            assertEquals("b@test.com", messageCaptor.getValue().recipient());
            assertEquals(MessageChannel.EMAIL, messageCaptor.getValue().channel());
            assertEquals(MessageContentType.HTML, messageCaptor.getValue().contentType());
        }
    }

    /**
     * 验证邮件发送失败时记录日志并继续发送其余邮件。
     */
    @Test
    @DisplayName("发布决策：单封邮件失败应继续发送其余")
    void publishDecisions_emailFailure_shouldContinueSending() {
        try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
            mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createUser(RoleType.DIRECTION_ADMIN));
            when(assessmentTimeRepository.findById(ASSESSMENT_TIME_ID)).thenReturn(Optional.of(createTime()));
            when(assessmentDecisionRepository.findByAssessmentTimeId(ASSESSMENT_TIME_ID))
                    .thenReturn(
                            List.of(
                                    createDecisionVOForUser(CANDIDATE_ID, true),
                                    createDecisionVOForUser(41L, false)));
            when(userDomainService.getUser(CANDIDATE_ID))
                    .thenReturn(Optional.of(createUserWithEmail(CANDIDATE_ID, "a@test.com")));
            when(userDomainService.getUser(41L))
                    .thenReturn(Optional.of(createUserWithEmail(41L, "b@test.com")));
            doThrow(new RuntimeException("SMTP error")).when(messageDispatcher)
                    .dispatchAsync(argThat(request -> "a@test.com".equals(request.recipient())));

            int result = assessmentJudgementAppService.publishDecisions(ASSESSMENT_TIME_ID);

            assertEquals(1, result);
            verify(messageDispatcher).dispatchAsync(argThat(request -> "b@test.com".equals(request.recipient())));
        }
    }

    // ========== 确认最终评分测试 ==========

    /**
     * 验证方向管理员可以对文件上传题确认最终评分。
     */
    @Test
    @DisplayName("确认最终评分：方向管理员对文件上传题应创建ADMIN_FINALIZED评判")
    void finalizeScore_directionAdminFileUploadWithComment_shouldCreateAdminFinalized() {
        try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
            mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createUser(RoleType.DIRECTION_ADMIN));
            when(assessmentAnswerRepository.findById(ANSWER_ID)).thenReturn(Optional.of(createAnswerEntity()));
            when(assessmentQuestionRepository.findById(QUESTION_ID))
                    .thenReturn(Optional.of(createQuestion(QuestionType.FILE_UPLOAD)));
            when(commentRepository.existsByAnswerIdAndUserId(ANSWER_ID, REVIEWER_ID)).thenReturn(true);
            when(assessmentJudgementDomainService.finalizeJudgement(any(AssessmentJudgementVO.class)))
                    .thenReturn(createJudgementVO(JudgementSource.ADMIN_FINALIZED, ReviewerType.DIRECTION_ADMIN));

            AssessmentJudgementResult result = assessmentJudgementAppService.finalizeScore(
                    new AssessmentJudgementCommands.FinalizeScoreCommand(ANSWER_ID, BigDecimal.valueOf(8)));

            assertEquals(JudgementSource.ADMIN_FINALIZED, result.source());
            ArgumentCaptor<AssessmentJudgementVO> captor = ArgumentCaptor.forClass(AssessmentJudgementVO.class);
            verify(assessmentJudgementDomainService).finalizeJudgement(captor.capture());
            assertEquals(JudgementSource.ADMIN_FINALIZED, captor.getValue().getSource());
            assertEquals(REVIEWER_ID, captor.getValue().getReviewerId());
        }
    }

    /**
     * 验证普通成员不能确认最终评分。
     */
    @Test
    @DisplayName("确认最终评分：普通成员应被拒绝")
    void finalizeScore_member_shouldThrowForbidden() {
        try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
            mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createUser(RoleType.MEMBER));

            assertThrows(
                    Forbidden.class,
                    () -> assessmentJudgementAppService.finalizeScore(
                            new AssessmentJudgementCommands.FinalizeScoreCommand(ANSWER_ID, BigDecimal.ONE)));
            verifyNoInteractions(assessmentJudgementDomainService);
        }
    }

    /**
     * 验证非文件上传题不能确认最终评分。
     */
    @Test
    @DisplayName("确认最终评分：单选题应被拒绝")
    void finalizeScore_singleChoice_shouldThrowBadRequest() {
        try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
            mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createUser(RoleType.DIRECTION_ADMIN));
            when(assessmentAnswerRepository.findById(ANSWER_ID)).thenReturn(Optional.of(createAnswerEntity()));
            when(assessmentQuestionRepository.findById(QUESTION_ID))
                    .thenReturn(Optional.of(createQuestion(QuestionType.SINGLE_CHOICE)));

            assertThrows(
                    BadRequest.class,
                    () -> assessmentJudgementAppService.finalizeScore(
                            new AssessmentJudgementCommands.FinalizeScoreCommand(ANSWER_ID, BigDecimal.ONE)));
            verify(assessmentJudgementDomainService, never()).finalizeJudgement(any());
        }
    }

    /**
     * 验证最终评分超出满分应被拒绝。
     */
    @Test
    @DisplayName("确认最终评分：超出满分应被拒绝")
    void finalizeScore_aboveMax_shouldThrowBadRequest() {
        try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
            mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createUser(RoleType.DIRECTION_ADMIN));
            when(assessmentAnswerRepository.findById(ANSWER_ID)).thenReturn(Optional.of(createAnswerEntity()));
            when(assessmentQuestionRepository.findById(QUESTION_ID))
                    .thenReturn(Optional.of(createQuestion(QuestionType.FILE_UPLOAD)));

            assertThrows(
                    BadRequest.class,
                    () -> assessmentJudgementAppService.finalizeScore(
                            new AssessmentJudgementCommands.FinalizeScoreCommand(ANSWER_ID, BigDecimal.valueOf(11))));
            verify(assessmentJudgementDomainService, never()).finalizeJudgement(any());
        }
    }

    /**
     * 验证未发表评论就确认最终评分应被拒绝。
     */
    @Test
    @DisplayName("确认最终评分：未发表评论应被拒绝")
    void finalizeScore_noComment_shouldThrowBadRequest() {
        try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
            mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createUser(RoleType.DIRECTION_ADMIN));
            when(assessmentAnswerRepository.findById(ANSWER_ID)).thenReturn(Optional.of(createAnswerEntity()));
            when(assessmentQuestionRepository.findById(QUESTION_ID))
                    .thenReturn(Optional.of(createQuestion(QuestionType.FILE_UPLOAD)));
            when(commentRepository.existsByAnswerIdAndUserId(ANSWER_ID, REVIEWER_ID)).thenReturn(false);

            assertThrows(
                    BadRequest.class,
                    () -> assessmentJudgementAppService.finalizeScore(
                            new AssessmentJudgementCommands.FinalizeScoreCommand(ANSWER_ID, BigDecimal.valueOf(8))));
            verify(assessmentJudgementDomainService, never()).finalizeJudgement(any());
        }
    }

    /**
     * 验证队长首次确认最终评分时，会统一批量插入所有无评分的成员（包括队长）。
     */
    @Test
    @DisplayName("确认最终评分：队长首次评分应统一批量插入全队无评分成员")
    void finalizeScore_teamLeaderFirstTime_shouldPropagateToMembersWithoutFinalized() {
        try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
            mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createUser(RoleType.DIRECTION_ADMIN));

            Long teamId = 60L;
            AssessmentAnswer leaderAnswer = AssessmentAnswer.reconstruct(
                    ANSWER_ID,
                    CANDIDATE_ID,
                    QUESTION_ID,
                    null,
                    null,
                    null,
                    null,
                    teamId);
            when(assessmentAnswerRepository.findById(ANSWER_ID)).thenReturn(Optional.of(leaderAnswer));
            when(assessmentQuestionRepository.findById(QUESTION_ID))
                    .thenReturn(Optional.of(createQuestion(QuestionType.FILE_UPLOAD)));
            when(commentRepository.existsByAnswerIdAndUserId(ANSWER_ID, REVIEWER_ID)).thenReturn(true);

            // 首次评分：无现有 ADMIN_FINALIZED
            when(
                    assessmentJudgementRepository
                            .findLatestByAnswerIdAndSource(ANSWER_ID, JudgementSource.ADMIN_FINALIZED))
                                    .thenReturn(Optional.empty());

            // 队伍存在，当前用户是队长
            AssessmentTeam team = AssessmentTeam.reconstruct(
                    teamId,
                    ASSESSMENT_TIME_ID,
                    CANDIDATE_ID,
                    "team",
                    "code",
                    AssessmentTeam.TeamStatus.ACTIVE,
                    LocalDateTime.now());
            when(assessmentTeamRepository.findById(teamId)).thenReturn(Optional.of(team));

            Long memberId1 = 41L;
            Long memberId2 = 42L;
            Long memberAnswerId1 = 101L;
            Long memberAnswerId2 = 102L;
            when(assessmentAnswerRepository.findByTeamIdAndQuestionId(teamId, QUESTION_ID))
                    .thenReturn(
                            List.of(
                                    leaderAnswer,
                                    AssessmentAnswer.reconstruct(
                                            memberAnswerId1,
                                            memberId1,
                                            QUESTION_ID,
                                            null,
                                            null,
                                            null,
                                            null,
                                            teamId),
                                    AssessmentAnswer.reconstruct(
                                            memberAnswerId2,
                                            memberId2,
                                            QUESTION_ID,
                                            null,
                                            null,
                                            null,
                                            null,
                                            teamId)));

            // 全队均无 ADMIN_FINALIZED
            when(assessmentJudgementRepository.findAnswerIdsBySource(any(), eq(JudgementSource.ADMIN_FINALIZED)))
                    .thenReturn(List.of());

            AssessmentJudgement leaderEntity = AssessmentJudgement.reconstruct(
                    100L,
                    ANSWER_ID,
                    QUESTION_ID,
                    ASSESSMENT_TIME_ID,
                    CANDIDATE_ID,
                    BigDecimal.valueOf(8),
                    BigDecimal.TEN,
                    JudgementStatus.JUDGED,
                    null,
                    JudgementSource.ADMIN_FINALIZED,
                    REVIEWER_ID,
                    ReviewerType.DIRECTION_ADMIN,
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    LocalDateTime.now());
            when(
                    assessmentJudgementRepository
                            .findLatestByAnswerIdAndSource(ANSWER_ID, JudgementSource.ADMIN_FINALIZED))
                                    .thenReturn(Optional.empty())
                                    .thenReturn(Optional.of(leaderEntity));

            AssessmentJudgementResult result = assessmentJudgementAppService.finalizeScore(
                    new AssessmentJudgementCommands.FinalizeScoreCommand(ANSWER_ID, BigDecimal.valueOf(8)));

            assertNotNull(result);
            assertEquals(JudgementSource.ADMIN_FINALIZED, result.source());

            // 不调用 finalizeJudgement，统一通过 batchInsert
            verify(assessmentJudgementDomainService, never()).finalizeJudgement(any());

            // 统一批量插入，包含队长和 2 个队员
            ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
            verify(assessmentJudgementRepository).batchInsert(captor.capture());
            List<com.bluenet.web.domain.model.entity.AssessmentJudgement> judgements = captor.getValue();
            assertEquals(3, judgements.size());
            assertTrue(judgements.stream().anyMatch(j -> j.getUserId().equals(CANDIDATE_ID)));
            assertTrue(judgements.stream().anyMatch(j -> j.getUserId().equals(memberId1)));
            assertTrue(judgements.stream().anyMatch(j -> j.getUserId().equals(memberId2)));
        }
    }

    /**
     * 验证队长再次确认最终评分时，只更新自己，不再传播。
     */
    @Test
    @DisplayName("确认最终评分：队长再次评分只更新自己")
    void finalizeScore_teamLeaderReFinalize_shouldUpdateLeaderOnly() {
        try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
            mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createUser(RoleType.DIRECTION_ADMIN));

            Long teamId = 60L;
            AssessmentAnswer leaderAnswer = AssessmentAnswer.reconstruct(
                    ANSWER_ID,
                    CANDIDATE_ID,
                    QUESTION_ID,
                    null,
                    null,
                    null,
                    null,
                    teamId);
            when(assessmentAnswerRepository.findById(ANSWER_ID)).thenReturn(Optional.of(leaderAnswer));
            when(assessmentQuestionRepository.findById(QUESTION_ID))
                    .thenReturn(Optional.of(createQuestion(QuestionType.FILE_UPLOAD)));
            when(commentRepository.existsByAnswerIdAndUserId(ANSWER_ID, REVIEWER_ID)).thenReturn(true);

            // 再次评分：已有 ADMIN_FINALIZED
            when(
                    assessmentJudgementRepository
                            .findLatestByAnswerIdAndSource(ANSWER_ID, JudgementSource.ADMIN_FINALIZED))
                                    .thenReturn(
                                            Optional.of(
                                                    mock(
                                                            com.bluenet.web.domain.model.entity.AssessmentJudgement.class)));

            when(assessmentJudgementDomainService.finalizeJudgement(any(AssessmentJudgementVO.class)))
                    .thenReturn(createJudgementVO(JudgementSource.ADMIN_FINALIZED, ReviewerType.DIRECTION_ADMIN));

            AssessmentJudgementResult result = assessmentJudgementAppService.finalizeScore(
                    new AssessmentJudgementCommands.FinalizeScoreCommand(ANSWER_ID, BigDecimal.valueOf(9)));

            assertNotNull(result);
            assertEquals(JudgementSource.ADMIN_FINALIZED, result.source());

            // 只调用 finalizeJudgement，不调用 batchInsert
            verify(assessmentJudgementDomainService).finalizeJudgement(any(AssessmentJudgementVO.class));
            verify(assessmentJudgementRepository, never()).batchInsert(any());
        }
    }

    /**
     * 验证给队员单独评分时，只更新该队员。
     */
    @Test
    @DisplayName("确认最终评分：队员单独评分只更新自己")
    void finalizeScore_teamMemberIndividual_shouldUpdateMemberOnly() {
        try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
            mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createUser(RoleType.DIRECTION_ADMIN));

            Long teamId = 60L;
            Long memberId = 41L;
            Long memberAnswerId = 101L;
            AssessmentAnswer memberAnswer = AssessmentAnswer.reconstruct(
                    memberAnswerId,
                    memberId,
                    QUESTION_ID,
                    null,
                    null,
                    null,
                    null,
                    teamId);
            when(assessmentAnswerRepository.findById(memberAnswerId)).thenReturn(Optional.of(memberAnswer));
            when(assessmentQuestionRepository.findById(QUESTION_ID))
                    .thenReturn(Optional.of(createQuestion(QuestionType.FILE_UPLOAD)));
            when(commentRepository.existsByAnswerIdAndUserId(memberAnswerId, REVIEWER_ID)).thenReturn(true);

            // 首次评分（对该队员而言）
            when(
                    assessmentJudgementRepository.findLatestByAnswerIdAndSource(
                            memberAnswerId,
                            JudgementSource.ADMIN_FINALIZED))
                                    .thenReturn(Optional.empty());

            // 不是队长
            AssessmentTeam team = AssessmentTeam.reconstruct(
                    teamId,
                    ASSESSMENT_TIME_ID,
                    CANDIDATE_ID,
                    "team",
                    "code",
                    AssessmentTeam.TeamStatus.ACTIVE,
                    LocalDateTime.now());
            when(assessmentTeamRepository.findById(teamId)).thenReturn(Optional.of(team));

            when(assessmentJudgementDomainService.finalizeJudgement(any(AssessmentJudgementVO.class)))
                    .thenReturn(createJudgementVO(JudgementSource.ADMIN_FINALIZED, ReviewerType.DIRECTION_ADMIN));

            AssessmentJudgementResult result = assessmentJudgementAppService.finalizeScore(
                    new AssessmentJudgementCommands.FinalizeScoreCommand(memberAnswerId, BigDecimal.valueOf(7)));

            assertNotNull(result);
            assertEquals(JudgementSource.ADMIN_FINALIZED, result.source());

            // 只调用 finalizeJudgement，不调用 batchInsert
            verify(assessmentJudgementDomainService).finalizeJudgement(any(AssessmentJudgementVO.class));
            verify(assessmentJudgementRepository, never()).batchInsert(any());
        }
    }

    /**
     * 验证队长首次评分时，已有评分的队员不会被覆盖。
     */
    @Test
    @DisplayName("确认最终评分：队长首次评分时跳过已有评分的队员")
    void finalizeScore_teamLeaderFirstTime_someMembersFinalized_shouldSkipFinalizedMembers() {
        try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
            mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createUser(RoleType.DIRECTION_ADMIN));

            Long teamId = 60L;
            AssessmentAnswer leaderAnswer = AssessmentAnswer.reconstruct(
                    ANSWER_ID,
                    CANDIDATE_ID,
                    QUESTION_ID,
                    null,
                    null,
                    null,
                    null,
                    teamId);
            when(assessmentAnswerRepository.findById(ANSWER_ID)).thenReturn(Optional.of(leaderAnswer));
            when(assessmentQuestionRepository.findById(QUESTION_ID))
                    .thenReturn(Optional.of(createQuestion(QuestionType.FILE_UPLOAD)));
            when(commentRepository.existsByAnswerIdAndUserId(ANSWER_ID, REVIEWER_ID)).thenReturn(true);

            // 首次评分：无现有 ADMIN_FINALIZED
            when(
                    assessmentJudgementRepository
                            .findLatestByAnswerIdAndSource(ANSWER_ID, JudgementSource.ADMIN_FINALIZED))
                                    .thenReturn(Optional.empty());

            // 队伍存在，当前用户是队长
            AssessmentTeam team = AssessmentTeam.reconstruct(
                    teamId,
                    ASSESSMENT_TIME_ID,
                    CANDIDATE_ID,
                    "team",
                    "code",
                    AssessmentTeam.TeamStatus.ACTIVE,
                    LocalDateTime.now());
            when(assessmentTeamRepository.findById(teamId)).thenReturn(Optional.of(team));

            Long memberId1 = 41L;
            Long memberId2 = 42L;
            Long memberAnswerId1 = 101L;
            Long memberAnswerId2 = 102L;
            when(assessmentAnswerRepository.findByTeamIdAndQuestionId(teamId, QUESTION_ID))
                    .thenReturn(
                            List.of(
                                    leaderAnswer,
                                    AssessmentAnswer.reconstruct(
                                            memberAnswerId1,
                                            memberId1,
                                            QUESTION_ID,
                                            null,
                                            null,
                                            null,
                                            null,
                                            teamId),
                                    AssessmentAnswer.reconstruct(
                                            memberAnswerId2,
                                            memberId2,
                                            QUESTION_ID,
                                            null,
                                            null,
                                            null,
                                            null,
                                            teamId)));

            // member1 已有 ADMIN_FINALIZED，队长和 member2 没有
            when(assessmentJudgementRepository.findAnswerIdsBySource(any(), eq(JudgementSource.ADMIN_FINALIZED)))
                    .thenReturn(List.of(memberAnswerId1));

            AssessmentJudgement leaderEntity = AssessmentJudgement.reconstruct(
                    100L,
                    ANSWER_ID,
                    QUESTION_ID,
                    ASSESSMENT_TIME_ID,
                    CANDIDATE_ID,
                    BigDecimal.valueOf(8),
                    BigDecimal.TEN,
                    JudgementStatus.JUDGED,
                    null,
                    JudgementSource.ADMIN_FINALIZED,
                    REVIEWER_ID,
                    ReviewerType.DIRECTION_ADMIN,
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    LocalDateTime.now());
            when(
                    assessmentJudgementRepository
                            .findLatestByAnswerIdAndSource(ANSWER_ID, JudgementSource.ADMIN_FINALIZED))
                                    .thenReturn(Optional.empty())
                                    .thenReturn(Optional.of(leaderEntity));

            AssessmentJudgementResult result = assessmentJudgementAppService.finalizeScore(
                    new AssessmentJudgementCommands.FinalizeScoreCommand(ANSWER_ID, BigDecimal.valueOf(8)));

            assertNotNull(result);

            // 统一批量插入：队长 + member2（member1 已存在故跳过）
            verify(assessmentJudgementDomainService, never()).finalizeJudgement(any());
            ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
            verify(assessmentJudgementRepository).batchInsert(captor.capture());
            List<com.bluenet.web.domain.model.entity.AssessmentJudgement> judgements = captor.getValue();
            assertEquals(2, judgements.size());
            assertTrue(judgements.stream().anyMatch(j -> j.getUserId().equals(CANDIDATE_ID)));
            assertTrue(judgements.stream().anyMatch(j -> j.getUserId().equals(memberId2)));
            assertTrue(judgements.stream().noneMatch(j -> j.getUserId().equals(memberId1)));
        }
    }

    // ========== 测试数据构造 ==========

    private UserVO createUser(RoleType roleType) {
        return UserVO.builder()
                .id(REVIEWER_ID)
                .roleName(roleType.getName())
                .direction(com.bluenet.web.domain.model.enumerate.Direction.COMPUTER_VISION)
                .build();
    }

    private UserVO createUserWithEmail(Long userId, String email) {
        return UserVO.builder()
                .id(userId)
                .email(email)
                .nickname("用户" + userId)
                .build();
    }

    private AssessmentAnswer createAnswerEntity() {
        return AssessmentAnswer.reconstruct(ANSWER_ID, CANDIDATE_ID, QUESTION_ID, null, null, null, null, null);
    }

    private AssessmentQuestion createQuestion(QuestionType questionType) {
        return AssessmentQuestion.reconstruct(
                QUESTION_ID,
                ASSESSMENT_TIME_ID,
                1,
                questionType,
                "题目",
                null,
                null,
                BigDecimal.TEN);
    }

    private AssessmentJudgementVO createJudgementVO(JudgementSource source, ReviewerType reviewerType) {
        return AssessmentJudgementVO.builder()
                .id(100L)
                .answerId(ANSWER_ID)
                .questionId(QUESTION_ID)
                .assessmentTimeId(ASSESSMENT_TIME_ID)
                .userId(CANDIDATE_ID)
                .score(BigDecimal.TEN)
                .maxScore(BigDecimal.TEN)
                .status(JudgementStatus.JUDGED)
                .source(source)
                .reviewerId(reviewerType == null ? null : REVIEWER_ID)
                .reviewerType(reviewerType)
                .judgedAt(LocalDateTime.now())
                .build();
    }

    private AssessmentDecisionVO createDecisionVO(boolean passed) {
        return AssessmentDecisionVO.builder()
                .id(200L)
                .userId(CANDIDATE_ID)
                .assessmentTimeId(ASSESSMENT_TIME_ID)
                .passed(passed)
                .decidedBy(REVIEWER_ID)
                .decisionComment("通过")
                .decidedAt(LocalDateTime.now())
                .build();
    }

    private AssessmentDecisionVO createDecisionVOForUser(Long userId, boolean passed) {
        return AssessmentDecisionVO.builder()
                .id(userId + 3000)
                .userId(userId)
                .assessmentTimeId(ASSESSMENT_TIME_ID)
                .passed(passed)
                .decidedBy(REVIEWER_ID)
                .decidedAt(LocalDateTime.now())
                .build();
    }

    private AssessmentTime createTime() {
        return createTime(1);
    }

    private AssessmentTime createTime(int epoch) {
        return AssessmentTime.reconstruct(
                ASSESSMENT_TIME_ID,
                com.bluenet.web.domain.model.enumerate.Direction.COMPUTER_VISION,
                epoch,
                2026,
                null,
                null,
                false,
                null,
                null,
                false);
    }

    private AssessmentTime createTimeWithNullGrade() {
        return AssessmentTime.reconstruct(
                ASSESSMENT_TIME_ID,
                com.bluenet.web.domain.model.enumerate.Direction.COMPUTER_VISION,
                1,
                null,
                null,
                null,
                false,
                null,
                null,
                false);
    }

    private AssessmentQuestionSubmissionVO createSubmissionVO(boolean judged) {
        AssessmentQuestionSubmissionVO.AssessmentQuestionSubmissionVOBuilder builder = AssessmentQuestionSubmissionVO
                .builder()
                .answerId(ANSWER_ID)
                .questionId(QUESTION_ID)
                .assessmentTimeId(ASSESSMENT_TIME_ID)
                .questionNo(1)
                .questionTitle("作品提交")
                .questionType(QuestionType.FILE_UPLOAD)
                .maxScore(BigDecimal.TEN)
                .candidateUserId(CANDIDATE_ID)
                .studentId("20260001")
                .username("张三")
                .submitTime(LocalDateTime.now());
        if (judged) {
            builder.judgementId(100L)
                    .judgementScore(BigDecimal.TEN)
                    .judgementMaxScore(BigDecimal.TEN)
                    .judgementStatus(JudgementStatus.JUDGED)
                    .source(JudgementSource.MANUAL)
                    .judgedAt(LocalDateTime.now());
        }
        return builder.build();
    }

    private AssessmentQuestionSubmissionHistoryVO createSubmissionHistoryVO(Long judgementId, BigDecimal score,
            boolean selectedBest) {
        AssessmentJudgementVO judgement = AssessmentJudgementVO.builder()
                .id(judgementId)
                .answerId(ANSWER_ID)
                .questionId(QUESTION_ID)
                .assessmentTimeId(ASSESSMENT_TIME_ID)
                .userId(CANDIDATE_ID)
                .score(score)
                .maxScore(BigDecimal.TEN)
                .status(JudgementStatus.JUDGED)
                .source(JudgementSource.AUTO)
                .judgedAt(LocalDateTime.now())
                .build();
        return AssessmentQuestionSubmissionHistoryVO.builder()
                .judgement(judgement)
                .selectedBest(selectedBest)
                .build();
    }

    private AssessmentCandidateScoreRowVO createCandidateScoreRowVO(
            Long questionId,
            Integer questionNo,
            boolean submitted,
            boolean judged) {
        return createCandidateScoreRowVOForUser(CANDIDATE_ID, "20260001", questionId, questionNo, submitted, judged);
    }

    private AssessmentCandidateScoreRowVO createCandidateScoreRowVOForUser(
            Long userId,
            String studentId,
            Long questionId,
            Integer questionNo,
            boolean submitted,
            boolean judged) {
        AssessmentCandidateScoreRowVO.AssessmentCandidateScoreRowVOBuilder builder = AssessmentCandidateScoreRowVO
                .builder()
                .candidateUserId(userId)
                .studentId(studentId)
                .username("候选人" + userId)
                .questionId(questionId)
                .questionNo(questionNo)
                .questionTitle("题目" + questionNo)
                .questionType(QuestionType.FILE_UPLOAD)
                .maxScore(BigDecimal.TEN);
        if (submitted) {
            builder.answerId(questionId + 1000)
                    .submitTime(LocalDateTime.now());
        }
        if (judged) {
            builder.judgementId(questionId + 2000)
                    .judgementScore(BigDecimal.TEN)
                    .judgementMaxScore(BigDecimal.TEN)
                    .judgementStatus(JudgementStatus.JUDGED)
                    .source(JudgementSource.MANUAL)
                    .judgedAt(LocalDateTime.now());
        }
        return builder.build();
    }
}
