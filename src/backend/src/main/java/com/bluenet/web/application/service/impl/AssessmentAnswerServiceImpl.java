package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.assessment_answer.AssessmentAnswerDTO;
import com.bluenet.web.api.dto.assessment_answer.CreateAnswerRequestDTO;
import com.bluenet.web.application.service.AssessmentAnswerService;
import com.bluenet.web.domain.model.vo.AssessmentAnswerVO;
import com.bluenet.web.domain.model.vo.AssessmentQuestionVO;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.repository.AssessmentAnswerRepository;
import com.bluenet.web.domain.repository.AssessmentSessionRepository;
import com.bluenet.web.domain.service.AssessmentAnswerDomainService;
import com.bluenet.web.domain.service.AssessmentQuestionDomainService;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 答案应用服务实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AssessmentAnswerServiceImpl implements AssessmentAnswerService {

    private final AssessmentAnswerDomainService assessmentAnswerDomainService;
    private final AssessmentQuestionDomainService assessmentQuestionDomainService;
    private final AssessmentAnswerRepository assessmentAnswerRepository;
    private final AssessmentSessionRepository assessmentSessionRepository;

    @Override
    @Transactional
    public AssessmentAnswerDTO createAnswer(CreateAnswerRequestDTO request) {
        UserVO currentUser = UserCTX.getCurrentUser();
        if (currentUser == null) {
            throw new SecurityException("未登录");
        }

        // 校验题目存在性
        AssessmentQuestionVO question = assessmentQuestionDomainService.getQuestionById(request.getQuestionId());

        // 校验限时考核是否已过期
        assessmentSessionRepository
                .findByUserIdAndAssessmentTimeId(currentUser.getId(), question.getAssessmentTimeId())
                .ifPresent(session -> {
                    if (session.getDeadline() != null
                            && LocalDateTime.now().isAfter(session.getDeadline())) {
                        throw new IllegalStateException("考核时间已到，无法提交答案");
                    }
                });

        // 构建答案VO
        AssessmentAnswerVO answerVO = AssessmentAnswerVO.builder()
                .userId(currentUser.getId())
                .questionId(request.getQuestionId())
                .content(request.getContent())
                .fileId(request.getFileId())
                .build();

        // 调用领域服务创建答案（含重复提交检查）
        AssessmentAnswerVO created = assessmentAnswerDomainService.createAnswer(answerVO);

        return convertToDTO(created);
    }

    @Override
    @Transactional
    public AssessmentAnswerDTO updateAnswer(CreateAnswerRequestDTO request) {
        UserVO currentUser = UserCTX.getCurrentUser();
        if (currentUser == null) {
            throw new SecurityException("未登录");
        }

        AssessmentQuestionVO question = assessmentQuestionDomainService.getQuestionById(request.getQuestionId());

        assessmentSessionRepository
                .findByUserIdAndAssessmentTimeId(currentUser.getId(), question.getAssessmentTimeId())
                .ifPresent(session -> {
                    if (session.getDeadline() != null
                            && LocalDateTime.now().isAfter(session.getDeadline())) {
                        throw new IllegalStateException("考核时间已到，无法修改答案");
                    }
                });

        Optional<AssessmentAnswerVO> existingOpt = assessmentAnswerRepository
                .findByUserIdAndQuestionId(currentUser.getId(), request.getQuestionId());
        if (existingOpt.isEmpty()) {
            throw new IllegalStateException("尚未提交过该题目的答案，无法修改");
        }

        AssessmentAnswerVO existing = existingOpt.get();
        assessmentAnswerDomainService.updateAnswer(existing, request.getFileId(), request.getContent());

        AssessmentAnswerVO updated = assessmentAnswerRepository
                .findByUserIdAndQuestionId(currentUser.getId(), request.getQuestionId())
                .orElseThrow(() -> new IllegalStateException("更新答案后查询失败"));

        return convertToDTO(updated);
    }

    @Override
    public AssessmentAnswerDTO getMyAnswer(Long questionId) {
        UserVO currentUser = UserCTX.getCurrentUser();
        if (currentUser == null) {
            throw new SecurityException("未登录");
        }

        Optional<AssessmentAnswerVO> answerOpt = assessmentAnswerRepository
                .findByUserIdAndQuestionId(currentUser.getId(), questionId);

        return answerOpt.map(this::convertToDTO).orElse(null);
    }

    private AssessmentAnswerDTO convertToDTO(AssessmentAnswerVO vo) {
        return AssessmentAnswerDTO.builder()
                .id(vo.getId())
                .questionId(vo.getQuestionId())
                .fileId(vo.getFileId())
                .content(vo.getContent())
                .submitTime(vo.getSubmitTime())
                .build();
    }
}
