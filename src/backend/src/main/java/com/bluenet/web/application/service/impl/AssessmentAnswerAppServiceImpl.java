package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.AssessmentAnswerResult;
import com.bluenet.web.application.AssessmentJudgementResult;
import com.bluenet.web.application.command.assessment_answer.AssessmentAnswerCommands;
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
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.model.vo.AssessmentJudgementVO;
import com.bluenet.web.domain.model.entity.AssessmentQuestion;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.vo.FileVO;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.model.vo.evaluation.MultipleChoiceContent;
import com.bluenet.web.domain.model.vo.evaluation.SingleChoiceContent;
import com.bluenet.web.domain.model.entity.AssessmentTeam;
import com.bluenet.web.domain.repository.AssessmentAnswerRepository;
import com.bluenet.web.domain.repository.AssessmentSessionRepository;
import com.bluenet.web.domain.repository.AssessmentTeamRepository;
import com.bluenet.web.domain.service.AssessmentDecisionDomainService;
import com.bluenet.web.domain.service.AssessmentJudgementDomainService;
import com.bluenet.web.domain.repository.AssessmentQuestionRepository;
import com.bluenet.web.domain.repository.AssessmentTimeRepository;
import com.bluenet.web.domain.service.CommentDomainService;
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
import java.util.ArrayList;
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
    private final AssessmentTeamRepository assessmentTeamRepository;
    private final ObjectMapper objectMapper;
    private final UserDomainService userDomainService;
    private final CommentDomainService commentDomainService;
    private final AssessmentDecisionDomainService assessmentDecisionDomainService;

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
        validateNotEliminated(currentUser, timeVO);
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

        Long teamId = null;
        if (question.getQuestionType() == QuestionType.FILE_UPLOAD && Boolean.TRUE.equals(timeVO.getAllowTeam())) {
            teamId = validateTeamLeaderForAnswer(command.userId(), timeVO.getId());
        }

        AssessmentAnswer entity = AssessmentAnswer.create(
                command.userId(),
                command.questionId(),
                command.content(),
                command.language(),
                command.fileId(),
                teamId);

        assessmentAnswerRepository.save(entity);

        // Auto-create answer records for team members when leader submits FILE_UPLOAD
        // answer
        if (teamId != null) {
            createTeamMemberAnswers(teamId, question, command);
        }

        log.info(
                "创建答案成功，userId: {}, questionId: {}, answerId: {}",
                command.userId(),
                command.questionId(),
                entity.getId());

        AssessmentJudgementVO judgement = judgeObjectiveAnswerIfNeeded(entity, question);
        AssessmentAnswerResult result = toResult(entity, judgement, java.util.Collections.emptyList());
        if (question.getQuestionType().isChoiceQuestion()) {
            return result.withJudgementErased();
        }
        return result;
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
        validateNotEliminated(currentUser, timeVO);
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

        if (question.getQuestionType() == QuestionType.FILE_UPLOAD && Boolean.TRUE.equals(timeVO.getAllowTeam())) {
            validateTeamLeaderForAnswer(command.userId(), timeVO.getId());
        }

        // 组队场景下答案统一：队长代表整支队伍提交。
        // 队长更新答案时，批量同步该题目下全队所有答案（含队长自己）。
        if (existing.getTeamId() != null) {
            syncTeamMemberAnswers(existing.getTeamId(), question, command);
        } else {
            // 非组队场景：仅更新当前用户自己的答案
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
        }

        AssessmentAnswer updated = assessmentAnswerRepository
                .findByUserIdAndQuestionId(command.userId(), command.questionId())
                .orElseThrow(() -> new GlobalException("更新答案后查询失败"));

        log.info("update answer success for answer {}", updated.getId());

        AssessmentJudgementVO judgement = judgeObjectiveAnswerIfNeeded(updated, question);
        AssessmentAnswerResult result = toResult(updated, judgement, java.util.Collections.emptyList());
        if (question.getQuestionType().isChoiceQuestion()) {
            return result.withJudgementErased();
        }
        return result;
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
        AssessmentQuestion question = assessmentQuestionRepository.findById(questionId)
                .orElse(null);

        Optional<AssessmentAnswer> answerOpt = assessmentAnswerRepository
                .findByUserIdAndQuestionId(userId, questionId);

        if (answerOpt.isEmpty()) {
            // For FILE_UPLOAD questions in team-enabled assessments, if user is team member
            // (not leader), return leader's answer
            if (question != null && question.getQuestionType() == QuestionType.FILE_UPLOAD) {
                AssessmentTime time = assessmentTimeRepository.findById(question.getAssessmentTimeId())
                        .orElse(null);
                if (time != null && Boolean.TRUE.equals(time.getAllowTeam())) {
                    Optional<AssessmentTeam> teamOpt = assessmentTeamRepository
                            .findByAssessmentTimeIdAndUserId(time.getId(), userId);
                    if (teamOpt.isPresent()) {
                        AssessmentTeam team = teamOpt.get();
                        if (!team.isLeader(userId)) {
                            Optional<AssessmentAnswer> leaderAnswerOpt = assessmentAnswerRepository
                                    .findByUserIdAndQuestionId(team.getLeaderId(), questionId);
                            if (leaderAnswerOpt.isPresent()) {
                                return toAnswerResult(leaderAnswerOpt.get(), question);
                            }
                        }
                    }
                }
            }
            return null;
        }

        AssessmentAnswer answer = answerOpt.get();
        return toAnswerResult(answer, question);
    }

    private AssessmentAnswerResult toAnswerResult(AssessmentAnswer answer, AssessmentQuestion question) {
        AssessmentJudgementVO judgement = findLatestJudgement(answer);
        List<com.bluenet.web.domain.model.vo.CommentVO> comments = commentDomainService
                .listCommentsByAnswerId(answer.getId());
        AssessmentAnswerResult result = toResult(answer, judgement, comments);

        if (question != null && question.getQuestionType().isChoiceQuestion()) {
            return result.withJudgementErased();
        }
        return result;
    }

    private Long validateTeamLeaderForAnswer(Long userId, Long assessmentTimeId) {
        Optional<AssessmentTeam> teamOpt = assessmentTeamRepository
                .findByAssessmentTimeIdAndUserId(assessmentTimeId, userId);
        if (teamOpt.isEmpty()) {
            throw new BadRequest("该题目需要加入队伍后才能提交答案");
        }
        AssessmentTeam team = teamOpt.get();
        if (!team.isActive()) {
            throw new BadRequest("队伍已解散，无法提交答案");
        }
        if (!team.isLeader(userId)) {
            throw new Forbidden("只有队长可以提交文件上传题的答案");
        }
        return team.getId();
    }

    private AssessmentTime validateDirectionMatch(UserVO user, AssessmentQuestion question) {
        AssessmentTime time = assessmentTimeRepository.findById(question.getAssessmentTimeId())
                .orElseThrow(() -> new BadRequest("考核时间不存在"));
        if (time.getDirection() != null && user.getDirection() != null
                && !user.getDirection().equals(time.getDirection())) {
            throw new Forbidden("方向不匹配");
        }
        return time;
    }

    private void validateTimeNotEnded(AssessmentTime time) {
        if (time.getEndTime() != null && LocalDateTime.now().isAfter(time.getEndTime())) {
            throw new BadRequest("考核时间已结束，无法提交答案");
        }
    }

    private void validateNotEliminated(UserVO user, AssessmentTime time) {
        RoleType roleType = RoleType.fromName(user.getRoleName());
        if (roleType == RoleType.CANDIDATE
                && assessmentDecisionDomainService.isEliminatedFromPriorEpoch(user.getId(), time)) {
            throw new Forbidden("已在该方向考核中被淘汰，无法提交答案");
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

    private AssessmentAnswerResult toResult(AssessmentAnswer answer, AssessmentJudgementVO judgement,
            List<com.bluenet.web.domain.model.vo.CommentVO> comments) {
        return new AssessmentAnswerResult(
                answer.getId(),
                answer.getQuestionId(),
                answer.getFileId(),
                answer.getContent(),
                answer.getLanguage(),
                answer.getSubmitTime(),
                toJudgementResult(judgement),
                comments);
    }

    private AssessmentJudgementResult toJudgementResult(AssessmentJudgementVO judgement) {
        if (judgement == null) {
            return null;
        }
        return new AssessmentJudgementResult(
                judgement.getId(),
                judgement.getAnswerId(),
                judgement.getQuestionId(),
                judgement.getAssessmentTimeId(),
                judgement.getUserId(),
                judgement.getScore(),
                judgement.getMaxScore(),
                judgement.getStatus(),
                judgement.getResultCode(),
                judgement.getSource(),
                judgement.getReviewerId(),
                judgement.getReviewerType(),
                judgement.getJudgedAt());
    }

    /**
     * 为队员批量创建答案记录。
     * <p>
     * 组队场景下，队长首次提交答案时，为所有尚未有答案记录的队员创建同名答案。 已存在答案的队员会被跳过，避免覆盖。
     * </p>
     */
    private void createTeamMemberAnswers(Long teamId, AssessmentQuestion question,
            AssessmentAnswerCommands.CreateAssessmentAnswerCommand command) {
        List<com.bluenet.web.domain.model.entity.AssessmentTeamMember> members = assessmentTeamRepository
                .findMembersByTeamId(teamId);
        List<Long> memberUserIds = members.stream()
                .map(com.bluenet.web.domain.model.entity.AssessmentTeamMember::getUserId)
                .filter(id -> !id.equals(command.userId()))
                .toList();
        if (memberUserIds.isEmpty()) {
            log.warn("队伍 id {} 无成员", teamId);
            return;
        }

        // 批量查询：哪些队员已经提交过该题目的答案（避免重复创建）
        List<Long> existingUserIds = assessmentAnswerRepository
                .findExistingAnswerUserIds(memberUserIds, command.questionId());

        LocalDateTime now = LocalDateTime.now();
        List<AssessmentAnswer> answersToInsert = new ArrayList<>();
        for (Long memberUserId : memberUserIds) {
            if (existingUserIds.contains(memberUserId)) {
                continue;
            }
            answersToInsert.add(
                    AssessmentAnswer.create(
                            memberUserId,
                            command.questionId(),
                            command.content(),
                            command.language(),
                            command.fileId(),
                            teamId));
        }

        if (!answersToInsert.isEmpty()) {
            assessmentAnswerRepository.batchInsert(answersToInsert);
            log.info(
                    "批量创建组员答案，teamId: {}, questionId: {}, count: {}",
                    teamId,
                    command.questionId(),
                    answersToInsert.size());
        }
    }

    /**
     * 批量同步队伍答案。
     * <p>
     * 组队场景下，队长更新答案时，通过 team_id + question_id 批量更新该队伍在该题目下的
     * 所有答案记录（含队长自己），确保全队答案保持一致。
     * </p>
     */
    private void syncTeamMemberAnswers(Long teamId, AssessmentQuestion question,
            AssessmentAnswerCommands.UpdateAssessmentAnswerCommand command) {
        int updated = assessmentAnswerRepository.updateTeamMemberAnswers(
                teamId,
                command.questionId(),
                command.fileId(),
                command.content(),
                command.language(),
                LocalDateTime.now());
        if (updated > 0) {
            log.info(
                    "批量同步队伍答案，teamId: {}, questionId: {}, count: {}",
                    teamId,
                    command.questionId(),
                    updated);
        }
    }
}
