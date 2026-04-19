package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.algorithm_judge.AlgorithmRunRequestDTO;
import com.bluenet.web.api.dto.algorithm_judge.AlgorithmSubmitResponseDTO;
import com.bluenet.web.api.dto.algorithm_judge.JudgeCaseResultDTO;
import com.bluenet.web.api.dto.algorithm_judge.JudgeJobPollingResponseDTO;
import com.bluenet.web.api.dto.assessment_answer.CreateAnswerRequestDTO;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentJudgementDTO;
import com.bluenet.web.application.service.AlgorithmJudgeService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.exception.GlobalException;
import com.bluenet.web.domain.model.entity.AlgorithmJudgeJob;
import com.bluenet.web.domain.model.enumerate.AlgorithmTestcaseType;
import com.bluenet.web.domain.model.enumerate.JudgeCaseStatus;
import com.bluenet.web.domain.model.enumerate.JudgeJobStatus;
import com.bluenet.web.domain.model.enumerate.ProgrammingLanguage;
import com.bluenet.web.domain.model.enumerate.QuestionType;
import com.bluenet.web.domain.model.vo.AlgorithmJudgeCaseResultVO;
import com.bluenet.web.domain.model.vo.AlgorithmJudgeJobVO;
import com.bluenet.web.domain.model.vo.AssessmentAnswerVO;
import com.bluenet.web.domain.model.vo.AssessmentJudgementVO;
import com.bluenet.web.domain.model.vo.AssessmentQuestionVO;
import com.bluenet.web.domain.model.vo.AssessmentTimeVO;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.model.vo.evaluation.AlgorithmContent;
import com.bluenet.web.domain.repository.AlgorithmJudgeCaseResultRepository;
import com.bluenet.web.domain.repository.AlgorithmJudgeJobRepository;
import com.bluenet.web.domain.repository.AssessmentAnswerRepository;
import com.bluenet.web.domain.repository.AssessmentSessionRepository;
import com.bluenet.web.domain.service.AssessmentAnswerDomainService;
import com.bluenet.web.domain.service.AssessmentJudgementDomainService;
import com.bluenet.web.domain.service.AssessmentQuestionDomainService;
import com.bluenet.web.domain.service.AssessmentTimeDomainService;
import com.bluenet.web.infrastructure.judge.AlgorithmJudgeJobPublisher;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AlgorithmJudgeServiceImpl implements AlgorithmJudgeService {
    private static final int DEFAULT_MAX_RETRY_COUNT = 3;

    private final AssessmentQuestionDomainService assessmentQuestionDomainService;
    private final AssessmentTimeDomainService assessmentTimeDomainService;
    private final AssessmentAnswerDomainService assessmentAnswerDomainService;
    private final AssessmentJudgementDomainService assessmentJudgementDomainService;
    private final AssessmentAnswerRepository assessmentAnswerRepository;
    private final AssessmentSessionRepository assessmentSessionRepository;
    private final AlgorithmJudgeJobRepository algorithmJudgeJobRepository;
    private final AlgorithmJudgeCaseResultRepository algorithmJudgeCaseResultRepository;
    private final AlgorithmJudgeJobPublisher algorithmJudgeJobPublisher;

    @Override
    @Transactional
    public AlgorithmSubmitResponseDTO run(AlgorithmRunRequestDTO request) {
        UserVO currentUser = getCurrentUser();
        AssessmentQuestionVO question = assessmentQuestionDomainService.getQuestionById(request.getQuestionId());
        AlgorithmContent content = validateAlgorithmQuestion(question);
        validateCandidateCanUseQuestion(currentUser, question);
        validateLanguage(content, request.getLanguage());

        AlgorithmTestcaseType testcaseType = request.getTestcaseType() == null
                ? AlgorithmTestcaseType.DEFAULT_RUN
                : request.getTestcaseType();
        if (testcaseType == AlgorithmTestcaseType.FORMAL) {
            throw new BadRequest("运行接口不能使用正式判题用例");
        }
        if (testcaseType == AlgorithmTestcaseType.DEFAULT_RUN
                && (content.getRunTestCases() == null || content.getRunTestCases().isEmpty())) {
            throw new BadRequest("题目未配置默认运行用例");
        }

        AlgorithmJudgeJob job = createJob(
                null,
                question,
                currentUser,
                request.getLanguage(),
                request.getSourceCode(),
                testcaseType,
                request.getCustomInput());
        algorithmJudgeJobRepository.save(job);
        algorithmJudgeJobPublisher.publish(job.getId(), testcaseType);
        return AlgorithmSubmitResponseDTO.builder()
                .judgeJobId(job.getId())
                .testcaseType(testcaseType)
                .build();
    }

    @Override
    @Transactional
    public AlgorithmSubmitResponseDTO submit(CreateAnswerRequestDTO request) {
        UserVO currentUser = getCurrentUser();
        AssessmentQuestionVO question = assessmentQuestionDomainService.getQuestionById(request.getQuestionId());
        AlgorithmContent content = validateAlgorithmQuestion(question);
        validateCandidateCanUseQuestion(currentUser, question);
        validateLanguage(content, request.getLanguage());
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new BadRequest("源代码不能为空");
        }

        AssessmentAnswerVO answer = saveOrUpdateAnswer(currentUser, question, request);
        AlgorithmJudgeJob job = createJob(
                answer.getId(),
                question,
                currentUser,
                request.getLanguage(),
                request.getContent(),
                AlgorithmTestcaseType.FORMAL,
                null);
        algorithmJudgeJobRepository.save(job);
        algorithmJudgeJobPublisher.publish(job.getId(), AlgorithmTestcaseType.FORMAL);
        return AlgorithmSubmitResponseDTO.builder()
                .answerId(answer.getId())
                .judgeJobId(job.getId())
                .testcaseType(AlgorithmTestcaseType.FORMAL)
                .build();
    }

    @Override
    public JudgeJobPollingResponseDTO getJob(Long jobId) {
        UserVO currentUser = getCurrentUser();
        AlgorithmJudgeJobVO job = algorithmJudgeJobRepository.findById(jobId)
                .orElseThrow(() -> new DataNotFound("判题任务不存在"));
        if (!job.getUserId().equals(currentUser.getId())) {
            throw new Forbidden("无权查看该判题任务");
        }

        List<JudgeCaseResultDTO> caseResults = job.getStatus() == JudgeJobStatus.SUCCEEDED
                ? algorithmJudgeCaseResultRepository.findByJudgeJobId(jobId)
                        .stream()
                        // 正式提交只向考生返回失败用例；这里再过滤一次，兼容历史已保存的可见 AC 用例。
                        .filter(result -> Boolean.TRUE.equals(result.getVisibleToCandidate()))
                        .filter(
                                result -> job.getTestcaseType() != AlgorithmTestcaseType.FORMAL
                                        || result.getStatus() != JudgeCaseStatus.AC)
                        .map(this::convertToDTO)
                        .toList()
                : List.of();

        return JudgeJobPollingResponseDTO.builder()
                .judgeJobId(job.getId())
                .testcaseType(job.getTestcaseType())
                .status(job.getStatus())
                .statusMessage(job.getStatusMessage())
                .caseResults(caseResults)
                .judgement(findJudgement(job))
                .build();
    }

    private UserVO getCurrentUser() {
        UserVO currentUser = UserCTX.getCurrentUser();
        if (currentUser == null) {
            throw new SecurityException("未登录");
        }
        return currentUser;
    }

    private AlgorithmContent validateAlgorithmQuestion(AssessmentQuestionVO question) {
        if (question.getQuestionType() != QuestionType.ALGORITHM
                || !(question.getContent()instanceof AlgorithmContent content)) {
            throw new BadRequest("题目不是算法题");
        }
        return content;
    }

    private void validateCandidateCanUseQuestion(UserVO user, AssessmentQuestionVO question) {
        AssessmentTimeVO timeVO = assessmentTimeDomainService.getById(question.getAssessmentTimeId())
                .orElseThrow(() -> new BadRequest("考核时间不存在"));
        if (user.getDirection() != null && !user.getDirection().equals(timeVO.getDirection())) {
            throw new Forbidden("方向不匹配");
        }
        // 限时考核复用答题提交的截止时间判断，避免运行/提交绕过考试时间。
        if (Boolean.TRUE.equals(timeVO.getTimeLimit())) {
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

    private AssessmentAnswerVO saveOrUpdateAnswer(
            UserVO user,
            AssessmentQuestionVO question,
            CreateAnswerRequestDTO request) {
        Optional<AssessmentAnswerVO> existing = assessmentAnswerRepository
                .findByUserIdAndQuestionId(user.getId(), question.getId());
        if (existing.isPresent()) {
            AssessmentAnswerVO answer = existing.get();
            assessmentAnswerRepository.updateContent(answer.getId(), request.getContent());
            assessmentAnswerRepository.updateLanguage(answer.getId(), request.getLanguage());
            assessmentAnswerRepository.updateSubmitTime(answer.getId(), LocalDateTime.now());
            return assessmentAnswerRepository.findById(answer.getId())
                    .orElseThrow(() -> new GlobalException("更新算法题答案后查询失败"));
        }

        AssessmentAnswerVO answer = AssessmentAnswerVO.builder()
                .userId(user.getId())
                .questionId(question.getId())
                .content(request.getContent())
                .language(request.getLanguage())
                .build();
        return assessmentAnswerDomainService.createAnswer(answer);
    }

    private AlgorithmJudgeJob createJob(
            Long answerId,
            AssessmentQuestionVO question,
            UserVO user,
            ProgrammingLanguage language,
            String sourceCode,
            AlgorithmTestcaseType testcaseType,
            String customInput) {
        AlgorithmJudgeJob job = new AlgorithmJudgeJob();
        job.setAnswerId(answerId);
        job.setQuestionId(question.getId());
        job.setAssessmentTimeId(question.getAssessmentTimeId());
        job.setUserId(user.getId());
        job.setLanguage(language);
        job.setSourceCode(sourceCode);
        job.setTestcaseType(testcaseType);
        job.setCustomInput(customInput);
        job.setStatus(JudgeJobStatus.PENDING);
        job.setRetryCount(0);
        job.setMaxRetryCount(DEFAULT_MAX_RETRY_COUNT);
        job.setStatusMessage("等待判题");
        return job;
    }

    private JudgeCaseResultDTO convertToDTO(AlgorithmJudgeCaseResultVO result) {
        return JudgeCaseResultDTO.builder()
                .caseNo(result.getCaseNo())
                .testcaseType(result.getTestcaseType())
                .status(result.getStatus())
                .input(result.getInput())
                .expectedOutput(result.getExpectedOutput())
                .actualOutput(result.getActualOutput())
                .stdout(result.getStdout())
                .stderr(result.getStderr())
                .timeUsedMs(result.getTimeUsedMs())
                .memoryUsedKb(result.getMemoryUsedKb())
                .message(result.getMessage())
                .build();
    }

    private AssessmentJudgementDTO findJudgement(AlgorithmJudgeJobVO job) {
        if (job.getAnswerId() == null || job.getStatus() != JudgeJobStatus.SUCCEEDED) {
            return null;
        }
        try {
            AssessmentJudgementVO judgement = assessmentJudgementDomainService.getLatestByAnswerId(job.getAnswerId());
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
        } catch (DataNotFound ignored) {
            return null;
        }
    }
}
