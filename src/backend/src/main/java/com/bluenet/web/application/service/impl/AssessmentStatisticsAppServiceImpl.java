package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.result.assessment.AssessmentStatisticsResult;
import com.bluenet.web.application.service.AssessmentQuestionAppService;
import com.bluenet.web.application.service.AssessmentStatisticsAppService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.enumerate.ObjectiveResultCode;
import com.bluenet.web.domain.model.enumerate.QuestionType;
import com.bluenet.web.domain.model.entity.AssessmentQuestion;
import com.bluenet.web.domain.model.entity.AssessmentJudgement;
import com.bluenet.web.domain.repository.AssessmentJudgementRepository;
import com.bluenet.web.domain.repository.AssessmentQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 考核统计应用服务实现。
 * <p>实现考核统计聚合在应用层的业务逻辑编排。</p>
 */
/**
 * 评测统计应用服务实现。
 * <p>
 * 实现评测统计聚合在应用层的业务逻辑编排。
 * </p>
 */
@Service
@RequiredArgsConstructor
public class AssessmentStatisticsAppServiceImpl implements AssessmentStatisticsAppService {
    private final AssessmentQuestionRepository assessmentQuestionRepository;
    private final AssessmentQuestionAppService assessmentQuestionAppService;
    private final AssessmentJudgementRepository assessmentJudgementRepository;

    @Value("${bluenet.assessment-statistics.candidate-visible:false}")
    private boolean candidateStatisticsVisible;

    /**
     * 获取题目统计信息。
     *
     * @param questionId
     *            题目ID
     * @return 题目统计结果
     */
    /**
     * 查询题目统计信息。
     *
     * @param questionId
     *            题目ID
     * @return 题目统计结果
     */
    @Override
    public AssessmentStatisticsResult getQuestionStatistics(Long questionId) {
        AssessmentQuestion question = assessmentQuestionRepository.findById(questionId)
                .orElseThrow(() -> new DataNotFound("题目不存在，ID: " + questionId));
        return buildQuestionStatistics(questionId, question.getQuestionType());
    }

    /**
     * 获取考生题目统计信息。
     *
     * @param questionId
     *            题目ID
     * @return 考生题目统计结果
     */
    /**
     * 查询考生端题目统计信息。
     *
     * @param questionId
     *            题目ID
     * @return 题目统计结果
     */
    @Override
    public AssessmentStatisticsResult getCandidateQuestionStatistics(Long questionId) {
        if (!candidateStatisticsVisible) {
            throw new BadRequest("考生端题目通过率展示未开启");
        }
        // Reuse the candidate question detail guard so aggregate statistics follow the
        // same scope as the page.
        assessmentQuestionAppService.getQuestionDetailForUser(questionId);
        AssessmentQuestion question = assessmentQuestionRepository.findById(questionId)
                .orElseThrow(() -> new DataNotFound("题目不存在，ID: " + questionId));
        return buildQuestionStatistics(questionId, question.getQuestionType());
    }

    private AssessmentStatisticsResult buildQuestionStatistics(Long questionId, QuestionType questionType) {
        if (!isObjectiveQuestion(questionType)) {
            throw new BadRequest("文件上传题不参与客观题通过率统计");
        }

        List<AssessmentJudgement> latestJudgements = assessmentJudgementRepository
                .findLatestObjectiveByQuestionId(questionId);
        Map<ObjectiveResultCode, Long> distribution = initDistribution(questionType);
        for (AssessmentJudgement judgement : latestJudgements) {
            ObjectiveResultCode resultCode = judgement.getResultCode();
            if (resultCode != null && distribution.containsKey(resultCode)) {
                distribution.compute(resultCode, (key, count) -> count == null ? 1L : count + 1L);
            }
        }

        long submittedCount = latestJudgements.size();
        long acceptedCount = distribution.getOrDefault(ObjectiveResultCode.AC, 0L);
        return new AssessmentStatisticsResult(
                questionId,
                questionType,
                submittedCount,
                acceptedCount,
                calculatePassRate(acceptedCount, submittedCount),
                distribution);
    }

    private boolean isObjectiveQuestion(QuestionType questionType) {
        return questionType == QuestionType.SINGLE_CHOICE
                || questionType == QuestionType.MULTIPLE_CHOICE
                || questionType == QuestionType.ALGORITHM;
    }

    private Map<ObjectiveResultCode, Long> initDistribution(QuestionType questionType) {
        Map<ObjectiveResultCode, Long> distribution = new EnumMap<>(ObjectiveResultCode.class);
        distribution.put(ObjectiveResultCode.AC, 0L);
        distribution.put(ObjectiveResultCode.WA, 0L);
        if (questionType == QuestionType.ALGORITHM) {
            distribution.put(ObjectiveResultCode.TLE, 0L);
            distribution.put(ObjectiveResultCode.RE, 0L);
            distribution.put(ObjectiveResultCode.CE, 0L);
            distribution.put(ObjectiveResultCode.MLE, 0L);
        }
        return distribution;
    }

    private BigDecimal calculatePassRate(long acceptedCount, long submittedCount) {
        if (submittedCount == 0) {
            return BigDecimal.ZERO;
        }
        // 固定四位小数，前端可自行转成百分比展示。
        return BigDecimal.valueOf(acceptedCount)
                .divide(BigDecimal.valueOf(submittedCount), 4, RoundingMode.HALF_UP);
    }
}
