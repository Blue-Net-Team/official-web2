package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.assessment_judgement.AssessmentDecisionDTO;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentDecisionRequestDTO;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentJudgementDTO;
import com.bluenet.web.api.dto.assessment_judgement.ManualReviewRequestDTO;
import com.bluenet.web.application.service.AssessmentJudgementService;
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
import com.bluenet.web.infrastructure.security.util.RoleHierarchy;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 考核评判应用服务实现。
 */
@Service
@RequiredArgsConstructor
public class AssessmentJudgementServiceImpl implements AssessmentJudgementService {
    private final AssessmentJudgementDomainService assessmentJudgementDomainService;
    private final AssessmentDecisionDomainService assessmentDecisionDomainService;
    private final AssessmentAnswerDomainService assessmentAnswerDomainService;
    private final AssessmentQuestionDomainService assessmentQuestionDomainService;

    @Override
    public AssessmentJudgementDTO getLatestByAnswerId(Long answerId) {
        return convertToDTO(assessmentJudgementDomainService.getLatestByAnswerId(answerId));
    }

    @Override
    public List<AssessmentJudgementDTO> listByQuestionId(Long questionId) {
        return assessmentJudgementDomainService.listByQuestionId(questionId)
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Override
    @Transactional
    public AssessmentJudgementDTO reviewFileUploadAnswer(ManualReviewRequestDTO request) {
        UserVO currentUser = requireCurrentUser();
        RoleType roleType = requireRole(currentUser);
        if (!RoleHierarchy.isMemberOrAbove(roleType)) {
            throw new Forbidden("只有团队成员及以上权限可以人工评分");
        }

        AssessmentAnswerVO answer = assessmentAnswerDomainService.getAnswerById(request.getAnswerId());
        AssessmentQuestionVO question = assessmentQuestionDomainService.getQuestionById(answer.getQuestionId());
        if (question.getQuestionType() != QuestionType.FILE_UPLOAD) {
            // 自动题的分数来自同步/异步评判，人工接口不能覆盖。
            throw new BadRequest("只有文件上传题可以人工评分");
        }
        if (request.getScore().compareTo(java.math.BigDecimal.ZERO) < 0
                || request.getScore().compareTo(question.getScore()) > 0) {
            throw new BadRequest("人工评分必须在 0 到题目满分之间");
        }

        AssessmentJudgementVO judgement = AssessmentJudgementVO.builder()
                .answerId(answer.getId())
                .questionId(question.getId())
                .assessmentTimeId(question.getAssessmentTimeId())
                .userId(answer.getUserId())
                .score(request.getScore())
                .maxScore(question.getScore())
                .status(JudgementStatus.JUDGED)
                .source(JudgementSource.MANUAL)
                .reviewerId(currentUser.getId())
                .reviewerType(resolveReviewerType(roleType))
                .comment(request.getComment())
                .judgedAt(LocalDateTime.now())
                .build();
        return convertToDTO(assessmentJudgementDomainService.createJudgement(judgement));
    }

    @Override
    @Transactional
    public AssessmentDecisionDTO decideAssessment(AssessmentDecisionRequestDTO request) {
        UserVO currentUser = requireCurrentUser();
        RoleType roleType = requireRole(currentUser);
        if (!RoleHierarchy.isDirectionAdminOrAbove(roleType)) {
            throw new Forbidden("只有方向管理员及以上权限可以设置最终通过决策");
        }

        AssessmentDecisionVO decision = AssessmentDecisionVO.builder()
                .userId(request.getUserId())
                .assessmentTimeId(request.getAssessmentTimeId())
                .passed(request.getPassed())
                .decidedBy(currentUser.getId())
                .decisionComment(request.getDecisionComment())
                .build();
        return convertToDTO(assessmentDecisionDomainService.saveDecision(decision));
    }

    private UserVO requireCurrentUser() {
        UserVO currentUser = UserCTX.getCurrentUser();
        if (currentUser == null) {
            throw new SecurityException("未登录");
        }
        return currentUser;
    }

    private RoleType requireRole(UserVO user) {
        RoleType roleType = RoleType.fromName(user.getRoleName());
        if (roleType == null) {
            throw new Forbidden("当前用户角色无效");
        }
        return roleType;
    }

    private ReviewerType resolveReviewerType(RoleType roleType) {
        return switch (roleType) {
            case SUPER_ADMIN -> ReviewerType.SUPER_ADMIN;
            case DIRECTION_ADMIN -> ReviewerType.DIRECTION_ADMIN;
            default -> ReviewerType.MEMBER;
        };
    }

    private AssessmentJudgementDTO convertToDTO(AssessmentJudgementVO judgement) {
        return AssessmentJudgementDTO.builder()
                .id(judgement.getId())
                .answerId(judgement.getAnswerId())
                .questionId(judgement.getQuestionId())
                .assessmentTimeId(judgement.getAssessmentTimeId())
                .userId(judgement.getUserId())
                .score(judgement.getScore())
                .maxScore(judgement.getMaxScore())
                .status(judgement.getStatus())
                .resultCode(judgement.getResultCode())
                .source(judgement.getSource())
                .reviewerId(judgement.getReviewerId())
                .reviewerType(judgement.getReviewerType())
                .comment(judgement.getComment())
                .judgedAt(judgement.getJudgedAt())
                .build();
    }

    private AssessmentDecisionDTO convertToDTO(AssessmentDecisionVO decision) {
        return AssessmentDecisionDTO.builder()
                .id(decision.getId())
                .userId(decision.getUserId())
                .assessmentTimeId(decision.getAssessmentTimeId())
                .passed(decision.getPassed())
                .decidedBy(decision.getDecidedBy())
                .decisionComment(decision.getDecisionComment())
                .decidedAt(decision.getDecidedAt())
                .build();
    }
}
