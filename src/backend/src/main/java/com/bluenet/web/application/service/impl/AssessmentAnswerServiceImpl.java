package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.assessment_answer.AssessmentAnswerDTO;
import com.bluenet.web.api.dto.assessment_answer.CreateAnswerRequestDTO;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentJudgementDTO;
import com.bluenet.web.application.service.AssessmentAnswerService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.exception.GlobalException;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.enumerate.JudgementSource;
import com.bluenet.web.domain.model.enumerate.JudgementStatus;
import com.bluenet.web.domain.model.enumerate.ObjectiveResultCode;
import com.bluenet.web.domain.model.enumerate.QuestionType;
import com.bluenet.web.domain.model.vo.AssessmentAnswerVO;
import com.bluenet.web.domain.model.vo.AssessmentJudgementVO;
import com.bluenet.web.domain.model.vo.AssessmentQuestionVO;
import com.bluenet.web.domain.model.vo.AssessmentTimeVO;
import com.bluenet.web.domain.model.vo.FileVO;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.model.vo.evaluation.MultipleChoiceContent;
import com.bluenet.web.domain.model.vo.evaluation.SingleChoiceContent;
import com.bluenet.web.domain.repository.AssessmentAnswerRepository;
import com.bluenet.web.domain.repository.AssessmentSessionRepository;
import com.bluenet.web.domain.service.AssessmentAnswerDomainService;
import com.bluenet.web.domain.service.AssessmentJudgementDomainService;
import com.bluenet.web.domain.service.AssessmentQuestionDomainService;
import com.bluenet.web.domain.service.AssessmentTimeDomainService;
import com.bluenet.web.domain.service.FileDomainService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 答案应用服务实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AssessmentAnswerServiceImpl implements AssessmentAnswerService {

    private final AssessmentAnswerDomainService assessmentAnswerDomainService;
    private final AssessmentQuestionDomainService assessmentQuestionDomainService;
    private final AssessmentTimeDomainService assessmentTimeDomainService;
    private final FileDomainService fileDomainService;
    private final AssessmentJudgementDomainService assessmentJudgementDomainService;
    private final AssessmentAnswerRepository assessmentAnswerRepository;
    private final AssessmentSessionRepository assessmentSessionRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public AssessmentAnswerDTO createAnswer(CreateAnswerRequestDTO request) {
        UserVO currentUser = UserCTX.getCurrentUser();
        if (currentUser == null) {
            throw new SecurityException("未登录");
        }

        // 校验题目存在性
        AssessmentQuestionVO question = assessmentQuestionDomainService.getQuestionById(request.getQuestionId());

        // 校验方向匹配
        AssessmentTimeVO timeVO = validateDirectionMatch(currentUser, question);

        // 校验 fileId 有效性和类型
        validateFileId(request.getFileId());

        // 校验限时考核是否已过期
        if (Boolean.TRUE.equals(timeVO.getTimeLimit())) {
            assessmentSessionRepository
                    .findByUserIdAndAssessmentTimeId(currentUser.getId(), question.getAssessmentTimeId())
                    .ifPresent(session -> {
                        if (session.getDeadline() != null
                                && LocalDateTime.now().isAfter(session.getDeadline())) {
                            throw new BadRequest("考核时间已到，无法提交答案");
                        }
                    });
        }

        // 构建答案VO
        AssessmentAnswerVO answerVO = AssessmentAnswerVO.builder()
                .userId(currentUser.getId())
                .questionId(request.getQuestionId())
                .content(request.getContent())
                .language(request.getLanguage())
                .fileId(request.getFileId())
                .build();

        // 调用领域服务创建答案（含重复提交检查）
        AssessmentAnswerVO created = assessmentAnswerDomainService.createAnswer(answerVO);
        AssessmentJudgementVO judgement = judgeObjectiveAnswerIfNeeded(created, question);

        return convertToDTO(created, judgement);
    }

    @Override
    @Transactional
    public AssessmentAnswerDTO updateAnswer(CreateAnswerRequestDTO request) {
        UserVO currentUser = UserCTX.getCurrentUser();
        if (currentUser == null) {
            throw new SecurityException("未登录");
        }

        AssessmentQuestionVO question = assessmentQuestionDomainService.getQuestionById(request.getQuestionId());

        // 校验方向匹配
        AssessmentTimeVO timeVO = validateDirectionMatch(currentUser, question);

        // 校验 fileId 有效性和类型
        validateFileId(request.getFileId());

        if (Boolean.TRUE.equals(timeVO.getTimeLimit())) {
            assessmentSessionRepository
                    .findByUserIdAndAssessmentTimeId(currentUser.getId(), question.getAssessmentTimeId())
                    .ifPresent(session -> {
                        if (session.getDeadline() != null
                                && LocalDateTime.now().isAfter(session.getDeadline())) {
                            throw new BadRequest("考核时间已到，无法修改答案");
                        }
                    });
        }

        Optional<AssessmentAnswerVO> existingOpt = assessmentAnswerRepository
                .findByUserIdAndQuestionId(currentUser.getId(), request.getQuestionId());
        if (existingOpt.isEmpty()) {
            throw new BadRequest("尚未提交过该题目的答案，无法修改");
        }

        AssessmentAnswerVO existing = existingOpt.get();
        assessmentAnswerDomainService.updateAnswer(existing, request.getFileId(), request.getContent());

        AssessmentAnswerVO updated = assessmentAnswerRepository
                .findByUserIdAndQuestionId(currentUser.getId(), request.getQuestionId())
                .orElseThrow(() -> new GlobalException("更新答案后查询失败"));
        AssessmentJudgementVO judgement = judgeObjectiveAnswerIfNeeded(updated, question);

        return convertToDTO(updated, judgement);
    }

    @Override
    public AssessmentAnswerDTO getMyAnswer(Long questionId) {
        UserVO currentUser = UserCTX.getCurrentUser();
        if (currentUser == null) {
            throw new SecurityException("未登录");
        }

        Optional<AssessmentAnswerVO> answerOpt = assessmentAnswerRepository
                .findByUserIdAndQuestionId(currentUser.getId(), questionId);

        return answerOpt.map(answer -> convertToDTO(answer, findLatestJudgement(answer))).orElse(null);
    }

    private AssessmentTimeVO validateDirectionMatch(UserVO user, AssessmentQuestionVO question) {
        AssessmentTimeVO timeVO = assessmentTimeDomainService.getById(question.getAssessmentTimeId())
                .orElseThrow(() -> new BadRequest("考核时间不存在"));
        if (user.getDirection() != null && !user.getDirection().equals(timeVO.getDirection())) {
            throw new Forbidden("方向不匹配");
        }
        return timeVO;
    }

    private void validateFileId(Long fileId) {
        if (fileId == null) {
            return;
        }
        FileVO fileVO = fileDomainService.getFileById(fileId);
        if (fileVO == null) {
            throw new BadRequest("文件不存在");
        }
        if (fileVO.getType() != FileType.WORK) {
            throw new BadRequest("文件类型不匹配，期望 WORK");
        }
    }

    private AssessmentJudgementVO judgeObjectiveAnswerIfNeeded(AssessmentAnswerVO answer,
            AssessmentQuestionVO question) {
        if (question.getQuestionType() == QuestionType.SINGLE_CHOICE) {
            return judgeSingleChoice(answer, question);
        }
        if (question.getQuestionType() == QuestionType.MULTIPLE_CHOICE) {
            return judgeMultipleChoice(answer, question);
        }
        return findLatestJudgement(answer);
    }

    private AssessmentJudgementVO judgeSingleChoice(AssessmentAnswerVO answer, AssessmentQuestionVO question) {
        if (!(question.getContent()instanceof SingleChoiceContent content)) {
            throw new GlobalException("单选题内容配置错误");
        }
        boolean accepted = normalizeAnswer(answer.getContent()).equals(normalizeAnswer(content.getCorrectAnswer()));
        return createAutomaticJudgement(answer, question, accepted ? ObjectiveResultCode.AC : ObjectiveResultCode.WA);
    }

    private AssessmentJudgementVO judgeMultipleChoice(AssessmentAnswerVO answer, AssessmentQuestionVO question) {
        if (!(question.getContent()instanceof MultipleChoiceContent content)) {
            throw new GlobalException("多选题内容配置错误");
        }
        Set<String> submittedAnswers = parseSubmittedMultipleChoiceAnswers(answer.getContent());
        Set<String> correctAnswers = normalizeAnswers(content.getCorrectAnswers());
        boolean accepted = submittedAnswers.equals(correctAnswers);
        return createAutomaticJudgement(answer, question, accepted ? ObjectiveResultCode.AC : ObjectiveResultCode.WA);
    }

    private AssessmentJudgementVO createAutomaticJudgement(
            AssessmentAnswerVO answer,
            AssessmentQuestionVO question,
            ObjectiveResultCode resultCode) {
        BigDecimal maxScore = question.getScore() == null ? BigDecimal.ZERO : question.getScore();
        BigDecimal score = resultCode == ObjectiveResultCode.AC ? maxScore : BigDecimal.ZERO;
        // 客观题同步评判只产生 AC/WA，单选题和多选题不进入队列也没有 pending 状态。
        AssessmentJudgementVO judgement = AssessmentJudgementVO.builder()
                .answerId(answer.getId())
                .questionId(question.getId())
                .assessmentTimeId(question.getAssessmentTimeId())
                .userId(answer.getUserId())
                .score(score)
                .maxScore(maxScore)
                .status(JudgementStatus.JUDGED)
                .resultCode(resultCode)
                .source(JudgementSource.AUTO)
                .build();
        return assessmentJudgementDomainService.createJudgement(judgement);
    }

    private AssessmentJudgementVO findLatestJudgement(AssessmentAnswerVO answer) {
        if (answer.getId() == null) {
            return null;
        }
        try {
            return assessmentJudgementDomainService.getLatestByAnswerId(answer.getId());
        } catch (DataNotFound ignored) {
            return null;
        }
    }

    private Set<String> parseSubmittedMultipleChoiceAnswers(String answerContent) {
        if (answerContent == null || answerContent.isBlank()) {
            return Set.of();
        }
        try {
            List<String> answers = objectMapper.readValue(answerContent, new TypeReference<>() {
            });
            return normalizeAnswers(answers);
        } catch (JsonProcessingException e) {
            throw new BadRequest("多选题答案格式错误");
        }
    }

    private Set<String> normalizeAnswers(List<String> answers) {
        if (answers == null) {
            return Set.of();
        }
        Set<String> normalized = new HashSet<>();
        for (String answer : answers) {
            String normalizedAnswer = normalizeAnswer(answer);
            if (!normalizedAnswer.isEmpty()) {
                normalized.add(normalizedAnswer);
            }
        }
        return normalized;
    }

    private String normalizeAnswer(String answer) {
        return answer == null ? "" : answer.trim();
    }

    private AssessmentAnswerDTO convertToDTO(AssessmentAnswerVO vo, AssessmentJudgementVO judgement) {
        return AssessmentAnswerDTO.builder()
                .id(vo.getId())
                .questionId(vo.getQuestionId())
                .fileId(vo.getFileId())
                .content(vo.getContent())
                .language(vo.getLanguage())
                .submitTime(vo.getSubmitTime())
                .judgement(convertToDTO(judgement))
                .build();
    }

    private AssessmentJudgementDTO convertToDTO(AssessmentJudgementVO judgement) {
        if (judgement == null) {
            return null;
        }
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
}
