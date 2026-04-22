package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.assessment_judgement.AssessmentCandidateScoreboardDTO;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentDecisionDTO;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentDecisionRequestDTO;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentDecisionWorkspaceDTO;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentJudgementDTO;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentQuestionScoreboardDTO;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentQuestionSubmissionDTO;
import com.bluenet.web.api.dto.assessment_judgement.ManualReviewRequestDTO;
import com.bluenet.web.application.converter.AssessmentJudgementConverter;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.model.enumerate.JudgementSource;
import com.bluenet.web.domain.model.enumerate.JudgementStatus;
import com.bluenet.web.domain.model.enumerate.QuestionType;
import com.bluenet.web.domain.model.enumerate.ReviewerType;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.model.vo.AssessmentAnswerVO;
import com.bluenet.web.domain.model.vo.AssessmentCandidateScoreRowVO;
import com.bluenet.web.domain.model.vo.AssessmentDecisionVO;
import com.bluenet.web.domain.model.vo.AssessmentJudgementVO;
import com.bluenet.web.domain.model.vo.AssessmentQuestionScoreboardVO;
import com.bluenet.web.domain.model.vo.AssessmentQuestionSubmissionHistoryVO;
import com.bluenet.web.domain.model.vo.AssessmentQuestionSubmissionVO;
import com.bluenet.web.domain.model.vo.AssessmentQuestionVO;
import com.bluenet.web.domain.model.vo.AssessmentTimeVO;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.repository.AssessmentDecisionRepository;
import com.bluenet.web.domain.repository.AssessmentJudgementRepository;
import com.bluenet.web.domain.service.AssessmentAnswerDomainService;
import com.bluenet.web.domain.service.AssessmentDecisionDomainService;
import com.bluenet.web.domain.service.AssessmentJudgementDomainService;
import com.bluenet.web.domain.service.AssessmentQuestionDomainService;
import com.bluenet.web.domain.service.AssessmentTimeDomainService;
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
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * AssessmentJudgementServiceImpl 单元测试。
 */
@DisplayName("AssessmentJudgementServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class AssessmentJudgementServiceImplTest {
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
    private AssessmentAnswerDomainService assessmentAnswerDomainService;

    @Mock
    private AssessmentQuestionDomainService assessmentQuestionDomainService;

    @Mock
    private AssessmentTimeDomainService assessmentTimeDomainService;

    @Mock
    private AssessmentJudgementRepository assessmentJudgementRepository;

    @Mock
    private AssessmentDecisionRepository assessmentDecisionRepository;

    @Mock
    private UserDomainService userDomainService;

    @Mock
    private MessageDispatcher messageDispatcher;

    @Spy
    private AssessmentJudgementConverter assessmentJudgementConverter = new AssessmentJudgementConverter();

    @InjectMocks
    private AssessmentJudgementServiceImpl assessmentJudgementService;

    /**
     * 验证答案最新评判查询会转换为接口 DTO。
     */
    @Test
    @DisplayName("查询答案最新评判：应返回DTO")
    void getLatestByAnswerId_existing_shouldReturnDTO() {
        when(assessmentJudgementDomainService.getLatestByAnswerId(ANSWER_ID))
                .thenReturn(createJudgementVO(JudgementSource.AUTO, null));

        AssessmentJudgementDTO result = assessmentJudgementService.getLatestByAnswerId(ANSWER_ID);

        assertEquals(ANSWER_ID, result.getAnswerId());
        assertEquals(JudgementSource.AUTO, result.getSource());
    }

    /**
     * 验证题目评判列表查询会转换为 DTO 列表。
     */
    @Test
    @DisplayName("查询题目评判列表：应返回DTO列表")
    void listByQuestionId_shouldReturnDTOList() {
        when(assessmentJudgementDomainService.listByQuestionId(QUESTION_ID))
                .thenReturn(List.of(createJudgementVO(JudgementSource.AUTO, null)));

        List<AssessmentJudgementDTO> result = assessmentJudgementService.listByQuestionId(QUESTION_ID);

        assertEquals(1, result.size());
        assertEquals(QUESTION_ID, result.get(0).getQuestionId());
    }

    /**
     * 验证成员可以对文件上传题创建人工评判。
     */
    @Test
    @DisplayName("成员人工评分文件上传题：应创建MANUAL评判")
    void reviewFileUploadAnswer_memberAndFileUpload_shouldCreateManualJudgement() {
        try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
            mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createUser(RoleType.MEMBER));
            when(assessmentAnswerDomainService.getAnswerById(ANSWER_ID)).thenReturn(createAnswerVO());
            when(assessmentQuestionDomainService.getQuestionById(QUESTION_ID))
                    .thenReturn(createQuestionVO(QuestionType.FILE_UPLOAD));
            when(assessmentJudgementDomainService.createJudgement(any(AssessmentJudgementVO.class)))
                    .thenReturn(createJudgementVO(JudgementSource.MANUAL, ReviewerType.MEMBER));

            AssessmentJudgementDTO result = assessmentJudgementService.reviewFileUploadAnswer(
                    ManualReviewRequestDTO.builder()
                            .answerId(ANSWER_ID)
                            .score(BigDecimal.valueOf(8))
                            .comment("完成度较好")
                            .build());

            assertEquals(JudgementSource.MANUAL, result.getSource());
            assertEquals(ReviewerType.MEMBER, result.getReviewerType());
            ArgumentCaptor<AssessmentJudgementVO> captor = ArgumentCaptor.forClass(AssessmentJudgementVO.class);
            verify(assessmentJudgementDomainService).createJudgement(captor.capture());
            assertNull(captor.getValue().getResultCode());
            assertEquals(JudgementStatus.JUDGED, captor.getValue().getStatus());
            assertEquals(REVIEWER_ID, captor.getValue().getReviewerId());
        }
    }

    /**
     * 验证考生不能调用人工评分接口。
     */
    @Test
    @DisplayName("考生人工评分：应拒绝")
    void reviewFileUploadAnswer_candidate_shouldThrowForbidden() {
        try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
            mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createUser(RoleType.CANDIDATE));

            assertThrows(
                    Forbidden.class,
                    () -> assessmentJudgementService.reviewFileUploadAnswer(
                            ManualReviewRequestDTO.builder().answerId(ANSWER_ID).score(BigDecimal.ONE).build()));
            verifyNoInteractions(assessmentAnswerDomainService);
        }
    }

    /**
     * 验证单选题不能被人工评分覆盖。
     */
    @Test
    @DisplayName("人工评分单选题：应拒绝覆盖自动评判")
    void reviewFileUploadAnswer_singleChoice_shouldThrowBadRequest() {
        try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
            mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createUser(RoleType.MEMBER));
            when(assessmentAnswerDomainService.getAnswerById(ANSWER_ID)).thenReturn(createAnswerVO());
            when(assessmentQuestionDomainService.getQuestionById(QUESTION_ID))
                    .thenReturn(createQuestionVO(QuestionType.SINGLE_CHOICE));

            assertThrows(
                    BadRequest.class,
                    () -> assessmentJudgementService.reviewFileUploadAnswer(
                            ManualReviewRequestDTO.builder().answerId(ANSWER_ID).score(BigDecimal.ONE).build()));
            verify(assessmentJudgementDomainService, never()).createJudgement(any());
        }
    }

    /**
     * 验证算法题不能被人工评分覆盖。
     */
    @Test
    @DisplayName("人工评分算法题：应拒绝覆盖自动评判")
    void reviewFileUploadAnswer_algorithm_shouldThrowBadRequest() {
        try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
            mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createUser(RoleType.MEMBER));
            when(assessmentAnswerDomainService.getAnswerById(ANSWER_ID)).thenReturn(createAnswerVO());
            when(assessmentQuestionDomainService.getQuestionById(QUESTION_ID))
                    .thenReturn(createQuestionVO(QuestionType.ALGORITHM));

            assertThrows(
                    BadRequest.class,
                    () -> assessmentJudgementService.reviewFileUploadAnswer(
                            ManualReviewRequestDTO.builder().answerId(ANSWER_ID).score(BigDecimal.ONE).build()));
            verify(assessmentJudgementDomainService, never()).createJudgement(any());
        }
    }

    /**
     * 验证人工评分不能超过题目满分。
     */
    @Test
    @DisplayName("人工评分超出满分：应拒绝")
    void reviewFileUploadAnswer_scoreAboveMax_shouldThrowBadRequest() {
        try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
            mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createUser(RoleType.MEMBER));
            when(assessmentAnswerDomainService.getAnswerById(ANSWER_ID)).thenReturn(createAnswerVO());
            when(assessmentQuestionDomainService.getQuestionById(QUESTION_ID))
                    .thenReturn(createQuestionVO(QuestionType.FILE_UPLOAD));

            assertThrows(
                    BadRequest.class,
                    () -> assessmentJudgementService.reviewFileUploadAnswer(
                            ManualReviewRequestDTO.builder()
                                    .answerId(ANSWER_ID)
                                    .score(BigDecimal.valueOf(11))
                                    .build()));
        }
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

            AssessmentDecisionDTO result = assessmentJudgementService.decideAssessment(
                    AssessmentDecisionRequestDTO.builder()
                            .userId(CANDIDATE_ID)
                            .assessmentTimeId(ASSESSMENT_TIME_ID)
                            .passed(true)
                            .decisionComment("通过")
                            .build());

            assertTrue(result.getPassed());
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
                    () -> assessmentJudgementService.decideAssessment(
                            AssessmentDecisionRequestDTO.builder()
                                    .userId(CANDIDATE_ID)
                                    .assessmentTimeId(ASSESSMENT_TIME_ID)
                                    .passed(true)
                                    .build()));
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
            when(assessmentTimeDomainService.getById(ASSESSMENT_TIME_ID)).thenReturn(Optional.of(createTimeVO()));
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

            List<AssessmentQuestionScoreboardDTO> result = assessmentJudgementService
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
            when(assessmentQuestionDomainService.getQuestionById(QUESTION_ID))
                    .thenReturn(createQuestionVO(QuestionType.FILE_UPLOAD));
            when(assessmentTimeDomainService.getById(ASSESSMENT_TIME_ID)).thenReturn(Optional.of(createTimeVO()));
            when(assessmentJudgementRepository.findQuestionSubmissions(QUESTION_ID, null, "JUDGED"))
                    .thenReturn(List.of(createSubmissionVO(true)));
            when(assessmentJudgementRepository.findQuestionSubmissionHistories(QUESTION_ID, List.of(CANDIDATE_ID)))
                    .thenReturn(List.of(createSubmissionHistoryVO(100L, BigDecimal.TEN, true)));

            List<AssessmentQuestionSubmissionDTO> result = assessmentJudgementService
                    .listQuestionSubmissions(QUESTION_ID, null, "judged");

            assertEquals(1, result.size());
            assertEquals(CANDIDATE_ID, result.get(0).getCandidateUserId());
            assertNotNull(result.get(0).getLatestJudgement());
            assertEquals(BigDecimal.TEN, result.get(0).getLatestJudgement().getScore());
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
            when(assessmentTimeDomainService.getById(ASSESSMENT_TIME_ID)).thenReturn(Optional.of(createTimeVO()));
            when(assessmentJudgementRepository.findCandidateScoreRows(ASSESSMENT_TIME_ID, null))
                    .thenReturn(
                            List.of(
                                    createCandidateScoreRowVO(QUESTION_ID, 1, true, true),
                                    createCandidateScoreRowVO(21L, 2, true, false)));

            List<AssessmentCandidateScoreboardDTO> result = assessmentJudgementService
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
            when(assessmentTimeDomainService.getById(ASSESSMENT_TIME_ID)).thenReturn(Optional.of(createTimeVO()));
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

            AssessmentDecisionWorkspaceDTO result = assessmentJudgementService
                    .getDecisionWorkspace(ASSESSMENT_TIME_ID, null, null);

            assertEquals(3L, result.getStatistics().getCandidates());
            assertEquals(1L, result.getStatistics().getPending());
            assertEquals(1L, result.getStatistics().getPassed());
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
            when(assessmentTimeDomainService.getById(ASSESSMENT_TIME_ID)).thenReturn(Optional.of(createTimeVO()));

            assertThrows(
                    Forbidden.class,
                    () -> assessmentJudgementService.listCandidateScoreboard(ASSESSMENT_TIME_ID, null));
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
            when(assessmentTimeDomainService.getById(ASSESSMENT_TIME_ID)).thenReturn(Optional.of(createTimeVO()));
            when(assessmentDecisionRepository.findByAssessmentTimeId(ASSESSMENT_TIME_ID))
                    .thenReturn(
                            List.of(
                                    createDecisionVOForUser(CANDIDATE_ID, true),
                                    createDecisionVOForUser(41L, false)));
            when(userDomainService.getUser(CANDIDATE_ID))
                    .thenReturn(Optional.of(createUserWithEmail(CANDIDATE_ID, "a@test.com")));
            when(userDomainService.getUser(41L))
                    .thenReturn(Optional.of(createUserWithEmail(41L, "b@test.com")));

            int result = assessmentJudgementService.publishDecisions(ASSESSMENT_TIME_ID);

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
            when(assessmentTimeDomainService.getById(ASSESSMENT_TIME_ID)).thenReturn(Optional.of(createTimeVO()));
            when(assessmentDecisionRepository.findByAssessmentTimeId(ASSESSMENT_TIME_ID))
                    .thenReturn(List.of());

            int result = assessmentJudgementService.publishDecisions(ASSESSMENT_TIME_ID);

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
            when(assessmentTimeDomainService.getById(999L)).thenReturn(Optional.empty());

            assertThrows(
                    com.bluenet.web.domain.exception.DataNotFound.class,
                    () -> assessmentJudgementService.publishDecisions(999L));
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
                    () -> assessmentJudgementService.publishDecisions(ASSESSMENT_TIME_ID));
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
            when(assessmentTimeDomainService.getById(ASSESSMENT_TIME_ID)).thenReturn(Optional.of(createTimeVO()));
            when(assessmentDecisionRepository.findByAssessmentTimeId(ASSESSMENT_TIME_ID))
                    .thenReturn(
                            List.of(
                                    createDecisionVOForUser(CANDIDATE_ID, true),
                                    createDecisionVOForUser(41L, false)));
            when(userDomainService.getUser(CANDIDATE_ID))
                    .thenReturn(Optional.of(createUserWithEmail(CANDIDATE_ID, null)));
            when(userDomainService.getUser(41L))
                    .thenReturn(Optional.of(createUserWithEmail(41L, "b@test.com")));

            int result = assessmentJudgementService.publishDecisions(ASSESSMENT_TIME_ID);

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
            when(assessmentTimeDomainService.getById(ASSESSMENT_TIME_ID)).thenReturn(Optional.of(createTimeVO()));
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

            int result = assessmentJudgementService.publishDecisions(ASSESSMENT_TIME_ID);

            assertEquals(1, result);
            verify(messageDispatcher).dispatchAsync(argThat(request -> "b@test.com".equals(request.recipient())));
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

    private AssessmentAnswerVO createAnswerVO() {
        return AssessmentAnswerVO.builder()
                .id(ANSWER_ID)
                .userId(CANDIDATE_ID)
                .questionId(QUESTION_ID)
                .build();
    }

    private AssessmentQuestionVO createQuestionVO(QuestionType questionType) {
        return AssessmentQuestionVO.builder()
                .id(QUESTION_ID)
                .assessmentTimeId(ASSESSMENT_TIME_ID)
                .questionType(questionType)
                .score(BigDecimal.TEN)
                .build();
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

    private AssessmentTimeVO createTimeVO() {
        return AssessmentTimeVO.builder()
                .id(ASSESSMENT_TIME_ID)
                .direction(com.bluenet.web.domain.model.enumerate.Direction.COMPUTER_VISION)
                .grade(2026)
                .epoch(1)
                .build();
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
