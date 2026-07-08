package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.AssessmentAnswerResult;
import com.bluenet.web.application.AssessmentJudgementResult;
import com.bluenet.web.application.CommentResult;
import com.bluenet.web.application.command.assessment_answer.AssessmentAnswerCommands;
import com.bluenet.web.application.service.AssessmentAnswerAppService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.AssessmentAnswer;
import com.bluenet.web.domain.model.entity.AssessmentJudgement;
import com.bluenet.web.domain.model.entity.AssessmentQuestion;
import com.bluenet.web.domain.model.entity.AssessmentTeam;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.entity.Comment;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.QuestionType;
import com.bluenet.web.domain.repository.AssessmentAnswerRepository;
import com.bluenet.web.domain.repository.AssessmentJudgementRepository;
import com.bluenet.web.domain.repository.AssessmentQuestionRepository;
import com.bluenet.web.domain.repository.AssessmentTeamRepository;
import com.bluenet.web.domain.repository.AssessmentTimeRepository;
import com.bluenet.web.domain.repository.CommentRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.domain.service.AssessmentAnswerDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 考核答案应用服务实现。
 * <p>
 * 负责参数校验、调用领域服务生成/修改实体、统一持久化与事务控制、结果转换。
 * </p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AssessmentAnswerAppServiceImpl implements AssessmentAnswerAppService {

    private final AssessmentQuestionRepository assessmentQuestionRepository;
    private final AssessmentAnswerRepository assessmentAnswerRepository;
    private final AssessmentTimeRepository assessmentTimeRepository;
    private final AssessmentTeamRepository assessmentTeamRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final AssessmentJudgementRepository assessmentJudgementRepository;
    private final AssessmentAnswerDomainService assessmentAnswerDomainService;

    @Override
    @Transactional
    public AssessmentAnswerResult createAnswer(AssessmentAnswerCommands.CreateAssessmentAnswerCommand command) {
        User currentUser = userRepository.findById(command.userId())
                .orElseThrow(() -> new SecurityException("未登录"));

        AssessmentQuestion question = assessmentQuestionRepository.findById(command.questionId())
                .orElseThrow(() -> new DataNotFound("题目不存在，ID: " + command.questionId()));

        AssessmentAnswer answer = assessmentAnswerDomainService.prepareAnswer(
                currentUser,
                question,
                command.content(),
                command.language(),
                command.fileId());

        assessmentAnswerRepository.save(answer);

        if (answer.getTeamId() != null) {
            List<AssessmentAnswer> teamMemberAnswers = assessmentAnswerDomainService.prepareTeamMemberAnswers(
                    answer,
                    question,
                    command.content(),
                    command.language(),
                    command.fileId());
            if (!teamMemberAnswers.isEmpty()) {
                assessmentAnswerRepository.batchInsert(teamMemberAnswers);
            }
        }

        AssessmentJudgement judgement = assessmentAnswerDomainService.prepareObjectiveJudgement(answer, question);
        if (judgement != null) {
            assessmentJudgementRepository.save(judgement);
        }

        log.info(
                "创建答案成功，userId: {}, questionId: {}, answerId: {}",
                command.userId(),
                command.questionId(),
                answer.getId());

        AssessmentAnswerResult result = toResult(answer, judgement, Collections.emptyList());
        if (question.getQuestionType().isChoiceQuestion()) {
            return result.withJudgementErased();
        }
        return result;
    }

    @Override
    @Transactional
    public AssessmentAnswerResult updateAnswer(AssessmentAnswerCommands.UpdateAssessmentAnswerCommand command) {
        User currentUser = userRepository.findById(command.userId())
                .orElseThrow(() -> new SecurityException("未登录"));

        AssessmentQuestion question = assessmentQuestionRepository.findById(command.questionId())
                .orElseThrow(() -> new DataNotFound("题目不存在，ID: " + command.questionId()));

        AssessmentAnswer existingAnswer = assessmentAnswerRepository
                .findByUserIdAndQuestionId(command.userId(), command.questionId())
                .orElseThrow(() -> new BadRequest("尚未提交过该题目的答案，无法修改"));

        List<AssessmentAnswer> answersToUpdate = assessmentAnswerDomainService.prepareUpdatedAnswers(
                currentUser,
                question,
                existingAnswer,
                command.content(),
                command.language(),
                command.fileId());

        for (AssessmentAnswer answer : answersToUpdate) {
            assessmentAnswerRepository.save(answer);
        }

        AssessmentAnswer updatedAnswer = answersToUpdate.stream()
                .filter(answer -> answer.getUserId().equals(command.userId()))
                .findFirst()
                .orElse(existingAnswer);

        log.info("update answer success for answer {}", updatedAnswer.getId());

        AssessmentJudgement judgement = assessmentAnswerDomainService
                .prepareObjectiveJudgement(updatedAnswer, question);
        if (judgement != null) {
            assessmentJudgementRepository.save(judgement);
        }

        AssessmentAnswerResult result = toResult(updatedAnswer, judgement, Collections.emptyList());
        if (question.getQuestionType().isChoiceQuestion()) {
            return result.withJudgementErased();
        }
        return result;
    }

    @Override
    public AssessmentAnswerResult getMyAnswer(Long userId, Long questionId) {
        AssessmentQuestion question = assessmentQuestionRepository.findById(questionId)
                .orElse(null);

        Optional<AssessmentAnswer> answerOpt = assessmentAnswerRepository
                .findByUserIdAndQuestionId(userId, questionId);

        if (answerOpt.isEmpty()) {
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
        AssessmentJudgement judgement = findLatestJudgement(answer);
        List<Comment> comments = commentRepository.findByAnswerId(answer.getId());
        Map<Long, String> usernameMap = fetchUsernameMap(comments);
        List<CommentResult> commentResults = comments.stream()
                .map(comment -> toCommentResult(comment, usernameMap.get(comment.getUserId())))
                .toList();
        AssessmentAnswerResult result = toResult(answer, judgement, commentResults);

        if (question != null && question.getQuestionType().isChoiceQuestion()) {
            return result.withJudgementErased();
        }
        return result;
    }

    private AssessmentJudgement findLatestJudgement(AssessmentAnswer answer) {
        if (answer.getId() == null) {
            return null;
        }
        return assessmentJudgementRepository.findLatestByAnswerId(answer.getId()).orElse(null);
    }

    private Map<Long, String> fetchUsernameMap(List<Comment> comments) {
        Set<Long> userIds = comments.stream()
                .map(Comment::getUserId)
                .filter(userId -> userId != null)
                .collect(Collectors.toSet());
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return userIds.stream()
                .map(userRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toMap(User::getId, User::getUsername));
    }

    private CommentResult toCommentResult(Comment comment, String username) {
        return CommentResult.builder()
                .id(comment.getId())
                .answerId(comment.getAnswerId())
                .userId(comment.getUserId())
                .username(username)
                .content(comment.getContent())
                .score(comment.getScore())
                .commentTime(comment.getCommentTime())
                .build();
    }

    private AssessmentAnswerResult toResult(AssessmentAnswer answer, AssessmentJudgement judgement,
            List<CommentResult> comments) {
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

    private AssessmentJudgementResult toJudgementResult(AssessmentJudgement judgement) {
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
}
