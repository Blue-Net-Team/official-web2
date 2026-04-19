package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.assessment_judgement.AssessmentDecisionDTO;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentDecisionRequestDTO;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentJudgementDTO;
import com.bluenet.web.api.dto.assessment_judgement.ManualReviewRequestDTO;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.model.enumerate.JudgementSource;
import com.bluenet.web.domain.model.enumerate.JudgementStatus;
import com.bluenet.web.domain.model.enumerate.QuestionType;
import com.bluenet.web.domain.model.enumerate.ReviewerType;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.model.vo.AssessmentAnswerVO;
import com.bluenet.web.domain.model.vo.AssessmentDecisionVO;
import com.bluenet.web.domain.model.vo.AssessmentJudgementVO;
import com.bluenet.web.domain.model.vo.AssessmentQuestionVO;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.service.AssessmentAnswerDomainService;
import com.bluenet.web.domain.service.AssessmentDecisionDomainService;
import com.bluenet.web.domain.service.AssessmentJudgementDomainService;
import com.bluenet.web.domain.service.AssessmentQuestionDomainService;
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

    @InjectMocks
    private AssessmentJudgementServiceImpl assessmentJudgementService;

    @Test
    @DisplayName("查询答案最新评判：应返回DTO")
    void getLatestByAnswerId_existing_shouldReturnDTO() {
        when(assessmentJudgementDomainService.getLatestByAnswerId(ANSWER_ID))
                .thenReturn(createJudgementVO(JudgementSource.AUTO, null));

        AssessmentJudgementDTO result = assessmentJudgementService.getLatestByAnswerId(ANSWER_ID);

        assertEquals(ANSWER_ID, result.getAnswerId());
        assertEquals(JudgementSource.AUTO, result.getSource());
    }

    @Test
    @DisplayName("查询题目评判列表：应返回DTO列表")
    void listByQuestionId_shouldReturnDTOList() {
        when(assessmentJudgementDomainService.listByQuestionId(QUESTION_ID))
                .thenReturn(List.of(createJudgementVO(JudgementSource.AUTO, null)));

        List<AssessmentJudgementDTO> result = assessmentJudgementService.listByQuestionId(QUESTION_ID);

        assertEquals(1, result.size());
        assertEquals(QUESTION_ID, result.get(0).getQuestionId());
    }

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
            // 文件上传题人工评分不写客观题AC/WA结果码，避免进入客观题自动通过率统计。
            assertNull(captor.getValue().getResultCode());
            assertEquals(JudgementStatus.JUDGED, captor.getValue().getStatus());
            assertEquals(REVIEWER_ID, captor.getValue().getReviewerId());
        }
    }

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

    private UserVO createUser(RoleType roleType) {
        return UserVO.builder()
                .id(REVIEWER_ID)
                .roleName(roleType.getName())
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
}
