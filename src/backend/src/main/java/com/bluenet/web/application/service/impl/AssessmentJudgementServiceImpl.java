package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.assessment_judgement.AssessmentCandidateScoreboardDTO;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentDecisionCandidateDTO;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentDecisionDTO;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentDecisionRequestDTO;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentDecisionStatisticsDTO;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentDecisionWorkspaceDTO;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentJudgementDTO;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentQuestionScoreboardDTO;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentQuestionSubmissionDTO;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentQuestionSubmissionHistoryDTO;
import com.bluenet.web.api.dto.assessment_judgement.ManualReviewRequestDTO;
import com.bluenet.web.application.converter.AssessmentJudgementConverter;
import com.bluenet.web.application.service.assessment.AssessmentDecisionNotificationTemplate;
import com.bluenet.web.application.service.assessment.AssessmentJudgementAccessGuard;
import com.bluenet.web.application.service.AssessmentJudgementService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.model.enumerate.JudgementSource;
import com.bluenet.web.domain.model.enumerate.JudgementStatus;
import com.bluenet.web.domain.model.enumerate.QuestionType;
import com.bluenet.web.domain.model.enumerate.ReviewerType;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.model.vo.AssessmentAnswerVO;
import com.bluenet.web.domain.model.vo.AssessmentCandidateScoreRowVO;
import com.bluenet.web.domain.model.vo.AssessmentDecisionVO;
import com.bluenet.web.domain.model.vo.AssessmentJudgementVO;
import com.bluenet.web.domain.model.vo.AssessmentQuestionVO;
import com.bluenet.web.domain.model.vo.AssessmentTimeVO;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.repository.AssessmentDecisionRepository;
import com.bluenet.web.domain.repository.AssessmentJudgementRepository;
import com.bluenet.web.domain.service.AssessmentAnswerDomainService;
import com.bluenet.web.domain.service.AssessmentDecisionDomainService;
import com.bluenet.web.domain.service.AssessmentJudgementDomainService;
import com.bluenet.web.domain.service.AssessmentQuestionDomainService;
import com.bluenet.web.domain.service.AssessmentTimeDomainService;
import com.bluenet.web.domain.service.UserDomainService;
import com.bluenet.web.application.message.MessageDispatcher;
import com.bluenet.web.domain.model.enumerate.MessageChannel;
import com.bluenet.web.application.message.MessageRequest;
import com.bluenet.web.domain.model.policy.RoleHierarchy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
 */
@Service
@Slf4j
public class AssessmentJudgementServiceImpl implements AssessmentJudgementService {
    private final AssessmentJudgementDomainService assessmentJudgementDomainService;
    private final AssessmentDecisionDomainService assessmentDecisionDomainService;
    private final AssessmentAnswerDomainService assessmentAnswerDomainService;
    private final AssessmentQuestionDomainService assessmentQuestionDomainService;
    private final AssessmentTimeDomainService assessmentTimeDomainService;
    private final AssessmentJudgementRepository assessmentJudgementRepository;
    private final AssessmentDecisionRepository assessmentDecisionRepository;
    private final UserDomainService userDomainService;
    private final MessageDispatcher messageDispatcher;
    private final AssessmentJudgementAccessGuard accessGuard;
    private final AssessmentDecisionNotificationTemplate notificationTemplate;
    private final AssessmentJudgementConverter assessmentJudgementConverter;

    /**
     * 保留原应用服务依赖入口，内部组合访问 guard 和通知模板，降低调用方和单测构造成本。
     */
    public AssessmentJudgementServiceImpl(
            AssessmentJudgementDomainService assessmentJudgementDomainService,
            AssessmentDecisionDomainService assessmentDecisionDomainService,
            AssessmentAnswerDomainService assessmentAnswerDomainService,
            AssessmentQuestionDomainService assessmentQuestionDomainService,
            AssessmentTimeDomainService assessmentTimeDomainService,
            AssessmentJudgementRepository assessmentJudgementRepository,
            AssessmentDecisionRepository assessmentDecisionRepository,
            UserDomainService userDomainService,
            MessageDispatcher messageDispatcher,
            AssessmentJudgementConverter assessmentJudgementConverter) {
        this.assessmentJudgementDomainService = assessmentJudgementDomainService;
        this.assessmentDecisionDomainService = assessmentDecisionDomainService;
        this.assessmentAnswerDomainService = assessmentAnswerDomainService;
        this.assessmentQuestionDomainService = assessmentQuestionDomainService;
        this.assessmentTimeDomainService = assessmentTimeDomainService;
        this.assessmentJudgementRepository = assessmentJudgementRepository;
        this.assessmentDecisionRepository = assessmentDecisionRepository;
        this.userDomainService = userDomainService;
        this.messageDispatcher = messageDispatcher;
        this.accessGuard = new AssessmentJudgementAccessGuard(assessmentTimeDomainService);
        this.notificationTemplate = new AssessmentDecisionNotificationTemplate();
        this.assessmentJudgementConverter = assessmentJudgementConverter;
    }

    /**
     * 获取指定答案的最新评判结果。
     *
     * @param answerId
     *            答案ID
     * @return 最新评判 DTO，无记录时返回 null
     */
    @Override
    public AssessmentJudgementDTO getLatestByAnswerId(Long answerId) {
        return assessmentJudgementConverter
                .convertToDTO(assessmentJudgementDomainService.getLatestByAnswerId(answerId));
    }

    /**
     * 获取指定题目的全部评判结果。
     *
     * @param questionId
     *            题目ID
     * @return 评判 DTO 列表
     */
    @Override
    public List<AssessmentJudgementDTO> listByQuestionId(Long questionId) {
        return assessmentJudgementDomainService.listByQuestionId(questionId)
                .stream()
                .map(assessmentJudgementConverter::convertToDTO)
                .toList();
    }

    /**
     * 对文件上传题执行人工评分，并记录评分人身份。
     *
     * @param request
     *            人工评分请求，包含答案ID、分数和可选评论
     * @return 新创建的评判 DTO
     */
    @Override
    @Transactional
    public AssessmentJudgementDTO reviewFileUploadAnswer(ManualReviewRequestDTO request) {
        UserVO currentUser = accessGuard.requireCurrentUser();
        RoleType roleType = accessGuard.requireRole(currentUser);
        if (!RoleHierarchy.isMemberOrAbove(roleType)) {
            throw new Forbidden("只有团队成员及以上权限可以人工评分");
        }

        AssessmentAnswerVO answer = assessmentAnswerDomainService.getAnswerById(request.getAnswerId());
        AssessmentQuestionVO question = assessmentQuestionDomainService.getQuestionById(answer.getQuestionId());
        if (question.getQuestionType() != QuestionType.FILE_UPLOAD) {
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
        return assessmentJudgementConverter.convertToDTO(assessmentJudgementDomainService.createJudgement(judgement));
    }

    /**
     * 保存考生在某轮考核中的通过或淘汰决策。
     *
     * @param request
     *            决策请求，包含考生ID、考核时间ID、是否通过和可选备注
     * @return 保存后的决策 DTO
     */
    @Override
    @Transactional
    public AssessmentDecisionDTO decideAssessment(AssessmentDecisionRequestDTO request) {
        UserVO currentUser = accessGuard.requireCurrentUser();
        RoleType roleType = accessGuard.requireRole(currentUser);
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
        return assessmentJudgementConverter.convertToDTO(assessmentDecisionDomainService.saveDecision(decision));
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
    public List<AssessmentQuestionScoreboardDTO> listQuestionScoreboard(
            Long assessmentTimeId,
            QuestionType questionType,
            String keyword) {
        accessGuard.requireMemberScope(assessmentTimeId);
        return assessmentJudgementRepository
                .findQuestionScoreboard(assessmentTimeId, questionType, normalizeKeyword(keyword))
                .stream()
                .map(assessmentJudgementConverter::convertScoreboardToDTO)
                .toList();
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
    public List<AssessmentQuestionSubmissionDTO> listQuestionSubmissions(Long questionId, String keyword,
            String status) {
        AssessmentQuestionVO question = assessmentQuestionDomainService.getQuestionById(questionId);
        accessGuard.requireMemberScope(question.getAssessmentTimeId());
        List<AssessmentQuestionSubmissionDTO> submissions = assessmentJudgementRepository
                .findQuestionSubmissions(questionId, normalizeKeyword(keyword), validateJudgementStatus(status))
                .stream()
                .map(assessmentJudgementConverter::convertSubmissionToDTO)
                .toList();
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
    public List<AssessmentCandidateScoreboardDTO> listCandidateScoreboard(Long assessmentTimeId, String keyword) {
        accessGuard.requireMemberScope(assessmentTimeId);
        List<AssessmentCandidateScoreRowVO> rows = assessmentJudgementRepository
                .findCandidateScoreRows(assessmentTimeId, normalizeKeyword(keyword));
        return assessmentJudgementConverter.buildCandidateScoreboards(assessmentTimeId, rows);
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
    public AssessmentDecisionWorkspaceDTO getDecisionWorkspace(
            Long assessmentTimeId,
            String keyword,
            String decisionStatus) {
        accessGuard.requireDecisionScope(assessmentTimeId);
        List<AssessmentCandidateScoreboardDTO> scoreboards = listCandidateScoreboard(assessmentTimeId, keyword);
        Map<Long, AssessmentDecisionVO> decisions = assessmentDecisionRepository
                .findByAssessmentTimeId(assessmentTimeId)
                .stream()
                .collect(Collectors.toMap(AssessmentDecisionVO::getUserId, Function.identity()));

        List<AssessmentDecisionCandidateDTO> candidates = scoreboards.stream()
                .map(
                        scoreboard -> assessmentJudgementConverter.convertDecisionCandidate(
                                scoreboard,
                                decisions.get(scoreboard.getCandidateUserId())))
                .filter(candidate -> matchesDecisionStatus(candidate, decisionStatus))
                .sorted(
                        Comparator.comparing(
                                AssessmentDecisionCandidateDTO::getStudentId,
                                Comparator.nullsLast(String::compareTo)))
                .toList();

        AssessmentDecisionStatisticsDTO statistics = assessmentJudgementConverter
                .calculateDecisionStatistics(scoreboards, decisions);
        return AssessmentDecisionWorkspaceDTO.builder()
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
        AssessmentTimeVO assessmentTime = assessmentTimeDomainService.getById(assessmentTimeId)
                .orElseThrow(() -> new DataNotFound("考核时间不存在，ID: " + assessmentTimeId));
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
                    : "";
            int epoch = assessmentTime.getEpoch() != null ? assessmentTime.getEpoch() : 0;
            String resultText = Boolean.TRUE.equals(decision.getPassed()) ? "通过" : "未通过";
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
     *            需要附加历史的提交 DTO 列表
     */
    private void attachSubmissionHistories(Long questionId, List<AssessmentQuestionSubmissionDTO> submissions) {
        if (submissions.isEmpty()) {
            return;
        }
        List<Long> userIds = submissions.stream()
                .map(AssessmentQuestionSubmissionDTO::getCandidateUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (userIds.isEmpty()) {
            return;
        }
        Map<Long, List<AssessmentQuestionSubmissionHistoryDTO>> historiesByUser = assessmentJudgementRepository
                .findQuestionSubmissionHistories(questionId, userIds)
                .stream()
                .collect(
                        Collectors.groupingBy(
                                history -> history.getJudgement() != null ? history.getJudgement().getUserId() : null,
                                LinkedHashMap::new,
                                Collectors.mapping(
                                        assessmentJudgementConverter::convertSubmissionHistoryToDTO,
                                        Collectors.toList())));
        submissions.forEach(
                submission -> submission.setHistories(
                        historiesByUser.getOrDefault(submission.getCandidateUserId(), List.of())));
    }

    /**
     * 判断候选人是否匹配指定的决策状态筛选条件。
     *
     * @param candidate
     *            候选人 DTO
     * @param decisionStatus
     *            决策状态筛选值
     * @return 是否匹配
     */
    private boolean matchesDecisionStatus(AssessmentDecisionCandidateDTO candidate, String decisionStatus) {
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

}
