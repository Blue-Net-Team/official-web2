package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.assessment_statistics.QuestionStatisticsDTO;
import com.bluenet.web.application.service.AssessmentQuestionService;
import com.bluenet.web.application.service.AssessmentStatisticsService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.model.enumerate.ObjectiveResultCode;
import com.bluenet.web.domain.model.enumerate.QuestionType;
import com.bluenet.web.domain.model.vo.AssessmentJudgementVO;
import com.bluenet.web.domain.model.vo.AssessmentQuestionVO;
import com.bluenet.web.domain.repository.AssessmentJudgementRepository;
import com.bluenet.web.domain.service.AssessmentQuestionDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AssessmentStatisticsServiceImpl implements AssessmentStatisticsService {
    private final AssessmentQuestionDomainService assessmentQuestionDomainService;
    private final AssessmentQuestionService assessmentQuestionService;
    private final AssessmentJudgementRepository assessmentJudgementRepository;

    @Value("${bluenet.assessment-statistics.candidate-visible:false}")
    private boolean candidateStatisticsVisible;

    @Override
    public QuestionStatisticsDTO getQuestionStatistics(Long questionId) {
        AssessmentQuestionVO question = assessmentQuestionDomainService.getQuestionById(questionId);
        return buildQuestionStatistics(questionId, question.getQuestionType());
    }

    @Override
    public QuestionStatisticsDTO getCandidateQuestionStatistics(Long questionId) {
        if (!candidateStatisticsVisible) {
            throw new BadRequest("考生端题目通过率展示未开启");
        }
        // Reuse the candidate question detail guard so aggregate statistics follow the
        // same scope as the page.
        assessmentQuestionService.getQuestionDetailForUser(questionId);
        AssessmentQuestionVO question = assessmentQuestionDomainService.getQuestionById(questionId);
        return buildQuestionStatistics(questionId, question.getQuestionType());
    }

    private QuestionStatisticsDTO buildQuestionStatistics(Long questionId, QuestionType questionType) {
        if (!isObjectiveQuestion(questionType)) {
            throw new BadRequest("文件上传题不参与客观题通过率统计");
        }

        List<AssessmentJudgementVO> latestJudgements = assessmentJudgementRepository
                .findLatestObjectiveByQuestionId(questionId);
        Map<ObjectiveResultCode, Long> distribution = initDistribution(questionType);
        for (AssessmentJudgementVO judgement : latestJudgements) {
            ObjectiveResultCode resultCode = judgement.getResultCode();
            if (resultCode != null && distribution.containsKey(resultCode)) {
                distribution.compute(resultCode, (key, count) -> count == null ? 1L : count + 1L);
            }
        }

        long submittedCount = latestJudgements.size();
        long acceptedCount = distribution.getOrDefault(ObjectiveResultCode.AC, 0L);
        return QuestionStatisticsDTO.builder()
                .questionId(questionId)
                .questionType(questionType)
                .submittedCount(submittedCount)
                .acceptedCount(acceptedCount)
                .passRate(calculatePassRate(acceptedCount, submittedCount))
                .resultDistribution(distribution)
                .build();
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
