package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.result.assessment.AssessmentDecisionResult;
import com.bluenet.web.application.result.assessment.AssessmentJudgementResult;
import com.bluenet.web.application.command.assessment_judgement.AssessmentJudgementCommands;
import com.bluenet.web.application.service.assessment.AssessmentDecisionPublicationService;
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
import com.bluenet.web.domain.model.entity.AssessmentDecision;
import com.bluenet.web.domain.model.entity.AssessmentJudgement;
import com.bluenet.web.domain.model.entity.AssessmentTeam;
import com.bluenet.web.domain.model.readmodel.AssessmentCandidateScoreRowReadModel;
import com.bluenet.web.application.result.assessment.AssessmentCandidateQuestionScore;
import com.bluenet.web.application.result.assessment.AssessmentCandidateScoreboard;
import com.bluenet.web.application.result.assessment.AssessmentDecisionCandidate;
import com.bluenet.web.application.result.assessment.AssessmentDecisionStatistics;
import com.bluenet.web.application.result.assessment.AssessmentDecisionWorkspace;
import com.bluenet.web.application.result.assessment.AssessmentQuestionScoreboard;
import com.bluenet.web.domain.model.readmodel.AssessmentQuestionSubmissionHistoryReadModel;
import com.bluenet.web.domain.model.readmodel.AssessmentQuestionSubmissionReadModel;
import com.bluenet.web.domain.model.entity.AssessmentQuestion;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.infrastructure.security.principal.RoleTypeResolver;
import com.bluenet.web.domain.repository.AssessmentAnswerRepository;
import com.bluenet.web.domain.repository.AssessmentDecisionRepository;
import com.bluenet.web.domain.repository.AssessmentJudgementRepository;
import com.bluenet.web.domain.repository.AssessmentQuestionRepository;
import com.bluenet.web.domain.repository.AssessmentTeamRepository;
import com.bluenet.web.domain.repository.AssessmentTimeRepository;
import com.bluenet.web.domain.repository.CommentRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.domain.service.AssessmentDecisionDomainService;
import com.bluenet.web.domain.service.AssessmentJudgementDomainService;
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
    private final UserRepository userRepository;
    private final AssessmentJudgementAccessGuard accessGuard;
    private final AssessmentDecisionPublicationService publicationService;
    private final CommentRepository commentRepository;
    private final RoleTypeResolver roleTypeResolver;

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
     * @param userRepository
     *            用户仓储
     * @param publicationService
     *            决策发布服务
     * @param commentRepository
     *            评论仓储
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
            UserRepository userRepository,
            AssessmentDecisionPublicationService publicationService,
            CommentRepository commentRepository,
            RoleTypeResolver roleTypeResolver) {
        this.assessmentJudgementDomainService = assessmentJudgementDomainService;
        this.assessmentDecisionDomainService = assessmentDecisionDomainService;
        this.assessmentAnswerRepository = assessmentAnswerRepository;
        this.assessmentQuestionRepository = assessmentQuestionRepository;
        this.assessmentTimeRepository = assessmentTimeRepository;
        this.assessmentJudgementRepository = assessmentJudgementRepository;
        this.assessmentDecisionRepository = assessmentDecisionRepository;
        this.assessmentTeamRepository = assessmentTeamRepository;
        this.userRepository = userRepository;
        this.accessGuard = new AssessmentJudgementAccessGuard(assessmentTimeRepository, roleTypeResolver);
        this.publicationService = publicationService;
        this.commentRepository = commentRepository;
        this.roleTypeResolver = roleTypeResolver;
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
        return assessmentJudgementRepository.findLatestByAnswerId(answerId)
                .map(this::entityToResult)
                .orElseThrow(() -> new DataNotFound("该答案暂无评判记录，ID: " + answerId));
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
        return assessmentJudgementRepository.findAllByQuestionId(questionId)
                .stream()
                .map(this::entityToResult)
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
        User currentUser = accessGuard.requireCurrentUser();
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
        LocalDateTime now = LocalDateTime.now();
        if (answer.getTeamId() != null) {
            // 组队场景：根据是否已有 ADMIN_FINALIZED 判断首次/再次评分
            boolean hasExistingFinalized = assessmentJudgementRepository
                    .findLatestByAnswerIdAndSource(answer.getId(), JudgementSource.ADMIN_FINALIZED)
                    .isPresent();
            if (hasExistingFinalized) {
                // 再次评分 → 只更新当前答案（队长或队员）
                result = finalizeSingleAnswer(answer, question, command, currentUser, roleType, now);
            } else {
                // 首次评分 → 判断是否为队长
                AssessmentTeam team = assessmentTeamRepository.findById(answer.getTeamId())
                        .orElseThrow(() -> new DataNotFound("队伍不存在，ID: " + answer.getTeamId()));
                if (team.isLeader(answer.getUserId())) {
                    result = finalizeTeamFirstTime(answer, question, command, currentUser, roleType, team);
                } else {
                    result = finalizeSingleAnswer(answer, question, command, currentUser, roleType, now);
                }
            }
        } else {
            result = finalizeSingleAnswer(answer, question, command, currentUser, roleType, now);
        }

        return result;
    }

    /**
     * 为单个答案确认最终评分。
     */
    private AssessmentJudgementResult finalizeSingleAnswer(
            AssessmentAnswer answer,
            AssessmentQuestion question,
            AssessmentJudgementCommands.FinalizeScoreCommand command,
            User currentUser,
            RoleType roleType,
            LocalDateTime judgedAt) {
        AssessmentJudgement judgement = AssessmentJudgement.create(
                answer.getId(),
                question.getId(),
                question.getAssessmentTimeId(),
                answer.getUserId(),
                command.score(),
                question.getScore(),
                JudgementStatus.JUDGED,
                null,
                JudgementSource.ADMIN_FINALIZED,
                currentUser.getId(),
                resolveReviewerType(roleType),
                judgedAt);
        return entityToResult(assessmentJudgementDomainService.finalizeJudgement(judgement));
    }

    /**
     * 队长首次评分：统一批量插入所有尚无 ADMIN_FINALIZED 的成员（包括队长）。
     */
    private AssessmentJudgementResult finalizeTeamFirstTime(
            AssessmentAnswer leaderAnswer,
            AssessmentQuestion question,
            AssessmentJudgementCommands.FinalizeScoreCommand command,
            User currentUser,
            RoleType roleType,
            AssessmentTeam team) {
        LocalDateTime now = LocalDateTime.now();

        // 1. 查询全队所有答案
        List<AssessmentAnswer> allAnswers = assessmentAnswerRepository
                .findByTeamIdAndQuestionId(team.getId(), question.getId());

        // 2. 批量查询已有 ADMIN_FINALIZED 的 answerIds
        List<Long> allAnswerIds = allAnswers.stream()
                .map(AssessmentAnswer::getId)
                .toList();
        List<Long> finalizedAnswerIds = assessmentJudgementRepository
                .findAnswerIdsBySource(allAnswerIds, JudgementSource.ADMIN_FINALIZED);
        Set<Long> finalizedAnswerIdSet = new java.util.HashSet<>(finalizedAnswerIds);

        // 3. 统一构建所有需要插入的 judgement（包括队长和队员）
        List<AssessmentJudgement> judgementsToInsert = new ArrayList<>();
        for (AssessmentAnswer answer : allAnswers) {
            if (finalizedAnswerIdSet.contains(answer.getId())) {
                continue; // 跳过已有 ADMIN_FINALIZED 的成员
            }
            AssessmentJudgement judgement = AssessmentJudgement.create(
                    answer.getId(),
                    question.getId(),
                    question.getAssessmentTimeId(),
                    answer.getUserId(),
                    command.score(),
                    question.getScore(),
                    JudgementStatus.JUDGED,
                    null,
                    JudgementSource.ADMIN_FINALIZED,
                    currentUser.getId(),
                    resolveReviewerType(roleType),
                    now);
            judgement.setCreatedAt(now);
            judgement.setUpdatedAt(now);
            judgementsToInsert.add(judgement);
        }

        // 4. 统一批量插入
        if (!judgementsToInsert.isEmpty()) {
            assessmentJudgementRepository.batchInsert(judgementsToInsert);
            log.info(
                    "队长首次评分批量传播，teamId: {}, questionId: {}, count: {}",
                    team.getId(),
                    question.getId(),
                    judgementsToInsert.size());
        }

        // 5. 查询队长最新记录并返回
        AssessmentJudgement leaderEntity = assessmentJudgementRepository
                .findLatestByAnswerIdAndSource(leaderAnswer.getId(), JudgementSource.ADMIN_FINALIZED)
                .orElseThrow(() -> new com.bluenet.web.domain.exception.GlobalException("队长评分记录创建失败"));
        return entityToResult(leaderEntity);
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
        User currentUser = accessGuard.requireCurrentUser();
        RoleType roleType = accessGuard.requireRole(currentUser);
        if (!RoleHierarchy.isDirectionAdminOrAbove(roleType)) {
            throw new Forbidden("只有方向管理员及以上权限可以设置最终通过决策");
        }

        AssessmentDecision decision = assessmentDecisionRepository
                .findByUserIdAndAssessmentTimeId(command.userId(), command.assessmentTimeId())
                .map(existing -> {
                    existing.updatePassed(command.passed(), currentUser.getId(), command.decisionComment());
                    assessmentDecisionRepository.save(existing);
                    return existing;
                })
                .orElseGet(() -> {
                    AssessmentDecision created = AssessmentDecision.create(
                            command.userId(),
                            command.assessmentTimeId(),
                            command.passed(),
                            currentUser.getId(),
                            command.decisionComment());
                    created.decideNow();
                    assessmentDecisionRepository.save(created);
                    return created;
                });
        AssessmentDecisionResult result = toDecisionResult(decision);

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
    public List<AssessmentQuestionScoreboard> listQuestionScoreboard(
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
    public List<AssessmentQuestionSubmissionReadModel> listQuestionSubmissions(Long questionId, String keyword,
            String status) {
        AssessmentQuestion question = assessmentQuestionRepository.findById(questionId)
                .orElseThrow(() -> new DataNotFound("题目不存在，ID: " + questionId));
        accessGuard.requireMemberScope(question.getAssessmentTimeId());
        List<AssessmentQuestionSubmissionReadModel> submissions = assessmentJudgementRepository
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
    public List<AssessmentCandidateScoreboard> listCandidateScoreboard(Long assessmentTimeId, String keyword) {
        accessGuard.requireMemberScope(assessmentTimeId);
        List<AssessmentCandidateScoreRowReadModel> rows = assessmentJudgementRepository
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
    public AssessmentDecisionWorkspace getDecisionWorkspace(
            Long assessmentTimeId,
            String keyword,
            String decisionStatus) {
        accessGuard.requireDecisionScope(assessmentTimeId);
        AssessmentTime assessmentTime = assessmentTimeRepository.findById(assessmentTimeId)
                .orElseThrow(() -> new DataNotFound("考核时间不存在，ID: " + assessmentTimeId));
        List<AssessmentCandidateScoreboard> scoreboards = listCandidateScoreboard(assessmentTimeId, keyword)
                .stream()
                .filter(
                        scoreboard -> !assessmentDecisionDomainService.isEliminatedFromPriorEpoch(
                                scoreboard.getCandidateUserId(),
                                assessmentTime))
                .toList();
        Map<Long, AssessmentDecision> decisions = assessmentDecisionRepository
                .findByAssessmentTimeId(assessmentTimeId)
                .stream()
                .collect(Collectors.toMap(AssessmentDecision::getUserId, Function.identity()));

        List<AssessmentDecisionCandidate> candidates = scoreboards.stream()
                .map(
                        scoreboard -> convertDecisionCandidate(
                                scoreboard,
                                decisions.get(scoreboard.getCandidateUserId())))
                .filter(candidate -> matchesDecisionStatus(candidate, decisionStatus))
                .sorted(
                        Comparator.comparing(
                                AssessmentDecisionCandidate::isReferred,
                                Comparator.reverseOrder())
                                .thenComparing(
                                        AssessmentDecisionCandidate::getStudentId,
                                        Comparator.nullsLast(String::compareTo)))
                .toList();

        AssessmentDecisionStatistics statistics = calculateDecisionStatistics(scoreboards, decisions);
        return AssessmentDecisionWorkspace.builder()
                .statistics(statistics)
                .candidates(candidates)
                .build();
    }

    /**
     * 发布指定考核轮次的决策结果邮件通知，并在全局最终考核通过时升级考生角色。
     *
     * @param assessmentTimeId
     *            考核时间ID
     * @return 成功发布的考生数量
     */
    @Override
    public int publishDecisions(Long assessmentTimeId) {
        accessGuard.requireDecisionScope(assessmentTimeId);
        AssessmentTime assessmentTime = assessmentTimeRepository.findById(assessmentTimeId)
                .orElseThrow(() -> new DataNotFound("考核时间不存在，ID: " + assessmentTimeId));

        if (!assessmentTime.isResultsPublished()) {
            assessmentTime.publishResults();
            assessmentTimeRepository.save(assessmentTime);
        }

        List<AssessmentDecision> decisions = assessmentDecisionRepository
                .findByAssessmentTimeId(assessmentTimeId)
                .stream()
                .filter(d -> d.getPassed() != null)
                .toList();
        int sentCount = 0;
        for (AssessmentDecision decision : decisions) {
            try {
                publicationService.publish(decision, assessmentTime);
                sentCount++;
            } catch (Exception e) {
                log.error("发布单个考生决策失败：userId={}", decision.getUserId(), e);
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
    private void attachSubmissionHistories(Long questionId, List<AssessmentQuestionSubmissionReadModel> submissions) {
        if (submissions.isEmpty()) {
            return;
        }
        List<Long> userIds = submissions.stream()
                .map(AssessmentQuestionSubmissionReadModel::getCandidateUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (userIds.isEmpty()) {
            return;
        }
        Map<Long, List<AssessmentQuestionSubmissionHistoryReadModel>> historiesByUser = assessmentJudgementRepository
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
    private boolean matchesDecisionStatus(AssessmentDecisionCandidate candidate, String decisionStatus) {
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
    private List<AssessmentCandidateScoreboard> buildCandidateScoreboards(
            Long assessmentTimeId,
            List<AssessmentCandidateScoreRowReadModel> rows) {
        Map<Long, List<AssessmentCandidateScoreRowReadModel>> grouped = rows.stream()
                .collect(
                        Collectors.groupingBy(
                                AssessmentCandidateScoreRowReadModel::getCandidateUserId,
                                LinkedHashMap::new,
                                Collectors.toList()));
        List<AssessmentCandidateScoreboard> result = new ArrayList<>();
        for (List<AssessmentCandidateScoreRowReadModel> candidateRows : grouped.values()) {
            AssessmentCandidateScoreRowReadModel first = candidateRows.get(0);
            List<AssessmentCandidateQuestionScore> questionScores = candidateRows.stream()
                    .map(row -> convertQuestionScore(assessmentTimeId, row))
                    .toList();
            BigDecimal totalScore = questionScores.stream()
                    .map(AssessmentCandidateQuestionScore::getScore)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal maxScore = questionScores.stream()
                    .map(AssessmentCandidateQuestionScore::getMaxScore)
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
                    AssessmentCandidateScoreboard.builder()
                            .candidateUserId(first.getCandidateUserId())
                            .studentId(first.getStudentId())
                            .username(first.getUsername())
                            .nickname(first.getNickname())
                            .totalScore(totalScore)
                            .maxScore(maxScore)
                            .judgedQuestionCount(judgedCount)
                            .pendingJudgementCount(pendingCount)
                            .questionScores(questionScores)
                            .teamId(first.getTeamId())
                            .teamName(first.getTeamName())
                            .isLeader(Boolean.TRUE.equals(first.getIsLeader()))
                            .internalReferralCode(first.getInternalReferralCode())
                            .referralUserName(first.getReferralUserName())
                            .build());
        }
        return result;
    }

    /**
     * 将评分汇总和决策记录组合为候选人 VO。
     */
    private AssessmentDecisionCandidate convertDecisionCandidate(
            AssessmentCandidateScoreboard scoreboard,
            AssessmentDecision decision) {
        return AssessmentDecisionCandidate.builder()
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
                .teamId(scoreboard.getTeamId())
                .teamName(scoreboard.getTeamName())
                .isLeader(scoreboard.getIsLeader())
                .internalReferralCode(scoreboard.getInternalReferralCode())
                .referralUserName(scoreboard.getReferralUserName())
                .build();
    }

    /**
     * 根据评分矩阵和决策记录计算候选人、待决策、通过和淘汰的统计数据。
     */
    private AssessmentDecisionStatistics calculateDecisionStatistics(
            List<AssessmentCandidateScoreboard> scoreboards,
            Map<Long, AssessmentDecision> decisions) {
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
        return AssessmentDecisionStatistics.builder()
                .candidates(candidates)
                .pending(candidates - passed - eliminated)
                .passed(passed)
                .eliminated(eliminated)
                .build();
    }

    /**
     * 将考生单题评分行 VO 转换为单题评分 VO。
     */
    private AssessmentCandidateQuestionScore convertQuestionScore(
            Long assessmentTimeId,
            AssessmentCandidateScoreRowReadModel row) {
        boolean submitted = row.getAnswerId() != null;
        boolean judged = row.getJudgementId() != null;
        return AssessmentCandidateQuestionScore.builder()
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
     * 从考生评分行 VO 中提取评判信息转换为实体。
     */
    private AssessmentJudgement convertJudgementFromScoreRow(Long assessmentTimeId,
            AssessmentCandidateScoreRowReadModel row) {
        if (row.getJudgementId() == null) {
            return null;
        }
        return AssessmentJudgement.reconstruct(
                row.getJudgementId(),
                row.getAnswerId(),
                row.getQuestionId(),
                assessmentTimeId,
                row.getCandidateUserId(),
                row.getJudgementScore(),
                row.getJudgementMaxScore(),
                row.getJudgementStatus(),
                row.getResultCode(),
                row.getSource(),
                row.getReviewerId(),
                row.getReviewerType(),
                row.getJudgedAt(),
                null,
                null);
    }

    // ========== 结果转换 ==========

    private AssessmentJudgementResult entityToResult(AssessmentJudgement entity) {
        if (entity == null) {
            return null;
        }
        return new AssessmentJudgementResult(
                entity.getId(),
                entity.getAnswerId(),
                entity.getQuestionId(),
                entity.getAssessmentTimeId(),
                entity.getUserId(),
                entity.getScore(),
                entity.getMaxScore(),
                entity.getStatus(),
                entity.getResultCode(),
                entity.getSource(),
                entity.getReviewerId(),
                entity.getReviewerType(),
                entity.getJudgedAt());
    }

    private AssessmentDecisionResult toDecisionResult(AssessmentDecision decision) {
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

}
