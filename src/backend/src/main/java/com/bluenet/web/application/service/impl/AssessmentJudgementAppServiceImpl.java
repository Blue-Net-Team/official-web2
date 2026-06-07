package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.AssessmentDecisionResult;
import com.bluenet.web.application.AssessmentJudgementResult;
import com.bluenet.web.application.command.assessment_judgement.AssessmentJudgementCommands;
import com.bluenet.web.application.message.MessageTemplateRegistry;
import com.bluenet.web.application.message.template.AssessmentDecisionNotificationTemplate;
import com.bluenet.web.application.service.assessment.AssessmentJudgementAccessGuard;
import com.bluenet.web.application.service.AssessmentJudgementAppService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.model.enumerate.JudgementSource;
import com.bluenet.web.domain.model.enumerate.JudgementStatus;
import com.bluenet.web.domain.model.enumerate.QuestionType;
import com.bluenet.web.domain.model.enumerate.ReviewerType;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.model.entity.AssessmentAnswer;
import com.bluenet.web.domain.model.vo.AssessmentCandidateScoreRowVO;
import com.bluenet.web.domain.model.vo.AssessmentCandidateScoreboardVO;
import com.bluenet.web.domain.model.vo.AssessmentCandidateQuestionScoreVO;
import com.bluenet.web.domain.model.vo.AssessmentDecisionCandidateVO;
import com.bluenet.web.domain.model.vo.AssessmentDecisionStatisticsVO;
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
import com.bluenet.web.domain.repository.AssessmentQuestionRepository;
import com.bluenet.web.domain.repository.AssessmentTeamRepository;
import com.bluenet.web.domain.repository.AssessmentTimeRepository;
import com.bluenet.web.domain.repository.CommentRepository;
import com.bluenet.web.domain.service.AssessmentDecisionDomainService;
import com.bluenet.web.domain.service.AssessmentJudgementDomainService;
import com.bluenet.web.domain.service.UserDomainService;
import com.bluenet.web.application.message.MessageDispatcher;
import com.bluenet.web.domain.model.enumerate.MessageChannel;
import com.bluenet.web.application.message.MessageRequest;
import com.bluenet.web.domain.model.policy.RoleHierarchy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 考核评判应用服务实现。
 * <p>
 * 实现考核评判聚合在应用层的业务逻辑编排。
 * </p>
 */
@Service
@Slf4j
public class AssessmentJudgementAppServiceImpl implements AssessmentJudgementAppService {
    private final AssessmentJudgementDomainService assessmentJudgementDomainService;
    private final AssessmentDecisionDomainService assessmentDecisionDomainService;
    private final AssessmentAnswerRepository assessmentAnswerRepository;
    private final AssessmentQuestionRepository assessmentQuestionRepository;
    private final AssessmentTimeRepository assessmentTimeRepository;
    private final AssessmentJudgementRepository assessmentJudgementRepository;
    private final AssessmentDecisionRepository assessmentDecisionRepository;
    private final AssessmentTeamRepository assessmentTeamRepository;
    private final UserDomainService userDomainService;
    private final MessageDispatcher messageDispatcher;
    private final AssessmentJudgementAccessGuard accessGuard;
    private final AssessmentDecisionNotificationTemplate notificationTemplate;
    private final CommentRepository commentRepository;

    /**
     * 保留原应用服务依赖入口，内部组合访问 guard 和通知模板，降低调用方和单测构造成本。
     *
     * @param assessmentJudgementDomainService
     *            考核评判领域服务
     * @param assessmentDecisionDomainService
     *            考核决策领域服务
     * @param assessmentAnswerRepository
     *            考核答案仓储
     * @param assessmentQuestionRepository
     *            考核题目仓储
     * @param assessmentTimeRepository
     *            考核时间仓储
     * @param assessmentJudgementRepository
     *            考核评判仓储
     * @param assessmentDecisionRepository
     *            考核决策仓储
     * @param assessmentTeamRepository
     *            考核队伍仓储
     * @param userDomainService
     *            用户领域服务
     * @param messageDispatcher
     *            消息分发器
     * @param messageTemplateRegistry
     *            消息模板注册表
     */
    public AssessmentJudgementAppServiceImpl(
            AssessmentJudgementDomainService assessmentJudgementDomainService,
            AssessmentDecisionDomainService assessmentDecisionDomainService,
            AssessmentAnswerRepository assessmentAnswerRepository,
            AssessmentQuestionRepository assessmentQuestionRepository,
            AssessmentTimeRepository assessmentTimeRepository,
            AssessmentJudgementRepository assessmentJudgementRepository,
            AssessmentDecisionRepository assessmentDecisionRepository,
            AssessmentTeamRepository assessmentTeamRepository,
            UserDomainService userDomainService,
            MessageDispatcher messageDispatcher,
            MessageTemplateRegistry messageTemplateRegistry,
            CommentRepository commentRepository) {
        this.assessmentJudgementDomainService = assessmentJudgementDomainService;
        this.assessmentDecisionDomainService = assessmentDecisionDomainService;
        this.assessmentAnswerRepository = assessmentAnswerRepository;
        this.assessmentQuestionRepository = assessmentQuestionRepository;
        this.assessmentTimeRepository = assessmentTimeRepository;
        this.assessmentJudgementRepository = assessmentJudgementRepository;
        this.assessmentDecisionRepository = assessmentDecisionRepository;
        this.assessmentTeamRepository = assessmentTeamRepository;
        this.userDomainService = userDomainService;
        this.messageDispatcher = messageDispatcher;
        this.accessGuard = new AssessmentJudgementAccessGuard(assessmentTimeRepository);
        this.notificationTemplate = new AssessmentDecisionNotificationTemplate(messageTemplateRegistry);
        this.commentRepository = commentRepository;
    }

    /**
     * 获取指定答案的最新评判结果。
     *
     * @param answerId
     *            答案ID
     * @return 最新评判结果
     */
    @Override
    public AssessmentJudgementResult getLatestByAnswerId(Long answerId) {
        return toResult(assessmentJudgementDomainService.getLatestByAnswerId(answerId));
    }

    /**
     * 获取指定题目的全部评判结果。
     *
     * @param questionId
     *            题目ID
     * @return 评判结果列表
     */
    @Override
    public List<AssessmentJudgementResult> listByQuestionId(Long questionId) {
        return assessmentJudgementDomainService.listByQuestionId(questionId)
                .stream()
                .map(this::toResult)
                .toList();
    }

    /**
     * 方向管理员确认文件上传题的最终评分。
     *
     * @param command
     *            最终评分命令
     * @return 评判结果
     */
    @Override
    @Transactional
    public AssessmentJudgementResult finalizeScore(AssessmentJudgementCommands.FinalizeScoreCommand command) {
        UserVO currentUser = accessGuard.requireCurrentUser();
        RoleType roleType = accessGuard.requireRole(currentUser);
        if (!RoleHierarchy.isDirectionAdminOrAbove(roleType)) {
            throw new Forbidden("只有方向管理员及以上权限可以确认最终评分");
        }

        AssessmentAnswer answer = assessmentAnswerRepository.findById(command.answerId())
                .orElseThrow(() -> new DataNotFound("答题不存在，ID: " + command.answerId()));
        AssessmentQuestion question = assessmentQuestionRepository.findById(answer.getQuestionId())
                .orElseThrow(() -> new DataNotFound("题目不存在，ID: " + answer.getQuestionId()));
        if (question.getQuestionType() != QuestionType.FILE_UPLOAD) {
            throw new BadRequest("只有文件上传题可以确认最终评分");
        }
        if (command.score().compareTo(BigDecimal.ZERO) < 0
                || command.score().compareTo(question.getScore()) > 0) {
            throw new BadRequest("最终评分必须在 0 到题目满分之间");
        }
        if (!commentRepository.existsByAnswerIdAndUserId(answer.getId(), currentUser.getId())) {
            throw new BadRequest("确认最终评分前，您需要先对该答案发表个人评论");
        }

        AssessmentJudgementResult result;
        if (answer.getTeamId() != null) {
            result = propagateFinalizedJudgementToTeamMembers(
                    answer.getTeamId(),
                    question,
                    command,
                    currentUser,
                    roleType,
                    answer.getUserId());
        } else {
            AssessmentJudgementVO judgement = AssessmentJudgementVO.builder()
                    .answerId(answer.getId())
                    .questionId(question.getId())
                    .assessmentTimeId(question.getAssessmentTimeId())
                    .userId(answer.getUserId())
                    .score(command.score())
                    .maxScore(question.getScore())
                    .status(JudgementStatus.JUDGED)
                    .source(JudgementSource.ADMIN_FINALIZED)
                    .reviewerId(currentUser.getId())
                    .reviewerType(resolveReviewerType(roleType))
                    .comment(command.comment())
                    .judgedAt(LocalDateTime.now())
                    .build();
            result = toResult(
                    assessmentJudgementDomainService.finalizeJudgement(judgement));
        }

        return result;
    }

    /**
     * 保存考生在某轮考核中的通过或淘汰决策。
     *
     * @param command
     *            决策命令，包含考生ID、考核时间ID、是否通过和可选备注
     * @return 保存后的决策结果
     */
    @Override
    @Transactional
    public AssessmentDecisionResult decideAssessment(AssessmentJudgementCommands.DecideAssessmentCommand command) {
        UserVO currentUser = accessGuard.requireCurrentUser();
        RoleType roleType = accessGuard.requireRole(currentUser);
        if (!RoleHierarchy.isDirectionAdminOrAbove(roleType)) {
            throw new Forbidden("只有方向管理员及以上权限可以设置最终通过决策");
        }

        AssessmentDecisionVO decision = AssessmentDecisionVO.builder()
                .userId(command.userId())
                .assessmentTimeId(command.assessmentTimeId())
                .passed(command.passed())
                .decidedBy(currentUser.getId())
                .decisionComment(command.decisionComment())
                .build();
        AssessmentDecisionResult result = toDecisionResult(assessmentDecisionDomainService.saveDecision(decision));

        AssessmentTime assessmentTime = assessmentTimeRepository.findById(command.assessmentTimeId())
                .orElseThrow(() -> new DataNotFound("考核时间不存在，ID: " + command.assessmentTimeId()));
        if (!assessmentTime.isResultsPublished()) {
            assessmentTime.publishResults();
            assessmentTimeRepository.update(assessmentTime);
        }

        return result;
    }

    /**
     * 查询题目维度评分汇总，进入 repository 前统一做权限和关键词处理。
     *
     * @param assessmentTimeId
     *            考核时间ID
     * @param questionType
     *            题型筛选，为 null 时不筛选
     * @param keyword
     *            题目关键词，前后空格会被裁剪
     * @return 题目评分汇总 DTO 列表
     */
    @Override
    public List<AssessmentQuestionScoreboardVO> listQuestionScoreboard(
            Long assessmentTimeId,
            QuestionType questionType,
            String keyword) {
        accessGuard.requireMemberScope(assessmentTimeId);
        return assessmentJudgementRepository
                .findQuestionScoreboard(assessmentTimeId, questionType, normalizeKeyword(keyword));
    }

    /**
     * 查询指定题目的提交列表，并补充算法题完整评判历史。
     *
     * @param questionId
     *            题目ID
     * @param keyword
     *            考生关键词
     * @param status
     *            评判状态筛选（JUDGED/PENDING）
     * @return 提交评分 DTO 列表，每条附带评判历史
     */
    @Override
    public List<AssessmentQuestionSubmissionVO> listQuestionSubmissions(Long questionId, String keyword,
            String status) {
        AssessmentQuestion question = assessmentQuestionRepository.findById(questionId)
                .orElseThrow(() -> new DataNotFound("题目不存在，ID: " + questionId));
        accessGuard.requireMemberScope(question.getAssessmentTimeId());
        List<AssessmentQuestionSubmissionVO> submissions = assessmentJudgementRepository
                .findQuestionSubmissions(questionId, normalizeKeyword(keyword), validateJudgementStatus(status));
        attachSubmissionHistories(questionId, submissions);
        return submissions;
    }

    /**
     * 查询考生维度评分矩阵，并按考生聚合题目得分。
     *
     * @param assessmentTimeId
     *            考核时间ID
     * @param keyword
     *            考生关键词
     * @return 考生评分汇总 DTO 列表
     */
    @Override
    public List<AssessmentCandidateScoreboardVO> listCandidateScoreboard(Long assessmentTimeId, String keyword) {
        accessGuard.requireMemberScope(assessmentTimeId);
        List<AssessmentCandidateScoreRowVO> rows = assessmentJudgementRepository
                .findCandidateScoreRows(assessmentTimeId, normalizeKeyword(keyword));
        return buildCandidateScoreboards(assessmentTimeId, rows);
    }

    /**
     * 查询录用决策工作台数据，组合评分矩阵、已有决策和统计结果。
     *
     * @param assessmentTimeId
     *            考核时间ID
     * @param keyword
     *            考生关键词
     * @param decisionStatus
     *            决策状态筛选（PENDING/PASSED/ELIMINATED）
     * @return 决策工作台 DTO，包含统计和候选人列表
     */
    @Override
    public AssessmentDecisionWorkspaceVO getDecisionWorkspace(
            Long assessmentTimeId,
            String keyword,
            String decisionStatus) {
        accessGuard.requireDecisionScope(assessmentTimeId);
        AssessmentTime assessmentTime = assessmentTimeRepository.findById(assessmentTimeId)
                .orElseThrow(() -> new DataNotFound("考核时间不存在，ID: " + assessmentTimeId));
        List<AssessmentCandidateScoreboardVO> scoreboards = listCandidateScoreboard(assessmentTimeId, keyword)
                .stream()
                .filter(
                        scoreboard -> !assessmentDecisionDomainService.isEliminatedFromPriorEpoch(
                                scoreboard.getCandidateUserId(),
                                assessmentTime))
                .toList();
        Map<Long, AssessmentDecisionVO> decisions = assessmentDecisionRepository
                .findByAssessmentTimeId(assessmentTimeId)
                .stream()
                .collect(Collectors.toMap(AssessmentDecisionVO::getUserId, Function.identity()));

        List<AssessmentDecisionCandidateVO> candidates = scoreboards.stream()
                .map(
                        scoreboard -> convertDecisionCandidate(
                                scoreboard,
                                decisions.get(scoreboard.getCandidateUserId())))
                .filter(candidate -> matchesDecisionStatus(candidate, decisionStatus))
                .sorted(
                        Comparator.comparing(
                                AssessmentDecisionCandidateVO::getStudentId,
                                Comparator.nullsLast(String::compareTo)))
                .toList();

        AssessmentDecisionStatisticsVO statistics = calculateDecisionStatistics(scoreboards, decisions);
        return AssessmentDecisionWorkspaceVO.builder()
                .statistics(statistics)
                .candidates(candidates)
                .build();
    }

    /**
     * 发布指定考核轮次的决策结果邮件通知。
     *
     * @param assessmentTimeId
     *            考核时间ID
     * @return 成功发送的邮件数量
     */
    @Override
    public int publishDecisions(Long assessmentTimeId) {
        accessGuard.requireDecisionScope(assessmentTimeId);
        AssessmentTime assessmentTime = assessmentTimeRepository.findById(assessmentTimeId)
                .orElseThrow(() -> new DataNotFound("考核时间不存在，ID: " + assessmentTimeId));

        if (!assessmentTime.isResultsPublished()) {
            assessmentTime.publishResults();
            assessmentTimeRepository.update(assessmentTime);
        }

        List<AssessmentDecisionVO> decisions = assessmentDecisionRepository
                .findByAssessmentTimeId(assessmentTimeId)
                .stream()
                .filter(d -> d.getPassed() != null)
                .toList();
        int sentCount = 0;
        for (AssessmentDecisionVO decision : decisions) {
            UserVO user = userDomainService.getUser(decision.getUserId()).orElse(null);
            if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
                log.warn("跳过无邮箱用户：userId={}", decision.getUserId());
                continue;
            }
            String subject = "[蓝网] 考核结果通知";
            String directionLabel = assessmentTime.getDirection() != null
                    ? assessmentTime.getDirection().getDescription()
                    : "全局";
            int epoch = assessmentTime.getEpoch() != null ? assessmentTime.getEpoch() : 0;

            Integer maxEpoch = assessmentTimeRepository.findMaxEpoch(
                    assessmentTime.getDirection(),
                    assessmentTime.getGrade()).orElse(null);
            boolean isFinalRound = maxEpoch != null && maxEpoch.equals(assessmentTime.getEpoch());

            String resultText;
            if (isFinalRound) {
                resultText = Boolean.TRUE.equals(decision.getPassed()) ? "录取" : "淘汰";
            } else {
                resultText = Boolean.TRUE.equals(decision.getPassed()) ? "通过" : "未通过";
            }

            String nickname = user.getNickname() != null ? user.getNickname() : user.getUsername();
            String htmlContent = notificationTemplate.buildHtml(nickname, directionLabel, epoch, resultText);
            try {
                messageDispatcher.dispatchAsync(
                        MessageRequest.html(MessageChannel.EMAIL, user.getEmail(), subject, htmlContent));
                sentCount++;
            } catch (Exception e) {
                log.error("发送决策邮件失败：userId={}, email={}", decision.getUserId(), user.getEmail(), e);
            }
        }
        return sentCount;
    }

    // ========== 权限与校验 ==========

    /**
     * 将用户角色映射为人工评审人类型。
     *
     * @param roleType
     *            用户角色类型
     * @return 对应的评审人类型
     */
    private ReviewerType resolveReviewerType(RoleType roleType) {
        return switch (roleType) {
            case SUPER_ADMIN -> ReviewerType.SUPER_ADMIN;
            case DIRECTION_ADMIN -> ReviewerType.DIRECTION_ADMIN;
            default -> ReviewerType.MEMBER;
        };
    }

    // ========== 参数规范化 ==========

    /**
     * 将关键词去除首尾空格，空字符串返回 null。
     *
     * @param keyword
     *            原始关键词
     * @return 去空格后的关键词，或 null
     */
    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }

    /**
     * 将状态字符串去除首尾空格并转大写，空字符串返回 null。
     *
     * @param status
     *            原始状态字符串
     * @return 大写状态，或 null
     */
    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        return status.trim().toUpperCase();
    }

    /**
     * 校验评判状态参数，仅接受 JUDGED 或 PENDING，非法值退化为 null（不过滤）。
     *
     * @param status
     *            原始状态字符串
     * @return 合法状态值，或 null
     */
    private String validateJudgementStatus(String status) {
        String normalized = normalizeStatus(status);
        if (normalized == null) {
            return null;
        }
        return ("JUDGED".equals(normalized) || "PENDING".equals(normalized)) ? normalized : null;
    }

    /**
     * 校验录用决策状态参数，仅接受 PENDING/PASSED/ELIMINATED，非法值退化为 null。
     *
     * @param decisionStatus
     *            原始决策状态字符串
     * @return 合法状态值，或 null
     */
    private String validateDecisionStatus(String decisionStatus) {
        String normalized = normalizeStatus(decisionStatus);
        if (normalized == null) {
            return null;
        }
        return Set.of("PENDING", "PASSED", "ELIMINATED").contains(normalized) ? normalized : null;
    }

    /**
     * 批量查询各考生的评判历史，按考生 ID 分组后附加到提交列表。
     *
     * @param questionId
     *            题目ID
     * @param submissions
     *            需要附加历史的提交 VO 列表
     */
    private void attachSubmissionHistories(Long questionId, List<AssessmentQuestionSubmissionVO> submissions) {
        if (submissions.isEmpty()) {
            return;
        }
        List<Long> userIds = submissions.stream()
                .map(AssessmentQuestionSubmissionVO::getCandidateUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (userIds.isEmpty()) {
            return;
        }
        Map<Long, List<AssessmentQuestionSubmissionHistoryVO>> historiesByUser = assessmentJudgementRepository
                .findQuestionSubmissionHistories(questionId, userIds)
                .stream()
                .collect(
                        Collectors.groupingBy(
                                history -> history.getJudgement() != null ? history.getJudgement().getUserId() : null,
                                LinkedHashMap::new,
                                Collectors.toList()));
        submissions.forEach(
                submission -> submission.setHistories(
                        historiesByUser.getOrDefault(submission.getCandidateUserId(), List.of())));
    }

    /**
     * 判断候选人是否匹配指定的决策状态筛选条件。
     *
     * @param candidate
     *            候选人 VO
     * @param decisionStatus
     *            决策状态筛选值
     * @return 是否匹配
     */
    private boolean matchesDecisionStatus(AssessmentDecisionCandidateVO candidate, String decisionStatus) {
        String normalized = validateDecisionStatus(decisionStatus);
        if (normalized == null) {
            return true;
        }
        return switch (normalized) {
            case "PENDING" -> candidate.getPassed() == null;
            case "PASSED" -> Boolean.TRUE.equals(candidate.getPassed());
            case "ELIMINATED" -> Boolean.FALSE.equals(candidate.getPassed());
            default -> true;
        };
    }

    // ========== 评分矩阵与决策聚合 ==========

    /**
     * 将考生维度扁平行数据按考生聚合，计算总分、已评和待评数量。
     */
    private List<AssessmentCandidateScoreboardVO> buildCandidateScoreboards(
            Long assessmentTimeId,
            List<AssessmentCandidateScoreRowVO> rows) {
        Map<Long, List<AssessmentCandidateScoreRowVO>> grouped = rows.stream()
                .collect(
                        Collectors.groupingBy(
                                AssessmentCandidateScoreRowVO::getCandidateUserId,
                                LinkedHashMap::new,
                                Collectors.toList()));
        List<AssessmentCandidateScoreboardVO> result = new ArrayList<>();
        for (List<AssessmentCandidateScoreRowVO> candidateRows : grouped.values()) {
            AssessmentCandidateScoreRowVO first = candidateRows.get(0);
            List<AssessmentCandidateQuestionScoreVO> questionScores = candidateRows.stream()
                    .map(row -> convertQuestionScore(assessmentTimeId, row))
                    .toList();
            BigDecimal totalScore = questionScores.stream()
                    .map(AssessmentCandidateQuestionScoreVO::getScore)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal maxScore = questionScores.stream()
                    .map(AssessmentCandidateQuestionScoreVO::getMaxScore)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            long judgedCount = questionScores.stream()
                    .filter(score -> Boolean.TRUE.equals(score.getJudged()))
                    .count();
            long pendingCount = questionScores.stream()
                    .filter(
                            score -> Boolean.TRUE.equals(score.getSubmitted())
                                    && !Boolean.TRUE.equals(score.getJudged()))
                    .count();
            result.add(
                    AssessmentCandidateScoreboardVO.builder()
                            .candidateUserId(first.getCandidateUserId())
                            .studentId(first.getStudentId())
                            .username(first.getUsername())
                            .nickname(first.getNickname())
                            .totalScore(totalScore)
                            .maxScore(maxScore)
                            .judgedQuestionCount(judgedCount)
                            .pendingJudgementCount(pendingCount)
                            .questionScores(questionScores)
                            .build());
        }
        return result;
    }

    /**
     * 将评分汇总和决策记录组合为候选人 VO。
     */
    private AssessmentDecisionCandidateVO convertDecisionCandidate(
            AssessmentCandidateScoreboardVO scoreboard,
            AssessmentDecisionVO decision) {
        return AssessmentDecisionCandidateVO.builder()
                .candidateUserId(scoreboard.getCandidateUserId())
                .studentId(scoreboard.getStudentId())
                .username(scoreboard.getUsername())
                .nickname(scoreboard.getNickname())
                .totalScore(scoreboard.getTotalScore())
                .maxScore(scoreboard.getMaxScore())
                .judgedQuestionCount(scoreboard.getJudgedQuestionCount())
                .pendingJudgementCount(scoreboard.getPendingJudgementCount())
                .decisionId(decision == null ? null : decision.getId())
                .passed(decision == null ? null : decision.getPassed())
                .decisionComment(decision == null ? null : decision.getDecisionComment())
                .decidedBy(decision == null ? null : decision.getDecidedBy())
                .decidedAt(decision == null ? null : decision.getDecidedAt())
                .questionScores(scoreboard.getQuestionScores())
                .build();
    }

    /**
     * 根据评分矩阵和决策记录计算候选人、待决策、通过和淘汰的统计数据。
     */
    private AssessmentDecisionStatisticsVO calculateDecisionStatistics(
            List<AssessmentCandidateScoreboardVO> scoreboards,
            Map<Long, AssessmentDecisionVO> decisions) {
        long candidates = scoreboards.size();
        long passed = scoreboards.stream()
                .map(scoreboard -> decisions.get(scoreboard.getCandidateUserId()))
                .filter(Objects::nonNull)
                .filter(decision -> Boolean.TRUE.equals(decision.getPassed()))
                .count();
        long eliminated = scoreboards.stream()
                .map(scoreboard -> decisions.get(scoreboard.getCandidateUserId()))
                .filter(Objects::nonNull)
                .filter(decision -> Boolean.FALSE.equals(decision.getPassed()))
                .count();
        return AssessmentDecisionStatisticsVO.builder()
                .candidates(candidates)
                .pending(candidates - passed - eliminated)
                .passed(passed)
                .eliminated(eliminated)
                .build();
    }

    /**
     * 将考生单题评分行 VO 转换为单题评分 VO。
     */
    private AssessmentCandidateQuestionScoreVO convertQuestionScore(
            Long assessmentTimeId,
            AssessmentCandidateScoreRowVO row) {
        boolean submitted = row.getAnswerId() != null;
        boolean judged = row.getJudgementId() != null;
        return AssessmentCandidateQuestionScoreVO.builder()
                .questionId(row.getQuestionId())
                .questionNo(row.getQuestionNo())
                .questionTitle(row.getQuestionTitle())
                .questionType(row.getQuestionType())
                .maxScore(row.getMaxScore())
                .answerId(row.getAnswerId())
                .submitted(submitted)
                .submitTime(row.getSubmitTime())
                .score(row.getJudgementScore())
                .judged(judged)
                .latestJudgement(convertJudgementFromScoreRow(assessmentTimeId, row))
                .build();
    }

    /**
     * 从考生评分行 VO 中提取评判信息转换为 VO。
     */
    private AssessmentJudgementVO convertJudgementFromScoreRow(Long assessmentTimeId,
            AssessmentCandidateScoreRowVO row) {
        if (row.getJudgementId() == null) {
            return null;
        }
        return AssessmentJudgementVO.builder()
                .id(row.getJudgementId())
                .answerId(row.getAnswerId())
                .questionId(row.getQuestionId())
                .assessmentTimeId(assessmentTimeId)
                .userId(row.getCandidateUserId())
                .score(row.getJudgementScore())
                .maxScore(row.getJudgementMaxScore())
                .status(row.getJudgementStatus())
                .resultCode(row.getResultCode())
                .source(row.getSource())
                .reviewerId(row.getReviewerId())
                .reviewerType(row.getReviewerType())
                .comment(row.getJudgementComment())
                .judgedAt(row.getJudgedAt())
                .build();
    }

    // ========== 结果转换 ==========

    private AssessmentJudgementResult toResult(AssessmentJudgementVO judgement) {
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
                judgement.getComment(),
                judgement.getJudgedAt());
    }

    private AssessmentDecisionResult toDecisionResult(AssessmentDecisionVO decision) {
        if (decision == null) {
            return null;
        }
        return new AssessmentDecisionResult(
                decision.getId(),
                decision.getUserId(),
                decision.getAssessmentTimeId(),
                decision.getPassed(),
                decision.getDecidedBy(),
                decision.getDecisionComment(),
                decision.getDecidedAt());
    }

    private AssessmentJudgementResult propagateFinalizedJudgementToTeamMembers(Long teamId, AssessmentQuestion question,
            AssessmentJudgementCommands.FinalizeScoreCommand command,
            UserVO currentUser, RoleType roleType,
            Long leaderUserId) {
        List<com.bluenet.web.domain.model.entity.AssessmentTeamMember> members = assessmentTeamRepository
                .findMembersByTeamId(teamId);
        if (members.isEmpty()) {
            return null;
        }

        // Batch query all member answers for this question
        List<com.bluenet.web.domain.model.entity.AssessmentAnswer> memberAnswers = assessmentAnswerRepository
                .findByTeamIdAndQuestionId(teamId, question.getId());

        LocalDateTime now = LocalDateTime.now();
        List<com.bluenet.web.domain.model.entity.AssessmentJudgement> judgementsToInsert = new ArrayList<>();
        Long leaderAnswerId = null;
        for (com.bluenet.web.domain.model.entity.AssessmentAnswer memberAnswer : memberAnswers) {
            com.bluenet.web.domain.model.entity.AssessmentJudgement memberJudgement = com.bluenet.web.domain.model.entity.AssessmentJudgement
                    .create(
                            memberAnswer.getId(),
                            question.getId(),
                            question.getAssessmentTimeId(),
                            memberAnswer.getUserId(),
                            command.score(),
                            question.getScore(),
                            JudgementStatus.JUDGED,
                            null,
                            JudgementSource.ADMIN_FINALIZED,
                            currentUser.getId(),
                            resolveReviewerType(roleType),
                            command.comment(),
                            now);
            memberJudgement.setCreatedAt(now);
            memberJudgement.setUpdatedAt(now);
            judgementsToInsert.add(memberJudgement);

            if (memberAnswer.getUserId().equals(leaderUserId)) {
                leaderAnswerId = memberAnswer.getId();
            }
        }

        if (!judgementsToInsert.isEmpty()) {
            assessmentJudgementRepository.batchInsert(judgementsToInsert);
            log.info(
                    "批量创建组员最终评分，teamId: {}, questionId: {}, count: {}",
                    teamId,
                    question.getId(),
                    judgementsToInsert.size());
        }

        if (leaderAnswerId == null) {
            return null;
        }
        return toResult(assessmentJudgementDomainService.getLatestByAnswerId(leaderAnswerId));
    }
}
