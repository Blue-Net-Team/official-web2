package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.AlgorithmJudgeResult;
import com.bluenet.web.application.command.algorithm_judge.AlgorithmJudgeCommands;
import com.bluenet.web.application.service.AlgorithmJudgeAppService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.exception.GlobalException;
import com.bluenet.web.domain.model.entity.AlgorithmJudgeCaseResult;
import com.bluenet.web.domain.model.entity.AlgorithmJudgeJob;
import com.bluenet.web.domain.model.enumerate.AlgorithmTestcaseType;
import com.bluenet.web.domain.model.enumerate.JudgeCaseStatus;
import com.bluenet.web.domain.model.enumerate.JudgeJobStatus;
import com.bluenet.web.domain.model.enumerate.ProgrammingLanguage;
import com.bluenet.web.domain.model.enumerate.QuestionType;
import com.bluenet.web.domain.model.entity.AssessmentAnswer;
import com.bluenet.web.domain.model.vo.AssessmentJudgementVO;
import com.bluenet.web.domain.model.entity.AssessmentQuestion;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.vo.evaluation.AlgorithmContent;
import com.bluenet.web.domain.repository.AlgorithmJudgeCaseResultRepository;
import com.bluenet.web.domain.repository.AlgorithmJudgeJobRepository;
import com.bluenet.web.domain.repository.AssessmentAnswerRepository;
import com.bluenet.web.domain.repository.AssessmentSessionRepository;
import com.bluenet.web.domain.service.AssessmentJudgementDomainService;
import com.bluenet.web.domain.repository.AssessmentQuestionRepository;
import com.bluenet.web.domain.repository.AssessmentTimeRepository;
import com.bluenet.web.infrastructure.judge.AlgorithmJudgeJobPublisher;
import com.bluenet.web.infrastructure.repository.mapper.JudgeLanguageLimitMapper;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 算法评测应用服务实现。
 * <p>
 * 实现算法评测聚合在应用层的业务逻辑编排。
 * </p>
 */
@Service
@RequiredArgsConstructor
public class AlgorithmJudgeAppServiceImpl implements AlgorithmJudgeAppService {
    private static final int DEFAULT_MAX_RETRY_COUNT = 3;

    private final AssessmentQuestionRepository assessmentQuestionRepository;
    private final AssessmentTimeRepository assessmentTimeRepository;

    private final AssessmentJudgementDomainService assessmentJudgementDomainService;
    private final AssessmentAnswerRepository assessmentAnswerRepository;
    private final AssessmentSessionRepository assessmentSessionRepository;
    private final AlgorithmJudgeJobRepository algorithmJudgeJobRepository;
    private final AlgorithmJudgeCaseResultRepository algorithmJudgeCaseResultRepository;
    private final AlgorithmJudgeJobPublisher algorithmJudgeJobPublisher;
    private final JudgeLanguageLimitMapper judgeLanguageLimitMapper;

    /**
     * 执行算法运行。
     *
     * @param command
     *            运行命令
     * @return 提交结果
     */
    @Override
    @Transactional
    public AlgorithmJudgeResult.SubmitResult run(AlgorithmJudgeCommands.RunCommand command) {
        User currentUser = getCurrentUser();
        AssessmentQuestion question = assessmentQuestionRepository.findById(command.questionId())
                .orElseThrow(() -> new DataNotFound("题目不存在，ID: " + command.questionId()));
        AlgorithmContent content = validateAlgorithmQuestion(question);
        validateCandidateCanUseQuestion(currentUser, question);
        validateLanguage(content, command.language());

        AlgorithmTestcaseType testcaseType = command.testcaseType() == null
                ? AlgorithmTestcaseType.DEFAULT_RUN
                : command.testcaseType();
        if (testcaseType == AlgorithmTestcaseType.FORMAL) {
            throw new BadRequest("运行接口不能使用正式判题用例");
        }
        if (testcaseType == AlgorithmTestcaseType.DEFAULT_RUN
                && (content.getRunTestCases() == null || content.getRunTestCases().isEmpty())
                && (content.getExamples() == null || content.getExamples().isEmpty())) {
            throw new BadRequest("题目未配置默认运行用例或题面样例");
        }

        AlgorithmJudgeJob job = AlgorithmJudgeJob.create(
                null,
                question.getId(),
                question.getAssessmentTimeId(),
                currentUser.getId(),
                command.language(),
                command.sourceCode(),
                testcaseType,
                command.customInput());
        algorithmJudgeJobRepository.save(job);
        algorithmJudgeJobPublisher.publish(job.getId(), testcaseType);
        return new AlgorithmJudgeResult.SubmitResult(job.getId(), null, testcaseType);
    }

    /**
     * 提交算法题。
     *
     * @param command
     *            提交命令
     * @return 提交结果
     */
    @Override
    @Transactional
    public AlgorithmJudgeResult.SubmitResult submit(AlgorithmJudgeCommands.SubmitCommand command) {
        User currentUser = getCurrentUser();
        AssessmentQuestion question = assessmentQuestionRepository.findById(command.questionId())
                .orElseThrow(() -> new DataNotFound("题目不存在，ID: " + command.questionId()));
        AlgorithmContent content = validateAlgorithmQuestion(question);
        validateCandidateCanUseQuestion(currentUser, question);
        validateLanguage(content, command.language());
        validateFormalLanguageLimit(question.getId(), command.language());
        if (command.content() == null || command.content().isBlank()) {
            throw new BadRequest("源代码不能为空");
        }

        AssessmentAnswer answer = saveOrUpdateAnswer(currentUser, question, command);
        AlgorithmJudgeJob job = AlgorithmJudgeJob.create(
                answer.getId(),
                question.getId(),
                question.getAssessmentTimeId(),
                currentUser.getId(),
                command.language(),
                command.content(),
                AlgorithmTestcaseType.FORMAL,
                null);
        algorithmJudgeJobRepository.save(job);
        algorithmJudgeJobPublisher.publish(job.getId(), AlgorithmTestcaseType.FORMAL);
        return new AlgorithmJudgeResult.SubmitResult(job.getId(), answer.getId(), AlgorithmTestcaseType.FORMAL);
    }

    /**
     * 根据ID查询判题任务。
     *
     * @param jobId
     *            判题任务ID
     * @return 判题任务结果
     */
    @Override
    public AlgorithmJudgeResult.PollResult getJob(Long jobId) {
        User currentUser = getCurrentUser();
        AlgorithmJudgeJob job = algorithmJudgeJobRepository.findById(jobId)
                .orElseThrow(() -> new DataNotFound("判题任务不存在"));
        if (!job.getUserId().equals(currentUser.getId())) {
            throw new Forbidden("无权查看该判题任务");
        }

        List<AlgorithmJudgeResult.CaseResult> caseResults = job.getStatus() == JudgeJobStatus.SUCCEEDED
                ? algorithmJudgeCaseResultRepository.findByJudgeJobId(jobId)
                        .stream()
                        // 正式提交只向考生返回失败用例；这里再过滤一次，兼容历史已保存的可见 AC 用例。
                        .filter(result -> Boolean.TRUE.equals(result.getVisibleToCandidate()))
                        .filter(
                                result -> job.getTestcaseType() != AlgorithmTestcaseType.FORMAL
                                        || result.getStatus() != JudgeCaseStatus.AC)
                        .map(this::toCaseResult)
                        .toList()
                : List.of();

        return new AlgorithmJudgeResult.PollResult(
                job.getId(),
                job.getTestcaseType(),
                job.getStatus(),
                job.getStatusMessage(),
                caseResults,
                findJudgement(job));
    }

    private User getCurrentUser() {
        User currentUser = UserCTX.getCurrentUser();
        if (currentUser == null) {
            throw new SecurityException("未登录");
        }
        return currentUser;
    }

    private AlgorithmContent validateAlgorithmQuestion(AssessmentQuestion question) {
        if (question.getQuestionType() != QuestionType.ALGORITHM
                || !(question.getContent()instanceof AlgorithmContent content)) {
            throw new BadRequest("题目不是算法题");
        }
        return content;
    }

    private void validateCandidateCanUseQuestion(User user, AssessmentQuestion question) {
        AssessmentTime time = assessmentTimeRepository.findById(question.getAssessmentTimeId())
                .orElseThrow(() -> new BadRequest("考核时间不存在"));
        if (user.getDirection() != null && !user.getDirection().equals(time.getDirection())) {
            throw new Forbidden("方向不匹配");
        }
        // 全局截止时间兜底校验，限时与非限时考核均适用
        if (time.getEndTime() != null && LocalDateTime.now().isAfter(time.getEndTime())) {
            throw new BadRequest("考核时间已结束，无法提交算法题");
        }
        // 限时考核额外校验个人会话截止时间
        if (Boolean.TRUE.equals(time.getTimeLimit())) {
            assessmentSessionRepository
                    .findByUserIdAndAssessmentTimeId(user.getId(), question.getAssessmentTimeId())
                    .ifPresent(session -> {
                        if (session.getDeadline() != null && LocalDateTime.now().isAfter(session.getDeadline())) {
                            throw new BadRequest("考核时间已到，无法提交算法题");
                        }
                    });
        }
    }

    private void validateLanguage(AlgorithmContent content, ProgrammingLanguage language) {
        if (language == null) {
            throw new BadRequest("编程语言不能为空");
        }
        Map<String, String> starterCode = content.getStarterCode();
        String languageKey = language.getValue();
        if (starterCode == null || !starterCode.containsKey(languageKey)) {
            throw new BadRequest("该题不支持提交语言：" + languageKey);
        }
    }

    private void validateFormalLanguageLimit(Long questionId, ProgrammingLanguage language) {
        int count = judgeLanguageLimitMapper.countConfirmedByQuestionIdAndLanguage(questionId, language.getValue());
        if (count == 0) {
            throw new BadRequest("该题当前语言尚未确认正式判题资源限制：" + language.getValue());
        }
    }

    private AssessmentAnswer saveOrUpdateAnswer(
            User user,
            AssessmentQuestion question,
            AlgorithmJudgeCommands.SubmitCommand command) {
        Optional<AssessmentAnswer> existing = assessmentAnswerRepository
                .findByUserIdAndQuestionId(user.getId(), question.getId());
        if (existing.isPresent()) {
            AssessmentAnswer answer = existing.get();
            answer.setContent(command.content());
            answer.setLanguage(command.language());
            answer.setSubmitTime(LocalDateTime.now());
            assessmentAnswerRepository.update(answer);
            return assessmentAnswerRepository.findById(answer.getId())
                    .orElseThrow(() -> new GlobalException("更新算法题答案后查询失败"));
        }

        AssessmentAnswer answer = AssessmentAnswer.create(
                user.getId(),
                question.getId(),
                command.content(),
                command.language(),
                null);
        assessmentAnswerRepository.save(answer);
        return answer;
    }

    private AlgorithmJudgeResult.CaseResult toCaseResult(AlgorithmJudgeCaseResult result) {
        return new AlgorithmJudgeResult.CaseResult(
                result.getCaseNo(),
                result.getTestcaseType(),
                result.getStatus(),
                result.getInput(),
                result.getExpectedOutput(),
                result.getActualOutput(),
                result.getStdout(),
                result.getStderr(),
                result.getTimeUsedMs(),
                result.getMemoryUsedKb(),
                result.getMessage());
    }

    private AlgorithmJudgeResult.JudgementInfo findJudgement(AlgorithmJudgeJob job) {
        if (job.getAnswerId() == null || job.getStatus() != JudgeJobStatus.SUCCEEDED) {
            return null;
        }
        try {
            AssessmentJudgementVO judgement = assessmentJudgementDomainService.getLatestByAnswerId(job.getAnswerId());
            return new AlgorithmJudgeResult.JudgementInfo(
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
        } catch (DataNotFound ignored) {
            return null;
        }
    }
}
