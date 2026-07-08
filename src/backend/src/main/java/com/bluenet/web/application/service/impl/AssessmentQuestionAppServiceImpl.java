package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.AssessmentQuestionResult;
import com.bluenet.web.application.UserQuestionListResult;
import com.bluenet.web.application.command.assessment_question.AssessmentQuestionCommands;
import com.bluenet.web.application.service.AssessmentQuestionAppService;
import com.bluenet.web.application.service.AssessmentSessionAppService;
import com.bluenet.web.application.command.assessment_session.AssessmentSessionCommands;
import com.bluenet.web.application.AssessmentSessionResult;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataConflict;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.AssessmentQuestion;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.enumerate.QuestionType;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.vo.evaluation.AlgorithmContent;
import com.bluenet.web.domain.repository.AssessmentAnswerRepository;
import com.bluenet.web.domain.repository.AssessmentQuestionRepository;
import com.bluenet.web.domain.repository.AssessmentTimeRepository;
import com.bluenet.web.domain.repository.FileRepository;
import com.bluenet.web.domain.service.AssessmentDecisionDomainService;
import com.bluenet.web.domain.util.GradeCalculator;
import com.bluenet.web.infrastructure.repository.mapper.JudgeProblemConfigMapper;
import com.bluenet.web.infrastructure.security.principal.RoleTypeResolver;
import com.bluenet.web.infrastructure.repository.dataobject.JudgeProblemConfigDO;
import com.bluenet.web.infrastructure.storage.JudgeAssetStorage;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 考核题目应用服务实现。
 * <p>实现考核题目聚合在应用层的业务逻辑编排。</p>
 */
/**
 * 评测题目应用服务实现。
 * <p>
 * 实现评测题目聚合在应用层的业务逻辑编排。
 * </p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AssessmentQuestionAppServiceImpl implements AssessmentQuestionAppService {

    private final AssessmentQuestionRepository assessmentQuestionRepository;
    private final AssessmentTimeRepository assessmentTimeRepository;
    private final AssessmentSessionAppService assessmentSessionAppService;
    private final AssessmentAnswerRepository assessmentAnswerRepository;
    private final RoleTypeResolver roleTypeResolver;
    private final FileRepository fileRepository;
    private final JudgeProblemConfigMapper judgeProblemConfigMapper;
    private final JudgeAssetStorage judgeAssetStorage;
    private final AssessmentDecisionDomainService assessmentDecisionDomainService;

    /**
     * 创建考题。
     *
     * @param command
     *            创建考题命令
     * @return 创建后的考题结果
     */
    /**
     * 创建评测题目。
     *
     * @param command
     *            创建评测题目命令
     * @return 创建的评测题目结果
     */
    @Override
    @Transactional
    public AssessmentQuestionResult createQuestion(AssessmentQuestionCommands.CreateAssessmentQuestionCommand command) {
        AssessmentTime time = assessmentTimeRepository.findById(command.assessmentTimeId())
                .orElseThrow(() -> new IllegalArgumentException("考核时间不存在"));

        validateQuestionContent(command.questionType(), command.content());

        assessmentQuestionRepository.findByTimeIdAndQuestionNo(command.assessmentTimeId(), command.questionNo())
                .ifPresent(q -> {
                    throw new DataConflict("该考核时间下题号 " + command.questionNo() + " 已存在");
                });

        AssessmentQuestion entity = AssessmentQuestion.create(
                command.assessmentTimeId(),
                command.questionNo(),
                command.questionType(),
                command.title(),
                command.content(),
                command.attachmentId(),
                command.score());

        assessmentQuestionRepository.save(entity);
        return toResult(entity, null);
    }

    /**
     * 更新考题。
     *
     * @param command
     *            更新考题命令
     * @return 更新后的考题结果
     */
    /**
     * 更新评测题目。
     *
     * @param command
     *            更新评测题目命令
     * @return 更新后的评测题目结果
     */
    @Override
    @Transactional
    public AssessmentQuestionResult updateQuestion(AssessmentQuestionCommands.UpdateAssessmentQuestionCommand command) {
        AssessmentQuestion existing = assessmentQuestionRepository.findById(command.id())
                .orElseThrow(() -> new DataNotFound("考题不存在，ID: " + command.id()));

        QuestionType questionType = command.questionType() != null
                ? command.questionType()
                : existing.getQuestionType();
        validateQuestionContent(questionType, command.content());

        if (command.questionNo() != null) {
            assessmentQuestionRepository.findByTimeIdAndQuestionNo(existing.getAssessmentTimeId(), command.questionNo())
                    .ifPresent(q -> {
                        if (!q.getId().equals(command.id())) {
                            throw new DataConflict("该考核时间下题号 " + command.questionNo() + " 已存在");
                        }
                    });
        }

        existing.update(
                command.questionNo(),
                command.questionType(),
                command.title(),
                command.content(),
                command.attachmentId(),
                command.score());

        assessmentQuestionRepository.save(existing);
        return toResult(existing, null);
    }

    /**
     * 删除考题。
     *
     * @param id
     *            考题ID
     */
    /**
     * 删除评测题目。
     *
     * @param id
     *            题目ID
     */
    @Override
    @Transactional
    public void deleteQuestion(Long id) {
        AssessmentQuestion existing = assessmentQuestionRepository.findById(id)
                .orElseThrow(() -> new DataNotFound("考题不存在，ID: " + id));

        if (existing.getQuestionType() == QuestionType.ALGORITHM) {
            JudgeProblemConfigDO config = judgeProblemConfigMapper.selectByQuestionId(id);
            if (config != null) {
                deleteJudgeAssets(config);
                judgeProblemConfigMapper.deleteByQuestionId(id);
                log.info("delete judge problem config for questionId={}", id);
            }
        }

        assessmentQuestionRepository.deleteById(id);
        log.info("delete question success id {}", id);
    }

    private void deleteJudgeAssets(JudgeProblemConfigDO config) {
        Long questionId = config.getQuestionId();

        try {
            judgeAssetStorage.deleteByPrefix("questions/" + questionId + "/current/generator/");
        } catch (Exception ex) {
            log.warn("删除 generator OSS 对象失败，questionId={}", questionId, ex);
        }

        try {
            judgeAssetStorage.deleteByPrefix("questions/" + questionId + "/current/manifest/");
        } catch (Exception ex) {
            log.warn("删除 manifest OSS 对象失败，questionId={}", questionId, ex);
        }

        try {
            judgeAssetStorage.deleteByPrefix("questions/" + questionId + "/current/standard/");
        } catch (Exception ex) {
            log.warn("删除标准解 OSS 对象失败，questionId={}", questionId, ex);
        }

        try {
            judgeAssetStorage.deleteByPrefix("questions/" + questionId + "/current/testcases/");
        } catch (Exception ex) {
            log.warn("删除测试数据 OSS 对象失败，questionId={}", questionId, ex);
        }
    }

    /**
     * 分页查询考题列表（管理员）。
     *
     * @param assessmentTimeId
     *            考核时间ID
     * @param page
     *            页码
     * @param size
     *            每页大小
     * @return 考题分页结果
     */
    /**
     * 管理员分页查询评测题目列表。
     *
     * @param assessmentTimeId
     *            评测场次ID
     * @param page
     *            页码
     * @param size
     *            每页大小
     * @return 评测题目分页结果
     */
    @Override
    public Page<AssessmentQuestionResult> listQuestionsForAdmin(Long assessmentTimeId, Integer page, Integer size) {
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 10;

        Page<AssessmentQuestion> entityPage = assessmentQuestionRepository.findAllByTimeId(
                assessmentTimeId,
                PageRequest.of(pageNum, pageSize));
        return entityPage.map(q -> toResult(q, null));
    }

    /**
     * 分页查询考题列表（用户）。
     *
     * @param assessmentTimeId
     *            考核时间ID
     * @param page
     *            页码
     * @param size
     *            每页大小
     * @return 用户考题列表结果
     */
    /**
     * 用户查询评测题目列表。
     *
     * @param assessmentTimeId
     *            评测场次ID
     * @param page
     *            页码
     * @param size
     *            每页大小
     * @return 用户题目列表结果
     */
    @Override
    public UserQuestionListResult listQuestionsForUser(Long assessmentTimeId, Integer page, Integer size) {
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 10;

        AssessmentTime time = assessmentTimeRepository.findById(assessmentTimeId)
                .orElseThrow(() -> new IllegalArgumentException("考核时间不存在"));

        User currentUser = UserCTX.getCurrentUser();
        if (currentUser != null) {
            RoleType roleType = roleTypeResolver.resolve(currentUser.getRoleId());
            if (roleType == RoleType.CANDIDATE) {
                if (time.getDirection() != null && !currentUser.getDirection().equals(time.getDirection())) {
                    throw new SecurityException("无权查看该考核的题目");
                }
                Integer userEnrollmentYear = GradeCalculator.resolveAssessmentYear(
                        currentUser.getStudentId(),
                        currentUser.getAssessmentGradeYear());
                if (time.getGrade() != null
                        && (userEnrollmentYear == null || !userEnrollmentYear.equals(time.getGrade()))) {
                    throw new SecurityException("无权查看该考核的题目");
                }
                LocalDateTime now = LocalDateTime.now();
                if (time.getStartTime() != null && now.isBefore(time.getStartTime())) {
                    throw new SecurityException("考核尚未开始");
                }
                if (assessmentDecisionDomainService.isEliminatedFromPriorEpoch(currentUser.getId(), time)) {
                    throw new SecurityException("已在该方向考核中被淘汰，无法查看后续轮次题目");
                }
            }
        }

        Page<AssessmentQuestion> entityPage = assessmentQuestionRepository.findAllByTimeId(
                assessmentTimeId,
                PageRequest.of(pageNum, pageSize));

        Page<AssessmentQuestionResult> resultPage = entityPage.map(entity -> {
            Boolean answered = null;
            if (currentUser != null) {
                answered = assessmentAnswerRepository
                        .existsByUserIdAndQuestionId(currentUser.getId(), entity.getId());
            }
            return toResultForUser(entity, answered);
        });

        String deadline = null;
        if (Boolean.TRUE.equals(time.getTimeLimit()) && currentUser != null) {
            AssessmentSessionResult session = assessmentSessionAppService
                    .getOrCreateSession(
                            new AssessmentSessionCommands.GetOrCreateSessionCommand(currentUser.getId(),
                                    assessmentTimeId));
            if (session != null && session.deadline() != null) {
                deadline = session.deadline().toString();
            }
        }

        boolean ended = time.getEndTime() != null && LocalDateTime.now().isAfter(time.getEndTime());
        return new UserQuestionListResult(resultPage, deadline, ended);
    }

    /**
     * 根据ID查询考题详情（用户）。
     *
     * @param id
     *            考题ID
     * @return 考题详情结果
     */
    /**
     * 用户查询题目详情。
     *
     * @param id
     *            题目ID
     * @return 评测题目详情结果
     */
    @Override
    public AssessmentQuestionResult getQuestionDetailForUser(Long id) {
        AssessmentQuestion entity = assessmentQuestionRepository.findById(id)
                .orElseThrow(() -> new DataNotFound("考题不存在，ID: " + id));

        User currentUser = UserCTX.getCurrentUser();

        if (currentUser != null) {
            RoleType roleType = roleTypeResolver.resolve(currentUser.getRoleId());
            if (roleType == RoleType.CANDIDATE) {
                AssessmentTime time = assessmentTimeRepository.findById(entity.getAssessmentTimeId())
                        .orElseThrow(() -> new IllegalArgumentException("考核时间不存在"));
                if (time.getDirection() != null && !currentUser.getDirection().equals(time.getDirection())) {
                    throw new SecurityException("无权查看该题目");
                }
                Integer userEnrollmentYear = GradeCalculator.resolveAssessmentYear(
                        currentUser.getStudentId(),
                        currentUser.getAssessmentGradeYear());
                if (time.getGrade() != null
                        && (userEnrollmentYear == null || !userEnrollmentYear.equals(time.getGrade()))) {
                    throw new SecurityException("无权查看该题目");
                }
                LocalDateTime now = LocalDateTime.now();
                if (time.getStartTime() != null && now.isBefore(time.getStartTime())) {
                    throw new SecurityException("考核尚未开始");
                }
                if (assessmentDecisionDomainService.isEliminatedFromPriorEpoch(currentUser.getId(), time)) {
                    throw new SecurityException("已在该方向考核中被淘汰，无法查看后续轮次题目");
                }
            }
        }

        AssessmentQuestion sanitized = sanitizeQuestionForUser(entity);
        return toResult(sanitized, null);
    }

    /**
     * 更新题目附件。
     *
     * @param questionId
     *            题目ID
     * @param fileId
     *            文件ID
     */
    /**
     * 更新题目附件。
     *
     * @param questionId
     *            题目ID
     * @param fileId
     *            附件文件ID
     */
    @Override
    @Transactional
    public void updateAttachment(Long questionId, Long fileId) {
        AssessmentQuestion question = assessmentQuestionRepository.findById(questionId)
                .orElseThrow(() -> new DataNotFound("考题不存在，ID: " + questionId));

        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new DataNotFound("文件不存在"));
        if (file.getType() != FileType.ASSESSMENT_ATTACHMENT) {
            throw new BadRequest("文件类型不匹配，期望 ASSESSMENT_ATTACHMENT");
        }

        assessmentQuestionRepository.updateAttachmentId(questionId, fileId);
        log.info("题目附件更新成功 - questionId={}, fileId={}", questionId, fileId);
    }

    private void validateQuestionContent(QuestionType questionType,
            com.bluenet.web.domain.model.vo.evaluation.QuestionContent content) {
        if (questionType != QuestionType.ALGORITHM) {
            return;
        }
        if (!(content instanceof AlgorithmContent algorithmContent)) {
            throw new IllegalArgumentException("算法题内容不能为空");
        }
        validateStarterCode(algorithmContent.getStarterCode());
        // 正式测试用例已迁移到独立判题配置，由 generator 生成并保存在判题 OSS 中。
        validateTestCases(algorithmContent.getTestCases(), false, "正式测试用例");
        validateTestCases(algorithmContent.getRunTestCases(), false, "默认运行测试用例");
    }

    private void validateStarterCode(Map<String, String> starterCode) {
        if (starterCode == null || starterCode.isEmpty()) {
            throw new IllegalArgumentException("算法题至少需要配置一个语言模板");
        }
        boolean hasUsableTemplate = starterCode.entrySet()
                .stream()
                .anyMatch(entry -> hasText(entry.getKey()) && hasText(entry.getValue()));
        if (!hasUsableTemplate) {
            throw new IllegalArgumentException("算法题至少需要配置一个有效语言模板");
        }
    }

    private void validateTestCases(List<AlgorithmContent.TestCase> testCases, boolean required, String label) {
        if (testCases == null || testCases.isEmpty()) {
            if (required) {
                throw new IllegalArgumentException("算法题至少需要配置一个" + label);
            }
            return;
        }
        for (int i = 0; i < testCases.size(); i++) {
            AlgorithmContent.TestCase testCase = testCases.get(i);
            if (testCase == null || !hasText(testCase.getInput()) || !hasText(testCase.getExpectedOutput())) {
                throw new IllegalArgumentException(label + "第" + (i + 1) + "个用例必须包含输入和期望输出");
            }
        }
    }

    private AssessmentQuestion sanitizeQuestionForUser(AssessmentQuestion question) {
        if (question.getContent() == null) {
            return question;
        }
        question.getContent().sanitizeForUser();
        return question;
    }

    private AssessmentQuestionResult toResult(AssessmentQuestion entity, Boolean answered) {
        return new AssessmentQuestionResult(
                entity.getId(),
                entity.getAssessmentTimeId(),
                entity.getQuestionNo(),
                entity.getQuestionType(),
                entity.getTitle(),
                entity.getContent(),
                entity.getAttachmentId(),
                entity.getScore(),
                answered);
    }

    private AssessmentQuestionResult toResultForUser(AssessmentQuestion entity, Boolean answered) {
        return new AssessmentQuestionResult(
                entity.getId(),
                entity.getAssessmentTimeId(),
                entity.getQuestionNo(),
                entity.getQuestionType(),
                entity.getTitle(),
                null,
                entity.getAttachmentId(),
                entity.getScore(),
                answered);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
