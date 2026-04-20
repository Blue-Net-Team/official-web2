package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.assessment_judgement.AssessmentCandidateQuestionScoreDTO;
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
import com.bluenet.web.domain.model.vo.AssessmentQuestionScoreboardVO;
import com.bluenet.web.domain.model.vo.AssessmentQuestionSubmissionHistoryVO;
import com.bluenet.web.domain.model.vo.AssessmentQuestionSubmissionVO;
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
import com.bluenet.web.infrastructure.email.EmailSender;
import com.bluenet.web.infrastructure.security.util.RoleHierarchy;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import lombok.RequiredArgsConstructor;
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
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AssessmentJudgementServiceImpl implements AssessmentJudgementService {
    private final AssessmentJudgementDomainService assessmentJudgementDomainService;
    private final AssessmentDecisionDomainService assessmentDecisionDomainService;
    private final AssessmentAnswerDomainService assessmentAnswerDomainService;
    private final AssessmentQuestionDomainService assessmentQuestionDomainService;
    private final AssessmentTimeDomainService assessmentTimeDomainService;
    private final AssessmentJudgementRepository assessmentJudgementRepository;
    private final AssessmentDecisionRepository assessmentDecisionRepository;
    private final UserDomainService userDomainService;
    private final EmailSender emailSender;

    /**
     * 获取指定答案的最新评判结果。
     *
     * @param answerId
     *            答案ID
     * @return 最新评判 DTO，无记录时返回 null
     */
    @Override
    public AssessmentJudgementDTO getLatestByAnswerId(Long answerId) {
        return convertToDTO(assessmentJudgementDomainService.getLatestByAnswerId(answerId));
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
                .map(this::convertToDTO)
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
        UserVO currentUser = requireCurrentUser();
        RoleType roleType = requireRole(currentUser);
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
        return convertToDTO(assessmentJudgementDomainService.createJudgement(judgement));
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
        requireMemberScope(assessmentTimeId);
        return assessmentJudgementRepository
                .findQuestionScoreboard(assessmentTimeId, questionType, normalizeKeyword(keyword))
                .stream()
                .map(this::convertScoreboardToDTO)
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
        requireMemberScope(question.getAssessmentTimeId());
        List<AssessmentQuestionSubmissionDTO> submissions = assessmentJudgementRepository
                .findQuestionSubmissions(questionId, normalizeKeyword(keyword), validateJudgementStatus(status))
                .stream()
                .map(this::convertSubmissionToDTO)
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
        requireMemberScope(assessmentTimeId);
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
    public AssessmentDecisionWorkspaceDTO getDecisionWorkspace(
            Long assessmentTimeId,
            String keyword,
            String decisionStatus) {
        requireDecisionScope(assessmentTimeId);
        List<AssessmentCandidateScoreboardDTO> scoreboards = listCandidateScoreboard(assessmentTimeId, keyword);
        Map<Long, AssessmentDecisionVO> decisions = assessmentDecisionRepository
                .findByAssessmentTimeId(assessmentTimeId)
                .stream()
                .collect(Collectors.toMap(AssessmentDecisionVO::getUserId, Function.identity()));

        List<AssessmentDecisionCandidateDTO> candidates = scoreboards.stream()
                .map(scoreboard -> convertDecisionCandidate(scoreboard, decisions.get(scoreboard.getCandidateUserId())))
                .filter(candidate -> matchesDecisionStatus(candidate, decisionStatus))
                .sorted(
                        Comparator.comparing(
                                AssessmentDecisionCandidateDTO::getStudentId,
                                Comparator.nullsLast(String::compareTo)))
                .toList();

        AssessmentDecisionStatisticsDTO statistics = calculateDecisionStatistics(scoreboards, decisions);
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
        requireDecisionScope(assessmentTimeId);
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
            String htmlContent = buildPublishEmailHtml(nickname, directionLabel, epoch, resultText);
            try {
                emailSender.sendHtmlAsync(user.getEmail(), subject, htmlContent);
                sentCount++;
            } catch (Exception e) {
                log.error("发送决策邮件失败：userId={}, email={}", decision.getUserId(), user.getEmail(), e);
            }
        }
        return sentCount;
    }

    /**
     * 构建决策通知 HTML 邮件内容。
     *
     * @param nickname
     *            考生昵称
     * @param directionLabel
     *            考核方向名称
     * @param epoch
     *            轮次
     * @param resultText
     *            结果文本
     * @return HTML 邮件内容
     */
    private String buildPublishEmailHtml(String nickname, String directionLabel, int epoch, String resultText) {
        return """
                <div style="font-family: 'Microsoft YaHei', sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                  <h2 style="color: #1890ff;">考核结果通知</h2>
                  <p>%s 你好，</p>
                  <p>你参加的 <strong>%s方向第%d轮</strong> 考核结果已公布：</p>
                  <p style="font-size: 18px; font-weight: bold; color: %s;">%s</p>
                  <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                  <p style="color: #999; font-size: 12px;">此邮件由系统自动发送，请勿回复。</p>
                </div>
                """
                .formatted(
                        nickname,
                        directionLabel,
                        epoch,
                        "通过".equals(resultText) ? "#52c41a" : "#ff4d4f",
                        resultText);
    }

    // ========== 权限与校验 ==========

    /**
     * 获取当前登录用户，未登录时抛出安全异常。
     *
     * @return 当前登录用户 VO
     */
    private UserVO requireCurrentUser() {
        UserVO currentUser = UserCTX.getCurrentUser();
        if (currentUser == null) {
            throw new SecurityException("未登录");
        }
        return currentUser;
    }

    /**
     * 解析用户角色类型，角色无效时抛出权限异常。
     *
     * @param user
     *            用户 VO
     * @return 角色类型枚举
     */
    private RoleType requireRole(UserVO user) {
        RoleType roleType = RoleType.fromName(user.getRoleName());
        if (roleType == null) {
            throw new Forbidden("当前用户角色无效");
        }
        return roleType;
    }

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

    /**
     * 校验当前用户是否为团队成员及以上，并检查考核时间方向归属。
     *
     * @param assessmentTimeId
     *            考核时间ID
     */
    private void requireMemberScope(Long assessmentTimeId) {
        UserVO currentUser = requireCurrentUser();
        RoleType roleType = requireRole(currentUser);
        if (!RoleHierarchy.isMemberOrAbove(roleType)) {
            throw new Forbidden("只有团队成员及以上权限可以查看考核评判");
        }
        assertAssessmentTimeScope(assessmentTimeId, currentUser, roleType);
    }

    /**
     * 校验当前用户是否为方向管理员及以上，并检查考核时间方向归属。
     *
     * @param assessmentTimeId
     *            考核时间ID
     */
    private void requireDecisionScope(Long assessmentTimeId) {
        UserVO currentUser = requireCurrentUser();
        RoleType roleType = requireRole(currentUser);
        if (!RoleHierarchy.isDirectionAdminOrAbove(roleType)) {
            throw new Forbidden("只有方向管理员及以上权限可以查看录用决策");
        }
        assertAssessmentTimeScope(assessmentTimeId, currentUser, roleType);
    }

    /**
     * 校验方向管理员只能访问本方向的考核时间，超管无此限制。
     *
     * @param assessmentTimeId
     *            考核时间ID
     * @param currentUser
     *            当前用户
     * @param roleType
     *            当前用户角色类型
     */
    private void assertAssessmentTimeScope(Long assessmentTimeId, UserVO currentUser, RoleType roleType) {
        AssessmentTimeVO assessmentTime = assessmentTimeDomainService.getById(assessmentTimeId)
                .orElseThrow(() -> new DataNotFound("考核时间不存在，ID: " + assessmentTimeId));
        if (roleType == RoleType.DIRECTION_ADMIN
                && currentUser.getDirection() != null
                && !Objects.equals(currentUser.getDirection(), assessmentTime.getDirection())) {
            throw new Forbidden("不能访问其他方向的考核评判数据");
        }
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

    // ========== VO → DTO 转换 ==========

    /**
     * 将题目评分汇总 VO 转换为接口 DTO。
     *
     * @param vo
     *            题目评分汇总 VO
     * @return 对应的接口 DTO
     */
    private AssessmentQuestionScoreboardDTO convertScoreboardToDTO(AssessmentQuestionScoreboardVO vo) {
        return AssessmentQuestionScoreboardDTO.builder()
                .questionId(vo.getQuestionId())
                .assessmentTimeId(vo.getAssessmentTimeId())
                .questionNo(vo.getQuestionNo())
                .questionType(vo.getQuestionType())
                .title(vo.getTitle())
                .maxScore(vo.getMaxScore())
                .submittedCount(vo.getSubmittedCount())
                .judgedCount(vo.getJudgedCount())
                .pendingCount(vo.getPendingCount())
                .averageScore(vo.getAverageScore())
                .build();
    }

    /**
     * 将题目提交 VO 转换为接口 DTO，内嵌最新评判信息。
     *
     * @param vo
     *            题目提交 VO
     * @return 对应的接口 DTO
     */
    private AssessmentQuestionSubmissionDTO convertSubmissionToDTO(AssessmentQuestionSubmissionVO vo) {
        return AssessmentQuestionSubmissionDTO.builder()
                .answerId(vo.getAnswerId())
                .questionId(vo.getQuestionId())
                .assessmentTimeId(vo.getAssessmentTimeId())
                .questionNo(vo.getQuestionNo())
                .questionTitle(vo.getQuestionTitle())
                .questionType(vo.getQuestionType())
                .maxScore(vo.getMaxScore())
                .candidateUserId(vo.getCandidateUserId())
                .studentId(vo.getStudentId())
                .username(vo.getUsername())
                .nickname(vo.getNickname())
                .fileId(vo.getFileId())
                .content(vo.getContent())
                .language(vo.getLanguage())
                .submitTime(vo.getSubmitTime())
                .latestJudgement(convertJudgementFromSubmission(vo))
                .build();
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
                                Collectors.mapping(this::convertSubmissionHistoryToDTO, Collectors.toList())));
        submissions.forEach(
                submission -> submission.setHistories(
                        historiesByUser.getOrDefault(submission.getCandidateUserId(), List.of())));
    }

    /**
     * 将评判历史 VO 转换为接口 DTO。
     *
     * @param vo
     *            评判历史 VO
     * @return 对应的接口 DTO
     */
    private AssessmentQuestionSubmissionHistoryDTO convertSubmissionHistoryToDTO(
            AssessmentQuestionSubmissionHistoryVO vo) {
        return AssessmentQuestionSubmissionHistoryDTO.builder()
                .judgement(convertToDTO(vo.getJudgement()))
                .selectedBest(Boolean.TRUE.equals(vo.getSelectedBest()))
                .build();
    }

    /**
     * 将考生维度扁平行数据按考生聚合，计算总分、已评和待评数量。
     *
     * @param assessmentTimeId
     *            考核时间ID
     * @param rows
     *            考生评分扁平行 VO 列表
     * @return 按考生聚合的评分汇总 DTO 列表
     */
    private List<AssessmentCandidateScoreboardDTO> buildCandidateScoreboards(
            Long assessmentTimeId,
            List<AssessmentCandidateScoreRowVO> rows) {
        Map<Long, List<AssessmentCandidateScoreRowVO>> grouped = rows.stream()
                .collect(
                        Collectors.groupingBy(
                                AssessmentCandidateScoreRowVO::getCandidateUserId,
                                LinkedHashMap::new,
                                Collectors.toList()));
        List<AssessmentCandidateScoreboardDTO> result = new ArrayList<>();
        for (List<AssessmentCandidateScoreRowVO> candidateRows : grouped.values()) {
            AssessmentCandidateScoreRowVO first = candidateRows.get(0);
            List<AssessmentCandidateQuestionScoreDTO> questionScores = candidateRows.stream()
                    .map(row -> convertQuestionScore(assessmentTimeId, row))
                    .toList();
            BigDecimal totalScore = questionScores.stream()
                    .map(AssessmentCandidateQuestionScoreDTO::getScore)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal maxScore = questionScores.stream()
                    .map(AssessmentCandidateQuestionScoreDTO::getMaxScore)
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
                    AssessmentCandidateScoreboardDTO.builder()
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
     * 将考生单题评分行 VO 转换为接口 DTO。
     *
     * @param assessmentTimeId
     *            考核时间ID
     * @param row
     *            考生单题评分行 VO
     * @return 单题评分状态 DTO
     */
    private AssessmentCandidateQuestionScoreDTO convertQuestionScore(
            Long assessmentTimeId,
            AssessmentCandidateScoreRowVO row) {
        boolean submitted = row.getAnswerId() != null;
        boolean judged = row.getJudgementId() != null;
        return AssessmentCandidateQuestionScoreDTO.builder()
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
     * 将评分汇总和决策记录组合为候选人 DTO。
     *
     * @param scoreboard
     *            考生评分汇总 DTO
     * @param decision
     *            已有决策 VO，无决策时为 null
     * @return 候选人 DTO
     */
    private AssessmentDecisionCandidateDTO convertDecisionCandidate(
            AssessmentCandidateScoreboardDTO scoreboard,
            AssessmentDecisionVO decision) {
        return AssessmentDecisionCandidateDTO.builder()
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

    /**
     * 根据评分矩阵和决策记录计算候选人、待决策、通过和淘汰的统计数据。
     *
     * @param scoreboards
     *            考生评分汇总列表
     * @param decisions
     *            按用户ID索引的决策记录映射
     * @return 决策统计 DTO
     */
    private AssessmentDecisionStatisticsDTO calculateDecisionStatistics(
            List<AssessmentCandidateScoreboardDTO> scoreboards,
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
        return AssessmentDecisionStatisticsDTO.builder()
                .candidates(candidates)
                .pending(candidates - passed - eliminated)
                .passed(passed)
                .eliminated(eliminated)
                .build();
    }

    // ========== 评判 VO → DTO 转换 ==========

    /**
     * 将评判 VO 转换为接口 DTO。
     *
     * @param judgement
     *            评判 VO，为 null 时返回 null
     * @return 对应的接口 DTO
     */
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

    /**
     * 将决策 VO 转换为接口 DTO。
     *
     * @param decision
     *            决策 VO
     * @return 对应的接口 DTO
     */
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

    /**
     * 从提交 VO 中提取评判信息转换为 DTO。
     */
    private AssessmentJudgementDTO convertJudgementFromSubmission(AssessmentQuestionSubmissionVO vo) {
        if (vo.getJudgementId() == null) {
            return null;
        }
        return AssessmentJudgementDTO.builder()
                .id(vo.getJudgementId())
                .answerId(vo.getAnswerId())
                .questionId(vo.getQuestionId())
                .assessmentTimeId(vo.getAssessmentTimeId())
                .userId(vo.getCandidateUserId())
                .score(vo.getJudgementScore())
                .maxScore(vo.getJudgementMaxScore())
                .status(vo.getJudgementStatus())
                .resultCode(vo.getResultCode())
                .source(vo.getSource())
                .reviewerId(vo.getReviewerId())
                .reviewerType(vo.getReviewerType())
                .comment(vo.getJudgementComment())
                .judgedAt(vo.getJudgedAt())
                .build();
    }

    /**
     * 从考生评分行 VO 中提取评判信息转换为 DTO。
     */
    private AssessmentJudgementDTO convertJudgementFromScoreRow(Long assessmentTimeId,
            AssessmentCandidateScoreRowVO row) {
        if (row.getJudgementId() == null) {
            return null;
        }
        return AssessmentJudgementDTO.builder()
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
}
