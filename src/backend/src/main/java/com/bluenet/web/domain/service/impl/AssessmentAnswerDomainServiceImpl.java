package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataConflict;
import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.exception.GlobalException;
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
import com.bluenet.web.domain.model.entity.AssessmentTeam;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.vo.question_content.MultipleChoiceContent;
import com.bluenet.web.domain.model.vo.question_content.SingleChoiceContent;
import com.bluenet.web.domain.repository.AssessmentAnswerRepository;
import com.bluenet.web.domain.repository.AssessmentSessionRepository;
import com.bluenet.web.domain.repository.AssessmentTeamRepository;
import com.bluenet.web.domain.repository.AssessmentTimeRepository;
import com.bluenet.web.domain.service.AssessmentAnswerDomainService;
import com.bluenet.web.domain.service.AssessmentDecisionDomainService;
import com.bluenet.web.domain.service.FileDomainService;
import com.bluenet.web.infrastructure.security.principal.RoleTypeResolver;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 考核答案领域服务实现。
 * <p>
 * 只负责校验、生成/修改领域实体；所有写操作由应用服务在事务中统一执行。
 * </p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AssessmentAnswerDomainServiceImpl implements AssessmentAnswerDomainService {

    private final AssessmentTimeRepository assessmentTimeRepository;
    private final AssessmentSessionRepository assessmentSessionRepository;
    private final AssessmentAnswerRepository assessmentAnswerRepository;
    private final AssessmentTeamRepository assessmentTeamRepository;
    private final FileDomainService fileDomainService;
    private final AssessmentDecisionDomainService assessmentDecisionDomainService;
    private final RoleTypeResolver roleTypeResolver;
    private final ObjectMapper objectMapper;

    @Override
    public AssessmentAnswer prepareAnswer(User user, AssessmentQuestion question,
            String content, ProgrammingLanguage language, Long fileId) {
        AssessmentTime time = validateDirectionMatch(user, question);
        validateTimeNotEnded(time);
        validateNotEliminated(user, time);
        validateFileId(fileId);
        validateSessionDeadline(user, time);

        if (assessmentAnswerRepository.existsByUserIdAndQuestionId(user.getId(), question.getId())) {
            throw new DataConflict("已经提交过该题目的答案");
        }

        Long teamId = resolveTeamIdForSubmission(user.getId(), question, time);

        AssessmentAnswer answer = AssessmentAnswer.create(
                user.getId(),
                question.getId(),
                content,
                language,
                fileId,
                teamId);

        log.info(
                "准备创建答案，userId: {}, questionId: {}, teamId: {}",
                user.getId(),
                question.getId(),
                teamId);
        return answer;
    }

    @Override
    public List<AssessmentAnswer> prepareTeamMemberAnswers(AssessmentAnswer leaderAnswer, AssessmentQuestion question,
            String content, ProgrammingLanguage language, Long fileId) {
        Long teamId = leaderAnswer.getTeamId();
        if (teamId == null) {
            return Collections.emptyList();
        }

        List<com.bluenet.web.domain.model.entity.AssessmentTeamMember> members = assessmentTeamRepository
                .findMembersByTeamId(teamId);
        List<Long> memberUserIds = members.stream()
                .map(com.bluenet.web.domain.model.entity.AssessmentTeamMember::getUserId)
                .filter(id -> !id.equals(leaderAnswer.getUserId()))
                .toList();
        if (memberUserIds.isEmpty()) {
            log.warn("队伍 id {} 无成员", teamId);
            return Collections.emptyList();
        }

        List<Long> existingUserIds = assessmentAnswerRepository
                .findExistingAnswerUserIds(memberUserIds, question.getId());

        List<AssessmentAnswer> answersToInsert = new ArrayList<>();
        for (Long memberUserId : memberUserIds) {
            if (existingUserIds.contains(memberUserId)) {
                continue;
            }
            answersToInsert.add(
                    AssessmentAnswer.create(
                            memberUserId,
                            question.getId(),
                            content,
                            language,
                            fileId,
                            teamId));
        }

        if (!answersToInsert.isEmpty()) {
            log.info(
                    "准备批量创建组员答案，teamId: {}, questionId: {}, count: {}",
                    teamId,
                    question.getId(),
                    answersToInsert.size());
        }
        return answersToInsert;
    }

    @Override
    public AssessmentJudgement prepareObjectiveJudgement(AssessmentAnswer answer, AssessmentQuestion question) {
        if (question.getQuestionType() == QuestionType.SINGLE_CHOICE) {
            return judgeSingleChoice(answer, question);
        }
        if (question.getQuestionType() == QuestionType.MULTIPLE_CHOICE) {
            return judgeMultipleChoice(answer, question);
        }
        return null;
    }

    @Override
    public List<AssessmentAnswer> prepareUpdatedAnswers(User user, AssessmentQuestion question,
            AssessmentAnswer existingAnswer,
            String content, ProgrammingLanguage language, Long fileId) {
        AssessmentTime time = validateDirectionMatch(user, question);
        validateTimeNotEnded(time);
        validateNotEliminated(user, time);
        validateFileId(fileId);
        validateSessionDeadline(user, time);

        if (question.getQuestionType() == QuestionType.FILE_UPLOAD && Boolean.TRUE.equals(time.getAllowTeam())) {
            validateTeamLeaderForAnswer(user.getId(), time.getId());
        }

        log.info(
                "准备更新答案，answerId: {}, fileId: {}, content length: {}",
                existingAnswer.getId(),
                fileId,
                content != null ? content.length() : 0);

        if (existingAnswer.getTeamId() != null) {
            return prepareTeamUpdatedAnswers(existingAnswer.getTeamId(), question, content, language, fileId);
        }

        existingAnswer.update(content, language, fileId);
        return List.of(existingAnswer);
    }

    private List<AssessmentAnswer> prepareTeamUpdatedAnswers(Long teamId, AssessmentQuestion question,
            String content, ProgrammingLanguage language, Long fileId) {
        List<AssessmentAnswer> teamAnswers = assessmentAnswerRepository
                .findByTeamIdAndQuestionId(teamId, question.getId());
        for (AssessmentAnswer answer : teamAnswers) {
            answer.update(content, language, fileId);
        }
        log.info(
                "准备批量同步队伍答案，teamId: {}, questionId: {}, count: {}",
                teamId,
                question.getId(),
                teamAnswers.size());
        return teamAnswers;
    }

    private Long resolveTeamIdForSubmission(Long userId, AssessmentQuestion question, AssessmentTime time) {
        if (question.getQuestionType() != QuestionType.FILE_UPLOAD || !Boolean.TRUE.equals(time.getAllowTeam())) {
            return null;
        }
        return validateTeamLeaderForAnswer(userId, time.getId());
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

    private AssessmentTime validateDirectionMatch(User user, AssessmentQuestion question) {
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

    private void validateNotEliminated(User user, AssessmentTime time) {
        RoleType roleType = roleTypeResolver.resolve(user.getRoleId());
        if (roleType == RoleType.CANDIDATE
                && assessmentDecisionDomainService.isEliminatedFromPriorEpoch(user.getId(), time)) {
            throw new Forbidden("已在该方向考核中被淘汰，无法提交答案");
        }
    }

    private void validateSessionDeadline(User user, AssessmentTime time) {
        if (!Boolean.TRUE.equals(time.getTimeLimit())) {
            return;
        }
        assessmentSessionRepository
                .findByUserIdAndAssessmentTimeId(user.getId(), time.getId())
                .ifPresent(session -> {
                    if (session.getDeadline() != null
                            && LocalDateTime.now().isAfter(session.getDeadline())) {
                        throw new BadRequest("考核时间已到，无法提交答案");
                    }
                });
    }

    private void validateFileId(Long fileId) {
        if (fileId == null) {
            return;
        }
        File file = fileDomainService.getFileById(fileId);
        if (file == null) {
            throw new BadRequest("文件不存在");
        }
        if (file.getType() != FileType.WORK) {
            throw new BadRequest("文件类型不匹配，期望 WORK");
        }
    }

    private AssessmentJudgement judgeSingleChoice(AssessmentAnswer answer, AssessmentQuestion question) {
        if (!(question.getContent()instanceof SingleChoiceContent content)) {
            throw new GlobalException("单选题内容配置错误");
        }
        boolean accepted = normalizeAnswer(answer.getContent()).equals(normalizeAnswer(content.getCorrectAnswer()));
        return createAutomaticJudgement(answer, question, accepted ? ObjectiveResultCode.AC : ObjectiveResultCode.WA);
    }

    private AssessmentJudgement judgeMultipleChoice(AssessmentAnswer answer, AssessmentQuestion question) {
        if (!(question.getContent()instanceof MultipleChoiceContent content)) {
            throw new GlobalException("多选题内容配置错误");
        }
        Set<String> submittedAnswers = parseSubmittedMultipleChoiceAnswers(answer.getContent());
        Set<String> correctAnswers = normalizeAnswers(content.getCorrectAnswers());
        boolean accepted = submittedAnswers.equals(correctAnswers);
        return createAutomaticJudgement(answer, question, accepted ? ObjectiveResultCode.AC : ObjectiveResultCode.WA);
    }

    private AssessmentJudgement createAutomaticJudgement(
            AssessmentAnswer answer, AssessmentQuestion question, ObjectiveResultCode resultCode) {
        BigDecimal maxScore = question.getScore() == null ? BigDecimal.ZERO : question.getScore();
        BigDecimal score = resultCode == ObjectiveResultCode.AC ? maxScore : BigDecimal.ZERO;
        return AssessmentJudgement.create(
                answer.getId(),
                question.getId(),
                question.getAssessmentTimeId(),
                answer.getUserId(),
                score,
                maxScore,
                JudgementStatus.JUDGED,
                resultCode,
                JudgementSource.AUTO,
                null,
                null,
                LocalDateTime.now());
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
}
