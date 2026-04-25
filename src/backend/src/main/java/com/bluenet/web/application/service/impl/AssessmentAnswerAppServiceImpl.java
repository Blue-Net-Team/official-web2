package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.assessment_judgement.AssessmentJudgementDTO;
import com.bluenet.web.application.AssessmentAnswerResult;
import com.bluenet.web.application.command.assessment_answer.AssessmentAnswerCommands;
import com.bluenet.web.application.converter.AssessmentJudgementAppConverter;
import com.bluenet.web.application.service.AssessmentAnswerAppService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataConflict;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.exception.GlobalException;
import com.bluenet.web.domain.model.entity.AssessmentAnswer;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.enumerate.JudgementSource;
import com.bluenet.web.domain.model.enumerate.JudgementStatus;
import com.bluenet.web.domain.model.enumerate.ObjectiveResultCode;
import com.bluenet.web.domain.model.enumerate.QuestionType;
import com.bluenet.web.domain.model.vo.AssessmentJudgementVO;
import com.bluenet.web.domain.model.entity.AssessmentQuestion;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.vo.FileVO;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.model.vo.evaluation.MultipleChoiceContent;
import com.bluenet.web.domain.model.vo.evaluation.SingleChoiceContent;
import com.bluenet.web.domain.repository.AssessmentAnswerRepository;
import com.bluenet.web.domain.repository.AssessmentSessionRepository;
import com.bluenet.web.domain.service.AssessmentJudgementDomainService;
import com.bluenet.web.domain.repository.AssessmentQuestionRepository;
import com.bluenet.web.domain.repository.AssessmentTimeRepository;
import com.bluenet.web.domain.service.FileDomainService;
import com.bluenet.web.domain.service.UserDomainService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * 考核答案应用服务实现。
 * <p>实现考核答案聚合在应用层的业务逻辑编排。</p>
 */
/**
 * 评测答案应用服务实现。
 * <p>
 * 实现评测答案聚合在应用层的业务逻辑编排。
 * </p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AssessmentAnswerAppServiceImpl implements AssessmentAnswerAppService {

    private final AssessmentQuestionRepository assessmentQuestionRepository;
    private final AssessmentTimeRepository assessmentTimeRepository;
    private final FileDomainService fileDomainService;
    private final AssessmentJudgementDomainService assessmentJudgementDomainService;
    private final AssessmentAnswerRepository assessmentAnswerRepository;
    private final AssessmentSessionRepository assessmentSessionRepository;
    private final ObjectMapper objectMapper;
    private final AssessmentJudgementAppConverter assessmentJudgementAppConverter;
    private final UserDomainService userDomainService;

    /**
     * 创建答案。
     *
     * @param command
     *            创建答案命令
     * @return 创建后的答案结果
     */
    /**
     * 创建评测答案。
     *
     * @param command
     *            创建评测答案命令
     * @return 创建的评测答案结果
     */
    @Override
    @Transactional
    public AssessmentAnswerResult createAnswer(AssessmentAnswerCommands.CreateAssessmentAnswerCommand command) {
        UserVO currentUser = userDomainService.getUser(command.userId())
                .orElseThrow(() -> new SecurityException("未登录"));

        AssessmentQuestion question = assessmentQuestionRepository.findById(command.questionId())
                .orElseThrow(() -> new DataNotFound("题目不存在，ID: " + command.questionId()));
        AssessmentTime timeVO = validateDirectionMatch(currentUser, question);
        validateTimeNotEnded(timeVO);
        validateFileId(command.fileId());

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

        if (assessmentAnswerRepository.existsByUserIdAndQuestionId(command.userId(), command.questionId())) {
            throw new DataConflict("已经提交过该题目的答案");
        }

        AssessmentAnswer entity = AssessmentAnswer.create(
                command.userId(),
                command.questionId(),
                command.content(),
                command.language(),
                command.fileId());

        assessmentAnswerRepository.save(entity);

        log.info(
                "创建答案成功，userId: {}, questionId: {}, answerId: {}",
                command.userId(),
                command.questionId(),
                entity.getId());

        AssessmentJudgementVO judgement = judgeObjectiveAnswerIfNeeded(entity, question);
        return toResult(entity, judgement);
    }

    /**
     * 更新答案。
     *
     * @param command
     *            更新答案命令
     * @return 更新后的答案结果
     */
    /**
     * 更新评测答案。
     *
     * @param command
     *            更新评测答案命令
     * @return 更新后的评测答案结果
     */
    @Override
    @Transactional
    public AssessmentAnswerResult updateAnswer(AssessmentAnswerCommands.UpdateAssessmentAnswerCommand command) {
        UserVO currentUser = userDomainService.getUser(command.userId())
                .orElseThrow(() -> new SecurityException("未登录"));

        AssessmentQuestion question = assessmentQuestionRepository.findById(command.questionId())
                .orElseThrow(() -> new DataNotFound("题目不存在，ID: " + command.questionId()));
        AssessmentTime timeVO = validateDirectionMatch(currentUser, question);
        validateTimeNotEnded(timeVO);
        validateFileId(command.fileId());

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

        Optional<AssessmentAnswer> existingOpt = assessmentAnswerRepository
                .findByUserIdAndQuestionId(command.userId(), command.questionId());
        if (existingOpt.isEmpty()) {
            throw new BadRequest("尚未提交过该题目的答案，无法修改");
        }

        AssessmentAnswer existing = existingOpt.get();
        log.info(
                "update answer {}, fileId: {}, content length: {}",
                existing.getId(),
                command.fileId(),
                command.content() != null ? command.content().length() : 0);

        if (command.fileId() != null) {
            existing.setFileId(command.fileId());
        }
        if (command.content() != null) {
            existing.setContent(command.content());
        }
        if (command.language() != null) {
            existing.setLanguage(command.language());
        }
        existing.setSubmitTime(LocalDateTime.now());
        assessmentAnswerRepository.update(existing);

        AssessmentAnswer updated = assessmentAnswerRepository
                .findByUserIdAndQuestionId(command.userId(), command.questionId())
                .orElseThrow(() -> new GlobalException("更新答案后查询失败"));

        log.info("update answer success for answer {}", updated.getId());

        AssessmentJudgementVO judgement = judgeObjectiveAnswerIfNeeded(updated, question);
        return toResult(updated, judgement);
    }

    /**
     * 获取当前用户答案。
     *
     * @param userId
     *            用户ID
     * @param questionId
     *            题目ID
     * @return 当前用户的答案结果
     */
    /**
     * 查询当前用户的评测答案。
     *
     * @param userId
     *            用户ID
     * @param questionId
     *            题目ID
     * @return 评测答案结果
     */
    @Override
    public AssessmentAnswerResult getMyAnswer(Long userId, Long questionId) {
        Optional<AssessmentAnswer> answerOpt = assessmentAnswerRepository
                .findByUserIdAndQuestionId(userId, questionId);

        return answerOpt.map(answer -> toResult(answer, findLatestJudgement(answer))).orElse(null);
    }

    private AssessmentTime validateDirectionMatch(UserVO user, AssessmentQuestion question) {
        AssessmentTime time = assessmentTimeRepository.findById(question.getAssessmentTimeId())
                .orElseThrow(() -> new BadRequest("考核时间不存在"));
        if (user.getDirection() != null && !user.getDirection().equals(time.getDirection())) {
            throw new Forbidden("方向不匹配");
        }
        return time;
    }

    private void validateTimeNotEnded(AssessmentTime time) {
        if (time.getEndTime() != null && LocalDateTime.now().isAfter(time.getEndTime())) {
            throw new BadRequest("考核时间已结束，无法提交答案");
        }
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

    private AssessmentJudgementVO judgeObjectiveAnswerIfNeeded(AssessmentAnswer answer, AssessmentQuestion question) {
        if (question.getQuestionType() == QuestionType.SINGLE_CHOICE) {
            return judgeSingleChoice(answer, question);
        }
        if (question.getQuestionType() == QuestionType.MULTIPLE_CHOICE) {
            return judgeMultipleChoice(answer, question);
        }
        return findLatestJudgement(answer);
    }

    private AssessmentJudgementVO judgeSingleChoice(AssessmentAnswer answer, AssessmentQuestion question) {
        if (!(question.getContent()instanceof SingleChoiceContent content)) {
            throw new GlobalException("单选题内容配置错误");
        }
        boolean accepted = normalizeAnswer(answer.getContent()).equals(normalizeAnswer(content.getCorrectAnswer()));
        return createAutomaticJudgement(answer, question, accepted ? ObjectiveResultCode.AC : ObjectiveResultCode.WA);
    }

    private AssessmentJudgementVO judgeMultipleChoice(AssessmentAnswer answer, AssessmentQuestion question) {
        if (!(question.getContent()instanceof MultipleChoiceContent content)) {
            throw new GlobalException("多选题内容配置错误");
        }
        Set<String> submittedAnswers = parseSubmittedMultipleChoiceAnswers(answer.getContent());
        Set<String> correctAnswers = normalizeAnswers(content.getCorrectAnswers());
        boolean accepted = submittedAnswers.equals(correctAnswers);
        return createAutomaticJudgement(answer, question, accepted ? ObjectiveResultCode.AC : ObjectiveResultCode.WA);
    }

    private AssessmentJudgementVO createAutomaticJudgement(
            AssessmentAnswer answer, AssessmentQuestion question, ObjectiveResultCode resultCode) {
        BigDecimal maxScore = question.getScore() == null ? BigDecimal.ZERO : question.getScore();
        BigDecimal score = resultCode == ObjectiveResultCode.AC ? maxScore : BigDecimal.ZERO;
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

    private AssessmentJudgementVO findLatestJudgement(AssessmentAnswer answer) {
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

    private AssessmentAnswerResult toResult(AssessmentAnswer answer, AssessmentJudgementVO judgement) {
        AssessmentJudgementDTO judgementDTO = assessmentJudgementAppConverter.convertToDTO(judgement);
        return new AssessmentAnswerResult(
                answer.getId(),
                answer.getQuestionId(),
                answer.getFileId(),
                answer.getContent(),
                answer.getLanguage(),
                answer.getSubmitTime(),
                judgementDTO);
    }
}
